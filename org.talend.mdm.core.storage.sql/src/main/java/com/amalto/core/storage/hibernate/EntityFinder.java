/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.lucene.search.Explanation;
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
                }
            }
        }

        @Override
        public String getQueryString() {
            return query.getQueryString();
        }

        @Override
        public Integer getMaxResults() {
            return query.getMaxResults();
        }

        @Override
        public Type[] getReturnTypes() throws HibernateException {
            return query.getReturnTypes();
        }

        @Override
        public String[] getReturnAliases() throws HibernateException {
            return query.getReturnAliases();
        }

        @Override
        public String[] getNamedParameters() throws HibernateException {
            return query.getNamedParameters();
        }

        @Override
        public Iterator iterate() throws HibernateException {
            Iterator iterator = query.iterate();
            return new IteratorWrapper(iterator, storage, session);
        }

        @Override
        public ScrollableResults scroll() throws HibernateException {
            ScrollableResults scrollableResults = query.scroll();
            return new ScrollableResultsWrapper(scrollableResults, storage, session);
        }

        @Override
        public ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException {
            ScrollableResults scrollableResults = query.scroll(scrollMode);
            return new ScrollableResultsWrapper(scrollableResults, storage, session);
        }

        @Override
        public List list() throws HibernateException {
            List<Wrapper> list = query.list();
            Set<Wrapper> newSet = new ListOrderedSet();
            for (Object item : list) {
                Wrapper element = EntityFinder.findEntity((Wrapper) item, storage, session);
                if (element != null) {
                    if (entityClassName.size() > 0) {
                        if (this.entityClassName.contains(element.getClass().getName())) {
                            newSet.add(element);
                        }
                    } else {
                        newSet.add(element);
                    }
                }
            }
            return new ArrayList<Wrapper>(newSet);
        }

        @Override
        public Object uniqueResult() throws HibernateException {
            return query.uniqueResult();
        }

        @Override
        public int executeUpdate() throws HibernateException {
            return query.executeUpdate();
        }

        @Override
        public FullTextQuery setMaxResults(int maxResults) {
            return query.setMaxResults(maxResults);
        }

        @Override
        public Integer getFirstResult() {
            return query.getFirstResult();
        }

        @Override
        public FullTextQuery setFirstResult(int firstResult) {
            return query.setFirstResult(firstResult);
        }

        @Override
        public boolean isReadOnly() {
            return query.isReadOnly();
        }

        @Override
        public Query setReadOnly(boolean readOnly) {
            return query.setReadOnly(readOnly);
        }

        @Override
        public LockOptions getLockOptions() {
            return query.getLockOptions();
        }

        @Override
        public Query setCacheable(boolean cacheable) {
            return query.setCacheable(cacheable);
        }

        @Override
        public String getCacheRegion() {
            return query.getCacheRegion();
        }

        @Override
        public Query setCacheRegion(String cacheRegion) {
            return query.setCacheRegion(cacheRegion);
        }

        @Override
        public Integer getTimeout() {
            return query.getTimeout();
        }

        @Override
        public Query setTimeout(int timeout) {
            return query.setTimeout(timeout);
        }

        @Override
        public Integer getFetchSize() {
            return query.getFetchSize();
        }

        @Override
        public FullTextQuery setFetchSize(int fetchSize) {
            return query.setFetchSize(fetchSize);
        }

        @Override
        public Query setLockOptions(LockOptions lockOptions) {
            return query.setLockOptions(lockOptions);
        }

        @Override
        public Query setLockMode(String alias, LockMode lockMode) {
            return query.setLockMode(alias, lockMode);
        }

        @Override
        public String getComment() {
            return query.getComment();
        }

        @Override
        public Query setComment(String comment) {
            return query.setComment(comment);
        }

        @Override
        public Query addQueryHint(String hint) {
            return query.addQueryHint(hint);
        }

        @Override
        public FlushMode getFlushMode() {
            return query.getFlushMode();
        }

        @Override
        public Query setFlushMode(FlushMode flushMode) {
            return query.setFlushMode(flushMode);
        }

        @Override
        public CacheMode getCacheMode() {
            return query.getCacheMode();
        }

        @Override
        public Query setCacheMode(CacheMode cacheMode) {
            return query.setCacheMode(cacheMode);
        }

        @Override
        public boolean isCacheable() {
            return query.isCacheable();
        }

        @Override
        public Query setParameter(int position, Object val, Type type) {
            return query.setParameter(position, val, type);
        }

        @Override
        public Query setParameter(String name, Object val, Type type) {
            return query.setParameter(name, val, type);
        }

        @Override
        public Query setParameter(int position, Object val) throws HibernateException {
            return query.setParameter(position, val);
        }

        @Override
        public Query setParameter(String name, Object val) throws HibernateException {
            return query.setParameter(name, val);
        }

        @Override
        public Query setParameters(Object[] values, Type[] types) throws HibernateException {
            return query.setParameters(values, types);
        }

        @Override
        public Query setParameterList(String name, Collection vals, Type type) throws HibernateException {
            return query.setParameterList(name, vals, type);
        }

        @Override
        public Query setParameterList(String name, Collection vals) throws HibernateException {
            return query.setParameterList(name, vals);
        }

        @Override
        public Query setParameterList(String name, Object[] vals, Type type) throws HibernateException {
            return query.setParameterList(name, vals, type);
        }

        @Override
        public Query setParameterList(String name, Object[] vals) throws HibernateException {
            return query.setParameterList(name, vals);
        }

        @Override
        public Query setProperties(Object bean) throws HibernateException {
            return query.setProperties(bean);
        }

        @Override
        public Query setProperties(Map bean) throws HibernateException {
            return query.setProperties(bean);
        }

        @Override
        public Query setString(int position, String val) {
            return query.setString(position, val);
        }

        @Override
        public Query setCharacter(int position, char val) {
            return query.setCharacter(position, val);
        }

        @Override
        public Query setBoolean(int position, boolean val) {
            return query.setBoolean(position, val);
        }

        @Override
        public Query setByte(int position, byte val) {
            return query.setByte(position, val);
        }

        @Override
        public Query setShort(int position, short val) {
            return query.setShort(position, val);
        }

        @Override
        public Query setInteger(int position, int val) {
            return query.setInteger(position, val);
        }

        @Override
        public Query setLong(int position, long val) {
            return query.setLong(position, val);
        }

        @Override
        public Query setFloat(int position, float val) {
            return query.setFloat(position, val);
        }

        @Override
        public Query setDouble(int position, double val) {
            return query.setDouble(position, val);
        }

        @Override
        public Query setBinary(int position, byte[] val) {
            return query.setBinary(position, val);
        }

        @Override
        public Query setText(int position, String val) {
            return query.setText(position, val);
        }

        @Override
        public Query setSerializable(int position, Serializable val) {
            return query.setSerializable(position, val);
        }

        @Override
        public Query setLocale(int position, Locale locale) {
            return query.setLocale(position, locale);
        }

        @Override
        public Query setBigDecimal(int position, BigDecimal number) {
            return query.setBigDecimal(position, number);
        }

        @Override
        public Query setBigInteger(int position, BigInteger number) {
            return query.setBigInteger(position, number);
        }

        @Override
        public Query setDate(int position, Date date) {
            return query.setDate(position, date);
        }

        @Override
        public Query setTime(int position, Date date) {
            return query.setTime(position, date);
        }

        @Override
        public Query setTimestamp(int position, Date date) {
            return query.setTimestamp(position, date);
        }

        @Override
        public Query setCalendar(int position, Calendar calendar) {
            return query.setCalendar(position, calendar);
        }

        @Override
        public Query setCalendarDate(int position, Calendar calendar) {
            return query.setCalendarDate(position, calendar);
        }

        @Override
        public Query setString(String name, String val) {
            return query.setString(name, val);
        }

        @Override
        public Query setCharacter(String name, char val) {
            return query.setCharacter(name, val);
        }

        @Override
        public Query setBoolean(String name, boolean val) {
            return query.setBoolean(name, val);
        }

        @Override
        public Query setByte(String name, byte val) {
            return query.setByte(name, val);
        }

        @Override
        public Query setShort(String name, short val) {
            return query.setShort(name, val);
        }

        @Override
        public Query setInteger(String name, int val) {
            return query.setInteger(name, val);
        }

        @Override
        public Query setLong(String name, long val) {
            return query.setLong(name, val);
        }

        @Override
        public Query setFloat(String name, float val) {
            return query.setFloat(name, val);
        }

        @Override
        public Query setDouble(String name, double val) {
            return query.setDouble(name, val);
        }

        @Override
        public Query setBinary(String name, byte[] val) {
            return query.setBinary(name, val);
        }

        @Override
        public Query setText(String name, String val) {
            return query.setText(name, val);
        }

        @Override
        public Query setSerializable(String name, Serializable val) {
            return query.setSerializable(name, val);
        }

        @Override
        public Query setLocale(String name, Locale locale) {
            return query.setLocale(name, locale);
        }

        @Override
        public Query setBigDecimal(String name, BigDecimal number) {
            return query.setBigDecimal(name, number);
        }

        @Override
        public Query setBigInteger(String name, BigInteger number) {
            return query.setBigInteger(name, number);
        }

        @Override
        public Query setDate(String name, Date date) {
            return query.setDate(name, date);
        }

        @Override
        public Query setTime(String name, Date date) {
            return query.setTime(name, date);
        }

        @Override
        public Query setTimestamp(String name, Date date) {
            return query.setTimestamp(name, date);
        }

        @Override
        public Query setCalendar(String name, Calendar calendar) {
            return query.setCalendar(name, calendar);
        }

        @Override
        public Query setCalendarDate(String name, Calendar calendar) {
            return query.setCalendarDate(name, calendar);
        }

        @Override
        public Query setEntity(int position, Object val) {
            return query.setEntity(position, val);
        }

        @Override
        public Query setEntity(String name, Object val) {
            return query.setEntity(name, val);
        }

        @Override
        public FullTextQuery setResultTransformer(ResultTransformer transformer) {
            return query.setResultTransformer(transformer);
        }

        @Override
        public <T> T unwrap(Class<T> tClass) {
            return query.unwrap(tClass);
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
        public boolean hasPartialResults() {
            return query.hasPartialResults();
        }

        @Override
        public FullTextQuery initializeObjectsWith(ObjectLookupMethod lookupMethod, DatabaseRetrievalMethod retrievalMethod) {
            return query.initializeObjectsWith(lookupMethod, retrievalMethod);
        }

        @Override
        public FullTextQuery setSort(Sort sort) {
            return query.setSort(sort);
        }

        @Override
        public FullTextQuery setFilter(org.apache.lucene.search.Filter filter) {
            return query.setFilter(filter);
        }

        @Override
        public int getResultSize() {
            return query.getResultSize();
        }

        @Override
        public FullTextQuery setCriteriaQuery(Criteria criteria) {
            return query.setCriteriaQuery(criteria);
        }

        @Override
        public FullTextQuery setProjection(String... strings) {
            return query.setProjection(strings);
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
        public FullTextFilter enableFullTextFilter(String s) {
            return query.enableFullTextFilter(s);
        }

        @Override
        public void disableFullTextFilter(String s) {
            query.disableFullTextFilter(s);
        }

        @Override
        public FacetManager getFacetManager() {
            return query.getFacetManager();
        }

        @Override
        public Explanation explain(int i) {
            return query.explain(i);
        }
    }
}
