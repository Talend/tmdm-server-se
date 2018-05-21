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

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;

class Delete implements DocumentSaver {

    private String[] savedId = new String[0];

    public void save(SaverSession session, DocumentSaverContext context) {
        String typeName = context.getUserDocument().getType().getName();
        savedId = context.getId();
        if (savedId.length == 0) {
            throw new IllegalStateException("No ID information to save instance of '" + typeName + "'");
        }
        MutableDocument databaseDocument = context.getDatabaseDocument();
        if (!StringUtils.EMPTY.equals(context.getTaskId())) {
            databaseDocument.setTaskId(context.getTaskId());
        }
        session.delete(context.getDataCluster(), databaseDocument, databaseDocument.getDeleteType());
        // Save update report (if any)
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        if (updateReportDocument != null) {
            saveUpdateReport(updateReportDocument, session.getSaverSource(), session);
        }
    }

    private static void saveUpdateReport(MutableDocument updateReportDocument, SaverSource saverSource, SaverSession session) {
        MetadataRepository metadataRepository = saverSource.getMetadataRepository(UpdateReport.UPDATE_REPORT_DATA_MODEL);
        ComplexTypeMetadata updateReportType = metadataRepository.getComplexType(UpdateReport.UPDATE_REPORT_TYPE);
        if (updateReportType == null) {
            throw new IllegalStateException("Could not find UpdateReport type.");
        }

        Collection<FieldMetadata> keyFields = updateReportType.getKeyFields();
        long updateReportTime = 0;
        for (FieldMetadata keyField : keyFields) {
            String keyFieldName = keyField.getName();
            Accessor keyAccessor = updateReportDocument.createAccessor(keyFieldName);
            if (!keyAccessor.exist()) {
                throw new RuntimeException("Unexpected state: update report does not have value for key '" + keyFieldName + "'.");
            }
            if ("TimeInMillis".equals(keyFieldName)) { //$NON-NLS-1$
                updateReportTime = Long.parseLong(keyAccessor.get());
            }
        }
        if (updateReportTime < 1) { // This is unexpected (would mean update report "TimeInMillis" is not present).
            throw new IllegalStateException("Missing update report time value.");
        }
        // Call session's save to save all items in correct order (one transaction per data cluster for the XML db).
        session.save(UpdateReport.UPDATE_REPORT_DATA_MODEL, updateReportDocument);
    }

    public String[] getSavedId() {
        return savedId;
    }

    public String getSavedConceptName() {
        throw new UnsupportedOperationException();
    }

    public String getBeforeSavingMessage() {
        return StringUtils.EMPTY;
    }

    public String getBeforeSavingMessageType() {
        return BeforeSaving.TYPE_INFO;
    }
}
