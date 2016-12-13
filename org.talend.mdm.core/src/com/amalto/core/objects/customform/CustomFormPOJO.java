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

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.metadata.LongString;

public class CustomFormPOJO extends ObjectPOJO {

    private String dataModel;

    private String entity;

    private String name;

    private String xml;

    private String role;

    public CustomFormPOJO() {
        super();
    }

    public CustomFormPOJO(String dataModel, String entity, String xml) {
        this.dataModel = dataModel;
        this.entity = entity;
        this.xml = xml.replaceFirst("<\\?xml.*\\?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDatamodel() {
        return dataModel;
    }

    public void setDatamodel(String datamodel) {
        this.dataModel = datamodel;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    @LongString
    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml.replaceFirst("<\\?xml.*\\?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ObjectPOJOPK getPK() {
        return new CustomFormPOJOPK(dataModel, entity, name);
    }
}
