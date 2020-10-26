/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate.mapping;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.schema.extract.spi.ColumnInformation;
import org.hibernate.tool.schema.extract.spi.TableInformation;
import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.hibernate.OracleCustomDialect;

@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
public class MDMTable extends Table {

    private static final String LONGTEXT = "longtext";

    private static RDBMSDataSource dataSource;

    private static final Logger LOGGER = LogManager.getLogger(MDMTable.class);

    public MDMTable(Namespace namespace, Identifier physicalTableName, String subselect, boolean isAbstract) {
        super(namespace, physicalTableName, subselect, isAbstract);
    }

    @Override
    public Iterator sqlAlterStrings(
            Dialect dialect,
            Metadata metadata,
            TableInformation tableInfo,
            Identifier defaultCatalog,
            Identifier defaultSchema) throws HibernateException {

        String tableName = tableInfo.getName().getTableName().getText();
        StringBuilder root = new StringBuilder("alter table ").append(tableName).append(' ');
        Iterator iter = getColumnIterator();
        List results = new ArrayList();

        while (iter.hasNext()) {
            Column column = (Column) iter.next();

            ColumnInformation columnInfo = tableInfo.getColumn(Identifier.toIdentifier(column.getName(), false));

            String sqlType = column.getSqlType(dialect, metadata);
            if (column.getSqlTypeCode() == null) {
                column.setSqlTypeCode(column.getSqlTypeCode(metadata));
            }
            String defaultValue = column.getDefaultValue();
            String columnName = column.getQuotedName(dialect);
            if (columnInfo == null) {
                // the column doesnt exist at all.
                StringBuilder alter = new StringBuilder(root.toString()).append(dialect.getAddColumnString()).append(' ')
                        .append(columnName).append(' ').append(sqlType);

                alter.append(convertDefaultValue(dialect, sqlType, defaultValue));

                if (column.isNullable()) {
                    alter.append(dialect.getNullColumnString());
                } else {
                    alter.append(" not null");
                }

                // add the UK str
                alter.append(generateUK(dialect, column));

                if (column.hasCheckConstraint() && dialect.supportsColumnCheck()) {
                    alter.append(" check(").append(column.getCheckConstraint()).append(')');
                }

                String columnComment = column.getComment();
                if (columnComment != null) {
                    alter.append(dialect.getColumnComment(columnComment));
                }

                alter.append(dialect.getAddColumnSuffixString());
                results.add(alter.toString());
                LOGGER.info("ALTER TABLE [" + tableName + "] command ==> " + alter.toString());
            } else if (MDMTableUtils.isAlterColumnField(column, columnInfo, dialect)) {
                StringBuilder alter = new StringBuilder(root.toString());

                if (dialect instanceof SQLServerDialect || dialect instanceof PostgreSQL94Dialect) {
                    alter.append(" ALTER COLUMN ");
                } else {
                    alter.append(" MODIFY ");
                }
                alter.append(' ').append(columnName).append(' ');
                if (dialect instanceof PostgreSQL94Dialect) {
                    alter.append("TYPE ");
                }
                alter.append(sqlType);
                alter.append(convertDefaultValue(dialect, sqlType, defaultValue));

                if (column.isNullable()) {
                    if (dialect instanceof Oracle8iDialect) {
                        alter.append(" check( ").append(columnName).append(" is null )");
                    } else {
                        alter.append(dialect.getNullColumnString());
                    }
                } else {
                    if (dialect instanceof PostgreSQL94Dialect) {
                        alter.append(", alter column ").append(columnName).append(" set not null ");
                    } else if (dialect instanceof Oracle8iDialect) {
                        alter.append(" check( ").append(columnName).append(" is not null )");
                    } else {
                        alter.append(" not null ");
                    }
                }

                // add the UK str
                alter.append(generateUK(dialect, column));

                if (column.hasCheckConstraint() && dialect.supportsColumnCheck()) {
                    alter.append(" check(").append(column.getCheckConstraint()).append(')');
                }

                String columnComment = column.getComment();
                if (columnComment != null) {
                    alter.append(dialect.getColumnComment(columnComment));
                }

                alter.append(dialect.getAddColumnSuffixString());
                LOGGER.info("ALTER TABLE [" + tableName + "] command ==> " + alter.toString());
                results.add(alter.toString());
            } else if (StringUtils.isNotBlank(defaultValue) && !isDateType(sqlType)) {
                StringBuilder alter = new StringBuilder(root.toString());
                boolean needAlterDefaultValue = true;
                if (dialect instanceof OracleCustomDialect) {
                    alter.append(" MODIFY ").append(columnName).append(" DEFAULT ").append(defaultValue);
                } else if (dialect instanceof SQLServerDialect) {
                    String existedDefaultValue = getDefaultValueForColumn(tableName, columnName);
                    if (StringUtils.isNotBlank(existedDefaultValue) && existedDefaultValue.equals(defaultValue)) {
                        needAlterDefaultValue = false;
                    } else {
                        String alterDropConstraintSQL = generateAlterDefaultValueConstraintSQL(tableName, columnName);
                        if (StringUtils.isNotBlank(alterDropConstraintSQL)) {
                            results.add(alterDropConstraintSQL);
                        }
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(alterDropConstraintSQL);
                        }
                        alter.append("  ADD DEFAULT ").append(defaultValue).append(" FOR ").append(columnName);
                    }
                } else {
                    if (isDefaultValueNeeded(sqlType, dialect)) {
                        alter.append(" ALTER COLUMN ").append(columnName).append(" SET DEFAULT ").append(defaultValue);
                    }
                }
                if (needAlterDefaultValue) {
                    alter.append(dialect.getAddColumnSuffixString());
                    LOGGER.info("ALTER TABLE [" + tableName + "] command ==> " + alter.toString());
                    results.add(alter.toString());
                }
            } else if (MDMTableUtils.isChangedToOptional(column, columnInfo)) {
                StringBuilder alter = new StringBuilder(root.toString());
                if (dialect instanceof PostgreSQL94Dialect || dialect instanceof DB2Dialect) {
                    alter.append(" ALTER COLUMN ").append(columnName).append(" DROP NOT NULL");
                } else if (dialect instanceof H2Dialect) {
                    alter.append(" ALTER COLUMN ").append(columnName).append(" SET NULL");
                } else if (dialect instanceof MySQLDialect) {
                    alter.append(" MODIFY COLUMN ").append(columnName).append(' ').append(sqlType).append(" DEFAULT NULL");
                } else if (dialect instanceof SQLServerDialect) {
                    alter.append(" ALTER COLUMN ").append(columnName).append(' ').append(sqlType).append(" NULL");
                } else if (dialect instanceof OracleCustomDialect) {
                    alter.append(" MODIFY ").append(columnName).append(" NULL");
                }
                LOGGER.info("ALTER TABLE [" + tableName + "] command ==> " + alter.toString());
                results.add(alter.toString());
            }
        }
        return results.iterator();
    }

    private String generateUK(Dialect dialect, Column col) {
        if (col.isUnique()) {
            String keyName = Constraint.generateName("UK_", this, col);
            UniqueKey uk = getOrCreateUniqueKey(keyName);
            uk.addColumn(col);
            return dialect.getUniqueDelegate().getColumnDefinitionUniquenessFragment(col);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Get the unique constraint name for the particular column in particular table using below SQL statement, then
     * do a Delete operation before adding new default value.
     *
     * @return
     */
    private String generateAlterDefaultValueConstraintSQL(String tableName, String columnName) {
        String alterDropConstraintSQL = StringUtils.EMPTY;
        try {
            String sql = "SELECT object_name(const.constid) as name FROM sys.sysconstraints const JOIN sys.columns cols " //$NON-NLS-1$
                    + "ON cols.object_id = const.id AND cols.column_id = const.colid " //$NON-NLS-1$
                    + "AND object_name(const.id)= ? AND cols.name = ?"; //$NON-NLS-1$
            List<String> parameters = new ArrayList<>();
            parameters.add(tableName);
            parameters.add(columnName);
            String queryResult = executeSQLForSQLServer(sql, parameters);
            if (StringUtils.isNotBlank(queryResult)) {
                alterDropConstraintSQL = "alter table " + tableName + " drop constraint " + queryResult;
            }
        } catch (Exception e) {
            LOGGER.error("Fetching SQLServer default value constraint failed.", e);
        }
        return alterDropConstraintSQL;
    }

    private String getDefaultValueForColumn(String tableName, String columnName) {
        String defaultValue = StringUtils.EMPTY;
        try {
            String sql = "SELECT ISNULL(CM.text,'')  FROM syscolumns C INNER JOIN systypes T ON C.xusertype = T.xusertype "
                    + "LEFT JOIN sys.extended_properties ETP ON  ETP.major_id = c.id AND ETP.minor_id = C.colid AND ETP.name ='MS_Description' "
                    + "LEFT join syscomments CM ON C.cdefault=CM.id WHERE C.id = object_id(?) AND C.name = ?";
            List<String> parameters = new ArrayList<>();
            parameters.add(tableName);
            parameters.add(columnName);
            defaultValue = executeSQLForSQLServer(sql, parameters);
        } catch (Exception e) {
            LOGGER.error("Fetching SQLServer default value failed.", e);
        }
        return defaultValue.replaceAll("\\(", "").replaceAll("\\)", "");
    }

    private String executeSQLForSQLServer(String sql, List<String> parameters) throws Exception {
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

    private String convertDefaultValue(Dialect dialect, String sqlType, String defaultValue) {
        String defaultSQL = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(defaultValue) && isDefaultValueNeeded(sqlType, dialect) && !isDateType(sqlType)) {
            defaultSQL = " DEFAULT " + defaultValue;
        }
        return defaultSQL;
    }

    private static boolean isDefaultValueNeeded(String sqlType, Dialect dialect) {
        return !LONGTEXT.equals(sqlType) || !(dialect instanceof MySQLDialect);
    }

    private static boolean isDateType(String sqlType) {
        return sqlType.equalsIgnoreCase(Timestamp.class.getSimpleName()) || sqlType.equalsIgnoreCase(Types.DATE)
                || sqlType.equalsIgnoreCase(Types.DATETIME) || sqlType.equalsIgnoreCase(Types.TIME);
    }
    
    public static void setDataSource(RDBMSDataSource dataSource) {
        MDMTable.dataSource = dataSource;
    }
}