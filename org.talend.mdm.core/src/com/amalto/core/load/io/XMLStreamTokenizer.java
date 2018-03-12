/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * <p>
 * "Cut" a stream that contains several XML documents into <code>String</code> instances, each of them containing a
 * valid XML fragment.
 * </p>
 * <p>
 * The stream : <br/>
 * <code>
 * &lt;root&gt;&lt;/root&gt;&lt;root&gt;&lt;/root&gt;
 * </code>
 * </p>
 * <p>
 * Will give the following 2 String instances : <br/>
 * <code>
 * &lt;root&gt;&lt;/root&gt;
 * </code> <br/>
 * <code>
 * &lt;root&gt;&lt;/root&gt;
 * </code>
 * </p>
 * <p/>
 * <p>
 * The contract of {@link Enumeration} is fully implemented so <code>hasMoreElements</code> must be called before
 * <code>nextElement()</code>
 * </p>
 */
public class XMLStreamTokenizer implements Enumeration<String> {

    private static final Logger LOG = Logger.getLogger(XMLStreamTokenizer.class);

    private final ResettableStringWriter stringWriter = new ResettableStringWriter();

    private final Reader inputStream;

    private String currentNextElement;

    private String rootElementName = null;

    private int previousCharacter;

    public XMLStreamTokenizer(InputStream inputStream) {
        this.inputStream = new InputStreamReader(inputStream);
    }

    public XMLStreamTokenizer(InputStream inputStream, String encoding) throws UnsupportedEncodingException {
        this.inputStream = new InputStreamReader(inputStream, encoding);
    }

    @Override
    public boolean hasMoreElements() {
        return moveNext();
    }

    @Override
    public String nextElement() {
        if (currentNextElement == null) {
            throw new IllegalStateException("Should call hasMoreElements() before nextElement()"); //$NON-NLS-1$
        }

        String retValue = currentNextElement;
        currentNextElement = null;
        return retValue;
    }

    private boolean moveNext() {
        try {
            boolean isFragmentComplete = false;
            int read;
            boolean inElement = false;
            boolean isEndElement = false;
            boolean isProcessingInstruction = false;
            String currentElementName = ""; //$NON-NLS-1$
            boolean lockElementName = false;
            int currentLevel = 0;

            while (!isFragmentComplete && (read = inputStream.read()) > 0) {
                switch (read) {
                case ' ':
                    if (inElement) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Lock element name due to attributes detected in element '" + currentElementName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        lockElementName = true;
                    }
                    break;
                case '?':
                    if (previousCharacter == '<') {
                        stringWriter.reset();
                        isProcessingInstruction = true;
                    }
                    break;
                case '<':
                    inElement = true;
                    break;
                case '/':
                    if (inElement && !lockElementName) {
                        isEndElement = true;
                    }
                    break;
                case '>':
                    if (previousCharacter != '/' && !isProcessingInstruction) {
                        if (isEndElement) {
                            logEndElement(currentElementName, currentLevel);
                            currentLevel--;
                        } else {
                            logStartElement(currentElementName, currentLevel);
                            currentLevel++;
                        }
                    }

                    if (inElement) {
                        if (isEndElement && rootElementName != null && rootElementName.equals(currentElementName)
                                && currentLevel == 0) {
                            isFragmentComplete = true;
                        } else if (rootElementName == null && !isProcessingInstruction) {
                            rootElementName = currentElementName;
                        }
                        currentElementName = ""; //$NON-NLS-1$

                        isEndElement = false;
                        inElement = false;
                        lockElementName = false;
                    }

                    if (isProcessingInstruction && previousCharacter == '?') {
                        isProcessingInstruction = false;
                        continue;
                    }

                    break;
                }
                if (inElement && read != '<' && read != '/' && !lockElementName) {
                    currentElementName += (char) read;
                }

                if (!isProcessingInstruction) {
                    stringWriter.append((char) read);
                }
                previousCharacter = read;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        currentNextElement = stringWriter.toString();
        stringWriter.reset();
        // Allow mixed content (see com.amalto.core.load.XMLStreamTokenizerTest.testMixedRootElements())
        rootElementName = null;
        return !currentNextElement.isEmpty();
    }

    private static void logStartElement(String currentElementName, int currentLevel) {
        if (LOG.isDebugEnabled()) {
            String indent = ""; //$NON-NLS-1$
            for (int i = 0; i < currentLevel; i++) {
                indent += '\t';
            }
            LOG.debug(indent + "->" + currentElementName); //$NON-NLS-1$
        }
    }

    private static void logEndElement(String currentElementName, int currentLevel) {
        if (LOG.isDebugEnabled()) {
            String indent = ""; //$NON-NLS-1$
            for (int i = 0; i < currentLevel - 1; i++) {
                indent += '\t';
            }
            LOG.debug(indent + "<-" + currentElementName); //$NON-NLS-1$
        }
    }

}
