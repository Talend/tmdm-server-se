/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.amalto.core.save.MultiRecordsSaveException;
import com.amalto.core.save.SaveException;
import com.amalto.core.storage.exception.FullTextQueryCompositeKeyException;
import com.amalto.core.util.CoreException;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.XmlServerException;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class UploadUtilTest  extends TestCase {
    
    HashMap<String, Boolean> visibleMap = null;
    String mandatoryField = "Id@Name@Age";
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        visibleMap = new HashMap<String, Boolean>();
        visibleMap.put("Id", true);
        visibleMap.put("Name", false);
        visibleMap.put("Age", true);        
    }

    public void testChechMandatoryField(){
        Set<String>  mandatorySet = UploadUtil.chechMandatoryField(mandatoryField, visibleMap.keySet());
        assertEquals(mandatorySet.size(), 0);
    }
    
    public void testGetFieldName(){
        assertEquals(UploadUtil.getFieldName("Name=true"), "Name");
    }
  
    public void testGetFieldVisible(){
        assertTrue(UploadUtil.getFieldVisible("Name=true"));
    }    
    
    public void testGetDefaultHeader(String header){
        UploadUtil.getDefaultHeader(header);
        String[] headerArray = UploadUtil.getDefaultHeader(header);        
        assertEquals(headerArray.length, 3);
        assertEquals(headerArray[0], "Id");
        assertEquals(headerArray[1], "Name");
        assertEquals(headerArray[2], "Age");
    }
    
    public void testGetRootCause(){
        RuntimeException runtimeException = new RuntimeException("RuntimeException Cause");
        SaveException saveException = new SaveException("SaveException Cause", runtimeException);
        CoreException coreException = new CoreException("CoreException Cause", saveException);
        RemoteException remoteException = new RemoteException("RemoteException Cause", coreException);
        Exception exception = new Exception("Exception Cause",remoteException);
        assertEquals("com.amalto.core.save.SaveException: SaveException Cause", UploadUtil.getRootCause(exception).getMessage());
        
        FullTextQueryCompositeKeyException fullTextQueryCompositeKeyException = new FullTextQueryCompositeKeyException("FullTextQueryCompositeKeyException Cause");
        runtimeException = new RuntimeException("RuntimeException Cause", fullTextQueryCompositeKeyException);
        XmlServerException xmlServerException = new XmlServerException("XmlServerException Cause", runtimeException);
        XtentisException xtentisException = new XtentisException("XtentisException Cause", xmlServerException);
        coreException = new CoreException("CoreException Cause", xtentisException);
        remoteException = new RemoteException("RemoteException Cause", coreException);
        exception = new Exception("Exception Cause",remoteException);
        assertEquals("com.amalto.core.util.XtentisException: XtentisException Cause", UploadUtil.getRootCause(exception).getMessage());
    }
    
    public void testGetMultiRecordsSaveException(){
        int errorRowCount = 101;
        String recordKey = "[12][34]";
        RuntimeException runtimeException = new RuntimeException("RuntimeException Cause");
        SaveException saveException = new SaveException("SaveException Cause", runtimeException);
        MultiRecordsSaveException multiRecordsSaveException = new MultiRecordsSaveException(saveException.getCause().getMessage(), saveException.getCause(), recordKey, errorRowCount);
        CoreException coreException = new CoreException("CoreException Cause", multiRecordsSaveException);
        RemoteException remoteException = new RemoteException("RemoteException Cause", coreException);
        Exception exception = new Exception("Exception Cause",remoteException);
        assertEquals("com.amalto.core.save.MultiRecordsSaveException: RuntimeException Cause", UploadUtil.getRootCause(exception).getMessage());
        
        if (UploadUtil.getRootCause(exception) != null && CoreException.class.isInstance(UploadUtil.getRootCause(exception))) {
            Throwable cause = UploadUtil.getRootCause(exception);
            assertTrue(MultiRecordsSaveException.class.isInstance(cause.getCause()));
            assertEquals(101, ((MultiRecordsSaveException)cause.getCause()).getRowCount());
            assertEquals(recordKey, ((MultiRecordsSaveException)cause.getCause()).getKeyInfo());
        }
    }
        
    public void testIsViewableXpathValid(){
        Set<String> viewableXpathsSet = new HashSet<String>();
        viewableXpathsSet.add("Product/Id");
        viewableXpathsSet.add("Product/Name");
        viewableXpathsSet.add("Product/Description");
        viewableXpathsSet.add("Product/Price");
        viewableXpathsSet.add("Product/Family");
        viewableXpathsSet.add("ProductFamily/Name");
        String concept = "Product";
        assertFalse(UploadUtil.isViewableXpathValid(viewableXpathsSet, concept));
        
        concept = "ProductFamily";
        assertFalse(UploadUtil.isViewableXpathValid(viewableXpathsSet, concept));
        
        viewableXpathsSet = new HashSet<String>();
        viewableXpathsSet.add("Product/Id");
        viewableXpathsSet.add("Product/Name");
        viewableXpathsSet.add("Product/Description");
        viewableXpathsSet.add("Product/Price");
        viewableXpathsSet.add("Product/Family");
        concept = "Product";
        assertTrue(UploadUtil.isViewableXpathValid(viewableXpathsSet, concept));
    }
}
