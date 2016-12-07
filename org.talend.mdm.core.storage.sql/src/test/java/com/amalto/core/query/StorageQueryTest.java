// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package com.amalto.core.query;

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.query.optimization.RangeOptimizer;
import com.amalto.core.query.optimization.UpdateReportOptimizer;
import com.amalto.core.query.user.Alias;
import com.amalto.core.query.user.BinaryLogicOperator;
import com.amalto.core.query.user.Compare;
import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.Field;
import com.amalto.core.query.user.IntegerConstant;
import com.amalto.core.query.user.IsNull;
import com.amalto.core.query.user.LongConstant;
import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.OrderBy.Direction;
import com.amalto.core.query.user.Predicate;
import com.amalto.core.query.user.Range;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.Split;
import com.amalto.core.query.user.StringConstant;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UnaryLogicOperator;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.UserQueryHelper;
import com.amalto.core.query.user.metadata.Timestamp;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.StorageWrapper;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.storage.record.DataRecordXmlWriter;
import com.amalto.core.storage.record.ViewSearchResultsWriter;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.amalto.xmlserver.interfaces.WhereOr;
import com.amalto.xmlserver.interfaces.XmlServerException;

@SuppressWarnings("nls")
public class StorageQueryTest extends StorageTestCase {

    private final String E1_Record1 = "<E1><subelement>aaa</subelement><subelement1>bbb</subelement1><name>asdf</name></E1>";

    private final String E1_Record2 = "<E1><subelement>ccc</subelement><subelement1>ddd</subelement1><name>cvcvc</name></E1>";

    private final String E1_Record3 = "<E1><subelement>ttt</subelement><subelement1>yyy</subelement1><name>nhhn</name></E1>";

    private final String E2_Record1 = "<E2><subelement>111</subelement><subelement1>222</subelement1><name>qwe</name><fk>[ccc][ddd]</fk></E2>";

    private final String E2_Record2 = "<E2><subelement>344</subelement><subelement1>544</subelement1><name>55</name><fk>[aaa][bbb]</fk></E2>";

    private final String E2_Record3 = "<E2><subelement>333</subelement><subelement1>444</subelement1><name>tyty</name><fk>[ttt][yyy]</fk></E2>";

    private final String E2_Record4 = "<E2><subelement>666</subelement><subelement1>777</subelement1><name>iuj</name><fk>[aaa][bbb]</fk></E2>";

    private final String E2_Record5 = "<E2><subelement>6767</subelement><subelement1>7878</subelement1><name>ioiu</name><fk>[ccc][ddd]</fk></E2>";

    private final String E2_Record6 = "<E2><subelement>999</subelement><subelement1>888</subelement1><name>iuiiu</name><fk>[ccc][ddd]</fk></E2>";

    private final String E2_Record7 = "<E2><subelement>119</subelement><subelement1>120</subelement1><name>zhang</name></E2>";

    private final String RepeatableElementsEntity_Record = "<RepeatableElementsEntity><id>1</id><info><name>n1</name><age>1</age></info><info><name>n2</name><age>2</age></info><info><name>n3</name><age>3</age></info></RepeatableElementsEntity>";

    private final String RR_Record1 = "<RR><Id>R1</Id><Name>R1</Name></RR>";

    private final String RR_Record2 = "<RR><Id>R2</Id><Name>R2</Name></RR>";

    private final String RR_Record3 = "<RR><Id>R3</Id><Name>R3</Name></RR>";

    private final String TT_Record1 = " <TT><Id>T1</Id><MUl><E1>1</E1><E2>1</E2><E3>[R1]</E3></MUl></TT>";

    private final String TT_Record2 = " <TT><Id>T2</Id><MUl><E1>2</E1><E2>2</E2><E3>[R2]</E3></MUl></TT>";

    private final String TT_Record3 = " <TT><Id>T3</Id><MUl><E1>3</E1><E2>3</E2><E3>[R3]</E3></MUl></TT>";
    
    private final String COMPTE_Record1 = "<Compte><Level>Compte SF</Level><Code>1</Code><Label>1</Label></Compte>";

    private final String COMPTE_Record2 = "<Compte><Level>Nature Comptable SF</Level><Code>11</Code><Label>11</Label><childOf>[Compte SF][1]</childOf></Compte>";
    
