/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.mdm.webapp.recyclebin.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NoPermissionException extends RuntimeException implements IsSerializable {

    private static final long serialVersionUID = 7618477708692572305L;

    public NoPermissionException() {
        super("User does not have permission on this record."); //$NON-NLS-1$
    }

    public NoPermissionException(Throwable cause) {
        super("User does not have permission on this record.", cause); //$NON-NLS-1$
    }

}
