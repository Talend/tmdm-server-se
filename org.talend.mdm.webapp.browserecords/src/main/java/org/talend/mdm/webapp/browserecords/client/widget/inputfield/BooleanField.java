/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield;

import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;


public class BooleanField extends SimpleComboBox<Boolean> {

    public BooleanField(){
        super();
        this.setEnabled(false);
        this.setForceSelection(true);
        this.setTriggerAction(TriggerAction.ALL);
    }
}
