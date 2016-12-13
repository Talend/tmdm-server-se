/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server.service;

import java.rmi.RemoteException;
import java.util.Set;

import com.amalto.core.util.XtentisException;
import com.amalto.webapp.core.util.XtentisWebappException;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSGetItemsSort;
import com.amalto.core.webservice.WSItem;
import com.amalto.core.webservice.WSStringArray;
import com.sun.xml.xsom.XSElementDecl;


/**
 * created by talend2 on 2013-2-6
 * Detailled comment
 *
 */
public interface WebService {

    public WSItem getItem(WSGetItem wSGetItem) throws XtentisWebappException,RemoteException;
        
    public WSStringArray getItemsBySort(WSGetItemsSort wsGetItemsSort) throws RemoteException, XtentisWebappException;
    
    public XSElementDecl getXSElementDecl(String dataModel, String concept) throws Exception;
    
    public boolean isEnterpriseVersion();
    
    public boolean userCanRead(Class<?> dataModel,String dataModelName) throws XtentisException;
    
    public boolean checkReadAccess(String dataModelName, String conceptName);
}
