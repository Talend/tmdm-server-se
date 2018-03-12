/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.util;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class SchemaProcessor {

    private static final Logger LOGGER = Logger.getLogger(SchemaProcessor.class);

    private static final String elementID = "xsd:element"; //$NON-NLS-1$

    public static String transform2types(String infoXML) {
        String transformedXml = null;
        try {
            Document doc = DocumentHelper.parseText(infoXML);

            // first level only
            @SuppressWarnings("unchecked")
            List<Element> elements = doc.getRootElement().elements();
            for (Iterator<Element> iterator = elements.iterator(); iterator.hasNext();) {
                Element element = (Element) iterator.next();

                // remove concept element
                if (element.getQualifiedName().equals(elementID)) {
                    Element parentElement = element.getParent();
                    if (parentElement != null)
                        parentElement.remove(element);
                }
            }

            transformedXml = doc.asXML();
        } catch (DocumentException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        }

        return transformedXml;
    }

}
