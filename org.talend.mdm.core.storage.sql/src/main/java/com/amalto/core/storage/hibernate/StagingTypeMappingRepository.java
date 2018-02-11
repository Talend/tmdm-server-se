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

import com.amalto.core.metadata.LongString;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.*;
import com.amalto.core.storage.Storage;

import javax.xml.XMLConstants;
import java.util.Collections;
import java.util.UUID;

class StagingTypeMappingRepository extends InternalRepository {
    public StagingTypeMappingRepository(TypeMappingStrategy mappingStrategy, RDBMSDataSource.DataSourceDialect dialect) {
        super(mappingStrategy, dialect);
    }

    public MetadataRepository visit(ComplexTypeMetadata complexType) { 
        TypeMapping typeMapping = complexType.accept(getTypeMappingCreator(complexType, strategy));

        // Add MDM specific record specific metadata
        ComplexTypeMetadata database = typeMapping.getDatabase();
        if (database.isInstantiable() && !database.isFrozen()) {
            TypeMetadata intType = new SoftTypeRef(internalRepository, XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.INT, false);
            TypeMetadata longType = new SoftTypeRef(internalRepository, XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.LONG, false);
            TypeMetadata booleanType = new SoftTypeRef(internalRepository, XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.BOOLEAN, false);
            TypeMetadata stringType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING);
            TypeMetadata limitedStringType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING);
            limitedStringType.setData(MetadataRepository.DATA_MAX_LENGTH, UUID.randomUUID().toString().length());
            // Time stamp
            database.addField(new SimpleTypeFieldMetadata(database, false, false, true, Storage.METADATA_TIMESTAMP, longType,
                    Collections.<String> emptyList(), Collections.<String> emptyList(), Collections.<String> emptyList(), StringUtils.EMPTY));
            // Task id
            database.addField(new SimpleTypeFieldMetadata(database, false, false, false, Storage.METADATA_TASK_ID,
                    limitedStringType, Collections.<String> emptyList(), Collections.<String> emptyList(), Collections
                            .<String> emptyList(), StringUtils.EMPTY));
            // Staging has task
            database.addField(new SimpleTypeFieldMetadata(database, false, false, false, Storage.METADATA_STAGING_HAS_TASK,
                    booleanType, Collections.<String> emptyList(), Collections.<String> emptyList(), Collections
                            .<String> emptyList(), StringUtils.EMPTY));
            // Staging status
            database.addField(new SimpleTypeFieldMetadata(database, false, false, false, Storage.METADATA_STAGING_STATUS,
                    intType, Collections.<String> emptyList(), Collections.<String> emptyList(), Collections.<String> emptyList(), StringUtils.EMPTY));
            // Staging source
            database.addField(new SimpleTypeFieldMetadata(database, false, false, false, Storage.METADATA_STAGING_SOURCE,
                    limitedStringType, Collections.<String> emptyList(), Collections.<String> emptyList(), Collections
                            .<String> emptyList(), StringUtils.EMPTY));
            // Staging block key
            database.addField(new SimpleTypeFieldMetadata(database, false, false, false, Storage.METADATA_STAGING_BLOCK_KEY,
                    limitedStringType, Collections.<String> emptyList(), Collections.<String> emptyList(), Collections
                            .<String> emptyList(), StringUtils.EMPTY));
            // Staging error field
            SimpleTypeFieldMetadata errorField = new SimpleTypeFieldMetadata(database, false, false, false,
                    Storage.METADATA_STAGING_ERROR, stringType, Collections.<String> emptyList(),
                    Collections.<String> emptyList(), Collections.<String> emptyList(), StringUtils.EMPTY);
            errorField.getType().setData(TypeMapping.SQL_TYPE, TypeMapping.SQL_TYPE_TEXT); 
            errorField.getType().setData(LongString.PREFER_LONGVARCHAR, true); // ORACLE will use VARCHAR2(4000 CHAR)
            database.addField(errorField);
            // Staging previous values field (useful for rematching)
            SimpleTypeFieldMetadata previousValuesField = new SimpleTypeFieldMetadata(database, false, false, false,
                    Storage.METADATA_STAGING_VALUES, stringType, Collections.<String> emptyList(),
                    Collections.<String> emptyList(), Collections.<String> emptyList(), StringUtils.EMPTY);
            previousValuesField.getType().setData(TypeMapping.SQL_TYPE, TypeMapping.SQL_TYPE_TEXT); 
            previousValuesField.getType().setData(LongString.PREFER_LONGVARCHAR, true); // ORACLE will use VARCHAR2(4000 CHAR)
            database.addField(previousValuesField);
        }

        // Register mapping
        internalRepository.addTypeMetadata(typeMapping.getDatabase());
        mappings.addMapping(typeMapping);
        return internalRepository;
    }
}
