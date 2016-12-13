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

public class JoboxException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1389881251234451454L;

    JoboxException() {
    }

    public JoboxException(String message) {
        super(message);
    }

    public JoboxException(Throwable throwable) {
        super(throwable);
    }

    public JoboxException(String localizedMessage, Throwable throwable) {
        super(localizedMessage, throwable);
    }
}
