/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.record;

import com.amalto.core.load.io.ResettableStringWriter;
import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;
import com.amalto.core.storage.record.metadata.DataRecordMetadataHelper;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import org.talend.mdm.commmon.metadata.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.*;
import java.io.StringReader;
import java.util.Stack;

public class XmlStringDataRecordReader implements DataRecordReader<String> {

    private static final XMLInputFactory xmlInputFactory;

    static {
        /*
         * FIXME The newInstance() is deprecated and the newFactory() method should be used instead. However since no
         * changes in behavior are defined by this replacement method, keep deprecated method to ensure there's no
         * classloading issues for now (see TMDM-3604).
         */
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
    }

    public DataRecord read(MetadataRepository repository, ComplexTypeMetadata type, String input) {
        if (type == null) {
            throw new IllegalArgumentException("Type can not be null");
        }
        if (input == null) {
            throw new IllegalArgumentException("Input can not be null");
        }

        FieldMetadata field = null;
        String firstWrongElementName = null;
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(input));
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
                            throw new IllegalStateException("Expected a complex type but got a " + typeMetadata.getClass().getName());
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
                                throw new IllegalArgumentException("Type '" + typeMetadata.getName() + "' does not own field '" + currentElementName + "'.");
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
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Field '" + field.getName() + "' of type '" + field.getType().getName() + "' can not receive value '" + data + "'.", e);
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
}
