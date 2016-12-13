/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.BaseRemoteService;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;

import com.amalto.webapp.core.util.SystemLocale;
import com.amalto.webapp.core.util.SystemLocaleFactory;

public class BaseRemoteServiceImpl extends AbstractService implements BaseRemoteService {

    private static final long serialVersionUID = -8948046628428276152L;

    public List<ItemBaseModel> getLanguageModels() {
        List<ItemBaseModel> list = new ArrayList<ItemBaseModel>();
        Map<String, SystemLocale> map = SystemLocaleFactory.getInstance().getSupportedLocales();
        for (String language : map.keySet()) {
            ItemBaseModel model = new ItemBaseModel();
            model.set("language", map.get(language).getLabel()); //$NON-NLS-1$
            model.set("value", map.get(language).getIso().toUpperCase()); //$NON-NLS-1$
            list.add(model);
        }
        return list;
    }
}