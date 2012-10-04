/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query.user;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class TimeConstant implements TypedExpression {

    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm:ss");

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

    public Date getValue() {
        return value;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Expression normalize() {
        return this;
    }

    public String getTypeName() {
        return "dateTime"; // TODO Constants
    }
}
