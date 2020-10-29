// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.hibernate;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Exportable;
import org.hibernate.boot.model.relational.InitCommand;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.schema.internal.DefaultSchemaFilter;
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.hibernate.tool.schema.internal.exec.GenerationTarget;
import org.hibernate.tool.schema.spi.CommandAcceptanceException;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaManagementException;

import com.amalto.core.storage.hibernate.mapping.MDMTableExporter;

public class MDMSchemaCreatorImpl extends SchemaCreatorImpl {

    private final SchemaFilter schemaFilter;

    public MDMSchemaCreatorImpl(HibernateSchemaManagementTool tool) {
        this(tool, DefaultSchemaFilter.INSTANCE);
    }

    public MDMSchemaCreatorImpl(HibernateSchemaManagementTool tool, SchemaFilter schemaFilter) {
        super(tool, schemaFilter);
        this.schemaFilter = schemaFilter;
    }

    public MDMSchemaCreatorImpl(ServiceRegistry serviceRegistry) {
        this(serviceRegistry, DefaultSchemaFilter.INSTANCE);
    }

    public MDMSchemaCreatorImpl(ServiceRegistry serviceRegistry, SchemaFilter schemaFilter) {
        super(serviceRegistry, schemaFilter);
        this.schemaFilter = schemaFilter;
    }
    
