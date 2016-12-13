/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.talend.mdm.webapp.base.client.model.Criteria;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyDrawer;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.shared.ReusableType;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

public class CommonUtil {

    private static final String BEFORE_DOMAIN = "\\b((https?|ftp)://)"; //$NON-NLS-1$

    private static final String PATH = "(/[-a-z0-9A-Z_:@&?=+,.!/~*'%#$]*)*"; //$NON-NLS-1$

    public static String getHost(String url) {
        String host = url.replaceAll(BEFORE_DOMAIN, "").replaceAll(PATH, ""); //$NON-NLS-1$ //$NON-NLS-2$
        return host;
    }

    public static String getXpathSuffix(String xpath) {
        return xpath.substring(xpath.lastIndexOf('/') + 1);
    }

    public static String getElementFromXpath(String xpath) {
        String[] arr = xpath.split("/");//$NON-NLS-1$
        for (int i = arr.length - 1; i > -1; i--) {
            if (arr[i] != "") {
                return arr[i];
            }
        }
        return xpath;
    }

    public static String getRealTypePath(ItemNodeModel nodeModel) {
        String realXPath = ""; //$NON-NLS-1$
        ItemNodeModel current = nodeModel;
        boolean isFirst = true;
        while (current != null) {
            String name;
            if (current.getRealType() != null) {
                name = current.getName() + ":" + current.getRealType(); //$NON-NLS-1$
            } else {
                name = current.getName();
            }

            current = (ItemNodeModel) current.getParent();
            if (isFirst) {
                realXPath = name;
                isFirst = false;
                continue;
            }
            realXPath = name + "/" + realXPath; //$NON-NLS-1$

        }
        return realXPath;
    }

    public static String getRealXPath(ItemNodeModel nodeModel) {
        String realXPath = ""; //$NON-NLS-1$
        ItemNodeModel current = nodeModel;
        boolean isFirst = true;
        while (current != null) {
            String name = getNodeNameWithIndex(current);
            current = (ItemNodeModel) current.getParent();
            if (isFirst) {
                realXPath = name;
                isFirst = false;
                continue;
            }
            realXPath = name + "/" + realXPath; //$NON-NLS-1$

        }
        return realXPath;
    }

    private static String getNodeNameWithIndex(ItemNodeModel nodeModel) {
        ItemNodeModel parent = (ItemNodeModel) nodeModel.getParent();
        if (parent != null) {
            List<ModelData> children = parent.getChildren();
            int index = 0;
            for (ModelData child : children) {
                ItemNodeModel childModel = (ItemNodeModel) child;
                if (childModel.getName().equals(nodeModel.getName())) {
                    index++;
                    if (childModel == nodeModel) {
                        return nodeModel.getName() + "[" + index + "]"; //$NON-NLS-1$//$NON-NLS-2$
                    }
                }
            }
        }
        return nodeModel.getName();
    }

