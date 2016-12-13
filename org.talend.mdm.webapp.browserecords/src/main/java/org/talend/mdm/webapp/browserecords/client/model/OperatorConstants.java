/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class OperatorConstants implements Serializable, IsSerializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static String EQUALS_OPERATOR = "EQUALS"; //$NON-NLS-1$

    public static String AND_OPERATOR = "AND"; //$NON-NLS-1$

    public static String OR_OPERATOR = "OR"; //$NON-NLS-1$

    public static Map<String, String> groupOperators = new LinkedHashMap<String, String>();

    public static Map<String, String> fullOperators = new LinkedHashMap<String, String>();

    public static Map<String, String> foreignKeyOperators = new LinkedHashMap<String, String>();

    public static Map<String, String> fulltextOperators = new LinkedHashMap<String, String>();

    public static Map<String, String> dateOperators = new LinkedHashMap<String, String>();

    public static Map<String, String> numOperators = new LinkedHashMap<String, String>();

    public static Map<String, String> booleanOperators = new LinkedHashMap<String, String>();

    public static Map<String, String> enumOperators = new LinkedHashMap<String, String>();

    public static Map<String, String> stringOperators = new LinkedHashMap<String, String>();

    public static Map<String, String> multiLanguageOperators = new LinkedHashMap<String, String>();

    static {
        groupOperators.put(AND_OPERATOR, MessagesFactory.getMessages().criteria_AND()); //$NON-NLS-1$
        groupOperators.put(OR_OPERATOR, MessagesFactory.getMessages().criteria_OR()); //$NON-NLS-1$

        fullOperators.put("CONTAINS", MessagesFactory.getMessages().criteria_CONTAINS()); //$NON-NLS-1$
        fullOperators.put("CONTAINS_SENTENCE", MessagesFactory.getMessages().criteria_CONTAINS_SENTENCE()); //$NON-NLS-1$
        fullOperators.put(EQUALS_OPERATOR, MessagesFactory.getMessages().criteria_EQUALS());
        fullOperators.put("NOT_EQUALS", MessagesFactory.getMessages().criteria_NOT_EQUALS()); //$NON-NLS-1$
        fullOperators.put("GREATER_THAN", MessagesFactory.getMessages().criteria_GREATER_THAN()); //$NON-NLS-1$
        fullOperators.put("GREATER_THAN_OR_EQUAL", MessagesFactory.getMessages().criteria_GREATER_THAN_OR_EQUAL()); //$NON-NLS-1$
        fullOperators.put("LOWER_THAN", MessagesFactory.getMessages().criteria_LOWER_THAN()); //$NON-NLS-1$
        fullOperators.put("LOWER_THAN_OR_EQUAL", MessagesFactory.getMessages().criteria_LOWER_THAN_OR_EQUAL()); //$NON-NLS-1$
        fullOperators.put("STARTSWITH", MessagesFactory.getMessages().criteria_STARTSWITH()); //$NON-NLS-1$
        fullOperators.put("EMPTY_NULL", MessagesFactory.getMessages().criteria_EMPTY_NULL()); //$NON-NLS-1$

        foreignKeyOperators.put(EQUALS_OPERATOR, MessagesFactory.getMessages().criteria_EQUALS());
        foreignKeyOperators.put("EMPTY_NULL", MessagesFactory.getMessages().criteria_EMPTY_NULL()); //$NON-NLS-1$

        fulltextOperators.put("FULLTEXTSEARCH", MessagesFactory.getMessages().criteria_FULLTEXTSEARCH()); //$NON-NLS-1$

        dateOperators.put(EQUALS_OPERATOR, MessagesFactory.getMessages().criteria_DATEEQUALS());
        dateOperators.put("LOWER_THAN", MessagesFactory.getMessages().criteria_DATELOWER_THAN()); //$NON-NLS-1$
        dateOperators.put("GREATER_THAN", MessagesFactory.getMessages().criteria_DATEGREATER_THAN()); //$NON-NLS-1$
        dateOperators.put("EMPTY_NULL", MessagesFactory.getMessages().criteria_EMPTY_NULL()); //$NON-NLS-1$

        numOperators.put(EQUALS_OPERATOR, MessagesFactory.getMessages().criteria_EQUALS());
        numOperators.put("NOT_EQUALS", MessagesFactory.getMessages().criteria_NOT_EQUALS()); //$NON-NLS-1$
        numOperators.put("GREATER_THAN", MessagesFactory.getMessages().criteria_GREATER_THAN()); //$NON-NLS-1$
        numOperators.put("GREATER_THAN_OR_EQUAL", MessagesFactory.getMessages().criteria_GREATER_THAN_OR_EQUAL()); //$NON-NLS-1$
        numOperators.put("LOWER_THAN", MessagesFactory.getMessages().criteria_LOWER_THAN()); //$NON-NLS-1$
        numOperators.put("LOWER_THAN_OR_EQUAL", MessagesFactory.getMessages().criteria_LOWER_THAN_OR_EQUAL()); //$NON-NLS-1$
        numOperators.put("EMPTY_NULL", MessagesFactory.getMessages().criteria_EMPTY_NULL()); //$NON-NLS-1$

        booleanOperators.put(EQUALS_OPERATOR, MessagesFactory.getMessages().criteria_EQUALS());

        enumOperators.put(EQUALS_OPERATOR, MessagesFactory.getMessages().criteria_EQUALS());
        enumOperators.put("LOWER_THAN", MessagesFactory.getMessages().criteria_LOWER_THAN()); //$NON-NLS-1$
        enumOperators.put("GREATER_THAN", MessagesFactory.getMessages().criteria_GREATER_THAN()); //$NON-NLS-1$
        enumOperators.put("LOWER_THAN_OR_EQUAL", MessagesFactory.getMessages().criteria_LOWER_THAN_OR_EQUAL()); //$NON-NLS-1$
        enumOperators.put("GREATER_THAN_OR_EQUAL", MessagesFactory.getMessages().criteria_GREATER_THAN_OR_EQUAL()); //$NON-NLS-1$
        enumOperators.put("EMPTY_NULL", MessagesFactory.getMessages().criteria_EMPTY_NULL()); //$NON-NLS-1$

        stringOperators.put("CONTAINS", MessagesFactory.getMessages().criteria_CONTAINS()); //$NON-NLS-1$
        stringOperators.put("CONTAINS_SENTENCE", MessagesFactory.getMessages().criteria_CONTAINS_SENTENCE()); //$NON-NLS-1$
        stringOperators.put(EQUALS_OPERATOR, MessagesFactory.getMessages().criteria_EQUALS());
        stringOperators.put("NOT_EQUALS", MessagesFactory.getMessages().criteria_NOT_EQUALS()); //$NON-NLS-1$
        stringOperators.put("STARTSWITH", MessagesFactory.getMessages().criteria_STARTSWITH()); //$NON-NLS-1$
        stringOperators.put("EMPTY_NULL", MessagesFactory.getMessages().criteria_EMPTY_NULL()); //$NON-NLS-1$

        multiLanguageOperators.put("CONTAINS", MessagesFactory.getMessages().criteria_CONTAINS()); //$NON-NLS-1$
        multiLanguageOperators.put("CONTAINS_SENTENCE", MessagesFactory.getMessages().criteria_CONTAINS_SENTENCE()); //$NON-NLS-1$
        multiLanguageOperators.put("STARTSWITH", MessagesFactory.getMessages().criteria_STARTSWITH()); //$NON-NLS-1$
        multiLanguageOperators.put("EMPTY_NULL", MessagesFactory.getMessages().criteria_EMPTY_NULL()); //$NON-NLS-1$
    }

}
