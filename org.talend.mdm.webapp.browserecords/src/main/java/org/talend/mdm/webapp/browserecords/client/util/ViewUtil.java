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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ColumnElement;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.DynamicTreeItem;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeEx;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class ViewUtil {

    public static String getConceptFromBrowseItemView(String viewPK) {
        String concept = viewPK.replaceAll("Browse_items_", "");//$NON-NLS-1$ //$NON-NLS-2$
        concept = concept.replaceAll("#.*", "");//$NON-NLS-1$ //$NON-NLS-2$
        return concept;
    }

    /**
     * DOC HSHU Comment method "getSearchableLabel".
     */
    public static String getViewableLabel(String language, TypeModel typeModel) {

        String label = typeModel.getLabel(language);
        if (LabelUtil.isDynamicLabel(label)) {
            label = typeModel.getName();
        }
        return label;

    }

    public static TreeEx transformToCustomLayout(TreeItemEx originalRoot, ColumnTreeModel columnLayoutModel, ViewBean viewBean) {

        // In case of custom layout, which displays some elements and not others,
        // we store the DynamicTreeItem corresponding to the displayed elements in
        // this set.
        Set<TreeItemEx> customLayoutDisplayedElements = new HashSet<TreeItemEx>();

        TreeEx tree = new TreeEx();
        DynamicTreeItem treeRootNode = new DynamicTreeItem();
        tree.addItem(treeRootNode);

        List<TreeItemEx> children = getChildren(originalRoot);
        List<ColumnElement> columnLayoutModels = columnLayoutModel.getColumnElements();
        if (columnLayoutModels != null) {
            for (ColumnElement ce : columnLayoutModels) {
                ListIterator<TreeItemEx> iter = children.listIterator();
                while (iter.hasNext()) {
                    TreeItemEx child = iter.next();
                    ItemNodeModel node = (ItemNodeModel) child.getUserObject();
                    String xpath = node.getBindingPath();

                    String typePath = node.getTypePath();
                    TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes().get(typePath);

                    if (("/" + xpath).equals(ce.getxPath())) { //$NON-NLS-1$
                        iter.remove();
                        treeRootNode.addItem(child);
                        child.getElement().getStyle().setPaddingLeft(23D, Unit.PX);
                        __transformToCustomLayout(child, ce, customLayoutDisplayedElements, viewBean);
                    }
                }
                if (ce.getHtmlSnippet() != null && ce.getHtmlSnippet().trim().length() > 0) {
                    treeRootNode.addItem(new HTML(ce.getHtmlSnippet()));
                }
            }
        }
        setValidFlags((DynamicTreeItem) originalRoot, customLayoutDisplayedElements);

        if (treeRootNode.getElement().getFirstChildElement() != null) {
            treeRootNode.getElement().getFirstChildElement().setClassName("rootNode"); //$NON-NLS-1$
        }
        treeRootNode.setState(true);
        return tree;
    }

    private static void __transformToCustomLayout(TreeItemEx item, ColumnElement columnEl,
            Set<TreeItemEx> customLayoutDisplayedElements, ViewBean viewBean) {
        customLayoutDisplayedElements.add(item);
        applyStyleTreeItem(item, columnEl.getLabelStyle(), convertCSS4ValueStyle(columnEl.getValueStyle()), columnEl.getStyle());
        if (columnEl.getChildren() == null) {
            return;
        }
        for (ColumnElement ce : columnEl.getChildren()) {

            for (int i = 0; i < item.getChildCount(); i++) {
                TreeItemEx child = item.getChild(i);
                ItemNodeModel node = (ItemNodeModel) child.getUserObject();
                if (node != null) {
                    TypeModel tm = viewBean.getBindingEntityModel().getMetaDataTypes().get(node.getTypePath());
                    String xpath = node.getBindingPath();
                    if (("/" + xpath).equals(ce.getxPath())) { //$NON-NLS-1$
                        __transformToCustomLayout(child, ce, customLayoutDisplayedElements, viewBean);
                        if (tm.getMaxOccurs() == 1) {
                            break;
                        }
                    }
                }
            }
            if (ce.getHtmlSnippet() != null && ce.getHtmlSnippet().trim().length() > 0) {
                item.addItem(new HTML(ce.getHtmlSnippet()));
            }
        }
    }

    public static ItemNodeModel transformToCustomLayoutModel(ItemNodeModel rootModel, List<ColumnTreeModel> columnTreeModels) {
        ItemNodeModel customLayoutModel = rootModel;        
        if(columnTreeModels.size() > 0){
            List<ItemNodeModel> nodeList = new ArrayList<ItemNodeModel>();
            for(ColumnTreeModel ctm: columnTreeModels){
                for(ColumnElement ce : ctm.getColumnElements()){
                    for(ModelData model : rootModel.getChildren()){
                        ItemNodeModel nodeModel = (ItemNodeModel)model;
                        if(ce.getxPath().equals("/" + nodeModel.getTypePath())){ //$NON-NLS-1$
                            if(ce.getChildren() != null){
                                nodeModel =  __transformToCustomLayoutModel(nodeModel, ce);
                            }
                            if(nodeModel != null){
                                nodeList.add(nodeModel);
                            }
                        }
                    }
                }
            }            
            customLayoutModel.setChildNodes(nodeList);
        }
        return customLayoutModel;
    }
    
    public static ItemNodeModel __transformToCustomLayoutModel(ItemNodeModel nodeModel, ColumnElement customLayoutElement) {
        ItemNodeModel customLayoutModel = nodeModel.clone(true);
        if(customLayoutElement.getChildren().size() < 1){
            return nodeModel;
        }                
        List<ItemNodeModel> nodeList = new ArrayList<ItemNodeModel>();
        for(ColumnElement node : customLayoutElement.getChildren()){            
            for(ModelData model : nodeModel.getChildren()){
                ItemNodeModel tempModel = (ItemNodeModel)model;
                if(node.getxPath().equals("/" + tempModel.getTypePath())){ //$NON-NLS-1$
                    if(node.getChildren() != null){
                        tempModel = __transformToCustomLayoutModel(tempModel, node);
                    }
                    nodeList.add(tempModel);
                }
            }
        }        
        customLayoutModel.setChildNodes(nodeList);
        return customLayoutModel;
    }
    
    public static void applyStyleTreeItem(final TreeItemEx item, final String labelStyle, final String valueStyle, String style) {
        String marginLeft = item.getElement().getStyle().getMarginLeft();
        String padding = item.getElement().getStyle().getPadding();
        setStyleAttribute(item.getElement(), style);
        item.getElement().getStyle().setProperty("marginLeft", marginLeft); //$NON-NLS-1$
        item.getElement().getStyle().setProperty("padding", padding); //$NON-NLS-1$
        // It need to reapply the display style according to nodeModel's visible attribute
        ItemNodeModel itemNodeModel = (ItemNodeModel) item.getUserObject();
        if (itemNodeModel != null) {
            item.setVisible(itemNodeModel.isVisible());
        }
        DeferredCommand.addCommand(new Command() {

            @Override
            public void execute() {
                if (item.getWidget() instanceof HorizontalPanel) {
                    HorizontalPanel hp = (HorizontalPanel) item.getWidget();
                    HTML label = (HTML) hp.getWidget(0);
                    setStyleAttribute(label.getElement(), labelStyle);
                    if (hp.getWidgetCount() >= 2 && hp.getWidget(1) instanceof Field<?>) {
                        final Field<?> field = (Field<?>) hp.getWidget(1);
                        El inputEl = getInputEl(field);
                        String width = inputEl.dom.getStyle().getWidth();
                        String height = inputEl.dom.getStyle().getHeight();
                        String overflow = inputEl.dom.getStyle().getOverflow();
                        setStyleAttribute(inputEl.dom, valueStyle);
                        inputEl.dom.getStyle().setProperty("width", width); //$NON-NLS-1$                       
                        if (!height.isEmpty()) {
                            inputEl.dom.getStyle().setProperty("height", height); //$NON-NLS-1$
                        }
                        if (!overflow.isEmpty()) {
                            inputEl.dom.getStyle().setProperty("overflow", overflow); //$NON-NLS-1$ 
                        }
                    }
                }
            }
        });
    }

    public static void copyStyleToTreeItem(TreeItemEx source, TreeItemEx target) {
        setStyleAttribute(target.getElement(), getStyleAttribute(source.getElement()));
        if (source.getWidget() instanceof HorizontalPanel) {
            final HorizontalPanel sourceHp = (HorizontalPanel) source.getWidget();
            final HorizontalPanel targetHp = (HorizontalPanel) target.getWidget();
            HTML sourceLabel = (HTML) sourceHp.getWidget(0);
            HTML targetLabel = (HTML) targetHp.getWidget(0);
            setStyleAttribute(targetLabel.getElement(), getStyleAttribute(sourceLabel.getElement()));
            if (sourceHp.getWidgetCount() >= 2 && sourceHp.getWidget(1) instanceof Field<?>) {
                DeferredCommand.addCommand(new Command() {
                    @Override
                    public void execute() {
                        El sourceInputEl = getInputEl((Field<?>) sourceHp.getWidget(1));
                        El targetInputEl = getInputEl((Field<?>) targetHp.getWidget(1));
                        setStyleAttribute(targetInputEl.dom, getStyleAttribute(sourceInputEl.dom));
                    }
                });
            }
        }
    }

    public static void setStyleAttribute(Element el, String styleText) {
        if (GXT.isIE) {
            el.getStyle().setProperty("cssText", styleText); //$NON-NLS-1$
        } else {
            el.setAttribute("style", styleText); //$NON-NLS-1$
        }
    }

    public static String getStyleAttribute(Element el) {
        if (GXT.isIE) {
            return el.getStyle().getProperty("cssText"); //$NON-NLS-1$
        } else {
            return el.getAttribute("style"); //$NON-NLS-1$
        }
    }

    /**
     * Recursively set the valid flags of the ItemNodeModel's corresponding to the dynamicTreeItem and all its children
     * dynamicTreeItem's to true. Used to set the valid flag for all those items excluded from the display because of a
     * custom layout. Because they are not displayed, their valid flags are not set by their attach handlers, which is
     * where the valid flag is normally set for fields that are displayed.
     */
    private static void setValidFlags(DynamicTreeItem dynamicTreeItem, Set<TreeItemEx> customLayoutDisplayedElements) {
        ItemNodeModel nodeModel = dynamicTreeItem.getItemNodeModel();
        if (nodeModel != null) {
            nodeModel.setValid(true);
        }
        int childCount = dynamicTreeItem.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            TreeItemEx child = dynamicTreeItem.getChild(i);
            if (!customLayoutDisplayedElements.contains(child)) {
                if (child instanceof DynamicTreeItem) {
                    setValidFlags((DynamicTreeItem) child, customLayoutDisplayedElements);
                }
            }
        }
    }

    private static native ArrayList<TreeItemEx> getChildren(TreeItemEx item)/*-{
		return item.@org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx::children;
    }-*/;

    private static native El getInputEl(Field<?> field)/*-{
		return field.@com.extjs.gxt.ui.client.widget.form.Field::getInputEl()();
    }-*/;
    
    public static String convertCSS4ValueStyle(String css) {
        if (css == null) {
            return null;
        }
        if (css.contains("background-color")) {//$NON-NLS-1$
            css = css.replaceAll("background-color", "background-image:none; background-color"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return css;
    }
    
    public static ItemBaseModel getDefaultSmartViewModel(List<ItemBaseModel> list, String concept) {
        String defSmartView = "Smart_view_" + concept; //$NON-NLS-1$
        String defSmartViewWithLang = defSmartView + "_" + Locale.getLanguage(); //$NON-NLS-1$
        ItemBaseModel model = null;
        for (ItemBaseModel item : list) {
            if (item.get("key").toString().toUpperCase() //$NON-NLS-1$
                    .startsWith(defSmartView.toUpperCase())) {
                if (item.get("key").toString().equalsIgnoreCase(defSmartView)) { //$NON-NLS-1$
                    return item;
                }

                if (item.get("key").toString().equalsIgnoreCase(defSmartViewWithLang)) { //$NON-NLS-1$
                    return item;
                }

                if (model == null) {
                    model = item;
                }
            }
        }
        return model;
    }
}
