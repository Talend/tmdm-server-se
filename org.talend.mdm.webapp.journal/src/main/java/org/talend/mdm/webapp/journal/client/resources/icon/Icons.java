/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.resources.icon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public interface Icons extends ClientBundle {

    Icons INSTANCE = GWT.create(Icons.class);

    @Source("browse.png")
    ImageResource browse();

    @Source("leaf.gif")
    ImageResource leaf();

    @Source("restore.png")
    ImageResource restore();

    @Source("view.png")
    ImageResource view();

    @Source("prev.png")
    ImageResource prev();

    @Source("next.png")
    ImageResource next();

    @Source("up.png")
    ImageResource up();

    @Source("down.png")
    ImageResource down();
}
