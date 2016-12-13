/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.webapp.browserecords.client.resources.icon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface Icons extends ClientBundle {

    Icons INSTANCE = GWT.create(Icons.class);

    @Source("link_edit.png")
    ImageResource link();

    @Source("funnel.png")
    ImageResource funnel();

    @Source("edit.png")
    ImageResource Edit();

    @Source("valid.png")
    ImageResource Valid();

    @Source("create.png")
    ImageResource add();

    @Source("chart_organisation_add.png")
    ImageResource chart_organisation_add();

    @Source("create.png")
    ImageResource Create();

    @Source("delete.png")
    ImageResource Delete();

    @Source("send_to_trash.png")
    ImageResource Send_to_trash();

    @Source("save.png")
    ImageResource Save();
    
    @Source("bulkupdate.png")
    ImageResource BulkUpdate();

    @Source("display.png")
    ImageResource Display();

    @Source("drop-add.gif")
    ImageResource drop_add();

    @Source("drop-no.gif")
    ImageResource drop_no();

    @Source("add-element.gif")
    ImageResource add_element();

    @Source("image_add.png")
    ImageResource image_add();

    @Source("clear-icon.gif")
    ImageResource clear_icon();

    @Source("dosearch.png")
    ImageResource dosearch();

    @Source("journal.png")
    ImageResource journal();
    
    @Source("journal.png")
    ImageResource navigator();

    @Source("browse.png")
    ImageResource masterRecords();

    @Source("browsestaging.png")
    ImageResource stagingRecords();

    @Source("duplicate.png")
    ImageResource duplicate();

    @Source("refreshToolbar.png")
    ImageResource refreshToolbar();

    @Source("link_go.png")
    ImageResource link_go();

    @Source("link_add.png")
    ImageResource link_add();

    @Source("link_delete.png")
    ImageResource link_delete();

    @Source("save_and_close.png")
    ImageResource save_and_close();

    @Source("launch_process.png")
    ImageResource launch_process();

    @Source("relations.png")
    ImageResource relations();

    @Source("opentab.png")
    ImageResource openTab();

    @Source("opentask.png")
    ImageResource openTask();

    @Source("status_valid.png")
    ImageResource statusValid();

    @Source("status_invalid.png")
    ImageResource statusInvalid();

    @Source("status_unknown.png")
    ImageResource statusUnknown();

    @Source("status_deleted.png")
    ImageResource statusDeleted();

    @Source("leaf.gif")
    ImageResource leaf();

    @Source("validate_record.png")
    ImageResource validateRecord();
}
