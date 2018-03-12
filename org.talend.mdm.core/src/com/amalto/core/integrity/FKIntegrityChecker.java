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

package com.amalto.core.integrity;

import static com.amalto.core.integrity.FKIntegrityCheckResult.ALLOWED;
import static com.amalto.core.integrity.FKIntegrityCheckResult.FORBIDDEN;
import static com.amalto.core.integrity.FKIntegrityCheckResult.FORBIDDEN_OVERRIDE_ALLOWED;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.amalto.core.server.ServerContext;
import com.amalto.core.util.XtentisException;
import org.talend.mdm.commmon.metadata.*;

/**
 * <p>
 * Entry point for all FK integrity checks. See below example of use:
 * </p>
 */
public class FKIntegrityChecker {

    private final static FKIntegrityChecker instance = new FKIntegrityChecker();

    private static final FKIntegrityCheckDataSource DEFAULT_DATA_SOURCE = new DefaultCheckDataSource();

    private FKIntegrityChecker() {
    }

    /**
     * @return Returns singleton instance of {@link FKIntegrityChecker}.
     */
    public static FKIntegrityChecker getInstance() {
        return instance;
    }

    /**
     * <p>
     * Returns a MDM user is allowed to delete an instance of type <code>concept</code> with id <code>ids</code>.
     * </p>
     *
     * @param clusterName An existing cluster name.
     * @param concept     An existing concept name.
     * @param ids         An instance ID (an array of values in case of composite keys).
     * @param override    <code>true</code> if user wants to override fk integrity (but {@link #getFKIntegrityPolicy(String, String, String[])}
     *                    <b>must</b> return {@link FKIntegrityCheckResult#FORBIDDEN_OVERRIDE_ALLOWED} in this case).
     * @return <code>true</code> if user is allowed to delete instance, <code>false</code> otherwise.
     * @throws XtentisException In case of unexpected error.
     * @see #allowDelete(String, String, String[], boolean, FKIntegrityCheckDataSource)
     */
    public boolean allowDelete(String clusterName, String concept, String[] ids, boolean override) throws XtentisException {
        return allowDelete(clusterName, concept, ids, override, DEFAULT_DATA_SOURCE);
    }

