/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import com.amalto.core.load.io.XMLStreamTokenizer;

/**
 *
 */
@SuppressWarnings("nls")
public class XMLStreamTokenizerTest extends TestCase {

    public void testFail() {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(new ByteArrayInputStream("<root></root>".getBytes()));
        try {
            tokenizer.nextElement();
            fail("Should call hasMoreElements before next");
        } catch (Exception e) {
            // Expected
        }
    }

    public void testSingleDocument() {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(new ByteArrayInputStream("<root></root>".getBytes()));
        int docCount = 0;
        while (tokenizer.hasMoreElements()) {
            assertEquals("<root></root>", tokenizer.nextElement());
            docCount++;
        }

        assertEquals(1, docCount);
    }

    public void testDocuments() {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(new ByteArrayInputStream("<root></root><root></root>".getBytes()));
        int docCount = 0;
        while (tokenizer.hasMoreElements()) {
            assertEquals("<root></root>", tokenizer.nextElement());
            docCount++;
        }

        assertEquals(2, docCount);
    }

    public void testDocumentsWithLineFeed() {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(new ByteArrayInputStream(
                "\n<root>\t<field></field></root>\n<root>\t<field></field></root>\n<root>\t<field></field></root>".getBytes()));
        int docCount = 0;
        while (tokenizer.hasMoreElements()) {
            assertEquals("\n<root>\t<field></field></root>", tokenizer.nextElement());
            docCount++;
        }

        assertEquals(3, docCount);
    }

    // See TMDM-2497 description
    public void testFormattedDocuments() {
        InputStream stream = this.getClass().getResourceAsStream("xmlTokenizer1.xml");
        assertNotNull(stream);
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(stream);
        int docCount = 0;
        while (tokenizer.hasMoreElements()) {
            docCount++;
        }

        assertTrue(docCount > 1);
        assertEquals(149, docCount);
    }

    public void testNestedDocuments() {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(new ByteArrayInputStream("<root><root></root></root>".getBytes()));
        int docCount = 0;
        while (tokenizer.hasMoreElements()) {
            assertEquals("<root><root></root></root>", tokenizer.nextElement());
            docCount++;
        }

        assertEquals(1, docCount);
    }

    public void testDocumentsWithAttributes() {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(new ByteArrayInputStream(
                "<root xsi:type=\"MyType<>\"></root>".getBytes()));
        int docCount = 0;
        while (tokenizer.hasMoreElements()) {
            assertEquals("<root xsi:type=\"MyType<>\"></root>", tokenizer.nextElement());
            docCount++;
        }

        assertEquals(1, docCount);
    }

    //
    public void testDocument() {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(new ByteArrayInputStream(
                "<ProductFamily><Id>2</Id><Name>Family 2</Name></ProductFamily><ProductFamily><Id>3</Id><Name>Family 3</Name></ProductFamily>"
                        .getBytes()));
        int docCount = 0;
        String[] expected = new String[] { "<ProductFamily><Id>2</Id><Name>Family 2</Name></ProductFamily>",
                "<ProductFamily><Id>3</Id><Name>Family 3</Name></ProductFamily>" };
        while (tokenizer.hasMoreElements()) {
            assertEquals(expected[docCount], tokenizer.nextElement());
            docCount++;
        }

        assertEquals(2, docCount);
    }

    //
    public void testDocument2() {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(
                new ByteArrayInputStream(
                        "<Product><Name>Talend Golf Shirt</Name><Description>Golf-style, collared t-shirt</Description><Availability>false</Availability><Price>16.99</Price><Family/><Id>231035935</Id><Picture>/imageserver/upload/TalendShop/golf_shirt.jpg?width=150&amp;amp;height=90&amp;amp;preserveAspectRatio=true</Picture><OnlineStore>Talend Shop@@http://www.cafepress.com/Talend.231035935</OnlineStore></Product><Product><Name>Talend Fitted T-Shirt</Name><Description>Fitted T. ultra-fine combed ring spun cotton</Description><Availability>false</Availability><Price>15.99</Price><Family/><Id>231035936</Id><Picture>/imageserver/upload/TalendShop/tshirt.jpg?width=150&amp;amp;height=90&amp;amp;preserveAspectRatio=true</Picture><OnlineStore>Talend Shop@@http://www.cafepress.com/Talend.231035936</OnlineStore></Product>"
                                .getBytes()));
        int docCount = 0;
        String[] expected = new String[] {
                "<Product><Name>Talend Golf Shirt</Name><Description>Golf-style, collared t-shirt</Description><Availability>false</Availability><Price>16.99</Price><Family/><Id>231035935</Id><Picture>/imageserver/upload/TalendShop/golf_shirt.jpg?width=150&amp;amp;height=90&amp;amp;preserveAspectRatio=true</Picture><OnlineStore>Talend Shop@@http://www.cafepress.com/Talend.231035935</OnlineStore></Product>",
                "<Product><Name>Talend Fitted T-Shirt</Name><Description>Fitted T. ultra-fine combed ring spun cotton</Description><Availability>false</Availability><Price>15.99</Price><Family/><Id>231035936</Id><Picture>/imageserver/upload/TalendShop/tshirt.jpg?width=150&amp;amp;height=90&amp;amp;preserveAspectRatio=true</Picture><OnlineStore>Talend Shop@@http://www.cafepress.com/Talend.231035936</OnlineStore></Product>" };
        while (tokenizer.hasMoreElements()) {
            assertEquals(expected[docCount], tokenizer.nextElement());
            docCount++;
        }

        assertEquals(2, docCount);
    }

