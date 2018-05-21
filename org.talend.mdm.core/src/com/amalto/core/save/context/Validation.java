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

package com.amalto.core.save.context;

import java.lang.reflect.Constructor;

import org.w3c.dom.Element;

import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.schema.validation.Validator;
import com.amalto.core.schema.validation.XmlSchemaValidator;
import com.amalto.core.util.ValidateException;

class Validation implements DocumentSaver {

    private final DocumentSaver next;

    private static boolean useSchematron = false;

    private static Class<?> schematronValidator;

    static {
        try {
            schematronValidator = Class.forName("com.amalto.core.validation.schematron.SchematronRecordValidator"); //$NON-NLS-1$
            useSchematron = true;
        } catch (ClassNotFoundException e) {
            useSchematron = false;
        }
    }

    Validation(DocumentSaver next) {
        this.next = next;
    }

    public void save(SaverSession session, DocumentSaverContext context) {
        try {
            Validator validator = new XmlSchemaValidator(context.getDataModelName(),
                    session.getSaverSource().getSchema(context.getDataModelName()),
                    Validator.NO_OP_VALIDATOR);
            if (useSchematron) {
                String schematron = context.getUserDocument().getType().getSchematron();
                if (!schematron.isEmpty()) {
                    Constructor<?> constructor = schematronValidator.getConstructor(com.amalto.core.schema.validation.Validator.class, String.class);
                    validator = (Validator) constructor.newInstance(validator, schematron);
                }
            }
            Element element = context.getDatabaseDocument().asValidationDOM().getDocumentElement();
            validator.validate(element);
            next.save(session, context);
        } catch (ValidateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getSavedId() {
        return next.getSavedId();
    }

    public String getSavedConceptName() {
        return next.getSavedConceptName();
    }

    public String getBeforeSavingMessage() {
        return next.getBeforeSavingMessage();
    }

    public String getBeforeSavingMessageType() {
        return next.getBeforeSavingMessageType();
    }

}
