// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.base.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.MetadataVisitor;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeMetadata;
import org.talend.mdm.commmon.metadata.Types;
import org.talend.mdm.commmon.util.core.EDBType;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.datamodel.management.BusinessConcept;
import org.talend.mdm.commmon.util.datamodel.management.ReusableType;
import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.server.util.CommonUtil;
import org.talend.mdm.webapp.base.server.util.Constants;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.base.shared.XpathUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSGetItemsByCustomFKFilters;
import com.amalto.core.webservice.WSInt;
import com.amalto.core.webservice.WSStringArray;
import com.amalto.core.webservice.WSWhereAnd;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.WSWhereItem;
import com.amalto.core.webservice.WSWhereOperator;
import com.amalto.core.webservice.WSXPathsSearch;
import com.amalto.webapp.core.dmagent.SchemaAbstractWebAgent;
import com.amalto.webapp.core.dmagent.SchemaWebAgent;
import com.amalto.webapp.core.util.Util;
import com.amalto.webapp.core.util.XmlUtil;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.extjs.gxt.ui.client.Style.SortDir;

public class ForeignKeyHelper {

    private static final Logger LOG = Logger.getLogger(ForeignKeyHelper.class);

    private static final Pattern TOTAL_COUNT_PATTERN = Pattern.compile("<totalCount>(.*)</totalCount>"); //$NON-NLS-1$

    private static SchemaAbstractWebAgent schemaManager = SchemaWebAgent.getInstance();

    public static void overrideSchemaManager(SchemaAbstractWebAgent _schemaManager) {
        schemaManager = _schemaManager;
    }

    public static ForeignKeyBean getForeignKeyBean(TypeModel model, EntityModel entityModel, String foreignKeyFilterValue,
            String dataClusterPK, String ids, String xml, String currentXpath, String language) throws Exception {
        ForeignKeyBean foreignKeyBean = null;

        boolean hasCompositeKey = false;
        if (entityModel.getKeys() != null && entityModel.getKeys().length > 1) {
            hasCompositeKey = true;
        }
        ForeignKeyHolder holder;
        if (hasCompositeKey && ids.contains(".")) { //$NON-NLS-1$
            model.setFilterValue(ids.split("[.]")[0]); //$NON-NLS-1$
        } else {
            model.setFilterValue(ids);
        }
        holder = getForeignKeyHolder(model, foreignKeyFilterValue);
        String[] results = null;
        if (holder != null) {
            String conceptName = holder.conceptName;
            List<String> xPaths = holder.xpaths;
            WSWhereItem whereItem = holder.whereItem;
            String fkFilter = holder.fkFilter;

            // Run the query
            if (!Util.isCustomFilter(fkFilter)) {
                results = CommonUtil
                        .getPort()
                        .xPathsSearch(
                                new WSXPathsSearch(new WSDataClusterPK(dataClusterPK), null, new WSStringArray(xPaths
                                        .toArray(new String[xPaths.size()])), whereItem, -1, 0, 20, null, null, true))
                        .getStrings();
            } else {
                String injectedXpath = Util.getInjectedXpath(fkFilter);
                results = CommonUtil
                        .getPort()
                        .getItemsByCustomFKFilters(
                                new WSGetItemsByCustomFKFilters(new WSDataClusterPK(dataClusterPK), conceptName,
                                        new WSStringArray(xPaths.toArray(new String[xPaths.size()])), injectedXpath, 0, 20, null,
                                        null, true, whereItem)).getStrings();
            }
        }
        if (results != null) {
            List<ForeignKeyBean> foreignKeyBeanList = convertForeignKeyBeanList(results, model, entityModel, dataClusterPK, 0,
                    language);
            if (foreignKeyBeanList != null && foreignKeyBeanList.size() > 0) {
                if (foreignKeyBeanList.size() > 1) {
                    for (ForeignKeyBean bean : foreignKeyBeanList) {
                        if (bean.getId() != null) {
                            if (unwrapKeyValueToString(bean.getId(), ".").equalsIgnoreCase(ids)) { //$NON-NLS-1$
                                foreignKeyBean = bean;
                                break;
                            }
                        }
                    }
                } else {
                    foreignKeyBean = foreignKeyBeanList.get(0);
                }
            }
        }
        return foreignKeyBean;
    }

