/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordConverter;

public class ObjectDataRecordConverter implements DataRecordConverter<Object> {

    private static final Logger LOGGER = Logger.getLogger(ObjectDataRecordConverter.class);

    private final StorageClassLoader storageClassLoader;

    private final Session session;

    public ObjectDataRecordConverter(StorageClassLoader storageClassLoader, Session session) {
        this.storageClassLoader = storageClassLoader;
        this.session = session;
    }

    @Override
    public Object convert(DataRecord dataRecord, TypeMapping mapping) {
        if (dataRecord == null) {
            throw new IllegalArgumentException("Data record can not be null");
        }
        if (mapping == null) {
            throw new IllegalArgumentException("Mapping cannot be null");
        }
        try {
            Class<?> mainInstanceClass = storageClassLoader.findClass(mapping.getDatabase().getName());
            // Try to load existing instance (if any).
            Wrapper mainInstance = null;
            Serializable id = null;
            if(DataRecord.CheckExistence.get() || "AutoIncrement".equals(dataRecord.getType().getName())) {
                try {
                    Collection<FieldMetadata> keyFields = dataRecord.getType().getKeyFields();
                    if (keyFields.size() == 0) {
                        throw new IllegalArgumentException("Type '" + dataRecord.getType().getName()
                                + "' does not define any key field.");
                    } else if (keyFields.size() == 1) {
                        String keyFieldName = keyFields.iterator().next().getName();
                        id = (Serializable) dataRecord.get(keyFieldName);
                        if (id == null) {
                            throw new IllegalArgumentException("Instance of type '" + dataRecord.getType().getName()
                                    + "' does not have value for '" + keyFieldName + "'.");
                        }
                        mainInstance = (Wrapper) session.get(mainInstanceClass, id);
                    } else {
                        List<Object> compositeIdValues = new ArrayList<Object>(keyFields.size());
                        for (FieldMetadata keyField : keyFields) {
                            compositeIdValues
                                    .add(StorageMetadataUtils.convert(
                                            StorageMetadataUtils.toString(dataRecord.get(keyField), keyField),
                                            mapping.getDatabase(keyField)));
                        }
                        id = createCompositeId(storageClassLoader, mainInstanceClass, compositeIdValues);
                        mainInstance = (Wrapper) session.get(mainInstanceClass, id);
                    }
                } catch (ObjectNotFoundException e) {
                    if (id != null && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Could not find instance with id '" + id + "' in current session. Consider it as a creation.");
                    }
                    // Ignored (means a new instance is required).
                }
            }
            boolean needNewInstance = mainInstance == null;
            // Instance does not exist, so create it.
            if (needNewInstance) {
                mainInstance = (Wrapper) mainInstanceClass.newInstance();
            }
            mapping.setValues(session, dataRecord, mainInstance);
            mainInstance.timestamp(System.currentTimeMillis());
            if (needNewInstance) {
                session.persist(mainInstance);
            }
            return mainInstance;
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while creating internal object for type '"
                    + dataRecord.getType().getName() + "'", e);
        }
    }

    public static Serializable createCompositeId(ClassLoader classLoader, Class<?> clazz, List<Object> compositeIdValues) {
        try {
            Class<?> idClass = classLoader.loadClass(clazz.getName() + "_ID"); //$NON-NLS-1$
            Class[] parameterClasses = new Class[compositeIdValues.size()];
            int i = 0;
            for (Object o : compositeIdValues) {
                parameterClasses[i++] = o.getClass();
            }
            Constructor<?> constructor = idClass.getConstructor(parameterClasses);
            return (Serializable) constructor.newInstance(compositeIdValues.toArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
