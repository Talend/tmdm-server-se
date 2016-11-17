/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import com.amalto.core.history.DOMMutableDocument;
import com.amalto.core.load.io.ResettableStringWriter;
import com.amalto.core.storage.StorageMetadataUtils;
import org.w3c.dom.Node;
import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.action.FieldInsertAction;
import com.amalto.core.history.action.FieldUpdateAction;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.*;

import java.util.*;

public class PartialUpdateActionCreator extends UpdateActionCreator {

    private static final Logger LOGGER = Logger.getLogger(PartialUpdateActionCreator.class);

    private final String partialUpdatePivot;

    private final String partialUpdateKey;

    private final Map<FieldMetadata, Integer> originalFieldToLastIndex = new HashMap<FieldMetadata, Integer>();

    private final Stack<String> leftPath = new Stack<String>();

    private final ResettableStringWriter leftCache = new ResettableStringWriter();

    private final Stack<String> rightPath = new Stack<String>();

    private final ResettableStringWriter rightCache = new ResettableStringWriter();

    private final Map<String, String> keyValueToPath = new HashMap<String, String>();

    private final LinkedList<String> usedPaths = new LinkedList<String>();

    private final Closure closure;

    private boolean preserveCollectionOldValues;

    private String lastMatchPath;

    private boolean inPivot;

    private ComplexTypeMetadata mainType;

