// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.save;

import java.util.HashMap;
import java.util.Map;

/**
 * DOC talend2 class global comment. Detailled comment
 */
public abstract class AbstractDocumentSaverContext implements DocumentSaverContext {

    private Map<String, String> autoIncrementFieldMap = new HashMap<String, String>();

    /**
     * Getter for hasAutoIncrementField.
     * 
     * @return the hasAutoIncrementField
     */
    @Override
    public Map<String, String> getAutoIncrementFieldMap() {
        return autoIncrementFieldMap;
    }

}
