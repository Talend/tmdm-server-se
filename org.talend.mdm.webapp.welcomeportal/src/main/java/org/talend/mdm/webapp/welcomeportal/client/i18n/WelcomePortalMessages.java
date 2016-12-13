/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client.i18n;

import com.google.gwt.i18n.client.Messages;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public interface WelcomePortalMessages extends Messages {

    String welcome_title();

    String welcome_panel();

    String welcome_desc();

    String start_title();

    String start_desc();

    String browse_items();

    String journal();

    String no_license_msg();

    String license_expired_msg();

    String loading_task_msg();

    String loading_alert_msg();

    String useful_links();

    String useful_links_desc();

    String tasks_title();

    String process_title();

    String chart_data_title();

    String chart_journal_title();
    
    String chart_journal_creation();
    
    String chart_journal_update();

    String chart_routing_event_title();
    
    String chart_routing_event_completed();
    
    String chart_routing_event_failed();

    String chart_matching_title();

    String chart_matching_duplicates();
    
    String autorefresh();
    
    String entities();
    
    String timeframe();
    
    String autorefresh_on();

    String autorefresh_off();

    String no_container();

    String alerts_title();

    String no_tasks();

    String no_standalone_process();

    String no_alerts();

    String waiting_task_prefix();

    String waiting_workflowtask_suffix();

    String waiting_dsctask(int newTasks, int pendingTasks);
    
    String waiting_dsctask_suffix();

    String tasks_desc();

    String alerts_desc();

    String process_desc();

    String waiting_msg();

    String waiting_desc();

    String run_status();

    String run_fail();

    String run_launched();

    String search_title();

    String search_button_text();

    String save_portal_config_failed();

    String retrieve_portal_config_failed();

    String chart_config_title();

    String chart_config_top5();

    String chart_config_top10();

    String chart_config_all();

    String chart_config_day();

    String chart_config_week();

    String chart_config_ok();

    String chart_config_cancel();
}
