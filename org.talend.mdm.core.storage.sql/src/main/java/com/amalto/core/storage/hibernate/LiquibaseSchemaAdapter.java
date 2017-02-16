/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import liquibase.Liquibase;
import liquibase.change.AbstractChange;
import liquibase.change.core.AddDefaultValueChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.database.DatabaseConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.util.DateUtils;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataVisitable;
import org.talend.mdm.commmon.metadata.compare.Change;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;
import org.talend.mdm.commmon.metadata.compare.ModifyChange;
import org.talend.mdm.commmon.metadata.compare.Compare.DiffResults;

import com.amalto.core.storage.HibernateStorageUtils;
import com.amalto.core.storage.datasource.RDBMSDataSource;

public class LiquibaseSchemaAdapter  {

    private static final String DATA_LIQUBASE_CHANGELOG_PATH = "/data/liqubase-changelog/";

    public static final String MDM_ROOT_URL = "mdm.root.url";

    private static final Logger LOGGER = Logger.getLogger(LiquibaseSchemaAdapter.class);

    private TableResolver tableResolver;
    
    private Map<ImpactAnalyzer.Impact, List<Change>> impacts;

    private Compare.DiffResults diffResults;

    private Dialect dialect;

    private RDBMSDataSource dataSource;

    public LiquibaseSchemaAdapter (TableResolver tableResolver, Map<ImpactAnalyzer.Impact, List<Change>> impacts, Compare.DiffResults diffResults, Dialect dialect, RDBMSDataSource dataSource) {
        this.tableResolver = tableResolver;
        this.impacts = impacts;
        this.diffResults = diffResults;
        this.dialect = dialect;
        this.dataSource = dataSource;
    }

    public void adapt(Connection connection) throws Exception {

        List<AbstractChange> changeType = findChangeFiles(diffResults, tableResolver);

        if (changeType.isEmpty()) {
            return;
        }

        try {

            DatabaseConnection liquibaseConnection = new liquibase.database.jvm.JdbcConnection(connection);

            liquibase.database.Database database = liquibase.database.DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(liquibaseConnection);

            String filePath = getChangeLogFilePath(changeType);

            Liquibase liquibase = new Liquibase(filePath, new FileSystemResourceAccessor(), database);
            liquibase.update("Liquibase update"); //$NON-NLS-1$
        } catch (Exception e1) {
            LOGGER.error("execute liquibase update failure", e1); //$NON-NLS-1$
            throw e1;
        }
    }

    private List<AbstractChange> findChangeFiles(DiffResults diffResults, TableResolver tableResolver) {
        List<AbstractChange> changeActionList = new ArrayList<AbstractChange>();

        List<Change> changeList = impacts.get(ImpactAnalyzer.Impact.MEDIUM);
        changeList.addAll(impacts.get(ImpactAnalyzer.Impact.LOW));

        for (ModifyChange modifyAction : diffResults.getModifyChanges()) {
            MetadataVisitable element = modifyAction.getElement();
            if (element instanceof FieldMetadata) {
                FieldMetadata previous = (FieldMetadata) modifyAction.getPrevious();
                FieldMetadata current = (FieldMetadata) modifyAction.getCurrent();

                String defaultValueRule = ((FieldMetadata) current).getData(MetadataRepository.DEFAULT_VALUE_RULE);
                defaultValueRule = HibernateStorageUtils.convertedDefaultValue(dataSource.getDialectName(), defaultValueRule, "");
                String tableName = tableResolver.get(current.getContainingType().getEntity()).toLowerCase();
                String columnName = tableResolver.get(current);
                String columnDataType = StringUtils.EMPTY;
                columnDataType = getColumnType(current, columnDataType);

                if (current.isMandatory() && !previous.isMandatory() && changeList.contains(modifyAction)) {
                    changeActionList.add(generateAddNotNullConstraintChange(defaultValueRule, tableName, columnName,
                            columnDataType));

                    if (StringUtils.isNotBlank(defaultValueRule)) {
                        changeActionList.add(generateAddDefaultValueChange(defaultValueRule, tableName, columnName,
                                columnDataType));
                    }
                }
            }
        }
        return changeActionList;
    }

