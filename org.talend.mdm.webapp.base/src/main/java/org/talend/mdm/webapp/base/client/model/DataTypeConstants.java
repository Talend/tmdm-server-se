/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.model;

import java.util.Date;

public enum DataTypeConstants implements DataType {

    STRING("string", ""), //$NON-NLS-1$ //$NON-NLS-2$
    NORMALIZEDSTRING("normalizedString", null), //$NON-NLS-1$ 
    LANGUAGE("language", null), //$NON-NLS-1$
    TOKEN("token", null), //$NON-NLS-1$
    NMTOKEN("NMTOKEN", null), //$NON-NLS-1$
    NMTOKENS("NMTOKENS", null), //$NON-NLS-1$
    NAME("Name", null), //$NON-NLS-1$
    NCNAME("NCName", null), //$NON-NLS-1$
    ID("ID", null), //$NON-NLS-1$
    IDREF("IDREF", null), //$NON-NLS-1$
    IDREFS("IDREFS", null), //$NON-NLS-1$
    ENTITY("ENTITY", null), //$NON-NLS-1$
    ENTITIES("ENTITIES", null), //$NON-NLS-1$
    ANYURI("anyURI", null), //$NON-NLS-1$
    QNAME("QName", null), //$NON-NLS-1$
    NOTATION("NOTATION", null), //$NON-NLS-1$

    INTEGER("integer", 0), //$NON-NLS-1$
    NONPOSITIVEINTEGER("nonPositiveInteger", 0), //$NON-NLS-1$
    NONNEGATIVEINTEGER("nonNegativeInteger", 0), //$NON-NLS-1$
    POSITIVEINTEGER("positiveInteger", null), //$NON-NLS-1$
    NEGATIVEINTEGER("negativeInteger", null), //$NON-NLS-1$
    LONG("long", 0), //$NON-NLS-1$
    INT("int", 0), //$NON-NLS-1$
    SHORT("short", 0), //$NON-NLS-1$
    UNSIGNEDLONG("unsignedLong", 0), //$NON-NLS-1$
    UNSIGNEDINT("unsignedInt", 0), //$NON-NLS-1$
    UNSIGNEDSHORT("unsignedShort", 0), //$NON-NLS-1$
    BYTE("byte", null), //$NON-NLS-1$
    UNSIGNEDBYTE("unsignedByte", null), //$NON-NLS-1$
    DECIMAL("decimal", 0.0), //$NON-NLS-1$
    FLOAT("float", 0.0), //$NON-NLS-1$
    DOUBLE("double", 0.0), //$NON-NLS-1$

    DURATION("duration", false), //$NON-NLS-1$
    DATETIME("dateTime", new Date()), //$NON-NLS-1$
    DATE("date", new Date()), //$NON-NLS-1$
    TIME("time", null), //$NON-NLS-1$
    GYEARMONTH("gYearMonth", null), //$NON-NLS-1$
    GYEAR("gYear", null), //$NON-NLS-1$
    GDAY("gDay", null), //$NON-NLS-1$
    GMONTH("gMonth", null), //$NON-NLS-1$

    BOOLEAN("boolean", false), //$NON-NLS-1$
    HEXBINARY("hexBinary", null), //$NON-NLS-1$
    BASE64BINARY("base64Binary", null), //$NON-NLS-1$

    UUID("UUID", ""), //$NON-NLS-1$ //$NON-NLS-2$
    AUTO_INCREMENT("AUTO_INCREMENT", ""), //$NON-NLS-1$ //$NON-NLS-2$
    PICTURE("PICTURE", ""), //$NON-NLS-1$ //$NON-NLS-2$ // Change picture default value to empty
    URL("URL", "@@http://"), //$NON-NLS-1$ //$NON-NLS-2$
    MLS("MULTI_LINGUAL", ""), //$NON-NLS-1$//$NON-NLS-2$

    UNKNOW("unknow", "");//$NON-NLS-1$ //$NON-NLS-2$

    String typeName;

    String baseTypeName;

    Object defaultValue;

    DataTypeConstants(String typeName) {
        this.typeName = typeName;
    }

    DataTypeConstants(String typeName, Object defaultValue) {
        this.typeName = typeName;
        this.defaultValue = defaultValue;
    }

    public String getTypeName() {
        return typeName;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.talend.mdm.webapp.itemsbrowser2.client.model.DataType#getBaseTypeName()
     */
    public String getBaseTypeName() {
        if (baseTypeName == null) {
            return getTypeName();
        }
        return baseTypeName;
    }

    public void setBaseTypeName(String baseTypeName) {
        this.baseTypeName = baseTypeName;
    }
}
