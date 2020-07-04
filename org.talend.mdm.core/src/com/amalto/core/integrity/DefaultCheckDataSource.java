/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.integrity;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;
import com.amalto.xmlserver.interfaces.WhereCondition;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.talend.mdm.commmon.metadata.*;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static com.amalto.core.integrity.FKIntegrityCheckResult.*;

class DefaultCheckDataSource implements FKIntegrityCheckDataSource {

    private final static Logger logger = LogManager.getLogger(DefaultCheckDataSource.class);

    public String getDataModel(String clusterName, String concept, String[] ids) throws XtentisException {
        String dataModel;
        try {
            ItemPOJOPK pk = new ItemPOJOPK(new DataClusterPOJOPK(clusterName), concept, ids);
            ItemPOJO item = Util.getItemCtrl2Local().getItem(pk);
            if (item == null) {
                String id = StringUtils.EMPTY;
                for (String currentIdValue : ids) {
                    id += "[" + currentIdValue + "]"; //$NON-NLS-1$ //$NON-NLS-2$
                }

                throw new RuntimeException("Document with id '" //$NON-NLS-1$
                        + id
                        + "' (concept name: '" //$NON-NLS-1$
                        + concept
                        + "') has already been deleted."); //$NON-NLS-1$
            } else {

                dataModel = item.getDataModelName();
            }
        } catch (Exception e) {
            throw new XtentisException(e);
        }
        return dataModel;
    }

    public long countInboundReferences(String clusterName, String[] ids, String fromTypeName, ReferenceFieldMetadata fromReference)
            throws XtentisException {
        // For the anonymous type and leave the type name empty
        if (fromTypeName == null || fromTypeName.trim().equals("")) { //$NON-NLS-1$
            return 0;
        }
        // Transform ids into the string format expected in base
        StringBuilder referencedId = new StringBuilder(); //$NON-NLS-1$
        for (String id : ids) {
            referencedId.append('[').append(id).append(']');
        }
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage storage = storageAdmin.get(clusterName, storageAdmin.getType(clusterName));
        MetadataRepository repository = storage.getMetadataRepository();
        ComplexTypeMetadata complexType = repository.getComplexType(fromTypeName);
        Set<List<FieldMetadata>> paths = StorageMetadataUtils.paths(complexType, fromReference);
        long inboundReferenceCount = 0;
        for (List<FieldMetadata> path : paths) {
            StringBuilder builder = new StringBuilder();
            builder.append(complexType.getName()).append('/');
            for (FieldMetadata fieldMetadata : path) {
                builder.append(fieldMetadata.getName()).append('/');
            }
            String leftPath = builder.toString();
            IWhereItem whereItem = new WhereCondition(leftPath,
                    WhereCondition.EQUALS,
                    referencedId.toString(),
                    WhereCondition.NO_OPERATOR);
            inboundReferenceCount += Util.getXmlServerCtrlLocal().countItems(clusterName, fromTypeName, whereItem);
        }
        return inboundReferenceCount;
    }

    public boolean isFKReferencedBySelf(String clusterName, String[] fkIds, String fromTypeName,
            ReferenceFieldMetadata fromReference) throws XtentisException {
        if (StringUtils.isBlank(fromTypeName)) {
            return false;
        }
        String[] pkIds = getItemPKsByCriteria(clusterName, fkIds, fromTypeName, fromReference);
        if (Arrays.equals(fkIds, pkIds) && fromTypeName.equals(fromReference.getReferencedType().getName())) {
            return true;
        }
        return false;
    }

