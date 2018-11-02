/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.SimpleProjection;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.storage.datasource.RDBMSDataSource.DataSourceDialect;

@SuppressWarnings({ "nls", "serial" })
public class MultilingualProjection extends SimpleProjection {

    private final String language;

    private final FieldMetadata field;

    private final TableResolver resolver;

    private final DataSourceDialect dataSourceDialect;

    MultilingualProjection(String language, FieldMetadata fieldMetadata, DataSourceDialect dataSourceDialect, TableResolver resolver) {
        this.language = language;
        this.field = fieldMetadata;
        this.dataSourceDialect = dataSourceDialect;
        this.resolver = resolver;
    }

    @Override
    public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
        ComplexTypeMetadata containingType = field.getContainingType();

        String columnName = resolver.get(field);
        String containerTable = resolver.get(containingType);

        final String colName = criteriaQuery.getColumn(criteria, containerTable + '.' + columnName);
        
        if (dataSourceDialect == DataSourceDialect.ORACLE_10G) {
            columnName = columnName.toUpperCase();
            containerTable = containerTable.toUpperCase();
        }

        StringBuilder sqlFragment = new StringBuilder();

        /**
         * generate the sql for the different database, data: [ZH:xxxxx][EN:yyyyy][FR:zzzzz], will get 'xxxxx' for ZH
         * H2 & MySQL: SUBSTRING(this_.x_name, LOCATE('[ZH:', this_.x_name) + 4, LOCATE(']', this_.x_name, LOCATE('[ZH:', this_.x_name)) - LOCATE('[ZH:', this_.x_name) - 4)
         * SQL_SERVER: SUBSTRING(this_.x_name, CHARINDEX('[ZH:', this_.x_name) + 4 , CHARINDEX(']', this_.x_name, CHARINDEX('[ZH:', this_.x_name)) -  CHARINDEX('ZH:', this_.x_name) - 3)
         * POSTGRES: SUBSTRING(this_.x_name, '\[ZH:(.*?)\]')
         * ORACLE_10G: SUBSTR(this_.x_name, INSTR(this_.x_name, '[ZH:') + 4, INSTR(this_.x_name, ']', INSTR(this_.x_name, '[ZH:')) - INSTR(this_.x_name, '[ZH:') - 4) 
         */
        String sql = StringUtils.EMPTY;
        switch (dataSourceDialect) {
        case H2:
        case MYSQL:
            sql = "SUBSTRING(" + colName + ", LOCATE('[" + language + ":', " + colName + ") + 4, LOCATE(']', " + colName
                    + ", LOCATE('[" + language + ":', " + colName + ")) - LOCATE('[" + language + ":', " + colName + ") - 4)";
            break;
        case SQL_SERVER:
            sql = "SUBSTRING(" + colName + ", CHARINDEX('[" + language + ":', " + colName + ") + 4 , CHARINDEX(']', " + colName
                    + ", CHARINDEX('[" + language + ":', " + colName + ")) -  CHARINDEX('" + language + ":', " + colName
                    + ") - 3)";
            break;
        case POSTGRES:
            sql = "SUBSTRING(" + colName + ", '\\[" + language + ":(.*?)\\]')";
            break;
        case ORACLE_10G:
            sql = "SUBSTR(" + colName + ", INSTR(" + colName + ", '[" + language + ":') + 4, INSTR(" + colName + ", ']', INSTR("
                    + colName + ", '[" + language + ":')) - INSTR(" + colName + ", '[" + language + ":') - 4)";
            break;
        case DB2:
        default:
            throw new NotImplementedException("Order by MULTI_LINGUAL field doesn't support database of: " + dataSourceDialect);
        }

        sqlFragment.append(sql).append(" as y").append(position).append('_');

        return sqlFragment.toString();
    }

    @Override
    public String[] getColumnAliases(String alias, int loc) {
        return new String[] { StringUtils.EMPTY };
    }
    
    @Override
    public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return new Type[] { new StringType() };
    }

    @Override
    public Type[] getTypes(String alias, Criteria criteria, CriteriaQuery criteriaQuery) {
        return new Type[] { new StringType() };
    }

    public static String getAlias() {
        return Types.MULTI_LINGUAL + '_' + Thread.currentThread().getId();
    }
}
