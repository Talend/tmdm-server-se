/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.bizhelpers;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.datamodel.management.ReusableType;
import org.talend.mdm.webapp.base.server.util.CommonUtil;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.FacetModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.base.shared.TypePath;
import org.talend.mdm.webapp.browserecords.client.creator.DataTypeCreator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.amalto.core.util.Util;
import com.amalto.core.webservice.WSConceptKey;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSGetBusinessConceptKey;
import com.amalto.webapp.core.dmagent.SchemaAbstractWebAgent;
import com.amalto.webapp.core.dmagent.SchemaWebAgent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;

public class DataModelHelper {

    private static final Logger logger = Logger.getLogger(DataModelHelper.class);

    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$

    private static XSElementDecl eleDecl;

    private static SchemaAbstractWebAgent schemaManager = SchemaWebAgent.getInstance();

    private static Map<String, List<String>> aliasXpathMap = new HashMap<String, List<String>>();

    public static void overrideSchemaManager(SchemaAbstractWebAgent _schemaManager) {
        schemaManager = _schemaManager;
    }

    public static XSElementDecl getEleDecl() {
        return eleDecl;
    }

    public static void parseSchema(String model, String concept, EntityModel entityModel, Collection<String> roles) {
        parseSchema(model, concept, null, null, entityModel, roles);
    }

    /**
     * This method shared eleDecl and made the thread unsafe, so it should be synchronized.
     */
    public static synchronized void parseSchema(String model, String concept, XSElementDecl elDecl, String[] ids,
            EntityModel entityModel,
            Collection<String> roles) {

        entityModel.setConceptName(concept);

        // set pk
        if (ids == null) {
            ids = getBusinessConceptKeys(model, concept);
        }
        entityModel.setKeys(ids);

        // analyst model
        if (elDecl == null) {
            elDecl = getBusinessConcept(model, concept);
        }
        eleDecl = elDecl;

        if (eleDecl != null) {
            travelXSElement(eleDecl, eleDecl.getName(), entityModel, null, roles, 0, 0);
            aliasXpathMap.clear();
        }

    }

    /**
     * DOC Administrator Comment method "convertXsd2ElDecl".
     * 
     * @param concept
     * @param xsd
     * @return
     * @throws SAXException
     */
    public static XSElementDecl convertXsd2ElDecl(String concept, String xsd) throws SAXException {
        XSElementDecl eleDecl;
        XSOMParser reader = new XSOMParser();
        reader.setAnnotationParser(new DomAnnotationParserFactory());
        reader.parse(new StringReader(xsd));
        XSSchemaSet xss = reader.getResult();
        eleDecl = getElementDeclByName(concept, xss);
        return eleDecl;
    }

