/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.creator;

import org.talend.mdm.webapp.base.client.widget.MultiLanguageField;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.BooleanField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatDateField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor.BooleanFieldCellEditor;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor.ComboBoxCellEditor;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor.DateFieldCellEditor;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor.DateTimeFieldCellEditor;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor.FKCellEditor;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor.MultiLanguageCellEditor;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor.NumberFieldCellEditor;

import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.google.gwt.user.client.ui.Widget;

public class CellEditorCreator {

    public static CellEditor createCellEditor(Widget field) {
        if (field instanceof SimpleComboBox) {
            if (field instanceof BooleanField) {
                return new BooleanFieldCellEditor((BooleanField) field);
            } else {
                return new ComboBoxCellEditor((SimpleComboBox) field);
            }
        }

        if (field instanceof FormatDateField) {
            FormatDateField formatDateField = (FormatDateField) field;
            if (formatDateField.isDateTime()) {
                return new DateTimeFieldCellEditor((DateField) field);
            } else {
                return new DateFieldCellEditor((FormatDateField) field);
            }
        }

        if (field instanceof NumberField) {
            return new NumberFieldCellEditor((NumberField) field);
        }

        if (field instanceof ForeignKeyField) {
            return new FKCellEditor((ForeignKeyField) field);
        }

        if (field instanceof MultiLanguageField) {
            return new MultiLanguageCellEditor((MultiLanguageField) field);
        }

        if (field instanceof Field) {
            return new CellEditor((Field<? extends Object>) field);
        }

        return null;

    }
}
