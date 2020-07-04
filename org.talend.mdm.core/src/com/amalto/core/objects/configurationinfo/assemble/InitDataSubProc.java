/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.configurationinfo.assemble;

import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.apache.logging.log4j.LogManager;

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
				LogManager.getLogger(this.getClass()).error("Init db error! ");
				e.printStackTrace();
			}
		}

	}



}
