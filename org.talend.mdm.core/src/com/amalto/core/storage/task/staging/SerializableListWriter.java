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

package com.amalto.core.storage.task.staging;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

@javax.ws.rs.ext.Provider
public class SerializableListWriter implements MessageBodyWriter<SerializableList> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return SerializableList.class.isAssignableFrom(aClass)
                && (MediaType.WILDCARD_TYPE.equals(mediaType) ||
                MediaType.TEXT_PLAIN_TYPE.equals(mediaType) ||
                MediaType.TEXT_XML_TYPE.equals(mediaType) ||
                MediaType.APPLICATION_JSON_TYPE.equals(mediaType) ||
                MediaType.TEXT_HTML_TYPE.equals(mediaType));
    }

    @Override
    public long getSize(SerializableList strings, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return strings.size();
    }

    @Override
    public void writeTo(SerializableList strings, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> stringObjectMultivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE) || mediaType.equals(MediaType.WILDCARD_TYPE)) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            for (Object string : strings) {
                bw.write(String.valueOf(string));
                bw.write(' ');
            }
            bw.flush();
        } else if (mediaType.equals(MediaType.TEXT_XML_TYPE) | mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            bw.write("<" + strings.getRootElement() + ">");
            for (Object string : strings) {
                bw.write("<" + strings.getItemElement() + ">");
                bw.write(String.valueOf(string));
                bw.write("</" + strings.getItemElement() + ">");
            }
            bw.write("</" + strings.getRootElement() + ">");
            bw.flush();
        } else if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            bw.write("{" + strings.getRootElement() + ":[");
            Iterator iterator = strings.iterator();
            while (iterator.hasNext()) {
                bw.write('\"' + String.valueOf(iterator.next()) + '\"');
                if (iterator.hasNext()) {
                    bw.write(",");
                }
            }
            bw.write("]}");
            bw.flush();
        } else {
            throw new IllegalArgumentException("Media type: '" + mediaType.getType() + "' is not supported.");
        }
    }
}
