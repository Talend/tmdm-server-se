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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.save.DOMDocument;

public class PartialDeleteSimulator {
    
    private final MutableDocument originalDocument;
    
    private final MutableDocument toDeleteDocument;
    
    private final String toDeletePivot;

    private final String toDeleteKey;

    public PartialDeleteSimulator(MutableDocument originalDocument, MutableDocument toDeleteDocument, String pivot, String key) {
        this.originalDocument = originalDocument;
        this.toDeleteDocument = toDeleteDocument;
        if (pivot.endsWith("/")) { //$NON-NLS-1$
            this.toDeletePivot = pivot.substring(0, pivot.length() - 1);
        } else {
            this.toDeletePivot = pivot;
        }
        if (!".".equals(key) && !StringUtils.isEmpty(key)) { //$NON-NLS-1$
            this.toDeleteKey = key.startsWith("/") ? key : "/" + key; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            this.toDeleteKey = StringUtils.EMPTY;
        }
    }

    /**
     * Simulate partial delete and return final document
     * @return
     */
    public DOMDocument simulateDelete() {
        List<String> toDeleteKeyValues = getToDeleteKeyValues();
        DOMDocument copyDocument = new DOMDocument(originalDocument.asDOM(), originalDocument.getType(),
                originalDocument.getDataCluster(), originalDocument.getDataModel());
        Accessor pivotAccessor = copyDocument.createAccessor(toDeletePivot);
        for (int i = pivotAccessor.size(); i >= 1; i--) {
            String path = toDeletePivot + '[' + i + ']';
            Accessor keyAccessor = copyDocument.createAccessor(path + toDeleteKey);
            String keyValue = keyAccessor.get();
            if (toDeleteKeyValues.contains(keyValue)) {
                if (StringUtils.isEmpty(toDeleteKey)) {
                    keyAccessor.delete();
                } else {
                    copyDocument.createAccessor(path).delete();
                }
            }
        }
        copyDocument.clean();
        return copyDocument;
    }
    
    private List<String> getToDeleteKeyValues() {
        List<String> keyValues = new ArrayList<String>();
        String pivotForToDeleteDocument = getPivotForToDeleteDocument();
        Accessor pivotAccessor = toDeleteDocument.createAccessor(pivotForToDeleteDocument);
        for (int i = 1; i <= pivotAccessor.size(); i++) {
            String path = pivotForToDeleteDocument + '[' + i + ']';
            Accessor keyAccessor = toDeleteDocument.createAccessor(path + toDeleteKey);
            String keyValue = keyAccessor.get();
            if (keyValue != null) {
                keyValues.add(keyValue);
            }
        }
        return keyValues;
    }

    /**
     * "Source document" and "toDeleteDocument" may not be be able to share "toDeletePiovt". <br>
     * Like <b>"Kids/Kid[2]/Habits/Habit"</b> means to delete source document's <b>SECOND</b> kid's habits,<br> 
     * but "toDeleteDocument" should use <b>"Kids/Kid[1]/Habits/Habit"</b> to get the data to delete.
     * @return
     */
    private String getPivotForToDeleteDocument(){
        if(toDeletePivot.contains("[") && toDeletePivot.contains("]")) { //$NON-NLS-1$ //$NON-NLS-2$
            StringBuilder pivot = new StringBuilder();
            StringTokenizer tokenizer = new StringTokenizer(toDeletePivot, "/"); //$NON-NLS-1$
            while (tokenizer.hasMoreElements()) {
                String element = (String) tokenizer.nextElement();
                pivot.append("/").append(StringUtils.substringBefore(element, "[")); //$NON-NLS-1$ //$NON-NLS-2$
                if (element.contains("[") && element.contains("]")) { //$NON-NLS-1$ //$NON-NLS-2$
                    pivot.append("[1]"); //$NON-NLS-1$
                }
            }
            return pivot.toString().substring(1);
        } else {
            return toDeletePivot;
        }
    }
}
