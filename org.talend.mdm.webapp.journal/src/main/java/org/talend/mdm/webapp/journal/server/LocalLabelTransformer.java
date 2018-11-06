/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server;

import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.dom4j.DocumentHelper;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.xml.sax.InputSource;

import com.amalto.core.history.Document;
import com.amalto.core.history.DocumentTransformer;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.save.DOMDocument;

public class LocalLabelTransformer implements DocumentTransformer {

    public static final String LABEL = "label";

    private Locale locale;

    public LocalLabelTransformer(String language) {
        locale = new Locale(language);
    }

    @Override
    public Document transform(MutableDocument document) {

        try {
            ComplexTypeMetadata typeMetadata = document.getType();
            org.dom4j.Document newDcoument = DocumentHelper.parseText(document.exportToString());

            org.dom4j.Element rootElement = newDcoument.getRootElement();
            String localLabel = typeMetadata.getName(locale);
            rootElement.addAttribute(LABEL, localLabel);

            Collection<FieldMetadata> fieldMetadataCollection = typeMetadata.getFields();
            for (FieldMetadata fieldMetadata : fieldMetadataCollection) {
                Iterator<org.dom4j.Element> it = rootElement.elementIterator(fieldMetadata.getName());
                while (it.hasNext()) {
                    org.dom4j.Element element = (org.dom4j.Element) it.next();
                    if (element != null) {
                        localLabel = fieldMetadata.getContainingType().getField(element.getName()).getName(locale);
                        element.addAttribute(LABEL, localLabel);
                    }
                    if (fieldMetadata instanceof ContainedTypeFieldMetadata) {
                        setContainedTypeFieldMetadata(
                                ((ContainedComplexTypeMetadata) fieldMetadata.getType()).getContainedType(), element);
                    }
                }
            }
            org.w3c.dom.Document newW3cDocument = MDMXMLUtils.getDocumentBuilderWithNamespace().get().parse(
                    new InputSource(new StringReader(newDcoument.asXML())));
            MutableDocument newDocument = new DOMDocument(newW3cDocument, typeMetadata, document.getDataCluster(),
                    document.getDataModel());
            return newDocument;
        } catch (Exception e) {
            return document;
        }
    }

    private void setContainedTypeFieldMetadata(ComplexTypeMetadata containedTypeFieldMetadata, org.dom4j.Element rootElement) {
        Collection<FieldMetadata> list = containedTypeFieldMetadata.getFields();
        for (FieldMetadata fieldMetadata : list) {
            Iterator<org.dom4j.Element> it = rootElement.elementIterator(fieldMetadata.getName());
            while (it.hasNext()) {
                org.dom4j.Element element = (org.dom4j.Element) it.next();
                if (element != null) {
                    String localLabel = fieldMetadata.getContainingType().getField(element.getName()).getName(locale);
                    element.addAttribute(LABEL, localLabel);
                }
                if (fieldMetadata instanceof ContainedTypeFieldMetadata) {
                    setContainedTypeFieldMetadata(((ContainedComplexTypeMetadata) fieldMetadata.getType()).getContainedType(),
                            element);
                }
            }
        }
        Collection<ComplexTypeMetadata> subTypeList = containedTypeFieldMetadata.getSubTypes();
        for (ComplexTypeMetadata fieldMetadata : subTypeList) {
            setContainedTypeFieldMetadata(fieldMetadata,rootElement);
        }
    }
}
