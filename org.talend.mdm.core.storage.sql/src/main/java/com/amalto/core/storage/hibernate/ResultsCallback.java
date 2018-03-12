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

package com.amalto.core.storage.hibernate;

interface ResultsCallback {
    /**
     * This method is called by when calling code before begins to iterate over results.
     */
    void onBeginOfResults();

    /**
     * This method is called by {@link com.amalto.core.storage.CloseableIterator#close()}  so implementations
     * of this interface may perform clean up.
     */
    void onEndOfResults();
}
