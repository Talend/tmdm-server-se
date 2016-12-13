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
 * This interface wraps any action that should be performed after a delete.
 * @see DeleteStrategy
 */
public interface PostDeleteAction {
    /**
     * Performs any 'action(s)' after delete has been performed by a {@link DeleteStrategy} implementation.
     */
    void doAction();
}
