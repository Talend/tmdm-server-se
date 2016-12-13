/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.dmagent;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.datamodel.management.SchemaManager;

import com.amalto.commons.core.datamodel.synchronization.DMUpdateEvent;
import com.amalto.commons.core.datamodel.synchronization.DataModelChangeListener;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.util.XtentisException;

public class DataModelSynchronizer implements DataModelChangeListener {

    private static final Logger LOGGER = Logger.getLogger(DataModelSynchronizer.class);

    @Override
    public void onChange(DMUpdateEvent dmUpdateEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(dmUpdateEvent);
        }

        String eventType = dmUpdateEvent.getEventType();
        SchemaManager schemaWebAgent = SchemaWebAgent.getInstance();
        try {

            if (eventType.equals(DMUpdateEvent.EVENT_TYPE_INIT) || eventType.equals(DMUpdateEvent.EVENT_TYPE_UPDATE)) {
                String dataModelSchema = getSchemaFromDB(dmUpdateEvent.getDataModelPK());
                schemaWebAgent.updateToDatamodelPool(dmUpdateEvent.getDataModelPK(), dataModelSchema);
            } else if (eventType.equals(DMUpdateEvent.EVENT_TYPE_DELETE)) {
                schemaWebAgent.removeFromDatamodelPool(dmUpdateEvent.getDataModelPK());
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String getSchemaFromDB(String uniqueID) throws XtentisException {
        DataModelPOJO dataModelPOJO = ObjectPOJO.load(DataModelPOJO.class, new ObjectPOJOPK(uniqueID));
        return dataModelPOJO.getSchema();
    }
}
