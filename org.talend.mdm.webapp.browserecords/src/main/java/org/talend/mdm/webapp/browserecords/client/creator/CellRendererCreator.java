/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.creator;

import java.util.List;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.MultiLanguageModel;
import org.talend.mdm.webapp.base.shared.FacetModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.util.FormatUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.shared.Constants;
import org.talend.mdm.webapp.browserecords.shared.FacetEnum;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class CellRendererCreator {

    public static GridCellRenderer<ModelData> createRenderer(TypeModel dataType, final String xpath) {
        if (dataType.getType().equals(DataTypeConstants.URL)) {
            GridCellRenderer<ModelData> renderer = new GridCellRenderer<ModelData>() {

                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ModelData> store, Grid<ModelData> grid) {
                    String value = model.get(property);
                    if (value != null) {
                        String[] url = value.split("@@");//$NON-NLS-1$
                        return "<a href='" + url[1] + "'>" + url[0] + "</a>";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    return "null";//$NON-NLS-1$
                }
            };
            return renderer;
        } else if (dataType.isSimpleType() && dataType.isMultiOccurrence()) {
            final boolean isMultiLanguageType = dataType.getType().equals(DataTypeConstants.MLS);
            GridCellRenderer<ModelData> renderer = new GridCellRenderer<ModelData>() {

                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ModelData> store, Grid<ModelData> grid) {
                    String rootNode = property.substring(0, property.indexOf('/'));
                    String targetNode = property.substring(property.lastIndexOf('/') + 1);
                    StringBuffer result = new StringBuffer();
                    ItemBean itemBean = (ItemBean) model;
                    Document doc = XMLParser.parse(itemBean.getItemXml());
                    NodeList nodeList = doc.getElementsByTagName(targetNode);
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        String displayValue = node.toString();
                        if (isMultiLanguageType) {
                            MultiLanguageModel multiLanguageModel = new MultiLanguageModel(displayValue);
                            displayValue = Format.htmlEncode(multiLanguageModel.getValueByLanguage(Locale.getLanguage()
                                    .toUpperCase()));
                        }

                        if (node instanceof Element) {
                            if (rootNode.equals(doc.getFirstChild().getNodeName())) {
                                appendContent(result, displayValue, ","); //$NON-NLS-1$                            
                            } else {
                                if ("result".equals(node.getParentNode().getNodeName())) { //$NON-NLS-1$
                                    appendContent(result, displayValue, ","); //$NON-NLS-1$         
                                }
                            }
                        }
                    }
                    if (isMultiLanguageType) {
                        model.set(property, result.toString());
                    }
                    return result;
                }
            };
            return renderer;
        } else if (dataType.getForeignkey() != null) {
            GridCellRenderer<ModelData> renderer = new GridCellRenderer<ModelData>() {

                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ModelData> store, Grid<ModelData> grid) {
                    ItemBean itemBean = (ItemBean) model;
                    ForeignKeyBean fkBean = itemBean.getForeignkeyDesc((String) model.get(property));
                    if (fkBean == null) {
                        return ""; //$NON-NLS-1$
                    }
                    fkBean.setShowInfo(true);
                    return fkBean.toString();
                }
            };
            return renderer;
        } else if (DataTypeConstants.FLOAT.getBaseTypeName().equals(dataType.getType().getBaseTypeName())
                || DataTypeConstants.DOUBLE.getBaseTypeName().equals(dataType.getType().getBaseTypeName())) {
            GridCellRenderer<ModelData> renderer = new GridCellRenderer<ModelData>() {

                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ModelData> store, Grid<ModelData> grid) {
                    if (((String) model.get(property)) != null && !((String) model.get(property)).equals("")) {
                        String value = ((String) model.get(property));
                        return Format.htmlEncode(FormatUtil.changeNumberToFormatedValue(value));
                    } else {
                        return Format.htmlEncode((String) model.get(property));
                    }
                }
            };
            return renderer;
        } else if (DataTypeConstants.DECIMAL.getBaseTypeName().equals(dataType.getType().getBaseTypeName())) {
            String fractionDigitsTemp = new String();
            List<FacetModel> facets = ((SimpleTypeModel) dataType).getFacets();
            if (facets != null) {
                for (FacetModel facet : facets) {
                    if (facet.getName().equals(FacetEnum.FRACTION_DIGITS.getFacetName())) {
                        fractionDigitsTemp = facet.getValue();
                        break;
                    }
                }
            }
            final String fractionDigits = fractionDigitsTemp;
            final String formats = dataType.getDisplayFomats()== null ? null : dataType.getDisplayFomats().get("format_" + Locale.getLanguage());
            GridCellRenderer<ModelData> renderer = new GridCellRenderer<ModelData>() {

                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ModelData> store, Grid<ModelData> grid) {
                    if (((String) model.get(property)) != null && !((String) model.get(property)).equals("")) {
                        String valueResult = (String)model.get(property);
                        String value = valueResult.indexOf(".") > 0 ? valueResult: valueResult.replaceAll(valueResult, valueResult +".0") ;

                        if (formats == null) {
                            Number numValue = FormatUtil.getDecimalValue((String) model.get(property), fractionDigits);
                            return Format.htmlEncode(FormatUtil.changeNumberToFormatedValue(numValue.toString()));
                        } else {
                            int digitsLength = value.trim().split("\\.")[1].length();
                            Number numValue = FormatUtil.getDecimalValue((String) model.get(property),
                                    String.valueOf(digitsLength));
                            return Format.htmlEncode(FormatUtil.changeNumberToFormatedValue(numValue.toString()));
                        }
                    } else {
                        return Format.htmlEncode((String) model.get(property));
                    }
                }
            };
            return renderer;

        } else if (DataTypeConstants.BOOLEAN.getBaseTypeName().equals(dataType.getType().getBaseTypeName())) {
            GridCellRenderer<ModelData> renderer = new GridCellRenderer<ModelData>() {

                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ModelData> store, Grid<ModelData> grid) {
                    String value = String.valueOf(model.get(property));
                    if (Constants.BOOLEAN_TRUE_DISPLAY_VALUE.equals(value) || Constants.BOOLEAN_TRUE_VALUE.equals(value)) {
                        return Constants.BOOLEAN_TRUE_DISPLAY_VALUE;
                    } else if (Constants.BOOLEAN_FALSE_DISPLAY_VALUE.equals(value) || Constants.BOOLEAN_FALSE_VALUE.equals(value)) {
                        return Constants.BOOLEAN_FALSE_DISPLAY_VALUE;
                    } else {
                        return ""; //$NON-NLS-1$
                    }
                }
            };
            return renderer;
        } else if (DataTypeConstants.STRING.equals(dataType.getType())) {
            GridCellRenderer<ModelData> renderer = new GridCellRenderer<ModelData>() {

                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ModelData> store, Grid<ModelData> grid) {
                    return Format.htmlEncode((String) model.get(property));
                }
            };
            return renderer;
        } else if (DataTypeConstants.MLS.equals(dataType.getType())) {
            GridCellRenderer<ModelData> renderer = new GridCellRenderer<ModelData>() {

                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                        ListStore<ModelData> store, Grid<ModelData> grid) {
                    String multiLanguageString = (String) model.get(property);
                    MultiLanguageModel multiLanguageModel = new MultiLanguageModel(multiLanguageString);
                    return Format.htmlEncode(multiLanguageModel.getValueByLanguage(Locale.getLanguage().toUpperCase()));
                }
            };
            return renderer;
        }
        return null;
    }

    private static StringBuffer appendContent(StringBuffer resource, String content, String separater) {
        if (!"".equals(resource.toString())) { //$NON-NLS-1$
            resource.append(separater);
        }
        resource.append(content);
        return resource;
    }
}
