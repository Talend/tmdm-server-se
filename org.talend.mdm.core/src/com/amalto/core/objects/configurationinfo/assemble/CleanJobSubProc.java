/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.configurationinfo.assemble;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.backgroundjob.BackgroundJobPOJO;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;

public class CleanJobSubProc extends AssembleSubProc{

    private final static Logger LOGGER = Logger.getLogger(CleanJobSubProc.class);

    @Override
    public void run() throws Exception {
        //clean-up background Job
		XmlServer server;
        try {
            server = Util.getXmlServerCtrlLocal();
        } catch (Exception e) {
            String err = "Auto Configuration in the background: unable to access the XML Server wrapper";
            LOGGER.error(err, e);
            throw new RuntimeException(err, e);
        }
        // zap Background jobs
        try {
            server.deleteCluster(ObjectPOJO.getCluster(BackgroundJobPOJO.class));
            server.createCluster(ObjectPOJO.getCluster(BackgroundJobPOJO.class));
        } catch (XtentisException e) {
            LOGGER.warn("Cleanup of Jobs failed!", e);
        }
    }

}
