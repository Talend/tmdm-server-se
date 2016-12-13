/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.spi.Configurable;

public class DBCPConnectionProvider implements ConnectionProvider, Configurable, org.hibernate.service.spi.Stoppable {

    private static final Logger log = Logger.getLogger(DBCPConnectionProvider.class);

    private static final String PREFIX = "hibernate.dbcp."; //$NON-NLS-1$

    private BasicDataSource ds;

    public Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
        } finally {
            logStatistics();
        }
        return conn;
    }

    public void closeConnection(Connection conn) throws SQLException {
        try {
            conn.close();
        } finally {
            logStatistics();
        }
    }

    public void close() throws HibernateException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Closing DBCP connection provider.");
            }
            ds.close();
        } catch (SQLException e) {
            log.error("Unable to close connection pool.", e);
        }
    }

    protected void logStatistics() {
        if (log.isDebugEnabled()) {
            log.debug("active: " + ds.getNumActive() + " (max: " + ds.getMaxTotal() + ")   " + "idle: " + ds.getNumIdle()
                    + "(max: " + ds.getMaxIdle() + ")");
        }
    }

    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return ConnectionProvider.class.equals(unwrapType) || DBCPConnectionProvider.class.isAssignableFrom(unwrapType)
                || DataSource.class.isAssignableFrom(unwrapType);

    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        if (ConnectionProvider.class.equals(unwrapType) || DBCPConnectionProvider.class.isAssignableFrom(unwrapType)) {
            return (T) this;
        } else if (DataSource.class.isAssignableFrom(unwrapType)) {
            return (T) ds;
        } else {
            throw new UnknownUnwrapTypeException(unwrapType);
        }
    }

    @Override
    public void configure(Map props) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Configure DBCPConnectionProvider");
            }
            // DBCP properties used to create the BasicDataSource
            Properties dbcpProperties = new Properties();
            // DriverClass & url
            String jdbcDriverClass = (String) props.get(Environment.DRIVER);
            String jdbcUrl = (String) props.get(Environment.URL);
            dbcpProperties.put("driverClassName", jdbcDriverClass);
            dbcpProperties.put("url", jdbcUrl);
            // Username / password
            String username = (String) props.get(Environment.USER);
            String password = (String) props.get(Environment.PASS);
            dbcpProperties.put("username", username);
            dbcpProperties.put("password", password);
            // Isolation level
            String isolationLevel = (String) props.get(Environment.ISOLATION);
            if ((isolationLevel != null) && (isolationLevel.trim().length() > 0)) {
                dbcpProperties.put("defaultTransactionIsolation", isolationLevel);
            }
            // Turn off autocommit
            dbcpProperties.put("defaultAutoCommit", String.valueOf(Boolean.FALSE));
            // Pool size
            String poolSize = (String) props.get(Environment.POOL_SIZE);
            if ((poolSize != null) && (poolSize.trim().length() > 0) && (Integer.parseInt(poolSize) > 0)) {
                dbcpProperties.put("maxActive", poolSize);
            }
            // Copy all DBCP properties removing the prefix
            for (Object o : props.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String key = (String) entry.getKey();
                if (key.startsWith(PREFIX)) {
                    String property = key.substring(PREFIX.length());
                    String value = (String) entry.getValue();
                    dbcpProperties.put(property, value);
                }
            }
            // Some debug info
            if (log.isDebugEnabled()) {
                StringWriter sw = new StringWriter();
                dbcpProperties.list(new PrintWriter(sw, true));
                log.debug(sw.toString());
            }
            // Let the factory create the pool
            ds = BasicDataSourceFactory.createDataSource(dbcpProperties);
            // The BasicDataSource has lazy initialization borrowing a connection will start the DataSource and make
            // sure it is configured correctly.
            Connection conn = ds.getConnection();
            conn.close();
            // Log pool statistics before continuing.
            logStatistics();
        } catch (Exception e) {
            String message = "Could not create a DBCP pool";
            log.error(message, e);
            if (ds != null) {
                try {
                    ds.close();
                } catch (Exception e2) {
                    log.debug("Unable to close DBCP after failed initialization.", e);
                }
                ds = null;
            }
            throw new HibernateException(message, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Configure DBCPConnectionProvider complete");
        }
    }

    @Override
    public void stop() {
        close();
    }
}
