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

        public final com.mysema.query.sql.PrimaryKey<QLocalizedData> localizedDataPK = createPrimaryKey(key, languageTag);

    }

    public final StringPath key = createString("key");

    public final StringPath languageTag = createString("languageTag");

    public final StringPath value = createString("value");

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
        addMetadata(key, ColumnMetadata.named("key_").ofType(12).withSize(255).notNull());
        addMetadata(languageTag, ColumnMetadata.named("language_tag").ofType(12).withSize(255).notNull());
        addMetadata(value, ColumnMetadata.named("value_").ofType(12).withSize(2000));
    }

}

