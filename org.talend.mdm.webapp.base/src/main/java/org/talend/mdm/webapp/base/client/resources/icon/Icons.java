/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.webapp.base.client.resources.icon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface Icons extends ClientBundle {

    Icons INSTANCE = GWT.create(Icons.class);

    @Source("create.png")
    ImageResource Create();

    @Source("delete.png")
    ImageResource Delete();
    
    @Source("edit.png")
    ImageResource Edit();

    @Source("world_edit.png")
    ImageResource world_edit();
    
    @Source("status_valid.png")
    ImageResource statusValid();
    
    @Source("status_invalid.png")
    ImageResource statusInvalid();
}
