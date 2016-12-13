/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.customform;

import com.amalto.core.objects.ObjectPOJOPK;


/**
 * DOC achen  class global comment. Detailled comment
 */
public class CustomFormPOJOPK extends ObjectPOJOPK {

    String datamodel;

    String entity;

    String name;
    public CustomFormPOJOPK() {

    }
    public CustomFormPOJOPK(String[] itemIds) {
        super(itemIds);
        datamodel = itemIds[0];
        entity = itemIds[1];
        name = itemIds[2];
    }

    public CustomFormPOJOPK(ObjectPOJOPK pk) {
        this(pk.getIds());
    }

    public CustomFormPOJOPK(String datamodel, String entity) {
        super(new String[] { datamodel, entity });
        this.datamodel = datamodel;
        this.entity = entity;
    }

    public CustomFormPOJOPK(String datamodel, String entity, String name) {
        super(new String[] { datamodel, entity, name });
        this.datamodel = datamodel;
        this.entity = entity;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
