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

package com.amalto.core.history.accessor;

import com.amalto.core.history.DOMMutableDocument;
import com.amalto.core.history.MutableDocument;

import java.util.StringTokenizer;

/**
 *
 */
public class DOMAccessorFactory {

    private DOMAccessorFactory() {
    }

    public static Accessor createAccessor(String xpath, MutableDocument document) {
        DOMAccessor current = new RootAccessor(document);
        if (xpath == null || xpath.isEmpty()) {
            return current;
        }

        StringTokenizer tokenizer = new StringTokenizer(xpath, "/"); //$NON-NLS-1$

        while (tokenizer.hasMoreElements()) {
            String element = (String) tokenizer.nextElement();

            if (element.startsWith("@")) { //$NON-NLS-1$
                current = new AttributeAccessor(current, xpath, document);
            } else if (element.contains("[")) { //$NON-NLS-1$
                int indexStart = element.indexOf('[');
                int indexEnd = element.indexOf(']');
                if (indexStart < 0  || indexEnd < 0) {
                    throw new RuntimeException("Field name '" + element + "' did not match many field pattern in path '" + xpath + "'.");
                }
                String fieldName = element.substring(0, indexStart);
                int index = Integer.parseInt(element.substring(indexStart + 1, indexEnd)) - 1;
                current = new ManyFieldAccessor(current, fieldName, index, (DOMMutableDocument) document);
            } else {
                current = new UnaryFieldAccessor(current, element, (DOMMutableDocument) document);
            }
        }

        return current;
    }
}
