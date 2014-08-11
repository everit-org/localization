package org.everit.osgi.localization.schema.qdsl;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;




/**
 * QDataSet is a Querydsl query type for QDataSet
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QDataSet extends com.mysema.query.sql.RelationalPathBase<QDataSet> {

    private static final long serialVersionUID = -1879463804;

    public static final QDataSet dataSet = new QDataSet("loc_dataset");

    public class PrimaryKeys {

        public final com.mysema.query.sql.PrimaryKey<QDataSet> dataSetPK = createPrimaryKey(name);

    }

    public class ForeignKeys {

        public final com.mysema.query.sql.ForeignKey<QLocalizedData> _locDatasetDataFk = createInvForeignKey(name, "dataset_name");

        public final com.mysema.query.sql.ForeignKey<QDefaultLocale> _locDatasetDefaultLocaleFk = createInvForeignKey(name, "dataset_name");

    }

    public final StringPath name = createString("name");

    public final StringPath versions = createString("versions");

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QDataSet(String variable) {
        super(QDataSet.class, forVariable(variable), "org.everit.osgi.localization", "loc_dataset");
        addMetadata();
    }

    public QDataSet(String variable, String schema, String table) {
        super(QDataSet.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDataSet(Path<? extends QDataSet> path) {
        super(path.getType(), path.getMetadata(), "org.everit.osgi.localization", "loc_dataset");
        addMetadata();
    }

    public QDataSet(PathMetadata<?> metadata) {
        super(QDataSet.class, metadata, "org.everit.osgi.localization", "loc_dataset");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("name_").ofType(12).withSize(255).notNull());
        addMetadata(versions, ColumnMetadata.named("versions").ofType(12).withSize(255).notNull());
    }

}

