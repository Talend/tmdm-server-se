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

import com.extjs.gxt.ui.client.widget.form.Field;

public class TextTypeFieldFactory extends TypeFieldFactory {

    public TextTypeFieldFactory() {

    }

    public TextTypeFieldFactory(TypeFieldSource source, TypeFieldCreateContext context) {
        super(source, context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.base.client.widget.typefield.TypeFieldFactory#createField()
     */
    @Override
    public Field<?> createField() {
        return genFormatTextField();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldFactory#createSearchField()
     */
    @Override
    public Field<?> createSearchField() {
        return genTextSearchField();
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
