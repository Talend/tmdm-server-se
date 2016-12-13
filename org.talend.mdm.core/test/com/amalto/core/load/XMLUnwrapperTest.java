/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import com.amalto.core.load.io.XMLStreamUnwrapper;

public class XMLUnwrapperTest extends TestCase {

    public void testEmptyRootElement() throws Exception {
        XMLStreamUnwrapper tokenizer = new XMLStreamUnwrapper(new ByteArrayInputStream("<root>\n</root>".getBytes()));
        assertFalse(tokenizer.hasMoreElements());
    }

    public void testLineFeedsElement() throws Exception {
        XMLStreamUnwrapper tokenizer = new XMLStreamUnwrapper(new ByteArrayInputStream("<root>\n<test>\n</test>\n</root>".getBytes()));
        int i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals("<test/>", element);
        }
        assertEquals(1, i);
    }

    public void testSingleRootElement() throws Exception {
        XMLStreamUnwrapper tokenizer = new XMLStreamUnwrapper(new ByteArrayInputStream("<root><test></test></root>".getBytes()));
        int i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals("<test/>", element);
        }
        assertEquals(1, i);
    }

    public void testSingleRootElements() throws Exception {
        XMLStreamUnwrapper tokenizer = new XMLStreamUnwrapper(new ByteArrayInputStream("<root><test></test><test></test></root>".getBytes()));
        int i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals("<test/>", element);
        }
        assertEquals(2, i);
    }

    public void testNestedElements() throws Exception {
        XMLStreamUnwrapper tokenizer = new XMLStreamUnwrapper(new ByteArrayInputStream("<root><test><value1>value</value1></test><test><value1>value</value1></test></root>".getBytes()));
        int i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals("<test><value1>value</value1></test>", element);
        }
        assertEquals(2, i);
    }

    public void testAttribute() throws Exception {
        XMLStreamUnwrapper tokenizer = new XMLStreamUnwrapper(new ByteArrayInputStream("<root><test><value1 attribute=\"test\">value</value1></test><test><value1 attribute=\"test\">value</value1></test></root>".getBytes()));
        int i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals("<test><value1 attribute=\"test\">value</value1></test>", element);
        }
        assertEquals(2, i);
    }

    public void testNSAttributes() throws Exception {
        XMLStreamUnwrapper tokenizer = new XMLStreamUnwrapper(new ByteArrayInputStream("<root><test xmlns:tmdm=\"http://www.talend.com/mdm\"><value1 tmdm:type=\"test\">value</value1></test><test xmlns:tmdm=\"http://www.talend.com/mdm\"><value1 tmdm:type=\"test\">value</value1></test></root>".getBytes()));
        int i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals("<test xmlns:tmdm=\"http://www.talend.com/mdm\"><value1 tmdm:type=\"test\">value</value1></test>", element);
        }
        assertEquals(2, i);
    }

    public void testInheritanceType() throws Exception {
        XMLStreamUnwrapper tokenizer = new XMLStreamUnwrapper(
                new ByteArrayInputStream(
                        "<records xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Societe><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe></records>"
                                .getBytes()));
        int i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals(
                    "<Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe>",
                    element);
        }
        assertEquals(1, i);

        tokenizer = new XMLStreamUnwrapper(
                new ByteArrayInputStream(
                        "<records xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe></records>"
                                .getBytes()));
        i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals(
                    "<Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe>",
                    element);
        }
        assertEquals(1, i);

        tokenizer = new XMLStreamUnwrapper(
                new ByteArrayInputStream(
                        "<records><Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe></records>"
                                .getBytes()));
        i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals(
                    "<Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe>",
                    element);
        }
        assertEquals(1, i);

        tokenizer = new XMLStreamUnwrapper(
                new ByteArrayInputStream(
                        "<records xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Societe><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe><Societe><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe><Societe><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe><Societe><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe></records>"
                                .getBytes()));
        i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals(
                    "<Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe>",
                    element);
        }
        assertEquals(4, i);

        tokenizer = new XMLStreamUnwrapper(
                new ByteArrayInputStream(
                        "<records><Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe><Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe><Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe><Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe></records>"
                                .getBytes()));
        i = 0;
        while (tokenizer.hasMoreElements()) {
            i++;
            final String element = tokenizer.nextElement();
            assertEquals(
                    "<Societe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><CodeOSMOSE>1</CodeOSMOSE><Contacts><Contact><StatutContactFk>[1]</StatutContactFk><SpecialisationContactType xsi:type=\"SpecialisationContactEmail\"><NatureEmailFk>[2]</NatureEmailFk><Email>1234</Email></SpecialisationContactType></Contact></Contacts><EtablissementsConso/></Societe>",
                    element);
        }
        assertEquals(4, i);
    }

}
