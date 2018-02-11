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

package com.amalto.core.history;

import com.amalto.core.history.action.ActionFactory;

import java.lang.reflect.Constructor;

/**
 *
 */
public class DocumentHistoryFactory {

    private static final String IMPLEMENTATION_CLASS_NAME = "com.amalto.core.history.DocumentHistoryImpl";

    private static final DocumentHistoryFactory INSTANCE = new DocumentHistoryFactory();

    private DocumentHistoryFactory() {
    }

    public static DocumentHistoryFactory getInstance() {
        return INSTANCE;
    }

    public DocumentHistory create() {
        try {
            Object documentHistoryObject = Class.forName(IMPLEMENTATION_CLASS_NAME).newInstance();
            return (DocumentHistory) documentHistoryObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DocumentHistory create(ActionFactory actionFactory, DocumentFactory documentFactory) {
        try {
            Class<?> clazz = Class.forName(IMPLEMENTATION_CLASS_NAME);
            Constructor<?> constructor = clazz.getConstructor(ActionFactory.class, DocumentFactory.class);
            Object documentHistoryObject = constructor.newInstance(actionFactory, documentFactory);
            return (DocumentHistory) documentHistoryObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
