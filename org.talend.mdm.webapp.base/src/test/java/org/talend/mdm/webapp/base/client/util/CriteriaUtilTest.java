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

import junit.framework.TestCase;

import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;


public class CriteriaUtilTest extends TestCase {
    
    List<SimpleCriterion> criterias;
    
    MultipleCriteria criteria;
    
    SimpleCriterion simpleCriterion1 = new SimpleCriterion("a","equal","a");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    
    SimpleCriterion simpleCriterion2 = new SimpleCriterion("b","equal","b");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    
    SimpleCriterion simpleCriterion3 = new SimpleCriterion("c","equal","c");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    SimpleCriterion simpleCriterion4 = new SimpleCriterion("d","equal","d");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    
    SimpleCriterion simpleCriterion5 = new SimpleCriterion("e","equal","e");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    
    SimpleCriterion simpleCriterion6 = new SimpleCriterion("f","equal","f");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    
    SimpleCriterion simpleCriterion7 = new SimpleCriterion("g","equal","g");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    
    SimpleCriterion simpleCriterion8 = new SimpleCriterion("h","equal","h");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    
    public void initCriteria(){
        
        criteria = new MultipleCriteria();
        criteria.add(simpleCriterion6);
        MultipleCriteria multipleCriteria1 = new MultipleCriteria(); 
        multipleCriteria1.add(simpleCriterion1);
        criteria.add(multipleCriteria1);
        criteria.add(simpleCriterion5);
        MultipleCriteria multipleCriteria2 = new MultipleCriteria();
        multipleCriteria2.add(simpleCriterion2);
        criteria.add(multipleCriteria2);
        MultipleCriteria multipleCriteria3 = new MultipleCriteria();
        multipleCriteria3.add(simpleCriterion3);
        multipleCriteria3.add(simpleCriterion4);
        criteria.add(multipleCriteria3);
        criteria.add(simpleCriterion7);
        criteria.add(simpleCriterion8);
    }
    
    public void initCriteriaList(){
        
        criterias = new LinkedList<SimpleCriterion>();
        criterias.add(simpleCriterion1);
        criterias.add(simpleCriterion2);
        criterias.add(simpleCriterion3);
        criterias.add(simpleCriterion4);
        criterias.add(simpleCriterion5);
        criterias.add(simpleCriterion6);
        criterias.add(simpleCriterion7);
        criterias.add(simpleCriterion8);
    }    
    
    public void testGetSimpleCriterions() {
        initCriteria();
        initCriteriaList();
        List <SimpleCriterion> simpleCriterions = CriteriaUtil.getSimpleCriterions(criteria);      
        for (SimpleCriterion simpleCriterion : criterias){
            assertTrue(simpleCriterions.contains(simpleCriterion));
        }        
    }
}
