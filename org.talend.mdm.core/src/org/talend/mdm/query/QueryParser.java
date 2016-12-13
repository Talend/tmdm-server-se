/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.query;

import com.amalto.core.query.user.Expression;
import com.google.gson.GsonBuilder;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import java.io.*;

/**
 *
 */
public class QueryParser {

    private final GsonBuilder builder;

    private QueryParser(MetadataRepository repository) {
        builder = new GsonBuilder();
        builder.registerTypeAdapter(Expression.class, new Deserializer(repository));
    }

    public static QueryParser newParser(MetadataRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Metadata repository cannot be null.");
        }
        return new QueryParser(repository);
    }

    public Expression parse(String query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null.");
        }
        try {
            return parse(new ByteArrayInputStream(query.getBytes("UTF-8"))); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding when parsing query from string.", e);
        }
    }

    public Expression parse(InputStream query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null.");
        }
        return parse(new InputStreamReader(query)).normalize();
    }

    public Expression parse(Reader query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null.");
        }
        return builder.create().fromJson(query, Expression.class).normalize();
    }

}
