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

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;

public class ItemsPagingToolBar extends PagingToolBar {

    private El inputEl;

    private Label pageSpace1;

    private Label pageSpace2;

    private Button addButton;

    private ToggleButton pageButton1;

    private ToggleButton pageButton2;

    private ToggleButton pageButton3;

    private ToggleButton pageButton4;

    private ToggleButton pageButton5;

    private List<ToggleButton> pageButtonList;

    private LabelToolItem sizeLabel;

    private NumberField sizeField;

    private boolean pageSpace1Visible;

    private boolean pageSpace2Visible;

    private int pageToggleButtonIndex;

    public ItemsPagingToolBar(int pageSize) {
        super(pageSize);
        this.pageSize = pageSize;
        initPagingToolBar();
        paintPagingToolBar();
    }

    private void initPagingToolBar() {

        pageSpace1 = new Label("..."); //$NON-NLS-1$

        pageSpace2 = new Label("..."); //$NON-NLS-1$

        addButton = new Button("add"); //$NON-NLS-1$

        pageButtonList = new ArrayList<ToggleButton>();
        pageButton1 = new ToggleButton("1"); //$NON-NLS-1$
        pageButton1.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                refreshPageButton(Integer.valueOf(pageButton1.getText()));
            }
        });
        pageButtonList.add(pageButton1);
        pageButton2 = new ToggleButton("2"); //$NON-NLS-1$
        pageButton2.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                refreshPageButton(Integer.valueOf(pageButton2.getText()));
            }
        });
        pageButtonList.add(pageButton2);
        pageButton3 = new ToggleButton("3"); //$NON-NLS-1$
        pageButton3.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                refreshPageButton(Integer.valueOf(pageButton3.getText()));
            }
        });
        pageButtonList.add(pageButton3);
        pageButton4 = new ToggleButton("4"); //$NON-NLS-1$
        pageButton4.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                refreshPageButton(Integer.valueOf(pageButton4.getText()));
            }
        });
        pageButtonList.add(pageButton4);
        pageButton5 = new ToggleButton("5"); //$NON-NLS-1$
        pageButton5.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                refreshPageButton(Integer.valueOf(pageButton5.getText()));
            }
        });
        pageButtonList.add(pageButton5);

        sizeLabel = new LabelToolItem(MessagesFactory.getMessages().page_size_label());

        sizeField = new NumberField() {

            @Override
            protected void onRender(Element target, int index) {
                super.onRender(target, index);
                inputEl = this.input;
            }
        };
        sizeField.setWidth(30);
        sizeField.setValue(pageSize);
        sizeField.setValidator(validator);
        sizeField.addListener(Events.Change, new Listener<BaseEvent>() {

            public void handleEvent(BaseEvent be) {
                if (sizeField.isValid() && sizeField.getValue() != null) {
                    setPageSize((int) Double.parseDouble(sizeField.getValue() + ""));//$NON-NLS-1$
                    first();
                }
            }
        });
        sizeField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent fe) {
                if (fe.getKeyCode() == KeyCodes.KEY_ENTER) {
                    blur(inputEl.dom);
                }
            }
        });
        sizeField.setValue(pageSize);
    }

    private native void blur(Element el)/*-{
        el.blur();
    }-*/;

    Validator validator = new Validator() {

        public String validate(Field<?> field, String value) {
            String valueStr = value == null ? "" : value.toString();//$NON-NLS-1$
            boolean success = true;
            try {
                int num = Integer.parseInt(valueStr);
                if (num <= 0) {
                    success = false;
                }
            } catch (NumberFormatException e) {
                success = false;
            }
            if (!success) {
                return MessagesFactory.getMessages().page_size_notice();
            }
            return null;
        }
    };

    public void paintPagingToolBar() {
        this.removeAll();
        this.insert(addButton, 0);
        this.insert(pageButton1, 1);
        this.insert(pageSpace1, 2);
        this.insert(pageButton2, 3);
        this.insert(pageButton3, 4);
        this.insert(pageButton4, 5);
        this.insert(pageSpace2, 6);
        this.insert(pageButton5, 7);
        this.insert(new SeparatorToolItem(), 8);
        this.insert(sizeLabel, 9);
        this.insert(sizeField, 10);
        refreshPageButton(2);
    }

    private void refreshPageButton(int pageCount) {
        this.pages = 7;
        pageButtonList.get(0).toggle(false);
        pageButtonList.get(1).toggle(false);
        pageButtonList.get(2).toggle(false);
        pageButtonList.get(3).toggle(false);
        pageButtonList.get(4).toggle(false);
        pageSpace1Visible = (pageCount < 4 ? false : true);
        pageSpace2Visible = (pageCount > this.pages - 3 ? false : true);

        if (this.pages > 0 && this.pages <= 5) {
            for (int i = 5; i > this.pages; i--) {
                pageButtonList.get(i).setVisible(false);
            }
            pageButtonList.get(2).toggle(true);
        } else {
            // 1 2 3 4 5 6 7

            if (pageCount < 3) {
                pageToggleButtonIndex = pageCount - 1;
                pageButtonList.get(1).setText("2"); //$NON-NLS-1$
                pageButtonList.get(2).setText("3"); //$NON-NLS-1$
                pageButtonList.get(3).setText("4"); //$NON-NLS-1$
            } else if (pageCount > this.pages - 2) {
                pageToggleButtonIndex = 4 + pageCount - this.pages;
                pageButtonList.get(1).setText(String.valueOf(this.pages - 3));
                pageButtonList.get(2).setText(String.valueOf(this.pages - 2));
                pageButtonList.get(3).setText(String.valueOf(this.pages - 1));
            } else {
                pageToggleButtonIndex = 2;
                pageButtonList.get(1).setText(String.valueOf(Integer.valueOf(pageCount) - 1));
                pageButtonList.get(2).setText(String.valueOf(pageCount));
                pageButtonList.get(3).setText(String.valueOf(Integer.valueOf(pageCount) + 1));
            }
        }
        pageSpace1.setVisible(pageSpace1Visible);
        pageSpace2.setVisible(pageSpace2Visible);
        pageButtonList.get(4).setText(String.valueOf(this.pages));
        pageButtonList.get(pageToggleButtonIndex).toggle(true);
    }
}
