/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save.context;

import com.amalto.core.history.accessor.NoOpAccessor;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.history.DeleteType;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.save.DOMDocument;
import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.ServerContext;

class UpdateReportDocument extends DOMDocument {

    private final Document updateReportDocument;

    private boolean isRecordingFieldChange;

    private int index = 0;

    private String currentNewValue = null;

    private boolean isCreated = false;

    public UpdateReportDocument(Document updateReportDocument) {
        super(updateReportDocument,
                internalGetType(),
                XSystemObjects.DC_UPDATE_PREPORT.getName(),
                UpdateReport.UPDATE_REPORT_DATA_MODEL);
        this.updateReportDocument = updateReportDocument;
    }

    private MutableDocument setField(String field, String value) {
        if (index++ % 2 == 0) {
            currentNewValue = value;
        } else {
            Element item = updateReportDocument.createElement("Item"); //$NON-NLS-1$
            {
                // Path
                Node pathNode = updateReportDocument.createElement("path"); //$NON-NLS-1$
                pathNode.appendChild(updateReportDocument.createTextNode(field));
                item.appendChild(pathNode);
                // Old value
                Node oldValueNode = updateReportDocument.createElement("oldValue"); //$NON-NLS-1$
                oldValueNode.appendChild(updateReportDocument.createTextNode(value));
                item.appendChild(oldValueNode);
                // New value
                Node newValueNode = updateReportDocument.createElement("newValue"); //$NON-NLS-1$
                if (currentNewValue != null) {
                    newValueNode.appendChild(updateReportDocument.createTextNode(currentNewValue));
                }
                item.appendChild(newValueNode);
            }
            updateReportDocument.getDocumentElement().appendChild(item);
            currentNewValue = null;
        }
        return this;
    }

    @Override
    public MutableDocument create(MutableDocument content) {
        isCreated = true;
        setOperationType(UpdateReportPOJO.OPERATION_TYPE_CREATE);
        return this;
    }

    @Override
    public MutableDocument setContent(MutableDocument content) {
        return this;
    }

    @Override
    public MutableDocument delete(DeleteType deleteType) {
        // Nothing to do
        // TODO Could extend saver to handle deletes?
        return this;
    }

    public void enableRecordFieldChange() {
        isRecordingFieldChange = true;
    }

    public void disableRecordFieldChange() {
        isRecordingFieldChange = false;
    }
    
    public boolean isCreated() {
        return isCreated;
    }

    public void setOperationType(String operationType) {
        Element item = null;
        NodeList operationTypeNodeList = updateReportDocument.getElementsByTagName("OperationType"); //$NON-NLS-1$
        for (int i = 0; i < operationTypeNodeList.getLength(); i++) {
            Node operationTypeNode = operationTypeNodeList.item(i);
            if (Node.ELEMENT_NODE == operationTypeNode.getNodeType()) {
                item = (Element) operationTypeNode;
                break;
            }
        }
        if (item == null) {
            item = updateReportDocument.createElement("OperationType"); //$NON-NLS-1$
        }
        if (operationType.equalsIgnoreCase(UpdateReportPOJO.OPERATION_TYPE_CREATE)) {
            if (!item.getTextContent().equalsIgnoreCase(UpdateReportPOJO.OPERATION_TYPE_CREATE)) {
                item.appendChild(updateReportDocument.createTextNode(UpdateReportPOJO.OPERATION_TYPE_CREATE));
                updateReportDocument.getDocumentElement().appendChild(item);
            }
        } else if (operationType.equalsIgnoreCase(UpdateReportPOJO.OPERATION_TYPE_UPDATE)) {
            if (!isCreated) {
                item.appendChild(updateReportDocument.createTextNode(UpdateReportPOJO.OPERATION_TYPE_UPDATE));
                updateReportDocument.getDocumentElement().appendChild(item);
            }
        }
    }

    @Override
    public Accessor createAccessor(String path) {
        if (!isRecordingFieldChange) {
            return super.createAccessor(path);
        } else if (!isCreated) {
            return new FieldChangeRecorder(path, this);
        } else {  // isCreated
            // TODO Don't record changes on created record (but uncomment line below and it will).
            // return new FieldChangeRecorder(path, this);
            return NoOpAccessor.INSTANCE;
        }
    }

    @Override
    public String getDataModel() {
        return UpdateReport.UPDATE_REPORT_DATA_MODEL;
    }

    @Override
    public ComplexTypeMetadata getType() {
        return internalGetType();
    }

    private static ComplexTypeMetadata internalGetType() {
        MetadataRepositoryAdmin admin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
        return admin.get(UpdateReport.UPDATE_REPORT_DATA_MODEL).getComplexType("Update"); //$NON-NLS-1$
    }

    private static class FieldChangeRecorder implements Accessor {

        private final String path;

        private final UpdateReportDocument updateReportDocument;

        public FieldChangeRecorder(String path, UpdateReportDocument updateReportDocument) {
            this.path = path;
            this.updateReportDocument = updateReportDocument;
        }

        public void set(String value) {
            updateReportDocument.setField(path, value);
        }

        public String get() {
            return StringUtils.EMPTY;
        }

        public void touch() {
        }

        public void create() {
            // Nothing to do.
        }

        @Override
        public void insert() {
            // Nothing to do.
        }

        public void createAndSet(String value) {
            set(value);
        }

        public void delete() {
            updateReportDocument.setField(path, "null"); //$NON-NLS-1$
        }

        public boolean exist() {
            return true;
        }

        public void markModified(Marker marker) {
            throw new UnsupportedOperationException();
        }

        public void markUnmodified() {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return 1;
        }

        public String getActualType() {
            return StringUtils.EMPTY;
        }

        @Override
        public int compareTo(Accessor accessor) {
            if (exist() != accessor.exist()) {
                return -1;
            }
            return accessor instanceof FieldChangeRecorder ? 0 : -1;
        }
    }

}
