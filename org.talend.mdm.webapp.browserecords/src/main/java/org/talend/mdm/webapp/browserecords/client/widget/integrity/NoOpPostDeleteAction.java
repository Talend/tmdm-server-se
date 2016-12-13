/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.integrity;

/**
 * A "no op" implementation to be used to end a responsibility chain.
 */
public class NoOpPostDeleteAction implements PostDeleteAction {

    /**
     * Singleton instance of the class.
     */
    public static final PostDeleteAction INSTANCE = new NoOpPostDeleteAction();

    private NoOpPostDeleteAction() {
    }

    /**
     * This implementation does nothing (and should not do anything).
     */
    public void doAction() {
    }
}
