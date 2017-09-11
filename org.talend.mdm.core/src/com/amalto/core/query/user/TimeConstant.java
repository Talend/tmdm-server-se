/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.query.user;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.talend.mdm.commmon.metadata.Types;

/**
 *
 */
public class TimeConstant implements ConstantExpression<Date> {

    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$

    private final Date value;

    public TimeConstant(String value) {
        synchronized (TIME_FORMAT) {
            try {
                this.value = TIME_FORMAT.parse(value);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Date getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    @Override
    public String getTypeName() {
        return Types.DATETIME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeConstant)) {
            return false;
        }
        TimeConstant that = (TimeConstant) o;
        return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
