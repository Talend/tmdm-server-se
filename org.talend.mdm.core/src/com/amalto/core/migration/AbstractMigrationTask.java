/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.migration;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amalto.core.objects.marshalling.MarshallingFactory;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;


public abstract class AbstractMigrationTask {

    public static final String CLUSTER_MIGRATION = "MDMMigration"; //$NON-NLS-1$

    protected static final Logger LOGGER = Logger.getLogger(AbstractMigrationTask.class);

    private Map<String, Boolean> handlerMap = null;

    private static final String UNIQUE_MIGRATION = "MIGRATION.completed.record";  //$NON-NLS-1$

    private boolean forceExe=false;

	public AbstractMigrationTask() {
	}

    public void setForceExe(boolean forceExe) {
		this.forceExe = forceExe;
	}

    protected boolean isDone() {
        String content = null;
        try {
            Util.getXmlServerCtrlLocal().createCluster(CLUSTER_MIGRATION);
            content = Util.getXmlServerCtrlLocal().getDocumentAsString(CLUSTER_MIGRATION, UNIQUE_MIGRATION);
        } catch (Exception e) {
            LOGGER.error("Communication with XML server.", e);
        }
        if (content == null) {
            if (handlerMap == null) {
                handlerMap = new HashMap<String, Boolean>();
            }
            return false;
        }
        try {
            MigrationTaskBox box = unmarshal(content);
            handlerMap = box.getHandlerMap();
            if (handlerMap == null) {
                handlerMap = new HashMap<String, Boolean>();
            }
            Boolean res = handlerMap.get(this.getClass().getName());
            return res != null ? res : false;
        } catch (Exception e) {
            LOGGER.error(e);
            return false;
        }
    }

    public void start() {	
		if (isDone() && !forceExe) {
			return;
		}
		Boolean result = execute();
		if(result==null) {
            result=false;
        }
        if (result) {
			handlerMap.put(this.getClass().getName(), true);
		} else {
			handlerMap.put(this.getClass().getName(), false);
		}
		try {
			MigrationTaskBox newBox=new MigrationTaskBox(handlerMap);
            XmlServer xmlServerCtrlLocal = Util.getXmlServerCtrlLocal();
            xmlServerCtrlLocal.start(CLUSTER_MIGRATION);
            xmlServerCtrlLocal.putDocumentFromString(newBox.toString(), UNIQUE_MIGRATION, CLUSTER_MIGRATION);
            xmlServerCtrlLocal.commit(CLUSTER_MIGRATION);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		LOGGER.info(this.getClass().getName()+" has been done. ");
	}
	
   protected abstract Boolean execute();
		
   public static MigrationTaskBox unmarshal(String marshalledRevision) throws XtentisException {
        try {
            return MarshallingFactory.getInstance().getUnmarshaller(MigrationTaskBox.class).unmarshal(new StringReader(marshalledRevision));
	    } catch (Exception e) {
    	    LOGGER.error(e);
    	    throw new XtentisException(e.getMessage(), e);
	    } 
	 }
}
