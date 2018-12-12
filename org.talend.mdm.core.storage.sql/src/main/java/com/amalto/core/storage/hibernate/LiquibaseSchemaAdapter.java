/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.util.DateUtils;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.MetadataVisitable;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.Compare.DiffResults;
import org.talend.mdm.commmon.metadata.compare.ModifyChange;
import org.talend.mdm.commmon.metadata.compare.RemoveChange;
import org.talend.mdm.commmon.util.core.CommonUtil;

import com.amalto.core.storage.HibernateStorageUtils;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource.DataSourceDialect;

import liquibase.Liquibase;
import liquibase.change.AbstractChange;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddDefaultValueChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.change.core.DropIndexChange;
import liquibase.change.core.DropNotNullConstraintChange;
import liquibase.database.DatabaseConnection;
import liquibase.precondition.core.IndexExistsPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

public class LiquibaseSchemaAdapter  {

    private static final String SQL_SERVER_SCHEMA = "dbo"; //$NON-NLS-1$

    private static final String SEPARATOR = "-"; //$NON-NLS-1$

    public static final String DATA_LIQUIBASE_CHANGELOG_PATH = "/data/liquibase-changelog/"; //$NON-NLS-1$

    public static final String MDM_ROOT = "mdm.root"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(LiquibaseSchemaAdapter.class);

    private TableResolver tableResolver;

    private Dialect dialect;

    private RDBMSDataSource dataSource;
    
    private StorageType storageType;

    private String catalogName;

    public LiquibaseSchemaAdapter(TableResolver tableResolver, Dialect dialect, RDBMSDataSource dataSource,
            StorageType storageType) {
        this.tableResolver = tableResolver;
        this.dialect = dialect;
        this.dataSource = dataSource;
        this.storageType = storageType;
    }

    public void adapt(Connection connection, Compare.DiffResults diffResults) throws Exception {

        catalogName = connection.getCatalog();

        List<AbstractChange> changeType = findChangeFiles(diffResults);

        if (changeType.isEmpty()) {
            return;
        }

        try {
            DatabaseConnection liquibaseConnection = new liquibase.database.jvm.JdbcConnection(connection);
            liquibaseConnection.setAutoCommit(true);

            liquibase.database.Database database = liquibase.database.DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(liquibaseConnection);

            String filePath = getChangeLogFilePath(changeType);

            Liquibase liquibase = new Liquibase(filePath, new FileSystemResourceAccessor(), database);
            if(LOGGER.isDebugEnabled()) {
                Writer output = new java.io.StringWriter();
                liquibase.update("Liquibase update", output);
                LOGGER.debug("DDL executed by liquibase: " + output.toString());
            } else {
                liquibase.update("Liquibase update");
            }
        } catch (Exception e1) {
            LOGGER.error("execute liquibase update failure", e1); //$NON-NLS-1$
            throw e1;
        }
    }

    protected List<AbstractChange> findChangeFiles(DiffResults diffResults) {
        List<AbstractChange> changeActionList = new ArrayList<AbstractChange>();

        if (!diffResults.getRemoveChanges().isEmpty()) {
            changeActionList.addAll(analyzeRemoveChange(diffResults));
        }

        if (!diffResults.getModifyChanges().isEmpty()) {

            changeActionList.addAll(analyzeModifyChange(diffResults));
        }
        return changeActionList;
    }

    protected String getTableName(FieldMetadata field) {
        String tableName = tableResolver.get(field.getContainingType());
        if (dataSource.getDialectName() == DataSourceDialect.POSTGRES) {
            tableName = tableName.toLowerCase();
        }
        return tableName;
    }

    private String getColumnName(FieldMetadata field) {
        String columnName = tableResolver.get(field);
        if (field instanceof ContainedTypeFieldMetadata) {
            columnName += "_x_talend_id"; //$NON-NLS-1$
        }
        if (field instanceof ReferenceFieldMetadata) {
            columnName += "_" + tableResolver.get(((ReferenceFieldMetadata) field).getReferencedField()); //$NON-NLS-1$
        }
        if (HibernateStorageUtils.isOracle(dataSource.getDialectName())) {
            columnName = columnName.toUpperCase();
        }
        return columnName;
    }

