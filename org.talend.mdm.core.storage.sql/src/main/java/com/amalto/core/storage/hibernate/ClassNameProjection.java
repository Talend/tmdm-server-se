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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.SimpleProjection;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

class ClassNameProjection extends SimpleProjection {

    private final String aliasName;

    public ClassNameProjection(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
        return "REPLACE(x_talend_class, '" + ClassCreator.PACKAGE_PREFIX + "', '') as y" + position + "_"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public String[] getAliases() {
        return new String[]{aliasName};
    }

    @Override
    public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return new Type[]{new StringType()};
    }
}
