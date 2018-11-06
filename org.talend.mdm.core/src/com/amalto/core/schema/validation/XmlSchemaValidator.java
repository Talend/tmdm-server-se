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

package com.amalto.core.schema.validation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.collections.map.MultiKeyMap;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.amalto.core.util.SAXErrorHandler;
import com.amalto.core.util.ValidateException;

public class XmlSchemaValidator implements Validator {

    private static final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private static final Map<String, Schema> schemaCache = new HashMap<String, Schema>();

    private static final MultiKeyMap validatorCache = new MultiKeyMap();;

    private final InputStream schemaAsStream;

    private final String dataModelName;

    private final Validator next;

    public XmlSchemaValidator(String dataModelName, InputStream schemaAsStream, Validator next) {
        this.schemaAsStream = schemaAsStream;
        this.dataModelName = dataModelName;
        this.next = next;
    }

    public void validate(Element element) throws ValidateException {
        javax.xml.validation.Validator validator = getValidator();

        SAXErrorHandler errorHandler = new SAXErrorHandler();
        try {
            validator.setErrorHandler(errorHandler);
            validator.validate(new DOMSource(element));
        } catch (Exception e) {
            // All errors are expected to be handled by the error handler.
            throw new RuntimeException("Unexpected validation issue.", e);
        }
        String errors = errorHandler.getErrors();
        if (!errors.isEmpty()) {
            throw new ValidateException(errors);
        }
        next.validate(element);
    }

    private javax.xml.validation.Validator getValidator() {
        javax.xml.validation.Validator validator;
        synchronized (schemaCache) {
            try {
                validator = (javax.xml.validation.Validator) validatorCache.get(dataModelName, Thread.currentThread());
                if (validator != null) {
                    return validator;
                }
                Schema parsedSchema = schemaCache.get(dataModelName);
                if (parsedSchema == null) {
                    parsedSchema = schemaFactory.newSchema(new StreamSource(schemaAsStream));
                    schemaCache.put(dataModelName, parsedSchema);
                }
                validator = parsedSchema.newValidator();
                validatorCache.put(dataModelName, Thread.currentThread(), validator);
            } catch (SAXException e) {
                throw new RuntimeException("Exception occurred during XML schema parsing.", e);
            }
        }
        return validator;
    }

    public static void invalidateCache(String dataModelName) {
        synchronized (schemaCache) {
            schemaCache.remove(dataModelName);
            validatorCache.clear();
        }
    }

    public static void invalidateCache(){
        synchronized (schemaCache) {
            schemaCache.clear();
            validatorCache.clear();
        }
    }
}
