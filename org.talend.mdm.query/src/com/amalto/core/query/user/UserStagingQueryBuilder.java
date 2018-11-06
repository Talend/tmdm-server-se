/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.query.user;

import com.amalto.core.query.user.metadata.*;

public class UserStagingQueryBuilder {
    /**
     * @return A {@link TypedExpression} that represents the staging area status of the record.
     * com.amalto.core.storage.task.StagingConstants
     */
    public static TypedExpression status() {
        return StagingStatus.INSTANCE;
    }

    /**
     * @return A {@link TypedExpression} that represents the staging area source of the record (this is user provided
     * value in staging area).
     */
    public static TypedExpression source() {
        return StagingSource.INSTANCE;
    }

    /**
     * @return A {@link TypedExpression} that represents the staging area last error of the record (the last exception
     * that happened during record validation).
     */
    public static TypedExpression error() {
        return StagingError.INSTANCE;
    }

    /**
     * @return A {@link TypedExpression} that represents the staging area block key.
     */
    public static TypedExpression blockKey() {
        return StagingBlockKey.INSTANCE;
    }

    /**
     * @return A {@link TypedExpression} that represents the number of similar record in group (>= 1).
     */
    public static TypedExpression groupSize() {
        return GroupSize.INSTANCE;
    }

    /**
     * @return A {@link TypedExpression} that represents the staging area task exists of the record.
     * com.amalto.core.storage.task.StagingConstants
     */
    public static TypedExpression hasTask() {
        return StagingHasTask.INSTANCE;
    }
}
