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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.query.user.Alias;
import com.amalto.core.query.user.BigDecimalConstant;
import com.amalto.core.query.user.BinaryLogicOperator;
import com.amalto.core.query.user.BooleanConstant;
import com.amalto.core.query.user.ByteConstant;
import com.amalto.core.query.user.Compare;
import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.DoubleConstant;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.Field;
import com.amalto.core.query.user.FieldFullText;
import com.amalto.core.query.user.FloatConstant;
import com.amalto.core.query.user.FullText;
import com.amalto.core.query.user.IntegerConstant;
import com.amalto.core.query.user.LongConstant;
import com.amalto.core.query.user.Predicate;
import com.amalto.core.query.user.Range;
import com.amalto.core.query.user.ShortConstant;
import com.amalto.core.query.user.StringConstant;
import com.amalto.core.query.user.TimeConstant;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UnaryLogicOperator;
import com.amalto.core.query.user.VisitorAdapter;
import com.amalto.core.query.user.metadata.MetadataField;
import com.amalto.core.query.user.metadata.StagingBlockKey;
import com.amalto.core.query.user.metadata.StagingError;
import com.amalto.core.query.user.metadata.StagingHasTask;
import com.amalto.core.query.user.metadata.StagingSource;
import com.amalto.core.query.user.metadata.StagingStatus;
import com.amalto.core.query.user.metadata.TaskId;
import com.amalto.core.query.user.metadata.Timestamp;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.exception.FullTextQueryCompositeKeyException;
import com.amalto.core.storage.exception.UnsupportedFullTextQueryException;

class LuceneQueryGenerator extends VisitorAdapter<Query> {

    private static final String FUZZY_SEARCH = "lucene.fuzzy.search"; //$NON-NLS-1$

    private final Collection<ComplexTypeMetadata> types;

    private List<TypedExpression> viewableFields;

    private String currentFieldName;

    private Object currentValue;

    private boolean isBuildingNot;

    LuceneQueryGenerator(Collection<ComplexTypeMetadata> types) {
        this.types = types;
    }

    LuceneQueryGenerator(List<TypedExpression> viewableFields, Collection<ComplexTypeMetadata> types) {
        this.types = types;
        this.viewableFields = viewableFields;
    }

