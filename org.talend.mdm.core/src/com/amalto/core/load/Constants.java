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

package com.amalto.core.load;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 */
public interface Constants {
    Attributes EMPTY_ATTRIBUTES = new AttributesImpl();
    String PAYLOAD_ELEMENT = "p"; //NON-NLS
    String WRAPPER_ELEMENT = "ii";
    String CONTAINER_ELEMENT = "c"; //NON-NLS
    String NAME_ELEMENT = "n"; //NON-NLS
    String DMN_ELEMENT = "dmn"; //NON-NLS
    String DMR_ELEMENT = "dmr"; //NON-NLS
    String SP_ELEMENT = "sp"; //NON-NLS
    String TIMESTAMP_ELEMENT = "t";
    String TASK_ID_ELEMENT = "taskId"; //NON-NLS
    String ID_ELEMENT = "i"; //NON-NLS
    boolean NON_FINAL_STATE = false;
    boolean FINAL_STATE = true;
    int DEFAULT_PARSER_LIMIT = -1; // Means no limit
}
