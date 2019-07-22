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

package com.amalto.core.history.accessor.record;

import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

import java.util.Collection;

class TypeValue implements Setter, Getter {

    static Setter SET = new TypeValue();

    static final Getter GET = new TypeValue();

    @Override
    public void set(MetadataRepository repository, DataRecord record, PathElement element, String value) {
        if (value == null) {
            return;
        }
        if (record != null) {
            if (element.field instanceof ReferenceFieldMetadata) {
                DataRecord dataRecord = (DataRecord) record.get(element.field);
                if (dataRecord == null) {
                    dataRecord = new DataRecord(((ReferenceFieldMetadata) element.field).getReferencedType(), UnsupportedDataRecordMetadata.INSTANCE);
                    record.set(element.field, dataRecord);
                    record = dataRecord;
                } else {
                    record = (DataRecord) record.get(element.field);
                }
            }
            if (!value.isEmpty()) {
                ComplexTypeMetadata type = record.getType();
                if (!value.equals(type.getName())) {
                    ComplexTypeMetadata newType = repository.getComplexType(value);
                    if (newType == null ) {
                        if(element.field instanceof ContainedTypeFieldMetadata){
                            newType = lookupSubField((ContainedTypeFieldMetadata)element.field, value);
                        }
                        if(newType == null) {
                            newType = (ComplexTypeMetadata) repository.getNonInstantiableType(StringUtils.EMPTY, value);
                        }
                    }
                    record.setType(newType);
                }
            }
        }
    }

    @Override
    public String get(DataRecord record, PathElement element) {
        if (element.field instanceof ReferenceFieldMetadata) {
            DataRecord dataRecord = (DataRecord) record.get(element.field);
            return dataRecord.getType().getName();
        } else {
            return record.getType().getName();
        }
    }

    /**
     * sub field of name with fieldName in containedField
     * @param containedField the entity need to lookup
     * @param fieldName  non instantiable field name
     * @return  sub field of name with fieldName in containedField
     */
    private ComplexTypeMetadata lookupSubField(ContainedTypeFieldMetadata containedField, String fieldName) {
        Collection<ComplexTypeMetadata> subFields = containedField.getContainedType().getSubTypes();
        for (ComplexTypeMetadata subField : subFields) {
            if (subField.getName().equals(fieldName)) {
                return subField;
            }
        }
        if (containedField.getType().getName().equals(fieldName)) {
            return containedField.getContainedType();
        }
        return null;
    }
}
