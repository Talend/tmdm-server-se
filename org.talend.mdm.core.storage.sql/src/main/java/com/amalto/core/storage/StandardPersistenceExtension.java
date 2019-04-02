/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.objects.configurationinfo.ConfigurationHelper;
import com.amalto.core.server.PersistenceExtension;
import com.amalto.core.server.Server;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.hibernate.LiquibaseUpdateReportSchemaAdapter;
import com.amalto.core.storage.prepare.JDBCStorageInitializer;
import com.amalto.core.storage.prepare.StorageInitializer;

public class StandardPersistenceExtension implements PersistenceExtension {

    private static final Logger LOGGER = Logger.getLogger(StandardPersistenceExtension.class);

    private static final String DATAMODEL_CONTAINER_NAME = "amaltoOBJECTSDataModel";//$NON-NLS-1$

    private static final String INIT_DB_RESOURCE_PATH = "/com/amalto/core/initdb/extensiondata/datamodel/UpdateReport"; //$NON-NLS-1$

    private RDBMSDataSource dataSource;

    @Override
    public boolean accept(Server server) {
        final StorageAdmin storageAdmin = server.getStorageAdmin();
        String dataSourceName = storageAdmin.getDatasource(UpdateReportPOJO.DATA_CLUSTER);

        if (!server.hasDataSource(dataSourceName, UpdateReportPOJO.DATA_CLUSTER, StorageType.MASTER)) {
            LOGGER.warn("Can not initialize " + StorageType.MASTER + " storage for '" + UpdateReportPOJO.DATA_CLUSTER + "': data source '" + dataSourceName + "' configuration is incomplete."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            return false;
        }
        boolean isClusterMode = MDMConfiguration.isClusterEnabled();
        if (isClusterMode && isDBInitialized(server, dataSourceName)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isDBInitialized(Server server, String dataSourceName) {
        DataSourceDefinition dataSourceDef = server.getDefinition(dataSourceName, UpdateReportPOJO.DATA_CLUSTER);
        dataSource = (RDBMSDataSource) dataSourceDef.get(StorageType.MASTER);
        Storage simpleStorage = (Storage) UpdateReportStorageProxy.newInstance(new Class[] { Storage.class }, dataSource);
        StorageInitializer initializer = new JDBCStorageInitializer();

        return initializer.isInitialized(simpleStorage) && isUpdateReportExisted(simpleStorage);
    }

    /**
     * The method major use in Oracle DB to check if table <b>UpdateReport</b> exists or not.
     * @param storage
     * @return
     */
    private boolean isUpdateReportExisted(Storage storage) {
        if (!HibernateStorageUtils.isOracle(dataSource.getDialectName())) {
            return true;
        }
        try {
            Class.forName(dataSource.getDriverClassName());
            try (Connection connection = DriverManager.getConnection(dataSource.getConnectionURL(), dataSource.getUserName(),
                    dataSource.getPassword()); Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM user_tables WHERE table_name = 'X_UPDATE_REPORT'"); //$NON-NLS-1$
                if (resultSet.next()) {
                    return resultSet.getInt(1) == 1;
                } else {
                    return false;
                }
            } catch (SQLException e) {
                LOGGER.error("Exception occurred during checking the query table exists.", e);//$NON-NLS-1$
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during processing querying of Oracle database", e);//$NON-NLS-1$
        }
    }

    @Override
    public void update() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dataSource.getConnectionURL(), dataSource.getUserName(), dataSource.getPassword());

            LiquibaseUpdateReportSchemaAdapter liquibaseChange = new LiquibaseUpdateReportSchemaAdapter(dataSource, StorageType.MASTER);
            liquibaseChange.adapt(connection, new Compare.DiffResults());
        } catch (Exception e) {
            String msg = "Unable to complete database schema update, execute liquibase failed."; //$NON-NLS-1$
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("Failed to close connection for liquibase.", e);//$NON-NLS-1$
                }
            }
        }
        try {
            updateUpdateReportXSDToDB();
        } catch (Exception e) {
            LOGGER.error("Could not delete UpdateReport document.", e); //$NON-NLS-1$
        }
    }

    private void updateUpdateReportXSDToDB() {
        try {
            InputStream in = StandardPersistenceExtension.class.getResourceAsStream(INIT_DB_RESOURCE_PATH);
            String xmlString = IOUtils.toString(in, StandardCharsets.UTF_8.name());
            String uniqueID = UpdateReportPOJO.DATA_CLUSTER;
            ConfigurationHelper.deleteDocument(DATAMODEL_CONTAINER_NAME, uniqueID);
            ConfigurationHelper.putDocument(DATAMODEL_CONTAINER_NAME, xmlString, uniqueID);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update the latest Update Report XSD to system table.", e); //$NON-NLS-1$
        }
    }

    private static class UpdateReportStorageProxy implements InvocationHandler {

        private static RDBMSDataSource dataSource;

        public static Object newInstance(Class<Storage>[] interfaces, RDBMSDataSource myDataSource) {
            dataSource = myDataSource;
            return Proxy.newProxyInstance(UpdateReportStorageProxy.class.getClassLoader(), interfaces, new UpdateReportStorageProxy());
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            if (methodName.equals("getDataSource") && returnType.isAssignableFrom(DataSource.class)) {//$NON-NLS-1$
                return dataSource;
            }
            throw new UnsupportedOperationException();
        }
    }
}
