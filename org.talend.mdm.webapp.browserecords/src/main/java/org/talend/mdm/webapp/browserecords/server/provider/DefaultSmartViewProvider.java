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

import com.amalto.core.webservice.WSGetTransformerV2;
import com.amalto.core.webservice.WSGetTransformerV2PKs;
import com.amalto.core.webservice.WSTransformerV2;
import com.amalto.core.webservice.WSTransformerV2PK;
import com.amalto.webapp.core.util.Util;
import com.amalto.webapp.core.util.XtentisWebappException;

public class DefaultSmartViewProvider implements SmartViewProvider {

    public WSTransformerV2PK[] getWSTransformerV2PKs() throws XtentisWebappException, RemoteException {
        return Util.getPort().getTransformerV2PKs(new WSGetTransformerV2PKs("*")).getWsTransformerV2PK(); //$NON-NLS-1$;
    }

    public String getDescription(WSTransformerV2PK transformerPK) throws XtentisWebappException, RemoteException {
        WSTransformerV2 wst = Util.getPort().getTransformerV2(new WSGetTransformerV2(transformerPK));
        return wst.getDescription();
    }
}
