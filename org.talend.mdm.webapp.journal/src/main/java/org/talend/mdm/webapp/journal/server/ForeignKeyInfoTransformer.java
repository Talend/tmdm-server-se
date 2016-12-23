/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.webapp.base.server.ForeignKeyHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.amalto.core.history.Document;
import com.amalto.core.history.DocumentTransformer;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

public class ForeignKeyInfoTransformer implements DocumentTransformer {

    private final TypeMetadata metadata;

    private final String dataClusterName;

    private MetadataRepository metadataRepository = null;

    Stack<String> xpath = new Stack<String>();

    String xpathStr = null;

    private final static Logger LOG = Logger.getLogger(ForeignKeyInfoTransformer.class);

    public ForeignKeyInfoTransformer(TypeMetadata metadata, String dataClusterName) {
        this.metadata = metadata;
        this.dataClusterName = dataClusterName;
        this.metadataRepository = new MetadataRepository();
    }

    public void setMetadataRepository(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public Document transform(MutableDocument document) {
        Map<ReferenceFieldMetadata, String> referenceFieldMetadataAndPaths = metadata.accept(new ForeignKeyInfoResolver());

        for (ReferenceFieldMetadata referenceFieldMetadata : referenceFieldMetadataAndPaths.keySet()) {
            List<PathItem> pathItems = getPathItems(referenceFieldMetadata);
            doTransform(document, referenceFieldMetadata, pathItems, 0);
        }

        return document;
    }

    private void doTransform(MutableDocument document, ReferenceFieldMetadata referenceFieldMetadata, List<PathItem> pathItems,
            int currentIndex) {
        PathItem currentItem = pathItems.get(currentIndex);
        boolean branchPathExists = document.createAccessor(getPath(pathItems, currentIndex)).exist();
        while (branchPathExists) {
            if (currentIndex == pathItems.size() - 1) {
                setForeignKeyValues(document, referenceFieldMetadata, pathItems);
                branchPathExists = false;
            } else {
                doTransform(document, referenceFieldMetadata, pathItems, currentIndex + 1);
                for (int i = currentIndex + 1; i < pathItems.size(); i++) {
                    pathItems.get(i).reset();
                }
                if (currentItem.isMany) {
                    currentItem.next();
                    branchPathExists = document.createAccessor(getPath(pathItems, currentIndex)).exist();
                } else {
                    branchPathExists = false;
                }
            }
        }
    }

    private static class PathItem {

        public String name;

        public boolean isMany;

        public int index;

        public PathItem(String name, boolean isMany) {
            this.name = name;
            this.isMany = isMany;
            if (isMany) {
                index = 1;
            } else {
                index = 0;
            }
        }

        public String getPath() {
            if (isMany) {
                return name + "[" + index + "]"; //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                return name;
            }
        }

        public void reset() {
            if (isMany) {
                this.index = 1;
            }
        }

        public void next() {
            if (isMany) {
                this.index++;
            }
        }

        @Override
        public String toString() {
            return "[Name=" + name + ", isMany=" + isMany + ", index=" + index + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

    }

    private List<PathItem> getPathItems(ReferenceFieldMetadata referenceFieldMetadata) {
        List<PathItem> paths = new ArrayList<PathItem>();
        paths.add(new PathItem(referenceFieldMetadata.getName(), referenceFieldMetadata.isMany()));
        processField(paths, referenceFieldMetadata.getContainingType().getContainer());
        Collections.reverse(paths);
        return paths;
    }

    private void processField(List<PathItem> paths, FieldMetadata field) {
        if (field != null) {
            paths.add(new PathItem(field.getName(), field.isMany()));
            if (field instanceof ContainedTypeFieldMetadata) {
                processField(paths, field.getContainingType().getContainer());
            }
        }
    }

    private String getPath(List<PathItem> items, int currentBranchIndex) {
        String path = ""; //$NON-NLS-1$
        for (int i = 0; i < items.size(); i++) {
            path += items.get(i).getPath();
            if (i == currentBranchIndex) {
                break;
            }
            if (i < items.size() - 1) {
                path += "/"; //$NON-NLS-1$
            }
        }
        return path;
    }

    public void setForeignKeyValues(MutableDocument document, ReferenceFieldMetadata referenceFieldMetadata,
            List<PathItem> pathItems) {
        PathItem leafItem = pathItems.get(pathItems.size() - 1);
        if (leafItem.isMany) {
            boolean pathExists = true;
            while (pathExists) {
                pathExists = setForeignKeyValue(document, getPath(pathItems, pathItems.size() - 1), referenceFieldMetadata);
                leafItem.next();
            }
        } else {
            setForeignKeyValue(document, getPath(pathItems, pathItems.size() - 1), referenceFieldMetadata);
        }
    }

    private boolean setForeignKeyValue(MutableDocument document, String path, ReferenceFieldMetadata referenceFieldMetadata) {
        Accessor accessor = document.createAccessor(path);
        if (accessor.exist()) { // The field might not be set, so check if it exists.
            String foreignKeyValue = accessor.get(); // Raw foreign key value (surrounded by "[")
            String resolvedForeignKeyInfo = resolveForeignKeyValue(referenceFieldMetadata, foreignKeyValue); // Value to be displayed to users

            if ("".equals(resolvedForeignKeyInfo)) { //$NON-NLS-1$
                resolvedForeignKeyInfo = foreignKeyValue;
            }
            accessor.set(resolvedForeignKeyInfo);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Set value " + resolvedForeignKeyInfo + " to " + path); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return true;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(path + " does not exists"); //$NON-NLS-1$
        }
        return false;
    }

    private String resolveForeignKeyValue(ReferenceFieldMetadata foreignKeyField, String foreignKeyValue) {
        String referencedTypeName = foreignKeyField.getReferencedType().getName();
        String returnForeignKeyValue = foreignKeyValue;
        Map<String, String> foreignKeyInfoMap = new HashMap<String, String>();
        ItemPOJO item;
        try {
            ItemPOJOPK pk = new ItemPOJOPK();
            pk.setConceptName(referencedTypeName);
            pk.setDataClusterPOJOPK(new DataClusterPOJOPK(dataClusterName));
            // For composite keys, format is "[id0][id1]...[idN]"
            String[] allKeys = returnForeignKeyValue.split("]"); //$NON-NLS-1$
            String[] key = new String[allKeys.length];
            int i = 0;
            for (String currentKey : allKeys) {
                key[i++] = currentKey.substring(1);
            }
            pk.setIds(key);

            item = Util.getItemCtrl2Local().getItem(pk);
        } catch (Exception e) {
            LOG.warn("Unable to access referenced entity in field '" //$NON-NLS-1$
                    + foreignKeyField.getName() + "' in type '" //$NON-NLS-1$
                    + foreignKeyField.getContainingType().getName() + "' (foreign key value: '" //$NON-NLS-1$
                    + returnForeignKeyValue + "')"); //$NON-NLS-1$
            return returnForeignKeyValue;
        }

        try {
            Element element = item.getProjection();
            StringBuilder foreignKeyInfo = new StringBuilder();
            for (FieldMetadata fieldMetadata : foreignKeyField.getForeignKeyInfoFields()) {
                String xpathToForeignKeyInfoField = null;
                if (referencedTypeName.equals(fieldMetadata.getContainingType().getName())) {
                    xpathToForeignKeyInfoField = "/" + referencedTypeName + "/" + fieldMetadata.getName();//$NON-NLS-1$ //$NON-NLS-2$
                } else {// for fkInfo fields defined in reusable types or annoymous types
                    xpathToForeignKeyInfoField = getResolvedXpath(referencedTypeName, fieldMetadata);
                }
                NodeList nodeList = Util.getNodeList(element, xpathToForeignKeyInfoField);
                if (nodeList.getLength() == 1 && nodeList.item(0).getTextContent() != null
                        && !nodeList.item(0).getTextContent().isEmpty()) {
                    if (foreignKeyInfo.length() > 0) {
                        foreignKeyInfo.append("-"); //$NON-NLS-1$
                    }
                    foreignKeyInfo.append(nodeList.item(0).getTextContent());
                    if(xpathToForeignKeyInfoField != null && xpathToForeignKeyInfoField.length() > 1){
                        foreignKeyInfoMap.put(xpathToForeignKeyInfoField.substring(1), nodeList.item(0).getTextContent());
                    }
                }
            }
            if (foreignKeyField.getForeignKeyInfoFields().size() > 0) {
                if(foreignKeyField.getForeignKeyInfoFormat() != null && foreignKeyField.getForeignKeyInfoFormat().length() > 0){
                    returnForeignKeyValue = ForeignKeyHelper.convertFKInfo2DisplayInfoByFormatDefinition(foreignKeyInfoMap, foreignKeyField.getForeignKeyInfoFormat());
                } else {
                    returnForeignKeyValue = foreignKeyInfo.toString();
                }
            }            
            return returnForeignKeyValue;
        } catch (XtentisException e) {
            throw new RuntimeException(e);
        }
    }

    private String getResolvedXpath(String referencedTypeName, FieldMetadata fieldMetadata) {
        if (this.xpath.empty()) {
            ComplexTypeMetadata referenceTypeMeta = (ComplexTypeMetadata) metadataRepository.getType(referencedTypeName);

            if (referenceTypeMeta == null) {
                throw new IllegalArgumentException(
                        "Cannot find type information for type '" + referencedTypeName + "' in data cluster '" + dataClusterName + "', in data model '" + dataClusterName + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }

            xpath.push(referencedTypeName);
            buildXpath(referenceTypeMeta, fieldMetadata);
            setXPathString();
            xpath.clear();
        }
        return this.xpathStr;
    }

    private void buildXpath(ComplexTypeMetadata referenceTypeMeta, FieldMetadata fieldMetadata) {

        for (FieldMetadata entityField : referenceTypeMeta.getFields()) {
            String currentFieldTypeName;
            if (entityField instanceof ContainedTypeFieldMetadata) {
                currentFieldTypeName = entityField.getType().getName();
                ComplexTypeMetadata containedType = ((ContainedTypeFieldMetadata) entityField).getContainedType();

                xpath.push(entityField.getName());
                if (currentFieldTypeName.equals(fieldMetadata.getContainingType().getName())) {
                    xpath.push(fieldMetadata.getName());
                    return;
                } else {
                    buildXpath(containedType, fieldMetadata);
                    if (xpath.peek().equals(fieldMetadata.getName())) {
                        return;
                    } else {
                        xpath.pop();
                    }
                }
            }
        }
    }

    private void setXPathString() {
        StringBuilder path = new StringBuilder("/"); //$NON-NLS-1$
        Iterator<String> iterator = xpath.iterator();
        while (iterator.hasNext()) {
            path.append(iterator.next());
            if (iterator.hasNext()) {
                path.append('/');
            }
        }

        this.xpathStr = path.toString();
    }

    private static class ForeignKeyInfoResolver extends DefaultMetadataVisitor<Map<ReferenceFieldMetadata, String>> {

        private final Map<ReferenceFieldMetadata, String> references = new HashMap<ReferenceFieldMetadata, String>();

        private StringBuilder path = null;

        @Override
        public Map<ReferenceFieldMetadata, String> visit(ComplexTypeMetadata metadata) {
            super.visit(metadata);
            return references;
        }

        @Override
        public Map<ReferenceFieldMetadata, String> visit(ReferenceFieldMetadata metadata) {
            if (metadata.hasForeignKeyInfo()) {
                if (!metadata.getPath().equals("")) { //$NON-NLS-1$
                    references.put(metadata, metadata.getPath());
                } else {
                    references.put(metadata, path.toString());
                }
            }
            super.visit(metadata);
            return references;
        }

        @Override
        public Map<ReferenceFieldMetadata, String> visit(FieldMetadata fieldMetadata) {
            super.visit(fieldMetadata);
            return references;
        }

        @Override
        public Map<ReferenceFieldMetadata, String> visit(SimpleTypeFieldMetadata metadata) {
            super.visit(metadata);
            return references;
        }

        @Override
        public Map<ReferenceFieldMetadata, String> visit(ContainedTypeFieldMetadata containedField) {
            path = new StringBuilder(""); //$NON-NLS-1$
            path.append(containedField.getName());
            super.visit(containedField);
            if (containedField.getContainedType().getSubTypes().size() > 0) {
                Collection<ComplexTypeMetadata> fields = containedField.getContainedType().getSubTypes();
                for (ComplexTypeMetadata fieldType : fields) {
                    path.append("/" + fieldType.getName()); //$NON-NLS-1$
                    fieldType.accept(this);
                }
            }
            return null;
        }

        @Override
        public Map<ReferenceFieldMetadata, String> visit(ContainedComplexTypeMetadata containedType) {
            Collection<FieldMetadata> fields = containedType.getFields();
            for (FieldMetadata field : fields) {
                path.append("/" + field.getName()); //$NON-NLS-1$
                field.accept(this);
            }

            return null;
        }

    }
}
