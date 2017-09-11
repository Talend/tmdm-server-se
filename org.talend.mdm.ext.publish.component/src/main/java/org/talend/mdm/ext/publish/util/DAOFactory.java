/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.util;

import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.Util;
import org.apache.log4j.Logger;

public class DAOFactory {

    private static final Logger LOGGER = Logger.getLogger(DAOFactory.class);

    /** unique instance */
    private static DAOFactory instance = null;

    private XmlServer server;

    private DAOFactory() {
        super();
        try {
            server = Util.getXmlServerCtrlLocal();
        } catch (Exception e) {
            String err = "Unable to access the XML Server wrapper";
            LOGGER.error(err, e);
        }
    }
    
    public static synchronized DAOFactory getUniqueInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    public DomainObjectsDAO getDomainObjectDAO() {
        return new DomainObjectsDAOImpl(this.server);
    }

    public PicturesDAO getPicturesDAO(String picturesLocation) {
        return new PicturesDAOFSImpl(picturesLocation);
    }

}
