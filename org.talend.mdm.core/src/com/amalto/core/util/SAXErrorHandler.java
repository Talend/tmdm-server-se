/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
/*
 */
package com.amalto.core.util;

import java.util.regex.Pattern;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXErrorHandler extends DefaultHandler {

    private static final Pattern namespacesP = Pattern.compile("(\"publicid:org.*?.xsd\":)|(\"rrn:org.*?.xsd\":)");

    private static final Pattern bracketsP = Pattern.compile("(\\()|(\\))");

    private static final Pattern commaP = Pattern.compile(",");

    private String errors;

    public SAXErrorHandler() {
    }

    public void warning(SAXParseException ex) throws SAXException {
        setErrors(formatError("Warning", ex));
        setErrors("\n");
    }

    public void error(SAXParseException ex) throws SAXException {
        setErrors(formatError("Error", ex));
        setErrors("\n");
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        setErrors(formatError("Fatal Error", ex));
        setErrors("\n");
        throw ex;
    }

    private String formatError(String type, SAXParseException ex) {
        String error = "[" + type + "] ";
        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(index + 1);
            }
            error = error + systemId;
        }
        String msg = ex.getMessage();
        msg = namespacesP.matcher(msg).replaceAll("");
        msg = bracketsP.matcher(msg).replaceAll("");
        msg = commaP.matcher(msg).replaceAll("");
        error = error + ":" + ex.getLineNumber() + ":" + ex.getColumnNumber() + ": " + msg;
        return error;
    }

    public String getErrors() {
        return (errors == null || errors.trim().length() == 0) ? "" : errors;
    }

    public void setErrors(String error) {
        if (errors == null)
            errors = "";
        errors += error;
    }

}  