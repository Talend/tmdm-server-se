/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.webapp.browserecordsinstaging.client.resources.icon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;

public interface Icons extends org.talend.mdm.webapp.browserecords.client.resources.icon.Icons {

    Icons INSTANCE = GWT.create(Icons.class);

    @Source("status_valid.png")
    ImageResource statusValid();

    @Source("status_invalid.png")
    ImageResource statusInvalid();

    @Source("status_unknown.png")
    ImageResource statusUnknown();

    @Source("status_deleted.png")
    ImageResource statusDeleted();
}
