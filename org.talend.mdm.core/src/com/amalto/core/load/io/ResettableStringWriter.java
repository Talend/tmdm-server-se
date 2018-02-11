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

package com.amalto.core.load.io;

import java.io.IOException;
import java.io.StringWriter;

/**
 * <p>
 * A string writer sub class that reuses the same {@link StringWriter}. This is useful to store strings that usually
 * have the same size.
 * </p>
 * <p>
 * Reusing the same {@link StringWriter} limit the cost(s) of resizing the buffer to the same size.
 * </p>
 */
public class ResettableStringWriter extends StringWriter {

    private StringWriter delegate = new StringWriter();

    private int currentLength = 0;

    private int maxLength = -1;

    public ResettableStringWriter() {
    }

    public String reset() {
        if (currentLength > maxLength) {
            maxLength = currentLength;
        }
        currentLength = 0;
        String result = delegate.toString();
        delegate = new StringWriter(maxLength);
        return result;
    }

    @Override
    public void write(int c) {
        currentLength++;
        delegate.write(c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        currentLength += len;
        delegate.write(cbuf, off, len);
    }

    @Override
    public void write(String str) {
        currentLength += str.length();
        delegate.write(str);
    }

    @Override
    public void write(String str, int off, int len) {
        currentLength += len;
        delegate.write(str, off, len);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        currentLength += cbuf.length;
        delegate.write(cbuf);
    }

    @Override
    public StringWriter append(CharSequence csq) {
        currentLength += csq.length();
        return delegate.append(csq);
    }

    @Override
    public StringWriter append(CharSequence csq, int start, int end) {
        currentLength += csq.length();
        return delegate.append(csq, start, end);
    }

    @Override
    public StringWriter append(char c) {
        currentLength++;
        return delegate.append(c);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public StringBuffer getBuffer() {
        return delegate.getBuffer();
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
