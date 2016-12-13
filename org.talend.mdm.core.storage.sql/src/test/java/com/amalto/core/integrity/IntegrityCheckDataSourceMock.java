/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.integrity;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.commmon.metadata.*;
import com.amalto.core.util.XtentisException;

/**
 * Test implementation of {@link FKIntegrityCheckDataSource} for unit test purposes.
 */
class IntegrityCheckDataSourceMock implements FKIntegrityCheckDataSource {

    private final MetadataRepository repository;

    private boolean hasMetConflict;

    public IntegrityCheckDataSourceMock(MetadataRepository repository) {
        this.repository = repository;
    }

    public String getDataModel(String clusterName, String concept, String[] ids) throws XtentisException {
        return "DataModel";
    }

    public long countInboundReferences(String clusterName, String[] ids, String fromTypeName, ReferenceFieldMetadata fromReference)
            throws XtentisException {
        return 1;
    }

    public Set<ReferenceFieldMetadata> getForeignKeyList(String concept, String dataModel) throws XtentisException {
        TypeMetadata type = repository.getType(concept);
        if (type != null) {
            return repository.accept(new InboundReferences(type));
        } else {
            return Collections.emptySet();
        }
    }

    public void resolvedConflict(Map<FKIntegrityCheckResult, Set<FieldMetadata>> checkResultToFields, FKIntegrityCheckResult conflictResolution) {
        hasMetConflict = true;
    }

    public boolean hasMetConflict() {
        return hasMetConflict;
    }
}
