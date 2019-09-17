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

package com.amalto.core.storage.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;
import org.talend.mdm.commmon.util.core.CommonUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.amalto.core.metadata.LongString;
import com.amalto.core.storage.HibernateMetadataUtils;
import com.amalto.core.storage.HibernateStorageUtils;
import com.amalto.core.storage.datasource.RDBMSDataSource;

// TODO Refactor (+ NON-NLS)
public class MappingGenerator extends DefaultMetadataVisitor<Element> {

    /**
     * Max limit for a string restriction (greater then this -> use CLOB or TEXT).
     */
    // public static final int MAX_VARCHAR_TEXT_LIMIT = 255;

    public static final String DISCRIMINATOR_NAME = "x_talend_class"; //$NON-NLS-1$

    public static final String SQL_DELETE_CASCADE = "SQL_DELETE_CASCADE"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(MappingGenerator.class);
    
    private final Document document;

    private final TableResolver resolver;

    public final RDBMSDataSource dataSource;

    private final Stack<String> tableNames = new Stack<String>();

    private boolean compositeId;

    private Element parentElement;

    private boolean isDoingColumns;

    private boolean isColumnMandatory;

    private String compositeKeyPrefix;

    private boolean generateConstrains;

    private boolean fromMemoryStorage;

    public MappingGenerator(Document document, TableResolver resolver, RDBMSDataSource dataSource) {
        this(document, resolver, dataSource, true, false);
    }

    public MappingGenerator(Document document,
                            TableResolver resolver,
                            RDBMSDataSource dataSource,
                            boolean generateConstrains) {
        this(document, resolver, dataSource, generateConstrains, false);
    }

    public MappingGenerator(Document document,
            TableResolver resolver,
            RDBMSDataSource dataSource,
            boolean generateConstrains,
            boolean fromMemoryStorage) {
        this.document = document;
        this.resolver = resolver;
        this.dataSource = dataSource;
        this.generateConstrains = generateConstrains;
        this.fromMemoryStorage = fromMemoryStorage;
    }


    @Override
    public Element visit(MetadataRepository repository) {
        // To disallow wrong usage of this class, disables visiting the whole repository
        throw new NotImplementedException("Repository visit is disabled in this visitor."); //$NON-NLS-1$
    }

