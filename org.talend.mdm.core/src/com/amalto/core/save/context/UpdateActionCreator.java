/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;
import org.w3c.dom.Node;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.accessor.DOMAccessor;
import com.amalto.core.history.action.FieldInsertAction;
import com.amalto.core.history.action.FieldUpdateAction;
import com.amalto.core.storage.StorageMetadataUtils;

public class UpdateActionCreator extends DefaultMetadataVisitor<List<Action>> {

    private static final Logger LOGGER = Logger.getLogger(UpdateActionCreator.class);

    protected final LinkedList<Action> actions = new LinkedList<Action>();

    protected final Date date;

    protected final String source;

    protected final String userName;

    protected final MutableDocument originalDocument;

    protected final MutableDocument newDocument;

    protected final int insertIndex;

    protected final MetadataRepository repository;

    private final Stack<String> path = new Stack<String>();

    private final Closure closure = new CompareClosure();

    private final Set<String> touchedPaths = new HashSet<String>();

    private final Map<FieldMetadata, Integer> originalFieldToLastIndex = new HashMap<FieldMetadata, Integer>();

    protected final boolean preserveCollectionOldValues;

    private String lastMatchPath;

    private boolean isDeletingContainedElement = false;

    private boolean generateTouchActions = false;
    
    private boolean isPartialDelete = false;

    private List<String> visitedOneToManyPath = new ArrayList<String>();

    private String rootTypeName = null;

    private final SaverSource saverSource;

    private final String dataCluster;

    private final String dataModel;

    public UpdateActionCreator(MutableDocument originalDocument,
                               MutableDocument newDocument,
                               Date date,
                               String source,
                               String userName,
                               boolean generateTouchActions,
                               MetadataRepository repository,
                               String dataCluster,
                               String dataModel,
                               SaverSource saverSource) {
        this(originalDocument,
                newDocument,
                date,
                false,
                -1,
                source,
                userName,
                generateTouchActions,
                repository,
                dataCluster,
                dataModel,
                saverSource);
    }

    public UpdateActionCreator(MutableDocument originalDocument,
                               MutableDocument newDocument,
                               Date date,
                               boolean preserveCollectionOldValues,
                               String source,
                               String userName,
                               boolean generateTouchActions,
                               MetadataRepository repository,
                               String dataCluster,
                               String dataModel,
                               SaverSource saverSource) {
        this(originalDocument,
                newDocument,
                date,
                preserveCollectionOldValues,
                -1,
                source,
                userName,
                generateTouchActions,
                repository,
                dataCluster,
                dataModel,
                saverSource);
    }

    public UpdateActionCreator(MutableDocument originalDocument,
                               MutableDocument newDocument,
                               Date date,
                               boolean preserveCollectionOldValues,
                               int insertIndex,
                               String source,
                               String userName,
                               boolean generateTouchActions,
                               MetadataRepository repository,
                               String dataCluster,
                               String dataModel,
                               SaverSource saverSource) {
        this.preserveCollectionOldValues = preserveCollectionOldValues;
        this.originalDocument = originalDocument;
        this.newDocument = newDocument;
        this.insertIndex = insertIndex;
        this.generateTouchActions = generateTouchActions;
        this.repository = repository;
        this.date = date;
        this.source = source;
        this.userName = userName;
        this.dataCluster = dataCluster;
        this.dataModel = dataModel;
        this.saverSource = saverSource;
    }

    @Override
    public List<Action> visit(ComplexTypeMetadata complexType) {
        if (rootTypeName == null) {
            rootTypeName = complexType.getName();
        }
        // This is an update, so both original and new document have a "entity root" element (TMDM-3883).
        generateNoOp("/"); //$NON-NLS-1$
        super.visit(complexType);
        return actions;
    }

    @Override
    public List<Action> visit(ContainedTypeFieldMetadata containedField) {
        handleField(containedField, new ContainedTypeClosure(containedField));
        return actions;
    }

    @Override
    public List<Action> visit(ReferenceFieldMetadata referenceField) {
        handleField(referenceField, getClosure());
        return actions;
    }

    @Override
    public List<Action> visit(SimpleTypeFieldMetadata simpleField) {
        handleField(simpleField, getClosure());
        return actions;
    }

    @Override
    public List<Action> visit(EnumerationFieldMetadata enumField) {
        handleField(enumField, getClosure());
        return actions;
    }

    protected Closure getClosure() {
        return closure;
    }
    
    protected void setPartialDelete(boolean isPartialDelete) {
        this.isPartialDelete = isPartialDelete;
    }

