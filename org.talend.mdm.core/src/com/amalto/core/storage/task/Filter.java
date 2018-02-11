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

package com.amalto.core.storage.task;

import com.amalto.core.query.user.Select;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

/**
 *
 */
public interface Filter {
    /**
     * Returns the select clause that corresponds to the <code>task</code> parameter. Filter implementations may also
     * add restrictions on status codes / entity names / ...
     * @param task A task used to get the default status filter.
     * @param type A entity type used to build the query. The returned {@link Select} must include the <code>type</code>
     *             in its type arguments.
     * @return A select clause the <code>task</code> can use to get records.
     * @see Filterable#getDefaultFilter()
     * @see com.amalto.core.query.user.Select#getTypes()
     */
    Select doFilter(Filterable task, ComplexTypeMetadata type);

    /**
     * @param type An entity type.
     * @return Returns <code>true</code> if <code>type</code> should be excluded from processing.
     */
    boolean exclude(ComplexTypeMetadata type);
}
