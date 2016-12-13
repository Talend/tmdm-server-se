/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server.i18n;

import java.util.Locale;

import org.talend.mdm.webapp.base.client.i18n.BaseMessages;

import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;

@SuppressWarnings("nls")
public final class BaseMessagesImpl implements BaseMessages {

    private static final Messages MESSAGES = MessagesFactory.getMessages("org.talend.mdm.webapp.base.client.i18n.BaseMessages",
            BaseMessagesImpl.class.getClassLoader());

    @Override
    public String exception_parse_illegalChar(int beginIndex) {
        return MESSAGES.getMessage("exception_parse_illegalChar", beginIndex);
    }

    @Override
    public String exception_parse_unknownOperator(String value) {
        return MESSAGES.getMessage("exception_parse_unknownOperator", value);
    }

    @Override
    public String exception_parse_missEndBlock(char endBlock, int i) {
        return MESSAGES.getMessage("exception_parse_missEndBlock", endBlock, i);
    }

    @Override
    public String exception_parse_tooManyEndBlock(char endBlock, int i) {
        return MESSAGES.getMessage("exception_parse_tooManyEndBlock", endBlock, i);
    }

    @Override
    public String page_size_label() {
        return MESSAGES.getMessage("page_size_label");
    }

    @Override
    public String page_size_notice() {
        return MESSAGES.getMessage("page_size_notice");
    }

    @Override
    public String info_title() {
        return MESSAGES.getMessage("info_title");
    }

    @Override
    public String error_title() {
        return MESSAGES.getMessage("error_title");
    }

    @Override
    public String warning_title() {
        return MESSAGES.getMessage("warning_title");
    }

    @Override
    public String confirm_title() {
        return MESSAGES.getMessage("confirm_title");
    }

    @Override
    public String unknown_error() {
        return MESSAGES.getMessage("unknown_error");
    }

    @Override
    public String session_timeout_error() {
        return MESSAGES.getMessage("session_timeout_error");
    }

    public String typemode_notfound_error(String typePath, String language) {
        if (typePath == null) {
            typePath = "";
        }
        if (language != null) {
            return MESSAGES.getMessage(new Locale(language), "typemode_notfound_error", typePath);
        } else {
            return MESSAGES.getMessage("typemode_notfound_error", typePath);
        }
    }

    public String typemode_notfound_error(String typePath) {
        return typemode_notfound_error(typePath, null);
    }

    @Override
    public String open_mls_title() {
        return MESSAGES.getMessage("open_mls_title");
    }

    @Override
    public String language_title() {
        return MESSAGES.getMessage("language_title");
    }

    @Override
    public String value_title() {
        return MESSAGES.getMessage("value_title");
    }

    @Override
    public String multiLanguage_edit_failure() {
        return MESSAGES.getMessage("multiLanguage_edit_failure");
    }

    @Override
    public String multiLangauge_language_duplicate() {
        return MESSAGES.getMessage("multiLangauge_language_duplicate");
    }

    @Override
    public String edit_success_info() {
        return MESSAGES.getMessage("edit_success_info");
    }

    @Override
    public String message_success() {
        return MESSAGES.getMessage("message_success");
    }

    @Override
    public String message_fail() {
        return MESSAGES.getMessage("message_fail");
    }

    @Override
    public String message_error() {
        return MESSAGES.getMessage("message_error");
    }

    @Override
    public String edititem() {
        return MESSAGES.getMessage("edititem");
    }

    @Override
    public String save_more_btn() {
        return MESSAGES.getMessage("save_more_btn");
    }

    @Override
    public String add_btn() {
        return MESSAGES.getMessage("add_btn");
    }

    @Override
    public String remove_btn() {
        return MESSAGES.getMessage("remove_btn");
    }

    @Override
    public String exception_fk_malform(String fk) {
        return MESSAGES.getMessage("exception_fk_malform", fk);
    }

    @Override
    public String overwrite_confirm() {
        return MESSAGES.getMessage("overwrite_confirm");
    }

    @Override
    public String label_exception_id_malform(String id) {
        return MESSAGES.getMessage("label_exception_id_malform");
    }

    @Override
    public String server_error() {
        return MESSAGES.getMessage("server_error");
    }

    @Override
    public String server_error_notification() {
        return MESSAGES.getMessage("server_error_notification");
    }

    @Override
    public String service_rest_error() {
        return MESSAGES.getMessage("service_rest_error");
    }

    @Override
    public String service_rest_exception() {
        return MESSAGES.getMessage("service_rest_exception");
    }

    @Override
    public String underlying_cause() {
        return MESSAGES.getMessage("underlying_cause");
    }

    @Override
    public String matching_failed(String concept) {
        return MESSAGES.getMessage("matching_failed");
    }

    @Override
    public String delete_success_prefix() {
        return MESSAGES.getMessage("delete_success_prefix");
    }

    @Override
    public String delete_fail_prefix() {
        return MESSAGES.getMessage("delete_fail_prefix");
    }

    @Override
    public String restore_success_prefix() {
        return MESSAGES.getMessage("restore_success_prefix");
    }

    @Override
    public String restore_fail_prefix() {
        return MESSAGES.getMessage("restore_fail_prefix");
    }
}
