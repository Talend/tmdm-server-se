/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

@SuppressWarnings("nls")
public class ClientResourceData {

    public static String getRecordProduct1() {
        StringBuffer record = new StringBuffer();
        record.append("<Product>");
        record.append("<Id>1</Id>");
        record.append("<Name>Test product 1</Name>");
        record.append("<Price>10</Price>");
        record.append("<OnlineStore>@@http://</OnlineStore>");
        record.append("<Availability>false</Availability>");
        record.append("<Picture>http://www.talendforge.org/img/style/talendforge.jpg</Picture>");
        record.append("</Product>");
        return record.toString();
    }

    public static String getRecordProduct2() {
        StringBuffer record = new StringBuffer();
        record.append("<Product> ");
        record.append("<Id>231035935</Id> ");
        record.append("<Name>Talend Golf Shirt</Name> ");
        record.append("<Description>Golf-style, collared t-shirt</Description> ");
        record.append("<Features> ");
        record.append("<Colors> ");
        record.append("<Color></Color> ");
        record.append("</Colors> ");
        record.append("<Sizes> ");
        record.append("<Size></Size> ");
        record.append("<Size>Medium</Size> ");
        record.append("<Size/> ");
        record.append("</Sizes> ");
        record.append("</Features> ");
        record.append("<Availability>true</Availability> ");
        record.append("<Price>16.99</Price> ");
        record.append("<Family>[1]</Family> ");
        record.append("<Picture>/imageserver/upload/TalendShop/golf_shirt.jpg</Picture> ");
        record.append("<OnlineStore>https://unknownhost/test</OnlineStore> ");
        record.append("</Product> ");
        return record.toString();
    }

    public static String getRecordProduct3() {
        StringBuffer record = new StringBuffer();
        record.append("<Product>");
        record.append("<Id>1</Id>");
        record.append("<Name>Test product 1</Name>");
        record.append("<Price>10</Price>");
        record.append("<Features> ");
        record.append("<Sizes> ");
        record.append("<Size>Large</Size> ");
        record.append("</Sizes> ");
        record.append("</Features> ");
        record.append("<OnlineStore>@@http://</OnlineStore>");
        record.append("<Availability>false</Availability>");
        record.append("<Picture>http://www.talendforge.org/img/style/talendforge.jpg</Picture>");
        record.append("</Product>");
        return record.toString();
    }

    public static String getModelProduct() {
        StringBuffer model = new StringBuffer();
        model.append("<models concept=\"Product\">");
        model.append("  <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Product\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Product/Id\" name=\"Id\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Product/Name\" name=\"Name\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"false\"  minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Product/Description\" name=\"Description\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"false\"  minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Features\" name=\"Features\" dataType=\"anonymous\"/>");
        model.append("     <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Features/Sizes\" name=\"Sizes\" dataType=\"anonymous\"/>");
        model.append("        <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"-1\" typePath=\"Product/Features/Sizes/Size\" name=\"Size\" dataType=\"Size\"/>");
        model.append("     <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Features/Colors\" name=\"Colors\" dataType=\"anonymous\"/>");
        model.append("        <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"1\"  maxOccurs=\"-1\" typePath=\"Product/Features/Colors/Color\" name=\"Color\" dataType=\"Color\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Availability\" name=\"Availability\" dataType=\"boolean\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Product/Price\" name=\"Price\" dataType=\"decimal\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"true\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Family\" name=\"Family\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/OnlineStore\" name=\"OnlineStore\" dataType=\"URL\"/>");
        model.append("     <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Stores\" name=\"Stores\" dataType=\"anonymous\"/>");
        model.append("        <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"0\"  maxOccurs=\"-1\" typePath=\"Product/Stores/Store\" name=\"Store\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Picture\" name=\"Picture\" dataType=\"PICTURE\"/>");
        model.append("</models>");
        return model.toString();
    }

