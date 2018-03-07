/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;

import com.amalto.core.storage.CloseableIterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.talend.mdm.commmon.metadata.AliasedFieldMetadata;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadataImpl;
import org.talend.mdm.commmon.metadata.CompoundFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.query.user.Alias;
import com.amalto.core.query.user.Count;
import com.amalto.core.query.user.Distinct;
import com.amalto.core.query.user.Field;
import com.amalto.core.query.user.Max;
import com.amalto.core.query.user.Min;
import com.amalto.core.query.user.StringConstant;
import com.amalto.core.query.user.Type;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.VisitorAdapter;
import com.amalto.core.query.user.metadata.GroupSize;
import com.amalto.core.query.user.metadata.StagingBlockKey;
import com.amalto.core.query.user.metadata.StagingError;
import com.amalto.core.query.user.metadata.StagingSource;
import com.amalto.core.query.user.metadata.StagingStatus;
import com.amalto.core.query.user.metadata.TaskId;
import com.amalto.core.query.user.metadata.Timestamp;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;

class ProjectionIterator implements CloseableIterator<DataRecord> {

    private static final Logger LOGGER = Logger.getLogger(ProjectionIterator.class);

    private final CloseableIterator<Object> iterator;

    private final List<TypedExpression> selectedFields;

    private final Set<ResultsCallback> callbacks;

    private final MappingRepository mappingMetadataRepository;

    private boolean firstNextCall = true;

    private boolean isClosed;

    public ProjectionIterator(MappingRepository mappingMetadataRepository, CloseableIterator<Object> iterator,
            List<TypedExpression> selectedFields, Set<ResultsCallback> callbacks) {
        this.iterator = iterator;
        this.selectedFields = selectedFields;
        this.callbacks = callbacks;
        this.mappingMetadataRepository = mappingMetadataRepository;
    }

