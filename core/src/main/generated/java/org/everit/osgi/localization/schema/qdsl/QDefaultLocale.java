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
 * QDefaultLocale is a Querydsl query type for QDefaultLocale
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QDefaultLocale extends com.mysema.query.sql.RelationalPathBase<QDefaultLocale> {

    private static final long serialVersionUID = 1498478375;

    public static final QDefaultLocale defaultLocale1 = new QDefaultLocale("loc_default_locale");

    public class PrimaryKeys {

        public final com.mysema.query.sql.PrimaryKey<QDefaultLocale> locDefaultLocalePk = createPrimaryKey(key_);

    }

    public final StringPath defaultLocale = createString("defaultLocale");

    public final StringPath key_ = createString("key_");

    public final PrimaryKeys pk = new PrimaryKeys();

    public QDefaultLocale(String variable) {
        super(QDefaultLocale.class, forVariable(variable), "org.everit.osgi.localization", "loc_default_locale");
        addMetadata();
    }

    public QDefaultLocale(String variable, String schema, String table) {
        super(QDefaultLocale.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDefaultLocale(Path<? extends QDefaultLocale> path) {
        super(path.getType(), path.getMetadata(), "org.everit.osgi.localization", "loc_default_locale");
        addMetadata();
    }

    public QDefaultLocale(PathMetadata<?> metadata) {
        super(QDefaultLocale.class, metadata, "org.everit.osgi.localization", "loc_default_locale");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(defaultLocale, ColumnMetadata.named("default_locale").ofType(12).withSize(20).notNull());
        addMetadata(key_, ColumnMetadata.named("key_").ofType(12).withSize(255).notNull());
    }

}

