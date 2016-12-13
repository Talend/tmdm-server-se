/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;


import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class LabelUtilTest extends TestCase {


    public void testabc() {
        assertEquals(LabelUtil.convertSpecialHTMLCharacter("'"), "&#39;"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(LabelUtil.convertSpecialHTMLCharacter("\""), "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testConvertList2String(){
        List<String> list = new ArrayList<String>();
        list.add("A"); //$NON-NLS-1$
        list.add("B"); //$NON-NLS-1$
        list.add("C"); //$NON-NLS-1$
        String result = LabelUtil.convertList2String(list, "@"); //$NON-NLS-1$
        assertEquals(result, "A@B@C"); //$NON-NLS-1$
        
        list.clear();
        list.add("1"); //$NON-NLS-1$
        list.add("2"); //$NON-NLS-1$
        list.add("3"); //$NON-NLS-1$
        result = LabelUtil.convertList2String(list, "-"); //$NON-NLS-1$
        assertEquals(result, "1-2-3"); //$NON-NLS-1$
    }
    
    public void testRemoveBrackets(){
        String id = "[1]"; //$NON-NLS-1$
        assertEquals(LabelUtil.removeBrackets(id), "1"); //$NON-NLS-1$
        
        id = "[ABC]"; //$NON-NLS-1$
        assertEquals(LabelUtil.removeBrackets(id), "ABC"); //$NON-NLS-1$
    }
}
