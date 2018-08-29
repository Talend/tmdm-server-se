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

package com.amalto.core.save.context;

import java.util.List;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.w3c.dom.Document;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.action.FieldUpdateAction;
import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.util.Util;

class UpdateReport implements DocumentSaver {

    public static final String UPDATE_REPORT_DATA_MODEL = "UpdateReport"; //$NON-NLS-1$

    public static final String UPDATE_REPORT_TYPE = "Update"; //$NON-NLS-1$

    private final DocumentSaver next;

    UpdateReport(DocumentSaver next) {
        this.next = next;
    }

    @Override
    public void save(SaverSession session, DocumentSaverContext context) {
        UpdateReportDocument updateReportDocument;
        Document updateReportAsDOM = (Document) SaverContextFactory.EMPTY_UPDATE_REPORT.cloneNode(true);
        if (context.isInvokeBeforeSaving()) {
            if (context.getUpdateReportDocument() != null) {
                updateReportAsDOM = context.getUpdateReportDocument().asDOM();
            }
        }       
        updateReportDocument = new UpdateReportDocument(updateReportAsDOM);
        
        StringBuilder key = new StringBuilder();
        String[] id = context.getId();
        for (int i = 0; i < id.length; i++) {
            key.append(id[i]);
            if (i < id.length - 1) {
                key.append('.');
            }
        }

        ComplexTypeMetadata type = context.getUserDocument().getType();
        List<Action> actions = context.getActions();
        boolean hasHeader = false;
        for (Action action : actions) {
            if (!hasHeader) {
                setHeader(updateReportDocument, "UserName", session.getSaverSource().getLegitimateUser()); //$NON-NLS-1$
                setHeader(updateReportDocument, "Source", String.valueOf(action.getSource())); //$NON-NLS-1$
                setHeader(updateReportDocument, "TimeInMillis", String.valueOf(action.getDate().getTime())); //$NON-NLS-1$
                setHeader(updateReportDocument, "DataCluster", String.valueOf(context.getDataCluster())); //$NON-NLS-1$
                setHeader(updateReportDocument, "DataModel", String.valueOf(context.getDataModelName())); //$NON-NLS-1$
                setHeader(updateReportDocument, "Concept", String.valueOf(type.getName())); //$NON-NLS-1$
                setHeader(updateReportDocument, "Key", key.toString()); //$NON-NLS-1$
                hasHeader = true;
                updateReportDocument.enableRecordFieldChange();
            }

            if (!(action instanceof ChangeTypeAction) && !isInherit(action, type)) {
                action.perform(updateReportDocument);
                action.undo(updateReportDocument);
            }
        }
        if (!updateReportDocument.isCreated()) {
            updateReportDocument.setOperationType(UpdateReportPOJO.OPERATION_TYPE_UPDATE);
        }
        updateReportDocument.disableRecordFieldChange();

        context.setUpdateReportDocument(updateReportDocument);
        next.save(session, context);
    }

    private boolean isInherit(Action action, ComplexTypeMetadata type) {
        if (!(action instanceof FieldUpdateAction)) {
            return false;
        }
        FieldUpdateAction filedUpdateAction = (FieldUpdateAction) action;

        String path = filedUpdateAction.getPath();
        path = Util.removeBracketWithNumber(path);

        TypeMetadata filedType = type.getField(path).getType();

        if (filedType instanceof ComplexTypeMetadata) {
            if (((ComplexTypeMetadata) filedType).getContainer().getType().getName()
                    .startsWith(MetadataRepository.ANONYMOUS_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    private void setHeader(MutableDocument updateReportDocument, String fieldName, String value) {
        Accessor accessor = updateReportDocument.createAccessor(fieldName);
        accessor.set(value);
    }

    @Override
    public String[] getSavedId() {
        return next.getSavedId();
    }

    @Override
    public String getSavedConceptName() {
        return next.getSavedConceptName();
    }

    @Override
    public String getBeforeSavingMessage() {
        return next.getBeforeSavingMessage();
    }

    @Override
    public String getBeforeSavingMessageType() {
        return next.getBeforeSavingMessageType();
    }
}