    public static String getRecordCustomer1() {
        StringBuffer record = new StringBuffer();
        record.append("<Customer>");
        record.append("<firstname>Mike</firstname>");
        record.append("<lastname>Burn</lastname>");
        record.append("<description>VIP Customer</description>");
        record.append("</Customer>");
        return record.toString();
    }

    public static String getModelCustomer() {
        StringBuffer model = new StringBuffer();
        model.append("<models concept=\"Customer\">");
        model.append("  <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Customer\" name=\"\" dataType=\"\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"true\" isFk=\"false\" isReadOnly=\"true\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Customer/firstname\" name=\"firstname\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"true\" isFk=\"false\" isReadOnly=\"true\" isVisible=\"false\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Customer/lastname\" name=\"lastname\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Customer/description\" name=\"description\" dataType=\"string\"/>");
        model.append("</models>");
        return model.toString();
    }

    public static String getRecordContract1() {
        StringBuffer record = new StringBuffer();
        record.append("<Contract>");
        record.append("<id>001</id>");
        record.append("<description>VIP Customer</description>");
        record.append("<owner>Starkey</owner>");
        record.append("<effectiveDate>1999-09-01</effectiveDate>");
        record.append("<partyNotes>");
        record.append("<firstPartyNote>First party agree</firstPartyNote>");
        record.append("<secondPartyNote>Second party agree</secondPartyNote>");
        record.append("</partyNotes>");
        record.append("</Contract>");
        return record.toString();
    }

    public static String getModelContract() {
        StringBuffer model = new StringBuffer();
        model.append("<models concept=\"Contract\">");
        model.append("  <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Contract\" name=\"\" dataType=\"\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"true\" isFk=\"false\" isReadOnly=\"true\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Contract/id\" name=\"id\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Contract/description\" name=\"description\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"true\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Contract/owner\" name=\"owner\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"true\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Contract/effectiveDate\" name=\"effectiveDate\" dataType=\"dateTime\"/>");
        model.append("  <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"true\" isVisible=\"true\"  minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Contract/partyNotes\" name=\"partyNotes\" dataType=\"anonymous\"/>");
        model.append("     <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Contract/partyNotes/firstPartyNote\" name=\"firstPartyNote\" dataType=\"string\"/>");
        model.append("     <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"true\" isVisible=\"true\" minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Contract/partyNotes/secondPartyNote\" name=\"secondPartyNote\" dataType=\"string\"/>");
        model.append("</models>");
        return model.toString();
    }

    public static String getRecordA() {
        StringBuffer recordA = new StringBuffer();
        recordA.append("<Test>");
        recordA.append("<subelement>111</subelement>");
        recordA.append("<name>zhang</name>");
        recordA.append("<age>25</age>");
        recordA.append("<memo>hello, morning</memo>");
        recordA.append("</Test>");
        return recordA.toString();
    }

