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
import com.amalto.core.storage.record.StorageConstants;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataVisitor;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.SoftTypeRef;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;

import javax.xml.XMLConstants;
import java.util.Collections;

class SystemTypeMappingRepository extends InternalRepository {

    public SystemTypeMappingRepository(TypeMappingStrategy strategy, RDBMSDataSource.DataSourceDialect dialect) {
        super(strategy, dialect);
    }

    @Override
    public MetadataRepository visit(MetadataRepository repository) {
        MetadataRepository result = super.visit(repository);
        for (TypeMetadata type : repository.getNonInstantiableTypes()) {
            type.accept(this);
        }
        return result;
    }

    @Override
    MetadataVisitor<TypeMapping> getTypeMappingCreator(TypeMetadata type, TypeMappingStrategy strategy) {
        MetadataVisitor<TypeMapping> defaultMappingCreator = super.getTypeMappingCreator(type, strategy);
        if (defaultMappingCreator instanceof ScatteredMappingCreator) {
            MappingCreatorContext scatteredContext = new StatefulContext(dialect);
            return new SystemScatteredMappingCreator(internalRepository,
                    mappings,
                    scatteredContext,
                    strategy.preferClobUse(),
                    strategy.useTechnicalFk());

        } else {
            return defaultMappingCreator;
        }
    }

    public MetadataRepository visit(ComplexTypeMetadata complexType) {
        MetadataVisitor<TypeMapping> creator = getTypeMappingCreator(complexType, strategy);
        TypeMapping typeMapping = complexType.accept(creator);
        // Add MDM specific record specific metadata: keep this additional fields for system objects too: MDM studio
        // may query these fields (see TMDM-5666).
        ComplexTypeMetadata database = typeMapping.getDatabase();
        if (database.isInstantiable() && !database.isFrozen() && database.getSuperTypes().isEmpty()) {
            TypeMetadata longType = new SoftTypeRef(internalRepository, XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.LONG, false);
            TypeMetadata stringType = new SoftTypeRef(internalRepository, XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING, false);
            database.addField(new SimpleTypeFieldMetadata(database, false, false, true, StorageConstants.METADATA_TIMESTAMP, longType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY));
            database.addField(new SimpleTypeFieldMetadata(database, false, false, false, StorageConstants.METADATA_TASK_ID, stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY));
        }
        // Register mapping
        internalRepository.addTypeMetadata(typeMapping.getDatabase());
        mappings.addMapping(typeMapping);
        return internalRepository;
    }
}
