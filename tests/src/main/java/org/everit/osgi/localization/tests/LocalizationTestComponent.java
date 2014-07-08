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

import java.util.Locale;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.localization.api.LocalizedDataStore;
import org.junit.Test;

/**
 * Test component for testing {@link LocalizedDataStore} methods.
 */
@Component(name = "LocalizationTest", immediate = true, metatype = true)
@Service(value = LocalizationTestComponent.class)
@Properties({ @Property(name = "eosgi.testEngine", value = "junit4"),
        @Property(name = "eosgi.testId", value = "localizationTest") })
@TestDuringDevelopment
public class LocalizationTestComponent {

    @Reference
    private LocalizedDataStore LocalizedDataStore;

    public void bindLocalizedDataStore(LocalizedDataStore localizedDataStore) {
        LocalizedDataStore = localizedDataStore;
    }

    @Test
    public void test() {
        LocalizedDataStore.addValue("testKey", Locale.ENGLISH, "testValue");

    }
}
