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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.localization.api.ErrorCode;
import org.everit.osgi.localization.api.LocalizationException;
import org.everit.osgi.localization.api.LocalizationService;
import org.everit.osgi.localization.api.dto.LocalizedData;
import org.everit.osgi.localization.schema.QLocalizedData;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.log.LogService;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.types.path.StringPath;

/**
 * Test component for testing {@link LocalizationService} methods.
 */
@Component(name = "LocalizationTest", immediate = true, metatype = true, policy = ConfigurationPolicy.REQUIRE)
@Service(value = LocalizationTestComponent.class)
@Properties({ @Property(name = "eosgi.testEngine", value = "junit4"),
        @Property(name = "eosgi.testId", value = "localizationTest"),
        @Property(name = "dataSource.target"),
        @Property(name = "logSource.target") })
public class LocalizationTestComponent {
    /**
     * Test key.
     */
    private static final String APPLE_KEY = "localization.test.apple";
    private static final String APPLE_HU = "alma";
    private static final String APPLE_HU_2 = "almacska";
    private static final String APPLE_EN = "apple";

    /**
     * Test keys.
     */
    private static final String PEAR_KEY = "localization.test.pear";
    private static final String PEAR_HU = "körte";
    private static final String PEAR_EN = "pear";

    private static final String PLUM_KEY = "localization.test.plum";
    private static final String PLUM_HU = "szilva";

    private static final String ORANGE_KEY = "localization.test.orange";
    private static final String ORANGE_HU = "narancs";
    private static final String ORANGE_EN = "orange";

    private static final String DUMMY_KEY = "localization.test.dummy";

    /**
     * Hungarian country code.
     */
    private static final String HUNGARIAN_COUNTRY_CODE = "hu";
    /**
     * Hungarian locale.
     */
    private static final Locale HU = new Locale(HUNGARIAN_COUNTRY_CODE);
    /**
     * English country code.
     */
    private static final String ENGLISH_COUNTRY_CODE = "en";
    /**
     * English locale.
     */
    private static final Locale EN = new Locale(ENGLISH_COUNTRY_CODE);

    /**
     * {@link ExpandedSQLTemplates}.
     */
    private static final SQLTemplates SQL_TEMPLATES = new ExpandedSQLTemplates();
    /**
     * Zero number.
     */
    private static final int TWO = 2;
    /**
     * {@link LocalizationService}.
     */
    @Reference(policy = ReferencePolicy.STATIC)
    private LocalizationService localizationService;
    /**
     * {@link DataSource}.
     */
    @Reference
    private DataSource dataSource;
    /**
     * {@link LogService}.
     */
    @Reference
    private LogService logService;

    public void bindDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void bindLocalizationService(final LocalizationService localizationService) {
        this.localizationService = localizationService;
    }

    public void bindLogService(final LogService logService) {
        this.logService = logService;
    }

    private void clearData() {
        try {
            Connection connection = dataSource.getConnection();
            QLocalizedData localizedData = new QLocalizedData("qLocalizedData");
            SQLDeleteClause deleteClause = new SQLDeleteClause(connection, SQL_TEMPLATES, localizedData);
            deleteClause.execute();
        } catch (SQLException e) {
            logService.log(Level.SEVERE.intValue(), e.getMessage(), e);
        }
    }

    private void testCreateLocalizedData() {
        localizationService.createLocalizedData(APPLE_KEY, HU, APPLE_HU, true);
        try {
            localizationService.createLocalizedData(APPLE_KEY, EN, APPLE_EN, true);
        } catch (LocalizationException e) {
            Assert.assertEquals(ErrorCode.DEFAULT_VALUE, e.getMessage());
        }
        localizationService.createLocalizedData(APPLE_KEY, EN, APPLE_EN, false);
        localizationService.createLocalizedData(PEAR_KEY, HU, PEAR_HU, true);
        localizationService.createLocalizedData(PEAR_KEY, EN, PEAR_EN, false);
        localizationService.createLocalizedData(PLUM_KEY, HU, PLUM_HU, true);
        localizationService.createLocalizedData(PLUM_KEY, EN, PEAR_EN, false);
        localizationService.createLocalizedData(ORANGE_KEY, HU, ORANGE_HU, true);
        localizationService.createLocalizedData(ORANGE_KEY, EN, ORANGE_EN, false);
    }

