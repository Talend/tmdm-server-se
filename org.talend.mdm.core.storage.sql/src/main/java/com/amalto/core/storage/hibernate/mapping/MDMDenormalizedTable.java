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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.util.collections.JoinedIterator;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.DenormalizedTable;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.hbm2ddl.ColumnMetadata;
import org.hibernate.tool.hbm2ddl.TableMetadata;

@SuppressWarnings("nls")
public class MDMDenormalizedTable extends DenormalizedTable {

    private static final Logger LOGGER = Logger.getLogger(MDMDenormalizedTable.class);

    public MDMDenormalizedTable(Table includedTable) {
        super(includedTable);
    }

    @Override
    public Iterator sqlAlterStrings(Dialect dialect, Mapping p, TableMetadata tableInfo, String defaultCatalog,
            String defaultSchema) throws HibernateException {

        StringBuilder root = new StringBuilder("alter table ")
                .append(getQualifiedName(dialect, defaultCatalog, defaultSchema))
                .append(' ');

        Iterator iter = getColumnIterator();
        List results = new ArrayList();

        while (iter.hasNext()) {
            Column column = (Column) iter.next();

            if (column.getSqlTypeCode() == null) {
                column.setSqlTypeCode(column.getSqlTypeCode(p));
            }
            if (column.getSqlType() == null) {
                column.setSqlType(column.getSqlType(dialect, p));
            }

            ColumnMetadata columnInfo = tableInfo.getColumnMetadata(column.getName());

            if (columnInfo == null) {
                // the column doesnt exist at all.
                StringBuilder alter = new StringBuilder(root.toString()).append(dialect.getAddColumnString()).append(' ')
                        .append(column.getQuotedName(dialect)).append(' ').append(column.getSqlType(dialect, p));

                String defaultValue = column.getDefaultValue();
                if (defaultValue != null) {
                    alter.append(" default ").append(defaultValue);
                }

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
                    alter.append(" check(").append(column.getCheckConstraint()).append(")");
                }

                String columnComment = column.getComment();
                if (columnComment != null) {
                    alter.append(dialect.getColumnComment(columnComment));
                }

                alter.append(dialect.getAddColumnSuffixString());

                results.add(alter.toString());
            } else if (MDMTableUtils.isAlterColumnField(column, columnInfo, dialect)) {
                StringBuilder alter = new StringBuilder(root.toString());

                if (dialect instanceof SQLServerDialect || dialect instanceof PostgreSQLDialect) {
                    alter.append(" ").append("alter COLUMN").append(" ");
                } else {
                    alter.append(" ").append("modify").append(" ");
                }
                alter.append(" ").append(column.getQuotedName(dialect)).append(" ");

                if (dialect instanceof PostgreSQLDialect) {
                    alter.append("TYPE").append(" ");
                }

                alter.append(column.getSqlType(dialect, p));

                LOGGER.debug(alter.toString());
                results.add(alter.toString());
            }
        }
        return results.iterator();
    }
}
