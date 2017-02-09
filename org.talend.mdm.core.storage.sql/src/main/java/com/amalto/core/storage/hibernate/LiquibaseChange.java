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
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Column;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataVisitable;
import org.talend.mdm.commmon.metadata.compare.Change;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;
import org.talend.mdm.commmon.metadata.compare.ModifyChange;
import org.talend.mdm.commmon.metadata.compare.Compare.DiffResults;

import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource.DataSourceDialect;

public class LiquibaseChange {

    private static final Logger LOGGER = Logger.getLogger(LiquibaseChange.class);

    private HibernateStorage storage;

    private TableResolver tableResolver;
    
    private SessionFactoryImplementor sessionFactoryImplementor;

    private Map<ImpactAnalyzer.Impact, List<Change>> impacts;

    public LiquibaseChange(HibernateStorage storage, TableResolver tableResolver) {
        this.storage = storage;
        this.tableResolver = tableResolver;
        sessionFactoryImplementor = (SessionFactoryImplementor) storage.getCurrentSession().getSessionFactory();
    }

    public void executeLiquibase(Compare.DiffResults diffResults) throws Exception {

        List<AbstractChange> changeType = findToModifyTypes(diffResults, tableResolver);

        try {
            Connection connection = sessionFactoryImplementor.getConnectionProvider().getConnection();

            DatabaseConnection liquibaseConnection = new liquibase.database.jvm.JdbcConnection(connection);

            liquibase.database.Database database = liquibase.database.DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(liquibaseConnection);

            for (AbstractChange chagne : changeType) {
                String filePath = generantionChangeLogfile(chagne);
                Liquibase liquibase = new Liquibase(filePath, new FileSystemResourceAccessor(), database);
                liquibase.update("Liquibase update");
            }
        } catch (Exception e1) {
            LOGGER.error("execute liquibase update failure", e1);
            throw e1;
        }
    }

    private List<AbstractChange> findToModifyTypes(DiffResults diffResults, TableResolver tableResolver) {
        List<AbstractChange> changeActionList = new ArrayList<AbstractChange>();

        Map<ImpactAnalyzer.Impact, List<Change>> impacts = getImpacts();

        List<Change> changeList = impacts.get(ImpactAnalyzer.Impact.MEDIUM);
        changeList.addAll(impacts.get(ImpactAnalyzer.Impact.LOW));

        for (ModifyChange modifyAction : diffResults.getModifyChanges()) {
            MetadataVisitable element = modifyAction.getElement();
            if (element instanceof ComplexTypeMetadata) {
                // Type modifications may include many things (inheritance changes for instance).
                // impactSort.get(Impact.HIGH).add(modifyAction);
            } else if (element instanceof FieldMetadata) {
                FieldMetadata previous = (FieldMetadata) modifyAction.getPrevious();
                FieldMetadata current = (FieldMetadata) modifyAction.getCurrent();

                String defaultValueRule = ((FieldMetadata) current).getData(MetadataRepository.DEFAULT_VALUE_RULE);
                if (current.isMandatory() && !previous.isMandatory()) {
                    if (changeList.contains(modifyAction)) {
                        String tableName = tableResolver.get(current.getContainingType().getEntity());
                        String columnName = tableResolver.get(current);
                        String columnDataType = "";
                        columnDataType = getColumnType(current, columnDataType);

                        if (StringUtils.isNotBlank(defaultValueRule)) {
                            defaultValueRule = convertedDefaultValue((RDBMSDataSource) storage.getDataSource(), defaultValueRule);
                            AddDefaultValueChange addDefaultValueChange = new AddDefaultValueChange();
                            addDefaultValueChange.setColumnDataType(columnDataType);
                            addDefaultValueChange.setColumnName(columnName);
                            addDefaultValueChange.setTableName(tableName);
                            if (columnDataType.equals("bit") || columnDataType.equals("boolean")) {
                                addDefaultValueChange.setDefaultValueBoolean(defaultValueRule.equals("1") ? true : false);
                            } else {
                                addDefaultValueChange.setDefaultValue(defaultValueRule);
                            }
                            changeActionList.add(addDefaultValueChange);
                        }
                        AddNotNullConstraintChange addNotNullConstraintChange = new AddNotNullConstraintChange();
                        addNotNullConstraintChange.setColumnDataType(columnDataType);
                        addNotNullConstraintChange.setColumnName(columnName);
                        addNotNullConstraintChange.setTableName(tableName);
                        if (columnDataType.equals("bit") || columnDataType.equals("boolean")) {
                            addNotNullConstraintChange.setDefaultNullValue(defaultValueRule.equals("1") ? "TRUE" : "FALSE");
                        } else {
                            addNotNullConstraintChange.setDefaultNullValue(defaultValueRule);
                        }
                        changeActionList.add(addNotNullConstraintChange);
                    }
                }
            }
        }
        return changeActionList;
    }

