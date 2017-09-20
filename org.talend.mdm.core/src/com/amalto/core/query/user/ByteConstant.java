/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.util.Util;

public class ByteConstant implements ConstantExpression<Byte> {

    private final Byte constant;

    private List<Byte> constantCollection = new ArrayList();

    public ByteConstant(String constant) {
        this.constant = Byte.parseByte(constant);
    }

    public ByteConstant(Collection constant) {
        this.constantCollection = constant;
        this.constant = null;
    }

    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Byte getValue() {
        return constant;
    }

    public String getTypeName() {
        return Types.BYTE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteConstant)) {
            return false;
        }
        ByteConstant that = (ByteConstant) o;
        return constant.equals(that.constant);
    }

    @Override
    public int hashCode() {
        return constant.hashCode();
    }

    @Override
    public Collection<Byte> getValueList() {
        return constantCollection;
    }
}
