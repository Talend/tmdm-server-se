/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import org.talend.mdm.commmon.metadata.*;
import org.apache.commons.lang.StringUtils;

import javax.xml.XMLConstants;
import java.util.*;

class ScatteredMappingCreator extends DefaultMetadataVisitor<TypeMapping> {

    public static final String GENERATED_ID = "x_talend_id"; //$NON-NLS-1$

    private final MetadataRepository internalRepository;

    private final MappingRepository mappings;

    private final boolean preferClobUse;

    private final boolean enforceTechnicalFK;

    private final Stack<ComplexTypeMetadata> currentType = new Stack<ComplexTypeMetadata>();

    private final Stack<TypeMapping> currentMapping = new Stack<TypeMapping>();

    private final MappingCreatorContext context;

    private TypeMapping entityMapping;

    private final Set<ComplexTypeMetadata> processedTypes = new HashSet<ComplexTypeMetadata>();

    public ScatteredMappingCreator(MetadataRepository repository,
            MappingRepository mappings,
            MappingCreatorContext context,
            boolean preferClobUse,
            boolean enforceTechnicalFK) {
        this.internalRepository = repository;
        this.mappings = mappings;
        this.context = context;
        this.preferClobUse = preferClobUse;
        this.enforceTechnicalFK = enforceTechnicalFK;
    }

    private TypeMapping handleField(FieldMetadata field) {
        SimpleTypeFieldMetadata newFlattenField;
        newFlattenField = new SimpleTypeFieldMetadata(currentType.peek(),
                field.isKey(),
                field.isMany(),
                isDatabaseMandatory(field, field.getDeclaringType()),
                context.getFieldColumn(field),
                field.getType(),
                field.getWriteUsers(),
                field.getHideUsers(),
                field.getWorkflowAccessRights(),
                StringUtils.EMPTY);
        TypeMetadata declaringType = field.getDeclaringType();
        if (declaringType != field.getContainingType() && declaringType.isInstantiable()) {
            SoftTypeRef type = new SoftTypeRef(internalRepository,
                        declaringType.getNamespace(),
                        declaringType.getName(),
                        true);
            newFlattenField.setDeclaringType(type);
        }
        String data = field.getType().<String> getData(MetadataRepository.DATA_MAX_LENGTH);
        if (data != null && preferClobUse) {
            if (Integer.parseInt(data) > context.getTextLimit()) {
                newFlattenField.getType().setData(TypeMapping.SQL_TYPE, TypeMapping.SQL_TYPE_CLOB);
                newFlattenField.setData(MetadataRepository.DATA_ZIPPED, Boolean.FALSE);
            }
        }

        String defaultValueRule = field.<String> getData(MetadataRepository.DEFAULT_VALUE_RULE);
        if (StringUtils.isNotBlank(defaultValueRule)) {
            newFlattenField.setData(MetadataRepository.DEFAULT_VALUE_RULE, defaultValueRule);
        }

        currentType.peek().addField(newFlattenField);
        entityMapping.map(field, newFlattenField);
        currentMapping.peek().map(field, newFlattenField);
        return null;
    }

    private static String newNonInstantiableTypeName(ComplexTypeMetadata fieldReferencedType) {
        return getNonInstantiableTypeName(fieldReferencedType.getName());
    }

    private static String getNonInstantiableTypeName(String typeName) {
        if (!typeName.startsWith("X_")) { //$NON-NLS-1$
            return "X_" + typeName.replace('-', '_'); //$NON-NLS-1$
        } else {
            return typeName;
        }
    }

