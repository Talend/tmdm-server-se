/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.i18n;

import com.google.gwt.i18n.client.Messages;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public interface BrowseRecordsMessages extends Messages {

    String info_title();

    String error_title();

    String confirm_title();

    String confirm_delete_img();

    String warning_title();

    String cancel_btn();

    String ok_btn();

    String create_btn();

    String create(String thing);

    String delete_btn();

    String moreActions_btn();

    String moreActions_tip();

    String duplicate_btn();

    String duplicate_tip();

    String journal_btn();

    String journal_tip();

    String navigator_btn();

    String navigator_tip();

    String refresh_tip();

    String refresh_error();

    String button_reset();

    String save_btn();

    String save_tip();

    String button_upload();

    String itemsBrowser_Import_Export();

    String import_btn();

    String export_btn();

    String bulkUpdate_btn();

    String bulkUpdate_title();

    String bulkUpdate_confirm();

    String bulkUpdate_success();

    String bulkUpdate_progress_bar_title();

    String bulkUpdate_progress_bar_message();

    String bulkUpdate_foreignkey_warning();

    String bulkUpdate_no_edit_info();

    String page_size_field_label();

    String page_size_label();

    String page_size_notice();

    String page_displaying_records();

    String edititem();

    String openitem_tab();

    String grid_record_select();

    String trash_btn();

    String delete_confirm();

    String delete_occurrence_confirm();

    String select_delete_item_record();

    String delete_item_record_failure(int value);

    String delete_item_record_success(int value);

    String delete_item_record_successNoupdate(String value);

    String save_success();

    String save_error();

    String save_fail(String value, String msg);

    String save_failEx(String value);

    String save_validationrule_fail(String value, String msg);

    String empty_entity();

    String loading();

    String search_btn();

    String search_initMsg();

    String advsearch_btn();

    String search_expression();

    String valid_expression();

    String invalid_expression();

    String search_expression_notempty();

    String search_modifiedon();

    String search_modifiedto();

    String match_group();

    String advsearch_filter();

    String advsearch_morelabel();

    String advsearch_subclause();

    String advsearch_lessinfo();

    String advsearch_bookmark();

    String required_field();

    String fk_RelatedRecord();

    String label_select_type();

    String criteria_CONTAINS();

    String criteria_CONTAINS_SENTENCE();

    String criteria_EQUALS();

    String criteria_NOT_EQUALS();

    String criteria_GREATER_THAN();

    String criteria_GREATER_THAN_OR_EQUAL();

    String criteria_LOWER_THAN();

    String criteria_LOWER_THAN_OR_EQUAL();

    String criteria_STARTSWITH();

    String criteria_FULLTEXTSEARCH();

    String criteria_DATEEQUALS();

    String criteria_DATELOWER_THAN();

    String criteria_DATEGREATER_THAN();

    String criteria_BOOLEQUALSTRUE();

    String criteria_BOOLEQUALSFALSE();

    String criteria_EMPTY_NULL();

    String bookmark_heading();

    String bookmarkmanagement_heading();

    String bookmark_name();

    String bookmark_shared();

    String bookmark_edit();

    String bookmark_del();

    String bookmark_update();

    String bookmark_search();

    String bookmark_DelMsg();

    String bookmark_existMsg();

    String bookmark_nameNotBlank();

    String bookmark_saveFailed();

    String bookmark_saveSuccess();

    String bookmark_invalid(String character);

    String check_totalDigits();

    String check_fractionDigits();

    String check_minInclusive();

    String check_maxInclusive();

    String check_minExclusive();

    String check_maxExclusive();

    String check_length();

    String check_minLength();

    String check_maxLength();

    String check_pattern(String value, String pattern);

    String status();

    String multiOccurrence_minimize(int value);

    String multiOccurrence_maximize(int value);

    String multiOccurrence_minimize_title(int minOccurs, String nodeName);

    String clone_title();

    String deepclone_title();

    String remove_title();

    String reset_value_title();

    String picture_select_title();

    String picture_upload_local_title();

    String picture_upload_remote_title();

    String picture_field_label();

    String picture_field_imgcatalog();

    String picture_field_imgid();

    String url_field_title();

    String message_success();

    String message_fail();

    String message_error();

    String data_model_not_specified();

    String delete_process_validation_failure();

    String delete_process_validation_success();

    String delete_record_failure();

    String delete_record_success();

    String delete_item_title();

    String delete_item_progress();

    String save_close_btn();

    String save_close_tip();

    String explain_button();

    String explain_tip();

    String no_taskid_warning_message();

    String compare_button();

    String compare_tip();

    String compare_result_title();

    String compare_choose_one_warning_message();

    String mass_update_choose_warning_message();

    String delete_tip();

    String upload_title();

    String export_title();

    String label_combo_filetype_select();

    String label_field_file();

    String label_field_filetype();

    String label_field_separator();

    String label_field_delimiter();

    String label_field_partial_update();

    String label_field_header_first();

    String label_field_encoding();

    String label_button_submit();

    String error_incompatible_file_type();

    String import_progress_bar_title();

    String import_progress_bar_message();

    String import_progress_bar_laod();

    String launch_process_tooltip();

    String process_failed();

    String process_launched();

    String process_progress_bar_title();

    String process_progress_bar_message();

    String process_select();

    String fk_integrity_fail_open_relations();

    String relations_btn();

    String relations_tooltip();

    String fk_integrity_fail_override();

    String msg_confirm_refresh_tree_detail();

    String msg_confirm_save_tree_detail(String entityName);

    String fk_del_title();

    String fk_select_title();

    String fk_open_title();

    String fk_add_title();

    String personalview_btn();

    String generatedview_btn();

    String smartview_defaultoption();

    String print_btn();

    String fk_integrity_list_partial_delete();

    String fk_info();

    String browse_record_title();

    String auto();

    String validation_error(String value);

    String message_validate_min_occurence(int value);

    String message_validate_max_occurence(int value);

    String open_task();

    String open_task_tooltip();

    String add_btn();

    String remove_btn();

    String edit_btn();

    String abstract_type();

    String fk_validate_min_occurence(String name, int value);

    String fk_validate_max_occurence(String name, int value);

    String import_success_label();

    String import_items_greater_than_default(String defaultNumber);

    String export_items_greater_than_defalut(String defaultNumber);

    String fk_edit_failure();

    String fk_save_validate(String name, int value);

    String entity_display_name();

    String save_success_but_exist_exception(String name, String message);

    String no_change_info();

    String upload_pic_ok();

    String upload_pic_fail();

    String save_concurrent_fail();

    String please_wait();

    String save_progress_bar_title();

    String save_progress_bar_message();

    String browse_title();

    String hierarchy_title();

    String search_result();

    String search_field_error_title();

    String search_field_error_info(String value);

    String msg_confirm_close_tab(String entityName);

    String fkinfo_display_label();

    String fkinfo_display_type_label();

    String error();

    String error_column_header(String columnName, String concept);

    String entity_no_access();

    String rendering_title();

    String render_message();

    String rendering_progress();

    String load_title();

    String load_message();

    String load_progress();

    String invalid_data(String name);

    String of_word();

    String display_items();

    String record_exists();

    String criteria_AND();

    String criteria_OR();

    String date_format_error();

    String output_report_null();

    String export_error();

    String missing_attribute(String path);

    String multiple_value_separator_field_label();

    String masterRecords_btn();

    String stagingRecords_btn();

    String stagingRecords_tip();

    String staging_data_viewer_title();

    String staging_hint();

    String source();

    String browse_staging_records();

    String status_000();

    String status_201(String dataCluser);

    String status_202(String dataContainer);

    String status_203(String dataCluser);

    String status_204(String dataCluser);

    String status_205();

    String status_206();

    String status_401(String dataCluser);

    String status_402(String dataCluser);

    String status_403();

    String status_404();

    String status_405();

    String mark_as_deleted();

    String mark_deleted_confirm();

    String no_golden_record_in_group(String groupId);

    String record_not_found_msg();

    String lineage_list_tab_title();

    String lineage_explain_tab_title();

    String explainResult_group_header();

    String explainResult_id_header();

    String explainResult_name_header();

    String explainResult_gid_header();

    String explainResult_gsize_header();

    String explainResult_master_header();

    String explainResult_confidence_header();

    String explainResult_score_header();

    String explainResult_field_score_header();

    String explainResult_details_header();

    String matchdetail_title();

    String foreignkey_filter_warning();

    String find_view_warning(String entity);

    String validate_record_btn();

    String validate_record_tip();

    String validate_record_passed_msg();

    String validate_record_failed_msg(String msg);

    String navigator_panel_label();

    String navigator_detailPanel_label();

    String setting_window_title();
}
