/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.typefield;

import java.util.Map;

import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TypeFieldCreateContext implements IsSerializable {


    private static final String DEFAULT_LANGUAGE = "en"; //$NON-NLS-1$


    /**
     * DOC Administrator TypeFieldCreateContext constructor comment.
     */
    public TypeFieldCreateContext() {

    }

    public TypeFieldCreateContext(TypeModel dataType) {
        super();
        this.dataType = dataType;
    }

    public TypeFieldCreateContext(TypeModel dataType, String language) {
        super();
        this.dataType = dataType;
        this.language = language;
    }

    private TypeModel dataType;

    private String language;

    private boolean withValue;

    private boolean updateStyle;

    private Map<String, TypeFieldStyle> typeFieldStyles;

    private ItemNodeModel node;

    private boolean isMandatory;
    
    private int autoTextAreaLength;

    private boolean isBulkUpdate = false;


    public TypeModel getDataType() {
        return dataType;
    }

    public void setDataType(TypeModel dataType) {
        this.dataType = dataType;
    }

    public String getLanguage() {
        return language == null ? DEFAULT_LANGUAGE : language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isWithValue() {
        return withValue;
    }

    protected void setWithValue(boolean withValue) {
        this.withValue = withValue;
    }

    public ItemNodeModel getNode() {
        return node;
    }

    protected void setNode(ItemNodeModel node) {
        this.node = node;
    }

    
    public boolean isUpdateStyle() {
        return updateStyle;
    }

    protected void setUpdateStyle(boolean updateStyle) {
        this.updateStyle = updateStyle;
    }

    public Map<String, TypeFieldStyle> getTypeFieldStyles() {
        return typeFieldStyles;
    }

    protected void setTypeFieldStyles(Map<String, TypeFieldStyle> typeFieldStyles) {
        this.typeFieldStyles = typeFieldStyles;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    
    public int getAutoTextAreaLength() {
        if (autoTextAreaLength == 0)
            autoTextAreaLength = BrowseRecords.getSession().getAppHeader().getAutoTextAreaLength();
        return autoTextAreaLength;
    }

    
    public void setAutoTextAreaLength(int autoTextAreaLength) {
        this.autoTextAreaLength = autoTextAreaLength;
    }

    public boolean isBulkUpdate() {
        return isBulkUpdate;
    }

    public void setBulkUpdate(boolean isBulkUpdate) {
        this.isBulkUpdate = isBulkUpdate;
    }
}
