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

import java.util.Collections;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadataImpl;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

class UpdateReportMappingCreator extends DefaultMetadataVisitor<TypeMapping> {

    private MappingRepository mappings;

    private final ComplexTypeMetadata DATABASE_UPDATE_REPORT_TYPE;

    private final ComplexTypeMetadata USER_UPDATE_REPORT_TYPE;

    private final MetadataRepository repository;

    public UpdateReportMappingCreator(TypeMetadata updateReportType, MetadataRepository repository, MappingRepository mappings, boolean preferClobUse) {
        this.repository = repository;
        if(updateReportType == null) {
            throw new IllegalStateException("Update report type cannot be null."); //$NON-NLS-1$
        }
        USER_UPDATE_REPORT_TYPE = (ComplexTypeMetadata) updateReportType;
        this.mappings = mappings;
        boolean isUUIDMode = MDMConfiguration.isClusterEnabled() ? true : false;
        ComplexTypeMetadata databaseUpdateReportType = new ComplexTypeMetadataImpl(StringUtils.EMPTY, "X_UPDATE_REPORT", true); //$NON-NLS-1$
        TypeMetadata stringType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING);
        TypeMetadata longStringType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING);
        TypeMetadata longType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.LONG);
        databaseUpdateReportType.addField(new SimpleTypeFieldMetadata(databaseUpdateReportType, false, false, false, "x_user_name", stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        databaseUpdateReportType.addField(new SimpleTypeFieldMetadata(databaseUpdateReportType, !isUUIDMode, false, true, "x_source", stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        databaseUpdateReportType.addField(new SimpleTypeFieldMetadata(databaseUpdateReportType, !isUUIDMode, false, true, "x_time_in_millis", longType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        databaseUpdateReportType.addField(new SimpleTypeFieldMetadata(databaseUpdateReportType, isUUIDMode, false, true, "x_uuid", stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        databaseUpdateReportType.addField(new SimpleTypeFieldMetadata(databaseUpdateReportType, false, false, false, "x_operation_type", stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        databaseUpdateReportType.addField(new SimpleTypeFieldMetadata(databaseUpdateReportType, false, false, false, "x_revision_id", stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        databaseUpdateReportType.addField(new SimpleTypeFieldMetadata(databaseUpdateReportType, false, false, false, "x_data_cluster", stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        databaseUpdateReportType.addField(new SimpleTypeFieldMetadata(databaseUpdateReportType, false, false, false, "x_data_model", stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        databaseUpdateReportType.addField(new SimpleTypeFieldMetadata(databaseUpdateReportType, false, false, false, "x_concept", stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        databaseUpdateReportType.addField(new SimpleTypeFieldMetadata(databaseUpdateReportType, false, false, false, "x_key", stringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        SimpleTypeFieldMetadata items_xml = new SimpleTypeFieldMetadata(databaseUpdateReportType, false, false, false, "x_items_xml", longStringType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY); //$NON-NLS-1$
        items_xml.getType().setData(TypeMapping.SQL_TYPE, preferClobUse ? TypeMapping.SQL_TYPE_CLOB: TypeMapping.SQL_TYPE_TEXT); 
        databaseUpdateReportType.addField(items_xml);
        DATABASE_UPDATE_REPORT_TYPE = (ComplexTypeMetadata) databaseUpdateReportType.freeze();
    }

    @Override
    public TypeMapping visit(ComplexTypeMetadata complexType) {
        if(!"Update".equals(complexType.getName())) { //$NON-NLS-1$
            throw new IllegalArgumentException("Expected a type 'Update' but got '" + complexType.getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return new UpdateReportTypeMapping(USER_UPDATE_REPORT_TYPE, DATABASE_UPDATE_REPORT_TYPE, mappings, repository);
    }
}
