/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import com.amalto.core.storage.datasource.RDBMSDataSource.DataSourceDialect;

import org.talend.mdm.commmon.metadata.*;
import org.apache.log4j.Logger;

import java.util.Collection;

public abstract class InternalRepository implements MetadataVisitor<MetadataRepository> {

    private static final Logger LOGGER = Logger.getLogger(InternalRepository.class);

    private final MappingCreatorContext scatteredContext;

    private MetadataRepository userRepository;

    final RDBMSDataSource.DataSourceDialect dialect;

    final TypeMappingStrategy strategy;

    MappingRepository mappings;

    MetadataRepository internalRepository;

    InternalRepository(TypeMappingStrategy strategy, RDBMSDataSource.DataSourceDialect dialect) {
        this.strategy = strategy;
        this.dialect = dialect;
        scatteredContext = new StatefulContext(dialect);
    }

    public MappingRepository getMappings() {
        return mappings;
    }

    public MetadataRepository getInternalRepository() {
        return internalRepository;
    }

    public MetadataRepository visit(MetadataRepository repository) {
        userRepository = repository;
        mappings = new MappingRepository();
        internalRepository = new MetadataRepository();
        for (TypeMetadata type : repository.getUserComplexTypes()) {
            type.accept(this);
        }
        for (TypeMapping typeMapping : mappings.getAllTypeMappings()) {
            typeMapping.freeze();
        }
        return internalRepository;
    }

    MetadataVisitor<TypeMapping> getTypeMappingCreator(TypeMetadata type, TypeMappingStrategy strategy) {
        if ("Update".equals(type.getName())) { //$NON-NLS-1$
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Mapping strategy: " + type.getName() + " -> fixed update report mapping.");
            }
            return new UpdateReportMappingCreator(type, userRepository, mappings, strategy.preferClobUse() && dialect != DataSourceDialect.DB2);
        }
        switch (strategy) {
            case AUTO:
                TypeMappingStrategy actualStrategy = type.accept(new MappingStrategySelector(20));
                return getTypeMappingCreator(type, actualStrategy);
            case FLAT:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Mapping strategy: " + type.getName() + " -> FLAT");
                }
                return new FlatTypeMappingCreator(internalRepository, mappings, dialect, strategy.preferClobUse());
            case SCATTERED:
            case SCATTERED_CLOB:
                if (LOGGER.isDebugEnabled()) {
                    if (strategy.preferClobUse()) {
                        LOGGER.debug("Mapping strategy: " + type.getName() + " -> SCATTERED (CLOBs)");
                    } else {
                        LOGGER.debug("Mapping strategy: " + type.getName() + " -> SCATTERED");
                    }
                }
                return new ScatteredMappingCreator(internalRepository,
                        mappings,
                        scatteredContext,
                        strategy.preferClobUse(),
                        strategy.useTechnicalFk());
            default:
                throw new IllegalArgumentException("Strategy '" + this.strategy + "' is not supported.");
        }
    }

    public MetadataRepository visit(ComplexTypeMetadata complexType) {
        MetadataVisitor<TypeMapping> mappingCreator = getTypeMappingCreator(complexType, strategy);
        complexType.accept(mappingCreator);
        return internalRepository;
    }

    public MetadataRepository visit(SimpleTypeMetadata simpleType) {
        internalRepository.addTypeMetadata(simpleType.copy());
        return internalRepository;
    }

    public MetadataRepository visit(ContainedComplexTypeMetadata containedType) {
        return internalRepository;
    }

    public MetadataRepository visit(SimpleTypeFieldMetadata simpleField) {
        return internalRepository;
    }

    public MetadataRepository visit(EnumerationFieldMetadata enumField) {
        return internalRepository;
    }

    public MetadataRepository visit(ReferenceFieldMetadata referenceField) {
        return internalRepository;
    }

    public MetadataRepository visit(ContainedTypeFieldMetadata containedField) {
        return internalRepository;
    }

    public MetadataRepository visit(FieldMetadata fieldMetadata) {
        return internalRepository;
    }

    private static class MappingStrategySelector extends DefaultMetadataVisitor<TypeMappingStrategy> {

        private final int fieldThreshold;

        private int fieldCount = 0;

        private MappingStrategySelector(int fieldThreshold) {
            this.fieldThreshold = fieldThreshold;
        }

        @Override
        public TypeMappingStrategy visit(ComplexTypeMetadata complexType) {
            if (!complexType.getSubTypes().isEmpty()) {
                return TypeMappingStrategy.SCATTERED;
            }
            if (!complexType.isInstantiable()) {
                return TypeMappingStrategy.SCATTERED;
            }
            Collection<FieldMetadata> fields = complexType.getFields();
            for (FieldMetadata field : fields) {
                TypeMappingStrategy result = field.accept(this);
                if (fieldCount > fieldThreshold || result == TypeMappingStrategy.SCATTERED) {
                    return TypeMappingStrategy.SCATTERED;
                }
            }
            return TypeMappingStrategy.FLAT;
        }

        @Override
        public TypeMappingStrategy visit(ContainedComplexTypeMetadata containedType) {
            fieldCount += containedType.getFields().size();
            Collection<FieldMetadata> fields = containedType.getFields();
            for (FieldMetadata field : fields) {
                TypeMappingStrategy result = field.accept(this);
                if (result == TypeMappingStrategy.SCATTERED) {
                    return result;
                }
            }
            if(containedType.getName().startsWith(MetadataRepository.ANONYMOUS_PREFIX)) {
                return TypeMappingStrategy.FLAT;
            } else {
                return TypeMappingStrategy.SCATTERED;
            }
        }

        @Override
        public TypeMappingStrategy visit(ContainedTypeFieldMetadata containedField) {
            ComplexTypeMetadata containedType = containedField.getContainedType();
            if (containedField.isMany()
                    || !containedType.getSubTypes().isEmpty()
                    || !containedType.getName().startsWith(MetadataRepository.ANONYMOUS_PREFIX)) {
                return TypeMappingStrategy.SCATTERED;
            }
            return super.visit(containedField);
        }

        @Override
        public TypeMappingStrategy visit(SimpleTypeFieldMetadata simpleField) {
            fieldCount++;
            return TypeMappingStrategy.AUTO;
        }
    }
}