    public void testProcessingInstructions() {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(new ByteArrayInputStream(
                "<?xml version=\"1.0\"?><root></root><?xml version=\"1.0\"?><root></root>".getBytes()));
        int docCount = 0;
        String[] expected = new String[] { "<root></root>", "<root></root>" };
        while (tokenizer.hasMoreElements()) {
            assertEquals(expected[docCount], tokenizer.nextElement());
            docCount++;
        }

        assertEquals(2, docCount);
    }

    public void testMixedRootElements() {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(new ByteArrayInputStream(
                "<root></root><alternative></alternative><root></root><alternative></alternative>".getBytes()));
        int docCount = 0;
        String[] expected = new String[] { "<root></root>", "<alternative></alternative>", "<root></root>",
                "<alternative></alternative>" };
        while (tokenizer.hasMoreElements()) {
            assertEquals(expected[docCount], tokenizer.nextElement());
            docCount++;
        }

        assertEquals(4, docCount);
    }

    public void testNonUTF8Characters() throws Exception {
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(this.getClass().getResourceAsStream("xmlTokenizer2.xml"), "CP1252");

        int docCount = 0;
        while (tokenizer.hasMoreElements()) {
            String nextElement = tokenizer.nextElement();
            assertFalse(nextElement.contains("Acc\u00E8s")); // Wrong encoding so should fail
            docCount++;
        }
        assertEquals(2, docCount);

        tokenizer = new XMLStreamTokenizer(this.getClass().getResourceAsStream("xmlTokenizer2.xml"), "UTF-8");
        docCount = 0;
        while (tokenizer.hasMoreElements()) {
            String nextElement = tokenizer.nextElement();
            assertTrue(nextElement.contains("Acc\u00E8s")); // Right encoding so should succeed
            docCount++;
        }
        assertEquals(2, docCount);

        tokenizer = new XMLStreamTokenizer(new ByteArrayInputStream(
                "<Test><Type>WITH_VALIDATION</Type><Value>éç</Value></Test>".getBytes()), "UTF-8");
        docCount = 0;
        while (tokenizer.hasMoreElements()) {
            String nextElement = tokenizer.nextElement();
            assertTrue(nextElement.contains("éç")); // Right encoding so should succeed
            docCount++;
        }
        assertEquals(1, docCount);
    }

    public void testInvalidXML() {
        // FIXME XMLStreamTokenizer does not mandates incoming XML roots to be valid. Therefore it might pass illegal
        // values and has to rely on additional parsing to detect that.
        String root1 = "<root att='\"'></root>";
        String root2 = "<root att=\"'\"></root>";
        String root3 = "<root><@#éç& < /> <&</root>";
        String root4 = "<root></root>";
        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(
                new ByteArrayInputStream((root1 + root2 + root3 + root4).getBytes()));
        int docCount = 0;
        String[] expected = new String[] { root1, root2, root3 + root4 };
        while (tokenizer.hasMoreElements()) {
            assertEquals(expected[docCount], tokenizer.nextElement());
            docCount++;
        }
        assertEquals(3, docCount);
    }

    public void testValidXML() {
        String root1 = "<root att='&quot;'></root>";
        String root2 = "<root att=\"&pos;\"></root>";
        String root3 = "<root>&lt; @#éç& &lt; &gt; &lt;&amp;</root>";
        String root4 = "<root></root>";

        XMLStreamTokenizer tokenizer = new XMLStreamTokenizer(
                new ByteArrayInputStream((root1 + root2 + root3 + root4).getBytes()));
        int docCount = 0;
        String[] expected = new String[] { root1, root2, root3, root4 };
        while (tokenizer.hasMoreElements()) {
            assertEquals(expected[docCount], tokenizer.nextElement());
            docCount++;
        }
        assertEquals(4, docCount);
    }
}
