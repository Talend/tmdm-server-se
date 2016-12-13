/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.query;

import static com.amalto.core.query.user.UserQueryBuilder.*;
import static com.amalto.core.query.user.UserStagingQueryBuilder.*;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import com.amalto.core.query.optimization.ConfigurableContainsOptimizer;
import com.amalto.core.query.user.BinaryLogicOperator;
import com.amalto.core.query.user.Compare;
import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.FieldFullText;
import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.Predicate;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.Split;
import com.amalto.core.query.user.StringConstant;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.UserQueryHelper;
import com.amalto.core.storage.FullTextResultsWriter;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.exception.FullTextQueryCompositeKeyException;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.storage.record.ViewSearchResultsWriter;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;

@SuppressWarnings("nls")
public class StorageFullTextTest extends StorageTestCase {

    private static Logger LOG = Logger.getLogger(StorageFullTextTest.class);
    private static ComplexTypeMetadata a1 = repository.getComplexType("a1");
    private static ComplexTypeMetadata a2 = repository.getComplexType("a2");
    private static ComplexTypeMetadata a3 = repository.getComplexType("a3");

    static {
        initStorage(DATASOURCE_FULLTEXT);
        try {
            populateData();
        } catch (Exception e) {
            LOG.error("Populate Date failed");
        }
    }