    @Override
    public Element visit(ComplexTypeMetadata complexType) {
        if (complexType.getKeyFields().isEmpty()) {
            throw new IllegalArgumentException("Type '" + complexType.getName() + "' has no key."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!complexType.getSuperTypes().isEmpty()) {
            return null;
        }

        tableNames.push(resolver.get(complexType));
        Element classElement;
        {
            String generatedClassName = ClassCreator.getClassName(complexType.getName());
            classElement = document.createElement("class"); //$NON-NLS-1$
            Attr className = document.createAttribute("name");  //$NON-NLS-1$
            className.setValue(generatedClassName);
            classElement.getAttributes().setNamedItem(className);
            Attr classTable = document.createAttribute("table"); //$NON-NLS-1$
            classTable.setValue(tableNames.peek());
            classElement.getAttributes().setNamedItem(classTable);
            // Adds schema name (if set in configuration)
            String value = dataSource.getAdvancedProperties().get("hibernate.default_schema"); //$NON-NLS-1
            if (value != null) {
                Attr classSchema = document.createAttribute("schema"); //$NON-NLS-1$
                classSchema.setValue(value);
                classElement.getAttributes().setNamedItem(classSchema);
            }
            // dynamic-update="true"
            Attr dynamicUpdate = document.createAttribute("dynamic-update");  //$NON-NLS-1$
            dynamicUpdate.setValue("true"); //$NON-NLS-1$
            classElement.getAttributes().setNamedItem(dynamicUpdate);
            // dynamic-insert="true"
            Attr dynamicInsert = document.createAttribute("dynamic-insert");  //$NON-NLS-1$
            dynamicInsert.setValue("true"); //$NON-NLS-1$
            classElement.getAttributes().setNamedItem(dynamicInsert);
            // <cache usage="read-write" include="non-lazy"/>
            Element cacheElement = document.createElement("cache"); //$NON-NLS-1$
            Attr usageAttribute = document.createAttribute("usage"); //$NON-NLS-1$
            usageAttribute.setValue("read-write"); //$NON-NLS-1$
            cacheElement.getAttributes().setNamedItem(usageAttribute);
            Attr includeAttribute = document.createAttribute("include"); //$NON-NLS-1$
            includeAttribute.setValue("non-lazy"); //$NON-NLS-1$
            cacheElement.getAttributes().setNamedItem(includeAttribute);
            Attr regionAttribute = document.createAttribute("region"); //$NON-NLS-1$
            regionAttribute.setValue("region"); //$NON-NLS-1$
            cacheElement.getAttributes().setNamedItem(regionAttribute);
            classElement.appendChild(cacheElement);

            Collection<FieldMetadata> keyFields = complexType.getKeyFields();
            List<FieldMetadata> allFields = new ArrayList<FieldMetadata>(complexType.getFields());

            // Process key fields first (Hibernate DTD enforces IDs to be declared first in <class/> element).
            Element idParentElement = classElement;
            if (keyFields.size() > 1) {
                /*
                <composite-id>
                            <key-property column="x_enterprise" name="x_enterprise"/>
                            <key-property column="x_id" name="x_id"/>
                        </composite-id>
                 */
                compositeId = true;
                idParentElement = document.createElement("composite-id"); //$NON-NLS-1$
                classElement.appendChild(idParentElement);

                Attr classAttribute = document.createAttribute("class"); //$NON-NLS-1$
                classAttribute.setValue(generatedClassName + "_ID"); //$NON-NLS-1$
                idParentElement.getAttributes().setNamedItem(classAttribute);

                Attr mappedAttribute = document.createAttribute("mapped"); //$NON-NLS-1$
                mappedAttribute.setValue("true"); //$NON-NLS-1$
                idParentElement.getAttributes().setNamedItem(mappedAttribute);
            }
            for (FieldMetadata keyField : keyFields) {
                idParentElement.appendChild(keyField.accept(this));
                boolean wasRemoved = allFields.remove(keyField);
                if (!wasRemoved) {
                    LOGGER.error("Field '" + keyField.getName() + "' was expected to be removed from processed fields."); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            compositeId = false;
            // Generate a discriminator (if needed).
            if (!complexType.getSubTypes().isEmpty() && !complexType.isInstantiable()) {
                // <discriminator column="PAYMENT_TYPE" type="string"/>
                Element discriminator = document.createElement("discriminator"); //$NON-NLS-1$
                Attr name = document.createAttribute("column"); //$NON-NLS-1$
                name.setValue(DISCRIMINATOR_NAME);
                discriminator.setAttributeNode(name);
                Attr type = document.createAttribute("type"); //$NON-NLS-1$
                type.setValue("string"); //$NON-NLS-1$
                discriminator.setAttributeNode(type);
                classElement.appendChild(discriminator);
            }
            // Process this type fields
            boolean wasGeneratingConstraints = generateConstrains;
            try {
                if (complexType.getName().startsWith("X_")) { //$NON-NLS-1$
                    Integer usageNumber = complexType.<Integer>getData(TypeMapping.USAGE_NUMBER);
                    // TMDM-8283: Don't turn on constraint generation even if reusable type is used only once.
                    generateConstrains = generateConstrains && (usageNumber == null || usageNumber <= 1);
                }
                for (FieldMetadata currentField : allFields) {
                    Element child = currentField.accept(this);
                    if (child == null) {
                        throw new IllegalArgumentException("Field type " + currentField.getClass().getName() + " is not supported."); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    classElement.appendChild(child);
                }
            } finally {
                generateConstrains = wasGeneratingConstraints;
            }
            // Sub types
            generatorSubType(complexType, classElement);
        }
        tableNames.pop();

        return classElement;
    }

    private void generatorSubType(ComplexTypeMetadata parentComplexType, Element classElement) {
        boolean wasGeneratingConstraints;
        // Sub types
        if (!parentComplexType.getDirectSubTypes().isEmpty()) {
            if (parentComplexType.isInstantiable()) {
                /*
                    <union-subclass name="CreditCardPayment" table="CREDIT_PAYMENT">
                           <property name="creditCardType" column=""/>
                           ...
                       </union-subclass>
                    */
                for (ComplexTypeMetadata subType : parentComplexType.getDirectSubTypes()) {
                    Element unionSubclass = document.createElement("union-subclass"); //$NON-NLS-1$
                    Attr name = document.createAttribute("name"); //$NON-NLS-1$
                    name.setValue(ClassCreator.getClassName(subType.getName()));
                    unionSubclass.setAttributeNode(name);

                    Attr tableName = document.createAttribute("table"); //$NON-NLS-1$
                    tableName.setValue(resolver.get(subType));
                    unionSubclass.setAttributeNode(tableName);

                    Collection<FieldMetadata> subTypeFields = subType.getFields();
                    for (FieldMetadata subTypeField : subTypeFields) {
                        if (!parentComplexType.hasField(subTypeField.getName()) && !subTypeField.isKey()) {
                            unionSubclass.appendChild(subTypeField.accept(this));
                        }
                    }
                    if(!subType.getDirectSubTypes().isEmpty()){
                        generatorSubType(subType, unionSubclass);
                    }
                    classElement.appendChild(unionSubclass);
                }
            } else {
                /*
                <subclass name="CreditCardPayment" discriminator-value="CREDIT">
                        <property name="creditCardType" column="CCTYPE"/>
                        ...
                    </subclass>
                 */
                wasGeneratingConstraints = generateConstrains;
                generateConstrains = false;
                try {
                    for (ComplexTypeMetadata subType : parentComplexType.getDirectSubTypes()) {
                        Element subclass = document.createElement("subclass"); //$NON-NLS-1$
                        Attr name = document.createAttribute("name"); //$NON-NLS-1$
                        name.setValue(ClassCreator.getClassName(subType.getName()));
                        subclass.setAttributeNode(name);
                        Attr discriminator = document.createAttribute("discriminator-value"); //$NON-NLS-1$
                        discriminator.setValue(ClassCreator.PACKAGE_PREFIX + subType.getName());
                        subclass.setAttributeNode(discriminator);

                        Collection<FieldMetadata> subTypeFields = subType.getFields();
                        for (FieldMetadata subTypeField : subTypeFields) {
                            if (!parentComplexType.hasField(subTypeField.getName()) && !subTypeField.isKey()) {
                                subclass.appendChild(subTypeField.accept(this));
                            }
                        }
                        if(!subType.getDirectSubTypes().isEmpty()){
                            generatorSubType(subType, subclass);
                        }
                        classElement.appendChild(subclass);
                    }
                } finally {
                    generateConstrains = wasGeneratingConstraints;
                }
            }
        }
    }

    @Override
    public Element visit(ContainedTypeFieldMetadata containedField) {
        throw new IllegalArgumentException("Type should have been flatten before calling this method."); //$NON-NLS-1$
    }

    @Override
    public Element visit(ContainedComplexTypeMetadata containedType) {
        throw new IllegalArgumentException("Type should have been flatten before calling this method."); //$NON-NLS-1$
    }

    @Override
    public Element visit(EnumerationFieldMetadata enumField) {
        // handle enum fields just like simple fields
        return handleSimpleField(enumField);
    }

    @Override
    public Element visit(ReferenceFieldMetadata referenceField) {
        if (referenceField.isKey()) {
            throw new UnsupportedOperationException("FK field '" + referenceField.getName() + "' cannot be key in type '" + referenceField.getDeclaringType().getName() + "'"); // Don't support FK as key //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            boolean enforceDataBaseIntegrity = generateConstrains && (!referenceField.allowFKIntegrityOverride() && referenceField.isFKIntegrity());
            if (!referenceField.isMany()) {
                return newManyToOneElement(referenceField, enforceDataBaseIntegrity);
            } else {
                /*
                <list name="bars" table="foo_bar">
                     <key column="foo_id"/>
                     <many-to-many column="bar_id" class="Bar"/>
                  </list>
                 */
                Element propertyElement = document.createElement("list"); //$NON-NLS-1$
                Attr name = document.createAttribute("name"); //$NON-NLS-1$
                name.setValue(referenceField.getName());
                propertyElement.getAttributes().setNamedItem(name);
                Attr lazy = document.createAttribute("lazy"); //$NON-NLS-1$
                lazy.setValue("extra"); //$NON-NLS-1$
                propertyElement.getAttributes().setNamedItem(lazy);
                // fetch="select"
                Attr joinAttribute = document.createAttribute("fetch"); //$NON-NLS-1$
                joinAttribute.setValue("select"); // Keep it "select" (Hibernate tends to duplicate results when using "fetch") //$NON-NLS-1$
                propertyElement.getAttributes().setNamedItem(joinAttribute);
                // cascade="true"
                if (Boolean.parseBoolean(referenceField.<String>getData(SQL_DELETE_CASCADE))) {
                    Attr cascade = document.createAttribute("cascade"); //$NON-NLS-1$
                    cascade.setValue("lock, save-update, delete"); //$NON-NLS-1$
                    propertyElement.getAttributes().setNamedItem(cascade);
                }
                Attr tableName = document.createAttribute("table"); //$NON-NLS-1$
                tableName.setValue(resolver.getCollectionTable(referenceField));
                propertyElement.getAttributes().setNamedItem(tableName);
                {
                    // <key column="foo_id"/> (one per key in referenced entity).
                    Element key = document.createElement("key"); //$NON-NLS-1$
                    propertyElement.appendChild(key);
                    for (FieldMetadata keyField : referenceField.getContainingType().getKeyFields()) {
                        Element elementColumn = document.createElement("column"); //$NON-NLS-1$
                        Attr columnName = document.createAttribute("name"); //$NON-NLS-1$
                        columnName.setValue(resolver.get(keyField));
                        elementColumn.getAttributes().setNamedItem(columnName);
                        key.appendChild(elementColumn);
                    }
                    // <index column="pos" />
                    Element index = document.createElement("index"); //$NON-NLS-1$
                    Attr indexColumn = document.createAttribute("column"); //$NON-NLS-1$
                    indexColumn.setValue("pos"); //$NON-NLS-1$
                    index.getAttributes().setNamedItem(indexColumn);
                    propertyElement.appendChild(index);
                    // many to many element
                    Element manyToMany = newManyToManyElement(enforceDataBaseIntegrity, referenceField);
                    propertyElement.appendChild(manyToMany);
                }
                return propertyElement;
            }
        }
    }

    private Element newManyToOneElement(ReferenceFieldMetadata referencedField, boolean enforceDataBaseIntegrity) {
        Element propertyElement = document.createElement("many-to-one"); //$NON-NLS-1$
        Attr propertyName = document.createAttribute("name"); //$NON-NLS-1$
        propertyName.setValue(referencedField.getName());
        Attr className = document.createAttribute("class"); //$NON-NLS-1$
        className.setValue(ClassCreator.getClassName(referencedField.getReferencedType().getName()));
        // fetch="join" lazy="false"
        Attr lazy = document.createAttribute("lazy"); //$NON-NLS-1$
        lazy.setValue("proxy"); //$NON-NLS-1$
        propertyElement.getAttributes().setNamedItem(lazy);
        Attr joinAttribute = document.createAttribute("fetch"); //$NON-NLS-1$
        joinAttribute.setValue("join"); //$NON-NLS-1$
        propertyElement.getAttributes().setNamedItem(joinAttribute);
        // foreign-key="..."
        String fkConstraintName = resolver.getFkConstraintName(referencedField);
        if (!fkConstraintName.isEmpty()) {
            Attr foreignKeyConstraintName = document.createAttribute("foreign-key"); //$NON-NLS-1$
            foreignKeyConstraintName.setValue(fkConstraintName);
            propertyElement.getAttributes().setNamedItem(foreignKeyConstraintName);
            Attr indexName = document.createAttribute("index"); //$NON-NLS-1$
            indexName.setValue("FK_" + fkConstraintName); //$NON-NLS-1$
            propertyElement.getAttributes().setNamedItem(indexName);
        }
        // Not null
        if (referencedField.isMandatory() && generateConstrains) {
            Attr notNull = document.createAttribute("not-null"); //$NON-NLS-1$
            notNull.setValue("true"); //$NON-NLS-1$
            propertyElement.getAttributes().setNamedItem(notNull);
        } else {
            Attr notNull = document.createAttribute("not-null"); //$NON-NLS-1$
            notNull.setValue("false"); //$NON-NLS-1$
            propertyElement.getAttributes().setNamedItem(notNull);
        }
        // If data model authorizes fk integrity override, don't enforce database FK integrity.
        if (enforceDataBaseIntegrity) {
            // Ensure default settings for Hibernate are set (in case they change).
            Attr notFound = document.createAttribute("not-found"); //$NON-NLS-1$
            notFound.setValue("exception"); //$NON-NLS-1$
            propertyElement.getAttributes().setNamedItem(notFound);
        } else {
            // Disables all warning/errors from Hibernate.
            Attr integrity = document.createAttribute("unique"); //$NON-NLS-1$
            integrity.setValue("false"); //$NON-NLS-1$
            propertyElement.getAttributes().setNamedItem(integrity);

            Attr foreignKey = document.createAttribute("foreign-key"); //$NON-NLS-1$*
            // Disables foreign key generation for DDL.
            foreignKey.setValue("none");  //$NON-NLS-1$
            propertyElement.getAttributes().setNamedItem(foreignKey);

            Attr notFound = document.createAttribute("not-found"); //$NON-NLS-1$
            notFound.setValue("ignore"); //$NON-NLS-1$
            propertyElement.getAttributes().setNamedItem(notFound);
        }
        propertyElement.getAttributes().setNamedItem(propertyName);
        propertyElement.getAttributes().setNamedItem(className);
        // Cascade delete
        if (Boolean.parseBoolean(referencedField.<String>getData(SQL_DELETE_CASCADE))) {
            Attr cascade = document.createAttribute("cascade"); //$NON-NLS-1$
            cascade.setValue("lock, save-update, delete"); //$NON-NLS-1$
            propertyElement.getAttributes().setNamedItem(cascade);
        }
        isDoingColumns = true;
        isColumnMandatory = referencedField.isMandatory() && generateConstrains;
        this.parentElement = propertyElement;
        compositeKeyPrefix = referencedField.getName();
        {
            referencedField.getReferencedField().accept(this);
        }
        isDoingColumns = false;
        return propertyElement;
    }

    private Element newManyToManyElement(boolean enforceDataBaseIntegrity, ReferenceFieldMetadata referencedField) {
        // <many-to-many column="bar_id" class="Bar"/>
        Element manyToMany = document.createElement("many-to-many"); //$NON-NLS-1$
        // If data model authorizes fk integrity override, don't enforce database FK integrity.
        if (enforceDataBaseIntegrity) {
            // Ensure default settings for Hibernate are set (in case they change).
            Attr notFound = document.createAttribute("not-found"); //$NON-NLS-1$
            notFound.setValue("exception"); //$NON-NLS-1$
            manyToMany.getAttributes().setNamedItem(notFound);
            // foreign-key="..."
            String fkConstraintName = resolver.getFkConstraintName(referencedField);
            if (!fkConstraintName.isEmpty()) {
                Attr foreignKeyConstraintName = document.createAttribute("foreign-key"); //$NON-NLS-1$
                foreignKeyConstraintName.setValue(fkConstraintName);
                manyToMany.getAttributes().setNamedItem(foreignKeyConstraintName);
            }
        } else {
            // Disables all warning/errors from Hibernate.
            Attr integrity = document.createAttribute("unique"); //$NON-NLS-1$
            integrity.setValue("false"); //$NON-NLS-1$
            manyToMany.getAttributes().setNamedItem(integrity);

            Attr foreignKey = document.createAttribute("foreign-key"); //$NON-NLS-1$
            // Disables foreign key generation for DDL.
            foreignKey.setValue("none"); //$NON-NLS-1$
            manyToMany.getAttributes().setNamedItem(foreignKey);

            Attr notFound = document.createAttribute("not-found"); //$NON-NLS-1$
            notFound.setValue("ignore"); //$NON-NLS-1$
            manyToMany.getAttributes().setNamedItem(notFound);
        }
        Attr className = document.createAttribute("class"); //$NON-NLS-1$
        className.setValue(ClassCreator.getClassName(referencedField.getReferencedType().getName()));
        manyToMany.getAttributes().setNamedItem(className);
        isDoingColumns = true;
        this.parentElement = manyToMany;
        isColumnMandatory = referencedField.isMandatory() && generateConstrains;
        compositeKeyPrefix = referencedField.getName();
        {
            referencedField.getReferencedField().accept(this);
        }
        isDoingColumns = false;
        return manyToMany;
    }

    @Override
    public Element visit(SimpleTypeFieldMetadata simpleField) {
        return handleSimpleField(simpleField);
    }

    private Element handleSimpleField(FieldMetadata field) {
        if (isDoingColumns) {
            Element column = document.createElement("column"); //$NON-NLS-1$
            Attr columnName = document.createAttribute("name"); //$NON-NLS-1$
            String columnNameValue = resolver.get(field, compositeKeyPrefix);
            columnName.setValue(columnNameValue); 
            column.getAttributes().setNamedItem(columnName);
            // TMDM-9003: should add not-null="false" explicitly if not-null isn't "true" for many-to-many scenario 
            Attr notNull = document.createAttribute("not-null"); //$NON-NLS-1$
            if (generateConstrains && isColumnMandatory) {
                notNull.setValue(Boolean.TRUE.toString());
            } else {
                notNull.setValue(Boolean.FALSE.toString());
            }
            column.getAttributes().setNamedItem(notNull);
            
            if (resolver.isIndexed(field)) { // Create indexes for fields that should be indexed.
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Creating index for field '" + field.getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                }
                Attr indexName = document.createAttribute("index"); //$NON-NLS-1$
                setIndexName(field, columnNameValue, indexName);
                column.getAttributes().setNamedItem(indexName);
            }
            parentElement.appendChild(column);
            return column;
        }
        if (field.isKey()) {
            Element idElement;
            if (!compositeId) {
                idElement = document.createElement("id"); //$NON-NLS-1$
                if (Types.UUID.equals(field.getType().getName()) && ScatteredMappingCreator.GENERATED_ID.equals(field.getName())) {
                    // <generator class="org.hibernate.id.UUIDGenerator"/>
                    Element generator = document.createElement("generator"); //$NON-NLS-1$
                    Attr generatorClass = document.createAttribute("class"); //$NON-NLS-1$
                    generatorClass.setValue("org.hibernate.id.UUIDGenerator"); //$NON-NLS-1$
                    generator.getAttributes().setNamedItem(generatorClass);
                    idElement.appendChild(generator);
                }
            } else {
                idElement = document.createElement("key-property"); //$NON-NLS-1$
            }
            Attr idName = document.createAttribute("name"); //$NON-NLS-1$
            idName.setValue(field.getName());
            Attr columnName = document.createAttribute("column"); //$NON-NLS-1$
            columnName.setValue(resolver.get(field));

            idElement.getAttributes().setNamedItem(idName);
            idElement.getAttributes().setNamedItem(columnName);
            if (field.getType().getData(MetadataRepository.DATA_MAX_LENGTH) != null) {
                Attr length = document.createAttribute("length"); //$NON-NLS-1$
                String value = field.getType().<String> getData(MetadataRepository.DATA_MAX_LENGTH);
                length.setValue(value);
                idElement.getAttributes().setNamedItem(length);
            }
            return idElement;
        } else {
            if (!field.isMany()) {
                Element propertyElement = document.createElement("property"); //$NON-NLS-1$
                Attr propertyName = document.createAttribute("name"); //$NON-NLS-1$
                propertyName.setValue(field.getName());
                Element columnElement = document.createElement("column"); //$NON-NLS-1$
                Attr columnName = document.createAttribute("name"); //$NON-NLS-1$
                columnName.setValue(resolver.get(field));

                if (resolver.isIndexed(field)) { // Create indexes for fields that should be indexed.
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Creating index for field '" + field.getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    Attr indexName = document.createAttribute("index"); //$NON-NLS-1$
                    setIndexName(field, field.getName(), indexName);
                    propertyElement.getAttributes().setNamedItem(indexName);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("*Not* creating index for field '" + field.getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                // Not null
                Attr notNull = document.createAttribute("not-null"); //$NON-NLS-1$
                if (generateConstrains && field.isMandatory()) {
                    notNull.setValue(Boolean.TRUE.toString());
                } else {
                    notNull.setValue(Boolean.FALSE.toString());
                }
                columnElement.getAttributes().setNamedItem(notNull);
                // default value
                addDefaultValueAttribute(field, columnElement);

                addFieldTypeAttribute(field, columnElement, propertyElement, dataSource.getDialectName());
                propertyElement.getAttributes().setNamedItem(propertyName);
                columnElement.getAttributes().setNamedItem(columnName);
                propertyElement.appendChild(columnElement);
                return propertyElement;
            } else {
                Element listElement = document.createElement("list"); //$NON-NLS-1$
                Attr name = document.createAttribute("name"); //$NON-NLS-1$
                name.setValue(field.getName());
                Attr tableName = document.createAttribute("table"); //$NON-NLS-1$
                tableName.setValue(resolver.getCollectionTable(field));
                listElement.getAttributes().setNamedItem(tableName);
                if (field.getContainingType().getKeyFields().size() == 1) {
                    // lazy="extra"
                    Attr lazyAttribute = document.createAttribute("lazy"); //$NON-NLS-1$
                    if (fromMemoryStorage) {
                        lazyAttribute.setValue("false"); //$NON-NLS-1$
                    } else {
                        lazyAttribute.setValue("extra"); //$NON-NLS-1$
                    }
                    listElement.getAttributes().setNamedItem(lazyAttribute);
                    // fetch="join"
                    Attr fetchAttribute = document.createAttribute("fetch"); //$NON-NLS-1$
                    fetchAttribute.setValue("join"); //$NON-NLS-1$
                    listElement.getAttributes().setNamedItem(fetchAttribute);
                    // inverse="true"
                    Attr inverse = document.createAttribute("inverse"); //$NON-NLS-1$
                    inverse.setValue("false"); //$NON-NLS-1$
                    listElement.getAttributes().setNamedItem(inverse);
                } else {
                    /*
                     * Hibernate does not handle correctly reverse collection when main entity owns multiple keys.
                     */
                    // lazy="false"
                    Attr lazyAttribute = document.createAttribute("lazy"); //$NON-NLS-1$
                    lazyAttribute.setValue("false"); //$NON-NLS-1$
                    listElement.getAttributes().setNamedItem(lazyAttribute);
                    // In case containing type has > 1 key, switch to fetch="select" since Hibernate returns incorrect
                    // results in case of fetch="join".
                    Attr fetchAttribute = document.createAttribute("fetch"); //$NON-NLS-1$
                    fetchAttribute.setValue("select"); //$NON-NLS-1$
                    listElement.getAttributes().setNamedItem(fetchAttribute);
                    // batch-size="20"
                    Attr batchSize = document.createAttribute("batch-size"); //$NON-NLS-1$
                    batchSize.setValue("20"); //$NON-NLS-1$
                    listElement.getAttributes().setNamedItem(batchSize);
                }
                // cascade="delete"
                Attr cascade = document.createAttribute("cascade"); //$NON-NLS-1$
                cascade.setValue("lock, all-delete-orphan"); //$NON-NLS-1$
                listElement.getAttributes().setNamedItem(cascade);
                // Keys
                Element key = document.createElement("key"); //$NON-NLS-1$
                Collection<FieldMetadata> keyFields = field.getContainingType().getKeyFields();
                for (FieldMetadata keyField : keyFields) {
                    Element column = document.createElement("column"); //$NON-NLS-1$
                    Attr columnName = document.createAttribute("name"); //$NON-NLS-1$
                    column.getAttributes().setNamedItem(columnName);
                    columnName.setValue(resolver.get(keyField));
                    key.appendChild(column);
                }
                // <element column="name" type="string"/>
                Element element = document.createElement("element"); //$NON-NLS-1$
                Element columnElement = document.createElement("column"); //$NON-NLS-1$
                Attr columnNameAttr = document.createAttribute("name"); //$NON-NLS-1$
                columnNameAttr.setValue("value"); //$NON-NLS-1$

                // default value
                addDefaultValueAttribute(field, columnElement);

                columnElement.getAttributes().setNamedItem(columnNameAttr);
                addFieldTypeAttribute(field, columnElement, element, dataSource.getDialectName());
                element.appendChild(columnElement);
                // Not null warning
                if (field.isMandatory()) {
                    LOGGER.warn("Field '" + field.getName() + "' is mandatory and a collection. Constraint can not be expressed in database schema."); //$NON-NLS-1$ //$NON-NLS-2$
                }
                // <index column="pos" />
                Element index = document.createElement("list-index"); //$NON-NLS-1$
                Attr indexColumn = document.createAttribute("column"); //$NON-NLS-1$
                indexColumn.setValue("pos"); //$NON-NLS-1$
                index.getAttributes().setNamedItem(indexColumn);
                listElement.getAttributes().setNamedItem(name);
                listElement.appendChild(key);
                listElement.appendChild(index);
                listElement.appendChild(element);
                return listElement;
            }
        }
    }

    private void addDefaultValueAttribute(FieldMetadata field, Element columnElement) {
        // default value
        String defaultValue = field.<String> getData(MetadataRepository.DEFAULT_VALUE);
        if (StringUtils.isNotBlank(defaultValue)) {

            Attr defaultValueAttr = document.createAttribute("default"); //$NON-NLS-1$
            //TMDM-14012 fixed the block of code to parse the XPath, and got the correct field type Name
            String fieldName = MetadataUtils.getSuperConcreteType(field.getType()).getName();
            if (!fieldName.equals(TypeMapping.SQL_TYPE_BOOLEAN)
                    || HibernateStorageUtils.isBooleanDefaultValue(fieldName, defaultValue.trim())) {
                defaultValueAttr.setValue(HibernateStorageUtils.convertedDefaultValue(fieldName, dataSource.getDialectName(), defaultValue.trim(), "'")); //$NON-NLS-1$
                columnElement.getAttributes().setNamedItem(defaultValueAttr);
            }
        }
    }

    private void setIndexName(FieldMetadata field, String fieldName, Attr indexName) {
        String prefix = field.getContainingType().getName();
        if (!tableNames.isEmpty() && field.getContainingType().getSuperTypes().isEmpty()) {
            prefix = tableNames.peek();
        }
        indexName.setValue(resolver.getIndex(fieldName, prefix)); //
    }

    private static void addFieldTypeAttribute(FieldMetadata field, Element columnElement, Element propertyElement,
            RDBMSDataSource.DataSourceDialect dialect) {
        Document document = columnElement.getOwnerDocument();
        Document propertyDocument = propertyElement.getOwnerDocument();
        Attr elementType = propertyDocument.createAttribute("type"); //$NON-NLS-1$
        TypeMetadata fieldType = field.getType();
        String elementTypeName;
        
        boolean isMultiLingualOrBase64Binary = Types.MULTI_LINGUAL.equalsIgnoreCase(fieldType.getName())
                || Types.BASE64_BINARY.equalsIgnoreCase(fieldType.getName());
        Object maxLength = CommonUtil.getSuperTypeMaxLength(fieldType, fieldType);

        if (isMultiLingualOrBase64Binary) {
            elementTypeName = TypeMapping.SQL_TYPE_TEXT;
        } else {
            Object sqlType = fieldType.getData(TypeMapping.SQL_TYPE);
            if (sqlType != null) { // SQL Type may enforce use of "CLOB" iso. "LONG VARCHAR"
                elementTypeName = String.valueOf(sqlType);
                if (dialect == RDBMSDataSource.DataSourceDialect.DB2) {
                    Attr length = document.createAttribute("length"); //$NON-NLS-1$
                    length.setValue("1048576"); //$NON-NLS-1$ 1MB CLOB limit for DB2
                    columnElement.getAttributes().setNamedItem(length);
                }
            } else if (maxLength != null) {
                String maxLengthValue = String.valueOf(maxLength);
                int maxLengthInt = Integer.parseInt(maxLengthValue);
                if (maxLengthInt > dialect.getTextLimit()) {
                    elementTypeName = TypeMapping.SQL_TYPE_TEXT;
                } else {
                    Attr length = document.createAttribute("length"); //$NON-NLS-1$
                    length.setValue(maxLengthValue);
                    columnElement.getAttributes().setNamedItem(length);
                    elementTypeName = HibernateMetadataUtils.getJavaType(fieldType);
                }
            } else if (fieldType.getData(MetadataRepository.DATA_TOTAL_DIGITS) != null
                    || fieldType.getData(MetadataRepository.DATA_FRACTION_DIGITS) != null) { // TMDM-8022

                Object totalDigits = fieldType.getData(MetadataRepository.DATA_TOTAL_DIGITS);
                Object fractionDigits = fieldType.getData(MetadataRepository.DATA_FRACTION_DIGITS);

                if (totalDigits != null) {
                    int totalDigitsInt = Integer.parseInt(totalDigits.toString());
                    totalDigitsInt = totalDigitsInt > dialect.getDecimalPrecision() ? dialect.getDecimalPrecision()
                            : totalDigitsInt;

                    String totalDigitsValue = String.valueOf(totalDigitsInt);
                    Attr length = document.createAttribute("precision"); //$NON-NLS-1$
                    length.setValue(totalDigitsValue);
                    columnElement.getAttributes().setNamedItem(length);
                }

                if (fractionDigits != null) {
                    int fractionDigitsInt = Integer.parseInt(fractionDigits.toString());
                    fractionDigitsInt = fractionDigitsInt >= dialect.getDecimalScale() ? dialect.getDecimalScale()
                            : fractionDigitsInt;

                    String fractionDigitsValue = String.valueOf(fractionDigitsInt);
                    Attr length = document.createAttribute("scale"); //$NON-NLS-1$
                    length.setValue(fractionDigitsValue);
                    columnElement.getAttributes().setNamedItem(length);
                }
                elementTypeName = HibernateMetadataUtils.getJavaType(fieldType);
            } else {
                elementTypeName = HibernateMetadataUtils.getJavaType(fieldType);
            }
        }
        // TMDM-4975: Oracle doesn't like when there are too many LONG columns.
        if (dialect == RDBMSDataSource.DataSourceDialect.ORACLE_10G && TypeMapping.SQL_TYPE_TEXT.equals(elementTypeName)) {
            if (field.getType().getData(LongString.PREFER_LONGVARCHAR) == null && isMultiLingualOrBase64Binary == false) {
                elementTypeName = TypeMapping.SQL_TYPE_CLOB;
            } else {// DATA_CLUSTER_POJO.X_VOCABULARY, X_TALEND_STAGING_ERROR, X_TALEND_STAGING_VALUES, and
                    // Types.MULTI_LINGUAL, Types.BASE64_BINARY still use VARCHAR2(4000 CHAR)
                elementTypeName = "string"; //$NON-NLS-1$
                Attr length = document.createAttribute("length"); //$NON-NLS-1$
                length.setValue("4000"); //$NON-NLS-1$
                columnElement.getAttributes().setNamedItem(length);
            }
        }
        elementType.setValue(elementTypeName);
        propertyElement.getAttributes().setNamedItem(elementType);
    }
}