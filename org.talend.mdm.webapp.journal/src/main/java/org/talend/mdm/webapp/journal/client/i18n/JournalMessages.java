/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.i18n;

import com.google.gwt.i18n.client.Messages;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public interface JournalMessages extends Messages {

    public String journal_title();
    
    public String info_title();

    public String search_panel_title();

    public String entity_label();

    public String key_label();

    public String source_label();

    public String operation_type_label();

    public String start_date_label();

    public String end_date_label();

    public String reset_button();

    public String search_button();

    public String exprot_excel_button();

    public String results_tab();

    public String timeline_tab();

    public String data_container_label();

    public String data_model_label();

    public String operation_time_label();

    public String user_name_label();

    public String update_report_detail_label();

    public String open_record_button();

    public String before_label();

    public String after_label();

    public String restore_button();

    public String data_change_viewer();

    public String journal_label();

    public String error_level();

    public String select_contain_model_msg();

    public String view_updatereport_button();
    
    public String prev_updatereport_button();
    
    public String next_updatereport_button();
    
    public String no_prev_report_msg();
    
    public String no_next_report_msg();
    
    public String updatereport_label();

    public String change_properties();

    public String previous_change_button();

    public String next_change_button();

    public String warning_title();

    public String search_date_error_message();
    
    public String restore_success();
    
    public String restore_fail();
    
    public String restore_confirm();
    
    public String menu_item_viewchages();
    
    public String strict_search_checkbox();
    
    public String restore_logic_delete_fail();
        
    public String restore_not_support(String operationType);
        
    public String restore_update_fail();
    
    public String action_not_supported(String action);
}
