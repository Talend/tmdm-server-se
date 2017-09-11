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

package com.amalto.core.storage.hibernate;

import com.amalto.core.storage.datasource.RDBMSDataSource;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.*;
import com.amalto.core.storage.Storage;

import javax.xml.XMLConstants;
import java.util.Collections;

class UserTypeMappingRepository extends InternalRepository {

    public UserTypeMappingRepository(TypeMappingStrategy mappingStrategy, RDBMSDataSource.DataSourceDialect dialect) {
        super(mappingStrategy, dialect);
    }

    public MetadataRepository visit(ComplexTypeMetadata complexType) {
        TypeMapping typeMapping = complexType.accept(getTypeMappingCreator(complexType, strategy));
        // Add MDM specific record specific metadata
        ComplexTypeMetadata database = typeMapping.getDatabase();
        if (database.isInstantiable() && !database.isFrozen()) {
            TypeMetadata longType = new SoftTypeRef(internalRepository, XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.LONG, false);
            TypeMetadata stringType = new SoftTypeRef(internalRepository, XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING, false);
            database.addField(new SimpleTypeFieldMetadata(database, false, false, true, Storage.METADATA_TIMESTAMP, longType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY));
            database.addField(new SimpleTypeFieldMetadata(database, false, false, false, Storage.METADATA_TASK_ID, stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY));
        }
        // Register mapping
        internalRepository.addTypeMetadata(typeMapping.getDatabase());
        mappings.addMapping(typeMapping);
        return internalRepository;
    }
}
