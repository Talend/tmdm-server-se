/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.util;

import junit.framework.TestCase;


/**
 * created by talend2 on 2013-1-31
 * Detailled comment
 *
 */
public class KeyUtilTest extends TestCase {
    
    int number_5_KeyCode = 53;
    int character_A_KeyCode = 65;
    int character_Z_KeyCode = 90;
    
    public void testIsCharacter() {
        assertEquals(true, KeyUtil.isCharacter(character_A_KeyCode));
        assertEquals(true, KeyUtil.isCharacter(character_Z_KeyCode));
        assertEquals(false, KeyUtil.isCharacter(number_5_KeyCode));
    }
    
    public void testGetKeyValueByKeyCode() {
        assertEquals("A", KeyUtil.getKeyValueByKeyCode(character_A_KeyCode)); //$NON-NLS-1$
        assertEquals("Z", KeyUtil.getKeyValueByKeyCode(character_Z_KeyCode)); //$NON-NLS-1$
        assertEquals("5", KeyUtil.getKeyValueByKeyCode(number_5_KeyCode)); //$NON-NLS-1$
    }
}
