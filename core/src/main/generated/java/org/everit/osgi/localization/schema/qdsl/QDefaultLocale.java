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

    public static final QDefaultLocale defaultLocale = new QDefaultLocale("loc_default_locale");

    public class PrimaryKeys {

        public final com.mysema.query.sql.PrimaryKey<QDefaultLocale> defaultLocalePK = createPrimaryKey(key);

    }

    public final StringPath key = createString("key");

    public final StringPath languageTag = createString("languageTag");

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
        addMetadata(key, ColumnMetadata.named("key_").ofType(12).withSize(255).notNull());
        addMetadata(languageTag, ColumnMetadata.named("language_tag").ofType(12).withSize(255).notNull());
    }

}