    public static int getCountOfBrotherOfTheSameName(ItemNodeModel nodeModel) {
        int count = 0;
        String name = nodeModel.getName();
        ItemNodeModel parentModel = (ItemNodeModel) nodeModel.getParent();
        if (parentModel == null) {
            return 1;
        }
        for (int i = 0; i < parentModel.getChildCount(); i++) {
            ItemNodeModel childModel = (ItemNodeModel) parentModel.getChild(i);
            if (name.equals(childModel.getName())) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasChildrenValue(ItemNodeModel parentModel) {
        List<ModelData> childs = parentModel.getChildren();
        if (childs != null && childs.size() > 0) {
            for (int i = 0; i < childs.size(); i++) {
                ItemNodeModel child = (ItemNodeModel) childs.get(i);
                if (hasChildrenValue(child)) {
                    return true;
                } else {
                    continue;
                }
            }
        } else {
            Serializable value = parentModel.getObjectValue();
            if (value != null) {
                if (value instanceof ForeignKeyBean) {
                    ForeignKeyBean fkBean = (ForeignKeyBean) value;
                    return fkBean.getId() != null && fkBean.getId().trim().length() > 0;
                } else {
                    return !"".equals(value); //$NON-NLS-1$
                }
            }
        }
        return false;
    }

    public static String getRealXpathWithoutLastIndex(ItemNodeModel nodeModel) {
        return getRealXPath(nodeModel).replaceAll("\\[\\d+\\]$", ""); //$NON-NLS-1$//$NON-NLS-2$
    }

    public static String getRealXpathWithoutLastIndex(String realPath) {
        return realPath.replaceAll("\\[\\d+\\]$", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * getDefaultTreeModel
     * 
     * @param model
     * @param language
     * @param defaultValue it is used when client side call the method
     * @param isCreate it is used when server side call the method
     * @param isCallByServerSide it is true when server side call the method, otherwise it is false
     * @return
     */
    public static List<ItemNodeModel> getDefaultTreeModel(TypeModel model, String language, boolean defaultValue,
            boolean isCreate, boolean isCallByServerSide) {
        List<ItemNodeModel> itemNodes = new ArrayList<ItemNodeModel>();

        if (model.getMinOccurs() > 1) {
            for (int i = 0; i < model.getMinOccurs(); i++) {
                ItemNodeModel itemNode = new ItemNodeModel();

                itemNodes.add(itemNode);
                if (model.getForeignkey() != null) {
                    break;
                }
            }
        } else {
            ItemNodeModel itemNode = new ItemNodeModel();
            itemNodes.add(itemNode);
        }

        for (ItemNodeModel node : itemNodes) {
            if (model.getMinOccurs() > 0) {
                node.setMandatory(true);
            }
            if (model.isSimpleType()) {
                if (defaultValue && !isCallByServerSide) {
                    setDefaultValue(model, node);
                } else if (isCreate && isCallByServerSide) {
                    node.setChangeValue(true);
                } else if (model.getType().getTypeName().equals(DataTypeConstants.UUID.getTypeName())) {
                    setDefaultValue(model, node);
                }
            } else {
                ComplexTypeModel complexModel = (ComplexTypeModel) model;
                List<TypeModel> children;
                if (model.isAbstract()) {
                    children = ReusableType.getDefaultReusableTypeChildren(complexModel, node);
                } else {
                    children = complexModel.getSubTypes();
                }
                List<ItemNodeModel> list = new ArrayList<ItemNodeModel>();
                for (TypeModel typeModel : children) {
                    list.addAll(getDefaultTreeModel(typeModel, language, defaultValue, isCreate, isCallByServerSide));
                }
                node.setChildNodes(list);
            }
            node.setName(model.getName());
            node.setTypePath(model.getTypePath());
            if (isCallByServerSide) {
                node.setHasVisiblueRule(model.isHasVisibleRule());
            }
            node.setHasVisiblueRule(model.isHasVisibleRule());
            node.setHide(model.isHide()) ;
            node.setVisible(!model.isHide()) ;
            node.setDescription(model.getDescriptionMap().get(language));
            node.setLabel(model.getLabel(language));
        }
        return itemNodes;
    }

    private static void setDefaultValue(TypeModel model, ItemNodeModel node) {

        if (model.getDefaultValueExpression() != null && model.getDefaultValueExpression().trim().length() > 0) {
            if (!"".equals(model.getForeignkey()) && model.getForeignkey() != null) { //$NON-NLS-1$
                ForeignKeyBean foreignKeyBean = new ForeignKeyBean();
                foreignKeyBean.setId(model.getDefaultValue());
                foreignKeyBean.setForeignKeyPath(model.getForeignkey());
                node.setObjectValue(foreignKeyBean);
            } else {
                node.setObjectValue(model.getDefaultValue());
            }
            node.setChangeValue(true);
        } else {
            if (model.getType().getTypeName().equals(DataTypeConstants.BOOLEAN.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.BOOLEAN.getDefaultValue());
            } else if (model.getType().getBaseTypeName().equals(DataTypeConstants.DATE.getTypeName())) {
                String dateStr = DateUtil.getDate((Date) DataTypeConstants.DATE.getDefaultValue());
                node.setObjectValue(dateStr);
            } else if (model.getType().getBaseTypeName().equals(DataTypeConstants.DATETIME.getTypeName())) {
                String dateStr = DateUtil.getDateTime((Date) DataTypeConstants.DATETIME.getDefaultValue());
                node.setObjectValue(dateStr);
            } else if (model.getType().getTypeName().equals(DataTypeConstants.DECIMAL.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.DECIMAL.getDefaultValue());
            } else if (model.getType().getTypeName().equals(DataTypeConstants.DOUBLE.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.DOUBLE.getDefaultValue());
            } else if (model.getType().getTypeName().equals(DataTypeConstants.FLOAT.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.FLOAT.getDefaultValue());
            } else if (model.getType().getTypeName().equals(DataTypeConstants.INT.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.INT.getDefaultValue());
            } else if (model.getType().getTypeName().equals(DataTypeConstants.INTEGER.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.INTEGER.getDefaultValue());
            } else if (model.getType().getTypeName().equals(DataTypeConstants.LONG.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.LONG.getDefaultValue());
            } else if (model.getType().getTypeName().equals(DataTypeConstants.SHORT.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.SHORT.getDefaultValue());
            } else if (model.getType().getTypeName().equals(DataTypeConstants.STRING.getTypeName())) {
                if (model.getForeignkey() != null && model.getForeignkey().trim().length() > 0) {
                    ForeignKeyBean foreignKeyBean = new ForeignKeyBean();
                    foreignKeyBean.setId(""); //$NON-NLS-1$
                    foreignKeyBean.setForeignKeyPath(model.getForeignkey());
                    node.setObjectValue(foreignKeyBean);
                } else {
                    node.setObjectValue((Serializable) DataTypeConstants.STRING.getDefaultValue());
                }
            } else if (model.getType().getTypeName().equals(DataTypeConstants.UUID.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.UUID.getDefaultValue());
            } else if (model.getType().getTypeName().equals(DataTypeConstants.AUTO_INCREMENT.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.AUTO_INCREMENT.getDefaultValue());
            } else if (model.getType().getTypeName().equals(DataTypeConstants.PICTURE.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.PICTURE.getDefaultValue());
            } else if (model.getType().getTypeName().equals(DataTypeConstants.URL.getTypeName())) {
                node.setObjectValue((Serializable) DataTypeConstants.URL.getDefaultValue());
            }
        }
    }

    public static ItemNodeModel recrusiveRoot(ItemNodeModel node) {
        ItemNodeModel parent = (ItemNodeModel) node.getParent();
        ItemNodeModel root = null;

        if (parent != null && parent.getParent() != null) {
            root = recrusiveRoot(parent);
        } else {
            root = parent;
        }

        return root;
    }

    public static boolean isSimpleCriteria(String criteria) {
        if (criteria.indexOf(" AND ") == -1 && criteria.indexOf(" OR ") == -1) { //$NON-NLS-1$//$NON-NLS-2$
            return true;
        }

        return false;
    }

    public static boolean validateSearchValue(Map<String, TypeModel> metaDataTypeMap, String value) {
        if (!value.contains("/")) //$NON-NLS-1$
        {
            return true;
        } else if (isString(value)) {
            return true;
        } else if (isPath(metaDataTypeMap, value)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isString(String value) {
        if (((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\"")))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            return true;
        } else {
            return false;
        }
    }

    private static boolean isPath(Map<String, TypeModel> metaDataTypeMap, String value) {
        if (!value.endsWith("/")) //$NON-NLS-1$
        {
            value = (value.startsWith("/") ? value.substring(1) : value); //$NON-NLS-1$
            if (metaDataTypeMap.get(value) != null) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Parsed a file name to a String array, the first object is the short file name, the second one is extension name
     * Return null if argument illegal
     * 
     * @param longFilePath
     * @return
     */
    public static String[] parseFileName(String longFilePath) {

        if (longFilePath == null || longFilePath.trim().length() == 0) {
            return null;
        }

        String shortFileName = "";
        String fileExtensioName = "";
        String shortpath = "";

        String[] tokenizer = longFilePath.split("[\\\\|/]");

        if (tokenizer != null && tokenizer.length > 0) {
            shortpath = tokenizer[tokenizer.length - 1];
        }

        if (shortpath.indexOf(".") != -1) {
            int pos = shortpath.lastIndexOf(".");
            shortFileName = shortpath.substring(0, pos);
            fileExtensioName = shortpath.substring(pos + 1);
        } else {
            shortFileName = shortpath;
        }

        String[] parsedResult = { shortFileName, fileExtensioName };
        return parsedResult;
    }

    public static String typePathToXpath(String typePath) {
        String[] paths = typePath.split("/"); //$NON-NLS-1$
        StringBuffer result = new StringBuffer();
        boolean isFirst = true;
        for (String path : paths) {
            String[] part = path.split(":"); //$NON-NLS-1$
            String xpart;
            if (part.length == 1) {
                xpart = part[0];
            } else {
                xpart = part[0] + "[@xsi:type='" + part[1] + "']"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (isFirst) {
                result.append(xpart);
                isFirst = false;
            } else {
                result.append("/" + xpart); //$NON-NLS-1$
            }

        }
        return result.toString();
    }

    public static boolean isReadOnlyOnUI(TypeModel typeModel) {
        TypeModel currentType = typeModel;
        boolean isReadOnly = false;
        while (currentType != null) {
            if (currentType.isReadOnly()) {
                isReadOnly = true;
                break;
            }
            currentType = currentType.getParentTypeModel();
        }
        return isReadOnly;
    }

    public static String[] extractIDs(ItemNodeModel model, ViewBean viewBean) {

        List<String> keys = new Vector<String>();

        if (model != null) {

            Map<String, String> keyMap = new HashMap<String, String>();

            List<ModelData> modelChildren = model.getChildren();

            if (modelChildren != null) {

                for (ModelData data : modelChildren) {

                    if (data instanceof ItemNodeModel) {

                        ItemNodeModel childModel = (ItemNodeModel) data;
                        if (childModel.isKey()) {

                            Object val = childModel.getObjectValue();
                            if (val != null) {

                                String valString = val.toString();
                                if (valString != null && !valString.isEmpty()) {
                                    keyMap.put(childModel.getTypePath(), valString);
                                }
                            }
                        }
                    }
                }
            }

            if (viewBean != null) {
                EntityModel entityModel = viewBean.getBindingEntityModel();
                String[] keyArray = entityModel.getKeys();
                for (String element : keyArray) {
                    if (keyMap.get(element) != null) {
                        keys.add(keyMap.get(element));
                    }
                }
            }
        }

        String[] keyArray = keys.toArray(new String[] {});

        return keyArray;
    }

    public static String getDownloadFileHeadName(TypeModel typeModel) {
        return typeModel.getName();
    }

    public static void setCurrentCachedEntity(String key, ItemPanel itemPanel) {
        HashMap<String, ItemPanel> map = BrowseRecords.getSession().getCurrentCachedEntity();
        if (map == null) {
            map = new HashMap<String, ItemPanel>();
        }
        map.put(key, itemPanel);
        BrowseRecords.getSession().put(UserSession.CURRENT_CACHED_ENTITY, map);
    }

    public static CriteriaAndC parseMultipleSearchExpression(char[] s, int c) throws Exception {
        MultipleCriteria cr = new MultipleCriteria("AND"); //$NON-NLS-1$
        CriteriaAndC ccr = null;
        String op = null;
        while (true) {
            ccr = (s[++c + 1] == '(') ? parseMultipleSearchExpression(s, c) : parseSimpleSearchExpression(s, c);
            cr.add(ccr.cr);
            if (s[(c = ccr.c + 1)] == ')') {
                return new CriteriaAndC((op == null && !(ccr.cr instanceof SimpleCriterion)) ? ccr.cr : cr, c);
            } else {
                int ce = ++c;
                while (s[++ce] != ' ') {
                    ;
                }
                if (op == null) {
                    cr.setOperator(op = new String(s, c, ce - c));
                }
                c = ce;
            }
        }
    }

    public static CriteriaAndC parseSimpleSearchExpression(char[] s, int c) throws Exception {
        int ce = ++c;
        while (s[++ce] != ')') {
            ;
        }
        String[] ts = new String(s, c, ce - c).split(" "); //$NON-NLS-1$

        int index = 2;

        String value = "";
        if (ts.length > index) {
            value = ts[index++];
            while (index < ts.length) {
                value = value.concat(" " + ts[index++]); //$NON-NLS-1$
            }
        }
        return new CriteriaAndC(new SimpleCriterion(ts[0], ts[1], value), ce);
    }

    public static class CriteriaAndC {

        public Criteria cr;

        public int c;

        public CriteriaAndC(Criteria cr, int c) {
            this.cr = cr;
            this.c = c;
        }
    }

    public static ForeignKeyDrawer switchForeignKeyEntityType(String targetEntity, String xpathForeignKey,
            String xpathInfoForeignKey) {
        ForeignKeyDrawer fkDrawer = new ForeignKeyDrawer();

        if (xpathForeignKey != null && xpathForeignKey.length() > 0) {
            xpathForeignKey = replaceXpathRoot(targetEntity, xpathForeignKey);
        }

        if (xpathInfoForeignKey != null && xpathInfoForeignKey.length() > 0) {
            String[] fkInfoPaths = xpathInfoForeignKey.split(",");//$NON-NLS-1$
            xpathInfoForeignKey = "";//$NON-NLS-1$
            for (String fkInfoPath : fkInfoPaths) {
                String relacedFkInfoPath = replaceXpathRoot(targetEntity, fkInfoPath);
                if (relacedFkInfoPath != null && relacedFkInfoPath.length() > 0) {
                    if (xpathInfoForeignKey.length() > 0) {
                        xpathInfoForeignKey += ",";//$NON-NLS-1$
                    }
                    xpathInfoForeignKey += relacedFkInfoPath;
                }
            }
        }
        fkDrawer.setXpathForeignKey(xpathForeignKey);
        fkDrawer.setXpathInfoForeignKey(xpathInfoForeignKey);
        return fkDrawer;
    }

    private static String replaceXpathRoot(String targetEntity, String xpath) {
        if (xpath.indexOf("/") != -1) { //$NON-NLS-1$
            return targetEntity + xpath.substring(xpath.indexOf("/"));//$NON-NLS-1$
        }
        return targetEntity;
    }

    public static String convertList2Xml(List<String> list, String rootName) {
        Document doc = XMLParser.createDocument();
        Element rootElement = doc.createElement(rootName);
        doc.appendChild(rootElement);
        for (int i = 0; i < list.size(); i++) {
            Element item = doc.createElement("item"); //$NON-NLS-1$  
            item.appendChild(doc.createTextNode(list.get(i)));
            rootElement.appendChild(item);
        }
        return doc.toString();
    }

    public static String convertListToXml(List<String> itemList, String rootName) {
        Document docuemnt = XMLParser.createDocument();
        Element rootElement = docuemnt.createElement(rootName);
        for (int i = 0; i < itemList.size(); i++) {
            Element itemElement = docuemnt.createElement("Item"); //$NON-NLS-1$
            itemElement.appendChild(docuemnt.createTextNode(itemList.get(i)));
            rootElement.appendChild(itemElement);
        }
        docuemnt.appendChild(rootElement);
        return docuemnt.toString();
    }

    public static String convertListToXmlString(List<String> itemList, String rootName, String itemName) {
        Document docuemnt = XMLParser.createDocument();
        Element rootElement = docuemnt.createElement(rootName);
        for (int i = 0; i < itemList.size(); i++) {
            Element itemElement = docuemnt.createElement("Item"); //$NON-NLS-1$
            itemElement.appendChild(docuemnt.createTextNode(itemList.get(i)));
            rootElement.appendChild(itemElement);
        }
        docuemnt.appendChild(rootElement);
        return docuemnt.toString();
    }

    public static List<String> findInheritanceNodePath(EntityModel entityModel) {
        Map<String, TypeModel> dataTypes = entityModel.getMetaDataTypes();
        List<String> xpathList = new LinkedList<String>();
        Iterator<Entry<String, TypeModel>> iterator = dataTypes.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, TypeModel> entry = iterator.next();
            if (entry.getValue() instanceof ComplexTypeModel) {
                ComplexTypeModel typeModel = (ComplexTypeModel) entry.getValue();
                if (typeModel.getReusableComplexTypes() != null && typeModel.getReusableComplexTypes().size() > 1) {
                    xpathList.add(typeModel.getXpath());
                }
            }
        }
        return xpathList;
    }

    public static TreePanel<BaseTreeModel> getExplainResultPanel(BaseTreeModel root) {
        TreeStore<BaseTreeModel> store = new TreeStore<BaseTreeModel>();
        store.add(root, true);
        TreePanel<BaseTreeModel> tree = new TreePanel<BaseTreeModel>(store);
        tree.setDisplayProperty("name"); //$NON-NLS-1$
        tree.getStyle().setLeafIcon(AbstractImagePrototype.create(Icons.INSTANCE.leaf()));
        return tree;
    }

    public static void setForeignKeyDisplayInfo(ForeignKeyBean fkBean) {
        if (fkBean != null) {
            String newDisplayInfo = ""; //$NON-NLS-1$
            if (fkBean.getDisplayInfo() != null && fkBean.getDisplayInfo().length() > 0) {
                String[] displayInfoWords = fkBean.getDisplayInfo().split("-"); //$NON-NLS-1$
                for (int ii = 0; ii < displayInfoWords.length; ii++) {
                    if (displayInfoWords[ii] != null && displayInfoWords[ii].trim().length() > 0
                            && !"null".equalsIgnoreCase(displayInfoWords[ii])) { //$NON-NLS-1$
                        newDisplayInfo = newDisplayInfo + displayInfoWords[ii] + "-"; //$NON-NLS-1$
                    }
                }
            }
            if (newDisplayInfo.length() > 0) {
                fkBean.setDisplayInfo(newDisplayInfo.substring(0, newDisplayInfo.length() - 1));
            } else {
                fkBean.setDisplayInfo(fkBean.getId());
            }
        }
    }
}