    private void populateData() {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();

        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>1</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France</name></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>2</id><creationDate>2011-10-10</creationDate><creationTime>2011-10-10T01:01:01</creationTime><name>USA</name><notes><note>Country note</note><comment>repeatable comment 1</comment><comment>Repeatable comment 2</comment></notes></Country>"));

        allRecords
                .add(factory
                        .read(repository,
                                address,
                                "<Address><id>1</id><enterprise>false</enterprise><Street>Street1</Street><ZipCode>10000</ZipCode><City>City</City><country>[1]</country></Address>"));
        allRecords
                .add(factory
                        .read(repository,
                                address,
                                "<Address><id>1</id><enterprise>true</enterprise><Street>Street1</Street><ZipCode>10000</ZipCode><City>City</City><country>[2]</country></Address>"));
        allRecords
                .add(factory
                        .read(repository,
                                address,
                                "<Address><id>2&amp;2</id><enterprise>true</enterprise><Street>Street2</Street><ZipCode>10000</ZipCode><City>City</City><country>[2]</country></Address>"));
        allRecords
                .add(factory
                        .read(repository,
                                address,
                                "<Address><id>3</id><enterprise>false</enterprise><Street>Street3</Street><ZipCode>10000</ZipCode><City>City</City><country>[1]</country></Address>"));
        allRecords
                .add(factory
                        .read(repository,
                                address,
                                "<Address><id>4</id><enterprise>false</enterprise><Street>Street3</Street><ZipCode>10000</ZipCode><City>City</City><OptionalCity>City2</OptionalCity><country>[1]</country></Address>"));
        allRecords
                .add(factory
                        .read(repository,
                                person,
                                "<Person><id>1</id><score>130000.00</score><lastname>Dupond</lastname><resume>[EN:my splendid resume, splendid isn't it][FR:mon magnifique resume, n'est ce pas ?]</resume><middlename>John</middlename><firstname>Julien</firstname><addresses><address>[2&amp;2][true]</address><address>[1][false]</address></addresses><age>10</age><Status>Employee</Status><Available>true</Available></Person>"));
        allRecords
                .add(factory
                        .read(repository,
                                person,
                                "<Person><id>2</id><score>170000.00</score><lastname>Dupont</lastname><middlename>John</middlename><firstname>Robert-Julien</firstname><addresses><address>[1][false]</address><address>[2&amp;2][true]</address></addresses><age>20</age><Status>Customer</Status><Available>false</Available></Person>"));
        allRecords
                .add(factory
                        .read(repository,
                                person,
                                "<Person><id>3</id><score>200000.00</score><lastname>Leblanc</lastname><middlename>John</middlename><firstname>Juste</firstname><addresses><address>[3][false]</address><address>[1][false]</address></addresses><age>30</age><Status>Friend</Status></Person>"));
        allRecords
                .add(factory
                        .read(repository,
                                person,
                                "<Person><id>4</id><score>200000.00</score><lastname>Leblanc</lastname><middlename>John</middlename><firstname>Julien</firstname><age>30</age><Status>Friend</Status></Person>"));
        allRecords.add(factory.read(repository, b, "<B><id>1</id><textB>TextB</textB></B>"));
        allRecords.add(factory.read(repository, d, "<D><id>2</id><textB>TextBD</textB><textD>TextDD</textD></D>"));
        allRecords.add(factory.read(repository, a, "<A><id>1</id><textA>TextA</textA><nestedB><text>Text1</text></nestedB></A>"));
        allRecords.add(factory.read(repository, a,
                "<A><id>2</id><textA>TextA</textA><nestedB><text>Text2</text></nestedB><refA>[1]</refA></A>"));
        allRecords
                .add(factory
                        .read(repository,
                                a,
                                "<A xmlns:tmdm=\"http://www.talend.com/mdm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>3</id><refB tmdm:type=\"B\">[1]</refB><textA>TextA</textA><nestedB xsi:type=\"Nested\"><text>Text</text></nestedB></A>"));
        allRecords
                .add(factory
                        .read(repository,
                                a,
                                "<A xmlns:tmdm=\"http://www.talend.com/mdm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>4</id><refB tmdm:type=\"D\">[2]</refB><textA>TextA</textA><nestedB xsi:type=\"Nested\"><text>Text</text></nestedB></A>"));
        allRecords
                .add(factory
                        .read(repository,
                                a,
                                "<A xmlns:tmdm=\"http://www.talend.com/mdm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>5</id><refB tmdm:type=\"B\">[2]</refB><textA>TextA</textA><nestedB xsi:type=\"Nested\"><text>Text</text></nestedB></A>"));

        allRecords.add(factory.read(repository, supplier, "<Supplier>\n" + "    <Id>1</Id>\n"
                + "    <SupplierName>Renault</SupplierName>\n" + "    <Contact>" + "        <Name>Jean Voiture</Name>\n"
                + "        <Phone>33123456789</Phone>\n" + "        <Email>test@test.org</Email>\n" + "    </Contact>\n"
                + "</Supplier>"));
        allRecords.add(factory.read(repository, supplier, "<Supplier>\n" + "    <Id>.127</Id>\n"
                + "    <SupplierName>Renault</SupplierName>\n" + "    <Contact>" + "        <Name>Jean Voiture</Name>\n"
                + "        <Phone>33123456789</Phone>\n" + "        <Email>test@test.org</Email>\n" + "    </Contact>\n"
                + "</Supplier>"));
        allRecords.add(factory.read(repository, supplier, "<Supplier>\n" + "    <Id>127.</Id>\n"
                + "    <SupplierName>Renault</SupplierName>\n" + "    <Contact>" + "        <Name>Jean Voiture</Name>\n"
                + "        <Phone>33123456789</Phone>\n" + "        <Email>test@test.org</Email>\n" + "    </Contact>\n"
                + "</Supplier>"));
        allRecords.add(factory.read(repository, supplier, "<Supplier>\n" + "    <Id>127.0.0.1</Id>\n"
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
        allRecords.add(factory.read(repository, productFamily, "<ProductFamily>\n" + "    <Id>1</Id>\n"
                + "    <Name>Product family #1</Name>\n" + "</ProductFamily>"));
        allRecords.add(factory.read(repository, productFamily, "<ProductFamily>\n" + "    <Id>2</Id>\n"
                + "    <Name>Product family #2</Name>\n" + "</ProductFamily>"));
        
        allRecords.add(factory.read(repository, productFamily, "<ProductFamily>\n" + "    <Id>3</Id>\n"
                + "    <Name>test name3</Name>\n" + "</ProductFamily>"));
        allRecords.add(factory.read(repository, productFamily, "<ProductFamily>\n" + "    <Id>4</Id>\n"
                + "    <Name>test_name4</Name>\n" + "</ProductFamily>"));
        allRecords.add(factory.read(repository, productFamily, "<ProductFamily>\n" + "    <Id>5</Id>\n"
                + "    <Name>test name5</Name>\n" + "</ProductFamily>"));
               
        allRecords.add(factory.read(repository, store, "<Store>\n" + "    <Id>1</Id>\n" + "    <Name>Store #1</Name>\n"
                + "</Store>"));
        allRecords.add(factory.read(repository, product, "<Product>\n" + "    <Id>1</Id>\n" + "    <Name>Product name</Name>\n"
                + "    <ShortDescription>Short description word</ShortDescription>\n"
                + "    <LongDescription>Long description</LongDescription>\n" + "    <Price>10</Price>\n" + "    <Features>\n"
                + "        <Sizes>\n" + "            <Size>Small</Size>\n" + "            <Size>Medium</Size>\n"
                + "            <Size>Large</Size>\n" + "        </Sizes>\n" + "        <Colors>\n"
                + "            <Color>Blue</Color>\n" + "            <Color>Red</Color>\n" + "        </Colors>\n"
                + "    </Features>\n" + "    <Status>Pending</Status>\n" + "    <Family>[2]</Family>\n"
                + "    <Supplier>[1]</Supplier>\n" + "</Product>"));
        allRecords.add(factory.read(repository, product, "<Product>\n" + "    <Id>2</Id>\n" + "    <Name>Renault car</Name>\n"
                + "    <ShortDescription>A car</ShortDescription>\n"
                + "    <LongDescription>Long description 2</LongDescription>\n" + "    <Price>10</Price>\n" + "    <Features>\n"
                + "        <Sizes>\n" + "            <Size>Large</Size>\n" + "        </Sizes>\n" + "        <Colors>\n"
                + "            <Color>Blue 2</Color>\n" + "            <Color>Blue 1</Color>\n"
                + "            <Color>Klein blue2</Color>\n" + "        </Colors>\n" + "    </Features>\n" + "    <Family/>\n"
                + "    <Status>Pending</Status>\n" + "    <Supplier>[2]</Supplier>\n" + "    <Supplier>[1]</Supplier>\n"
                + "<Stores><Store>[1]</Store></Stores></Product>"));

        allRecords.add(factory.read(repository, e1, E1_Record1));
        allRecords.add(factory.read(repository, e1, E1_Record2));
        allRecords.add(factory.read(repository, e1, E1_Record3));

        allRecords.add(factory.read(repository, e2, E2_Record1));
        allRecords.add(factory.read(repository, e2, E2_Record2));
        allRecords.add(factory.read(repository, e2, E2_Record3));
        allRecords.add(factory.read(repository, e2, E2_Record4));
        allRecords.add(factory.read(repository, e2, E2_Record5));
        allRecords.add(factory.read(repository, e2, E2_Record6));
        allRecords.add(factory.read(repository, e2, E2_Record7));
        allRecords.add(factory.read(repository, e2, E2_Record7));
        allRecords.add(factory.read(repository, e2, E2_Record7));
        allRecords
                .add(factory
                        .read(repository, manager1,
                                "<Manager1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><birthday>2014-05-01T12:00:00</birthday><id>1</id></Manager1>"));
        allRecords
                .add(factory
                        .read(repository,
                                employee1,
                                "<Employee1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>1</Id><Holiday>2014-05-16T12:00:00</Holiday><birthday>2014-05-23T12:00:00</birthday><manager>[1][2014-05-01T12:00:00]</manager></Employee1>"));
        allRecords.add(factory.read(repository, entityA,
                "<EntityA xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><IdA>100</IdA></EntityA>"));
        allRecords.add(factory.read(repository, entityB,
                "<EntityB xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><IdB>B1</IdB><A_FK>[100]</A_FK></EntityB>"));
        allRecords
                .add(factory
                        .read(repository, ContainedEntityB,
                                "<ContainedEntityB xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>B_record1</id></ContainedEntityB>"));
        allRecords
                .add(factory
                        .read(repository, ContainedEntityB,
                                "<ContainedEntityB xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>B_record2</id></ContainedEntityB>"));
        allRecords
                .add(factory
                        .read(repository, ContainedEntityB,
                                "<ContainedEntityB xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>B_record3</id></ContainedEntityB>"));
        allRecords
                .add(factory
                        .read(repository, ContainedEntityB,
                                "<ContainedEntityB xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>B_record4</id></ContainedEntityB>"));
        allRecords
                .add(factory
                        .read(repository, ContainedEntityB,
                                "<ContainedEntityB xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>B_record5</id></ContainedEntityB>"));
        allRecords.add(factory.read(repository, city,
                "<City xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Code>BJ</Code><Name>Beijing</Name></City>"));
        allRecords.add(factory.read(repository, city,
                "<City xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Code>SH</Code><Name>Shanghai</Name></City>"));
        allRecords
                .add(factory
                        .read(repository,
                                organization,
                                "<Organization xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><org_id>1</org_id><post_address><street>changan rd</street><city>[BJ]</city><country><name>cn</name><code></code></country></post_address><org_address><street>waitan rd</street><city>[SH]</city><country><name>fr</name><code>33</code></country></org_address></Organization>"));
        allRecords.add(factory.read(repository, repeatableElementsEntity, RepeatableElementsEntity_Record));
        allRecords.add(factory.read(repository, rr, RR_Record1));
        allRecords.add(factory.read(repository, rr, RR_Record2));
        allRecords.add(factory.read(repository, rr, RR_Record3));
        allRecords.add(factory.read(repository, tt, TT_Record1));
        allRecords.add(factory.read(repository, tt, TT_Record2));
        allRecords.add(factory.read(repository, tt, TT_Record3));
        allRecords.add(factory.read(repository, compte, COMPTE_Record1));
        allRecords.add(factory.read(repository, compte, COMPTE_Record2));

        allRecords.add(factory.read(repository, contexte, "<Contexte><IdContexte>111</IdContexte><name>aaa</name><name>bbb</name></Contexte>"));
        allRecords.add(factory.read(repository, contexte, "<Contexte><IdContexte>222</IdContexte><name>ccc</name></Contexte>"));
        allRecords.add(factory.read(repository, contexte, "<Contexte><IdContexte>333</IdContexte><name>ddd</name></Contexte>"));
        allRecords.add(factory.read(repository, personne, "<Personne><IdMDM>1</IdMDM><Contextes><ContexteFk>[111]</ContexteFk><ContexteFk>[222]</ContexteFk><ContexteFk>[333]</ContexteFk></Contextes></Personne>"));
        
        allRecords.add(factory.read(repository, cpo_service,"<cpo_service><id_service>111111</id_service><etat>I</etat></cpo_service>"));
        allRecords.add(factory.read(repository, cpo_service,"<cpo_service><id_service>222222</id_service><id_service_pere>[111111]</id_service_pere><etat>I</etat></cpo_service>"));
        allRecords.add(factory.read(repository, hierarchy, "<HierarchySearchItem><HierarchySearchName>test1</HierarchySearchName><Owner>administrator</Owner><Separator>-</Separator><HierarchyRelation>true</HierarchyRelation><HierarchySearchCriterias><Concept>cpo_service</Concept><View>Browse_items_cpo_service</View><LabelXpath>cpo_service/id_service</LabelXpath><FkXpath>cpo_service/id_service_pere</FkXpath></HierarchySearchCriterias><HierarchySearchCriterias><Concept>cpo_service</Concept><View>Browse_items_cpo_service</View><LabelXpath>cpo_service/id_service</LabelXpath></HierarchySearchCriterias></HierarchySearchItem>"));
        
        allRecords.add(factory.read(repository, location, "<Location><LocationId>t1</LocationId><name>t1</name></Location>"));
        allRecords.add(factory.read(repository, location, "<Location><LocationId>t2</LocationId><name>t2</name><translation><language>en</language><locationTranslation>Trans1</locationTranslation><src>src</src></translation><translation><language>fr</language><locationTranslation>Trans2</locationTranslation><src>src</src></translation></Location>"));
        allRecords.add(factory.read(repository, organisation, "<Organisation><OrganisationId>1</OrganisationId><locations><src>abc</src><location>[t1]</location></locations></Organisation>"));
        allRecords.add(factory.read(repository, organisation, "<Organisation><OrganisationId>1</OrganisationId><locations><src>abc</src></locations></Organisation>"));
        allRecords.add(factory.read(repository, organisation, "<Organisation><OrganisationId>2</OrganisationId><locations><src>abc</src><location>[t2]</location></locations></Organisation>"));

        allRecords.add(factory.read(repository, e_entity, "<E_Entity><E_EntityId>E_id1</E_EntityId><name>E_name1</name></E_Entity>"));
        allRecords.add(factory.read(repository, e_entity, "<E_Entity><E_EntityId>E_id2</E_EntityId><name>E_name2</name></E_Entity>"));
        allRecords.add(factory.read(repository, t_entity, "<T_Entity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><T_EntityId>T_id1</T_EntityId><T_Field xsi:type=\"T1\"><T1_Field1><A1_Field1><element><elementB>[E_id1]</elementB></element></A1_Field1><A1_Field2><element><elementB>[E_id2]</elementB></element></A1_Field2></T1_Field1></T_Field></T_Entity>"));

        try {
            storage.begin();
            storage.update(allRecords);
            storage.commit();
        } finally {
            storage.end();
        }

    }

    @Override
    public void setUp() throws Exception {
        populateData();
        super.setUp();
        userSecurity.setActive(false); // Not testing security here
    }

    @Override
    public void tearDown() throws Exception {
        try {
            storage.begin();
            {
                UserQueryBuilder qb = from(person);
                storage.delete(qb.getSelect());

                qb = from(address);
                storage.delete(qb.getSelect());

                qb = from(country);
                storage.delete(qb.getSelect());

                qb = from(e2);
                storage.delete(qb.getSelect());

                qb = from(e1);
                storage.delete(qb.getSelect());

                qb = from(employee1);
                storage.delete(qb.getSelect());

                qb = from(manager1);
                storage.delete(qb.getSelect());

                qb = from(product);
                storage.delete(qb.getSelect());
            }
            storage.commit();
        } finally {
            storage.end();
        }
    }

    public void testXmlSerialization() {
        UserQueryBuilder qb = from(person).where(eq(person.getField("id"), "1"));

        StorageResults results = storage.fetch(qb.getSelect());
        DataRecordXmlWriter writer = new DataRecordXmlWriter();
        try {
            String expectedXml = "<Person><id>1</id><firstname>Julien</firstname><middlename>John</middlename><lastname>"
                    + "Dupond</lastname><resume>[EN:my splendid resume, splendid isn&apos;t it][FR:mon magnifique resume, n&apos;est ce pas ?]</resume>"
                    + "<age>10</age><score>130000.00</score><Available>true</Available><addresses><address>[2&amp;2][true]</address><address>"
                    + "[1][false]</address></addresses><Status>Employee</Status></Person>";
            String expectedXml2 = "<Person><id>1</id><firstname>Julien</firstname><middlename>John</middlename><lastname>"
                    + "Dupond</lastname><resume>[EN:my splendid resume, splendid isn&apos;t it][FR:mon magnifique resume, n&apos;est ce pas ?]</resume>"
                    + "<age>10</age><score>130000</score><Available>true</Available><addresses><address>[2&amp;2][true]</address><address>"
                    + "[1][false]</address></addresses><Status>Employee</Status></Person>";
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (DataRecord result : results) {
                try {
                    writer.write(result, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String actual = new String(output.toByteArray());
            if (!"Oracle".equalsIgnoreCase(DATABASE)) {
                assertEquals(expectedXml, actual);
            } else {
                assertEquals(expectedXml2, actual);
            }
        } finally {
            results.close();
        }

    }

    public void testXmlSerializationDefaultFKType() {
        UserQueryBuilder qb = from(a).where(eq(a.getField("id"), "3"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        DataRecordXmlWriter writer = new DataRecordXmlWriter();
        try {
            String expectedXml = "<A><id>3</id><textA>TextA</textA><refB>[1]</refB><nestedB xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"Nested\"><text>Text</text></nestedB></A>";
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (DataRecord result : results) {
                try {
                    writer.write(result, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String actual = new String(output.toByteArray());
            assertEquals(expectedXml, actual);
        } finally {
            results.close();
        }

    }

    public void testXmlSerializationSubtypeFKType() {
        // link through child D: <id>4</id><refB tmdm:type=\"D\">[2]</refB>
        UserQueryBuilder qb = from(a).where(eq(a.getField("id"), "4"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        DataRecordXmlWriter writer = new DataRecordXmlWriter();
        try {
            String expectedXml = "<A><id>4</id><textA>TextA</textA><refB xmlns:tmdm=\"http://www.talend.com/mdm\" tmdm:type=\"D\">[2]</refB><nestedB xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"Nested\"><text>Text</text></nestedB></A>";
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (DataRecord result : results) {
                try {
                    writer.write(result, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String actual = new String(output.toByteArray());
            assertEquals(expectedXml, actual);
        } finally {
            results.close();
        }
        // link through parent B: <id>4</id><refB tmdm:type=\"B\">[2]</refB>
        qb = from(a).where(eq(a.getField("id"), "5"));
        results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        writer = new DataRecordXmlWriter();
        try {
            String expectedXml = "<A><id>5</id><textA>TextA</textA><refB xmlns:tmdm=\"http://www.talend.com/mdm\" tmdm:type=\"D\">[2]</refB><nestedB xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"Nested\"><text>Text</text></nestedB></A>";
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (DataRecord result : results) {
                try {
                    writer.write(result, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            String actual = new String(output.toByteArray());
            assertEquals(expectedXml, actual);
        } finally {
            results.close();
        }
    }

    public void testSelectWithUselessIsa() throws Exception {
        UserQueryBuilder qb = from(person).isa(person);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSelectByGroupOfValues() throws Exception {
        UserQueryBuilder qb = from(person).where(eq(person.getField("lastname"), "Dupond", "Dupont"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result);
                assertTrue("Dupond".equals(result.get("lastname")) || "Dupont".equals(result.get("lastname")));
            }
        } finally {
            results.close();
        }
    }

    public void testSelectByGroupOfEmptyValues() throws Exception {
        try {
            from(a).where(eq(a.getField("nestedB/text"), new String[0]));
            fail("Expects an exception: 'no value' is not accepted");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            from(a).where(eq(a.getField("nestedB/text")));
            fail("Expects an exception: 'no value' is not accepted");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            from(a).where(eq(a.getField("nestedB/text"), (String[]) null));
            fail("Expects an exception: null value is not accepted");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testSelectByGroupOfValuesOnCollection() throws Exception {
        UserQueryBuilder qb = from(product).where(eq(product.getField("Features/Sizes/Size"), "Medium", "Large"));
        try {
            storage.fetch(qb.getSelect());
            fail("Expected an exception (not supported operation).");
        } catch (Exception e) {
            // Expected: Do not support collection search criteria with multiple values.
        }
    }

    public void testSelectByGroupOfValuesOnNested() throws Exception {
        UserQueryBuilder qb = from(a).where(eq(a.getField("nestedB/text"), "Text1", "Text2"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result);
                assertTrue("Text1".equals(result.get("nestedB/text")) || "Text2".equals(result.get("nestedB/text")));
            }
        } finally {
            results.close();
        }
    }

    public void testSelectId() throws Exception {
        Collection<FieldMetadata> keyFields = person.getKeyFields();
        assertEquals(1, keyFields.size());
        FieldMetadata keyField = keyFields.iterator().next();

        UserQueryBuilder qb = from(person).select(person.getField("id"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get(keyField));
            }
        } finally {
            results.close();
        }
    }

    public void testSelectByGroupOfIds() throws Exception {
        Collection<FieldMetadata> keyFields = person.getKeyFields();
        assertEquals(1, keyFields.size());
        FieldMetadata keyField = keyFields.iterator().next();
        UserQueryBuilder qb = from(person).where(eq(person.getField("id"), "1", "2"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get(keyField));
            }
        } finally {
            results.close();
        }
    }

    public void testSelectById() throws Exception {
        Collection<FieldMetadata> keyFields = person.getKeyFields();
        assertEquals(1, keyFields.size());
        FieldMetadata keyField = keyFields.iterator().next();

        UserQueryBuilder qb = from(person).where(eq(person.getField("id"), "1"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get(keyField));
            }
        } finally {
            results.close();
        }
    }

    public void testSelectByIdIncludingDots() throws Exception {
        Collection<FieldMetadata> keyFields = supplier.getKeyFields();
        assertEquals(1, keyFields.size());
        FieldMetadata keyField = keyFields.iterator().next();

        UserQueryBuilder qb = from(supplier).where(eq(supplier.getField("Id"), "127.0.0.1"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get(keyField));
            }
        } finally {
            results.close();
        }
        // Wrapper test
        StorageWrapper wrapper = new StorageWrapper() {

            @Override
            protected Storage getStorage(String dataClusterName) {
                return storage;
            }

        };
        // Get document by id
        String documentAsString = wrapper.getDocumentAsString("Test", "Test.Supplier.127.0.0.1");
        assertNotNull(documentAsString);
        // Get cluster ids
        String[] ids = wrapper.getAllDocumentsUniqueID("Test");
        boolean found = false;
        for (String id : ids) {
            if ("Test.Supplier.127.0.0.1".equals(id)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        // Delete document
        long result = wrapper.deleteDocument("Test", "Test.Supplier.127.0.0.1", "");
        assertTrue(result >= 0);
        wrapper.getAllDocumentsUniqueID("Test");
    }

    public void testSelectByIdIncludingDots2() throws Exception {
        Collection<FieldMetadata> keyFields = supplier.getKeyFields();
        assertEquals(1, keyFields.size());
        FieldMetadata keyField = keyFields.iterator().next();

        UserQueryBuilder qb = from(supplier).where(eq(supplier.getField("Id"), ".127"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get(keyField));
            }
        } finally {
            results.close();
        }
        // Wrapper test
        StorageWrapper wrapper = new StorageWrapper() {

            @Override
            protected Storage getStorage(String dataClusterName) {
                return storage;
            }

        };
        // Get document by id
        String documentAsString = wrapper.getDocumentAsString("Test", "Test.Supplier..127");
        assertNotNull(documentAsString);
        // Get cluster ids
        String[] ids = wrapper.getAllDocumentsUniqueID("Test");
        boolean found = false;
        for (String id : ids) {
            if ("Test.Supplier..127".equals(id)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        // Delete document
        long result = wrapper.deleteDocument("Test", "Test.Supplier..127", "");
        assertTrue(result >= 0);
        wrapper.getAllDocumentsUniqueID("Test");
    }

    public void testSelectByIdIncludingDots3() throws Exception {
        Collection<FieldMetadata> keyFields = supplier.getKeyFields();
        assertEquals(1, keyFields.size());
        FieldMetadata keyField = keyFields.iterator().next();

        UserQueryBuilder qb = from(supplier).where(eq(supplier.getField("Id"), "127."));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get(keyField));
            }
        } finally {
            results.close();
        }
        // Wrapper test
        StorageWrapper wrapper = new StorageWrapper() {

            @Override
            protected Storage getStorage(String dataClusterName) {
                return storage;
            }

        };
        // Get document by id
        String documentAsString = wrapper.getDocumentAsString("Test", "Test.Supplier.127.");
        assertNotNull(documentAsString);
        // Get cluster ids
        String[] ids = wrapper.getAllDocumentsUniqueID("Test");
        boolean found = false;
        for (String id : ids) {
            if ("Test.Supplier.127.".equals(id)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        // Delete document
        long result = wrapper.deleteDocument("Test", "Test.Supplier.127.", "");
        assertTrue(result >= 0);
        wrapper.getAllDocumentsUniqueID("Test");
    }

    public void testSelectByIdExclusion() throws Exception {
        Collection<FieldMetadata> keyFields = person.getKeyFields();
        assertEquals(1, keyFields.size());
        FieldMetadata keyField = keyFields.iterator().next();

        UserQueryBuilder qb = from(person).where(not(eq(person.getField("id"), "1")));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getSize());
            assertEquals(3, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get(keyField));
            }
        } finally {
            results.close();
        }
    }

    public void testSelectByIdWithProjection() throws Exception {
        Collection<FieldMetadata> keyFields = person.getKeyFields();
        assertEquals(1, keyFields.size());
        FieldMetadata keyField = keyFields.iterator().next();

        UserQueryBuilder qb = from(person).select(person.getField("id")).where(eq(person.getField("id"), "1"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get(keyField));
            }
        } finally {
            results.close();
        }
    }

    public void testOrderByCompositeKey() throws Exception {
        // Test ASC direction
        FieldMetadata personLastName = person.getField("lastname");
        FieldMetadata personId = person.getField("id");
        UserQueryBuilder qb = from(person).orderBy(personLastName, OrderBy.Direction.ASC).orderBy(personId,
                OrderBy.Direction.DESC);
        String[] ascExpectedValues = { "Dupond", "Dupont", "Leblanc", "Leblanc" };

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());
            int i = 0;
            for (DataRecord result : results) {
                assertEquals(ascExpectedValues[i++], result.get(personLastName));
            }
        } finally {
            results.close();
        }
        //
        qb = from(address).selectId(address);
        List<TypedExpression> sortFields = UserQueryHelper.getFields(address, "../../i");
        for (TypedExpression sortField : sortFields) {
            qb.orderBy(sortField, OrderBy.Direction.DESC);
        }

        StorageResults storageResults = storage.fetch(qb.getSelect());
        String[] expected = { "4", "3", "2&2", "1", "1" };
        int i = 0;
        for (DataRecord result : storageResults) {
            assertEquals(expected[i++], result.get("id"));
        }
    }
    
    public void testOrderByCompoundField() throws Exception {
        FieldMetadata code = compte.getField("Code");
        String[] ascExpectedValues = { "1", "11" };
        UserQueryBuilder qb = from(compte).select(compte.getField("Code")).select(compte.getField("Label"))
                .select(compte.getField("childOf")).orderBy(compte.getField("childOf"), OrderBy.Direction.ASC);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
            int i = 0;
            for (DataRecord result : results) {
                assertEquals(ascExpectedValues[i++], result.get(code));
            }
        } finally {
            results.close();
        }

        String[] descExpectedValues = { "11", "1" };
        qb = from(compte).select(compte.getField("Code")).select(compte.getField("Label")).select(compte.getField("childOf"))
                .orderBy(compte.getField("childOf"), OrderBy.Direction.DESC);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
            int i = 0;
            for (DataRecord result : results) {
                assertEquals(descExpectedValues[i++], result.get(code));
            }
        } finally {
            results.close();
        }

    }

    public void testOrderByPK() throws Exception {
        // Test ASC direction
        FieldMetadata personLastName = person.getField("lastname");
        UserQueryBuilder qb = from(person).select(personLastName).orderBy(person.getField("id"), OrderBy.Direction.ASC);
        String[] ascExpectedValues = { "Dupond", "Dupont", "Leblanc", "Leblanc" };

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());

            int i = 0;
            for (DataRecord result : results) {
                assertEquals(ascExpectedValues[i++], result.get(personLastName));
            }

        } finally {
            results.close();
        }
        // Test normalize
        qb = from(person).select(personLastName).orderBy(person.getField("id"), OrderBy.Direction.ASC)
                .orderBy(person.getField("id"), OrderBy.Direction.ASC);
        assertEquals(1, ((Select) qb.getSelect().normalize()).getOrderBy().size());
        qb = from(person).select(personLastName).orderBy(person.getField("id"), OrderBy.Direction.ASC)
                .orderBy(person.getField("id"), OrderBy.Direction.DESC);
        assertEquals(2, ((Select) qb.getSelect().normalize()).getOrderBy().size());
        qb = from(person).select(personLastName).orderBy(person.getField("id"), OrderBy.Direction.ASC)
                .orderBy(person.getField("lastname"), OrderBy.Direction.ASC)
                .orderBy(person.getField("lastname"), OrderBy.Direction.ASC);
        assertEquals(2, ((Select) qb.getSelect().normalize()).getOrderBy().size());
    }

    public void testOrderByASC() throws Exception {
        // Test ASC direction
        FieldMetadata personLastName = person.getField("lastname");
        UserQueryBuilder qb = from(person).orderBy(personLastName, OrderBy.Direction.ASC);
        String[] ascExpectedValues = { "Dupond", "Dupont", "Leblanc", "Leblanc" };

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());

            int i = 0;
            for (DataRecord result : results) {
                assertEquals(ascExpectedValues[i++], result.get(personLastName));
            }

        } finally {
            results.close();
        }
    }

    public void testOrderByDESC() throws Exception {
        FieldMetadata personLastName = person.getField("lastname");
        UserQueryBuilder qb = from(person).orderBy(personLastName, OrderBy.Direction.DESC);
        String[] descExpectedValues = { "Leblanc", "Leblanc", "Dupont", "Dupond" };

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());

            int i = 0;
            for (DataRecord result : results) {
                assertEquals(descExpectedValues[i++], result.get(personLastName));
            }

        } finally {
            results.close();
        }
    }

    public void testNoConditionQuery() throws Exception {
        UserQueryBuilder qb = from(person);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }

        qb = from(address);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(5, results.getSize());
            assertEquals(5, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testEqualsCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(eq(person.getField("lastname"), "Dupond"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(address).where(eq(address.getField("Street"), "Street1"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }

        qb = from(address).where(eq(address.getField("Street"), (String) null));
        assertEquals(IsNull.class, qb.getSelect().getCondition().getClass());
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        
        qb = from(organisation).where(eq(organisation.getField("locations/location"), (String) null));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

    }

    public void testEqualsDateCondition() throws Exception {
        UserQueryBuilder qb = from(country).where(eq(country.getField("creationDate"), "2010-10-10"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testEqualsTimeCondition() throws Exception {
        UserQueryBuilder qb = from(country).where(eq(country.getField("creationTime"), "2010-10-10T00:00:01"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testEqualsBooleanCondition() throws Exception {
        UserQueryBuilder qb = from(address).where(eq(address.getField("enterprise"), "true"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }

        qb = from(address).where(eq(address.getField("enterprise"), "false"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getSize());
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testNotEqualsCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(neq(person.getField("lastname"), "Dupond"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getSize());
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testGreaterThanCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(gt(person.getField("age"), "10"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getSize());
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testGreaterThanDateCondition() throws Exception {
        UserQueryBuilder qb = from(country).where(gt(country.getField("creationDate"), "2000-01-01"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testGreaterThanTimeCondition() throws Exception {
        UserQueryBuilder qb = from(country).where(gt(country.getField("creationTime"), "2000-01-01T00:00:00"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testGreaterThanDecimalCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(gt(person.getField("score"), "100000"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testLessThanCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(lt(person.getField("age"), "20"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testLessThanDateCondition() throws Exception {
        UserQueryBuilder qb = from(country).where(lt(country.getField("creationDate"), "2020-01-01"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testLessThanTimeCondition() throws Exception {
        UserQueryBuilder qb = from(country).where(lt(country.getField("creationTime"), "2020-01-01T00:00:00"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testLessThanDecimalCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(lt(person.getField("score"), "1000000"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testGreaterThanEqualsCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(gte(person.getField("age"), "10"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testIntervalCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(gte(person.getField("age"), "10")).where(lte(person.getField("age"), "30"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testGreaterThanEqualsDateCondition() throws Exception {
        UserQueryBuilder qb = from(country).where(gte(country.getField("creationDate"), "2011-10-10"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testGreaterThanEqualsTimeCondition() throws Exception {
        UserQueryBuilder qb = from(country).where(gte(country.getField("creationTime"), "2011-10-10T00:00:00"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testGreaterThanEqualsDecimalCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(gte(person.getField("score"), "170000"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getSize());
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testLessThanEqualsCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(lte(person.getField("age"), "20"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testLessThanEqualsDateCondition() throws Exception {
        UserQueryBuilder qb = from(country).where(lte(country.getField("creationDate"), "2010-10-10"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testLessThanEqualsTimeCondition() throws Exception {
        UserQueryBuilder qb = from(country).where(lte(country.getField("creationTime"), "2010-10-10T00:00:01"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testLessThanEqualsDecimalCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(lte(person.getField("score"), "170000"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testStartsWithCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(startsWith(person.getField("firstname"), "Ju"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getSize());
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }

        qb = from(person).where(startsWith(person.getField("firstname"), "^Ju"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getSize());
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testContainsCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(contains(person.getField("lastname"), "Dupo"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }

        qb = from(address).where(contains(address.getField("Street"), "Street"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(5, results.getSize());
            assertEquals(5, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testContainsConditionWithAllSimpledTypeFields() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(product);
        String fieldName = "Product/../*";
        IWhereItem item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.CONTAINS, "1",
                WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        StorageResults storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
        }
        // Test correct translation when there's only one actual condition
        qb = UserQueryBuilder.from(product);
        fieldName = "Product/../*";
        item = new WhereOr(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.CONTAINS_TEXT_OF, "1",
                WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
        }
    }

    public void testContainsTextOfConditionWithAllSimpledTypeFields() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(product);
        String fieldName = "Product/../*";
        IWhereItem item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.CONTAINS_TEXT_OF,
                "1", WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        StorageResults storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
        }
        // Test correct translation when there's only one actual condition
        qb = UserQueryBuilder.from(product);
        fieldName = "Product/../*";
        item = new WhereOr(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.CONTAINS_TEXT_OF, "1",
                WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
        }
    }

    public void testConditionOr() throws Exception {
        UserQueryBuilder qb = from(person).where(
                or(eq(person.getField("lastname"), "Dupond"), eq(person.getField("firstname"), "Robert-Julien")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testConditionAnd() throws Exception {
        UserQueryBuilder qb = from(person).where(
                and(eq(person.getField("lastname"), "Dupond"), eq(person.getField("firstname"), "Robert-Damien")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getSize());
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }

        // Wheres are equivalent to "and" statements
        qb = from(person).where(eq(person.getField("lastname"), "Dupond")).where(
                eq(person.getField("firstname"), "Robert-Damien"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getSize());
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testConditionNot() throws Exception {
        UserQueryBuilder qb = from(person).where(
                and(eq(person.getField("lastname"), "Dupond"), not(eq(person.getField("firstname"), "Robert"))));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        // Equivalent to the previous query (chained wheres are "and")
        qb = from(person).where(eq(person.getField("lastname"), "Dupond")).where(not(eq(person.getField("firstname"), "Robert")));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testJoinQuery() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .join(person.getField("addresses/address"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(6, results.getSize());
            assertEquals(6, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testJoinQueryWithId() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .where(and(eq(person.getField("id"), "1"), UserQueryHelper.TRUE)).join(person.getField("addresses/address"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }

        qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .where(and(UserQueryHelper.TRUE, eq(person.getField("id"), "1"))).join(person.getField("addresses/address"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testJoinQueryNormalize() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .where(and(eq(person.getField("id"), "1"), UserQueryHelper.TRUE)).join(person.getField("addresses/address"));
        Select select = qb.getSelect();
        assertTrue(select.getCondition() instanceof BinaryLogicOperator);
        Select normalizedSelect = (Select) select.normalize(); // Binary condition can be simplified because right is
                                                               // TRUE
        assertTrue(normalizedSelect.getCondition() instanceof Compare);

        qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .where(and(UserQueryHelper.TRUE, eq(person.getField("id"), "1"))).join(person.getField("addresses/address"));
        select = qb.getSelect();
        assertTrue(select.getCondition() instanceof BinaryLogicOperator);
        normalizedSelect = (Select) select.normalize(); // Binary condition can be simplified because right is
                                                        // TRUE
        assertTrue(normalizedSelect.getCondition() instanceof Compare);
    }

    public void testJoinQueryUsingSingleParameterJoin() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .join(person.getField("addresses/address"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(6, results.getSize());
            assertEquals(6, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testJoinQueryWithCondition() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .join(person.getField("addresses/address")).where(eq(person.getField("lastname"), "Dupond"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testJoinQueryWithConditionAnd() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .join(person.getField("addresses/address")).where(eq(person.getField("lastname"), "Dupond"))
                .where(eq(person.getField("firstname"), "Julien"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testJoinQueryWithConditionNot() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .join(person.getField("addresses/address"))
                .where(and(eq(person.getField("lastname"), "Dupond"), not(eq(person.getField("firstname"), "Julien"))));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getSize());
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testDoubleJoinQuery() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .select(country.getField("name")).join(person.getField("addresses/address"))
                .join(address.getField("country"), country.getField("id"));
        StorageResults results = storage.fetch(qb.getSelect());

        try {
            assertEquals(6, results.getSize());
            assertEquals(6, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testDoubleJoinQueryWithCondition() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).select(address.getField("Street"))
                .select(country.getField("name")).join(person.getField("addresses/address"))
                .join(address.getField("country"), country.getField("id")).where(eq(person.getField("lastname"), "Dupond"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testPaging() throws Exception {
        UserQueryBuilder qb = from(person).limit(1);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(4, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("id"));
            }
        } finally {
            results.close();
        }
        qb = from(person).limit(-1);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(4, actualCount);
        } finally {
            results.close();
        }
        qb = from(person);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());
            int actualCount = 0;
            for (DataRecord result : results) {
                actualCount++;
            }
            assertEquals(4, actualCount);
        } finally {
            results.close();
        }
        qb = from(person).limit(1).start(1);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
        qb = from(person).limit(1).start(4);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
            assertEquals(4, results.getCount());
            assertFalse(results.iterator().hasNext());
        } finally {
            results.close();
        }
        qb = from(person).selectId(person).limit(-1);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getSize());
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testPagingWithOuterJoin() throws Exception {
        UserQueryBuilder qb = from(product).start(0).limit(2);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            assertEquals(2, results.getCount());
            int iteratorCount = 0;
            for (DataRecord result : results) {
                assertNotNull(result.get("Id"));
                iteratorCount++;
            }
            assertEquals(results.getSize(), iteratorCount);
        } finally {
            results.close();
        }
    }

    public void testEnumeration() throws Exception {
        UserQueryBuilder qb = from(person).where(eq(person.getField("Status"), "Friend"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testTimestamp() throws Exception {
        UserQueryBuilder qb = from(person).where(eq(person.getField("id"), "3"));
        StorageResults results = storage.fetch(qb.getSelect());

        long lastModificationTime1;
        try {
            assertEquals(1, results.getCount());
            Iterator<DataRecord> iterator = results.iterator();
            assertTrue(iterator.hasNext());
            DataRecord result = iterator.next();
            assertNotNull(result);
            DataRecordMetadata recordMetadata = result.getRecordMetadata();
            assertNotNull(recordMetadata);
            lastModificationTime1 = recordMetadata.getLastModificationTime();
            assertNotSame("0", lastModificationTime1);
        } finally {
            results.close();
        }

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        DataRecord record = factory
                .read(repository,
                        person,
                        "<Person><id>3</id><score>200000</score><lastname>Leblanc</lastname><middlename>John</middlename><firstname>Juste</firstname><addresses><address>[3][false]</address><address>[1][false]</address></addresses><age>30</age><Status>Friend</Status></Person>");
        try {
            storage.begin();
            storage.update(record);
            storage.commit();
        } finally {
            storage.end();
        }

        qb = from(person).where(eq(person.getField("id"), "3"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        long lastModificationTime2;
        try {
            assertEquals(1, results.getCount());
            Iterator<DataRecord> iterator = results.iterator();
            assertTrue(iterator.hasNext());
            DataRecord result = iterator.next();
            assertNotNull(result);
            DataRecordMetadata recordMetadata = result.getRecordMetadata();
            assertNotNull(recordMetadata);
            lastModificationTime2 = recordMetadata.getLastModificationTime();
            assertNotSame("0", lastModificationTime2);
        } finally {
            results.close();
            storage.commit();
        }

        // Now the actual timestamp test
        assertNotSame(lastModificationTime1, lastModificationTime2);
    }

    public void testAliases() throws Exception {
        long endTime = System.currentTimeMillis() + 60000;

        UserQueryBuilder qb = from(person).select(alias(timestamp(), "timestamp")).select(alias(taskId(), "taskid"))
                .selectId(person).where(gte(timestamp(), "0")).where(lte(timestamp(), String.valueOf(endTime))).limit(20)
                .start(0);

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
            for (DataRecord result : results) {
                assertNotNull(result.get("timestamp"));
                assertNull(result.get("taskid"));
            }
        } finally {
            results.close();
        }
    }

    public void testFKSearchWithIncompatibleValue() throws Exception {
        UserQueryBuilder qb = from(address).selectId(address).select(address.getField("country"))
                .where(contains(address.getField("country"), "aaaa")); // Id to country is integer
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }

        qb = from(address).selectId(address).select(address.getField("country"))
                .where(or(contains(address.getField("country"), "aaaa"), eq(address.getField("id"), "1")));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testFKSearchWithIncompatibleValueAndNot() throws Exception {
        UserQueryBuilder qb = from(address).selectId(address).select(address.getField("country"))
                .where(not(contains(address.getField("country"), "aaaa"))); // Id to country is integer
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(5, results.getCount());
        } finally {
            results.close();
        }

        qb = from(address).selectId(address).select(address.getField("country"))
                .where(or(not(contains(address.getField("country"), "aaaa")), eq(address.getField("id"), "1")));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(5, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testFKSearch() throws Exception {
        UserQueryBuilder qb = from(address).selectId(address).select(address.getField("country"))
                .where(eq(address.getField("country"), "[1]"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testFKOrderBy() throws Exception {
        UserQueryBuilder qb = from(address).selectId(address).select(address.getField("country"))
                .orderBy(address.getField("country"), OrderBy.Direction.ASC);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(5, results.getCount());
            int previousValue = -1;
            for (DataRecord result : results) {
                int newValue = ((Integer) result.get(address.getField("country")));
                assertTrue(previousValue <= newValue);
                previousValue = newValue;
            }
        } finally {
            results.close();
        }

        qb = from(address).selectId(address).select(address.getField("country"))
                .orderBy(address.getField("country"), OrderBy.Direction.DESC);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(5, results.getCount());
            int previousValue = Integer.MAX_VALUE;
            for (DataRecord result : results) {
                int newValue = ((Integer) result.get(address.getField("country")));
                assertTrue(previousValue >= newValue);
                previousValue = newValue;
            }
        } finally {
            results.close();
        }
    }

    public void testFKOrderByIncludingNull() throws Exception {
        UserQueryBuilder qb = from(product).selectId(product).select(product.getField("Family"))
                .orderBy(product.getField("Family"), OrderBy.Direction.ASC);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            int i = 0;
            String[] expected = new String[] { null, "2" };
            for (DataRecord result : results) {
                String value = ((String) result.get(product.getField("Family")));
                assertEquals(expected[i++], value);
            }
        } finally {
            results.close();
        }

        qb = from(product).selectId(product).select(product.getField("Family"))
                .orderBy(product.getField("Family"), OrderBy.Direction.DESC);
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            int i = 0;
            String[] expected = new String[] { "2", null };
            for (DataRecord result : results) {
                String value = ((String) result.get(product.getField("Family")));
                assertEquals(expected[i++], value);
            }
        } finally {
            results.close();
        }
    }

    public void testNonMandatoryFKSelection() throws Exception {
        UserQueryBuilder qb = from(product).selectId(product).select(product.getField("Name")).select(product.getField("Family"));

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            int actualIterationCount = 0;
            ViewSearchResultsWriter writer = new ViewSearchResultsWriter();
            for (DataRecord result : results) {
                assertTrue("2".equals(result.get("Family")) || result.get("Family") == null);
                actualIterationCount++;
                writer.write(result, new NullOutputStream());
            }
            assertEquals(2, actualIterationCount);
        } finally {
            results.close();
        }
    }

    public void testEmptyOrNull() throws Exception {
        UserQueryBuilder qb = from(address).selectId(address).where(emptyOrNull(address.getField("City")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }

        //
        qb = from(address).selectId(address).where(emptyOrNull(address.getField("OptionalCity")));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }

        //
        qb = from(address).selectId(address).where(not(emptyOrNull(address.getField("OptionalCity"))));
        results = storage.fetch(qb.getSelect().normalize());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testEmptyOrNullOnContainedElement() throws Exception {
        UserQueryBuilder qb = from(country).selectId(country).where(emptyOrNull(country.getField("notes/note")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testIsEmptyOrNullOnNonString() throws Exception {
        UserQueryBuilder qb = from(address).selectId(address).where(emptyOrNull(address.getField("enterprise")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }

        //
        qb = from(address).selectId(address).where(not(emptyOrNull(address.getField("enterprise"))));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(5, results.getCount());
        } finally {
            results.close();
        }

        //
        qb = from(country).selectId(country).where(emptyOrNull(country.getField("creationDate")));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testBoolean() throws Exception {
        UserQueryBuilder qb = from(person).selectId(person).where(eq(person.getField("Available"), "false"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testDate() throws Exception {
        UserQueryBuilder qb = from(country).where(lte(country.getField("creationTime"), "2010-10-10T00:00:01"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());

            ViewSearchResultsWriter writer = new ViewSearchResultsWriter();
            StringWriter resultWriter = new StringWriter();
            for (DataRecord result : results) {
                writer.write(result, resultWriter);
            }
            assertEquals("<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + "\t<id>1</id>\n"
                    + "\t<creationDate>2010-10-10</creationDate>\n" + "\t<creationTime>2010-10-10T00:00:01</creationTime>\n"
                    + "\t<name>France</name>\n" + "</result>", resultWriter.toString());
        } finally {
            results.close();
        }

    }

    public void testInterFieldCondition1() throws Exception {
        UserQueryBuilder qb = from(person).selectId(person).where(lte(person.getField("id"), person.getField("score")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testInterFieldCondition2() throws Exception {
        UserQueryBuilder qb = from(person).where(eq(person.getField("id"), person.getField("score")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testRecursiveQuery() throws Exception {
        UserQueryBuilder qb = from(a).selectId(a).select(a.getField("refA"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            Set<Object> expectedValues = new HashSet<Object>();
            expectedValues.add(null);
            expectedValues.add("1");
            assertEquals(5, results.getCount());
            for (DataRecord result : results) {
                Object value = result.get("refA");
                boolean wasRemoved = expectedValues.remove(value);
                assertTrue(wasRemoved);
                if (value == null) {
                    expectedValues.add(null);
                }
            }
            expectedValues.remove(null);
            assertEquals(0, expectedValues.size());
        } finally {
            results.close();
        }
    }

    public void testTimeStampQuery() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(person);
        String fieldName = "Person/../../t";
        IWhereItem item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.GREATER_THAN,
                "1000", WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        Select select = qb.getSelect();
        select = (Select) select.normalize();
        Condition condition = select.getCondition();
        assertTrue(condition instanceof Compare);
        assertTrue(((Compare) condition).getLeft() instanceof Timestamp);
        // Test correct translation when there's only one actual condition
        item = new WhereOr(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.GREATER_THAN, "1000",
                WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        select = qb.getSelect();
        select = (Select) select.normalize();
        condition = select.getCondition();
        assertTrue(condition instanceof Compare);
        assertTrue(((Compare) condition).getLeft() instanceof Timestamp);
    }

    public void testContainsOnNumericField() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(address).where(contains(address.getField("ZipCode"), "10000"));
        Condition condition = qb.getSelect().getCondition();
        assertTrue(condition instanceof Compare);
        assertTrue(((Compare) condition).getLeft() instanceof Field);
        assertTrue(((Compare) condition).getRight() instanceof IntegerConstant);
        assertTrue(((Compare) condition).getPredicate() == Predicate.EQUALS);

        StorageResults results = storage.fetch(qb.getSelect());
        for (DataRecord result : results) {
            assertEquals(10000, result.get("ZipCode"));
        }
    }

    public void testNonValueFieldAndQueryOnId() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(person).select(person.getField("addresses"), person.getField("id"))
                .where(eq(person.getField("id"), "1"));
        StorageResults results = storage.fetch(qb.getSelect());
        for (DataRecord result : results) {
            assertEquals("1", result.get("id"));
            assertEquals("", result.get("addresses"));
        }
    }

    public void testNonValueFieldAndQueryOnValue() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(person).select(person.getField("addresses"), person.getField("id"))
                .where(eq(person.getField("firstname"), "Juste"));
        StorageResults results = storage.fetch(qb.getSelect());
        for (DataRecord result : results) {
            assertEquals("3", result.get("id"));
            assertEquals("", result.get("addresses"));
        }
    }

    public void testRangeOnTimestamp() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(person).where(
                and(gte(timestamp(), "0"), lte(timestamp(), String.valueOf(System.currentTimeMillis()))));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testRangeOnTimestampWithCondition() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(person).where(
                or(and(gte(timestamp(), "0"), lte(timestamp(), String.valueOf(System.currentTimeMillis()))),
                        eq(person.getField("id"), "1")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }

        qb = UserQueryBuilder.from(person).where(
                and(and(gte(timestamp(), "0"), lte(timestamp(), String.valueOf(System.currentTimeMillis()))),
                        eq(person.getField("id"), "1")));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testCollectionClean() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        DataRecord productInstance = factory.read(repository, product, "<Product>\n" + "    <Id>1</Id>\n"
                + "    <Name>Product name</Name>\n" + "    <ShortDescription>Short description word</ShortDescription>\n"
                + "    <LongDescription>Long description</LongDescription>\n" + "    <Price>10</Price>\n" + "    <Features>\n"
                + "        <Sizes>\n" + "            <Size>Small</Size>\n" + "            <Size>Medium</Size>\n"
                + "            <Size>Large</Size>\n" + "        </Sizes>\n" + "        <Colors>\n"
                + "            <Color>Blue</Color>\n" + "            <Color>Red</Color>\n" + "        </Colors>\n"
                + "    </Features>\n" + "    <Status>Pending</Status>\n" + "</Product>");
        try {
            storage.begin();
            storage.update(productInstance);
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = from(product).where(eq(product.getField("Id"), "1"));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                Object o = result.get("Features/Colors/Color");
                assertTrue(o instanceof List);
                assertEquals(2, ((List) o).size());
            }
        } finally {
            results.close();
            storage.commit();
        }

        productInstance = factory.read(repository, product, "<Product>\n" + "    <Id>1</Id>\n"
                + "    <Name>Product name</Name>\n" + "    <ShortDescription>Short description word</ShortDescription>\n"
                + "    <LongDescription>Long description</LongDescription>\n" + "    <Price>10</Price>\n" + "    <Features>\n"
                + "        <Sizes>\n" + "            <Size>Small</Size>\n" + "            <Size>Medium</Size>\n"
                + "            <Size>Large</Size>\n" + "        </Sizes>\n" + "        <Colors><Color/><Color/></Colors>\n"
                + "    </Features>\n" + "    <Status>Pending</Status>\n" + "</Product>");
        try {
            storage.begin();
            storage.update(productInstance);
            storage.commit();
        } finally {
            storage.end();
        }

        qb = from(product).where(eq(product.getField("Id"), "1"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                Object o = result.get("Features/Colors/Color");
                assertTrue(o instanceof List);
                assertEquals(0, ((List) o).size());
            }
        } finally {
            results.close();
            storage.commit();
        }

        productInstance = factory.read(repository, product, "<Product>\n" + "    <Id>1</Id>\n"
                + "    <Name>Product name</Name>\n" + "    <ShortDescription>Short description word</ShortDescription>\n"
                + "    <LongDescription>Long description</LongDescription>\n" + "    <Price>10</Price>\n" + "    <Features>\n"
                + "        <Sizes>\n" + "            <Size>Small</Size>\n" + "            <Size>Medium</Size>\n"
                + "            <Size>Large</Size>\n" + "        </Sizes>\n" + "        <Colors>"
                + "            <Color>Blue</Color>\n" + "            <Color>Red</Color>\n" + "        </Colors>\n"
                + "    </Features>\n" + "    <Status>Pending</Status>\n" + "</Product>");
        try {
            storage.begin();
            storage.update(productInstance);
            storage.commit();
        } finally {
            storage.end();
        }

        qb = from(product).where(eq(product.getField("Id"), "1"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                Object o = result.get("Features/Colors/Color");
                assertTrue(o instanceof List);
                assertEquals(2, ((List) o).size());
            }
        } finally {
            results.close();
            storage.commit();
        }
    }

    public void testUpdateReportCreation() throws Exception {
        StringBuilder builder = new StringBuilder();
        InputStream testResource = this.getClass().getResourceAsStream("StorageQueryTest_1.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(testResource));
        String current;
        while ((current = reader.readLine()) != null) {
            builder.append(current);
        }

        DataRecordReader<String> dataRecordReader = new XmlStringDataRecordReader();
        DataRecord report = dataRecordReader.read(repository, updateReport, builder.toString());

        try {
            storage.begin();
            storage.update(report);
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = from(updateReport);
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        StringWriter storedDocument = new StringWriter();
        try {
            DataRecordXmlWriter writer = new DataRecordXmlWriter();
            for (DataRecord result : results) {
                writer.write(result, storedDocument);
            }
            assertEquals(builder.toString(), storedDocument.toString());
        } finally {
            results.close();
            storage.commit();
        }

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
    }

    public void testUpdateReportQueryOptimization() throws Exception {
        UpdateReportOptimizer optimizer = new UpdateReportOptimizer();

        Condition condition = and(eq(updateReport.getField("Concept"), "Product"),
                eq(updateReport.getField("DataModel"), "metadata.xsd"));
        UserQueryBuilder qb = from(updateReport).where(condition);
        assertEquals(condition, qb.getSelect().getCondition());
        optimizer.optimize(qb.getSelect());
        assertEquals(condition, qb.getSelect().getCondition());

        condition = eq(updateReport.getField("Concept"), "C");
        qb = from(updateReport).where(condition);
        assertEquals(condition, qb.getSelect().getCondition());
        optimizer.optimize(qb.getSelect());
        assertEquals(condition, qb.getSelect().getCondition()); // No data model: no optimization can be done.

        condition = and(eq(updateReport.getField("Concept"), "C"), eq(updateReport.getField("DataModel"), "metadata.xsd"));
        qb = from(updateReport).where(condition);
        assertEquals(condition, qb.getSelect().getCondition());
        optimizer.optimize(qb.getSelect());
        assertNotSame(condition, qb.getSelect().getCondition()); // C has super type, so condition changed.

        condition = and(eq(updateReport.getField("Concept"), "C"),
                and(eq(updateReport.getField("DataModel"), "metadata.xsd"), eq(updateReport.getField("TimeInMillis"), "0")));
        qb = from(updateReport).where(condition);
        assertEquals(condition, qb.getSelect().getCondition());
        optimizer.optimize(qb.getSelect());
        assertNotSame(condition, qb.getSelect().getCondition()); // C has super type, so condition changed.
    }

    public void testUpdateReportCreationWithoutSource() throws Exception {
        StringBuilder builder = new StringBuilder();
        InputStream testResource = this.getClass().getResourceAsStream("StorageQueryTest_2.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(testResource));
        String current;
        while ((current = reader.readLine()) != null) {
            builder.append(current);
        }

        DataRecordReader<String> dataRecordReader = new XmlStringDataRecordReader();
        DataRecord report = dataRecordReader.read(repository, updateReport, builder.toString());

        try {
            storage.begin();
            assertNull(report.get("Source"));
            storage.update(report);
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = from(updateReport);
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        StringWriter storedDocument = new StringWriter();
        try {
            DataRecordXmlWriter writer = new DataRecordXmlWriter();
            for (DataRecord result : results) {
                writer.write(result, storedDocument);
                assertEquals("none", result.get("Source"));
            }
            assertNotSame(builder.toString(), storedDocument.toString());
        } finally {
            results.close();
            storage.commit();
        }

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
    }

    public void testUpdateReportTimeStampQuery() throws Exception {
        StringBuilder builder = new StringBuilder();
        InputStream testResource = this.getClass().getResourceAsStream("StorageQueryTest_1.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(testResource));
        String current;
        while ((current = reader.readLine()) != null) {
            builder.append(current);
        }

        DataRecordReader<String> dataRecordReader = new XmlStringDataRecordReader();
        DataRecord report = dataRecordReader.read(repository, updateReport, builder.toString());
        try {
            storage.begin();
            storage.update(report);
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = from(updateReport).where(gt(timestamp(), "0"));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
            storage.commit();
        }

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
    }

    public void testUpdateReportContentKeyWordsQuery() throws Exception {
        StringBuilder builder = new StringBuilder();
        InputStream testResource = this.getClass().getResourceAsStream("StorageQueryTest_1.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(testResource));
        String current;
        while ((current = reader.readLine()) != null) {
            builder.append(current);
        }

        DataRecordReader<String> dataRecordReader = new XmlStringDataRecordReader();
        DataRecord report = dataRecordReader.read(repository, updateReport, builder.toString());
        try {
            storage.begin();
            storage.update(report);
            storage.commit();
        } finally {
            storage.end();
        }

        // build query condition
        ItemPKCriteria criteria = new ItemPKCriteria();
        criteria.setClusterName("UpdateReport");
        criteria.setContentKeywords("Product");
        String contentKeywords = criteria.getContentKeywords();
        // build Storage whereCondition, the codes come from
        // com.amalto.core.storage.StorageWrapper.buildQueryBuilder(UserQueryBuilder, ItemPKCriteria,
        // ComplexTypeMetadata)
        Condition condition = null;
        UserQueryBuilder qb = from(updateReport);
        for (FieldMetadata field : updateReport.getFields()) {
            if (StorageMetadataUtils.isValueAssignable(contentKeywords, field)) {
                if (!(field instanceof ContainedTypeFieldMetadata)) {
                    if (condition == null) {
                        condition = contains(field, contentKeywords);
                    } else {
                        condition = or(condition, contains(field, contentKeywords));
                    }
                }
            }
        }
        qb.where(condition);
        assertEquals(condition, qb.getSelect().getCondition());
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
            storage.commit();
        }

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
    }

    public void testUpdateReportTimeInMillisQuery() throws Exception {
        StringBuilder builder = new StringBuilder();
        InputStream testResource = this.getClass().getResourceAsStream("StorageQueryTest_1.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(testResource));
        String current;
        while ((current = reader.readLine()) != null) {
            builder.append(current);
        }

        DataRecordReader<String> dataRecordReader = new XmlStringDataRecordReader();
        DataRecord report = dataRecordReader.read(repository, updateReport, builder.toString());
        try {
            storage.begin();
            storage.update(report);
            storage.commit();
        } finally {
            storage.end();
        }
        // Test max on TimeInMillis
        storage.begin();
        UserQueryBuilder qb = UserQueryBuilder.from(updateReport).select(max(updateReport.getField("TimeInMillis"))).limit(1);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertNotNull(result.get("max"));
            }
        } finally {
            results.close();
            storage.commit();
        }
        // build query condition
        ItemPKCriteria criteria = new ItemPKCriteria();
        criteria.setClusterName("UpdateReport");
        criteria.setContentKeywords("1307525701796");
        String contentKeywords = criteria.getContentKeywords();
        // build Storage whereCondition, the codes come from
        // com.amalto.core.storage.StorageWrapper.buildQueryBuilder(UserQueryBuilder, ItemPKCriteria,
        // ComplexTypeMetadata)
        Condition condition = null;
        qb = from(updateReport);
        for (FieldMetadata field : updateReport.getFields()) {
            if (StorageMetadataUtils.isValueAssignable(contentKeywords, field)) {
                if (!(field instanceof ContainedTypeFieldMetadata)) {
                    if (condition == null) {
                        condition = contains(field, contentKeywords);
                    } else {
                        condition = or(condition, contains(field, contentKeywords));
                    }
                }
            }
        }
        qb.where(condition);
        assertEquals(condition, qb.getSelect().getCondition());
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
            storage.commit();
        }

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
    }

    public void testUpdateReportQueryByKeys() throws Exception {
        StringBuilder builder = new StringBuilder();
        InputStream testResource = this.getClass().getResourceAsStream("StorageQueryTest_1.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(testResource));
        String current;
        while ((current = reader.readLine()) != null) {
            builder.append(current);
        }

        DataRecordReader<String> dataRecordReader = new XmlStringDataRecordReader();
        DataRecord report = dataRecordReader.read(repository, updateReport, builder.toString());

        try {
            storage.begin();
            storage.update(report);
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = from(updateReport).where(
                and(eq(updateReport.getField("Source"), UpdateReportPOJO.GENERIC_UI_SOURCE),
                        eq(updateReport.getField("TimeInMillis"), String.valueOf(1307525701796L))));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        StringWriter storedDocument = new StringWriter();
        try {
            assertEquals(1, results.getCount());
            DataRecordXmlWriter writer = new DataRecordXmlWriter();
            for (DataRecord result : results) {
                writer.write(result, storedDocument);
            }
            assertEquals(builder.toString(), storedDocument.toString());
        } finally {
            results.close();
            storage.commit();
        }

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
    }

    public void testUpdateReportTaskIdQuery() throws Exception {
        StringBuilder builder = new StringBuilder();
        InputStream testResource = this.getClass().getResourceAsStream("StorageQueryTest_1.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(testResource));
        String current;
        while ((current = reader.readLine()) != null) {
            builder.append(current);
        }

        DataRecordReader<String> dataRecordReader = new XmlStringDataRecordReader();
        DataRecord report = dataRecordReader.read(repository, updateReport, builder.toString());
        try {
            storage.begin();
            storage.update(report);
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = from(updateReport).where(isNull(taskId()));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
            storage.commit();
        }

        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
    }

    public void testNativeQueryWithReturn() throws Exception {
        UserQueryBuilder qb = from("SELECT * FROM PERSON;");
        StorageResults results = storage.fetch(qb.getExpression());
        assertEquals(4, results.getCount());
        assertEquals(4, results.getSize());
        for (DataRecord result : results) {
            assertNotNull(result.get("col0") != null);
        }
    }

    public void testNativeQueryWithNoReturn() throws Exception {
        UserQueryBuilder qb = from("UPDATE PERSON set x_firstname='My SQL modified firstname';");
        StorageResults results = storage.fetch(qb.getExpression());
        assertEquals(0, results.getCount());
        assertEquals(0, results.getSize());
        for (DataRecord result : results) {
            // Test iterator too (even if size is 0).
        }
        qb = from(person).where(eq(person.getField("firstname"), "Julien"));
        results = storage.fetch(qb.getExpression());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }

        qb = from(person).where(eq(person.getField("firstname"), "My SQL modified firstname"));
        results = storage.fetch(qb.getExpression());
        try {
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testContainsWithWildcards() throws Exception {
        UserQueryBuilder qb = from(person).where(contains(person.getField("firstname"), "*Ju*e"));

        Select select = qb.getSelect();
        assertNotNull(select);
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertTrue(condition instanceof Compare);
        Compare compareCondition = (Compare) condition;
        Expression right = compareCondition.getRight();
        assertTrue(right instanceof StringConstant);
        assertEquals("*Ju*e", ((StringConstant) right).getValue());

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testMultiLingualSearch() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("resume")).where(
                contains(person.getField("resume"), "*[EN:*splendid*]*"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(person).select(person.getField("resume")).where(contains(person.getField("resume"), "*[FR:*magnifique*]*"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(person).select(person.getField("resume")).where(contains(person.getField("resume"), "*[FR:*splendid*]*"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testSortOnXPath() throws Exception {
        UserQueryBuilder qb = from(person).selectId(person);
        TypedExpression sortField = UserQueryHelper.getFields(person, "../../i").get(0);
        qb.orderBy(sortField, OrderBy.Direction.DESC);

        StorageResults storageResults = storage.fetch(qb.getSelect());
        String[] expected = { "4", "3", "2", "1" };
        int i = 0;
        for (DataRecord result : storageResults) {
            assertEquals(expected[i++], result.get("id"));
        }
    }

    public void testSelectIdFromXPath() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname"));
        qb.select(person, "../../i");
        qb.where(eq(person.getField("id"), "1"));
        qb.orderBy(person.getField("firstname"), OrderBy.Direction.ASC);

        StorageResults storageResults = storage.fetch(qb.getSelect());
        for (DataRecord result : storageResults) {
            for (FieldMetadata fieldMetadata : result.getSetFields()) {
                assertNotNull(result.get(fieldMetadata));
            }
        }
    }

    public void testCompositeFKCollectionSearch() throws Exception {
        UserQueryBuilder qb = from(person).selectId(person).where(eq(person.getField("addresses/address"), "[3][false]"));
        StorageResults storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
        }
    }

    public void testCompositeFKCollectionSearchWithWhereItem() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(person);
        String fieldName = "Person/addresses/address";
        IWhereItem item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.EQUALS,
                "[3][false]", WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        StorageResults storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
        }

        qb = UserQueryBuilder.from(person);
        item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.EQUALS, null,
                WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, storageResults.getCount());
        } finally {
            storageResults.close();
        }
    }

    public void testFKCollectionSearch() throws Exception {
        UserQueryBuilder qb = from(product).selectId(product).where(eq(product.getField("Supplier"), "[2]"));
        StorageResults storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
        }
    }

    public void testFKCollectionSearchWithWhereItem() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(product);
        String fieldName = "Product/Supplier";
        IWhereItem item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.EQUALS, "[2]",
                WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        StorageResults storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
        }
    }

    public void testValueCollectionSearch() throws Exception {
        UserQueryBuilder qb = from(product).selectId(product).where(eq(product.getField("Features/Colors/Color"), "Blue"));
        StorageResults storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
        }
    }

    public void testValueCollectionSearchWithWhereItem() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(product);
        String fieldName = "Product/Features/Colors/Color";
        IWhereItem item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.EQUALS, "Blue",
                WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        StorageResults storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
        }
    }

    public void testValueCollectionSearchInNested() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords.add(factory.read(repository, person,
                "<Person><id>4</id><score>200000.00</score><lastname>Leblanc</lastname><middlename>John"
                        + "</middlename><firstname>Juste</firstname><addresses><address>[3][false]"
                        + "</address><address>[1][false]</address></addresses><age>30</age>"
                        + "<knownAddresses><knownAddress><Street>Street 1</Street><City>City 1</City>"
                        + "<Phone>012345</Phone></knownAddress>"
                        + "<knownAddress><Street>Street 2</Street><City>City 2</City><Phone>567890"
                        + "</Phone></knownAddress></knownAddresses>" + "<Status>Friend</Status></Person>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        UserQueryBuilder qb = from(person).selectId(person).where(
                eq(person.getField("knownAddresses/knownAddress/City"), "City 1"));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
            storage.commit();
        }
        qb = from(person).selectId(person).where(eq(person.getField("knownAddresses/knownAddress/City"), "City 0"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
            storage.commit();
        }
    }

    public void testValueSelectInNested() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords.add(factory.read(repository, person,
                "<Person><id>4</id><score>200000.00</score><lastname>Leblanc</lastname><middlename>John"
                        + "</middlename><firstname>Juste</firstname><addresses><address>[3][false]"
                        + "</address><address>[1][false]</address></addresses><age>30</age>"
                        + "<knownAddresses><knownAddress><Street>Street 1</Street><City>City 1</City>"
                        + "<Phone>012345</Phone></knownAddress>"
                        + "<knownAddress><Street>Street 2</Street><City>City 2</City><Phone>567890"
                        + "</Phone></knownAddress></knownAddresses>" + "<Status>Friend</Status></Person>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        UserQueryBuilder qb = from(person).selectId(person).select(person.getField("knownAddresses/knownAddress/City"))
                .where(not(eq(person.getField("knownAddresses/knownAddress/City"), "")));
        storage.begin();
        try {
            StorageResults results = storage.fetch(qb.getSelect());
            List<String> expected = new LinkedList<String>();
            expected.add("City 1");
            expected.add("City 2");
            for (DataRecord result : results) {
                assertTrue(expected.remove(result.get("City")));
            }
            assertTrue(expected.isEmpty());
        } finally {
            storage.commit();
        }
    }

    public void testSelectCompositeFK() throws Exception {
        ComplexTypeMetadata a1 = repository.getComplexType("a1");
        ComplexTypeMetadata a2 = repository.getComplexType("a2");

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords.add(factory.read(repository, a2,
                "<a2><subelement>1</subelement><subelement1>10</subelement1><b3>String b3</b3><b4>String b4</b4></a2>"));
        allRecords.add(factory.read(repository, a1,
                "<a1><subelement>1</subelement><subelement1>11</subelement1><b1>String b1</b1><b2>[1][10]</b2></a1>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();

        UserQueryBuilder qb = from(a1).selectId(a1).select(a1.getField("b1")).select(a1.getField("b2"));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                Object b2Value = result.get("b2");
                assertTrue(b2Value instanceof Object[]);
                Object[] b2Values = (Object[]) b2Value;
                assertEquals("1", b2Values[0]);
                assertEquals("10", b2Values[1]);
            }
        } finally {
            storage.commit();
            results.close();
        }
    }

    public void testJoinAndSelectJoinField() throws Exception {
        UserQueryBuilder qb = from(product).selectId(product).select(product.getField("Family")).select(store.getField("Name"))
                .join(product.getField("Stores/Store")).where(eq(store.getField("Name"), "Store #1")).limit(20);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("Store #1", result.get("Name"));
            }
        } finally {
            results.close();
        }
    }

    public void testFetchAllE1() {
        UserQueryBuilder qb = from(e1);
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(3, results.getCount());
        DataRecordXmlWriter writer = new DataRecordXmlWriter();
        Set<String> expectedStrings = new HashSet<String>();
        expectedStrings.add(E1_Record1);
        expectedStrings.add(E1_Record2);
        expectedStrings.add(E1_Record3);
        for (DataRecord result : results) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                writer.write(result, output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            expectedStrings.remove(output.toString());
        }
        assertTrue(expectedStrings.isEmpty());
    }

    public void testFetchAllE1ByAliasI() {
        UserQueryBuilder qb = from(e1);
        qb.selectId(e1);
        qb.select(e1, "../../i");
        qb.select(e1, "name");
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(3, results.getCount());

        DataRecordWriter writer = new DataRecordWriter() {

            @Override
            public void write(DataRecord record, OutputStream output) throws IOException {
                Writer out = new BufferedWriter(new OutputStreamWriter(output, "UTF-8")); //$NON-NLS-1$
                write(record, out);
            }

            @Override
            public void write(DataRecord record, Writer writer) throws IOException {
                writer.write("<result>"); //$NON-NLS-1$
                for (FieldMetadata fieldMetadata : record.getSetFields()) {
                    Object value = record.get(fieldMetadata);
                    if (value != null) {
                        writer.append("<").append(fieldMetadata.getName()).append(">");
                        writer.append(StringEscapeUtils.escapeXml(String.valueOf(value)));
                        writer.append("</").append(fieldMetadata.getName()).append(">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
                }
                writer.append("</result>"); //$NON-NLS-1$
                writer.flush();
            }

            @Override
            public void setSecurityDelegator(SecuredStorage.UserDelegator delegator) {
                // Not needed for test.
            }
        };
        Set<String> expectedStrings = new HashSet<String>();
        expectedStrings
                .add("<result><subelement>aaa</subelement><subelement1>bbb</subelement1><i>aaa</i><i>bbb</i><name>asdf</name></result>");
        expectedStrings
                .add("<result><subelement>ccc</subelement><subelement1>ddd</subelement1><i>ccc</i><i>ddd</i><name>cvcvc</name></result>");
        expectedStrings
                .add("<result><subelement>ttt</subelement><subelement1>yyy</subelement1><i>ttt</i><i>yyy</i><name>nhhn</name></result>");
        for (DataRecord result : results) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                writer.write(result, output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            expectedStrings.remove(output.toString());
        }
        assertTrue(expectedStrings.isEmpty());
    }

    public void testFetchE2ByForeignKeyToCompositeKeys() {
        UserQueryBuilder qb = from(e2).where(eq(e2.getField("fk"), "[ccc][ddd]"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(3, results.getCount());

        Set<String> expectedStrings = new HashSet<String>();
        expectedStrings.add(E2_Record1);
        expectedStrings.add(E2_Record5);
        expectedStrings.add(E2_Record6);
        DataRecordXmlWriter writer = new DataRecordXmlWriter();
        for (DataRecord result : results) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                writer.write(result, output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            expectedStrings.remove(output.toString());
        }
        assertTrue(expectedStrings.isEmpty());

        qb = from(e2).where(eq(e2.getField("fk"), "[aaa][bbb]"));
        results = storage.fetch(qb.getSelect());
        assertEquals(2, results.getCount());

        expectedStrings = new HashSet<String>();
        expectedStrings.add(E2_Record2);
        expectedStrings.add(E2_Record4);
        for (DataRecord result : results) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                writer.write(result, output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            expectedStrings.remove(output.toString());
        }
        assertTrue(expectedStrings.isEmpty());

        qb = from(e2).where(eq(e2.getField("fk"), "[ttt][yyy]"));
        results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());

        expectedStrings = new HashSet<String>();
        expectedStrings.add(E2_Record3);
        for (DataRecord result : results) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                writer.write(result, output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            expectedStrings.remove(output.toString());
        }
        assertTrue(expectedStrings.isEmpty());
    }

    public void testFetchAllE2() throws Exception {
        UserQueryBuilder qb = from(e2);
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(7, results.getCount());
        DataRecordXmlWriter writer = new DataRecordXmlWriter();
        StringWriter output = new StringWriter();
        List<String> expectedResults = new LinkedList<String>(Arrays.asList(E2_Record1, E2_Record2, E2_Record3, E2_Record4,
                E2_Record5, E2_Record6, E2_Record7));
        for (DataRecord result : results) {
            writer.write(result, output);
            expectedResults.remove(output.toString());
            output = new StringWriter();
        }
        assertTrue(expectedResults.isEmpty());
    }

    public void testBuildCondition() {
        UserQueryBuilder qb = from(product);
        List<IWhereItem> conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Product/Family", "JOINS", "ProductFamily/Id", "&"));
        IWhereItem fullWhere = new WhereAnd(conditions);
        BinaryLogicOperator condition = (BinaryLogicOperator) UserQueryHelper.buildCondition(qb, fullWhere, repository);
        assertNotNull(condition);

        conditions.clear();
        conditions.add(new WhereCondition("../../t", ">=", "1364227200000", "&"));
        fullWhere = new WhereAnd(conditions);
        condition = (BinaryLogicOperator) UserQueryHelper.buildCondition(qb, fullWhere, repository);
        assertNotNull(condition);
        assertTrue(Predicate.AND.equals(condition.getPredicate()));
        assertTrue(condition.getRight() instanceof Compare);
        Compare compare = (Compare) condition.getRight();
        assertTrue(compare.getLeft() instanceof Timestamp);
        assertTrue(Predicate.GREATER_THAN_OR_EQUALS.equals(compare.getPredicate()));
        assertTrue(compare.getRight() instanceof LongConstant);
        LongConstant value = (LongConstant) compare.getRight();
        assertEquals(Long.valueOf(1364227200000L), value.getValue());
    }

    public void testBuildNotCondition() {
        UserQueryBuilder qb = from(product);
        List<IWhereItem> conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Product/Name", "=", "Renault car", "!"));
        IWhereItem fullWhere = new WhereAnd(conditions);
        UnaryLogicOperator condition = (UnaryLogicOperator) UserQueryHelper.buildCondition(qb, fullWhere, repository).normalize();
        assertNotNull(condition);

        assertTrue(Predicate.NOT.equals(condition.getPredicate()));
        Compare compare = (Compare) condition.getCondition();
        assertTrue(compare.getLeft() instanceof Field);
        assertTrue(Predicate.EQUALS.equals(compare.getPredicate()));
        assertTrue(compare.getRight() instanceof StringConstant);
        StringConstant value = (StringConstant) compare.getRight();
        assertEquals("Renault car", value.getValue());
    }

    public void testDuplicateFieldNames() {
        UserQueryBuilder qb = from(product);

        List<String> viewables = new ArrayList<String>();
        viewables.add("Product/Id");
        viewables.add("Product/Name");
        viewables.add("Product/Family");
        viewables.add("ProductFamily/Id");
        viewables.add("ProductFamily/Name");

        List<IWhereItem> conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Product/Family", "JOINS", "ProductFamily/Id", "&"));

        IWhereItem fullWhere = new WhereAnd(conditions);
        qb.where(UserQueryHelper.buildCondition(qb, fullWhere, repository));

        // add order by Id to make the test stable
        qb.orderBy(product.getField("Id"), Direction.ASC);
        
        for (String viewableBusinessElement : viewables) {
            String viewableTypeName = StringUtils.substringBefore(viewableBusinessElement, "/"); //$NON-NLS-1$
            String viewablePath = StringUtils.substringAfter(viewableBusinessElement, "/"); //$NON-NLS-1$
            qb.select(UserQueryHelper.getFields(repository.getComplexType(viewableTypeName), viewablePath).get(0));
        }

        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(2, results.getCount());

        DataRecordWriter writer = new ViewSearchResultsWriter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> strings = new ArrayList<String>();
        for (DataRecord result : results) {
            try {
                writer.write(result, output);
                String document = new String(output.toByteArray(), Charset.forName("UTF-8"));
                strings.add(document);
                output.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        assertEquals(2, strings.size());
        assertEquals(
                "<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n\t<Id>1</Id>\n\t<Name>Product name</Name>\n\t<Family>[2]</Family>\n\t<Id>2</Id>\n\t<Name>Product family #2</Name>\n</result>",
                strings.get(0));
        assertEquals(
                "<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n\t<Id>2</Id>\n\t<Name>Renault car</Name>\n\t<Family/>\n\t<Id/>\n\t<Name/>\n</result>",
                strings.get(1));
    }

    public void testFetchAllE2WithJoinE1() {

        ComplexTypeMetadata type = repository.getComplexType("Product");
        UserQueryBuilder qb = UserQueryBuilder.from(type);

        List<TypedExpression> fields = UserQueryHelper.getFields(product, "Id");
        for (TypedExpression field : fields) {
            qb.select(field);
            qb.orderBy(product.getField("Id"), OrderBy.Direction.ASC);
        }
        fields = UserQueryHelper.getFields(product, "Name");
        for (TypedExpression field : fields) {
            qb.select(field);
        }
        fields = UserQueryHelper.getFields(productFamily, "Name");
        for (TypedExpression field : fields) {
            TypedExpression typeExpression = new Alias(field, "ProductFamily_Name");
            qb.select(typeExpression);
        }

        ArrayList conditions = new ArrayList();
        WhereCondition cond = new WhereCondition("Product/Family", "JOINS", "ProductFamily/Id", "&", false);
        conditions.add(cond);
        WhereAnd fullWhere = new WhereAnd(conditions);
        qb.where(UserQueryHelper.buildCondition(qb, fullWhere, repository));

        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(2, results.getCount());

        DataRecordWriter writer = new ViewSearchResultsWriter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> resultsAsString = new ArrayList<String>();
        for (DataRecord result : results) {
            try {
                writer.write(result, output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String document = new String(output.toByteArray(), Charset.forName("UTF-8"));
            resultsAsString.add(document);
            output.reset();
        }
        assertEquals(2, resultsAsString.size());

        StringBuilder sb = new StringBuilder();
        sb.append("<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        sb.append("\t<Id>1</Id>\n");
        sb.append("\t<Name>Product name</Name>\n");
        sb.append("\t<ProductFamily_Name>Product family #2</ProductFamily_Name>\n");
        sb.append("</result>");
        assertEquals(sb.toString(), resultsAsString.get(0));

        sb = new StringBuilder();
        sb.append("<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        sb.append("\t<Id>2</Id>\n");
        sb.append("\t<Name>Renault car</Name>\n");
        sb.append("\t<ProductFamily_Name/>\n");
        sb.append("</result>");
        assertEquals(sb.toString(), resultsAsString.get(1));

        // Test Fetch Product by whereCondition = (Product/Id Equals 1) and (Product/Family Joins ProductFamily/Id)
        WhereCondition condition = new WhereCondition("Product/Id", "=", "1", "&", false);
        fullWhere.add(condition);
        qb.where(UserQueryHelper.buildCondition(qb, fullWhere, repository));

        results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());

        writer = new ViewSearchResultsWriter();
        output = new ByteArrayOutputStream();
        resultsAsString = new ArrayList<String>();
        for (DataRecord result : results) {
            try {
                writer.write(result, output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String document = new String(output.toByteArray(), Charset.forName("UTF-8"));
            resultsAsString.add(document);
            output.reset();
        }
        assertEquals(1, resultsAsString.size());

        sb = new StringBuilder();
        sb.append("<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        sb.append("\t<Id>1</Id>\n");
        sb.append("\t<Name>Product name</Name>\n");
        sb.append("\t<ProductFamily_Name>Product family #2</ProductFamily_Name>\n");
        sb.append("</result>");
        assertEquals(sb.toString(), resultsAsString.get(0));
    }

    public void testFetchAllE2WithViewSearchResultsWriter() throws Exception {
        UserQueryBuilder qb = from(e2);
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(7, results.getCount());
        DataRecordWriter writer = new ViewSearchResultsWriter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ArrayList<String> resultsAsString = new ArrayList<String>();
        for (DataRecord result : results) {
            try {
                writer.write(result, output);
            } catch (IOException e) {
                throw new XmlServerException(e);
            }
            String document = new String(output.toByteArray(), Charset.forName("UTF-8"));
            resultsAsString.add(document);
            output.reset();
        }

        assertEquals(7, resultsAsString.size());

        String startRoot = "<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
        String endRoot = "</result>";

        Set<String> expectedResults = new HashSet<String>();
        expectedResults.add(startRoot
                + "<subelement>111</subelement><subelement1>222</subelement1><name>qwe</name><fk>[ccc][ddd]</fk>" + endRoot);
        expectedResults.add(startRoot
                + "<subelement>344</subelement><subelement1>544</subelement1><name>55</name><fk>[aaa][bbb]</fk>" + endRoot);
        expectedResults.add(startRoot
                + "<subelement>333</subelement><subelement1>444</subelement1><name>tyty</name><fk>[ttt][yyy]</fk>" + endRoot);
        expectedResults.add(startRoot
                + "<subelement>666</subelement><subelement1>777</subelement1><name>iuj</name><fk>[aaa][bbb]</fk>" + endRoot);
        expectedResults.add(startRoot
                + "<subelement>6767</subelement><subelement1>7878</subelement1><name>ioiu</name><fk>[ccc][ddd]</fk>" + endRoot);
        expectedResults.add(startRoot
                + "<subelement>999</subelement><subelement1>888</subelement1><name>iuiiu</name><fk>[ccc][ddd]</fk>" + endRoot);
        expectedResults.add(startRoot + "<subelement>119</subelement><subelement1>120</subelement1><name>zhang</name>" + endRoot);
        for (String s : resultsAsString) {
            expectedResults.remove(s.replaceAll("\\r|\\n|\\t", ""));
        }
        assertTrue(expectedResults.isEmpty());
    }

    public void testFKInReusableTypeWithViewSearch() throws Exception {
        UserQueryBuilder qb = from(organization).selectId(organization)
                .select(alias(organization.getField("org_address/city"), "city1"))
                .select(alias(organization.getField("org_address/street"), "street1"))
                .select(alias(organization.getField("post_address/city"), "city2"))
                .select(alias(organization.getField("post_address/street"), "street2"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        for (DataRecord result : results) {
            assertEquals("SH", String.valueOf(result.get("city1")));
            assertEquals("waitan rd", String.valueOf(result.get("street1")));
            assertEquals("BJ", String.valueOf(result.get("city2")));
            assertEquals("changan rd", String.valueOf(result.get("street2")));
        }
    }

    public void testFKInreusableTypeWithViewSearch2() throws Exception {
        UserQueryBuilder qb = from(organization).selectId(organization).select(organization.getField("org_address/city"))
                .select(organization.getField("org_address/street"))
                .select(alias(organization.getField("post_address/city"), "city"))
                .select(alias(organization.getField("post_address/street"), "street"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        DataRecordWriter writer = new ViewSearchResultsWriter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String resultAsString = "";
        for (DataRecord result : results) {
            try {
                writer.write(result, output);
            } catch (IOException e) {
                throw new XmlServerException(e);
            }
            resultAsString = new String(output.toByteArray(), Charset.forName("UTF-8"));
            output.reset();
        }
        String startRoot = "<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
        String endRoot = "</result>";

        String expectedResult = startRoot
                + "<org_id>1</org_id><city>[SH]</city><street>waitan rd</street><city>[BJ]</city><street>changan rd</street>"
                + endRoot;
        assertTrue(expectedResult.equals(resultAsString.replaceAll("\\r|\\n|\\t", "")));
    }
    
    public void testEmptyOrNullInMultidepthComplexType() throws Exception {
        Storage staging = new HibernateStorage("MDMStaging", StorageType.STAGING);
        staging.init(ServerContext.INSTANCE.get().getDefinition("RDBMS-1-NO-FT", "MDM"));
        staging.prepare(repository, true);
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        
        try {
            allRecords
            .add(factory
                    .read(repository,
                            organization,
                            "<Organization xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><org_id>1</org_id><post_address><street>changan rd</street><city>[BJ]</city><country><name>cn</name><code></code></country></post_address><org_address><street>waitan rd</street><city>[SH]</city><country><name>fr</name><code>33</code></country></org_address></Organization>"));
            staging.begin();
            staging.update(allRecords);
            staging.commit();
            
            UserQueryBuilder qb = from(organization).selectId(organization)
                    .where(emptyOrNull(organization.getField("post_address/country/code")));
            StorageResults results = staging.fetch(qb.getSelect());
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", String.valueOf(result.get("org_id")));
            }
            
        } catch (Exception e) {
            staging.rollback();
        } finally {
            staging.begin();
            staging.delete(from(organization).getSelect());
            staging.commit();
        }
        
        try {
            allRecords = new LinkedList<DataRecord>();
            allRecords
            .add(factory
                    .read(repository,
                            organization,
                            "<Organization xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><org_id>2</org_id><org_address><street>longbai rd</street><city>[SH]</city><country><name>zh</name><code>44</code></country></org_address></Organization>"));
            staging.begin();
            staging.update(allRecords);
            staging.commit();
            
            UserQueryBuilder qb = from(organization).selectId(organization)
                    .where(emptyOrNull(organization.getField("post_address/country/code")));
            StorageResults results = staging.fetch(qb.getSelect());
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("2", String.valueOf(result.get("org_id")));
            }
            
        } catch (Exception e) {
            staging.rollback();
        } finally {
            staging.rollback();
        }
        
    }

    public void testStringFieldConstraint() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        DataRecord dataRecord = factory.read(repository, product, "<Product>\n" + "    <Id>3</Id>\n"
                + "    <Name>A long name to be short due to constraints</Name>\n"
                + "    <ShortDescription>A car</ShortDescription>\n"
                + "    <LongDescription>Long description 2</LongDescription>\n" + "    <Price>10</Price>\n" + "    <Features>\n"
                + "        <Sizes>\n" + "            <Size>Large</Size>\n" + "        </Sizes>\n" + "        <Colors>\n"
                + "            <Color>Blue 2</Color>\n" + "            <Color>Blue 1</Color>\n"
                + "            <Color>Klein blue2</Color>\n" + "        </Colors>\n" + "    </Features>\n" + "    <Family/>\n"
                + "    <Status>Pending</Status>\n" + "    <Supplier>[2]</Supplier>\n" + "    <Supplier>[1]</Supplier>\n"
                + "<Stores><Store>[1]</Store></Stores></Product>");
        storage.begin();
        storage.update(dataRecord);
        try {
            storage.commit();
            fail("Expected an exception (value too long for name)");
        } catch (Exception e) {
            // Expected
            storage.rollback();
        }

        dataRecord = factory.read(repository, product, "<Product>\n" + "    <Id>3</Id>\n" + "    <Name>A long nam</Name>\n"
                + "    <ShortDescription>A car</ShortDescription>\n"
                + "    <LongDescription>Long description 2</LongDescription>\n" + "    <Price>10</Price>\n" + "    <Features>\n"
                + "        <Sizes>\n" + "            <Size>Large</Size>\n" + "        </Sizes>\n" + "        <Colors>\n"
                + "            <Color>Blue 2</Color>\n" + "            <Color>Blue 1</Color>\n"
                + "            <Color>Klein blue2</Color>\n" + "        </Colors>\n" + "    </Features>\n" + "    <Family/>\n"
                + "    <Status>Pending</Status>\n" + "    <Supplier>[2]</Supplier>\n" + "    <Supplier>[1]</Supplier>\n"
                + "<Stores><Store>[1]</Store></Stores></Product>");
        storage.begin();
        storage.update(dataRecord);
        storage.commit(); // This one should work.

        UserQueryBuilder qb = from(product).select(product.getField("Name")).where(eq(product.getField("Id"), "3"));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
        for (DataRecord result : results) {
            assertEquals("A long nam", result.get("Name"));
        }
        storage.commit();

    }

    public void testEnumerationSelect() throws Exception {
        UserQueryBuilder qb = from(product).select(product.getField("Status"));

        StorageResults results = storage.fetch(qb.getSelect());
        assertTrue("There should be at least 2 records", results.getCount() >= 2);
        List<String> expectedStatuses = Arrays.asList("Created", "Removed", "Active", "Pending");
        for (DataRecord result : results) {
            assertNotNull(result.get("Status"));
            assertTrue(expectedStatuses.contains(String.valueOf(result.get("Status"))));
        }
    }

    public void testManyFieldSelect() throws Exception {
        UserQueryBuilder qb = from(product).select(product.getField("Features/Sizes/Size"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertTrue("There should be at least 2 records", results.getCount() >= 2);
        Set<String> expectedResults = new HashSet<String>();
        expectedResults.add("Small,Medium,Large");
        expectedResults.add("Large");
        for (DataRecord result : results) {
            expectedResults.remove(result.get("Size"));
        }
        assertTrue(expectedResults.isEmpty());
    }

    public void testManyFieldIndexCondition() throws Exception {
        UserQueryBuilder qb = from(product).where(eq(index(product.getField("Features/Sizes/Size"), 1), "Medium"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());

        qb = from(product).where(eq(index(product.getField("Features/Sizes/Size"), 0), "Medium"));
        results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());

        qb = from(product);
        qb.where(UserQueryHelper.buildCondition(qb, new WhereCondition("Product/Features/Sizes/Size[2]", WhereCondition.EQUALS,
                "Medium", WhereCondition.PRE_NONE), repository));
        results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());

        qb = from(product);
        qb.where(UserQueryHelper.buildCondition(qb, new WhereCondition("Product/Features/Sizes/Size[1]", WhereCondition.EQUALS,
                "Medium", WhereCondition.PRE_NONE), repository));
        results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());
    }

    public void testManyFieldIndexConditionOnFK() throws Exception {
        UserQueryBuilder qb = from(product).where(eq(index(product.getField("Stores/Store"), 1), "2"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());

        qb = from(product).where(eq(index(product.getField("Stores/Store"), 0), "1"));
        results = storage.fetch(qb.getSelect());
        assertEquals(1, results.getCount());
    }

    public void testManyFieldUsingAndCondition() throws Exception {
        UserQueryBuilder qb = from(product).where(eq(product.getField("Features/Sizes/Size"), "ValueDoesNotExist"));
        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());

        qb = from(product).where(
                and(eq(product.getField("Id"), "1"), eq(product.getField("Features/Sizes/Size"), "ValueDoesNotExist")));
        results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());
    }

    public void testContainsCaseSensitivity() throws Exception {
        Storage s1 = new HibernateStorage("MDM1", StorageType.MASTER);
        s1.init(ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-DS1", "MDM"));
        s1.prepare(repository, true);
        Storage s2 = new HibernateStorage("MDM2", StorageType.MASTER);
        s2.init(ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-DS2", "MDM"));
        s2.prepare(repository, true);
        // Create country instance on both storages.
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>1</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France</name></Country>"));
        try {
            s1.begin();
            s1.update(allRecords);
            s1.commit();
        } finally {
            s1.end();
        }
        try {
            s2.begin();
            s2.update(allRecords);
            s2.commit();
        } finally {
            s1.end();
        }
        // DS1 is case sensitive, DS2 isn't
        UserQueryBuilder qb = from(country).where(contains(country.getField("name"), "FRANCE"));
        StorageResults results = s1.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        qb = from(country).where(contains(country.getField("name"), "france"));
        results = s1.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        qb = from(country).where(contains(country.getField("name"), "France"));
        results = s1.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
        // DS2
        qb = from(country).where(contains(country.getField("name"), "FRANCE"));
        results = s2.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
        qb = from(country).where(contains(country.getField("name"), "france"));
        results = s2.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
        qb = from(country).where(contains(country.getField("name"), "France"));
        results = s2.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testStartsWithCaseSensitivity() throws Exception {
        Storage s1 = new HibernateStorage("MDM1", StorageType.MASTER);
        s1.init(ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-DS1", "MDM"));
        s1.prepare(repository, true);
        Storage s2 = new HibernateStorage("MDM2", StorageType.MASTER);
        s2.init(ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-DS2", "MDM"));
        s2.prepare(repository, true);
        // Create country instance on both storages.
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords
                .add(factory
                        .read(repository,
                                country,
                                "<Country><id>1</id><creationDate>2010-10-10</creationDate><creationTime>2010-10-10T00:00:01</creationTime><name>France</name></Country>"));
        try {
            s1.begin();
            s1.update(allRecords);
            s1.commit();
        } finally {
            s1.end();
        }
        try {
            s2.begin();
            s2.update(allRecords);
            s2.commit();
        } finally {
            s1.end();
        }
        // DS1 is case sensitive, DS2 isn't
        UserQueryBuilder qb = from(country).where(startsWith(country.getField("name"), "FRA"));
        StorageResults results = s1.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        results = s2.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testIsEmptyOrNullOnRepeatable() throws Exception {       
        UserQueryBuilder qb = UserQueryBuilder.from(country).where(isEmpty(country.getField("notes/comment")));
        StorageResults results = storage.fetch(qb.getSelect());
        for (DataRecord result : results) {
            assertEquals("Country", result.getType().getName());
            assertEquals(1, result.get("id"));
        }

        qb = UserQueryBuilder.from(country).where(
                or(isEmpty(country.getField("notes/comment")), isNull(country.getField("notes/comment"))));
        results = storage.fetch(qb.getSelect());
        for (DataRecord result : results) {
            assertEquals("Country", result.getType().getName());
            assertEquals(1, result.get("id"));
        }
        
        
        qb = UserQueryBuilder.from(person).where(
                or(isEmpty(person.getField("addresses/address")), isNull(person.getField("addresses/address"))));
        results = storage.fetch(qb.getSelect());
        assertEquals(0, results.getCount());
        results.close();
    }

    public void testFullText_OR() throws Exception {
        UserQueryBuilder qb = from(product).where(
                or(contains(product.getField("Id"), "1"), contains(product.getField("Id"), "2")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testFullText_AND() throws Exception {
        UserQueryBuilder qb = from(product).where(
                and(contains(product.getField("Id"), "1"), contains(product.getField("Id"), "2")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testJoinOptimization() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords.add(factory.read(repository, person,
                "<Person><id>5</id><score>200000.00</score><lastname>Leblanc</lastname><middlename>John"
                        + "</middlename><firstname>Juste</firstname><addresses><address>[3][false]"
                        + "</address><address>[1][false]</address></addresses><age>30</age>"
                        + "<knownAddresses><knownAddress><Street>Street 1</Street><City>City 1</City>"
                        + "<Phone>012345</Phone></knownAddress>"
                        + "<knownAddress><Street>Street 2</Street><City>City 2</City><Phone>567890"
                        + "</Phone><Notes><Note>test note</Note></Notes></knownAddress></knownAddresses>"
                        + "<Status>Friend</Status></Person>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();

        UserQueryBuilder qb = UserQueryBuilder.from(person).where(
                eq(person.getField("knownAddresses/knownAddress/Notes/Note"), "test note"));
        StorageResults results = storage.fetch(qb.getSelect());
        for (DataRecord result : results) {
            assertEquals("5", result.get("id"));
        }
    }

    public void testJoinOptimizationWithOr() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords.add(factory.read(repository, person,
                "<Person><id>5</id><score>20000.00</score><lastname>Leblanc</lastname><middlename>John"
                        + "</middlename><firstname>Juste</firstname><addresses><address>[3][false]"
                        + "</address><address>[1][false]</address></addresses><age>30</age>"
                        + "<knownAddresses><knownAddress><Street>Street 1</Street><City>City 1</City>"
                        + "<Phone>012345</Phone></knownAddress>"
                        + "<knownAddress><Street>Street 2</Street><City>City 2</City><Phone>567890"
                        + "</Phone><Notes><Note>test note</Note></Notes></knownAddress></knownAddresses>"
                        + "<Status>Friend</Status></Person>"));
        allRecords.add(factory.read(repository, person,
                "<Person><id>6</id><score>666.00</score><lastname>Leblanc</lastname><middlename>John"
                        + "</middlename><firstname>Juste</firstname><addresses><address>[3][false]"
                        + "</address><address>[1][false]</address></addresses><age>30</age>"
                        + "<knownAddresses><knownAddress><Street>Street 1</Street><City>City 1</City>"
                        + "<Phone>012345</Phone></knownAddress>"
                        + "<knownAddress><Street>Street 2</Street><City>City 2</City><Phone>567890"
                        + "</Phone><Notes><Note>test note</Note></Notes></knownAddress></knownAddresses>"
                        + "<Status>Friend</Status></Person>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();

        UserQueryBuilder qb = UserQueryBuilder.from(person).where(
                or(eq(person.getField("knownAddresses/knownAddress/Notes/Note"), "test note"),
                        eq(person.getField("score"), "666")));
        storage.begin();
        try {
            StorageResults results = storage.fetch(qb.getSelect());
            // assertEquals(2, results.getCount()); -> should be 3
            assertEquals(3, results.getCount());
            for (DataRecord result : results) {
                assertTrue(result.get("id").equals("5") || result.get("id").equals("6"));
            }
        } finally {
            storage.commit();
        }

        /*
         * qb = UserQueryBuilder.from(person) .where(and(eq(person.getField("knownAddresses/knownAddress/Notes/Note"),
         * "test note"), eq(person.getField("score"), "777"))); results = storage.fetch(qb.getSelect()); assertEquals(0,
         * results.getCount());
         */
    }

    public void testContainsWithUnderscoreInSearchWords1() throws Exception {
        UserQueryBuilder qb = from(productFamily).where(contains(productFamily.getField("Name"), "test"));

        Select select = qb.getSelect();
        assertNotNull(select);
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertTrue(condition instanceof Compare);
        Compare compareCondition = (Compare) condition;
        Expression right = compareCondition.getRight();
        assertTrue(right instanceof StringConstant);
        assertEquals("test", ((StringConstant) right).getValue());

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testContainsWithoutUnderscoreInSearchWords2() throws Exception {
        UserQueryBuilder qb = from(productFamily).where(contains(productFamily.getField("Name"), "test name"));

        Select select = qb.getSelect();
        assertNotNull(select);
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertTrue(condition instanceof Compare);
        Compare compareCondition = (Compare) condition;
        Expression right = compareCondition.getRight();
        assertTrue(right instanceof StringConstant);
        assertEquals("test name", ((StringConstant) right).getValue());

        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testQueryWithFK() throws Exception {
        UserQueryBuilder qb = from(product).where(
                and(contains(product.getField("Id"), "1"), eq(product.getField("Family"), "[2]")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(product).where(contains(product.getField("Family"), "b"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }

    }

    public void testQueryWithIntFK() throws Exception {
        UserQueryBuilder qb = from(entityB).where(
                or(contains(entityB.getField("A_FK"), "b"), contains(entityB.getField("IdB"), "b")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }

        qb = from(entityB).where(contains(entityB.getField("A_FK"), "b"));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testMax() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(person).select(max(person.getField("age"))).limit(1);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertEquals(30, result.get("max"));
            }
        } finally {
            results.close();
        }

        qb = UserQueryBuilder.from(person).select(max(timestamp())).limit(1);
        results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertTrue(((Long) result.get("max")) < System.currentTimeMillis());
            }
        } finally {
            results.close();
        }
    }

    public void testMin() throws Exception {
        UserQueryBuilder qb = UserQueryBuilder.from(person).select(min(person.getField("age"))).limit(1);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertEquals(10, result.get("min"));
            }
        } finally {
            results.close();
        }

        qb = UserQueryBuilder.from(person).select(min(timestamp())).limit(1);
        results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertTrue(((Long) result.get("min")) < System.currentTimeMillis());
            }
        } finally {
            results.close();
        }
    }

    public void testRangeOptimization() throws Exception {
        RangeOptimizer optimizer = new RangeOptimizer();
        // No optimization
        UserQueryBuilder qb = UserQueryBuilder.from(person).where(
                and(gte(person.getField("id"), "0"), lte(person.getField("id"), "1")));
        Select select = qb.getSelect();
        assertTrue(select.getCondition() instanceof BinaryLogicOperator);
        // Optimization
        optimizer.optimize(select);
        assertTrue(select.getCondition() instanceof Range);
        assertEquals(new StringConstant("0"), ((Range) select.getCondition()).getStart());
        assertEquals(new StringConstant("1"), ((Range) select.getCondition()).getEnd());
        // Optimization
        qb = UserQueryBuilder.from(person).where(and(lte(person.getField("id"), "1"), gte(person.getField("id"), "0")));
        select = qb.getSelect();
        optimizer.optimize(select);
        assertTrue(select.getCondition() instanceof Range);
        assertEquals(new StringConstant("0"), ((Range) select.getCondition()).getStart());
        assertEquals(new StringConstant("1"), ((Range) select.getCondition()).getEnd());
        // No optimization (not applicable)
        qb = UserQueryBuilder.from(person).where(
                and(and(gte(person.getField("id"), "0"), eq(person.getField("score"), "0")), lte(person.getField("id"), "1")));
        select = qb.getSelect();
        optimizer.optimize(select);
        assertTrue(select.getCondition() instanceof BinaryLogicOperator);
        assertFalse(((BinaryLogicOperator) select.getCondition()).getLeft() instanceof Range);
        // Optimization
        qb = UserQueryBuilder.from(person).where(
                and(and(gte(person.getField("id"), "0"), lte(person.getField("id"), "1")), eq(person.getField("score"), "0")));
        select = qb.getSelect();
        optimizer.optimize(select);
        assertTrue(select.getCondition() instanceof BinaryLogicOperator);
        assertTrue(((BinaryLogicOperator) select.getCondition()).getLeft() instanceof Range);
    }

    public void testPkIncludeDataTimeType() throws Exception {
        UserQueryBuilder qb = from(employee1).where(eq(employee1.getField("manager"), "[1][2014-05-01T12:00:00]"));
        StorageResults results = storage.fetch(qb.getSelect());
        DataRecordXmlWriter writer = new DataRecordXmlWriter();
        try {
            String expectedXml = "<Employee1><Id>1</Id><Holiday>2014-05-16T12:00:00</Holiday><birthday>2014-05-23T12:00:00</birthday><manager>[1][2014-05-01T12:00:00]</manager></Employee1>";
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (DataRecord result : results) {
                try {
                    writer.write(result, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String actual = new String(output.toByteArray());
            assertEquals(expectedXml, actual);
        } finally {
            results.close();
        }
    }

    public void testGetByIdWithProjection() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).where(eq(person.getField("id"), "1"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertEquals("Julien", result.get("firstname"));
            }
        } finally {
            results.close();
        }
    }

    public void testGetByIdWithCondition() throws Exception {
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).where(
                and(eq(person.getField("id"), "1"), eq(person.getField("id"), "2")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount()); // Id can't be equals to "1" AND "2"...
        } finally {
            results.close();
        }

        qb = from(person).select(person.getField("firstname")).where(
                or(eq(person.getField("id"), "1"), eq(person.getField("id"), "2")));
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount()); // ... but "1" OR "2" returns 2 results.
        } finally {
            results.close();
        }
    }

    public void testDistinctProjection() throws Exception {
        UserQueryBuilder qb = from(person).select(alias(distinct(person.getField("firstname")), "firstname"));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            Set<String> expected = new HashSet<String>();
            expected.add("Julien");
            expected.add("Juste");
            expected.add("Robert-Julien");
            for (DataRecord result : results) {
                expected.remove(result.get("firstname"));
            }
            assertEquals(0, expected.size());
        } finally {
            results.close();
        }
    }

    public void testMaxProjection() throws Exception {
        UserQueryBuilder qb = from(person).select(max(person.getField("score")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertEquals("200000.00", String.valueOf(result.get("max")));
            }
        } finally {
            results.close();
        }
    }

    public void testMinProjection() throws Exception {
        UserQueryBuilder qb = from(person).select(min(person.getField("score")));
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertEquals("130000.00", String.valueOf(result.get("min")));
            }
        } finally {
            results.close();
        }
    }

    public void testTaskIdProjection() throws Exception {
        UserQueryBuilder qb = from(person).select(taskId());
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertNull(result.get("metadata:taskId"));
            }
        } finally {
            results.close();
        }
    }

    public void testTimestampProjection() throws Exception {
        UserQueryBuilder qb = from(person).select(timestamp());
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertNotNull(result.get("metadata:timestamp"));
                assertTrue(((Long) result.get("metadata:timestamp")) > 0);
            }
        } finally {
            results.close();
        }
        qb = from(person).selectId(person).select(person.getField("firstname")).select(timestamp()).select(taskId())
                .where(contains(person.getField("firstname"), "Jul"));
        results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertNotNull(result.get("metadata:timestamp"));
                assertTrue(((Long) result.get("metadata:timestamp")) > 0);
            }
        } finally {
            results.close();
        }
    }

    public void testCountProjection() throws Exception {
        UserQueryBuilder qb = from(person).select(count());
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            for (DataRecord result : results) {
                assertNotNull(result.get("count"));
                assertEquals(4l, result.get("count"));
            }
        } finally {
            results.close();
        }
    }

    public void testManyRelationToRecordChange() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        // Update 'FKtoMultiB' list records (record1..record5)
        
        {
            storage.begin();
            UserQueryBuilder qb = from(ContainedEntityB).select(ContainedEntityB.getField("id"));
            StorageResults records = storage.fetch(qb.getSelect());
            try {
                assertEquals(5, records.getCount());
            } finally {
                storage.commit();
            }
        }
        
        
        allRecords
                .add(factory
                        .read(repository,
                                ContainedEntityA,
                                "<ContainedEntityA xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Aid>a_id</Aid><FKtoB><Bid/></FKtoB><FKtoMultiB><Bid>[B_record1]</Bid></FKtoMultiB><FKtoMultiB><Bid>[B_record2]</Bid></FKtoMultiB><FKtoMultiB><Bid>[B_record3]</Bid></FKtoMultiB><FKtoMultiB><Bid>[B_record4]</Bid></FKtoMultiB><FKtoMultiB><Bid>[B_record5]</Bid></FKtoMultiB></ContainedEntityA>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        // Delete last 'FKtoMultiB' list record (record1..record4)
        allRecords = new LinkedList<DataRecord>();
        allRecords
                .add(factory
                        .read(repository,
                                ContainedEntityA,
                                "<ContainedEntityA xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Aid>a_id</Aid><FKtoB><Bid/></FKtoB><FKtoMultiB><Bid>[B_record1]</Bid></FKtoMultiB><FKtoMultiB><Bid>[B_record2]</Bid></FKtoMultiB><FKtoMultiB><Bid>[B_record3]</Bid></FKtoMultiB><FKtoMultiB><Bid>[B_record4]</Bid></FKtoMultiB></ContainedEntityA>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        // Delete 'record5' which is no longer used
        UserQueryBuilder qb = from(ContainedEntityB).where(eq(ContainedEntityB.getField("id"), "B_record5"));
        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
        // Test actual deletion of 'record5'.
        storage.begin();
        qb = from(ContainedEntityB).select(ContainedEntityB.getField("id"));
        StorageResults records = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, records.getCount());
        } finally {
            storage.commit();
        }
        // Delete all 'FKtoMultiB' list records ()
        allRecords = new LinkedList<DataRecord>();
        allRecords
                .add(factory
                        .read(repository,
                                ContainedEntityA,
                                "<ContainedEntityA xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Aid>a_id</Aid><FKtoB><Bid/></FKtoB><FKtoMultiB><Bid></Bid></FKtoMultiB></ContainedEntityA>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        // Delete all remaining records
        qb = from(ContainedEntityB).where(startsWith(ContainedEntityB.getField("id"), "B_record"));
        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
        // Test actual deletion of remaining 'recordN'.
        storage.begin();
        qb = from(ContainedEntityB).select(ContainedEntityB.getField("id"));
        records = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, records.getCount());
        } finally {
            storage.commit();
        }
    }

    public void testSingleRelationRecordsChangeWithFK() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        // Add 'record1'
        allRecords
                .add(factory
                        .read(repository,
                                ContainedEntityA,
                                "<ContainedEntityA xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Aid>a_id</Aid><FKtoB><Bid>[B_record1]</Bid></FKtoB></ContainedEntityA>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        // Update 'record1' to 'record2'
        allRecords = new LinkedList<DataRecord>();
        allRecords
                .add(factory
                        .read(repository,
                                ContainedEntityA,
                                "<ContainedEntityA xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Aid>a_id</Aid><FKtoB><Bid>[B_record2]</Bid></FKtoB></ContainedEntityA>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        // Delete 'record1' which is no longer used
        UserQueryBuilder qb = from(ContainedEntityB).where(contains(ContainedEntityB.getField("id"), "B_record1"));
        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
        // Update 'FKtoB' field to null
        allRecords = new LinkedList<DataRecord>();
        allRecords
                .add(factory
                        .read(repository,
                                ContainedEntityA,
                                "<ContainedEntityA xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Aid>a_id</Aid><FKtoB><Bid></Bid></FKtoB><FKtoMultiB><Bid/></FKtoMultiB></ContainedEntityA>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        // Delete 'record2' which is no longer used
        qb = from(ContainedEntityB).where(contains(ContainedEntityB.getField("id"), "B_record2"));
        storage.begin();
        storage.delete(qb.getSelect());
        storage.commit();
    }

    public void testEmptyOrNullOnFK() throws Exception {
        FieldMetadata field = address.getField("country");

        UserQueryBuilder qb = from(address).where(isNull(field));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        qb = from(address).where(or(isNull(field), isEmpty(field)));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();

        qb = from(address).where(emptyOrNull(field));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();
        
        qb = UserQueryBuilder.from(product).where(emptyOrNull(product.getField("Stores/Store")));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();
        
        qb = UserQueryBuilder.from(product).where(emptyOrNull(product.getField("Family")));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();
        
        qb = UserQueryBuilder.from(product).where(emptyOrNull(product.getField("Features/Sizes/Size")));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();
        
        qb = from(organisation).where(emptyOrNull(organisation.getField("locations/location")));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
        storage.commit();
        
    }

    public void testSubEntityContainedFK() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        // Add 'record6' to ContainedEntityB
        allRecords.add(factory.read(repository, ContainedEntityB,
                "<ContainedEntityB xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><id>record6</id></ContainedEntityB>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        // Add ContainedEntityC record that CsubType/Sub_FK_to_B point to ContainedEntityB "record6"
        allRecords = new LinkedList<DataRecord>();
        allRecords
                .add(factory
                        .read(repository,
                                ContainedEntityC,
                                "<ContainedEntityC xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Cid>c1</Cid><FK_to_B>[record6]</FK_to_B></ContainedEntityC>"));
        storage.begin();
        storage.update(allRecords);
        storage.commit();
        // Can find relation records "c1" from "record6"
        storage.begin();
        UserQueryBuilder qb = from(ContainedEntityC).selectId(ContainedEntityC).where(
                or(eq(ContainedEntityC.getField("FK_to_B"), "record6"),
                        eq(ContainedEntityC.getField("CsubType/Sub_FK_to_B"), "record6")));
        StorageResults records = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, records.getCount());
        } finally {
            storage.commit();
        }
    }

    public void testOrderByExpression() throws Exception {
        // Most common to least common order (DESC).
        UserQueryBuilder qb = from(person).select(person.getField("firstname")).orderBy(count(person.getField("firstname")),
                OrderBy.Direction.DESC);
        storage.begin();
        StorageResults records = storage.fetch(qb.getSelect());
        try {
            // First should be "Julien" (2 occurrences in test data).
            try {
                for (DataRecord record : records) {
                    assertEquals("Julien", record.get("firstname"));
                    break;
                }
            } finally {
                records.close();
            }
        } finally {
            storage.commit();
        }
        // Least common to most common order (ASC).
        storage.begin();
        qb = from(person).select(person.getField("firstname"))
                .orderBy(count(person.getField("firstname")), OrderBy.Direction.ASC);
        records = storage.fetch(qb.getSelect());
        try {
            // Last should be "Julien" (2 occurrences in test data).
            try {
                String lastValue = null;
                for (DataRecord record : records) {
                    lastValue = String.valueOf(record.get("firstname"));
                }
                assertEquals("Julien", lastValue);
            } finally {
                records.close();
            }
        } finally {
            storage.commit();
        }
    }

    @SuppressWarnings("rawtypes")
	public void testSetValueToFKPointToSelfEntity() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        FieldMetadata fieldID = PointToSelfEntity.getField("Id");
        FieldMetadata fieldFK = PointToSelfEntity.getField("FirstFK");
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        // Add 3 records without setting FK
        allRecords
                .add(factory
                        .read(repository,
                                PointToSelfEntity,
                                "<PointToSelfEntity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>id1</Id><Name>name1</Name></PointToSelfEntity>"));
        allRecords
		        .add(factory
		                .read(repository,
		                        PointToSelfEntity,
		                        "<PointToSelfEntity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>id2</Id><Name>name2</Name></PointToSelfEntity>"));
		allRecords
		        .add(factory
		                .read(repository,
		                        PointToSelfEntity,
		                        "<PointToSelfEntity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>id3</Id><Name>name3</Name></PointToSelfEntity>"));
		storage.begin();
        storage.update(allRecords);
        storage.commit();

        // Update their FK
        allRecords = new LinkedList<DataRecord>();
        allRecords
		        .add(factory
		                .read(repository,
		                        PointToSelfEntity,
		                        "<PointToSelfEntity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>id1</Id><Name>name1</Name><FirstFK>[id1]</FirstFK></PointToSelfEntity>"));
        allRecords
		        .add(factory
		                .read(repository,
		                        PointToSelfEntity,
		                        "<PointToSelfEntity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>id2</Id><Name>name2</Name><FirstFK>[id3]</FirstFK></PointToSelfEntity>"));
		allRecords
		        .add(factory
		                .read(repository,
		                        PointToSelfEntity,
		                        "<PointToSelfEntity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Id>id3</Id><Name>name3</Name><FirstFK>[id2]</FirstFK></PointToSelfEntity>"));
		storage.begin();
        storage.update(allRecords);
        storage.commit();
        
        storage.begin();
        UserQueryBuilder qb = from(PointToSelfEntity);
        StorageResults records = storage.fetch(qb.getSelect());
		try {
			assertEquals(3, records.getCount());
			boolean valid1 = false;
		    boolean valid2 = false;
		    boolean valid3 = false;
			for (DataRecord result : records) {
				String id = (String)result.get(fieldID);
				DataRecord recordFK = (DataRecord) ((List) result.get(fieldFK)).get(0);
				if ("id1".equals(id)) {
					valid1 = "id1".equals(recordFK.get(fieldID));
				} else if ("id2".equals(id)) {
					valid2 = "id3".equals(recordFK.get(fieldID));
				} else {
					valid3 = "id2".equals(recordFK.get(fieldID));
				}
			}
			assertTrue(valid1 && valid2 && valid3);
		} finally {
			storage.commit();
		}
    }

    public void testRepeatableElementsCount() throws Exception {
        UserQueryBuilder qb = from(repeatableElementsEntity).select(repeatableElementsEntity.getField("id"))
                .select(repeatableElementsEntity.getField("info/name")).limit(20).start(0);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(3, results.getCount());
        } finally {
            results.close();
        }
    }

    public void testOrderByFk() throws Exception {
        // Most common to least common order (DESC).
        UserQueryBuilder qb = from(tt).select(tt.getField("Id")).select(tt.getField("MUl/E3"))
                .orderBy(tt.getField("MUl/E3"), OrderBy.Direction.DESC);
        storage.begin();
        StorageResults records = storage.fetch(qb.getSelect());
        try {
            try {
                for (DataRecord record : records) {
                    assertEquals("T3", record.get("Id"));
                    break;
                }
            } finally {
                records.close();
            }
        } finally {
            storage.commit();
        }

        qb = from(tt).select(tt.getField("Id")).select(tt.getField("MUl/E3"))
                .orderBy(tt.getField("MUl/E3"), OrderBy.Direction.ASC);
        storage.begin();
        records = storage.fetch(qb.getSelect());
        try {
            try {
                for (DataRecord record : records) {
                    assertEquals("T1", record.get("Id"));
                    break;
                }
            } finally {
                records.close();
            }
        } finally {
            storage.commit();
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testQueryWithFKContainsMultipleValues() throws Exception {
        FieldMetadata field = personne.getField("Contextes/ContexteFk");
        UserQueryBuilder qb = from(personne).where(contains(field, "111"));
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            List<Object> contextes = (List<Object>)results.iterator().next().get(field);
            assertEquals(3, contextes.size());
        } finally {
            results.close();
        }
        
        qb = from(personne).where(contains(field, "222"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            List<Object> contextes = (List<Object>)results.iterator().next().get(field);
            assertEquals(3, contextes.size());
        } finally {
            results.close();
        }
        
        qb = from(personne).where(contains(field, "333"));
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            List<Object> contextes = (List<Object>)results.iterator().next().get(field);
            assertEquals(3, contextes.size());
        } finally {
            results.close();
        }
        
        storage.commit();
    }

    public void testIdContainsSlash() throws Exception {
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        DataRecord record1 = factory.read(repository, tt,
                "<TT><Id>Slash/Id</Id><MUl><E1>1</E1><E2>1</E2><E3>[R1]</E3></MUl></TT>");
        try {
            storage.begin();
            storage.update(record1);
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = UserQueryBuilder.from(tt);
        String fieldName = "TT/Id";
        IWhereItem item = new WhereOr(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.EQUALS, "Slash/Id",
                WhereCondition.NO_OPERATOR)));
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));
        storage.begin();
        StorageResults storageResults = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, storageResults.getCount());
        } finally {
            storageResults.close();
            storage.commit();
        }
    }
    
    public void testQueryHierachyWhenEntityContainsFKReferToEntitySelf() {
        UserQueryBuilder qb = from(hierarchy);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getSize());
        } finally {
            results.close();
            storage.commit();
        }
    }
    
    public void testQueryByFieldInComplexTypeWhileEntityInheritsAnotherType(){
        FieldMetadata field_1 = checkPointDetails_1.getField("PointAddressDetails/AddressDetails/TerritoryCode");
        UserQueryBuilder qb_1 = from(checkPointDetails_1).where(eq(field_1, "2"));
        StorageResults results_1 = null;
        Exception ex_1 = null;
        try {
            results_1 = storage.fetch(qb_1.getSelect());
        } catch(Exception e){
            ex_1 = e;
        } finally {
            if(results_1 != null){
                results_1.close();
            }
        }
        if(ex_1 != null)
            ex_1.printStackTrace();
        assertNull(ex_1);
        
        FieldMetadata field_2 = checkPointDetails_2.getField("PointAddressDetails/AddressDetails/TerritoryCode");
        UserQueryBuilder qb_2 = from(checkPointDetails_2).where(eq(field_2, "2"));
        StorageResults results_2 = null;
        Exception ex_2 = null;
        try {
            results_2 = storage.fetch(qb_2.getSelect());
        } catch(Exception e){
            ex_2 = e;
        } finally {
            if(results_2 != null){
                results_2.close();
            }
        }
        if(ex_2 != null)
            ex_2.printStackTrace();
        assertNull(ex_2);
    }

    public void testPathContainsMultiChildQuery() throws Exception {
        UserQueryBuilder qb = from(t_entity).select(e_entity.getField("E_EntityId"))
                .where(or(eq(t_entity.getField("T_Field/T1_Field1/A1_Field1/element/elementB"), "E_id1")
                        , eq(t_entity.getField("T_Field/T1_Field1/A1_Field2/element/elementB"), "E_id1")))
                .join(t_entity.getField("T_Field/T1_Field1/A1_Field1/element/elementB"));

        assertTrue(qb.getSelect().normalize() instanceof Select);
        Select sel = (Select) qb.getSelect().normalize();
        assertTrue(sel.getCondition() instanceof BinaryLogicOperator);
        BinaryLogicOperator blo = (BinaryLogicOperator) sel.getCondition();
        assertNotNull(blo.getLeft());
        assertNotNull(blo.getRight());
        assertTrue(blo.getLeft() instanceof Compare);
        assertTrue(blo.getRight() instanceof Compare);
        
        StorageResults results = storage.fetch(sel);
        try {
            assertEquals(results.getCount(), 1);
        } finally {
            results.close();
        }
        
        qb = from(t_entity).select(e_entity.getField("E_EntityId"))
                .where(or(eq(t_entity.getField("T_Field/T1_Field1/A1_Field1/element/elementB"), "E_id2")
                        , eq(t_entity.getField("T_Field/T1_Field1/A1_Field2/element/elementB"), "E_id2")))
                .join(t_entity.getField("T_Field/T1_Field1/A1_Field2/element/elementB"));
        
        sel = (Select) qb.getSelect().normalize();
        results = storage.fetch(sel);
        try {
            assertEquals(results.getCount(), 1);
        } finally {
            results.close();
        }
    }
    
    public void testTMDM8828() {
        UserQueryBuilder qb = from(organisation);

        List<String> viewables = new ArrayList<String>();
        viewables.add("Organisation/OrganisationId");
        viewables.add("Location/LocationId");
        viewables.add("Location/translation/locationTranslation");
        
        // add order by Id to make the test stable
        qb.orderBy(organisation.getField("OrganisationId"), Direction.ASC);
        
        List<IWhereItem> conditions = new ArrayList<IWhereItem>();
        conditions.add(new WhereCondition("Organisation/locations/location", "JOINS", "Location/LocationId", "&"));
        conditions.add(new WhereCondition("Organisation/OrganisationId", "=", "2", "&"));

        IWhereItem fullWhere = new WhereAnd(conditions);
        qb.where(UserQueryHelper.buildCondition(qb, fullWhere, repository));
        
        for (String viewableBusinessElement : viewables) {
            String viewableTypeName = StringUtils.substringBefore(viewableBusinessElement, "/"); //$NON-NLS-1$
            String viewablePath = StringUtils.substringAfter(viewableBusinessElement, "/"); //$NON-NLS-1$
            TypedExpression expression = UserQueryHelper.getFields(repository.getComplexType(viewableTypeName), viewablePath).get(0);
            qb.select(expression);
        }

        StorageResults results = storage.fetch(qb.getSelect());
        assertEquals(2, results.getCount());

        DataRecordWriter writer = new ViewSearchResultsWriter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> strings = new ArrayList<String>();
        for (DataRecord result : results) {
            try {
                writer.write(result, output);
                String document = new String(output.toByteArray(), Charset.forName("UTF-8"));
                strings.add(document);
                output.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        assertEquals(2, strings.size());
        assertEquals(
                "<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n\t<OrganisationId>2</OrganisationId>\n\t<LocationId>t2</LocationId>\n\t<locationTranslation>Trans1</locationTranslation>\n</result>",
                strings.get(0));
        assertEquals(
                "<result xmlns:metadata=\"http://www.talend.com/mdm/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n\t<OrganisationId>2</OrganisationId>\n\t<LocationId>t2</LocationId>\n\t<locationTranslation>Trans2</locationTranslation>\n</result>",
                strings.get(1));
    }
    
    public void testAdvancedSearchWithMultiCondition() throws Exception {
        UserQueryBuilder qb = from(person).where(and(contains(person.getField("lastname"), "Du*"), contains(person.getField("middlename"), "jo*")));
        StorageResults results = storage.fetch(qb.getSelect());
        List<String> ids = new ArrayList<String>();
        ids.add("1");
        ids.add("2");
        try{
            assertEquals(2, results.getSize());
            for (DataRecord result : results) {
                this.assertTrue(ids.contains(result.get(person.getField("id"))));
            }
        } finally {
            results.close();
        }
    }

    public void testQueryNoAccessField() {
        UserQueryBuilder qb = from(person).where(eq(person.getField("id"), "1"));

        StorageResults results = storage.fetch(qb.getSelect());
        DataRecordXmlWriter writer = new DataRecordXmlWriter();
        writer.setSecurityDelegator(new TestUserDelegator());
        try {
            // System_Users have no access to status field.
            String expectedXml = "<Person><id>1</id><firstname>Julien</firstname><middlename>John</middlename><lastname>"
                    + "Dupond</lastname><resume>[EN:my splendid resume, splendid isn&apos;t it][FR:mon magnifique resume, n&apos;est ce pas ?]</resume>"
                    + "<age>10</age><score>130000.00</score><Available>true</Available><addresses><address>[2&amp;2][true]</address><address>"
                    + "[1][false]</address></addresses></Person>";
            String expectedXml2 = "<Person><id>1</id><firstname>Julien</firstname><middlename>John</middlename><lastname>"
                    + "Dupond</lastname><resume>[EN:my splendid resume, splendid isn&apos;t it][FR:mon magnifique resume, n&apos;est ce pas ?]</resume>"
                    + "<age>10</age><score>130000</score><Available>true</Available><addresses><address>[2&amp;2][true]</address><address>"
                    + "[1][false]</address></addresses></Person>";
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (DataRecord result : results) {
                try {
                    writer.write(result, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String actual = new String(output.toByteArray());
            if (!"Oracle".equalsIgnoreCase(DATABASE)) {
                assertEquals(expectedXml, actual);
            } else {
                assertEquals(expectedXml2, actual);
            }
        } finally {
            results.close();
        }
    }

    //TMDM-9703 tMDMInput with one filter return different records size by different batch size
    public void test_largeVolumeData() throws Exception {
        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();

        for (int i = 1; i <= 1500; i++) {

            DataRecord record = factory.read(repository, product, "<Product>\n" + "    <Id>P-" + i + "</Id>\n"
                    + "    <Name>Product name</Name>\n" + "    <ShortDescription>Short description word</ShortDescription>\n"
                    + "    <LongDescription>Long description</LongDescription>\n" + "    <Price>10</Price>\n"
                    + "    <Features>\n" + "        <Sizes>\n" + "            <Size>Small</Size>\n"
                    + "            <Size>Medium</Size>\n" + "            <Size>Large</Size>\n" + "        </Sizes>\n"
                    + "        <Colors>\n" + "            <Color>Blue</Color>\n" + "            <Color>Red</Color>\n"
                    + "        </Colors>\n" + "    </Features>\n" + "    <Status>Pending</Status>\n"
                    + "    <Family>[2]</Family>\n" + "    <Supplier>[1]</Supplier>\n" + "</Product>");
            allRecords.add(record);
        }

        try {
            storage.begin();
            storage.update(allRecords);
            storage.commit();
        } finally {
            storage.end();
        }
        int start = 0;
        int limit = 50;

        UserQueryBuilder qb = UserQueryBuilder.from(product).where(startsWith(product.getField("Id"), "P-"));
        // Condition and paging
        qb.start(start < 0 ? 0 : start); // UI can send negative start index
        qb.limit(limit);

        StorageResults results = storage.fetch(qb.getSelect());

        assertEquals(1500, results.getCount());
        assertEquals(50, results.getSize());
        int i = 0;
        for (DataRecord result : results) {
            if(result.get("Id") != null){
                i++;
            }
        }
        assertEquals(50, i);

        qb = UserQueryBuilder.from(product).where(startsWith(product.getField("Id"), "P-"));
        try {
            storage.begin();
            storage.delete(qb.getSelect());
            storage.commit();
        } finally {
            storage.end();
        }

        qb = UserQueryBuilder.from(product);

        results = storage.fetch(qb.getSelect());
        assertEquals(2, results.getCount());
    }

    private static class TestUserDelegator implements SecuredStorage.UserDelegator {

        boolean isActive = true;

        public void setActive(boolean active) {
            isActive = active;
        }

        @Override
        public boolean hide(FieldMetadata field) {
            return isActive && field.getHideUsers().contains("System_Users");
        }

        @Override
        public boolean hide(ComplexTypeMetadata type) {
            return isActive && type.getHideUsers().contains("System_Users");
        }
    }

}
