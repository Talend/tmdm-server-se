/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataType;
import org.talend.mdm.webapp.base.client.model.SubTypeBean;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class TypeModel implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private DataType type;

    private String xpath;

    private String typePath;

    private TypePath typePathObject;

    private List<String> aliasTypePaths;

    private Map<String, String> labelMap;

    private Map<String, String> descriptionMap;

    private boolean readOnly = false;

    private boolean visible = true;

    private boolean hide = false ;

    private int minOccurs;

    private int maxOccurs;

    private boolean nillable = true;

    private boolean retrieveFKinfos = false;

    private String foreignkey;

    private boolean notSeparateFk;

    private List<String> foreignKeyInfo;

    private String foreignKeyInfoFormat;

    private String foreignKeyFilter;

    private String filterValue;

    private List<String> primaryKeyInfo;

    private Map<String, String> facetErrorMsgs;

    private Map<String, String> displayFomats;

    private boolean denyCreatable = false;

    private boolean denyLogicalDeletable = false;

    private boolean denyPhysicalDeleteable = false;

    private boolean polymorphism;

    private ArrayList<SubTypeBean> reusableTypes;

    private boolean isAbstract = false;

    private String defaultValueExpression;

    private String visibleExpression;

    private String defaultValue;

    private boolean hasVisibleRule = false;

    private boolean autoExpand = false;

    private TypeModel parentTypeModel;

    public TypeModel() {
        super();
    }

    public TypeModel(String name, DataType type) {
        super();
        this.name = name;
        this.type = type;
        this.labelMap = new HashMap<String, String>();
        this.descriptionMap = new HashMap<String, String>();
        // FIXME do we need init here?
        this.facetErrorMsgs = new HashMap<String, String>();
        this.displayFomats = new HashMap<String, String>();

    }

    public DataType getType() {
        return type;
    }

    /*
     * public void setType(DataType typeName) { this.typeName = typeName; }
     */

    public String getName() {
        return name;
    }

    /*
     * public void setName(String name) { this.name = name; }
     */

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getTypePath() {
        return typePath;
    }

    public void setTypePath(String typePath) {
        this.typePath = typePath;
    }

    public TypePath getTypePathObject() {
        return typePathObject;
    }

    public void setTypePathObject(TypePath typePathObject) {
        this.typePathObject = typePathObject;
    }

    public String getLabel(String language) {
        String label = getLabelMap().get(language);
        if (label == null)
            return getName();
        return label;
    }

    public Map<String, String> getLabelMap() {
        return labelMap;
    }

    public void setLabelMap(Map<String, String> labelMap) {
        this.labelMap = labelMap;
    }

    /**
     * Sets the labelMap.
     * 
     * @param labelMap the labelMap to set
     */
    public void addLabel(String language, String label) {
        labelMap = getLabelMap();
        labelMap.put(language, label);
    }

    public Map<String, String> getDescriptionMap() {
        return descriptionMap;
    }

    public void addDescription(String language, String label) {
        descriptionMap = getDescriptionMap();
        descriptionMap.put(language, label);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public int getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(int minOccurs) {
        this.minOccurs = minOccurs;
    }

    public int getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getForeignkey() {
        return foreignkey;
    }

    public void setForeignkey(String foreignkey) {
        this.foreignkey = foreignkey;
    }

    public boolean isNotSeparateFk() {
        return notSeparateFk;
    }

    public void setNotSeparateFk(boolean notSeparateFk) {
        this.notSeparateFk = notSeparateFk;
    }

    public boolean isRetrieveFKinfos() {
        return retrieveFKinfos;
    }

    public void setRetrieveFKinfos(boolean retrieveFKinfos) {
        this.retrieveFKinfos = retrieveFKinfos;
    }

    public List<String> getForeignKeyInfo() {
        return foreignKeyInfo;
    }

    public void setForeignKeyInfo(List<String> foreignKeyInfo) {
        Iterator<String> iterator = foreignKeyInfo.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next == null || next.isEmpty()) {
                iterator.remove();
            }
        }
        this.foreignKeyInfo = foreignKeyInfo;
    }

    public String getForeignKeyInfoFormat() {
        return this.foreignKeyInfoFormat;
    }

    public void setForeignKeyInfoFormat(String foreignKeyInfoFormat) {
        this.foreignKeyInfoFormat = foreignKeyInfoFormat;
    }

    public String getForeignKeyFilter() {
        return foreignKeyFilter;
    }

    public void setForeignKeyFilter(String foreignKeyFilter) {
        this.foreignKeyFilter = foreignKeyFilter;
    }

    public String getFilterValue() {
        return this.filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public List<String> getPrimaryKeyInfo() {
        return primaryKeyInfo;
    }

    public void setPrimaryKeyInfo(List<String> primaryKeyInfo) {
        this.primaryKeyInfo = primaryKeyInfo;
    }

    public Map<String, String> getFacetErrorMsgs() {
        return facetErrorMsgs;
    }

    public void addFacetErrorMsg(String language, String label) {
        facetErrorMsgs = getFacetErrorMsgs();
        facetErrorMsgs.put(language, label);
    }

    public Map<String, String> getDisplayFomats() {
        return displayFomats;
    }

    public void addDisplayFomat(String language, String label) {
        displayFomats = getDisplayFomats();
        displayFomats.put(language, label);
    }

    public boolean isDenyCreatable() {
        return denyCreatable;
    }

    public void setDenyCreatable(boolean denyCreatable) {
        this.denyCreatable = denyCreatable;
    }

    public boolean isDenyLogicalDeletable() {
        return denyLogicalDeletable;
    }

    public void setDenyLogicalDeletable(boolean denyLogicalDeletable) {
        this.denyLogicalDeletable = denyLogicalDeletable;
    }

    public boolean isDenyPhysicalDeleteable() {
        return denyPhysicalDeleteable;
    }

    public void setDenyPhysicalDeleteable(boolean denyPhysicalDeleteable) {
        this.denyPhysicalDeleteable = denyPhysicalDeleteable;
    }

    public boolean isHasVisibleRule() {
        return hasVisibleRule;
    }

    public void setHasVisibleRule(boolean hasVisibleRule) {
        this.hasVisibleRule = hasVisibleRule;
    }

    public boolean isPolymorphism() {
        return polymorphism;
    }

    public void setPolymorphism(boolean polymorphism) {
        this.polymorphism = polymorphism;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public ArrayList<SubTypeBean> getReusableTypes() {
        return reusableTypes;
    }

    public void setReusableTypes(ArrayList<SubTypeBean> subTypes) {
        this.reusableTypes = subTypes;
    }

    public String getDefaultValueExpression() {
        return defaultValueExpression;
    }

    public void setDefaultValueExpression(String defaultValueExpression) {
        this.defaultValueExpression = defaultValueExpression;
    }

    public String getVisibleExpression() {
        return visibleExpression;
    }

    public void setVisibleExpression(String visibleExpression) {
        this.visibleExpression = visibleExpression;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isAutoExpand() {
        return autoExpand;
    }

    public void setAutoExpand(boolean autoExpand) {
        this.autoExpand = autoExpand;
    }

    public TypeModel getParentTypeModel() {
        return parentTypeModel;
    }

    public void setParentTypeModel(TypeModel parentTypeModel) {
        this.parentTypeModel = parentTypeModel;
    }

    public int[] getRange() {
        int min = 0;
        int max = 0;
        if (getMinOccurs() <= 0) {
            min = 1;
        } else {
            min = getMinOccurs();
        }

        if (getMaxOccurs() == -1) {
            max = Integer.MAX_VALUE;
        } else if (getMaxOccurs() >= min) {
            max = getMaxOccurs();
        }
        return new int[] { min, max };
    }

    public boolean isMultiOccurrence() {
        int[] range = getRange();
        int min = range[0];
        int max = range[1];
        boolean multiple = false;
        if (max > min && min >= 1) {
            multiple = true;
        }
        return multiple;
    }

    public abstract boolean isSimpleType();

    public abstract boolean hasEnumeration();

    protected String nullSafe(Object input, String emptyValue) {
        return input == null ? emptyValue : input.toString();
    }

    protected String nullSafe(Object input) {
        return nullSafe(input, "null"); //$NON-NLS-1$
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(" ") //$NON-NLS-1$
                .append(this.getClass().getName())
                .append(" [name=").append(nullSafe(name)).append(", type=").append(nullSafe(type)) //$NON-NLS-1$ //$NON-NLS-2$
                .append(", xpath=").append(nullSafe(xpath)).append(", typePath=").append(nullSafe(typePath)) //$NON-NLS-1$ //$NON-NLS-2$
                .append(", labelMap=").append(nullSafe(labelMap)).append(", descriptionMap=").append(nullSafe(descriptionMap)) //$NON-NLS-1$ //$NON-NLS-2$
                .append(", minOccurs=").append(nullSafe(minOccurs)).append(", maxOccurs=").append(nullSafe(maxOccurs)) //$NON-NLS-1$ //$NON-NLS-2$
                .append(", nillable=").append(nullSafe(nillable)).append(", readOnly=").append(nullSafe(readOnly)) //$NON-NLS-1$//$NON-NLS-2$
                .append(", visible=").append(nullSafe(visible)).append(", denyCreatable=").append(nullSafe(denyCreatable)) //$NON-NLS-1$//$NON-NLS-2$
                .append(", denyLogicalDeletable=").append(nullSafe(denyLogicalDeletable)).append(", denyPhysicalDeleteable=") //$NON-NLS-1$//$NON-NLS-2$
                .append(nullSafe(denyPhysicalDeleteable))
                .append(", facetErrorMsgs=").append(nullSafe(facetErrorMsgs)) //$NON-NLS-1$
                .append(", displayFomats=").append(nullSafe(displayFomats)).append(", autoExpand=").append(nullSafe(autoExpand)) //$NON-NLS-1$//$NON-NLS-2$
                .append(", primaryKeyInfo=").append(nullSafe(primaryKeyInfo)).append(", retrieveFKinfos=") //$NON-NLS-1$//$NON-NLS-2$
                .append(nullSafe(retrieveFKinfos))
                .append(", foreignkey=").append(nullSafe(foreignkey)) //$NON-NLS-1$
                .append(", notSeparateFk=").append(nullSafe(notSeparateFk)).append(", foreignKeyInfo=") //$NON-NLS-1$//$NON-NLS-2$
                .append(nullSafe(foreignKeyInfo))
                .append(", foreignKeyInfoFormat=").append(nullSafe(foreignKeyInfoFormat)) //$NON-NLS-1$
                .append(", foreignKeyFilter=").append(nullSafe(foreignKeyFilter)).append(", isAbstract=") //$NON-NLS-1$//$NON-NLS-2$
                .append(nullSafe(isAbstract)).append(", polymorphism=").append(nullSafe(polymorphism)).append(", defaultValue=") //$NON-NLS-1$//$NON-NLS-2$
                .append(nullSafe(defaultValue))
                .append(", defaultValueExpression=").append(nullSafe(defaultValueExpression)) //$NON-NLS-1$
                .append(", hasVisibleRule=").append(nullSafe(hasVisibleRule)).append(", visibleExpression=") //$NON-NLS-1$//$NON-NLS-2$
                .append(nullSafe(visibleExpression))
                .append(", parentTypeModel=").append(parentTypeModel == null ? "null" : parentTypeModel.getTypePath()).append("]"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

        return sb.toString();

    }

}
