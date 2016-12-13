/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.util;

import java.util.List;

import org.talend.mdm.webapp.base.client.model.Image;

import com.google.gwt.junit.client.GWTTestCase;


@SuppressWarnings("nls")
public class ImageUitlGWTTest extends GWTTestCase{
    
    private static String testXml = null;
    static{
        StringBuffer sb=new StringBuffer();
        sb.append("<list> ");
        sb.append(" <entry> ");
        sb.append("     <name>a-mytshirt.jpg</name> ");
        sb.append("     <imageName>mytshirt.jpg</imageName> ");
        sb.append("     <catalog>a</catalog> ");
        sb.append("     <uri>/imageserver/upload/a/mytshirt.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>alpha-taking_two_minutes.png</name> ");
        sb.append("     <imageName>taking_two_minutes.png</imageName> ");
        sb.append("     <catalog>alpha</catalog> ");
        sb.append("     <uri>/imageserver/upload/alpha/taking_two_minutes.png</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>b-taking_two_hours.png</name> ");
        sb.append("     <imageName>taking_two_hours.png</imageName> ");
        sb.append("     <catalog>b</catalog> ");
        sb.append("     <uri>/imageserver/upload/b/taking_two_hours.png</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c-taking_two_minutes.png</name> ");
        sb.append("     <imageName>taking_two_minutes.png</imageName> ");
        sb.append("     <catalog>c</catalog> ");
        sb.append("     <uri>/imageserver/upload/c/taking_two_minutes.png</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-002.gif</name> ");
        sb.append("     <imageName>002.gif</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/002.gif</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-152.jpg</name> ");
        sb.append("     <imageName>152.jpg</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/152.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-aa.jpg</name> ");
        sb.append("     <imageName>aa.jpg</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/aa.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-cap.jpg</name> ");
        sb.append("     <imageName>cap.jpg</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/cap.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-dog.jpg</name> ");
        sb.append("     <imageName>dog.jpg</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/dog.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-e61ec6ff01351000e0000000c0a80089.jpg</name> ");
        sb.append("     <imageName>e61ec6ff01351000e0000000c0a80089.jpg</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/e61ec6ff01351000e0000000c0a80089.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-f62f654401351000e0000000c0a80089.jpg</name> ");
        sb.append("     <imageName>f62f654401351000e0000000c0a80089.jpg</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/f62f654401351000e0000000c0a80089.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-images.jpg</name> ");
        sb.append("     <imageName>images.jpg</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/images.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-people.png_0.1.png</name> ");
        sb.append("     <imageName>people.png_0.1.png</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/people.png_0.1.png</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-people_0.1.png</name> ");
        sb.append("     <imageName>people_0.1.png</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/people_0.1.png</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-tmall-back_0.1.gif</name> ");
        sb.append("     <imageName>tmall-back_0.1.gif</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/tmall-back_0.1.gif</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-wanted-poster-1.jpg</name> ");
        sb.append("     <imageName>wanted-poster-1.jpg</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/wanted-poster-1.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-yahooext_0.1.gif.gif</name> ");
        sb.append("     <imageName>yahooext_0.1.gif.gif</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/yahooext_0.1.gif.gif</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-yahoo_0.1.gif.gif</name> ");
        sb.append("     <imageName>yahoo_0.1.gif.gif</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/yahoo_0.1.gif.gif</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-yahoo_ext_small_0.1.gif</name> ");
        sb.append("     <imageName>yahoo_ext_small_0.1.gif</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/yahoo_ext_small_0.1.gif</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-yahoo_ext_small_0.1.gif.gif</name> ");
        sb.append("     <imageName>yahoo_ext_small_0.1.gif.gif</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/yahoo_ext_small_0.1.gif.gif</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-ybox-close_0.1.gif</name> ");
        sb.append("     <imageName>ybox-close_0.1.gif</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/ybox-close_0.1.gif</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-ybox-close_0.1.png</name> ");
        sb.append("     <imageName>ybox-close_0.1.png</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/ybox-close_0.1.png</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-yourworkplace1_0.1.gif</name> ");
        sb.append("     <imageName>yourworkplace1_0.1.gif</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/yourworkplace1_0.1.gif</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201203-yourworkplace1_0.1.png.png</name> ");
        sb.append("     <imageName>yourworkplace1_0.1.png.png</imageName> ");
        sb.append("     <catalog>c201203</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201203/yourworkplace1_0.1.png.png</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201204-817acd8f01361000e0000001c0a84801.jpg</name> ");
        sb.append("     <imageName>817acd8f01361000e0000001c0a84801.jpg</imageName> ");
        sb.append("     <catalog>c201204</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201204/817acd8f01361000e0000001c0a84801.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201204-Chrysanthemum.jpg</name> ");
        sb.append("     <imageName>Chrysanthemum.jpg</imageName> ");
        sb.append("     <catalog>c201204</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201204/Chrysanthemum.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201204-golf_shirt.jpg</name> ");
        sb.append("     <imageName>golf_shirt.jpg</imageName> ");
        sb.append("     <catalog>c201204</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201204/golf_shirt.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201204-hshu.jpg</name> ");
        sb.append("     <imageName>hshu.jpg</imageName> ");
        sb.append("     <catalog>c201204</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201204/hshu.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201204-images-test.jpg</name> ");
        sb.append("     <imageName>images-test.jpg</imageName> ");
        sb.append("     <catalog>c201204</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201204/images-test.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201204-stein.jpg</name> ");
        sb.append("     <imageName>stein.jpg</imageName> ");
        sb.append("     <catalog>c201204</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201204/stein.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>c201204-wanted-poster-1.jpg</name> ");
        sb.append("     <imageName>wanted-poster-1.jpg</imageName> ");
        sb.append("     <catalog>c201204</catalog> ");
        sb.append("     <uri>/imageserver/upload/c201204/wanted-poster-1.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>han-mug.jpg</name> ");
        sb.append("     <imageName>mug.jpg</imageName> ");
        sb.append("     <catalog>han</catalog> ");
        sb.append("     <uri>/imageserver/upload/han/mug.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>MyCatalog-eb0f251b01351000e0000000c0a84801.jpg</name> ");
        sb.append("     <imageName>eb0f251b01351000e0000000c0a84801.jpg</imageName> ");
        sb.append("     <catalog>MyCatalog</catalog> ");
        sb.append("     <uri>/imageserver/upload/MyCatalog/eb0f251b01351000e0000000c0a84801.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>MyCatalog-tshirt_0.1.jpg</name> ");
        sb.append("     <imageName>tshirt_0.1.jpg</imageName> ");
        sb.append("     <catalog>MyCatalog</catalog> ");
        sb.append("     <uri>/imageserver/upload/MyCatalog/tshirt_0.1.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>MyCatalog-WeeMee_fde1cab646f33847b84ac8aadf473ab4_for_han_shu.talendbj_0.1.jpg</name> ");
        sb.append("     <imageName>WeeMee_fde1cab646f33847b84ac8aadf473ab4_for_han_shu.talendbj_0.1.jpg</imageName> ");
        sb.append("     <catalog>MyCatalog</catalog> ");
        sb.append("     <uri>/imageserver/upload/MyCatalog/WeeMee_fde1cab646f33847b84ac8aadf473ab4_for_han_shu.talendbj_0.1.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>myfolder007-eca5cb0d01351000e0000001c0a84801.jpg</name> ");
        sb.append("     <imageName>eca5cb0d01351000e0000001c0a84801.jpg</imageName> ");
        sb.append("     <catalog>myfolder007</catalog> ");
        sb.append("     <uri>/imageserver/upload/myfolder007/eca5cb0d01351000e0000001c0a84801.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>myfolder007-myfile007.jpg</name> ");
        sb.append("     <imageName>myfile007.jpg</imageName> ");
        sb.append("     <catalog>myfolder007</catalog> ");
        sb.append("     <uri>/imageserver/upload/myfolder007/myfile007.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>MYTEST-hshu.jpg</name> ");
        sb.append("     <imageName>hshu.jpg</imageName> ");
        sb.append("     <catalog>MYTEST</catalog> ");
        sb.append("     <uri>/imageserver/upload/MYTEST/hshu.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>other3-ec644e3701351000e0000002c0a84801.jpg</name> ");
        sb.append("     <imageName>ec644e3701351000e0000002c0a84801.jpg</imageName> ");
        sb.append("     <catalog>other3</catalog> ");
        sb.append("     <uri>/imageserver/upload/other3/ec644e3701351000e0000002c0a84801.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>others-ec644e3701351000e0000000c0a84801.jpg</name> ");
        sb.append("     <imageName>ec644e3701351000e0000000c0a84801.jpg</imageName> ");
        sb.append("     <catalog>others</catalog> ");
        sb.append("     <uri>/imageserver/upload/others/ec644e3701351000e0000000c0a84801.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>others-ec644e3701351000e0000001c0a84801.jpg</name> ");
        sb.append("     <imageName>ec644e3701351000e0000001c0a84801.jpg</imageName> ");
        sb.append("     <catalog>others</catalog> ");
        sb.append("     <uri>/imageserver/upload/others/ec644e3701351000e0000001c0a84801.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>TestCatalog-0Z4442M1-8.jpg</name> ");
        sb.append("     <imageName>0Z4442M1-8.jpg</imageName> ");
        sb.append("     <catalog>TestCatalog</catalog> ");
        sb.append("     <uri>/imageserver/upload/TestCatalog/0Z4442M1-8.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>TestCatalog-cap.jpg</name> ");
        sb.append("     <imageName>cap.jpg</imageName> ");
        sb.append("     <catalog>TestCatalog</catalog> ");
        sb.append("     <uri>/imageserver/upload/TestCatalog/cap.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>TestCatalog-stein_0.1.jpg</name> ");
        sb.append("     <imageName>stein_0.1.jpg</imageName> ");
        sb.append("     <catalog>TestCatalog</catalog> ");
        sb.append("     <uri>/imageserver/upload/TestCatalog/stein_0.1.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>wanted-poster-1.jpg</name> ");
        sb.append("     <imageName>poster-1.jpg</imageName> ");
        sb.append("     <catalog>wanted</catalog> ");
        sb.append("     <uri>/imageserver/upload/wanted/poster-1.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>x-mug.jpg</name> ");
        sb.append("     <imageName>mug.jpg</imageName> ");
        sb.append("     <catalog>x</catalog> ");
        sb.append("     <uri>/imageserver/upload/x/mug.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>y-spaghetti.jpg</name> ");
        sb.append("     <imageName>spaghetti.jpg</imageName> ");
        sb.append("     <catalog>y</catalog> ");
        sb.append("     <uri>/imageserver/upload/y/spaghetti.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append(" <entry> ");
        sb.append("     <name>z-mug.jpg</name> ");
        sb.append("     <imageName>mug.jpg</imageName> ");
        sb.append("     <catalog>z</catalog> ");
        sb.append("     <uri>/imageserver/upload/z/mug.jpg</uri> ");
        sb.append(" </entry> ");
        sb.append("</list> ");
        testXml = sb.toString();
    }
        

    public void testGetImages() throws Exception
    {
        List<Image> images = ImageUtil.getImages(testXml);

        assertEquals(48, images.size());

        Image image0 = images.get(0);
        assertEquals("a-mytshirt.jpg", image0.getName()); //$NON-NLS-1$
        assertEquals("mytshirt.jpg", image0.getFileName()); //$NON-NLS-1$
        assertEquals("a", image0.getCatalog()); //$NON-NLS-1$
        assertEquals("/imageserver/upload/a/mytshirt.jpg", image0.getUri()); //$NON-NLS-1$

        Image image5 = images.get(5);
        assertEquals("c201203-152.jpg", image5.getName()); //$NON-NLS-1$
        assertEquals("152.jpg", image5.getFileName()); //$NON-NLS-1$
        assertEquals("c201203", image5.getCatalog()); //$NON-NLS-1$
        assertEquals("/imageserver/upload/c201203/152.jpg", image5.getUri()); //$NON-NLS-1$

        Image image15 = images.get(15);
        assertEquals("c201203-wanted-poster-1.jpg", image15.getName()); //$NON-NLS-1$
        assertEquals("wanted-poster-1.jpg", image15.getFileName()); //$NON-NLS-1$
        assertEquals("c201203", image15.getCatalog()); //$NON-NLS-1$
        assertEquals("/imageserver/upload/c201203/wanted-poster-1.jpg", image15.getUri()); //$NON-NLS-1$
    }
    
    public void testGetImagesWithEmptyResponse() throws Exception {

        List<Image> images = ImageUtil.getImages(null);
        assertTrue(images.isEmpty());

        images = ImageUtil.getImages("");
        assertTrue(images.isEmpty());

        images = ImageUtil.getImages("<list/>");
        assertTrue(images.isEmpty());

    }

    // GWTTestCase Required
    @Override
    public String getModuleName() {        
        return "org.talend.mdm.webapp.base.TestBase"; //$NON-NLS-1$
    }
}
