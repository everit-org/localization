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

/**
 * The ErrorCodes of the LocalizatonExceptions.
 */
public final class ErrorCode {
    /**
     * When have some problem whit the default value. Non-exist or duplicated.
     */
    public static final String DEFAULT_VALUE = "Default value error. Non-exist or duplicated.";
    /**
     * When try to remove the default locale.
     */
    public static final String DEFAULT_LOCALE_CANNOT_BE_REMOVED = "Default locale can not be removed.";

    private ErrorCode() {
    }
}
