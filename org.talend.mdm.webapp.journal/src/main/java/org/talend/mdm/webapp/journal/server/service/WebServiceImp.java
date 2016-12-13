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
import java.util.Map;
import java.util.Set;

import com.amalto.core.util.LocalUser;
import com.amalto.core.util.XtentisException;
import com.amalto.webapp.core.dwr.CommonDWR;
import com.amalto.webapp.core.util.DataModelAccessor;
import com.amalto.webapp.core.util.Util;
import com.amalto.webapp.core.util.Webapp;
import com.amalto.webapp.core.util.XtentisWebappException;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSGetItemsSort;
import com.amalto.core.webservice.WSItem;
import com.amalto.core.webservice.WSStringArray;
import com.sun.xml.xsom.XSElementDecl;


/**
 * created by talend2 on 2013-2-7
 * Detailled comment
 *
 */
public class WebServiceImp implements WebService {

    public WSItem getItem(WSGetItem wSGetItem) throws XtentisWebappException, RemoteException {
        return Util.getPort().getItem(wSGetItem);
    }
    
    public WSStringArray getItemsBySort(WSGetItemsSort wsGetItemsSort) throws RemoteException, XtentisWebappException {
        return Util.getPort().getItemsSort(wsGetItemsSort);
    }

    public XSElementDecl getXSElementDecl(String dataModel, String concept) throws Exception {
      Map<String, XSElementDecl> map = CommonDWR.getConceptMap(dataModel);
      return map.get(concept);
    }
    
    @Override
    public boolean isEnterpriseVersion() {
        return Webapp.INSTANCE.isEnterpriseVersion();
    }

    @Override
    public boolean userCanRead(Class<?> dataModel, String dataModelName) throws XtentisException {
        return LocalUser.getLocalUser().userCanRead(dataModel,dataModelName);
    }

    @Override
    public boolean checkReadAccess(String dataModelName, String conceptName) {
        return DataModelAccessor.getInstance().checkReadAccess(dataModelName, conceptName);
    }
}
