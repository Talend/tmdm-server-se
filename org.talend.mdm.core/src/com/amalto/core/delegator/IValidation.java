/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.delegator;

import java.io.ByteArrayInputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.amalto.core.util.CVCException;
import com.amalto.core.util.SAXErrorHandler;
import com.amalto.core.util.Util;

public class IValidation {

    private static final Logger LOGGER        = Logger.getLogger(IValidation.class);

    private final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    public Document validation(Element element, String schema) throws Exception {
        if (schema == null) {
            throw new IllegalArgumentException("Schema cannot be null.");
        }
        if (MDMXMLUtils.isExistExtEntity(schema)) {
            throw new IllegalArgumentException("External entities are not allowed in Schema.");
        }
		// Validate DOM element using javax.xml.validation.Schema API (avoid re-parse).
        SAXErrorHandler seh = new SAXErrorHandler();
        StreamSource source = new StreamSource(new ByteArrayInputStream(schema.getBytes("UTF-8"))); //$NON-NLS-1
        Schema parsedSchema = schemaFactory.newSchema(source);
        Validator validator = parsedSchema.newValidator();
        validator.setErrorHandler(seh);
        validator.validate(new DOMSource(element));
        // check if document parsed correctly against the schema
        // ignore cvc-complex-type.2.3 error
        String errors = seh.getErrors();
        boolean isComplex23 = errors.contains("cvc-complex-type.2.3") && errors.endsWith("is element-only."); //$NON-NLS-1$ //$NON-NLS-2$
        if (!errors.equals("") && !isComplex23) { //$NON-NLS-1$
            String xmlString = Util.nodeToString(element);
            String err = "The item " + element.getLocalName() + " did not validate against the model: \n" + errors + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + xmlString;
            LOGGER.debug(err);
            throw new CVCException(err);
        }
        return element.getOwnerDocument();
    }
}