    @Override
    public Query visit(Compare condition) {
        condition.getLeft().accept(this);
        Expression right = condition.getRight();
        right.accept(this);
        if (condition.getPredicate() == Predicate.EQUALS || condition.getPredicate() == Predicate.CONTAINS
                || condition.getPredicate() == Predicate.STARTS_WITH) {
            String searchValue = String.valueOf(currentValue);
            BooleanQuery termQuery = new BooleanQuery();
            if(searchValue != null && searchValue.startsWith("\'") && searchValue.endsWith("\'")){ //$NON-NLS-1$ //$NON-NLS-2$
                PhraseQuery query = new PhraseQuery();
                StringTokenizer tokenizer = new StringTokenizer(searchValue.substring(1, searchValue.length()-1));
                while (tokenizer.hasMoreTokens()) {
                    query.add(new Term(currentFieldName, tokenizer.nextToken().toLowerCase()));
                }
                termQuery.add(query, BooleanClause.Occur.SHOULD);
            } else {
                StringTokenizer tokenizer = new StringTokenizer(searchValue);
                while (tokenizer.hasMoreTokens()) {
                    TermQuery newTermQuery = new TermQuery(new Term(currentFieldName, tokenizer.nextToken().toLowerCase()));
                    termQuery.add(newTermQuery, isBuildingNot ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                    if (condition.getPredicate() == Predicate.STARTS_WITH) {
                        break;
                    }
                }
            }
            return termQuery;
        } else if (condition.getPredicate() == Predicate.GREATER_THAN
                || condition.getPredicate() == Predicate.GREATER_THAN_OR_EQUALS
                || condition.getPredicate() == Predicate.LOWER_THAN || condition.getPredicate() == Predicate.LOWER_THAN_OR_EQUALS) {
            throw new RuntimeException("Greater than, less than are not supported in full text searches."); //$NON-NLS-1$
        } else {
            throw new NotImplementedException("No support for predicate '" + condition.getPredicate() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public Query visit(BinaryLogicOperator condition) {
        Query left = condition.getLeft().accept(this);
        Query right = condition.getRight().accept(this);
        BooleanQuery query = new BooleanQuery();
        if (condition.getPredicate() == Predicate.OR) {
            query.add(left, BooleanClause.Occur.SHOULD);
            query.add(right, BooleanClause.Occur.SHOULD);
        } else if (condition.getPredicate() == Predicate.AND) {
            query.add(left, isNotQuery(left) ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST);
            query.add(right, isNotQuery(right) ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST);
        } else {
            throw new NotImplementedException("No support for '" + condition.getPredicate() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return query;
    }

    private static boolean isNotQuery(Query left) {
        if (left instanceof BooleanQuery) {
            for (BooleanClause booleanClause : ((BooleanQuery) left).getClauses()) {
                if (booleanClause.getOccur() == BooleanClause.Occur.MUST_NOT) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Query visit(UnaryLogicOperator condition) {
        if (condition.getPredicate() == Predicate.NOT) {
            isBuildingNot = true;
            Query query = condition.getCondition().accept(this);
            isBuildingNot = false;
            return query;
        } else {
            throw new NotImplementedException("No support for predicate '" + condition.getPredicate() + "'."); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    @Override
    public Query visit(Range range) {
        if (range.getExpression() instanceof MetadataField) {
            if (range.getExpression() instanceof Timestamp) {
                Timestamp field = (Timestamp) range.getExpression();
                field.accept(this);
            } else {
                MetadataField field = (MetadataField) range.getExpression();
                field.getProjectionExpression().accept(this);
            }
        }
        range.getStart().accept(this);
        Long currentRangeStart = ((Long) currentValue) == Long.MIN_VALUE ? null : (Long) currentValue;
        range.getEnd().accept(this);
        Long currentRangeEnd = ((Long) currentValue) == Long.MAX_VALUE ? null : (Long) currentValue;
        return NumericRangeQuery.newLongRange(currentFieldName, currentRangeStart, currentRangeEnd, true, true);
    }

    @Override
    public Query visit(Timestamp timestamp) {
        currentFieldName = Storage.METADATA_TIMESTAMP;
        return null;
    }

    @Override
    public Query visit(StagingStatus stagingStatus) {
        currentFieldName = Storage.METADATA_STAGING_STATUS;
        return null;
    }

    @Override
    public Query visit(TaskId taskId) {
        currentFieldName = Storage.METADATA_TASK_ID;
        return null;
    }

    @Override
    public Query visit(StagingError stagingError) {
        currentFieldName = Storage.METADATA_STAGING_ERROR;
        return null;
    }

    @Override
    public Query visit(StagingSource stagingSource) {
        currentFieldName = Storage.METADATA_STAGING_SOURCE;
        return null;
    }

    @Override
    public Query visit(StagingBlockKey stagingBlockKey) {
        currentFieldName = Storage.METADATA_STAGING_BLOCK_KEY;
        return null;
    }

    @Override
    public Query visit(StagingHasTask stagingHasTask) {
        currentFieldName = Storage.METADATA_STAGING_HAS_TASK;
        return null;
    }

    @Override
    public Query visit(Field field) {
        currentFieldName = field.getFieldMetadata().getName();
        return null;
    }

    @Override
    public Query visit(Alias alias) {
        currentFieldName = alias.getAliasName();
        return null;
    }

    @Override
    public Query visit(StringConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(IntegerConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(DateConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(DateTimeConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(BooleanConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(BigDecimalConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(TimeConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(ShortConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(ByteConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(LongConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(DoubleConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(FloatConstant constant) {
        currentValue = constant.getValue();
        return null;
    }

    @Override
    public Query visit(final FullText fullText) {
        // TODO Test me on conditions where many types share same field names.
        final Map<String, Boolean> fieldsMap = new HashMap<String, Boolean>();
        final Set<String> processedTypeNames = new HashSet<String>();
        for (final ComplexTypeMetadata type : types) {
            if (!type.isInstantiable()) {
                continue;
            }
            String typeName = type.getName();
            if (processedTypeNames.contains(typeName)) {
                continue;
            } else {
                processedTypeNames.add(typeName);
            }
            type.accept(new DefaultMetadataVisitor<Void>() {
                private String prefix = StringUtils.EMPTY;
                @Override
                public Void visit(ContainedComplexTypeMetadata containedType) {
                    super.visit(containedType);
                    for (ComplexTypeMetadata subType : containedType.getSubTypes()) {
                        subType.accept(this);
                    }
                    return null;
                }

                @Override
                public Void visit(ReferenceFieldMetadata referenceField) {
                    ComplexTypeMetadata referencedType = referenceField.getReferencedType();
                    String typeName = referencedType.getName();
                    if (processedTypeNames.contains(typeName)) {
                        return null;
                    } else {
                        processedTypeNames.add(typeName);
                    }
                    //to support associated entities lucene query
                    if (StringUtils.isNotEmpty(referenceField.getPath())) {
                        prefix = referenceField.getPath().replace("/", ".") + ".";
                    }
                    referencedType.accept(this);
                    prefix = StringUtils.EMPTY;
                    return null;
                }

                @Override
                public Void visit(SimpleTypeFieldMetadata simpleField) {
                    if (!Storage.METADATA_TIMESTAMP.equals(simpleField.getName())
                            && !Storage.METADATA_TASK_ID.equals(simpleField.getName())) {
                        if (StorageMetadataUtils.isValueSearchable(fullText.getValue(), simpleField) && isFieldSearchable(simpleField)) {
                            fieldsMap.put(prefix + simpleField.getName(), simpleField.isKey());
                        }
                    }
                    return null;
                }

                @Override
                public Void visit(EnumerationFieldMetadata enumField) {
                    if (StorageMetadataUtils.isValueAssignable(fullText.getValue(), enumField) && isFieldSearchable(enumField)) {
                        fieldsMap.put(prefix + enumField.getName(), enumField.isKey());
                    }
                    return null;
                }
            });
        }

        processedTypeNames.clear();
        String[] fieldsAsArray = fieldsMap.keySet().toArray(new String[fieldsMap.size()]);
        StringBuilder queryBuffer = new StringBuilder();
        Iterator<Map.Entry<String, Boolean>> fieldsIterator = fieldsMap.entrySet().iterator();
        String fullTextValue = getFullTextValue(fullText);
        BooleanQuery query = new BooleanQuery();
        Query idQuery = null;
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, Boolean> next = fieldsIterator.next();
            if (next.getValue()) {
                queryBuffer.append(next.getKey()).append(ToLowerCaseFieldBridge.ID_POSTFIX + ':').append(fullTextValue);
                idQuery = new PrefixQuery(new Term(next.getKey(), fullText.getValue()));
            } else {
                queryBuffer.append(next.getKey()).append(':').append(fullTextValue);
            }
            if (fieldsIterator.hasNext()) {
                queryBuffer.append(" OR "); //$NON-NLS-1$
            }
        }

        String fullTextQuery = queryBuffer.toString();
        if (idQuery != null) {
            query.add(idQuery, BooleanClause.Occur.SHOULD);
        }
        query.add(parseQuery(fieldsAsArray, fullTextQuery, fullText.getValue()), BooleanClause.Occur.SHOULD);
        return query;
    }

    @Override
    public Query visit(FieldFullText fieldFullText) {
        FieldMetadata fieldMetadata = fieldFullText.getField().getFieldMetadata();
        String fieldName = fieldMetadata.getName();
        if (fieldMetadata instanceof ReferenceFieldMetadata) {
            ReferenceFieldMetadata referenceFieldMetadata = ((ReferenceFieldMetadata) fieldMetadata);
            if (referenceFieldMetadata.getReferencedType().getKeyFields().size() > 1) {
                throw new FullTextQueryCompositeKeyException(referenceFieldMetadata.getReferencedType().getName());
            } else {
                fieldName = fieldName + "." + referenceFieldMetadata.getReferencedField().getName(); //$NON-NLS-1$
            }
        }
        String[] fieldsAsArray = new String[] { fieldName };
        String fullTextValue = getFullTextValue(fieldFullText);
        String fullTextQuery = fieldName + ':' + fullTextValue;
        if (fieldFullText.getField().getFieldMetadata().isKey()) {
            BooleanQuery query = new BooleanQuery();
            query.add(new PrefixQuery(new Term(fieldName, fieldFullText.getValue())), BooleanClause.Occur.SHOULD);
            fieldsAsArray = new String[] { fieldName + ToLowerCaseFieldBridge.ID_POSTFIX };
            fullTextQuery = fieldName + ToLowerCaseFieldBridge.ID_POSTFIX + ":" + fullTextValue; //$NON-NLS-1$
            query.add(parseQuery(fieldsAsArray, fullTextQuery, fieldFullText.getValue()), BooleanClause.Occur.SHOULD);
            return query;
        }
        return parseQuery(fieldsAsArray, fullTextQuery, fieldFullText.getValue());
    }

    private Query parseQuery(String[] fieldsAsArray, String fullTextQuery, String keywords) {
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fieldsAsArray, new StandardAnalyzer());
        // Very important! Lucene does an implicit lower case for "expanded terms" (which is something used).
        parser.setLowercaseExpandedTerms(true);
        try {
            return parser.parse(fullTextQuery);
        } catch (Exception e) {
            if (org.apache.lucene.queryparser.classic.ParseException.class.isInstance(e)) {
                throw new UnsupportedFullTextQueryException("'" + keywords + "' is unsupported keywords", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            throw new RuntimeException("Invalid generated Lucene query", e); //$NON-NLS-1$
        }
    }

    private static String getFullTextValue(FullText fullText) {
        return getSearchTextValue(fullText.getValue().toLowerCase().trim());
    }

    private static String getSearchTextValue(String value) {
        int index = 0;
        while (value.charAt(index) == '*') { // Skip '*' characters at beginning.
            index++;
        }
        if (index > 0) {
            value = value.substring(index);
        }

        boolean isFuzzySearch = isFuzzySearch(value);

        char tilde = '~'; //$NON-NLS-1$
        char[] removes = new char[] { '[', ']', '+', '!', '(', ')', '^', '\"', tilde, ':', ';', '\\', '-', '@', '#', '$', '%', '&',
                '=', ',', '.', '<', '>' }; // Removes reserved characters

        String fuzzyTerm = StringUtils.EMPTY;
        String queryTerm = value;
        if (isFuzzySearch) {
            fuzzyTerm = value.substring(value.lastIndexOf(tilde));
            queryTerm = value.substring(0, value.lastIndexOf(tilde));
        }
        for (char remove : removes) {
            queryTerm = queryTerm.replace(remove, ' '); //$NON-NLS-1$
        }
        value = queryTerm.trim() + fuzzyTerm;
        if (value != null && value.length() > 1 && value.startsWith("'") && value.endsWith("'")) { //$NON-NLS-1$//$NON-NLS-2$
            value = "\"" + value.substring(1, value.length() - 1) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            if (value.contains(" ")) { //$NON-NLS-1$
                return getMultiKeywords(value, isFuzzySearch);
            } else {
                if (!value.endsWith("*") && !isFuzzySearch) { //$NON-NLS-1$
                    value += '*'; //$NON-NLS-1$
                }
            }
        }
        return value;
    }

    /**
     * 1. default value for configuration <b>lucene.fuzzy.search</b> is {@code true}, if not configuration it, also
     * return {@code true}, only specific configuration it to false, will return {@code false} <br/>
     * 
     * 2. if the <i>value</i> ending ~ and ~0.8(less than 1 and include 0 and 1),
     * <i>value</i>{@code .matches(}<i>\w*?~((0(\.\d)?)|1)?</i>{@code )} return {@code true} <br/>
     * 
     * <pre>
     * "roam~".matches("\\w*?~((0(\\.\\d)?)|1)?")       = true
     * "roam~ ".matches("\\w*?~((0(\\.\\d)?)|1)?")      = true
     * "roam~1".matches("\\w*?~((0(\\.\\d)?)|1)?")      = true
     * "roam~0".matches("\\w*?~((0(\\.\\d)?)|1)?")      = true
     * "roam~0.5".matches("\\w*?~((0(\\.\\d)?)|1)?")    = true
     * "roam~2".matches("\\w*?~((0(\\.\\d)?)|1)?")      = false
     * </pre>
     * 
     * Only configuration of lucene.fuzzy.search is true and the <i>value</i> is ending ~ and ~decimal of less than 1,
     * will return {@code true}
     * 
     * @param value
     * @return true if, value ending ~ and ~decimal of less than 1 and configuration of lucene.fuzzy.search is true
     */
    private static boolean isFuzzySearch(String value) {
        boolean enableFuzzySearch = Boolean.parseBoolean(MDMConfiguration.getConfiguration().getProperty(FUZZY_SEARCH, "true")); //$NON-NLS-1$
        return value.matches("\\w*?~((0(\\.\\d)?)|1)?") && enableFuzzySearch; //$NON-NLS-1$
    }

    private static String getMultiKeywords(String value, boolean isFuzzySearch) {
        List<String> blocks = new ArrayList<String>(Arrays.asList(value.split(" "))); //$NON-NLS-1$
        StringBuffer sb = new StringBuffer();
        for (String block : blocks) {
            if (StringUtils.isNotEmpty(block)) {
                if (!block.endsWith("*") && !isFuzzySearch) { //$NON-NLS-1$
                    sb.append(block + "* "); //$NON-NLS-1$
                } else {
                    sb.append(block + " "); //$NON-NLS-1$
                }
            }
        }
        return sb.toString();
    }

    private boolean isFieldSearchable(FieldMetadata currentField) {
        if (viewableFields == null || viewableFields.isEmpty()) {//global search
            return true;
        }
        for (TypedExpression expression : viewableFields) {
            boolean results = false;
            results = expression.accept(new VisitorAdapter<Boolean>() {
                public Boolean visit(Field field) {
                    FieldMetadata fieldMetadata = ((Field) expression).getFieldMetadata();
                    return fieldMetadata.accept(new DefaultMetadataVisitor<Boolean>() {

                        public Boolean visit(ReferenceFieldMetadata referenceField) {
                            if (referenceField.getReferencedType().getName().equals(currentField.getContainingType().getName())
                                    && referenceField.getReferencedField().getName().equals(currentField.getName())) {
                                return true;
                            }
                            return false;
                        }

                        public Boolean visit(SimpleTypeFieldMetadata simpleType) {
                            if (simpleType.getContainingType().getName().equals(currentField.getContainingType().getName())
                                    && simpleType.getName().equals(currentField.getName())) {
                                return true;
                            }
                            return false;
                        }
                    });
                }

                public Boolean visit(Alias alias) {
                    TypedExpression internalTypedExpression = ((Alias) expression).getTypedExpression();
                    if (internalTypedExpression instanceof Field) {
                        FieldMetadata fieldMetadata = ((Field) internalTypedExpression).getFieldMetadata();
                        if (fieldMetadata.getContainingType().getName().equals(currentField.getContainingType().getName())
                                && fieldMetadata.getName().equals(currentField.getName())) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            if (results) {
                return true;
            }
        }
        return false;
    }
}