    public void createFromMetadata(
            Metadata metadata,
            ExecutionOptions options,
            Dialect dialect,
            Formatter formatter,
            GenerationTarget... targets) {
//      super.createFromMetadata(metadata, options, dialect, formatter, targets);

        boolean tryToCreateCatalogs = false;
        boolean tryToCreateSchemas = false;
        if (options.shouldManageNamespaces()) {
            if (dialect.canCreateSchema()) {
                tryToCreateSchemas = true;
            }
            if (dialect.canCreateCatalog()) {
                tryToCreateCatalogs = true;
            }
        }

        final Database database = metadata.getDatabase();

        final Set<String> exportIdentifiers = new HashSet<String>(50);

        // first, create each catalog/schema
        if (tryToCreateCatalogs || tryToCreateSchemas) {
            Set<Identifier> exportedCatalogs = new HashSet<Identifier>();
            for (Namespace namespace : database.getNamespaces()) {

                if (!schemaFilter.includeNamespace(namespace)) {
                    continue;
                }

                if (tryToCreateCatalogs) {
                    final Identifier catalogLogicalName = namespace.getName().getCatalog();
                    final Identifier catalogPhysicalName = namespace.getPhysicalName().getCatalog();

                    if (catalogPhysicalName != null && !exportedCatalogs.contains(catalogLogicalName)) {
                        applySqlStrings(dialect.getCreateCatalogCommand(catalogPhysicalName.render(dialect)), formatter, options,
                                targets);
                        exportedCatalogs.add(catalogLogicalName);
                    }
                }

                if (tryToCreateSchemas && namespace.getPhysicalName().getSchema() != null) {
                    applySqlStrings(dialect.getCreateSchemaCommand(namespace.getPhysicalName().getSchema().render(dialect)),
                            formatter, options, targets);
                }
            }
        }

        // next, create all "before table" auxiliary objects
        for (AuxiliaryDatabaseObject auxiliaryDatabaseObject : database.getAuxiliaryDatabaseObjects()) {
            if (!auxiliaryDatabaseObject.beforeTablesOnCreation()) {
                continue;
            }

            if (auxiliaryDatabaseObject.appliesToDialect(dialect)) {
                checkExportIdentifier(auxiliaryDatabaseObject, exportIdentifiers);
                applySqlStrings(
                        dialect.getAuxiliaryDatabaseObjectExporter().getSqlCreateStrings(auxiliaryDatabaseObject, metadata),
                        formatter, options, targets);
            }
        }

        // then, create all schema objects (tables, sequences, constraints, etc) in each schema
        for (Namespace namespace : database.getNamespaces()) {

            if (!schemaFilter.includeNamespace(namespace)) {
                continue;
            }

            // sequences
            for (Sequence sequence : namespace.getSequences()) {
                if (!schemaFilter.includeSequence(sequence)) {
                    continue;
                }
                checkExportIdentifier(sequence, exportIdentifiers);
                applySqlStrings(dialect.getSequenceExporter().getSqlCreateStrings(sequence, metadata), formatter, options,
                        targets);
            }

            // tables
            for (Table table : namespace.getTables()) {
                if (!table.isPhysicalTable()){
                    continue;
                }
                if (!schemaFilter.includeTable(table)) {
                    continue;
                }
                checkExportIdentifier(table, exportIdentifiers);
                applySqlStrings(
                        // dialect.getTableExporter().getSqlCreateStrings(table, metadata),
                        MDMTableExporter.getInstance(dialect).getSqlCreateStrings(table, metadata), formatter, options, targets);
            }

            for (Table table : namespace.getTables()) {
                if (!table.isPhysicalTable()){
                    continue;
                }
                if (!schemaFilter.includeTable(table)) {
                    continue;
                }
                // indexes
                final Iterator indexItr = table.getIndexIterator();
                while (indexItr.hasNext()) {
                    final Index index = (Index) indexItr.next();
                    checkExportIdentifier(index, exportIdentifiers);
                    applySqlStrings(dialect.getIndexExporter().getSqlCreateStrings(index, metadata), formatter, options, targets);
                }

                // unique keys
                final Iterator ukItr = table.getUniqueKeyIterator();
                while (ukItr.hasNext()) {
                    final UniqueKey uniqueKey = (UniqueKey) ukItr.next();
                    checkExportIdentifier(uniqueKey, exportIdentifiers);
                    applySqlStrings(dialect.getUniqueKeyExporter().getSqlCreateStrings(uniqueKey, metadata), formatter, options,
                            targets);
                }
            }
        }

        //NOTE : Foreign keys must be created *after* all tables of all namespaces for cross namespace fks. see HHH-10420
        for (Namespace namespace : database.getNamespaces()) {
            // NOTE : Foreign keys must be created *after* unique keys for numerous DBs.  See HHH-8390

            if (!schemaFilter.includeNamespace(namespace)) {
                continue;
            }

            for (Table table : namespace.getTables()) {
                if (!schemaFilter.includeTable(table)) {
                    continue;
                }
                // foreign keys
                final Iterator fkItr = table.getForeignKeyIterator();
                while (fkItr.hasNext()) {
                    final ForeignKey foreignKey = (ForeignKey) fkItr.next();
                    applySqlStrings(dialect.getForeignKeyExporter().getSqlCreateStrings(foreignKey, metadata), formatter, options,
                            targets);
                }
            }
        }

        // next, create all "after table" auxiliary objects
        for (AuxiliaryDatabaseObject auxiliaryDatabaseObject : database.getAuxiliaryDatabaseObjects()) {
            if (auxiliaryDatabaseObject.appliesToDialect(dialect)
                    && !auxiliaryDatabaseObject.beforeTablesOnCreation()) {
                checkExportIdentifier(auxiliaryDatabaseObject, exportIdentifiers);
                applySqlStrings(
                        dialect.getAuxiliaryDatabaseObjectExporter().getSqlCreateStrings(auxiliaryDatabaseObject, metadata),
                        formatter, options, targets);
            }
        }

        // and finally add all init commands
        for (InitCommand initCommand : database.getInitCommands()) {
            // todo: this should alo probably use the DML formatter...
            applySqlStrings(initCommand.getInitCommands(), formatter, options, targets);
        }
    
    }

    public static void applySqlStrings(
            String[] sqlStrings,
            Formatter formatter,
            ExecutionOptions options,
            GenerationTarget... targets) {
        if (sqlStrings == null) {
            return;
        }

        for (String sqlString : sqlStrings) {
            applySqlString(sqlString, formatter, options, targets);
        }
    }

    private static void applySqlString(
            String sqlString,
            Formatter formatter,
            ExecutionOptions options,
            GenerationTarget... targets) {
        if (StringHelper.isEmpty(sqlString)) {
            return;
        }

        try {
            String sqlStringFormatted = formatter.format(sqlString);
            for (GenerationTarget target : targets) {
                target.accept(sqlStringFormatted);
            }
        }
        catch (CommandAcceptanceException e) {
            options.getExceptionHandler().handleException(e);
        }
    }
    
    private static void checkExportIdentifier(Exportable exportable, Set<String> exportIdentifiers) {
        final String exportIdentifier = exportable.getExportIdentifier();
        if (exportIdentifiers.contains(exportIdentifier)) {
            throw new SchemaManagementException("SQL strings added more than once for: " + exportIdentifier);
        }
        exportIdentifiers.add(exportIdentifier);
    }
}
