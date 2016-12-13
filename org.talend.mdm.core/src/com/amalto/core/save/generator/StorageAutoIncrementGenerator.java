/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save.generator;

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.TransactionManager;

/**
 * This {@link com.amalto.core.save.generator.AutoIdGenerator generator} is a more secure way to generate auto increment
 * values in case of concurrent access to the same underlying database.
 */
@SuppressWarnings("nls")
public class StorageAutoIncrementGenerator implements AutoIdGenerator {

    private static final Logger LOGGER = Logger.getLogger(StorageAutoIncrementGenerator.class);

    private static final String AUTO_INCREMENT = "AutoIncrement";

    private final Storage system;

    /**
     * Build a {@link com.amalto.core.save.generator.AutoIdGenerator} using current available system storage (from current
     * context).
     */
    StorageAutoIncrementGenerator() {
        this(ServerContext.INSTANCE.get().getStorageAdmin().get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM));
    }

    /**
     * Build a {@link com.amalto.core.save.generator.AutoIdGenerator} using provided system
     * {@link com.amalto.core.storage.Storage storage}.
     * 
     * @param storage The {@link com.amalto.core.storage.Storage system} storage to use. This storage must be of
     * {@link com.amalto.core.storage.StorageType#SYSTEM} type.
     */
    public StorageAutoIncrementGenerator(Storage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("System storage cannot be null.");
        }
        if (storage.getType() != StorageType.SYSTEM) {
            throw new IllegalArgumentException("Storage should be of " + StorageType.SYSTEM + " (was " + storage.getType() + ").");
        }
        this.system = storage;
        // Create auto increment if doesn't exist
        MetadataRepository repository = system.getMetadataRepository();
        ComplexTypeMetadata autoIncrementType = repository.getComplexType(AUTO_INCREMENT);
        UserQueryBuilder qb = from(autoIncrementType).forUpdate();

        // Initialization of AutoIncrement should be AD-HOC, when embedded within a Long Transaction.
        TransactionManager manager = ServerContext.INSTANCE.get().getTransactionManager();
        manager.associate(manager.create(Transaction.Lifetime.AD_HOC));

        system.begin();
        try {
            StorageResults results = system.fetch(qb.getSelect());
            if (results.getCount() == 0) {
                DataRecord autoIncrementRecord = new DataRecord(autoIncrementType, UnsupportedDataRecordMetadata.INSTANCE);
                autoIncrementRecord.set(autoIncrementType.getField("id"), AUTO_INCREMENT); 
                system.update(autoIncrementRecord);
            }
            system.commit();
        } catch (Exception e) {
            // There might be caused by concurrent access to the database, leading to "duplicate pk" issues. However,
            // all the code in this class cares about is a AutoIncrement instance in database, not by whom.
            system.rollback();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Unable to create auto increment (might be caused by concurrent access to database).", e);
            }
        }
    }

    @Override
    public String generateId(String dataClusterName, String conceptName, String keyElementName) {
        Server server = ServerContext.INSTANCE.get();
        // Create or get current transaction
        TransactionManager manager = server.getTransactionManager();
        Transaction previousTransaction = null;
        if (manager.hasTransaction()) {
            previousTransaction = manager.currentTransaction(); // Remember existing transaction (re-set at the end).
        }
        // Query and update auto increment value using custom lock strategy
        Transaction transaction = manager.create(Transaction.Lifetime.LONG);
        manager.associate(transaction);
        try {
            // Set lock strategy
            transaction.setLockStrategy(Transaction.LockStrategy.LOCK_FOR_UPDATE);
            system.begin(); // Implicitly adds system in current transaction
            // Query and update auto increment record
            MetadataRepository repository = system.getMetadataRepository();
            ComplexTypeMetadata autoIncrementType = repository.getComplexType(AUTO_INCREMENT);
            // Get the auto increment record
            UserQueryBuilder qb = from(autoIncrementType)
                    .where(eq(autoIncrementType.getField("id"), AUTO_INCREMENT))  
                    .limit(1)
                    .forUpdate();
            DataRecord autoIncrementRecord = null;
            StorageResults autoIncrementRecordResults = system.fetch(qb.getSelect());
            Iterator<DataRecord> iterator = autoIncrementRecordResults.iterator();
            if (iterator.hasNext()) {
                autoIncrementRecord = iterator.next();
            } else {
                autoIncrementRecord = new DataRecord(autoIncrementType, new DataRecordMetadataImpl());
                autoIncrementRecord.set(autoIncrementType.getField("id"), AUTO_INCREMENT);
            }
            // Update it
            String key = dataClusterName + "." + AutoIncrementGenerator.getConceptForAutoIncrement(dataClusterName, conceptName) + "." + keyElementName;
            Integer value = getValue(autoIncrementType, autoIncrementRecord, key);
            // Update the DB record before leaving
            system.update(autoIncrementRecord);
            if (!DataRecord.ValidateRecord.get()) { // don't actually save if for Record Validation
                transaction.commit();
            } else {
                transaction.rollback();
            }
            return String.valueOf(value);
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException("Unable to get auto increment value.", e);  
        } finally {
            if (previousTransaction != null) {
                manager.associate(previousTransaction); // Restore previous transaction (if any).
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private Integer getValue(ComplexTypeMetadata autoIncrementType, DataRecord autoIncrementRecord, String key){
        Integer value = null;
        FieldMetadata entryField = autoIncrementType.getField("entry"); 
        ComplexTypeMetadata entryType = (ComplexTypeMetadata) entryField.getType();
        FieldMetadata keyField = entryType.getField("key"); 
        FieldMetadata valueField = entryType.getField("value"); 
        List<DataRecord> entries = (List<DataRecord>) autoIncrementRecord.get(entryField);
        if (entries != null) {
            for (DataRecord entry : entries) { // Find entry for type in database object
                if (key.equals(String.valueOf(entry.get(keyField)))) {
                    Integer integer = (Integer) entry.get(valueField);
                    integer ++;
                    entry.set(valueField, integer);
                    value = integer;
                }
            }
        }
        if (value == null) { // No entry for current asked type, creates one
            DataRecord entry = new DataRecord((ComplexTypeMetadata) entryField.getType(), UnsupportedDataRecordMetadata.INSTANCE);
            entry.set(keyField, key);
            entry.set(valueField, 0);
            autoIncrementRecord.set(entryField, entry); // This add at end of collection is already present
            value = getValue(autoIncrementType, autoIncrementRecord, key);
        }
        return value;
    }

    @Override
    public void saveState(XmlServer server) {
        // No need to do anything (implementation saves new values when generated).
    }

    @Override
    public boolean isInitialized() {
        return true;
    }
    
    @Override
    public void init() {
        // Nothing to do.
    }
}
