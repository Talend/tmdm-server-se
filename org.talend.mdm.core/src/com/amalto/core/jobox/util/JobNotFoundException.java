/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.util;

public class JobNotFoundException extends JoboxException {

    public JobNotFoundException(String jobName, String version) {
        super(jobName + " " + version); //$NON-NLS-1$ 
    }

    @Override
    public String getLocalizedMessage() {
        return (super.getLocalizedMessage().contains(";")) ? super.getLocalizedMessage().substring(0,
                super.getLocalizedMessage().indexOf(";")) : super.getLocalizedMessage();
    }
}