    private AddDefaultValueChange generateAddDefaultValueChange(String defaultValueRule, String tableName, String columnName,
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

    private AddNotNullConstraintChange generateAddNotNullConstraintChange(String defaultValueRule, String tableName,
            String columnName, String columnDataType) {
        AddNotNullConstraintChange addNotNullConstraintChange = new AddNotNullConstraintChange();
        addNotNullConstraintChange.setColumnDataType(columnDataType);
        addNotNullConstraintChange.setColumnName(columnName);
        addNotNullConstraintChange.setTableName(tableName);
        if (isBooleanType(columnDataType)) {
            addNotNullConstraintChange.setDefaultNullValue(defaultValueRule.equals("1") ? "TRUE" : "FALSE"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            addNotNullConstraintChange.setDefaultNullValue(defaultValueRule);
        }
        return addNotNullConstraintChange;
    }

    private boolean isBooleanType(String columnDataType) {
        return columnDataType.equals("bit") || columnDataType.equals("boolean"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String getChangeLogFilePath(List<AbstractChange> changeType) {
        // create a changelog
        liquibase.changelog.DatabaseChangeLog databaseChangeLog = new liquibase.changelog.DatabaseChangeLog();

        for (AbstractChange change : changeType) {
            // create a changeset
            liquibase.changelog.ChangeSet changeSet = new liquibase.changelog.ChangeSet(UUID.randomUUID().toString(),
                    "administrator", false, false, StringUtils.EMPTY, null, null, true, null, databaseChangeLog); //$NON-NLS-1$

            changeSet.addChange(change);

            // add created changeset to changelog
            databaseChangeLog.addChangeSet(changeSet);
        }

        return generateChangeLogFile(databaseChangeLog);
    }

    private String generateChangeLogFile(liquibase.changelog.DatabaseChangeLog databaseChangeLog) {
        String changeLogFilePath = StringUtils.EMPTY;
        // create a new serializer
        XMLChangeLogSerializer xmlChangeLogSerializer = new XMLChangeLogSerializer();

        String mdmRootLocation = System.getProperty(MDM_ROOT_URL).replace("file:/", ""); //$NON-NLS-1$
        String filePath = mdmRootLocation + DATA_LIQUBASE_CHANGELOG_PATH;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
            filePath += DateUtils.format(System.currentTimeMillis(), "yyyyMMdd");//$NON-NLS-1$
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }

            changeLogFilePath = filePath + "/" + DateUtils.format(System.currentTimeMillis(), "yyyyMMddHHmm") + "-" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + System.currentTimeMillis() + ".xml"; //$NON-NLS-1$
            File changeLogFile = new File(changeLogFilePath);
            if (!changeLogFile.exists()) {
                changeLogFile.createNewFile();
            }
            FileOutputStream baos = new FileOutputStream(changeLogFile);
            xmlChangeLogSerializer.write(databaseChangeLog.getChangeSets(), baos);
        } catch (FileNotFoundException e) {
            LOGGER.error("liquibase changelog file can't exist" + e); //$NON-NLS-1$
        } catch (IOException e) {
            LOGGER.error("write liquibase changelog file failure", e); //$NON-NLS-1$
        }
        return changeLogFilePath;
    }

    private String getColumnType(FieldMetadata current, String columnDataType) {
        int hibernateTypeCode = 0;
        Object currentLength = current.getData(MetadataRepository.DATA_MAX_LENGTH);

        if (current.getType().getName().equals("string")) { //$NON-NLS-1$
            hibernateTypeCode = java.sql.Types.VARCHAR;
            if (currentLength == null) {
                columnDataType = dialect.getTypeName(hibernateTypeCode, Column.DEFAULT_LENGTH, Column.DEFAULT_PRECISION,
                        Column.DEFAULT_SCALE);
            } else {
                columnDataType = dialect.getTypeName(hibernateTypeCode, Integer.valueOf(currentLength.toString()),
                        Column.DEFAULT_PRECISION, Column.DEFAULT_SCALE);
            }
        } else if (current.getType().getName().equals("int") || current.getType().getName().equals("short") //$NON-NLS-1$ //$NON-NLS-2$
                || current.getType().getName().equals("long") || current.getType().getName().equals("integer")) { //$NON-NLS-1$ //$NON-NLS-2$
            hibernateTypeCode = java.sql.Types.INTEGER;
            columnDataType = dialect.getTypeName(hibernateTypeCode);
        } else if (current.getType().getName().equals("boolean")) { //$NON-NLS-1$
            hibernateTypeCode = java.sql.Types.BOOLEAN;
            columnDataType = dialect.getTypeName(hibernateTypeCode);
        } else if (current.getType().getName().equals("date") || current.getType().getName().equals("datetime")) { //$NON-NLS-1$ //$NON-NLS-2$
            hibernateTypeCode = java.sql.Types.TIMESTAMP;
            columnDataType = dialect.getTypeName(hibernateTypeCode);
        } else if (current.getType().getName().equals("double") || current.getType().getName().equals("float") //$NON-NLS-1$ //$NON-NLS-2$
                || current.getType().getName().equals("demical")) { //$NON-NLS-1$
            hibernateTypeCode = java.sql.Types.DOUBLE;
            columnDataType = dialect.getTypeName(hibernateTypeCode);
        }
        return columnDataType;
    }
}
