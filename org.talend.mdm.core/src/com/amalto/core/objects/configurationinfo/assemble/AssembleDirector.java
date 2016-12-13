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

public class AssembleDirector {

	/** Stores the Builder instance of the Director */
	private final AssembleBuilder fBuilder;

	/** 
	 * This construtor creates a Director instance.
	 * @param builder Concrete Builder implementation.
	 */
	public AssembleDirector(AssembleBuilder builder) {
		super();
		fBuilder = builder;
	}

	/** 
	 * This method constructs an object by using the Builder interface.
	 */
	public void constructAll() {
		fBuilder.buildPartCleanJob();
		fBuilder.buildPartInitData();
		fBuilder.buildPartMigrateVersion();
		fBuilder.buildPartStartEngine();
		fBuilder.buildStartService();
		fBuilder.buildInitJobox();
		fBuilder.buildInitDataModelPools();
	}
	
	public void constructCleanPart() {
		fBuilder.buildPartCleanJob();
	}
	
	public void constructInitPart() {
		fBuilder.buildPartInitData();
	}
	
	public void constructMigratePart() {
		fBuilder.buildPartMigrateVersion();
	}

}
