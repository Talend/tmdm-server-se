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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import com.amalto.core.history.Action;
import com.amalto.core.history.FieldAction;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.action.FieldUpdateAction;
import com.amalto.core.history.action.NoOpAction;

abstract class AbstractChangeTypeAction implements FieldAction {

    protected final String path;

    protected final ComplexTypeMetadata newType;

    protected final ComplexTypeMetadata previousType;

    private final Date date;

    private final String source;

    private final String userName;

    private final FieldMetadata field;

    protected final boolean hasChangedType;

    private final List<Action> impliedActions;

    protected AbstractChangeTypeAction(MutableDocument document, Date date, String source, String userName, String path,
            ComplexTypeMetadata previousType, ComplexTypeMetadata newType, FieldMetadata field) {
        this.source = source;
        this.date = date;
        this.userName = userName;
        this.path = path;
        this.newType = newType;
        this.previousType = previousType;
        this.field = field;
        // Compute paths to fields that changed from previous type (only if type changed).
        Set<String> pathToClean = new TreeSet<String>();
        hasChangedType = previousType != newType || !previousType.getName().equals(newType.getName());
        if (!hasChangedType) {
            impliedActions = Collections.emptyList();
        } else if (previousType != null) {
            boolean hasChangedType = previousType != newType || !previousType.getName().equals(newType.getName());
            if (!hasChangedType) {
                impliedActions = Collections.singletonList(NoOpAction.instance());
            } else {
                // Create field update actions
                previousType.accept(new TypeComparison(newType, pathToClean));
                impliedActions = new ArrayList<Action>(1 + pathToClean.size());
                if (!pathToClean.isEmpty()) {
                    List<String> indexedPathToClean = getIndexedPathToClean(document, previousType, path, pathToClean);
                    for (String currentPathToDelete : indexedPathToClean) {
                        String deletedPath = path + '/' + currentPathToDelete;
                        String oldValue = document.createAccessor(deletedPath).get();
                        impliedActions.add(new FieldUpdateAction(date, source, userName, deletedPath, oldValue, null, field));
                    }
                }
            }
        } else {
            impliedActions = Collections.emptyList();
        }
    }
    
    private List<String> getIndexedPathToClean(MutableDocument document, ComplexTypeMetadata previousType, String parentPath,
            Set<String> pathToClean) {
        Set<String> indexedPathToClean = new TreeSet<String>();
        Map<String, Set<String>> partPathsMap = new HashMap<String, Set<String>>();
        for (String path : pathToClean) {
            boolean isMany = previousType.getField(path).isMany();
            if (path.split("/").length == 1) { // Currency //$NON-NLS-1$
                int size = document.createAccessor(parentPath + '/' + path).size();
                if (size > 0) {// element exists in document
                    indexedPathToClean.add(path);
                    Set<String> paths = getPathsWithIndex(document, path, isMany, size);
                    if (paths.size() > 0) {
                        partPathsMap.put(path, paths);// Currency[1], Currency[2]
                    }
                }
            } else {// Currency/Code, Currency/DictRecordDetails, Currency/DictRecordDetails/Action
                String firstPart = path.substring(0, path.lastIndexOf('/')); 
                String lastPart = path.substring(firstPart.length());
                Set<String> partPaths = partPathsMap.get(firstPart);
                if (partPaths != null) {
                    Set<String> paths = new TreeSet<String>();
                    for (String partPath : partPaths) {
                        String indexedPartPath = partPath + lastPart;// Currency[1]/Code, Currency[1]/DictRecordDetails, Currency[1]/DictRecordDetails[1]/Action
                        int size = document.createAccessor(parentPath + '/' + indexedPartPath).size();
                        if (size > 0) {// element exists in document
                            indexedPathToClean.add(indexedPartPath);
                            paths.addAll(getPathsWithIndex(document, indexedPartPath, isMany, size));
                        }
                    }
                    if (paths.size() > 0) {
                        partPathsMap.put(path, paths);
                    }
                }
            }
        }
        List<String> list = new ArrayList<String>();
        list.addAll(indexedPathToClean);
        Collections.reverse(list);
        return list;
    }
    
    private Set<String> getPathsWithIndex(MutableDocument document, String path, boolean isMany, int size) {
        Set<String> paths = new TreeSet<String>();
        if (isMany) {
            for (int i = size; i > 0; i--) {
                paths.add(path + '[' + i + ']'); 
            }
        } else {
            paths.add(path);
        }
        return paths;
    }

    public Date getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isAllowed(Set<String> roles) {
        return true;
    }

    @Override
    public boolean isTransient() {
        return !hasChangedType;
    }

    public MutableDocument addModificationMark(MutableDocument document) {
        throw new UnsupportedOperationException();
    }

    public MutableDocument removeModificationMark(MutableDocument document) {
        throw new UnsupportedOperationException();
    }

    public FieldMetadata getField() {
        return field;
    }

    /**
     * @return A list of {@link com.amalto.core.history.Action actions} this type change action implies. Changing from a
     * type to another may remove existing fields, this method returns list of value deleted actions when fields no
     * longer exists in new type.
     */
    public List<Action> getImpliedActions() {
        return impliedActions;
    }
}
