/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.talend.mdm.webapp.base.client.util.FormatUtil;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;


public class MultiLanguageModel extends ItemBaseModel {

    private static final long serialVersionUID = 1L;

    private static final String linkedSymbol = ":"; //$NON-NLS-1$

    private String multiLanguageString;

    private String currentLanguageValue;

    private LinkedHashMap<String, String> languageValueMap = new LinkedHashMap<String, String>();

    public MultiLanguageModel(String _multiLanguageString) {
        this.multiLanguageString = _multiLanguageString;
        LinkedHashMap<String, String> temp_languageValueMap = MultilanguageMessageParser
                .getLanguageValueMap(_multiLanguageString);
        for (String language : temp_languageValueMap.keySet()) {
            languageValueMap.put(language.toUpperCase(), FormatUtil.languageValueDecode(temp_languageValueMap.get(language)));
        }
    }

    public String getValueByLanguage(String language) {
        currentLanguageValue = languageValueMap.isEmpty() ? this.multiLanguageString : languageValueMap.get(language);
        return currentLanguageValue = currentLanguageValue != null ? currentLanguageValue : ""; //$NON-NLS-1$
    }

    public void setValueByLanguage(String language, String value) {
        if (value != null && value.trim().length() > 0) {
            languageValueMap.put(language, value);
        } else {
            languageValueMap.remove(language);
        }
        toString();
    }

    public LinkedHashMap<String, String> getLanguageValueMap() {
        return this.languageValueMap;
    }

    public void clear() {
        this.languageValueMap.clear();
        this.multiLanguageString = ""; //$NON-NLS-1$
        this.currentLanguageValue = ""; //$NON-NLS-1$
    }

    public String toString() {
        if (languageValueMap == null || languageValueMap.size() == 0) {
            this.multiLanguageString = ""; //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        List<String> keys = new ArrayList<String>(languageValueMap.keySet());
        Collections.sort(keys);
        StringBuffer sb = new StringBuffer();
        for (String language : keys) {
            sb.append("[" + language + linkedSymbol + FormatUtil.languageValueEncode(languageValueMap.get(language)) + "]"); //$NON-NLS-1$//$NON-NLS-2$
        }
        multiLanguageString = sb.toString();
        return multiLanguageString;
    }

}