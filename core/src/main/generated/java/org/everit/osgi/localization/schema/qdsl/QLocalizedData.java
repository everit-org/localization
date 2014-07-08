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
package org.everit.osgi.localization.schema.qdsl;

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

    private static final long serialVersionUID = 1426427121;

    public static final QLocalizedData localizedData = new QLocalizedData("loc_data");

    public class PrimaryKeys {

        public final com.mysema.query.sql.PrimaryKey<QLocalizedData> locDataPk = createPrimaryKey(key_, locale_);

    }

    public final StringPath key_ = createString("key_");

    public final StringPath locale_ = createString("locale_");

    public final StringPath value_ = createString("value_");

    public final PrimaryKeys pk = new PrimaryKeys();

    public QLocalizedData(String variable) {
        super(QLocalizedData.class, forVariable(variable), "org.everit.osgi.localization", "loc_data");
        addMetadata();
    }

    public QLocalizedData(String variable, String schema, String table) {
        super(QLocalizedData.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocalizedData(Path<? extends QLocalizedData> path) {
        super(path.getType(), path.getMetadata(), "org.everit.osgi.localization", "loc_data");
        addMetadata();
    }

    public QLocalizedData(PathMetadata<?> metadata) {
        super(QLocalizedData.class, metadata, "org.everit.osgi.localization", "loc_data");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(key_, ColumnMetadata.named("key_").ofType(12).withSize(255).notNull());
        addMetadata(locale_, ColumnMetadata.named("locale_").ofType(12).withSize(20).notNull());
        addMetadata(value_, ColumnMetadata.named("value_").ofType(12).withSize(2000));
    }

}

