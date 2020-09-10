/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.Metadata;
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
import liquibase.change.core.DropTableChange;
import liquibase.database.DatabaseConnection;
import liquibase.resource.FileSystemResourceAccessor;

public class LiquibaseSchemaAdapter extends AbstractLiquibaseSchemaAdapter {

    private static final String SQL_SERVER_SCHEMA = "dbo"; //$NON-NLS-1$

    private TableResolver tableResolver;

    private Dialect dialect;

    private String catalogName;

    private Metadata metadata;

    public LiquibaseSchemaAdapter(TableResolver tableResolver, Dialect dialect, RDBMSDataSource dataSource,
            StorageType storageType) {
        super(dataSource, storageType);
        this.tableResolver = tableResolver;
        this.dialect = dialect;
    }

    @Override
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
        return upperOrLowerCase(tableName);
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

                String defaultValue = current.getData(MetadataRepository.DEFAULT_VALUE);
                defaultValue = HibernateStorageUtils.convertedDefaultValue(current.getType().getName(),
                        dataSource.getDialectName(), defaultValue, StringUtils.EMPTY);
                String tableName = getTableName(current);
                String columnDataType = getColumnTypeName(current);
                String columnName = upperOrLowerCase(tableResolver.get(current));

                if (current.isMandatory() && !previous.isMandatory() && !isModifyMinOccursForRepeatable(previous, current)) {
                    if (storageType == StorageType.MASTER) {
                        changeActionList
                                .add(generateAddNotNullConstraintChange(defaultValue, tableName, columnName, columnDataType));
                    }
                    if (StringUtils.isNotBlank(defaultValue)) {
                        changeActionList
                                .add(generateAddDefaultValueChange(defaultValue, tableName, columnName, columnDataType));
                    }
                } else if (!current.isMandatory() && previous.isMandatory()) {
                    if (HibernateStorageUtils.isSQLServer(dataSource.getDialectName()) && storageType == StorageType.MASTER) {
                        changeActionList.add(generateDropIndexChange(SQL_SERVER_SCHEMA, tableName,
                                tableResolver.getIndex(columnName, tableName)));
                    }
                    if (storageType == StorageType.MASTER && !isModifyMinOccursForRepeatable(previous, current)) {
                        changeActionList.add(generateDropNotNullConstraintChange(tableName, columnName, columnDataType));
                    }
                    if (!isModifyMinOccursForRepeatable(previous, current) && StringUtils.isNotBlank(defaultValue)
                            && HibernateStorageUtils.isMySQL(dataSource.getDialectName())) {
                        changeActionList
                                .add(generateAddDefaultValueChange(defaultValue, tableName, columnName, columnDataType));
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
        Set<String> dropTableSet = new HashSet<String>();

        for (RemoveChange removeAction : diffResults.getRemoveChanges()) {

            MetadataVisitable element = removeAction.getElement();
            if (element instanceof FieldMetadata && (!isContainedComplexFieldTypeMetadata((FieldMetadata) element)
                    || isSimpleTypeFieldMetadata((FieldMetadata) element) || isContainedComplexType((FieldMetadata) element))) {
                FieldMetadata field = (FieldMetadata) element;

                String tableName = getTableName(field);
                String columnName = tableResolver.get(field);

                // Remove the table for 0-many field.
                if (field.isMany()) {
                    dropTableSet.add(upperOrLowerCase(tableResolver.getCollectionTableToDrop(field)));
                } else {
                	// Need remove the FK constraint first before remove a reference field.
                	// FK constraint only exists in master DB.
                	if (element instanceof ReferenceFieldMetadata && storageType == StorageType.MASTER) {                
	                    ReferenceFieldMetadata referenceField = (ReferenceFieldMetadata) element;
                        if (!(referenceField.getContainingType().equals(referenceField.getReferencedType())
                                && HibernateStorageUtils.isOracle(dataSource.getDialectName()))) {
                            String fkName = tableResolver.getFkConstraintName(referenceField);
                            if (fkName.isEmpty()) {
                                List<Column> columns = new ArrayList<>();
                                columns.add(new Column(columnName));
                                fkName = Constraint.generateName(new ForeignKey().generatedConstraintNamePrefix(),
                                        new Table(tableResolver.get(field.getContainingType().getEntity())), columns);
                            }
                            List<String> fkList = dropFKMap.get(tableName);
                            if (fkList == null) {
                                fkList = new ArrayList<String>();
                            }
                            fkList.add(upperOrLowerCase(fkName));
                            dropFKMap.put(tableName, fkList);
                        }
	                } 
                    List<String> columnList = dropColumnMap.get(tableName);
                    if (columnList == null) {
                        columnList = new ArrayList<String>();
                    }
                    columnList.add(columnName);
                    dropColumnMap.put(tableName, columnList);
                }                               

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
        
        for (String tableName : dropTableSet) {
            DropTableChange dropTableChange = new DropTableChange();
            dropTableChange.setTableName(tableName);
            changeActionList.add(dropTableChange);
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
    
    private String upperOrLowerCase(String name) {
    	if (HibernateStorageUtils.isOracle(dataSource.getDialectName())) {
    		return name.toUpperCase();
    	} else if (HibernateStorageUtils.isPostgres(dataSource.getDialectName())) {
    		return name.toLowerCase();
    	}
    	return name;
    }
}
