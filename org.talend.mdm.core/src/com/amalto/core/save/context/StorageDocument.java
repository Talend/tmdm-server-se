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

package com.amalto.core.save.context;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.collections.map.LRUMap;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.w3c.dom.Document;

import com.amalto.core.history.DeleteType;
import com.amalto.core.history.DocumentTransformer;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.accessor.record.DataRecordAccessor;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordIncludeNullValueXmlWriter;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.storage.record.DataRecordXmlWriter;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;

public class StorageDocument implements MutableDocument {

    private final String dataModelName;

    private final MetadataRepository repository;

    private final Map<String, Accessor> accessorCache = new LRUMap(20);

    private DataRecord dataRecord;

    private String taskId;

    private DataRecord previousDataRecord;

    private int refDeep = 0;

    private boolean isDeleted = false;

    public StorageDocument(String dataModelName, MetadataRepository repository, DataRecord dataRecord) {
        this.dataModelName = dataModelName;
        this.repository = repository;
        this.dataRecord = dataRecord;
        this.taskId = dataRecord.getRecordMetadata().getTaskId();
    }

    @Override
    public Accessor createAccessor(String path) {
        Accessor accessor = accessorCache.get(path);
        if (accessor == null) {
            accessor = new DataRecordAccessor(repository, getDataRecord(), path);
            accessorCache.put(path, accessor);
        }
        return accessor;
    }

