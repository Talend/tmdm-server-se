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

package com.amalto.core.load.context;

import com.amalto.core.load.LoadParserCallback;
import com.amalto.core.load.Metadata;
import com.amalto.core.load.State;
import com.amalto.core.load.exception.ParserCallbackException;
import com.amalto.core.load.path.PathMatcher;
import com.amalto.core.load.payload.EndPayload;
import com.amalto.core.load.payload.StartPayload;
import com.amalto.core.load.xml.Selector;
import com.amalto.core.save.generator.AutoIdGenerator;
import com.amalto.core.server.api.XmlServer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Load parser context implementation that has 2 main features:
 * <ul>
 * <li>Metadata is always ready (no need to parse the document to get the ID)</li>
 * <li>Metadata stored in this context is ID read-only: any attempt to modify it throw exception</li>
 * </ul>
 */
public class AutoGenStateContext implements StateContext {

    private final String[] idPaths;

    private final StateContext delegate;

    private final AutoGenMetadata metadata;

    private State currentState = StartPayload.INSTANCE;

    private boolean hasGeneratedAutomaticId;

    private final AutoIdGenerator generator;

    private List<PathMatcher> normalFieldPaths;

    private List<String> normalFieldInXML;

    private LinkedList<String> readElementPath;

    private final AutoIdGenerator[] normalFieldGenerator;

    private AutoGenStateContext(StateContext delegate, String[] idPaths, AutoIdGenerator generator, String[] normalFieldPaths,
            AutoIdGenerator[] normalFieldGenerator) {
        this.delegate = delegate;
        this.idPaths = idPaths;
        this.generator = generator;
        this.readElementPath = new LinkedList<>();
        this.normalFieldInXML = new ArrayList<>();
        this.normalFieldPaths = new ArrayList<>(normalFieldPaths.length);
        this.normalFieldGenerator = normalFieldGenerator;
        for (String idPath : normalFieldPaths) {
            getNormalFieldPaths().add(new PathMatcher(idPath));
        }
        metadata = new AutoGenMetadata(this.delegate.getMetadata(), idPaths, this.generator);
    }

    /**
     * @param context         A context implementation. The <code>context</code> may not support auto generated PK.
     *                        But if it already does, this does not throw any exception.
     * @param idPaths         The paths to the elements that contains the generated ids.
     * @param autoIdGenerator Implementation of {@link com.amalto.core.save.generator.AutoIdGenerator} able to generate id in this context.  @return A context that generate metadata automatically.
     * @return A {@link StateContext} implementation able to generate automatic ids.
     */
    public static StateContext decorate(StateContext context, String[] idPaths, AutoIdGenerator autoIdGenerator,
            String[] normalFieldPaths, AutoIdGenerator[] autoNormalFieldGenerator) {
        return new AutoGenStateContext(context, idPaths, autoIdGenerator, normalFieldPaths, autoNormalFieldGenerator);
    }

    public Metadata getMetadata() {
        // Return *this* metadata as this is what matters here.
        return this.metadata;
    }

    public boolean isMetadataReady() {
        // Always return true since ID is auto-generated.
        return hasGeneratedAutomaticId;
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

    public String getPayLoadElementName() {
        return delegate.getPayLoadElementName();
    }

    public StateContextWriter getWriter() {
        return delegate.getWriter();
    }

    public void setCurrent(State state) {
        currentState = state;
        if (state == Selector.INSTANCE && !hasGeneratedAutomaticId) {
            currentState = new AutoIdGeneration(state, idPaths);
            hasGeneratedAutomaticId = true;
        }
    }

    @Override
    public State getCurrent() {
        return currentState;
    }

    public LoadParserCallback getCallback() {
        return delegate.getCallback();
    }

    public boolean hasFinished() {
        return currentState.isFinal();
    }

    public boolean hasFinishedPayload() {
        return currentState == EndPayload.INSTANCE;
    }

    public void setWriter(StateContextWriter contextWriter) {
        delegate.setWriter(contextWriter);
    }

    public boolean isFlushDone() {
        return delegate.isFlushDone();
    }

    public void setFlushDone() {
        delegate.setFlushDone();
    }

    public void reset() {
        hasGeneratedAutomaticId = false;
        delegate.reset();
        metadata.reset();
    }

    public void leaveElement() {
        delegate.leaveElement();
    }

    public void enterElement(String elementLocalName) {
        delegate.enterElement(elementLocalName);
    }

    public int getDepth() {
        return delegate.getDepth();
    }

    public boolean isIdElement() {
        return delegate.isIdElement();
    }

    public String getCurrentIdElement() {
        return delegate.getCurrentIdElement();
    }

    public boolean skipElement() {
        return delegate.isIdElement();
    }

    public void close(XmlServer server) {
        // This line is rather important since the generator might need to persist its state
        // see DefaultAutoIdGenerator for instance.
        generator.saveState(server);
    }

    @Override
    public List<PathMatcher> getNormalFieldPaths(){
        return this.normalFieldPaths;
    }

    @Override
    public List<String> getNormalFieldInXML() {
        return this.normalFieldInXML;
    }

    @Override
    public LinkedList<String> getReadElementPath() {
        return this.readElementPath;
    }

    @Override
    public AutoIdGenerator[] getNormalFieldGenerators() {
        return normalFieldGenerator;
    }

}