    protected List<AbstractChange> analyzeModifyChange(DiffResults diffResults) {
        List<AbstractChange> changeActionList = new ArrayList<AbstractChange>();
        for (ModifyChange modifyAction : diffResults.getModifyChanges()) {
            MetadataVisitable element = modifyAction.getElement();
            if ((!isContainedComplexFieldTypeMetadata((FieldMetadata) element)
                    || isSimpleTypeFieldMetadata((FieldMetadata) element)
                    || isContainedComplexType((FieldMetadata) element))
                    && !isContainedTypeFieldMetadata((FieldMetadata) element)) {
                FieldMetadata previous = (FieldMetadata) modifyAction.getPrevious();
                FieldMetadata current = (FieldMetadata) modifyAction.getCurrent();

                if (MetadataUtils.isAnonymousType(current.getContainingType())) {
                    continue;
                }

                String defaultValueRule = current.getData(MetadataRepository.DEFAULT_VALUE_RULE);
                defaultValueRule = HibernateStorageUtils.convertedDefaultValue(current.getType().getName(),
                        dataSource.getDialectName(), defaultValueRule, StringUtils.EMPTY);
                String tableName = getTableName(current);
                String columnDataType = getColumnTypeName(current);
                String columnName = getColumnName(current);

                if (current.isMandatory() && !previous.isMandatory() && !isModifyMinOccursForRepeatable(previous, current)) {
                    if (storageType == StorageType.MASTER) {
                        changeActionList
                                .add(generateAddNotNullConstraintChange(defaultValueRule, tableName, columnName, columnDataType));
                    }
                    if (StringUtils.isNotBlank(defaultValueRule)) {
                        changeActionList
                                .add(generateAddDefaultValueChange(defaultValueRule, tableName, columnName, columnDataType));
                    }
                } else if (!current.isMandatory() && previous.isMandatory()) {
                    if (HibernateStorageUtils.isSQLServer(dataSource.getDialectName()) && storageType == StorageType.MASTER) {
                        changeActionList.add(generateDropIndexChange(SQL_SERVER_SCHEMA, tableName,
                                tableResolver.getIndex(columnName, tableName)));
                    }
                    if (storageType == StorageType.MASTER && !isModifyMinOccursForRepeatable(previous, current)) {
                        changeActionList.add(generateDropNotNullConstraintChange(tableName, columnName, columnDataType));
                    }
                    if (!isModifyMinOccursForRepeatable(previous, current) && StringUtils.isNotBlank(defaultValueRule)
                            && HibernateStorageUtils.isMySQL(dataSource.getDialectName())) {
                        changeActionList
                                .add(generateAddDefaultValueChange(defaultValueRule, tableName, columnName, columnDataType));
                    }
                }
            }
        }
        return changeActionList;
    }

