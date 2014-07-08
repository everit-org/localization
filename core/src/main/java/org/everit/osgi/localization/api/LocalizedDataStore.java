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
import java.util.Locale;
import java.util.Map;

import org.everit.osgi.localization.api.dto.LocalizedValue;

import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.Coalesce;

/**
 * A service that makes it possible to store localized key-value pairs. Keys and values have the type
 */
public interface LocalizedDataStore {

    /**
     * Saving a new String based localized data with the given key and locale. If there was no value for the specified
     * key before, this value will be the default.
     *
     * @param key
     *            The key of the data. If null throw IllegalArgumentException
     * @param locale
     *            The locale of the data. If null throw IllegalArgumentException
     * @param value
     *            The value of the data. If length more then 2000 throw IllegalArgumentException.
     * @return The created localized data.
     * @throws IllegalArgumentException
     *             if the key, locale or value is null or value is longer than 2000.
     */
    LocalizedValue addValue(String key, Locale locale, String value);

    /**
     * Clears the cache of the store. This should be only necessary if another module does a bulk update (e.g.: updating
     * default locale for a set of records) that cannot be done via the API of this module.
     */
    void clearCache();

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
    Coalesce<String> generateLocalizedExpression(final Path<String> localizationKey, final Locale locale);

    /**
     * Clones the return value of the {@link #getValue(String, Locale)} method.
     *
     * @param key
     *            The key of the localized data. Cannot be null.
     * @return The Map of the localized data by locale.
     * @throws IllegalArgumentException
     *             if the key is null.
     */
    Map<Locale, LocalizedValue> getLocalizedValuesByKey(String key);

    /**
     * Getting the list of locals that are available for the given key.
     *
     * @param key
     *            The key of the localized data.
     * @return The available locals.
     */
    Collection<Locale> getSupportedLocalesForKey(String key);

    /**
     * Getting a localized data by key.
     *
     * @param key
     *            The key of the localized data. If null throw IllegalArgumentException.
     * @param locale
     *            Null means default locale.
     * @return The localized data. If there is no data based on the given locale the default locale will be checked in
     *         the same way as it is done in {@link java.util.ResourceBundle}. If there is no default locale null will
     *         be returned.
     * @throws IllegalArgumentException
     *             if key or locale is null.
     */
    LocalizedValue getValue(String key, Locale locale);

    /**
     * Removes all values that belong the a specific key.
     *
     * @param key
     *            The key.
     * @return The number of records that were deleted. In other words: The number of locales that were specified for
     *         the key.
     */
    long removeKey(String key);

    /**
     * Removes a localized value from the store. If there is no such value, the function has no effect.
     *
     * @param key
     *            The key of the localized value.
     * @param locale
     *            The {@link Locale} of the value.
     */
    void removeValue(String key, Locale locale);

    /**
     * Updating an existing String based localized data. The localized data is identified by it's key and locale.
     *
     * @param key
     *            The key of the data. If null throw IllegalArgumentException
     * @param locale
     *            The locale of the data. If null throw IllegalArgumentException
     * @param value
     *            The value of the data. Maximum length is 2000 if more throw IllegalArgumentException.
     * @return The number of rows affected during update.
     * @throws IllegalArgumentException
     *             if the key, locale or value is null or value is longer than 2000.
     */
    void updateValue(String key, Locale locale, String value);
}
