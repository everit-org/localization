/**
 * This file is part of Everit - Localization Tests.
 *
 * Everit - Localization Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Localization Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Localization Tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.localization.tests;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.cache.infinispan.config.CacheFactoryProps;
import org.everit.osgi.cache.infinispan.config.CacheProps;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Component class for initializing the environments configuration like dataSource and services.
 */
@Component(immediate = true, metatype = true)
@Service(value = ConfigurationInitComponent.class)
public class ConfigurationInitComponent {
    /**
     * {@link ConfigurationAdmin}.
     */
    @Reference(bind = "bindConfigAdmin")
    private ConfigurationAdmin configAdmin;

    @Activate
    public void activate(final BundleContext bundleContext) {
        try {
            Dictionary<String, Object> xaDataSourceProps = new Hashtable<String, Object>();

            // xaDataSourceProps.put(DataSourceFactory.JDBC_URL, "localhost:5432:test");
            // xaDataSourceProps.put(DataSourceFactory.JDBC_USER, "test");
            // xaDataSourceProps.put(DataSourceFactory.JDBC_PASSWORD, "test");
            // xaDataSourceProps.put("datasourceFactory.target", "(osgi.jdbc.driver.class=org.postgresql.Driver)");

            xaDataSourceProps.put(DataSourceFactory.JDBC_URL, "jdbc:h2:mem:localizationTestDatabase");
            xaDataSourceProps.put("datasourceFactory.target", "(osgi.jdbc.driver.class=org.h2.Driver)");

            String xaDataSourcePid = getOrCreateConfiguration("org.everit.osgi.jdbc.dsf.XADataSourceComponent",
                    xaDataSourceProps);

            Dictionary<String, Object> pooledDataSourceProps = new Hashtable<String, Object>();
            pooledDataSourceProps.put("xaDataSource.target", "(service.pid=" + xaDataSourcePid + ")");
            String pooledDataSourcePid = getOrCreateConfiguration(
                    "org.everit.osgi.jdbc.commons.dbcp.ManagedDataSourceComponent",
                    pooledDataSourceProps);

            getOrCreateConfiguration(CacheFactoryProps.CACHE_FACTORY_COMPONENT_NAME, new Hashtable<String, Object>());

            Hashtable<String, Object> cacheConfigurationProps = new Hashtable<>();
            cacheConfigurationProps.put(CacheProps.TRANSACTION__TRANSACTION_MODE,
                    CacheProps.TRANSACTION__TRANSACTION_MODE_OPT_TRANSACTIONAL);
            cacheConfigurationProps.put(CacheProps.CACHE_NAME, "localizationCache");
            getOrCreateConfiguration(CacheProps.CACHE_CONFIGURATION_COMPONENT_NAME, cacheConfigurationProps);

            Dictionary<String, Object> migratedDataSourceProps = new Hashtable<String, Object>();
            migratedDataSourceProps.put("embeddedDataSource.target", "(service.pid=" + pooledDataSourcePid + ")");
            migratedDataSourceProps.put("schemaExpression", "org.everit.osgi.localization.schema");
            String liquiBaseDataSorucePid = getOrCreateConfiguration(
                    "org.everit.osgi.liquibase.datasource.LiquibaseDataSourceComponent", migratedDataSourceProps);

            Dictionary<String, Object> localizationProps = new Hashtable<String, Object>();
            localizationProps
                    .put("dataSource.target", "(" + Constants.SERVICE_PID + "=" + liquiBaseDataSorucePid + ")");
            getOrCreateConfiguration("org.everit.osgi.localization.LocalizationComponent", localizationProps);

            getOrCreateConfiguration("LocalizationTest", new Hashtable<String, Object>());

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void bindConfigAdmin(final ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    private String getOrCreateConfiguration(final String factoryPid, final Dictionary<String, Object> props)
            throws IOException,
            InvalidSyntaxException {
        Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=" + factoryPid + ")");
        if ((configurations != null) && (configurations.length > 0)) {
            return configurations[0].getPid();
        }
        Configuration configuration = configAdmin.createFactoryConfiguration(factoryPid, null);
        configuration.update(props);
        return configuration.getPid();
    }
}