    @Override
    public Document asDOM() {
        synchronized (SaverContextFactory.DOCUMENT_BUILDER) {
            try {
                DocumentBuilder documentBuilder = SaverContextFactory.DOCUMENT_BUILDER;
                return documentBuilder.parse(new ByteArrayInputStream(exportToString().getBytes("UTF-8")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Document asValidationDOM() {
        synchronized (SaverContextFactory.DOCUMENT_BUILDER) {
            try {
                SkipAttributeDocumentBuilder builder = new SkipAttributeDocumentBuilder(SaverContextFactory.DOCUMENT_BUILDER, true);
                return builder.parse(new ByteArrayInputStream(exportToString().getBytes("UTF-8")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public MutableDocument create(MutableDocument content) {
        if (content instanceof StorageDocument) {
            if (isDeleted()) {
                recover(DeleteType.PHYSICAL);
            }
            dataRecord = ((StorageDocument) content).getDataRecord();
        } else {
            DataRecordReader<String> reader = new XmlStringDataRecordReader();
            dataRecord = reader.read(repository, dataRecord.getType(), content.exportToString());
        }
        accessorCache.clear();
        return this;
    }

    @Override
    public MutableDocument setContent(MutableDocument content) {
        XmlStringDataRecordReader reader = new XmlStringDataRecordReader();
        dataRecord = reader.read(repository, dataRecord.getType(), content.exportToString());
        accessorCache.clear();
        return this;
    }

    @Override
    public MutableDocument delete(DeleteType deleteType) {
        isDeleted = true;
        previousDataRecord = dataRecord;
        dataRecord = new DataRecord(dataRecord.getType(), new DataRecordMetadataImpl(System.currentTimeMillis(), null));
        return this;
    }

    @Override
    public MutableDocument recover(DeleteType deleteType) {
        dataRecord = previousDataRecord;
        isDeleted = false;
        return this;
    }

    @Override
    public com.amalto.core.history.Document applyChanges() {
        return this;
    }

    @Override
    public MutableDocument copy() {
        return new StorageDocument(dataModelName, repository, dataRecord);
    }

    @Override
    public void clean() {
        try {
            clean(dataRecord);
        } finally {
            accessorCache.clear();
        }
    }

    private DataRecord clean(DataRecord dataRecord) {
        ComplexTypeMetadata type = dataRecord.getType();
        for (FieldMetadata entityField : type.getFields()) {
            Object fieldData = dataRecord.get(entityField);
            if (fieldData == null) {
                dataRecord.remove(entityField);
            } else if (entityField.isMany()) {
                List list = (List) fieldData;
                Iterator iterator = list.iterator();
                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    if (entityField.getType() instanceof ContainedComplexTypeMetadata) {
                        if (clean((DataRecord) next) == null) {
                            iterator.remove();
                        }
                    } else if (next == null) {
                        iterator.remove();
                    }
                }
                if (list.isEmpty()) {
                    dataRecord.remove(entityField);
                }
            } else if (entityField.getType() instanceof ComplexTypeMetadata) {
                DataRecord cleanedDataRecord = clean((DataRecord) fieldData);
                if (cleanedDataRecord == null) {
                    dataRecord.remove(entityField);
                }
            }
        }
        if (dataRecord.getSetFields().isEmpty()) {
            return null;
        } else {
            return dataRecord;
        }
    }

    /**
     * Clean the second level reference child node due to the REST API only display the first reference ID.
     * Before:
     * <pre>
     *    Contrat
     *       |__AP-AA
     *       |   |__Anony_x0
     *       |         |__Anony_x2
     *       |              |__ EDA (Reference)
     *       |                   |__LocalUse (Reference)
     *       |__Anony_x3
     *            |__ EDP (Reference)
     *                 |__Book (Reference)
     * </pre>
     * After:
     * <pre>
     *    Contrat
     *       |__AP-AA
     *       |    |__Anony_x0
     *       |         |__Anony_x2
     *       |              |__ EDA (Reference)
     *       |__Anony_x3
     *            |__ EDP (Reference)
     * </pre>
     * @param dataRecord
     * @return
     */
    @SuppressWarnings("rawtypes")
    public DataRecord cleanMultiLevelRef(DataRecord dataRecord) {
        Set<FieldMetadata> cloneDataRecord = new HashSet<FieldMetadata>(dataRecord.getSetFields());
        for (FieldMetadata entityField : cloneDataRecord) {
            Object fieldData = dataRecord.get(entityField);
            if (fieldData == null) {
                continue;
            }
            if (entityField instanceof ReferenceFieldMetadata) {
                if (++refDeep >= 2) {
                    dataRecord.remove(entityField);
                    refDeep = 0;
                    continue;
                }
                if (fieldData instanceof DataRecord) {
                    cleanMultiLevelRef((DataRecord) fieldData);
                }
                refDeep = 0;
            } else if (entityField.isMany()) {
                for (Iterator it = ((List) fieldData).iterator(); it.hasNext();) {
                    Object next = it.next();
                    if (next != null && fieldData instanceof DataRecord) {
                        cleanMultiLevelRef((DataRecord) next);
                        refDeep = 0;
                    }
                }
            } else if (entityField.getType() instanceof ComplexTypeMetadata && fieldData instanceof DataRecord) {
                cleanMultiLevelRef((DataRecord) fieldData);
                refDeep = 0;
            }
        }
        return dataRecord;
    }

    @Override
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String exportToString() {
        StringWriter output = new StringWriter();
        DataRecordWriter writer = new DataRecordXmlWriter();
        try {
            writer.write(dataRecord, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toString();
    }

    @Override
    public String exportToStringWithNullFields() {
        StringWriter output = new StringWriter();
        DataRecordXmlWriter writer = new DataRecordIncludeNullValueXmlWriter();
        try {
            writer.write(dataRecord, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toString();
    }

    @Override
    public com.amalto.core.history.Document transform(DocumentTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public void restore() {
    }

    @Override
    public ComplexTypeMetadata getType() {
        return dataRecord.getType();
    }

    @Override
    public String getDataModel() {
        return dataModelName;
    }

    @Override
    public String getDataCluster() {
        return dataModelName;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public DeleteType getDeleteType() {
        return null;
    }

    public DataRecord getDataRecord() {
        return dataRecord;
    }

    @Override
    public boolean considerMissingElementsAsEmpty() {
        // documents coming from Storage are always complete
        return true;
    }
}