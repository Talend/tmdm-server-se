/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.commmon.util.datamodel.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class DataModelBean {

    private List<BusinessConcept> businessConcepts = null;

    private List<ReusableType> reusableTypes = null;

    /**
     * DOC HSHU DataModelBean constructor comment.
     */
    public DataModelBean() {
        businessConcepts = new ArrayList<BusinessConcept>();
        reusableTypes = new ArrayList<ReusableType>();
    }

    public List<BusinessConcept> getBusinessConcepts() {
        return businessConcepts;
    }

    public void addBusinessConcept(BusinessConcept businessConcept) {
        this.businessConcepts.add(businessConcept);
    }

    public List<ReusableType> getReusableTypes() {
        return reusableTypes;
    }

    public void addReusableType(ReusableType dataType) {
        this.reusableTypes.add(dataType);
    }

    public Map<String, ReusableType> getReusableTypeMap() {
        Map<String, ReusableType> reusableTypeMap = new HashMap<String, ReusableType>();
        if (reusableTypes == null)
            return reusableTypeMap;

        for (ReusableType reuseableType : reusableTypes) {
            reusableTypeMap.put(reuseableType.getName(), reuseableType);
        }
        return reusableTypeMap;
    }
    
    public ReusableType getReusableType(String typeName) {
        if (reusableTypes == null || reusableTypes.size() == 0 || typeName == null)
            return null;

        for (ReusableType reuseableType : reusableTypes) {
            if (reuseableType != null && reuseableType.getName() != null && reuseableType.getName().equals(typeName))
                return reuseableType;
        }

        return null;
    }

    public Map<String, BusinessConcept> getBusinessConceptMap() {
        Map<String, BusinessConcept> businessConceptMap = new HashMap<String, BusinessConcept>();
        if (businessConcepts == null)
            return businessConceptMap;

        for (BusinessConcept businessConcept : businessConcepts) {
            businessConceptMap.put(businessConcept.getName(), businessConcept);
        }
        return businessConceptMap;
    }

    public BusinessConcept getBusinessConcept(String conceptName) {
        if (businessConcepts == null || businessConcepts.size() == 0 || conceptName == null)
            return null;
        
        for (BusinessConcept bizConcept : businessConcepts) {
            if(bizConcept!=null&&bizConcept.getName()!=null&&bizConcept.getName().equals(conceptName))
                return bizConcept;
        }

        return null;
    }

    /**
     * DOC HSHU Comment method "dump".
     */
    public void dump() {
        StringBuffer toPrint = new StringBuffer();
        toPrint.append("\n");// FIXME BR
        toPrint.append("{Reusable Types}:");
        for (int i = 0; i < reusableTypes.size(); i++) {
            toPrint.append(reusableTypes.get(i));
            if (i != reusableTypes.size() - 1)
                toPrint.append(",");
        }
        toPrint.append("\n");
        toPrint.append("{Business Concepts}: ");
        toPrint.append("\n");
        for (int i = 0; i < businessConcepts.size(); i++) {
            toPrint.append(i + ": " + businessConcepts.get(i));
            toPrint.append("\n");
        }

        System.out.println(toPrint.toString());
    }

}
