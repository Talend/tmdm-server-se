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
import java.util.List;

import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.RDBMSDataSource;

import liquibase.Liquibase;
import liquibase.change.AbstractChange;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.database.DatabaseConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

public class LiquibaseUpdateReportSchemaAdapter extends AbstractLiquibaseSchemaAdapter {
    
    private static final String TABLE_NAME = "x_update_report";//$NON-NLS-1$

    private static final String PRIMARY_KEY_NAME = "x_uuid";//$NON-NLS-1$

    private static final String PRIMARY_CONSTRAINT_NAME = "pk_update_report";//$NON-NLS-1$

    private static final String PRIMARY_INFO_NAME = "PRIMARY";//$NON-NLS-1$

    public LiquibaseUpdateReportSchemaAdapter(RDBMSDataSource dataSource,StorageType storageType) {
        super(dataSource, storageType);
    }

    @Override
    public void adapt(Connection connection, Compare.DiffResults diffResults) throws Exception {
        List<AbstractChange> changeType = fillChangedFiles();
        if (changeType.isEmpty()) {
            return;
        }
        try {
            DatabaseConnection liquibaseConnection = new liquibase.database.jvm.JdbcConnection(connection);
            liquibaseConnection.setAutoCommit(true);

            liquibase.database.Database database = liquibase.database.DatabaseFactory.getInstance().findCorrectDatabaseImplementation(liquibaseConnection);
            String filePath = getChangeLogFilePath(changeType);
            Liquibase liquibase = new Liquibase(filePath, new FileSystemResourceAccessor(), database);

            if (isPrimaryKeyUUIDExists(database)) {
                return;
            }
            if (LOGGER.isDebugEnabled()) {
                Writer output = new java.io.StringWriter();
                liquibase.update("Liquibase update", output); //$NON-NLS-1$
                LOGGER.debug("DDL executed by liquibase: " + output.toString());
            } else {
                liquibase.update("Liquibase update"); //$NON-NLS-1$
            }
        } catch (Exception e1) {
            LOGGER.error("Executing liquibase update failed.", e1); //$NON-NLS-1$
            throw e1;
        }
    }

    private boolean isPrimaryKeyUUIDExists(liquibase.database.Database database) {
        PrimaryKey example = new PrimaryKey();
        Table table = new Table();
        table.setSchema(new Schema());
        table.setName(getValidTableName());
        example.setTable(table);
        example.setName(PRIMARY_INFO_NAME);

        PrimaryKey primaryKey;
        try {
            primaryKey = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
        } catch (Exception e) {
            LOGGER.error("Executing precondition primary key checking failed.", e);//$NON-NLS-1$
            return false;
        }
        if (primaryKey == null) {
            LOGGER.warn("The specified primary key doesn't exist in the database.");//$NON-NLS-1$
            return false;
        }
        for (Column col : primaryKey.getColumns()) {
            if (PRIMARY_KEY_NAME.equals(col.getName())) {
                return true;
            }
        }
        return false;
    }

    private List<AbstractChange> fillChangedFiles() {
        List<AbstractChange> changeActionList = new ArrayList<>();

        DropPrimaryKeyChange dropPrimaryKeyChange = new DropPrimaryKeyChange();
        dropPrimaryKeyChange.setTableName(getValidTableName());
        changeActionList.add(dropPrimaryKeyChange);

        AddPrimaryKeyChange addPrimaryKeyChange = new AddPrimaryKeyChange();
        addPrimaryKeyChange.setTableName(getValidTableName());
        addPrimaryKeyChange.setColumnNames(PRIMARY_KEY_NAME);
        addPrimaryKeyChange.setConstraintName(PRIMARY_CONSTRAINT_NAME);

        changeActionList.add(addPrimaryKeyChange);
        return changeActionList;
    }

    private String getValidTableName() {
        String dataSourceName = MDMConfiguration.getConfiguration().getProperty("db.default.datasource"); //$NON-NLS-1$
        if ("MySQL8-Default".equals(dataSourceName)) { //$NON-NLS-1$
            return TABLE_NAME.toUpperCase();
        } else {
            return TABLE_NAME;
        }
    }
}
