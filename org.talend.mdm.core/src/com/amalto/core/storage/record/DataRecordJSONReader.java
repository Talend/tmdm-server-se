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

package com.amalto.core.storage.record;

import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class DataRecordJSONReader implements DataRecordReader<JsonElement> {

    private static final Logger LOGGER = Logger.getLogger(DataRecordJSONReader.class);

    private final String JSON_REF = "$ref"; //$NON-NLS-1$

    private final String MDM_TYPE = "-tmdm:type"; //$NON-NLS-1$

    private final String MDM_NAMESPACE = "-xmlns:tmdm"; //$NON-NLS-1$

    private final String REERENCE_VALUE = "#text"; //$NON-NLS-1$

    private JsonElement rootElement = null;

    private String entityName = null;

    private static Set<String> tagNames = null;

    public DataRecordJSONReader() {
    }

    public DataRecord read(MetadataRepository repository, ComplexTypeMetadata type, JsonElement element) {
        DataRecordMetadata metadata = new DataRecordMetadataImpl();
        DataRecord dataRecord = new DataRecord(type, metadata);
        entityName = type.getName();
        rootElement = (JsonElement) element.getAsJsonObject().get(type.getName());
        tagNames = new HashSet<>();
        readElement(repository, dataRecord, type, rootElement);
        // Process fields that are links to other field values.
        ComplexTypeMetadata dataRecordType = dataRecord.getType();
        Collection<FieldMetadata> fields = dataRecordType.getFields();
        for (FieldMetadata field : fields) {
            if (field.getData(ClassRepository.LINK) != null) {
                dataRecord.set(field, dataRecord.get(field.<String>getData(ClassRepository.LINK)));
            }
        }
        return dataRecord;
    }

    /**
     * The xsi:type in XML:
     * <pre>
     *    {@code
     *    <Person><PersonId>33</PersonId><Name>person-name-322aa3</Name>
     *        <Address xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="USAddress">
     *        <zip>10221</zip><Line1>usa-new</Line1></Address>
     *    </Person>
     *
     *    The expected JSON input:
     *    {
     *        "Person": {
     *            "PersonId": "33",
     *            "Name": "person-name-322aa3",
     *            "Address": {
     *                "$ref": "USAddress",
     *                "zip": "102221",
     *                "Line1": "usa-new"
     *            }
     *        }
     *    }
     * </pre>
     */
    private String getContainedValue(String typeName) {
        try {
            if (null == rootElement.getAsJsonObject().get(typeName)) {
                return StringUtils.EMPTY;
            }
            JsonObject root = rootElement.getAsJsonObject().get(typeName).getAsJsonObject();
            for (Iterator<Entry<String, JsonElement>> iterator = root.entrySet().iterator(); iterator.hasNext(); ) {
                Entry<String, JsonElement> entry = iterator.next();
                String tagName = entry.getKey();
                JsonElement currentElement = entry.getValue();
                if (tagName.equalsIgnoreCase(JSON_REF)) {
                    String refName = currentElement.getAsString();
                    if (StringUtils.isNotEmpty(refName)) {
                        return refName;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get JSON contained value.", e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        }
        return StringUtils.EMPTY;
    }

    /**
    * <pre>
    *    {
    *        "Product": {
    *            "Id": "231035933",
    *            "Name": "Talend Dog T-Shirt",
    *            "Family": "[3]",
    *            "Stores": {
    *                "Store": [
    *                    "[TalendUS]",
    *                    "[TalendFR]"
    *                ]
    *            }
    *        }
    *    }
    * </pre>
    *    
    * As above Json input, the root element is Product, fields Id, Name and Family are JsonPrimitive.
    * Field Stores is ComplexTypeMetadata and JsonObject, and filed Store is JsonArray.
    * Field Family and each Store are ReferenceFieldMetadata, 
    *    
    */
    private void readElement(MetadataRepository repository, DataRecord dataRecord, ComplexTypeMetadata type, JsonElement element) {
        JsonObject root = element.getAsJsonObject();
        for (Iterator<Entry<String, JsonElement>> iterator = root.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<String, JsonElement> entry = iterator.next();
            String tagName = entry.getKey();
            tagNames.add(tagName);
            JsonElement currentChild = entry.getValue();
            if (currentChild instanceof JsonObject) {
                if (!type.hasField(tagName)) {
                    throwNotOwnedField(tagName);
                }
                readJsonObject(repository, dataRecord, type, (JsonObject)currentChild, tagName);
            } else if (currentChild instanceof JsonPrimitive) {
                readJsonPrimitive(repository, dataRecord, type, (JsonPrimitive)currentChild, tagName);
            } else if (currentChild instanceof JsonArray) {
                int size = ((JsonArray) currentChild).size();
                for (int i=0; i<size; i++) {
                    JsonElement childObject = ((JsonArray) currentChild).get(i);
                    if (childObject instanceof JsonPrimitive) {
                        readJsonPrimitive(repository, dataRecord, type, (JsonPrimitive)childObject, tagName);
                    } else if (childObject instanceof JsonObject) {
                        if (!type.hasField(tagName)) {
                            throwNotOwnedField(tagName);
                        }
                        readJsonObject(repository, dataRecord, type, (JsonObject)childObject, tagName);
                    }
                }
            }
        }
    }

    private void throwNotOwnedField(String tagName) {
        throw new IllegalArgumentException("Entity '" + entityName + "' doesn't own field '" + tagName + "'."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private void readJsonObject(MetadataRepository repository, DataRecord dataRecord, ComplexTypeMetadata type, JsonObject currentChild, String tagName) {
        FieldMetadata field = type.getField(tagName);
        String containedValue = getContainedValue(tagName);
        if (field.getType() instanceof ContainedComplexTypeMetadata) {
            ComplexTypeMetadata containedType = (ComplexTypeMetadata) field.getType();
            for (ComplexTypeMetadata subType : containedType.getSubTypes()) {
                if (StringUtils.isNotEmpty(containedValue) && subType.getName().equals(containedValue)) {
                    containedType = subType;
                    break;
                }
            }
            DataRecord containedRecord = new DataRecord(containedType, UnsupportedDataRecordMetadata.INSTANCE);
            dataRecord.set(field, containedRecord);
            readElement(repository, containedRecord, containedType, currentChild);
        } else if (field instanceof ReferenceFieldMetadata) {
            ComplexTypeMetadata actualType = ((ReferenceFieldMetadata) field).getReferencedType();
            String referencedType = null;
            String referenceValue = null;
            List<String> referencedTypeAndValue = getReferencedTypeAndValue(tagName);
            if (null != referencedTypeAndValue) {
                referencedType = referencedTypeAndValue.get(0);
                referenceValue = referencedTypeAndValue.get(1);
                if (!referencedType.isEmpty()) {
                    actualType = repository.getComplexType(referencedType);
                }
            }
            if (actualType == null) {
                throw new IllegalArgumentException("Type '" + referencedType + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            dataRecord.set(field, StorageMetadataUtils.convert(referenceValue, field, actualType));
        } else {
            readElement(repository, dataRecord, type, currentChild);
        }
    }

    /**
     *    The mdm:type in XML:
     * <pre>
     *     {@code
     *     <PartyProduct><id>PartyProduct1</id><name>PartyProduct1</name>
     *         <supplier xmlns:tmdm='http://www.talend.com/mdm' tmdm:type='PartyIndividual'>[PartyIndividual1]</supplier>
     *     </PartyProduct>
     *   
     *    The expected JSON input:
     *    {
     *        "PartyProduct": {
     *            "id": "PartyProduct1",
     *            "name": "PartyProduct1",
     *            "supplier": {
     *                "-xmlns:tmdm": "http://www.talend.com/mdm",
     *                "-tmdm:type": "PartyCompany",
     *                "#text": "[PartyCompany1]"
     *            }
     *        }
     *    }
     * </pre>
     * 
     */
    private List<String> getReferencedTypeAndValue(String typeName) {
        try {
            if (null == rootElement.getAsJsonObject().get(typeName)) {
                return null;
            }
            List<String> referencedTypeAndValue = new ArrayList<>();
            JsonObject root = rootElement.getAsJsonObject().get(typeName).getAsJsonObject();
            for (Iterator<Entry<String, JsonElement>> iterator = root.entrySet().iterator(); iterator.hasNext(); ) {
                Entry<String, JsonElement> entry = iterator.next();
                String tagName = entry.getKey();
                JsonElement currentElement = entry.getValue();
                if (tagName.equalsIgnoreCase(MDM_TYPE)) {
                    String refType = currentElement.getAsString();
                    if (StringUtils.isNotEmpty(refType)) {
                        referencedTypeAndValue.add(refType);
                    }
                } else if (tagName.equalsIgnoreCase(REERENCE_VALUE)) {
                    String refValue = currentElement.getAsString();
                    if (StringUtils.isNotEmpty(refValue)) {
                        referencedTypeAndValue.add(refValue);
                    }
                }
            }
            return referencedTypeAndValue.size() > 0 ? referencedTypeAndValue : null;
        } catch (Exception e) {
            LOGGER.warn("Failed to get JSON referenced type and value.", e); //$NON-NLS-1$
            return null;
        }
    }
    
    private void readJsonPrimitive(MetadataRepository repository, DataRecord dataRecord, ComplexTypeMetadata type, JsonPrimitive currentChild, String tagName) {
        if (tagName.equalsIgnoreCase(JSON_REF) || 
            tagName.equalsIgnoreCase(MDM_TYPE) ||
            tagName.equalsIgnoreCase(MDM_NAMESPACE) ||            
            tagName.equalsIgnoreCase(REERENCE_VALUE)) {
            return;
        }

        String nodeValue = currentChild.getAsString();
        if (type.hasField(tagName)) {
            FieldMetadata field = type.getField(tagName);
            if (field instanceof ReferenceFieldMetadata) {
                ComplexTypeMetadata actualType = ((ReferenceFieldMetadata) field).getReferencedType();
                dataRecord.set(field, StorageMetadataUtils.convert(nodeValue, field, actualType));
            } else {
                dataRecord.set(field, StorageMetadataUtils.convert(nodeValue, field, type));
            }
        } else {
            throwNotOwnedField(tagName);
        }
    }

    public static Set<String> getValidTagNames() {
        return tagNames;
    }
}