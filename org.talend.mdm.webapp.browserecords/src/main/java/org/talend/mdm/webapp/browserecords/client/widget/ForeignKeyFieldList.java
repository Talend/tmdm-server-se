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

import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeySelector;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class ForeignKeyFieldList extends ContentPanel {

    public ItemNodeModel itemNode;

    protected TypeModel typeModel;

    protected int pageStartIndex;

    protected int pageSize;

    protected int pageNumber;

    protected int pageCount;

    protected int pageToggleButtonIndex;

    protected List<ForeignKeyBean> foreignKeyBeans;

    protected VerticalPanel verticalPanel;

    protected List<Field<?>> fields;

    protected HorizontalPanel pagingBar;

    protected Label pageSpace1;

    protected Label pageSpace2;

    protected HorizontalPanel separatorLeft;

    protected HorizontalPanel separatorRight;

    protected Button addButton;

    protected List<ToggleButton> pageButtonList;

    protected ToggleButton pageButton1;

    protected ToggleButton pageButton2;

    protected ToggleButton pageButton3;

    protected ToggleButton pageButton4;

    protected ToggleButton pageButton5;

    protected boolean pageSpace1Visible;

    protected boolean pageSpace2Visible;

    protected LabelToolItem sizeLabel;

    protected El inputEl;

    protected NumberField sizeField;

    private ItemsDetailPanel itemsDetailPanel;

    public ForeignKeyFieldList(ItemNodeModel itemNode, TypeModel typeModel, ItemsDetailPanel itemsDetailPanel) {
        this.itemNode = itemNode;
        this.typeModel = typeModel;
        this.itemsDetailPanel = itemsDetailPanel;
        pageSize = 5;
        pageNumber = 1;
        fields = new ArrayList<Field<?>>();
        pagingBar = new HorizontalPanel();
        this.initPagingBar();
        this.setHeaderVisible(false);
        this.createWidget();
        this.setWidth(350);
    }

    /**
     * Adds a field (pre-render).
     * 
     * @param field the field to add
     */
    public void add(Field<?> field) {
        fields.add(field);
    }

    /**
     * Returns the field at the index.
     * 
     * @param index the index
     * @return the field
     */
    public Field<?> getField(int index) {
        return fields.get(index);
    }

    /**
     * Returns all the child field's.
     * 
     * @return the fields
     */
    public List<Field<?>> getFieldList() {
        return fields;
    }

    private void initPagingBar() {
        addButton = new Button();
        addButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.add()));
        addButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                addField();
            }
        });

        pageSpace1 = new Label("..."); //$NON-NLS-1$

        pageSpace2 = new Label("..."); //$NON-NLS-1$

        separatorLeft = new HorizontalPanel();
        separatorLeft.setWidth(5);
        separatorRight = new HorizontalPanel();
        separatorRight.setWidth(5);

        pageButtonList = new ArrayList<ToggleButton>();
        pageButton1 = new ToggleButton("1"); //$NON-NLS-1$
        pageButton1.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                refreshPage(Integer.valueOf(pageButton1.getText()));
            }
        });
        pageButtonList.add(pageButton1);
        pageButton2 = new ToggleButton("2"); //$NON-NLS-1$
        pageButton2.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                refreshPage(Integer.valueOf(pageButton2.getText()));
            }
        });
        pageButtonList.add(pageButton2);
        pageButton3 = new ToggleButton("3"); //$NON-NLS-1$
        pageButton3.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                refreshPage(Integer.valueOf(pageButton3.getText()));
            }
        });
        pageButtonList.add(pageButton3);
        pageButton4 = new ToggleButton("4"); //$NON-NLS-1$
        pageButton4.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                refreshPage(Integer.valueOf(pageButton4.getText()));
            }
        });
        pageButtonList.add(pageButton4);
        pageButton5 = new ToggleButton("5"); //$NON-NLS-1$
        pageButton5.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                refreshPage(Integer.valueOf(pageButton5.getText()));
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

            @Override
            public void handleEvent(BaseEvent be) {
                if (sizeField.isValid() && sizeField.getValue() != null) {
                    pageSize = sizeField.getValue().intValue();
                    pageNumber = 1;
                    refreshPage(pageNumber);
                    //                    setPageSize((int)Double.parseDouble(sizeField.getValue()+""));//$NON-NLS-1$
                    // first();
                }
            }
        });
        sizeField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent fe) {
                if (fe.getKeyCode() == KeyCodes.KEY_ENTER) {
                    // blur(inputEl.dom);
                }
            }
        });
        sizeField.setValue(pageSize);

        pagingBar.add(addButton);
        addButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.add()));
        pagingBar.add(separatorLeft);
        pagingBar.add(pageButton1);
        pagingBar.add(pageSpace1);
        pagingBar.add(pageButton2);
        pagingBar.add(pageButton3);
        pagingBar.add(pageButton4);
        pagingBar.add(pageSpace2);
        pagingBar.add(pageButton5);
        pagingBar.add(separatorRight);
        pagingBar.add(sizeLabel);
        pagingBar.add(sizeField);
        pagingBar.setSpacing(5);
        pagingBar.setStyleName("x-toolbar"); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    public void createWidget() {

        verticalPanel = new VerticalPanel();
        verticalPanel.setLayout(new FitLayout());
        pageCount = this.getPageCount();
        // if (typeModel.isSimpleType()
        // || (!typeModel.isSimpleType() && ((ComplexTypeModel) typeModel).getReusableComplexTypes().size() > 0)) {
        // Field<?> field = TreeDetailGridFieldCreator.createField(node, typeModel, Locale.getLanguage());
        // this.add(field);
        // }

        if (itemNode.getObjectValue() != null) {
            if (itemNode.getObjectValue() instanceof ForeignKeyBean) {
                foreignKeyBeans = new ArrayList<ForeignKeyBean>();
                foreignKeyBeans.add((ForeignKeyBean) itemNode.getObjectValue());
            } else if (itemNode.getObjectValue() instanceof List) {
                foreignKeyBeans = (List<ForeignKeyBean>) itemNode.getObjectValue();
            }

            for (int i = 0; i < foreignKeyBeans.size(); i++) {
                ForeignKeyBean foreignKeyBean = foreignKeyBeans.get(i);
                Field<?> field = createField(foreignKeyBean);
                add(field);
            }
        } else {
            foreignKeyBeans = new ArrayList<ForeignKeyBean>();
            if (typeModel.getMinOccurs() > 0) {
                addField();
            }
        }

        verticalPanel.setSpacing(10);
        this.add(verticalPanel);
        refreshPage(pageNumber);
        this.setBottomComponent(pagingBar);
    }

    private void refreshPage(int currentPageNumber) {
        pageNumber = currentPageNumber;
        pageCount = this.getPageCount();
        pageStartIndex = (currentPageNumber - 1) * pageSize;
        verticalPanel.removeAll();
        if (this.fields.size() > 0) {
            for (int i = pageStartIndex; i < pageStartIndex + pageSize && i < this.fields.size(); i++) {
                if (typeModel.isSimpleType()
                        || (!typeModel.isSimpleType() && ((ComplexTypeModel) typeModel).getReusableComplexTypes().size() > 0)) {
                    HorizontalPanel recordPanel = new HorizontalPanel();
                    Button removeButton = new Button();
                    removeButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Delete()));
                    addRemoveListener(removeButton, i);
                    recordPanel.add(removeButton);
                    // HTML label = new HTML();
                    // String html = itemNode.getLabel();
                    // if (itemNode.isKey() || typeModel.getMinOccurs() >= 1)
                    //                        html = html + "<span style=\"color:red\"> *</span>"; //$NON-NLS-1$
                    // label.setHTML(html);
                    // recordPanel.add(label);
                    Field<?> field = this.getField(i);
                    field.setWidth(200);
                    recordPanel.add(field);
                    recordPanel.setSpacing(2);
                    verticalPanel.add(recordPanel);
                }
            }
        }
        refreshPageButton();
        refresh();
    }

    private void refreshPageButton() {

        resetPageButtonList();

        if (this.pageCount >= 0 && this.pageCount <= 5) {

            for (int i = 4; i >= this.pageCount; i--) {
                pageButtonList.get(i).setVisible(false);
            }
            pageToggleButtonIndex = pageNumber - 1;
        } else {

            pageSpace1.setVisible(pageNumber < 4 ? false : true);
            pageSpace2.setVisible(pageNumber > this.pageCount - 3 ? false : true);

            if (pageNumber < 3) {
                pageToggleButtonIndex = pageNumber - 1;
            } else if (pageNumber > this.pageCount - 2) {
                pageToggleButtonIndex = 4 + this.pageNumber - pageCount;
                pageButtonList.get(1).setText(String.valueOf(this.pageCount - 3));
                pageButtonList.get(2).setText(String.valueOf(this.pageCount - 2));
                pageButtonList.get(3).setText(String.valueOf(this.pageCount - 1));
            } else {
                pageToggleButtonIndex = 2;
                pageButtonList.get(1).setText(String.valueOf(Integer.valueOf(pageNumber) - 1));
                pageButtonList.get(2).setText(String.valueOf(pageNumber));
                pageButtonList.get(3).setText(String.valueOf(Integer.valueOf(pageNumber) + 1));
            }
            pageButtonList.get(4).setText(String.valueOf(this.pageCount));
        }
        if (pageToggleButtonIndex >= 0) {
            pageButtonList.get(pageToggleButtonIndex).toggle(true);
        }
    }

    private void resetPageButtonList() {
        pageSpace1.setVisible(false);
        pageSpace2.setVisible(false);

        pageButtonList.get(0).setVisible(true);
        pageButtonList.get(1).setVisible(true);
        pageButtonList.get(2).setVisible(true);
        pageButtonList.get(3).setVisible(true);
        pageButtonList.get(4).setVisible(true);

        pageButtonList.get(0).toggle(false);
        pageButtonList.get(1).toggle(false);
        pageButtonList.get(2).toggle(false);
        pageButtonList.get(3).toggle(false);
        pageButtonList.get(4).toggle(false);

        pageButtonList.get(0).setText("1"); //$NON-NLS-1$
        pageButtonList.get(1).setText("2"); //$NON-NLS-1$
        pageButtonList.get(2).setText("3"); //$NON-NLS-1$
        pageButtonList.get(3).setText("4"); //$NON-NLS-1$
        pageButtonList.get(4).setText("5"); //$NON-NLS-1$
    }

    public void addField() {
        if (this.fields.size() < typeModel.getMaxOccurs()) {
            ForeignKeyBean foreignKeyBean = new ForeignKeyBean();
            foreignKeyBeans.add(foreignKeyBean);
            this.add(createField(foreignKeyBean));
            refreshPage(this.getPageCount());
            this.refresh();
        } else {
            MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                    .message_validate_max_occurence(typeModel.getMaxOccurs()), null);
        }
    }

    public Field<?> createField(Object value) {
        Field<?> field = null;
        if (typeModel.getForeignkey() != null) {
            ForeignKeySelector foreignKeySelector = new ForeignKeySelector(typeModel, ForeignKeyFieldList.this, itemsDetailPanel,
                    itemNode);
            foreignKeySelector.setValue((ForeignKeyBean) value);
            field = foreignKeySelector;
            // ((List<ForeignKeyBean>) itemNode.getObjectValue()).add((ForeignKeyBean)value);
            addForeignKeyFieldListener(field, (ForeignKeyBean) value);
        }
        return field;
    }

    public void refresh() {
        this.doLayout();
    }

    private int getPageCount() {
        return (this.fields.size() % this.pageSize == 0 ? this.getFieldList().size() / this.pageSize : this.getFieldList().size()
                / this.pageSize + 1);
    }

    private void addForeignKeyFieldListener(final Field<?> field, final ForeignKeyBean foreignKeyBean) {

        field.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent fe) {
                int index = foreignKeyBeans.indexOf(foreignKeyBean);
                foreignKeyBeans.set(index, (ForeignKeyBean) fe.getValue());
                itemNode.setChangeValue(true);
                validate();
            }
        });

        field.addListener(Events.Attach, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent fe) {
                validate();
            }
        });
    }

    private void addRemoveListener(final Button button, final int i) {

        button.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {

                if (fields.size() < typeModel.getMinOccurs()) {
                    MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                            .message_validate_min_occurence(typeModel.getMinOccurs()), null);
                } else {
                    removeForeignKeyWidget(foreignKeyBeans.get(i));
                }
            }
        });
    }

    Validator validator = new Validator() {

        @Override
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

    public void removeForeignKeyWidget(ForeignKeyBean value) {
        if (fields.size() < typeModel.getMinOccurs()) {
            MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                    .message_validate_min_occurence(typeModel.getMinOccurs()), null);
        } else {
            int index = foreignKeyBeans.indexOf(value);
            foreignKeyBeans.remove(index);
            fields.remove(index);
            itemNode.setChangeValue(true);
            refreshPage(this.getPageCount());
        }
    }

    public void validate() {
        boolean flag = true;
        if (typeModel.getMinOccurs() > 0) {
            if (foreignKeyBeans.size() == 0 || "".equals(foreignKeyBeans.get(0).getId())) { //$NON-NLS-1$
                MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                        .message_validate_min_occurence(typeModel.getMinOccurs()), null);
                flag = false;
            }
        }
        itemNode.setValid(flag);
    }
}
