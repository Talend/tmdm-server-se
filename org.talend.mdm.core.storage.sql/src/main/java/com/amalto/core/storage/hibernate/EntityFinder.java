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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Sort;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.graph.RootGraph;
import org.hibernate.query.ParameterMetadata;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.spi.QueryProducerImplementor;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.filter.FullTextFilter;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.ObjectLookupMethod;
import org.hibernate.search.query.engine.spi.FacetManager;
import org.hibernate.search.spatial.Coordinates;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.InboundReferences;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

public class EntityFinder {

    private EntityFinder() {
    }

    /**
     * Starting from <code>wrapper</code>, goes up the containment tree using references introspection in metadata.
     * @param wrapper A {@link Wrapper} instance (so an object managed by {@link HibernateStorage}.
     * @param storage A {@link HibernateStorage} instance. It will be used to compute references from the internal
     *                data model.
     * @param session A Hibernate {@link Session}.
     * @return The top level (aka the Wrapper instance that represent a MDM entity).
     */
    @SuppressWarnings({ "deprecation", "rawtypes" })
    public static Wrapper findEntity(Wrapper wrapper, HibernateStorage storage, Session session) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (!(contextClassLoader instanceof StorageClassLoader)) {
            throw new IllegalStateException("Expects method to be called in the context of a storage operation.");
        }
        StorageClassLoader classLoader = (StorageClassLoader) contextClassLoader;
        ComplexTypeMetadata wrapperType = classLoader.getTypeFromClass(wrapper.getClass());
        if (wrapperType == null) {
            throw new IllegalArgumentException("Wrapper '" + wrapper.getClass().getName() + "' isn't known in current storage.");
        }
        if (wrapperType.isInstantiable()) {
            return wrapper;
        }
        InboundReferences incomingReferences = new InboundReferences(wrapperType);
        InternalRepository internalRepository = storage.getTypeEnhancer();
        Set<ReferenceFieldMetadata> references = internalRepository.getInternalRepository().accept(incomingReferences);
        if (references.isEmpty()) {
            throw new IllegalStateException("Cannot find container type for '" + wrapperType.getName() + "'.");
        }
        String keyFieldName = wrapperType.getKeyFields().iterator().next().getName();
        Object id = wrapper.get(keyFieldName);
        for (ReferenceFieldMetadata reference : references) {
            ComplexTypeMetadata containingType = reference.getContainingType();
            Class<? extends Wrapper> clazz = classLoader.getClassFromType(containingType);
            Criteria criteria = session.createCriteria(clazz, "a0"); //$NON-NLS-1$
            criteria.createAlias("a0." + reference.getName(), "a1", CriteriaSpecification.INNER_JOIN); //$NON-NLS-1$
            criteria.add(Restrictions.eq("a1." + keyFieldName, id)); //$NON-NLS-1$
            List list = criteria.list();
            if (!list.isEmpty()) {
                Wrapper container = (Wrapper) list.get(0);
                if (list.size() > 1) {
                    Object previousItem = list.get(0);
                    for(int i = 1; i < list.size(); i++) {
                        Object currentItem = list.get(i);
                        if(!previousItem.equals(currentItem)) {
                            throw new IllegalStateException("Expected contained instance to have only one owner.");
                        }
                        previousItem = currentItem;
                    }
                }
                return findEntity(container, storage, session);
            }
        }
        return null;
    }

    /**
     * Wraps a {@link FullTextQuery} so it returns only "top level" Hibernate objects (iso. of possible technical objects).
     * This method ensures all methods that returns results will return expected results.
     *
     * @see org.hibernate.Query#scroll()
     * @param query The full text query to wrap.
     * @param storage The {@link HibernateStorage} implementation used to perform the query.
     * @param session A open, read for immediate usage Hibernate {@link Session}.
     * @return A wrapper that implements and supports all methods of {@link FullTextQuery}.
     */
    public static FullTextQuery wrap(FullTextQuery query, HibernateStorage storage, Session session, List<ComplexTypeMetadata> types) {
        return new QueryWrapper(query, storage, session, types);
    }

    private static class ScrollableResultsWrapper implements ScrollableResults {

        private final ScrollableResults scrollableResults;

        private final HibernateStorage storage;

        private final Session session;

        public ScrollableResultsWrapper(ScrollableResults scrollableResults, HibernateStorage storage, Session session) {
            this.scrollableResults = scrollableResults;
            this.storage = storage;
            this.session = session;
        }

        @Override
        public boolean next() throws HibernateException {
            return scrollableResults.next();
        }

        @Override
        public boolean previous() throws HibernateException {
            return scrollableResults.previous();
        }

        @Override
        public boolean scroll(int i) throws HibernateException {
            return scrollableResults.scroll(i);
        }

        @Override
        public boolean last() throws HibernateException {
            return scrollableResults.last();
        }

        @Override
        public boolean first() throws HibernateException {
            return scrollableResults.first();
        }

        @Override
        public void beforeFirst() throws HibernateException {
            scrollableResults.beforeFirst();
        }

        @Override
        public void afterLast() throws HibernateException {
            scrollableResults.afterLast();
        }

        @Override
        public boolean isFirst() throws HibernateException {
            return scrollableResults.isFirst();
        }

        @Override
        public boolean isLast() throws HibernateException {
            return scrollableResults.isLast();
        }

        @Override
        public void close() throws HibernateException {
            scrollableResults.close();
        }

        @Override
        public Object[] get() throws HibernateException {
            Object[] objects = scrollableResults.get();
            Object[] entities = new Object[objects.length];
            int i = 0;
            for (Object object : objects) {
                entities[i++] = EntityFinder.findEntity((Wrapper) object, storage, session);
            }
            return entities;
        }

        @Override
        public Object get(int i) throws HibernateException {
            return EntityFinder.findEntity((Wrapper) scrollableResults.get(i), storage, session);
        }

        @Override
        public Type getType(int i) {
            return scrollableResults.getType(i);
        }

        @Override
        public Integer getInteger(int col) throws HibernateException {
            return scrollableResults.getInteger(col);
        }

        @Override
        public Long getLong(int col) throws HibernateException {
            return scrollableResults.getLong(col);
        }

        @Override
        public Float getFloat(int col) throws HibernateException {
            return scrollableResults.getFloat(col);
        }

        @Override
        public Boolean getBoolean(int col) throws HibernateException {
            return scrollableResults.getBoolean(col);
        }

        @Override
        public Double getDouble(int col) throws HibernateException {
            return scrollableResults.getDouble(col);
        }

        @Override
        public Short getShort(int col) throws HibernateException {
            return scrollableResults.getShort(col);
        }

        @Override
        public Byte getByte(int col) throws HibernateException {
            return scrollableResults.getByte(col);
        }

        @Override
        public Character getCharacter(int col) throws HibernateException {
            return scrollableResults.getCharacter(col);
        }

        @Override
        public byte[] getBinary(int col) throws HibernateException {
            return scrollableResults.getBinary(col);
        }

        @Override
        public String getText(int col) throws HibernateException {
            return scrollableResults.getText(col);
        }

        @Override
        public Blob getBlob(int col) throws HibernateException {
            return scrollableResults.getBlob(col);
        }

        @Override
        public Clob getClob(int col) throws HibernateException {
            return scrollableResults.getClob(col);
        }

        @Override
        public String getString(int col) throws HibernateException {
            return scrollableResults.getString(col);
        }

        @Override
        public BigDecimal getBigDecimal(int col) throws HibernateException {
            return scrollableResults.getBigDecimal(col);
        }

        @Override
        public BigInteger getBigInteger(int col) throws HibernateException {
            return scrollableResults.getBigInteger(col);
        }

        @Override
        public Date getDate(int col) throws HibernateException {
            return scrollableResults.getDate(col);
        }

        @Override
        public Locale getLocale(int col) throws HibernateException {
            return scrollableResults.getLocale(col);
        }

        @Override
        public Calendar getCalendar(int col) throws HibernateException {
            return scrollableResults.getCalendar(col);
        }

        @Override
        public TimeZone getTimeZone(int col) throws HibernateException {
            return scrollableResults.getTimeZone(col);
        }

        @Override
        public int getRowNumber() throws HibernateException {
            return scrollableResults.getRowNumber();
        }

        @Override
        public boolean setRowNumber(int rowNumber) throws HibernateException {
            return scrollableResults.setRowNumber(rowNumber);
        }
    }

    @SuppressWarnings("rawtypes")
    private static class IteratorWrapper implements Iterator {

        private final Iterator iterator;

        private final HibernateStorage storage;

        private final Session session;

        public IteratorWrapper(Iterator iterator, HibernateStorage storage, Session session) {
            this.iterator = iterator;
            this.storage = storage;
            this.session = session;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Object next() {
            return EntityFinder.findEntity((Wrapper) iterator.next(), storage, session);
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class QueryWrapper implements FullTextQuery {

        private final FullTextQuery query;

        private final HibernateStorage storage;

        private final Session session;

        private List<String> entityClassName = new ArrayList<String>();
        
        public QueryWrapper(FullTextQuery query, HibernateStorage storage, Session session, List<ComplexTypeMetadata> types) {
            this.query = query;
            this.storage = storage;
            this.session = session;
            if (types != null && types.size() > 0) {
                for (ComplexTypeMetadata ctm : types) {
                    entityClassName.add(ClassCreator.getClassName(ctm.getName()));
                    if (ctm.getSubTypes() != null) {
                        for (ComplexTypeMetadata subType : ctm.getSubTypes()) {
                            entityClassName.add(ClassCreator.getClassName(subType.getName()));
                        }
                    }
                }
            }
        }
        
        @Override
        public int getResultSize() {
            return query.getResultSize();
        }

        @Override
        public FullTextFilter enableFullTextFilter(String name) {
            return query.enableFullTextFilter(name);
        }

        @Override
        public void disableFullTextFilter(String name) {
            query.disableFullTextFilter(name);
        }

        @Override
        public FacetManager getFacetManager() {
           return query.getFacetManager();
        }

        @Override
        public Explanation explain(int documentId) {
            return query.explain(documentId);
        }

        @Override
        public boolean hasPartialResults() {
            return query.hasPartialResults();
        }

        @Override
        public List getResultList() {
            return query.getResultList();
        }

        @Override
        public Object getSingleResult() {
            return query.getSingleResult();
        }

        @Override
        public int executeUpdate() {
            return query.executeUpdate();
        }

        @Override
        public int getMaxResults() {
            return query.getMaxResults();
        }

        @Override
        public int getFirstResult() {
            return query.getFirstResult();
        }

        @Override
        public Map<String, Object> getHints() {
            return query.getHints();
        }

        @Override
        public Set<Parameter<?>> getParameters() {
            return query.getParameters();
        }

        @Override
        public Parameter<?> getParameter(String name) {
            return query.getParameter(name);
        }

        @Override
        public <T> Parameter<T> getParameter(String name, Class<T> type) {
            return query.getParameter(name, type);
        }

        @Override
        public Parameter<?> getParameter(int position) {
            return query.getParameter(position);
        }

        @Override
        public <T> Parameter<T> getParameter(int position, Class<T> type) {
            return query.getParameter(position, type);
        }

        @Override
        public boolean isBound(Parameter<?> param) {
           return query.isBound(param);
        }

        @Override
        public <T> T getParameterValue(Parameter<T> param) {
            return query.getParameterValue(param);
        }

        @Override
        public Object getParameterValue(String name) {
            return query.getParameterValue(name);
        }

        @Override
        public Object getParameterValue(int position) {
            return query.getParameterValue(position);
        }

        @Override
        public FlushModeType getFlushMode() {
            return query.getFlushMode();
        }

        @Override
        public LockModeType getLockMode() {
            return query.getLockMode();
        }

        @Override
        public QueryProducerImplementor getProducer() {
            return query.getProducer();
        }

        @Override
        public void setOptionalId(Serializable id) {
            query.setOptionalId(id);
        }

        @Override
        public void setOptionalEntityName(String entityName) {
           query.setOptionalEntityName(entityName);
        }

        @Override
        public void setOptionalObject(Object optionalObject) {
            query.setOptionalObject(optionalObject);
        }

        @Override
        public Optional uniqueResultOptional() {
            return query.uniqueResultOptional();
        }

        @Override
        public Stream stream() {
            return query.stream();
        }

        @Override
        public org.hibernate.query.Query applyGraph(RootGraph graph, GraphSemantic semantic) {
            return query.applyGraph(graph, semantic);
        }

        @Override
        public org.hibernate.query.Query setParameter(Parameter param, Instant value, TemporalType temporalType) {
            return query.setParameter(param, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(Parameter param, LocalDateTime value, TemporalType temporalType) {
            return query.setParameter(param, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(Parameter param, ZonedDateTime value, TemporalType temporalType) {
            return query.setParameter(param, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(Parameter param, OffsetDateTime value, TemporalType temporalType) {
            return query.setParameter(param, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(String name, Instant value, TemporalType temporalType) {
            return query.setParameter(name, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(String name, LocalDateTime value, TemporalType temporalType) {
            return query.setParameter(name, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(String name, ZonedDateTime value, TemporalType temporalType) {
            return query.setParameter(name, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(String name, OffsetDateTime value, TemporalType temporalType) {
            return query.setParameter(name, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(int position, Instant value, TemporalType temporalType) {
            return query.setParameter(position, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(int position, LocalDateTime value, TemporalType temporalType) {
            return query.setParameter(position, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(int position, ZonedDateTime value, TemporalType temporalType) {
            return query.setParameter(position, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(int position, OffsetDateTime value, TemporalType temporalType) {
            return query.setParameter(position, value, temporalType);
        }

        @Override
        public ScrollableResults scroll() {
            ScrollableResults scrollableResults = query.scroll();
            return new ScrollableResultsWrapper(scrollableResults, storage, session);
        }

        @Override
        public ScrollableResults scroll(ScrollMode scrollMode) {
            ScrollableResults scrollableResults = query.scroll(scrollMode);
            return new ScrollableResultsWrapper(scrollableResults, storage, session);
        }

        @Override
        public List list() {
            List<Wrapper> list = query.list();
            Set<Wrapper> newSet = new ListOrderedSet();
            for (Object item : list) {
                Wrapper element = EntityFinder.findEntity((Wrapper) item, storage, session);
                if (element != null) {
                    if (entityClassName.size() > 0) {
                        String elementName = element.getClass().getName();
                        if (this.entityClassName.contains(elementName)) {
                            newSet.add(element);
                        } else if (elementName.contains("_$$")) {
                            if (this.entityClassName.contains(elementName.subSequence(0, elementName.indexOf("_$$")))) {
                                newSet.add(element);
                            }
                        }
                    } else {
                        newSet.add(element);
                    }
                }
            }
            return new ArrayList<Wrapper>(newSet);
        }

        @Override
        public Object uniqueResult() {
            return query.uniqueResult();
        }

        @Override
        public FlushMode getHibernateFlushMode() {
            return query.getHibernateFlushMode();
        }

        @Override
        public CacheMode getCacheMode() {
            return query.getCacheMode();
        }

        @Override
        public String getCacheRegion() {
            return query.getCacheRegion();
        }

        @Override
        public Integer getFetchSize() {
            return query.getFetchSize();
        }

        @Override
        public LockOptions getLockOptions() {
            return query.getLockOptions();
        }

        @Override
        public String getComment() {
            return query.getComment();
        }

        @Override
        public String getQueryString() {
            return query.getQueryString();
        }

        @Override
        public ParameterMetadata getParameterMetadata() {
           return query.getParameterMetadata();
        }

        @Override
        public org.hibernate.query.Query setParameter(Parameter param, Object value) {
            throw new UnsupportedOperationException( "parameters not supported in fullText queries" );
        }

        @Override
        public org.hibernate.query.Query setParameter(Parameter param, Calendar value, TemporalType temporalType) {
            throw new UnsupportedOperationException( "parameters not supported in fullText queries" );
        }

        @Override
        public org.hibernate.query.Query setParameter(Parameter param, Date value, TemporalType temporalType) {
            throw new UnsupportedOperationException( "parameters not supported in fullText queries" );
        }

        @Override
        public org.hibernate.query.Query setParameter(String name, Object value) {
            return query.setParameter(name, value);
        }

        @Override
        public org.hibernate.query.Query setParameter(String name, Object val, Type type) {
            return query.setParameter(name, val, type);
        }

        @Override
        public org.hibernate.query.Query setParameter(String name, Calendar value, TemporalType temporalType) {
            return query.setParameter(name, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(String name, Date value, TemporalType temporalType) {
            return query.setParameter(name, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(int position, Object value) {
            return query.setParameter(position, value);
        }

        @Override
        public org.hibernate.query.Query setParameter(int position, Calendar value, TemporalType temporalType) {
            return query.setParameter(position, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(int position, Date value, TemporalType temporalType) {
            return query.setParameter(position, value, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(QueryParameter parameter, Object val) {
            throw new UnsupportedOperationException( "parameters not supported in fullText queries" );
        }

        @Override
        public org.hibernate.query.Query setParameter(int position, Object val, TemporalType temporalType) {
            return query.setParameter(position, val, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(QueryParameter parameter, Object val, Type type) {
            return query.setParameter(parameter, val, type);
        }

        @Override
        public org.hibernate.query.Query setParameter(int position, Object val, Type type) {
            return query.setParameter(position, val, type);
        }

        @Override
        public org.hibernate.query.Query setParameter(QueryParameter parameter, Object val, TemporalType temporalType) {
            return query.setParameter(parameter, val, temporalType);
        }

        @Override
        public org.hibernate.query.Query setParameter(String name, Object val, TemporalType temporalType) {
            return query.setParameter(name, val, temporalType);
        }

        @Override
        public org.hibernate.query.Query setLockMode(LockModeType lockMode) {
            return query.setLockMode(lockMode);
        }

        @Override
        public org.hibernate.query.Query setReadOnly(boolean readOnly) {
            return query.setReadOnly(readOnly);
        }

        @Override
        public org.hibernate.query.Query setHibernateFlushMode(FlushMode flushMode) {
            return query.setHibernateFlushMode(flushMode);
        }

        @Override
        public org.hibernate.query.Query setCacheMode(CacheMode cacheMode) {
            return query.setCacheMode(cacheMode);
        }

        @Override
        public org.hibernate.query.Query setCacheable(boolean cacheable) {
            return query.setCacheable(cacheable);
        }

        @Override
        public org.hibernate.query.Query setCacheRegion(String cacheRegion) {
            return query.setCacheRegion(cacheRegion);
        }

        @Override
        public org.hibernate.query.Query setTimeout(int timeout) {
            return query.setTimeout(timeout);
        }

        @Override
        public org.hibernate.query.Query setLockOptions(LockOptions lockOptions) {
            return query.setLockOptions(lockOptions);
        }

        @Override
        public org.hibernate.query.Query setLockMode(String alias, LockMode lockMode) {
            return query.setLockMode(alias, lockMode);
        }

        @Override
        public org.hibernate.query.Query setComment(String comment) {
            return query.setComment(comment);
        }

        @Override
        public org.hibernate.query.Query addQueryHint(String hint) {
            return query.addQueryHint(hint);
        }

        @Override
        public org.hibernate.query.Query setParameterList(QueryParameter parameter, Collection values) {
            return query.setParameterList(parameter, values);
        }

        @Override
        public org.hibernate.query.Query setParameterList(String name, Collection values) {
            return query.setParameterList(name, values);
        }

        @Override
        public org.hibernate.query.Query setParameterList(String name, Collection values, Type type) {
            return query.setParameterList(name, values, type);
        }

        @Override
        public org.hibernate.query.Query setParameterList(String name, Object[] values, Type type) {
            return query.setParameterList(name, values, type);
        }

        @Override
        public org.hibernate.query.Query setParameterList(String name, Object[] values) {
            return query.setParameterList(name, values);
        }

        @Override
        public org.hibernate.query.Query setProperties(Object bean) {
            return query.setProperties(bean);
        }

        @Override
        public org.hibernate.query.Query setProperties(Map bean) {
            return query.setProperties(bean);
        }

        @Override
        @Deprecated
        public org.hibernate.query.Query setEntity(int position, Object val) {
            return query.setEntity(position, val);
        }

        @Override
        @Deprecated
        public org.hibernate.query.Query setEntity(String name, Object val) {
            return query.setEntity(name, val);
        }

        @Override
        @Deprecated
        public RowSelection getQueryOptions() {
            return query.getQueryOptions();
        }

        @Override
        @Deprecated
        public boolean isCacheable() {
            return query.isCacheable();
        }

        @Override
        @Deprecated
        public Integer getTimeout() {
            return query.getTimeout();
        }

        @Override
        @Deprecated
        public boolean isReadOnly() {
            return query.isReadOnly();
        }

        @Override
        @Deprecated
        public Type[] getReturnTypes() {
            return query.getReturnTypes();
        }

        @Override
        @Deprecated
        public Iterator iterate() {
            Iterator iterator = query.iterate();
            return new IteratorWrapper(iterator, storage, session);
        }

        @Override
        @Deprecated
        public String[] getNamedParameters() {
            return query.getNamedParameters();
        }

        @Override
        @Deprecated
        public Query setParameterList(int position, Collection values) {
            return query.setParameterList(position, values);
        }

        @Override
        @Deprecated
        public Query setParameterList(int position, Collection values, Type type) {
            return query.setParameterList(position, values, type);
        }

        @Override
        @Deprecated
        public Query setParameterList(int position, Object[] values, Type type) {
            return query.setParameterList(position, values);
        }

        @Override
        @Deprecated
        public Query setParameterList(int position, Object[] values) {
            return query.setParameterList(position, values);
        }

        @Override
        @Deprecated
        public Type determineProperBooleanType(int position, Object value, Type defaultType) {
            return query.determineProperBooleanType(position, value, defaultType);
        }

        @Override
        @Deprecated
        public Type determineProperBooleanType(String name, Object value, Type defaultType) {
            return query.determineProperBooleanType(name, value, defaultType);
        }

        @Override
        @Deprecated
        public String[] getReturnAliases() {
            return query.getReturnAliases();
        }

        @Override
        @Deprecated
        public FullTextQuery setResultTransformer(ResultTransformer transformer) {
            return query.setResultTransformer(transformer);
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            return query.unwrap(type);
        }

        @Override
        public FullTextQuery setSort(Sort sort) {
            return query.setSort(sort);
        }

        @Override
        @Deprecated
        public FullTextQuery setFilter(Filter filter) {
           return query.setFilter(filter);
        }

        @Override
        public FullTextQuery setCriteriaQuery(Criteria criteria) {
            return query.setCriteriaQuery(criteria);
        }

        @Override
        public FullTextQuery setProjection(String... fields) {
            return query.setProjection(fields);
        }

        @Override
        public FullTextQuery setSpatialParameters(double latitude, double longitude, String fieldName) {
            return query.setSpatialParameters(latitude, longitude, fieldName);
        }

        @Override
        public FullTextQuery setSpatialParameters(Coordinates center, String fieldName) {
            return query.setSpatialParameters(center, fieldName);
        }

        @Override
        public FullTextQuery setFirstResult(int firstResult) {
            return query.setFirstResult(firstResult);
        }

        @Override
        public FullTextQuery setMaxResults(int maxResults) {
            return query.setMaxResults(maxResults);
        }

        @Override
        public FullTextQuery setHint(String hintName, Object value) {
            return query.setHint(hintName, value);
        }

        @Override
        public FullTextQuery setFlushMode(FlushModeType flushMode) {
            return query.setFlushMode(flushMode);
        }

        @Override
        public FullTextQuery setFetchSize(int i) {
            return query.setFetchSize(i);
        }

        @Override
        public FullTextQuery setTimeout(long timeout, TimeUnit timeUnit) {
            return query.setTimeout(timeout, timeUnit);
        }

        @Override
        public FullTextQuery limitExecutionTimeTo(long timeout, TimeUnit timeUnit) {
            return query.limitExecutionTimeTo(timeout, timeUnit);
        }

        @Override
        public FullTextQuery initializeObjectsWith(ObjectLookupMethod lookupMethod, DatabaseRetrievalMethod retrievalMethod) {
            return query.initializeObjectsWith(lookupMethod, retrievalMethod);
        }

    }
}
