/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.talend.mdm.webapp.base.shared.EntityModel;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.IsSerializable;

public class ItemNodeModel extends BaseTreeModel implements IsSerializable {

    private static final long serialVersionUID = 1L;

    private static int ID = 0;

    private String description;

    private String label;

    private String typePath;

    private int index = 0;

    // private Serializable objectValue;

    private boolean isKey;

    private String dynamicLabel;

    private String realType;

    private boolean hide = false;

    private boolean visible = true;

    private boolean valid = false;

    private boolean blankValid = false;

    private boolean rendered = false;

    private boolean hasVisiblueRule = false;

    private boolean isCloned = false;

    private boolean mandatory;

    private String typeName;

    private boolean isMassUpdate = false;

    private boolean isEdited = false;

    private boolean isFieldMandatory = false;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isHasVisiblueRule() {
        if (hasVisiblueRule) {
            return true;
        }

        List<ModelData> child = getChildren();

        for (ModelData model : child) {
            if (((ItemNodeModel) model).isHasVisiblueRule()) {
                return true;
            }
        }

        return hasVisiblueRule;
    }

    public void setHasVisiblueRule(boolean hasVisiblueRule) {
        this.hasVisiblueRule = hasVisiblueRule;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isBlankValid() {
        return this.blankValid;
    }

    public void setBlankValid(boolean blankValid) {
        this.blankValid = blankValid;
    }

    public boolean isRendered() {
        return this.rendered;
    }

    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    private boolean isChangeValue;

    public boolean isChangeValue() {
        return isChangeValue;
    }

    public void setChangeValue(boolean isChangeValue) {
        this.isChangeValue = isChangeValue;
    }

    public String getDynamicLabel() {
        return dynamicLabel;
    }

    public void setDynamicLabel(String dynamicLabel) {
        this.dynamicLabel = dynamicLabel;
    }

    public boolean isKey() {
        return isKey;
    }

    public void setKey(boolean isKey) {
        this.isKey = isKey;
    }

    public Serializable getObjectValue() {
        return get("objectValue"); //$NON-NLS-1$
    }

    public void setObjectValue(Serializable objectValue) {
        set("objectValue", objectValue); //$NON-NLS-1$
    }

    public ItemNodeModel() {
        set("id", ID++); //$NON-NLS-1$
    }

    public ItemNodeModel(String name) {
        set("id", ID++); //$NON-NLS-1$
        set("name", name); //$NON-NLS-1$
    }

    @Override
    public String toString() {
        return _toString(""); //$NON-NLS-1$
    }

    private String _toString(String pre) {
        StringBuffer sb = new StringBuffer();
        sb.append(pre + getName() + ":" + (getObjectValue() == null ? "null" : getObjectValue().toString())); //$NON-NLS-1$ //$NON-NLS-2$
        if (getRealType() != null && getRealType().trim().length() > 0) {
            sb.append("  xsi:" + getRealType()); //$NON-NLS-1$
        }
        sb.append("\n"); //$NON-NLS-1$
        List<ModelData> children = this.getChildren();
        if (children != null) {
            for (ModelData child : children) {
                ItemNodeModel childModel = (ItemNodeModel) child;
                sb.append(childModel._toString(pre + "    ")); //$NON-NLS-1$
            }
        }
        return sb.toString();
    }

    public String toValue() {
        StringBuffer sb = new StringBuffer();
        sb.append(getObjectValue() != null ? getObjectValue().toString() : ""); //$NON-NLS-1$
        for (ModelData model : this.getChildren()) {
            ItemNodeModel node = (ItemNodeModel) model;
            sb.append("+\r\n"); //$NON-NLS-1$
            sb.append(node.toValue() + "---"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    public Integer getId() {
        return (Integer) get("id"); //$NON-NLS-1$
    }

    public String getName() {
        return get("name"); //$NON-NLS-1$
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBindingPath() {

        StringBuffer xp = new StringBuffer();
        List<String> paths = new ArrayList<String>();
        TreeModel parent = this;

        while (parent != null) {
            paths.add((String) parent.get("name")); //$NON-NLS-1$
            parent = parent.getParent();
        }

        for (int i = paths.size() - 1; i >= 0; i--) {
            if (i != paths.size() - 1) {
                xp.append("/"); //$NON-NLS-1$
            }
            xp.append(paths.get(i));
        }
        return xp.toString();
    }

    public String getTypePath() {
        return typePath;
    }

    public void setTypePath(String typePath) {
        this.typePath = typePath;
    }

    public void setName(String name) {
        set("name", name); //$NON-NLS-1$
    }

    public void setChildNodes(List<ItemNodeModel> defaultTreeModel) {
        removeAll();
        if (defaultTreeModel != null) {
            for (ModelData child : defaultTreeModel) {
                add(child);
            }
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getRealType() {
        return realType;
    }

    public void setRealType(String realType) {
        this.realType = realType;
    }

    public boolean isCloned() {
        return isCloned;
    }

    public void setCloned(boolean isCloned) {
        this.isCloned = isCloned;
    }

    public boolean isMassUpdate() {
        return isMassUpdate;
    }

    public void setMassUpdate(boolean isMassUpdate) {
        this.isMassUpdate = isMassUpdate;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }

    public boolean isFieldMandatory() {
        return isFieldMandatory;
    }

    public void setFieldMandatory(boolean isFieldMandatory) {
        this.isFieldMandatory = isFieldMandatory;
    }

    public ItemNodeModel clone(boolean withValue) {
        ItemNodeModel clonedModel = new ItemNodeModel(get("name").toString()); //$NON-NLS-1$
        clonedModel.setTypePath(getTypePath());
        List<ItemNodeModel> clonedList = new ArrayList<ItemNodeModel>();
        for (ModelData data : this.getChildren()) {
            ItemNodeModel clonedData = ((ItemNodeModel) data).clone(withValue);
            clonedList.add(clonedData);
        }
        clonedModel.setChildNodes(clonedList);
        clonedModel.setLabel(this.label);
        clonedModel.setDescription(description);
        clonedModel.setDynamicLabel(dynamicLabel);
        clonedModel.setCloned(true);
        clonedModel.setTypeName(typeName);
        clonedModel.setMandatory(mandatory);
        if (this.getRealType() != null) {
            clonedModel.setRealType(this.getRealType());
        }
        if (withValue) {
            clonedModel.setObjectValue(getObjectValue());
        }
        return clonedModel;
    }

    public void sort(EntityModel entityModel) {
        sort(this, entityModel, false);
    }

    public void sort(EntityModel entityModel, boolean firstLevelOnly) {
        sort(this, entityModel, firstLevelOnly);
    }

    private void sort(ItemNodeModel nodeModel, final EntityModel entityModel, boolean firstLevelOnly) {

        if (entityModel == null || !entityModel.hasMetaDataType()) {
            return;
        }

        List<ModelData> children = nodeModel.getChildren();
        if (children != null && children.size() > 0) {

            // sort children node
            Collections.sort(children, new Comparator<ModelData>() {

                @Override
                public int compare(ModelData o1, ModelData o2) {
                    if (o1 == null || o2 == null) {
                        return 0;
                    }

                    int index1 = entityModel.getIndexOfMetaDataType(((ItemNodeModel) o1).getTypePath());
                    int index2 = entityModel.getIndexOfMetaDataType(((ItemNodeModel) o2).getTypePath());
                    return index1 - index2;
                }
            });

            if (!firstLevelOnly) {
                for (ModelData modelData : children) {
                    ItemNodeModel childNodeModel = (ItemNodeModel) modelData;
                    sort(childNodeModel, entityModel, firstLevelOnly);
                }
            }

        }
    }

    public void clearNodeValue() {
        if (this.isLeaf()) {
            Serializable value = getObjectValue();
            if (value != null) {
                setObjectValue(null);
                setChangeValue(true);
            }
        }
        for (ModelData model : this.getChildren()) {
            ItemNodeModel node = (ItemNodeModel) model;
            node.clearNodeValue();
        }
    }

}
