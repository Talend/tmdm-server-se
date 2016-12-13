/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.i18n;

import com.google.gwt.i18n.client.Messages;

public interface BaseMessages extends Messages {

    String exception_parse_illegalChar(int beginIndex);

    String exception_parse_unknownOperator(String value);

    String exception_parse_missEndBlock(char endBlock, int i);

    String exception_parse_tooManyEndBlock(char endBlock, int i);

    String page_size_label();

    String page_size_notice();

    String info_title();

    String error_title();

    String warning_title();

    String confirm_title();

    String unknown_error();

    String session_timeout_error();

    String open_mls_title();

    String language_title();

    String value_title();

    String multiLanguage_edit_failure();

    String multiLangauge_language_duplicate();

    String edit_success_info();

    String message_success();

    String message_error();

    String message_fail();

    String edititem();

    String add_btn();

    String remove_btn();

    String save_more_btn();

    String exception_fk_malform(String fk);

    String overwrite_confirm();

    String label_exception_id_malform(String id);

    String server_error();

    String server_error_notification();

    String service_rest_error();

    String service_rest_exception();

    String underlying_cause();

    String matching_failed(String concept);

    String delete_success_prefix();

    String delete_fail_prefix();

    String restore_success_prefix();

    String restore_fail_prefix();
}
