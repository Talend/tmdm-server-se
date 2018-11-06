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
public class Paging implements Visitable {

    private int start = 0;

    private int limit = Integer.MAX_VALUE;

    public void setStart(int start) {
        this.start = start;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getStart() {
        return start;
    }

    public int getLimit() {
        return limit;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Paging)) {
            return false;
        }
        Paging paging = (Paging) o;
        if (limit != paging.limit) {
            return false;
        }
        if (start != paging.start) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + limit;
        return result;
    }
}
