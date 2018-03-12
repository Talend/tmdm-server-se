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

import static com.amalto.core.query.user.UserQueryBuilder.*;

public class DefaultFilter implements Filter {

    public final static Filter INSTANCE = new DefaultFilter();

    private DefaultFilter() {
    }

    @Override
    public Select doFilter(Filterable task, ComplexTypeMetadata type) {
        return from(type).where(task.getDefaultFilter()).getSelect();
    }

    @Override
    public boolean exclude(ComplexTypeMetadata type) {
        return false;
    }
}
