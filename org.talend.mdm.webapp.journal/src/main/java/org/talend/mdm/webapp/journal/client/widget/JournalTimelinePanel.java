/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.widget;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.journal.client.Journal;
import org.talend.mdm.webapp.journal.client.JournalServiceAsync;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * DOC talend2 class global comment. Detailled comment
 */
public class JournalTimelinePanel extends ContentPanel {

    private JournalServiceAsync service = Registry.get(Journal.JOURNAL_SERVICE);

    public static String TIMELIME_INIT = "init"; //$NON-NLS-1$

    private boolean isActive = false;

    private boolean isInit = false;

    private JavaScriptObject eventSource;

    private JavaScriptObject lastCenterVisibleDate;

    private int timeOutId;

    private JavaScriptObject eventCache;

    private int timeLinePanelHeight;

    private String dataModel;

    private String entity;

    private String key;

    private String source;

    private String operationType;

    private String startDate;

    private String endDate;

    private boolean isStrict;

    private int start;

    private int limit;

    private String sort;

    private String field;

    private String language;

    public JournalTimelinePanel() {
        this.setLayout(new FitLayout());
        this.setBodyBorder(false);
        this.setFrame(false);
        this.setHeaderVisible(false);
    }

    private native void initJsEnv()/*-{
        this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventCache = [];
        this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventSource = new $wnd.Timeline.DefaultEventSource();
    }-*/;

    public void initTimeline(int startIndex, String config) {
        isInit = true;
        initJsEnv();
        JournalSearchCriteria criteria = Registry.get(Journal.SEARCH_CRITERIA);
        dataModel = criteria.getDataModel() == null ? "" : criteria.getDataModel(); //$NON-NLS-1$
        entity = criteria.getEntity() == null ? "" : criteria.getEntity(); //$NON-NLS-1$
        key = criteria.getKey() == null ? "" : criteria.getKey(); //$NON-NLS-1$
        source = criteria.getSource() == null ? "" : criteria.getSource(); //$NON-NLS-1$
        operationType = criteria.getOperationType() == null ? "" : criteria.getOperationType(); //$NON-NLS-1$
        startDate = criteria.getStartDate() == null ? "" : String.valueOf(criteria.getStartDate().getTime()); //$NON-NLS-1$
        endDate = criteria.getEndDate() == null ? "" : String.valueOf(criteria.getEndDate().getTime()); //$NON-NLS-1$
        isStrict = criteria.isStrict();
        String[] cfgArray = (startIndex + config).split(","); //$NON-NLS-1$
        start = Integer.parseInt(cfgArray[0]);
        limit = Integer.parseInt(cfgArray[1]);
        sort = "ASC"; //$NON-NLS-1$
        field = "operationTime"; //$NON-NLS-1$
        language = cfgArray[4];
        loadTimeline(start);
    }

    private void loadTimeline(int startIndex) {
        start = startIndex;
        service.getReportString(startIndex, limit, sort, field, language, dataModel, entity, key, source, operationType,
                startDate, endDate, isStrict, new SessionAwareAsyncCallback<String>() {

            public void onSuccess(String result) {
                parseResult(result);
            }
        });
    }

    public native void parseResult(String result)/*-{
        if (result != null){
            var obj = result.split("@||@");
            jsonData = eval('(' + obj[0] + ')');
            if (this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::isInit) {
                this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::isInit = false;
                this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::renderTimeline(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(jsonData,obj[1]);
            } else {
                if (!this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventCache[this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::start]) {
                    this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventSource.loadJSON(jsonData, document.location.href);
                }
                this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventCache[this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::start] = true;
            }
        }
    }-*/;

