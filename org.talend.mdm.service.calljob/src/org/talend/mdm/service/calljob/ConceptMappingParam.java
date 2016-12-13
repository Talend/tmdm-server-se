/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.service.calljob;


public class ConceptMappingParam {
	
	private String concept;
	
	//using json format
	//TODO now only support 2 level mapping
	//maybe we can support tree structure
	//or simple functions
	private String fields;
	
	public ConceptMappingParam(String concept, String fields) {
		super();
		this.concept = concept;
		this.fields = fields;
	}

	public String getConcept() {
		return concept==null?"":concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public String getFields() {
		return fields==null?"":fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}
	
	
}
