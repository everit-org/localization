package org.everit.osgi.localization.schema.qdsl.util;

import java.util.Locale;

import com.mysema.query.types.Path;
import com.mysema.query.types.expr.Coalesce;

public interface LocalizationQdslUtil {
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
     * SELECT ld.data FROM LocalizedData ld WHERE ld.key = x.a AND ld.locale = ‘hu-HU’),
     * SELECT ld.data FROM LocalizedData ld WHERE ld.key = x.a AND ld.locale = ‘hu’),
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
    Coalesce<String> localize(Path<String> localizationKey, Locale locale);
}
