/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget;

import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

public class SaveRowEditor extends RowEditor<ItemBean> {

    BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    @Override
    protected void onRowClick(GridEvent<ItemBean> e) {
        // cancel click Editor
    }

    @Override
    public void startEditing(int rowIndex, boolean doFocus) {
        super.startEditing(rowIndex, doFocus);
        grid.getSelectionModel().setLocked(true);
    }

    @Override
    public void stopEditing(boolean saveChanges) {
        super.stopEditing(saveChanges);
        grid.getSelectionModel().setLocked(false);
    }

    public void createElements(String xpath, String elName, List<?> value, Map<String, Element> elementSet, Document doc) {
        Element parent = null;
        String[] xps = xpath.split("/");//$NON-NLS-1$
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        for (String xp : xps) {
            if (isFirst) {
                sb.append(xp);
                isFirst = false;
            } else {
                sb.append("/" + xp);//$NON-NLS-1$
            }
            Element tempEl = elementSet.get(sb.toString());
            if (tempEl == null) {
                tempEl = (Element) doc.createElement(xp);
                elementSet.put(sb.toString(), tempEl);
            }
            if (parent != null) {
                parent.appendChild(tempEl);
            }
            parent = tempEl;
        }
        for (Object o : value) {
            Element el = doc.createElement(elName);
            el.appendChild(doc.createTextNode(o.toString()));
            parent.appendChild(el);
        }
    }
}
