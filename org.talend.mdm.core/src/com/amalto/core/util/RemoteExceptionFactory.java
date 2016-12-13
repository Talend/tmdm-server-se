/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.rmi.RemoteException;

public class RemoteExceptionFactory {

    public static RemoteException aggregateCauses(Exception nestedException, boolean ignoreClassName) {
        String errorMessage;
        if (nestedException.getCause() != null) {
            StringBuilder errorMessageBuf = new StringBuilder();
            errorMessageBuf.append(nestedException.getLocalizedMessage()).append("; "); //$NON-NLS-1$
            Throwable t = nestedException.getCause();
            errorMessageBuf.append(genCausedByMsg(t, ignoreClassName));
            while ((t = t.getCause()) != null) {
                if (t.getLocalizedMessage() != null && !t.getLocalizedMessage().isEmpty()) {
                    errorMessageBuf.append(genCausedByMsg(t, ignoreClassName));
                }
            }
            errorMessage = errorMessageBuf.toString();
        } else {
            errorMessage = nestedException.getLocalizedMessage();
        }
        return new RemoteException(errorMessage, nestedException);
    }

    // FIXME is there any other better approach?
    private static String genCausedByMsg(Throwable t, boolean ignoreClassName) {
        StringBuilder msg = new StringBuilder();
        msg.append("\n\t[Caused by]: "); //$NON-NLS-1$
        if (!ignoreClassName) {
            msg.append(t.getClass().getName()).append(": "); //$NON-NLS-1$
        }
        msg.append(t.getLocalizedMessage());
        return msg.toString();
    }

}
