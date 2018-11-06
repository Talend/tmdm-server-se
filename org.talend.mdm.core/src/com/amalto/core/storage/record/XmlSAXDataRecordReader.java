/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.record;

import java.util.Stack;

import javax.xml.XMLConstants;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.amalto.core.load.io.ResettableStringWriter;
import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.record.metadata.DataRecordMetadataHelper;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;

public class XmlSAXDataRecordReader implements DataRecordReader<XmlSAXDataRecordReader.Input> {

    public static class Input {

        private final XMLReader reader;

        private final InputSource input;

        public Input(XMLReader reader, InputSource input) {
            this.reader = reader;
            this.input = input;
        }
    }

    @Override
    public DataRecord read(MetadataRepository repository, ComplexTypeMetadata type, Input input) {
        try {
            InputSource inputSource = input.input;
            XMLReader xmlReader = input.reader;
            xmlReader.setFeature(MDMXMLUtils.FEATURE_DISALLOW_DOCTYPE, true);
            xmlReader.setFeature(MDMXMLUtils.FEATURE_LOAD_EXTERNAL, false);
            xmlReader.setFeature(MDMXMLUtils.FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
            xmlReader.setFeature(MDMXMLUtils.FEATURE_EXTERNAL_PARAM_ENTITIES, false);
            DataRecordContentHandler handler = new DataRecordContentHandler(type, repository);
            xmlReader.setContentHandler(handler);
            xmlReader.parse(inputSource);
            return handler.getDataRecord();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static class DataRecordContentHandler implements ContentHandler {

        private final Stack<TypeMetadata> currentType = new Stack<TypeMetadata>();

        private final Stack<DataRecord> dataRecordStack = new Stack<DataRecord>();

        private final Stack<FieldMetadata> currentField = new Stack<FieldMetadata>();

        private final ResettableStringWriter charactersBuffer = new ResettableStringWriter();

        private final ComplexTypeMetadata mainType;

        private final MetadataRepository repository;

        private boolean hasMetUserElement = false;

        private boolean isReadingTimestamp = false;

        private boolean isReadingTaskId = false;

        private final DataRecord dataRecord;

        private int accumulateXml = 0;

        private boolean isMetadata = false;

        private String metadataField;

        public DataRecordContentHandler(ComplexTypeMetadata type, MetadataRepository repository) {
            mainType = type;
            this.repository = repository;
            dataRecord = new DataRecord(type, new DataRecordMetadataImpl(0, null));
            dataRecordStack.push(dataRecord);
            hasMetUserElement = false;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!hasMetUserElement) {
                if (mainType.getName().equals(localName)) {
                    hasMetUserElement = true;
                    currentType.push(mainType);
                } else {
                    if ("t".equals(localName)) { //$NON-NLS-1$
                        isReadingTimestamp = true;
                    } else if ("taskId".equals(localName)) { //$NON-NLS-1$
                        isReadingTaskId = true;
                    }
                }
            } else {
                if (accumulateXml > 0) {
                    charactersBuffer.append('<').append(localName).append('>');
                    if (localName.equals(currentField.peek().getName())) {
                        accumulateXml++;
                    }
                    return;
                }
                if (METADATA_NAMESPACE.equals(uri)) {
                    metadataField = localName;
                    isMetadata = true;
                } else {
                    FieldMetadata field = ((ComplexTypeMetadata) currentType.peek()).getField(localName);
                    if (field instanceof ReferenceFieldMetadata) {
                        ComplexTypeMetadata actualType = ((ReferenceFieldMetadata) field).getReferencedType();
                        String mdmType = attributes.getValue(SkipAttributeDocumentBuilder.TALEND_NAMESPACE, "type"); //$NON-NLS-1$
                        if (mdmType != null) {
                            actualType = repository.getComplexType(mdmType);
                        }
                        if (actualType == null) {
                            throw new IllegalArgumentException("Type '" + mdmType + "' does not exist in data model.");
                        }
                        currentType.push(actualType);
                    } else if (field instanceof ContainedTypeFieldMetadata) {
                        ComplexTypeMetadata actualType = (ComplexTypeMetadata) field.getType();
                        String xsiType = attributes.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type"); //$NON-NLS-1$
                        if (xsiType != null) {
                            for (ComplexTypeMetadata subType : actualType.getSubTypes()) {
                                if (subType.getName().equals(xsiType)) {
                                    actualType = subType;
                                    break;
                                }
                            }
                        }
                        DataRecord containedRecord = new DataRecord(actualType, UnsupportedDataRecordMetadata.INSTANCE);
                        dataRecordStack.peek().set(field, containedRecord);
                        dataRecordStack.push(containedRecord);
                        currentType.push(actualType);
                    } else {
                        TypeMetadata type = field.getType();
                        if (ClassRepository.EMBEDDED_XML.equals(type.getName())) {
                            accumulateXml = 1;
                        }
                        currentType.push(type);
                    }
                    currentField.push(field);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (accumulateXml > 0) {
                charactersBuffer.append("</").append(localName).append('>');
                if (localName.equals(currentField.peek().getName())) {
                    accumulateXml--;
                }
            }
            if (accumulateXml > 0) {
                return;
            }
            String value = charactersBuffer.reset();
            if (isMetadata) {
                DataRecordMetadataHelper.setMetadataValue(dataRecord.getRecordMetadata(), metadataField, value);
                isMetadata = false;
            } else if (hasMetUserElement && !currentField.isEmpty()) {
                FieldMetadata field = currentField.pop();
                TypeMetadata type = currentType.pop();
                if (!value.isEmpty()) {
                    dataRecordStack.peek().set(field, StorageMetadataUtils.convert(value, field, type));
                }
                if (field instanceof ContainedTypeFieldMetadata) {
                    dataRecordStack.pop();
                }
            } else {
                if (isReadingTimestamp) {
                    dataRecordStack.peek().getRecordMetadata().setLastModificationTime(Long.parseLong(value));
                    isReadingTimestamp = false;
                } else if (isReadingTaskId) {
                    dataRecordStack.peek().getRecordMetadata().setTaskId(value.isEmpty() ? null : value);
                    isReadingTaskId = false;
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            charactersBuffer.append(new String(ch, start, length).trim());
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
        }

        public DataRecord getDataRecord() {
            return dataRecord;
        }
    }
}