    /**
     * <p>
     * Returns a MDM user is allowed to delete an instance of type <code>concept</code> with id <code>ids</code>.
     * </p>
     *
     * @param clusterName An existing cluster name.
     * @param concept     An existing concept name.
     * @param ids         An instance ID (an array of values in case of composite keys).
     * @param override    <code>true</code> if user wants to override fk integrity (but {@link #getFKIntegrityPolicy(String, String, String[])}
     *                    <b>must</b> return {@link FKIntegrityCheckResult#FORBIDDEN_OVERRIDE_ALLOWED} in this case).
     * @param dataSource  A {@link FKIntegrityCheckResult} implementation to use during this check.
     * @return <code>true</code> if user is allowed to delete instance, <code>false</code> otherwise.
     * @throws XtentisException In case of unexpected error.
     */
    public boolean allowDelete(String clusterName, String concept, String[] ids, boolean override, FKIntegrityCheckDataSource dataSource) throws XtentisException {
        FKIntegrityCheckResult policy = getFKIntegrityPolicy(clusterName, concept, ids, dataSource);
        switch (policy) {
            case FORBIDDEN:
                return false;
            case FORBIDDEN_OVERRIDE_ALLOWED:
                return override; // return true if overriding... false otherwise.
            case ALLOWED:
                return true;
            default:
                throw new XtentisException("Value '" + policy + "' is not supported.");  //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * <p>
     * Returns what kind of integrity check is allowed when deleting an instance of <code>concept</code> with id <code>ids</code>.
     * </p>
     * <p>
     * <b>Note:</b> The data model name will be extracted from the document stored in database.
     * </p>
     *
     * @param clusterName An existing cluster name.
     * @param concept     An existing concept name.
     * @param ids         An instance ID (an array of values in case of composite keys).
     * @return A value of {@link FKIntegrityCheckResult} that corresponds to the type of policy that should be enforced.
     * @throws XtentisException In case of unexpected error during check.
     * @see FKIntegrityCheckResult
     * @see #getFKIntegrityPolicy(String, String, String[], FKIntegrityCheckDataSource)
     */
    public FKIntegrityCheckResult getFKIntegrityPolicy(String clusterName, String concept, String[] ids) throws XtentisException {
        return getFKIntegrityPolicy(clusterName, concept, ids, DEFAULT_DATA_SOURCE);
    }

    /**
     * <p>
     * Returns what kind of integrity check is allowed when deleting an instance of <code>concept</code> with id <code>ids</code>.
     * </p>
     * <p>
     * <b>Note:</b> The data model name will be extracted from the document stored in database.
     * </p>
     *
     * @param clusterName An existing cluster name.
     * @param concept     An existing concept name.
     * @param ids         An instance ID (an array of values in case of composite keys).
     * @param dataSource  A {@link FKIntegrityCheckResult} implementation to use during this check.
     * @return A value of {@link FKIntegrityCheckResult} that corresponds to the type of policy that should be enforced.
     * @throws XtentisException In case of unexpected error during check.
     * @see FKIntegrityCheckResult
     */
    public FKIntegrityCheckResult getFKIntegrityPolicy(String clusterName, String concept, String[] ids, FKIntegrityCheckDataSource dataSource) throws XtentisException {
        // Extract data model from database
        String dataModel = dataSource.getDataModel(clusterName, concept, ids);
        // Gets field(s) to check
        Set<ReferenceFieldMetadata> fieldToCheck = dataSource.getForeignKeyList(concept, dataModel);
        // Sort all fields by FK integrity policy
        Map<FKIntegrityCheckResult, Set<FieldMetadata>> checkResultToFields = new HashMap<FKIntegrityCheckResult, Set<FieldMetadata>>();
        for (ReferenceFieldMetadata incomingReference : fieldToCheck) {
            // TMDM-5434: Checks if containing type is an actual entity type
            String referencingTypeName = incomingReference.getEntityTypeName();
            MetadataRepository repository = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin().get(dataModel);
            ComplexTypeMetadata containingType = repository.getComplexType(referencingTypeName);
            if (containingType == null || !containingType.isInstantiable()) {
                continue; // Discard checks from reusable types.
            }
            if (incomingReference.isFKIntegrity()) { // Don't execute a count if we don't care about FK integrity for the field.
                boolean allowOverride = incomingReference.allowFKIntegrityOverride();
                long count = dataSource.countInboundReferences(clusterName, ids, referencingTypeName, incomingReference);
                if (count > 0) {
                    if (allowOverride) {
                        get(checkResultToFields, FORBIDDEN_OVERRIDE_ALLOWED).add(incomingReference);
                    } else {
                        get(checkResultToFields, FORBIDDEN).add(incomingReference);
                    }
                } else {
                    get(checkResultToFields, ALLOWED).add(incomingReference);
                }
            } else {
                // FK definition does not enforce FK integrity so it's allowed.
                get(checkResultToFields, ALLOWED).add(incomingReference);
            }
        }
        if (checkResultToFields.isEmpty()) {
            // No FK pointing to record was found... returns allowed.
            return ALLOWED;
        }
        // Interpretation of results
        if (hasOnly(FORBIDDEN, checkResultToFields)) {
            return FORBIDDEN;
        } else if (hasOnly(FORBIDDEN_OVERRIDE_ALLOWED, checkResultToFields)) {
            return FORBIDDEN_OVERRIDE_ALLOWED;
        } else if (hasOnly(ALLOWED, checkResultToFields)) {
            return ALLOWED;
        } else {
            // Mixed results (some fields are forbidden and/or forbidden allowed and/or allowed)
            // Order of 'if' matters since method should return the less permissive policy (FORBIDDEN has higher priority
            // than ALLOWED).
            FKIntegrityCheckResult conflictResolution;
            if (has(FORBIDDEN, checkResultToFields)) {
                conflictResolution = FORBIDDEN;
            } else if (has(FORBIDDEN_OVERRIDE_ALLOWED, checkResultToFields)) {
                conflictResolution = FORBIDDEN_OVERRIDE_ALLOWED;
            } else if (has(ALLOWED, checkResultToFields)) {
                conflictResolution = ALLOWED;
            } else {
                throw new IllegalStateException("Cannot resolve FK integrity conflict."); //$NON-NLS-1$
            }
            // Log in server's log how conflict was solved.
            dataSource.resolvedConflict(checkResultToFields, conflictResolution);
            return conflictResolution;
        }
    }

    /**
     * 'Safe' getter for map. If value does not exist, creates it and put it at the right key for next calls.
     *
     * @param map The map to be queried.
     * @param key The key where the caller expects an non-null value.
     * @return The value for <code>key</code> or a newly created value if it didn't exist in <code>map</code>.
     */
    private static <K,V> Set<V> get(Map<K, Set<V>> map, K key) {
        Set<V> value = map.get(key);
        if (value == null) {
            value = new HashSet<V>();
            map.put(key, value);
        }
        return value;
    }

    /**
     * @param checkResult         A {@link FKIntegrityCheckResult} value.
     * @param checkResultToFields A {@link Map} containing {@link FieldMetadata} sorted by key {@link FKIntegrityCheckResult} depending on
     *                            what kind of integrity check should be performed.
     * @return true if <code>checkResultToFields</code> contains <b>at least one</b> <code>checkResult</code>, false otherwise.
     * @see #hasOnly(FKIntegrityCheckResult, java.util.Map)
     */
    private static boolean has(FKIntegrityCheckResult checkResult, Map<FKIntegrityCheckResult, Set<FieldMetadata>> checkResultToFields) {
        return checkResultToFields.get(checkResult) != null;
    }

    /**
     * @param checkResult         A {@link FKIntegrityCheckResult} value.
     * @param checkResultToFields A {@link Map} containing {@link FieldMetadata} sorted by key {@link FKIntegrityCheckResult} depending on
     *                            what kind of integrity check should be performed.
     * @return true if <code>checkResultToFields</code> contains <b>only</b> <code>checkResult</code>, false otherwise.
     * @see #has(FKIntegrityCheckResult, java.util.Map)
     */
    private static boolean hasOnly(FKIntegrityCheckResult checkResult, Map<FKIntegrityCheckResult, Set<FieldMetadata>> checkResultToFields) {
        return checkResultToFields.get(checkResult) != null && checkResultToFields.size() == 1;
    }
}
