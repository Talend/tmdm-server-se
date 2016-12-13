/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
/**
 * 
 */
package com.amalto.core.objects.configurationinfo.assemble;


public interface AssembleBuilder {

	/** 
	 * This method constructs and assembles a particular part of a Proc.
	 */
	public void buildPartCleanJob();

	/** 
	 * This method constructs and assembles a particular part of a Proc.
	 */
	public void buildPartInitData();
	
	/** 
	 * This method constructs and assembles a particular part of a Proc.
	 */
	public void buildPartMigrateVersion();
	
	/** 
	 * This method constructs and assembles a particular part of a Proc.
	 */
	public void buildPartStartEngine();
	
	/**
	 * This method constructs and assembles a particular part of a Proc.
	 */
	public void buildStartService();
	
	/** 
	 * This method constructs and assembles a particular part of a Proc.
	 */
	public void buildInitJobox();
	
	/** 
     * This method constructs and assembles a particular part of a Proc.
     */
    public void buildInitDataModelPools();
}
