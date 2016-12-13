/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield;

import org.talend.mdm.webapp.base.client.widget.ComboBoxEx;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ResizeEvent;
import com.extjs.gxt.ui.client.event.ResizeListener;
import com.extjs.gxt.ui.client.fx.Resizable;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

public class ComboBoxField<D extends ModelData> extends ComboBoxEx<D> {

    public ComboBoxField() {
        setUserProperties(BrowseRecords.getSession().getAppHeader().getUserProperties());
    }

    class Resize extends Resizable {

        public Resize(BoxComponent resize) {
            super(resize);
        }

        public native BoxComponent getBoxComponent() /*-{
			return this.@com.extjs.gxt.ui.client.fx.Resizable::resize;
        }-*/;

    }

    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        LayoutContainer list = (LayoutContainer) this.getListView().getParent();
        list.setLayout(new FitLayout());
        final Resize resizable = new Resize(list);

        resizable.addResizeListener(new ResizeListener() {

            public void resizeEnd(ResizeEvent re) {
                setMinListWidth(resizable.getBoxComponent().getWidth());
            }
        });
    }

    @Override
    public void disable() {
        super.disable();
        setEditable(false);
        if (input != null) {
            input.dom.setAttribute("contenteditable", "false"); //$NON-NLS-1$//$NON-NLS-2$
            input.dom.removeAttribute("tabIndex"); //$NON-NLS-1$
        }
    }

    @Override
    public void enable() {
        super.enable();
        setEditable(true);
        if (input != null) {
            input.dom.setAttribute("contenteditable", "true"); //$NON-NLS-1$//$NON-NLS-2$
            input.dom.removeAttribute("tabIndex"); //$NON-NLS-1$
        }
    }

    @Override
    public void onDisable() {
        addStyleName(disabledStyle);
    }
}
