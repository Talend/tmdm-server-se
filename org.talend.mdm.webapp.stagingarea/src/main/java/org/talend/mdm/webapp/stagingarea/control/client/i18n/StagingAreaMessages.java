/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.client.i18n;

import com.google.gwt.i18n.client.Messages;

public interface StagingAreaMessages extends Messages {

    String stagingarea_title();

    String status();

    String current_validation();

    String previous_validation();

    String data_container();

    String data_model();

    String refresh();

    String valid();

    String invalid();

    String waiting();

    String nodata();

    String staging_area_title();

    String total_desc(String total);

    String waiting_desc(String waiting);

    String invalid_desc(String prefix, String invalid, String postfix);

    String open_invalid_record();

    String valid_desc(String prefix, String valid, String postfix);

    String start_validation();

    String with_filtering();

    String no_validation();

    String auto_refresh();

    String record_to_process();

    String invalid_record();

    String eta();

    String percentage(int process, int total, double percentage);

    String display_before();

    String search();

    String start_date();

    String end_date();

    String process_records();

    String invalid_records();

    String total_record();

    String on();

    String off();

    String cancel();

    String please_confirm();

    String confirm_message();

    String loading();

    String entity_selector();

    String change_concepts();

    String delete_all_concept();

    String reset_concept();

    String entity_filter();

    String entity_filter_title();

    String status_filter();

    String default_option();

    String selected_statuses();

    String datetime_filter();

    String entity();

    String dep_fk();

    String all();

    String status_notice();

    String ok();

    String status_code();

    String today();

    String yesterday();

    String last_week();

    String last_month();

    String customizing();

    String to();

    String validation_in_progress();
}
