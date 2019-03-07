/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import com.amalto.core.storage.datasource.RDBMSDataSource;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.*;

import javax.xml.XMLConstants;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

class FlatTypeMappingCreator extends DefaultMetadataVisitor<TypeMapping> {

    private final LinkedList<String> prefix = new LinkedList<String>();

    private final MetadataRepository internalRepository;

    private final MappingRepository mappings;
    
    private final boolean preferClobUse;

    private final MappingCreatorContext context;

    private TypeMapping typeMapping;

    private boolean forceKey = false;

    public FlatTypeMappingCreator(MetadataRepository repository, MappingRepository mappings, RDBMSDataSource.DataSourceDialect dialect, boolean preferClobUse) {
        this.mappings = mappings;
        this.internalRepository = repository;
        this.context = new StatelessContext(prefix, dialect);
        this.preferClobUse = preferClobUse;
    }

    @Override
    public TypeMapping visit(ReferenceFieldMetadata referenceField) {
        String name = context.getFieldColumn(referenceField);
        String typeName = referenceField.getReferencedType().getName().replace('-', '_');
        ComplexTypeMetadata referencedType = new SoftTypeRef(internalRepository, referenceField.getReferencedType().getNamespace(), typeName, true);
        FieldMetadata referencedField = new SoftIdFieldRef(internalRepository, typeName);

        TypeMetadata declaringType = referenceField.getDeclaringType();
        ComplexTypeMetadata database = typeMapping.getDatabase();
        ReferenceFieldMetadata newFlattenField = new ReferenceFieldMetadata(database,
                referenceField.isKey(),
                referenceField.isMany(),
                isDatabaseMandatory(referenceField, declaringType),
                name,
                referencedType,
                referencedField,
                Collections.<FieldMetadata>emptyList(),
                referenceField.getForeignKeyInfoFormat(), 
                referenceField.isFKIntegrity(),
                referenceField.allowFKIntegrityOverride(),
                new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING),
                referenceField.getWriteUsers(),
                referenceField.getHideUsers(),
                referenceField.getWorkflowAccessRights(),
                StringUtils.EMPTY,
                StringUtils.EMPTY);
        if (!referenceField.getContainingType().equals(declaringType)) {
            String declaringTypeName = declaringType.isInstantiable() ? declaringType.getName() : "X_" + declaringType.getName(); //$NON-NLS-1$
            SoftTypeRef internalDeclaringType = new SoftTypeRef(internalRepository, declaringType.getNamespace(),
                    declaringTypeName, declaringType.isInstantiable());
            newFlattenField.setDeclaringType(internalDeclaringType);           
        }
        database.addField(newFlattenField);
        typeMapping.map(referenceField, newFlattenField);
        return typeMapping;
    }

    @Override
    public TypeMapping visit(ContainedComplexTypeMetadata containedType) {
        mappings.addMapping(typeMapping);
        Collection<FieldMetadata> fields = containedType.getFields();
        for (FieldMetadata field : fields) {
            field.accept(this);
        }
        return typeMapping;
    }

    @Override
    public TypeMapping visit(ContainedTypeFieldMetadata containedField) {
        prefix.add(containedField.getName());
        {
            ComplexTypeMetadata containedType = containedField.getContainedType();
            containedType.accept(this);
            for (ComplexTypeMetadata subType : containedType.getSubTypes()) {
                for (FieldMetadata subTypeField : subType.getFields()) {
                    subTypeField.accept(this);
                }
            }
        }
        prefix.removeLast();
        return typeMapping;
    }

    @Override
    public TypeMapping visit(SimpleTypeFieldMetadata field) {
        SimpleTypeFieldMetadata newFlattenField;
        ComplexTypeMetadata database = typeMapping.getDatabase();
        TypeMetadata declaringType = field.getDeclaringType();
        if (field.getContainingType() == declaringType) {
            newFlattenField = new SimpleTypeFieldMetadata(database,
                    false,
                    field.isMany(),
                    isDatabaseMandatory(field, declaringType),
                    context.getFieldColumn(field),
                    field.getType(),
                    field.getWriteUsers(),
                    field.getHideUsers(),
                    field.getWorkflowAccessRights(),
                    StringUtils.EMPTY);
            database.addField(newFlattenField);
        } else {
            SoftTypeRef internalDeclaringType;
            if (!declaringType.isInstantiable()) {
                internalDeclaringType = new SoftTypeRef(internalRepository, declaringType.getNamespace(), "X_" + declaringType.getName(), declaringType.isInstantiable()); //$NON-NLS-1$
            } else {
                internalDeclaringType = new SoftTypeRef(internalRepository, declaringType.getNamespace(), declaringType.getName(), declaringType.isInstantiable());
            }
            newFlattenField = new SimpleTypeFieldMetadata(database,
                    false,
                    field.isMany(),
                    isDatabaseMandatory(field, declaringType),
                    context.getFieldColumn(field),
                    field.getType(),
                    field.getWriteUsers(),
                    field.getHideUsers(),
                    field.getWorkflowAccessRights(),
                    StringUtils.EMPTY);
            newFlattenField.setDeclaringType(internalDeclaringType);
            database.addField(newFlattenField);
        }
        String data = field.getType().<String> getData(MetadataRepository.DATA_MAX_LENGTH);
        if (data != null && preferClobUse) {
            if (Integer.parseInt(data) > context.getTextLimit()) {
                newFlattenField.getType().setData(TypeMapping.SQL_TYPE, TypeMapping.SQL_TYPE_CLOB);
                newFlattenField.setData(MetadataRepository.DATA_ZIPPED, Boolean.FALSE);
            }
        }

        String defaultValue = field.<String>getData(MetadataRepository.DEFAULT_VALUE);
        if (StringUtils.isNotBlank(defaultValue)) {
            newFlattenField.setData(MetadataRepository.DEFAULT_VALUE, defaultValue);
        }

        String defaultValueRule = field.<String>getData(MetadataRepository.DEFAULT_VALUE_RULE);
        if (StringUtils.isNotBlank(defaultValue)) {
            newFlattenField.setData(MetadataRepository.DEFAULT_VALUE_RULE, defaultValueRule);
        }
        typeMapping.map(field, newFlattenField);
        return typeMapping;
    }

    @Override
    public TypeMapping visit(EnumerationFieldMetadata enumField) {
        // Seems pretty strange to use an enum field as key but nothing prevents user to do this.
        boolean isKey = enumField.isKey() || forceKey;
        FieldMetadata newFlattenField;
        if (enumField.getContainingType() == enumField.getDeclaringType()) {
            ComplexTypeMetadata database = typeMapping.getDatabase();
            newFlattenField = new EnumerationFieldMetadata(database,
                    isKey,
                    enumField.isMany(),
                    isDatabaseMandatory(enumField, enumField.getDeclaringType()),
                    context.getFieldColumn(enumField),
                    enumField.getType(),
                    enumField.getWriteUsers(),
                    enumField.getHideUsers(),
                    enumField.getWorkflowAccessRights(),
                    StringUtils.EMPTY);
            database.addField(newFlattenField);
        } else {
            newFlattenField = new SoftFieldRef(internalRepository,
                    context.getFieldColumn(enumField),
                    enumField.getContainingType().getName());
        }
        typeMapping.map(enumField, newFlattenField);
        return typeMapping;
    }

    @Override
    public TypeMapping visit(ComplexTypeMetadata complexType) {
        typeMapping = new FlatTypeMapping(complexType, FlatTypeMappingCreator.this.mappings);
        Collection<FieldMetadata> fields = complexType.getFields();
        for (FieldMetadata field : fields) {
            field.accept(this);
        }
        Collection<FieldMetadata> keyFields = complexType.getKeyFields();
        ComplexTypeMetadata database = typeMapping.getDatabase();
        database.setData(TypeMapping.USAGE_NUMBER, complexType.getUsages().size());
        Collection<TypeMetadata> superTypes = complexType.getSuperTypes();
        for (TypeMetadata superType : superTypes) {
            database.addSuperType(new SoftTypeRef(internalRepository, superType.getNamespace(), superType.getName(), true));
        }
        forceKey = true;
        for (FieldMetadata keyField : keyFields) {
            database.registerKey(typeMapping.getDatabase(keyField));
        }
        forceKey = false;
        if (typeMapping.getUser().getKeyFields().isEmpty() && typeMapping.getUser().getSuperTypes().isEmpty()) { // Assumes super type defines key field.
            SoftTypeRef type = new SoftTypeRef(internalRepository, XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING, false);
            SimpleTypeFieldMetadata fieldMetadata = new SimpleTypeFieldMetadata(database,
                    true,
                    false,
                    true,
                    ScatteredMappingCreator.GENERATED_ID,
                    type,
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList(),
                    StringUtils.EMPTY);
            database.addField(fieldMetadata);
        }
        return typeMapping;
    }
}
