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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.CompoundFieldMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.amalto.core.load.io.ResettableStringWriter;
import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.Split;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.UserQueryHelper;
import com.amalto.core.query.user.metadata.Timestamp;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.storage.record.DataRecordIncludeNullValueXmlWriter;
import com.amalto.core.storage.record.DataRecordXmlWriter;
import com.amalto.core.storage.record.SystemDataRecordXmlWriter;
import com.amalto.core.storage.record.XmlDOMDataRecordReader;
import com.amalto.core.storage.record.XmlSAXDataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.IXmlServerSLWrapper;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;
import com.amalto.xmlserver.interfaces.XmlServerException;

import static com.amalto.core.query.user.UserQueryBuilder.*;

public class StorageWrapper implements IXmlServerSLWrapper {

    private static final Logger LOGGER = Logger.getLogger(StorageWrapper.class);
    
    protected static final String PROVISIONING_PREFIX_INFO = "PROVISIONING.User."; //$NON-NLS-1$

    private final DataRecordReader<String> xmlStringReader = new XmlStringDataRecordReader();

    protected StorageAdmin storageAdmin;

    public StorageWrapper() {
    }

    private Select getSelectTypeById(ComplexTypeMetadata type, String uniqueID) {
        ComplexTypeMetadata typeForSelect = type;
        while (typeForSelect.getSuperTypes() != null && !typeForSelect.getSuperTypes().isEmpty()
                && typeForSelect.getSuperTypes().size() > 0) {
            typeForSelect = (ComplexTypeMetadata) typeForSelect.getSuperTypes().iterator().next();
        }
        String[] splitUniqueID = uniqueID.split("\\."); //$NON-NLS-1$
        UserQueryBuilder qb = UserQueryBuilder.from(typeForSelect);
        Collection<FieldMetadata> keyFields = type.getKeyFields();
        if (splitUniqueID.length < (2 + keyFields.size())) {
            throw new IllegalArgumentException("ID '" + uniqueID + "' does not contain all required values for key of type '" + type.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        } 
        if (keyFields.size() == 1) {
            String uniqueIDPrefix = splitUniqueID[0] + '.' + splitUniqueID[1] + '.';
            String key = StringUtils.removeStart(uniqueID, uniqueIDPrefix);
            qb.where(eq(keyFields.iterator().next(), key));
        } else {
            int currentIndex = 2;
            for (FieldMetadata keyField : keyFields) {
                qb.where(eq(keyField, splitUniqueID[currentIndex++]));
            }
        }
        return qb.getSelect();
    }

    protected static String getTypeName(String uniqueID) {
        if (uniqueID == null) {
            throw new IllegalArgumentException("Unique id can not be null."); //$NON-NLS-1$
        }
        String[] splitUniqueID = uniqueID.split("\\."); //$NON-NLS-1$
        if(splitUniqueID.length < 3) {
            throw new IllegalArgumentException("Unique id is not valid."); //$NON-NLS-1$
        }
        return splitUniqueID[1];
    }

    @Override
    public boolean isUpAndRunning() {
        return getStorageAdmin() != null;
    }

    @Override
    public String[] getAllClusters() throws XmlServerException {
        return getStorageAdmin().getAll();
    }

    @Override
    public long deleteCluster(String clusterName) throws XmlServerException {
        long start = System.currentTimeMillis();
        {
            // TMDM-4692 Delete both master and staging data containers.
            getStorageAdmin().delete(clusterName, StorageType.MASTER, true);
            getStorageAdmin().delete(clusterName, StorageType.STAGING, true);
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public long deleteAllClusters() throws XmlServerException {
        long start = System.currentTimeMillis();
        {
            getStorageAdmin().deleteAll(true);
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public long createCluster(String clusterName) throws XmlServerException {
        long start = System.currentTimeMillis();
        {
            StorageAdmin admin = getStorageAdmin();
            String dataSourceName = admin.getDatasource(clusterName);
            admin.create(clusterName, clusterName, admin.getType(clusterName), dataSourceName);
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public boolean existCluster(String cluster) throws XmlServerException {
        StorageType storageType = cluster.endsWith(StorageAdmin.STAGING_SUFFIX) ? StorageType.STAGING : StorageType.MASTER;
        return getStorageAdmin().exist(cluster, storageType);
    }

    @Override
    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName) throws XmlServerException {
    
        return putDocumentFromFile(fileName, uniqueID, clusterName, IXmlServerSLWrapper.TYPE_DOCUMENT);
    }

    @Override
    public long putDocumentFromFile(String fileName, String uniqueID, String clusterName, String documentType) throws XmlServerException {
    
        long startTime = System.currentTimeMillis();
        File file = new File(fileName);
        if (!file.canRead()) {
            throw new XmlServerException("Can not read file '" + fileName + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String content;
        try {
            if ("BINARY".equals(documentType)) { //$NON-NLS-1$
                content = "<filename>" + file.getName() + "</filename>"; //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                content = FileUtils.readFileToString(file);
            }
            putDocumentFromString(content, uniqueID, clusterName);
        } catch (Exception e) {
            throw new XmlServerException("Can not save document file.", e); //$NON-NLS-1$
        }
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public long putDocumentFromString(String xmlString, String uniqueID, String clusterName) throws XmlServerException {
    
        return putDocumentFromString(xmlString, uniqueID, clusterName, null);
    }

    @Override
    public long putDocumentFromString(String xmlString, String uniqueID, String clusterName, String documentType) throws XmlServerException {
    
        String typeName = getTypeName(uniqueID);
        long start = System.currentTimeMillis();
        {
            Storage storage = getStorage(clusterName);
            MetadataRepository repository = storage.getMetadataRepository();
            DataRecord record = xmlStringReader.read(repository, repository.getComplexType(typeName), xmlString);
            try {
                storage.update(record);
            } catch (Exception e) {
                throw new XmlServerException(e);
            }
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public long putDocumentFromDOM(Element root, String uniqueID, String clusterName) throws XmlServerException {
    
        String typeName = getTypeName(uniqueID);
        long start = System.currentTimeMillis();
        {
            DataRecordReader<Element> reader = new XmlDOMDataRecordReader();
            Storage storage = getStorage(clusterName);
            MetadataRepository repository = storage.getMetadataRepository();
            DataRecord record = reader.read(repository, repository.getComplexType(typeName), root);
            storage.update(record);
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public long putDocumentFromSAX(String dataClusterName, XMLReader docReader, InputSource input) throws XmlServerException {
    
        String typeName = getTypeName(input.getPublicId());
        long start = System.currentTimeMillis();
        {
            Storage storage = getStorage(dataClusterName);
            if (storage == null) {
                throw new XmlServerException("Data cluster '" + dataClusterName + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            DataRecordReader<XmlSAXDataRecordReader.Input> reader = new XmlSAXDataRecordReader();
            XmlSAXDataRecordReader.Input readerInput = new XmlSAXDataRecordReader.Input(docReader, input);
            MetadataRepository repository = storage.getMetadataRepository();
            DataRecord record = reader.read(repository, repository.getComplexType(typeName), readerInput);
            storage.update(record);
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public String getDocumentAsString(String clusterName, String uniqueID) throws XmlServerException {
        return getDocumentAsString(clusterName, uniqueID, "UTF-8"); //$NON-NLS-1$
    }

    @Override
    public String getDocumentAsString(String clusterName, String uniqueID, String encoding) throws XmlServerException {
        // TODO Web UI sends incomplete ids (in case of save of instance with auto increment). Fix caller code in IXtentisRMIPort.
        String[] splitUniqueID = uniqueID.split("\\."); //$NON-NLS-1$
        if (splitUniqueID.length < 3) {
            return null;
        }
        if (encoding == null) {
            encoding = "UTF-8"; //$NON-NLS-1$
        }
        Storage storage = getStorage(clusterName);
        MetadataRepository repository = storage.getMetadataRepository();
        ComplexTypeMetadata type = repository.getComplexType(splitUniqueID[1]);
        Select select = getSelectTypeById(type, uniqueID);
        StorageResults results = null;
        try {
            storage.begin();
            results = storage.fetch(select);
            String xmlString = getXmlString(clusterName, type, results.iterator(), uniqueID, encoding, true);
            storage.commit();
            return xmlString;
        } catch (Exception e) {
            storage.rollback();
            throw new XmlServerException(e);
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    @Override
    public byte[] getDocumentBytes(String clusterName, String uniqueID, String documentType) throws XmlServerException {
        try {
            return getDocumentAsString(clusterName, uniqueID).getBytes("UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new XmlServerException(e);
        }
    }

    @Override
    public String[] getAllDocumentsUniqueID(String clusterName) throws XmlServerException {
        return getAllDocumentsUniqueID(clusterName, true);
    }

    protected String[] getAllDocumentsUniqueID(String clusterName, boolean includeClusterAndTypeName) throws XmlServerException {
        String pureClusterName = getPureClusterName(clusterName);
        Storage storage = getStorage(pureClusterName);
        MetadataRepository repository = storage.getMetadataRepository();
        Collection<ComplexTypeMetadata> typeToQuery;
        if (clusterName.contains("/")) { //$NON-NLS-1$
            String typeName = StringUtils.substringAfter(clusterName, "/"); //$NON-NLS-1$
            ComplexTypeMetadata complexType = repository.getComplexType(typeName);
            if (complexType == null) {
                throw new IllegalArgumentException("Type '" + typeName + "' does not exist in container '" + pureClusterName + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            typeToQuery = Collections.singletonList(complexType);
        } else {
            typeToQuery = getClusterTypes(clusterName);
        }
        try {
            List<String> uniqueIDs = new LinkedList<String>();
            storage.begin();            
            for (ComplexTypeMetadata currentType : typeToQuery) {
                UserQueryBuilder qb = from(currentType).selectId(currentType);
                StorageResults results = storage.fetch(qb.getSelect());
                try {
                    for (DataRecord result : results) {
                        uniqueIDs.add(getUniqueID(pureClusterName, currentType, result, includeClusterAndTypeName));
                    }
                } finally {
                    results.close();
                }
            }
            storage.commit();
            return uniqueIDs.toArray(new String[uniqueIDs.size()]);
        } catch (Exception e) {
            storage.rollback();
            throw new XmlServerException(e);
        }
    }

    @Override
    public long deleteDocument(String clusterName, final String uniqueID, String documentType) throws XmlServerException {
    
        long start = System.currentTimeMillis();
        {
            String typeName = getTypeName(uniqueID);
            Storage storage = getStorage(clusterName);
            ComplexTypeMetadata type = storage.getMetadataRepository().getComplexType(typeName);
            Select select = getSelectTypeById(type, uniqueID);
            try {
                storage.begin();
                storage.delete(select);
                storage.commit();
            } catch (Exception e) {
                storage.rollback();
                throw new XmlServerException(e);
            } finally {
                storage.end();
            }
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public int deleteItems(String clusterName, String conceptName, IWhereItem whereItem) throws XmlServerException {
        if (conceptName == null) {
            throw new IllegalArgumentException("Concept name can not be null."); //$NON-NLS-1$
        }
        if (clusterName == null) {
            throw new IllegalArgumentException("Could not find cluster name for concept '" + conceptName + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        Storage storage = getStorage(clusterName);
        MetadataRepository repository = storage.getMetadataRepository();
        ComplexTypeMetadata type = repository.getComplexType(conceptName);
        if (type == null) {
            throw new IllegalArgumentException("Type '" + conceptName + "' does not exist in cluster '" + storage.getName() //$NON-NLS-1$ //$NON-NLS-2$
                    + "'."); //$NON-NLS-1$
        }
        UserQueryBuilder qb = from(type);
        qb.where(UserQueryHelper.buildCondition(qb, whereItem, repository));
        try {
            int count;
            storage.begin();
            StorageResults results = storage.fetch(qb.getSelect());
            try {
                count = results.getCount();
            } finally {
                results.close();
            }
            storage.delete(qb.getSelect());
            storage.commit();
            return count;
        } catch (Exception e) {
            storage.rollback();
            throw new XmlServerException(e);
        }
    }

    @Override
    public long moveDocumentById(String sourceClusterName, String uniqueID, String targetClusterName) throws XmlServerException {
        throw new NotImplementedException();
    }

    @Override
    public long countItems(String clusterName, String conceptName, IWhereItem whereItem) throws XmlServerException {
        if (conceptName == null) {
            throw new IllegalArgumentException("Concept name can not be null."); //$NON-NLS-1$
        }
        if (clusterName == null) {
            throw new IllegalArgumentException("Could not find cluster name for concept '" + conceptName + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        Storage storage = getStorage(clusterName);
        MetadataRepository repository = storage.getMetadataRepository();
        Collection<ComplexTypeMetadata> types;
        if ("*".equals(conceptName)) { //$NON-NLS-1$
            types = repository.getUserComplexTypes();
        } else {
            ComplexTypeMetadata type = repository.getComplexType(conceptName);
            if (type == null) {
                throw new IllegalArgumentException("Type '" + conceptName + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            types = Collections.singletonList(type);
        }
        try {
            int count = 0;
            storage.begin();
            for (ComplexTypeMetadata type : types) {
                UserQueryBuilder qb = from(type);
                qb.where(UserQueryHelper.buildCondition(qb, whereItem, repository));
                StorageResults results = storage.fetch(qb.getSelect());
                try {
                    count += results.getCount();
                } finally {
                    results.close();
                }
            }
            storage.commit();
            return count;
        } catch (Exception e) {
            storage.rollback();
            throw new XmlServerException(e);
        }
    }

    @Override
    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters) throws XmlServerException {
        return runQuery(clusterName, query, parameters, 0, 0, false);
    }

    @Override
    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters, boolean includeNullValue) throws XmlServerException {
        return runQuery(clusterName, query, parameters, 0, 0, false, true);
    }

    @Override
    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters, int start, int limit, boolean withTotalCount) throws XmlServerException {
        return runQuery(clusterName, query, parameters, 0, 0, false, false);
    }

    public ArrayList<String> runQuery(String clusterName, String query, String[] parameters, int start, int limit, boolean withTotalCount, boolean includeNullValue) throws XmlServerException {
        Storage storage = getStorage(clusterName);
        // replace parameters in the procedure
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                String param = parameters[i];
                query = query.replaceAll("([^\\\\])%" + i + "([^\\d]*)", "$1" + param + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        }   
        StorageResults results = null;
        try {
            storage.begin();
            results = storage.fetch(from(query).getExpression());
            ResettableStringWriter writer = new ResettableStringWriter();
            DataRecordWriter xmlWriter ;
            if(includeNullValue == false){
                xmlWriter = new DataRecordXmlWriter("result"); //$NON-NLS-1$
            } else {
                xmlWriter = new DataRecordIncludeNullValueXmlWriter("result"); //$NON-NLS-1$
            }
            ArrayList<String> resultsAsString = new ArrayList<String>(results.getCount());
            for (DataRecord result : results) {
                xmlWriter.write(result, writer);
                resultsAsString.add(writer.toString());
                writer.reset();
            }
            storage.commit();
            return resultsAsString;
        } catch (Exception e) {
            storage.rollback();
            throw new RuntimeException("Could not create query results", e); //$NON-NLS-1$
        } finally {
            if(results != null) {
                results.close();
            }
        }
    }

    @Override
    public List<String> getItemPKsByCriteria(ItemPKCriteria criteria) throws XmlServerException {
        String clusterName = criteria.getClusterName();
        Storage storage = getStorage(clusterName);
        MetadataRepository repository = storage.getMetadataRepository();
        int totalCount = 0;
        List<String> itemPKResults = new LinkedList<String>();
        String typeName = criteria.getConceptName();
        try {
            storage.begin();
            if (typeName != null && !typeName.isEmpty()) {
                totalCount = getTypeItemCount(criteria, repository.getComplexType(typeName), storage);
                itemPKResults.addAll(getTypeItems(criteria, repository.getComplexType(typeName), storage, repository
                        .getComplexType(typeName).getName()));
            } else {
                // TMDM-4651: Returns type in correct dependency order.
                Collection<ComplexTypeMetadata> types = getClusterTypes(clusterName);
                int maxCount = criteria.getMaxItems();
                if (criteria.getSkip() < 0) { // MDM Studio may send negative values
                    criteria.setSkip(0);
                }
                List<String> currentInstanceResults;
                for (ComplexTypeMetadata type : types) {
                    int count = getTypeItemCount(criteria, type, storage);
                    totalCount += count;
                    if (itemPKResults.size() < maxCount) {
                        if (count > criteria.getSkip()) {
                            currentInstanceResults = getTypeItems(criteria, type, storage, type.getName());
                            int n = maxCount - itemPKResults.size();
                            if (n <= currentInstanceResults.size()) {
                                itemPKResults.addAll(currentInstanceResults.subList(0, n));
                            } else {
                                itemPKResults.addAll(currentInstanceResults);
                            }
                            criteria.setMaxItems(criteria.getMaxItems() - currentInstanceResults.size());
                            criteria.setSkip(0);
                        } else {
                            criteria.setSkip(criteria.getSkip() - count);
                        }
                    }
                }
            }
            itemPKResults.add(0, "<totalCount>" + totalCount + "</totalCount>"); //$NON-NLS-1$ //$NON-NLS-2$
            storage.commit();
        } catch (Exception e) {
            storage.rollback();
            throw new XmlServerException(e);
        }
        return itemPKResults;
    }

    protected Collection<ComplexTypeMetadata> getClusterTypes(String clusterName) {
        Storage storage = getStorage(clusterName);
        MetadataRepository repository = storage.getMetadataRepository();
        return MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
    }

    protected int getTypeItemCount(ItemPKCriteria criteria, ComplexTypeMetadata type, Storage storage) {
        StorageResults results = storage.fetch(buildQueryBuilder(from(type).selectId(type), criteria, type).getSelect());
        try {
            return results.getCount();
        } finally {
            results.close();
        }
    }

    protected List<String> getTypeItems(ItemPKCriteria criteria, ComplexTypeMetadata type, Storage storage,
            String resultElementName) throws XmlServerException {
        // Build base query
        UserQueryBuilder qb = from(type).select(alias(timestamp(), "timestamp")) //$NON-NLS-1$
                .select(alias(taskId(), "taskid")) //$NON-NLS-1$
                .selectId(type).limit(criteria.getMaxItems()).start(criteria.getSkip());
        List<String> list = new LinkedList<String>();
        StorageResults results = storage.fetch(buildQueryBuilder(qb, criteria, type).getSelect());
        DataRecordWriter writer = new ItemPKCriteriaResultsWriter(resultElementName, type);
        ResettableStringWriter stringWriter = new ResettableStringWriter();       
        try {
            for (DataRecord result : results) {
                writer.write(result, stringWriter);
                list.add(stringWriter.toString());
                stringWriter.reset();
            }
        } catch (Exception e) {
            storage.rollback();
            throw new XmlServerException(e);
        } finally {
            results.close();
        }
        return list;
    }

    private UserQueryBuilder buildQueryBuilder(UserQueryBuilder qb, ItemPKCriteria criteria, ComplexTypeMetadata type) {
        // Filter by keys: expected format here: $EntityTypeName/Path/To/Field$[id_for_lookup]
        String keysString = criteria.getKeys();
        if (keysString != null && !keysString.isEmpty()) {
            buildKeyCondition(qb, type, criteria.getClusterName(), keysString);
        }

        String keysKeywords = criteria.getKeysKeywords();
        if (keysKeywords != null && !keysKeywords.isEmpty()) {
            if (keysKeywords.charAt(0) == '$') {
                char[] chars = keysKeywords.toCharArray();
                List<String> paths = new LinkedList<String>();
                StringBuilder currentPath = new StringBuilder();
                StringBuilder id = new StringBuilder();
                StringBuilder idForLookup = new StringBuilder();
                int dollarCount = 0;
                int slashCount = 0;
                for (char current : chars) {
                    switch (current) {
                    case '$':
                        dollarCount++;
                        break;
                    case ',':
                        paths.add(currentPath.toString());
                        currentPath = new StringBuilder();
                        slashCount = 0;
                        break;
                    case '/':
                        slashCount++;
                    default:
                        if (dollarCount == 0) {
                            id.append(current);
                        } else if (dollarCount >= 2) {
                            idForLookup.append(current);
                        } else if (slashCount > 0) {
                            if (slashCount != 1 || '/' != current) {
                                currentPath.append(current);
                            }
                        }
                    }
                }
                paths.add(currentPath.toString());
                if (currentPath.toString().isEmpty()) {
                    // TODO Implement compound key support
                    qb.where(eq(type.getKeyFields().iterator().next(), id.toString()));
                } else {
                    Condition globalCondition = UserQueryHelper.FALSE;
                    for (String path : paths) {
                        Condition pathCondition = UserQueryHelper.FALSE;
                        // TMDM-10201, if FK point to a inheritance type, like: keysKeywords = $Feature/Value/EnumFK,EnumValueType/EnumFK$[1]
                        // Only need to query "Feature" by "Value/EnumFK=1"
                        FieldMetadata field = null;
                        try {
                            field = type.getField(path);
                        } catch (IllegalArgumentException e) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Ignoring field '" + path + "'", e); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                        if (field != null) {
                            Set<FieldMetadata> candidateFields = Collections.singleton(type.getField(path));
                            for (FieldMetadata candidateField : candidateFields) {
                                if (candidateField instanceof ReferenceFieldMetadata
                                        && ((ReferenceFieldMetadata) candidateField).getReferencedField() instanceof CompoundFieldMetadata) {
                                    // composite key: fkValue of the form '[value1.value2.value3...]'
                                    pathCondition = or(
                                            eq(candidateField, StringUtils.replace(idForLookup.toString(), ".", "][")), pathCondition); //$NON-NLS-1$ //$NON-NLS-2$
                                } else {
                                    pathCondition = or(eq(candidateField, idForLookup.toString()), pathCondition);
                                }
                            }
                            globalCondition = or(globalCondition, pathCondition);
                        }
                    }
                    qb.where(globalCondition);
                }
            } else {
                buildKeyCondition(qb, type, criteria.getClusterName(), keysKeywords);
            }
        }
        // Filter by timestamp
        if (criteria.getFromDate() > 0) {
            qb.where(gte(timestamp(), String.valueOf(criteria.getFromDate())));
        }
        if (criteria.getToDate() > 0) {
            qb.where(lte(timestamp(), String.valueOf(criteria.getToDate())));
        }
        // Content keywords
        String contentKeywords = criteria.getContentKeywords();
        if (contentKeywords != null && !contentKeywords.isEmpty()) {
            if (criteria.isUseFTSearch()) {
                qb.where(fullText(contentKeywords));
            } else {
                Condition condition = null;
                for (FieldMetadata field : type.getFields()) {
                    if (StorageMetadataUtils.isValueAssignable(contentKeywords, field.getType().getName())) {
                        if (!(field instanceof ContainedTypeFieldMetadata)) {
                            if (condition == null) {
                                condition = contains(field, contentKeywords);
                            } else {
                                condition = or(condition, contains(field, contentKeywords));
                            }
                        }
                    }
                }
                if (condition != null) {
                    qb.where(condition);
                }
            }
        }

        return qb;
    }
    
    @Override
    public void clearCache() {
    }
    
    @Override
    public boolean supportTransaction() {
        return true;
    }

    protected StorageAdmin getStorageAdmin() {
        if (storageAdmin == null) {
            storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        }
        return storageAdmin;
    }

    protected Storage getStorage(String clusterName) {
        StorageAdmin admin = getStorageAdmin();
        if (!admin.exist(clusterName, admin.getType(clusterName))) {
            throw new IllegalStateException("Data container '" + clusterName + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return admin.get(clusterName, admin.getType(clusterName));
    }

    @Override
    public void start(String dataClusterName) throws XmlServerException {
        Storage storage = getStorage(dataClusterName);
        storage.begin();
    }

    @Override
    public void commit(String dataClusterName) throws XmlServerException {
        Storage storage = getStorage(dataClusterName);
        storage.commit();
    }

    @Override
    public void rollback(String dataClusterName) throws XmlServerException {
        Storage storage = getStorage(dataClusterName);
        storage.rollback();
    }

    @Override
    public void end(String dataClusterName) throws XmlServerException {
        Storage storage = getStorage(dataClusterName);
        storage.end();
    }

    @Override
    public void close() throws XmlServerException {
        getStorageAdmin().close();
    }
    
    @Override
    public List<String> globalSearch(String dataCluster, String keyword, int start, int end) throws XmlServerException {
        Storage storage = getStorage(dataCluster);
        MetadataRepository repository = storage.getMetadataRepository();
        Iterator<ComplexTypeMetadata> types = repository.getUserComplexTypes().iterator();
        if (types.hasNext()) {
            ComplexTypeMetadata mainType = types.next();
            while (types.hasNext() && mainType.getKeyFields().size() > 1) {
                ComplexTypeMetadata next = types.next();
                if (next.getKeyFields().size() > 1) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Ignoring type '" + next.getName() + "' (compound key)."); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                mainType = next;
            }
            UserQueryBuilder qb = from(mainType);
            while (types.hasNext()) {
                ComplexTypeMetadata additionalType = types.next();
                if (additionalType.getKeyFields().size() == 1) {
                    qb.and(additionalType);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Ignoring type '" + additionalType.getName() + "' (compound key)."); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            qb.where(fullText(keyword));
            qb.start(start);
            qb.limit(end);
            StorageResults results = null;
            try {
                storage.begin();
                results = Split.fetchAndMerge(storage, qb.getSelect()); // TMDM-7290: Split main query into smaller queries.
                DataRecordWriter writer = new FullTextResultsWriter(keyword);
                List<String> resultsAsXmlStrings = new ArrayList<String>(results.getCount() + 1);
                resultsAsXmlStrings.add(String.valueOf(results.getCount()));
                for (DataRecord result : results) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    writer.write(result, output);
                    output.flush();
                    resultsAsXmlStrings.add(output.toString());
                }
                storage.commit();
                return resultsAsXmlStrings;
            } catch (Exception e) {
                storage.rollback();
                throw new XmlServerException(e);
            } finally {
                if (results != null) {
                    results.close();
                }
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void exportDocuments(String clusterName, int start, int end, boolean includeMetadata, OutputStream outputStream) throws XmlServerException {
        // No support for bulk export when using SQL storages (this could be in HibernateStorage but would require to define new API).
        throw new NotImplementedException("No support for bulk export."); //$NON-NLS-1$
    }

    private void buildKeyCondition(UserQueryBuilder qb, ComplexTypeMetadata type, String clusterName, String keysString) {
        Collection<FieldMetadata> keyFields = type.getKeyFields();
        if (clusterName.equals(XSystemObjects.DC_UPDATE_PREPORT.getName()) && type.getName().equals("Update")) { //$NON-NLS-1$
            // UpdateReport: Source.TimeInMillis is the key
            String[] keys = keysString.split("\\."); //$NON-NLS-1$
            if (keys.length == 1 || keys.length > 2) {
                throw new IllegalArgumentException("The key format is 'Source.TimeInMillis' for type " + type.getName()); //$NON-NLS-1$
            } else if (keys.length == 2) {
                if (keys[1] == null || keys[1].trim().isEmpty()
                        || !StorageMetadataUtils.isValueAssignable(keys[1], Timestamp.INSTANCE.getTypeName())) {
                    throw new IllegalArgumentException(
                            "The key format is 'Source.TimeInMillis' for type '" + type.getName() + "'" + //$NON-NLS-1$//$NON-NLS-2$
                                    " and the TimeInMillis key value must be a long type."); //$NON-NLS-1$
                }
                int i = 0;
                for (FieldMetadata keyField : keyFields) {
                    qb.where(eq(keyField, keys[i]));
                    i++;
                }
            }
        } else {
            if (keyFields.size() > 1) {
                throw new IllegalArgumentException("Expected type '" + type.getName() + "' to contain only 1 key field."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String uniqueKeyFieldName = keyFields.iterator().next().getName();
            qb.where(eq(type.getField(uniqueKeyFieldName), keysString));
        }
    }

    @Override
    public String[] getDocumentsAsString(String clusterName, String[] uniqueIDs) throws XmlServerException {
        return getDocumentsAsString(clusterName, uniqueIDs, "UTF-8"); //$NON-NLS-1$
    }

    @Override
    public String[] getDocumentsAsString(String clusterName, String[] uniqueIDs, String encoding) throws XmlServerException {
        if (uniqueIDs == null || uniqueIDs.length == 0) {
            return new String[0];
        }
        List<String> xmlStrings = new ArrayList<String>(uniqueIDs.length);
        for (String uniqueID : uniqueIDs) { 
            xmlStrings.add(getDocumentAsString(clusterName, uniqueID, encoding));
        }
        return xmlStrings.toArray(new String[xmlStrings.size()]);
    }

    public static String getPureClusterName(String clusterName){
        return clusterName.contains("/") ? StringUtils.substringBefore(clusterName, "/") : clusterName;//$NON-NLS-1$ //$NON-NLS-2$
    }

    protected String getXmlString(String clusterName, ComplexTypeMetadata type, Iterator<DataRecord> iterator, String uniqueID, String encoding, boolean isUserFormat) throws IOException {
        String xmlString = null;
        if (iterator.hasNext()) {
            DataRecord result = iterator.next();
            if (iterator.hasNext()) {
                iterateUnexceptedRecords(LOGGER, uniqueID, iterator);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
            // Enforce root element name in case query returned instance of a subtype.
            DataRecordWriter dataRecordXmlWriter = isUserFormat ? new DataRecordXmlWriter(type) : new SystemDataRecordXmlWriter(
                    (ClassRepository) getStorage(clusterName).getMetadataRepository(), type);           
            if (isUserFormat) {
                dataRecordXmlWriter.setSecurityDelegator(SecuredStorage.getDelegator());
                String key = uniqueID.startsWith(PROVISIONING_PREFIX_INFO) ? StringUtils.substringAfter(uniqueID,
                        PROVISIONING_PREFIX_INFO) : uniqueID.split("\\.")[2]; //$NON-NLS-1$
                long timestamp = result.getRecordMetadata().getLastModificationTime();
                String taskId = result.getRecordMetadata().getTaskId();
                String modelName = StringUtils.substringBeforeLast(clusterName, StorageAdmin.STAGING_SUFFIX);
                byte[] start = ("<ii><c>" + clusterName + "</c><dmn>" + modelName + "</dmn><dmr/><sp/><t>" + timestamp + "</t><taskId>" + taskId + "</taskId><i>" + StringEscapeUtils.escapeXml(key) + "</i><p>").getBytes(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                output.write(start);
            }
            dataRecordXmlWriter.write(result, output);
            if (isUserFormat) {
                byte[] end = ("</p></ii>").getBytes(); //$NON-NLS-1$
                output.write(end);
            }
            output.flush();
            xmlString = new String(output.toByteArray(), encoding);
        }
        return xmlString;
    }

    protected static String getUniqueID(String clusterName, ComplexTypeMetadata type, DataRecord record, boolean includeClusterAndTypeName) {
        StringBuilder builder = new StringBuilder();
        if (includeClusterAndTypeName) {
            builder.append(clusterName).append('.').append(type.getName()).append('.');
        }
        Iterator<FieldMetadata> iterator = type.getKeyFields().iterator();
        while (iterator.hasNext()) {
            builder.append(String.valueOf(record.get(iterator.next())));
            if (iterator.hasNext()) {
                builder.append('.');
            }
        }
        return builder.toString();
    }

    /**
     * Iterate unexcepted records to make sure Session can be truely closed, and log WARN message <br />
     * See TMDM-6712: Consumes all results in iterator
     */
    protected static void iterateUnexceptedRecords(Logger logger, String uniqueID, Iterator<DataRecord> iterator) {
        int recordsLeft = 1;
        while (iterator.hasNext()) { // TMDM-6712: Consumes all results in iterator
            iterator.next();
            if (recordsLeft % 10 == 0) {
                logger.warn("Processing query with id '"+ uniqueID +"' lead to unexpected number of results (" + recordsLeft + " so far)."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
            }
            recordsLeft++;
        }
        throw new IllegalStateException("Expected only 1 result with id '"+ uniqueID +"'."); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
