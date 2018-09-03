/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.datasource;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

public class RDBMSDataSource implements DataSource {

    public static enum ContainsOptimization {
        FULL_TEXT,
        LIKE,
        DISABLED
    }

    public static enum DataSourceDialect {
        H2(255, 1000, 1000),
        ORACLE_10G(4000, 38, 37), // Set to 4000 for 6.0+ versions (older versions will still use 255 for backward compatibility)
        MYSQL(255, 65, 30),
        POSTGRES(255, 1000, 1000),
        SQL_SERVER(4000, 38, 38),
        DB2(255, 38, 38);

        private final int textLimit;

        private final int decimalPrecision;

        private final int decimalScale;

        DataSourceDialect(int textLimit, int decimalPrecision, int decimalScale) {
            this.textLimit = textLimit;
            this.decimalPrecision = decimalPrecision;
            this.decimalScale = decimalScale;
        }

        /**
         * @return A positive integer that indicates a threshold for using clob/text field on field max length.
         * @see org.talend.mdm.commmon.metadata.MetadataRepository#DATA_MAX_LENGTH
         * @see org.talend.mdm.commmon.metadata.FieldMetadata#getData(String)
         */
        public int getTextLimit() {
            return textLimit;
        }

        public int getDecimalPrecision() {
            return decimalPrecision;
        }

        public int getDecimalScale() {
            return decimalScale;
        }
    }

    public static enum SchemaGeneration {
        CREATE,
        VALIDATE,
        UPDATE
    }

    private static final Logger LOGGER = Logger.getLogger(RDBMSDataSource.class);

    private static final String CONTAINER_PLACEHOLDER = "${container}"; //$NON-NLS-1$

    private final boolean generateTechnicalFK;

    private final ContainsOptimization containsOptimization;

    private final Boolean caseSensitiveSearch;

    private final String name;

    private final SchemaGeneration schemaGeneration;

    private final Map<String, String> advancedProperties;

    private final String cacheDirectory;

    private final String initConnectionURL;

    private final String initUserName;

    private final String initPassword;

    private final DataSourceDialect dialect;

    private final String driverClassName;

    private final String password;

    private String indexDirectory;

    private String userName;

    private String connectionURL;

    private String databaseName;

    private int connectionPoolMinSize;

    private int connectionPoolMaxSize;

    private boolean isShared;

    private int nameMaxLength;

    private boolean generateConstraints;

    public RDBMSDataSource(RDBMSDataSource dataSource) {
        caseSensitiveSearch = dataSource.caseSensitiveSearch;
        name = dataSource.name;
        schemaGeneration = dataSource.schemaGeneration;
        advancedProperties = dataSource.advancedProperties;
        cacheDirectory = dataSource.cacheDirectory;
        initConnectionURL = dataSource.initConnectionURL;
        initUserName = dataSource.initUserName;
        initPassword = dataSource.initPassword;
        dialect = dataSource.dialect;
        driverClassName = dataSource.driverClassName;
        password = dataSource.password;
        indexDirectory = dataSource.indexDirectory;
        userName = dataSource.userName;
        connectionURL = dataSource.connectionURL;
        databaseName = dataSource.databaseName;
        connectionPoolMinSize = dataSource.connectionPoolMinSize;
        connectionPoolMaxSize = dataSource.connectionPoolMaxSize;
        containsOptimization = dataSource.containsOptimization;
        generateTechnicalFK = dataSource.generateTechnicalFK;
        generateConstraints = dataSource.generateConstraints;
        defineMaxNamedLength();
    }

