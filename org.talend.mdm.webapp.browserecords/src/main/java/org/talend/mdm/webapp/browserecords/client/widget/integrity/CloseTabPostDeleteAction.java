/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.webapp.browserecords.client.widget.integrity;

import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;

/**
 * Close a record tab once delete is completed.
 */
public class CloseTabPostDeleteAction implements PostDeleteAction {

    private final PostDeleteAction next;

    private final ItemDetailToolBar bar;

    /**
     * @param bar The render toolbar(record could be opened from many places)
     * @param next If you don't know what to pass as <code>next</code> argument, check the
     * constant {@link NoOpPostDeleteAction#INSTANCE}.
     */
    public CloseTabPostDeleteAction(ItemDetailToolBar bar, PostDeleteAction next) {
        if (bar == null) {
            throw new IllegalArgumentException("Bar argument cannot be null"); //$NON-NLS-1$
        }
        this.bar = bar;
        this.next = next;
    }

    public void doAction() {
        if (bar.isOutMost()) {
            bar.closeOutTabPanel();
            if (bar.isStaging()) {
                selectStagingRecord();
            } else {
                selectBrowseRecord();
            }
            doSearch4SearchEntityPanel();
        } else if (bar.isFkToolBar()) {
            bar.closeCurrentTabPanel();// TMDM-3556, it need to close current tab when delete FK
        }
        next.doAction();
    }
    
    private native void selectBrowseRecord()/*-{
        var tabPanel = $wnd.amalto.core.getTabPanel();
        var panel = tabPanel.getItem("Browse Records");         
        if (panel != undefined){
            tabPanel.setSelection(panel.getItemId());
        }
    }-*/;
    
    private native void selectStagingRecord()/*-{
        var tabPanel = $wnd.amalto.core.getTabPanel();
        var panel = tabPanel.getItem("BrowseRecordsInStaging");         
        if (panel != undefined){
            tabPanel.setSelection(panel.getItemId());
        }
    }-*/;
    
    private native void doSearch4SearchEntityPanel()/*-{
        var tabPanel = $wnd.amalto.core.getTabPanel();
        var searchEntityPanel = tabPanel.getItem("searchEntityPanel");
        if (searchEntityPanel != undefined){
            searchEntityPanel.doSearchList();
        }
    }-*/;
}
