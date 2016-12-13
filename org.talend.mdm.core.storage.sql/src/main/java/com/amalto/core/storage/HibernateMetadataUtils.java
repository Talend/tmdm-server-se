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

import com.amalto.core.storage.hibernate.TypeMapping;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;

public class HibernateMetadataUtils {
    /**
     * Returns the corresponding Java type for the {@link org.talend.mdm.commmon.metadata.TypeMetadata} type.
     *
     * @param metadata A {@link org.talend.mdm.commmon.metadata.TypeMetadata} instance.
     * @return The name of Java class for the <code>metadata</code> argument. Returned string might directly be used for
     * a {@link Class#forName(String)} call.
     */
    public static String getJavaType(TypeMetadata metadata) {
        String sqlType = metadata.getData(TypeMapping.SQL_TYPE);
        if (sqlType != null && TypeMapping.SQL_TYPE_CLOB.equals(sqlType)) {
            return "java.sql.Clob"; //$NON-NLS-1$
        }
        String type = org.talend.mdm.commmon.metadata.MetadataUtils.getSuperConcreteType(metadata).getName();
        if (Types.STRING.equals(type) || Types.TOKEN.equals(type)) {
            return "java.lang.String"; //$NON-NLS-1$
        } else if (Types.ANY_URI.equals(type)) {
            return "java.lang.String"; //$NON-NLS-1$
        } else if (Types.INT.equals(type) || Types.INTEGER.equals(type) || Types.POSITIVE_INTEGER.equals(type)
                || Types.NON_POSITIVE_INTEGER.equals(type) || Types.NON_NEGATIVE_INTEGER.equals(type)
                || Types.NEGATIVE_INTEGER.equals(type) || Types.UNSIGNED_INT.equals(type)) {
            return "java.lang.Integer"; //$NON-NLS-1$
        } else if (Types.BOOLEAN.equals(type)) {
            return "java.lang.Boolean"; //$NON-NLS-1$
        } else if (Types.DECIMAL.equals(type)) {
            return "java.math.BigDecimal"; //$NON-NLS-1$
        } else if (Types.DATE.equals(type) || Types.DATETIME.equals(type) || Types.TIME.equals(type)) {
            return "java.sql.Timestamp"; //$NON-NLS-1$
        } else if (Types.DURATION.equals(type)) {
            // TMDM-7768: Maps duration to string (format validation to be performed by XSD checks)
            return "java.lang.String"; //$NON-NLS-1$
        } else if (Types.UNSIGNED_SHORT.equals(type) || Types.SHORT.equals(type)) {
            return "java.lang.Short"; //$NON-NLS-1$
        } else if (Types.UNSIGNED_LONG.equals(type) || Types.LONG.equals(type)) {
            return "java.lang.Long"; //$NON-NLS-1$
        } else if (Types.FLOAT.equals(type)) {
            return "java.lang.Float"; //$NON-NLS-1$
        } else if (Types.BASE64_BINARY.equals(type)) {
            return "java.lang.String"; //$NON-NLS-1$
        } else if (Types.BYTE.equals(type) || Types.UNSIGNED_BYTE.equals(type)) {
            return "java.lang.Byte"; //$NON-NLS-1$
        } else if (Types.HEX_BINARY.equals(type)) {
            return "java.lang.String"; //$NON-NLS-1$
        } else if (Types.DOUBLE.equals(type)) {
            return "java.lang.Double"; //$NON-NLS-1$
        } else if (Types.UUID.equals(type)) {
            return "java.lang.String"; //$NON-NLS-1$
        } else {
            throw new UnsupportedOperationException("No support for field typed as '" + type + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
