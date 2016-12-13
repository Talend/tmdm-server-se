/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.model;

import com.amalto.core.storage.task.StagingConstants;
import org.talend.mdm.webapp.browserecords.client.resources.Resources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.IsSerializable;

public class RecordStatusWrapper implements IsSerializable {

    private RecordStatus status;

    private boolean isValid;

    private ImageResource icon;

    private String color;

    public RecordStatusWrapper(RecordStatus status) {
        super();
        this.status = status;
        checkStatus(status);
    }

    private void checkStatus(RecordStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Record status can be empty!"); //$NON-NLS-1$
        }
        int statusCode = status.getStatusCode();
        if (statusCode == Integer.parseInt(StagingConstants.NEW)) {
            isValid = false;
            icon = null;
            color = null;
        } else if (statusCode == Integer.parseInt(StagingConstants.DELETED)
                || statusCode == Integer.parseInt(StagingConstants.FAIL_DELETE_CONSTRAINTS)) {
            isValid = true;
            icon = Resources.ICONS.statusDeleted();
            color = "red"; //$NON-NLS-1$ 
        } else if (statusCode >= 200 && statusCode < 300) {
            isValid = true;
            icon = Resources.ICONS.statusValid();
            color = "green"; //$NON-NLS-1$
        } else if (statusCode >= 400) {
            isValid = false;
            icon = Resources.ICONS.statusInvalid();
            color = "red"; //$NON-NLS-1$
        } else {
            isValid = false;
            icon = Resources.ICONS.statusUnknown();
            color = null;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public ImageResource getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public RecordStatus getStatus() {
        return status;
    }

}
