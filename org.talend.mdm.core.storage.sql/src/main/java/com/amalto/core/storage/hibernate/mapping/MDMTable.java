/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.hbm2ddl.ColumnMetadata;
import org.hibernate.tool.hbm2ddl.TableMetadata;

import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.hibernate.OracleCustomDialect;

@SuppressWarnings({ "nls", "rawtypes", "deprecation", "serial", "unchecked" })
public class MDMTable extends Table {

    private static final String LONGTEXT = "longtext";

    protected RDBMSDataSource dataSource;

    private static final Logger LOGGER = Logger.getLogger(MDMTable.class);

    @Override
    public String sqlCreateString(Dialect dialect, Mapping p, String defaultCatalog, String defaultSchema) {
        StringBuilder buf = new StringBuilder(hasPrimaryKey() ? dialect.getCreateTableString()
                : dialect.getCreateMultisetTableString()).append(' ')
                .append(getQualifiedName(dialect, defaultCatalog, defaultSchema)).append(" (");

        boolean identityColumn = getIdentifierValue() != null
                && getIdentifierValue().isIdentityColumn(p.getIdentifierGeneratorFactory(), dialect);

        // Try to find out the name of the primary key to create it as identity if the IdentityGenerator is used
        String pkname = null;
        if (hasPrimaryKey() && identityColumn) {
            pkname = ((Column) getPrimaryKey().getColumnIterator().next()).getQuotedName(dialect);
        }

        Iterator iter = getColumnIterator();
        while (iter.hasNext()) {
            Column col = (Column) iter.next();

            buf.append(col.getQuotedName(dialect)).append(' ');

            if (identityColumn && col.getQuotedName(dialect).equals(pkname)) {
                // to support dialects that have their own identity data type
                if (dialect.hasDataTypeInIdentityColumn()) {
                    buf.append(col.getSqlType(dialect, p));
                }
                buf.append(' ').append(dialect.getIdentityColumnString(col.getSqlTypeCode(p)));
            } else {
                String sqlType = col.getSqlType(dialect, p);
                buf.append(sqlType);

                String defaultValue = col.getDefaultValue();
                buf.append(convertDefaultValue(dialect, sqlType, defaultValue));

                if (col.isNullable()) {
                    buf.append(dialect.getNullColumnString());
                } else {
                    buf.append(" not null");
                }

            }

            if (col.isUnique()) {
                String keyName = Constraint.generateName("UK_", this, col);
                UniqueKey uk = getOrCreateUniqueKey(keyName);
                uk.addColumn(col);
                buf.append(dialect.getUniqueDelegate().getColumnDefinitionUniquenessFragment(col));
            }

            if (col.hasCheckConstraint() && dialect.supportsColumnCheck()) {
                buf.append(" check (").append(col.getCheckConstraint()).append(')');
            }

            String columnComment = col.getComment();
            if (columnComment != null) {
                buf.append(dialect.getColumnComment(columnComment));
            }

            if (iter.hasNext()) {
                buf.append(", ");
            }

        }
        if (hasPrimaryKey()) {
            buf.append(", ").append(getPrimaryKey().sqlConstraintString(dialect));
        }

        buf.append(dialect.getUniqueDelegate().getTableCreationUniqueConstraintsFragment(this));

        if (dialect.supportsTableCheck()) {
            Iterator chiter = getCheckConstraintsIterator();
            while (chiter.hasNext()) {
                buf.append(", check (").append(chiter.next()).append(')');
            }
        }

        buf.append(')');

        if (getComment() != null) {
            buf.append(dialect.getTableComment(getComment()));
        }

        String createSQL = buf.append(dialect.getTableTypeString()).toString();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(createSQL);
        }
        return createSQL;
    }

    @Override
    public Iterator sqlAlterStrings(Dialect dialect, Mapping p, TableMetadata tableInfo, String defaultCatalog,
            String defaultSchema) throws HibernateException {

        String tableName = getQualifiedName(dialect, defaultCatalog, defaultSchema);
        StringBuilder root = new StringBuilder("alter table ").append(tableName).append(' ');

        Iterator iter = getColumnIterator();
        List results = new ArrayList();

        while (iter.hasNext()) {
            Column column = (Column) iter.next();

            ColumnMetadata columnInfo = tableInfo.getColumnMetadata(column.getName());

            String sqlType = column.getSqlType(dialect, p);
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

                if (column.isUnique()) {
                    String keyName = Constraint.generateName("UK_", this, column);
                    UniqueKey uk = getOrCreateUniqueKey(keyName);
                    uk.addColumn(column);
                    alter.append(dialect.getUniqueDelegate().getColumnDefinitionUniquenessFragment(column));
                }

                if (column.hasCheckConstraint() && dialect.supportsColumnCheck()) {
                    alter.append(" check(").append(column.getCheckConstraint()).append(')');
                }

                String columnComment = column.getComment();
                if (columnComment != null) {
                    alter.append(dialect.getColumnComment(columnComment));
                }

                alter.append(dialect.getAddColumnSuffixString());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(alter.toString());
                }
                results.add(alter.toString());
            } else if (MDMTableUtils.isAlterColumnField(column, columnInfo, dialect)) {
                StringBuilder alter = new StringBuilder(root.toString());

                if (dialect instanceof SQLServerDialect || dialect instanceof PostgreSQLDialect) {
                    alter.append(" ALTER COLUMN ");
                } else {
                    alter.append(" MODIFY ");
                }
                alter.append(' ').append(columnName).append(' ');

                if (dialect instanceof PostgreSQLDialect) {
                    alter.append("TYPE ");
                }

                alter.append(sqlType);

                alter.append(convertDefaultValue(dialect, sqlType, defaultValue));

                if (column.isNullable()) {
                    alter.append(dialect.getNullColumnString());
                } else {
                    alter.append(" not null ");
                }

                if (column.isUnique()) {
                    String keyName = Constraint.generateName("UK_", this, column);
                    UniqueKey uk = getOrCreateUniqueKey(keyName);
                    uk.addColumn(column);
                    alter.append(dialect.getUniqueDelegate().getColumnDefinitionUniquenessFragment(column));
                }

                if (column.hasCheckConstraint() && dialect.supportsColumnCheck()) {
                    alter.append(" check(").append(column.getCheckConstraint()).append(')');
                }

                String columnComment = column.getComment();
                if (columnComment != null) {
                    alter.append(dialect.getColumnComment(columnComment));
                }

                alter.append(dialect.getAddColumnSuffixString());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(alter.toString());
                }
                results.add(alter.toString());
            } else if (StringUtils.isNotBlank(defaultValue)) {
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
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(alter.toString());
                    }
                    results.add(alter.toString());
                }
            } else if (MDMTableUtils.isChangedToOptional(column, columnInfo)) {
                StringBuilder alter = new StringBuilder(root.toString());
                if (dialect instanceof PostgreSQLDialect || dialect instanceof DB2Dialect) {
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
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(alter.toString());
                }
                results.add(alter.toString());
            }
        }
        return results.iterator();
    }

    private String generateAlterDefaultValueConstraintSQL(String tableName, String columnName) {
        String alterDropConstraintSQL = StringUtils.EMPTY;
        try {
            String sql = "SELECT c.name FROM sysconstraints a INNER JOIN syscolumns b ON a.colid=b.colid INNER JOIN sysobjects c ON a.constid=c.id "
                    + "WHERE a.id = object_id (?) AND b.name = ?";
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
        if (StringUtils.isNotBlank(defaultValue) && isDefaultValueNeeded(sqlType, dialect)) {
            defaultSQL = " DEFAULT " + defaultValue;
        }
        return defaultSQL;
    }

    public static boolean isDefaultValueNeeded(String sqlType, Dialect dialect) {
        if (LONGTEXT.equals(sqlType) && dialect instanceof MySQLDialect) {
            return false;
        }
        return true;
    }

    public void setDataSource(RDBMSDataSource dataSource) {
        this.dataSource = dataSource;
    }
}