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


/**
 * created by talend2 on 2013-1-30
 * Detailled comment
 *
 */
public class KeyUtil {
    
    public static final int CHARACTER_A = 65;
    
    public static final int CHARACTER_Z = 90;
    
    public static boolean isCharacter(int keyCode) {
        return (keyCode >= CHARACTER_A && keyCode <= CHARACTER_Z);
    }
    
    public static String getKeyValueByKeyCode(int keyCode) {
        return String.valueOf((char)keyCode);
    }

}
