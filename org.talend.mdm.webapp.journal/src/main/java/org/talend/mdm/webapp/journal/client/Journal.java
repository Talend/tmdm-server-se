/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client;

import org.talend.mdm.webapp.base.client.ServiceEnhancer;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.journal.client.mvc.JournalController;
import org.talend.mdm.webapp.journal.client.util.TimelineUtil;
import org.talend.mdm.webapp.journal.client.widget.JournalSearchPanel;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class Journal implements EntryPoint {

    public static final String JOURNAL_SERVICE = "JournalService"; //$NON-NLS-1$

    public static final String JOURNAL_ID = "Journal"; //$NON-NLS-1$

    public static final String SEARCH_CRITERIA = "SearchCriteria"; //$NON-NLS-1$

    private native void registerJournalService()/*-{
		var instance = this;
		$wnd.amalto.journal.Journal.browseJournalWithCriteria = function(ids,
				concept, isItemsBroswer) {
			instance.@org.talend.mdm.webapp.journal.client.Journal::initJournalfromBrowseRecord(Ljava/lang/String;Ljava/lang/String;)(ids, concept);
			instance.@org.talend.mdm.webapp.journal.client.Journal::initUI()();
		}
    }-*/;

    @Override
    public void onModuleLoad() {
        XDOM.setAutoIdPrefix(GWT.getModuleName() + "-" + XDOM.getAutoIdPrefix()); //$NON-NLS-1$
        registerPubService();
        TimelineUtil.regLoadDate();
        TimelineUtil.regShowDialog();
        Log.setUncaughtExceptionHandler();

        ServiceDefTarget service = GWT.create(JournalService.class);
        ServiceEnhancer.customizeService(service);
        Registry.register(JOURNAL_SERVICE, service);
        Registry.register(SEARCH_CRITERIA, new JournalSearchCriteria());

        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addController(new JournalController());
        registerJournalService();
    }

    private native void registerPubService()/*-{
		var instance = this;
		$wnd.amalto.journal = {};
		$wnd.amalto.journal.Journal = function() {

			function initUI() {
				instance.@org.talend.mdm.webapp.journal.client.Journal::resetSearchCondition()();
				instance.@org.talend.mdm.webapp.journal.client.Journal::initUI()();
			}

			return {
				init : function() {
					initUI();
				}
			}
		}();
    }-*/;

    private native void _initUI()/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var panel = tabPanel.getItem("Journal");
		if (panel == undefined) {
			@org.talend.mdm.webapp.journal.client.GenerateContainer::generateContentPanel()();
			panel = this.@org.talend.mdm.webapp.journal.client.Journal::createPanel()();
			tabPanel.add(panel);
		} else {
			this.@org.talend.mdm.webapp.journal.client.Journal::onSearchWithCriteria()();
		}
		tabPanel.setSelection(panel.getItemId());
    }-*/;

    native JavaScriptObject createPanel()/*-{
		var instance = this;
		var panel = {
			render : function(el) {
				instance.@org.talend.mdm.webapp.journal.client.Journal::renderContent(Ljava/lang/String;)(el.id);
			},
			setSize : function(width, height) {
				var cp = @org.talend.mdm.webapp.journal.client.GenerateContainer::getContentPanel()();
				cp.@com.extjs.gxt.ui.client.widget.ContentPanel::setSize(II)(width, height);
			},
			getItemId : function() {
				var cp = @org.talend.mdm.webapp.journal.client.GenerateContainer::getContentPanel()();
				return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getItemId()();
			},
			getEl : function() {
				var cp = @org.talend.mdm.webapp.journal.client.GenerateContainer::getContentPanel()();
				var el = cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getElement()();
				return {
					dom : el
				};
			},
			doLayout : function() {
				var cp = @org.talend.mdm.webapp.journal.client.GenerateContainer::getContentPanel()();
				return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::doLayout()();
			},
			title : function() {
				var cp = @org.talend.mdm.webapp.journal.client.GenerateContainer::getContentPanel()();
				return cp.@com.extjs.gxt.ui.client.widget.ContentPanel::getHeading()();
			}
		};
		return panel;
    }-*/;

    public void renderContent(final String contentId) {
        onModuleRender();

        final ContentPanel content = GenerateContainer.getContentPanel();

        if (GWT.isScript()) {
            RootPanel panel = RootPanel.get(contentId);
            panel.add(content);
        } else {
            final Element element = DOM.getElementById(contentId);
            SimplePanel panel = new SimplePanel() {

                @Override
                protected void setElement(Element elem) {
                    super.setElement(element);
                }
            };
            RootPanel rootPanel = RootPanel.get();
            rootPanel.clear();
            rootPanel.add(panel);
            panel.add(content);
        }
    }

    public void initUI() {
        _initUI();
    }

    private void onModuleRender() {
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.dispatch(JournalEvents.InitFrame);
    }

    private void initJournalfromBrowseRecord(String ids, String concept) {
        resetSearchCondition();
        JournalSearchCriteria criteria = Registry.get(Journal.SEARCH_CRITERIA);
        criteria.setKey(ids);
        JournalSearchPanel.getInstance().setKeyFieldValue(ids);
        criteria.setEntity(concept);
        JournalSearchPanel.getInstance().setEntityFieldValue(concept);
        JournalSearchPanel.getInstance().setCurrentDataModel();
    }

    private void onSearchWithCriteria() {
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.dispatch(JournalEvents.DoSearch);
    }

    private void resetSearchCondition() {
        JournalSearchPanel.getInstance().reset();
        JournalSearchCriteria criteria = Registry.get(Journal.SEARCH_CRITERIA);
        criteria.setStrict(true);
        criteria.setDataModel(UserContextUtil.getDataModel());
        criteria.setEntity(null);
        criteria.setKey(null);
        criteria.setOperationType(null);
        criteria.setSource(null);
        criteria.setStartDate(null);
        criteria.setEndDate(null);
        JournalSearchPanel.getInstance().initPanel();
    }
}