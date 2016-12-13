/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.datasource;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class RDBMSDataSourceBuilder {

    public static final int CONNECTION_POOL_MAX_SIZE_DEFAULT = 50;
    
    private boolean generateTechnicalFK = true;

    private RDBMSDataSource.ContainsOptimization containsOptimization = RDBMSDataSource.ContainsOptimization.FULL_TEXT;

    private boolean caseSensitiveSearch = true;

    private String name;

    private RDBMSDataSource.SchemaGeneration schemaGeneration = RDBMSDataSource.SchemaGeneration.UPDATE;

    private Map<String, String> advancedProperties = Collections.emptyMap();

    private String cacheDirectory;

    private String initConnectionURL;

    private String initUserName;

    private String initPassword;

    private RDBMSDataSource.DataSourceDialect dialect;

    private String driverClassName;

    private String password;

    private String indexDirectory = StringUtils.EMPTY;

    private String userName;

    private String connectionURL;

    private String databaseName;

    private int connectionPoolMinSize = 5;

    private int connectionPoolMaxSize = CONNECTION_POOL_MAX_SIZE_DEFAULT;

    private boolean generateConstraints;

    private RDBMSDataSourceBuilder() {
    }

    public static RDBMSDataSourceBuilder newBuilder() {
        return new RDBMSDataSourceBuilder();
    }

    public RDBMSDataSourceBuilder generateConstraints(boolean generateConstraints) {
        this.generateConstraints = generateConstraints;
        return this;
    }

    public RDBMSDataSourceBuilder generateTechnicalFK(boolean generateTechnicalFK) {
        this.generateTechnicalFK = generateTechnicalFK;
        return this;
    }

    public RDBMSDataSourceBuilder containsOptimization(RDBMSDataSource.ContainsOptimization containsOptimization) {
        this.containsOptimization = containsOptimization;
        return this;
    }

    public RDBMSDataSourceBuilder caseSensitiveSearch(Boolean caseSensitiveSearch) {
        this.caseSensitiveSearch = caseSensitiveSearch;
        return this;
    }

    public RDBMSDataSourceBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public RDBMSDataSourceBuilder schemaGeneration(RDBMSDataSource.SchemaGeneration schemaGeneration) {
        this.schemaGeneration = schemaGeneration;
        return this;
    }

    public RDBMSDataSourceBuilder advancedProperties(Map<String, String> advancedProperties) {
        this.advancedProperties = advancedProperties;
        return this;
    }

    public RDBMSDataSourceBuilder cacheDirectory(String cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        return this;
    }

    public RDBMSDataSourceBuilder initConnectionURL(String initConnectionURL) {
        this.initConnectionURL = initConnectionURL;
        return this;
    }

    public RDBMSDataSourceBuilder initUserName(String initUserName) {
        this.initUserName = initUserName;
        return this;
    }

    public RDBMSDataSourceBuilder initPassword(String initPassword) {
        this.initPassword = initPassword;
        return this;
    }

    public RDBMSDataSourceBuilder dialect(RDBMSDataSource.DataSourceDialect dialect) {
        this.dialect = dialect;
        return this;
    }

    public RDBMSDataSourceBuilder driverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }

    public RDBMSDataSourceBuilder password(String password) {
        this.password = password;
        return this;
    }

    public RDBMSDataSourceBuilder indexDirectory(String indexDirectory) {
        this.indexDirectory = indexDirectory;
        return this;
    }

    public RDBMSDataSourceBuilder userName(String userName) {
        this.userName = userName;
        return this;
    }

    public RDBMSDataSourceBuilder connectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
        return this;
    }

    public RDBMSDataSourceBuilder databaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public RDBMSDataSourceBuilder connectionPoolMinSize(int connectionPoolMinSize) {
        this.connectionPoolMinSize = connectionPoolMinSize;
        return this;
    }

    public RDBMSDataSourceBuilder connectionPoolMaxSize(int connectionPoolMaxSize) {
        this.connectionPoolMaxSize = connectionPoolMaxSize;
        return this;
    }

    public RDBMSDataSource build() {
        return new RDBMSDataSource(name, dialect.name(), driverClassName, userName, password, connectionPoolMinSize,
                connectionPoolMaxSize, indexDirectory, cacheDirectory, caseSensitiveSearch, schemaGeneration.name(),
                generateTechnicalFK, advancedProperties, connectionURL, databaseName, containsOptimization, initPassword,
                initUserName, initConnectionURL, generateConstraints);
    }
}
