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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.amalto.core.storage.record.StorageConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

import com.amalto.core.storage.hibernate.MultiLingualIndexedBridge.MultilingualIndexHandler;

/**
 * This Bridge providing bidirectional mapping capability for Reference fields.Likewise, we will splitting a complex
 * type into multiple fields, like Store.Implementations must provide a set method when insert/update reference type
 * index record. created by hwzhu on Aug 4, 2018
 *
 */
@SuppressWarnings("nls")
public class ReferenceEntityBridge implements TwoWayFieldBridge {

    private static final Logger LOGGER = Logger.getLogger(ReferenceEntityBridge.class);

    private static final Set<String> processedElementSet = new LinkedHashSet<String>();

    @Override
    public Object get(String name, Document document) {
        return document.get(name);
    }

    @Override
    public String objectToString(Object object) {
        if (object == null) {
            return StringUtils.EMPTY;
        }
        return String.valueOf(object);
    }

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

        if (value == null) {
            return;
        }
        processedElementSet.clear();
        if (value instanceof Collection) {//oneToMany
            List<? extends Object> curVal = (List<?>) value;
            for (Iterator<? extends Object> iterator = curVal.iterator(); iterator.hasNext();) {
                Object item = iterator.next();
                Class<?> clazz = item.getClass();
                createEvaluationClassInternal(name, item, clazz, document, luceneOptions);
            }
        } else {//manyToOne
            Class<?> clazz = value.getClass();
            createEvaluationClassInternal(name, value, clazz, document, luceneOptions);
        }
    }

    /* Literal name "X_" means current class is a complex type, like X_MyComplex.*/
    private static void createEvaluationClassInternal(String name, Object dataObject, Class<?> clazz, Document document, LuceneOptions luceneOptions) {
        String className = clazz.getName();
        if (!clazz.getName().contains("X_")) { //$NON-NLS-2$
            className = StringUtils.substringBefore(clazz.getName(), "_"); //$NON-NLS-2$
        }
        if (processedElementSet.contains(className)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Collection processedElementSet have contained element " + className);//$NON-NLS-2$
            }
            return;
        }
        processedElementSet.add(className);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Right now, adds the specified element [" + className + "] to this processedElementSet");//$NON-NLS-2$
        }
        evaluateClass(name, dataObject, clazz, document, luceneOptions);
        processedElementSet.remove(className);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removes the given element [" + className + "] from processedElementSet sucessfully");//$NON-NLS-2$
        }
    }

    private static void evaluateClass(String name, Object dataObject, Class<?> clazz, Document document, LuceneOptions luceneOptions) {
        if (clazz != Object.class) {
            indexedMemberFields(name, dataObject, clazz, document, luceneOptions);
            Class<?> clazzs = clazz.getSuperclass();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Begin to evaluate all fields of parent class " + clazzs.getName());
            }
            createEvaluationClassInternal(name, dataObject, clazzs, document, luceneOptions);
        }
    }

    private static void indexedMemberFields(String name, Object dataObject, Class<?> clazz, Document document,
            LuceneOptions luceneOptions) {

        if (clazz.isPrimitive() || clazz.getName().startsWith("java.lang")) {
            luceneOptions.addFieldToDocument(name, MultilingualIndexHandler.getIndexedContent(dataObject.toString()), document);
            return;
        }
        Field[] allFields = clazz.getFields();
        Method[] allMethod = clazz.getDeclaredMethods();
        for (Method method : allMethod) {
            String methodName = method.getName();
            if (!StringUtils.startsWith(methodName, "get") || methodName.contains("x_talend_task_id")
                    || methodName.contains("x_talend_timestamp")
                    || methodName.contains("x_talend_id")) {
                continue;
            }
            for (Field field : allFields) {
                field.setAccessible(true);
                if (!StringUtils.equals(methodName, "get" + field.getName())) {
                    continue;
                }
                Object value = null;
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("calling method " + method.getName() + " in java class " + clazz.getName());
                    }
                    value = method.invoke(dataObject);
                } catch (Exception e) {
                    throw new UnsupportedOperationException("No support for the invoke method '" + method.getName() + "'", e);//$NON-NLS-1$
                }
                if (value == null) {
                    break;
                }

               /* For the entity as bellow:
                * A
                * |__Id (SimpleField)
                * |__B (ContainedField)
                *    |__C (ComplexField)
                *       |__D (Reference)
                * Use '<A><Id>1</Id><B><C><D>[1]</D></C></B></A>' to save, but D(1) doesn't exist.
                * Generente lucence index for D(1) will fail, so need to set it to null for now.
                */
                try {
                    if (value != null && value.toString() != null) {
                        IndexHandler handler = getHandler(field, value);
                        handler.handle(name, value, field, document, luceneOptions);
                    }
                } catch (ObjectNotFoundException e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Filed '" + field.getName() + "' doesn't exist yet.", e);
                    }
                    ((Wrapper) dataObject).set(field.getName(), null);
                    break;
                }
            }
        }
    }

    private static IndexHandler getHandler(Field culField, Object curVal) {
        if (curVal == null || StorageConstants.METADATA_TIMESTAMP.equals(culField.getName())
                || StorageConstants.METADATA_TASK_ID.equals(culField.getName())) {
            return new EmptyIndexHandler();
        }
        if (Collection.class.isAssignableFrom(culField.getType())) {
            return new CollectionTypeIndexHandler();
        } else if (culField.getType().isArray()) {
            return new ArrayTypeIndexHandler();
        } else if (!culField.getType().isPrimitive() && !culField.getType().getName().startsWith("java.lang")
                && !isDateType(culField)) {
            return new ReferenceTypeIndexHandler();
        } else if (isDateType(culField)) {
            return new DateTypeIndexHandler();
        } else {
            return new SimpleTypeIndexHandler();
        }
    }

    private static boolean isDateType(Field culField) {
        if (culField.getType().getName().startsWith("java.util.Date") || (culField.getType().getSuperclass() != null
                && culField.getType().getSuperclass().getName().startsWith("java.util.Date"))) {
            return true;
        } else {
            return false;
        }
    }

    private static interface IndexHandler {
        void handle(String name, Object dataObject, Field field, Document document, LuceneOptions luceneOptions);
    }

    private static class ArrayTypeIndexHandler implements IndexHandler {
        @Override
        public void handle(String name, Object value, Field field, Document document, LuceneOptions luceneOptions) {
            Object[] objArray = null;
            try {
                objArray = (Object[]) value;
            } catch (Exception e) {
                throw new UnsupportedOperationException("Failed to case value to array type,caused by ", e);//$NON-NLS-1$
            }
            if (objArray == null) {
                return;
            }
            Class<?> sClass = field.getType().getComponentType();
            if (sClass.isPrimitive() || sClass.getName().startsWith("java.lang")) {
                String arrayStr = "";
                for (Object obj : objArray) {
                    arrayStr += obj + " ";
                }
                luceneOptions.addFieldToDocument(name + "." + field.getName(), MultilingualIndexHandler.getIndexedContent(arrayStr), document);
            } else {
                name += "." + field.getName(); // x_stores.store.name
                for (Object obj : objArray) {
                    createEvaluationClassInternal(name, obj, sClass, document, luceneOptions);
                }
            }
        }
    }

    private static class ReferenceTypeIndexHandler implements IndexHandler {
        @Override
        public void handle(String name, Object value, Field field, Document document, LuceneOptions luceneOptions) {
            createEvaluationClassInternal(name + "." + field.getName(), value, value.getClass(), document, luceneOptions);
        }
    }

    private static class CollectionTypeIndexHandler implements IndexHandler {
        @Override
        public void handle(String name, Object value, Field field, Document document, LuceneOptions luceneOptions) {
            try {
                Collection<? extends Object> valList = (Collection<?>) value;// field.get(dataObject);
                name += "." + field.getName(); // x_stores.store.name
                for (Iterator<?> it = valList.iterator(); it.hasNext();) {
                    Object itemObj = it.next();
                    createEvaluationClassInternal(name, itemObj, itemObj.getClass(), document, luceneOptions);
                }
            } catch (Exception e) {
                throw new UnsupportedOperationException(
                        "Failed to use a reflection type technique to index value of each element of collection,caused by ", e);//$NON-NLS-1$
            }
        }
    }

    private static class DateTypeIndexHandler implements IndexHandler {
        @Override
        public void handle(String name, Object value, Field field, Document document, LuceneOptions luceneOptions) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("insert a new Date index record with key-value pair [ " + name + "." + field.getName() + ":"
                        + value.toString());
            }
            if (value instanceof java.util.Date) {
                java.util.Date date = convertStringToDate("yyyy-MM-dd HH:mm:ss", value.toString());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                String indexKey = name + "." + field.getName();
                luceneOptions.addFieldToDocument(indexKey, cal.get(Calendar.YEAR) + "", document);
                luceneOptions.addFieldToDocument(indexKey, cal.get(Calendar.MONTH) + 1 + "", document);
                luceneOptions.addFieldToDocument(indexKey, cal.get(Calendar.DAY_OF_MONTH) + "", document);
                luceneOptions.addFieldToDocument(indexKey, cal.get(Calendar.HOUR_OF_DAY) + "." + cal.get(Calendar.MINUTE),
                        document);
                luceneOptions.addFieldToDocument(indexKey, cal.get(Calendar.SECOND) + "", document);
            }
        }
    }

    private static class SimpleTypeIndexHandler implements IndexHandler {
        @Override
        public void handle(String name, Object value, Field field, Document document, LuceneOptions luceneOptions) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("insert a new index record with key-value pair [ " + name + "." + field.getName() + ":"
                        + value.toString());
            }
            luceneOptions.addFieldToDocument(name + "." + field.getName(), MultilingualIndexHandler.getIndexedContent(value.toString()), document);
        }
    }

    private static class EmptyIndexHandler implements IndexHandler {
        @Override
        public void handle(String name, Object dataObject, Field field, Document document, LuceneOptions luceneOptions) {
            LOGGER.info("nothing to do for field " + field.getName());
        }
    }

    public static final Date convertStringToDate(String aMask, String strDate) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("converting '" + strDate + "' to date with mask '" + aMask + "'");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        Date date = null;
        try {
            if (strDate != null && strDate.length() > 0) {
                SimpleDateFormat df = new SimpleDateFormat(aMask);
                date = df.parse(strDate);
            }
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return (date);
    }
}
