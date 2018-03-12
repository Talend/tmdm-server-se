/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.load;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.amalto.core.save.generator.UUIDIdGenerator;
import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.amalto.core.save.generator.AutoIdGenerator;
import com.amalto.core.load.context.StateContext;
import com.amalto.core.load.exception.ParserCallbackException;
import com.amalto.core.load.io.XMLRootInputStream;
import com.amalto.core.server.api.XmlServer;

/**
 *
 */
@SuppressWarnings("nls")
public class LoadParserTest extends TestCase {

    private static Logger LOG = Logger.getLogger(LoadParserTest.class);

    private final TestAutoIdGenerator idGenerator = null; // Intentionally null to test cases when no id generator is needed.

    static {
        try {
            MDMConfiguration.createConfiguration("", true);
        } catch (IllegalStateException e) {
            // already configured;
        }
    }
    
    public void testArgs() {
        ParserTestCallback callback = new ParserTestCallback();

        try {
            LoadParser.parse(null, null, null);
        } catch (IllegalArgumentException e) {
            assertEquals("Input stream cannot be null", e.getMessage());
        }

        try {
            LoadParser.parse(new ByteArrayInputStream(StringUtils.EMPTY.getBytes()), null, callback);
        } catch (IllegalArgumentException e) {
            assertEquals("Configuration cannot be null", e.getMessage());
        }

        try {
            LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{null}, false, "clusterName", "modelName", idGenerator);
            LoadParser.parse(new ByteArrayInputStream(StringUtils.EMPTY.getBytes()), config, null);
        } catch (IllegalArgumentException e) {
            assertEquals("LoadParser callback cannot be null", e.getMessage());
        }
    }

    public void testCallbackFailure() {
        LoadParserCallback callback = new LoadParserCallback() {
            public void flushDocument(XMLReader docReader, InputSource input) {
                throw new RuntimeException();
            }
        };

        try {
            LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id"}, false, "clusterName", "modelName", idGenerator);
            LoadParser.parse(new ByteArrayInputStream("<root><Id>0</Id></root>".getBytes()), config, callback);
            Assert.fail("Should have failed due to callback exception.");
        } catch (Exception e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof ParserCallbackException);
        }
    }

    public void test1() {
        InputStream testResource = this.getClass().getResourceAsStream("test1.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"id"}, false, "clusterName", "modelName", idGenerator);
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "element1"));
        assertEquals("999", callback.getId());
    }

    public void test2() {
        InputStream testResource = this.getClass().getResourceAsStream("test2.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("Geoname", new String[]{"geonameid"}, false, "clusterName", "modelName", idGenerator);
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(31, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Geoname"));
        assertTrue(hasParsedElement(callback, "latitude"));
        assertTrue(hasParsedCharacters(callback, "Font de la Xona"));
        assertEquals("3038815", callback.getId());

        if (LOG.isDebugEnabled()) {
            testResource = this.getClass().getResourceAsStream("test2.xml");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource, config, callback2);
        }
    }

    public void test3() {
        InputStream testResource = this.getClass().getResourceAsStream("test3.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("Product", new String[]{"Id"}, false, "clusterName", "modelName", idGenerator);
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(29, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Product"));
        assertTrue(hasParsedElement(callback, "Features"));
        assertTrue(hasParsedCharacters(callback, "porttitor pharetra quis sed risus."));
        assertEquals("1", callback.getId());
    }

    public void test4() {
        InputStream testResource = this.getClass().getResourceAsStream("test4.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"element1"}, false, "clusterName", "modelName", idGenerator);
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(16, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "This is sample text"));
        assertEquals("", callback.getId());

        if (LOG.isDebugEnabled()) {
            testResource = this.getClass().getResourceAsStream("test4.xml");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource, config, callback2);
        }
    }

    public void test5() {
        InputStream testResource = this.getClass().getResourceAsStream("test5.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"uniqueId"}, false, "clusterName", "modelName", idGenerator);
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(14, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedAttribute(callback, "attribute1"));
        assertTrue(hasParsedAttribute(callback, "attribute2"));
        assertEquals("0", callback.getId());

        if (LOG.isDebugEnabled()) {
            testResource = this.getClass().getResourceAsStream("test5.xml");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource, config, callback2);
        }
    }

    public void test6() {
        TestAutoIdGenerator idGenerator = new TestAutoIdGenerator();
        InputStream testResource = this.getClass().getResourceAsStream("test6.xml");
        assertFalse(idGenerator.isStateSaved());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"uniqueId"}, true, "clusterName", "modelName", idGenerator);

        if (LOG.isDebugEnabled()) {
            InputStream testResource2 = this.getClass().getResourceAsStream("test6.xml");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource2, config, callback2);
            idGenerator.reset();
        }

        StateContext context = LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(14, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedAttribute(callback, "attribute1"));
        assertTrue(hasParsedAttribute(callback, "attribute2"));
        assertEquals("0", callback.getId());

        assertFalse(idGenerator.isStateSaved());
        context.close(null);
        assertTrue(idGenerator.isStateSaved());
    }

    public void test6_UUID() {
        AutoIdGenerator idGenerator = new UUIDIdGenerator();
        InputStream testResource = this.getClass().getResourceAsStream("test6.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"uniqueId"}, true, "clusterName", "modelName", idGenerator);

        if (LOG.isDebugEnabled()) {
            InputStream testResource2 = this.getClass().getResourceAsStream("test6.xml");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource2, config, callback2);
        }

        StateContext context = LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(14, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedAttribute(callback, "attribute1"));
        assertTrue(hasParsedAttribute(callback, "attribute2"));
        assertNotSame("0", callback.getId());
        UUID.fromString(callback.getId()); // Throws exception if not a valid UUID format

        context.close(null);
    }

    public void test7() {
        TestAutoIdGenerator idGenerator = new TestAutoIdGenerator();
        InputStream testResource = this.getClass().getResourceAsStream("test7.xml");
        assertFalse(idGenerator.isStateSaved());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("Product", new String[]{"Id"}, false, "clusterName", "modelName", idGenerator);

        if (LOG.isDebugEnabled()) {
            InputStream testResource2 = this.getClass().getResourceAsStream("test7.xml");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource2, config, callback2);
            idGenerator.reset();
        }

        StateContext context = LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertTrue(hasParsedElement(callback, "Picture"));
        assertEquals("231035933", callback.getId());

        context.close(null);
    }


    public void test3MultiThread() {
        int threadNumber = 20;
        Set<Thread> threads = new HashSet<Thread>(threadNumber + 1);

        Runnable runnable = new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    InputStream testResource = this.getClass().getResourceAsStream("test3.xml");
                    assertNotNull(testResource);

                    ParserTestCallback callback = new ParserTestCallback();

                    LoadParser.Configuration config = new LoadParser.Configuration("Product", new String[]{"Id"}, false, "clusterName", "modelName", idGenerator);
                    LoadParser.parse(testResource, config, callback);
                    assertTrue(callback.hasBeenFlushed());
                    assertEquals(29, callback.getStartedElements().size());
                    assertTrue(hasParsedElement(callback, "Product"));
                    assertTrue(hasParsedElement(callback, "Features"));
                    assertTrue(hasParsedCharacters(callback, "porttitor pharetra quis sed risus."));
                    assertEquals("1", callback.getId());
                }
            }
        };

        for (int i = 0; i < threadNumber; i++) {
            threads.add(new Thread(runnable));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void testMultipleXmlRootWithProcessingInstructions() {
        String documents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><element1/><element2>text</element2></root><?xml version=\"1.0\" encoding=\"UTF-8\"?><root><element1/><element2>text</element2></root>";
        InputStream testResource = new ByteArrayInputStream(documents.getBytes());
        testResource = new XMLRootInputStream(testResource, "doc");
        assertNotNull(testResource);
        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"element2"}, false, "clusterName", "modelName", idGenerator);

        if (LOG.isDebugEnabled()) {
            InputStream testResource2 = new ByteArrayInputStream(documents.getBytes());
            testResource2 = new XMLRootInputStream(testResource2, "doc");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource2, config, callback2);
        }

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(2, callback.getFlushCount());
        assertEquals(26, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("text", callback.getId());
    }

    public void testMultipleXmlRoot() {
        InputStream testResource = new ByteArrayInputStream("<root><element1/><element2>text</element2></root><root><element1/><element2>text</element2></root>".getBytes());
        testResource = new XMLRootInputStream(testResource, "doc");
        assertNotNull(testResource);
        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"element2"}, false, "clusterName", "modelName", idGenerator);

        if (LOG.isDebugEnabled()) {
            InputStream testResource2 = new ByteArrayInputStream("<root><element1/><element2>text</element2></root><root><element1/><element2>text</element2></root>".getBytes());
            testResource2 = new XMLRootInputStream(testResource2, "doc");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource2, config, callback2);
        }

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(2, callback.getFlushCount());
        assertEquals(26, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("text", callback.getId());
    }

    public void testMultipleXmlRootWithAutoGenId() {
        TestAutoIdGenerator idGenerator = new TestAutoIdGenerator();
        InputStream testResource = new ByteArrayInputStream("<Product><Name>Test1</Name><Description>Descr1</Description><Availability>false</Availability><Price>0.0</Price></Product><Product><Name>Test1</Name><Description>Descr1</Description><Availability>false</Availability><Price>0.0</Price></Product>".getBytes());
        testResource = new XMLRootInputStream(testResource, "doc");
        assertNotNull(testResource);
        assertFalse(idGenerator.isStateSaved());
        LoadParser.Configuration config = new LoadParser.Configuration("Product", new String[]{"id"}, true, "clusterName", "modelName", idGenerator);

        if (LOG.isDebugEnabled()) {
            InputStream testResource2 = new ByteArrayInputStream("<Product><Name>Test1</Name><Description>Descr1</Description><Availability>false</Availability><Price>0.0</Price></Product><Product><Name>Test1</Name><Description>Descr1</Description><Availability>false</Availability><Price>0.0</Price></Product>".getBytes());
            testResource2 = new XMLRootInputStream(testResource2, "doc");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource2, config, callback2);
            idGenerator.reset();
        }

        ParserTestCallback callback = new ParserTestCallback();

        StateContext context = LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(2, callback.getFlushCount());
        assertEquals(32, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Product"));
        assertTrue(hasParsedElement(callback, "Name"));
        assertTrue(hasParsedCharacters(callback, "Test1"));
        assertEquals("1", callback.getId());
        assertTrue(idGenerator.getKeyFields().contains("id"));

        assertFalse(idGenerator.isStateSaved());
        context.close(null);
        assertTrue(idGenerator.isStateSaved());
    }

    public void testAutoGenIdWithExistingElements() {
        TestAutoIdGenerator idGenerator = new TestAutoIdGenerator();
        InputStream testResource = new ByteArrayInputStream("<Product><id>1000</id><Name>Test1</Name><Description>Descr1</Description><Availability>false</Availability><Price>0.0</Price></Product>".getBytes());
        testResource = new XMLRootInputStream(testResource, "doc");
        assertNotNull(testResource);
        assertFalse(idGenerator.isStateSaved());
        LoadParser.Configuration config = new LoadParser.Configuration("Product", new String[]{"id"}, true, "clusterName", "modelName", idGenerator);

        if (LOG.isDebugEnabled()) {
            InputStream testResource2 = new ByteArrayInputStream("<Product><id>1000</id><Name>Test1</Name><Description>Descr1</Description><Availability>false</Availability><Price>0.0</Price></Product>".getBytes());
            testResource2 = new XMLRootInputStream(testResource2, "doc");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource2, config, callback2);
            idGenerator.reset();
        }


        ParserTestCallback callback = new ParserTestCallback();

        StateContext context = LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(16, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Product"));
        assertTrue(hasParsedElement(callback, "Name"));
        assertTrue(hasParsedCharacters(callback, "Test1"));
        assertEquals("0", callback.getId());
        assertTrue(idGenerator.getKeyFields().contains("id"));

        assertFalse(idGenerator.isStateSaved());
        context.close(null);
        assertTrue(idGenerator.isStateSaved());
    }


    public void testMultipleXmlRootWithAutoGenCompoundId() {
        TestAutoIdGenerator idGenerator = new TestAutoIdGenerator();
        InputStream testResource = new ByteArrayInputStream("<Product><Name>Test1</Name><Description>Descr1</Description><Availability>false</Availability><Price>0.0</Price></Product><Product><Name>Test1</Name><Description>Descr1</Description><Availability>false</Availability><Price>0.0</Price></Product>".getBytes());
        testResource = new XMLRootInputStream(testResource, "doc");
        assertNotNull(testResource);
        assertFalse(idGenerator.isStateSaved());
        LoadParser.Configuration config = new LoadParser.Configuration("Product", new String[]{"id1", "id2"}, true, "clusterName", "modelName", idGenerator);

        if (LOG.isDebugEnabled()) {
            InputStream testResource2 = new ByteArrayInputStream("<Product><Name>Test1</Name><Description>Descr1</Description><Availability>false</Availability><Price>0.0</Price></Product><Product><Name>Test1</Name><Description>Descr1</Description><Availability>false</Availability><Price>0.0</Price></Product>".getBytes());
            testResource2 = new XMLRootInputStream(testResource2, "doc");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource2, config, callback2);
            idGenerator.reset();
        }

        ParserTestCallback callback = new ParserTestCallback();

        StateContext context = LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(2, callback.getFlushCount());
        assertEquals(36, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Product"));
        assertTrue(hasParsedElement(callback, "Name"));
        assertTrue(hasParsedCharacters(callback, "Test1"));
        assertEquals("2:3", callback.getId());

        assertTrue(idGenerator.getKeyFields().contains("id1"));
        assertTrue(idGenerator.getKeyFields().contains("id2"));

        assertFalse(idGenerator.isStateSaved());
        context.close(null);
        assertTrue(idGenerator.isStateSaved());

    }

    public void testMultipleXmlRootFailure() {
        InputStream testResource = new ByteArrayInputStream("<root><element1/><element2>text</element2></root><root><element1/><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        try {
            LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"element2"}, false, "clusterName", "modelName", idGenerator);
            LoadParser.parse(testResource, config, callback);
            fail("Should have failed. There are 2 roots in XML document.");
        } catch (Exception e) {
            // Expected
        }

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("text", callback.getId());
    }

    public void testSimpleId() {
        InputStream testResource = new ByteArrayInputStream("<root><Id>1</Id><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id"}, false, "clusterName", "modelName", idGenerator);
        LoadParser.parse(testResource, config, callback);

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("1", callback.getId());
    }

    public void testIdFailure() {
        InputStream testResource = new ByteArrayInputStream("<root><Id>1</Id><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        try {
            LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"element_that_does_not_exist"}, false, "clusterName", "modelName", idGenerator);
            LoadParser.parse(testResource, config, callback);
            fail("Expected an error since id field does not exist");
        } catch (Exception e) {
            // Expected
        }

        assertFalse(callback.hasBeenFlushed());
        assertEquals(0, callback.getFlushCount());
        assertEquals(0, callback.getStartedElements().size());
    }

    public void testEmptyId() {
        InputStream testResource = new ByteArrayInputStream("<root><Id></Id><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id"}, false, "clusterName", "modelName", idGenerator);
        LoadParser.parse(testResource, config, callback);

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("", callback.getId());
    }

    public void testCompoundId() {
        InputStream testResource = new ByteArrayInputStream("<root><Id1>1</Id1><Id2>2</Id2><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id1", "Id2"}, false, "clusterName", "modelName", idGenerator);
        LoadParser.parse(testResource, config, callback);

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(15, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("1:2", callback.getId());
    }

    public void testNewId() {
        InputStream testResource = new ByteArrayInputStream("<root><NewId>1</NewId><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"NewId"}, false, "clusterName", "modelName", idGenerator);
        LoadParser.parse(testResource, config, callback);

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("1", callback.getId());
    }

    public void testNestedId() {
        InputStream testResource = new ByteArrayInputStream("<root><Id><RealId>1</RealId></Id><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id/RealId"}, false, "clusterName", "modelName", idGenerator);
        LoadParser.parse(testResource, config, callback);

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(14, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("1", callback.getId());
    }
    
    public void testIdContainsInvalidCharacter() {
        InputStream testResource = new ByteArrayInputStream("<root><Id>p[roduct4','p[roduct]','p]roduct'</Id><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);
        
        try {
            ParserTestCallback callback = new ParserTestCallback();
            LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id"}, false, "clusterName", "modelName", idGenerator);
            LoadParser.parse(testResource, config, callback);
            Assert.fail("Should have failed due to invalid ID character.");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
            assertEquals("ID p[roduct4','p[roduct]','p]roduct' contains invalid character '''", e.getMessage());
        }
    }

    public void testEscapeCharacters() throws Exception {

        final String xmlSource = "<Product><Id>123</Id><Name>a&amp;b&lt;c&gt;s</Name></Product>";

        Properties props = MDMConfiguration.getConfiguration();
        props.setProperty("xmlserver.class", "com.amalto.core.storage.DispatchWrapper");
        props.setProperty("qizx.db.type", "server");

        InputStream testResource = new ByteArrayInputStream(xmlSource.getBytes());
        assertNotNull(testResource);
        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("Product", new String[] { "Id" }, false,
                "Product", "Product", idGenerator);
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Product"));
        assertTrue(hasParsedElement(callback, "Name"));
        assertTrue(hasParsedCharacters(callback, "a&b<c>s"));
        assertEquals("123", callback.getId());

        props = MDMConfiguration.getConfiguration();
        props.setProperty("xmlserver.class", "com.amalto.core.storage.DispatchWrapper");
        props.setProperty("qizx.db.type", "embedded");

        testResource = new ByteArrayInputStream(xmlSource.getBytes());
        assertNotNull(testResource);
        callback = new ParserTestCallback();

        config = new LoadParser.Configuration("Product", new String[] { "Id" }, false, "Product", "Product", idGenerator);
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Product"));
        assertTrue(hasParsedElement(callback, "Name"));
        assertTrue(hasParsedCharacters(callback, "a&b<c>s"));
        assertEquals("123", callback.getId());

        props = MDMConfiguration.getConfiguration();
        props.setProperty("xmlserver.class", "org.talend.mdm.qizx.xmldb.QizxWrapper");
        props.setProperty("qizx.db.type", "server");

        testResource = new ByteArrayInputStream(xmlSource.getBytes());
        assertNotNull(testResource);
        callback = new ParserTestCallback();

        config = new LoadParser.Configuration("Product", new String[] { "Id" }, false, "Product", "Product", idGenerator);
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Product"));
        assertTrue(hasParsedElement(callback, "Name"));
        assertTrue(hasParsedCharacters(callback, "a&amp;b&lt;c&gt;s"));

        assertEquals("123", callback.getId());

        props = MDMConfiguration.getConfiguration();
        props.setProperty("xmlserver.class", "org.talend.mdm.qizx.xmldb.QizxWrapper");
        props.setProperty("qizx.db.type", "embedded");

        testResource = new ByteArrayInputStream(xmlSource.getBytes());
        assertNotNull(testResource);
        callback = new ParserTestCallback();

        config = new LoadParser.Configuration("Product", new String[] { "Id" }, false, "Product", "Product", idGenerator);
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Product"));
        assertTrue(hasParsedElement(callback, "Name"));
        assertTrue(hasParsedCharacters(callback, "a&b<c>s"));
        assertEquals("123", callback.getId());
    }

    private boolean hasParsedCharacters(ParserTestCallback callback, String string) {
        return callback.getParsedCharacters().contains(string);
    }

    private static class ParserTestCallback implements LoadParserCallback {
        private final List<String> startedElements = new ArrayList<String>();
        private final List<String> parsedAttributes = new ArrayList<String>();
        private final Set<String> characters = new HashSet<String>();
        private int flushCount;
        private String id;

        public boolean hasBeenFlushed() {
            return flushCount > 0;
        }

        public int getFlushCount() {
            return flushCount;
        }

        public List<String> getStartedElements() {
            return startedElements;
        }

        public List<String> getParsedAttributes() {
            return parsedAttributes;
        }

        public String getId() {
            return id;
        }

        public Set<String> getParsedCharacters() {
            return Collections.unmodifiableSet(characters);
        }

        public void flushDocument(XMLReader docReader, InputSource input) {
            flushCount++;

            try {
                docReader.setContentHandler(new DefaultHandler() {
                    public boolean isId;

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        startedElements.add(localName);

                        if ("i".equals(localName)) {
                            isId = true;
                        }

                        for (int i = 0; i < attributes.getLength(); i++) {
                            parsedAttributes.add(attributes.getLocalName(i));
                        }
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) throws SAXException {
                        if (isId) {
                            isId = false;
                        }
                        super.endElement(uri, localName, qName);
                    }

                    @Override
                    public void characters(char[] ch, int start, int length) throws SAXException {
                        String string = new String(ch);
                        if (isId) {
                            if (id != null && !id.isEmpty()) {
                                id += ':' + string;
                            } else {
                                id = string;
                            }
                        } else {
                            characters.add(string.trim());
                        }
                        super.characters(ch, start, length);
                    }

                    @Override
                    public void startDocument() throws SAXException {
                        id = null;
                    }
                });
                docReader.parse(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean hasParsedElement(ParserTestCallback callback, String elementName) {
        boolean hasParsedElement = false;
        for (String currentElementName : callback.getStartedElements()) {
            hasParsedElement = elementName.equals(currentElementName);
            if (hasParsedElement) {
                break;
            }
        }
        return hasParsedElement;
    }

    private boolean hasParsedAttribute(ParserTestCallback callback, String attributeName) {
        boolean hasParsedAttribute = false;
        for (String currentAttributeName : callback.getParsedAttributes()) {
            hasParsedAttribute = attributeName.equals(currentAttributeName);
            if (hasParsedAttribute) {
                break;
            }
        }
        return hasParsedAttribute;
    }

    private static class ConsolePrintParserCallback implements LoadParserCallback {
        private final Map<String, String> namespaceToPrefix = new HashMap<String, String>();
        private final Set<String> declaredNamespaces = new HashSet<String>();

        public void flushDocument(XMLReader docReader, InputSource input) {
            try {
                docReader.setContentHandler(new ContentHandler() {
                    int indent = 0;

                    private void indent() {
                        for (int i = 0; i < indent; i++) {
                            System.out.print('\t');
                        }
                    }

                    public void setDocumentLocator(Locator locator) {
                        LOG.debug("ParserScalabilityTest$ConsolePrintParserCallback.setDocumentLocator");
                    }

                    public void startDocument() throws SAXException {
                        LOG.debug("[Document start]");
                    }

                    public void endDocument() throws SAXException {
                        if (indent > 0) {
                            throw new IllegalStateException("XML document isn't well-formed.");
                        }
                        LOG.debug("[Document end]");
                    }

                    public void startPrefixMapping(String prefix, String uri) throws SAXException {
                        namespaceToPrefix.put(uri, prefix);
                    }

                    public void endPrefixMapping(String prefix) throws SAXException {
                        String namespaceToRemove = null;
                        Set<Map.Entry<String, String>> entries = namespaceToPrefix.entrySet();
                        for (Map.Entry<String, String> entry : entries) {
                            if (prefix != null && prefix.equals(entry.getValue())) {
                                namespaceToRemove = entry.getKey();
                            }
                        }

                        if (namespaceToRemove == null) {
                            throw new RuntimeException("Could not find namespace with prefix '" + prefix + "' in current context");
                        }

                        declaredNamespaces.remove(namespaceToRemove);
                        namespaceToPrefix.remove(namespaceToRemove);
                    }

                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        indent();
                        System.out.print('<' + localName);
                        if (attributes.getLength() > 0) {
                            System.out.print(' ');
                        }
                        for (int i = 0; i < attributes.getLength(); i++) {
                            String attributeURI = attributes.getURI(i);
                            String prefix = StringUtils.EMPTY;
                            if (StringUtils.EMPTY.equals(attributeURI)) {
                                prefix = namespaceToPrefix.get(attributeURI) + ':';
                            }
                            System.out.print(prefix + attributes.getLocalName(i) + "=\"" + attributes.getValue(i) + "\" ");
                        }
                        Set<Map.Entry<String, String>> entries = namespaceToPrefix.entrySet();
                        for (Map.Entry<String, String> entry : entries) {
                            if (!declaredNamespaces.contains(entry.getKey())) {
                                System.out.print(" xmlns:" + entry.getValue() + "=\"" + entry.getKey() + "\" ");
                                declaredNamespaces.add(entry.getKey());
                            }
                        }
                        System.out.print('>');
                        indent++;
                    }

                    public void endElement(String uri, String localName, String qName) throws SAXException {
                        indent--;
                        indent();
                        System.out.print("</" + localName + ">");
                    }

                    public void characters(char[] ch, int start, int length) throws SAXException {
                        indent();
                        for (int i = start; i < length; i++) {
                            System.out.print(ch[i]);
                        }
                        System.out.print("");
                    }

                    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                        LOG.debug("ParserScalabilityTest$ConsolePrintParserCallback.ignorableWhitespace");
                    }

                    public void processingInstruction(String target, String data) throws SAXException {
                        LOG.debug("ParserScalabilityTest$ConsolePrintParserCallback.processingInstruction");
                    }

                    public void skippedEntity(String name) throws SAXException {
                        LOG.debug("ParserScalabilityTest$ConsolePrintParserCallback.skippedEntity");
                    }
                });
                docReader.parse(input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class TestAutoIdGenerator implements AutoIdGenerator {

        private boolean savedState;

        private final Set<String> keyElementNames = new HashSet<String>();

        private int currentId = 0;

        public String generateId(String dataClusterName, String conceptName, String keyElementName) {
            keyElementNames.add(keyElementName);
            savedState = false;
            return String.valueOf(currentId++);
        }

        public void saveState(XmlServer server) {
            savedState = true;
        }

        @Override
        public void init() {
        }

        public boolean isStateSaved() {
            return savedState;
        }

        public Set<String> getKeyFields() {
            return keyElementNames;
        }

        public void reset() {
            currentId = 0;
        }
    }
}
