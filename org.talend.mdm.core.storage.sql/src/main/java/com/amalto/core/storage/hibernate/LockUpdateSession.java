/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.LobHelper;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MultiIdentifierLoadAccess;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.SessionEventListener;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionBuilder;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.hibernate.Transaction;
import org.hibernate.TypeHelper;
import org.hibernate.UnknownProfileException;
import org.hibernate.graph.RootGraph;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.query.NativeQuery;
import org.hibernate.stat.SessionStatistics;

/**
 * A implementation of {@link org.hibernate.Session} that ensures all read operations are performed using pessimistic
 * locks.
 *
 * @see com.amalto.core.storage.transaction.Transaction.LockStrategy
 * @see com.amalto.core.storage.transaction.StorageTransaction#getLockStrategy()
 * @see HibernateStorageTransaction#getLockStrategy()
 */
class LockUpdateSession implements Session {

    private static final LockOptions options = LockOptions.UPGRADE.setLockMode(LockMode.PESSIMISTIC_WRITE);

    private static final LockMode mode = LockMode.PESSIMISTIC_WRITE;

    private final Session delegate;

    LockUpdateSession(Session delegate) {
        this.delegate = delegate;
    }

    @Override
    public SharedSessionBuilder sessionWithOptions() {
        return delegate.sessionWithOptions();
    }