    /**
     * Interface to encapsulate action to execute on fields
     */
    interface Closure {
        void execute(FieldMetadata field);
    }

    String getLeftPath() {
        return computePath(path);
    }

    String getRightPath() {
        return computePath(path);
    }

    private String computePath(Stack<String> path) {
        if (path.isEmpty()) {
            throw new IllegalStateException();
        } else {
            StringBuilder builder = new StringBuilder();
            Iterator<String> pathIterator = path.iterator();
            while (pathIterator.hasNext()) {
                builder.append(pathIterator.next());
                if (pathIterator.hasNext()) {
                    builder.append('/');
                }
            }
            return builder.toString();
        }
    }

    /**
     * Check if need to visit the one-to-many path
     * 
     * @param path
     * @return
     */
    private boolean isNeedToVisit(String path) {
        return path.contains("[") ? false : !visitedOneToManyPath.contains(path);
    }

    protected void handleField(FieldMetadata field, Closure closure) {
        path.add(field.getName());
        if (field.isMany()) {
            String currentPath = getLeftPath();
            Accessor leftAccessor;
            Accessor rightAccessor;
            try {
                rightAccessor = newDocument.createAccessor(currentPath);
                if (!rightAccessor.exist() && !isDeletingContainedElement) {
                    // If new list does not exist, it means element was omitted in new version (legacy behavior).
                    // TMDM-9559 'isPartialDelete' only affect for ONE time on the TOP element
                    if (!isPartialDelete || !isNeedToVisit(currentPath)) {
                        return;
                    }
                }
                leftAccessor = originalDocument.createAccessor(currentPath);
                visitedOneToManyPath.add(currentPath);
            } finally {
                path.pop();
            }
            // Proceed in "reverse" order (highest index to lowest) so there won't be issues when deleting elements in
            // a sequence (if element #2 is deleted before element #3, element #3 becomes #2...).
            int max = Math.max(leftAccessor.size(), rightAccessor.size());
            for (int i = max; i > 0; i--) {
                // XPath indexes are 1-based (not 0-based).
                path.add(field.getName() + '[' + i + ']');
                closure.execute(field);
                path.pop();
            }
        } else {
            closure.execute(field);
            path.pop();
        }
    }

