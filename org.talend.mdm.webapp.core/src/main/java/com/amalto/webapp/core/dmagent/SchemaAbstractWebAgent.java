/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.dmagent;

import java.util.List;

import org.talend.mdm.commmon.util.datamodel.management.BusinessConcept;
import org.talend.mdm.commmon.util.datamodel.management.ReusableType;
import org.talend.mdm.commmon.util.datamodel.management.SchemaManager;

/**
 * DOC Administrator  class global comment. Detailed comment
 */
public abstract class SchemaAbstractWebAgent extends SchemaManager {
    
    public abstract ReusableType getReusableType(String typeName) throws Exception;

    public abstract BusinessConcept getBusinessConcept(String conceptName) throws Exception;

    public abstract List<ReusableType> getMySubtypes(String parentTypeName) throws Exception;

    public abstract List<ReusableType> getMySubtypes(String parentTypeName, boolean deep) throws Exception;
    
    public abstract List<BusinessConcept> getAllBusinessConcepts() throws Exception;

}
