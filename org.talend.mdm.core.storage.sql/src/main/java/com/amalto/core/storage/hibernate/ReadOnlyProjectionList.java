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
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.type.Type;

class ReadOnlyProjectionList extends ProjectionList {

    private final ProjectionList delegate;

    private ReadOnlyProjectionList(ProjectionList projectionList) {
        delegate = projectionList;
    }

    public static ProjectionList makeReadOnly(ProjectionList projectionList) {
        return new ReadOnlyProjectionList(projectionList);
    }

    @Override
    public ProjectionList create() {
        return this;
    }

    @Override
    public ProjectionList add(Projection projection) {
        return this;
    }

    @Override
    public ProjectionList add(Projection projection, String alias) {
        return this;
    }

    @Override
    public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return delegate.getTypes(criteria, criteriaQuery);
    }

    @Override
    public String toSqlString(Criteria criteria, int loc, CriteriaQuery criteriaQuery) throws HibernateException {
        return delegate.toSqlString(criteria, loc, criteriaQuery);
    }

    @Override
    public String toGroupSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return delegate.toGroupSqlString(criteria, criteriaQuery);
    }

    @Override
    public String[] getColumnAliases(int loc) {
        return delegate.getColumnAliases(loc);
    }

    @Override
    public String[] getColumnAliases(int loc, Criteria criteria, CriteriaQuery criteriaQuery) {
        return delegate.getColumnAliases(loc, criteria, criteriaQuery);
    }

    @Override
    public String[] getColumnAliases(String alias, int loc) {
        return delegate.getColumnAliases(alias, loc);
    }

    @Override
    public String[] getColumnAliases(String alias, int loc, Criteria criteria, CriteriaQuery criteriaQuery) {
        return delegate.getColumnAliases(alias, loc, criteria, criteriaQuery);
    }

    @Override
    public Type[] getTypes(String alias, Criteria criteria, CriteriaQuery criteriaQuery) {
        return delegate.getTypes(alias, criteria, criteriaQuery);
    }

    @Override
    public String[] getAliases() {
        return delegate.getAliases();
    }

    @Override
    public Projection getProjection(int i) {
        return delegate.getProjection(i);
    }

    @Override
    public int getLength() {
        return delegate.getLength();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean isGrouped() {
        return delegate.isGrouped();
    }

    public ProjectionList inner() {
        return delegate;
    }
}
