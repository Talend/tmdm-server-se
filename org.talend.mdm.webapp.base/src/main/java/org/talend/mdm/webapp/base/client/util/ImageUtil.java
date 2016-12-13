/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.util;

import java.util.LinkedList;
import java.util.List;

import org.talend.mdm.webapp.base.client.model.Image;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class ImageUtil {

    final public static String IMAGE_SERVER_PATH = "services/pubcomponent/pictures"; //$NON-NLS-1$

    final public static String IMAGE_PATH = "imageserver/"; //$NON-NLS-1$

    public static List<Image> getImages(String xml) throws Exception {

        List<Image> images = new LinkedList<Image>();

        if (xml == null || xml.trim().length() == 0)
            return images;

        Document docment = XMLParser.parse(xml);
        Element element = docment.getDocumentElement();
        XMLParser.removeWhitespace(element);

        if (!element.hasChildNodes())
            return images;

        NodeList result = element.getElementsByTagName("entry"); //$NON-NLS-1$
        for (int i = 0; i < result.getLength(); i++) {
            Image image = new Image();
            Element node = (Element) result.item(i);
            image.setName(getElementTextValue(node, "name")); //$NON-NLS-1$
            image.setFileName(getElementTextValue(node, "imageName")); //$NON-NLS-1$
            image.setCatalog(getElementTextValue(node, "catalog")); //$NON-NLS-1$
            image.setUri(getElementTextValue(node, "uri")); //$NON-NLS-1$
            images.add(image);
        }
        return images;
    }

    private static String getElementTextValue(Element parent, String elementTag) {

        if (parent == null || elementTag == null)
            throw new IllegalArgumentException();

        NodeList foundNodes = parent.getElementsByTagName(elementTag);

        if (foundNodes.getLength() == 0)
            return null;

        Node textNode = foundNodes.item(0).getFirstChild();

        if (textNode == null)
            return null;

        return textNode.getNodeValue();
    }
}
