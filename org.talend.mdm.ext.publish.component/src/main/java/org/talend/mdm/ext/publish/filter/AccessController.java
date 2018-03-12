/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.filter;

import java.util.List;

import org.apache.log4j.Logger;

public abstract class AccessController {

    private static final Logger logger = Logger.getLogger(AccessController.class);

    protected Logger getLocalLogger() {
        return logger;
    }

    public abstract boolean validate(List<String> resourceInstances, AccessControlPropertiesReader propertiesReader);

}