    public static String getForeignKeyFilter(boolean ifFKFilter, String dataObject, String xml, String currentXpath,
            TypeModel model) throws Exception {
        String fkFilter;
        if (ifFKFilter) {
            fkFilter = model.getForeignKeyFilter().replaceAll("&quot;", "\""); //$NON-NLS-1$ //$NON-NLS-2$
            fkFilter = parseForeignKeyFilter(xml, dataObject, fkFilter, currentXpath);
        } else {
            fkFilter = ""; //$NON-NLS-1$
        }
        return fkFilter;
    }

    public static ItemBasePageLoadResult<ForeignKeyBean> getForeignKeyList(BasePagingLoadConfigImpl config, TypeModel model,
            EntityModel entityModel, String foreignKeyFilterValue, String dataClusterPK) throws Exception {
        ForeignKeyHolder holder = getForeignKeyHolder(model, foreignKeyFilterValue);
        return _getForeignKeyList(config, model, entityModel, dataClusterPK, holder);
    }

    public static ItemBasePageLoadResult<ForeignKeyBean> _getForeignKeyList(BasePagingLoadConfigImpl config, TypeModel model,
            EntityModel entityModel, String dataClusterPK, ForeignKeyHolder holder) throws Exception {
        List<String> foreignKeyInfo = model.getForeignKeyInfo();
        String[] results = null;
        if (holder != null) {
            String conceptName = holder.conceptName;
            List<String> xPaths = holder.xpaths;
            WSWhereItem whereItem = holder.whereItem;
            String fkFilter = holder.fkFilter;

            String sortDir;
            String xpath;

            if (SortDir.ASC.equals(SortDir.findDir(config.getSortDir()))) {
                sortDir = Constants.SEARCH_DIRECTION_ASC;
            } else if (SortDir.DESC.equals(SortDir.findDir(config.getSortDir()))) {
                sortDir = Constants.SEARCH_DIRECTION_DESC;
            } else {
                sortDir = null;
            }

            if (sortDir != null) {
                if (foreignKeyInfo != null && foreignKeyInfo.size() > 0) {
                    // TMDM-5276: use substringBeforeLast in case sort field is a contained field
                    // (Entity/field1/.../fieldN)
                    xpath = config.getSortField();
                } else {
                    xpath = StringUtils.substringBefore(xPaths.get(0), "/") + "/../../i"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else {
                xpath = null;
            }

            // Run the query
            if (!Util.isCustomFilter(fkFilter)) {
                results = CommonUtil
                        .getPort()
                        .xPathsSearch(
                                new WSXPathsSearch(new WSDataClusterPK(dataClusterPK), null, new WSStringArray(xPaths
                                        .toArray(new String[xPaths.size()])), whereItem, -1, config.getOffset(), config
                                        .getLimit(), xpath, sortDir, true)).getStrings();
            } else {
                String injectedXpath = Util.getInjectedXpath(fkFilter);
                results = CommonUtil
                        .getPort()
                        .getItemsByCustomFKFilters(
                                new WSGetItemsByCustomFKFilters(new WSDataClusterPK(dataClusterPK), conceptName,
                                        new WSStringArray(xPaths.toArray(new String[xPaths.size()])), injectedXpath, config
                                                .getOffset(), config.getLimit(), xpath, sortDir, true, whereItem)).getStrings();
            }
        }
        if (results != null) {

            Matcher matcher = TOTAL_COUNT_PATTERN.matcher(results[0]);
            String count;
            if (matcher.matches()) {
                count = matcher.group(1);
            } else {
                throw new IllegalArgumentException("Total count '" + results[0] + "' does not match expected format"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            boolean isPagingAccurate = CommonUtil.getPort().isPagingAccurate(new WSInt(Integer.valueOf(count))).is_true();
            return new ItemBasePageLoadResult<ForeignKeyBean>(convertForeignKeyBeanList(results, model, entityModel,
                    dataClusterPK, config.getOffset(), (String) config.get("language")), config.getOffset(), //$NON-NLS-1$
                    Integer.valueOf(count), isPagingAccurate);
        } else {
            return new ItemBasePageLoadResult<ForeignKeyBean>(new ArrayList<ForeignKeyBean>(), config.getOffset(), 0);
        }
    }

    private static List<ForeignKeyBean> convertForeignKeyBeanList(String[] results, TypeModel model, EntityModel entityModel,
            String dataClusterPK, int offset, String language) throws Exception {
        String foreignKeyPath = model.getForeignkey();
        List<String> foreignKeyInfo = model.getForeignKeyInfo();
        List<ForeignKeyBean> fkBeans = new ArrayList<ForeignKeyBean>();
        if (LOG.isDebugEnabled()) {
            for (String result : results) {
                LOG.debug(result);
            }
        }
        String fk = foreignKeyPath.split("/")[0]; //$NON-NLS-1$
        BusinessConcept businessConcept = schemaManager.getBusinessConcept(fk);
        // init foreignKey info type
        if (foreignKeyInfo != null && foreignKeyInfo.size() > 0 && businessConcept != null) {
            businessConcept.load();
        }
        // Polymorphism FK
        boolean isPolymorphismFK = false;
        if (businessConcept != null) {
            String fkReusableType = businessConcept.getCorrespondTypeName();
            if (fkReusableType != null) {
                List<ReusableType> subTypes = SchemaWebAgent.getInstance().getMySubtypes(fkReusableType, true);
                List<ReusableType> parentTypes = SchemaWebAgent.getInstance().getMyParents(fkReusableType);
                isPolymorphismFK = subTypes.size() > 0 || parentTypes.size() > 0;
            }
        }

        for (int currentResult = 1; currentResult < results.length; currentResult++) { // TMDM-2834: first
                                                                                       // result is count
            if (results[currentResult] == null) {
                // Ignore null results.
                continue;
            }
            Element resultAsDOM = Util.parse(results[currentResult]).getDocumentElement();

            ForeignKeyBean bean = new ForeignKeyBean();
            if (businessConcept != null && isPolymorphismFK) {
                bean.setTypeName(businessConcept.getCorrespondTypeName());
                bean.setConceptName(fk);
            }
            String id = ""; //$NON-NLS-1$
            NodeList nodes = com.amalto.core.util.Util.getNodeList(resultAsDOM, "//i"); //$NON-NLS-1$
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i) instanceof Element) {
                        id += "[" + (nodes.item(i).getTextContent() == null ? "" : nodes.item(i).getTextContent()) + "]"; //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
                    }
                }
            }

            if (resultAsDOM.getNodeName().equals("result")) { //$NON-NLS-1$
                initFKBean(dataClusterPK, entityModel, resultAsDOM, bean, fk, foreignKeyInfo,
                        businessConcept != null ? businessConcept.getXpathDerivedSimpleTypeMap() : null, language);
                convertFKInfo2DisplayInfo(bean, model);
            } else {
                bean.set(resultAsDOM.getNodeName(), resultAsDOM.getTextContent().trim());
            }
            bean.setId(id);
            fkBeans.add(bean);
        }
        return fkBeans;
    }

