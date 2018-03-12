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

package com.amalto.core.storage;

import com.amalto.core.storage.record.DataRecord;

/**
 * A {@link Iterable} implementation to navigate in results for a {@link Storage#fetch(com.amalto.core.query.user.Expression)} call.
 * This interface also provides additional methods such as:
 * <ul>
 *     <li>{@link #getSize()}</li>
 *     <li>{@link #getCount()}</li>
 * </ul>
 */
public interface StorageResults extends Iterable<DataRecord> {

    /**
     * <p>
     * @return Returns how many {@link DataRecord} are in results. This is the "limit" value passed to the
     * paging of the user query. This means this method may return {@link Integer#MAX_VALUE} if no page size was specified
     * in query.
     * </p>
     * <p>
     * This method returns the page size so even if the query returns no result, this method may return a positive
     * value (> 0).
     * </p>
     * @see com.amalto.core.query.user.Paging
     * @see #getCount()
     */
    int getSize();

    /**
     * @return Returns how many records match the query in the storage. Please note that this method may perform a count
     * using the same {@link com.amalto.core.query.user.Select} instance: in this case all selected fields are not taken
     * into account, only conditions are.
     */
    int getCount();

    /**
     * Call this method to close underlying resources. This method should be called to free up resources if the iterator
     * is not entirely consumed.
     */
    void close();

}
