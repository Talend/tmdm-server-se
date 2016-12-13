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
package com.amalto.commons.core.utils;

import java.util.regex.Pattern;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author bgrieder
 *
 */
public class SAXErrorHandler extends DefaultHandler {
    
    private Pattern namespacesP;
    private Pattern bracketsP;
    private Pattern commaP;

    private String errors;
    
    /**
     * ParserBean.java
     * Constructor
     * 
     */
    public SAXErrorHandler() {
        super();
        namespacesP = Pattern.compile("(\"publicid:org.*?.xsd\":)|(\"rrn:org.*?.xsd\":)");
        bracketsP = Pattern.compile("(\\()|(\\))");
        commaP = Pattern.compile(",");
    }

    
    public void warning(SAXParseException ex)
    throws SAXException
	{
	    setErrors(formatError("Warning", ex));
	    setErrors("\n");
	}
	
	public void error(SAXParseException ex)
	    throws SAXException
	{
	    setErrors(formatError("Error", ex));
	    setErrors("\n");
	}
	
	public void fatalError(SAXParseException ex)
	    throws SAXException
	{
	    setErrors(formatError("Fatal Error", ex));
	    setErrors("\n");
	    throw ex;
	}
	
	private String formatError(String type, SAXParseException ex)
	{
	    String error = "";
	    error = "[" + type + "] ";
	    String systemId = ex.getSystemId();
	    if(systemId != null)
	    {
	        int index = systemId.lastIndexOf('/');
	        if(index != -1)
	            systemId = systemId.substring(index + 1);
	        error = error + systemId;
	    }
	    String msg = ex.getMessage();
	    msg = namespacesP.matcher(msg).replaceAll("");
	    msg = bracketsP.matcher(msg).replaceAll("");
	    msg = commaP.matcher(msg).replaceAll("");
	    error = error + ":" + ex.getLineNumber() + ":" + ex.getColumnNumber() + ": " + msg;
	    return error;
	}
	
	public String getErrors()
	{
	    return (errors==null)?"":errors;
	}
	
	public void setErrors(String error)
	{
	    if(errors == null)
	        errors = "";
	    errors += error;
	}
    
}  