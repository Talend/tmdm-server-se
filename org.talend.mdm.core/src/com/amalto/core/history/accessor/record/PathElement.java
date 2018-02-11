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

package com.amalto.core.history.accessor.record;

import org.talend.mdm.commmon.metadata.FieldMetadata;

class PathElement {
    FieldMetadata field = null;
    int index = -1; // 0-based index
    Setter setter;
    Getter getter;
}
