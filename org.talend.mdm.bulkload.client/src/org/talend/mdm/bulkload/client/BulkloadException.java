/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.mdm.bulkload.client;

import java.io.PrintStream;
import java.io.PrintWriter;

public class BulkloadException extends RuntimeException {

    private static final String SERVER_EXCEPTION_SEPARATOR = "== SERVER EXCEPTION ==";

    private static final String LINE_SEPARATOR  = System.getProperty("line.separator"); //$NON-NLS-1$

    private final String serverException;

    public BulkloadException(String serverException) {
        super("An exception happened during bulk load on MDM server.");
        this.serverException = serverException;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ((serverException != null && serverException.trim().length() > 0) ? "\n" + serverException : ""); //$NON-NLS-1$ //$NON-NLS-2$ 
    }

    public String getServerException() {
        return serverException;
    }

    @Override
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        s.append(LINE_SEPARATOR);
        s.append(SERVER_EXCEPTION_SEPARATOR);
        s.append(LINE_SEPARATOR);
        s.append(serverException);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        s.println(SERVER_EXCEPTION_SEPARATOR);
        s.println(serverException);
    }
}
