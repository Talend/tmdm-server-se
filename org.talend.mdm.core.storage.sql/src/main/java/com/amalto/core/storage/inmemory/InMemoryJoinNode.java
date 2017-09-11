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

package com.amalto.core.storage.inmemory;

import com.amalto.core.query.user.Select;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import java.util.*;

class InMemoryJoinNode {

    static enum Merge {
        UNION,
        INTERSECTION,
        NONE
    }

    long execTime;

    String name;

    Merge merge = Merge.NONE;

    Select expression;

    // TODO not very classy use of a Map
    final Map<InMemoryJoinNode, InMemoryJoinNode> children = new HashMap<InMemoryJoinNode, InMemoryJoinNode>();

    ComplexTypeMetadata type;

    FieldMetadata childProperty;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InMemoryJoinNode that = (InMemoryJoinNode) o;
        return !(name != null ? !name.equals(that.name) : that.name != null);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InMemoryJoinNode {" + "name='" + name + '\'' + '}';
    }
}
