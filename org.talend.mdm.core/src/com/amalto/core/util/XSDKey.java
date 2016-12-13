/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.io.Serializable;

/**
 * @author bgrieder
 *
 */
public class XSDKey implements Serializable {

    private final String selector;

	private final String[] fields;

    private final String[] fieldTypes;

    /**
	 * @param selector
	 * @param fields
     * @param fieldTypes
	 */
	public XSDKey(String selector, String[] fields, String[] fieldTypes) {
		this.selector = selector;
		this.fields = fields;
        this.fieldTypes = fieldTypes;
    }

	public String[] getFields() {
		return fields;
	}

    public String getSelector() {
		return selector;
	}

    public String[] getFieldTypes() {
        return fieldTypes;
    }
}
