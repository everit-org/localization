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
package org.everit.osgi.localization.schema;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;




/**
 * QLocalizedData is a Querydsl query type for QLocalizedData
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QLocalizedData extends com.mysema.query.sql.RelationalPathBase<QLocalizedData> {

    private static final long serialVersionUID = -556535143;

    public static final QLocalizedData localizedData = new QLocalizedData("localized_data");

    public final BooleanPath defaultLocale = createBoolean("defaultLocale");

    public final StringPath key_ = createString("key_");

    public final StringPath locale_ = createString("locale_");

    public final NumberPath<Long> localizedDataId = createNumber("localizedDataId", Long.class);

    public final StringPath value_ = createString("value_");

    public final com.mysema.query.sql.PrimaryKey<QLocalizedData> localizedDataPk = createPrimaryKey(localizedDataId);

    public QLocalizedData(String variable) {
        super(QLocalizedData.class, forVariable(variable), "org.everit.osgi.localization.schema", "localized_data");
        addMetadata();
    }

    public QLocalizedData(String variable, String schema, String table) {
        super(QLocalizedData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocalizedData(Path<? extends QLocalizedData> path) {
        super(path.getType(), path.getMetadata(), "org.everit.osgi.localization.schema", "localized_data");
        addMetadata();
    }

    public QLocalizedData(PathMetadata<?> metadata) {
        super(QLocalizedData.class, metadata, "org.everit.osgi.localization.schema", "localized_data");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(defaultLocale, ColumnMetadata.named("default_locale").ofType(16).withSize(1).notNull());
        addMetadata(key_, ColumnMetadata.named("key_").ofType(12).withSize(255));
        addMetadata(locale_, ColumnMetadata.named("locale_").ofType(12).withSize(10));
        addMetadata(localizedDataId, ColumnMetadata.named("localized_data_id").ofType(-5).withSize(19).notNull());
        addMetadata(value_, ColumnMetadata.named("value_").ofType(12).withSize(2000));
    }

}

