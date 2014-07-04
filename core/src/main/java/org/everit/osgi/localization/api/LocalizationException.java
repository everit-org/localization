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
 * Localization component related {@link RuntimeException}.
 */
public class LocalizationException extends RuntimeException {

    /**
     * Generated serialVersionUID.
     */
    private static final long serialVersionUID = -9085836630544428292L;

    /**
     * Default constructor.
     */
    public LocalizationException() {
        super();
    }

    public LocalizationException(final String message) {
        super(message);
    }

    public LocalizationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LocalizationException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public LocalizationException(final Throwable cause) {
        super(cause);
    }

}
