/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load.context;

import com.amalto.core.load.LoadParserCallback;
import com.amalto.core.load.Metadata;
import com.amalto.core.load.State;
import com.amalto.core.load.exception.ParserCallbackException;
import com.amalto.core.load.path.PathMatch;
import com.amalto.core.load.path.PathMatcher;
import com.amalto.core.load.payload.EndPayload;
import com.amalto.core.load.payload.StartPayload;
import com.amalto.core.server.api.XmlServer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 *
 */
public class DefaultStateContext implements StateContext {
    private final LoadParserCallback callback;

    private final String payLoadElementName;

    private final BufferStateContextWriter bufferStateContextWriter = new BufferStateContextWriter();

    private final Stack<String> currentLocation = new Stack<String>();

    private final Metadata metadata;

    private final Set<PathMatcher> paths;

    private State currentState = StartPayload.INSTANCE;

    private StateContextWriter contextWriter;

    private int payloadLimit = -1;

    private int payloadCount = 0;

    private boolean isFlushDone;

    private boolean isMetadataReady;

    private PathMatcher lastPartialMatchPath;

    private int idToMatchCount = 0;

    private boolean isIdElement;

    private String currentIdElementName;

    public DefaultStateContext(String payLoadElementName, String[] idPaths, String dataClusterName, String dataModelName, int payloadLimit, LoadParserCallback callback) {
        this(payLoadElementName, idPaths, dataClusterName, dataModelName, callback);
        this.payloadLimit = payloadLimit;
    }

    private DefaultStateContext(String payLoadElementName, String[] idPaths, String dataClusterName, String dataModelName, LoadParserCallback callback) {
        if (payLoadElementName == null) {
            throw new IllegalArgumentException("Payload element name cannot be null.");
        }

        paths = new HashSet<PathMatcher>(idPaths.length + 1);
        for (String idPath : idPaths) {
            paths.add(new PathMatcher(idPath));
        }
        idToMatchCount = idPaths.length;
        contextWriter = bufferStateContextWriter;
        this.callback = callback;
        this.payLoadElementName = payLoadElementName;

        metadata = new DefaultMetadata();
        metadata.setName(payLoadElementName);
        metadata.setDmn(payLoadElementName);
        metadata.setContainer(dataClusterName);
        metadata.setDataClusterName(dataClusterName);
        metadata.setDmn(dataModelName);
    }

    public String getPayLoadElementName() {
        return payLoadElementName;
    }

    public void setWriter(StateContextWriter contextWriter) {
        this.contextWriter = contextWriter;
    }

    public boolean isFlushDone() {
        return isFlushDone;
    }

    public boolean isMetadataReady() {
        return isMetadataReady;
    }

    public void setFlushDone() {
        this.isFlushDone = true;
    }

    public void reset() {
        isFlushDone = false;
        isMetadataReady = false;
        contextWriter = bufferStateContextWriter.reset();
        currentLocation.empty();
        idToMatchCount = paths.size();
        metadata.reset();
    }

    public StateContextWriter getWriter() {
        return contextWriter;
    }

    public void setCurrent(State state) {
        currentState = state;
    }

    public void parse(XMLStreamReader reader) {
        try {
            currentState.parse(this, reader);
        } catch (ParserCallbackException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            // Parsing exceptions should not happen, interrupt parsing
            throw new RuntimeException(e);
        }
    }

    public LoadParserCallback getCallback() {
        return callback;
    }

    public boolean hasFinished() {
        return payloadCount == payloadLimit || currentState.isFinal();
    }

    public boolean hasFinishedPayload() {
        boolean hasFinishedPayload;
        if (currentState == EndPayload.INSTANCE) {
            hasFinishedPayload = true;
            payloadCount++;
        } else {
            hasFinishedPayload = false;
        }
        return hasFinishedPayload;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void leaveElement() {
        if (!currentLocation.isEmpty()) {
            currentLocation.pop();
        }
        isIdElement = false;
        currentIdElementName = null;
    }

    public void enterElement(String elementLocalName) {
        currentLocation.push(elementLocalName);

        // Check path
        if (!isMetadataReady) {
            PathMatcher match = match(elementLocalName);
            if (match != null) {
                isIdElement = true;
                currentIdElementName = elementLocalName;
                idToMatchCount--;
            }

            if (idToMatchCount == 0) {
                isMetadataReady = true;
            }
        }
    }

    /**
     * Check if the <code>elementName</code> match:
     * <ul>
     * <li>any of the XPaths, in case none has matched yet (in case of first element, for instance).</li>
     * <li>The last XPath that partially matched.</li>
     * </ul>
     *
     * @param elementName A local element name
     * @return The {@link PathMatcher} if <code>elementName</code> completed the matched, null otherwise.
     */
    private PathMatcher match(String elementName) {
        if (lastPartialMatchPath == null) {
            for (PathMatcher currentPath : paths) {
                PathMatch match = currentPath.match(elementName);
                switch (match) {
                    case PARTIAL:
                        lastPartialMatchPath = currentPath;
                    case NONE:
                        break;
                    case FULL:
                        return currentPath;
                    default:
                        throw new IllegalArgumentException("Unsupported match type: " + match);
                }

                if (lastPartialMatchPath != null) {
                    break;
                }
            }
            return null;
        } else {
            PathMatch match = lastPartialMatchPath.match(elementName);
            switch (match) {
                case NONE:
                    lastPartialMatchPath = null;
                case PARTIAL:
                    return null;
                case FULL:
                    return lastPartialMatchPath;
                default:
                    throw new IllegalArgumentException("Unsupported match type: " + match);
            }
        }
    }

    public int getDepth() {
        return currentLocation.size();
    }

    public boolean isIdElement() {
        return isIdElement;
    }

    public String getCurrentIdElement() {
        return currentIdElementName;
    }

    public boolean skipElement() {
        return false;
    }

    public void close(XmlServer server) {
        // Nothing to do
    }

}