    protected static class ForeignKeyHolder {

        String conceptName;

        List<String> xpaths;

        String orderbyPath;

        WSWhereItem whereItem;

        String fkFilter;
    }

    protected static ForeignKeyHolder getForeignKeyHolder(TypeModel model, String foreignKeyFilterValue) throws Exception {
        String foreignKeyPath = model.getForeignkey();
        List<String> foreignKeyInfo = model.getForeignKeyInfo();
        String filterValue = model.getFilterValue();
        // to verify
        String xpathInfoForeignKey;
        if (foreignKeyInfo != null && foreignKeyInfo.size() > 0) {
            xpathInfoForeignKey = Util.joinStrings(foreignKeyInfo.toArray(new String[foreignKeyInfo.size()]), ",");//$NON-NLS-1$
        } else {
            xpathInfoForeignKey = "";//$NON-NLS-1$
        }

        String initxpathForeignKey = Util.getForeignPathFromPath(foreignKeyPath);

        List<WSWhereItem> conditions = new ArrayList<WSWhereItem>();
        WSWhereCondition whereCondition = Util.getConditionFromPath(foreignKeyPath);
        if (whereCondition != null) {
            conditions.add(new WSWhereItem(whereCondition, null, null));
        }
        if (!Util.isCustomFilter(foreignKeyFilterValue)) {
            // get FK filter
            WSWhereItem filterWhereItem = Util.getConditionFromFKFilter(foreignKeyPath, xpathInfoForeignKey,
                    foreignKeyFilterValue, false);
            if (filterWhereItem != null) {
                conditions.add(filterWhereItem);
            }
        }

        initxpathForeignKey = initxpathForeignKey.split("/")[0]; //$NON-NLS-1$
        if (xpathInfoForeignKey == null) {
            xpathInfoForeignKey = ""; //$NON-NLS-1$
        }

        // foreign key set by business concept
        if (initxpathForeignKey.split("/").length == 1) { //$NON-NLS-1$
            String conceptName = initxpathForeignKey;
            // determine if we have xPath Infos: e.g. labels to display
            String[] xpathInfos = new String[1];
            if (xpathInfoForeignKey.trim().length() != 0) {
                xpathInfos = xpathInfoForeignKey.split(","); //$NON-NLS-1$
            } else {
                xpathInfos[0] = initxpathForeignKey;
            }
            filterValue = filterValue == null ? "" : filterValue; //$NON-NLS-1$
            // build query - add a content condition on the pivot if we search for a particular value
            if (filterValue != null
                    && !"".equals(filterValue.trim()) && !".*".equals(filterValue.trim()) && !"'*'".equals(filterValue.trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                WSWhereItem queryWhereItem = getFKQueryCondition(conceptName, foreignKeyPath, xpathInfoForeignKey, filterValue);
                if (queryWhereItem != null) {
                    conditions.add(queryWhereItem);
                }
            }

            WSWhereItem whereItem = null;
            if (conditions.size() > 1) {
                WSWhereAnd and = new WSWhereAnd(conditions.toArray(new WSWhereItem[conditions.size()]));
                whereItem = new WSWhereItem(null, and, null);
            } else if (conditions.size() == 1) {
                whereItem = conditions.get(0);
            }

            // add the xPath Infos Path
            ArrayList<String> xPaths = new ArrayList<String>();
            if (foreignKeyInfo.size() > 0) {
                // add the xPath Infos Path
                for (String xpathInfo : xpathInfos) {
                    xPaths.add(Util.getFormatedFKInfo(xpathInfo.replaceFirst(xpathInfo.split("/")[0], initxpathForeignKey), //$NON-NLS-1$
                            initxpathForeignKey));
                }
            }
            // add the key paths last, since there may be multiple keys
            xPaths.add(initxpathForeignKey + "/../../i"); //$NON-NLS-1$
            // order by
            String orderbyPath = null;
            if (!MDMConfiguration.getDBType().getName().equals(EDBType.QIZX.getName())) {
                if (xpathInfoForeignKey.length() != 0) {
                    orderbyPath = Util.getFormatedFKInfo(xpathInfos[0].replaceFirst(initxpathForeignKey, initxpathForeignKey),
                            initxpathForeignKey);
                }
            }
            ForeignKeyHolder holder = new ForeignKeyHolder();
            holder.xpaths = xPaths;
            holder.orderbyPath = orderbyPath;
            holder.conceptName = conceptName;
            holder.whereItem = whereItem;
            holder.fkFilter = foreignKeyFilterValue;
            return holder;
        }
        return null;
    }

    @SuppressWarnings("unused")
    protected static WSWhereItem getFKQueryCondition(String concept, String xpathForeignKey, String xpathInfoForeignKey,
            String keyValue) throws Exception {
        String initxpathForeignKey = Util.getForeignPathFromPath(xpathForeignKey);
        initxpathForeignKey = initxpathForeignKey.split("/")[0]; //$NON-NLS-1$
        String[] xpathInfos = new String[1];

        if (xpathInfoForeignKey.trim().length() != 0) {
            xpathInfos = xpathInfoForeignKey.split(","); //$NON-NLS-1$
        } else {
            xpathInfos[0] = initxpathForeignKey;
        }

        String fkWhere = initxpathForeignKey + "/../* " + WhereCondition.CONTAINS + " " + keyValue; //$NON-NLS-1$ //$NON-NLS-2$
        if (xpathInfoForeignKey.trim().length() > 0) {
            String realXpathForeignKey = null; // In studio, ForeignKey = ConceptName, but not ConceptName/Id
            List<String> fkXpathList = new ArrayList<String>();
            StringBuffer sb = new StringBuffer();

            MetadataRepository repository = CommonUtil.getCurrentRepository();
            ComplexTypeMetadata foreignType = repository.getComplexType(concept);
            FieldSearchMetadataVisitor visitor = new FieldSearchMetadataVisitor();
            visitor.setValue(keyValue);

            if (xpathForeignKey.indexOf("/") == -1) { //$NON-NLS-1$
                String[] fks = Util.getBusinessConceptKeys(initxpathForeignKey);
                if (fks != null && fks.length > 0) {
                    realXpathForeignKey = fks[0];
                    fkXpathList.addAll(Arrays.asList(fks));
                }
            } else {
                fkXpathList.add(xpathForeignKey);
            }

            fkXpathList.addAll(Arrays.asList(xpathInfos));
            if (fkXpathList != null && fkXpathList.size() > 0) {
                for (String xpath : fkXpathList) {
                    visitor.setFieldPath(xpath.split("/")); //$NON-NLS-1$
                    String operater = foreignType.accept(visitor);
                    if (operater != null) {
                        if (sb != null && sb.length() > 0) {
                            sb.append(" OR "); //$NON-NLS-1$
                        }
                        sb.append((xpath.startsWith(".") ? XpathUtil.convertAbsolutePath( //$NON-NLS-1$
                                (realXpathForeignKey != null && realXpathForeignKey.trim().length() > 0) ? realXpathForeignKey
                                        : xpathForeignKey, xpath) : xpath)
                                + " " + operater + " " + keyValue); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            fkWhere = sb.toString();
        }
        return Util.buildWhereItems(fkWhere);
    }

    protected static void initFKBean(String dataClusterPK, EntityModel entityModel, Element ele, ForeignKeyBean bean, String fk,
            List<String> getForeignKeyInfos, Map<String, String> xpathTypeMap, String language) throws Exception {
        int positionIndex = 0;
        for (int i = 0; i < ele.getChildNodes().getLength(); i++) {
            if (ele.getChildNodes().item(i) instanceof Element) {
                positionIndex++;
                Element curEle = (Element) ele.getChildNodes().item(i);
                String value = curEle.getTextContent().trim();
                String nodeName = curEle.getNodeName();
                String fkInfo;
                int keyInfoIndex = positionIndex - 1;

                if (positionIndex <= getForeignKeyInfos.size()) {
                    fkInfo = getForeignKeyInfos.get(keyInfoIndex);
                    while (!fkInfo.endsWith(nodeName) && keyInfoIndex < getForeignKeyInfos.size() - 1) {
                        fkInfo = getForeignKeyInfos.get(++keyInfoIndex);
                    }
                    if (!fkInfo.endsWith(nodeName)) {
                        fkInfo = fk + "/" + curEle.getNodeName(); //$NON-NLS-1$ 
                    }
                } else {
                    fkInfo = fk + "/" + curEle.getNodeName(); //$NON-NLS-1$
                }

                if (getForeignKeyInfos != null && getForeignKeyInfos.contains(fkInfo)) {
                    if (entityModel != null) {
                        value = getDisplayValue(curEle.getTextContent().trim(), fkInfo, dataClusterPK, entityModel, language);
                    }
                    if (xpathTypeMap != null && xpathTypeMap.containsKey(fkInfo)
                            && xpathTypeMap.get(fkInfo).equals("xsd:MULTI_LINGUAL")) { //$NON-NLS-1$
                        bean.getForeignKeyInfo().put(fkInfo, MultilanguageMessageParser.getValueByLanguage(value, language));
                    } else {
                        bean.getForeignKeyInfo().put(fkInfo, value);
                    }
                }
                bean.set(curEle.getNodeName(), value);
                initFKBean(dataClusterPK, entityModel, curEle, bean, fk, getForeignKeyInfos, xpathTypeMap, language);
            }
        }
    }

    public static String getDisplayValue(String value, String path, String dataClusterPK, EntityModel entityModel, String language)
            throws Exception {
        List<String> subFKInfoList = null;
        String subFKInfo = ""; //$NON-NLS-1$
        TypeModel subModel = entityModel.getTypeModel(path);
        if (subModel != null && subModel.getForeignKeyInfo() != null && subModel.getForeignKeyInfo().size() > 0
                && !"".equals(value)) { //$NON-NLS-1$
            subFKInfoList = subModel.getForeignKeyInfo();
        }

        if (subFKInfoList != null) {
            ItemPOJOPK pk = new ItemPOJOPK();
            String[] ids = CommonUtil.extractFKRefValue(value, language);
            for (String id : ids) {
                if (id == null || StringUtils.EMPTY.equals(id.trim())) {
                    return StringUtils.EMPTY;
                }
            }
            pk.setIds(ids);
            String conceptName = subModel.getForeignkey().split("/")[0]; //$NON-NLS-1$
            pk.setConceptName(conceptName);
            pk.setDataClusterPOJOPK(new DataClusterPOJOPK(dataClusterPK));
            ItemPOJO item = com.amalto.core.util.Util.getItemCtrl2Local().getItem(pk);
            if (item != null) {
                org.w3c.dom.Document document = item.getProjection().getOwnerDocument();
                List<String> foreignKeyInfo = subModel.getForeignKeyInfo();
                for (String foreignKeyPath : foreignKeyInfo) {
                    NodeList nodes = com.amalto.core.util.Util.getNodeList(document,
                            StringUtils.substringAfter(foreignKeyPath, "/")); //$NON-NLS-1$
                    if (nodes.getLength() == 1) {
                        String foreignKeyValue = nodes.item(0).getTextContent();

                        if (subModel.getType().equals(DataTypeConstants.MLS)) {
                            foreignKeyValue = MultilanguageMessageParser.getValueByLanguage(foreignKeyValue, language);
                        }

                        if (subFKInfo.equals("")) { //$NON-NLS-1$
                            subFKInfo += foreignKeyValue;
                        } else {
                            subFKInfo += "-" + foreignKeyValue; //$NON-NLS-1$
                        }
                    }
                }
            }
            return subFKInfo;
        } else {
            return value;
        }
    }

    public static void convertFKInfo2DisplayInfo(ForeignKeyBean bean, TypeModel model) {
        String ForeignKeyInfoFormat = model.getForeignKeyInfoFormat();
        if (ForeignKeyInfoFormat != null && ForeignKeyInfoFormat.length() > 0) {
            bean.setDisplayInfo(convertFKInfo2DisplayInfoByFormatDefinition(bean.getForeignKeyInfo(), ForeignKeyInfoFormat));
        } else {
            List<String> foreignKeyInfos = model.getForeignKeyInfo();
            if (foreignKeyInfos.size() != 0) {
                StringBuilder displayInfo = new StringBuilder();
                Map<String, String> foreignKeyInfoMap = bean.getForeignKeyInfo();
                for (String info : foreignKeyInfos) {
                    if (!info.isEmpty() && foreignKeyInfoMap.get(info) != null) {
                        if (displayInfo.length() == 0) {
                            displayInfo.append(foreignKeyInfoMap.get(info));
                        } else {
                            displayInfo.append("-"); //$NON-NLS-1$
                            displayInfo.append(foreignKeyInfoMap.get(info));
                        }
                    }
                }
                bean.setDisplayInfo(displayInfo.toString());
            }
        }
    }

    @SuppressWarnings("unused")
    public static String convertFKInfo2DisplayInfoByFormatDefinition(Map<String, String> keyInfoMap, String foreignKeyFormat) {
        String fkFormat = foreignKeyFormat;
        Map<String, String> tagValues = new HashMap<String, String>();
        Pattern TAG_REGEX = Pattern.compile("\"(.+?)\""); //$NON-NLS-1$
        Matcher matcher = TAG_REGEX.matcher(fkFormat);
        int index = 0;
        while (matcher.find()) {
            String key = "$index" + index; //$NON-NLS-1$
            tagValues.put(key, matcher.group(1));
            fkFormat = fkFormat.replace("\"" + matcher.group(1) + "\"", key); //$NON-NLS-1$ //$NON-NLS-2$
            index++;
        }

        String[] tmpSplit = fkFormat.split("\\+"); //$NON-NLS-1$
        StringBuffer sb = new StringBuffer();
        for (String s : tmpSplit) {
            if (s.contains("/")) { //$NON-NLS-1$
                if (keyInfoMap.containsKey(s.trim())) {
                    sb.append(keyInfoMap.get(s.trim()));
                }
            } else {
                if (tagValues.containsKey(s.trim())) {
                    sb.append(tagValues.get(s.trim()));
                }
            }
        }
        return sb.toString();
    }

    private static String parseForeignKeyFilter(String xml, String dataObject, String fkFilter, String currentXpath)
            throws Exception {
        String parsedFkfilter = fkFilter;
        if (fkFilter != null) {
            if (Util.isCustomFilter(fkFilter)) {
                fkFilter = StringEscapeUtils.unescapeXml(fkFilter);
                parsedFkfilter = parseRightExpression(xml, dataObject, fkFilter, currentXpath);
                return parsedFkfilter;
            }
            // parse
            String[] criterias = org.talend.mdm.webapp.base.shared.util.CommonUtil.getCriteriasByForeignKeyFilter(fkFilter);
            List<Map<String, String>> conditions = new ArrayList<Map<String, String>>();
            for (String cria : criterias) {
                Map<String, String> conditionMap = org.talend.mdm.webapp.base.shared.util.CommonUtil
                        .buildConditionByCriteria(cria);
                String value = conditionMap.get("Value"); //$NON-NLS-1$
                value = StringEscapeUtils.unescapeXml(value);
                value = parseRightValueOrPath(xml, dataObject, value, currentXpath);
                if (isFkPath(conditionMap.get("Xpath"))) { //$NON-NLS-1$
                    value = org.talend.mdm.webapp.base.shared.util.CommonUtil.wrapFkValue(value);
                } else {
                    value = org.talend.mdm.webapp.base.shared.util.CommonUtil.unwrapFkValue(value);
                }
                conditionMap.put("Value", value);//$NON-NLS-1$
                conditions.add(conditionMap);
            }
            // build
            parsedFkfilter = org.talend.mdm.webapp.base.shared.util.CommonUtil.buildForeignKeyFilterByConditions(conditions);
        }
        return parsedFkfilter;
    }

    public static String unwrapKeyValueToString(String value, String symbol) {
        if (value.startsWith("[") && value.endsWith("]")) { //$NON-NLS-1$ //$NON-NLS-2$
            StringBuffer sb = new StringBuffer();
            if (value.contains("][")) { //$NON-NLS-1$
                for (String s : value.split("]")) { //$NON-NLS-1$
                    sb = sb.append(s.substring(1, s.length()) + symbol);
                }
                return sb.substring(0, sb.length() - symbol.length());
            } else {
                return org.talend.mdm.webapp.base.shared.util.CommonUtil.unwrapFkValue(value);
            }
        } else {
            return value;
        }
    }

    public static boolean isFkPath(String fkPath) {
        String concept = fkPath.split("/")[0]; //$NON-NLS-1$
        try {
            BusinessConcept businessConcept = schemaManager.getBusinessConcept(concept);
            Map<String, String> fkMap = businessConcept.getForeignKeyMap();
            if (fkMap != null && fkMap.containsKey("/" + fkPath)) { //$NON-NLS-1$
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static String parseRightExpression(String xml, String dataObject, String rightExpression, String currentXpath)
            throws Exception {
        // Use legacy logic for custom FK filter
        String patternString = dataObject + "(/[A-Za-z0-9_]*)+";//$NON-NLS-1$
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(rightExpression);
        while (matcher.find()) {
            for (int j = 0; j < matcher.groupCount(); j++) {
                String gettedXpath = matcher.group(j);
                if (gettedXpath != null) {
                    String replaceValue = parseRightValueOrPath(xml, dataObject, gettedXpath, currentXpath);
                    if (replaceValue != null) {
                        rightExpression = rightExpression.replaceFirst(patternString, "\"" + replaceValue + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        }
        return rightExpression;

    }

    private static String parseRightValueOrPath(String xml, String dataObject, String rightValueOrPath, String currentXpath)
            throws Exception {
        if (rightValueOrPath == null || currentXpath == null) {
            throw new IllegalArgumentException();
        }
        // cases handle
        String result = rightValueOrPath;// by default result equals input value/path
        if (org.talend.mdm.webapp.base.shared.util.CommonUtil.isFilterValue(rightValueOrPath)) {
            result = rightValueOrPath.substring(1, rightValueOrPath.length() - 1);
        } else {
            if (xml != null) {
                // get context for expression
                org.dom4j.Document doc = XmlUtil.parseDocument(Util.parse(xml));
                org.dom4j.Node currentNode = doc.selectSingleNode(currentXpath);
                org.dom4j.Node targetNode = null;
                if (org.talend.mdm.webapp.base.shared.util.CommonUtil.isRelativePath(rightValueOrPath)) {
                    targetNode = currentNode.selectSingleNode(rightValueOrPath);
                } else {
                    String xpath = rightValueOrPath.startsWith("/") ? rightValueOrPath : "/" + rightValueOrPath; //$NON-NLS-1$//$NON-NLS-2$
                    targetNode = currentNode.selectSingleNode(xpath);
                }
                if (targetNode != null && targetNode.getText() != null) {
                    result = targetNode.getText();
                }
            }
        }

        return result;
    }

    private static class FieldSearchMetadataVisitor implements MetadataVisitor<String> {

        private static int level;

        private static String[] fieldPath;

        private static String value;

        @Override
        public String visit(MetadataRepository repository) {
            return null;
        }

        @Override
        public String visit(SimpleTypeMetadata simpleType) {
            return null;
        }

        @Override
        public String visit(ComplexTypeMetadata complexType) {
            if (fieldPath.length >= level) {
                if (complexType.getName().equals(fieldPath[level])) {
                    return WhereCondition.FULLTEXTSEARCH;
                } else {
                    Collection<FieldMetadata> fields = complexType.getFields();
                    for (FieldMetadata field : fields) {
                        if (field.getName().equals(fieldPath[level])) {
                            level++;
                            return field.accept(this);
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public String visit(ContainedComplexTypeMetadata containedType) {
            if (fieldPath.length >= level) {
                Collection<FieldMetadata> fields = containedType.getFields();
                for (FieldMetadata field : fields) {
                    if (field.getName().equals(fieldPath[level])) {
                        level++;
                        return field.accept(this);
                    }
                }
            }
            return null;
        }

        @Override
        public String visit(SimpleTypeFieldMetadata simpleField) {
            String typeName = MetadataUtils.getSuperConcreteType(simpleField.getType()).getName();
            if (!(Types.STRING.equals(typeName) || Types.TOKEN.equals(typeName) || Types.DURATION.equals(typeName))) {
                value = value.replace("'", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (StorageMetadataUtils.isValueAssignable(value, typeName)) {
                if (Types.STRING.equals(typeName) || Types.TOKEN.equals(typeName) || Types.DURATION.equals(typeName)) {
                    return WSWhereOperator.CONTAINS.toString();
                } else if (Types.INTEGER.equals(typeName) || Types.POSITIVE_INTEGER.equals(typeName)
                        || Types.NEGATIVE_INTEGER.equals(typeName) || Types.NON_NEGATIVE_INTEGER.equals(typeName)
                        || Types.NON_POSITIVE_INTEGER.equals(typeName) || Types.INT.equals(typeName)
                        || Types.UNSIGNED_INT.equals(typeName)) {
                    return WSWhereOperator.EQUALS.toString();
                } else if (Types.DATE.equals(typeName) || Types.DATETIME.equals(typeName) || Types.TIME.equals(typeName)) {
                    return WSWhereOperator.EQUALS.toString();
                } else if (Types.BOOLEAN.equals(typeName)) {
                    return WSWhereOperator.EQUALS.toString();
                } else if (Types.DECIMAL.equals(typeName)) {
                    return WSWhereOperator.EQUALS.toString();
                } else if (Types.FLOAT.equals(typeName) || Types.LONG.equals(typeName) || Types.UNSIGNED_LONG.equals(typeName)
                        || Types.SHORT.equals(typeName) || Types.UNSIGNED_SHORT.equals(typeName) || Types.DOUBLE.equals(typeName)
                        || Types.UNSIGNED_DOUBLE.equals(typeName) || Types.DOUBLE.equals(typeName)
                        || Types.UNSIGNED_DOUBLE.equals(typeName) || Types.BYTE.equals(typeName)
                        || Types.UNSIGNED_BYTE.equals(typeName)) {
                    return WSWhereOperator.EQUALS.toString();
                } else {
                    LOG.error("No support for type '" + typeName + "'");
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        public String visit(EnumerationFieldMetadata enumField) {
            return null;
        }

        @Override
        public String visit(ReferenceFieldMetadata referenceField) {
            return null;
        }

        @Override
        public String visit(ContainedTypeFieldMetadata containedField) {
            if (fieldPath.length >= level) {
                return containedField.getType().accept(this);
            }
            return null;
        }

        @Override
        public String visit(FieldMetadata fieldMetadata) {
            return null;
        }

        public void setFieldPath(String[] fieldPath) {
            this.fieldPath = fieldPath;
            this.level = 1;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}