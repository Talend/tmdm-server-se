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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.util.DateUtils;
import org.talend.mdm.commmon.metadata.compare.Compare;

import com.amalto.core.storage.HibernateStorageUtils;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.RDBMSDataSource;

import liquibase.change.AbstractChange;
import liquibase.change.core.DropIndexChange;
import liquibase.precondition.core.IndexExistsPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

public abstract class AbstractLiquibaseSchemaAdapter {

    protected static final Logger LOGGER = Logger.getLogger(AbstractLiquibaseSchemaAdapter.class);

    private static final String SEPARATOR = "-"; //$NON-NLS-1$

    public static final String DATA_LIQUIBASE_CHANGELOG_PATH = "/data/liquibase-changelog/"; //$NON-NLS-1$

    public static final String MDM_ROOT = "mdm.root"; //$NON-NLS-1$

    protected RDBMSDataSource dataSource;

    protected StorageType storageType;

    public AbstractLiquibaseSchemaAdapter(RDBMSDataSource dataSource,StorageType storageType) {
        this.dataSource = dataSource;
        this.storageType = storageType;
    }

    /**
     * Each change which describes the change/refactoring to apply to the database,
     * Liquibase supports multiple descriptive changes for all major database.
     * @param connection : current connection object.
     * @param diffResults
     * @throws Exception
     */
    public abstract void adapt(Connection connection, Compare.DiffResults diffResults) throws Exception;

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
            LOGGER.error("Liquibase change log file doesn't exist.", e);//$NON-NLS-1$
            return StringUtils.EMPTY;
        } catch (IOException e) {
            LOGGER.error("Writing liquibase change log file failed.", e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    LOGGER.error("Closing liquibase changelog file stream failed.", e); //$NON-NLS-1$
                }
            }
        }
    }
}
