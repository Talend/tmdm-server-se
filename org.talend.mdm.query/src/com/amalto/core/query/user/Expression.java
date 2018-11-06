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

package com.amalto.core.query.user;

/**
 *
 */
public interface Expression extends Visitable {

    /**
     * @return A <em>normalized</em> version of the Expression (e.g. prune unnecessary branches in conditions).
     */
    Expression normalize();

    /**
     * @return <code>true</code> if the result of the expression can be cached. <code>false</code> otherwise.
     */
    boolean cache();
}