    private static void populateData() throws Exception {
        try {
            clean();
        } catch (Exception e) {
            // ignore
        }
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords.add(factory.read(repository, productFamily, "<ProductFamily>\n" + "    <Id>1</Id>\n"
                + "    <Name>ProductFamily1</Name>\n" + "</ProductFamily>"));
        allRecords.add(factory.read(repository, productFamily, "<ProductFamily>\n" + "    <Id>2</Id>\n"
                + "    <Name>ProductFamily2</Name>\n" + "</ProductFamily>"));
        allRecords.add(factory.read(repository, productFamily, "<ProductFamily>\n" + "    <Id>3</Id>\n"
                + "    <Name>ProductFamily3</Name>\n" + "</ProductFamily>"));
        allRecords.add(factory.read(repository, productFamily, "<ProductFamily>\n" + "    <Id>4</Id>\n"
                + "    <Name>test_name4</Name>\n" + "</ProductFamily>"));
        allRecords.add(factory.read(repository, product, "<Product>\n" + "    <Id>1</Id>\n" + "    <Name>talend</Name>\n"
                + "    <ShortDescription>Short description word</ShortDescription>\n"
                + "    <LongDescription>Long description</LongDescription>\n" + "    <Price>10</Price>\n" + "    <Features>\n"
                + "        <Sizes>\n" + "            <Size>Small</Size>\n" + "            <Size>Medium</Size>\n"
                + "            <Size>Large</Size>\n" + "        </Sizes>\n" + "        <Colors>\n"
                + "            <Color>Blue</Color>\n" + "            <Color>Red</Color>\n" + "        </Colors>\n"
                + "    </Features>\n" + "    <Status>Pending</Status>\n" + "    <Supplier>[1]</Supplier>\n" + "</Product>"));
        allRecords.add(factory.read(repository, product, "<Product>\n" + "    <Id>2</Id>\n" + "    <Name>Renault car</Name>\n"
                + "    <ShortDescription>A car</ShortDescription>\n"
                + "    <LongDescription>Long description 2</LongDescription>\n" + "    <Price>10</Price>\n" + "    <Features>\n"
                + "        <Sizes>\n" + "            <Size>Large</Size>\n" + "        <Size>Large</Size></Sizes>\n"
                + "        <Colors>\n" + "            <Color>Blue 2</Color>\n" + "            <Color>Blue 1</Color>\n"
                + "            <Color>Klein blue2</Color>\n" + "        </Colors>\n" + "    </Features>\n"
                + "    <Family>[1]</Family>\n" + "    <Status>Pending</Status>\n" + "    <Supplier>[2]</Supplier>\n"
                + "    <Supplier>[1]</Supplier>\n" + "</Product>"));
        allRecords.add(factory.read(repository, supplier, "<Supplier>\n" + "    <Id>1</Id>\n"
                + "    <SupplierName>Renault</SupplierName>\n" + "    <Contact>" + "        <Name>Jean Voiture</Name>\n"
                + "        <Phone>33123456789</Phone>\n" + "        <Email>test@test.org</Email>\n" + "    </Contact>\n"
                + "</Supplier>"));
        allRecords.add(factory.read(repository, supplier, "<Supplier>\n" + "    <Id>2</Id>\n"
                + "    <SupplierName>Starbucks Talend</SupplierName>\n" + "    <Contact>" + "        <Name>Jean Cafe</Name>\n"
                + "        <Phone>33234567890</Phone>\n" + "        <Email>test@testfactory.org</Email>\n" + "    </Contact>\n"
                + "</Supplier>"));
        allRecords.add(factory.read(repository, supplier, "<Supplier>\n" + "    <Id>3</Id>\n"
                + "    <SupplierName>Talend</SupplierName>\n" + "    <Contact>" + "        <Name>Jean Paul</Name>\n"
                + "        <Phone>33234567890</Phone>\n" + "        <Email>test@talend.com</Email>\n" + "    </Contact>\n"
                + "</Supplier>"));
        allRecords.add(factory.read(repository, supplier, "<Supplier>\n" + "    <Id>4</Id>\n"
                + "    <SupplierName>IdSoftware</SupplierName>\n" + "    <Contact>" + "        <Name>John Carmack</Name>\n"
                + "        <Phone>123456789</Phone>\n" + "        <Email></Email>\n" + "    </Contact>\n" + "</Supplier>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>1</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France</name></Country>"));
        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>2</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France1</name><notes><note>Country note</note><comment>repeatable comment 1</comment><comment>Repeatable comment 2</comment></notes></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>3</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France2</name></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>4</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France3</name></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>5</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France4</name></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>6</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France5</name></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>7</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France6</name></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>8</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France7</name></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>9</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France8</name></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>10</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France9</name></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>11</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France10</name></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                countryLong,
                                "<CountryLong><id>1</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France1</name></CountryLong>"));

        allRecords
                .add(factory
                        .read(repository,
                                countryLong,
                                "<CountryLong><id>2</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France2</name></CountryLong>"));

        allRecords
                .add(factory
                        .read(repository,
                                countryLong,
                                "<CountryLong><id>3</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France3</name></CountryLong>"));

        allRecords
                .add(factory
                        .read(repository,
                                countryShort,
                                "<CountryShort><id>1</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France1</name></CountryShort>"));

        allRecords
                .add(factory
                        .read(repository,
                                countryShort,
                                "<CountryShort><id>2</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France2</name></CountryShort>"));

        allRecords
                .add(factory
                        .read(repository,
                                countryShort,
                                "<CountryShort><id>3</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France3</name></CountryShort>"));
        allRecords
                .add(factory
                        .read(repository,
                                countryShort,
                                "<CountryShort><id>4</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France3</name></CountryShort>"));

        allRecords
                .add(factory
                        .read(repository,
                                person,
                                "<Person><id>1</id><score>130000.00</score><lastname>Dupond</lastname><resume>[EN:my splendid resume, splendid isn't it][FR:mon magnifique resume, n'est ce pas ?]</resume><middlename>John</middlename><firstname>Julien</firstname><addresses></addresses><age>10</age><Status>Employee</Status><Available>true</Available></Person>"));
        allRecords
                .add(factory
                        .read(repository,
                                person,
                                "<Person><id>2</id><score>170000.00</score><lastname>Dupont</lastname><middlename>John</middlename><firstname>Robert-Julien</firstname><addresses></addresses><age>20</age><Status>Customer</Status><Available>false</Available></Person>"));
        allRecords
                .add(factory
                        .read(repository,
                                person,
                                "<Person><id>3</id><score>200000.00</score><lastname>Leblanc</lastname><middlename>John</middlename><firstname>Juste</firstname><addresses></addresses><age>30</age><Status>Friend</Status></Person>"));
        allRecords
                .add(factory
                        .read(repository,
                                person,
                                "<Person><id>4</id><score>200000.00</score><lastname>Leblanc</lastname><middlename>John</middlename><firstname>Julien</firstname><age>30</age><Status>Friend</Status></Person>"));
        allRecords
                .add(factory
                        .read(repository,
                                person,
                                "<Person><id>1234</id><firstname>quan</firstname><middlename>kevin</middlename><lastname>cui</lastname><resume>[EN:Hello [World:]][FR:bonjour :le][ZH:ni Hao]</resume><age>22</age><score>100</score><Available></Available><Status>Customer</Status></Person>"));

        allRecords
                .add(factory
                        .read(repository,
                                fullTextSearchEntityA,
                                "<FullTextSearchEntityA><Id>id1</Id><Name>name1</Name><Address><AddressName>address1</AddressName><City><CityName>city1</CityName></City></Address></FullTextSearchEntityA>"));
        allRecords.add(factory.read(repository, a2,
                "<a2><subelement>1</subelement><subelement1>10</subelement1><b3>String b3</b3><b4>String b4</b4></a2>"));
        allRecords.add(factory.read(repository, a1,
                "<a1><subelement>1</subelement><subelement1>11</subelement1><b1>String b1</b1><b2>[1][10]</b2></a1>"));
        allRecords.add(factory.read(repository, a2,
                "<a2><subelement>1</subelement><subelement1>2</subelement1><b3>String b3</b3><b4>String b4</b4></a2>"));
        allRecords.add(factory.read(repository, a3, "<a3><id>3</id><name>hamdi</name><a2>[1][2]</a2></a3>"));
        allRecords.add(factory.read(repository, store, "<Store><Id>Upper Case Id</Id><Name>name1</Name></Store>"));
        allRecords.add(factory.read(repository, store, "<Store><Id>lower case id</Id><Name>name2</Name></Store>"));
        allRecords.add(factory.read(repository, store, "<Store><Id>a</Id><Name>name3</Name></Store>"));
        allRecords.add(factory.read(repository, store, "<Store><Id>a&amp;b</Id><Name>name3</Name></Store>"));
        allRecords.add(factory.read(repository, store, "<Store><Id>A&amp;B</Id><Name>name3</Name></Store>"));

        allRecords.add(factory.read(repository, employee, "<Employee><name>employee 1</name><age>11</age><jobTitle>jobTitle 11</jobTitle></Employee>"));
        allRecords.add(factory.read(repository, employee, "<Employee><name>employee 2</name><age>22</age><jobTitle>jobTitle 22</jobTitle></Employee>"));
        allRecords.add(factory.read(repository, employee, "<Employee><name>employee 3</name><age>33</age><jobTitle>jobTitle 33</jobTitle></Employee>"));
        allRecords.add(factory.read(repository, persons, "<Persons><name>person 1</name><age>11</age></Persons>"));
        allRecords.add(factory.read(repository, persons, "<Persons><name>person 2</name><age>22</age></Persons>"));
        allRecords.add(factory.read(repository, persons, "<Persons><name>person 3</name><age>33</age></Persons>"));
        allRecords.add(factory.read(repository, manager, "<Manager><name>manager 1</name><age>11</age><jobTitle>jobTitle 11</jobTitle><dept>dept 1</dept></Manager>"));
        allRecords.add(factory.read(repository, manager, "<Manager><name>manager 2</name><age>22</age><jobTitle>jobTitle 22</jobTitle><dept>dept 2</dept></Manager>"));
        allRecords.add(factory.read(repository, manager, "<Manager><name>manager 3</name><age>33</age><jobTitle>jobTitle 33</jobTitle><dept>dept 3</dept></Manager>"));
        
        try {
            storage.begin();
            storage.update(allRecords);
            storage.commit();
        } catch (Exception e) {
            storage.rollback();
            throw new RuntimeException(e);
        } finally {
            storage.end();
        }
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public static void clean() throws Exception {
        storage.begin();
        {
            UserQueryBuilder qb = from(productFamily);
            storage.delete(qb.getSelect());

            qb = from(supplier);
            storage.delete(qb.getSelect());

            qb = from(product);
            storage.delete(qb.getSelect());

            qb = from(country);
            storage.delete(qb.getSelect());

            qb = from(countryLong);
            storage.delete(qb.getSelect());

            qb = from(countryShort);
            storage.delete(qb.getSelect());

            qb = from(person);
            storage.delete(qb.getSelect());

            qb = from(fullTextSearchEntityA);
            storage.delete(qb.getSelect());

            qb = from(a1);
            storage.delete(qb.getSelect());

            qb = from(a2);
            storage.delete(qb.getSelect());

            qb = from(a3);
            storage.delete(qb.getSelect());

            qb = from(store);
            storage.delete(qb.getSelect());
        }
        storage.commit();
        storage.end();
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testNumericQueryMix() throws Exception {
        UserQueryBuilder qb = from(product).where(fullText("2"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                LOG.info("result = " + result);
            }
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testMatchesInSameInstance() throws Exception {
        UserQueryBuilder qb = from(country).where(fullText("2010"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                LOG.info("result = " + result);
            }
            assertEquals(11, results.getCount());
        } finally {
            results.close();
        }

        qb = from(product).where(fullText("Large"));
        results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                LOG.info("result = " + result);
            }
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSimpleSearch() throws Exception {
        UserQueryBuilder qb = from(supplier).where(fullText("Renault"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                LOG.info("result = " + result);
            }
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchOrderBy() throws Exception {
        UserQueryBuilder qb = from(supplier).select(supplier.getField("Id")).where(fullText("Talend"))
                .orderBy(supplier.getField("Id"), OrderBy.Direction.ASC);

        StorageResults records = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, records.getCount());
            int currentId = -1;
            for (DataRecord record : records) {
                Integer id = Integer.parseInt((String) record.get("Id"));
                assertTrue(id > currentId);
                currentId = id;
            }
        } finally {
            records.close();
        }

        qb = from(supplier).select(supplier.getField("Id")).where(fullText("Talend"))
                .orderBy(supplier.getField("Id"), OrderBy.Direction.DESC);

        records = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, records.getCount());
            int currentId = Integer.MAX_VALUE;
            for (DataRecord record : records) {
                Integer id = Integer.parseInt((String) record.get("Id"));
                assertTrue(id < currentId);
                currentId = id;
            }
        } finally {
            records.close();
        }
    }

    public void testSimpleSearchOrderByWithContainsCondition() throws Exception {
        // Order by "Id" field
        UserQueryBuilder qb = from(productFamily).where(contains(productFamily.getField("Name"), "Product")).orderBy(
                productFamily.getField("Id"), OrderBy.Direction.DESC);

        StorageResults records = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, records.getCount());
            int currentId = Integer.MAX_VALUE;
            for (DataRecord record : records) {
                Integer id = Integer.parseInt((String) record.get("Id"));
                assertTrue(id < currentId);
                currentId = id;
            }
        } finally {
            records.close();
        }
        // Order by "Name" field
        qb = from(productFamily).where(contains(productFamily.getField("Name"), "Product")).orderBy(
                productFamily.getField("Name"), OrderBy.Direction.DESC);

        records = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, records.getCount());
            int currentId = Integer.MAX_VALUE;
            for (DataRecord record : records) {
                Integer id = Integer.parseInt((String) record.get("Id"));
                assertTrue(id < currentId);
                currentId = id;
            }
        } finally {
            records.close();
        }
    }

    public void testMultipleTypesSearch() throws Exception {
        UserQueryBuilder qb = from(supplier).and(product).where(fullText("Renault"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testDateSearch() throws Exception {
        UserQueryBuilder qb = from(country).where(fullText("2010")); // Default StandardAnalyzer will split text
                                                                     // "2010-10-12" into "2010", "10", "12"

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(11, results.getCount());
        } finally {
            results.close();
        }
    }

    /**
     * TMDM-8970 : exception occured when launching this test before fix test for id of int type
     * 
     * @throws Exception
     */
    public void testFullSearchCountry() throws Exception {
        UserQueryBuilder qb = from(country).where(fullText("France"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            // debug code for unstable case
            for(DataRecord dataRecord: results){
                assertTrue(String.valueOf(dataRecord.get("name")).startsWith("France"));
            }
            assertEquals(11, results.getCount());
        } finally {
            results.close();
        }
    }

    /**
     * TMDM-8970 : exception occured when launching this test before fix test for id of long type
     * 
     * @throws Exception
     */
    public void testFullSearchCountryLong() throws Exception {
        UserQueryBuilder qb = from(countryLong).where(fullText("F"));
        qb.limit(2);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    /**
     * TMDM-8970 : exception occured when launching this test before fix test for id of short type
     * 
     * @throws Exception
     */
    public void testFullSearchCountryShort() throws Exception {
        UserQueryBuilder qb = from(countryShort).where(fullText("F"));
        qb.limit(2);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testCollectionSearch() throws Exception {
        UserQueryBuilder qb = from(product).where(fullText("Blue"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchWithCondition() throws Exception {
        UserQueryBuilder qb = from(supplier).where(fullText("Renault")).where(
                eq(supplier.getField("Contact/Name"), "Jean Voiture"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(supplier).where(fullText("Renault")).where(eq(supplier.getField("Id"), "1"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(supplier).where(fullText("Renault")).where(eq(supplier.getField("Id"), "2"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchWithWildcard() throws Exception {
        UserQueryBuilder qb = from(supplier).where(fullText("*"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(4, actualCount);
        } finally {
            results.close();
        }
        //
        qb = from(supplier).where(fullText("**"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(4, actualCount);
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchWithWildcardOnTypes() throws Exception {
        UserQueryBuilder qb = from(supplier).and(product).where(fullText("*"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(6, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(6, actualCount);
        } finally {
            results.close();
        }
        //
        qb = from(supplier).and(product).where(fullText("**"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(6, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(6, actualCount);
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchWithSpace() throws Exception {
        UserQueryBuilder qb = from(supplier).where(fullText(" "));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }

        qb = from(supplier).where(fullText("     "));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }

        qb = from(supplier).and(product).where(fullText("     "));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(6, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchWithContainsCondition() throws Exception {
        UserQueryBuilder qb = from(supplier).where(fullText("Renault"))
                .where(contains(supplier.getField("Contact/Name"), "Jean"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(supplier).where(fullText("Talend")).where(contains(supplier.getField("Contact/Name"), "Jean"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchWithContainsConditionAndNot() throws Exception {
        UserQueryBuilder qb = from(supplier).where(fullText("Renault")).where(
                and(contains(supplier.getField("Contact/Name"), "Jean"), not(eq(supplier.getField("Id"), "0"))));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotSame(0, result.get("Id"));
            }
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchWithGreaterThanCondition() throws Exception {
        UserQueryBuilder qb = from(supplier).where(fullText("Renault")).where(gt(supplier.getField("Id"), "1"));
        try {
            storage.fetch(qb.getSelect());
            fail("Expected: not supported.");
        } catch (Exception e) {
            // Expected.
        }

        qb = from(supplier).where(fullText("Renault")).where(gte(supplier.getField("Id"), "1"));
        try {
            storage.fetch(qb.getSelect());
            fail("Expected: not supported.");
        } catch (Exception e) {
            // Expected.
        }

        qb = from(supplier).where(fullText("Jean")).where(gte(supplier.getField("Id"), "2"));
        try {
            storage.fetch(qb.getSelect());
            fail("Expected: not supported.");
        } catch (Exception e) {
            // Expected.
        }
    }

    public void testSimpleSearchWithContainsAndIsEmptyNullCondition() throws Exception {
        UserQueryBuilder qb = from(supplier).where(
                and(contains(supplier.getField("Id"), "4"),
                        or(isEmpty(supplier.getField("Contact/Email")), isNull(supplier.getField("Contact/Email")))));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(supplier).where(
                and(contains(supplier.getField("Contact/Name"), "John"),
                        or(isNull(supplier.getField("Contact/Email")), isEmpty(supplier.getField("Contact/Email")))));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchWithLessThanCondition() throws Exception {
        UserQueryBuilder qb = from(supplier).where(fullText("Renault")).where(lt(supplier.getField("Id"), "1"));
        try {
            storage.fetch(qb.getSelect());
            fail("Expected: not supported.");
        } catch (Exception e) {
            // Expected.
        }

        qb = from(supplier).where(fullText("Renault")).where(lte(supplier.getField("Id"), "1"));
        try {
            storage.fetch(qb.getSelect());
            fail("Expected: not supported.");
        } catch (Exception e) {
            // Expected.
        }
    }

    public void testSimpleSearchWithJoin() throws Exception {
        UserQueryBuilder qb = from(product).and(productFamily).selectId(product).select(productFamily.getField("Name"))
                .where(fullText("Renault")).join(product.getField("Family"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("Id"));
                assertEquals("", result.get("Name"));
            }
        } finally {
            results.close();
        }

        qb = from(product).and(productFamily).selectId(product).select(productFamily.getField("Name")).where(fullText("Renault"))
                .join(product.getField("Family")).limit(20);

        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("Id"));
                assertEquals("", result.get("Name"));
            }
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchWithProjection() throws Exception {
        UserQueryBuilder qb = from(supplier).select(supplier.getField("Contact/Name")).where(fullText("Renault"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(1, result.getSetFields().size());
                assertNotNull(result.get("Name"));
            }
        } finally {
            results.close();
        }
    }

    public void testSimpleSearchWithProjectionAlias() throws Exception {
        UserQueryBuilder qb = from(supplier).select(alias(supplier.getField("Contact/Name"), "element")).where(
                fullText("Renault"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(1, result.getSetFields().size());
                assertNotNull(result.get("element"));
            }
        } finally {
            results.close();
        }

        qb = from(supplier).select(alias(supplier.getField("Contact/Name"), "element")).where(fullText("Renault")).start(0)
                .limit(20);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals(1, result.getSetFields().size());
                assertNotNull(result.get("element"));
            }
        } finally {
            results.close();
        }
    }

    public void testFKSearchWithProjection() throws Exception {
        UserQueryBuilder qb = from(product).select(product.getField("Family")).where(fullText("car"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            ViewSearchResultsWriter writer = new ViewSearchResultsWriter();
            StringWriter resultWriter = new StringWriter();
            for (DataRecord result : results) {
                writer.write(result, resultWriter);
            }
            assertEquals("<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + "\t<Family>[1]</Family>\n" + "</result>",
                    resultWriter.toString());
        } finally {
            results.close();
        }
    }

    public void testMultipleTypesSearchWithCondition() throws Exception {
        UserQueryBuilder qb = from(supplier).where(fullText("Renault")).where(
                eq(supplier.getField("Contact/Name"), "Jean Voiture"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testFullTextSuggestion() throws Exception {
        try {
            storage.getFullTextSuggestion("Ren", Storage.FullTextSuggestion.START, 3);
            fail("Expected due to Lucene version being used.");
        } catch (Exception e) {
            // Expected.
        }
    }

    public void testFullTextAlternative() throws Exception {
        try {
            storage.getFullTextSuggestion("strabuks", Storage.FullTextSuggestion.ALTERNATE, 3);
            fail("Expected due to Lucene version being used");
        } catch (Exception e) {
            // Expected
        }

    }

    public void testFullTextResultsFormat() throws Exception {
        UserQueryBuilder qb = from(product).where(fullText("Renault"));

        StorageResults results = null;
        try {
            results = storage.fetch(qb.getSelect());
            assertEquals(1, results.getCount());

            DataRecordWriter writer = new FullTextResultsWriter("Renault");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (DataRecord result : results) {
                writer.write(result, output);
            }
            assertTrue(output.toString().contains("<b>Renault car</b>"));
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    public void testFullTextResultsInNestedFormat() throws Exception {
        UserQueryBuilder qb = from(product).where(fullText("Klein"));

        StorageResults results = null;
        try {
            results = storage.fetch(qb.getSelect());
            assertEquals(1, results.getCount());

            DataRecordWriter writer = new FullTextResultsWriter("Klein");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (DataRecord result : results) {
                writer.write(result, output);
            }
            assertTrue(output.toString().contains("<b>Klein blue2</b>"));
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    public void testNoFullText() throws Exception {
        Storage storage = new HibernateStorage("noFullText");
        try {
            storage.init(getDatasource("RDBMS-1-NO-FT"));
            storage.prepare(repository, Collections.<Expression> emptySet(), false, false);
            UserQueryBuilder qb = from(product).where(fullText("Test"));

            try {
                storage.fetch(qb.getSelect());
                fail("Full text is not enabled");
            } catch (Exception e) {
                assertEquals("Storage 'noFullText(MASTER)' is not configured to support full text queries.", e.getCause().getMessage());
            }
        } finally {
            storage.close();
        }
    }

    public void testSimpleWithoutFkValueSearch() throws Exception {
        UserQueryBuilder qb = from(product).select(product.getField("Family")).where(fullText("talend")).limit(20);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("", result.get("Family"));
            }
        } finally {
            results.close();
        }
    }

    public void testFullTestWithCompositeKeySearch() throws Exception {

        try {
            UserQueryBuilder qb = from(a1).selectId(a1).select(a1.getField("b1")).select(a1.getField("b2"))
                    .where(fullText("String")).limit(20);
            storage.fetch(qb.getSelect());
            fail();
        } catch (RuntimeException runtimeException) {
            if (FullTextQueryCompositeKeyException.class.isInstance(runtimeException.getCause())) {
                assertEquals("a1", runtimeException.getCause().getMessage());
            } else {
                throw runtimeException;
            }
        }
    }

    public void testFieldFullText() throws Exception {
        UserQueryBuilder qb = from(product).where(fullText(product.getField("ShortDescription"), "description")).limit(20);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(product).where(fullText(product.getField("ShortDescription"), "long")).limit(20);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testFullTextAndRangeTimeQuery() throws Exception {
        // Original case not working due to a issue in Lucene, where some collectors cannot accept out-of-order scoring.
        // should be fixed in Lucene 5.0, https://issues.apache.org/jira/browse/LUCENE-6179
        UserQueryBuilder qb = from(product).where(
                or(fullText(product.getField("ShortDescription"), "description"),
                        and(gte(timestamp(), "0"), lte(timestamp(), String.valueOf(System.currentTimeMillis()))))).limit(20);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testTaskIdProjection() throws Exception {
        UserQueryBuilder qb = from(product).select(alias(taskId(), "taskId")).select(alias(error(), "error"))
                .where(fullText(product.getField("ShortDescription"), "description"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNull(result.get("taskId"));
                assertNull(result.get("error"));
            }
        } finally {
            results.close();
        }

        qb = from(product).select(alias(taskId(), "taskId")).select(alias(error(), "error"))
                .where(fullText(product.getField("ShortDescription"), "description")).limit(20);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNull(result.get("taskId"));
                assertNull(result.get("error"));
            }
        } finally {
            results.close();
        }
    }

    public void testSearchOnContainedType() throws Exception {
        UserQueryBuilder qb = from(product).where(fullText(product.getField("Features"), "klein"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                Object object = result.get("Features/Colors/Color");
                assertTrue(object instanceof List);
                assertEquals(3, ((List) object).size());
                assertEquals("Klein blue2", ((List) object).get(2));
            }
        } finally {
            results.close();
        }

        qb = UserQueryBuilder.from(product);
        String fieldName = "Product/Features";
        IWhereItem item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.FULLTEXTSEARCH,
                "klein", WhereCondition.NO_OPERATOR)));
        Condition condition = UserQueryHelper.buildCondition(qb, item, repository);
        Expression normalizedCondition = condition.normalize();
        assertTrue(normalizedCondition instanceof BinaryLogicOperator);
        assertTrue(((BinaryLogicOperator) normalizedCondition).getLeft() instanceof FieldFullText);
        qb = qb.where(condition);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                Object object = result.get("Features/Colors/Color");
                assertTrue(object instanceof List);
                assertEquals(3, ((List) object).size());
                assertEquals("Klein blue2", ((List) object).get(2));
            }
        } finally {
            results.close();
        }
    }

    public void testTimeStampProjectionNoAlias() throws Exception {
        // TMDM-7737: Test metadata field projection *with* paging in query.
        UserQueryBuilder qb = from(product).select(timestamp()).where(fullText(product.getField("Features"), "klein")).start(0)
                .limit(20);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testIdFieldContainUpperCaseKeyWordSearch() throws Exception {
        UserQueryBuilder qb = from(store).selectId(store).where(contains(store.getField("Id"), "case"));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }

        qb = from(store).selectId(store).where(fullText("case"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    // TMDM-8798 FK constraint warning when creating a fk on the fly with special character
    public void testIdFieldContainSpecialCharacter() throws Exception {
        UserQueryBuilder qb = from(store).selectId(store).where(contains(store.getField("Id"), "a&b"));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(store).selectId(store).where(contains(store.getField("Id"), "a"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }

        qb = from(store).selectId(store).where(contains(store.getField("Id"), "A&B"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(store).selectId(store).where(fullText("a&b"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(store).selectId(store).where(fullText("a"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }

        qb = from(store).selectId(store).where(fullText("A&B"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testGenerateIdFetchSize() throws Exception {
        // Test "stream resultset"
        RDBMSDataSource dataSource = new RDBMSDataSource("TestDataSource", "MySQL", "", "", "", 0, 0, "", "", false, "update",
                false, new HashMap(), "", "", null, "", "", "", false);
        Configuration configuration = new Configuration();
        configuration.setProperty(Environment.STATEMENT_FETCH_SIZE, "1000");
        HibernateStorage storage = new HibernateStorage("HibernateStorage");
        storage.init(getDatasource("RDBMS-1-NO-FT"));
        storage.prepare(repository, Collections.<Expression> emptySet(), false, false);

        Class storageClass = storage.getClass();
        Field dataSourceField = storageClass.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(storage, dataSource);

        Field configurationField = storageClass.getDeclaredField("configuration");
        configurationField.setAccessible(true);
        configurationField.set(storage, configuration);

        Method generateIdFetchSizeMethod = storageClass.getDeclaredMethod("generateIdFetchSize", null);
        generateIdFetchSizeMethod.setAccessible(true);
        assertEquals(Integer.MIN_VALUE, generateIdFetchSizeMethod.invoke(storage, null));

        // Test config batch size
        dataSource = new RDBMSDataSource("TestDataSource", "H2", "", "", "", 0, 0, "", "", false, "update", false, new HashMap(),
                "", "", null, "", "", "", false);
        storage = new HibernateStorage("HibernateStorage");
        storage.init(getDatasource("RDBMS-1-NO-FT"));
        storage.prepare(repository, Collections.<Expression> emptySet(), false, false);
        storageClass = storage.getClass();

        dataSourceField = storageClass.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(storage, dataSource);

        configurationField = storageClass.getDeclaredField("configuration");
        configurationField.setAccessible(true);
        configurationField.set(storage, configuration);
        assertEquals(1000, generateIdFetchSizeMethod.invoke(storage, null));

        // Test default batch size
        configuration = new Configuration();
        configurationField = storageClass.getDeclaredField("configuration");
        configurationField.setAccessible(true);
        configurationField.set(storage, configuration);
        assertEquals(500, generateIdFetchSizeMethod.invoke(storage, null));
    }

    public void testFieldQueryWhenHavingCompositeFK() throws Exception {

        UserQueryBuilder qb = from(a3).select(a3.getField("id")).select(a3.getField("a2"))
                .where(fullText(a3.getField("name"), "hamdi")).limit(20);
        StorageResults results = storage.fetch(qb.getSelect());
        Exception exception = null;
        try {
            assertEquals(1, results.getCount());
            DataRecord result = results.iterator().next();
            Object b2 = result.get(a3.getField("a2"));
            assertTrue(b2 instanceof Object[]);
            assertEquals("1", ((Object[]) b2)[0]);
            assertEquals("2", ((Object[]) b2)[1]);
        } catch (Exception e) {
            exception = e;
        } finally {
            results.close();
        }
        assertNull(exception);
    }

    public void testSearchOnMultiLingualType() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("id")).where(contains(person.getField("resume"), " world "));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertEquals("1234", result.get("id"));
            }
        } finally {
            results.close();
        }

        qb = from(person).selectId(person).where(contains(person.getField("resume"), " bon "));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertEquals("1234", result.get("id"));
            }
        } finally {
            results.close();
        }

        qb = from(person).selectId(person).where(contains(person.getField("resume"), " le "));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertEquals("1234", result.get("id"));
            }
        } finally {
            results.close();
        }

        qb = from(person).selectId(person).where(contains(person.getField("resume"), "hao"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
                assertEquals("1234", result.get("id"));
            }
        } finally {
            results.close();
        }
    }

    public void testFullTextOnRepeatable() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(country).where(fullText("repeatable"));
        StorageResults results = storage.fetch(qb.getSelect());
        for (DataRecord result : results) {
            assertEquals("Country", result.getType().getName());
            assertEquals(2, result.get("id"));
        }
    }

    public void testFullText() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(country).where(fullText("note"));
        StorageResults results = storage.fetch(qb.getSelect());
        for (DataRecord result : results) {
            assertEquals("Country", result.getType().getName());
            assertEquals(2, result.get("id"));
        }

        qb = UserQueryBuilder.from(fullTextSearchEntityA).where(fullText("city"));
        results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        for (DataRecord result : results) {
            assertEquals("id1", result.get("Id"));
        }
    }

    public void testFullTextWithMultiKeywords() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(supplier).where(contains(supplier.getField("SupplierName"), "Star Id"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(2, results.getCount());
        for (DataRecord result : results) {
            if ("2".equals(result.get("Id"))) {
                assertEquals("Starbucks Talend", result.get("SupplierName"));
            }
            if ("4".equals(result.get("Id"))) {
                assertEquals("IdSoftware", result.get("SupplierName"));
            }
        }
    }

    public void testTypeSplitWithPaging() throws Exception {
        // Build expected results
        UserQueryBuilder qb = UserQueryBuilder.from(person).and(product).where(fullText("Julien"));
        List<String> expected = new LinkedList<String>();
        StorageResults records = storage.fetch(qb.getSelect());
        int count = records.getCount();
        int size = records.getSize();
        for (DataRecord record : records) {
            expected.add(String.valueOf(record.get("id")));
        }
        // Ensures split behavior is same as no split
        StorageResults split = Split.fetchAndMerge(storage, qb.getSelect());
        for (DataRecord record : split) {
            assertTrue(expected.remove(String.valueOf(record.get("id"))));
        }
        assertEquals(count, split.getCount());
        assertEquals(size, split.getSize());
    }

    public void testTypeSplit() throws Exception {
        // Build expected results
        UserQueryBuilder qb = UserQueryBuilder.from(person).and(product).where(fullText("Julien"));
        List<DataRecord> expected = new LinkedList<DataRecord>();
        StorageResults records = storage.fetch(qb.getSelect());
        int count = records.getCount();
        int size = records.getSize();
        for (DataRecord record : records) {
            expected.add(record);
        }
        // Ensures split behavior is same as no split
        StorageResults split = Split.fetchAndMerge(storage, qb.getSelect());
        int i = 0;
        for (DataRecord record : split) {
            assertEquals(record, expected.get(i++));
        }
        assertEquals(count, split.getCount());
        assertEquals(size, split.getSize());
    }

    public void testContainsWithUnderscoreInSearchWords3() throws Exception {
        UserQueryBuilder qb = from(productFamily).where(contains(productFamily.getField("Name"), "te*t_nam*"));

        Select select = qb.getSelect();
        assertNotNull(select);
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertTrue(condition instanceof Compare);
        Compare compareCondition = (Compare) condition;
        Expression right = compareCondition.getRight();
        assertTrue(right instanceof StringConstant);
        assertEquals("te*t_nam*", ((StringConstant) right).getValue());

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testManyFieldUsingAndContainsCondition() throws Exception {
        UserQueryBuilder qb = from(product).where(contains(product.getField("Features/Sizes/Size"), "ValueDoesNotExist"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());

        qb = from(product).where(
                and(eq(product.getField("Id"), "1"), contains(product.getField("Features/Sizes/Size"), "ValueDoesNotExist")));
        results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());

        qb = from(product).where(contains(product.getField("Features/Sizes/Size"), "large"));
        results = storage.fetch(qb.getSelect());
        assertEquals(2, results.getCount());
    }

    public void testContainsWithReservedCharacters() throws Exception {
        DataSourceDefinition definition = getDatasource(DATASOURCE_FULLTEXT);
        DataSource datasource = definition.getMaster();
        assertTrue(datasource instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) datasource;
        TestRDBMSDataSource testDataSource = new TestRDBMSDataSource(rdbmsDataSource);
        testDataSource.setCaseSensitiveSearch(false);
        testDataSource.setSupportFullText(true);
        testDataSource.setOptimization(RDBMSDataSource.ContainsOptimization.FULL_TEXT);
        ConfigurableContainsOptimizer optimizer = new ConfigurableContainsOptimizer(testDataSource);
        // Only '-' should disable contains optimization
        UserQueryBuilder qb = UserQueryBuilder.from(person).where(contains(person.getField("id"), "-"));
        Select copy = qb.getSelect().copy();
        optimizer.optimize(copy);
        assertFalse(copy.getCondition() instanceof FieldFullText);
        StorageResults records = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, records.getCount());
        } finally {
            records.close();
        }
        // Only '/' should disable contains optimization
        qb = UserQueryBuilder.from(person).where(contains(person.getField("id"), "/"));
        copy = qb.getSelect().copy();
        optimizer.optimize(copy);
        assertFalse(copy.getCondition() instanceof FieldFullText);
        records = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, records.getCount());
        } finally {
            records.close();
        }
        // Contains optimization should be disabled for next query
        qb = UserQueryBuilder.from(person).where(contains(person.getField("id"), "1-1"));
        copy = qb.getSelect().copy();
        optimizer.optimize(copy);
        assertFalse(copy.getCondition() instanceof FieldFullText);
        records = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, records.getCount());
        } finally {
            records.close();
        }
        // Contains optimization should be enabled
        qb = UserQueryBuilder.from(person).where(contains(person.getField("id"), "_"));
        copy = qb.getSelect().copy();
        optimizer.optimize(copy);
        assertTrue(copy.getCondition() instanceof FieldFullText);
        records = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, records.getCount());
        } finally {
            records.close();
        }
        // Contains optimization should also be enabled
        qb = UserQueryBuilder.from(person).where(contains(person.getField("id"), "1_1"));
        copy = qb.getSelect().copy();
        optimizer.optimize(copy);
        assertTrue(copy.getCondition() instanceof FieldFullText);
        records = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, records.getCount());
        } finally {
            records.close();
        }
    }

    public void testContainsOptimization() throws Exception {
        DataSourceDefinition definition = getDatasource(DATASOURCE_FULLTEXT);
        DataSource datasource = definition.getMaster();
        assertTrue(datasource instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) datasource;
        TestRDBMSDataSource testDataSource = new TestRDBMSDataSource(rdbmsDataSource);
        testDataSource.setCaseSensitiveSearch(false);
        testDataSource.setSupportFullText(true);
        ConfigurableContainsOptimizer optimizer = new ConfigurableContainsOptimizer(testDataSource);
        // Default optimization
        UserQueryBuilder qb = UserQueryBuilder.from(person).where(
                contains(person.getField("knownAddresses/knownAddress/Notes/Note"), "test note"));
        Select select = qb.getSelect();
        assertTrue(select.getCondition() instanceof Compare);
        assertTrue(((Compare) select.getCondition()).getPredicate() == Predicate.CONTAINS);
        // LIKE optimization
        testDataSource.setOptimization(RDBMSDataSource.ContainsOptimization.LIKE);
        qb = UserQueryBuilder.from(person)
                .where(contains(person.getField("knownAddresses/knownAddress/Notes/Note"), "test note"));
        select = qb.getSelect();
        optimizer.optimize(select);
        assertTrue(select.getCondition() instanceof Compare);
        assertTrue(((Compare) select.getCondition()).getPredicate() == Predicate.CONTAINS);
        // DISABLED optimization
        testDataSource.setOptimization(RDBMSDataSource.ContainsOptimization.DISABLED);
        qb = UserQueryBuilder.from(person)
                .where(contains(person.getField("knownAddresses/knownAddress/Notes/Note"), "test note"));
        select = qb.getSelect();
        try {
            optimizer.optimize(select);
            fail("Contains use is disabled.");
        } catch (Exception e) {
            // Expected
        }
        // FULL TEXT optimization
        testDataSource.setOptimization(RDBMSDataSource.ContainsOptimization.FULL_TEXT);
        qb = UserQueryBuilder.from(person)
                .where(contains(person.getField("knownAddresses/knownAddress/Notes/Note"), "test note"));
        select = qb.getSelect();
        optimizer.optimize(select);
        assertTrue(select.getCondition() instanceof FieldFullText);
        assertEquals("test note", ((FieldFullText) select.getCondition()).getValue());
    }

    public void testContainsOptimizationOnReusableTypes() throws Exception {
        DataSourceDefinition definition = getDatasource(DATASOURCE_FULLTEXT);
        DataSource datasource = definition.getMaster();
        assertTrue(datasource instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) datasource;
        TestRDBMSDataSource testDataSource = new TestRDBMSDataSource(rdbmsDataSource);
        testDataSource.setCaseSensitiveSearch(false);
        testDataSource.setSupportFullText(true);
        ConfigurableContainsOptimizer optimizer = new ConfigurableContainsOptimizer(testDataSource);
        // Default optimization
        UserQueryBuilder qb = UserQueryBuilder.from(customer).where(contains(customer.getField("address1/Street"), "test note"));
        Select select = qb.getSelect();
        assertTrue(select.getCondition() instanceof Compare);
        assertTrue(((Compare) select.getCondition()).getPredicate() == Predicate.CONTAINS);
        // LIKE optimization
        testDataSource.setOptimization(RDBMSDataSource.ContainsOptimization.LIKE);
        qb = UserQueryBuilder.from(customer).where(contains(customer.getField("address1/Street"), "test note"));
        select = qb.getSelect();
        optimizer.optimize(select);
        assertTrue(select.getCondition() instanceof Compare);
        assertTrue(((Compare) select.getCondition()).getPredicate() == Predicate.CONTAINS);
        // DISABLED optimization
        testDataSource.setOptimization(RDBMSDataSource.ContainsOptimization.DISABLED);
        qb = UserQueryBuilder.from(customer).where(contains(customer.getField("address1/Street"), "test note"));
        select = qb.getSelect();
        try {
            optimizer.optimize(select);
            fail("Contains use is disabled.");
        } catch (Exception e) {
            // Expected
        }
        // FULL TEXT optimization
        testDataSource.setOptimization(RDBMSDataSource.ContainsOptimization.FULL_TEXT);
        qb = UserQueryBuilder.from(customer).where(contains(customer.getField("address1/Street"), "test note"));
        select = qb.getSelect();
        optimizer.optimize(select);
        assertTrue(select.getCondition() instanceof Compare);
        assertTrue(((Compare) select.getCondition()).getPredicate() == Predicate.CONTAINS);
    }

    public void testContainsSentenceSearch() throws Exception {
        UserQueryBuilder qb = from(supplier).where(contains(supplier.getField("Contact/Name"), "'jean cafe'"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertTrue(results.getSize() == 1);
            for (DataRecord result : results) {
                assertEquals("2", result.get("Id"));
            }
        } finally {
            results.close();
        }

        qb = from(product).where(and(eq(product.getField("Id"), "2"), contains(product.getField("Name"), "'Renault car'"))).where(fullText("car"));
        results = storage.fetch(qb.getSelect());
        try {
            assertTrue(results.getSize() == 1);
            for (DataRecord result : results) {
                assertEquals("A car", result.get("ShortDescription"));
            }
        } finally {
            results.close();
        }
    }
    
    public void testFullTextSearchOnComplexType() throws Exception {
        UserQueryBuilder qb = from(persons).where(fullText("1"));
        qb.limit(5);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            int recordCount = 0;
            assertEquals(3, results.getCount());
            for (DataRecord result : results) {
                if (result != null) {
                    if ("person 1".equals(result.get("name")) || "employee 1".equals(result.get("name")) || "manager 1".equals(result.get("name"))) {
                        recordCount++;
                    }
                }
            }
            assertEquals(3, recordCount);
        } finally {
            results.close();
        }
        
        qb = from(employee).where(fullText("1"));
        results = storage.fetch(qb.getSelect());
        try {
            int recordCount = 0;
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                if (result != null) {
                    if ("employee 1".equals(result.get("name")) || "manager 1".equals(result.get("name"))) {
                        recordCount++;
                    }
                }
            }
            assertEquals(2, recordCount);
        } finally {
            results.close();
        }
        
        qb = from(manager).where(fullText("1"));
        results = storage.fetch(qb.getSelect());
        try {
            int recordCount = 0;
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                if (result != null) {
                    if ("manager 1".equals(result.get("name"))) {
                        recordCount++;
                    }
                }
            }
            assertEquals(1, recordCount);
        } finally {
            results.close();
        }
    }

    private static class TestRDBMSDataSource extends RDBMSDataSource {

        private ContainsOptimization optimization;

        private boolean supportFullText;

        private boolean isCaseSensitiveSearch;

        public TestRDBMSDataSource(RDBMSDataSource rdbmsDataSource) {
            super(rdbmsDataSource);
        }

        @Override
        public boolean supportFullText() {
            return supportFullText;
        }

        private void setSupportFullText(boolean supportFullText) {
            this.supportFullText = supportFullText;
        }

        @Override
        public boolean isCaseSensitiveSearch() {
            return isCaseSensitiveSearch;
        }

        public void setCaseSensitiveSearch(boolean caseSensitiveSearch) {
            isCaseSensitiveSearch = caseSensitiveSearch;
        }

        @Override
        public ContainsOptimization getContainsOptimization() {
            return optimization;
        }

        public void setOptimization(ContainsOptimization optimization) {
            this.optimization = optimization;
        }
    }
}