    /**
     * DOC HSHU Comment method "getBusinessConcept".
     * 
     * @throws Exception
     */
    public static XSElementDecl getBusinessConcept(String model, String concept) {
        XSElementDecl eleDecl = null;
        try {
            eleDecl = schemaManager.getBusinessConcept(concept).getE();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return eleDecl;
    }

    /**
     * DOC HSHU Comment method "getElementDeclByName".
     * 
     * @param eleName
     * @param xss
     * @return
     */
    public static XSElementDecl getElementDeclByName(String eleName, XSSchemaSet xss) {
        Collection<XSSchema> schemas = xss.getSchemas();
        for (XSSchema xsa : schemas) {
            Map<String, XSElementDecl> elems = xsa.getElementDecls();
            XSElementDecl eleDecl = elems.get(eleName);
            if (eleDecl != null) {
                return eleDecl;
            }
        }
        return null;
    }

    /**
     * DOC HSHU Comment method "travelXSElement". go through XSElement
     * 
     * @param e
     * @param currentXPath
     */
    public static void travelXSElement(XSElementDecl e, String currentXPath, EntityModel entityModel,
            ComplexTypeModel parentTypeModel, Collection<String> roles, int minOccurs, int maxOccurs) {
        if (e != null) {

            TypeModel typeModel = null;

            // parse element
            typeModel = parseElement(currentXPath, e, typeModel, entityModel, roles);

            // parse annotation
            if (typeModel != null) {
                parseAnnotation(e, typeModel, roles);
                typeModel.setMinOccurs(minOccurs);
                typeModel.setMaxOccurs(maxOccurs);
            }

            // set parent typeModel
            if (parentTypeModel != null) {
                parentTypeModel.addSubType(typeModel);
            }

            if (typeModel instanceof ComplexTypeModel) {
                parentTypeModel = (ComplexTypeModel) typeModel;
            }

            // add to entityModel
            if (typeModel != null) {
                entityModel.getMetaDataTypes().put(currentXPath, typeModel);
            }

            // recursion travel
            if (e.getType().isComplexType()) {
                XSModelGroup group = e.getType().asComplexType().getContentType().asParticle().getTerm().asModelGroup();
                if (group != null) {
                    XSParticle[] subParticles = group.getChildren();
                    if (subParticles != null) {
                        for (XSParticle xsParticle : subParticles) {
                            travelParticle(xsParticle, currentXPath, entityModel, parentTypeModel, roles);
                        }
                    }
                }
            }

        }
    }

    private static TypeModel parseElement(String currentXPath, XSElementDecl e, TypeModel typeModel, EntityModel entityModel,
            Collection<String> roles) {
        String typeName = e.getType().getName();
        String baseTypeName;

        if (e.getType().isComplexType()) {
            baseTypeName = e.getType().getBaseType().getName();
            typeModel = new ComplexTypeModel(e.getName(), DataTypeCreator.getDataType(typeName, baseTypeName));

            // go for polymiorphism
            List<ReusableType> subTypes = null;
            try {
                subTypes = schemaManager.getMySubtypes(typeName, true);
            } catch (Exception e1) {
                logger.error(e1.getMessage(), e1);
                return null;
            }

            if (subTypes != null && subTypes.size() > 0) {
                ReusableType parentReusableType = new ReusableType(e.getType());
                parentReusableType.load();
                typeModel.setAbstract(parentReusableType.isAbstract());
                ComplexTypeModel parentType = (ComplexTypeModel) typeModel;
                ComplexTypeModel abstractReusableComplexType = new ComplexTypeModel(typeName, DataTypeCreator.getDataType(
                        typeName, baseTypeName));
                abstractReusableComplexType.setAbstract(parentReusableType.isAbstract());
                abstractReusableComplexType.setLabelMap(parentReusableType.getLabelMap());
                parentType.addComplexReusableTypes(abstractReusableComplexType);
                if (typeModel.isAbstract()) {
                    parentType.addComplexReusableTypes(new ComplexTypeModel(StringUtils.EMPTY, DataTypeCreator.getDataType(
                            typeName, baseTypeName)));
                }
                ReusableType abstractReusable = null;
                try {
                    abstractReusable = schemaManager.getReusableType(typeName);
                } catch (Exception e1) {
                    logger.error(e1.getMessage(), e1);
                    return null;
                }
                XSModelGroup abstractGroup = abstractReusable.getXsParticle().getTerm().asModelGroup();
                if (abstractGroup != null) {
                    XSParticle[] subParticles = abstractGroup.getChildren();
                    if (subParticles != null) {
                        for (XSParticle xsParticle : subParticles) {
                            List<String> currentXPathAlias = aliasXpathMap.get(currentXPath);
                            if (currentXPathAlias == null) {
                                currentXPathAlias = new ArrayList<String>();
                                aliasXpathMap.put(currentXPath, currentXPathAlias);
                            }
                            currentXPathAlias.add(currentXPath + ":" + abstractReusable.getName()); //$NON-NLS-1$
                            travelParticle(xsParticle, currentXPath, entityModel, abstractReusableComplexType, roles);
                        }
                    }
                }
                for (ReusableType reusableType : subTypes) {
                    if (!reusableType.isAbstract()) {
                        ComplexTypeModel reusableComplexType = new ComplexTypeModel(reusableType.getName(),
                                DataTypeCreator.getDataType(reusableType.getName(), baseTypeName));
                        reusableComplexType.setPolymorphism(true);
                        reusableComplexType.setLabelMap(reusableType.getLabelMap());
                        int orderValue;
                        if (reusableType.getOrderValue() == null) {
                            orderValue = 0;
                        }
                        try {
                            orderValue = Integer.parseInt(reusableType.getOrderValue());
                        } catch (NumberFormatException nfe) {
                            orderValue = 0;
                        }
                        reusableComplexType.setOrderValue(orderValue);
                        XSModelGroup group = reusableType.getXsParticle().getTerm().asModelGroup();
                        if (group != null) {
                            XSParticle[] subParticles = group.getChildren();
                            if (subParticles != null) {
                                for (XSParticle xsParticle : subParticles) {
                                    travelParticle(xsParticle,
                                            currentXPath + ":" + reusableType.getName(), entityModel, reusableComplexType, roles); //$NON-NLS-1$ 
                                }
                            }
                        }
                        parentType.addComplexReusableTypes(reusableComplexType);
                    }
                }
            }

        } else if (e.getType().isSimpleType()) {
            baseTypeName = getBuiltinPrimitiveType(e.getType()).getName();
            typeModel = new SimpleTypeModel(e.getName(), DataTypeCreator.getDataType(typeName, baseTypeName));

            // enumeration&&facet
            XSRestrictionSimpleType restirctionType = e.getType().asSimpleType().asRestriction();
            ArrayList<FacetModel> restrictions = new ArrayList<FacetModel>();
            ArrayList<String> enumeration = new ArrayList<String>();
            if (restirctionType != null) {
                Iterator<XSFacet> it = restirctionType.iterateDeclaredFacets();
                while (it.hasNext()) {
                    XSFacet xsf = it.next();
                    if ("enumeration".equals(xsf.getName())) { //$NON-NLS-1$
                        enumeration.add(xsf.getValue().toString());
                    } else {
                        FacetModel r = new FacetModel(xsf.getName(), xsf.getValue().toString());
                        restrictions.add(r);
                    }
                }// end while
                ((SimpleTypeModel) typeModel).setEnumeration(enumeration);
                ((SimpleTypeModel) typeModel).setFacets(restrictions);
            }// end if

        } else {
            // return null
        }
        if (typeModel != null) {
            typeModel.setXpath(convertTypePath2Xpath(currentXPath));
            typeModel.setTypePath(currentXPath);
            typeModel.setTypePathObject(new TypePath(currentXPath, aliasXpathMap));
            typeModel.setNillable(e.isNillable());
        }
        return typeModel;
    }

    private static String convertTypePath2Xpath(String typePath) {

        if (typePath == null || typePath.trim().length() == 0) {
            return ""; //$NON-NLS-1$
        }
        StringBuilder resultPath = new StringBuilder();
        String[] paths = typePath.split("/"); //$NON-NLS-1$
        for (String path : paths) {
            if (path == null || path.trim().length() == 0) {
                continue;
            }
            int pos = path.indexOf(":"); //$NON-NLS-1$
            if (pos != -1) {
                path = path.substring(0, pos);
            }
            if (resultPath.toString().length() > 0) {
                resultPath.append("/"); //$NON-NLS-1$
            }
            resultPath.append(path);
        }

        return resultPath.toString();

    }

    private static XSType getBuiltinPrimitiveType(XSType type) {
        // See http://www.w3.org/TR/xmlschema-2/#built-in-primitive-datatypes
        // Suitable for simple types only
        assert type.isSimpleType();

        // Special handling for non-primitive type integer
        if (SCHEMA_NAMESPACE.equals(type.getTargetNamespace()) && "integer".equals(type.getName())) { //$NON-NLS-1$
            return type;
        }

        XSType baseType = type.getBaseType();
        assert baseType != null;
        // Drill down to get the primitive type
        if (!SCHEMA_NAMESPACE.equals(baseType.getTargetNamespace()) || !"anySimpleType".equals(baseType.getName())) { //$NON-NLS-1$
            baseType = getBuiltinPrimitiveType(baseType);
            return baseType;
        }
        return type;
    }

    private static void travelParticle(XSParticle xsParticle, String currentXPath, EntityModel entityModel,
            ComplexTypeModel parentTypeModel, Collection<String> roles) {
        if (xsParticle.getTerm().asModelGroup() != null) {
            XSParticle[] xsps = xsParticle.getTerm().asModelGroup().getChildren();
            for (XSParticle xsp : xsps) {
                travelParticle(xsp, currentXPath, entityModel, parentTypeModel, roles);
            }
        } else if (xsParticle.getTerm().asElementDecl() != null) {
            XSElementDecl subElement = xsParticle.getTerm().asElementDecl();
            travelXSElement(
                    subElement,
                    currentXPath + "/" + subElement.getName(), entityModel, parentTypeModel, roles, xsParticle.getMinOccurs().intValue(), xsParticle.getMaxOccurs().intValue()); //$NON-NLS-1$
        }
    }

    private static void parseAnnotation(XSElementDecl e, TypeModel typeModel, Collection<String> roles) {
        boolean writable = false;
        ArrayList<String> pkInfoList = new ArrayList<String>();
        ArrayList<String> fkInfoList = new ArrayList<String>();
        if (e.getAnnotation() != null && e.getAnnotation().getAnnotation() != null) {
            Element annotations = (Element) e.getAnnotation().getAnnotation();
            NodeList annotList = annotations.getChildNodes();
            for (int k = 0; k < annotList.getLength(); k++) {
                if ("appinfo".equals(annotList.item(k).getLocalName())) {//$NON-NLS-1$                   
                    Node source = annotList.item(k).getAttributes().getNamedItem("source");//$NON-NLS-1$                                                    
                    if (source == null) {
                        continue;
                    }
                    String appinfoSource = source.getNodeValue();
                    if (annotList.item(k) != null && annotList.item(k).getFirstChild() != null) {
                        String appinfoSourceValue = annotList.item(k).getFirstChild().getNodeValue().trim();
                        if (appinfoSource.contains("X_Label")) {//$NON-NLS-1$
                            typeModel.addLabel(getLangFromLabelAnnotation(appinfoSource), appinfoSourceValue);
                        } else if (appinfoSource.contains("X_Description")) {//$NON-NLS-1$
                            String description = appinfoSourceValue;
                            String encodedDESP = description != null ? StringEscapeUtils.escapeHtml(description) : "";//$NON-NLS-1$ //Do we need escape?
                            typeModel.addDescription(getLangFromDescAnnotation(appinfoSource), encodedDESP);
                        } else if ("X_Write".equals(appinfoSource)) {//$NON-NLS-1$
                            if (roles.contains(appinfoSourceValue)) {
                                writable = true;
                            }
                        } else if ("X_Hide".equals(appinfoSource)) {//$NON-NLS-1$
                            if (roles.contains(appinfoSourceValue)) {
                                typeModel.setHide(true) ;
                                typeModel.setVisible(!typeModel.isHide());
                            }
                        } else if ("X_ForeignKey".equals(appinfoSource)) {//$NON-NLS-1$
                            typeModel.setForeignkey(appinfoSourceValue);
                        } else if ("X_ForeignKey_NotSep".equals(appinfoSource)) {//$NON-NLS-1$
                            boolean notSeparateFk = appinfoSourceValue == null ? false : Boolean.valueOf(appinfoSourceValue);
                            typeModel.setNotSeparateFk(notSeparateFk);
                        } else if ("X_Retrieve_FKinfos".equals(appinfoSource)) {//$NON-NLS-1$
                            typeModel.setRetrieveFKinfos("true".equals(appinfoSourceValue));//$NON-NLS-1$
                        } else if ("X_ForeignKeyInfo".equals(appinfoSource)) {//$NON-NLS-1$
                            fkInfoList.add(appinfoSourceValue);
                        } else if ("X_ForeignKeyInfoFormat".equals(appinfoSource)) {//$NON-NLS-1$
                            typeModel.setForeignKeyInfoFormat(appinfoSourceValue);
                        } else if ("X_ForeignKey_Filter".equals(appinfoSource)) {//$NON-NLS-1$
                            typeModel.setForeignKeyFilter(appinfoSourceValue);
                        } else if ("X_PrimaryKeyInfo".equals(appinfoSource)) {//$NON-NLS-1$
                            pkInfoList.add(appinfoSourceValue);
                        } else if (appinfoSource.indexOf("X_Facet_") != -1) {//$NON-NLS-1$
                            typeModel.addFacetErrorMsg(getLangFromFacetAnnotation(appinfoSource), appinfoSourceValue);
                        } else if (appinfoSource.indexOf("X_Display_Format_") != -1) {//$NON-NLS-1$
                            typeModel.addDisplayFomat(getLangFromDisplayAnnotation(appinfoSource), appinfoSourceValue);
                        } else if ("X_Deny_Create".equals(appinfoSource)) {//$NON-NLS-1$
                            if (roles.contains(appinfoSourceValue)) {
                                typeModel.setDenyCreatable(true);
                            }
                        } else if ("X_Deny_LogicalDelete".equals(appinfoSource)) {//$NON-NLS-1$
                            if (roles.contains(appinfoSourceValue)) {
                                typeModel.setDenyLogicalDeletable(true);
                            }
                        } else if ("X_Deny_PhysicalDelete".equals(appinfoSource)) {//$NON-NLS-1$
                            if (roles.contains(appinfoSourceValue)) {
                                typeModel.setDenyPhysicalDeleteable(true);
                            }
                        } else if ("X_Default_Value_Rule".equals(appinfoSource)) { //$NON-NLS-1$
                            typeModel.setDefaultValueExpression(appinfoSourceValue);
                        } else if ("X_Visible_Rule".equals(appinfoSource)) { //$NON-NLS-1$
                            typeModel.setHasVisibleRule(true);
                            typeModel.setVisibleExpression(appinfoSourceValue);
                        } else if ("X_AutoExpand".equals(appinfoSource)) { //$NON-NLS-1$
                            String v = annotList.item(k).getFirstChild().getNodeValue();

                            if (v != null) {
                                typeModel.setAutoExpand(Boolean.valueOf(v));
                            }
                        }
                    }
                }
            }// end for
        }
        if (!Util.isEnterprise()) {
            typeModel.setReadOnly(false);
        } else {
            typeModel.setReadOnly(!writable);
        }
        typeModel.setForeignKeyInfo(fkInfoList);
        typeModel.setPrimaryKeyInfo(pkInfoList);
    }

    /**
     * DOC HSHU Comment method "getLangFromLabelAnnotation".
     */
    private static String getLangFromLabelAnnotation(String label) {
        String format = "X_Label_(.+)";//$NON-NLS-1$
        String lang = getLangFromAnnotation(label, format);
        return lang;
    }

    private static String getLangFromDescAnnotation(String label) {
        String format = "X_Description_(.+)";//$NON-NLS-1$
        String lang = getLangFromAnnotation(label, format);
        return lang;
    }

    private static String getLangFromFacetAnnotation(String label) {
        String format = "X_Facet_(.+)";//$NON-NLS-1$
        String lang = getLangFromAnnotation(label, format);
        return lang;
    }

    private static String getLangFromDisplayAnnotation(String label) {
        String format = "X_Display_(.+)";//$NON-NLS-1$
        String lang = getLangFromAnnotation(label, format);
        return lang;
    }

    private static String getLangFromAnnotation(String label, String format) {
        String lang = "EN";//$NON-NLS-1$
        Pattern p = Pattern.compile(format);
        Matcher matcher = p.matcher(label);
        while (matcher.find()) {
            lang = matcher.group(1);
        }
        if (lang != null) {
            lang = lang.toLowerCase();
        }
        return lang;
    }

    private static String[] getBusinessConceptKeys(String model, String concept) {

        String[] keys = null;
        try {
            WSConceptKey key = CommonUtil.getPort().getBusinessConceptKey(
                    new WSGetBusinessConceptKey(new WSDataModelPK(model), concept));

            WSConceptKey copyKey = new WSConceptKey();
            copyKey.setFields((String[]) ArrayUtils.clone(key.getFields()));
            copyKey.setSelector(new String(key.getSelector()));

            keys = copyKey.getFields();
            for (int i = 0; i < keys.length; i++) {
                if (".".equals(key.getSelector())) { //$NON-NLS-1$
                    keys[i] = concept + "/" + keys[i]; //$NON-NLS-1$ 
                } else {
                    keys[i] = key.getSelector() + keys[i];
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return keys;

    }

    /**
     * @param metaDataTypes
     * @param typePath
     * @return
     * @throws TypeModelNotFoundException
     */
    public static TypeModel findTypeModelByTypePath(Map<String, TypeModel> metaDataTypes, String typePath)
            throws TypeModelNotFoundException {

        if (metaDataTypes == null || typePath == null) {
            throw new IllegalArgumentException();
        }

        TypeModel model = null;

        model = metaDataTypes.get(typePath);
        if (model == null) {
            for (String keyTypePath : metaDataTypes.keySet()) {
                TypeModel myTypeModel = metaDataTypes.get(keyTypePath);
                if (myTypeModel.getTypePathObject() != null && myTypeModel.getTypePathObject().hasVariantion()) {
                    List<String> allPossibleTypepath = myTypeModel.getTypePathObject().getAllAliasXpaths();
                    if (allPossibleTypepath != null && allPossibleTypepath.contains(typePath)) {
                        model = myTypeModel;
                        break;
                    }
                }
            }
        }

        if (model == null) {
            throw new TypeModelNotFoundException(typePath);
        } else {
            return model;
        }
    }
}
