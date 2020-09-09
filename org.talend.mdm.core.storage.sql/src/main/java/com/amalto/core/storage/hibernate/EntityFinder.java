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
        
        /* (non-Javadoc)
         * @see org.hibernate.search.jpa.FullTextQuery#getResultSize()
         */
        @Override
        public int getResultSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.jpa.FullTextQuery#enableFullTextFilter(java.lang.String)
         */
        @Override
        public FullTextFilter enableFullTextFilter(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.jpa.FullTextQuery#disableFullTextFilter(java.lang.String)
         */
        @Override
        public void disableFullTextFilter(String name) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.jpa.FullTextQuery#getFacetManager()
         */
        @Override
        public FacetManager getFacetManager() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.jpa.FullTextQuery#explain(int)
         */
        @Override
        public Explanation explain(int documentId) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.jpa.FullTextQuery#hasPartialResults()
         */
        @Override
        public boolean hasPartialResults() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getResultList()
         */
        @Override
        public List getResultList() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getSingleResult()
         */
        @Override
        public Object getSingleResult() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#executeUpdate()
         */
        @Override
        public int executeUpdate() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getMaxResults()
         */
        @Override
        public int getMaxResults() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getFirstResult()
         */
        @Override
        public int getFirstResult() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getHints()
         */
        @Override
        public Map<String, Object> getHints() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getParameters()
         */
        @Override
        public Set<Parameter<?>> getParameters() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getParameter(java.lang.String)
         */
        @Override
        public Parameter<?> getParameter(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getParameter(java.lang.String, java.lang.Class)
         */
        @Override
        public <T> Parameter<T> getParameter(String name, Class<T> type) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getParameter(int)
         */
        @Override
        public Parameter<?> getParameter(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getParameter(int, java.lang.Class)
         */
        @Override
        public <T> Parameter<T> getParameter(int position, Class<T> type) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#isBound(javax.persistence.Parameter)
         */
        @Override
        public boolean isBound(Parameter<?> param) {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getParameterValue(javax.persistence.Parameter)
         */
        @Override
        public <T> T getParameterValue(Parameter<T> param) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getParameterValue(java.lang.String)
         */
        @Override
        public Object getParameterValue(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getParameterValue(int)
         */
        @Override
        public Object getParameterValue(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getFlushMode()
         */
        @Override
        public FlushModeType getFlushMode() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.persistence.Query#getLockMode()
         */
        @Override
        public LockModeType getLockMode() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.spi.QueryImplementor#getProducer()
         */
        @Override
        public QueryProducerImplementor getProducer() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.spi.QueryImplementor#setOptionalId(java.io.Serializable)
         */
        @Override
        public void setOptionalId(Serializable id) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.spi.QueryImplementor#setOptionalEntityName(java.lang.String)
         */
        @Override
        public void setOptionalEntityName(String entityName) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.spi.QueryImplementor#setOptionalObject(java.lang.Object)
         */
        @Override
        public void setOptionalObject(Object optionalObject) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#uniqueResultOptional()
         */
        @Override
        public Optional uniqueResultOptional() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#stream()
         */
        @Override
        public Stream stream() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#applyGraph(org.hibernate.graph.RootGraph, org.hibernate.graph.GraphSemantic)
         */
        @Override
        public org.hibernate.query.Query applyGraph(RootGraph graph, GraphSemantic semantic) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(javax.persistence.Parameter, java.time.Instant, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(Parameter param, Instant value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(javax.persistence.Parameter, java.time.LocalDateTime, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(Parameter param, LocalDateTime value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(javax.persistence.Parameter, java.time.ZonedDateTime, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(Parameter param, ZonedDateTime value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(javax.persistence.Parameter, java.time.OffsetDateTime, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(Parameter param, OffsetDateTime value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(java.lang.String, java.time.Instant, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(String name, Instant value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(java.lang.String, java.time.LocalDateTime, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(String name, LocalDateTime value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(java.lang.String, java.time.ZonedDateTime, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(String name, ZonedDateTime value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(java.lang.String, java.time.OffsetDateTime, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(String name, OffsetDateTime value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(int, java.time.Instant, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(int position, Instant value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(int, java.time.LocalDateTime, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(int position, LocalDateTime value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(int, java.time.ZonedDateTime, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(int position, ZonedDateTime value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(int, java.time.OffsetDateTime, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(int position, OffsetDateTime value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#scroll()
         */
        @Override
        public ScrollableResults scroll() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#scroll(org.hibernate.ScrollMode)
         */
        @Override
        public ScrollableResults scroll(ScrollMode scrollMode) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#list()
         */
        @Override
        public List list() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#uniqueResult()
         */
        @Override
        public Object uniqueResult() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#getHibernateFlushMode()
         */
        @Override
        public FlushMode getHibernateFlushMode() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#getCacheMode()
         */
        @Override
        public CacheMode getCacheMode() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#getCacheRegion()
         */
        @Override
        public String getCacheRegion() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#getFetchSize()
         */
        @Override
        public Integer getFetchSize() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#getLockOptions()
         */
        @Override
        public LockOptions getLockOptions() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#getComment()
         */
        @Override
        public String getComment() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#getQueryString()
         */
        @Override
        public String getQueryString() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#getParameterMetadata()
         */
        @Override
        public ParameterMetadata getParameterMetadata() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(javax.persistence.Parameter, java.lang.Object)
         */
        @Override
        public org.hibernate.query.Query setParameter(Parameter param, Object value) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(javax.persistence.Parameter, java.util.Calendar, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(Parameter param, Calendar value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(javax.persistence.Parameter, java.util.Date, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(Parameter param, Date value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(java.lang.String, java.lang.Object)
         */
        @Override
        public org.hibernate.query.Query setParameter(String name, Object value) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(java.lang.String, java.lang.Object, org.hibernate.type.Type)
         */
        @Override
        public org.hibernate.query.Query setParameter(String name, Object val, Type type) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(java.lang.String, java.util.Calendar, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(String name, Calendar value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(java.lang.String, java.util.Date, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(String name, Date value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(int, java.lang.Object)
         */
        @Override
        public org.hibernate.query.Query setParameter(int position, Object value) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(int, java.util.Calendar, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(int position, Calendar value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(int, java.util.Date, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(int position, Date value, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(org.hibernate.query.QueryParameter, java.lang.Object)
         */
        @Override
        public org.hibernate.query.Query setParameter(QueryParameter parameter, Object val) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(int, java.lang.Object, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(int position, Object val, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(org.hibernate.query.QueryParameter, java.lang.Object, org.hibernate.type.Type)
         */
        @Override
        public org.hibernate.query.Query setParameter(QueryParameter parameter, Object val, Type type) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(int, java.lang.Object, org.hibernate.type.Type)
         */
        @Override
        public org.hibernate.query.Query setParameter(int position, Object val, Type type) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(org.hibernate.query.QueryParameter, java.lang.Object, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(QueryParameter parameter, Object val, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameter(java.lang.String, java.lang.Object, javax.persistence.TemporalType)
         */
        @Override
        public org.hibernate.query.Query setParameter(String name, Object val, TemporalType temporalType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setLockMode(javax.persistence.LockModeType)
         */
        @Override
        public org.hibernate.query.Query setLockMode(LockModeType lockMode) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setReadOnly(boolean)
         */
        @Override
        public org.hibernate.query.Query setReadOnly(boolean readOnly) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setHibernateFlushMode(org.hibernate.FlushMode)
         */
        @Override
        public org.hibernate.query.Query setHibernateFlushMode(FlushMode flushMode) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setCacheMode(org.hibernate.CacheMode)
         */
        @Override
        public org.hibernate.query.Query setCacheMode(CacheMode cacheMode) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setCacheable(boolean)
         */
        @Override
        public org.hibernate.query.Query setCacheable(boolean cacheable) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setCacheRegion(java.lang.String)
         */
        @Override
        public org.hibernate.query.Query setCacheRegion(String cacheRegion) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setTimeout(int)
         */
        @Override
        public org.hibernate.query.Query setTimeout(int timeout) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setLockOptions(org.hibernate.LockOptions)
         */
        @Override
        public org.hibernate.query.Query setLockOptions(LockOptions lockOptions) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setLockMode(java.lang.String, org.hibernate.LockMode)
         */
        @Override
        public org.hibernate.query.Query setLockMode(String alias, LockMode lockMode) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setComment(java.lang.String)
         */
        @Override
        public org.hibernate.query.Query setComment(String comment) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#addQueryHint(java.lang.String)
         */
        @Override
        public org.hibernate.query.Query addQueryHint(String hint) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameterList(org.hibernate.query.QueryParameter, java.util.Collection)
         */
        @Override
        public org.hibernate.query.Query setParameterList(QueryParameter parameter, Collection values) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameterList(java.lang.String, java.util.Collection)
         */
        @Override
        public org.hibernate.query.Query setParameterList(String name, Collection values) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameterList(java.lang.String, java.util.Collection, org.hibernate.type.Type)
         */
        @Override
        public org.hibernate.query.Query setParameterList(String name, Collection values, Type type) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameterList(java.lang.String, java.lang.Object[], org.hibernate.type.Type)
         */
        @Override
        public org.hibernate.query.Query setParameterList(String name, Object[] values, Type type) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setParameterList(java.lang.String, java.lang.Object[])
         */
        @Override
        public org.hibernate.query.Query setParameterList(String name, Object[] values) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setProperties(java.lang.Object)
         */
        @Override
        public org.hibernate.query.Query setProperties(Object bean) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setProperties(java.util.Map)
         */
        @Override
        public org.hibernate.query.Query setProperties(Map bean) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setEntity(int, java.lang.Object)
         */
        @Override
        public org.hibernate.query.Query setEntity(int position, Object val) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.query.Query#setEntity(java.lang.String, java.lang.Object)
         */
        @Override
        public org.hibernate.query.Query setEntity(String name, Object val) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#getQueryOptions()
         */
        @Override
        public RowSelection getQueryOptions() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#isCacheable()
         */
        @Override
        public boolean isCacheable() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#getTimeout()
         */
        @Override
        public Integer getTimeout() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#isReadOnly()
         */
        @Override
        public boolean isReadOnly() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#getReturnTypes()
         */
        @Override
        public Type[] getReturnTypes() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#iterate()
         */
        @Override
        public Iterator iterate() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#getNamedParameters()
         */
        @Override
        public String[] getNamedParameters() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#setParameterList(int, java.util.Collection)
         */
        @Override
        public Query setParameterList(int position, Collection values) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#setParameterList(int, java.util.Collection, org.hibernate.type.Type)
         */
        @Override
        public Query setParameterList(int position, Collection values, Type type) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#setParameterList(int, java.lang.Object[], org.hibernate.type.Type)
         */
        @Override
        public Query setParameterList(int position, Object[] values, Type type) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#setParameterList(int, java.lang.Object[])
         */
        @Override
        public Query setParameterList(int position, Object[] values) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#determineProperBooleanType(int, java.lang.Object, org.hibernate.type.Type)
         */
        @Override
        public Type determineProperBooleanType(int position, Object value, Type defaultType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#determineProperBooleanType(java.lang.String, java.lang.Object, org.hibernate.type.Type)
         */
        @Override
        public Type determineProperBooleanType(String name, Object value, Type defaultType) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.Query#getReturnAliases()
         */
        @Override
        public String[] getReturnAliases() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setResultTransformer(org.hibernate.transform.ResultTransformer)
         */
        @Override
        public FullTextQuery setResultTransformer(ResultTransformer transformer) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#unwrap(java.lang.Class)
         */
        @Override
        public <T> T unwrap(Class<T> type) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setSort(org.apache.lucene.search.Sort)
         */
        @Override
        public FullTextQuery setSort(Sort sort) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setFilter(org.apache.lucene.search.Filter)
         */
        @Override
        public FullTextQuery setFilter(Filter filter) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setCriteriaQuery(org.hibernate.Criteria)
         */
        @Override
        public FullTextQuery setCriteriaQuery(Criteria criteria) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setProjection(java.lang.String[])
         */
        @Override
        public FullTextQuery setProjection(String... fields) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setSpatialParameters(double, double, java.lang.String)
         */
        @Override
        public FullTextQuery setSpatialParameters(double latitude, double longitude, String fieldName) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setSpatialParameters(org.hibernate.search.spatial.Coordinates, java.lang.String)
         */
        @Override
        public FullTextQuery setSpatialParameters(Coordinates center, String fieldName) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setFirstResult(int)
         */
        @Override
        public FullTextQuery setFirstResult(int firstResult) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setMaxResults(int)
         */
        @Override
        public FullTextQuery setMaxResults(int maxResults) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setHint(java.lang.String, java.lang.Object)
         */
        @Override
        public FullTextQuery setHint(String hintName, Object value) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setFlushMode(javax.persistence.FlushModeType)
         */
        @Override
        public FullTextQuery setFlushMode(FlushModeType flushMode) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setFetchSize(int)
         */
        @Override
        public FullTextQuery setFetchSize(int i) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#setTimeout(long, java.util.concurrent.TimeUnit)
         */
        @Override
        public FullTextQuery setTimeout(long timeout, TimeUnit timeUnit) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#limitExecutionTimeTo(long, java.util.concurrent.TimeUnit)
         */
        @Override
        public FullTextQuery limitExecutionTimeTo(long timeout, TimeUnit timeUnit) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.hibernate.search.FullTextQuery#initializeObjectsWith(org.hibernate.search.query.ObjectLookupMethod, org.hibernate.search.query.DatabaseRetrievalMethod)
         */
        @Override
        public FullTextQuery initializeObjectsWith(ObjectLookupMethod lookupMethod, DatabaseRetrievalMethod retrievalMethod) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
