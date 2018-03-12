/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.metadata;

import java.lang.annotation.*;

/**
 * Indicates if a method returns a long string.
 * @see ClassRepository
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.METHOD)
public @interface LongString {
    
    public static final String PREFER_LONGVARCHAR = "PREFER_LONGVARCHAR"; //$NON-NLS-1$
    
    /**
     * For DB2, CLOB type doesn't support CURSOR SCROLL SENSITIVE for read-only ResultSet like: JOIN, but pagination query needs SCROLL.
     * So if a field is not too long(<=32700) and needs JOIN query, should be mapped as LONGVARCHAR, not CLOB, like: DataClusterPOJO.vocabulary.
     */
    boolean preferLongVarchar() default false;
}