    protected List<AbstractChange> analyzeRemoveChange(DiffResults diffResults) {
        List<AbstractChange> changeActionList = new ArrayList<AbstractChange>();

        Map<String, List<String>> dropColumnMap = new HashMap<>();
        Map<String, List<String>> dropFKMap = new HashMap<>();
        Map<String, List<String[]>> dropIndexMap = new HashMap<>();

        for (RemoveChange removeAction : diffResults.getRemoveChanges()) {

            MetadataVisitable element = removeAction.getElement();
            if (element instanceof FieldMetadata && (!isContainedComplexFieldTypeMetadata((FieldMetadata) element)
                    || isSimpleTypeFieldMetadata((FieldMetadata) element) || isContainedComplexType((FieldMetadata) element))) {
                FieldMetadata field = (FieldMetadata) element;

                String tableName = getTableName(field);
                String columnName = getColumnName(field);

                // Need remove the FK constraint first before remove a reference field.
                // FK constraint only exists in master DB.
                if (element instanceof ReferenceFieldMetadata && storageType == StorageType.MASTER) {
                    ReferenceFieldMetadata referenceField = (ReferenceFieldMetadata) element;
                    String fkName = tableResolver.getFkConstraintName(referenceField);
                    if (fkName.isEmpty()) {
                        List<Column> columns = new ArrayList<>();
                        columns.add(new Column(columnName.toLowerCase()));
                        fkName = Constraint.generateName(new ForeignKey().generatedConstraintNamePrefix(),
                                new Table(tableResolver.get(field.getContainingType().getEntity())), columns);
                        if (HibernateStorageUtils.isPostgres(dataSource.getDialectName())) {
                            fkName = fkName.toLowerCase();
                        }
                    }
                    List<String> fkList = dropFKMap.get(tableName);
                    if (fkList == null) {
                        fkList = new ArrayList<String>();
                    }
                    fkList.add(fkName);
                    dropFKMap.put(tableName, fkList);
                }
                List<String> columnList = dropColumnMap.get(tableName);
                if (columnList == null) {
                    columnList = new ArrayList<String>();
                }
                columnList.add(columnName);
                dropColumnMap.put(tableName, columnList);

                List<String[]> indexList = dropIndexMap.get(tableName);
                if (indexList == null) {
                    indexList = new ArrayList<String[]>();
                }
                if (HibernateStorageUtils.isSQLServer(dataSource.getDialectName()) && storageType == StorageType.MASTER) {
                    indexList.add(new String[] { SQL_SERVER_SCHEMA, tableName, tableResolver.getIndex(columnName, tableName) });
                    dropIndexMap.put(tableName, indexList);
                }
            }
        }

        for (Map.Entry<String, List<String[]>> entry : dropIndexMap.entrySet()) {
            List<String[]> dropIndexInfoList = entry.getValue();
            for (String[] dropIndexInfo : dropIndexInfoList) {
                changeActionList.add(generateDropIndexChange(dropIndexInfo[0], dropIndexInfo[1], dropIndexInfo[2]));
            }
        }

        for (Map.Entry<String, List<String>> entry : dropFKMap.entrySet()) {
            List<String> fks = entry.getValue();
            for (String fk : fks) {
                DropForeignKeyConstraintChange dropFKChange = new DropForeignKeyConstraintChange();
                dropFKChange.setBaseTableName(entry.getKey());
                dropFKChange.setConstraintName(fk);
                changeActionList.add(dropFKChange);
            }
        }

        for (Map.Entry<String, List<String>> entry : dropColumnMap.entrySet()) {
            List<String> columns = entry.getValue();
            List<ColumnConfig> columnConfigList = new ArrayList<ColumnConfig>();
            for (String columnName : columns) {
                columnConfigList.add(new ColumnConfig(new liquibase.structure.core.Column(columnName)));
            }

            DropColumnChange dropColumnChange = new DropColumnChange();
            dropColumnChange.setTableName(entry.getKey());
            dropColumnChange.setColumns(columnConfigList);

            changeActionList.add(dropColumnChange);
        }
        return changeActionList;
    }

    protected DropIndexChange generateDropIndexChange(String schemaName, String tableName, String indexName) {
        DropIndexChange dropIndexChange = new DropIndexChange();
        dropIndexChange.setSchemaName(schemaName);
        dropIndexChange.setCatalogName(catalogName);
        dropIndexChange.setTableName(tableName);
        dropIndexChange.setIndexName(indexName);
        return dropIndexChange;
    }

    protected DropNotNullConstraintChange generateDropNotNullConstraintChange(String tableName, String columnName,
            String columnDataType) {
        DropNotNullConstraintChange dropNotNullConstraintChange = new DropNotNullConstraintChange();
        dropNotNullConstraintChange.setTableName(tableName);
        dropNotNullConstraintChange.setColumnName(columnName);
        dropNotNullConstraintChange.setColumnDataType(columnDataType);
        return dropNotNullConstraintChange;
    }

    protected AddDefaultValueChange generateAddDefaultValueChange(String defaultValueRule, String tableName, String columnName,
            String columnDataType) {
        AddDefaultValueChange addDefaultValueChange = new AddDefaultValueChange();
        addDefaultValueChange.setColumnDataType(columnDataType);
        addDefaultValueChange.setColumnName(columnName);
        addDefaultValueChange.setTableName(tableName);
        if (isBooleanType(columnDataType)) {
            addDefaultValueChange.setDefaultValueBoolean(defaultValueRule.equals("1") ? true : false); //$NON-NLS-1$
        } else {
            addDefaultValueChange.setDefaultValue(defaultValueRule);
        }
        return addDefaultValueChange;
    }