    public ProjectionIterator(MappingRepository mappingMetadataRepository, final ScrollableResults results,
            List<TypedExpression> selectedFields, final Set<ResultsCallback> callbacks) {
        this(mappingMetadataRepository, new CloseableIterator<Object>() {

            private boolean hasNext;

            private boolean isClosed = false;

            private boolean consumedResult = true;

            @Override
            public boolean hasNext() {
                if (!consumedResult) {
                    return hasNext;
                }
                try {
                    hasNext = results.next();
                    consumedResult = false;
                    return hasNext;
                } catch (Exception e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Unable to check for next result.", e); //$NON-NLS-1$
                    }
                    return false;
                }
            }

            @Override
            public Object next() {
                consumedResult = true;
                return results.get();
            }

            @Override
            public void remove() {
            }

            @Override
            public void close() throws IOException {
                if (!isClosed) {
                    try {
                        results.close();
                    } finally {
                        isClosed = true;
                    }
                }
            }
        }, selectedFields, callbacks);
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = iterator.hasNext();
        if (!hasNext) {
            notifyCallbacks();
        }
        return hasNext;
    }

    private void notifyCallbacks() {
        if (!isClosed) {
            // TMDM-6712: Ensure all iterator resources are released.
            try {
                iterator.close();
            } catch (Throwable t) {
                LOGGER.error(t);
            }
            for (ResultsCallback callback : callbacks) {
                try {
                    callback.onEndOfResults();
                } catch (Throwable t) {
                    LOGGER.error(t);
                }
            }
            isClosed = true;
        }
    }

    @Override
    public DataRecord next() {
        if (firstNextCall) {
            for (ResultsCallback callback : callbacks) {
                callback.onBeginOfResults();
            }
            firstNextCall = false;
        }
        DataRecord record;
        try {
            final ComplexTypeMetadata explicitProjectionType = new ComplexTypeMetadataImpl(StringUtils.EMPTY,
                    Storage.PROJECTION_TYPE, false);
            record = new DataRecord(explicitProjectionType, UnsupportedDataRecordMetadata.INSTANCE);
            Object[] values;
            Object next = iterator.next();
            if (next instanceof Object[]) {
                values = (Object[]) next;
            } else {
                values = new Object[] { next };
            }
            ProjectionElementCreator projectionElementCreator = new ProjectionElementCreator(explicitProjectionType, values);
            List<ProjectionElement> elements = new LinkedList<ProjectionElement>();
            for (TypedExpression selectedField : selectedFields) {
                elements.add(selectedField.accept(projectionElementCreator));
            }
            for (ProjectionElement element : elements) {
                explicitProjectionType.addField(element.field);
                record.set(element.field, element.value);
            }
            explicitProjectionType.freeze();
        } catch (Exception e) {
            notifyCallbacks();
            throw new RuntimeException(e);
        }
        return record;
    }

    @Override
    public void remove() {
    }

    @Override
    public void close() throws IOException {
        notifyCallbacks();
    }

    private static class ProjectionElement {

        FieldMetadata field;

        Object value;
    }

    private class ProjectionElementCreator extends VisitorAdapter<ProjectionElement> {

        private final ComplexTypeMetadata explicitProjectionType;

        private final Object[] values;

        int currentIndex = 0;

        boolean isAlias;

        private ProjectionElement currentElement;

        public ProjectionElementCreator(ComplexTypeMetadata explicitProjectionType, Object[] values) {
            this.explicitProjectionType = explicitProjectionType;
            this.values = values;
            isAlias = false;
        }

        private void createElement(String typeName, String fieldName) {
            SimpleTypeMetadata fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, typeName);
            FieldMetadata field = new SimpleTypeFieldMetadata(explicitProjectionType, false, false, false, fieldName, fieldType,
                    Collections.<String> emptyList(), Collections.<String> emptyList(), Collections.<String> emptyList(),
                    StringUtils.EMPTY);
            currentElement = new ProjectionElement();
            currentElement.field = field;
        }
        
        private void createElement(String typeName, String fieldName, SimpleTypeFieldMetadata fieldMetadata) {
            SimpleTypeMetadata fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, typeName);
            FieldMetadata field = new SimpleTypeFieldMetadata(fieldMetadata.getContainingType(), false, false, false, fieldName, fieldType,
                    Collections.<String> emptyList(), Collections.<String> emptyList(), Collections.<String> emptyList(),
                    StringUtils.EMPTY);
            currentElement = new ProjectionElement();
            currentElement.field = field;
        }

        private void createElement(String typeName, String aliasName, FieldMetadata aliasedField) {
            SimpleTypeMetadata fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, typeName);
            FieldMetadata field = new AliasedFieldMetadata(explicitProjectionType, false, false, false, aliasName, fieldType,
                    Collections.<String> emptyList(), Collections.<String> emptyList(), aliasedField);
            currentElement = new ProjectionElement();
            currentElement.field = field;
        }

        private void createReferenceElement(ReferenceFieldMetadata fieldMetadata) {
            FieldMetadata field = new ReferenceFieldMetadata(fieldMetadata.getContainingType(), false, false, false,
                    fieldMetadata.getName(), fieldMetadata.getReferencedType(), fieldMetadata.getReferencedField(),
                    fieldMetadata.getForeignKeyInfoFields(), fieldMetadata.getForeignKeyInfoFormat(), false, false, new SimpleTypeMetadata(
                            XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING), Collections.<String> emptyList(),
                    Collections.<String> emptyList(), Collections.<String> emptyList(), StringUtils.EMPTY, StringUtils.EMPTY);
            currentElement = new ProjectionElement();
            currentElement.field = field;
        }

        @Override
        public ProjectionElement visit(Count count) {
            // Do nothing on field creation, count is expected to be nested in a com.amalto.core.query.user.Alias.
            currentElement.value = values[currentIndex++];
            return null;
        }

        @Override
        public ProjectionElement visit(Max max) {
            // Do nothing on field creation, max is expected to be nested in a com.amalto.core.query.user.Alias.
            currentElement.value = values[currentIndex++];
            return null;
        }

        @Override
        public ProjectionElement visit(Min min) {
            // Do nothing on field creation, min is expected to be nested in a com.amalto.core.query.user.Alias.
            currentElement.value = values[currentIndex++];
            return null;
        }

        @Override
        public ProjectionElement visit(Alias alias) {
            isAlias = true;
            if (alias.getTypedExpression() instanceof Field) {
                Field fieldExpression = (Field) alias.getTypedExpression();
                createElement(alias.getTypeName(), alias.getAliasName(), fieldExpression.getFieldMetadata());
            } else {
                createElement(alias.getTypeName(), alias.getAliasName());
            }
            alias.getTypedExpression().accept(this);
            isAlias = false;
            return currentElement;
        }

        @Override
        public ProjectionElement visit(Distinct distinct) {
            currentElement.value = values[currentIndex++];
            return currentElement;
        }

        @Override
        public ProjectionElement visit(Type type) {
            Object value = values[currentIndex++];
            if (value != null) {
                String typeName = value.toString();
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (!(contextClassLoader instanceof StorageClassLoader)) {
                    throw new IllegalStateException("Expected a instance of " + StorageClassLoader.class.getName() //$NON-NLS-1$
                            + " as current class loader."); //$NON-NLS-1$
                }
                try {
                    Class<?> aClass = contextClassLoader.loadClass(ClassCreator.getClassName(typeName));
                    ComplexTypeMetadata typeFromClass = ((StorageClassLoader) contextClassLoader).getTypeFromClass(aClass);
                    value = mappingMetadataRepository.getMappingFromDatabase(typeFromClass).getUser().getName();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Exception occurred during type name conversion.", e); //$NON-NLS-1$
                }
            }
            currentElement.value = value;
            return currentElement;
        }

        @Override
        public ProjectionElement visit(StringConstant constant) {
            currentElement.value = values[currentIndex++];
            return currentElement;
        }

        @Override
        public ProjectionElement visit(Timestamp timestamp) {
            if (!isAlias) {
                createElement(timestamp.getTypeName(), "metadata:" + Timestamp.TIMESTAMP_ALIAS); //$NON-NLS-1$
            }
            currentElement.value = values[currentIndex++];
            return currentElement;
        }

        @Override
        public ProjectionElement visit(TaskId taskId) {
            if (!isAlias) {
                createElement(taskId.getTypeName(), "metadata:" + TaskId.TASK_ID_ALIAS); //$NON-NLS-1$
            }
            currentElement.value = values[currentIndex++];
            return currentElement;
        }

        @Override
        public ProjectionElement visit(GroupSize groupSize) {
            if (!isAlias) {
                createElement(groupSize.getTypeName(), "metadata:" + GroupSize.GROUP_SIZE_ALIAS); //$NON-NLS-1$
            }
            currentElement.value = values[currentIndex++];
            return currentElement;
        }

        @Override
        public ProjectionElement visit(Field field) {
            FieldMetadata fieldMetadata = field.getFieldMetadata();
            if (!isAlias) {
                if (fieldMetadata instanceof ReferenceFieldMetadata) {
                    FieldMetadata referencedField = ((ReferenceFieldMetadata)mappingMetadataRepository.getMappingFromUser(fieldMetadata.getContainingType()).getDatabase(fieldMetadata)).getReferencedField();
                    if (referencedField instanceof CompoundFieldMetadata) {
                        ((ReferenceFieldMetadata)fieldMetadata).setReferencedField(referencedField);
                    }
                    createReferenceElement((ReferenceFieldMetadata) fieldMetadata);
                } else {
                    TypeMetadata type = fieldMetadata.getType();
                    if (fieldMetadata instanceof SimpleTypeFieldMetadata) {
                        type = MetadataUtils.getSuperConcreteType(type);
                        createElement(type.getName(), fieldMetadata.getName(), (SimpleTypeFieldMetadata)fieldMetadata);
                    } else {
                        createElement(type.getName(), fieldMetadata.getName());
                    }
                }
            }
            if (fieldMetadata instanceof ReferenceFieldMetadata
                    && ((ReferenceFieldMetadata) fieldMetadata).getReferencedField() instanceof CompoundFieldMetadata) {
                FieldMetadata referencedField = ((ReferenceFieldMetadata) fieldMetadata).getReferencedField();
                int length = ((CompoundFieldMetadata) referencedField).getFields().length;
                Object[] fieldValues = new Object[length];
                System.arraycopy(values, currentIndex, fieldValues, 0, length);
                // Only include composite FK value if there's an actual key value.
                currentElement.value = isNullValue(fieldValues) ? null : fieldValues;
                currentIndex += length;
            } else if (fieldMetadata.getType().getData(TypeMapping.SQL_TYPE) != null
                    && TypeMapping.SQL_TYPE_CLOB.equals(fieldMetadata.getType().getData(TypeMapping.SQL_TYPE))) {
                try {
                    Object clobValue = values[currentIndex++];
                    if(clobValue != null) {
                        Reader characterStream = ((Clob)clobValue ).getCharacterStream();
                        currentElement.value = new String(IOUtils.toCharArray(characterStream));
                    }
                } catch (Exception e) {
                    currentElement.value = ""; //$NON-NLS-1$
                    throw new RuntimeException("Unexpected read from clob exception", e); //$NON-NLS-1$
                }
            } else {
                currentElement.value = values[currentIndex++];
            }
            return currentElement;
        }

        private boolean isNullValue(Object[] fieldValues) {
            if (fieldValues == null) {
                return true;
            }
            for (Object o : fieldValues) {
                if (o != null) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ProjectionElement visit(StagingStatus stagingStatus) {
            if (!isAlias) {
                createElement(stagingStatus.getTypeName(), "metadata:" + StagingStatus.STAGING_STATUS_ALIAS); //$NON-NLS-1$
            }
            currentElement.value = values[currentIndex++];
            return currentElement;
        }

        @Override
        public ProjectionElement visit(StagingError stagingError) {
            if (!isAlias) {
                createElement(stagingError.getTypeName(), "metadata:" + StagingError.STAGING_ERROR_ALIAS); //$NON-NLS-1$
            }
            currentElement.value = values[currentIndex++];
            return currentElement;
        }

        @Override
        public ProjectionElement visit(StagingSource stagingSource) {
            if (!isAlias) {
                createElement(stagingSource.getTypeName(), "metadata:" + StagingSource.STAGING_SOURCE_ALIAS); //$NON-NLS-1$
            }
            currentElement.value = values[currentIndex++];
            return currentElement;
        }

        @Override
        public ProjectionElement visit(StagingBlockKey stagingBlockKey) {
            if (!isAlias) {
                createElement(stagingBlockKey.getTypeName(), "metadata:" + StagingBlockKey.STAGING_BLOCK_ALIAS); //$NON-NLS-1$
            }
            currentElement.value = values[currentIndex++];
            return currentElement;
        }
    }
}
