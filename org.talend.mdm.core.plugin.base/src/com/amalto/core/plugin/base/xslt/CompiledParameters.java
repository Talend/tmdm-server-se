/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.xslt;

import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.amalto.core.util.Util;

public class CompiledParameters implements Serializable {

    private static final long serialVersionUID = 2908739898798787L;

    private String outputMethod = null;

    private String xslt;

    public static CompiledParameters deserialize(String xml) throws IOException, ClassNotFoundException, TransformerException,
            ParserConfigurationException, SAXException {
        CompiledParameters parameters = new CompiledParameters();
        Element params = Util.parse(xml).getDocumentElement();
        parameters.setXslt(Util.getFirstTextNode(params, "xslt"));
        parameters.setOutputMethod(Util.getFirstTextNode(params, "method"));
        return parameters;
    }

    public String getOutputMethod() {
        return outputMethod;
    }

    public void setOutputMethod(String outputMethod) {
        this.outputMethod = outputMethod;
    }

    public String getXslt() {
        return xslt;
    }

    public void setXslt(String xslt) {
        this.xslt = xslt;
    }

    public String serialize() throws IOException {
        String xml = "<parameters>";
        xml += "<method>" + getOutputMethod() + "</method>";
        xml += "<xslt>" + StringEscapeUtils.escapeXml(xslt) + "</xslt>";
        xml += "</parameters>";
        return xml;
    }

}
