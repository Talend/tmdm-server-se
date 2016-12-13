/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.client.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.talend.mdm.webapp.stagingarea.control.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.stagingarea.control.shared.model.ConceptRelationshipModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.FilterModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

class FilterDialog extends Window {

    public interface FilterListener {

        void onFilter(FilterModel filterModel);
    }

    private final ContentPanel container = new ContentPanel();

    private final EntitySet entitySet;

    private final StatusSet statusSet = new StatusSet();

    private final TimeSet timeSet = new TimeSet();

    public static void showFilter(ConceptRelationshipModel relation, FilterListener listener) {
        FilterDialog dialog = new FilterDialog(relation, listener);
        dialog.show();
    }

    private FilterDialog(final ConceptRelationshipModel relation, final FilterListener listener) {
        this.setClosable(false);
        this.setLayout(new FitLayout());
        this.setModal(true);

        container.setHeaderVisible(false);
        container.setBodyBorder(false);
        container.setScrollMode(Scroll.AUTOY);

        entitySet = new EntitySet(relation);
        entitySet.setSize(600, 260);
        FieldSet conceptSet = new FieldSet();
        conceptSet.setHeading(MessagesFactory.getMessages().entity_filter());
        conceptSet.add(new HTML("<b>" + MessagesFactory.getMessages().entity_filter_title() + "</b>"), new RowData(1, -1, //$NON-NLS-1$//$NON-NLS-2$
                new Margins(0, 10, 0, 10)));
        conceptSet.add(entitySet, new RowData(1, -1, new Margins(0, 0, 0, 0)));
        Button reset = new Button(MessagesFactory.getMessages().reset_concept());
        conceptSet.add(reset, new RowData(1, 1, new Margins(0, 10, 0, 10)));

        container.add(conceptSet, new RowData(1, -1, new Margins(0, 10, 0, 10)));
        container.add(statusSet, new RowData(1, -1, new Margins(0, 10, 0, 10)));
        container.add(timeSet, new RowData(1, 1, new Margins(0, 10, 0, 10)));

        Button ok = new Button(MessagesFactory.getMessages().ok());
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                hide();
                FilterModel filter = new FilterModel();
                List<String> concepts = entitySet.getSelectedConcepts();
                filter.setConcepts(concepts);
                filter.setStatusCodes(statusSet.getStatuses());
                filter.setStartDate(timeSet.getStartDate());
                filter.setEndDate(timeSet.getEndDate());
                listener.onFilter(filter);
            }
        });
        Button cancel = new Button(MessagesFactory.getMessages().cancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                hide();
            }
        });
        this.addButton(ok);
        this.addButton(cancel);
        this.setButtonAlign(HorizontalAlignment.RIGHT);

        this.add(container);
        reset.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                for (CheckBox box : entitySet.boxes) {
                    box.setValue(false, true);
                }
            }
        });
        setSize(660, 570);
    }

    class EntitySet extends FieldSet {

        final AbsolutePanel wrapPanel = new AbsolutePanel();

        final ConceptRelationshipModel relationModel;

        final Grid header = new Grid(1, 2);

        final ScrollPanel conceptSetContainer = new ScrollPanel();

        final FlexTable flextable = new FlexTable();

        final List<CheckBox> boxes = new ArrayList<CheckBox>();

        public EntitySet(ConceptRelationshipModel relationModel) {
            this.relationModel = relationModel;
            this.setLayout(new FitLayout());
            this.setBorders(false);
            initLayout();
            initBoxesEVent();
        }

        final ValueChangeHandler<Boolean> selectedTogetherHandler = new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                CheckBox sourceBox = (CheckBox) event.getSource();
                for (CheckBox box : boxes) {
                    if (sourceBox.getText().equals(box.getText())) {
                        box.setValue(sourceBox.getValue(), true);
                    }
                }
            }
        };

        private void initLayout() {

            flextable.setWidth("100%"); //$NON-NLS-1$

            flextable.getElement().getStyle().setFontSize(12D, Unit.PX);
            flextable.setCellSpacing(3);
            flextable.setCellPadding(5);

            flextable.getElement().getStyle().setBackgroundColor("white"); //$NON-NLS-1$
            flextable.getColumnFormatter().setWidth(0, "50%"); //$NON-NLS-1$

            flextable.setText(0, 0, ""); //$NON-NLS-1$
            flextable.getFlexCellFormatter().setColSpan(0, 0, 2);
            flextable.getRowFormatter().getElement(0).setAttribute("height", "36"); //$NON-NLS-1$//$NON-NLS-2$

            int i = 1;
            for (String concept : relationModel.getConcepts()) {
                final CheckBox conceptCheck = new CheckBox(concept);
                boxes.add(conceptCheck);
                conceptCheck.getElement().getStyle().setMarginLeft(5D, Unit.PX);
                final FlowPanel fksPanel = new FlowPanel();
                fksPanel.setVisible(false);
                String[] fkConcepts = relationModel.getRelationShipMap().get(concept);
                for (String fkConcept : fkConcepts) {
                    CheckBox fkBox = new CheckBox(fkConcept);
                    fkBox.getElement().getStyle().setMarginLeft(5D, Unit.PX);
                    boxes.add(fkBox);
                    fksPanel.add(new CheckBoxWrap(fkBox));
                }
                conceptCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        fksPanel.setVisible(conceptCheck.getValue());
                    }
                });

                flextable.setWidget(i, 0, new CheckBoxWrap(conceptCheck));
                flextable.setWidget(i, 1, fksPanel);
                flextable.getCellFormatter().getElement(i, 0).getStyle().setBackgroundColor("rgb(233,233,233)"); //$NON-NLS-1$
                flextable.getCellFormatter().getElement(i, 1).getStyle().setBackgroundColor("rgb(243,243,243)"); //$NON-NLS-1$
                flextable.getRowFormatter().getElement(i).setAttribute("height", "24"); //$NON-NLS-1$//$NON-NLS-2$
                i++;
            }

            conceptSetContainer.setSize("100%", "100%"); //$NON-NLS-1$//$NON-NLS-2$
            conceptSetContainer.setWidget(flextable);
            conceptSetContainer.getElement().getStyle().setBackgroundColor("white"); //$NON-NLS-1$

            Label conceptLb = new Label(MessagesFactory.getMessages().entity());
            conceptLb.getElement().getStyle().setColor("rgb(102,102,102)"); //$NON-NLS-1$
            conceptLb.getElement().getStyle().setMarginLeft(5D, Unit.PX);
            Label fkRelation = new Label(MessagesFactory.getMessages().dep_fk());
            fkRelation.getElement().getStyle().setColor("rgb(102,102,102)"); //$NON-NLS-1$
            fkRelation.getElement().getStyle().setMarginLeft(5D, Unit.PX);

            header.setCellSpacing(3);
            header.setCellPadding(5);
            header.getColumnFormatter().setWidth(0, "50%"); //$NON-NLS-1$
            header.getElement().getStyle().setBackgroundColor("white"); //$NON-NLS-1$
            header.getCellFormatter().getElement(0, 0).getStyle().setBackgroundColor("rgb(223,223,223)"); //$NON-NLS-1$
            header.getCellFormatter().getElement(0, 1).getStyle().setBackgroundColor("rgb(243,243,243"); //$NON-NLS-1$

            header.getRowFormatter().getElement(0).getStyle().setFontWeight(FontWeight.BOLD);
            header.getRowFormatter().getElement(0).getStyle().setFontSize(14D, Unit.PX);
            header.getRowFormatter().getElement(0).setAttribute("height", "36"); //$NON-NLS-1$//$NON-NLS-2$
            header.setWidget(0, 0, conceptLb);
            header.setWidget(0, 1, fkRelation);

            wrapPanel.add(conceptSetContainer, 0, 0);
            wrapPanel.add(header, 0, 0);
            this.add(wrapPanel, new RowData(1, 1, new Margins(0, 0, 0, 0)));
        }

        @Override
        protected void onRender(Element parent, int pos) {
            super.onRender(parent, pos);
            adjust();
        }

        private void adjust() {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    int cH = container.getElement().getOffsetHeight();
                    int fH = flextable.getElement().getOffsetHeight();
                    if (fH > cH) {
                        header.setWidth(getBody().dom.getOffsetWidth() - 17 + "px"); //$NON-NLS-1$
                    } else {
                        header.setWidth("100%"); //$NON-NLS-1$
                    }
                }
            });
        }

        private void initBoxesEVent() {
            for (CheckBox box : boxes) {
                box.addValueChangeHandler(selectedTogetherHandler);
            }
        }

        private List<String> getSelectedConcepts() {
            List<String> concepts = new ArrayList<String>();
            for (int i = 1; i < flextable.getRowCount(); i++) {
                CheckBoxWrap conceptBox = (CheckBoxWrap) flextable.getWidget(i, 0);
                if (conceptBox.getValue()) {
                    concepts.add(conceptBox.getText());
                }
            }
            return concepts;
        }

        class CheckBoxWrap extends Composite {

            final SimplePanel sp = new SimplePanel();

            final CheckBox box;

            public CheckBoxWrap(CheckBox box) {
                sp.setWidget(box);
                this.box = box;
                sp.getElement().getStyle().setFloat(Float.LEFT);
                this.initWidget(sp);
            }

            public CheckBox getBox() {
                return box;
            }

            public boolean getValue() {
                return box.getValue();
            }

            public void setValue(boolean value, boolean fireEvents) {
                box.setValue(value, fireEvents);
            }

            public String getText() {
                return box.getText();
            }
        }
    }

    class StatusSet extends FieldSet {

        final Label defaultLb = new Label(MessagesFactory.getMessages().default_option());

        final Label selectedStatuses = new Label(MessagesFactory.getMessages().selected_statuses());

        boolean isDefault;

        final HorizontalPanel statusLine = new HorizontalPanel();

        final CheckBox status000 = new CheckBox("000"); //$NON-NLS-1$

        final CheckBox status204 = new CheckBox("204"); //$NON-NLS-1$

        final CheckBox status404 = new CheckBox("404"); //$NON-NLS-1$

        final FlexTable table = new FlexTable();

        public StatusSet() {
            initEvent();
            table.setCellPadding(3);

            status000.setValue(true);
            status204.setValue(true);
            status404.setValue(true);
            status000.getElement().getStyle().setMarginRight(5D, Unit.PX);
            status204.getElement().getStyle().setMarginRight(5D, Unit.PX);
            status404.getElement().getStyle().setMarginRight(5D, Unit.PX);

            statusLine.add(status000);
            statusLine.add(status204);
            statusLine.add(status404);

            isDefault = true;
            defaultLb.getElement().getStyle().setCursor(Cursor.TEXT);
            defaultLb.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            defaultLb.getElement().getStyle().setColor("black"); //$NON-NLS-1$
            defaultLb.getElement().getStyle().setMargin(5D, Unit.PX);

            selectedStatuses.getElement().getStyle().setCursor(Cursor.POINTER);
            selectedStatuses.getElement().getStyle().setColor("blue"); //$NON-NLS-1$
            selectedStatuses.getElement().getStyle().setMargin(5D, Unit.PX);

            statusLine.setVisible(false);

            table.setWidget(0, 0, defaultLb);
            table.setText(0, 1, " - "); //$NON-NLS-1$
            table.setWidget(0, 2, selectedStatuses);
            table.setWidget(0, 3, statusLine);

            this.add(table);
            this.setHeading(MessagesFactory.getMessages().status_filter());
        }

        private void initEvent() {
            defaultLb.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    statusLine.setVisible(false);
                    isDefault = true;
                    defaultLb.getElement().getStyle().setCursor(Cursor.TEXT);
                    defaultLb.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                    defaultLb.getElement().getStyle().setColor("black"); //$NON-NLS-1$

                    selectedStatuses.getElement().getStyle().setCursor(Cursor.POINTER);
                    selectedStatuses.getElement().getStyle().setColor("blue"); //$NON-NLS-1$
                    selectedStatuses.getElement().getStyle().setFontWeight(FontWeight.NORMAL);
                }
            });

            selectedStatuses.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    statusLine.setVisible(true);
                    isDefault = false;
                    selectedStatuses.getElement().getStyle().setCursor(Cursor.TEXT);
                    selectedStatuses.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                    selectedStatuses.getElement().getStyle().setColor("black"); //$NON-NLS-1$

                    defaultLb.getElement().getStyle().setCursor(Cursor.POINTER);
                    defaultLb.getElement().getStyle().setColor("blue"); //$NON-NLS-1$
                    defaultLb.getElement().getStyle().setFontWeight(FontWeight.NORMAL);
                }
            });

            ClickHandler statusClick = new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    if (getStatuses().size() == 0) {
                        event.preventDefault();
                    }
                }
            };

            status000.addClickHandler(statusClick);
            status204.addClickHandler(statusClick);
            status404.addClickHandler(statusClick);
        }

        public List<String> getStatuses() {
            List<String> statuses = new ArrayList<String>();
            if (isDefault) {
                return statuses;
            }
            if (status000.getValue()) {
                statuses.add("000"); //$NON-NLS-1$
            }
            if (status204.getValue()) {
                statuses.add("204"); //$NON-NLS-1$
            }
            if (status404.getValue()) {
                statuses.add("404"); //$NON-NLS-1$
            }
            return statuses;
        }
    }

    class TimeSet extends FieldSet {

        final Label all = new Label(MessagesFactory.getMessages().all());

        final Label today = new Label(MessagesFactory.getMessages().today());

        final Label yesterday = new Label(MessagesFactory.getMessages().yesterday());

        final Label lastWeek = new Label(MessagesFactory.getMessages().last_week());

        final Label lastMonth = new Label(MessagesFactory.getMessages().last_month());

        final Label custom = new Label(MessagesFactory.getMessages().customizing());

        Label selectedLabel = all;

        final DateField startDate = new DateField();

        final DateField endDate = new DateField();

        final HorizontalPanel termPanel = new HorizontalPanel();

        final FlexTable table = new FlexTable();

        final VerticalPanel vp = new VerticalPanel();

        static final long DAY = 1000 * 60 * 60 * 24;

        final Date now = new Date();

        final ClickHandler labelHandler = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Label timeLabel = (Label) event.getSource();
                if (timeLabel != selectedLabel) {

                    selectedLabel.getElement().getStyle().setCursor(Cursor.POINTER);
                    selectedLabel.getElement().getStyle().setColor("blue"); //$NON-NLS-1$
                    selectedLabel.getElement().getStyle().setFontWeight(FontWeight.NORMAL);

                    timeLabel.getElement().getStyle().setCursor(Cursor.TEXT);
                    timeLabel.getElement().getStyle().setColor("black"); //$NON-NLS-1$
                    timeLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);

                    DateTimePropertyEditor editor = new DateTimePropertyEditor("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
                    startDate.setPropertyEditor(editor);
                    endDate.setPropertyEditor(editor);

                    if (timeLabel == all) {
                        startDate.setValue(null);
                        endDate.setValue(null);
                    } else if (timeLabel == today) {
                        startDate.setValue(getStartOfDay(new Date(now.getTime())));
                        endDate.setValue(getEndOfDay());
                    } else if (timeLabel == yesterday) {
                        startDate.setValue(getStartOfDay(new Date(now.getTime() - DAY)));
                        endDate.setValue(getEndOfDay());
                    } else if (timeLabel == lastWeek) {
                        startDate.setValue(getStartOfDay(new Date(now.getTime() - (7 * DAY))));
                        endDate.setValue(getEndOfDay());
                    } else if (timeLabel == lastMonth) {
                        startDate.setValue(getStartOfDay(new Date(now.getTime() - (30 * DAY))));
                        endDate.setValue(getEndOfDay());
                    }

                    if (timeLabel == custom) {
                        termPanel.setVisible(true);
                    } else {
                        termPanel.setVisible(false);
                    }

                    selectedLabel = timeLabel;
                }
            }

            private Date getStartOfDay(Date date) {
                date.setHours(0);
                date.setMinutes(0);
                date.setSeconds(0);
                return date;
            }

            private Date getEndOfDay() {
                // Use of deprecated methods but Calendar can't be used in GWT
                Date date = new Date(System.currentTimeMillis());
                date.setHours(23);
                date.setMinutes(59);
                date.setSeconds(59);
                return date;
            }
        };

        public TimeSet() {
            all.getElement().getStyle().setCursor(Cursor.TEXT);
            all.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            all.getElement().getStyle().setColor("black"); //$NON-NLS-1$

            today.getElement().getStyle().setCursor(Cursor.POINTER);
            today.getElement().getStyle().setColor("blue"); //$NON-NLS-1$
            today.getElement().getStyle().setMargin(5D, Unit.PX);

            yesterday.getElement().getStyle().setCursor(Cursor.POINTER);
            yesterday.getElement().getStyle().setColor("blue"); //$NON-NLS-1$
            yesterday.getElement().getStyle().setMargin(5D, Unit.PX);

            lastWeek.getElement().getStyle().setCursor(Cursor.POINTER);
            lastWeek.getElement().getStyle().setColor("blue"); //$NON-NLS-1$
            lastWeek.getElement().getStyle().setMargin(5D, Unit.PX);

            lastMonth.getElement().getStyle().setCursor(Cursor.POINTER);
            lastMonth.getElement().getStyle().setColor("blue"); //$NON-NLS-1$
            lastMonth.getElement().getStyle().setMargin(5D, Unit.PX);

            custom.getElement().getStyle().setCursor(Cursor.POINTER);
            custom.getElement().getStyle().setColor("blue"); //$NON-NLS-1$
            custom.getElement().getStyle().setMargin(5D, Unit.PX);

            all.addClickHandler(labelHandler);
            today.addClickHandler(labelHandler);
            yesterday.addClickHandler(labelHandler);
            lastWeek.addClickHandler(labelHandler);
            lastMonth.addClickHandler(labelHandler);
            custom.addClickHandler(labelHandler);

            table.setWidget(0, 0, all);
            table.setText(0, 1, "-"); //$NON-NLS-1$
            table.setWidget(0, 2, today);
            table.setText(0, 3, "-"); //$NON-NLS-1$
            table.setWidget(0, 4, yesterday);
            table.setText(0, 5, "-"); //$NON-NLS-1$
            table.setWidget(0, 6, lastWeek);
            table.setText(0, 7, "-"); //$NON-NLS-1$
            table.setWidget(0, 8, lastMonth);

            table.setWidget(0, 9, custom);

            startDate.setValue(null);
            endDate.setValue(null);
            termPanel.setVisible(false);
            termPanel.add(startDate);
            termPanel.add(new Label(MessagesFactory.getMessages().to()));
            termPanel.add(endDate);

            vp.add(table);
            vp.add(termPanel);
            this.setHeading(MessagesFactory.getMessages().datetime_filter());
            this.add(vp);
        }

        public Date getStartDate() {
            return startDate.getValue();
        }

        public Date getEndDate() {
            return endDate.getValue();
        }
    }

}