    @Override
    public TypeMapping visit(ReferenceFieldMetadata referenceField) {
        ComplexTypeMetadata fieldReferencedType = referenceField.getReferencedType();
        ComplexTypeMetadata referencedType;
        if (fieldReferencedType.isInstantiable()) {
            String typeName = fieldReferencedType.getName().replace('-', '_');
            referencedType = new SoftTypeRef(internalRepository,
                    fieldReferencedType.getNamespace(),
                    typeName,
                    true);
        } else {
            referencedType = new SoftTypeRef(internalRepository,
                    fieldReferencedType.getNamespace(),
                    newNonInstantiableTypeName(fieldReferencedType),
                    true);
        }

        String referencedTypeName = referencedType.getName().replace('-', '_');
        FieldMetadata referencedFieldCopy = new SoftIdFieldRef(internalRepository, referencedTypeName);

        ComplexTypeMetadata database = currentType.peek();

        boolean fkIntegrity = referenceField.isFKIntegrity() && (fieldReferencedType != entityMapping.getUser()); // Don't enforce FK integrity for references to itself.
        ReferenceFieldMetadata newFlattenField = new ReferenceFieldMetadata(currentType.peek(),
                referenceField.isKey(),
                referenceField.isMany(),
                isDatabaseMandatory(referenceField, referenceField.getDeclaringType()),
                context.getFieldColumn(referenceField),
                referencedType,
                referencedFieldCopy,
                Collections.<FieldMetadata>emptyList(),
                referenceField.getForeignKeyInfoFormat(), 
                fkIntegrity,
                referenceField.allowFKIntegrityOverride(),
                new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING),
                referenceField.getWriteUsers(),
                referenceField.getHideUsers(),
                referenceField.getWorkflowAccessRights(),
                StringUtils.EMPTY,
                StringUtils.EMPTY);
        newFlattenField.setData(MetadataRepository.DATA_MAX_LENGTH, Types.UUID_LENGTH);
        database.addField(newFlattenField);
        entityMapping.map(referenceField, newFlattenField);
        currentMapping.peek().map(referenceField, newFlattenField);
        return null;
    }

    @Override
    public TypeMapping visit(ContainedComplexTypeMetadata containedType) {
        String typeName = containedType.getName().replace('-', '_');
        String databaseSuperType = createContainedType(getNonInstantiableTypeName(typeName), null, containedType);
        for (ComplexTypeMetadata subType : containedType.getSubTypes()) {
            String subTypeName = subType.getName().replace('-', '_');
            createContainedType(getNonInstantiableTypeName(subTypeName), databaseSuperType, subType);
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
                    originalContainedType.getPrimaryKeyInfo(),
                    originalContainedType.getLookupFields(),
                    false,
                    originalContainedType.getWorkflowAccessRights());
            internalContainedType.setData(TypeMapping.USAGE_NUMBER, originalContainedType.getUsages().size());
            internalRepository.addTypeMetadata(internalContainedType);
            if (superTypeName == null) {
                // Generate a technical ID only if contained type does not have super type (subclasses will inherit it).
                SimpleTypeFieldMetadata fieldMetadata = new SimpleTypeFieldMetadata(internalContainedType,
                        true,
                        false,
                        true,
                        GENERATED_ID,
                        new SoftTypeRef(internalRepository, internalRepository.getUserNamespace(), Types.UUID, false),
                        originalContainedType.getWriteUsers(),
                        originalContainedType.getHideUsers(),
                        originalContainedType.getWorkflowAccessRights(), StringUtils.EMPTY);
                internalContainedType.addField(fieldMetadata);
                fieldMetadata.setData(MetadataRepository.DATA_MAX_LENGTH, Types.UUID_LENGTH);
            } else {
                SoftTypeRef type = new SoftTypeRef(internalRepository,
                        internalContainedType.getNamespace(),
                        superTypeName,
                        false);
                internalContainedType.addSuperType(type);
            }
            internalRepository.addTypeMetadata(internalContainedType);
        }
        // Visit contained type fields
        TypeMapping mapping = mappings.getMappingFromUser(originalContainedType);
        if (mapping == null) {
            mapping = new FlatTypeMapping(originalContainedType, internalContainedType, mappings);
            mappings.addMapping(mapping);
        }
        currentMapping.push(mapping);
        currentType.push(internalContainedType);
        {
            super.visit(originalContainedType);
        }
        currentType.pop();
        currentMapping.pop();
        return typeName;
    }

    @Override
    public TypeMapping visit(ContainedTypeFieldMetadata containedField) {
        String containedTypeName = newNonInstantiableTypeName(containedField.getContainedType());
        SoftTypeRef typeRef = new SoftTypeRef(internalRepository,
                containedField.getDeclaringType().getNamespace(),
                containedTypeName,
                false);
        ReferenceFieldMetadata newFlattenField = new ReferenceFieldMetadata(currentType.peek(),
                false,
                containedField.isMany(),
                isDatabaseMandatory(containedField, containedField.getDeclaringType()),
                context.getFieldColumn(containedField),
                typeRef,
                new SoftIdFieldRef(internalRepository, containedTypeName),
                Collections.<FieldMetadata>emptyList(),
                StringUtils.EMPTY, 
                enforceTechnicalFK,
                false,
                new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING),
                containedField.getWriteUsers(),
                containedField.getHideUsers(),
                containedField.getWorkflowAccessRights(),
                StringUtils.EMPTY,
                StringUtils.EMPTY);
        newFlattenField.setData(MetadataRepository.DATA_MAX_LENGTH, Types.UUID_LENGTH);
        newFlattenField.setData(MappingGenerator.SQL_DELETE_CASCADE, Boolean.TRUE.toString());
        currentType.peek().addField(newFlattenField);
        currentMapping.peek().map(containedField, newFlattenField);
        entityMapping.map(containedField, newFlattenField);
        if (!processedTypes.contains(containedField.getContainedType())) {
            processedTypes.add(containedField.getContainedType());
            containedField.getContainedType().accept(this);
            // recreate contained -> container association on mapped types
            TypeMapping containedTypeMapping = mappings.getMappingFromUser(containedField.getContainedType());
            ComplexTypeMetadata mappedContainedComplexType = containedTypeMapping.getDatabase();
            mappedContainedComplexType.setContainer(newFlattenField);
        }
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
        TypeMapping typeMapping;
        if (complexType.isInstantiable()) {
            entityMapping = new ScatteredTypeMapping(complexType, mappings);
            typeMapping = entityMapping;
        } else {
            typeMapping = new ScatteredTypeMapping(complexType, mappings);
            mappings.addMapping(typeMapping);
        }
        ComplexTypeMetadata database = typeMapping.getDatabase();
        if (!complexType.isInstantiable()) {
            // In this mapping prefix non instantiable types with "x_" so table name is not mixed up with an entity
            // table with same name.
            database.setName(newNonInstantiableTypeName(database));
        }
        currentMapping.push(typeMapping);
        currentType.push(database);
        {
            internalRepository.addTypeMetadata(database);
            if (complexType.getKeyFields().isEmpty() && complexType.getSuperTypes().isEmpty()) {
                // Assumes super type will define an id.
                SoftTypeRef type = new SoftTypeRef(internalRepository, StringUtils.EMPTY, Types.UUID, false);
                SimpleTypeFieldMetadata fieldMetadata = new SimpleTypeFieldMetadata(database, true, false, true, GENERATED_ID,
                        type, Collections.<String> emptyList(), Collections.<String> emptyList(),
                        Collections.<String> emptyList(), StringUtils.EMPTY);
                database.addField(fieldMetadata);
                fieldMetadata.setData(MetadataRepository.DATA_MAX_LENGTH, Types.UUID_LENGTH);
            }
            for (TypeMetadata superType : complexType.getSuperTypes()) {
                if (superType.isInstantiable()) {
                    SoftTypeRef type = new SoftTypeRef(internalRepository, superType.getNamespace(), superType.getName().replace(
                            '-', '_'), superType.isInstantiable());
                    database.addSuperType(type);
                } else {
                    SoftTypeRef type = new SoftTypeRef(internalRepository, superType.getNamespace(),
                            getNonInstantiableTypeName(superType.getName()), superType.isInstantiable());
                    database.addSuperType(type);
                }
            }
            super.visit(complexType);
            for (FieldMetadata keyField : complexType.getKeyFields()) {
                database.registerKey(database.getField(context.getFieldColumn(keyField.getName())));
            }
        }
        currentType.pop();
        currentMapping.pop();
        if (complexType.isInstantiable() && !currentType.isEmpty()) { // This is unexpected
            throw new IllegalStateException("Type remained in process stack."); //$NON-NLS-1$
        }
        return typeMapping;
    }
}