    @Override
    public void flush() throws HibernateException {
        delegate.flush();
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        delegate.setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public void setCacheMode(CacheMode cacheMode) {
        delegate.setCacheMode(cacheMode);
    }

    @Override
    public CacheMode getCacheMode() {
        return delegate.getCacheMode();
    }

    @Override
    public SessionFactory getSessionFactory() {
        return delegate.getSessionFactory();
    }

//    @Override
//    public Connection close() throws HibernateException {
//        return delegate.close();
//    }

    @Override
    public void cancelQuery() throws HibernateException {
        delegate.cancelQuery();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public boolean isDirty() throws HibernateException {
        return delegate.isDirty();
    }

    @Override
    public boolean isDefaultReadOnly() {
        return delegate.isDefaultReadOnly();
    }

    @Override
    public void setDefaultReadOnly(boolean readOnly) {
        delegate.setDefaultReadOnly(readOnly);
    }

    @Override
    public Serializable getIdentifier(Object object) {
        return delegate.getIdentifier(object);
    }

    @Override
    public boolean contains(Object object) {
        return delegate.contains(object);
    }

    @Override
    public void evict(Object object) {
        delegate.evict(object);
    }

    @Override
    @Deprecated
    public Object load(Class theClass, Serializable id, LockMode lockMode) {
        return delegate.load(theClass, id, mode);
    }

    @Override
    public Object load(Class theClass, Serializable id, LockOptions lockOptions) {
        return delegate.load(theClass, id, options);
    }

    @Override
    @Deprecated
    public Object load(String entityName, Serializable id, LockMode lockMode) {
        return delegate.load(entityName, id, mode);
    }

    @Override
    public Object load(String entityName, Serializable id, LockOptions lockOptions) {
        return delegate.load(entityName, id, options);
    }

    @Override
    public Object load(Class theClass, Serializable id) {
        return delegate.load(theClass, id, options);
    }

    @Override
    public Object load(String entityName, Serializable id) {
        return delegate.load(entityName, id, options);
    }

    @Override
    public void load(Object object, Serializable id) {
        delegate.load(object.getClass(), id, options);
    }

    @Override
    public void replicate(Object object, ReplicationMode replicationMode) {
        delegate.replicate(object, replicationMode);
    }

    @Override
    public void replicate(String entityName, Object object, ReplicationMode replicationMode) {
        delegate.replicate(entityName, object, replicationMode);
    }

    @Override
    public Serializable save(Object object) {
        return delegate.save(object);
    }

    @Override
    public Serializable save(String entityName, Object object) {
        return delegate.save(entityName, object);
    }

    @Override
    public void saveOrUpdate(Object object) {
        delegate.saveOrUpdate(object);
    }

    @Override
    public void saveOrUpdate(String entityName, Object object) {
        delegate.saveOrUpdate(entityName, object);
    }

    @Override
    public void update(Object object) {
        delegate.update(object);
    }

    @Override
    public void update(String entityName, Object object) {
        delegate.update(entityName, object);
    }

    @Override
    public Object merge(Object object) {
        return delegate.merge(object);
    }

    @Override
    public Object merge(String entityName, Object object) {
        return delegate.merge(entityName, object);
    }

    @Override
    public void persist(Object object) {
        delegate.persist(object);
    }

    @Override
    public void persist(String entityName, Object object) {
        delegate.persist(entityName, object);
    }

    @Override
    public void delete(Object object) {
        delegate.delete(object);
    }

    @Override
    public void delete(String entityName, Object object) {
        delegate.delete(entityName, object);
    }

    @Override
    @Deprecated
    public void lock(Object object, LockMode lockMode) {
        delegate.lock(object, mode);
    }

    @Override
    @Deprecated
    public void lock(String entityName, Object object, LockMode lockMode) {
        delegate.lock(entityName, object, mode);
    }

    @Override
    public LockRequest buildLockRequest(LockOptions lockOptions) {
        return delegate.buildLockRequest(lockOptions);
    }

    @Override
    public void refresh(Object object) {
        delegate.refresh(object);
    }

    @Override
    public void refresh(String entityName, Object object) {
        delegate.refresh(entityName, object);
    }

    @Override
    @Deprecated
    public void refresh(Object object, LockMode lockMode) {
        delegate.refresh(object, mode);
    }

    @Override
    public void refresh(Object object, LockOptions lockOptions) {
        delegate.refresh(object, options);
    }

    @Override
    public void refresh(String entityName, Object object, LockOptions lockOptions) {
        delegate.refresh(entityName, object, options);
    }

    @Override
    public LockMode getCurrentLockMode(Object object) {
        return delegate.getCurrentLockMode(object);
    }

    @Override
    public Query createFilter(Object collection, String queryString) {
        return delegate.createFilter(collection, queryString);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Object get(Class clazz, Serializable id) {
        return delegate.get(clazz, id);
    }

    @Override
    @Deprecated
    public Object get(Class clazz, Serializable id, LockMode lockMode) {
        return delegate.get(clazz, id, mode);
    }

    @Override
    public Object get(Class clazz, Serializable id, LockOptions lockOptions) {
        return delegate.get(clazz, id, options);
    }

    @Override
    public Object get(String entityName, Serializable id) {
        return delegate.get(entityName, id, options);
    }

    @Override
    @Deprecated
    public Object get(String entityName, Serializable id, LockMode lockMode) {
        return delegate.get(entityName, id, mode);
    }

    @Override
    public Object get(String entityName, Serializable id, LockOptions lockOptions) {
        return delegate.get(entityName, id, options);
    }

    @Override
    public String getEntityName(Object object) {
        return delegate.getEntityName(object);
    }

    @Override
    public IdentifierLoadAccess byId(String entityName) {
        return delegate.byId(entityName);
    }

    @Override
    public IdentifierLoadAccess byId(Class entityClass) {
        return delegate.byId(entityClass);
    }

    @Override
    public NaturalIdLoadAccess byNaturalId(String entityName) {
        return delegate.byNaturalId(entityName);
    }

    @Override
    public NaturalIdLoadAccess byNaturalId(Class entityClass) {
        return delegate.byNaturalId(entityClass);
    }

    @Override
    public SimpleNaturalIdLoadAccess bySimpleNaturalId(String entityName) {
        return delegate.bySimpleNaturalId(entityName);
    }

    @Override
    public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class entityClass) {
        return delegate.bySimpleNaturalId(entityClass);
    }

    @Override
    public Filter enableFilter(String filterName) {
        return delegate.enableFilter(filterName);
    }

    @Override
    public Filter getEnabledFilter(String filterName) {
        return delegate.getEnabledFilter(filterName);
    }

    @Override
    public void disableFilter(String filterName) {
        delegate.disableFilter(filterName);
    }

    @Override
    public SessionStatistics getStatistics() {
        return delegate.getStatistics();
    }

    @Override
    public boolean isReadOnly(Object entityOrProxy) {
        return delegate.isReadOnly(entityOrProxy);
    }

    @Override
    public void setReadOnly(Object entityOrProxy, boolean readOnly) {
        delegate.setReadOnly(entityOrProxy, readOnly);
    }

    @Override
    public void doWork(Work work) throws HibernateException {
        delegate.doWork(work);
    }

    @Override
    public <T> T doReturningWork(ReturningWork<T> work) throws HibernateException {
        return delegate.doReturningWork(work);
    }

    @Override
    public Connection disconnect() {
        return delegate.disconnect();
    }

    @Override
    public void reconnect(Connection connection) {
        delegate.reconnect(connection);
    }

    @Override
    public boolean isFetchProfileEnabled(String name) throws UnknownProfileException {
        return delegate.isFetchProfileEnabled(name);
    }

    @Override
    public void enableFetchProfile(String name) throws UnknownProfileException {
        delegate.enableFetchProfile(name);
    }

    @Override
    public void disableFetchProfile(String name) throws UnknownProfileException {
        delegate.disableFetchProfile(name);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return delegate.getTypeHelper();
    }

    @Override
    public LobHelper getLobHelper() {
        return delegate.getLobHelper();
    }

    @Override
    public void addEventListeners(SessionEventListener... listeners) {
        delegate.addEventListeners(listeners);
    }

    @Override
    public String getTenantIdentifier() {
        return delegate.getTenantIdentifier();
    }

    @Override
    public Transaction beginTransaction() {
        return delegate.beginTransaction();
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public org.hibernate.query.Query getNamedQuery(String queryName) {
        return delegate.getNamedQuery(queryName);
    }

    @Override
    public org.hibernate.query.Query createQuery(String queryString) {
        return delegate.createQuery(queryString);
    }

    @Override
    public NativeQuery createSQLQuery(String queryString) {
        return delegate.createSQLQuery(queryString);
    }

    @Override
    public ProcedureCall getNamedProcedureCall(String name) {
        return delegate.getNamedProcedureCall(name);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName) {
        return delegate.createStoredProcedureCall(procedureName);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
        return delegate.createStoredProcedureCall(procedureName, resultClasses);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
        return delegate.createStoredProcedureCall(procedureName, resultSetMappings);
    }

    @Override
    public Criteria createCriteria(Class persistentClass) {
        Criteria criteria = delegate.createCriteria(persistentClass);
        criteria.setLockMode(mode);
        return criteria;
    }

    @Override
    public Criteria createCriteria(Class persistentClass, String alias) {
        Criteria criteria = delegate.createCriteria(persistentClass, alias);
        criteria.setLockMode(mode);
        return criteria;
    }

    @Override
    public Criteria createCriteria(String entityName) {
        Criteria criteria = delegate.createCriteria(entityName);
        criteria.setLockMode(mode);
        return criteria;
    }

    @Override
    public Criteria createCriteria(String entityName, String alias) {
        Criteria criteria = delegate.createCriteria(entityName, alias);
        criteria.setLockMode(mode);
        return criteria;
    }

    /* (non-Javadoc)
     * @see org.hibernate.SharedSessionContract#close()
     */
    @Override
    public void close() throws HibernateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.hibernate.SharedSessionContract#getJdbcBatchSize()
     */
    @Override
    public Integer getJdbcBatchSize() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.SharedSessionContract#setJdbcBatchSize(java.lang.Integer)
     */
    @Override
    public void setJdbcBatchSize(Integer jdbcBatchSize) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.hibernate.query.QueryProducer#createNamedQuery(java.lang.String)
     */
    @Override
    public org.hibernate.query.Query createNamedQuery(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.query.QueryProducer#createNativeQuery(java.lang.String)
     */
    @Override
    public NativeQuery createNativeQuery(String sqlString) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.query.QueryProducer#createNativeQuery(java.lang.String, java.lang.String)
     */
    @Override
    public NativeQuery createNativeQuery(String sqlString, String resultSetMapping) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.query.QueryProducer#getNamedNativeQuery(java.lang.String)
     */
    @Override
    public NativeQuery getNamedNativeQuery(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#remove(java.lang.Object)
     */
    @Override
    public void remove(Object entity) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object)
     */
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, java.util.Map)
     */
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, javax.persistence.LockModeType)
     */
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, javax.persistence.LockModeType, java.util.Map)
     */
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getReference(java.lang.Class, java.lang.Object)
     */
    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#setFlushMode(javax.persistence.FlushModeType)
     */
    @Override
    public void setFlushMode(FlushModeType flushMode) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#lock(java.lang.Object, javax.persistence.LockModeType)
     */
    @Override
    public void lock(Object entity, LockModeType lockMode) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#lock(java.lang.Object, javax.persistence.LockModeType, java.util.Map)
     */
    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, java.util.Map)
     */
    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, javax.persistence.LockModeType)
     */
    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, javax.persistence.LockModeType, java.util.Map)
     */
    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#detach(java.lang.Object)
     */
    @Override
    public void detach(Object entity) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getLockMode(java.lang.Object)
     */
    @Override
    public LockModeType getLockMode(Object entity) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(String propertyName, Object value) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getProperties()
     */
    @Override
    public Map<String, Object> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#createNamedStoredProcedureQuery(java.lang.String)
     */
    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#createStoredProcedureQuery(java.lang.String)
     */
    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#createStoredProcedureQuery(java.lang.String, java.lang.Class[])
     */
    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#createStoredProcedureQuery(java.lang.String, java.lang.String[])
     */
    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#joinTransaction()
     */
    @Override
    public void joinTransaction() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#isJoinedToTransaction()
     */
    @Override
    public boolean isJoinedToTransaction() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap(Class<T> cls) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getDelegate()
     */
    @Override
    public Object getDelegate() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getEntityManagerFactory()
     */
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getCriteriaBuilder()
     */
    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getMetamodel()
     */
    @Override
    public Metamodel getMetamodel() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.jpa.HibernateEntityManager#getSession()
     */
    @Override
    public Session getSession() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#setHibernateFlushMode(org.hibernate.FlushMode)
     */
    @Override
    public void setHibernateFlushMode(FlushMode flushMode) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#getHibernateFlushMode()
     */
    @Override
    public FlushMode getHibernateFlushMode() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#contains(java.lang.String, java.lang.Object)
     */
    @Override
    public boolean contains(String entityName, Object object) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#byMultipleIds(java.lang.Class)
     */
    @Override
    public <T> MultiIdentifierLoadAccess<T> byMultipleIds(Class<T> entityClass) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#byMultipleIds(java.lang.String)
     */
    @Override
    public MultiIdentifierLoadAccess byMultipleIds(String entityName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#createEntityGraph(java.lang.Class)
     */
    @Override
    public <T> RootGraph<T> createEntityGraph(Class<T> rootType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#createEntityGraph(java.lang.String)
     */
    @Override
    public RootGraph<?> createEntityGraph(String graphName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#getEntityGraph(java.lang.String)
     */
    @Override
    public RootGraph<?> getEntityGraph(String graphName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#createQuery(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> org.hibernate.query.Query<T> createQuery(String queryString, Class<T> resultType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#createQuery(javax.persistence.criteria.CriteriaQuery)
     */
    @Override
    public <T> org.hibernate.query.Query<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#createQuery(javax.persistence.criteria.CriteriaUpdate)
     */
    @Override
    public org.hibernate.query.Query createQuery(CriteriaUpdate updateQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#createQuery(javax.persistence.criteria.CriteriaDelete)
     */
    @Override
    public org.hibernate.query.Query createQuery(CriteriaDelete deleteQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.hibernate.Session#createNamedQuery(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> org.hibernate.query.Query<T> createNamedQuery(String name, Class<T> resultType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#createNativeQuery(java.lang.String, java.lang.Class)
     */
    @Override
    public NativeQuery createNativeQuery(String sqlString, Class resultClass) {
        // TODO Auto-generated method stub
        return null;
    }
}
