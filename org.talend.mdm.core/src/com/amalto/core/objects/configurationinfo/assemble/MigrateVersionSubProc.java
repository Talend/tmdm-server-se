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

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.server.api.ConfigurationInfo;
import com.amalto.core.util.Util;

public class MigrateVersionSubProc extends AssembleSubProc {

    protected static final Logger LOGGER = Logger.getLogger(MigrateVersionSubProc.class);

    @Override
    public void run() throws Exception {
        // perform upgrades
        boolean autoupgrade = "true".equals(MDMConfiguration.getConfiguration().getProperty("system.data.auto.upgrade", "true"));
        if (autoupgrade) {
            try {
                ConfigurationInfo ctrl = Util.getConfigurationInfoCtrlLocal();
                ctrl.autoUpgrade();
            } catch (Exception e) {
                LOGGER.error("Migrate error! ", e);
            }
        }
    }
}
