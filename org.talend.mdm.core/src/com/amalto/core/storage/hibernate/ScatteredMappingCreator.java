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

package com.amalto.core.storage.hibernate;

import com.amalto.core.metadata.*;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Stack;

class ScatteredMappingCreator extends DefaultMetadataVisitor<TypeMapping> {

    private static final String GENERATED_ID = "x_talend_id";  //$NON-NLS-1$

    private final MetadataRepository internalRepository;

    private final MappingRepository mappings;

    private final Stack<ComplexTypeMetadata> currentType = new Stack<ComplexTypeMetadata>();

    private TypeMapping mapping;

    public ScatteredMappingCreator(MetadataRepository repository, MappingRepository mappings) {
        internalRepository = repository;
        this.mappings = mappings;
    }

    private TypeMapping handleField(FieldMetadata field) {
        SimpleTypeFieldMetadata newFlattenField;
        String name = getFieldName(field);
        newFlattenField = new SimpleTypeFieldMetadata(currentType.peek(), field.isKey(), field.isMany(), field.isMandatory(), name, field.getType(), field.getWriteUsers(), field.getHideUsers());
        if (field.getDeclaringType() != field.getContainingType()) {
            newFlattenField.setDeclaringType(new SoftTypeRef(internalRepository, field.getDeclaringType().getNamespace(), field.getDeclaringType().getName(), true));
        }
        currentType.peek().addField(newFlattenField);
        mapping.map(field, newFlattenField);
        return null;
    }

    private String getFieldName(FieldMetadata field) {
        return "x_" + field.getName().toLowerCase(); //$NON-NLS-1$
    }

    @Override
    public TypeMapping visit(ReferenceFieldMetadata referenceField) {
        String name = getFieldName(referenceField);
        ComplexTypeMetadata referencedType = new SoftTypeRef(internalRepository, StringUtils.EMPTY, referenceField.getReferencedType().getName(), true);

        FieldMetadata referencedFieldCopy = new SoftIdFieldRef(internalRepository, referencedType.getName());
        FieldMetadata foreignKeyInfoFieldCopy = referenceField.hasForeignKeyInfo() ? referenceField.getForeignKeyInfoField().copy(internalRepository) : null;

        ComplexTypeMetadata database = currentType.peek();

        boolean fkIntegrity = referenceField.isFKIntegrity() && (referenceField.getReferencedType() != mapping.getUser()); // Don't enforce FK integrity for references to itself.
        ReferenceFieldMetadata newFlattenField = new ReferenceFieldMetadata(currentType.peek(),
                referenceField.isKey(),
                referenceField.isMany(),
                referenceField.isMandatory(),
                name,
                referencedType,
                referencedFieldCopy,
                foreignKeyInfoFieldCopy,
                fkIntegrity,
                referenceField.allowFKIntegrityOverride(),
                referenceField.getWriteUsers(),
                referenceField.getHideUsers());
        database.addField(newFlattenField);
        mapping.map(referenceField, newFlattenField);
        return null;
    }

    @Override
    public TypeMapping visit(ContainedComplexTypeMetadata containedType) {
        String typeName = containedType.getName().replace('-', '_');
        String databaseSuperType = createContainedType(typeName, null, containedType);
        for (ComplexTypeMetadata subType : containedType.getSubTypes()) {
            createContainedType(subType.getName().replace('-', '_'), databaseSuperType, subType);
        }
        return null;
    }

    private String createContainedType(String typeName, String superTypeName, ComplexTypeMetadata originalContainedType) {
        ComplexTypeMetadata internalContainedType = (ComplexTypeMetadata) internalRepository.getType(typeName);
        if (internalContainedType == null) {
            internalContainedType = new ComplexTypeMetadataImpl(originalContainedType.getNamespace(),
                    typeName,
                    originalContainedType.getWriteUsers(),
                    originalContainedType.getDenyCreate(),
                    originalContainedType.getHideUsers(),
                    originalContainedType.getDenyDelete(ComplexTypeMetadata.DeleteType.PHYSICAL),
                    originalContainedType.getDenyDelete(ComplexTypeMetadata.DeleteType.LOGICAL),
                    originalContainedType.getSchematron(),
                    false);
            internalRepository.addTypeMetadata(internalContainedType);
            if (superTypeName == null) {  // Generate a technical ID only if contained type does not have super type (subclasses will inherit it).
                internalContainedType.addField(new SimpleTypeFieldMetadata(internalContainedType,
                        true,
                        false,
                        true,
                        GENERATED_ID,
                        new SoftTypeRef(internalRepository, internalRepository.getUserNamespace(), "UUID", false), //$NON-NLS-1$
                        originalContainedType.getWriteUsers(),
                        originalContainedType.getHideUsers()));
            } else {
                internalContainedType.addSuperType(new SoftTypeRef(internalRepository, internalContainedType.getNamespace(), superTypeName, false), internalRepository);
            }
            internalRepository.addTypeMetadata(internalContainedType);
        }
        currentType.push(internalContainedType);
        {
            super.visit(originalContainedType);
        }
        currentType.pop();
        return typeName;
    }

    @Override
    public TypeMapping visit(ContainedTypeFieldMetadata containedField) {
        String fieldName = getFieldName(containedField);
        String containedTypeName = containedField.getContainedType().getName();
        SoftTypeRef typeRef = new SoftTypeRef(internalRepository,
                containedField.getDeclaringType().getNamespace(),
                containedTypeName,
                false);
        ReferenceFieldMetadata newFlattenField = new ReferenceFieldMetadata(currentType.peek(),
                false,
                containedField.isMany(),
                containedField.isMandatory(),
                fieldName,
                typeRef,
                new SoftIdFieldRef(internalRepository, containedTypeName),
                null,
                false,  // No need to enforce FK in references to these technical objects.
                false,
                containedField.getWriteUsers(),
                containedField.getHideUsers());
        newFlattenField.setData("SQL_DELETE_CASCADE", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        currentType.peek().addField(newFlattenField);
        mapping.map(containedField, newFlattenField);
        containedField.getContainedType().accept(this);
        return null;
    }

    @Override
    public TypeMapping visit(SimpleTypeFieldMetadata simpleField) {
        return handleField(simpleField);
    }

    @Override
    public TypeMapping visit(EnumerationFieldMetadata enumField) {
        return handleField(enumField);
    }

    @Override
    public TypeMapping visit(ComplexTypeMetadata complexType) {
        mapping = new ScatteredTypeMapping(complexType, mappings);
        ComplexTypeMetadata database = mapping.getDatabase();

        currentType.push(database);
        {
            internalRepository.addTypeMetadata(database);
            if (complexType.getKeyFields().isEmpty() && complexType.getSuperTypes().isEmpty()) { // Assumes super type will define an id.
                SoftTypeRef type = new SoftTypeRef(internalRepository, StringUtils.EMPTY, "UUID", false);
                database.addField(new SimpleTypeFieldMetadata(database, true, false, true, GENERATED_ID, type, Collections.<String>emptyList(), Collections.<String>emptyList())); //$NON-NLS-1$
            }
            for (TypeMetadata superType : complexType.getSuperTypes()) {
                database.addSuperType(new SoftTypeRef(internalRepository, superType.getNamespace(), superType.getName(), superType.isInstantiable()), internalRepository);
            }
            super.visit(complexType);
        }
        currentType.pop();
        if (!currentType.isEmpty()) { // This is unexpected
            throw new IllegalStateException("Type remained in process stack.");
        }
        return mapping;
    }
}
