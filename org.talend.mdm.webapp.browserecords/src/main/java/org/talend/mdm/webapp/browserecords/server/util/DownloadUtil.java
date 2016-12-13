/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.talend.mdm.webapp.base.server.util.XmlUtil;

public class DownloadUtil {

    public static void assembleFkMap(Map<String, String> colFkMap, Map<String, List<String>> fkMap, String fkColXPath,
            String fkInfo) throws Exception {
        if (!fkColXPath.equalsIgnoreCase("")) { //$NON-NLS-1$
            String[] fkColXPathArr = convertXml2Array(fkColXPath, "item"); //$NON-NLS-1$
            String[] fkInfoArr = convertXml2Array(fkInfo, "item"); //$NON-NLS-1$
            for (int i = 0; i < fkColXPathArr.length; i++) {
                String[] fkStr = fkInfoArr[i].split(","); //$NON-NLS-1$
                List<String> fkList = new ArrayList<String>();
                for (String str : fkStr) {
                    fkList.add(str);
                }
                String[] arr = fkColXPathArr[i].split(","); //$NON-NLS-1$
                fkMap.put(arr[0], fkList);
                colFkMap.put(arr[0], arr[1]);
            }
        }
    }

    public static String[] convertXml2Array(String xml, String nodeName) throws Exception {
        List<String> resultList = new ArrayList<String>();
        Document document = XmlUtil.parseText(xml);
        List<Element> elements = document.getRootElement().elements(nodeName);
        for (Element element : elements) {
            resultList.add(element.getText());
        }
        return resultList.toArray(new String[resultList.size()]);
    }

    public static boolean isJoinField(String xPath, String concept) {
        String str = xPath.substring(0, xPath.indexOf("/")); //$NON-NLS-1$
        return str.equalsIgnoreCase(concept);
    }

    public static String getJoinFieldValue(Document doc, String xPath, int count) {
        if (doc.getRootElement().elements().size() > count) {
            Node node = (Node) doc.getRootElement().elements().get(count);
            String joinConcept = xPath.substring(0, xPath.indexOf("/")); //$NON-NLS-1$
            String joinFieldXpath = xPath.replaceFirst(joinConcept + "/", "/result/"); //$NON-NLS-1$//$NON-NLS-2$
            if (node.getPath().equalsIgnoreCase(joinFieldXpath)) {
                return node.getText();
            }
        }
        return ""; //$NON-NLS-1$
    }
}
