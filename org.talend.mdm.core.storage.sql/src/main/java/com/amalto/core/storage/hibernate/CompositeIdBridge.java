/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

public class CompositeIdBridge implements TwoWayFieldBridge {

    public Object get(String name, Document document) {
        return document.get(name);
    }

    public String objectToString(Object object) {
        if (object == null) {
            return StringUtils.EMPTY;
        }
        return object.toString();
    }

    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        luceneOptions.addFieldToDocument(name, String.valueOf(value), document);
    }
}