    public RDBMSDataSource(String name,
                           String dialectName,
                           String driverClassName,
                           String userName,
                           String password,
                           int connectionPoolMinSize,
                           int connectionPoolMaxSize,
                           String indexDirectory,
                           String cacheDirectory,
                           Boolean caseSensitiveSearch,
                           String schemaGeneration,
                           Boolean generateTechnicalFK,
                           Map<String, String> advancedProperties,
                           String connectionURL,
                           String databaseName,
                           ContainsOptimization containsOptimization,
                           String initPassword,
                           String initUserName,
                           String initConnectionURL,
                           boolean generateConstraints) {
        if ("MySQL".equalsIgnoreCase(dialectName)) { //$NON-NLS-1$
            dialect = DataSourceDialect.MYSQL;
        } else if ("H2".equalsIgnoreCase(dialectName)) { //$NON-NLS-1$
            dialect = DataSourceDialect.H2;
        } else if ("Oracle11g".equalsIgnoreCase(dialectName)) { //$NON-NLS-1$
            dialect = DataSourceDialect.ORACLE_10G;
        } else if ("Oracle10g".equalsIgnoreCase(dialectName)) { //$NON-NLS-1$
            dialect = DataSourceDialect.ORACLE_10G;
        } else if ("SQLServer".equalsIgnoreCase(dialectName)) { //$NON-NLS-1$
            dialect = DataSourceDialect.SQL_SERVER;
        } else if ("Postgres".equalsIgnoreCase(dialectName)) { //$NON-NLS-1$
            dialect = DataSourceDialect.POSTGRES;
        } else if ("DB2".equalsIgnoreCase(dialectName)) { //$NON-NLS-1$
            dialect = DataSourceDialect.DB2;
        } else {
            throw new IllegalArgumentException("No support for database '" + dialectName + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if ("update".equalsIgnoreCase(schemaGeneration)) { //$NON-NLS-1$
            this.schemaGeneration = SchemaGeneration.UPDATE;
        } else if ("validate".equalsIgnoreCase(schemaGeneration)) { //$NON-NLS-1$
            this.schemaGeneration = SchemaGeneration.VALIDATE;
        } else if ("create".equalsIgnoreCase(schemaGeneration)) { //$NON-NLS-1$
            this.schemaGeneration = SchemaGeneration.CREATE;
        } else {
            throw new IllegalArgumentException("No support for schema generation '" + schemaGeneration + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.initPassword = initPassword;
        this.initUserName = initUserName;
        this.initConnectionURL = initConnectionURL;
        this.name = name;
        this.driverClassName = driverClassName;
        this.userName = userName;
        this.password = password;
        this.connectionPoolMinSize = connectionPoolMinSize;
        this.connectionPoolMaxSize = connectionPoolMaxSize;
        this.indexDirectory = indexDirectory;
        this.cacheDirectory = cacheDirectory;
        this.caseSensitiveSearch = caseSensitiveSearch;
        this.connectionURL = connectionURL;
        this.databaseName = databaseName;
        this.advancedProperties = advancedProperties;
        this.containsOptimization = containsOptimization;
        this.generateTechnicalFK = generateTechnicalFK;
        defineMaxNamedLength();
        this.generateConstraints = generateConstraints;
        // Disable full text index for update reports.
        if (XSystemObjects.DC_UPDATE_PREPORT.getName().equals(name)) {
            if (LOGGER.isDebugEnabled() && supportFullText()) {
                LOGGER.debug("Disabling full text for update report storage."); //$NON-NLS-1$
            }
            this.indexDirectory = StringUtils.EMPTY;
        }
    }

    public boolean generateConstraints() {
        return generateConstraints;
    }

    public boolean generateTechnicalFK() {
        return generateTechnicalFK;
    }

    public ContainsOptimization getContainsOptimization() {
        return containsOptimization;
    }

    public boolean isCaseSensitiveSearch() {
        return caseSensitiveSearch;
    }

    public DataSourceDialect getDialectName() {
        return dialect;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public String getCacheDirectory() {
        return cacheDirectory;
    }

    public String getIndexDirectory() {
        return indexDirectory;
    }

    public boolean supportFullText() {
        return !indexDirectory.isEmpty();
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    // Intentionally left with 'friendly' visibility.
    void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    // Intentionally left with 'friendly' visibility.
    void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getInitConnectionURL() {
        return initConnectionURL;
    }

    public String getInitUserName() {
        return initUserName;
    }

    public String getInitPassword() {
        return initPassword;
    }

    public int getConnectionPoolMinSize() {
        return this.connectionPoolMinSize;
    }

    public int getConnectionPoolMaxSize() {
        return this.connectionPoolMaxSize;
    }

    public boolean hasInit() {
        return dialect == DataSourceDialect.H2 || !initConnectionURL.isEmpty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isShared() {
        return isShared;
    }

    @Override
    public void setShared(boolean isShared) {
        this.isShared = isShared;
    }

    private static void replacePlaceholder(DataSource dataSource, String placeholderName, String value) {
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) dataSource;
        // JDBC URL
        String connectionURL = rdbmsDataSource.getConnectionURL();
        String processedConnectionURL;
        RDBMSDataSource.DataSourceDialect dialect = ((RDBMSDataSource) dataSource).getDialectName();
        switch (dialect) {
            case POSTGRES:
                // Postgres always creates lower case database name
                processedConnectionURL = connectionURL.replace(placeholderName, value).toLowerCase();
                break;
            case MYSQL:
                // TMDM-6559: MySQL doesn't like '-' in database name
                processedConnectionURL = connectionURL.replace(placeholderName, value);
                if (processedConnectionURL.indexOf('-') > 0) {
                    // Uses URI-based parsing to prevent replace of '-' in host name.
                    URI uri = URI.create(processedConnectionURL.substring(5));
                    if (uri.getPath().indexOf('-') > 0) {
                        String previousURL = processedConnectionURL;
                        processedConnectionURL = processedConnectionURL.replace(uri.getPath(), uri.getPath().replace('-', '_'));
                        LOGGER.warn("JDBC URL '" + previousURL + "' contains character(s) not supported by MySQL (replaced with '" + processedConnectionURL + "' by MDM)."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
                break;
            case H2:
            case ORACLE_10G:
            case SQL_SERVER:
            default: // default for all databases
                processedConnectionURL = connectionURL.replace(placeholderName, value);
                break;
        }
        rdbmsDataSource.setConnectionURL(processedConnectionURL);
        // Database name
        String databaseName = rdbmsDataSource.getDatabaseName();
        String processedDatabaseName = databaseName.replace(placeholderName, value);
        switch (dialect) {
            case POSTGRES:
                // Postgres always creates lower case database name
                processedDatabaseName = processedDatabaseName.toLowerCase();
                break;
            case MYSQL:
                if (processedDatabaseName.indexOf('-') > 0) {
                    LOGGER.warn("Database name '" + processedDatabaseName + "' contains character(s) not supported by MySQL."); //$NON-NLS-1$ //$NON-NLS-2$
                }
                processedDatabaseName = processedDatabaseName.replace('-', '_'); // TMDM-6559: MySQL doesn't like '-' in
                // database name
                break;
            case H2:
            case ORACLE_10G:
            case SQL_SERVER:
            case DB2:
            default:
                // Nothing to do for other databases
                break;
        }
        rdbmsDataSource.setDatabaseName(processedDatabaseName);
        // User name
        rdbmsDataSource.setUserName(rdbmsDataSource.getUserName().replace(placeholderName, value));
        // Advanced properties
        Map<String, String> advancedProperties = rdbmsDataSource.getAdvancedProperties();
        for (Map.Entry<String, String> entry : advancedProperties.entrySet()) {
            advancedProperties.put(entry.getKey(), entry.getValue().replace(placeholderName, value));
        }
    }

    @Override
    public DataSource transform(String container) {
        RDBMSDataSource copy = new RDBMSDataSource(this);
        // Additional post parsing (replace potential ${container} with container parameter value).
        replacePlaceholder(copy, CONTAINER_PLACEHOLDER, container);
        return copy;
    }

    public SchemaGeneration getSchemaGeneration() {
        return schemaGeneration;
    }

    public Map<String, String> getAdvancedProperties() {
        return advancedProperties;
    }

    /**
     * Getter for nameMaxLength.
     * 
     * @return the nameMaxLength
     */
    public int getNameMaxLength() {
        return this.nameMaxLength;
    }

    public Properties getAdvancedPropertiesIncludeUserInfo() {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : advancedProperties.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        properties.setProperty("user", this.getUserName()); // $NON-NLS-1
        properties.setProperty("password", this.getPassword()); // $NON-NLS-1
        return properties;
    }

    /**
     * Sets the nameMaxLength.
     * 
     * @param nameMaxLength the nameMaxLength to set
     */
    public void setNameMaxLength(int nameMaxLength) {
        this.nameMaxLength = nameMaxLength;
    }

    private void defineMaxNamedLength() {
        switch (getDialectName()) {
        case ORACLE_10G:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Oracle database table name length limit: 30."); //$NON-NLS-1$
            }
            nameMaxLength = 30;
            break;
        case MYSQL:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("MySQL database table name length limit: 64."); //$NON-NLS-1$
            }
            nameMaxLength = 64;
            break;
        case SQL_SERVER:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SQL Server database table name length limit: 128."); //$NON-NLS-1$
            }
            nameMaxLength = 128;
            break;
        case POSTGRES:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Postgres database table length limit: 63."); //$NON-NLS-1$
            }
            nameMaxLength = 63;
            break;
        case DB2:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("DB2 database table name length limit: 30."); //$NON-NLS-1$
            }
            nameMaxLength = 30;
            break;
        case H2:
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No limitation for H2 table name length."); //$NON-NLS-1$
            }
            nameMaxLength = Integer.MAX_VALUE;
            break;
        default:
            throw new IllegalArgumentException("Not supported: " + getDialectName()); //$NON-NLS-1$
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RDBMSDataSource)) {
            return false;
        }

        RDBMSDataSource that = (RDBMSDataSource) o;

        if (connectionPoolMaxSize != that.connectionPoolMaxSize) {
            return false;
        }
        if (connectionPoolMinSize != that.connectionPoolMinSize) {
            return false;
        }
        if (generateTechnicalFK != that.generateTechnicalFK) {
            return false;
        }
        if (isShared != that.isShared) {
            return false;
        }
        if (advancedProperties != null ? !advancedProperties.equals(that.advancedProperties) : that.advancedProperties != null) {
            return false;
        }
        if (cacheDirectory != null ? !cacheDirectory.equals(that.cacheDirectory) : that.cacheDirectory != null) {
            return false;
        }
        if (caseSensitiveSearch != null ? !caseSensitiveSearch.equals(that.caseSensitiveSearch)
                : that.caseSensitiveSearch != null) {
            return false;
        }
        if (connectionURL != null ? !connectionURL.equals(that.connectionURL) : that.connectionURL != null) {
            return false;
        }
        if (containsOptimization != that.containsOptimization) {
            return false;
        }
        if (databaseName != null ? !databaseName.equals(that.databaseName) : that.databaseName != null) {
            return false;
        }
        if (dialect != that.dialect) {
            return false;
        }
        if (driverClassName != null ? !driverClassName.equals(that.driverClassName) : that.driverClassName != null) {
            return false;
        }
        if (indexDirectory != null ? !indexDirectory.equals(that.indexDirectory) : that.indexDirectory != null) {
            return false;
        }
        if (initConnectionURL != null ? !initConnectionURL.equals(that.initConnectionURL) : that.initConnectionURL != null) {
            return false;
        }
        if (initPassword != null ? !initPassword.equals(that.initPassword) : that.initPassword != null) {
            return false;
        }
        if (initUserName != null ? !initUserName.equals(that.initUserName) : that.initUserName != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (password != null ? !password.equals(that.password) : that.password != null) {
            return false;
        }
        if (schemaGeneration != that.schemaGeneration) {
            return false;
        }
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (generateTechnicalFK ? 1 : 0);
        result = 31 * result + (containsOptimization != null ? containsOptimization.hashCode() : 0);
        result = 31 * result + (caseSensitiveSearch != null ? caseSensitiveSearch.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (schemaGeneration != null ? schemaGeneration.hashCode() : 0);
        result = 31 * result + (advancedProperties != null ? advancedProperties.hashCode() : 0);
        result = 31 * result + (cacheDirectory != null ? cacheDirectory.hashCode() : 0);
        result = 31 * result + (initConnectionURL != null ? initConnectionURL.hashCode() : 0);
        result = 31 * result + (initUserName != null ? initUserName.hashCode() : 0);
        result = 31 * result + (initPassword != null ? initPassword.hashCode() : 0);
        result = 31 * result + (dialect != null ? dialect.hashCode() : 0);
        result = 31 * result + (driverClassName != null ? driverClassName.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (indexDirectory != null ? indexDirectory.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (connectionURL != null ? connectionURL.hashCode() : 0);
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        result = 31 * result + connectionPoolMinSize;
        result = 31 * result + connectionPoolMaxSize;
        result = 31 * result + (isShared ? 1 : 0);
        return result;
    }
}
