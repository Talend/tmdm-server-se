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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyTabModel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

public class UserSession implements Serializable {

    private static final long serialVersionUID = -4121455619510069503L;

    public static final String CURRENT_VIEW = "currentView"; //$NON-NLS-1$

    public static final String CURRENT_ENTITY_MODEL = "currentEntityModel"; //$NON-NLS-1$

    public static final String APP_HEADER = "appHeader"; //$NON-NLS-1$

    public static final String CUSTOMIZE_MODEL_LIST = "customizeModelList";; //$NON-NLS-1$

    public static final String CUSTOMIZE_MODEL_VIEW_MAP = "customizeModelViewMap"; //$NON-NLS-1$

    public static final String CUSTOMIZE_CRITERION_STORE = "customizeCriterionStore"; //$NON-NLS-1$

    public static final String CUSTOMIZE_CRITERION_STORE_ADVANCE = "advanceCustomizeCriterionStore"; //$NON-NLS-1$

    private Map<String, Object> sessionMap = null;

    public static final String CURRENT_LINEAGE_ENTITY_LIST = "currentLineageEntityList"; //$NON-NLS-1$

    public static final String CURRENT_RUNNABLE_PROCESS_LIST = "currentRunnableProcessList"; //$NON-NLS-1$

    public static final String CURRENT_CACHED_ENTITY = "currentCachedEntity"; //$NON-NLS-1$

    public static final String CURRENT_CACHED_FKTABS = "currentCachedFK"; //$NON-NLS-1$

    public UserSession() {
        super();
        this.sessionMap = new HashMap<String, Object>();
    }

    public void put(String key, Object value) {
        this.sessionMap.put(key, value);
    }

    public Object get(String key) {
        return this.sessionMap.get(key);
    }

    public AppHeader getAppHeader() {
        return (AppHeader) get(APP_HEADER);
    }

    public ViewBean getCurrentView() {
        return (ViewBean) get(CURRENT_VIEW);
    }

    public EntityModel getCurrentEntityModel() {
        return (EntityModel) get(CURRENT_ENTITY_MODEL);
    }

    @SuppressWarnings("unchecked")
    public List<ItemBaseModel> getCustomizeModelList() {
        return (List<ItemBaseModel>) get(CUSTOMIZE_MODEL_LIST);
    }

    @SuppressWarnings("unchecked")
    public Map<ItemBaseModel, ViewBean> getCustomizeModelViewMap() {
        return (Map<ItemBaseModel, ViewBean>) get(CUSTOMIZE_MODEL_VIEW_MAP);
    }

    public MultipleCriteria getCustomizeCriterionStore() {
        return (MultipleCriteria) get(CUSTOMIZE_CRITERION_STORE);
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, ItemPanel> getCurrentCachedEntity() {
        return (HashMap<String, ItemPanel>) get(CURRENT_CACHED_ENTITY);
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, LinkedHashMap<String, ForeignKeyTabModel>> getCurrentCachedFKTabs() {
        return (HashMap<String, LinkedHashMap<String, ForeignKeyTabModel>>) get(CURRENT_CACHED_FKTABS);
    }
}
