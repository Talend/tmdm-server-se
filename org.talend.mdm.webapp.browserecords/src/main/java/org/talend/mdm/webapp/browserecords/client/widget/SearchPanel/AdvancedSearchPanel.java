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

import java.util.Date;

import org.talend.mdm.webapp.base.client.exception.ParserException;
import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
import org.talend.mdm.webapp.base.client.util.Parser;
import org.talend.mdm.webapp.base.shared.OperatorValueConstants;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsSearchContainer;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class AdvancedSearchPanel extends FormPanel {

    private ViewBean view;

    private TextField<String> expressionTextField;

    protected ComboBox<BaseModel> cb;

    private BaseModel modifiedonModel = new BaseModel();

    private BaseModel matchgroupModel = new BaseModel();

    private AdvancedSearchPanel instance = this;

    private static String ge = "GREATER_THAN_OR_EQUAL";//$NON-NLS-1$

    private static String le = "LOWER_THAN_OR_EQUAL";//$NON-NLS-1$

    public static String modifiedON = "../../t";//$NON-NLS-1$

    public static String matchGroup = "../../taskId"; //$NON-NLS-1$

    private static String blank = " ";//$NON-NLS-1$

    private Button filterButton = new Button();

    private Button searchBtn;

    ModifiedOnCriteria modifiedonCriteria;

    MatchGroupCriteria matchgroupCriteria;

    private boolean staging;

    final public void setCriteria(String criteriaStr) {
        if (criteriaStr.indexOf(modifiedON + blank) > -1) {
            // modified on condition
            String express = criteriaStr.indexOf(modifiedON) - 5 > -1 ? criteriaStr.substring(0,
                    criteriaStr.indexOf(modifiedON) - 5) + ")" : "";//$NON-NLS-1$ //$NON-NLS-2$ 
            parseSearchExpression(express);
            expressionTextField.setValue(express);
            String condition = criteriaStr.endsWith(")") ? criteriaStr.substring(criteriaStr.indexOf(modifiedON), criteriaStr.length() - 1) : criteriaStr.substring(criteriaStr.indexOf(modifiedON), criteriaStr.length()); //$NON-NLS-1$
            if (!modifiedonCriteria.isAttached()) {
                matchgroupCriteria.removeFromParent();
                instance.add(modifiedonCriteria, new FormData("95%")); //$NON-NLS-1$ 
                instance.layout(true);
            }

            modifiedonCriteria.setStartDate(null);
            modifiedonCriteria.setEndDate(null);

            if (condition.indexOf(ge) > -1) {
                Date d = new Date();
                int index = condition.indexOf(ge) + ge.length() + 1;
                if (condition.indexOf(blank, index) == -1) {
                    d.setTime(Long.valueOf(condition.substring(index)));
                } else {
                    d.setTime(Long.valueOf(condition.substring(index, condition.indexOf(blank, index))));
                }

                modifiedonCriteria.setStartDate(d);
            }
            if (condition.indexOf(le) > -1) {
                Date d = new Date();
                int index = condition.indexOf(le) + le.length() + 1;
                if (condition.indexOf(blank, index) == -1) {
                    d.setTime(Long.valueOf(condition.substring(index)));
                } else {
                    d.setTime(Long.valueOf(condition.substring(index, condition.indexOf(blank, index))));
                }
                modifiedonCriteria.setEndDate(d);
            }
            cb.setValue(modifiedonModel);
        } else if (criteriaStr.indexOf(matchGroup + blank) > -1) {
            String express = filterOutMatchGroupConditon(criteriaStr);
            parseSearchExpression(express);
            expressionTextField.setValue(express);
            String condition = getMatchGroupCondition(criteriaStr);
            if (!matchgroupCriteria.isAttached()) {
                modifiedonCriteria.removeFromParent();
                instance.add(matchgroupCriteria, new FormData("75%")); //$NON-NLS-1$ 
                instance.layout(true);
            }
            populateMatchGroupCriteria(condition);

            cb.setValue(matchgroupModel);
        } else {
            if (modifiedonCriteria.isAttached()) {
                modifiedonCriteria.removeFromParent();
            }
            if (matchgroupCriteria.isAttached()) {
                matchgroupCriteria.removeFromParent();
            }
            parseSearchExpression(criteriaStr);
            expressionTextField.setValue(criteriaStr);
            cb.setValue(null);
        }
        if (criteriaStr.contains(OperatorValueConstants.FULLTEXTSEARCH)) {
            cb.setEnabled(false);
        } else {
            cb.setEnabled(true);
        }
    }

    private void populateMatchGroupCriteria(String condition) {
        String value;
        int firstBlankIndex = condition.indexOf(blank);
        int secondBlankIndex = condition.indexOf(blank, firstBlankIndex + 1);
        value = condition.substring(secondBlankIndex + 1);

        matchgroupCriteria.setValue(value);
        matchgroupCriteria.setOperator(condition.substring(firstBlankIndex + 1, secondBlankIndex));
    }

    private String getMatchGroupCondition(String criteriaStr) {
        int startIndex = criteriaStr.indexOf(matchGroup);
        String condition = criteriaStr.substring(startIndex, criteriaStr.indexOf(')', startIndex));
        return condition;
    }

    private String filterOutMatchGroupConditon(String criteriaStr) {
        int start;
        int end;
        int preEnd;
        int nextStart;
        String express;
        start = criteriaStr.indexOf(matchGroup);
        end = criteriaStr.indexOf(')', start);
        if (start <= 2) {
            nextStart = criteriaStr.indexOf('(', end);
            if (nextStart == -1) {
                express = ""; //$NON-NLS-1$
            } else {
                express = '(' + criteriaStr.substring(nextStart);
            }
        } else {
            preEnd = criteriaStr.lastIndexOf(')', start);
            if (criteriaStr.charAt(end + 1) == ')') {
                express = criteriaStr.substring(0, preEnd + 1) + ')';
            } else {
                express = criteriaStr.substring(0, preEnd + 1) + criteriaStr.substring(end + 1);
            }
        }
        return express;
    }

    public static void parseSearchExpression(String s) {
        try {
            if (!s.isEmpty()) {
                if (!s.startsWith("(") && !s.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
                    s = "((" + s + "))"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                char[] sa = s.toCharArray();
                BrowseRecords.getSession().put(
                        UserSession.CUSTOMIZE_CRITERION_STORE_ADVANCE,
                        sa[1] == '(' ? CommonUtil.parseMultipleSearchExpression(sa, 0).cr : CommonUtil
                                .parseSimpleSearchExpression(sa, 0).cr);
            }
        } catch (Exception e) {
            if (Log.isErrorEnabled()) {
                Log.error(e.toString());
            }
        }
    }

    public String RemoveUnsafeCriteria() {
        String curCriteria = getCriteria();
        return curCriteria.replaceAll("[\\s]+) OR ([\\s]+", " OR ").replaceAll("[\\s]+) AND ([\\s]+", " AND ");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    public String getCriteria() {
        MultipleCriteria criteriaStore = (MultipleCriteria) (BrowseRecords.getSession().get(
                UserSession.CUSTOMIZE_CRITERION_STORE_ADVANCE) == null ? BrowseRecords.getSession().get(
                UserSession.CUSTOMIZE_CRITERION_STORE) : BrowseRecords.getSession().get(
                UserSession.CUSTOMIZE_CRITERION_STORE_ADVANCE));
        String express = criteriaStore != null ? criteriaStore.toString() : null;// expressionTextField.getValue();
        String curCriteria = null, curDate = null;
        if (modifiedonCriteria.isAttached()) {
            if (modifiedonCriteria.getStartDate() != null) {
                curDate = modifiedON + blank + ge + blank + modifiedonCriteria.getStartDate().getTime();
            }
            if (modifiedonCriteria.getEndDate() != null) {
                if (curDate != null) {
                    curDate += " AND " + modifiedON + blank + le + blank + modifiedonCriteria.getEndDate().getTime(); //$NON-NLS-1$
                } else {
                    curDate = modifiedON + blank + le + blank + modifiedonCriteria.getEndDate().getTime();
                }
            }

            if (curDate != null) {
                curCriteria = (express == null) ? curDate : express.substring(0, express.lastIndexOf(")")) + " AND " + curDate //$NON-NLS-1$  //$NON-NLS-2$
                        + ")"; //$NON-NLS-1$
            } else {
                curCriteria = (express == null) ? curDate : express;
            }
        } else if (matchgroupCriteria.isAttached()) {
            String matchgroupCriteriaStr = null;
            if (matchgroupCriteria.getValue() != null && matchgroupCriteria.getValue().trim().length() > 0) {
                matchgroupCriteriaStr = matchGroup + blank + matchgroupCriteria.getOperator() + blank
                        + matchgroupCriteria.getValue();
            }
            if (matchgroupCriteriaStr != null) {
                curCriteria = (express == null) ? matchgroupCriteriaStr
                        : express.substring(0, express.lastIndexOf(")")) + " AND " + matchgroupCriteriaStr + ")"; //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
            }
        } else {
            curCriteria = express;
        }

        return curCriteria;
    }

    public void cleanCriteria() {
        setCriteria(""); //$NON-NLS-1$    
    }

    public void setView(ViewBean viewbean) {
        this.view = viewbean;
    }

    public AdvancedSearchPanel(ViewBean viewbean, Button search) {
        modifiedonCriteria = new ModifiedOnCriteria();
        matchgroupCriteria = new MatchGroupCriteria();
        this.searchBtn = search;
        this.view = viewbean;
        setHeaderVisible(false);
        // setLayout(new FitLayout());

        this.setFrame(true);
        this.setBodyBorder(false);
        this.setHeaderVisible(false);
        this.setScrollMode(Scroll.AUTO);
        this.setLabelWidth(110);
        this.setAutoHeight(true);

        filterButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Edit()));
        filterButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Window winFilter = new Window();
                winFilter.setHeading(MessagesFactory.getMessages().advsearch_filter());
                winFilter.setModal(true);
                winFilter.setScrollMode(Scroll.AUTO);
                // winFilter.setAutoHeight(true);
                // winFilter.setAutoWidth(true);
                // winFilter.setWidth(645);
                // winFilter.setHeight(162);
                ContentPanel root = new ContentPanel();
                root.setHeaderVisible(false);
                root.setBodyBorder(false);
                root.setFrame(false);
                root.setScrollMode(Scroll.AUTO);
                final MultipleCriteriaPanel multiCriteria = new MultipleCriteriaPanel(null, view, winFilter, staging);
                multiCriteria.addStyleName("filter-panel"); //$NON-NLS-1$
                root.add(multiCriteria);
                winFilter.add(root);

                Button searchBtn = new Button(MessagesFactory.getMessages().ok_btn());
                searchBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        MultipleCriteria mutilCriteria = multiCriteria.getCriteria();
                        if (mutilCriteria != null && mutilCriteria.getChildren().size() > 0) {
                            BrowseRecords.getSession().put(UserSession.CUSTOMIZE_CRITERION_STORE_ADVANCE, mutilCriteria);
                            setCriteria(mutilCriteria.toString());
                            winFilter.close();
                        } else {
                            MessageBox.info(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                                    .advsearch_lessinfo(), null);
                        }
                    }

                });
                winFilter.addButton(searchBtn);
                winFilter.show();
                String curField = expressionTextField.getValue();
                if (curField != null && !curField.equals("")) { //$NON-NLS-1$
                    try {
                        parseSearchExpression(curField);
                        MultipleCriteria criteriaStore = (MultipleCriteria) BrowseRecords.getSession().get(
                                UserSession.CUSTOMIZE_CRITERION_STORE_ADVANCE);
                        multiCriteria.setCriteria(criteriaStore);
                    } catch (Exception e) {
                        Log.error(e.getMessage(), e);
                    }
                }
                DOM.setStyleAttribute(winFilter.getBody().dom, "backgroundColor", "white"); //$NON-NLS-1$  //$NON-NLS-2$
            }
        });

        final Button validButton = new Button();
        validButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.Valid()));
        validButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                try {
                    String curField = expressionTextField.getValue();
                    if (curField != null && curField.length() != 0) {
                        Parser.parse(curField);
                        MessageBox.info(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                                .valid_expression(), null);
                    }
                } catch (ParserException e) {
                    Log.error(e.getMessage(), e);
                    MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                            .invalid_expression() + e.getMessage(), null);
                }
            }
        });

        expressionTextField = new TextField<String>() {

            @Override
            protected void onRender(Element target, int index) {
                // add button
                El wrap = new El(DOM.createDiv());
                wrap.addStyleName("x-form-field-wrap"); //$NON-NLS-1$
                wrap.addStyleName("x-form-file-wrap"); //$NON-NLS-1$

                input = new El(DOM.createInputText());
                input.addStyleName(fieldStyle);
                input.addStyleName("x-form-file-text"); //$NON-NLS-1$
                input.setId(XDOM.getUniqueId());

                if (GXT.isIE && target.getTagName().equals("TD")) { //$NON-NLS-1$
                    input.setStyleAttribute("position", "static"); //$NON-NLS-1$  //$NON-NLS-2$
                }

                wrap.appendChild(input.dom);

                setElement(wrap.dom, target, index);

                filterButton.addStyleName("x-form-filter-btn"); //$NON-NLS-1$
                filterButton.render(wrap.dom);
                validButton.addStyleName("x-form-valid-btn"); //$NON-NLS-1$
                validButton.render(wrap.dom);
                super.onRender(target, index);
                input.setEnabled(false);
            }

            @Override
            protected void onResize(int width, int height) {
                super.onResize(width, height);
                filterButton.setSize(25, 25);
                validButton.setSize(25, 25);
                input.setWidth(width - 50 - 6, true);
            }

            @Override
            protected void doAttachChildren() {
                super.doAttachChildren();
                ComponentHelper.doAttach(filterButton);
                ComponentHelper.doAttach(validButton);
            }

            @Override
            protected void doDetachChildren() {
                super.doDetachChildren();
                ComponentHelper.doDetach(filterButton);
                ComponentHelper.doDetach(validButton);
            }

        };

        expressionTextField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchBtn != null) {
                        searchBtn.fireEvent(Events.Select);
                    }
                }
            }
        });

        expressionTextField.setFieldLabel(MessagesFactory.getMessages().search_expression());
        this.add(expressionTextField, new FormData("70%")); //$NON-NLS-1$

        cb = new ComboBox<BaseModel>();
        cb.setEditable(false);
        cb.setFieldLabel(MessagesFactory.getMessages().advsearch_morelabel());
        cb.setAllowBlank(true);
        ListStore<BaseModel> list = new ListStore<BaseModel>();

        modifiedonModel.set("name", MessagesFactory.getMessages().search_modifiedon()); //$NON-NLS-1$
        modifiedonModel.set("value", "modifiedon"); //$NON-NLS-1$  //$NON-NLS-2$
        list.add(modifiedonModel);

        matchgroupModel.set("name", MessagesFactory.getMessages().match_group()); //$NON-NLS-1$
        matchgroupModel.set("value", "matchgroup"); //$NON-NLS-1$ //$NON-NLS-2$
        list.add(matchgroupModel);

        cb.setDisplayField("name"); //$NON-NLS-1$
        cb.setValueField("value"); //$NON-NLS-1$
        cb.setStore(list);
        cb.setTriggerAction(TriggerAction.ALL);

        cb.addSelectionChangedListener(new SelectionChangedListener<BaseModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<BaseModel> se) {
                if (se.getSelectedItem() != null) {
                    String selvalue = se.getSelectedItem().get("value"); //$NON-NLS-1$                   
                    if (selvalue.equals("modifiedon")) { //$NON-NLS-1$
                        if (!modifiedonCriteria.isAttached()) {
                            matchgroupCriteria.removeFromParent();
                            modifiedonCriteria.setStartDate(null);
                            modifiedonCriteria.setEndDate(null);
                            instance.add(modifiedonCriteria, new FormData("95%")); //$NON-NLS-1$
                        }
                    } else if (selvalue.equals("matchgroup")) { //$NON-NLS-1$
                        if (!matchgroupCriteria.isAttached()) {
                            modifiedonCriteria.removeFromParent();
                            matchgroupCriteria.setValue(null);
                            instance.add(matchgroupCriteria, new FormData("75%")); //$NON-NLS-1$
                        }
                    }
                    instance.layout(true);
                    ItemsSearchContainer.getInstance().resizeTop(ItemsSearchContainer.SIZE + instance.getOffsetHeight());
                }
            }

        });
        this.add(cb, new FormData("35%")); //$NON-NLS-1$

    }

    public AdvancedSearchPanel(ViewBean viewbean, Button search, boolean staging) {
        this(viewbean, search);
        this.staging = staging;
    }
}
