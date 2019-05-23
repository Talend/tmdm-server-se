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
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

import java.util.Collection;
import java.util.stream.Collectors;

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
                    if (newType == null) {
                        newType = lookupNonInstantiableFieldWithName(repository.getComplexType(element.field.getEntityTypeName()),
                                value);
                        if (newType == null) {
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
     * return the field of one Non Instantiable type in Data Model
     * <p>
     * for Below Data Model, if lookup the "JuniorSchool", returned type of JuniorSchool is subType in School,
     * which is one contained field type for Test.
     * <pre>
     * Entity:
     *   Test
     *     |__Id
     *     |__Name
     *     |__School (complex type)
     *
     * Complex Type:
     *    School
     *      |__Name
     *      |__Address
     *
     *    JuniorSchool:School
     *      |__Location
     *
     *    SeniorSchool::School
     *      |__Exam
     *  </pre>
     *
     * @param parentType the entity need to lookup
     * @param fieldName  non instantiable field name
     * @return non instantiable field
     */
    protected ComplexTypeMetadata lookupNonInstantiableFieldWithName(ComplexTypeMetadata parentType, String fieldName) {
        Collection<FieldMetadata> containedFieldColl = parentType.getFields().stream()
                .filter(fieldMetadata -> fieldMetadata instanceof ContainedTypeFieldMetadata).collect(Collectors.toList());
        for (FieldMetadata containedField : containedFieldColl) {
            Collection<ComplexTypeMetadata> subTypes = ((ContainedComplexTypeMetadata) containedField.getType())
                    .getContainedType().getSubTypes();
            for (ComplexTypeMetadata subType : subTypes) {
                if (subType.getName().equals(fieldName)) {
                    return subType;
                }
            }
            for (ComplexTypeMetadata subType : subTypes) {
                return lookupNonInstantiableFieldWithName(subType, fieldName);
            }
            return lookupNonInstantiableFieldWithName(
                    ((ContainedComplexTypeMetadata) containedField.getType()).getContainedType(), fieldName);
        }
        return null;
    }
}
