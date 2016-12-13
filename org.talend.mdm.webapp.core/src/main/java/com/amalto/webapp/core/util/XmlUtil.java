/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;


public class XmlUtil {
    
    public static Document parseDocument(org.w3c.dom.Document doc) {
        if (doc == null) {
            return (null);
        }
        org.dom4j.io.DOMReader xmlReader = new org.dom4j.io.DOMReader();
        return (xmlReader.read(doc));
    }
    
    public static String escapeXml(String value) {
        if (value == null)
            return null;
        boolean isEscaped=false;
        if (value.contains("&quot;") || //$NON-NLS-1$
                value.contains("&amp;") || //$NON-NLS-1$
                value.contains("&lt;") || //$NON-NLS-1$
                value.contains("&gt;")) { //$NON-NLS-1$
            isEscaped = true;
        }
        if(!isEscaped) {
            value=StringEscapeUtils.escapeXml(value);
        }
        return value;
    }

}
