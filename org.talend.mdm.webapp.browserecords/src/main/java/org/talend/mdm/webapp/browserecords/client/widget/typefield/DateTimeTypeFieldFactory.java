/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.typefield;

import java.util.Date;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.OperatorConstants;
import org.talend.mdm.webapp.browserecords.client.util.DateUtil;
import org.talend.mdm.webapp.browserecords.client.util.LabelUtil;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatDateField;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;


public class DateTimeTypeFieldFactory extends TypeFieldFactory {

    public DateTimeTypeFieldFactory() {

    }

    public DateTimeTypeFieldFactory(TypeFieldSource source, TypeFieldCreateContext context) {
        super(source, context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.base.client.widget.typefield.TypeFieldFactory#createField()
     */
    @Override
    public Field<?> createField() {

        Field<?> field = null;

        if (DataTypeConstants.DATE.getTypeName().equals(baseType)) {
            field = createFormatDateField(false);
        } else if (DataTypeConstants.DATETIME.getTypeName().equals(baseType)) {
            field = createFormatDateField(true);
        } else if (DataTypeConstants.DURATION.getTypeName().equals(baseType)
                || DataTypeConstants.TIME.getTypeName().equals(baseType)
                || DataTypeConstants.GYEARMONTH.getTypeName().equals(baseType)
                || DataTypeConstants.GYEAR.getTypeName().equals(baseType)
                || DataTypeConstants.GDAY.getTypeName().equals(baseType)
                || DataTypeConstants.GMONTH.getTypeName().equals(baseType)) {
            // TODO have a time widget
            field = genFormatTextField();
        }

        return field;
    }

    /**
     * Create a FormatDateField according to isDateTime parameter
     * @param isDateTime
     * @return
     */
    public Field<?> createFormatDateField(boolean isDateTime){
        final FormatDateField dateField = new FormatDateField();
        if (!isEmpty(displayformatPattern)) {               
            dateField.setFormatPattern(displayformatPattern);
        }
        dateField.setDateTime(isDateTime);
        dateField.setPropertyEditor(new DateTimePropertyEditor(isDateTime ? DateUtil.formatDateTimePattern : DateUtil.datePattern));

        if (context.isWithValue() && hasValue()) {
        	try {
        		// It will be better to call dateField.getPropertyEditor().convertStringValue(value);
        		Date d = DateUtil.convertStringToDate(isDateTime ? DateUtil.dateTimePattern : DateUtil.datePattern, getValue().toString());
        		dateField.setValue(hasValue() ? d : null);
        	} catch (Exception e) {
            	String label = context.getNode().getDynamicLabel() != null && context.getNode().getDynamicLabel().trim().length() > 0 ? 
            			context.getNode().getDynamicLabel() : LabelUtil.getNormalLabel(context.getNode().getLabel());
            	DeferredCommand.addCommand(new Command(){
                	public void execute() {
                		dateField.setRawValue(getValue().toString());
                		dateField.validate();
                	}
            	});
            	MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages().invalid_data(label), null);
			}
        }
        return dateField;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldFactory#createSearchField()
     */
    @Override
    public Field<?> createSearchField() {
        Field<?> field = null;
        if (baseType.equals(DataTypeConstants.DATE.getBaseTypeName())) {
            DateField dateField = new DateField();
            dateField.setMessageTarget("tooltip");//$NON-NLS-1$
            dateField.setPropertyEditor(new DateTimePropertyEditor("yyyy-MM-dd"));//$NON-NLS-1$            
            field = dateField;
            source.setOperatorMap(OperatorConstants.dateOperators);
        } else if (baseType.equals(DataTypeConstants.DATETIME.getBaseTypeName())) {
            DateField dateField = new DateField();
            dateField.setPropertyEditor(new DateTimePropertyEditor(DateUtil.formatDateTimePattern));
            field = dateField;
            source.setOperatorMap(OperatorConstants.dateOperators);
        } else {
            field = genTextSearchField();
        }
        return field;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldFactory#updateStyle()
     */
    @Override
    public void updateStyle(Field<?> field) {
        updateBuiltInTypeFiledsStyle(field);
    }


}
