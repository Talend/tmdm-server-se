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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.shared.TypeModel;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EntityModel implements IsSerializable {

    private String conceptName;

    private String[] keys;

    private LinkedHashMap<String, TypeModel> metaDataTypes;//It must be an ordered collection

    public EntityModel() {
        this.metaDataTypes = new LinkedHashMap<String, TypeModel>();
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    /**
     * It can be called by action class
     * @param language
     * @return
     */
    public String getConceptLabel(String language) {
        TypeModel typeModel = getTypeModel(conceptName);
        if (typeModel != null) {
            return typeModel.getLabel(language);
        }
        return conceptName;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public Map<String, TypeModel> getMetaDataTypes() {
        return metaDataTypes;
    }

    public void setMetaDataTypes(LinkedHashMap<String, TypeModel> metaDataTypes) {
        this.metaDataTypes = metaDataTypes;
    }

    public boolean addMetaDataType(String typePath, TypeModel typeModel) {

        if (metaDataTypes == null)
            return false;

        metaDataTypes.put(typePath, typeModel);
        return true;

    }

    public int getIndexOfMetaDataType(String typePath) {

        if (metaDataTypes == null || typePath == null)
            return -1;

        List<String> keys = new ArrayList<String>(metaDataTypes.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (typePath.equals(key))
                return i;
        }

        return -1;

    }

    public TypeModel getTypeModel(String typePath) {

        if (metaDataTypes == null || typePath == null)
            return null;

        return metaDataTypes.get(typePath);

    }

    public Iterator<String> iterateMetaDataTypes() {
        if (metaDataTypes == null)
            return null;

        return metaDataTypes.keySet().iterator();
    }

    public boolean hasMetaDataType() {

        if (metaDataTypes == null || metaDataTypes.size() == 0)
            return false;

        return true;

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EntityModel [conceptName=").append(conceptName).append(", keys=").append(Arrays.toString(keys)).append("]"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        if (metaDataTypes != null && metaDataTypes.size() > 0) {
            sb.append("\n"); //$NON-NLS-1$
            for (String typePath : metaDataTypes.keySet()) {
                sb.append(typePath).append(":").append(metaDataTypes.get(typePath)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }

        }
        return sb.toString();
    }


}
