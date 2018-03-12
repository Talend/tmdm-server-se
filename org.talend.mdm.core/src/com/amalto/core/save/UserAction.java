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

package com.amalto.core.save;

public enum UserAction {
    /**
     * MDM will decide whether user is doing a {@link #CREATE} or {@link #UPDATE}.
     */
    AUTO,
    /**
     * MDM will decide whether user is doing a {@link #CREATE_STRICT} or {@link #UPDATE}.
     */
    AUTO_STRICT,
    /**
     * <p>
     * User is creating a new record (record is not expected to be already stored in database). CREATE overrides
     * user-provided values for MDM generated values (AutoIncrement, UUID).
     * </p>
     * <p>If you want you to ensure MDM takes the values supplied by user (incl. for AutoIncrement and UUID), see
     * {@link #CREATE_STRICT}.</p>
     */
    CREATE,
    /**
     * Similar to {@link #CREATE}, but won't override id values provided by the user (even if id is AutoIncrement or
     * UUID).
     */
    CREATE_STRICT,
    /**
     * User is replacing database content with a new version of the record (record is expected to already exist in
     * database).
     */
    REPLACE,
    /**
     * User has only sent values that should be modified in database. For many valued elements, the <b>whole</b>
     * sequence of elements is replaced.
     */
    UPDATE,
    /**
     * User has only sent values that should be modified in database. Using {@link com.amalto.core.save.DocumentSaverContext#getPartialUpdatePivot()}
     * and {@link DocumentSaverContext#getPartialUpdateKey()}, user can perform selective changes.
     */
    PARTIAL_UPDATE,
    /**
     * User has only sent values that should be deleted in database. Using {@link com.amalto.core.save.DocumentSaverContext#getPartialUpdatePivot()}
     * and {@link DocumentSaverContext#getPartialUpdateKey()}, user can perform selective changes.
     */
    PARTIAL_DELETE,
    /**
     * User deletes the record but sends it to the MDM trash.
     * @see #PHYSICAL_DELETE
     */
    LOGICAL_DELETE,
    /**
     * User deletes the record from MDM, no recovery is possible.
     * @see #LOGICAL_DELETE
     */
    PHYSICAL_DELETE
}
