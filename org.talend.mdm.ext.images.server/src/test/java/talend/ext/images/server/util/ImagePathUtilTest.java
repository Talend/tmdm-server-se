/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package talend.ext.images.server.util;

import junit.framework.TestCase;

public class ImagePathUtilTest  extends TestCase {
    
    public void testParseFileFullName() {
        String[] fullName = ImagePathUtil.parseFileFullName("C:/talend/Talend-MDMServer-V6.1.0/resources/upload//c201708/picture.png");
        assertEquals("picture", fullName[0]);
        assertEquals("png", fullName[1]);
        fullName = ImagePathUtil.parseFileFullName("C:\\Users\\yjli\\Desktop\\picture_1.png");
        assertEquals("picture_1", fullName[0]);
        assertEquals("png", fullName[1]);
        fullName = ImagePathUtil.parseFileFullName("/home/talend/picture 2.png");
        assertEquals("picture 2", fullName[0]);
        assertEquals("png", fullName[1]);
        fullName = ImagePathUtil.parseFileFullName("picture 2.png");
        assertEquals("picture 2", fullName[0]);
        assertEquals("png", fullName[1]);
    }
}
