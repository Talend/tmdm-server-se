/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.provider;

import java.rmi.RemoteException;

import com.amalto.core.webservice.WSTransformerV2PK;
import com.amalto.webapp.core.util.XtentisWebappException;

public interface SmartViewProvider {

    public WSTransformerV2PK[] getWSTransformerV2PKs() throws XtentisWebappException, RemoteException;

    public String getDescription(WSTransformerV2PK transformerV2PK) throws XtentisWebappException, RemoteException;
}