    private String generantionChangeLogfile(AbstractChange changeType) {
        String changeLogFilePath = "";
        // create a changelog
        liquibase.changelog.DatabaseChangeLog databaseChangeLog = new liquibase.changelog.DatabaseChangeLog();

        // create a changeset
        liquibase.changelog.ChangeSet changeSet = new liquibase.changelog.ChangeSet(UUID.randomUUID().toString(),
                "administrator", false, false, "", null, null, true, null, databaseChangeLog);

        changeSet.addChange(changeType);

        // add created changeset to changelog
        databaseChangeLog.addChangeSet(changeSet);

        // create a new serializer
        XMLChangeLogSerializer xmlChangeLogSerializer = new XMLChangeLogSerializer();

        String mdmRootLocation = System.getProperty("mdmRootLocation");
        String filePath = mdmRootLocation + "/data/liqubase-changelog/"
                + DateUtils.format(System.currentTimeMillis(), "yyyyMMdd");
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
            changeLogFilePath = filePath + "/" + changeType + "-" + System.currentTimeMillis() + ".xml";
            File changeLogFile = new File(changeLogFilePath);
            if (!changeLogFile.exists()) {
                changeLogFile.createNewFile();
            }
            FileOutputStream baos = new FileOutputStream(changeLogFile);
            xmlChangeLogSerializer.write(databaseChangeLog.getChangeSets(), baos);
        } catch (FileNotFoundException e) {
            LOGGER.error("liquibase changelog file can't exist" + e);
        } catch (IOException e) {
            LOGGER.error("write liquibase changelog file failure", e);
        }
        return changeLogFilePath;
    }

    private String getColumnType(FieldMetadata current, String columnDataType) {
        int hibernateTypeCode = 0;
        Object currentLength = current.getData(MetadataRepository.DATA_MAX_LENGTH);
        Dialect dialect = sessionFactoryImplementor.getDialect();

        if (current.getType().getName().equals("string")) {
            hibernateTypeCode = java.sql.Types.VARCHAR;
            if (currentLength == null) {
                columnDataType = dialect.getTypeName(hibernateTypeCode, Column.DEFAULT_LENGTH, Column.DEFAULT_PRECISION,
                        Column.DEFAULT_SCALE);
            } else {
                columnDataType = dialect.getTypeName(hibernateTypeCode, Integer.valueOf(currentLength.toString()),
                        Column.DEFAULT_PRECISION, Column.DEFAULT_SCALE);
            }
        } else if (current.getType().getName().equals("int") || current.getType().getName().equals("short")
                || current.getType().getName().equals("long") || current.getType().getName().equals("integer")) {
            hibernateTypeCode = java.sql.Types.INTEGER;
            columnDataType = dialect.getTypeName(hibernateTypeCode);
        } else if (current.getType().getName().equals("boolean")) {
            hibernateTypeCode = java.sql.Types.BOOLEAN;
            columnDataType = dialect.getTypeName(hibernateTypeCode);
        } else if (current.getType().getName().equals("date") || current.getType().getName().equals("datetime")) {
            hibernateTypeCode = java.sql.Types.DATE;
            columnDataType = dialect.getTypeName(hibernateTypeCode);
        } else if (current.getType().getName().equals("double") || current.getType().getName().equals("float")
                || current.getType().getName().equals("demical")) {
            hibernateTypeCode = java.sql.Types.DOUBLE;
            columnDataType = dialect.getTypeName(hibernateTypeCode);
        } else if (current.getType().getName().equals("int")) {
            columnDataType = dialect.getTypeName(hibernateTypeCode);
        }
        return columnDataType;
    }

    public String convertedDefaultValue(RDBMSDataSource dataSource, String defaultValueRule) {
        String covertValue = defaultValueRule;
        DataSourceDialect dialectName = dataSource.getDialectName();
        if (defaultValueRule.equalsIgnoreCase(MetadataRepository.FN_FALSE)) {
            if (dialectName == RDBMSDataSource.DataSourceDialect.SQL_SERVER
                    || dialectName == RDBMSDataSource.DataSourceDialect.ORACLE_10G) {
                covertValue = "0"; //$NON-NLS-1$
            } else {
                covertValue = Boolean.FALSE.toString();
            }
        } else if (defaultValueRule.equalsIgnoreCase(MetadataRepository.FN_TRUE)) {
            if (dialectName == RDBMSDataSource.DataSourceDialect.SQL_SERVER
                    || dialectName == RDBMSDataSource.DataSourceDialect.ORACLE_10G) {
                covertValue = "1"; //$NON-NLS-1$
            } else {
                covertValue = Boolean.TRUE.toString();
            }
        } else if (defaultValueRule.startsWith("\"") && defaultValueRule.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
            covertValue = defaultValueRule.replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return covertValue;
    }
    
    public Map<ImpactAnalyzer.Impact, List<Change>> getImpacts() {
        return impacts;
    }

    public void setImpacts(Map<ImpactAnalyzer.Impact, List<Change>> impacts) {
        this.impacts = impacts;
    }

    public TableResolver getTableResolver() {
        return tableResolver;
    }

    public void setTableResolver(TableResolver tableResolver) {
        this.tableResolver = tableResolver;
    }

}
