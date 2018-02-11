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

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.SQLCriterion;
import org.hibernate.type.Type;

class FieldTypeCriterion extends SQLCriterion {

    private final Criteria criteria;

    private final String typeName;

    FieldTypeCriterion(Criteria typeSelectionCriteria, String typeName) {
        super(StringUtils.EMPTY, new Object[0], new Type[0]);
        this.criteria = typeSelectionCriteria;
        this.typeName = typeName;
    }

    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return "(" //$NON-NLS-1$
                + criteriaQuery.getSQLAlias(this.criteria)
                + "." //$NON-NLS-1$
                + MappingGenerator.DISCRIMINATOR_NAME
                + " = '" //$NON-NLS-1$
                + typeName
                + "')"; //$NON-NLS-1$
    }
}
