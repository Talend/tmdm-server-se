/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.migration.tasks;

import com.amalto.core.migration.AbstractMigrationTask;
import com.amalto.core.objects.configurationinfo.ConfigurationHelper;
import org.apache.logging.log4j.LogManager;

public class RemoveOldMigrationClusterTask extends AbstractMigrationTask{

	@Override
	protected Boolean execute() {


		try {

			ConfigurationHelper.removeCluster("MIGRATION");


		} catch (Exception e) {
			String err = "Unable to Remove Old Migration Cluster";
			LogManager.getLogger(RemoveOldMigrationClusterTask.class).error(err, e);
			return false;
		}

		return true;
	}

}
