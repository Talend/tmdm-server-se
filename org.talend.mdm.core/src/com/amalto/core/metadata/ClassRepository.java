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

package com.amalto.core.metadata;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.util.ArrayListHolder;
import com.amalto.xmlserver.interfaces.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.*;

import javax.xml.XMLConstants;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ClassRepository extends MetadataRepository {

    public static final String LINK = "LINK"; //$NON-NLS-1$

    public static final String MAP_TYPE_NAME = "map"; //$NON-NLS-1$

    public static final String EMBEDDED_XML = "embeddedXml"; //$NON-NLS-1$

    private static final SimpleTypeMetadata STRING = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING);

    private static final Logger LOGGER = Logger.getLogger(ClassRepository.class);

    private static final String GETTER_PREFIX = "get"; //$NON-NLS-1$

    private static final String BOOLEAN_PREFIX = "is"; //$NON-NLS-1$

    private static final String JAVA_LANG_PREFIX = "java.lang."; //$NON-NLS-1$

    private static final int MAJ_DIFF = 'A' - 'a';

    private final ComplexTypeMetadata MAP_TYPE;

    private final Stack<ComplexTypeMetadata> typeStack = new Stack<ComplexTypeMetadata>();

    private final Map<String, Class> entityToJavaClass = new HashMap<String, Class>();

    private final Map<Class, Iterable<Class>> registeredSubClasses = new HashMap<Class, Iterable<Class>>();

    private int listCounter = 0;

    private Class<?> listItemType;

    public ClassRepository() {
        // Create a type for MAPs
        ComplexTypeMetadata internalMapType = new ComplexTypeMetadataImpl(getUserNamespace(), MAP_TYPE_NAME, false);
        SimpleTypeMetadata embeddedXml = new SimpleTypeMetadata(StringUtils.EMPTY, EMBEDDED_XML);
        embeddedXml.addSuperType(STRING);
        embeddedXml.setData(MetadataRepository.DATA_MAX_LENGTH, String.valueOf(Integer.MAX_VALUE));
        internalMapType.addField(new SimpleTypeFieldMetadata(internalMapType, false, false, false, "key", STRING, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        internalMapType.addField(new SimpleTypeFieldMetadata(internalMapType, false, false, false, "value", embeddedXml, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY)); //$NON-NLS-1$
        MAP_TYPE = (ComplexTypeMetadata) internalMapType.freeze();
        addTypeMetadata(MAP_TYPE);
        // Register known subclasses
        registeredSubClasses.put(IWhereItem.class, Arrays.<Class>asList(CustomWhereCondition.class,
                WhereAnd.class,
                WhereCondition.class,
                WhereLogicOperator.class,
                WhereOr.class));
    }

    @Override
    protected ValidationHandler getValidationHandler() {
        return NoOpValidationHandler.INSTANCE;
    }

    public void load(Class... classes) {
        // Parse classes
        for (Class clazz : classes) {
            loadClass(clazz);
        }
        // Freeze types
        for (TypeMetadata typeMetadata : getTypes()) {
            typeMetadata.freeze();
        }
        // Freeze usages
        freezeUsages();
    }

    public Class getJavaClass(String entityTypeName) {
        return entityToJavaClass.get(entityTypeName);
    }

    private TypeMetadata loadClass(Class clazz) {
        String typeName = getTypeName(clazz);
        if (getType(typeName) != null) { // If already defined return it.
            return getType(typeName);
        } else if (getNonInstantiableType(StringUtils.EMPTY, typeName) != null) {
            return getNonInstantiableType(StringUtils.EMPTY, typeName);
        }
        entityToJavaClass.put(typeName, clazz);
        if (Map.class.isAssignableFrom(clazz)) {
            return MAP_TYPE;
        }
        if (ArrayListHolder.class.equals(clazz)) {
            typeName = typeName + listCounter++;
        }
        boolean isEntity = typeStack.isEmpty();
        ComplexTypeMetadata classType = new ComplexTypeMetadataImpl(StringUtils.EMPTY, typeName, isEntity);
        addTypeMetadata(classType);
        typeStack.push(classType);
        String keyFieldName = ""; //$NON-NLS-1$
        if (isEntity && ObjectPOJO.class.isAssignableFrom(clazz)) {
            SimpleTypeFieldMetadata keyField = new SimpleTypeFieldMetadata(typeStack.peek(),
                    true,
                    false,
                    true,
                    "unique-id", //$NON-NLS-1$
                    STRING,
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList(),
                    StringUtils.EMPTY);
            keyField.setData(LINK, "PK/unique-id"); //$NON-NLS-1$
            classType.addField(keyField);
        } else if (isEntity) {
            keyFieldName = "unique-id"; //$NON-NLS-1$
        }
        // Class is abstract / interface: load sub classes
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            Iterable<Class> subClasses = getSubclasses(clazz);
            ComplexTypeMetadata superType = typeStack.peek();
            if (superType.isInstantiable()) {
                typeStack.clear();
            }
            for (Class subClass : subClasses) {
                TypeMetadata typeMetadata = loadClass(subClass);
                typeMetadata.setInstantiable(superType.isInstantiable());
                typeMetadata.addSuperType(superType);
            }
            if (superType.isInstantiable()) {
                typeStack.push(superType);
            }
        }
        // Analyze methods
        Method[] classMethods = getMethods(clazz);
        for (Method declaredMethod : classMethods) {
            if (!Modifier.isStatic(declaredMethod.getModifiers())) {
                if (isBeanMethod(declaredMethod) && isClassMethod(clazz, declaredMethod)) {
                    String fieldName = getName(declaredMethod);
                    if (typeStack.peek().hasField(fieldName)) {
                        continue; // TODO Avoid override of fields (like PK)
                    }
                    Class<?> returnType = declaredMethod.getReturnType();
                    FieldMetadata newField;
                    boolean isMany = false;
                    boolean isKey = keyFieldName.equals(fieldName);
                    if (Iterable.class.isAssignableFrom(returnType)) {
                        returnType = listItemType != null ? listItemType : getListItemClass(declaredMethod, returnType);
                        listItemType = null;
                        isMany = true;
                    } else if (ArrayListHolder.class.isAssignableFrom(returnType)) {
                        listItemType = getListItemClass(declaredMethod, returnType);
                        isMany = false;
                    } else if (Map.class.isAssignableFrom(returnType)) {
                        isMany = true;
                    } else if (returnType.isArray()) {
                        isMany = true;
                        returnType = ((Class) returnType.getComponentType());
                    } else if (returnType.getName().startsWith("org.w3c.")) { //$NON-NLS-1$
                        // TODO Serialized XML to string column
                        continue;
                    } else if (Class.class.equals(returnType)) {
                        continue;
                    } else if (returnType.getPackage() != null && returnType.getPackage().getName().startsWith("java.io")) { //$NON-NLS-1$
                        continue;
                    }
                    if (returnType.isPrimitive() || returnType.getName().startsWith(JAVA_LANG_PREFIX)) {
                        String fieldTypeName = returnType.getName().toLowerCase();
                        if (fieldTypeName.startsWith(JAVA_LANG_PREFIX)) {
                            fieldTypeName = StringUtils.substringAfter(fieldTypeName, JAVA_LANG_PREFIX);
                        }
                        TypeMetadata fieldType;
                        if (Types.BYTE.equals(fieldTypeName) && isMany) {
                            fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.BASE64_BINARY);
                        } else {
                            fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, fieldTypeName);
                        }
                        newField = new SimpleTypeFieldMetadata(typeStack.peek(),
                                isKey,
                                isMany,
                                isKey,
                                fieldName,
                                fieldType,
                                Collections.<String>emptyList(),
                                Collections.<String>emptyList(),
                                Collections.<String>emptyList(),
                                StringUtils.EMPTY);
                        LongString annotation = declaredMethod.getAnnotation(LongString.class);
                        if (Types.STRING.equals(fieldTypeName) && annotation != null) {
                            fieldType.setData(MetadataRepository.DATA_MAX_LENGTH, String.valueOf(Integer.MAX_VALUE));
                            if (annotation.preferLongVarchar()) {
                                fieldType.setData(LongString.PREFER_LONGVARCHAR, Boolean.TRUE);
                            }
                        }
                    } else {
                        ComplexTypeMetadata fieldType;
                        if (Map.class.isAssignableFrom(returnType)) {
                            fieldType = MAP_TYPE;
                        } else {
                            fieldType = (ComplexTypeMetadata) loadClass(returnType);
                        }
                        if (!isEntity || !fieldType.isInstantiable()) {
                            newField = new ContainedTypeFieldMetadata(typeStack.peek(),
                                    isMany,
                                    false,
                                    fieldName,
                                    new SoftTypeRef(this,
                                            StringUtils.EMPTY,
                                            fieldType.getName(),
                                            false),
                                    false, // never be true
                                    Collections.<String>emptyList(),
                                    Collections.<String>emptyList(),
                                    Collections.<String>emptyList(),
                                    StringUtils.EMPTY);
                        } else {
                            newField = new ReferenceFieldMetadata(typeStack.peek(),
                                    false,
                                    isMany,
                                    false,
                                    fieldName,
                                    fieldType,
                                    fieldType.getField("unique-id"), //$NON-NLS-1$
                                    Collections.<FieldMetadata>emptyList(),
                                    StringUtils.EMPTY,
                                    true,
                                    false,
                                    STRING,
                                    Collections.<String>emptyList(),
                                    Collections.<String>emptyList(),
                                    Collections.<String>emptyList(),
                                    StringUtils.EMPTY,
                                    StringUtils.EMPTY);
                        }
                    }
                    typeStack.peek().addField(newField);
                }
            }
        }
        
        typeStack.peek().addField(new SimpleTypeFieldMetadata(typeStack.peek(),
                false,
                false,
                false,
                "digest", //$NON-NLS-1$
                new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING),
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                StringUtils.EMPTY));
        
        return typeStack.pop();
    }

    private static Method[] getMethods(Class clazz) {
        // TMDM-5851: Work around several issues in introspection (getDeclaredMethods() and getMethods() may return
        // inherited methods if returned type is a sub class of super class method).
        Map<String, Class<?>> superClassMethods = new HashMap<String, Class<?>>();
        if (clazz.getSuperclass() != null) {
            for (Method method : clazz.getSuperclass().getMethods()) {
                superClassMethods.put(method.getName(), method.getReturnType());
            }
        }
        Map<String, Method> methods = new HashMap<String, Method>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!(superClassMethods.containsKey(method.getName()) && superClassMethods.get(method.getName()).equals(method.getReturnType()))) {
                methods.put(method.getName(), method);
            }
        }
        Method[] allMethods = clazz.getMethods();
        for (Method method : allMethods) {
            if (!methods.containsKey(method.getName())) {
                methods.put(method.getName(), method);
            }
        }
        Method[] classMethods = methods.values().toArray(new Method[methods.size()]);
        // TMDM-5483: getMethods() does not always return methods in same order: sort them to ensure fixed order.
        Arrays.sort(classMethods, new Comparator<Method>() {
            @Override
            public int compare(Method method1, Method method2) {
                return method1.getName().compareTo(method2.getName());
            }
        });
        return classMethods;
    }

    // TODO Make it non specific to ServiceBean
    private static boolean isClassMethod(Class clazz, Method declaredMethod) {
        Class superClass = clazz.getSuperclass();
        if (!Object.class.equals(superClass)) {
            try {
                return superClass.getMethod(declaredMethod.getName(), declaredMethod.getParameterTypes()) != null;
            } catch (NoSuchMethodException e) {
                return true;
            }
        }
        return true;
    }

    private static boolean isBeanMethod(Method declaredMethod) {
        return isGetter(declaredMethod) || isBooleanGetter(declaredMethod);
    }

    private static boolean isBooleanGetter(Method declaredMethod) {
        String name = declaredMethod.getName();
        return name.startsWith(BOOLEAN_PREFIX) && name.length() > BOOLEAN_PREFIX.length();
    }

    private static boolean isGetter(Method declaredMethod) {
        String name = declaredMethod.getName();
        return name.startsWith(GETTER_PREFIX) && name.length() > GETTER_PREFIX.length();
    }

    private static Class<?> getListItemClass(Method declaredMethod, Class<?> returnType) {
        Type genericReturnType = declaredMethod.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            Type type = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
            if (type instanceof Class<?>) {
                returnType = ((Class) type);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("List returned by " + declaredMethod.getName() + " isn't using generic types.");
                }
                returnType = String.class;
            }
        }
        return returnType;
    }

    private Iterable<Class> getSubclasses(Class clazz) {
        Iterable<Class> classes = registeredSubClasses.get(clazz);
        return classes != null ? classes : Collections.<Class>emptySet();
    }

    private String getTypeName(Class clazz) {
        return format(StringUtils.substringAfterLast(clazz.getName(), ".")); //$NON-NLS-1$
    }

    private static String getName(Method declaredMethod) {
        if (isGetter(declaredMethod)) {
            return format(declaredMethod.getName().substring(GETTER_PREFIX.length()));
        } else if (isBooleanGetter(declaredMethod)) {
            return format(declaredMethod.getName().substring(BOOLEAN_PREFIX.length()));
        } else {
            throw new IllegalArgumentException("Cannot infer field name from method '" + declaredMethod.getName() + "'.");
        }
    }

    // DataModelPOJO -> data-model-pOJO (serialization convention by castor xml).
    public static String format(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() == 1) {
            return s.toLowerCase();
        }
        StringBuilder typeName = new StringBuilder();
        char[] chars = s.toCharArray();
        if (chars.length >= 2 && (isMaj(chars[0]) != isMaj(chars[1]))) {
            chars[0] = shift(chars[0]);
        }
        typeName.append(chars[0]);
        for (int i = 1; i < chars.length; i++) {
            char current = chars[i];
            if (isMaj(current) && !isMaj(chars[i - 1])) {
                typeName.append('-');
                typeName.append(shift(current));
            } else {
                typeName.append(current);
            }
        }
        return typeName.toString();
    }

    private static boolean isMaj(char c) {
        return c >= 'A' && c < 'a';
    }

    private static char shift(char c) {
        if (isMaj(c)) {
            return (char) (c - MAJ_DIFF);
        } else {
            return (char) (c + MAJ_DIFF);
        }
    }
}
