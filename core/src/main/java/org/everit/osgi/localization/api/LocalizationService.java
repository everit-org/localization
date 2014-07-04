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
package org.everit.osgi.localization.api;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.everit.osgi.localization.api.dto.LocalizedData;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;

/**
 * Service for handling localized data.
 */
public interface LocalizationService {

    /**
     * Clear the LocalizationService cache variable.
     */
    void clearCache();

    /**
     * Saving a new String based localized data with the given key and locale.
     * 
     * @param key
     *            The key of the data. If null throw IllegalArgumentException
     * @param locale
     *            The locale of the data. If null throw IllegalArgumentException
     * @param value
     *            The value of the data. If length more then 2000 throw IllegalArgumentException.
     * @param defaultLocale
     *            If true this will be the value of the default locale as well for the given key.
     * @return The created localized data.
     * @throws IllegalArgumentException
     *             if the key, locale or value is null or value is longer than 2000.
     */
    LocalizedData createLocalizedData(String key, Locale locale, String value, boolean defaultLocale);

    /**
     * Reads all the available locals from the database.
     * 
     * @return A Map of available locals ( en_GB - Great Britain )
     */
    List<Locale> getAvailableLocales();

    /**
     * Getting a localized data by key.
     * 
     * @param key
     *            The key of the localized data. If null throw IllegalArgumentException.
     * @param locale
     *            The locale we get the data based on. If null throw IllegalArgumentException.
     * @return The localized data. If there is no data based on the given locale the default locale will be checked in
     *         the same way as it is done in {@link java.util.ResourceBundle}. If there is no default locale null will
     *         be returned.
     * @throws IllegalArgumentException
     *             if key or locale is null.
     */
    LocalizedData getLocalizedDataByKey(String key, Locale locale);

    /**
     * Clones the return value of the {@link #getLocalizedDataByKey(String, Locale)} method.
     * 
     * @param key
     *            The key of the localized data. Cannot be null.
     * @return The Map of the localized data by locale.
     * @throws IllegalArgumentException
     *             if the key is null.
     */
    Map<Locale, LocalizedData> getLocalizedDataMapClone(String key);

    /**
     * Creates the expression with COALESCE of the localization value based on the localization key and locale. The
     * coalesce query is built in the following order: If there is a localization key available on the locale, then its
     * value will be returned. If there is no localization key available on the locale, then the value of the default
     * locale will be returned. If there is no default locale available, then the localization key will be returned.
     * <p/>
     * <p>
     * The following example demonstrates the result of using this method. The first SQL statement simply queries the
     * columns "a", "b" and "c" from table "x". The second SQL statement queries the same as the previous except the
     * query will return the localized value of column "a" with logic describe above. The LocalizedData table is the
     * representation of the {@link LocalizedDataEntity} entity.
     * </p>
     * 
     * <pre>
     * SELECT x.a, x.b, x.c FROM x;
     * </pre>
     * 
     * <pre>
     * SELECT
     * COALESCE(
     * SELECT ld.data FROM LocalizedData ld WHERE ld.key = x.a AND ld.locale = ‘hu_HU’),
     * SELECT ld.data FROM LocalizedData ld WHERE ld.key = x.a AND ld.default = true),
     * x.a)
     * x.b, x.c FROM x;
     * </pre>
     * 
     * @param localizationKey
     *            {@link Path} for the localization key from "x" table.
     * @param locale
     *            {@link Locale} to be used in for the highest priority.
     * 
     * @return {@link Expression} with coalesce subQueries.
     */
    Expression<String> getLocalizedValue(final Path<String> localizationKey, final Locale locale);

    /**
     * Getting the list of locals that are available for the given key.
     * 
     * @param key
     *            The key of the localized data.
     * @return The available locals.
     */
    Collection<Locale> getSupportedLocalesByKey(String key);

    /**
     * Removes a localized data by it's key and locale.
     * 
     * @param key
     *            The key of the localized data.
     * @param locale
     *            The locale of the data.
     * @return The number of rows affected.
     */
    long removeLocalizedData(String key, Locale locale);

    /**
     * Updating an existing String based localized data. The localized data is identified by it's key and locale.
     * 
     * @param key
     *            The key of the data. If null throw IllegalArgumentException
     * @param locale
     *            The locale of the data. If null throw IllegalArgumentException
     * @param value
     *            The value of the data. Maximum length is 2000 if more throw IllegalArgumentException.
     * @param defaultLocale
     *            If true this will be the value of the default locale as well for the given key.
     * @return The number of rows affected during update.
     * @throws IllegalArgumentException
     *             if the key, locale or value is null or value is longer than 2000.
     */
    long updateLocalizedData(String key, Locale locale, String value, boolean defaultLocale);

    /**
     * Updating more existing String based localized data. First search the default data and update, after that update
     * the other data.
     * 
     * @param localizedDataMap
     *            The map contains the localized datas. The map key is the locale. If a localizedData not exist with the
     *            given key.
     * @throws IllegalArgumentException
     *             If the localizedDataMap is null.
     */
    void updateLocalizedDataMap(Map<String, LocalizedData> localizedDataMap);
}
