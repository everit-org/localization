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
package org.everit.osgi.localization.api.dto;

import java.util.Locale;

/**
 * DTO class of a localized data.
 */
public class LocalizedValue {

    /**
     * Locale is default or not.
     */
    private final boolean defaultLocale;

    /**
     * The key of the data.
     */
    private final String key;

    /**
     * Locale of the attribute.
     */
    private final Locale locale;

    /**
     * The localized value.
     */
    private final String value;

    /**
     * Default constructor.
     */
    public LocalizedValue() {
        key = null;
        locale = null;
        defaultLocale = false;
        value = null;
    }

    /**
     * Public constructor for LocalizedData.
     *
     * @param key
     *            The key of the data.
     * @param locale
     *            The locale of the data.
     * @param defaultLocale
     *            The data is default locale or not.
     * @param value
     *            The localized value.
     */
    public LocalizedValue(final String key, final Locale locale, final boolean defaultLocale,
            final String value) {
        super();
        this.key = key;
        this.locale = locale;
        this.defaultLocale = defaultLocale;
        this.value = value;
    }

    /**
     * Public constructor with String locale.
     *
     * @param key
     *            The key of the data.
     * @param locale
     *            The locale of the data.
     * @param defaultLocale
     *            The data is default locale or not.
     * @param value
     *            The localized value.
     */
    public LocalizedValue(final String key, final String locale, final boolean defaultLocale,
            final String value) {
        this.key = key;
        this.locale = new Locale(locale);
        this.defaultLocale = defaultLocale;
        this.value = value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LocalizedValue other = (LocalizedValue) obj;
        if (defaultLocale != other.defaultLocale) {
            return false;
        }
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public String getKey() {
        return key;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int prime2 = 1231;
        final int prime3 = 1237;
        int result = 1;
        result = (prime * result) + (defaultLocale ? prime2 : prime3);
        result = (prime * result) + ((key == null) ? 0 : key.hashCode());
        result = (prime * result) + ((locale == null) ? 0 : locale.hashCode());
        result = (prime * result) + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    public boolean isDefaultLocale() {
        return defaultLocale;
    }
}
