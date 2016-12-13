/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.commmon.util.datamodel.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.impl.RestrictionSimpleTypeImpl;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class BusinessConcept {

    private static final boolean Lazy_Load = true;

    public static final String APPINFO_X_HIDE = "X_Hide"; //$NON-NLS-1$

    public static final String APPINFO_X_WRITE = "X_Write"; //$NON-NLS-1$

    public static final String APPINFO_X_DEFAULT_VALUE_RULE = "X_Default_Value_Rule"; //$NON-NLS-1$

    public static final String APPINFO_X_VISIBLE_RULE = "X_Visible_Rule"; //$NON-NLS-1$

    public static final String APPINFO_X_FOREIGNKEY = "X_ForeignKey"; //$NON-NLS-1$

    private static final String TYPE_PREFIX = "xsd:"; //$NON-NLS-1$

    private XSElementDecl e;

    private String name;

    private String correspondTypeName;

    private Map<String, String> defaultValueRulesMap;

    private Map<String, String> visibleRulesMap;

    private Map<String, String> foreignKeyMap;

    private Map<String, String> inheritanceForeignKeyMap;

    private List<ReusableType> reuseTypeList;

    private Set<String> parentTypeNameSet;

    private Map<String, ReusableType> subReuseTypeMap;

    private Map<String, String> xpathTypeMap;
    
    private Map<String, String> xpathDerivedSimpleTypeMap;

    private List<String> keyFieldPaths;

    // TODO: translate it from technique to business logic
    // annotations{label,access rules,foreign keys,workflow,schematron,lookup fields...}
    // restrictions
    // enumeration


    public BusinessConcept() {

    }

    public BusinessConcept(XSElementDecl e) {
        super();
        this.e = e;
        this.name = e.getName();
        this.correspondTypeName = e.getType().getName();
        if (!Lazy_Load)
            load();
    }

    public XSElementDecl getE() {
        return e;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCorrespondTypeName() {
        return correspondTypeName;
    }

    private void findFkByReuseType(String xPath, ReusableType reuseType, Set<String> nameSet){
        Map<String, ReusableType> map = reuseType.getxPathReusableTypeMap();
        if(map.size() == 0)
            return;
        
        Set<String> keySet = map.keySet();
        for(String key : keySet){
            ReusableType type = map.get(key);
            String pathWithoutName = key.replaceFirst("/" + reuseType.getName(), ""); //$NON-NLS-1$//$NON-NLS-2$
            if (type == null)
                continue;
            Map<String, String> fkMap = type.getForeignKeyMap();
            if(fkMap.size() > 0){
                Set<String> fkKeySet = fkMap.keySet();
                for (String str : fkKeySet) {
                    String fk = fkMap.get(str);
                    String fkWithoutName = str.replaceFirst("/" + type.getName(), ""); //$NON-NLS-1$//$NON-NLS-2$                        
                    inheritanceForeignKeyMap.put(xPath + pathWithoutName + fkWithoutName, fk);
                }
            }
            
            if(!nameSet.contains(type.getName())){
                nameSet.add(type.getName());
                this.findFkByReuseType(xPath + pathWithoutName, type, nameSet);
            }            
        }
    }
    
    public void load() {
        beforeLoad();
        travelXSElement(getE(), "/" + getName()); //$NON-NLS-1$
        if (subReuseTypeMap.size() > 0) {
            Set<String> keySet = subReuseTypeMap.keySet();
            for (String key : keySet) {
                ReusableType reuseType = subReuseTypeMap.get(key);
                String pathWithoutName = key.replaceFirst(reuseType.getName() + ":", ""); //$NON-NLS-1$//$NON-NLS-2$
                Set<String> nameSet = new HashSet<String>();
                Map<String, String> fkMap = reuseType.getForeignKeyMap();
                if (fkMap.size() > 0) {
                    Set<String> fkKeySet = fkMap.keySet();
                    for (String str : fkKeySet) {
                        String fk = fkMap.get(str);
                        String fkWithoutName = str.replaceFirst("/" + reuseType.getName(), ""); //$NON-NLS-1$//$NON-NLS-2$                        
                        inheritanceForeignKeyMap.put(pathWithoutName + fkWithoutName, fk);
                    }
                }
                
                nameSet.add(reuseType.getName());
                this.findFkByReuseType(pathWithoutName, reuseType, nameSet);
                
            }
        }
    }

    private void beforeLoad() {
        // prepare map
        defaultValueRulesMap = new HashMap<String, String>();
        visibleRulesMap = new HashMap<String, String>();
        foreignKeyMap = new HashMap<String, String>();
        inheritanceForeignKeyMap = new HashMap<String, String>();
        subReuseTypeMap = new HashMap<String, ReusableType>();
        xpathTypeMap = new HashMap<String, String>();
        xpathDerivedSimpleTypeMap = new HashMap<String, String>();
        keyFieldPaths = new ArrayList<String>();
    }

    public Map<String, String> getXpathDerivedSimpleTypeMap() {
        return this.xpathDerivedSimpleTypeMap;
    }

    public Map<String, String> getDefaultValueRulesMap() {
        return defaultValueRulesMap;
    }

    public Map<String, String> getVisibleRulesMap() {
        return visibleRulesMap;
    }

    public Map<String, String> getForeignKeyMap() {
        return foreignKeyMap;
    }

    public Map<String, String> getXpathTypeMap() {
        return xpathTypeMap;
    }

    public void setXpathTypeMap(Map<String, String> xpathTypeMap) {
        this.xpathTypeMap = xpathTypeMap;
    }

    private void setSubReuseType(String typeName, String currentXPath) {
        if (typeName == null)
            return;
        if (parentTypeNameSet == null || !parentTypeNameSet.contains(typeName))
            return;

        for (ReusableType reuseType : reuseTypeList) {
            if (reuseType.getParentName().equalsIgnoreCase(typeName)) {
                subReuseTypeMap.put(reuseType.getName() + ":" + currentXPath, reuseType); //$NON-NLS-1$
                if (parentTypeNameSet.contains(reuseType.getName())) {
                    setSubReuseType(reuseType.getName(), currentXPath);
                }
            }
        }
    }

    /**
     * DOC Starkey Comment method "getSubReuseTypeMap".
     * 
     * @return
     */
    public Map<String, ReusableType> getSubReuseTypeMap() {
        return subReuseTypeMap;
    }

    /**
     * DOC HSHU Comment method "travelXSElement". go through XSElement
     * 
     * @param e
     * @param currentXPath
     */
    private void travelXSElement(XSElementDecl e, String currentXPath) {
        if (e != null) {
            //set base type
            setTypeMap(e, currentXPath);

            // set key filed xpath
            setKeyFiledPaths(e);

            // parse annotation
            parseAnnotation(currentXPath, e);

            if (e.getType().isComplexType()) {
                setSubReuseType(e.getType().getName(), currentXPath);
                XSModelGroup group = e.getType().asComplexType().getContentType().asParticle().getTerm().asModelGroup();
                if (group != null) {
                    XSParticle[] subParticles = group.getChildren();
                    if (subParticles != null) {
                        for (int i = 0; i < subParticles.length; i++) {
                            XSParticle xsParticle = subParticles[i];
                            travelParticle(xsParticle, currentXPath);
                        }
                    }
                }
            }

        }
    }

    private void setKeyFiledPaths(XSElementDecl e) {
        List<XSIdentityConstraint> idConstraints = e.getIdentityConstraints();
        if (idConstraints != null) {
            for (XSIdentityConstraint xsIdentityConstraint : idConstraints) {
                String selector = null;
                if (xsIdentityConstraint.getSelector() != null && xsIdentityConstraint.getSelector().getXPath() != null)
                    selector = xsIdentityConstraint.getSelector().getXPath().value;

                // must have selector
                if (selector != null) {
                    String prefix = selector.equals(".") ? "" : "/"+selector; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                    List<XSXPath> fields = xsIdentityConstraint.getFields();
                    if (fields != null && fields.size() > 0) {
                        for (XSXPath xsxPath : fields) {
                            if (xsxPath.getXPath() != null) {
                                StringBuilder keyPath = new StringBuilder()
                                        .append(this.getName()).append(prefix).append("/") //$NON-NLS-1$ //$NON-NLS-2$
                                        .append(xsxPath.getXPath().value);
                                keyFieldPaths.add(keyPath.toString());
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * Set TypeMap
     * 
     * @param e
     * @param currentXPath
     */
    private void setTypeMap(XSElementDecl e, String currentXPath) {
        if (e != null && currentXPath != null) {

            if (currentXPath.startsWith("/")) //$NON-NLS-1$
                currentXPath = currentXPath.substring(1);

            if (e.getType() == null) {
                xpathTypeMap.put(currentXPath, ReusableType.UNKNOWN_TYPE);
            }else{
                if (e.getType() instanceof XSComplexType) {
                    xpathTypeMap.put(currentXPath, ReusableType.COMPLEX_TYPE);
                } else if (e.getType() instanceof XSSimpleType) {
                    if (isDerivedSimpleType(e)) {
                        xpathTypeMap.put(currentXPath, TYPE_PREFIX + e.getType().getBaseType().getName());
                        xpathDerivedSimpleTypeMap.put(currentXPath, TYPE_PREFIX + e.getType().getName());
                    } else {
                        xpathTypeMap.put(currentXPath, TYPE_PREFIX + e.getType().getName());
                    }
                }
            }
        }
    }

    private boolean isDerivedSimpleType(XSElementDecl e) {
        return e.getType().getBaseType() instanceof RestrictionSimpleTypeImpl;
    }

    private void travelParticle(XSParticle xsParticle, String currentXPath) {
        if (xsParticle.getTerm().asModelGroup() != null) {
            XSParticle[] xsps = xsParticle.getTerm().asModelGroup().getChildren();
            for (int j = 0; j < xsps.length; j++) {
                travelParticle(xsps[j], currentXPath);
            }
        } else if (xsParticle.getTerm().asElementDecl() != null) {
            XSElementDecl subElement = xsParticle.getTerm().asElementDecl();
            travelXSElement(subElement, currentXPath + "/" + subElement.getName()); //$NON-NLS-1$
        }
    }

    private void parseAnnotation(String currentXPath, XSElementDecl e) {
        if (e.getAnnotation() != null && e.getAnnotation().getAnnotation() != null) {
            Element annotations = (Element) e.getAnnotation().getAnnotation();
            NodeList annotList = annotations.getChildNodes();
            for (int k = 0; k < annotList.getLength(); k++) {
                if ("appinfo".equals(annotList.item(k).getLocalName())) { //$NON-NLS-1$
                    Node source = annotList.item(k).getAttributes().getNamedItem("source"); //$NON-NLS-1$
                    if (source == null)
                        continue;
                    String appinfoSource = source.getNodeValue();
                    if (annotList.item(k) != null && annotList.item(k).getFirstChild() != null) {
                        String appinfoSourceValue = annotList.item(k).getFirstChild().getNodeValue();
                        if (appinfoSource.equals(BusinessConcept.APPINFO_X_DEFAULT_VALUE_RULE)) {
                            defaultValueRulesMap.put(currentXPath, appinfoSourceValue);
                        } else if (appinfoSource.equals(BusinessConcept.APPINFO_X_VISIBLE_RULE)) {
                            visibleRulesMap.put(currentXPath, appinfoSourceValue);
                        } else if (appinfoSource.equals(BusinessConcept.APPINFO_X_VISIBLE_RULE)) {
                            visibleRulesMap.put(currentXPath, appinfoSourceValue);
                        } else if (appinfoSource.equals(BusinessConcept.APPINFO_X_FOREIGNKEY)) {
                            foreignKeyMap.put(currentXPath, appinfoSourceValue);
                        }
                    }

                }
            }
        }
    }

    @Override
    public String toString() {
        return "BusinessConcept [name=" + name + "]"; //$NON-NLS-1$//$NON-NLS-2$
    }

    public Map<String, String> getInheritanceForeignKeyMap() {
        return inheritanceForeignKeyMap;
    }

    public void setInheritanceForeignKeyMap(Map<String, String> inheritanceForeignKeyMap) {
        this.inheritanceForeignKeyMap = inheritanceForeignKeyMap;
    }

    public List<ReusableType> getReuseTypeList() {
        return reuseTypeList;
    }

    public void setReuseTypeList(List<ReusableType> reuseTypeList) {
        this.reuseTypeList = reuseTypeList;
        parentTypeNameSet = new HashSet<String>();
        if (reuseTypeList != null) {
            for (ReusableType type : reuseTypeList) {
                if (!type.getParentName().equalsIgnoreCase("anyType")) //$NON-NLS-1$
                    parentTypeNameSet.add(type.getParentName());
            }
        }
    }

    public List<String> getKeyFieldPaths() {
        return keyFieldPaths;
    }

}
