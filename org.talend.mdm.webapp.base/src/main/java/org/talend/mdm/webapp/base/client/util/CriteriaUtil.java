/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.util;

import java.util.LinkedList;
import java.util.List;

import org.talend.mdm.webapp.base.client.model.Criteria;
import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;


public class CriteriaUtil {
    
    public static List<SimpleCriterion> getSimpleCriterions(MultipleCriteria criteria){
        List<SimpleCriterion> simpleCriterions = new LinkedList<SimpleCriterion>();
        recursionCriteria(criteria.getChildren(),simpleCriterions);
        return simpleCriterions;
    }
    
    private static void recursionCriteria(List<Criteria> criterias,List<SimpleCriterion> simpleCriterions){
        for (Criteria c : criterias) {
            if (c instanceof SimpleCriterion) {
                simpleCriterions.add((SimpleCriterion)c) ;
            }else{
                recursionCriteria(((MultipleCriteria)c).getChildren(),simpleCriterions);
            }                
        }
    }
}
