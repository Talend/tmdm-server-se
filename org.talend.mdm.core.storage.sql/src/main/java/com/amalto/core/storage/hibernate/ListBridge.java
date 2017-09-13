/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

import java.util.Iterator;
import java.util.List;

public class ListBridge implements TwoWayFieldBridge {

    public Object get(String name, Document document) {
        return document.get(name);
    }

    public String objectToString(Object object) {
        return getValueFromObject((List) object);
    }

    private static String getValueFromObject(List object) {
        if (object == null) {
            return StringUtils.EMPTY;
        }
        Iterator valuesIterator = object.iterator();
        StringBuilder builder = new StringBuilder();
        while (valuesIterator.hasNext()) {
            builder.append(String.valueOf(valuesIterator.next()));
            if (valuesIterator.hasNext()) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        luceneOptions.addFieldToDocument(name, getValueFromObject((List) value), document);
    }
}
