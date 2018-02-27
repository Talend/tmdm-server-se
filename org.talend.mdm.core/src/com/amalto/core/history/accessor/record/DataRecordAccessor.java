/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.history.accessor.record;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;

public class DataRecordAccessor implements Accessor {

    private final DataRecord dataRecord;

    private final String path;

    private final MetadataRepository repository;

    private LinkedList<PathElement> pathElements = null;

    private Boolean cachedExist;

    public DataRecordAccessor(MetadataRepository repository, DataRecord dataRecord, String path) {
        this.repository = repository;
        this.dataRecord = dataRecord;
        this.path = path;
    }

    private void initPath() {
        if (this.pathElements == null) {
            this.pathElements = getPath(dataRecord, path);
        }
    }

    @SuppressWarnings("rawtypes")
    private static LinkedList<PathElement> getPath(DataRecord dataRecord, String path) {
        LinkedList<PathElement> elements = new LinkedList<PathElement>();
        StringTokenizer tokenizer = new StringTokenizer(path, "/"); //$NON-NLS-1$
        DataRecord current = dataRecord;
        while (tokenizer.hasMoreElements()) {
            String element = tokenizer.nextToken();
            PathElement pathElement = new PathElement();
            if (element.indexOf('@') == 0) {
                pathElement.field = elements.getLast().field;
                pathElement.setter = TypeValue.SET;
                pathElement.getter = TypeValue.GET;
            } else {
                if (current == null) {
                    throw new IllegalStateException("Cannot update '" + path + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (element.indexOf('[') > 0) {
                    pathElement.field = current.getType().getField(StringUtils.substringBefore(element, "[")); //$NON-NLS-1$
                    if (!pathElement.field.isMany()) {
                        throw new IllegalStateException("Expected a repeatable field for '" + element + "' in path '" + path //$NON-NLS-1$ //$NON-NLS-2$
                                + "'."); //$NON-NLS-1$
                    }
                    int indexStart = element.indexOf('[');
                    int indexEnd = element.indexOf(']');
                    if (indexStart < 0 || indexEnd < 0) {
                        throw new RuntimeException("Field name '" + element + "' did not match many field pattern in path '" //$NON-NLS-1$ //$NON-NLS-2$
                                + path + "'."); //$NON-NLS-1$
                    }
                    pathElement.index = Integer.parseInt(element.substring(indexStart + 1, indexEnd)) - 1;
                    pathElement.setter = ManyValue.SET;
                    pathElement.getter = ManyValue.GET;
                    List list = (List) current.get(pathElement.field);
                    if (list == null || pathElement.index > list.size() - 1) {
                        throw new IllegalStateException("Cannot update '" + path + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    Object value = list.get(pathElement.index);
                    if (value instanceof DataRecord) {
                        current = (DataRecord) value;
                    } else if (value instanceof List) {
                        throw new IllegalStateException();
                    }
                } else {
                    pathElement.field = current.getType().getField(element);
                    pathElement.setter = SimpleValue.SET;
                    pathElement.getter = SimpleValue.GET;
                    if (pathElement.field instanceof ContainedTypeFieldMetadata
                            || pathElement.field instanceof ReferenceFieldMetadata) {
                        Object value = current.get(pathElement.field);
                        if (value instanceof DataRecord) {
                            current = (DataRecord) value;
                        }
                    }
                }
            }
            elements.add(pathElement);
        }
        return elements;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void set(String value) {
        try {
            initPath();
            DataRecord current = dataRecord;
            ListIterator<PathElement> listIterator = pathElements.listIterator();
            PathElement pathElement = null;
            while (listIterator.hasNext()) {
                pathElement = listIterator.next();
                if (!listIterator.hasNext()) {
                    break;
                }
                if (!pathElement.field.isMany()) {
                    if (pathElement.field instanceof ContainedTypeFieldMetadata) {
                        current = (DataRecord) current.get(pathElement.field);
                    }
                } else {
                    List<Object> list = (List) current.get(pathElement.field);
                    if (list == null) {
                        list = new ArrayList<Object>();
                        current.set(pathElement.field, list);
                    }
                    while (pathElement.index > list.size() - 1) { // Expand list size
                        list.add(null);
                    }
                    if (pathElement.field instanceof ContainedTypeFieldMetadata) {
                        current = (DataRecord) ((List) current.get(pathElement.field)).get(pathElement.index);
                    }
                }
            }
            if (pathElement != null) {
                pathElement.setter.set(repository, current, pathElement, value);
            }
        } finally {
            cachedExist = true;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public String get() {
        initPath();
        DataRecord current = dataRecord;
        ListIterator<PathElement> listIterator = pathElements.listIterator();
        PathElement pathElement = null;
        while (listIterator.hasNext()) {
            pathElement = listIterator.next();
            if (!listIterator.hasNext()) {
                break;
            }
            if (!pathElement.field.isMany()) {
                if (pathElement.field instanceof ContainedTypeFieldMetadata
                        || pathElement.field instanceof ReferenceFieldMetadata) {
                    current = (DataRecord) current.get(pathElement.field);
                }
            } else {
                List<Object> list = (List) current.get(pathElement.field);
                if (list == null) {
                    list = new ArrayList<Object>();
                    current.set(pathElement.field, list);
                }
                while (pathElement.index > list.size() - 1) { // Expand list size
                    list.add(null);
                }
                if (pathElement.field instanceof ContainedTypeFieldMetadata) {
                    current = (DataRecord) ((List) current.get(pathElement.field)).get(pathElement.index);
                }
            }
        }
        if (pathElement != null) {
            return pathElement.getter.get(current, pathElement);
        }
        throw new IllegalArgumentException("Path '" + path + "' does not exist in document."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void touch() {
        // No need to implement anything for this kind of accessor.
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void create() {
        try {
            StringTokenizer tokenizer = new StringTokenizer(path, "/"); //$NON-NLS-1$
            DataRecord current = dataRecord;
            while (tokenizer.hasMoreElements()) {
                String element = tokenizer.nextToken();
                if (element.indexOf('@') < 0) {
                    if (current == null) {
                        throw new IllegalStateException(); // TODO Message
                    }
                    if (element.indexOf('[') > 0) {
                        FieldMetadata field = current.getType().getField(StringUtils.substringBefore(element, "[")); //$NON-NLS-1$
                        if (!field.isMany()) {
                            throw new IllegalStateException("Expected a repeatable field for '" + element + "' in path '" + path //$NON-NLS-1$ //$NON-NLS-2$
                                    + "'."); //$NON-NLS-1$
                        }
                        int indexStart = element.indexOf('[');
                        int indexEnd = element.indexOf(']');
                        if (indexStart < 0 || indexEnd < 0) {
                            throw new RuntimeException("Field name '" + element + "' did not match many field pattern in path '" //$NON-NLS-1$ //$NON-NLS-2$
                                    + path + "'."); //$NON-NLS-1$
                        }
                        int index = Integer.parseInt(element.substring(indexStart + 1, indexEnd)) - 1;
                        List list = (List) current.get(field);
                        boolean newList = list == null;
                        if (newList) {
                            list = new LinkedList();
                        }
                        while (index >= list.size()) {
                            if (field instanceof ContainedTypeFieldMetadata) {
                                DataRecord record = new DataRecord((ComplexTypeMetadata) field.getType(),
                                        UnsupportedDataRecordMetadata.INSTANCE);
                                try {
                                    list.add(record);
                                } catch (UnsupportedOperationException e) {
                                    list = addFieldValueToNewList(current, field, list, record);
                                }
                            } else if (field instanceof ReferenceFieldMetadata) {
                                DataRecord record = new DataRecord(((ReferenceFieldMetadata) field).getReferencedType(),
                                        UnsupportedDataRecordMetadata.INSTANCE);
                                try {
                                    list.add(record);
                                } catch (UnsupportedOperationException e) {
                                    list = addFieldValueToNewList(current, field, list, record);
                                }
                            } else {
                                try {
                                    list.add(null);
                                } catch (UnsupportedOperationException e) {
                                    list = addFieldValueToNewList(current, field, list, null);
                                }
                            }
                        }
                        if (newList) {
                            current.set(field, list);
                        }
                        Object value = list.get(index);
                        if (value instanceof DataRecord) {
                            current = (DataRecord) value;
                        }
                    } else {
                        FieldMetadata field = current.getType().getField(element);
                        if (field instanceof ContainedTypeFieldMetadata) {
                            Object value = current.get(field);
                            if (value == null) {
                                DataRecord record = new DataRecord(((ContainedTypeFieldMetadata) field).getContainedType(),
                                        UnsupportedDataRecordMetadata.INSTANCE);
                                current.set(field, record);
                                current = record;
                            } else {
                                current = (DataRecord) value;
                            }
                        }
                    }
                }
            }
        } finally {
            cachedExist = true;
        }
    }

    protected List addFieldValueToNewList(DataRecord current, FieldMetadata field, List list, DataRecord record) {
        List newValueList = new ArrayList(list);
        newValueList.add(record);
        current.remove(field);
        current.set(field, newValueList);
        return (List) current.get(field);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void insert() {
        try {
            if (!exist()) {
                create();
            } else {
                DataRecord current = dataRecord;
                ListIterator<PathElement> elements = getPath(dataRecord, path).listIterator();
                PathElement pathElement = null;
                while (elements.hasNext()) {
                    pathElement = elements.next();
                    if (!elements.hasNext()) {
                        break;
                    }
                    if (pathElement.field instanceof ContainedTypeFieldMetadata) {
                        Object o = current.get(pathElement.field);
                        if (pathElement.field.isMany()) {
                            List list = (List) o;
                            current = (DataRecord) list.get(pathElement.index);
                        } else {
                            current = (DataRecord) o;
                        }
                    }
                }
                if (pathElement != null && pathElement.field.isMany()) {
                    List list = (List) current.get(pathElement.field);
                    try {
                        list.add(pathElement.index, null);
                    } catch (UnsupportedOperationException e) {
                        List newValueList = new ArrayList(list);
                        newValueList.add(pathElement.index, null);
                        current.remove(pathElement.field);
                        current.set(pathElement.field, newValueList);
                    }
                }
            }
        } finally {
            cachedExist = true;
        }
    }

    @Override
    public void createAndSet(String value) {
        create();
        pathElements = null;
        initPath();
        set(value);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void delete() {
        if (!exist()) {
            return; // Value is already deleted.
        }
        try {
            initPath();
            DataRecord current = dataRecord;
            ListIterator<PathElement> listIterator = pathElements.listIterator();
            PathElement pathElement = null;
            while (listIterator.hasNext()) {
                pathElement = listIterator.next();
                if (!listIterator.hasNext()) {
                    break;
                }
                if (!pathElement.field.isMany()) {
                    if (pathElement.field instanceof ContainedTypeFieldMetadata) {
                        current = (DataRecord) current.get(pathElement.field);
                    }
                } else {
                    if (pathElement.field instanceof ContainedTypeFieldMetadata) {
                        current = (DataRecord) ((List) current.get(pathElement.field)).get(pathElement.index);
                    } else {
                        List<Object> list = (List) dataRecord.get(pathElement.field);
                        if (list == null) {
                            list = new ArrayList<Object>();
                            dataRecord.set(pathElement.field, list);
                        }
                    }
                }
            }
            if (pathElement != null) {
                if (pathElement.field.isMany()) {
                    if (pathElement.index < 0) {
                        try {
                            ((List) current.get(pathElement.field)).clear();
                        } catch (UnsupportedOperationException e) {
                            current.set(pathElement.field, null);
                        }
                    } else {
                        try {
                            ((List) current.get(pathElement.field)).remove(pathElement.index);
                        } catch (UnsupportedOperationException e) {
                            List originList = ((List) current.get(pathElement.field));
                            List newValueList = new ArrayList(originList);
                            newValueList.remove(pathElement.index);
                            current.remove(pathElement.field);
                            current.set(pathElement.field, newValueList);
                        }
                    }
                } else {
                    current.set(pathElement.field, null);
                }
            }
        } finally {
            cachedExist = false;
        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public boolean exist() {
        if (cachedExist != null) {
            return cachedExist;
        }
        StringTokenizer tokenizer = new StringTokenizer(path, "/"); //$NON-NLS-1$
        DataRecord current = dataRecord;
        while (tokenizer.hasMoreElements()) {
            String element = tokenizer.nextToken();
            if (element.indexOf('@') == 0) {
                cachedExist = true;
                return true;
            } else {
                if (current == null) {
                    cachedExist = false;
                    return false;
                }
                if (element.indexOf('[') > 0) {
                    String fieldName = StringUtils.substringBefore(element, "["); //$NON-NLS-1$
                    if (!current.getType().hasField(fieldName)) {
                        cachedExist = false;
                        return false;
                    }
                    FieldMetadata field = current.getType().getField(fieldName);
                    if (!field.isMany()) {
                        throw new IllegalStateException("Expected a repeatable field for '" + element + "' in path '" + path //$NON-NLS-1$ //$NON-NLS-2$
                                + "'."); //$NON-NLS-1$
                    }
                    int indexStart = element.indexOf('[');
                    int indexEnd = element.indexOf(']');
                    if (indexStart < 0 || indexEnd < 0) {
                        throw new RuntimeException("Field name '" + element + "' did not match many field pattern in path '" //$NON-NLS-1$ //$NON-NLS-2$
                                + path + "'."); //$NON-NLS-1$
                    }
                    int index = Integer.parseInt(element.substring(indexStart + 1, indexEnd)) - 1;
                    List list = (List) current.get(field);
                    if (list == null || index > list.size() - 1) {
                        cachedExist = false;
                        return false;
                    }
                    Object value = list.get(index);
                    if (value instanceof DataRecord) {
                        current = (DataRecord) value;
                    }
                } else {
                    if (!current.getType().hasField(element) || current.get(element) == null) {
                        cachedExist = false;
                        return false;
                    }
                    FieldMetadata field = current.getType().getField(element);
                    if (field instanceof ContainedTypeFieldMetadata) {
                        Object value = current.get(field);
                        if (value instanceof DataRecord) {
                            current = (DataRecord) value;
                        }
                        // We can't get sub element value of ReferenceFieldMetadata,like Product/Family/Name.Because we
                        // can't use path Product/Family/Name to retrieve value from Product document.
                    } else if (field instanceof ReferenceFieldMetadata && tokenizer.hasMoreElements()) {
                        cachedExist = false;
                        return false;
                    }
                }
            }
        }
        cachedExist = true;
        return true;
    }

    @Override
    public void markModified(Marker marker) {
        // No need to implement anything for this kind of accessor.
    }

    @Override
    public void markUnmodified() {
        // No need to implement anything for this kind of accessor.
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int size() {
        if (!exist()) {
            return 0;
        }
        initPath();
        DataRecord current = dataRecord;
        for (PathElement pathElement : pathElements) {
            if (pathElement.field instanceof ContainedTypeFieldMetadata) {
                if (!pathElement.field.isMany()) {
                    if (!current.getType().hasField(pathElement.field.getName())) {
                        return 0;
                    }
                    Object o = current.get(pathElement.field);
                    if (o == null) {
                        return 0;
                    } else if (pathElement == pathElements.getLast()) {
                        return 1;
                    }
                    current = (DataRecord) o;
                } else {
                    if (!current.getType().hasField(pathElement.field.getName())) {
                        return 0;
                    }
                    List list = (List) current.get(pathElement.field);
                    if (list == null) {
                        return 0;
                    } else if (pathElement.index < 0 || pathElement == pathElements.getLast()) {
                        return list.size();
                    }
                    current = (DataRecord) list.get(pathElement.index);
                }
            } else {
                if (!current.getType().hasField(pathElement.field.getName())) {
                    return 0;
                } else if (!pathElement.field.isMany()) {
                    return current.get(pathElement.field) == null ? 0 : 1;
                } else {
                    List list = (List) current.get(pathElement.field);
                    return list == null ? 0 : list.size();
                }
            }
        }
        return 0;
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public String getActualType() {
        initPath();
        DataRecord current = dataRecord;
        for (PathElement pathElement : pathElements) {
            if (pathElement.field instanceof ContainedTypeFieldMetadata || pathElement.field instanceof ReferenceFieldMetadata) {
                if (!pathElement.field.isMany()) {
                    Object o = current.get(pathElement.field);
                    if (o == null) {
                        return StringUtils.EMPTY;
                    }
                    current = (DataRecord) o;
                } else {
                    List list = (List) current.get(pathElement.field);
                    if (list == null) {
                        return StringUtils.EMPTY;
                    }
                    int index = pathElement.index == -1 ? 0 : pathElement.index;
                    current = (DataRecord) list.get(index);
                }
            }
        }
        return current.getType().getName();
    }

    @Override
    public int compareTo(Accessor accessor) {
        if (exist() != accessor.exist()) {
            return -1;
        }
        if (exist()) {
            return get().equals(accessor.get()) ? 0 : -1;
        }
        return -1;
    }
}
