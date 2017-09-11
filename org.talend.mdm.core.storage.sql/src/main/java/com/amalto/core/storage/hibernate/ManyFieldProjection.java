/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import com.amalto.core.storage.datasource.RDBMSDataSource;
import org.apache.commons.lang.NotImplementedException;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.SimpleProjection;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import java.util.Set;

class ManyFieldProjection extends SimpleProjection {

    private final Set<String> aliases;

    private final FieldMetadata field;

    private final TableResolver resolver;

    private final RDBMSDataSource dataSource;

    ManyFieldProjection(Set<String> aliases, FieldMetadata field, TableResolver resolver, RDBMSDataSource dataSource) {
        this.aliases = aliases;
        this.field = field;
        this.resolver = resolver;
        this.dataSource = dataSource;
    }

    @Override
    public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
        Criteria subCriteria = StandardQueryHandler.findCriteria(criteria, aliases);
        ComplexTypeMetadata containingType = field.getContainingType();
        String containerTable = resolver.get(containingType);
        String collectionTable = resolver.getCollectionTable(field);
        String containerIdColumn = resolver.get(containingType.getKeyFields().iterator().next());
        StringBuilder sqlFragment = new StringBuilder();
        switch (dataSource.getDialectName()) {
            // For Postgres, uses "string_agg" function (introduced in 9.0).
            case POSTGRES:
                sqlFragment.append("(select string_agg(") //$NON-NLS-1$
                        .append(collectionTable)
                        .append(".value, ',') FROM ").append(containerTable); //$NON-NLS-1$
                for (FieldMetadata currentKey : containingType.getKeyFields()) {
                    String keyName = resolver.get(currentKey);
                    sqlFragment.append(" INNER JOIN ") //$NON-NLS-1$
                            .append(collectionTable)
                            .append(" on ") //$NON-NLS-1$
                            .append(containerTable).append('.').append(keyName)
                            .append(" = ") //$NON-NLS-1$
                            .append(collectionTable).append('.').append(keyName);
                }
                sqlFragment.append(" WHERE ") //$NON-NLS-1$
                        .append(containerTable)
                        .append('.')
                        .append(containerIdColumn)
                        .append(" = ") //$NON-NLS-1$
                        .append(criteriaQuery.getSQLAlias(subCriteria))
                        .append('.')
                        .append(containerIdColumn).append(") as y").append(position).append('_'); //$NON-NLS-1$
                break;
            // Following databases support group_concat function
            case H2:
            case MYSQL:
                sqlFragment.append("(select group_concat(") //$NON-NLS-1$
                        .append(collectionTable)
                        .append(".value separator ',') FROM ").append(containerTable); //$NON-NLS-1$
                for (FieldMetadata currentKey : containingType.getKeyFields()) {
                    String keyName = resolver.get(currentKey);
                    sqlFragment.append(" INNER JOIN ") //$NON-NLS-1$
                            .append(collectionTable)
                            .append(" on ") //$NON-NLS-1$
                            .append(containerTable).append('.').append(keyName)
                            .append(" = ") //$NON-NLS-1$
                            .append(collectionTable).append('.').append(keyName);
                }
                sqlFragment.append(" WHERE ") //$NON-NLS-1$
                        .append(containerTable)
                        .append('.')
                        .append(containerIdColumn)
                        .append(" = ") //$NON-NLS-1$
                        .append(criteriaQuery.getSQLAlias(subCriteria))
                        .append('.')
                        .append(containerIdColumn).append(") as y").append(position).append('_'); //$NON-NLS-1$
                break;
            // Use Oracle 10g "listagg" function (no group_concat on Oracle).
            case ORACLE_10G:
                sqlFragment.append("(select listagg(") //$NON-NLS-1$
                        .append(collectionTable)
                        .append(".value, ',') WITHIN GROUP (ORDER BY pos) FROM ").append(containerTable); //$NON-NLS-1$
                for (FieldMetadata currentKey : containingType.getKeyFields()) {
                    String keyName = resolver.get(currentKey);
                    sqlFragment.append(" INNER JOIN ") //$NON-NLS-1$
                            .append(collectionTable)
                            .append(" on ") //$NON-NLS-1$
                            .append(containerTable).append('.').append(keyName)
                            .append(" = ") //$NON-NLS-1$
                            .append(collectionTable).append('.').append(keyName);
                }
                sqlFragment.append(" WHERE ") //$NON-NLS-1$
                        .append(containerTable)
                        .append('.')
                        .append(containerIdColumn)
                        .append(" = ") //$NON-NLS-1$
                        .append(criteriaQuery.getSQLAlias(subCriteria))
                        .append('.')
                        .append(containerIdColumn).append(") as y").append(position).append('_'); //$NON-NLS-1$
                break;
            // SQL Server doesn't support the group_concat function -> use "stuff" function
            case SQL_SERVER:
                sqlFragment.append("STUFF((select ',' + ") //$NON-NLS-1$
                        .append(collectionTable)
                        .append(".value FROM ").append(containerTable); //$NON-NLS-1$
                for (FieldMetadata currentKey : containingType.getKeyFields()) {
                    String keyName = resolver.get(currentKey);
                    sqlFragment.append(" INNER JOIN ") //$NON-NLS-1$
                            .append(collectionTable)
                            .append(" on ") //$NON-NLS-1$
                            .append(containerTable).append('.').append(keyName)
                            .append(" = ") //$NON-NLS-1$
                            .append(collectionTable).append('.').append(keyName);
                }
                sqlFragment.append(" WHERE ") //$NON-NLS-1$
                        .append(containerTable)
                        .append('.')
                        .append(containerIdColumn)
                        .append(" = ") //$NON-NLS-1$
                        .append(criteriaQuery.getSQLAlias(subCriteria))
                        .append('.')
                        .append(containerIdColumn)
                        .append(" FOR XML PATH ('')), 1, 1, '') as y").append(position).append('_'); //$NON-NLS-1$
                break;
            // DB2 supports listagg() function after DB2 9.7
            case DB2:
                sqlFragment.append("(select listagg(") //$NON-NLS-1$
                        .append(collectionTable)
                        .append(".value, ',') WITHIN GROUP (ORDER BY pos) FROM ").append(containerTable); //$NON-NLS-1$
                for (FieldMetadata currentKey : containingType.getKeyFields()) {
                    String keyName = resolver.get(currentKey);
                    sqlFragment.append(" INNER JOIN ") //$NON-NLS-1$
                            .append(collectionTable)
                            .append(" on ") //$NON-NLS-1$
                            .append(containerTable).append('.').append(keyName)
                            .append(" = ") //$NON-NLS-1$
                            .append(collectionTable).append('.').append(keyName);
                }
                sqlFragment.append(" WHERE ") //$NON-NLS-1$
                        .append(containerTable)
                        .append('.')
                        .append(containerIdColumn)
                        .append(" = ") //$NON-NLS-1$
                        .append(criteriaQuery.getSQLAlias(subCriteria))
                        .append('.')
                        .append(containerIdColumn).append(") as y").append(position).append('_'); //$NON-NLS-1$
                break;
            default:
                throw new NotImplementedException("Support for repeatable element not implemented for dialect '" + dataSource.getDialectName() + "'.");
        }
        return sqlFragment.toString();
    }

    @Override
    public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return new Type[]{new StringType()};
    }
}