    public PartialUpdateActionCreator(MutableDocument originalDocument,
                                      MutableDocument newDocument,
                                      Date date,
                                      boolean preserveCollectionOldValues,
                                      int insertIndex,
                                      String pivot,
                                      String key,
                                      String source,
                                      String userName,
                                      boolean generateTouchActions,
                                      MetadataRepository repository,
                                      String dataCluster,
                                      String dataModel,
                                      SaverSource saverSource) {
        super(originalDocument,
                newDocument,
                date,
                preserveCollectionOldValues,
                insertIndex,
                source,
                userName,
                generateTouchActions,
                repository,
                dataCluster,
                dataModel,
                saverSource);
        this.preserveCollectionOldValues = preserveCollectionOldValues;
        if (!pivot.isEmpty()) {
            // Pivot MUST NOT end with '/' and key MUST start with '/' (see TMDM-4381).
            if (pivot.charAt(pivot.length() - 1) == '/') {
                partialUpdatePivot = pivot.substring(0, pivot.length() - 1);
            } else {
                partialUpdatePivot = pivot;
            }
            if (!key.isEmpty() && key.charAt(0) != '/') {
                this.partialUpdateKey = '/' + key;
            } else {
                this.partialUpdateKey = key;
            }
        } else {
            inPivot = true;
            partialUpdatePivot = originalDocument.getType().getName();
            partialUpdateKey = key;
        }
        // Special comparison closure for partial update that compares only if we are in pivot.
        closure = new Closure() {
            public void execute(FieldMetadata field) {
                if (inPivot) {
                    compare(field);
                }
            }
        };
        // Initialize key values in database document to a path in partial update document.
        Accessor accessor = newDocument.createAccessor(partialUpdatePivot);
        for (int i = 1; i <= accessor.size(); i++) {
            String path = partialUpdatePivot + '[' + i + ']';
            Accessor keyAccessor = newDocument.createAccessor(path + partialUpdateKey);
            if (!keyAccessor.exist()) {
                throw new IllegalStateException("Path '" + path + partialUpdateKey + "' does not exist in document sent for partial update."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String keyValue = keyAccessor.get();
            if (keyValue != null) {
                keyValueToPath.put(keyValue, path);
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Key value at '" + path + "' has no value (was null). Ignoring it."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    public PartialUpdateActionCreator(MutableDocument originalDocument, MutableDocument newDocument, Date date,
            boolean preserveCollectionOldValues, int insertIndex, String pivot, String key, String source, String userName,
            boolean generateTouchActions, MetadataRepository repository) {
        this(originalDocument, newDocument, date, preserveCollectionOldValues, insertIndex, pivot, key, source, userName,
                generateTouchActions, repository, null, null, null);
    }

    @Override
    public List<Action> visit(ComplexTypeMetadata complexType) {
        if (mainType == null) {
            mainType = complexType;
        }
        List<Action> actionList = super.visit(complexType);
        if (complexType == mainType) {
            if (!preserveCollectionOldValues) {
                /*
                 * There might be elements not used for the update. In case overwrite=true, expected behavior is to append
                 * unused elements at the end. The code below removes used elements in partial update (with overwrite=true)
                 * then do a new partial update (with overwrite=false) so new elements are added at the end (this is the
                 * behavior of a overwrite=false).
                 */
                // TODO these code will be replaced by fhuaulme's new API
                List<Node> nodes = new ArrayList<Node>();
                for (String usedPath : usedPaths) {
                    Accessor accessor = newDocument.createAccessor(usedPath);
                    if (accessor.exist()){
                        accessor.touch();
                        if (newDocument instanceof DOMMutableDocument) {
                            nodes.add(((DOMMutableDocument) newDocument).getLastAccessedNode());
                        }
                    }
                }
                for (Node node : nodes) {
                    if (node != null && node.getParentNode() != null){
                        node.getParentNode().removeChild(node);
                    }
                }
                // Since this a costly operation do this only if there are still elements under the pivot.
                int leftElementCount = newDocument.createAccessor(StringUtils.substringBeforeLast(partialUpdatePivot, "/")).size(); //$NON-NLS-1$
                if (leftElementCount > 0) {
                    preserveCollectionOldValues = true;
                    mainType.accept(this);
                }
            }
        }
        return actionList;
    }

    private static void resetPath(String currentPath, Stack<String> path) {
        StringTokenizer pathIterator = new StringTokenizer(currentPath, "/"); //$NON-NLS-1$
        path.clear();
        while (pathIterator.hasMoreTokens()) {
            path.add(pathIterator.nextToken());
        }
    }

    @Override
    protected Closure getClosure() {
        return closure;
    }

    String getLeftPath() {
        return computePath(leftPath, leftCache);
    }

    String getRightPath() {
        return computePath(rightPath, rightCache);
    }

    private String computePath(Stack<String> path, ResettableStringWriter cache) {
        if (path.isEmpty()) {
            return StringUtils.EMPTY;
        } else {
            Iterator<String> pathIterator = path.iterator();
            while (pathIterator.hasNext()) {
                cache.append(pathIterator.next());
                if (pathIterator.hasNext()) {
                    cache.append('/');
                }
            }
            return cache.reset();
        }
    }

    protected void handleField(FieldMetadata field, Closure closure) {
        enterLeft(field);
        enterRight(field);
        boolean isPivot = partialUpdatePivot.equals(getLeftPath());
        try {
            if (!inPivot && isPivot) {
                inPivot = true;
                if (!field.isMany()) {
                    LOGGER.warn("Partial update pivot '" + partialUpdatePivot + "' is not a repeatable element (it might be a configuration issue)."); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            if (inPivot && field.isMany()) {
                Accessor leftAccessor;
                Accessor rightAccessor;
                rightAccessor = newDocument.createAccessor(getRightPath());
                if (!rightAccessor.exist()) {
                    // If new list does not exist, it means element was omitted in new version (legacy behavior).
                    // TMDM-nnnn: Don't forget to exit current fields to prevent incorrect comparisons.
                    leaveLeft();
                    leaveRight();
                    return;
                }
                leftAccessor = originalDocument.createAccessor(getLeftPath());
                leaveLeft();
                leaveRight();
                int max = Math.max(leftAccessor.size(), rightAccessor.size());
                for (int i = 1; i <= max; i++) {
                    // XPath indexes are 1-based (not 0-based).
                    if (preserveCollectionOldValues) {
                        if (insertIndex < 0) {
                            enterLeft(field, (leftAccessor.size() + i));
                        } else {
                            enterLeft(field, insertIndex);
                        }
                    } else {
                        enterLeft(field, i);
                    }
                    doCompare(field, closure, i);
                    leaveLeft();
                }
                if (preserveCollectionOldValues) {
                    enterLeft(field, leftAccessor.size() + rightAccessor.size());
                } else {
                    enterLeft(field, max);
                }
                lastMatchPath = getLeftPath();
                leaveLeft();
            } else {
                closure.execute(field);
                leaveLeft();
                leaveRight();
            }
        } finally {
            if (inPivot && isPivot) {
                inPivot = false;
            }
        }
    }

    private void enterLeft(FieldMetadata field) {
        leftPath.add(field.getName());
    }

    private void enterLeft(FieldMetadata field, int position) {
        leftPath.add(field.getName() + '[' + position + ']');
    }

    private void leaveLeft() {
        if (!leftPath.isEmpty()) {
            leftPath.pop();
        }
    }

    private void enterRight(FieldMetadata field) {
        rightPath.add(field.getName());
    }

    private void enterRight(FieldMetadata field, int position) {
        rightPath.add(field.getName() + '[' + position + ']');
    }

    private void leaveRight() {
        if (!rightPath.isEmpty()) {
            rightPath.pop();
        }
    }

    private void doCompare(FieldMetadata field, Closure closure, int i) {
        if (inPivot) {
            String left = getLeftPath();
            Accessor originalKeyAccessor = originalDocument.createAccessor(left + '/' + partialUpdateKey);
            String newDocumentPath;
            if (originalKeyAccessor.exist()) {
                newDocumentPath = keyValueToPath.get(originalKeyAccessor.get());
                usedPaths.add(newDocumentPath);
            } else {
                newDocumentPath = null;
            }
            if (newDocumentPath == null) {
                if (preserveCollectionOldValues) {
                    enterRight(field, i);
                } else if(usedPaths.size() != 0 && usedPaths.getLast() != null && left.startsWith(usedPaths.getLast())) { // Implicit !preserveCollectionOldValues
                    enterRight(field, i);
                } else {
                    return;
                }
            } else {
                resetPath(newDocumentPath, rightPath);
            }
        } else {
            enterRight(field, i);
        }
        closure.execute(field);
        leaveRight();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void compare(FieldMetadata comparedField) {
        if (comparedField.isKey()) {
            // Can't update a key: don't even try to compare the field (but update lastMatchPath in case next compared
            // element is right after key field).
            lastMatchPath = getLeftPath();
            return;
        }
        if (rightPath.isEmpty()) {
            throw new IllegalStateException("Path in new document can not be empty."); //$NON-NLS-1$
        }
        if (leftPath.isEmpty()) {
            throw new IllegalStateException("Path in database document can not be empty."); //$NON-NLS-1$
        }
        String leftPath = getLeftPath();
        String rightPath = getRightPath();
        Accessor originalAccessor = originalDocument.createAccessor(leftPath);
        Accessor newAccessor = newDocument.createAccessor(rightPath);
        if (!originalAccessor.exist()) {
            if (newAccessor.exist()) { // new accessor exist (no op if it does not).
                String newValue = newAccessor.get();
                if (newValue != null && !newValue.isEmpty()) {
                    generateNoOp(lastMatchPath);
                    if (insertIndex < 0) {
                        actions.add(new FieldUpdateAction(date, source, userName, leftPath, StringUtils.EMPTY, newValue, comparedField));
                    } else {
                        actions.add(new FieldInsertAction(date, source, userName, leftPath, StringUtils.EMPTY, newValue, comparedField));
                    }
                }
            }
        } else { // original accessor exist
            String oldValue = originalAccessor.get();
            lastMatchPath = leftPath;
            if (newAccessor.exist()) {
                String newValue = newAccessor.get();
                if (newValue != null) {
                    if (comparedField.isMany()) {
                        if (preserveCollectionOldValues) {
                            // Append at the end of the collection
                            if (!originalFieldToLastIndex.containsKey(comparedField)) {
                                originalFieldToLastIndex.put(comparedField, originalAccessor.size());
                            }
                            leaveLeft();
                            if (insertIndex < 0) {
                                int newIndex = originalFieldToLastIndex.get(comparedField);
                                enterLeft(comparedField, (newIndex + 1));
                                actions.add(new FieldUpdateAction(date, source, userName, getLeftPath(), StringUtils.EMPTY,
                                        newValue, comparedField));
                                originalFieldToLastIndex.put(comparedField, newIndex + 1);
                            } else {
                                enterLeft(comparedField, insertIndex);
                                actions.add(new FieldInsertAction(date, source, userName, getLeftPath(), StringUtils.EMPTY,
                                        newValue, comparedField));
                            }
                        }
                    } else if (oldValue != null && !oldValue.equals(newValue)) {
                        if (!Types.STRING.equals(comparedField.getType().getName()) && !(comparedField instanceof ReferenceFieldMetadata)) {
                            // Field is not string. To ensure false positive difference detection, creates a typed value.
                            Object oldObject = StorageMetadataUtils.convert(oldValue, comparedField);
                            Object newObject = StorageMetadataUtils.convert(newValue, comparedField);
                            if (oldObject != null && newObject != null && oldObject instanceof Comparable) {
                                if (((Comparable) oldObject).compareTo(newObject) == 0) {
                                    // Objects are the 'same' (e.g. 10.0 is same as 10).
                                    return;
                                }
                            } else {
                                if (oldObject != null && oldObject.equals(newObject)) {
                                    return;
                                } else if (newObject != null && newObject.equals(oldObject)) {
                                    return;
                                }
                            }
                        }
                        if (insertIndex < 0) {
                            actions.add(new FieldUpdateAction(date, source, userName, leftPath, oldValue, newValue, comparedField));
                        } else {
                            actions.add(new FieldInsertAction(date, source, userName, leftPath, oldValue, newValue, comparedField));
                        }
                    }
                }
            }
        }
    }
}
