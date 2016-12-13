/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.role;

import java.util.HashMap;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.util.RoleSpecification;
import com.amalto.core.util.XtentisException;


/**
 * @author Bruno Grieder
 * A role with the specifications for each Object Type
 * 
 */
public class RolePOJO extends ObjectPOJO{
   
		
    private String name;
    private String description;
    private HashMap<String,RoleSpecification> specifications;
    
    
    /**
     * 
     */
    public RolePOJO() {
        this.specifications = new HashMap<String,RoleSpecification>();
    }    
	public RolePOJO(String name) {
		super();
		this.name = name;
	}
	

	/**
	 * @return Returns the Name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	/**
	 * @return Returns the Description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * The specifications of the role<br/>
	 * The key is the Object Type
	 */
	public HashMap<String, RoleSpecification> getRoleSpecifications() {
		return specifications;
	}
	/**
	 * Sets the specifications of the role for the Object Types
	 * @param specifications
	 */
	public void setRoleSpecifications(HashMap<String,RoleSpecification> specifications) {
		this.specifications = specifications;
	}
	
	
	@Override
	public ObjectPOJOPK getPK() {
		if (getName()==null) return null;
		return new ObjectPOJOPK(new String[] {name});
	}
	
    @Override
    public ObjectPOJOPK store() throws XtentisException {
        /*if(SecurityConfig.isSecurityPermission(getName())) {
            throw new IllegalArgumentException("Role '" + getName() + "' is reserved and cannot be created");
        }*/
    	return super.store();
    }
	
	

}
