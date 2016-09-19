// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage;

import java.util.List;

import junit.framework.TestCase;


public class StorageMetadataUtilsTest extends TestCase {

    public void testGetIds() {
        String ids = "[123], [456], [ab7]"; //$NON-NLS-1$        
        List<String> idList = StorageMetadataUtils.getIds(ids);
        
        for (int i=0; i<idList.size(); i++) {
            if (i==0) {
                assertEquals("123", idList.get(i)); //$NON-NLS-1$
            } else if (i==1) {
                assertEquals("456", idList.get(i)); //$NON-NLS-1$
            } else if (i==2) {
                assertEquals("ab7", idList.get(i)); //$NON-NLS-1$
            }
        }
    }
    
}
