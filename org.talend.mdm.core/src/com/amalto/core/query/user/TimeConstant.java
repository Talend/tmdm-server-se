/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import java.util.List;

import org.talend.mdm.commmon.metadata.Types;

public class TimeConstant implements ConstantExpression<Date> {

    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$

    private final Date value;

    private List<Date> valueList;

    public TimeConstant(String value) {
        assert value != null;
        synchronized (TIME_FORMAT) {
            try {
                this.value = TIME_FORMAT.parse(value);
                this.valueList = null;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public TimeConstant(List<Date> valueList) {
        assert valueList != null;
        this.valueList = valueList;
        this.value = null;
    }

    @Override
    public Date getValue() {
        if (isExpressionList()) {
            throw new IllegalStateException("The property of 'value' is not valid."); //$NON-NLS-1$
        }
        return value;
    }

    @Override
    public List<Date> getValueList() {
        if (!isExpressionList()) {
            throw new IllegalStateException("The property of 'valueList' is not valid."); //$NON-NLS-1$
        }
        return valueList;
    }

    @Override
    public boolean isExpressionList() {
        return this.valueList != null;
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
        if (isExpressionList()) {
            return valueList.equals(that.valueList);
        } else {
            return value.equals(that.value);
        }
    }

    @Override
    public int hashCode() {
        if (isExpressionList()) {
            return valueList.hashCode();
        } else {
            return value.hashCode();
        }
    }

}
