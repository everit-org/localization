/**
 * This file is part of Everit - Localization.
 *
 * Everit - Localization is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Localization is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Localization.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.localization.internal;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.cache.api.CacheConfiguration;
import org.everit.osgi.cache.api.CacheFactory;
import org.everit.osgi.cache.api.CacheHolder;
import org.everit.osgi.localization.api.ErrorCode;
import org.everit.osgi.localization.api.LocalizationException;
import org.everit.osgi.localization.api.LocalizationService;
import org.everit.osgi.localization.api.dto.LocalizedData;
import org.everit.osgi.localization.schema.QLocalizedData;
import org.everit.osgi.querydsl.support.QuerydslSupport;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.log.LogService;

import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import com.mysema.query.types.ConstructorExpression;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.Coalesce;
import com.mysema.query.types.query.StringSubQuery;

/**
 * Implementation for {@link LocalizationService}.
 */
@Component(name = "org.everit.osgi.localization.LocalizationComponent", metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({ @Property(name = "dataSource.target"), @Property(name = "cacheFactory.target"),
        @Property(name = "logService.target") })
@Service
public class LocalizationComponent implements LocalizationService {

    /**
     * {@link CacheConfiguration}.
     */
    @Reference
    private CacheConfiguration<String, Map<Locale, LocalizedData>> cacheConfiguration;

    /**
     * {@link CacheFactory}.
     */
    @Reference
    private CacheFactory cacheFactory;
    /**
     * {@link CacheHolder}.
     */
    private CacheHolder<String, Map<Locale, LocalizedData>> cacheHolder;
    /**
     * {@link DataSource}.
     */
    @Reference
    private DataSource dataSource;
    /**
     * Caching that is used in front of the database. The localeCache is holding all the locals which are stored in the
     * database. When new data are stored in the DB the cache reloads all the locals from DB again.
     */
    private List<Locale> localeCache = new ArrayList<Locale>();
    /**
     * Caching that is used in front of the entity manager. The localizedDataCache is based on the key of the localized
     * data. When a new localized data is created or an existing is updated based on a key all of the localized data
     * will be deleted belonging to the specified key.
     */
    private ConcurrentMap<String, Map<Locale, LocalizedData>> localizedDataCache;
    /**
     * {@link LogService}.
     */
    @Reference
    private LogService logService;
    @Reference
    private QuerydslSupport querydslSupport;
    /**
     * {@link TransactionHelper}.
     */
    @Reference
    private TransactionHelper t;

    @Activate
    public void activate(final BundleContext context) {
        BundleWiring bundleWiring = context.getBundle().adapt(BundleWiring.class);
        ClassLoader classLoader = bundleWiring.getClassLoader();
        cacheHolder = cacheFactory.createCache(cacheConfiguration, classLoader);
        localizedDataCache = cacheHolder.getCache();
    }

    public void bindCacheFactory(final CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    public void bindDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void bindLogService(final LogService logService) {
        this.logService = logService;
    }

    public void bindQuerydslSupport(QuerydslSupport querydslSupport) {
        this.querydslSupport = querydslSupport;
    }

    @Override
    public void clearCache() {
        localizedDataCache.clear();
        localeCache.clear();
    }

    /**
     * Convert String to Locale.
     *
     * @param localeString
     *            The string what have to convert.
     * @return The new Locale or null if conversion fails.
     */
    private Locale convertStringToLocale(final String localeString) {
        if (localeString == null) {
            return null;
        }
        String[] localeParts = localeString.split("_");
        String language = "";
        String country = "";
        String variant = "";
        if ((localeParts.length > 0) && !"".equals(localeParts[0])) {
            language = localeParts[0];
            if ((localeParts.length > 1) && !"".equals(localeParts[1])) {
                country = localeParts[1];
                if ((localeParts.length > 2) && !"".equals(localeParts[2])) {
                    variant = localeParts[2];
                }
            }
            return new Locale(language, country, variant);
        }
        return null;
    }

    @Override
    public LocalizedData createLocalizedData(final String key, final Locale locale, final String value,
            final boolean defaultLocale) {

        return querydslSupport.execute((connection, configuration) -> {
            String defaultLocaleStr = getDefaultLocaleByKey(key);
            if (!"".equals(defaultLocaleStr) && defaultLocale) {
                throw new LocalizationException(ErrorCode.DEFAULT_VALUE);
            }

            QLocalizedData localizedData = new QLocalizedData("qLocalizedData");
            SQLInsertClause insertClause = new SQLInsertClause(connection, configuration, localizedData);
            insertClause.set(localizedData.defaultLocale, defaultLocale)
                    .set(localizedData.key_, key)
                    .set(localizedData.locale_, locale.toString())
                    .set(localizedData.value_, value)
                    .executeWithKey(localizedData.localizedDataId);
            return new LocalizedData(key, locale, defaultLocale, value);
        });
    }

    @Deactivate
    public void deactivate() {
        cacheHolder.close();
    }

    private List<LocalizedData> findLocalizedDataByKeyFromDataBase(final String key) {
        return querydslSupport.execute((connection, configuration) -> {
            QLocalizedData localizedData = QLocalizedData.localizedData;
            SQLQuery selectClause = new SQLQuery(connection, configuration);
            selectClause.from(localizedData);
            selectClause.where(localizedData.key_.eq(key));
            return selectClause.list((ConstructorExpression.create(LocalizedData.class,
                    localizedData.key_,
                    localizedData.locale_,
                    localizedData.defaultLocale,
                    localizedData.value_)));
        });
    }

    @Override
    public List<Locale> getAvailableLocales() {
        if (localeCache.isEmpty()) {
            List<Locale> availableLocales = new ArrayList<Locale>();
            List<String> tempLocals = getAvailableLocals();
            for (String locString : tempLocals) {
                availableLocales.add(convertStringToLocale(locString));
            }
            localeCache = Collections.unmodifiableList(availableLocales);
        }
        return localeCache;
    }

    /**
     * Reads all the locals in the database.
     *
     * @return {@link List} of locale strings.
     */
    public List<String> getAvailableLocals() {
        return querydslSupport.execute((connection, configuration) -> {
            QLocalizedData localizedData = new QLocalizedData("qLocalizedData");
            SQLQuery selectClause = new SQLQuery(connection, configuration);
            return selectClause.from(localizedData).distinct().list(localizedData.locale_);
        });
    }

    /**
     * Finds the default locale for a given key.
     *
     * @param key
     *            The key of the localized data.
     * @return String value of the default locale for the localizedData found by the given key.
     */
    private String getDefaultLocaleByKey(final String key) {
        return querydslSupport.execute((connection, configuration) -> {
            QLocalizedData localizedData = new QLocalizedData("qLocalizedData");
            SQLQuery query = new SQLQuery(connection, configuration);

            query.from(localizedData);
            query.where(localizedData.key_.eq(key), localizedData.defaultLocale.eq(true)).count();
            List<String> list = query.list(localizedData.locale_);
            if (list.size() > 1) {
                throw new LocalizationException(ErrorCode.DEFAULT_VALUE);
            } else if (list.size() == 1) {
                return list.get(0);
            } else {
                return key;
            }
        });
    }

    @Override
    public LocalizedData getLocalizedDataByKey(final String key, final Locale locale) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null.");
        }
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null.");
        }

        Map<Locale, LocalizedData> localizedDataMap = getLocalizedDataMap(key);
        if (localizedDataMap == null) {
            return null;
        }
        LocalizedData localizedData = localizedDataMap.get(locale);
        if (localizedData == null) {
            if (locale.getVariant() != "") {
                String localeString = locale.getLanguage() + locale.getCountry();
                localizedData = localizedDataMap.get(convertStringToLocale(localeString));
            }
            if ((localizedData == null) && (locale.getCountry() != "")) {
                String localeString = locale.getLanguage();
                localizedData = localizedDataMap.get(convertStringToLocale(localeString));
            }
            if (localizedData == null) {
                for (LocalizedData tmpLocalizedData : localizedDataMap.values()) {
                    if (tmpLocalizedData.isDefaultLocale()) {
                        return tmpLocalizedData;
                    }
                }
            }
        }
        return localizedData;
    }

    private Map<Locale, LocalizedData> getLocalizedDataMap(final String key) {
        Map<Locale, LocalizedData> localizedDataMap = localizedDataCache.get(key);
        if (localizedDataMap == null) {
            localizedDataMap = getLocalizedDataMapByKeyFromDataBase(key);
        }
        return localizedDataMap;
    }

    /**
     * Returns the localized data records from database that belong to a key. Refresh the localizedDataCache.
     *
     * @param key
     *            The key of data.
     * @return The Map of found data.
     */
    private Map<Locale, LocalizedData> getLocalizedDataMapByKeyFromDataBase(final String key) {
        List<LocalizedData> localizedDataList = findLocalizedDataByKeyFromDataBase(key);
        if ((localizedDataList == null) || localizedDataList.isEmpty()) {
            return null;
        }
        Map<Locale, LocalizedData> localizedDataMap = new HashMap<Locale, LocalizedData>();
        for (LocalizedData localizedData : localizedDataList) {
            localizedDataMap.put(localizedData.getLocale(), localizedData);
        }
        localizedDataCache.put(key, localizedDataMap);
        return localizedDataMap;
    }

    @Override
    public Map<Locale, LocalizedData> getLocalizedDataMapClone(final String key) {
        Map<Locale, LocalizedData> localizedDataMap = getLocalizedDataMap(key);
        if (localizedDataMap == null) {
            return null;
        }

        Map<Locale, LocalizedData> clonedLocalizedDataMap = new HashMap<Locale, LocalizedData>();
        for (Locale locale : localizedDataMap.keySet()) {
            LocalizedData localizedData = localizedDataMap.get(locale);
            clonedLocalizedDataMap.put(locale, localizedData);
        }
        return clonedLocalizedDataMap;
    }

    @Override
    public Expression<String> getLocalizedValue(final Path<String> localizationKey, final Locale locale) {
        StringSubQuery localizedQuery = getLocalizedValueSubQuery(localizationKey, locale);
        StringSubQuery defaultQuery = getLocalizedValueSubQuery(localizationKey, null);
        return new Coalesce<String>(String.class).add(localizedQuery).add(defaultQuery).add(localizationKey);
    }

    private StringSubQuery getLocalizedValueSubQuery(final Path<String> localizationKey, final Locale locale) {
        QLocalizedData localizedData = new QLocalizedData(QLocalizedData.localizedData);
        SQLSubQuery subQuery = new SQLSubQuery().from(localizedData);
        BooleanExpression predicate = localizedData.key_.eq(localizationKey);
        if (locale != null) {
            predicate = predicate.and(localizedData.locale_.eq(locale.toString()));
        } else {
            predicate = predicate.and(localizedData.defaultLocale.eq(Boolean.TRUE));
        }
        return subQuery.where(predicate).unique(localizedData.value_);
    }

    /**
     * Pessimistic locking on the record matching the given parameters.
     *
     * @param connection
     *            {@link Connection}.
     * @param localizedData
     *            {@link QLocalizedData}.
     * @param key
     *            Key of LocalizedData to be locked.
     * @param locale
     *            Locale of LocalizedData to be locked.
     * @return {@link List} of {@link LocalizedData} which are lock
     */
    private List<LocalizedData> getLockOnLocalizedData(final Connection connection, final QLocalizedData localizedData,
            final String key, final Locale locale, final Configuration configuration) {
        SQLQuery query = new SQLQuery(connection, configuration);
        query.from(localizedData);
        query.where(localizedData.key_.eq(key).and(localizedData.locale_.eq(locale.toString())));
        List<LocalizedData> result = query.forUpdate().list((ConstructorExpression.create(LocalizedData.class,
                localizedData.key_,
                localizedData.locale_,
                localizedData.defaultLocale,
                localizedData.value_)));
        return result;
    }

    @Override
    public Collection<Locale> getSupportedLocalesByKey(final String key) {
        Map<Locale, LocalizedData> localizedDataMaps = getLocalizedDataMap(key);
        Set<Locale> cloneSupportedLocales = new HashSet<Locale>();
        Set<Locale> supportedLocales = localizedDataMaps.keySet();
        for (Locale l : supportedLocales) {
            cloneSupportedLocales.add(l);
        }
        return cloneSupportedLocales;
    }

    @Override
    public long removeLocalizedData(final String key, final Locale locale) {
        Map<Locale, LocalizedData> localizedDataMap = getLocalizedDataMap(key);
        LocalizedData removeData = localizedDataMap.get(locale);
        if (removeData == null) {
            return 0;
        }
        if (removeData.isDefaultLocale() && (localizedDataMap.size() > 1)) {
            throw new IllegalArgumentException(ErrorCode.DEFAULT_LOCALE_CANNOT_BE_REMOVED);
        }

        return querydslSupport.execute((connection, configuration) -> {
            QLocalizedData localizedData = new QLocalizedData("qLocalizedData");
            SQLDeleteClause deleteClause = new SQLDeleteClause(connection, configuration, localizedData);
            deleteClause.where(localizedData.key_.eq(key).and(localizedData.locale_.eq(locale.toString())));
            localizedDataCache.remove(key);
            return deleteClause.execute();
        });
    }

    @Override
    public long updateLocalizedData(final String key, final Locale locale, final String value,
            final boolean defaultLocale) {
        return t.required(() -> {
            return (Long) querydslSupport.execute((connection, configuration) -> {
                QLocalizedData localizedData = new QLocalizedData("qLocalizedData");
                List<LocalizedData> list =
                        getLockOnLocalizedData(connection, localizedData, key, locale, configuration);
                if (list.size() == 0) {
                    return 0;
                }

                if ((list.get(0).isDefaultLocale() != defaultLocale)
                        && (list.get(0).isDefaultLocale() || defaultLocale)) {
                    throw new LocalizationException(ErrorCode.DEFAULT_VALUE);
                }

                SQLUpdateClause updateClause = new SQLUpdateClause(connection, configuration, localizedData);
                updateClause.where(localizedData.key_.eq(key).and(localizedData.locale_.eq(locale.toString())));
                updateClause.set(localizedData.defaultLocale, defaultLocale);
                updateClause.set(localizedData.value_, value);
                localizedDataCache.remove(key);
                return updateClause.execute();
            });
        });
    }

    @Override
    public void updateLocalizedDataMap(final Map<String, LocalizedData> localizedDataMap) {
        if (localizedDataMap == null) {
            throw new IllegalArgumentException("localizedDataMap map cannot be null.");
        }
        String defaultLocalizedDataKey = "";
        for (String key : localizedDataMap.keySet()) {
            if (localizedDataMap.get(key).isDefaultLocale()) {
                defaultLocalizedDataKey = key;
                LocalizedData localizedData = localizedDataMap.get(key);
                updateLocalizedData(localizedData.getKey(), localizedData.getLocale(), localizedData.getValue(),
                        localizedData.isDefaultLocale());
            }
        } // Because we can not remove the defaultLocale.
        for (String key : localizedDataMap.keySet()) {
            if (!key.equals(defaultLocalizedDataKey)) {
                LocalizedData localizedData = localizedDataMap.get(key);
                updateLocalizedData(localizedData.getKey(), localizedData.getLocale(), localizedData.getValue(),
                        localizedData.isDefaultLocale());
            }
        }
    }
}