    @SuppressWarnings("unchecked")
    protected void compare(FieldMetadata comparedField) {
        if (comparedField.isKey()) {
            // Can't update a key: don't even try to compare the field (but update lastMatchPath in case next compared
            // element is right after key field).
            lastMatchPath = getLeftPath();
            return;
        }
        if (path.isEmpty()) {
            throw new IllegalStateException("Path for compare can not be empty.");
        }
        String path = getLeftPath();
        Accessor originalAccessor = originalDocument.createAccessor(path);
        Accessor newAccessor = newDocument.createAccessor(path);
        if (!originalAccessor.exist()) {
            if (newAccessor.exist()) { // new accessor exist
                if (newAccessor.get() != null && !newAccessor.get().isEmpty()) { // Empty accessor means no op to ensure legacy behavior
                    generateNoOp(lastMatchPath);
                    actions.add(new FieldUpdateAction(date, source, userName, path, StringUtils.EMPTY, newAccessor.get(), comparedField));
                    generateNoOp(path);
                } else if (EUUIDCustomType.AUTO_INCREMENT.getName().equalsIgnoreCase(comparedField.getType().getName())) {
                    generateNoOp(lastMatchPath);
                    String conceptName = rootTypeName + "." + comparedField.getName().replaceAll("/", "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    String autoIncrementValue = saverSource.nextAutoIncrementId(dataCluster, dataModel, conceptName);
                    actions.add(new FieldUpdateAction(date, source, userName, path, StringUtils.EMPTY, autoIncrementValue,
                            comparedField));
                    generateNoOp(path);
                } else if (EUUIDCustomType.UUID.getName().equalsIgnoreCase(comparedField.getType().getName())) {
                    String uuidValue = UUID.randomUUID().toString();
                    actions.add(new FieldUpdateAction(date, source, userName, path, StringUtils.EMPTY, uuidValue, comparedField));
                }
            }
        } else { // original accessor exist
            String oldValue = originalAccessor.get();
            lastMatchPath = path;
            if (!newAccessor.exist()) {
                if (comparedField.isMany() && !preserveCollectionOldValues) {
                    // TMDM-5216: Visit sub fields include old/new values for sub elements.
                    // TMDM-6868: Keeps previous value of isDeletingContainedElement in case element contains also
                    // contained-typed elements.
                    if (comparedField instanceof ContainedTypeFieldMetadata) {
                        boolean previous = isDeletingContainedElement; // Back up flag value
                        try {
                            isDeletingContainedElement = true;
                            TypeMetadata type = repository.getNonInstantiableType(repository.getUserNamespace(), originalAccessor.getActualType());
                            if (type == null) {
                                type = ((ContainedTypeFieldMetadata) comparedField).getContainedType();
                            }
                            type.accept(this);
                        } finally {
                            isDeletingContainedElement = previous; // Restore previous value for flag.
                        }
                    } else if (!isDeletingContainedElement) {
                        // TMDM-5257: RemoveSimpleTypeNodeWithOccurrence
                        // Null values may happen if accessor is targeting an element that contains other elements
                        actions.add(new FieldUpdateAction(date, source, userName, path, oldValue == null ? StringUtils.EMPTY
                                : oldValue, null, comparedField));
                    }
                }
                else if(!comparedField.isMany() && this.newDocument.considerMissingElementsAsEmpty()){
                    actions.add(new FieldUpdateAction(date, source, userName, path, oldValue == null ? StringUtils.EMPTY : oldValue, StringUtils.EMPTY, comparedField));
                }
                
                if (isDeletingContainedElement) {
                    // Null values may happen if accessor is targeting an element that contains other elements
                    actions.add(new FieldUpdateAction(date, source, userName, path, oldValue == null ? StringUtils.EMPTY
                            : oldValue, null, comparedField));
                }
            } else { // new accessor exist
                String newValue = newAccessor.get();
                if (newValue != null && !(comparedField instanceof ContainedTypeFieldMetadata)) {
                    if (comparedField.isMany() && preserveCollectionOldValues) {
                        // Append at the end of the collection
                        if (!originalFieldToLastIndex.containsKey(comparedField)) {
                            originalFieldToLastIndex.put(comparedField, originalAccessor.size());
                        }
                        String previousPathElement = this.path.pop();
                        int insertIndex = getInsertIndex(comparedField);
                        this.path.push(comparedField.getName() + "[" + insertIndex + "]");
                        actions.add(new FieldInsertAction(date, source, userName, getLeftPath(), StringUtils.EMPTY, newValue, comparedField));
                        this.path.pop();
                        this.path.push(previousPathElement);
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
                            if (EUUIDCustomType.AUTO_INCREMENT.getName().equalsIgnoreCase(
                                            comparedField.getType().getName())) {
                                String conceptName = rootTypeName + "." + comparedField.getName().replaceAll("/", "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                newValue = saverSource.nextAutoIncrementId(dataCluster, dataModel, conceptName);
                            } else if (EUUIDCustomType.UUID.getName().equalsIgnoreCase(comparedField.getType().getName())) {
                                newValue = UUID.randomUUID().toString();
                            }
                        }
                        actions.add(new FieldUpdateAction(date, source, userName, path, oldValue, newValue.isEmpty() ? null : newValue, comparedField));
                    } else if (oldValue != null && oldValue.equals(newValue)) {
                        generateNoOp(path);
                    }
                }
            }
        }
    }

    private int getInsertIndex(FieldMetadata comparedField) {
        if (insertIndex < 0) {
            int newIndex = originalFieldToLastIndex.get(comparedField);
            newIndex = newIndex + 1;
            originalFieldToLastIndex.put(comparedField, newIndex);
            return newIndex;
        } else {
            return insertIndex;
        }
    }

    protected void generateNoOp(String path) {
        if (generateTouchActions) {
            boolean exist = (path != null && originalDocument.createAccessor(path).exist());
            if (exist && !touchedPaths.contains(path)) {
                touchedPaths.add(path);
                actions.add(new TouchAction(path, date, source, userName));
            }
        }
    }

    private class ContainedTypeClosure implements Closure {

        private final ContainedTypeFieldMetadata containedField;

        public ContainedTypeClosure(ContainedTypeFieldMetadata containedField) {
            this.containedField = containedField;
        }

        public void execute(FieldMetadata field) {
            ComplexTypeMetadata type = containedField.getContainedType();

            Accessor leftAccessor = originalDocument.createAccessor(getLeftPath());
            Accessor rightAccessor = newDocument.createAccessor(getRightPath());
            if (rightAccessor.exist()) {
                String newType = rightAccessor.getActualType();
                String previousType = StringUtils.EMPTY;
                if (leftAccessor.exist()) {
                    previousType = leftAccessor.getActualType();
                }

                if (!newType.isEmpty() && !newType.startsWith(MetadataRepository.ANONYMOUS_PREFIX)) {
                    ComplexTypeMetadata newTypeMetadata = (ComplexTypeMetadata) repository.getNonInstantiableType(repository.getUserNamespace(), newType);
                    ComplexTypeMetadata previousTypeMetadata = (ComplexTypeMetadata) repository.getNonInstantiableType(repository.getUserNamespace(), previousType);
                    // Perform some checks about the xsi:type value (valid or not?).
                    if (newTypeMetadata == null) {
                        throw new IllegalArgumentException("Type '" + newType + "' was not found.");
                    }
                    // Check if type of element isn't a subclass of declared type (use of xsi:type).
                    if (!newTypeMetadata.isAssignableFrom(field.getType())) {
                        throw new IllegalArgumentException("Type '" + field.getType().getName()
                                + "' is not assignable from type '" + newTypeMetadata.getName() + "'");
                    }
                    if (!newTypeMetadata.getSuperTypes().isEmpty() || !newTypeMetadata.getSubTypes().isEmpty()) {
                        actions.addAll(ChangeTypeAction.create(originalDocument, date, source, userName, getLeftPath(), previousTypeMetadata, newTypeMetadata, field));
                    } else if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Ignore type change on '" + getLeftPath() + "': type '" + newTypeMetadata.getName() + "' does not belong to an inheritance tree.");
                    }
                    type = newTypeMetadata;
                }
            }
            Action before = actions.isEmpty() ? null : actions.getLast();
            type.accept(UpdateActionCreator.this);

            compare(field);
            // Way to detect if there is a change in elements below: check if last action in list changed.
            Action last = actions.isEmpty() ? null : actions.getLast();
            boolean hasActions = last != before;
            if (leftAccessor.exist() || (rightAccessor.exist() && hasActions)) {
                lastMatchPath = getLeftPath();
            }
        }
    }

