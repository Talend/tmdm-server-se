// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.RowCountProjection;

/**
 * Overwrite the {@link RowCountProjection#toSqlString(Criteria, int, CriteriaQuery)} method to generate special SQL.
 * The class only be used when hibernate create criteria object against ManyFieldProjection.
 * created by hwzhu on May 7, 2019
 *
 */
public class ManyFieldRowCountProjection extends RowCountProjection {

    private static final long serialVersionUID = -1038777504090589686L;

    private Projection projection;

    public ManyFieldRowCountProjection(Projection projection) {
        super();
        this.projection = projection;
    }

    @Override
    public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
        return projection.toSqlString(criteria, position, criteriaQuery);
    }
}
