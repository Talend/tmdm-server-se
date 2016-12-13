/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.io.IOException;

import org.w3c.dom.Element;


public class Role {

	String name;

	String description;

	String role_to_assign;

	String[] applications;



	public Role() {
	}

	/**
	 * @return Returns the applications.
	 */
	public String[] getApplications() {
		return applications;
	}

	/**
	 * @param applications The applications to set.
	 */
	public void setApplications(String[] applications) {
		this.applications = applications;
	}

	/**
	 * @return Returns the description.
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
	 * @return Returns the name.
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
	 * @return Returns the role_to_assign.
	 */
	public String getRole_to_assign() {
		return role_to_assign;
	}

	/**
	 * @param role_to_assign The role_to_assign to set.
	 */
	public void setRole_to_assign(String role_to_assign) {
		this.role_to_assign = role_to_assign;
	}



	/**
	 * Role format
	 
	 <role>
	 	<name/>
	 	<description/>
	 	<role_to_assign/>
	 	<applications>
 			<name/>
	 	</applications>
	 </role>
	 	
	 */
	
	
	public String serialize() {
		String role = 
			"<Role>" +
			"    <name>"+name+"</name>"+
			"    <description>"+description+"</description>"+
			"    <role_to_assign>"+(role_to_assign == null ? "" : role_to_assign)+"</role_to_assign>";
		if ((applications!=null) && (applications.length!=0)) {
			role+=
				"    <applications>";
			for (int i = 0; i < applications.length; i++) {
				role+="<name>"+applications[i]+"</name>";
			}
			role+=
				"    </applications>";
		}
		role+=
		"</Role>";			

		
		return role;
		
		
	}
	
	public static Role parse(String xml) throws Exception{
		Role role = new Role();
		parse(xml, role);
		return role;
	}
	
	
	public static void parse(String xml, Role role) throws Exception{
				
		try {
			Element result = Util.parse(xml).getDocumentElement();
			role.setName(Util.getFirstTextNode(result, "//name"));
			role.setDescription(Util.getFirstTextNode(result, "//description"));
			role.setRole_to_assign(Util.getFirstTextNode(result, "//role_to_assign"));
			role.setApplications(Util.getTextNodes(result, "//applications/name"));
						
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to parse role: " +": "+e.getLocalizedMessage());
		}
		
	}
	
	
	 private void writeObject(java.io.ObjectOutputStream out)   throws IOException {
		 out.write(serialize().getBytes("UTF-8"));
	 }
	 
	 private void readObject(java.io.ObjectInputStream in)  throws IOException, ClassNotFoundException {
		 try {
			 String xml = in.readUTF();
			 if ((xml == null) || ("".equals(xml))) return;
			 parse(xml, this);
		 } catch (Exception x) {throw new IOException(x.getLocalizedMessage());}
	 }
	
	
	

	
	/****************************************************************
	 * Original role stuff from Role - not used here
	 *
	 */
	
	
//	/* (non-Javadoc)
//	 * @see org.jboss.portal.core.model.Role#getUsers()
//	 */
//	public Set getUsers() {
//		return null;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.jboss.portal.core.model.Role#setDisplayName(java.lang.String)
//	 */
//	public void setDisplayName(String name) {
//		this.description = name;
//	}
//
//	
//	/* (non-Javadoc)
//	 * @see org.jboss.portal.core.model.Role#getDisplayName()
//	 */
//	public String getDisplayName() {
//		return this.description;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.jboss.portal.core.model.Role#getID()
//	 */
//	public Integer getID() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//

	

}
