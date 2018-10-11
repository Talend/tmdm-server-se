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

package com.amalto.core.storage.hibernate;

import com.amalto.core.storage.StorageMetadataUtils;
import org.hibernate.engine.spi.TypedValue;
import org.talend.mdm.commmon.metadata.*;
import com.amalto.core.query.user.*;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;

import java.util.*;

abstract class AbstractQueryHandler extends VisitorAdapter<StorageResults> {

    static final Criterion TRUE_CRITERION = new Criterion() {
        public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            return "1=1"; //$NON-NLS-1$
        }

        public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            return new TypedValue[0];
        }
    };

    static final Criterion FALSE_CRITERION = new Criterion() {
        public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            return "1=0"; //$NON-NLS-1$
        }

        public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            return new TypedValue[0];
        }
    };


    static final int JDBC_FETCH_SIZE = 20;

    final ValueAdapter VALUE_ADAPTER = new ValueAdapter();

    final Session session;

    final Storage storage;

    final StorageClassLoader storageClassLoader;

    final Select select;

    final List<TypedExpression> selectedFields;

    final Set<ResultsCallback> callbacks;

    AbstractQueryHandler(Storage storage,
            StorageClassLoader storageClassLoader,
            Session session,
            Select select,
            List<TypedExpression> selectedFields,
            Set<ResultsCallback> callbacks) {
        this.storageClassLoader = storageClassLoader;
        this.session = session;
        this.storage = storage;
        this.select = select;
        this.callbacks = callbacks;
        this.selectedFields = selectedFields;
    }

    String getFieldName(Field field) {
        return getFieldName(field.getFieldMetadata(), true);
    }

    String getFieldName(FieldMetadata fieldMetadata, boolean includeTypeName) {
        // Move up to the first complex type (contained type do not have any mapping).
        TypeMetadata containingType = fieldMetadata.getContainingType().getEntity();
        if (containingType == null) {
            throw new IllegalStateException("Could not find containing type mapping for field '" + fieldMetadata.getName() + "'.");
        }
        String fieldName;
        if (fieldMetadata instanceof ReferenceFieldMetadata && !fieldMetadata.isMany()) { // Handle query on FK field
            FieldMetadata referencedField = ((ReferenceFieldMetadata) fieldMetadata).getReferencedField();
            if (!(referencedField instanceof CompoundFieldMetadata)) {
                // If asked to resolve referenced field (to return "country.id" instead of "country" -> useful for
                // conditions on FKs).
                fieldName = fieldMetadata.getName() + '.' + getFieldName(referencedField, false);
            } else {
                fieldName = fieldMetadata.getName();
            }
        } else { // Simple field
            fieldName = fieldMetadata.getName();
        }

        if (includeTypeName) {
            return fieldMetadata.getContainingType().getName() + '.' + fieldName;
        } else {
            return fieldName;
        }
    }

    @Override
    public StorageResults visit(Condition condition) {
        return null;
    }

    @Override
    public StorageResults visit(Distinct distinct) {
        return null;
    }

    class ValueAdapter extends VisitorAdapter<Object> {

        @Override
        public Object visit(Id id) {
            ComplexTypeMetadata type = id.getType();
            Collection<FieldMetadata> keyFields = type.getKeyFields();
            List<String> ids = new LinkedList<String>();
            StringBuilder builder = null;
            if (!id.isExpressionList()) {
                String idAsString = id.getId();
                if (idAsString.startsWith("[")) { //$NON-NLS-1$
                    for (char currentChar : idAsString.toCharArray()) {
                        switch (currentChar) {
                        case '[':
                            builder = new StringBuilder();
                            break;
                        case ']':
                            if (builder != null) {
                                ids.add(builder.toString());
                            }
                            break;
                        default:
                            if (builder != null) {
                                builder.append(currentChar);
                            }
                            break;
                        }
                    }
                    if (ids.isEmpty()) {
                        throw new IllegalArgumentException("Id '" + idAsString
                                + "' does not match expected format (no id found).");
                    }
                } else {
                    ids.add(idAsString);
                }
                Iterator<FieldMetadata> iterator = keyFields.iterator();
                if (ids.size() == 1) {
                    return StorageMetadataUtils.convert(ids.get(0), iterator.next());
                } else {
                    Object[] convertedId = new Object[ids.size()];
                    for (int i = 0; i < ids.size(); i++) {
                        convertedId[i] = StorageMetadataUtils.convert(ids.get(i), iterator.next());
                    }
                    return convertedId;
                }
            } else {
                ids = id.getIdList();
                Iterator<FieldMetadata> iterator = keyFields.iterator();
                return StorageMetadataUtils.convert(ids, iterator.next());
            }
        }

        @Override
        public Object visit(Condition condition) {
            return null;
        }

        @Override
        public Object visit(Compare condition) {
            return condition.getRight().accept(this);
        }

        @Override
        public Object visit(ConstantCollection collection) {
            Object[] values = new Object[collection.getValues().length];
            int i = 0;
            for (Expression expression : collection.getValues()) {
                values[i++] = expression.accept(this);
            }
            return values;
        }

        @Override
        public Object visit(StringConstant constant) {
            return constant.getValueObject();
        }

        @Override
        public Object visit(IntegerConstant constant) {
            return constant.getValueObject();
        }

        @Override
        public Object visit(DateConstant constant) {
            return StorageMetadataUtils.convert(constant);
        }

        @Override
        public Object visit(DateTimeConstant constant) {
            return StorageMetadataUtils.convert(constant);
        }

        @Override
        public Object visit(BooleanConstant constant) {
            return constant.getValueObject();
        }

        @Override
        public Object visit(BigDecimalConstant constant) {
            return constant.getValueObject();
        }

        @Override
        public Object visit(TimeConstant constant) {
            return StorageMetadataUtils.convert(constant);
        }

        @Override
        public Object visit(ShortConstant constant) {
            return constant.getValueObject();
        }

        @Override
        public Object visit(ByteConstant constant) {
            return constant.getValueObject();
        }

        @Override
        public Object visit(LongConstant constant) {
            return constant.getValueObject();
        }

        @Override
        public Object visit(DoubleConstant constant) {
            return constant.getValueObject();
        }

        @Override
        public Object visit(FloatConstant constant) {
            return constant.getValueObject();
        }

        @Override
        public Object visit(Field field) {
            // If this happens, it is very likely wrong visitor was used (see FIELD_VISITOR).
            throw new UnsupportedOperationException("Can not compare to an other field value.");
        }
    }

    protected static class FieldCondition {
        boolean isComputedProperty;

        boolean isMany;

        List<String> criterionFieldNames = new LinkedList<String>();

        FieldMetadata fieldMetadata;

        Field field;

        boolean isProperty;

        int position = -1;
    }
}
