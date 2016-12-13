/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage;

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.load.io.ResettableStringWriter;
import com.amalto.core.query.StorageTestCase;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordWriter;

/**
 * created by suplch on 2013-7-1 Detailled comment
 * 
 */
public class ItemPKCriteriaResultsWriterTest extends TestCase {

    public ItemPKCriteriaResultsWriterTest() {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }

    public void testWriter() throws Exception {

        final MetadataRepository repository = prepareMetadata("Product.xsd"); //$NON-NLS-1$
        final Storage storage = prepareStorage(repository);

        StorageWrapper wrapper = new StorageWrapper() {

            @Override
            protected Storage getStorage(String dataClusterName) {
                return storage;
            }

        };

        String xml = "<ii><c>Product</c><n>Product</n><dmn>Product</dmn><i>333</i><t>1372654669313</t><taskId></taskId><p> <Product><Id>333</Id><Name>333</Name><Description>333</Description><Price>333</Price></Product></p></ii>"; //$NON-NLS-1$
        wrapper.putDocumentFromString(xml, "Product.Product.333", "Product"); //$NON-NLS-1$ //$NON-NLS-2$

        ComplexTypeMetadata typeForSelect = repository.getComplexType("Product"); //$NON-NLS-1$
        Select select = getSelectTypeById(typeForSelect, new String[] { "Product", "Product", "333" }); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

        StorageResults records = storage.fetch(select);
        Iterator<DataRecord> iterator = records.iterator();

        DataRecordWriter writer = new ItemPKCriteriaResultsWriter(typeForSelect.getName(), typeForSelect);

        assertTrue(iterator.hasNext());
        ResettableStringWriter stringWriter = new ResettableStringWriter();
        DataRecord record = iterator.next();
        writer.write(record, stringWriter);
        String resultXml = stringWriter.toString();
        assertNotNull(resultXml);
        assertTrue(resultXml.contains("<i>333</i>")); //$NON-NLS-1$

        xml = "<ii><c>Product</c><n>Product</n><dmn>Product</dmn><i>&quot;5&lt;6&gt;4&quot;&amp;7</i><t>1372654669313</t><taskId></taskId><p> <Product><Id>&quot;5&lt;6&gt;4&quot;&amp;7</Id><Name>333</Name><Description>333</Description><Price>333</Price></Product></p></ii>"; //$NON-NLS-1$
        wrapper.putDocumentFromString(xml, "Product.Product.\"5<6>4\"&7", "Product"); //$NON-NLS-1$ //$NON-NLS-2$

        typeForSelect = repository.getComplexType("Product"); //$NON-NLS-1$
        select = getSelectTypeById(typeForSelect, new String[] { "Product", "Product", "\"5<6>4\"&7" }); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

        records = storage.fetch(select);
        iterator = records.iterator();

        writer = new ItemPKCriteriaResultsWriter(typeForSelect.getName(), typeForSelect);

        assertTrue(iterator.hasNext());

        stringWriter = new ResettableStringWriter();
        record = iterator.next();
        writer.write(record, stringWriter);
        resultXml = stringWriter.toString();
        assertNotNull(resultXml);
        assertTrue(resultXml.contains("<i>&quot;5&lt;6&gt;4&quot;&amp;7</i>")); //$NON-NLS-1$
    }

    private static Select getSelectTypeById(ComplexTypeMetadata type, String[] splitUniqueId) {
        ComplexTypeMetadata typeForSelect = type;
        while (typeForSelect.getSuperTypes() != null && !typeForSelect.getSuperTypes().isEmpty()
                && typeForSelect.getSuperTypes().size() > 0) {
            typeForSelect = (ComplexTypeMetadata) typeForSelect.getSuperTypes().iterator().next();
        }
        UserQueryBuilder qb = UserQueryBuilder.from(typeForSelect);
        Collection<FieldMetadata> keyFields = type.getKeyFields();

        if (splitUniqueId.length < (2 + keyFields.size())) {
            StringBuilder builder = new StringBuilder();
            for (String currentId : splitUniqueId) {
                builder.append(currentId).append('.');
            }
            throw new IllegalArgumentException("Id '" + builder.toString() //$NON-NLS-1$
                    + "' does not contain all required values for key of type '" + type.getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        int currentIndex = 2;
        for (FieldMetadata keyField : keyFields) {
            qb.where(eq(keyField, splitUniqueId[currentIndex++]));
        }
        return qb.getSelect();
    }

    private MetadataRepository prepareMetadata(String dataModelFile) {
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageWrapperTest.class.getResourceAsStream(dataModelFile));
        return repository;
    }

    private Storage prepareStorage(MetadataRepository repository) {
        Storage storage = new HibernateStorage("Product"); //$NON-NLS-1$
        storage.init(ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-Default", "MDM")); //$NON-NLS-1$ //$NON-NLS-2$
        storage.prepare(repository, true);
        return storage;
    }
}
