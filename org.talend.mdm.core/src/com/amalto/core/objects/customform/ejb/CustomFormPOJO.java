// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.objects.customform.ejb;

import com.amalto.core.ejb.ObjectPOJO;
import com.amalto.core.ejb.ObjectPOJOPK;

/**
 * DOC achen  class global comment. Detailled comment
 */
public class CustomFormPOJO extends ObjectPOJO {

    private String datamodel;

    private String entity;

    private String xml;

    public CustomFormPOJO() {
        super();
    }

    public CustomFormPOJO(String datamodel, String entity, String xml) {
        this.datamodel = datamodel;
        this.entity = entity;
        this.xml = xml;
    }

    public String getDatamodel() {
        return datamodel;
    }

    public void setDatamodel(String datamodel) {
        this.datamodel = datamodel;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }


    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.amalto.core.ejb.ObjectPOJO#getPK()
     */
    @Override
    public ObjectPOJOPK getPK() {
        return new CustomFormPOJOPK(datamodel, entity);
    }
}
