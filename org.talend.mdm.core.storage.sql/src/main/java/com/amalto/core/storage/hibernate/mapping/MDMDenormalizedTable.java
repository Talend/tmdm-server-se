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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.DenormalizedTable;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.schema.extract.spi.ColumnInformation;
import org.hibernate.tool.schema.extract.spi.TableInformation;

public class MDMDenormalizedTable extends DenormalizedTable {

    private static final long serialVersionUID = 2989474981864788563L;
    private static final Logger LOGGER = LogManager.getLogger(MDMDenormalizedTable.class);

    public MDMDenormalizedTable(Table includedTable) {
        super(includedTable);
    }

    @Override
    public Iterator sqlAlterStrings(
            Dialect dialect,
            Metadata metadata,
            TableInformation tableInfo,
            Identifier defaultCatalog,
            Identifier defaultSchema) throws HibernateException {

        StringBuilder root = new StringBuilder("ALTER TABLE ").append(tableInfo.getName().getTableName().getText()).append(' ');

        Iterator iter = getColumnIterator();
        List results = new ArrayList();

        while (iter.hasNext()) {
            Column column = (Column) iter.next();

            if (column.getSqlTypeCode() == null) {
                column.setSqlTypeCode(column.getSqlTypeCode(metadata));
            }
            if (column.getSqlType() == null) {
                column.setSqlType(column.getSqlType(dialect, metadata));
            }

            ColumnInformation columnInfo = tableInfo.getColumn(Identifier.toIdentifier(column.getName(), false));

            if (columnInfo == null) {
                // the column doesnt exist at all.
                StringBuilder alter = new StringBuilder(root.toString()).append(dialect.getAddColumnString()).append(' ')
                        .append(column.getQuotedName(dialect)).append(' ').append(column.getSqlType(dialect, metadata));

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
                LOGGER.info("TABLE UPDATED: " + alter.toString());
                results.add(alter.toString());
            } else if (MDMTableUtils.isAlterColumnField(column, columnInfo, dialect)) {
                StringBuilder alter = new StringBuilder(root.toString());

                if (dialect instanceof SQLServerDialect || dialect instanceof PostgreSQL94Dialect) {
                    alter.append(" ").append("ALTER COLUMN").append(" ");
                } else {
                    alter.append(" ").append("MODIFY").append(" ");
                }
                alter.append(" ").append(column.getQuotedName(dialect)).append(" ");

                if (dialect instanceof PostgreSQL94Dialect) {
                    alter.append("TYPE").append(" ");
                }

                alter.append(column.getSqlType(dialect, metadata));
                LOGGER.info("TABLE UPDATED: " + alter.toString());
                results.add(alter.toString());
            }
        }
        return results.iterator();
    }
}