    private void testGetAvailableLocales() {
        List<Locale> locales = localizationService.getAvailableLocales();
        Assert.assertEquals(TWO, locales.size());

        boolean containsHUN = false;
        boolean containsEN = false;
        for (Locale locale : locales) {
            if (locale.getLanguage().equals(HUNGARIAN_COUNTRY_CODE)) {
                containsHUN = true;
            }
            if (locale.getLanguage().equals(ENGLISH_COUNTRY_CODE)) {
                containsEN = true;
            }
        }
        Assert.assertTrue(containsHUN);
        Assert.assertTrue(containsEN);
    }

    private void testGetLocalizedDataByKey() {
        LocalizedData appleHU = localizationService.getLocalizedDataByKey(APPLE_KEY, HU);
        Assert.assertEquals(APPLE_HU, appleHU.getValue());
        LocalizedData appleEN = localizationService.getLocalizedDataByKey(APPLE_KEY, EN);
        Assert.assertEquals(APPLE_EN, appleEN.getValue());
        LocalizedData orangeEN = localizationService.getLocalizedDataByKey(ORANGE_KEY, EN);
        Assert.assertEquals(ORANGE_EN, orangeEN.getValue());
    }

    private void testGetLocalizedValue() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            QLocalizedData localizedData = new QLocalizedData("testLocData"); // MUST be different from
                                                                              // localizationServices alias.
            SQLQuery selectClause = new SQLQuery(connection, SQL_TEMPLATES);
            selectClause.from(localizedData);

            StringPath key_ = localizedData.key_;
            selectClause.where(key_.eq(APPLE_KEY));
            // FIXME should be uniqueResult instead of list, but it returns [alma, alma]
            List<String> result = selectClause.list(localizationService.getLocalizedValue(key_, EN));
            Assert.assertEquals(result.get(0), APPLE_EN);

            // FIXME should be uniqueResult
            result = selectClause.list(localizationService.getLocalizedValue(key_, null));
            // Assert.assertEquals(result, APPLE_HU);
            //
            // FIXME should be uniqueResult
            result = selectClause.list(localizationService.getLocalizedValue(key_, new Locale(
                    "ru")));
            // Assert.assertEquals(result, APPLE_HU);
            //
            selectClause.where(localizedData.key_.eq(DUMMY_KEY));
            // FIXME should be uniqueResult
            result = selectClause.list(localizationService.getLocalizedValue(localizedData.key_, HU));
            // Assert.assertEquals(result, DUMMY_KEY);
        } catch (SQLException e) {
            logService.log(Level.SEVERE.intValue(), e.getMessage(), e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logService.log(Level.SEVERE.intValue(), e.getMessage(), e);
            }
        }
    }

    /**
     * Test method for {@link LocalizationService} createLocalization and deleteLocalization methods.
     */
    @Test
    @TestDuringDevelopment
    public void testLocalizationService() {
        clearData();
        testCreateLocalizedData();
        testGetLocalizedValue();
        testGetAvailableLocales();
        testGetLocalizedDataByKey();
        testUpdateLocalizedData();
        testRemoveLocalizedData();
    }

    private void testRemoveLocalizedData() {
        long removed = localizationService.removeLocalizedData(ORANGE_KEY, EN);
        Assert.assertEquals(1, removed);
        removed = localizationService.removeLocalizedData(ORANGE_KEY, EN);
        Assert.assertEquals(0, removed);
        removed = localizationService.removeLocalizedData(ORANGE_KEY, HU);
        Assert.assertEquals(1, removed);
    }

    private void testUpdateLocalizedData() {
        long affected = localizationService.updateLocalizedData(DUMMY_KEY, HU, APPLE_HU_2, false);
        Assert.assertEquals(0, affected);

        affected = localizationService.updateLocalizedData(APPLE_KEY, HU, APPLE_HU_2, true);
        Assert.assertEquals(1, affected);

        LocalizedData localizedData = localizationService.getLocalizedDataByKey(APPLE_KEY, HU);
        Assert.assertEquals(APPLE_HU_2, localizedData.getValue());

        try {
            affected = localizationService.updateLocalizedData(APPLE_KEY, HU, APPLE_HU_2, false);
        } catch (LocalizationException e) {
            Assert.assertEquals(ErrorCode.DEFAULT_VALUE, e.getMessage());
        }
    }
}
