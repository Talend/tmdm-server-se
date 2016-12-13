/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.integrity;

import java.util.Map;
import java.util.Set;

import com.amalto.core.util.XtentisException;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

/**
 * Abstraction of data storage with only operations needed for FK integrity check.
 */
public interface FKIntegrityCheckDataSource {
    /**
     * Gets the data model for the instance in <code>clusterName</code> with type <code>concept</code> with id
     * <code>ids</code>.
     *
     * @param clusterName A valid data cluster name.
     * @param concept     A valid type name.
     * @param ids         Id of an existing instance in database. In case of composite key, each item of the array contains
     *                    a key value.
     * @return Returns the data model for the instance in <code>clusterName</code> with type <code>concept</code>
     *         with id <code>ids</code>.
     * @throws com.amalto.core.util.XtentisException
     *          In case of any exception during data model check.
     */
    String getDataModel(String clusterName, String concept, String[] ids) throws XtentisException;

    /**
     * <p>
     * Count how many instances of <code>fromType</code> points to instance to <code>referenceId</code> via the
     * relation <code>fromReference</code>.
     * </p>
     * <p>
     * <i>Note</i>: Type of instance with id <code>id</code> should be inferred from <code>fromReference</code>.
     * </p>
     *
     * @param clusterName   Cluster name where the count should be done.
     * @param ids           Id of an existing instance in database. In case of composite key, each item of the array contains
     *                      a key value.
     * @param fromTypeName  Type name where count is performed.
     * @param fromReference Reference from <code>fromType</code> to the id <code>id</code>.   @return A count greater or equals to 0.
     * @return A count greater or equals to 0.
     * @throws com.amalto.core.util.XtentisException
     *          In case of any exception during count execution.
     */
    long countInboundReferences(String clusterName, String[] ids, String fromTypeName, ReferenceFieldMetadata fromReference) throws XtentisException;

    /**
     * <p>
     * Returns a {@link java.util.Set} of {@link FieldMetadata} of all fields that <b>point to</b> the
     * concept <code>concept</code>. Fields are inferred from data model only (thus no need for id in this method).
     * </p>
     * <p>
     * Returns a empty set if type in <code>concept</code> does not exist.
     * </p>
     * @param dataModel A data model name
     * @param concept   A concept name.
     * @return A {@link java.util.Set} of {@link ReferenceFieldMetadata} or empty set if no field points to
     *         <code>concept</code>.
     * @throws com.amalto.core.util.XtentisException
     *          In case of unexpected error during metadata analysis.
     */
    Set<ReferenceFieldMetadata> getForeignKeyList(String concept, String dataModel) throws XtentisException;

    /**
     * Called by FK integrity check when a conflict is found based on static metadata check. This method is designed
     * for information purposes only (e.g. display conflict resolution on log).
     *
     * @param checkResultToFields A {@link java.util.Map} containing all references checked sorted by FK integrity to apply.
     * @param conflictResolution  A {@link com.amalto.core.integrity.FKIntegrityCheckResult} value, result of the conflict resolution.
     */
    void resolvedConflict(Map<FKIntegrityCheckResult, Set<FieldMetadata>> checkResultToFields, FKIntegrityCheckResult conflictResolution);
}
