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

package com.amalto.core.metadata;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class SimpleTypeMetadata implements TypeMetadata {

    private final String nameSpace;

    private final List<TypeMetadata> superTypes = new LinkedList<TypeMetadata>();

    private String name;

    private boolean isFrozen = false;

    public SimpleTypeMetadata(String nameSpace, String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }

        this.name = name;
        this.nameSpace = nameSpace;
    }

    public Collection<TypeMetadata> getSuperTypes() {
        return superTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (isFrozen) {
            throw new IllegalStateException("Cannot change name after type was frozen.");
        }
        this.name = name;
    }

    public String getNamespace() {
        return nameSpace;
    }

    public boolean isAbstract() {
        return true;
    }

    public boolean isAssignableFrom(TypeMetadata type) {
        // Don't support inheritance with simple types.
        return false;
    }

    public TypeMetadata copy(MetadataRepository repository) {
        SimpleTypeMetadata copy = new SimpleTypeMetadata(nameSpace, name);
        for (TypeMetadata superType : superTypes) {
            copy.addSuperType(superType.copy(repository), repository);
        }
        return copy;
    }

    public TypeMetadata copyShallow() {
        return new SimpleTypeMetadata(nameSpace, name);
    }

    public TypeMetadata freeze() {
        isFrozen = true;
        return this;
    }

    public void addSuperType(TypeMetadata superType, MetadataRepository repository) {
        superTypes.add(superType);
    }

    public <T> T accept(MetadataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return '[' + nameSpace + ':' + name + ']';
    }

}
