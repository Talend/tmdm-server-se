/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.configurationinfo.assemble;

import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.initdb.InitDBUtil;

public class InitDataSubProc extends AssembleSubProc{
	

	@Override
	public void run() throws Exception {
		
		//perform initial 
		boolean autoinit = "true".equals(MDMConfiguration.getConfiguration().getProperty(
				"system.data.auto.init", 
				"false"
			));
		if(autoinit){
			InitDBUtil.init();
	    	try {
				InitDBUtil.initDB();
			} catch (Exception e) {
				org.apache.log4j.Logger.getLogger(this.getClass()).error("Init db error! ");
				e.printStackTrace();
			}
		}
		
	}



}
