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
package org.talend.mdm.webapp.browserecords.client.model;

import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.ItemResult;

public class ItemBean extends org.talend.mdm.webapp.base.client.model.ItemBean {

    private static final long serialVersionUID = -1733441307059646670L;

    public static final String TREEMODE = "M_TREE_VIEW", PERSOMODE = "M_PERSO_VIEW", SMARTMODE = "M_SMART_VIEW"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private String description;

    private String displayPKInfo;

    private String smartViewMode = TREEMODE;
    
    private List<String> pkInfoList;

    private boolean readOnly;

    private String taskId;

    private String label;
    
    private Map<String,String> originalLookupFieldMap;
    
    public String getLabel() {
        if (label == null) {
            return getConcept();
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Deprecated
    public ItemBean() {
        this.allowNestedValues = false;
    }

    public ItemBean(String concept, String ids, String itemXml) {
        super(concept, ids, itemXml);
        this.allowNestedValues = false;
    }
    
    public ItemBean(String concept, String ids, String itemXml, String description, List<String> pkInfoList) {
        this(concept, ids, itemXml);
        this.description = description;
        this.pkInfoList = pkInfoList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayPKInfo() {
        return displayPKInfo;
    }

    public void setDisplayPKInfo(String displayPKInfo) {
        this.displayPKInfo = displayPKInfo;
    }

    public String getSmartViewMode() {
        return smartViewMode;
    }

    public void setSmartViewMode(String smartViewMode) {
        this.smartViewMode = smartViewMode;
    }
    
    public List<String> getPkInfoList() {
        return pkInfoList;
    }

    public void setPkInfoList(List<String> pkInfoList) {
        this.pkInfoList = pkInfoList;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public long getLastUpdateTime() {
        return ((Long) this.get("time")).longValue(); //$NON-NLS-1$
    }

    public void setLastUpdateTime(ItemNodeModel itemNodeModel) {
        if (itemNodeModel != null)
         {
            this.set("time", itemNodeModel.get("time")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void setLastUpdateTime(ItemResult result) {
        if (result != null && result.getInsertionTime() > 0)
         {
            this.set("time", result.getInsertionTime()); //$NON-NLS-1$
        }
    }
    
	public Map<String, String> getOriginalLookupFieldMap() {
		return originalLookupFieldMap;
	}

	public void setOriginalLookupFieldMap(Map<String, String> originalLookupFieldMap) {
		this.originalLookupFieldMap = originalLookupFieldMap;
	}

	@Override
    public void copy(org.talend.mdm.webapp.base.client.model.ItemBean itemBean) {
        super.copy(itemBean);
        if (itemBean instanceof ItemBean){
            ItemBean item = (ItemBean) itemBean;
            this.description = item.getDescription();
            this.displayPKInfo = item.getDisplayPKInfo();
            this.pkInfoList = item.getPkInfoList();
            this.smartViewMode = item.getSmartViewMode();
            this.readOnly = item.isReadOnly();
            this.taskId = item.getTaskId();
            this.label = item.getLabel();
        }
    }
}
