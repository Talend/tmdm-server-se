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

import org.talend.mdm.commmon.metadata.Types;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

/**
 *
 */
public class DateTimeConstant implements ConstantExpression<Date> {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$

    private final Date value;

    private Collection<Date> constantCollection = new ArrayList();

    public DateTimeConstant(String value) {
        synchronized (DATE_FORMAT) {
            try {
                if (value.contains(UserQueryBuilder.IN_VALUE_SPLIT)) {
                    Collection<String> stringCollection = Arrays.asList(value.split(UserQueryBuilder.IN_VALUE_SPLIT));
                    Collection<Date> resultCollection = new ArrayList();
                    for (String tmp : stringCollection) {
                        resultCollection.add(DATE_FORMAT.parse(tmp));
                    }
                    this.constantCollection = resultCollection;
                    this.value = null;
                } else {
                    this.value = DATE_FORMAT.parse(value);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Date getValue() {
        return value;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    public String getTypeName() {
        return Types.DATETIME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DateTimeConstant)) {
            return false;
        }
        DateTimeConstant that = (DateTimeConstant) o;
        return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : constantCollection.isEmpty() ? 0 : constantCollection.hashCode();
    }

    @Override
    public String getStringValue() {
        if (value != null) {
            return String.valueOf(DATE_FORMAT.format(value));
        } else {
            StringBuilder sb = new StringBuilder();
            for (Date date : constantCollection) {
                sb.append(DATE_FORMAT.format(date));
                sb.append(UserQueryBuilder.IN_VALUE_SPLIT);
            }
            return sb.toString().substring(0, sb.toString().length() - UserQueryBuilder.IN_VALUE_SPLIT.length());
        }
    }

    @Override
    public Collection<Date> getValueList() {
        return constantCollection;
    }
}