    protected AddNotNullConstraintChange generateAddNotNullConstraintChange(String defaultValueRule, String tableName,
            String columnName, String columnDataType) {
        AddNotNullConstraintChange addNotNullConstraintChange = new AddNotNullConstraintChange();
        addNotNullConstraintChange.setColumnDataType(columnDataType);
        addNotNullConstraintChange.setColumnName(columnName);
        addNotNullConstraintChange.setTableName(tableName);
        if (isBooleanType(columnDataType) && StringUtils.isNoneBlank(defaultValueRule)) {
            addNotNullConstraintChange.setDefaultNullValue(defaultValueRule.equals("1") ? "TRUE" : "FALSE"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            addNotNullConstraintChange.setDefaultNullValue(defaultValueRule);
        }
        return addNotNullConstraintChange;
    }

    protected String getChangeLogFilePath(List<AbstractChange> changeType) {
        // create a changelog
        liquibase.changelog.DatabaseChangeLog databaseChangeLog = new liquibase.changelog.DatabaseChangeLog();

        for (AbstractChange change : changeType) {

            // create a changeset
            liquibase.changelog.ChangeSet changeSet = new liquibase.changelog.ChangeSet(UUID.randomUUID().toString(),
                    "administrator", false, false, StringUtils.EMPTY, null, null, true, null, databaseChangeLog); //$NON-NLS-1$
            changeSet.addChange(change);

            // add created changeset to changelog
            databaseChangeLog.addChangeSet(changeSet);
            if (change instanceof DropIndexChange && HibernateStorageUtils.isSQLServer(dataSource.getDialectName())
                    && storageType == StorageType.MASTER) {
                PreconditionContainer preconditionContainer = new PreconditionContainer();
                preconditionContainer.setOnFail(PreconditionContainer.FailOption.MARK_RAN.toString());

                DropIndexChange dropIndexChange = (DropIndexChange) change;
                IndexExistsPrecondition indexExistsPrecondition = new IndexExistsPrecondition();
                indexExistsPrecondition.setSchemaName(dropIndexChange.getSchemaName());
                indexExistsPrecondition.setCatalogName(dropIndexChange.getCatalogName());
                indexExistsPrecondition.setTableName(dropIndexChange.getTableName());
                indexExistsPrecondition.setIndexName(dropIndexChange.getIndexName());

                preconditionContainer.addNestedPrecondition(indexExistsPrecondition);
                changeSet.setPreconditions(preconditionContainer);
            }
        }

        return generateChangeLogFile(databaseChangeLog);
    }

    protected String generateChangeLogFile(liquibase.changelog.DatabaseChangeLog databaseChangeLog) {
        // create a new serializer
        XMLChangeLogSerializer xmlChangeLogSerializer = new XMLChangeLogSerializer();

        FileOutputStream baos = null;
        try {
            File mdmRootFileDir = new File(System.getProperty(MDM_ROOT));
            File changeLogDir = new File(mdmRootFileDir, DATA_LIQUIBASE_CHANGELOG_PATH);

            if (!changeLogDir.exists()) {
                changeLogDir.mkdirs();
            }
            changeLogDir = new File(changeLogDir, DateUtils.format(System.currentTimeMillis(), "yyyyMMdd"));//$NON-NLS-1$
            if (!changeLogDir.exists()) {
                changeLogDir.mkdir();
            }

            File changeLogFile = new File(changeLogDir, DateUtils.format(System.currentTimeMillis(), "yyyyMMddHHmm") + SEPARATOR //$NON-NLS-1$
                    + System.currentTimeMillis() + SEPARATOR + storageType + ".xml"); //$NON-NLS-1$
            if (!changeLogFile.exists()) {
                changeLogFile.createNewFile();
            }
            baos = new FileOutputStream(changeLogFile);
            xmlChangeLogSerializer.write(databaseChangeLog.getChangeSets(), baos);
            return changeLogFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            LOGGER.error("liquibase changelog file can't exist" + e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        } catch (IOException e) {
            LOGGER.error("write liquibase changelog file failure", e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    LOGGER.error("close liquibase changelog file stream failed", e); //$NON-NLS-1$
                }
            }
        }
    }

    protected String getColumnTypeName(FieldMetadata current) {
        int hibernateTypeCode = 0;
        TypeMetadata type = MetadataUtils.getSuperConcreteType(current.getType());

        Object currentLength = CommonUtil.getSuperTypeMaxLength(current.getType(), current.getType());
        Object currentTotalDigits = current.getType().getData(MetadataRepository.DATA_TOTAL_DIGITS);
        Object currentFractionDigits = current.getType().getData(MetadataRepository.DATA_FRACTION_DIGITS);

        int length = currentLength == null ? Column.DEFAULT_LENGTH : Integer.parseInt(currentLength.toString());
        int precision = currentTotalDigits == null ? Column.DEFAULT_PRECISION : Integer.parseInt(currentTotalDigits.toString());
        int scale = currentFractionDigits == null ? Column.DEFAULT_SCALE : Integer.parseInt(currentFractionDigits.toString());

        if (type.getName().equals("short")) { //$NON-NLS-1$
            hibernateTypeCode = java.sql.Types.SMALLINT;
        } else if (type.getName().equals("int") || type.getName().equals("integer")) { //$NON-NLS-1$ //$NON-NLS-2$
            hibernateTypeCode = java.sql.Types.INTEGER;
        } else if (type.getName().equals("long")) { //$NON-NLS-1$
            hibernateTypeCode = java.sql.Types.BIGINT;
        } else if (type.getName().equals("boolean")) { //$NON-NLS-1$
            hibernateTypeCode = java.sql.Types.BOOLEAN;
        } else if (type.getName().equals("date") || type.getName().equals("dateTime") || type.getName().equals("time")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            hibernateTypeCode = java.sql.Types.TIMESTAMP;
        } else if (type.getName().equals("float")) { //$NON-NLS-1$ 
            hibernateTypeCode = java.sql.Types.FLOAT;
        } else if (type.getName().equals("double")) { //$NON-NLS-1$
            hibernateTypeCode = java.sql.Types.DOUBLE;
        } else if (type.getName().equals("decimal")) { //$NON-NLS-1$
            hibernateTypeCode = java.sql.Types.NUMERIC;
        } else {
            hibernateTypeCode = java.sql.Types.VARCHAR;
        }

        return dialect.getTypeName(hibernateTypeCode, length, precision, scale);
    }

    protected boolean isModifyMinOccursForRepeatable(FieldMetadata previous, FieldMetadata current) {
        int previousMinOccurs = previous.getData(MetadataRepository.MIN_OCCURS);
        int previousMaxOccurs = previous.getData(MetadataRepository.MAX_OCCURS);
        int currentMinOccurs = current.getData(MetadataRepository.MIN_OCCURS);
        int currentMxnOccurs = current.getData(MetadataRepository.MAX_OCCURS);

        if (previousMaxOccurs == currentMxnOccurs && currentMxnOccurs == -1) {
            if (previousMinOccurs != currentMinOccurs) {
                return true;
            }
        }
        return false;
    }

    protected boolean isContainedComplexFieldTypeMetadata(FieldMetadata fieldMetadata) {
        return fieldMetadata.getContainingType() instanceof ContainedComplexTypeMetadata
                && (fieldMetadata.getType() instanceof SimpleTypeMetadata);
    }

    protected boolean isSimpleTypeFieldMetadata(FieldMetadata fieldMetadata) {
        return fieldMetadata instanceof SimpleTypeFieldMetadata
                && (fieldMetadata.getType() instanceof SimpleTypeMetadata);
    }

    protected boolean isContainedComplexType(FieldMetadata fieldMetadata) {
        return (fieldMetadata.getContainingType() instanceof ComplexTypeMetadata)
                && (fieldMetadata.getType() instanceof ContainedComplexTypeMetadata);
    }

    protected  boolean isContainedTypeFieldMetadata(FieldMetadata fieldMetadata){
        return fieldMetadata instanceof ContainedTypeFieldMetadata;
    }

    protected boolean isBooleanType(String columnDataType) {
        return columnDataType.equals("bit") || columnDataType.equals("boolean"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
