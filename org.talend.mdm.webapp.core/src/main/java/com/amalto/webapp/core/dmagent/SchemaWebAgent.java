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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.commmon.util.datamodel.management.BusinessConcept;
import org.talend.mdm.commmon.util.datamodel.management.DataModelBean;
import org.talend.mdm.commmon.util.datamodel.management.DataModelID;
import org.talend.mdm.commmon.util.datamodel.management.ReusableType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.core.util.LocalUser;
import com.amalto.webapp.core.bean.Configuration;
import com.amalto.webapp.core.dwr.CommonDWR;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;

public class SchemaWebAgent extends SchemaAbstractWebAgent {

    private static SchemaWebAgent agent;

    private SchemaWebAgent() {
    }

    public static SchemaWebAgent getInstance() {
        if (agent == null) {
            agent = new SchemaWebAgent();
        }

        return agent;
    }

    @Override
    protected void addToPool(DataModelID dataModelID, DataModelBean dataModelBean) {
        DataModelWebPool.getUniqueInstance().put(dataModelID, dataModelBean);
    }

    @Override
    protected boolean existInPool(DataModelID dataModelID) {
        DataModelBean dataModelBean = DataModelWebPool.getUniqueInstance().get(dataModelID);
        return !(dataModelBean == null);
    }

    @Override
    protected void removeFromPool(DataModelID dataModelID) {
        DataModelWebPool.getUniqueInstance().remove(dataModelID);
    }

    @Override
    protected DataModelBean getFromPool(DataModelID dataModelID) throws Exception {
        return DataModelWebPool.getUniqueInstance().get(dataModelID);
    }

    @Override
    public BusinessConcept getBusinessConcept(String conceptName) throws Exception {
        DataModelID dataModelID = getMyDataModelTicket();
        return super.getBusinessConcept(conceptName, dataModelID);
    }

    public BusinessConcept getFirstBusinessConceptFromRootType(String typeName) throws Exception {
        BusinessConcept targetBusinessConcept = null;
        DataModelID dataModelID = getMyDataModelTicket();
        List<BusinessConcept> businessConcepts = super.getBusinessConcepts(dataModelID);
        for (BusinessConcept businessConcept : businessConcepts) {
            String businessConceptTypeName = businessConcept.getCorrespondTypeName();
            if (businessConceptTypeName != null && businessConceptTypeName.equals(typeName)) {
                targetBusinessConcept = businessConcept;
                break;
            }
        }
        return targetBusinessConcept;
    }

    @Override
    public ReusableType getReusableType(String typeName) throws Exception {
        DataModelID dataModelID = getMyDataModelTicket();
        return super.getReusableType(typeName, dataModelID);
    }

    @Override
    public List<ReusableType> getMySubtypes(String parentTypeName) throws Exception {
        return getMySubtypes(parentTypeName, false);
    }

    @Override
    public List<ReusableType> getMySubtypes(String parentTypeName, boolean deep) throws Exception {
        return getMySubtypes(parentTypeName, deep, getFromPool(getMyDataModelTicket()));
    }

    public List<ReusableType> getMyParents(String subTypeName) throws Exception {
        DataModelBean dataModelBean = getFromPool(getMyDataModelTicket());
        List<ReusableType> reusableTypes = dataModelBean.getReusableTypes();
        List<ReusableType> parentsTypes = new ArrayList<ReusableType>();
        getParents(subTypeName, reusableTypes, parentsTypes);
        return parentsTypes;
    }

    private void getParents(String subTypeName, List<ReusableType> reusableTypes, List<ReusableType> parentsTypes) {
        for (ReusableType reusableType : reusableTypes) {
            if (reusableType.getName().equals(subTypeName)) {
                String parentName = reusableType.getParentName();
                ReusableType type = findParentType(parentName, reusableTypes);
                if (type != null) {
                    parentsTypes.add(type);
                    getParents(parentName, reusableTypes, parentsTypes);
                }
            }
        }
    }

    private ReusableType findParentType(String parName, List<ReusableType> reusableTypes) {
        ReusableType reUsable = null;
        for (ReusableType reusableType : reusableTypes) {
            if (reusableType.getName().equals(parName)) {
                reUsable = reusableType;
                break;
            }
        }
        return reUsable;
    }

    public boolean isMySubType(String parentTypeName, String subTypeName) throws Exception {
        List<ReusableType> subTypes = getMySubtypes(parentTypeName);
        return subTypes.contains(new ReusableType(subTypeName));
    }

    public boolean equalOrInheritanceEntities(String superEntityName, String subEntityName) throws Exception {
        if (superEntityName.equals(subEntityName)) {
            return true;
        } else {
            DataModelBean dataModelBean = getFromPool(getMyDataModelTicket());
            BusinessConcept superBusinessConcept = dataModelBean.getBusinessConcept(superEntityName);
            BusinessConcept subBusinessConcept = dataModelBean.getBusinessConcept(subEntityName);
            if (superBusinessConcept != null && subBusinessConcept != null) {
                return isMySubType(superBusinessConcept.getCorrespondTypeName(), subBusinessConcept.getCorrespondTypeName());
            } else {
                return false;
            }
        }
    }

