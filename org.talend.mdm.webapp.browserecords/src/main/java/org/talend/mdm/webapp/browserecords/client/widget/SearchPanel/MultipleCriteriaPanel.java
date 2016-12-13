/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.SearchPanel;

import org.talend.mdm.webapp.base.client.model.Criteria;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.OperatorConstants;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class MultipleCriteriaPanel extends SimplePanel {

    private MultipleCriteriaPanel parent;

    private VerticalPanel rightPanel;

    private Panel separationLeftPanel;

    private Panel separationRightPanel;

    private Window parentWin;

    private ComboBox<ItemBaseModel> operatorComboBox;

    private ViewBean view;

    private ListStore<ItemBaseModel> list;

    private boolean staging;

    public MultipleCriteriaPanel(MultipleCriteriaPanel parent, ViewBean view, Window win) {
        super();
        this.parent = parent;
        this.view = view;
        this.parentWin = win;
        this.add(getMainPanel());
    }

    public MultipleCriteriaPanel(MultipleCriteriaPanel parent, ViewBean view, Window win, boolean staging) {
        this(parent, view, win);
        this.staging = staging;
    }

    public MultipleCriteriaPanel(ViewBean view) {
        this(null, view, null);
    }

    private Panel getMainPanel() {
        final HorizontalPanel main = new HorizontalPanel();
        main.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        main.add(getOperatorPanel());

        separationLeftPanel = getSeparationPanel("left"); //$NON-NLS-1$
        main.add(separationLeftPanel);

        getRightPanel();
        main.add(rightPanel);

        separationRightPanel = getSeparationPanel("right"); //$NON-NLS-1$
        main.add(separationRightPanel);

        if (parent != null) {
            main.add(new Image(Icons.INSTANCE.Delete()) {

                {
                    addClickListener(new ClickListener() {

                        @Override
                        public void onClick(Widget sender) {
                            if (parent != null) {
                                parent.removeChildFilter(MultipleCriteriaPanel.this);
                            }
                            redraw();
                        }
                    });
                }
            });
        }

        // This timer is there because if we simply call redraw(), it does not work (because getOffsetHeight() doesn't
        // work, probably because panel isn't displayed yet).
        Timer timer = new Timer() {

            @Override
            public void run() {
                redraw();
            }
        };
        timer.schedule(10);

        return main;
    }

    private Panel getOperatorPanel() {
        Panel toReturn = new SimplePanel();

        operatorComboBox = new ComboBox<ItemBaseModel>();
        list = new ListStore<ItemBaseModel>();
        ItemBaseModel field = null;

        for (String curOper : OperatorConstants.groupOperators.keySet()) {
            field = new ItemBaseModel();
            field.set("name", OperatorConstants.groupOperators.get(curOper)); //$NON-NLS-1$
            field.set("value", curOper); //$NON-NLS-1$
            list.add(field);
        }
        if (list.getCount() > 0) {
            operatorComboBox.setValue(list.getAt(0));
        }

        operatorComboBox.setDisplayField("name"); //$NON-NLS-1$
        operatorComboBox.setValueField("value"); //$NON-NLS-1$
        operatorComboBox.setStore(list);
        operatorComboBox.setWidth("75px"); //$NON-NLS-1$        
        operatorComboBox.setTriggerAction(TriggerAction.ALL);
        toReturn.add(operatorComboBox);
        toReturn.setWidth("80px"); //$NON-NLS-1$
        return toReturn;
    }

    private Panel getSeparationPanel(String position) {
        Panel right = new SimplePanel();
        right.add(new Label(" ")); //$NON-NLS-1$
        right.setWidth("10px"); //$NON-NLS-1$
        right.setHeight("97%"); //$NON-NLS-1$
        right.addStyleName("separation-" + position); //$NON-NLS-1$
        return right;
    }

    private void getRightPanel() {
        rightPanel = new VerticalPanel();
        addSimpleCriterionPanel();
        rightPanel.add(getAddCriterionPanel());
    }

    private Panel getAddCriterionPanel() {
        HorizontalPanel toReturn = new HorizontalPanel();

        toReturn.setSpacing(5);

        final Image image = new Image(Icons.INSTANCE.add());

        image.addClickListener(new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                addSimpleCriterionPanel();
            }

        });

        toReturn.add(image);

        final Image button = new Image(Icons.INSTANCE.chart_organisation_add());
        button.setTitle(MessagesFactory.getMessages().advsearch_subclause());
        button.addClickListener(new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                addMultipleCriteriaPanel();
            }
        });
        toReturn.add(button);

        return toReturn;
    }

    private SimpleCriterionPanel addSimpleCriterionPanel() {
        final SimpleCriterionPanel newPanel = new SimpleCriterionPanel(MultipleCriteriaPanel.this, rightPanel, null, staging);
        newPanel.updateFields(view);
        final int index = (rightPanel.getWidgetCount() == 0 ? 0 : rightPanel.getWidgetCount() - 1);
        rightPanel.insert(newPanel, index);

        redraw();
        return newPanel;
    }

    private MultipleCriteriaPanel addMultipleCriteriaPanel() {
        final MultipleCriteriaPanel newPanel = new MultipleCriteriaPanel(MultipleCriteriaPanel.this, view, null);
        final int index = (rightPanel.getWidgetCount() == 0 ? 0 : rightPanel.getWidgetCount() - 1);
        rightPanel.insert(newPanel, index);
        redraw();
        return newPanel;
    }

    protected void redraw() {
        final int offsetHeight = rightPanel.getOffsetHeight();
        final int offsetWidth = rightPanel.getOffsetWidth();
        if (separationLeftPanel != null) {
            separationLeftPanel.setHeight(offsetHeight + "px"); //$NON-NLS-1$
        }
        if (separationRightPanel != null) {
            separationRightPanel.setHeight(offsetHeight + "px"); //$NON-NLS-1$   
        }

        if (this.parentWin != null) {
            if (offsetWidth > 0) {
                if (offsetWidth > 600) {
                    this.parentWin.setWidth("800px"); //$NON-NLS-1$
                } else {
                    this.parentWin.setWidth(offsetWidth + 160 + "px"); //$NON-NLS-1$
                }
                this.parentWin.center();
            }
            if (this.parentWin.getOffsetHeight() > 600) {
                this.parentWin.setHeight(600);
            }

        }

        if (parent != null) {
            parent.redraw();
        }
    }

    protected void removeChildFilter(MultipleCriteriaPanel toRemove) {
        rightPanel.remove(toRemove);
    }

    public MultipleCriteria getCriteria() {
        MultipleCriteria toReturn = new MultipleCriteria(operatorComboBox.getValue() == null ? "" : operatorComboBox.getValue() //$NON-NLS-1$
                .get("value").toString()); //$NON-NLS-1$

        for (int i = 0; i < rightPanel.getWidgetCount(); i++) {
            Widget widget = rightPanel.getWidget(i);
            if (widget instanceof SimpleCriterionPanel) {
                SimpleCriterionPanel criterionPanel = (SimpleCriterionPanel) widget;
                if (criterionPanel.getCriteria() != null) {
                    toReturn.add(criterionPanel.getCriteria());
                }
            } else if (widget instanceof MultipleCriteriaPanel) {
                MultipleCriteriaPanel filterPanel = (MultipleCriteriaPanel) widget;
                toReturn.add(filterPanel.getCriteria());
            }
        }

        return toReturn;
    }

    @Override
    public void clear() {
        for (int i = 0; i < rightPanel.getWidgetCount(); i++) {
            Widget widget = rightPanel.getWidget(i);
            if (widget instanceof SimpleCriterionPanel) {
                rightPanel.remove(widget);
            } else if (widget instanceof MultipleCriteriaPanel) {
                rightPanel.remove(widget);
            }
        }
    }

    public void setCriteria(Criteria criteria) {
        clear();
        if (criteria instanceof MultipleCriteria) {
            MultipleCriteria multipleCriteria = (MultipleCriteria) criteria;

            operatorComboBox.setValue(list.findModel("value", multipleCriteria.getOperator())); //$NON-NLS-1$
            for (Criteria current : multipleCriteria.getChildren()) {
                if (current instanceof SimpleCriterion) {
                    SimpleCriterionPanel newPanel = addSimpleCriterionPanel();
                    newPanel.setCriterion((SimpleCriterion) current);
                } else if (current instanceof MultipleCriteria) {
                    MultipleCriteriaPanel newPanel = addMultipleCriteriaPanel();
                    newPanel.setCriteria(current);
                }
            }
        } else if (criteria instanceof SimpleCriterion) {
            SimpleCriterionPanel newPanel = addSimpleCriterionPanel();
            newPanel.setCriterion((SimpleCriterion) criteria);
        }
    }
}