    private class CompareClosure implements Closure {

        public void execute(FieldMetadata field) {
            compare(field);
            if (field instanceof ReferenceFieldMetadata) {
                Accessor leftAccessor = originalDocument.createAccessor(getLeftPath());
                Accessor rightAccessor = newDocument.createAccessor(getRightPath());
                if (rightAccessor.exist()) {
                    String newType = rightAccessor.getActualType();
                    String previousType = StringUtils.EMPTY;
                    if (leftAccessor.exist()) {
                        previousType = leftAccessor.getActualType();
                    }

                    if (!newType.isEmpty() && !newType.startsWith(MetadataRepository.ANONYMOUS_PREFIX)) {
                        ComplexTypeMetadata newTypeMetadata = (ComplexTypeMetadata) repository.getNonInstantiableType(repository.getUserNamespace(), newType);
                        ComplexTypeMetadata previousTypeMetadata = null;
                        if (newTypeMetadata != null && !newTypeMetadata.isInstantiable()) {
                            ComplexTypeMetadata actualNewTypeMetadata = newTypeMetadata;
                            Collection<TypeMetadata> instantiableTypes = repository.getInstantiableTypes();
                            for (TypeMetadata instantiableType : instantiableTypes) {
                                if (newType.equals(instantiableType.getData(MetadataRepository.COMPLEX_TYPE_NAME))) {
                                    actualNewTypeMetadata = (ComplexTypeMetadata) instantiableType;
                                }
                            }
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Replacing type '" + newType + "' with '" + actualNewTypeMetadata.getName() + ".");
                            }
                            newTypeMetadata = actualNewTypeMetadata;
                            previousTypeMetadata = (ComplexTypeMetadata) repository.getNonInstantiableType(repository.getUserNamespace(), previousType);
                        } else if (newTypeMetadata == null) {
                            newTypeMetadata = (ComplexTypeMetadata) repository.getType(newType);
                            previousTypeMetadata = (ComplexTypeMetadata) repository.getType(previousType);
                        }
                        // Record the type change information (if applicable).
                        if (!newTypeMetadata.getSuperTypes().isEmpty() || !newTypeMetadata.getSubTypes().isEmpty()) {
                            actions.addAll(ChangeReferenceTypeAction.create(originalDocument, date, source, userName, getLeftPath(), previousTypeMetadata, newTypeMetadata, field));
                        } else if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Ignore reference type change on '" + getLeftPath() + "': type '" + newTypeMetadata.getName() + "' does not belong to an inheritance tree.");
                        }
                    }
                }
                if (leftAccessor.exist() || rightAccessor.exist()) {
                    lastMatchPath = getLeftPath();
                }
            }
        }
    }
}
