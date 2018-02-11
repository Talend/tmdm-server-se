/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.dispatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.set.CompositeSet;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.compare.Compare.DiffResults;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;

import com.amalto.core.query.user.Expression;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.transaction.StorageTransaction;

/**
 * A {@link com.amalto.core.storage.Storage} implementation that puts together several instances of Storage and expose
 * them as a single Storage instance.
 */
public class CompositeStorage implements Storage {

    private final Storage[] storages;

    /**
     * @param storages All the {@link com.amalto.core.storage.Storage} instances to be used in this composite Storage
     * implementation.
     */
    public CompositeStorage(Storage... storages) {
        this.storages = storages;
    }

    @Override
    public Storage asInternal() {
        List<Storage> internalStorages = new LinkedList<Storage>();
        for (Storage storage : storages) {
            internalStorages.add(storage.asInternal());
        }
        return new CompositeStorage(internalStorages.toArray(new Storage[internalStorages.size()]));
    }

    @Override
    public int getCapabilities() {
        int capabilities = 0;
        for (Storage storage : storages) {
            capabilities &= storage.getCapabilities();
        }
        return capabilities;
    }

    @Override
    public StorageTransaction newStorageTransaction() {
        return null;
    }

    @Override
    public void init(DataSourceDefinition dataSource) {
        for (Storage storage : storages) {
            storage.init(dataSource);
        }
    }

    @Override
    public void prepare(MetadataRepository repository, Set<Expression> optimizedExpressions, boolean force,
            boolean dropExistingData) {
        for (Storage storage : storages) {
            storage.prepare(repository, optimizedExpressions, force, dropExistingData);
        }
    }

    @Override
    public void prepare(MetadataRepository repository, boolean dropExistingData) {
        for (Storage storage : storages) {
            storage.prepare(repository, dropExistingData);
        }
    }

    @Override
    public MetadataRepository getMetadataRepository() {
        MetadataRepository current = storages[0].getMetadataRepository();
        for (Storage storage : storages) {
            if (storage.getMetadataRepository() != current) {
                throw new IllegalStateException("Storages do not share same metadata repository.");
            }
            current = storage.getMetadataRepository();
        }
        return current;
    }

    @Override
    public StorageResults fetch(Expression userQuery) {
        final List<StorageResults> results = new LinkedList<StorageResults>();
        for (Storage storage : storages) {
            results.add(storage.fetch(userQuery));
        }
        return new CompositeStorageResults(results);
    }

    @Override
    public void update(DataRecord record) {
        for (Storage storage : storages) {
            storage.update(record);
        }
    }

    @Override
    public void update(Iterable<DataRecord> records) {
        for (Storage storage : storages) {
            storage.update(records);
        }
    }

    @Override
    public void delete(Expression userQuery) {
        for (Storage storage : storages) {
            storage.delete(userQuery);
        }
    }

    @Override
    public void delete(DataRecord record) {
        for (Storage storage : storages) {
            storage.delete(record);
        }
    }

    @Override
    public void close() {
        for (Storage storage : storages) {
            storage.close();
        }
    }

    @Override
    public void close(boolean dropExistingData) {
        for (Storage storage : storages) {
            storage.close(dropExistingData);
        }
    }

    @Override
    public void begin() {
        for (Storage storage : storages) {
            storage.begin();
        }
    }

    @Override
    public void commit() {
        for (Storage storage : storages) {
            storage.commit();
        }
    }

    @Override
    public void rollback() {
        for (Storage storage : storages) {
            storage.rollback();
        }
    }

    @Override
    public void end() {
    }

    @Override
    public void reindex() {
        for (Storage storage : storages) {
            storage.reindex();
        }
    }

    @Override
    public Set<String> getFullTextSuggestion(String keyword, FullTextSuggestion mode, int suggestionSize) {
        CompositeSet results = new CompositeSet();
        for (Storage storage : storages) {
            results.addComposited(storage.getFullTextSuggestion(keyword, mode, suggestionSize));
        }
        return results;
    }

    @Override
    public String getName() {
        StringBuilder builder = new StringBuilder();
        for (Storage storage : storages) {
            builder.append(storage.getName()).append(' ');
        }
        return builder.toString();
    }

    @Override
    public DataSource getDataSource() {
        DataSource current = storages[0].getDataSource();
        for (Storage storage : storages) {
            if (storage.getMetadataRepository() != current) {
                throw new IllegalStateException("Storages do not share same datasource.");
            }
            current = storage.getDataSource();
        }
        return current;
    }

    @Override
    public StorageType getType() {
        StorageType current = storages[0].getType();
        for (Storage storage : storages) {
            if (storage.getType() != current) {
                throw new IllegalStateException("Storages do not share same datasource.");
            }
            current = storage.getType();
        }
        return current;
    }

    @Override
    public ImpactAnalyzer getImpactAnalyzer() {
        final List<ImpactAnalyzer> analyzers = new LinkedList<ImpactAnalyzer>();
        for (Storage storage : storages) {
            analyzers.add(storage.getImpactAnalyzer());
        }
        return new CompositeImpactAnalyzer(analyzers);
    }

    @Override
    public void adapt(MetadataRepository newRepository, boolean force) {
        for (Storage storage : storages) {
            storage.adapt(newRepository, force);
        }
    }

    @Override
    public boolean isClosed() {
        for (Storage storage : storages) {
            if (storage.isClosed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ComplexTypeMetadata> findSortedTypesToDrop(DiffResults diffResults, boolean force) {
        final List<ComplexTypeMetadata> sortedtypesToDrop = new ArrayList<ComplexTypeMetadata>();
        for (Storage storage : storages) {
            sortedtypesToDrop.addAll(storage.findSortedTypesToDrop(diffResults, force));
        }
        return sortedtypesToDrop;
    }

    @Override
    public Set<String> findTablesToDrop(List<ComplexTypeMetadata> sortedTypesToDrop) {
        return new HashSet<String>();
    }
}