    /*
     * This function refers to: IXtentisWSDelegator.java#doGetItemPKsByCriteria
     * Used FK entity ids to get the PK ids which the FK was referenced.
     */
    private String[] getItemPKsByCriteria(String clusterName, String[] fkIds, String fromTypeName,
            ReferenceFieldMetadata fromReference) throws XtentisException {
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage storage = storageAdmin.get(clusterName, storageAdmin.getType(clusterName));
        MetadataRepository repository = storage.getMetadataRepository();
        ComplexTypeMetadata complexType = repository.getComplexType(fromTypeName);
        Set<List<FieldMetadata>> paths = StorageMetadataUtils.paths(complexType, fromReference);

        StringBuilder builder = new StringBuilder();
        for (List<FieldMetadata> path : paths) {
            builder.append(complexType.getName()).append('/');
            for (FieldMetadata fieldMetadata : path) {
                builder.append(fieldMetadata.getName()).append('/');
            }
        }
        String xpathString = builder.toString();
        xpathString = StringUtils.removeEnd(xpathString, "/"); //$NON-NLS-1$

        // Transform ids into the string format
        StringBuilder referencedId = new StringBuilder();
        for (String id : fkIds) {
            referencedId.append('[').append(id).append(']');
        }
        // Expect to have a format as: $EntityA/EntityB/EntityAFk$[id]
        String keysKeywords = "$" + xpathString + "$" + referencedId; //$NON-NLS-1$ //$NON-NLS-2$

        ItemPKCriteria criteria = new ItemPKCriteria();
        criteria.setClusterName(clusterName);
        criteria.setConceptName(fromTypeName);
        criteria.setKeysKeywords(keysKeywords);
        criteria.setCompoundKeyKeywords(false);
        criteria.setFromDate(new Date(0).getTime());
        criteria.setToDate(new Date().getTime());
        criteria.setUseFTSearch(false);
        List<String> results = Util.getItemCtrl2Local().getItemPKsByCriteria(criteria);
        XPath xpath = XPathFactory.newInstance().newXPath();
        DocumentBuilder documentBuilder = MDMXMLUtils.getDocumentBuilder().get();
        String[] pkIds = null;
        try {
            String result = results.get(1);
            Element element = documentBuilder.parse(new InputSource(new StringReader(result))).getDocumentElement();
            NodeList pksIdsList = (NodeList) xpath.evaluate("./ids/i", element, XPathConstants.NODESET); //$NON-NLS-1$
            pkIds = new String[pksIdsList.getLength()];
            for (int j = 0; j < pksIdsList.getLength(); j++) {
                pkIds[j] = (pksIdsList.item(j).getFirstChild() == null ? "" //$NON-NLS-1$
                        : pksIdsList.item(j).getFirstChild().getNodeValue());
            }
        } catch (Exception e) {
            throw new XtentisException(e);
        }
        return pkIds;
    }

    public Set<ReferenceFieldMetadata> getForeignKeyList(String concept, String dataModel) throws XtentisException {
        // Get FK(s) to check
        MetadataRepository mr = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin().get(dataModel);
        TypeMetadata type = mr.getType(concept);
        if (type != null) {
            return mr.accept(new InboundReferences(type));
        } else {
            logger.warn("Type '" + concept + "' does not exist anymore in data model '" + dataModel + "'. No integrity check will be performed."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return Collections.emptySet();
        }
    }

    public void resolvedConflict(Map<FKIntegrityCheckResult, Set<FieldMetadata>> checkResultToFields, FKIntegrityCheckResult conflictResolution) {
        if (logger.isInfoEnabled()) {
            logger.info("Found conflicts in data model relative to FK integrity checks"); //$NON-NLS-1$
            logger.info("= Forbidden deletes ="); //$NON-NLS-1$
            dumpFields(FORBIDDEN, checkResultToFields);
            logger.info("= Forbidden deletes (override allowed) ="); //$NON-NLS-1$
            dumpFields(FORBIDDEN_OVERRIDE_ALLOWED, checkResultToFields);
            logger.info("= Allowed deletes ="); //$NON-NLS-1$
            dumpFields(ALLOWED, checkResultToFields);
            logger.info("Conflict resolution: " + conflictResolution); //$NON-NLS-1$
        }
    }

    private static void dumpFields(FKIntegrityCheckResult checkResult, Map<FKIntegrityCheckResult, Set<FieldMetadata>> checkResultToFields) {
        Set<FieldMetadata> fields = checkResultToFields.get(checkResult);
        if (fields != null) {
            for (FieldMetadata fieldMetadata : fields) {
                logger.info(fieldMetadata.toString());
            }
        }
    }
}
