/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.transformers;

import java.util.ArrayList;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.transformers.util.TransformerProcessStep;



/**
 * @author bgrieder
 * 
 */
public class TransformerV2POJO extends ObjectPOJO{
   

    private String name;
    private String description;
    private ArrayList<TransformerProcessStep> processSteps;

       
    /**
     * 
     */
    public TransformerV2POJO() {
        super();
    }
    
    
    
    
	public TransformerV2POJO(
			String name, 
			ArrayList<TransformerProcessStep> processSteps
	) {
		super();
		this.name = name;
		this.processSteps = processSteps;
	}


	public TransformerV2POJO(
			String name,
			String description,
			ArrayList<TransformerProcessStep> processSteps
	) {
		super();
		this.name = name;
		this.description = description;
		this.processSteps = processSteps;
	}

	
	@Override
	public ObjectPOJOPK getPK() {
		return new ObjectPOJOPK(new String[] {name});
	}



	public String getName() {
		return name;
	}



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
	

	public ArrayList<TransformerProcessStep> getProcessSteps() {
		return processSteps;
	}



	public void setProcessSteps(ArrayList<TransformerProcessStep> processSteps) {
		this.processSteps = processSteps;
	}





 

}
