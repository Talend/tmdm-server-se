/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.widget;

import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;
import org.talend.mdm.webapp.base.shared.Constants;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonGroup;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.ToolBarLayout;
import com.extjs.gxt.ui.client.widget.menu.HeaderMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

public class PagingToolBarEx extends PagingToolBar {

    public static String BROWSERECORD_PAGESIZE = "browseRecord_pagesize"; //$NON-NLS-1$

    public static String BROWSERECORD_FOREIGNKEY_PAGESIZE = "browseRecord_foreignkey_pagesize"; //$NON-NLS-1$

    private El inputEl;

    NumberField sizeField;

    boolean isFireKeyEnter;

    boolean isBrowseRecordsGridCall;

    private String name;

    public PagingToolBarEx(int pageSize) {
        super(pageSize);
        if (!validatePageSize(pageSize)) {
            setPageSize(Constants.PAGE_SIZE);
        }
        setLayout(new PagingToolBarExLayout());
        LabelToolItem sizeLabel = new LabelToolItem(BaseMessagesFactory.getMessages().page_size_label());

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
        sizeField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent fe) {
                if (fe.getKeyCode() == KeyCodes.KEY_ENTER) {
                    isFireKeyEnter = true;
                    blur(inputEl.dom);
                    if (!sizeField.isFireChangeEventOnSetValue()) {
                        if (isBrowseRecordsGridCall && sizeField.isValid()) {
                            Cookies.setCookie(name != null ? name : BROWSERECORD_PAGESIZE,
                                    String.valueOf(sizeField.getValue().intValue()));
                        }
                        refreshData();
                    }
                }
            }
        });

        Grid grid = new Grid(1, 2);
        grid.setWidget(0, 0, sizeLabel);
        grid.setWidget(0, 1, sizeField);

        WidgetComponent sizeComp = new WidgetComponent(grid);

        this.insert(new SeparatorToolItem(), this.getItemCount() - 2);
        this.insert(sizeComp, this.getItemCount() - 2);
        removeButtonToolTip();
    }

    public PagingToolBarEx(int pageSize, String name) {
        this(pageSize);
        this.name = name;
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        this.layout(true);
    }

    private native void blur(Element el)/*-{
                                        el.blur();
                                        }-*/;

    private Validator validator = new Validator() {

        @Override
        public String validate(Field<?> field, String value) {
            String valueStr = value == null ? "" : value.toString(); //$NON-NLS-1$
            boolean success = false;
            try {
                success = validatePageSize(Integer.parseInt(valueStr));
            } catch (NumberFormatException e) {
                success = false;
            }
            if (!success) {
                return BaseMessagesFactory.getMessages().page_size_notice();
            }
            return null;
        }
    };

    private boolean validatePageSize(int pageSizeNumber) {
        if (pageSizeNumber <= 0) {
            return false;
        } else {
            return true;
        }
    }

    public void lastAfterCreate() {
        if (totalLength == 0) {
            refresh();
            return;
        }
        int extra = totalLength % pageSize;
        if (extra == 0) {
            pages++;
            setActivePage(pages);
        } else {
            last();
        }
    }

    private void refreshData() {
        if (sizeField.isValid() && sizeField.getValue() != null) {
            setPageSize((int) Double.parseDouble(sizeField.getValue() + "")); //$NON-NLS-1$
            first();
        }
    }

    public void setBrowseRecordsGridCall(boolean isBrowseRecordsGridCall) {
        this.isBrowseRecordsGridCall = isBrowseRecordsGridCall;
    }

    private void removeButtonToolTip() {
        this.first.removeToolTip();
        this.prev.removeToolTip();
        this.next.removeToolTip();
        this.last.removeToolTip();
        this.refresh.removeToolTip();
    }

    class PagingToolBarExLayout extends ToolBarLayout {

        private El tempInputEl;

        @Override
        protected void initMore() {
            super.initMore();
            moreMenu.setWidth(250);
        }

        @Override
        protected void addComponentToMenu(Menu menu, Component c) {
            if (c instanceof SeparatorToolItem) {
                menu.add(new SeparatorMenuItem());
            } else if (c instanceof SplitButton) {
                final SplitButton sb = (SplitButton) c;
                MenuItem item = new MenuItem(sb.getText(), sb.getIcon());
                item.setEnabled(c.isEnabled());
                item.setItemId(c.getItemId());
                if (sb.getMenu() != null) {
                    item.setSubMenu(sb.getMenu());
                }
                item.addSelectionListener(new SelectionListener<MenuEvent>() {

                    @Override
                    public void componentSelected(MenuEvent ce) {
                        ButtonEvent e = new ButtonEvent(sb);
                        e.setEvent(ce.getEvent());
                        sb.fireEvent(Events.Select, e);
                    }

                });
                item.setId("MenuItem_" + sb.getId()); //$NON-NLS-1$
                menu.add(item);
            } else if (c instanceof LabelToolItem) {
                LabelToolItem l = (LabelToolItem) c;
                MenuItem item = new MenuItem(l.getLabel());
                menu.add(item);
            } else if (c instanceof WidgetComponent) {
                WidgetComponent wc = (WidgetComponent) c;
                Widget wg = wc.getWidget();
                if (wg instanceof Grid) {
                    final NumberField sizeF = new NumberField() {

                        @Override
                        protected void onRender(Element target, int index) {
                            super.onRender(target, index);
                            tempInputEl = this.input;
                        }
                    };
                    sizeF.setId("BrowseRecords_PageSize"); //$NON-NLS-1$

                    sizeF.setWidth(30);
                    sizeF.setValidator(validator);
                    sizeF.addListener(Events.Change, new Listener<BaseEvent>() {

                        @Override
                        public void handleEvent(BaseEvent be) {
                            sizeField.setValue((int) Double.parseDouble(sizeF.getValue() + "")); //$NON-NLS-1$
                            if (!sizeField.isFireChangeEventOnSetValue()) {
                                sizeField.fireEvent(Events.Change);
                            }
                            moreMenu.hide();
                        }
                    });

                    sizeF.addListener(Events.KeyDown, new Listener<FieldEvent>() {

                        @Override
                        public void handleEvent(FieldEvent fe) {
                            if (fe.getKeyCode() == KeyCodes.KEY_ENTER) {
                                blur(tempInputEl.dom);
                            }
                        }
                    });

                    sizeF.setValue((int) (Double.parseDouble(sizeField.getValue() + ""))); //$NON-NLS-1$
                    Grid sizeGrid = new Grid(1, 2);
                    sizeGrid.setWidget(0, 0, new LabelToolItem(BaseMessagesFactory.getMessages().page_size_label()));
                    sizeGrid.setWidget(0, 1, sizeF);
                    sizeGrid.getElement().getStyle().setMarginLeft(24D, Unit.PX);
                    WidgetComponent wcc = new WidgetComponent(sizeGrid);
                    wcc.setStyleName("x-menu-item"); //$NON-NLS-1$
                    menu.add(wcc);
                }
            } else if (c instanceof Button) {
                final Button b = (Button) c;
                String menuText = b.getText();
                ToolTip tt = b.getToolTip();
                if (menuText == null || menuText.trim().length() == 0) {
                    if (tt != null) {
                        menuText = tt.getToolTipConfig() == null ? "" : tt.getToolTipConfig().getText(); //$NON-NLS-1$
                    } else {
                        menuText = ""; //$NON-NLS-1$
                    }
                }
                MenuItem item = new MenuItem(menuText, b.getIcon());
                if (tt != null) {
                    item.setToolTip(b.getToolTip().getToolTipConfig());
                }
                item.setItemId(c.getItemId());
                if (b.getMenu() != null) {
                    item.setHideOnClick(false);
                    item.setSubMenu(b.getMenu());
                }
                item.setEnabled(c.isEnabled());
                item.addSelectionListener(new SelectionListener<MenuEvent>() {

                    @Override
                    public void componentSelected(MenuEvent ce) {
                        ButtonEvent e = new ButtonEvent(b);
                        e.setEvent(ce.getEvent());
                        b.fireEvent(Events.Select, e);
                    }

                });
                item.setId("MenuItem_" + b.getId()); //$NON-NLS-1$
                menu.add(item);
            } else if (c instanceof ButtonGroup) {
                ButtonGroup g = (ButtonGroup) c;
                g.setItemId(c.getItemId());
                menu.add(new SeparatorMenuItem());
                String heading = g.getHeading();
                if (heading != null && heading.length() > 0 && !heading.equals("&#160;")) { //$NON-NLS-1$
                    menu.add(new HeaderMenuItem(g.getHeading()));
                }
                for (Component c2 : g.getItems()) {
                    addComponentToMenu(menu, c2);
                }
                menu.add(new SeparatorMenuItem());
            }

            if (menu.getItemCount() > 0) {
                if (menu.getItem(0) instanceof SeparatorMenuItem) {
                    menu.remove(menu.getItem(0));
                }
                if (menu.getItemCount() > 0) {
                    if (menu.getItem(menu.getItemCount() - 1) instanceof SeparatorMenuItem) {
                        menu.remove(menu.getItem(menu.getItemCount() - 1));
                    }
                }
            }
        }
    }
}