    public native void renderTimeline(JavaScriptObject jsonData, String dateFormat)/*-{
        var timelineDiv = $doc.getElementById("journalTimeLine");
        if (timelineDiv == null){
            return 
        }       
                                                                                   
        var theme1 = $wnd.Timeline.ClassicTheme.create();
        theme1.autoWidth = true;
        theme1.timeline_start = new Date(Date.UTC(1950, 0, 1));
        theme1.timeline_stop = new Date(Date.UTC(2020, 0, 1));  
        var theme2 = $wnd.Timeline.ClassicTheme.create();
        theme2.event.tape.height = 6;
        theme2.event.track.height = theme2.event.tape.height + 10;
        var d = $wnd.Timeline.DateTime.parseGregorianDateTime(dateFormat)
                                                                                   
        var bandInfos = [ $wnd.Timeline.createBandInfo({
            width : this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::getTimeLinePanelHeight()() * 0.7,          
            intervalUnit : $wnd.Timeline.DateTime.HOUR,
            intervalPixels : 240,
            eventSource:    this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventSource,
            date:           d,
            theme:          theme1,   
            layout:         'original' 
        }), $wnd.Timeline.createBandInfo({
            width : this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::getTimeLinePanelHeight()() * 0.2,         
            intervalUnit : $wnd.Timeline.DateTime.DAY,
            intervalPixels : 150,
            eventSource:    this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventSource,
            date:           d,
            theme:          theme2,
            layout:         'overview'
        }), $wnd.Timeline.createBandInfo({
            width : this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::getTimeLinePanelHeight()() * 0.1,         
            intervalUnit : $wnd.Timeline.DateTime.YEAR,
            intervalPixels : 110,
            eventSource:    this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventSource,
            date:           d,
            theme:          theme2,
            layout:         'overview',
            syncWith : 0,
            highlight : true
        }) ];
           bandInfos[1].syncWith = 0;
           bandInfos[2].syncWith = 0;
           bandInfos[1].highlight = true;
           bandInfos[2].highlight = true;
                                                                                   
           var timeLine = $wnd.Timeline.create(timelineDiv, bandInfos,
           $wnd.Timeline.HORIZONTAL);
           this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventSource.loadJSON(jsonData, $doc.location.href);
           var currentInstance = this;
           timeLine.getBand(1).addOnScrollListener(function(band) {
           currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::getTimeRange(Lcom/google/gwt/core/client/JavaScriptObject;)(band);
        });

           timeLine.getBand(2).addOnScrollListener(function(band) {  
           currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::getTimeRange(Lcom/google/gwt/core/client/JavaScriptObject;)(band);       
        });                                                                                                                            
                                                                                   
    }-*/;

    public native void getTimeRange(JavaScriptObject band)/*-{

        var currentInstance = this;                                                          
        if (currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::timeOutId) {
            $wnd.clearTimeout(currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::timeOutId);
        }

        currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::timeOutId = $wnd.setTimeout(function() {
            var startTime = band.getMinVisibleDate();
            var endTime = band.getMaxVisibleDate();
            var centerVisibleDate = band.getCenterVisibleDate();
            if (centerVisibleDate && currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::lastCenterVisibleDate) {
                var startIndex = null;

                if (centerVisibleDate.getTime() - currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::lastCenterVisibleDate.getTime() > 0) {
                    startIndex = currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::start + currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::limit;
                } else if (centerVisibleDate.getTime() - currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::lastCenterVisibleDate.getTime() < 0) {
                    startIndex = currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::start - currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::limit;
                }
                if (startIndex != null && startIndex >= 0) {
                    currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::loadDate(I)(startIndex);
                }
            }
            currentInstance.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::lastCenterVisibleDate = centerVisibleDate;
        }, 250);
    }-*/;

    public native void loadDate(int startIndex)/*-{
        if (this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventCache) {
            if (this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::eventCache[startIndex]) {
                return;
            }
            this.@org.talend.mdm.webapp.journal.client.widget.JournalTimelinePanel::loadTimeline(I)(startIndex);
        }
    }-*/;

    public int getTimeLinePanelHeight() {
        return this.timeLinePanelHeight;
    }

    public void setTimeLinePanelHeight(int timeLinePanelHeight) {
        this.timeLinePanelHeight = timeLinePanelHeight;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
