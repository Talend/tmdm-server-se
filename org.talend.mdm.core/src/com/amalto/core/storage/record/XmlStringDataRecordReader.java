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

import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;

import com.amalto.core.load.io.ResettableStringWriter;
import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.UserQueryHelper;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;
import com.amalto.core.storage.record.metadata.DataRecordMetadataHelper;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;

public class XmlStringDataRecordReader implements DataRecordReader<String> {

    private String storageName;

    public DataRecord read(MetadataRepository repository, ComplexTypeMetadata type, String input) {
        return read(repository, type, input, false);
    }
    
    public DataRecord read(MetadataRepository repository, ComplexTypeMetadata type, String input, boolean includeFK) {
        if (type == null) {
            throw new IllegalArgumentException("Type can not be null"); //$NON-NLS-1$
        }
        if (input == null) {
            throw new IllegalArgumentException("Input can not be null"); //$NON-NLS-1$
        }

        FieldMetadata field = null;
        String firstWrongElementName = null;
        try {
            XMLEventReader xmlEventReader = MDMXMLUtils.createXMLEventReader(new StringReader(input));
            ResettableStringWriter xmlAccumulator = new ResettableStringWriter();
            int skipLevel = Integer.MAX_VALUE;
            int xmlAccumulatorLevel = 0;
            Stack<TypeMetadata> currentType = new Stack<TypeMetadata>();
            currentType.push(type);

            long lastModificationTime = 0;
            String taskId = null;

            // TODO To refactor (not really extensible)
            int level = 0;
            boolean hasMetUserElement = false;
            boolean isReadingTimestamp = false;
            boolean isReadingTaskId = false;
            boolean containsMetadata = false;
            while (!hasMetUserElement && xmlEventReader.hasNext()) {
                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    if (!hasMetUserElement) {
                        if (level == 0 && "ii".equals(startElement.getName().getLocalPart())) { //$NON-NLS-1$
                            containsMetadata = true;
                        } else if ("t".equals(startElement.getName().getLocalPart())) { //$NON-NLS-1$
                            isReadingTimestamp = true;
                        } else if ("taskId".equals(startElement.getName().getLocalPart())) {  //$NON-NLS-1$
                            isReadingTaskId = true;
                        }
                    }
                    if ((containsMetadata && level > 1) || (!containsMetadata && level == 0)
                            && startElement.getName().getLocalPart().equals(type.getName())) {
                        hasMetUserElement = true;
                    }
                    level++;
                } else if (event.isEndElement()) {
                    level--;
                } else if (event.isCharacters()) {
                    Characters characters = event.asCharacters();
                    if (isReadingTimestamp) {
                        try {
                            lastModificationTime = Long.parseLong(characters.getData().trim());
                        } catch (NumberFormatException e) {
                            throw new RuntimeException(e);
                        }
                        isReadingTimestamp = false;
                    } else if (isReadingTaskId) {
                        String value = characters.getData().trim();
                        taskId = value.isEmpty() ? null : value;
                        isReadingTaskId = false;
                    }
                }
            }

            DataRecordMetadata metadata = new DataRecordMetadataImpl(lastModificationTime, taskId);
            DataRecord dataRecord = new DataRecord(type, metadata);

            Stack<DataRecord> dataRecords = new Stack<DataRecord>();
            dataRecords.push(dataRecord);
            int userXmlPayloadLevel = level;
            String currentElementName = null;
            boolean isMetadataField = false;
            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    if (level >= userXmlPayloadLevel) {
                        StartElement startElement = xmlEvent.asStartElement();
                        currentElementName = startElement.getName().getLocalPart();
                        isMetadataField = METADATA_NAMESPACE.equals(startElement.getName().getNamespaceURI());
                        if(isMetadataField) {
                            skipLevel = level;
                        }
                        TypeMetadata typeMetadata = currentType.peek();
                        if (!(typeMetadata instanceof ComplexTypeMetadata)) {
                            throw new IllegalStateException("Expected a complex type but got a " + typeMetadata.getClass().getName()); //$NON-NLS-1$
                        }
                        if (level < skipLevel) {
                            if (!((ComplexTypeMetadata) typeMetadata).hasField(currentElementName)) {
                                if (firstWrongElementName == null) {
                                    firstWrongElementName = currentElementName;
                                }
                                skipLevel = level;
                                continue;
                            }
                            field = ((ComplexTypeMetadata) typeMetadata).getField(currentElementName);
                            if (field == null) {
                                throw new IllegalArgumentException("Type '" + typeMetadata.getName() + "' does not own field '" + currentElementName + "'."); //$NON-NLS-2$ //$NON-NLS-3$
                            }
                            TypeMetadata fieldType = field.getType();
                            if (ClassRepository.EMBEDDED_XML.equals(fieldType.getName())) {
                                xmlAccumulatorLevel = level;
                            }
                            if (xmlAccumulatorLevel > userXmlPayloadLevel) {
                                xmlAccumulator.append('<').append(currentElementName).append('>');
                            } else {
                                // Reads MDM type attribute for actual FK type
                                if (field instanceof ReferenceFieldMetadata) {
                                    Attribute actualType = startElement.getAttributeByName(new QName(SkipAttributeDocumentBuilder.TALEND_NAMESPACE, "type")); //$NON-NLS-1$
                                    if (actualType != null) {
                                        fieldType = repository.getComplexType(actualType.getValue());
                                    } else {
                                        fieldType = ((ReferenceFieldMetadata) field).getReferencedType();
                                    }
                                } else if (field instanceof ContainedTypeFieldMetadata) { // Reads xsi:type for actual contained type.
                                    Attribute actualType = startElement.getAttributeByName(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type")); //$NON-NLS-1$
                                    if (actualType != null) {
                                        if (!actualType.getValue().equals(fieldType.getName())) {
                                            for (ComplexTypeMetadata subType : ((ContainedComplexTypeMetadata) fieldType).getSubTypes()) {
                                                if (actualType.getValue().equals(subType.getName())) {
                                                    fieldType = subType;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    DataRecord containedDataRecord = new DataRecord((ComplexTypeMetadata) fieldType, UnsupportedDataRecordMetadata.INSTANCE);
                                    dataRecords.peek().set(field, containedDataRecord);
                                    dataRecords.push(containedDataRecord);
                                }
                                currentType.push(fieldType);
                            }
                        }
                    }
                    level++;
                } else if (xmlEvent.isCharacters()) {
                    if (level < skipLevel) {
                        if (xmlAccumulatorLevel > userXmlPayloadLevel) {
                            xmlAccumulator.append('<').append(xmlEvent.asCharacters().getData()).append('>');
                        } else if (level >= userXmlPayloadLevel && field != null) {
                            Object value;
                            String data = xmlEvent.asCharacters().getData();
                            try {
                                value = StorageMetadataUtils.convert(data, field, currentType.peek());
                                if (includeFK && field instanceof ReferenceFieldMetadata && value != null && this.storageName != null) {
                                    value = getReferenceFieldData(this.storageName, repository, (ReferenceFieldMetadata)field, data);
                                }
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Field '" + field.getName() + "' of type '" + field.getType().getName() + "' can not receive value '" + data + "'.", e);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            }
                            if (value != null) {
                                dataRecords.peek().set(field, value);
                            }
                        }
                    }
                    if(isMetadataField) {
                        DataRecordMetadataHelper.setMetadataValue(metadata, currentElementName, xmlEvent.asCharacters().getData());
                    }
                } else if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
                    String endElementLocalPart = endElement.getName().getLocalPart();
                    if (level == userXmlPayloadLevel && endElementLocalPart.equals(type.getName())) {
                        break;
                    }
                    if (level < skipLevel) {
                        if (!isMetadataField) {
                            if (xmlAccumulatorLevel > userXmlPayloadLevel) {
                                xmlAccumulator.append("</").append(xmlEvent.asEndElement().getName().getLocalPart()).append('>'); //$NON-NLS-1$
                                continue;
                            } else if (xmlAccumulatorLevel == level) {
                                if (field == null && firstWrongElementName != null) {
                                    throw new IllegalArgumentException("Entity '" + type.getName() + "' does not own field '" + firstWrongElementName + "'."); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
                                }
                                dataRecords.peek().set(field, xmlAccumulator.reset());
                                xmlAccumulatorLevel = 0;
                            } else if (currentType.peek() instanceof ComplexTypeMetadata && !(field instanceof ReferenceFieldMetadata)) {
                                dataRecords.pop();
                            }
                            field = null;
                            currentType.pop();
                        }
                    }
                    level--;
                    if (level == skipLevel) {
                        skipLevel = Integer.MAX_VALUE;
                    }
                    // Reset metadata parsing
                    isMetadataField = false;
                }
            }
            DataRecord createdDataRecord = dataRecords.pop();
            while (!dataRecords.isEmpty()) {
                createdDataRecord = dataRecords.pop();
            }
            return createdDataRecord;
        } catch (Exception e) {
            if (field == null && firstWrongElementName != null) {
                throw new IllegalArgumentException("Entity '" + type.getName() + "' does not own field '" + firstWrongElementName + "'."); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
            }
            throw new RuntimeException(e);
        }
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }
    
    public DataRecord getReferenceFieldData(String storageName, MetadataRepository repository, ReferenceFieldMetadata refField, String key) {
        List<String> ids = StorageMetadataUtils.getIds(key);
        if (ids.isEmpty() || ids.size() != refField.getReferencedType().getKeyFields().size()) {
            throw new IllegalArgumentException("Id '" + key + "' not matched with " + refField.getReferencedType().getName() + " key fields."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        List<IWhereItem> whereItemList = new ArrayList<IWhereItem>();
        int i = 0;
        for (FieldMetadata fm : refField.getReferencedType().getKeyFields()) {
            whereItemList.add(new WhereCondition(refField.getReferencedType().getName() + "/" + fm.getName(), WhereCondition.EQUALS, ids.get(i), WhereCondition.NO_OPERATOR)); //$NON-NLS-1$
            i++;
        }
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage storage = storageAdmin.get(storageName, StorageType.STAGING);
        if (storage == null) {
            throw new IllegalArgumentException("Container '" + storageName + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        IWhereItem whereItems = new WhereAnd(whereItemList);
        UserQueryBuilder userQuery = from(refField.getReferencedType());
        userQuery = userQuery.where(UserQueryHelper.buildCondition(userQuery, whereItems, repository));
        StorageResults records = storage.fetch(userQuery.getSelect());
        return records.iterator().next();
    }
}
