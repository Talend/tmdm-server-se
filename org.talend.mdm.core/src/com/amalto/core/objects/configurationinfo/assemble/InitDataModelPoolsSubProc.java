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

import java.util.Collection;

import com.amalto.commons.core.datamodel.synchronization.DMUpdateEvent;
import com.amalto.commons.core.datamodel.synchronization.DataModelChangeNotifier;
import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.server.api.DataModel;
import com.amalto.core.util.Util;

@SuppressWarnings("serial")
public class InitDataModelPoolsSubProc extends AssembleSubProc {

    @Override
    public void run() throws Exception {
        DataModel dataModelCtrl;
        try {
            dataModelCtrl = Util.getDataModelCtrlLocal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        @SuppressWarnings("unchecked")
        Collection<DataModelPOJOPK> dmpks = dataModelCtrl.getDataModelPKs(".*"); //$NON-NLS-1$
        for (DataModelPOJOPK dataModelPOJOPK : dmpks) {
            // synchronize with outer agents
            DataModelChangeNotifier dmUpdateEventNotifer = DataModelChangeNotifier.createInstance();
            DMUpdateEvent event = new DMUpdateEvent(dataModelPOJOPK.getUniqueId(), DMUpdateEvent.EVENT_TYPE_INIT);
            dmUpdateEventNotifer.notifyChange(event);
        }

    }

}
