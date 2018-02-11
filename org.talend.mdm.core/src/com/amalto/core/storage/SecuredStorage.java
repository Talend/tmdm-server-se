/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.compare.Compare.DiffResults;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;

import com.amalto.core.query.user.Expression;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.transaction.StorageTransaction;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.XtentisException;

public class SecuredStorage implements Storage {

    private final Storage delegate;

    private final UserDelegator delegator;

    private static final Logger LOGGER = Logger.getLogger(SecuredStorage.class);

    /**
     * Interface to handle user visibility rules.
     * @see #UNSECURED
     */
    public interface UserDelegator {

        /**
         * @param field A field in data model.
         * @return <code>true</code> if user should not see the <code>field</code>, <code>false</code> otherwise.
         */
        boolean hide(FieldMetadata field);

        /**
         * @param type A entity type in data model.
         * @return <code>true</code> if user should not see the <code>type</code>, <code>false</code> otherwise.
         */
        boolean hide(ComplexTypeMetadata type);
    }

    /**
     * A {@link com.amalto.core.storage.SecuredStorage.UserDelegator delegator} implementation that always allows
     * display of fields and types.
     */
    public static final UserDelegator UNSECURED = new SecuredStorage.UserDelegator() {

        public boolean hide(FieldMetadata field) {
            return false;
        }

        public boolean hide(ComplexTypeMetadata type) {
            return false;
        }
    };

    // Returns a UserDelegator instance configured using current user's roles.
    public static SecuredStorage.UserDelegator getDelegator() {
        final HashSet<String> roles;
        try {
            roles = LocalUser.getLocalUser().getRoles();
        } catch (XtentisException e) {
            String message = "Unable to access user current roles."; //$NON-NLS-1$
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
        return new SecuredStorage.UserDelegator() {

            @Override
            public boolean hide(FieldMetadata field) {
                boolean allowed = Collections.disjoint(field.getHideUsers(), roles);
                return !allowed;
            }

            @Override
            public boolean hide(ComplexTypeMetadata type) {
                boolean allowed = Collections.disjoint(type.getHideUsers(), roles);
                return !allowed;
            }
        };
    }

    public SecuredStorage(Storage delegate, UserDelegator delegator) {
        this.delegate = delegate;
        this.delegator = delegator;
    }

    @Override
    public Storage asInternal() {
        return delegate.asInternal();
    }

    @Override
    public int getCapabilities() {
        return delegate.getCapabilities();
    }

    @Override
    public StorageTransaction newStorageTransaction() {
        return delegate.newStorageTransaction();
    }

    public void init(DataSourceDefinition dataSource) {
        delegate.init(dataSource);
    }

    public void prepare(MetadataRepository repository, Set<Expression> optimizedExpressions, boolean force,
            boolean dropExistingData) {
        delegate.prepare(repository, optimizedExpressions, force, dropExistingData);
    }

    public void prepare(MetadataRepository repository, boolean dropExistingData) {
        delegate.prepare(repository, dropExistingData);
    }

    @Override
    public MetadataRepository getMetadataRepository() {
        return delegate.getMetadataRepository();
    }

    public StorageResults fetch(Expression userQuery) {
        Expression cleanedExpression = userQuery.accept(new SecurityQueryCleaner(delegator));
        return delegate.fetch(cleanedExpression);
    }

    public void update(DataRecord record) {
        delegate.update(record);
    }

    public void update(Iterable<DataRecord> records) {
        delegate.update(records);
    }

    public void delete(Expression userQuery) {
        delegate.delete(userQuery);
    }

    @Override
    public void delete(DataRecord record) {
        delegate.delete(record);
    }

    public void close() {
        delegate.close();
    }

    @Override
    public void close(boolean dropExistingData) {
        delegate.close(dropExistingData);
    }

    public void begin() {
        delegate.begin();
    }

    public void commit() {
        delegate.commit();
    }

    public void rollback() {
        delegate.rollback();
    }

    public void end() {
        delegate.end();
    }

    public void reindex() {
        delegate.reindex();
    }

    public Set<String> getFullTextSuggestion(String keyword, FullTextSuggestion mode, int suggestionSize) {
        return delegate.getFullTextSuggestion(keyword, mode, suggestionSize);
    }

    public String getName() {
        return delegate.getName();
    }

    public DataSource getDataSource() {
        return delegate.getDataSource();
    }

    @Override
    public StorageType getType() {
        return delegate.getType();
    }

    @Override
    public ImpactAnalyzer getImpactAnalyzer() {
        return delegate.getImpactAnalyzer();
    }

    @Override
    public void adapt(MetadataRepository newRepository, boolean force) {
        delegate.adapt(newRepository, force);
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public List<ComplexTypeMetadata> findSortedTypesToDrop(DiffResults diffResults, boolean force) {
        return delegate.findSortedTypesToDrop(diffResults, force);
    }

    @Override
    public Set<String> findTablesToDrop(List<ComplexTypeMetadata> sortedTypesToDrop) {
        return delegate.findTablesToDrop(sortedTypesToDrop);
    }
}
