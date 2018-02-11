/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.save;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.history.DeleteType;
import com.amalto.core.history.Document;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import com.amalto.core.server.api.XmlServer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DefaultCommitter implements SaverSession.Committer {

    private final XmlServer xmlServerCtrlLocal;

    public DefaultCommitter() {
        xmlServerCtrlLocal = Util.getXmlServerCtrlLocal();
    }

    public void begin(String dataCluster) {
        try {
            if (xmlServerCtrlLocal.supportTransaction()) {
                xmlServerCtrlLocal.start(dataCluster);
            }
        } catch (XtentisException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit(String dataCluster) {
        try {
            if (xmlServerCtrlLocal.supportTransaction()) {
                xmlServerCtrlLocal.commit(dataCluster);
            }
        } catch (XtentisException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback(String dataCluster) {
        try {
            if (xmlServerCtrlLocal.supportTransaction()) {
                xmlServerCtrlLocal.rollback(dataCluster);
            }
        } catch (XtentisException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Document document) {
        try {
            ComplexTypeMetadata type = document.getType();
            boolean putInCache = type.getSuperTypes().isEmpty() && type.getSubTypes().isEmpty();
            Collection<FieldMetadata> keyFields = type.getKeyFields();
            List<String> ids = new LinkedList<String>();
            for (FieldMetadata keyField : keyFields) {
                String keyFieldName = keyField.getName();
                Accessor keyAccessor = ((MutableDocument)document).createAccessor(keyFieldName);
                if (!keyAccessor.exist()) {
                    throw new RuntimeException("Unexpected state: '"+ type +"' does not have value for key '" + keyFieldName + "'.");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
                }
                ids.add(keyAccessor.get());
            }
            ItemPOJO item = new ItemPOJO(new DataClusterPOJOPK(document.getDataCluster()),
                    type.getName(),
                    ids.toArray(new String[ids.size()]), // it need to set ids value
                    System.currentTimeMillis(),
                    document.exportToString());
            item.setTaskId(document.getTaskId());
            item.setDataModelName(document.getDataModel()); // it need to set dataModelName
            item.store(putInCache);
        } catch (XtentisException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Document document, DeleteType deleteType) {
        try {
            ComplexTypeMetadata type = document.getType();
            boolean putInCache = type.getSuperTypes().isEmpty() && type.getSubTypes().isEmpty();
            Collection<FieldMetadata> keyFields = type.getKeyFields();
            List<String> ids = new LinkedList<String>();
            for (FieldMetadata keyField : keyFields) {
                String keyFieldName = keyField.getName();
                Accessor keyAccessor = ((MutableDocument)document).createAccessor(keyFieldName);
                if (!keyAccessor.exist()) {
                    throw new RuntimeException("Unexpected state: '"+ type +"' does not have value for key '" + keyFieldName + "'.");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
                }
                ids.add(keyAccessor.get());
            }
            ItemPOJO item = new ItemPOJO(new DataClusterPOJOPK(document.getDataCluster()),
                    type.getName(),
                    ids.toArray(new String[ids.size()]), // it need to set ids value
                    System.currentTimeMillis(),
                    document.exportToString());
            item.setTaskId(document.getTaskId());
            item.setDataModelName(document.getDataModel()); // it need to set dataModelName
            item.store(putInCache);
        } catch (XtentisException e) {
            throw new RuntimeException(e);
        }
    }
}
