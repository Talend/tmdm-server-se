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

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;
import org.hibernate.search.engine.impl.LuceneOptionsImpl;
import org.hibernate.search.engine.metadata.impl.DocumentFieldMetadata;

public class ToLowerCaseFieldBridge implements TwoWayFieldBridge {

    public static final String ID_POSTFIX = "_lowercase_copy"; //$NON-NLS-1$

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
        @SuppressWarnings("deprecation")
        DocumentFieldMetadata fieldMetadata = new DocumentFieldMetadata.Builder(null, Store.YES, Field.Index.ANALYZED,
                Field.TermVector.WITH_POSITIONS_OFFSETS).boost(1F).build();
        LuceneOptions luceneIndexOptions = new LuceneOptionsImpl(fieldMetadata, 1f, 1f);
        luceneIndexOptions.addFieldToDocument(name + ID_POSTFIX, String.valueOf(value).toLowerCase(), document);
        luceneOptions.addFieldToDocument(name, String.valueOf(value), document);
    }

}
