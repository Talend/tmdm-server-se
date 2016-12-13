/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.util;

import org.talend.mdm.webapp.journal.client.Journal;
import org.talend.mdm.webapp.journal.client.widget.JournalGridPanel;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;

import com.extjs.gxt.ui.client.Registry;

/**
 * DOC Administrator  class global comment. Detailled comment
 */
public class TimelineUtil {
    
    public static String TIMELIME_INIT = "init"; //$NON-NLS-1$
    
    public static String TIMELIME_LOAD = "load"; //$NON-NLS-1$
    
    public native static void regLoadDate()/*-{
        $wnd.loadDate = function(start, limit){
            $wnd.startIndex = start;
            if ($wnd.eventCache){
                if ($wnd.eventCache[$wnd.startIndex]){
                    return;
                }
            }
            var type = "load";
            @org.talend.mdm.webapp.journal.client.util.TimelineUtil::loadTimeline(Ljava/lang/String;)(type);
        }
    }-*/;

    public native static void regShowDialog()/*-{
        $wnd.showDialog = function(modelStr){
            $wnd.closeTimelineBubble();
            @org.talend.mdm.webapp.journal.client.util.TimelineUtil::openTabPanel(Ljava/lang/String;)(modelStr);
        }
    }-*/;
    
    public static void openTabPanel(String modelStr){
        String[] configArr = modelStr.split(","); //$NON-NLS-1$
        JournalGridModel model = new JournalGridModel();
        model.setIds(configArr[0]);
        model.setDataContainer(configArr[1]);
        model.setDataModel(configArr[2]);
        model.setEntity(configArr[3]);
        model.setKey(configArr[4]);
        model.setOperationType(configArr[5]);
        model.setOperationTime(configArr[6]);
        model.setSource(configArr[7]);
        model.setUserName(configArr[8]);
        JournalGridPanel.getInstance().openTabPanel(model);
    }
        
    public static void loadTimeline(String type) {
        JournalSearchCriteria criteria = Registry.get(Journal.SEARCH_CRITERIA);
        String dataModel = criteria.getDataModel() == null ? "" : criteria.getDataModel(); //$NON-NLS-1$
        String entity = criteria.getEntity() == null ? "" : criteria.getEntity(); //$NON-NLS-1$
        String key = criteria.getKey() == null ? "" : criteria.getKey(); //$NON-NLS-1$
        String source = criteria.getSource() == null ? "" : criteria.getSource(); //$NON-NLS-1$
        String operationType = criteria.getOperationType() == null ? "" : criteria.getOperationType(); //$NON-NLS-1$
        String startDate = criteria.getStartDate() == null ? "" : String.valueOf(criteria.getStartDate().getTime()); //$NON-NLS-1$
        String endDate = criteria.getEndDate() == null ? "" : String.valueOf(criteria.getEndDate().getTime()); //$NON-NLS-1$
        if (type.equalsIgnoreCase(TimelineUtil.TIMELIME_INIT))
            loadTimelineInit(dataModel, entity, key, source, operationType, startDate, endDate, criteria.isStrict());
        else if (type.equalsIgnoreCase(TimelineUtil.TIMELIME_LOAD))
            loadTimelineLoad(dataModel, entity, key, source, operationType, startDate, endDate, criteria.isStrict());
    }
    
    public native static void loadTimelineInit(String dataModel, String entity, String key, String source, String operationType,
            String startDate, String endDate, boolean isStrict)/*-{
        $wnd.JournalInterface.getReportString($wnd.startIndex + $wnd.configStr, dataModel, entity, key, source, operationType,
                startDate, endDate, isStrict, $wnd.journalCallback);
    }-*/;
    
    public native static void loadTimelineLoad(String dataModel, String entity, String key, String source, String operationType,
            String startDate, String endDate, boolean isStrict)/*-{
        $wnd.JournalInterface.getReportString($wnd.startIndex + $wnd.configStr, dataModel, entity, key, source, operationType,
                startDate, endDate, isStrict, $wnd.journalLoadDateCallback);
    }-*/;

    public native static void setSearchStart(int searchStart)/*-{
        $wnd.searchStart = searchStart;
    }-*/;
    
    public native static void setTimelinePanelActive(boolean isActive)/*-{
        $wnd.timelinePanelActive = isActive;
    }-*/;
    
    public native static void setTimeLinePanelHeight(int height)/*-{
        $wnd.timeLinePanelHeight = height;
    }-*/;
    
    public native static void setPageSize(int pageSize)/*-{
        $wnd.pageSize = pageSize;
    }-*/;
    
    public native static void setConfigStr(String configStr)/*-{
        $wnd.configStr = configStr;
    }-*/;
    
    public native static void setEnterprise(boolean isEnterprise)/*-{
        $wnd.isEnterprise = isEnterprise;
    }-*/;
    
    public native static void setStartIndex(int startIndex)/*-{
        $wnd.startIndex = startIndex;
    }-*/;    
}