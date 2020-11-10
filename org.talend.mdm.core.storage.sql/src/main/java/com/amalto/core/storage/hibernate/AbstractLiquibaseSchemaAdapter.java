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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.util.DateUtils;
import org.talend.mdm.commmon.metadata.compare.Compare;

import com.amalto.core.storage.HibernateStorageUtils;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource.DataSourceDialect;

import liquibase.change.AbstractChange;
import liquibase.change.core.DropIndexChange;
import liquibase.precondition.core.IndexExistsPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

public abstract class AbstractLiquibaseSchemaAdapter {

    protected static final Logger LOGGER = LogManager.getLogger(AbstractLiquibaseSchemaAdapter.class);

    private static final String SEPARATOR = "-"; //$NON-NLS-1$

    public static final String DATA_LIQUIBASE_CHANGELOG_PATH = "/data/liquibase-changelog/"; //$NON-NLS-1$

    public static final String MDM_ROOT = "mdm.root"; //$NON-NLS-1$

    protected static RDBMSDataSource dataSource;

    protected StorageType storageType;

    public AbstractLiquibaseSchemaAdapter(RDBMSDataSource dataSource,StorageType storageType) {
        AbstractLiquibaseSchemaAdapter.dataSource = dataSource;
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

    protected boolean existsForeignKeyConstraints(String tableName, String constraintName) {
        String result = StringUtils.EMPTY;
        try {
            ExistenceFKCheckingEnum curFKCheckingSource = ExistenceFKCheckingEnum.selectDataSource(((RDBMSDataSource) dataSource).getDialectName());
            String sql = curFKCheckingSource.getSqlString();
            List<String> parameters = new ArrayList<>();
            parameters.add(curFKCheckingSource.getQualityName(tableName));
            parameters.add(curFKCheckingSource.getQualityName(constraintName));
            result = executeSQL(sql, parameters);
            if (Integer.parseInt(result) > 0) {
                LOGGER.info("Query retrieves foreign key constraints on the given table "+ tableName + " with constraint name " + constraintName);
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute sql statement, \n caused by ", e);
        }
        return false;
    }

    private static String executeSQL(String sql, List<String> parameters) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        String result = StringUtils.EMPTY;
        try {
            Properties properties = dataSource.getAdvancedPropertiesIncludeUserInfo();
            connection = DriverManager.getConnection(dataSource.getConnectionURL(), properties);
            statement = connection.prepareStatement(sql);
            for (int i = 0; i < parameters.size(); i++) {
                statement.setString(i + 1, parameters.get(i));
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                result = rs.getString(1);
            }
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.error("Unexpected error when closing connection.", e);
            }
        }
        return result;
    }

    /**
     * Query below returns their number of foreign key constrants against different database.
     */
    private static enum ExistenceFKCheckingEnum {
        INNER_POSTGRES(DataSourceDialect.POSTGRES) {

            @Override
            protected String getSqlString() {
                return "SELECT count(*) FROM information_schema.table_constraints WHERE TABLE_NAME = ? AND CONSTRAINT_NAME = ?"; //$NON-NLS-1$
            }

            @Override
            protected String getQualityName(String rawName) {
                return rawName.toLowerCase().trim();
            }
        },

        INNER_H2(DataSourceDialect.H2) {

            @Override
            protected String getSqlString() {
                return "SELECT count(*) FROM INFORMATION_SCHEMA.CONSTRAINTS WHERE TABLE_NAME = ? AND CONSTRAINT_NAME = ?"; //$NON-NLS-1$
            }

            @Override
            protected String getQualityName(String rawName) {
                return rawName.toUpperCase().trim();
            }
        },

        INNER_MYSQL(DataSourceDialect.MYSQL) {

            @Override
            protected String getSqlString() {
                return "SELECT count(*) FROM information_schema.referential_constraints WHERE TABLE_NAME = ? AND CONSTRAINT_NAME = ?"; //$NON-NLS-1$
            }
        },

        INNER_ORACLE_10G(DataSourceDialect.ORACLE_10G) {

            @Override
            protected String getSqlString() {
                return "SELECT count(*) FROM USER_CONSTRAINTS WHERE TABLE_NAME = ? AND CONSTRAINT_NAME = ?"; //$NON-NLS-1$
            }

            @Override
            protected String getQualityName(String rawName) {
                return rawName.toUpperCase().trim();
            }
        },

        INNER_SQL_SERVER(DataSourceDialect.SQL_SERVER) {

            @Override
            protected String getSqlString() {
                return "SELECT count(*) FROM sys.foreign_keys fk INNER JOIN sys.tables t ON t.object_id = fk.parent_object_id WHERE t.name = ? AND fk.name = ?"; //$NON-NLS-1$
            }
        },

        INNER_DB2(DataSourceDialect.DB2) {

            @Override
            protected String getSqlString() {
                return "SELECT count(*) FROM syscat.references WHERE tabschema = ? AND constname = ?"; //$NON-NLS-1$
            }
        };

      private DataSourceDialect dataSourceDialect;

      private ExistenceFKCheckingEnum(DataSourceDialect dataSourceDialect) {
          this.dataSourceDialect = dataSourceDialect;
      }

      protected abstract String getSqlString();

      protected String getQualityName(String rawName) {
          return rawName;
      }

      private static ExistenceFKCheckingEnum selectDataSource(DataSourceDialect dataSourceDialect) {
          for (ExistenceFKCheckingEnum item : ExistenceFKCheckingEnum.values()) {
              if (item.dataSourceDialect.equals(dataSourceDialect)) {
                  return item;
              }
          }
          throw new NotImplementedException("Support for repeatable element not implemented for dialect '" + dataSourceDialect + "'.");
      }
  }
}
