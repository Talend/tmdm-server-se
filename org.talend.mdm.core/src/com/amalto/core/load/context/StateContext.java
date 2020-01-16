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

    /**
     * @return all AUTO_INCREMENT or UUID fields except the Primary key
     */
    List<PathMatcher> getNormalFieldPaths();

    /**
     * @return all field path which supplied in the xml content.
     */
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

    AutoIdGenerator[] getNormalFieldGenerators();

    Stack<String> getReadElementPath();

    /**
     * judge the AUTO_INCREMENT/UUID field is existed in the supplied xml content.
     * 1. if the field is in the entity,
     *   1) supplied the value in the xml content: don't add
     *   2) not supplied in the xml content: added this field.
     * 2. the field is existed in one complex type such as Course/Like,
     *   1). supplied in the xml content, don't add.
     *   2). not supplied in the xml content:
     *     I. contains other field's value of this complex type, added this field.
     *     II. have no any fields'value of this complex type,  don't add.
     * @return all fields of type are AUTO_INCREMENT or UUID field except PK, which need to generate value.
     */
    default String[] getAutoIncrementNormalFields() {
        List<String> normalFieldPathList = new ArrayList<>(getNormalFieldPaths().size());
        for (PathMatcher pathMatcher : getNormalFieldPaths()) {
            boolean isMatched = false; // mark it as  full matched
            boolean isParentMatched = false; // mark it as partial matched
            String pathMatchedStr = pathMatcher.toString();
            for (String path : getNormalFieldInXML()) {
                if (pathMatchedStr.contains("/") && path.contains("/")) {
                    int i = 0;
                    String[] pathArray = path.split("/");
                    if (isMatched) {
                        break;
                    }
                    for (String s : pathArray) {
                        PathMatch match = pathMatcher.match(s);
                        if (match == PathMatch.FULL) {
                            isMatched = true;
                            isParentMatched = false;
                            break;
                        } else if (match == PathMatch.PARTIAL && i++ == pathArray.length - 2) {
                            isParentMatched = true;
                        }
                    }
                } else if (!pathMatchedStr.contains("/") && !path.contains("/")) {
                    if (pathMatcher.match(path) == PathMatch.FULL) {
                        isMatched = true;
                        break;
                    }
                }
            }
            if ((!isMatched && !pathMatchedStr.contains("/")) || isParentMatched) {
                normalFieldPathList.add(pathMatchedStr);
            } else {
                normalFieldPathList.add(null);
            }
        }
        return normalFieldPathList.toArray(new String[] {});
    }
}
