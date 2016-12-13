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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class ItemCreator {

    /**
     * DOC HSHU Comment method "createDefaultItemBean".
     * 
     * @param concept
     * @param entityModel
     * @return
     */
    public static ItemBean createDefaultItemBean(String concept, EntityModel entityModel) {
        ItemBean itemBean = new ItemBean(concept, "", null);//$NON-NLS-1$

        Map<String, TypeModel> types = entityModel.getMetaDataTypes();
        Set<String> xpaths = types.keySet();
        for (String path : xpaths) {
            TypeModel typeModel = types.get(path);

            if (path.equals(itemBean.getConcept()) || path.equals("/" + itemBean.getConcept())) { //$NON-NLS-1$
                itemBean.setLabel(typeModel.getLabel(UrlUtil.getLanguage()));
                itemBean.setDescription(typeModel.getDescriptionMap().get(UrlUtil.getLanguage()));
            }

            if (typeModel.isSimpleType()) {

                if (typeModel.getType().getBaseTypeName().equals(DataTypeConstants.DATE.getTypeName())) {
                    itemBean.set(path, new Date());
                } else if (typeModel.isMultiOccurrence()) {
                    List<Serializable> list = new ArrayList<Serializable>();
                    int[] range = typeModel.getRange();
                    int min = range[0];
                    for (int i = 0; i < min; i++) {
                        list.add("");//$NON-NLS-1$
                    }
                    itemBean.set(path, list);
                } else {
                    itemBean.set(path, "");//$NON-NLS-1$
                }
            }
        }

        return itemBean;
    }
}