    public static String getModelA() {
        StringBuffer modelA = new StringBuffer();
        modelA.append("<models concept=\"Test\">");
        modelA.append("<model isSimple=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test\" name=\"Test\" dataType=\"Test\"/>");
        modelA.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/subelement\" name=\"subelement\" dataType=\"string\"/>");
        modelA.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/name\" name=\"name\" dataType=\"string\"/>");
        modelA.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/age\" name=\"age\" dataType=\"int\"/>");
        modelA.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/memo\" name=\"memo\" dataType=\"string\"/>");
        modelA.append("</models>");
        return modelA.toString();
    }

    public static String getRecordB() {
        StringBuffer recordB = new StringBuffer();
        recordB.append("<Test>");
        recordB.append("<subelement>111</subelement>");
        recordB.append("<name></name>");
        recordB.append("<age>25</age>");
        recordB.append("<memo></memo>");
        recordB.append("</Test>");
        return recordB.toString();
    }

    public static String getModelB() {
        StringBuffer modelB = new StringBuffer();
        modelB.append("<models concept=\"Test\">");
        modelB.append("<model isSimple=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test\" name=\"Test\" dataType=\"Test\"/>");
        modelB.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/subelement\" name=\"subelement\" dataType=\"string\"/>");
        modelB.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/name\" name=\"name\" dataType=\"string\"/>");
        modelB.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/age\" name=\"age\" dataType=\"int\"/>");
        modelB.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/memo\" name=\"memo\" dataType=\"string\"/>");
        modelB.append("</models>");
        return modelB.toString();
    }

    public static String getRecordC() {
        StringBuffer recordC = new StringBuffer();
        recordC.append("<Test>");
        recordC.append("<subelement>111</subelement>");
        recordC.append("<name>zhang</name>");
        recordC.append("<age>25</age>");
        recordC.append("<memo>I'm zhang yang</memo>");
        recordC.append("</Test>");
        return recordC.toString();
    }

    public static String getModelC() {
        StringBuffer modelC = new StringBuffer();
        modelC.append("<models concept=\"Test\">");
        modelC.append("<model isSimple=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test\" name=\"Test\" dataType=\"Test\"/>");
        modelC.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/subelement\" name=\"subelement\" dataType=\"string\"/>");
        modelC.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"false\" typePath=\"Test/name\" name=\"name\" dataType=\"string\"/>");
        modelC.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"false\" typePath=\"Test/age\" name=\"age\" dataType=\"int\"/>");
        modelC.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"false\" typePath=\"Test/memo\" name=\"memo\" dataType=\"string\"/>");
        modelC.append("</models>");
        return modelC.toString();
    }

    public static String getRecordD() {
        StringBuffer recordD = new StringBuffer();
        recordD.append("<Test>");
        recordD.append("<subelement>111</subelement>");
        recordD.append("<name>zhang</name>");
        recordD.append("<age>25</age>");
        recordD.append("<memo>I'm zhang yang</memo>");
        recordD.append("</Test>");
        return recordD.toString();
    }

    public static String getModelD() {
        StringBuffer modelD = new StringBuffer();
        modelD.append("<models concept=\"Test\">");
        modelD.append("<model isSimple=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test\" name=\"Test\" dataType=\"Test\"/>");
        modelD.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/subelement\" name=\"subelement\" dataType=\"string\"/>");
        modelD.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/name\" name=\"name\" dataType=\"string\"/>");
        modelD.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"true\" isVisible=\"true\" typePath=\"Test/age\" name=\"age\" dataType=\"int\"/>");
        modelD.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"true\" isVisible=\"true\" typePath=\"Test/memo\" name=\"memo\" dataType=\"string\"/>");
        modelD.append("</models>");
        return modelD.toString();
    }

    public static String getRecordE() {
        StringBuffer recordE = new StringBuffer();
        recordE.append("<Test>");
        recordE.append("<subelement>111</subelement>");
        recordE.append("<name>zhang</name>");
        recordE.append("<age>25</age>");
        recordE.append("<memo></memo>");
        recordE.append("<memo></memo>");
        recordE.append("<memo></memo>");
        recordE.append("<memo></memo>");
        recordE.append("</Test>");
        return recordE.toString();
    }

    public static String getModelE() {
        StringBuffer modelE = new StringBuffer();
        modelE.append("<models concept=\"Test\">");
        modelE.append("<model isSimple=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test\" name=\"Test\" dataType=\"Test\"/>");
        modelE.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/subelement\" name=\"subelement\" dataType=\"string\"/>");
        modelE.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/name\" name=\"name\" dataType=\"string\"/>");
        modelE.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/age\" name=\"age\" dataType=\"int\"/>");
        modelE.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"0\"  maxOccurs=\"-1\" typePath=\"Test/memo\" name=\"memo\" dataType=\"string\"/>");
        modelE.append("</models>");
        return modelE.toString();
    }

    public static String getRecordF() {
        StringBuffer recordF = new StringBuffer();
        recordF.append("<Test>");
        recordF.append("<subelement>111</subelement>");
        recordF.append("<name>zhang</name>");
        recordF.append("<age>25</age>");
        recordF.append("<memo></memo>");
        recordF.append("<memo>hello</memo>");
        recordF.append("<memo></memo>");
        recordF.append("<memo>bye</memo>");
        recordF.append("</Test>");
        return recordF.toString();
    }

    public static String getModelF() {
        StringBuffer modelF = new StringBuffer();
        modelF.append("<models concept=\"Test\">");
        modelF.append("<model isSimple=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test\" name=\"Test\" dataType=\"Test\"/>");
        modelF.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/subelement\" name=\"subelement\" dataType=\"string\"/>");
        modelF.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/name\" name=\"name\" dataType=\"string\"/>");
        modelF.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/age\" name=\"age\" dataType=\"int\"/>");
        modelF.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/memo\" name=\"memo\" dataType=\"string\"/>");
        modelF.append("</models>");
        return modelF.toString();
    }

    public static String getRecordG() {
        StringBuffer recordG = new StringBuffer();
        recordG.append("<Test>");
        recordG.append("<subelement>111</subelement>");
        recordG.append("<name>zhang</name>");
        recordG.append("<age></age>");
        recordG.append("<memo></memo>");
        recordG.append("<memo>hello</memo>");
        recordG.append("<memo></memo>");
        recordG.append("<memo>bye</memo>");
        recordG.append("<cp>");
        recordG.append("<title></title>");
        recordG.append("<address></address>");
        recordG.append("<address></address>");
        recordG.append("<address></address>");
        recordG.append("</cp>");
        recordG.append("</Test>");
        return recordG.toString();
    }

    public static String getModelG() {
        StringBuffer modelG = new StringBuffer();
        modelG.append("<models concept=\"Test\">");
        modelG.append("<model isSimple=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test\" name=\"Test\" dataType=\"Test\"/>");
        modelG.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/subelement\" name=\"subelement\" dataType=\"string\"/>");
        modelG.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/name\" name=\"name\" dataType=\"string\"/>");
        modelG.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/age\" name=\"age\" dataType=\"int\"/>");
        modelG.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/memo\" name=\"memo\" dataType=\"string\"/>");
        modelG.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/cp\" name=\"cp\" dataType=\"CP\"/>");
        modelG.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/cp/title\" name=\"title\" dataType=\"string\"/>");
        modelG.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/cp/address\" name=\"address\" dataType=\"string\"/>");
        modelG.append("</models>");
        return modelG.toString();
    }

    public static String getRecordH() {
        StringBuffer recordH = new StringBuffer();
        recordH.append("<Test>"); //$NON-NLS-1$
        recordH.append("<id>1</id>"); //$NON-NLS-1$
        recordH.append("<name>test</name>"); //$NON-NLS-1$
        recordH.append("<cp>"); //$NON-NLS-1$
        recordH.append("<title>Hello</title>"); //$NON-NLS-1$
        recordH.append("<address>Beijing</address>"); //$NON-NLS-1$
        recordH.append("<fk>North Five Loop</fk>"); //$NON-NLS-1$
        recordH.append("<cpChild>"); //$NON-NLS-1$
        recordH.append("<tel>13800000000</tel>"); //$NON-NLS-1$
        recordH.append("</cpChild>"); //$NON-NLS-1$
        recordH.append("</cp>"); //$NON-NLS-1$
        recordH.append("</Test>"); //$NON-NLS-1$
        return recordH.toString();
    }

    public static String getModelH() {
        StringBuffer modelH = new StringBuffer();
        modelH.append("<models concept=\"Test\">"); //$NON-NLS-1$
        modelH.append("<model isSimple=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test\" name=\"Test\" dataType=\"Test\"/>"); //$NON-NLS-1$
        modelH.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/id\" name=\"id\" dataType=\"string\"/>"); //$NON-NLS-1$
        modelH.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/name\" name=\"name\" dataType=\"string\"/>"); //$NON-NLS-1$
        modelH.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/cp\" name=\"cp\" dataType=\"CP\"/>"); //$NON-NLS-1$
        modelH.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/cp/title\" name=\"title\" dataType=\"string\"/>"); //$NON-NLS-1$
        modelH.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/cp/address\" name=\"address\" dataType=\"string\"/>"); //$NON-NLS-1$
        modelH.append("<model isSimple=\"true\" isFk=\"true\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/cp/fk\" name=\"fk\" dataType=\"string\"/>"); //$NON-NLS-1$
        modelH.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/cp/cpChild\" name=\"cpChild\" dataType=\"anonymous\"/>"); //$NON-NLS-1$
        modelH.append("<model isSimple=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Test/cp/cpChild/tel\" name=\"tel\" dataType=\"string\"/>"); //$NON-NLS-1$
        modelH.append("</models>"); //$NON-NLS-1$
        return modelH.toString();
    }

    public static String getRecordProductWithStore() {
        StringBuffer record = new StringBuffer();
        record.append("<Product>");
        record.append("<Id>2000</Id>");
        record.append("<Name>Talend Golf Shirt</Name>");
        record.append("<Family>[1]</Family>");
        record.append("<Stores>");
        record.append("<Store>[1]</Store>");
        record.append("<Store>[2]</Store>");
        record.append("<Store>[3]</Store>");
        record.append("</Stores>");
        record.append("</Product>");
        return record.toString();
    }

    public static String getModelProductWithStore() {
        StringBuffer model = new StringBuffer();
        model.append("<models concept=\"Product\">");
        model.append("  <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Product\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Product/Id\" name=\"Id\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Product/Name\" name=\"Name\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"true\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Family\" name=\"Family\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Stores\" name=\"Stores\" dataType=\"anonymous\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"0\"  maxOccurs=\"-1\" typePath=\"Product/Stores/Store\" name=\"Store\" dataType=\"string\"/>");
        model.append("</models>");
        return model.toString();
    }

    public static String getModelProductFamily() {
        StringBuffer model = new StringBuffer();
        model.append("<models concept=\"ProductFamily\">");
        model.append("  <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"ProductFamily\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"ProductFamily/Id\" name=\"Id\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"true\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"ProductFamily/Name\" name=\"Name\" dataType=\"string\" defaultValueExpression=\"name\"/>");
        model.append("</models>");
        return model.toString();
    }

    public static String getRecordProductFamily() {
        StringBuffer model = new StringBuffer();
        model.append("<ProductFamily>");
        model.append("<Id></Id>");
        model.append("<Name>name</Name>");
        model.append("</ProductFamily>");
        return model.toString();
    }

    public static String getModelProductWithSupplier() {
        StringBuffer model = new StringBuffer();
        model.append("<models concept=\"Product\">");
        model.append("  <model isSimple=\"false\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" typePath=\"Product\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"true\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Product/Id\" name=\"Id\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"false\" isReadOnly=\"false\" isVisible=\"true\" minOccurs=\"1\"  maxOccurs=\"1\" typePath=\"Product/Name\" name=\"Name\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"true\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Family\" name=\"Family\" dataType=\"string\"/>");
        model.append("  <model isSimple=\"true\" isKey=\"false\" isFk=\"true\" isReadOnly=\"false\" isVisible=\"true\"  minOccurs=\"0\"  maxOccurs=\"1\" typePath=\"Product/Supplier\" name=\"Supplier\" dataType=\"string\"/>");
        model.append("</models>");
        return model.toString();
    }

    public static String getRecordProductWithSupplier() {
        StringBuffer record = new StringBuffer();
        record.append("<Product xmlns:tmdm=\"http://www.talend.com/mdm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        record.append("<Id>1</Id>");
        record.append("<Name>Talend MDM</Name>");
        record.append("<Family>[1]</Family>");
        record.append("<Supplier tmdm:type=\"Company\">[1]</Supplier>");
        record.append("</Product>");
        return record.toString();
    }
}
