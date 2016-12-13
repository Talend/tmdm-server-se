/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.typefield;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TypeFieldSource implements IsSerializable {
    
    public static final String CELL_EDITOR = "CELL_EDITOR"; //$NON-NLS-1$

    public static final String FORM_INPUT = "FORM_INPUT"; //$NON-NLS-1$

    public static final String SEARCH_EDITOR = "SEARCH_EDITOR"; //$NON-NLS-1$

    private String name;
    
    public Map<String, String> operatorMap;

    /**
     * DOC Administrator TypeFieldSource constructor comment.
     */
    public TypeFieldSource() {

    }

    public TypeFieldSource(String sourceName) {
        super();
        this.name = sourceName;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getOperatorMap() {
        return operatorMap;
    }

    public void setOperatorMap(Map<String, String> operatorMap) {
        this.operatorMap = operatorMap;
    }

}
