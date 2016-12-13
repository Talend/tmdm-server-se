/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.converter;

import org.talend.mdm.webapp.base.client.model.MultiLanguageModel;
import org.talend.mdm.webapp.browserecords.client.util.Locale;

import com.extjs.gxt.ui.client.binding.Converter;

public class MultiLanguageConverter extends Converter {

    private MultiLanguageModel multiLanguageModel;

    private String language = Locale.getLanguage();

    public Object convertModelValue(Object value) {
        multiLanguageModel.setValueByLanguage(language, value != null ? value.toString() : ""); //$NON-NLS-1$
        return multiLanguageModel.toString();
    }

    public Object convertFieldValue(Object value) {
        multiLanguageModel = new MultiLanguageModel(value != null ? value.toString() : ""); //$NON-NLS-1$
        return multiLanguageModel.getValueByLanguage(Locale.getLanguage().toUpperCase());
    }

}