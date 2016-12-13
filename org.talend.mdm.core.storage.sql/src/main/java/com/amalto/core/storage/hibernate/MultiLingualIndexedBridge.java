/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

public class MultiLingualIndexedBridge implements TwoWayFieldBridge {

    @Override
    public Object get(String name, Document document) {
        return document.get(name);
    }

    @Override
    public String objectToString(Object object) {
        if (object == null) {
            return StringUtils.EMPTY;
        }
        return String.valueOf(object);
    }

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        luceneOptions.addFieldToDocument(name, getMultiLingualIndexedContent(String.valueOf(value)), document);
    }


    @SuppressWarnings("unused")
    private String getMultiLingualIndexedContent(String value) {
        if (value.startsWith("[") && value.endsWith("]")) { //$NON-NLS-1$ //$NON-NLS-2$
            List<String> blocks = new ArrayList<String>(Arrays.asList(value.split("]"))); //$NON-NLS-1$
            List<String> newBlocks = new ArrayList<String>();
            StringBuffer sb = new StringBuffer();

            for (String block : blocks) {
                if (block.startsWith("[") && block.contains(":")) { //$NON-NLS-1$ //$NON-NLS-2$
                    newBlocks.add(block.substring(block.indexOf(":") + 1)); //$NON-NLS-1$
                }
            }

            if (newBlocks != null && newBlocks.size() > 0) {
                for (String block : newBlocks) {
                    sb.append(block + " "); //$NON-NLS-1$
                }
                if (sb != null && sb.length() > 0) {
                    return sb.toString();
                }
            }
        }
        return value;
    }
}
