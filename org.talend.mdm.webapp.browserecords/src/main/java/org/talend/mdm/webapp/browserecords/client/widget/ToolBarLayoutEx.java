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


import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.SearchPanel.SimpleCriterionPanel;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.ComboBoxField;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonGroup;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.ToolBarLayout;
import com.extjs.gxt.ui.client.widget.menu.HeaderMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.user.client.Element;

/**
 * Extend ToolBarLayout function, support comboBox component to shrink displaying.
 */
public class ToolBarLayoutEx extends ToolBarLayout {

    private SimpleCriterionPanel<?> simplePanelClone;

    protected void initMore() {
        if (more == null) {
            moreMenu = new MenuEx();
            moreMenu.addListener(Events.BeforeShow, new Listener<MenuEvent>() {

                public void handleEvent(MenuEvent be) {
                    clearMenu();
                    for (Component c : container.getItems()) {
                        if (isHidden(c)) {
                            addComponentToMenu(be.getContainer(), c);
                        }
                    }
                    if (be.getContainer().getItemCount() == 0) {
                        be.getContainer().add(new HeaderMenuItem(getNoItemsMenuText()));
                    }
                }

            });

            more = new Button();
            more.addStyleName("x-toolbar-more"); //$NON-NLS-1$
            more.setIcon(GXT.IMAGES.toolbar_more());
            more.setMenu(moreMenu);

        }
        Element td = insertCell(more, getExtrasTr(), 100);
        if (more.isRendered()) {
            td.appendChild(more.el().dom);
        } else {
            more.render(td);
        }
        ComponentHelper.doAttach(more);

        moreMenu.setWidth(230);
    }

    private native El getExtrasTr()/*-{
        return this.@com.extjs.gxt.ui.client.widget.layout.ToolBarLayout::extrasTr;
    }-*/;

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
        } else if (c instanceof ComboBox<?>) {
            final ComboBoxField<ItemBaseModel> cb = (ComboBoxField<ItemBaseModel>) c;
            ComboBoxField<ItemBaseModel> comboBoxClone = new ComboBoxField<ItemBaseModel>();
            comboBoxClone.setStore(cb.getStore());
            comboBoxClone.setDisplayField("name");//$NON-NLS-1$
            comboBoxClone.setValueField("value");//$NON-NLS-1$
            comboBoxClone.setTypeAhead(true);
            comboBoxClone.setTriggerAction(TriggerAction.ALL);
            comboBoxClone.setForceSelection(true);
            comboBoxClone.setEmptyText(MessagesFactory.getMessages().empty_entity());
            comboBoxClone.setLoadingText(MessagesFactory.getMessages().loading());
            comboBoxClone.setStyleAttribute("padding-right", "17px"); //$NON-NLS-1$ //$NON-NLS-2$

            if (cb.getValue() != null)
                comboBoxClone.setValue(cb.getValue());
            comboBoxClone.addSelectionChangedListener(new SelectionChangedListener<ItemBaseModel>() {

                @Override
                public void selectionChanged(SelectionChangedEvent<ItemBaseModel> se) {
                    cb.setValue(se.getSelectedItem());
                    BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry
                            .get(BrowseRecords.BROWSERECORDS_SERVICE);
                    service.getView(se.getSelectedItem().get("value").toString(), Locale.getLanguage(), //$NON-NLS-1$
                            new SessionAwareAsyncCallback<ViewBean>() {

                                public void onSuccess(ViewBean viewbean) {
                                    simplePanelClone.updateFields(viewbean);
                                    for (Component c : moreMenu.getItems()) {
                                        c.setEnabled(true);
                                    }
                                }
                            });

                }
            });
            comboBoxClone.setId("MenuItem_" + cb.getId()); //$NON-NLS-1$
            menu.add(comboBoxClone);
        } else if (c instanceof HorizontalPanel) {
            SimpleCriterionPanel<?> simplePanel = (SimpleCriterionPanel<?>) c;
            simplePanelClone = simplePanel.clonePanel();
            menu.add(simplePanelClone);
        } else if (c instanceof Button) {
            final Button b = (Button) c;
            String menuText = b.getText();
            if ((menuText == null || menuText.trim().length() == 0) && b.getToolTip() != null) {
                menuText = b.getToolTip().getToolTipConfig() == null ? "" : b.getToolTip().getToolTipConfig().getText(); //$NON-NLS-1$
            }
            MenuItem item = new MenuItem(menuText, b.getIcon());
            if (b.getToolTip() != null)
                item.setToolTip(b.getToolTip().getToolTipConfig());
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

    class MenuEx extends Menu {

        public MenuEx() {
            super();
            monitorWindowResize = false;
        }
    }
}
