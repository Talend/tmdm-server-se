/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.ruleengine;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;

public class DisplayRuleTestData {

    private static final Logger LOG = Logger.getLogger(DisplayRuleTestData.class);

    public static Document getDocument(String name) {
        Document document = null;
        try {
            InputStream is = DisplayRuleTestData.class.getResourceAsStream(name);
            document = org.talend.mdm.webapp.base.server.util.XmlUtil.parse(is);

        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return document;
    }

    public static Map<String, TypeModel> get_Basic_DefaultValue_Rule() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subElementType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        subElementType.setTypePath("TestDefaultModel/subelement"); //$NON-NLS-1$
        metaDatas.put(subElementType.getTypePath(), subElementType);

        SimpleTypeModel nameType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        nameType.setTypePath("TestDefaultModel/name"); //$NON-NLS-1$
        nameType.setDefaultValueExpression("'zhang'"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel ct_titleType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        ct_titleType.setTypePath("TestDefaultModel/cp/title"); //$NON-NLS-1$
        ct_titleType.setDefaultValueExpression("'hello'"); //$NON-NLS-1$
        metaDatas.put(ct_titleType.getTypePath(), ct_titleType);

        SimpleTypeModel cp_contentType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        cp_contentType.setTypePath("TestDefaultModel/cp/content"); //$NON-NLS-1$
        cp_contentType.setDefaultValueExpression("1+2"); //$NON-NLS-1$
        metaDatas.put(cp_contentType.getTypePath(), cp_contentType);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_Basic_DefaultValue_Rule_WithFunction() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        subelementType.setTypePath("TestDefaultModel/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        nameType.setTypePath("TestDefaultModel/name"); //$NON-NLS-1$
        nameType.setDefaultValueExpression("'zhang'"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel cp_titleType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        cp_titleType.setTypePath("TestDefaultModel/cp/title"); //$NON-NLS-1$
        cp_titleType.setDefaultValueExpression("fn:concat('hello', 'world!!!')"); //$NON-NLS-1$
        metaDatas.put(cp_titleType.getTypePath(), cp_titleType);

        SimpleTypeModel cp_contentType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        cp_contentType.setTypePath("TestDefaultModel/cp/content"); //$NON-NLS-1$
        cp_contentType.setDefaultValueExpression("fn:abs(-5) + fn:abs(-5)"); //$NON-NLS-1$
        metaDatas.put(cp_contentType.getTypePath(), cp_contentType);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_DefaultValue_Rule_WithFunctionAndXPath() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        subelementType.setTypePath("DefaultRuleWithFunctionAndXPath/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel titleType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        titleType.setTypePath("DefaultRuleWithFunctionAndXPath/title"); //$NON-NLS-1$
        titleType.setDefaultValueExpression("fn:concat('detail name is [', ../detail/name,']')"); //$NON-NLS-1$
        metaDatas.put(titleType.getTypePath(), titleType);

        SimpleTypeModel detail_nameType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        detail_nameType.setTypePath("DefaultRuleWithFunctionAndXPath/detail/name"); //$NON-NLS-1$
        detail_nameType.setDefaultValueExpression("fn:concat('zhang ','yang')"); //$NON-NLS-1$
        metaDatas.put(detail_nameType.getTypePath(), detail_nameType);

        SimpleTypeModel detail_contentType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        detail_contentType.setTypePath("DefaultRuleWithFunctionAndXPath/detail/content"); //$NON-NLS-1$
        detail_contentType
                .setDefaultValueExpression("fn:concat('name is [',../name,string,'] title is [', /DefaultRuleWithFunctionAndXPath/title,']')"); //$NON-NLS-1$
        metaDatas.put(detail_contentType.getTypePath(), detail_contentType);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_DefaultValueForBoolean() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        subelementType.setTypePath("TestBoolean/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        nameType.setTypePath("TestBoolean/name"); //$NON-NLS-1$
        nameType.setDefaultValueExpression("'zhang'"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel finishedType = new SimpleTypeModel(null, DataTypeConstants.BOOLEAN);
        finishedType.setTypePath("TestBoolean/finished"); //$NON-NLS-1$
        finishedType.setDefaultValueExpression("fn:true()"); //$NON-NLS-1$
        metaDatas.put(finishedType.getTypePath(), finishedType);

        SimpleTypeModel finished1Type = new SimpleTypeModel(null, DataTypeConstants.BOOLEAN);
        finished1Type.setTypePath("TestBoolean/finished1"); //$NON-NLS-1$
        finished1Type.setDefaultValueExpression("fn:false()"); //$NON-NLS-1$
        metaDatas.put(finished1Type.getTypePath(), finished1Type);

        SimpleTypeModel finished2Type = new SimpleTypeModel(null, DataTypeConstants.BOOLEAN);
        finished2Type.setTypePath("TestBoolean/finished2"); //$NON-NLS-1$
        finished2Type.setDefaultValueExpression("1 != 2"); //$NON-NLS-1$
        metaDatas.put(finished2Type.getTypePath(), finished2Type);

        SimpleTypeModel finished3Type = new SimpleTypeModel(null, DataTypeConstants.BOOLEAN);
        finished3Type.setTypePath("TestBoolean/finished3"); //$NON-NLS-1$
        finished3Type.setDefaultValueExpression("1 = 2"); //$NON-NLS-1$
        metaDatas.put(finished3Type.getTypePath(), finished3Type);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_DefaultValueForEnumeration() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        subelementType.setTypePath("TestEnumeration/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        nameType.setTypePath("TestEnumeration/name"); //$NON-NLS-1$
        nameType.setDefaultValueExpression("'zhang'"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel ageType = new SimpleTypeModel(null, DataTypeConstants.INT);
        ageType.setTypePath("TestEnumeration/age"); //$NON-NLS-1$
        ageType.setDefaultValueExpression("'21-30'"); //$NON-NLS-1$
        metaDatas.put(ageType.getTypePath(), ageType);

        SimpleTypeModel favoriteType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        favoriteType.setTypePath("TestEnumeration/favorite"); //$NON-NLS-1$
        favoriteType.setDefaultValueExpression("'Orange'"); //$NON-NLS-1$
        metaDatas.put(favoriteType.getTypePath(), favoriteType);

        SimpleTypeModel numType = new SimpleTypeModel(null, DataTypeConstants.INT);
        numType.setTypePath("TestEnumeration/num"); //$NON-NLS-1$
        numType.setDefaultValueExpression("6"); //$NON-NLS-1$
        metaDatas.put(numType.getTypePath(), numType);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_Multiple_Occurence_DefaultValueRule() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        subelementType.setTypePath("MultipleOccurence/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        nameType.setTypePath("MultipleOccurence/name"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel cp_titleType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        cp_titleType.setTypePath("MultipleOccurence/cp/title"); //$NON-NLS-1$
        metaDatas.put(cp_titleType.getTypePath(), cp_titleType);

        SimpleTypeModel cp_addressType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        cp_addressType.setTypePath("MultipleOccurence/cp/address"); //$NON-NLS-1$
        cp_addressType.setDefaultValueExpression("fn:concat('hello ','this is address')"); //$NON-NLS-1$
        metaDatas.put(cp_addressType.getTypePath(), cp_addressType);

        SimpleTypeModel telType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        telType.setTypePath("MultipleOccurence/tel"); //$NON-NLS-1$
        telType.setDefaultValueExpression("fn:concat('phone',': 1323234323')"); //$NON-NLS-1$
        metaDatas.put(telType.getTypePath(), telType);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_DefaultRuleForInheritance() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        subelementType.setTypePath("DefaultRuleForInheritance/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        nameType.setTypePath("DefaultRuleForInheritance/name"); //$NON-NLS-1$
        nameType.setDefaultValueExpression("fn:concat('zhang ','yang')"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel person_nameType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        person_nameType.setTypePath("DefaultRuleForInheritance/person/name"); //$NON-NLS-1$
        metaDatas.put(person_nameType.getTypePath(), person_nameType);

        SimpleTypeModel personNameType = new SimpleTypeModel(null, DataTypeConstants.STRING);
        personNameType.setTypePath("DefaultRuleForInheritance/person/name"); //$NON-NLS-1$
        metaDatas.put(personNameType.getTypePath(), personNameType);

        ComplexTypeModel person_StudentType = new ComplexTypeModel(null, DataTypeConstants.INT);
        person_StudentType.setTypePath("DefaultRuleForInheritance/person/score"); //$NON-NLS-1$
        person_StudentType.setDefaultValueExpression("fn:concat(/DefaultRuleForInheritance/name,'''s score is 100')"); //$NON-NLS-1$
        metaDatas.put(person_StudentType.getTypePath(), person_StudentType);

        ComplexTypeModel person_TeacherType = new ComplexTypeModel(null, DataTypeConstants.INT);
        person_TeacherType.setTypePath("DefaultRuleForInheritance/person/salary"); //$NON-NLS-1$
        person_TeacherType.setDefaultValueExpression("fn:concat(/DefaultRuleForInheritance/name,'''s salary is 1000')"); //$NON-NLS-1$
        metaDatas.put(person_TeacherType.getTypePath(), person_TeacherType);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_Basic_VisibleRule() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel();
        subelementType.setTypePath("BasicVisibleRule/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel();
        nameType.setTypePath("BasicVisibleRule/name"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel testfieldType = new SimpleTypeModel();
        testfieldType.setTypePath("BasicVisibleRule/testfield"); //$NON-NLS-1$
        testfieldType.setVisibleExpression("1=1"); //$NON-NLS-1$
        metaDatas.put(testfieldType.getTypePath(), testfieldType);

        SimpleTypeModel cp_titleType = new SimpleTypeModel();
        cp_titleType.setTypePath("BasicVisibleRule/cp/title"); //$NON-NLS-1$
        metaDatas.put(cp_titleType.getTypePath(), cp_titleType);

        SimpleTypeModel cp_addressType = new SimpleTypeModel();
        cp_addressType.setTypePath("BasicVisibleRule/cp/address"); //$NON-NLS-1$
        cp_addressType.setVisibleExpression("1=2"); //$NON-NLS-1$
        metaDatas.put(cp_addressType.getTypePath(), cp_addressType);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_Basic_VisibleRule_Rule_WithFunction() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel();
        subelementType.setTypePath("BasicVisibleRuleFunction/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel();
        nameType.setTypePath("BasicVisibleRuleFunction/name"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel testfieldType = new SimpleTypeModel();
        testfieldType.setTypePath("BasicVisibleRuleFunction/testfield"); //$NON-NLS-1$
        testfieldType.setVisibleExpression("fn:string-length('hello') > 10"); //$NON-NLS-1$
        metaDatas.put(testfieldType.getTypePath(), testfieldType);

        SimpleTypeModel cp_titleType = new SimpleTypeModel();
        cp_titleType.setTypePath("BasicVisibleRuleFunction/cp/title"); //$NON-NLS-1$
        cp_titleType.setVisibleExpression("fn:ends-with('hello','h')"); //$NON-NLS-1$
        metaDatas.put(cp_titleType.getTypePath(), cp_titleType);

        SimpleTypeModel cp_addressType = new SimpleTypeModel();
        cp_addressType.setTypePath("BasicVisibleRuleFunction/cp/address"); //$NON-NLS-1$
        cp_addressType.setVisibleExpression("fn:ends-with('hello','o')"); //$NON-NLS-1$
        metaDatas.put(cp_addressType.getTypePath(), cp_addressType);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_VisibleRule_Rule_WithFunctionXPath() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel();
        subelementType.setTypePath("BasicVisibleRuleWithFunctionXPath/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel();
        nameType.setTypePath("BasicVisibleRuleWithFunctionXPath/name"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel testfieldType = new SimpleTypeModel();
        testfieldType.setTypePath("BasicVisibleRuleWithFunctionXPath/testfield"); //$NON-NLS-1$
        testfieldType.setVisibleExpression("fn:string-length(/BasicVisibleRuleWithFunctionXPath/name) > 3"); //$NON-NLS-1$
        metaDatas.put(testfieldType.getTypePath(), testfieldType);

        SimpleTypeModel cp_titleType = new SimpleTypeModel();
        cp_titleType.setTypePath("BasicVisibleRuleWithFunctionXPath/cp/title"); //$NON-NLS-1$
        cp_titleType.setVisibleExpression("fn:string-length(/BasicVisibleRuleWithFunctionXPath/name) > 4"); //$NON-NLS-1$
        metaDatas.put(cp_titleType.getTypePath(), cp_titleType);

        SimpleTypeModel cp_addressType = new SimpleTypeModel();
        cp_addressType.setTypePath("BasicVisibleRuleWithFunctionXPath/cp/address"); //$NON-NLS-1$
        cp_addressType.setVisibleExpression("fn:string-length(/BasicVisibleRuleWithFunctionXPath/name) > 5"); //$NON-NLS-1$
        metaDatas.put(cp_addressType.getTypePath(), cp_addressType);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_VisibleRule_UsingBoolean_Attribute() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel();
        subelementType.setTypePath("VisibleRuleBoolean/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel();
        nameType.setTypePath("VisibleRuleBoolean/name"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel finishedType = new SimpleTypeModel();
        finishedType.setTypePath("VisibleRuleBoolean/finished"); //$NON-NLS-1$
        finishedType.setVisibleExpression("fn:string-length('hello') > 10"); //$NON-NLS-1$
        metaDatas.put(finishedType.getTypePath(), finishedType);

        SimpleTypeModel cp_titleType = new SimpleTypeModel();
        cp_titleType.setTypePath("VisibleRuleBoolean/cp/title"); //$NON-NLS-1$
        metaDatas.put(cp_titleType.getTypePath(), cp_titleType);

        SimpleTypeModel cp_finished1Type = new SimpleTypeModel();
        cp_finished1Type.setTypePath("VisibleRuleBoolean/cp/finished1"); //$NON-NLS-1$
        cp_finished1Type.setVisibleExpression("fn:starts-with('hello','h')"); //$NON-NLS-1$
        metaDatas.put(cp_finished1Type.getTypePath(), cp_finished1Type);

        SimpleTypeModel cp_finished2Type = new SimpleTypeModel();
        cp_finished2Type.setTypePath("VisibleRuleBoolean/cp/finished2"); //$NON-NLS-1$
        cp_finished2Type.setVisibleExpression("fn:starts-with('hello','o')"); //$NON-NLS-1$
        metaDatas.put(cp_finished2Type.getTypePath(), cp_finished2Type);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_VisibleRuleForInheritance() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel();
        subelementType.setTypePath("VisibleRuleForInheritance/subelement"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel();
        nameType.setTypePath("VisibleRuleForInheritance/name"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel testfieldType = new SimpleTypeModel();
        testfieldType.setTypePath("VisibleRuleForInheritance/testfield"); //$NON-NLS-1$
        testfieldType.setVisibleExpression("fn:string-length('hello')>3"); //$NON-NLS-1$
        metaDatas.put(testfieldType.getTypePath(), testfieldType);

        SimpleTypeModel person_nameType = new SimpleTypeModel();
        person_nameType.setTypePath("VisibleRuleForInheritance/person/name"); //$NON-NLS-1$
        person_nameType.setVisibleExpression("fn:string-length('hello world')>8"); //$NON-NLS-1$
        metaDatas.put(person_nameType.getTypePath(), person_nameType);

        ComplexTypeModel person_StudentType = new ComplexTypeModel();
        person_StudentType.setTypePath("VisibleRuleForInheritance/person/score"); //$NON-NLS-1$
        person_StudentType.setVisibleExpression("fn:string-length('hello world')>15"); //$NON-NLS-1$
        metaDatas.put(person_StudentType.getTypePath(), person_StudentType);

        ComplexTypeModel person_TeacherType = new ComplexTypeModel();
        person_TeacherType.setTypePath("VisibleRuleForInheritance/person/salary"); //$NON-NLS-1$
        person_TeacherType.setVisibleExpression("fn:string-length('hello world')>10"); //$NON-NLS-1$
        metaDatas.put(person_TeacherType.getTypePath(), person_TeacherType);

        return metaDatas;
    }

    public static Map<String, TypeModel> getVisibleRuleForMultiOccurence() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel subelementType = new SimpleTypeModel();
        subelementType.setTypePath("Test/id"); //$NON-NLS-1$
        metaDatas.put(subelementType.getTypePath(), subelementType);

        SimpleTypeModel nameType = new SimpleTypeModel();
        nameType.setTypePath("Test/name"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        SimpleTypeModel documentsType = new SimpleTypeModel();
        documentsType.setTypePath("Test/documents"); //$NON-NLS-1$
        documentsType.setVisibleExpression("/Test/name/text()='1'"); //$NON-NLS-1$
        documentsType.setMinOccurs(0);
        documentsType.setMaxOccurs(1);
        metaDatas.put(documentsType.getTypePath(), documentsType);

        SimpleTypeModel documentType = new SimpleTypeModel();
        documentType.setTypePath("Test/documents/document"); //$NON-NLS-1$
        documentType.setVisibleExpression("position()>1"); //$NON-NLS-1$
        documentType.setMinOccurs(0);
        documentType.setMaxOccurs(-1);
        metaDatas.put(documentType.getTypePath(), documentType);

        return metaDatas;
    }

    public static Map<String, TypeModel> get_VisibleRuleForComplexTypeNode() {
        Map<String, TypeModel> metaDatas = new LinkedHashMap<String, TypeModel>();

        SimpleTypeModel idType = new SimpleTypeModel();
        idType.setTypePath("Test/id"); //$NON-NLS-1$
        metaDatas.put(idType.getTypePath(), idType);

        SimpleTypeModel nameType = new SimpleTypeModel();
        nameType.setTypePath("Test/name"); //$NON-NLS-1$
        metaDatas.put(nameType.getTypePath(), nameType);

        ComplexTypeModel oemModel = new ComplexTypeModel();
        oemModel.setTypePath("Test/oem"); //$NON-NLS-1$
        oemModel.setVisibleExpression("fn:matches(../name ,\"test\")"); //$NON-NLS-1$
        metaDatas.put(oemModel.getTypePath(), oemModel);

        SimpleTypeModel oem_type = new SimpleTypeModel();
        oem_type.setTypePath("Test/oem/oem_type"); //$NON-NLS-1$
        metaDatas.put(oem_type.getTypePath(), oem_type);

        SimpleTypeModel oem_a = new SimpleTypeModel();
        oem_a.setTypePath("Test/oem/a"); //$NON-NLS-1$
        oem_a.setVisibleExpression("fn:starts-with(../oem_type,\"a\")"); //$NON-NLS-1$
        metaDatas.put(oem_a.getTypePath(), oem_a);

        SimpleTypeModel oem_b = new SimpleTypeModel();
        oem_b.setTypePath("Test/oem/b"); //$NON-NLS-1$
        oem_b.setVisibleExpression("fn:starts-with(../oem_type,\"b\")"); //$NON-NLS-1$
        metaDatas.put(oem_b.getTypePath(), oem_b);

        SimpleTypeModel oem_c = new SimpleTypeModel();
        oem_c.setTypePath("Test/oem/c"); //$NON-NLS-1$
        oem_c.setVisibleExpression("fn:starts-with(../oem_type,\"c\")"); //$NON-NLS-1$
        metaDatas.put(oem_c.getTypePath(), oem_c);

        return metaDatas;
    }
}
