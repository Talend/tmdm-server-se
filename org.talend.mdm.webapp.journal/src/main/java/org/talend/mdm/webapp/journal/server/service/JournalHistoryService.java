/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;

import com.amalto.core.server.ServerContext;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.journal.server.ForeignKeyInfoTransformer;
import org.talend.mdm.webapp.journal.server.LocalLabelTransformer;
import org.talend.mdm.webapp.journal.shared.JournalParameters;

import com.amalto.core.objects.DroppedItemPOJO;
import com.amalto.core.objects.DroppedItemPOJOPK;
import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.history.Action;
import com.amalto.core.history.DocumentHistory;
import com.amalto.core.history.DocumentHistoryFactory;
import com.amalto.core.history.DocumentHistoryNavigator;
import com.amalto.core.history.DocumentTransformer;
import com.amalto.core.history.EmptyDocument;
import com.amalto.core.history.ModificationMarker;
import com.amalto.core.history.UniqueIdTransformer;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;

public class JournalHistoryService {

    private static final String CURRENT_ACTION = "current"; //$NON-NLS-1$

    private static final String PREVIOUS_ACTION = "before"; //$NON-NLS-1$

    private static final String NEXT_ACTION = "next"; //$NON-NLS-1$

    private static final Messages MESSAGES = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.journal.client.i18n.JournalMessages", JournalHistoryService.class.getClassLoader()); //$NON-NLS-1$

    private static JournalHistoryService service;

    private DocumentHistory documentHistory;

    private JournalHistoryService() {
        documentHistory = DocumentHistoryFactory.getInstance().create();
    }

    public static synchronized JournalHistoryService getInstance() {
        if (service == null) {
            service = new JournalHistoryService();
        }
        return service;
    }

    public String getComparisionTreeString(JournalParameters parameter) throws Exception {
        DocumentHistoryNavigator navigator = documentHistory.getHistory(parameter.getDataClusterName(),
                parameter.getDataModelName(), parameter.getConceptName(), parameter.getId());
        MetadataRepository metadataRepository = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin()
                .get(parameter.getDataModelName());
        if (metadataRepository == null) {
            throw new IllegalArgumentException("Data model '" + parameter.getDataModelName() + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        TypeMetadata documentTypeMetadata = metadataRepository.getType(parameter.getConceptName());
        if (documentTypeMetadata == null) {
            try {
                documentTypeMetadata = metadataRepository.getType(parameter.getConceptName());
                if (documentTypeMetadata == null) {
                    throw new IllegalArgumentException(
                            "Cannot find type information for type '" + parameter.getDataModelName() + "' in data cluster '" + parameter.getDataClusterName() + "', in data model '" + parameter.getDataModelName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
            } catch (Exception e) {
                throw new ServletException("Could not initialize type information", e); //$NON-NLS-1$
            }
        }
        navigator.goTo(new Date(parameter.getDate()));
        Action modificationMarkersAction = navigator.currentAction();
        com.amalto.core.history.Document document = EmptyDocument.INSTANCE;
        if (CURRENT_ACTION.equalsIgnoreCase(parameter.getAction())) {
            document = navigator.current();
        } else if (PREVIOUS_ACTION.equalsIgnoreCase(parameter.getAction())) {
            if (navigator.hasPrevious()) {
                document = navigator.previous();
            }
        } else if (NEXT_ACTION.equalsIgnoreCase(parameter.getAction())) {
            if (navigator.hasNext()) {
                document = navigator.next();
            }
        } else {
            throw new ServletException(new IllegalArgumentException("Action '" + parameter.getAction() + " is not supported.")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ForeignKeyInfoTransformer foreignKeyInfoTransformer = new ForeignKeyInfoTransformer(documentTypeMetadata,
                parameter.getDataClusterName());
        foreignKeyInfoTransformer.setMetadataRepository(metadataRepository);
        ModificationMarker modificationMarker = new ModificationMarker(modificationMarkersAction);
        UniqueIdTransformer idTransformer = new UniqueIdTransformer();
        LocalLabelTransformer multipleLanguageLabel = new LocalLabelTransformer(com.amalto.core.util.LocaleUtil.getLocale().getLanguage());
        List<DocumentTransformer> transformers = Arrays.asList(foreignKeyInfoTransformer, idTransformer, modificationMarker,multipleLanguageLabel);
        com.amalto.core.history.Document transformedDocument = document;
        for (DocumentTransformer transformer : transformers) {
            transformedDocument = document.transform(transformer);
        }
        return transformedDocument.exportToString();
    }

    public void restoreRecord(JournalParameters parameter, String language) throws Exception {
        if (UpdateReportPOJO.OPERATION_TYPE_LOGICAL_DELETE.equals(parameter.getOperationType())) {
            ItemPOJOPK refItemPOJOPK = new ItemPOJOPK(new DataClusterPOJOPK(parameter.getDataClusterName()),
                    parameter.getConceptName(), parameter.getId());
            DroppedItemPOJOPK droppedItemPOJOPK = new DroppedItemPOJOPK(refItemPOJOPK, "/"); //$NON-NLS-1$
            if (DroppedItemPOJO.load(droppedItemPOJOPK) == null) {
                throw new ServiceException(MESSAGES.getMessage(new Locale(language), "restore_logic_delete_fail")); //$NON-NLS-1$              
            }
        } else if (UpdateReportPOJO.OPERATION_TYPE_UPDATE.equals(parameter.getOperationType())) {
            ItemPOJOPK itemPOJOPK = new ItemPOJOPK(new DataClusterPOJOPK(parameter.getDataClusterName()),
                    parameter.getConceptName(), parameter.getId());
            if (ItemPOJO.load(itemPOJOPK) == null) {
                throw new ServiceException(MESSAGES.getMessage(new Locale(language), "restore_update_fail")); //$NON-NLS-1$
            }
        } else {
            throw new ServiceException(MESSAGES.getMessage(new Locale(language),
                    "restore_not_support", parameter.getOperationType())); //$NON-NLS-1$
        }
        Date historyDate = new Date(parameter.getDate());
        DocumentHistoryNavigator navigator = documentHistory.getHistory(parameter.getDataClusterName(),
                parameter.getDataModelName(), parameter.getConceptName(), parameter.getId());
        navigator.goTo(historyDate);
        com.amalto.core.history.Document document = EmptyDocument.INSTANCE;
        if (CURRENT_ACTION.equalsIgnoreCase(parameter.getAction())) {
            document = navigator.current();
        } else if (PREVIOUS_ACTION.equalsIgnoreCase(parameter.getAction())) {
            if (navigator.hasPrevious()) {
                document = navigator.previous();
            }
        } else if (NEXT_ACTION.equalsIgnoreCase(parameter.getAction())) {
            if (navigator.hasNext()) {
                document = navigator.next();
            }
        } else {
            throw new ServiceException(MESSAGES.getMessage(new Locale(language), "action_not_supported", parameter.getAction())); //$NON-NLS-1$
        }
        document.restore();
    }
}
