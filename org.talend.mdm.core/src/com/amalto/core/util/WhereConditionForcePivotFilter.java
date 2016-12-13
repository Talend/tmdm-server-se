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

import java.util.Map;

import com.amalto.xmlserver.interfaces.WhereCondition;

public class WhereConditionForcePivotFilter extends WhereConditionFilter {

    public static final String FORCE_PIVOT = "FORCE_PIVOT"; //$NON-NLS-1$

    public WhereConditionForcePivotFilter(Map context) {
        super(context);
    }

    @Override
    public void doFilter(WhereCondition wc) {
        if (super.context.get(FORCE_PIVOT) == null) {
            return;
        }
        if (wc.getLeftPath() == null || wc.getLeftPath().length() == 0) {
            return;
        }
        String forcePivot = (String) context.get(FORCE_PIVOT);
        String leftPath = wc.getLeftPath();
        if (!forcePivot.equals("/") && !forcePivot.equals("//")) { //$NON-NLS-1$ //$NON-NLS-2$
            if (forcePivot.startsWith("/")) { //$NON-NLS-1$
                forcePivot = forcePivot.substring(1);
            }
            if (forcePivot.startsWith("/")) { //$NON-NLS-1$
                forcePivot = forcePivot.substring(1);
            }
        }
        // TMDM-5365 fix: ensure "EntityField" gets transformed into "Entity/EntityField" when entity name is in field name's start.
        if (!leftPath.startsWith(forcePivot + "/")) { //$NON-NLS-1$
            //it is partial
            leftPath = forcePivot + "/" + leftPath; //$NON-NLS-1$
            wc.setLeftPath(leftPath);
        }
    }

}
