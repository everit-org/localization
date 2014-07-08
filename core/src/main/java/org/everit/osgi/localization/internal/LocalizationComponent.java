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

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
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
import org.everit.osgi.localization.api.LocalizedDataStore;
import org.everit.osgi.localization.api.dto.LocalizedValue;
import org.everit.osgi.localization.schema.qdsl.QDefaultLocale;
import org.everit.osgi.localization.schema.qdsl.QLocalizedData;
import org.everit.osgi.querydsl.support.QuerydslSupport;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.log.LogService;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.Coalesce;

/**
 * Implementation for {@link LocalizedDataStore}.
 */
@Component(name = "org.everit.osgi.localization.LocalizatedDataStore", metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({ @Property(name = "querydslSupport.target"), @Property(name = "cacheConfiguration.target"),
        @Property(name = "cacheFactory.target"), @Property(name = "logService.target"),
        @Property(name = "transactionHelper.target") })
@Service
public class LocalizationComponent implements LocalizedDataStore {

    /**
     * {@link CacheConfiguration}.
     */
    @Reference(bind = "setCacheConfiguration")
    private CacheConfiguration<String, Map<Locale, LocalizedValue>> cacheConfiguration;
    /**
     * {@link CacheFactory}.
     */
    @Reference(bind = "setCacheFactory")
    private CacheFactory cacheFactory;

    /**
     * {@link CacheHolder}.
     */
    private CacheHolder<String, Map<Locale, LocalizedValue>> cacheHolder;

    /**
     * Caching that is used in front of the entity manager. The localizedDataCache is based on the key of the localized
     * data. When a new localized data is created or an existing is updated based on a key all of the localized data
     * will be deleted belonging to the specified key.
     */
    private ConcurrentMap<String, Map<Locale, LocalizedValue>> localizedDataCache;

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
        BundleWiring bundleWiring = context.getBundle().adapt(BundleWiring.class);
        ClassLoader classLoader = bundleWiring.getClassLoader();
        cacheHolder = cacheFactory.createCache(cacheConfiguration, classLoader);
        localizedDataCache = cacheHolder.getCache();
    }

    @Override
    public LocalizedValue addValue(String key, Locale locale, String value) {
        return t.required(() -> {
            LocalizedValue insertedValue = addValueInTransaction(key, locale, value);
            localizedDataCache.remove(key);
            return insertedValue;
        });
    }

    private LocalizedValue addValueInTransaction(final String key, final Locale locale, final String value) {
        insertValueToDatabase(key, locale, value);
        String defaultLocale = lockDefaultLocaleInDatabase(key);
        if (defaultLocale == null) {
            insertDefaultLocaleIntoDatabase(key, locale.toLanguageTag());
            return new LocalizedValue(key, locale, true, value);
        } else {
            return new LocalizedValue(key, locale, false, value);
        }
    }

    @Override
    public void clearCache() {
        localizedDataCache.clear();
    }

    @Deactivate
    public void deactivate() {
        cacheHolder.close();
    }

    @Override
    public Coalesce<String> generateLocalizedExpression(final Path<String> localizationKey, final Locale locale) {
        // TODO
        return null;
    }

    @Override
    public Map<Locale, LocalizedValue> getLocalizedValuesByKey(final String key) {
        // TODO
        return null;
    }

    @Override
    public Collection<Locale> getSupportedLocalesForKey(final String key) {
        // TODO
        return null;
    }

    @Override
    public LocalizedValue getValue(final String key, final Locale locale) {
        return null;
    }

    private void insertDefaultLocaleIntoDatabase(String key, String languageTag) {
        querydslSupport.execute((connection, configuration) -> {
            QDefaultLocale defaultLocale = QDefaultLocale.defaultLocale1;
            SQLInsertClause insert = new SQLInsertClause(connection, configuration, defaultLocale);
            insert.set(defaultLocale.key_, key).set(defaultLocale.defaultLocale, languageTag).execute();
            return null;
        });
    }

    private void insertValueToDatabase(String key, Locale locale, String value) {
        querydslSupport.execute((connection, configuration) -> {
            QLocalizedData localizedData = QLocalizedData.localizedData;
            SQLInsertClause insertLocalizedData = new SQLInsertClause(connection, configuration, localizedData);
            insertLocalizedData.set(localizedData.key_, key).set(localizedData.locale_, locale.toLanguageTag())
                    .set(localizedData.value_, value).execute();

            return null;
        });
    }

    /**
     * Locks (pessimistic) the default locale record for the specified key. This method should be called within a
     * transaction.
     *
     * @param key
     *            The key of the localized value.
     * @return The current default locale if there was record to lock or null if there was no record to lock.
     */
    private String lockDefaultLocaleInDatabase(String key) {
        return querydslSupport.execute((connection, configuration) -> {
            SQLQuery query = new SQLQuery(connection, configuration);
            QDefaultLocale defaultLocale = QDefaultLocale.defaultLocale1;
            return query.from(defaultLocale).where(defaultLocale.key_.eq(key)).forUpdate()
                    .uniqueResult(defaultLocale.defaultLocale);
        });

    }

    @Override
    public long removeKey(String key) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void removeValue(final String key, final Locale locale) {
        // TODO
    }

    public void setCacheConfiguration(CacheConfiguration<String, Map<Locale, LocalizedValue>> cacheConfiguration) {
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
    public void updateValue(final String key, final Locale locale, final String value) {
        // TODO
    }
}
