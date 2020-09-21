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
        return this.delegate.sessionWithOptions();
    }

    @Override
    public void flush() throws HibernateException {
        this.delegate.flush();
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        this.delegate.setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return this.delegate.getFlushMode();
    }

    @Override
    public void setCacheMode(CacheMode cacheMode) {
        this.delegate.setCacheMode(cacheMode);
    }

    @Override
    public CacheMode getCacheMode() {
        return this.delegate.getCacheMode();
    }

    @Override
    public SessionFactory getSessionFactory() {
        return this.delegate.getSessionFactory();
    }

    @Override
    public void cancelQuery() throws HibernateException {
        this.delegate.cancelQuery();
    }

    @Override
    public boolean isOpen() {
        return this.delegate.isOpen();
    }

    @Override
    public boolean isConnected() {
        return this.delegate.isConnected();
    }

    @Override
    public boolean isDirty() throws HibernateException {
        return this.delegate.isDirty();
    }

    @Override
    public boolean isDefaultReadOnly() {
        return this.delegate.isDefaultReadOnly();
    }

    @Override
    public void setDefaultReadOnly(boolean readOnly) {
        this.delegate.setDefaultReadOnly(readOnly);
    }

    @Override
    public Serializable getIdentifier(Object object) {
        return this.delegate.getIdentifier(object);
    }

    @Override
    public boolean contains(Object object) {
        return this.delegate.contains(object);
    }

    @Override
    public void evict(Object object) {
        this.delegate.evict(object);
    }

    @Override
    @Deprecated
    public Object load(Class theClass, Serializable id, LockMode lockMode) {
        return this.delegate.load(theClass, id, mode);
    }

    @Override
    public Object load(Class theClass, Serializable id, LockOptions lockOptions) {
        return this.delegate.load(theClass, id, options);
    }

    @Override
    @Deprecated
    public Object load(String entityName, Serializable id, LockMode lockMode) {
        return this.delegate.load(entityName, id, mode);
    }

    @Override
    public Object load(String entityName, Serializable id, LockOptions lockOptions) {
        return this.delegate.load(entityName, id, options);
    }

    @Override
    public Object load(Class theClass, Serializable id) {
        return this.delegate.load(theClass, id, options);
    }

    @Override
    public Object load(String entityName, Serializable id) {
        return this.delegate.load(entityName, id, options);
    }

    @Override
    public void load(Object object, Serializable id) {
        this.delegate.load(object.getClass(), id, options);
    }

    @Override
    public void replicate(Object object, ReplicationMode replicationMode) {
        this.delegate.replicate(object, replicationMode);
    }

    @Override
    public void replicate(String entityName, Object object, ReplicationMode replicationMode) {
        this.delegate.replicate(entityName, object, replicationMode);
    }

    @Override
    public Serializable save(Object object) {
        return this.delegate.save(object);
    }

    @Override
    public Serializable save(String entityName, Object object) {
        return this.delegate.save(entityName, object);
    }

    @Override
    public void saveOrUpdate(Object object) {
        this.delegate.saveOrUpdate(object);
    }

    @Override
    public void saveOrUpdate(String entityName, Object object) {
        this.delegate.saveOrUpdate(entityName, object);
    }

    @Override
    public void update(Object object) {
        this.delegate.update(object);
    }

    @Override
    public void update(String entityName, Object object) {
        this.delegate.update(entityName, object);
    }

    @Override
    public Object merge(Object object) {
        return this.delegate.merge(object);
    }

    @Override
    public Object merge(String entityName, Object object) {
        return this.delegate.merge(entityName, object);
    }

    @Override
    public void persist(Object object) {
        this.delegate.persist(object);
    }

    @Override
    public void persist(String entityName, Object object) {
        this.delegate.persist(entityName, object);
    }

    @Override
    public void delete(Object object) {
        this.delegate.delete(object);
    }

    @Override
    public void delete(String entityName, Object object) {
        this.delegate.delete(entityName, object);
    }

    @Override
    @Deprecated
    public void lock(Object object, LockMode lockMode) {
        this.delegate.lock(object, mode);
    }

    @Override
    @Deprecated
    public void lock(String entityName, Object object, LockMode lockMode) {
        this.delegate.lock(entityName, object, mode);
    }

    @Override
    public LockRequest buildLockRequest(LockOptions lockOptions) {
        return this.delegate.buildLockRequest(lockOptions);
    }

    @Override
    public void refresh(Object object) {
        this.delegate.refresh(object);
    }

    @Override
    public void refresh(String entityName, Object object) {
        this.delegate.refresh(entityName, object);
    }

    @Override
    @Deprecated
    public void refresh(Object object, LockMode lockMode) {
        this.delegate.refresh(object, mode);
    }

    @Override
    public void refresh(Object object, LockOptions lockOptions) {
        this.delegate.refresh(object, options);
    }

    @Override
    public void refresh(String entityName, Object object, LockOptions lockOptions) {
        this.delegate.refresh(entityName, object, options);
    }

    @Override
    public LockMode getCurrentLockMode(Object object) {
        return this.delegate.getCurrentLockMode(object);
    }

    @Override
    public Query createFilter(Object collection, String queryString) {
        return this.delegate.createFilter(collection, queryString);
    }

    @Override
    public void clear() {
        this.delegate.clear();
    }

    @Override
    public Object get(Class clazz, Serializable id) {
        return this.delegate.get(clazz, id);
    }

    @Override
    @Deprecated
    public Object get(Class clazz, Serializable id, LockMode lockMode) {
        return this.delegate.get(clazz, id, mode);
    }

    @Override
    public Object get(Class clazz, Serializable id, LockOptions lockOptions) {
        return this.delegate.get(clazz, id, options);
    }

    @Override
    public Object get(String entityName, Serializable id) {
        return this.delegate.get(entityName, id, options);
    }

    @Override
    @Deprecated
    public Object get(String entityName, Serializable id, LockMode lockMode) {
        return this.delegate.get(entityName, id, mode);
    }

    @Override
    public Object get(String entityName, Serializable id, LockOptions lockOptions) {
        return this.delegate.get(entityName, id, options);
    }

    @Override
    public String getEntityName(Object object) {
        return this.delegate.getEntityName(object);
    }

    @Override
    public IdentifierLoadAccess byId(String entityName) {
        return this.delegate.byId(entityName);
    }

    @Override
    public IdentifierLoadAccess byId(Class entityClass) {
        return this.delegate.byId(entityClass);
    }

    @Override
    public NaturalIdLoadAccess byNaturalId(String entityName) {
        return this.delegate.byNaturalId(entityName);
    }

    @Override
    public NaturalIdLoadAccess byNaturalId(Class entityClass) {
        return this.delegate.byNaturalId(entityClass);
    }

    @Override
    public SimpleNaturalIdLoadAccess bySimpleNaturalId(String entityName) {
        return this.delegate.bySimpleNaturalId(entityName);
    }

    @Override
    public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class entityClass) {
        return this.delegate.bySimpleNaturalId(entityClass);
    }

    @Override
    public Filter enableFilter(String filterName) {
        return this.delegate.enableFilter(filterName);
    }

    @Override
    public Filter getEnabledFilter(String filterName) {
        return this.delegate.getEnabledFilter(filterName);
    }

    @Override
    public void disableFilter(String filterName) {
        this.delegate.disableFilter(filterName);
    }

    @Override
    public SessionStatistics getStatistics() {
        return this.delegate.getStatistics();
    }

    @Override
    public boolean isReadOnly(Object entityOrProxy) {
        return this.delegate.isReadOnly(entityOrProxy);
    }

    @Override
    public void setReadOnly(Object entityOrProxy, boolean readOnly) {
        this.delegate.setReadOnly(entityOrProxy, readOnly);
    }

    @Override
    public void doWork(Work work) throws HibernateException {
        this.delegate.doWork(work);
    }

    @Override
    public <T> T doReturningWork(ReturningWork<T> work) throws HibernateException {
        return this.delegate.doReturningWork(work);
    }

    @Override
    public Connection disconnect() {
        return this.delegate.disconnect();
    }

    @Override
    public void reconnect(Connection connection) {
        this.delegate.reconnect(connection);
    }

    @Override
    public boolean isFetchProfileEnabled(String name) throws UnknownProfileException {
        return this.delegate.isFetchProfileEnabled(name);
    }

    @Override
    public void enableFetchProfile(String name) throws UnknownProfileException {
        this.delegate.enableFetchProfile(name);
    }

    @Override
    public void disableFetchProfile(String name) throws UnknownProfileException {
        this.delegate.disableFetchProfile(name);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return this.delegate.getTypeHelper();
    }

    @Override
    public LobHelper getLobHelper() {
        return this.delegate.getLobHelper();
    }

    @Override
    public void addEventListeners(SessionEventListener... listeners) {
        this.delegate.addEventListeners(listeners);
    }

    @Override
    public String getTenantIdentifier() {
        return this.delegate.getTenantIdentifier();
    }

    @Override
    public Transaction beginTransaction() {
        return this.delegate.beginTransaction();
    }

    @Override
    public Transaction getTransaction() {
        return this.delegate.getTransaction();
    }

    @Override
    public org.hibernate.query.Query getNamedQuery(String queryName) {
        return this.delegate.getNamedQuery(queryName);
    }

    @Override
    public org.hibernate.query.Query createQuery(String queryString) {
        return this.delegate.createQuery(queryString);
    }

    @Override
    public NativeQuery createSQLQuery(String queryString) {
        return this.delegate.createSQLQuery(queryString);
    }

    @Override
    public ProcedureCall getNamedProcedureCall(String name) {
        return this.delegate.getNamedProcedureCall(name);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName) {
        return this.delegate.createStoredProcedureCall(procedureName);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
        return this.delegate.createStoredProcedureCall(procedureName, resultClasses);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
        return this.delegate.createStoredProcedureCall(procedureName, resultSetMappings);
    }

    @Override
    public Criteria createCriteria(Class persistentClass) {
        Criteria criteria = this.delegate.createCriteria(persistentClass);
        criteria.setLockMode(mode);
        return criteria;
    }

    @Override
    public Criteria createCriteria(Class persistentClass, String alias) {
        Criteria criteria = this.delegate.createCriteria(persistentClass, alias);
        criteria.setLockMode(mode);
        return criteria;
    }

    @Override
    public Criteria createCriteria(String entityName) {
        Criteria criteria = this.delegate.createCriteria(entityName);
        criteria.setLockMode(mode);
        return criteria;
    }

    @Override
    public Criteria createCriteria(String entityName, String alias) {
        Criteria criteria = this.delegate.createCriteria(entityName, alias);
        criteria.setLockMode(mode);
        return criteria;
    }

    @Override
    public void close() throws HibernateException {
        this.delegate.close();
    }

    @Override
    public Integer getJdbcBatchSize() {
        return this.delegate.getJdbcBatchSize();
    }

    @Override
    public void setJdbcBatchSize(Integer jdbcBatchSize) {
        this.delegate.setJdbcBatchSize(jdbcBatchSize);      
    }

    @Override
    public org.hibernate.query.Query createNamedQuery(String name) {
        return this.delegate.createNamedQuery(name);
    }

    @Override
    public NativeQuery createNativeQuery(String sqlString) {
        return this.delegate.createNativeQuery(sqlString);
    }

    @Override
    public NativeQuery createNativeQuery(String sqlString, String resultSetMapping) {
        return this.delegate.createNativeQuery(sqlString, resultSetMapping);
    }

    @Override
    public NativeQuery getNamedNativeQuery(String name) {
        return this.delegate.getNamedNativeQuery(name);
    }

    @Override
    public void remove(Object entity) {
       this.delegate.remove(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return this.delegate.find(entityClass, primaryKey);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return this.delegate.find(entityClass, primaryKey, properties);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return this.delegate.find(entityClass, primaryKey, lockMode);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return this.delegate.find(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return this.delegate.getReference(entityClass, primaryKey);
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        this.delegate.setFlushMode(flushMode);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        this.delegate.lock(entity, lockMode);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        this.delegate.lock(entity, lockMode, properties);
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        this.delegate.refresh(entity, properties);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        this.delegate.refresh(entity, lockMode);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        this.delegate.refresh(entity, lockMode, properties);
    }

    @Override
    public void detach(Object entity) {
        this.delegate.detach(entity);
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return this.delegate.getLockMode(entity);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        this.delegate.setProperty(propertyName, value);
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.delegate.getProperties();
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return this.delegate.createNamedStoredProcedureQuery(name);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return this.delegate.createStoredProcedureQuery(procedureName);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return this.delegate.createStoredProcedureQuery(procedureName, resultClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return this.delegate.createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    @Override
    public void joinTransaction() {
        this.delegate.joinTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
        return this.delegate.isJoinedToTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return this.delegate.unwrap(cls);
    }

    @Override
    public Object getDelegate() {
        return this.delegate;
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return this.delegate.getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return this.delegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return this.delegate.getMetamodel();
    }

    @Override
    public Session getSession() {
        return this.delegate;
    }

    @Override
    public void setHibernateFlushMode(FlushMode flushMode) {
       this.delegate.setHibernateFlushMode(flushMode);
    }

    @Override
    public FlushMode getHibernateFlushMode() {
        return this.delegate.getHibernateFlushMode();
    }

    @Override
    public boolean contains(String entityName, Object object) {
       return this.delegate.contains(entityName, object);
    }

    @Override
    public <T> MultiIdentifierLoadAccess<T> byMultipleIds(Class<T> entityClass) {
        return this.delegate.byMultipleIds(entityClass);
    }

    @Override
    public MultiIdentifierLoadAccess byMultipleIds(String entityName) {
        return this.delegate.byMultipleIds(entityName);
    }

    @Override
    public <T> RootGraph<T> createEntityGraph(Class<T> rootType) {
        return this.createEntityGraph(rootType);
    }

    @Override
    public RootGraph<?> createEntityGraph(String graphName) {
        return this.delegate.createEntityGraph(graphName);
    }

    @Override
    public RootGraph<?> getEntityGraph(String graphName) {
        return this.delegate.getEntityGraph(graphName);
    }

    @Override
    public <T> org.hibernate.query.Query<T> createQuery(String queryString, Class<T> resultType) {
        return this.delegate.createQuery(queryString, resultType);
    }

    @Override
    public <T> org.hibernate.query.Query<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return this.delegate.createQuery(criteriaQuery);
    }

    @Override
    public org.hibernate.query.Query createQuery(CriteriaUpdate updateQuery) {
        return this.delegate.createQuery(updateQuery);
    }

    @Override
    public org.hibernate.query.Query createQuery(CriteriaDelete deleteQuery) {
        return this.delegate.createQuery(deleteQuery);
    }

    @Override
    public <T> org.hibernate.query.Query<T> createNamedQuery(String name, Class<T> resultType) {
        return this.delegate.createNamedQuery(name, resultType);
    }

    @Override
    public NativeQuery createNativeQuery(String sqlString, Class resultClass) {
        return this.delegate.createNativeQuery(sqlString, resultClass);
    }
}
