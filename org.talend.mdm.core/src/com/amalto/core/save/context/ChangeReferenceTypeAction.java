/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.w3c.dom.Document;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;

class ChangeReferenceTypeAction extends AbstractChangeTypeAction {

    private ChangeReferenceTypeAction(MutableDocument document, Date date, String source, String userName, String path, ComplexTypeMetadata previousType,
            ComplexTypeMetadata newType, FieldMetadata field) {
        super(document, date, source, userName, path, previousType, newType, field);
    }

    public static List<Action> create(MutableDocument document, Date date, String source, String userName, String path,
            ComplexTypeMetadata previousType, ComplexTypeMetadata newType, FieldMetadata field) {
        // Create action
        List<Action> actions = new LinkedList<Action>();
        ChangeReferenceTypeAction typeAction = new ChangeReferenceTypeAction(document, date, source, userName, path, previousType,
                newType, field);
        actions.addAll(typeAction.getImpliedActions());
        actions.add(typeAction);
        return actions;
    }

    public MutableDocument perform(MutableDocument document) {
        if (!hasChangedType) {
            document.createAccessor(path).touch();
            return document;
        }
        // Ensure tmdm prefix is declared
        Document domDocument = document.asDOM();
        String xsi = domDocument.lookupNamespaceURI(SkipAttributeDocumentBuilder.TALEND_NAMESPACE); //$NON-NLS-1$
        if (xsi == null) {
            domDocument.getDocumentElement().setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:tmdm", SkipAttributeDocumentBuilder.TALEND_NAMESPACE); //$NON-NLS-1$
        }
        Accessor typeAccessor = document.createAccessor(path + "/@tmdm:type"); //$NON-NLS-1$
        String typeName = newType.getName();
        typeAccessor.createAndSet(typeName);
        return document;
    }

    public MutableDocument undo(MutableDocument document) {
        if (!hasChangedType) {
            document.createAccessor(path).touch();
            return document;
        }
        Accessor accessor = document.createAccessor(path + "/@tmdm:type"); //$NON-NLS-1$
        if (previousType != null) {
            accessor.set(previousType.getName());
        }
        return document;
    }

    public String getDetails() {
        if (hasChangedType) {
            return "Change FK type to " + newType.getName(); //$NON-NLS-1$
        } else {
            return "Change FK type to " + newType.getName() + " (NO OP)"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public String toString() {
        return "ChangeReferenceTypeAction{" + //$NON-NLS-1$
                "path='" + path + '\'' + //$NON-NLS-1$
                ", newType=" + newType + //$NON-NLS-1$
                ", changedType= " + hasChangedType + //$NON-NLS-1$
                '}';
    }

}
