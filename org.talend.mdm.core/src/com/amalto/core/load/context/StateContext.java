/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load.context;

import com.amalto.core.load.LoadParserCallback;
import com.amalto.core.load.path.PathMatch;
import com.amalto.core.load.path.PathMatcher;
import com.amalto.core.save.generator.AutoIdGenerator;
import com.amalto.core.server.api.XmlServer;

import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@SuppressWarnings("nls")
public interface StateContext {

    List<PathMatcher> getNormalFieldPaths();

    List<String> getNormalFieldInXML();

    void parse(XMLStreamReader reader);

    String getPayLoadElementName();

    StateContextWriter getWriter();

    void setCurrent(com.amalto.core.load.State state);

    com.amalto.core.load.State getCurrent();

    LoadParserCallback getCallback();

    boolean hasFinished();

    boolean hasFinishedPayload();

    com.amalto.core.load.Metadata getMetadata();

    void setWriter(StateContextWriter contextWriter);

    boolean isFlushDone();

    boolean isMetadataReady();

    void setFlushDone();

    void reset();

    void leaveElement();

    void enterElement(String elementLocalName);

    int getDepth();

    boolean isIdElement();

    String getCurrentIdElement();

    boolean skipElement();

    void close(XmlServer server);

    AutoIdGenerator[] getAutoFieldGenerator();

    Stack<String> getReadElementPath();

    default String[] needAutoIncGeneratorField() {
        List<String> normalFieldPathList = new ArrayList<>(getNormalFieldPaths().size());
        for (PathMatcher pathMatcher : getNormalFieldPaths()) {
            boolean isMatched = false;
            for (String path : getNormalFieldInXML()) {
                if (path.contains("/")) {
                    for (String s : path.split("/")) {
                        if (pathMatcher.match(s) == PathMatch.FULL) {
                            isMatched = true;
                        }
                    }
                } else {
                    if (pathMatcher.match(path) == PathMatch.FULL) {
                        isMatched = true;
                    }
                }
            }
            if (!isMatched) {
                normalFieldPathList.add(pathMatcher.toString());
            } else {
                normalFieldPathList.add(null);
            }
        }
        return normalFieldPathList.toArray(new String[] {});
    }
}
