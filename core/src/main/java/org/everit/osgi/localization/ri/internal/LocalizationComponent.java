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
package org.everit.osgi.localization.ri.internal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

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
import org.everit.osgi.localization.Localization;
import org.everit.osgi.localization.schema.qdsl.QDefaultLocale;
import org.everit.osgi.localization.schema.qdsl.QLocalizedData;
import org.everit.osgi.localization.schema.qdsl.util.LocalizationQdslUtil;
import org.everit.osgi.querydsl.support.QuerydslSupport;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.log.LogService;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.Coalesce;

/**
 * Implementation for {@link Localization}.
 */
@Component(name = "org.everit.osgi.localization.Localization", metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({ @Property(name = "querydslSupport.target"), @Property(name = "cacheFactory.target"),
        @Property(name = "cacheConfiguration.target"), @Property(name = "transactionHelper.target"),
        @Property(name = "logService.target"),
        @Property(name = Constants.SERVICE_DESCRIPTION, propertyPrivate = false) })
@Service
public class LocalizationComponent implements Localization, LocalizationQdslUtil {

    /**
     * Caching that is used in front of the entity manager. The localizedDataCache is based on the key of the localized
     * data. When a new localized data is created or an existing is updated based on a key all of the localized data
     * will be deleted belonging to the specified key.
     */
    private ConcurrentMap<String, LocalizedValue[]> cache;
    /**
     * {@link CacheConfiguration}.
     */
    @Reference(bind = "setCacheConfiguration")
    private CacheConfiguration<String, LocalizedValue[]> cacheConfiguration;

    /**
     * {@link CacheFactory}.
     */
    @Reference(bind = "setCacheFactory")
    private CacheFactory cacheFactory;

    /**
     * {@link CacheHolder}.
     */
    private CacheHolder<String, LocalizedValue[]> cacheHolder;

    /**
     * {@link LogService}.
     */
    @Reference(bind = "setLogService")
    private LogService logService;

    @Reference(bind = "setQuerydslSupport")
    private QuerydslSupport querydslSupport;

    @Reference(name = "transactionHelper", bind = "setTransactionHelper")
    private TransactionHelper t;

    @Activate
    public void activate(final BundleContext context) {
        cacheHolder = cacheFactory.createCache(cacheConfiguration, LocalizedValue.class.getClassLoader());
        cache = cacheHolder.getCache();
    }

    @Override
    public void addValue(String key, Locale locale, String value) {
        String languageTag = locale.toLanguageTag();
        t.required(() -> {
            boolean existed = lockOnKey(key);
            if (!existed) {
                insertDefaultLocaleToDB(key, languageTag);
            }

            return null;
        });

    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Deactivate
    public void deactivate() {
        cacheHolder.close();
    }

    @Override
    public Optional<String> getExactValue(String key, Locale locale) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(locale, "Value must not be null");

        LocalizedValue[] localizedValues = getLocalizedValues(key);
        if (localizedValues == null) {
            return Optional.empty();
        }

        String result = null;
        String languageTag = locale.toLanguageTag();
        for (int i = 0, n = localizedValues.length; result == null && i < n; i++) {
            if (localizedValues[i].getLanguageTag().equals(languageTag)) {
                result = localizedValues[i].getValue();
            }
        }
        return Optional.ofNullable(result);
    }

    private LocalizedValue[] getLocalizedValues(String key) {
        LocalizedValue[] cachedLocalizedValues = cache.get(key);
        if (cachedLocalizedValues != null) {
            return cachedLocalizedValues;
        }
        // If localized values are not in the cache, read them from DB in the way that after putting them into the
        // cache, they should be the same if we read them again from the database.
        return t.notSupported(() -> {
            LocalizedValue[] dbLocalizedValues = readSortedLocalizedValuesFromDB(key);
            cache.put(key, dbLocalizedValues);
            LocalizedValue[] newDbLocalizedValues = readSortedLocalizedValuesFromDB(key);
            while (!Arrays.equals(dbLocalizedValues, newDbLocalizedValues)) {
                dbLocalizedValues = newDbLocalizedValues;
                cache.put(key, dbLocalizedValues);
                newDbLocalizedValues = readSortedLocalizedValuesFromDB(key);
            }

            return dbLocalizedValues;
        });
    }

    @Override
    public Locale[] getSupportedLocalesForKey(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getValue(String key, Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getValue(String key, Locale locale, String defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    private void insertDefaultLocaleToDB(String key, String languageTag) {

        querydslSupport.execute((connection, configuration) -> {
            QDefaultLocale defaultLocale = QDefaultLocale.defaultLocale;

            SQLInsertClause insert = new SQLInsertClause(connection, configuration, defaultLocale);
            insert.set(defaultLocale.key, key).set(defaultLocale.languageTag, languageTag).execute();
            return null;
        });
    }

    @Override
    public Coalesce<String> localize(Path<String> localizationKey, Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean lockOnKey(String key) {
        return querydslSupport.execute((connection, configuration) -> {
            QDefaultLocale defaultLocale = QDefaultLocale.defaultLocale;
            SQLQuery query = new SQLQuery(connection, configuration);

            List<String> dbResult = query.from(defaultLocale).where(defaultLocale.key.eq(key)).forUpdate()
                    .list(defaultLocale.key);

            return dbResult.size() > 0;
        });
    }

    private LocalizedValue[] readSortedLocalizedValuesFromDB(String key) {

        return querydslSupport.execute((connection, configuration) -> {

            SQLQuery query = new SQLQuery(connection, configuration);
            QLocalizedData localizedData = QLocalizedData.localizedData;
            QDefaultLocale defaultLocale = QDefaultLocale.defaultLocale;

            List<Tuple> dbResults = query.from(localizedData)
                    .innerJoin(defaultLocale).on(localizedData.key.eq(defaultLocale.key))
                    .where(localizedData.key.eq(key))
                    .orderBy(localizedData.languageTag.asc(), localizedData.value.asc())
                    .list(localizedData.languageTag, localizedData.value, defaultLocale.languageTag);

            LocalizedValue[] results = new LocalizedValue[dbResults.size()];
            if (results.length == 0) {
                return results;
            }
            String defaultLanguageTag = dbResults.get(0).get(defaultLocale.languageTag);
            int i = 0;
            Iterator<Tuple> iterator = dbResults.iterator();
            while (iterator.hasNext()) {
                Tuple tuple = iterator.next();
                LocalizedValue result = new LocalizedValue();
                String languageTag = tuple.get(localizedData.languageTag);
                result.setLanguageTag(languageTag);
                result.setValue(tuple.get(localizedData.value));
                result.setDefaultValue(languageTag.equals(defaultLanguageTag));
                results[i] = result;
                i++;
            }

            return results;
        });
    }

    @Override
    public long removeKey(String key) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void removeValue(String key, Locale locale) {
        // TODO Auto-generated method stub

    }

    public void setCacheConfiguration(CacheConfiguration<String, LocalizedValue[]> cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

    public void setCacheFactory(final CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    public void setLogService(final LogService logService) {
        this.logService = logService;
    }

    public void setQuerydslSupport(QuerydslSupport querydslSupport) {
        this.querydslSupport = querydslSupport;
    }

    public void setTransactionHelper(TransactionHelper transactionHelper) {
        this.t = transactionHelper;
    }

    @Override
    public void switchDefaultLocale(String key, Locale locale) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateValue(String key, Locale locale, String value) {
        // TODO Auto-generated method stub

    }
}
