/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DateTools;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

public class ToStringBridge implements TwoWayFieldBridge {

    public Object get(String name, Document document) {
        return document.get(name);
    }

    public String objectToString(Object object) {
        if (object == null) {
            return StringUtils.EMPTY;
        }
        return String.valueOf(object);
    }

    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        if (value instanceof Date) {
            Date date = (Date) value;
            String stringDate = DateTools.dateToString(date, DateTools.Resolution.SECOND);
            luceneOptions.addFieldToDocument(name, stringDate, document);
        } else {
            luceneOptions.addFieldToDocument(name, String.valueOf(value), document);
        }
    }
}