    private DataModelID getMyDataModelTicket() throws Exception {
        Configuration config = Configuration.getConfiguration();
        String dataModelPK = config.getModel();
        return new DataModelID(dataModelPK);
    }

    public Map<String, String> getReferenceEntities(ReusableType reusableType, String entityName) {
        Map<String, String> references = new HashMap<String, String>();
        XSParticle xsparticle = reusableType.getXsParticle();
        List<XSParticle> xsps = reusableType.getAllChildren(xsparticle);
        String reusableName = reusableType.getName();

        if (xsps.size() > 0) {
            for (XSParticle xsp : xsps) {
                XSAnnotation xsa = xsp.getTerm().asElementDecl() == null ? null : xsp.getTerm().asElementDecl().getAnnotation();
                String name = xsp.getTerm().asElementDecl().getName();
                if (xsa != null && xsa.getAnnotation() != null) {
                    Element el = (Element) xsa.getAnnotation();
                    NodeList annotList = el.getChildNodes();
                    for (int k = 0; k < annotList.getLength(); k++) {
                        if ("appinfo".equals(annotList.item(k).getLocalName())) { //$NON-NLS-1$
                            Node source = annotList.item(k).getAttributes().getNamedItem("source"); //$NON-NLS-1$
                            if (source == null) {
                                continue;
                            }
                            String appinfoSource = annotList.item(k).getAttributes().getNamedItem("source").getNodeValue(); //$NON-NLS-1$
                            if ("X_ForeignKey".equals(appinfoSource)) { //$NON-NLS-1$
                                String nodeValue = annotList.item(k).getFirstChild().getNodeValue();
                                if (nodeValue.startsWith(entityName)) {
                                    references.put(reusableName + "/" + name, nodeValue); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                }
            }
        }

        return references;
    }

    public List<String> getReferenceEntities(String entityName) throws Exception {
        List<String> references = new ArrayList<String>();
        DataModelID dataModelID = getMyDataModelTicket();
        DataModelBean dataModelBean = getFromPool(dataModelID);
        List<BusinessConcept> businessConcepts = dataModelBean.getBusinessConcepts();
        List<ReusableType> reuseTypeList = dataModelBean.getReusableTypes();
        Map<String, ReusableType> reusableTypeMap = dataModelBean.getReusableTypeMap();
        // Pre-load Reusable Types
        for (ReusableType type : reuseTypeList) {
            type.load(reusableTypeMap);// load/parse the reusableType
        }
        // Find possible ReusableTypes which has FK to point to the target entity
        Map<String, ReusableType> possibleReusableTypeMap = new HashMap<String, ReusableType>();
        for (ReusableType type : reuseTypeList) {
            Map<String, String> foreignKeyMap = type.getForeignKeyMap();
            // does this reUsableType point to the target entity
            if (foreignKeyMap != null && foreignKeyMap.size() > 0) {
                for (String xpathOnEntity : foreignKeyMap.keySet()) {
                    String fkPath = foreignKeyMap.get(xpathOnEntity);
                    String myEntityName = getEntityNameFromXPath(fkPath);
                    if (isValidatedEntityName(myEntityName, dataModelBean)
                            && equalOrInheritanceEntities(myEntityName, entityName)) {
                        // if true, add it to the possible map
                        possibleReusableTypeMap.put(getEntityNameFromXPath(xpathOnEntity), type);
                        // find all types which has this type
                        List<ReusableType> containTypes = getMyContainsType(reuseTypeList, type);
                        if (containTypes != null && containTypes.size() > 0) {
                            for (ReusableType containType : containTypes) {
                                possibleReusableTypeMap.put(containType.getName(), containType);
                            }
                        }
                        // also find all parent types if have
                        List<ReusableType> myParentTypes = getMyParents(type.getName());
                        if (myParentTypes != null && myParentTypes.size() > 0) {
                            for (ReusableType parentType : myParentTypes) {
                                possibleReusableTypeMap.put(parentType.getName(), parentType);
                            }
                        }
                    }
                }
            }
        }
        // Add possible entities to the result set based on the business concept schema
        for (BusinessConcept businessConcept : businessConcepts) {
            businessConcept.setReuseTypeList(reuseTypeList);
            businessConcept.load();
            Collection<String> fkPaths = null;
            Map<String, String> myForeignKeyMap = businessConcept.getForeignKeyMap();
            if (myForeignKeyMap != null) {
                fkPaths = myForeignKeyMap.values();
            }
            if (fkPaths != null) {
                for (String fkPath : fkPaths) {
                    // if the fkpath is pointing to this entity
                    if (fkPath != null && getEntityNameFromXPath(fkPath) != null
                            && getEntityNameFromXPath(fkPath).equals(entityName)) {
                        if (!references.contains(businessConcept.getName())) {
                            references.add(businessConcept.getName());
                        }
                    }

                }
            }
        }
        // Add possible entities to the result set based on the reusable type schema
        // Imply possible entities based on a possible reusableTypeList
        for (String theTypeName : possibleReusableTypeMap.keySet()) {
            // if we could find the corresponding entities of this reusable type then add it/them to the list
            for (BusinessConcept businessConcept : businessConcepts) {
                // check root type
                if (businessConcept.getCorrespondTypeName() != null
                        && businessConcept.getCorrespondTypeName().equals(theTypeName)) {
                    if (!references.contains(businessConcept.getName())) {
                        references.add(businessConcept.getName());
                    }
                    continue;
                }
                // check children node via a flat result map
                Map<String, ReusableType> subTypesOfTargetEntity = businessConcept.getSubReuseTypeMap();
                for (String keyPath : subTypesOfTargetEntity.keySet()) {
                    if (subTypesOfTargetEntity.get(keyPath) != null
                            && subTypesOfTargetEntity.get(keyPath).getName().equals(theTypeName)) {
                        if (!references.contains(businessConcept.getName())) {
                            references.add(businessConcept.getName());
                        }
                        break;
                    }
                }
            }
        }
        return references;
    }

    private List<ReusableType> getMyContainsType(List<ReusableType> reuseTypeList, ReusableType type) {
        List<ReusableType> containTypes = new ArrayList<ReusableType>();
        for (ReusableType reusableType : reuseTypeList) {
            if (reusableType.getxPathReusableTypeMap() != null && reusableType.getxPathReusableTypeMap().size() > 0) {
                for (ReusableType toCheckType : reusableType.getxPathReusableTypeMap().values()) {
                    if (toCheckType != null && toCheckType.getName() != null && toCheckType.getName().equals(type.getName())) {
                        containTypes.add(toCheckType);
                    }
                }
            }
        }
        return containTypes;
    }

    private boolean isValidatedEntityName(String entityName, DataModelBean dataModelBean) {
        if (entityName == null || entityName.trim().length() == 0) {
            return false;
        }
        return dataModelBean.getBusinessConceptMap().containsKey(entityName);
    }

    public String getEntityNameFromXPath(String xpath) {
        if (xpath == null || xpath.trim().length() == 0) {
            return null;
        }
        if (xpath.startsWith("//")) { //$NON-NLS-1$
            xpath = xpath.substring(2);
        } else if (xpath.startsWith("/")) { //$NON-NLS-1$
            xpath = xpath.substring(1);
        }
        String[] xPortions = xpath.split("/"); //$NON-NLS-1$
        String entityName = null;
        if (xPortions != null && xPortions.length > 0) {
            entityName = xPortions[0];
        }
        return entityName;
    }

    public List<String> getBindingType(XSElementDecl e) throws Exception {
        List<String> types = new ArrayList<String>();

        if (e.getType().isComplexType()) {
            XSParticle[] subParticles = e.getType().asComplexType().getContentType().asParticle().getTerm().asModelGroup()
                    .getChildren();
            if (subParticles != null) {
                for (XSParticle xsParticle : subParticles) {
                    if (xsParticle.getTerm().asElementDecl() == null) {
                        continue;
                    }
                    String type = xsParticle.getTerm().asElementDecl().getType().getName();
                    List<ReusableType> subTypes = getMySubtypes(type, true);
                    if (subTypes.size() > 0) {
                        types.add(type);
                    }
                }
            }
        }

        return types;
    }

    public boolean isEntityDenyPhysicalDeletable(String concept) throws Exception {
        Configuration config = Configuration.getConfiguration();
        Map<String, XSElementDecl> conceptMap = CommonDWR.getConceptMap(config.getModel());
        XSElementDecl declaration = conceptMap.get(concept);
        if (declaration == null) {
            return false;
        }
        XSAnnotation xsa = declaration.getAnnotation();
        Collection<String> roles = LocalUser.getLocalUser().getRoles();
        if (xsa != null && xsa.getAnnotation() != null) {
            Element el = (Element) xsa.getAnnotation();
            NodeList annotationList = el.getChildNodes();
            for (int k = 0; k < annotationList.getLength(); k++) {
                if ("appinfo".equals(annotationList.item(k).getLocalName())) { //$NON-NLS-1$
                    Node source = annotationList.item(k).getAttributes().getNamedItem("source"); //$NON-NLS-1$
                    if (source == null) {
                        continue;
                    }
                    String appInfoSource = annotationList.item(k).getAttributes().getNamedItem("source").getNodeValue(); //$NON-NLS-1$
                    if ("X_Deny_PhysicalDelete".equals(appInfoSource)) { //$NON-NLS-1$
                        if (roles.contains(annotationList.item(k).getFirstChild().getNodeValue())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<BusinessConcept> getAllBusinessConcepts() throws Exception {
        return super.getBusinessConcepts(getMyDataModelTicket());
    }
}
