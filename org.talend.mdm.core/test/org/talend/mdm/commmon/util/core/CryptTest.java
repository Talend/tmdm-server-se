/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.commmon.util.core;

import junit.framework.TestCase;

public class CryptTest extends TestCase {

    @SuppressWarnings("nls")
    public void testEncrypt() throws Exception {
        String pwd = "administrator";
        assertEquals("NKNCzuAvtojMffRWt9ZLRw==,Encrypt", Crypt.encrypt(pwd));

        pwd = "admin123";
        assertEquals("w4AXOA1a34afqqnlmVLB4A==,Encrypt", Crypt.encrypt(pwd));

        pwd = "";
        assertEquals("", Crypt.encrypt(pwd));
    }

    @SuppressWarnings("nls")
    public void testDecrypt() throws Exception {
        String encryptedPwd = "w4AXOA1a34afqqnlmVLB4A==,Encrypt";
        assertEquals("admin123", Crypt.decrypt(encryptedPwd));

        encryptedPwd = "w4AXOA1a34afqqnlmVLB4A==";
        assertEquals("w4AXOA1a34afqqnlmVLB4A==", Crypt.decrypt(encryptedPwd));

        encryptedPwd = "";
        assertEquals("", Crypt.decrypt(encryptedPwd));
    }
}