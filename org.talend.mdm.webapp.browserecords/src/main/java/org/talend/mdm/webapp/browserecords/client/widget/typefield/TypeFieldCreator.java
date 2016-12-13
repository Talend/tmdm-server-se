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

import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataType;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.FacetModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.shared.FacetEnum;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Widget;



/**
 * author hshu
 * TODO refactor me to web-base
 */
public class TypeFieldCreator implements IsSerializable {

    private TypeFieldSource source;

    private TypeFieldCreateContext context;

    /**
     * DOC Administrator TypeFieldCreator constructor comment.
     */
    public TypeFieldCreator() {

    }

    public TypeFieldCreator(TypeFieldSource source, TypeFieldCreateContext context) {
        super();
        this.source = source;
        this.context = context;
    }

    public Field<?> createFieldWithValueAndUpdateStyle(ItemNodeModel node, Map<String, TypeFieldStyle> sytles) {

        this.context.setWithValue(true);
        this.context.setNode(node);
        this.context.setUpdateStyle(true);
        this.context.setTypeFieldStyles(sytles);

        return createField();
    }

    public Field<?> createFieldWithUpdateStyle(Map<String, TypeFieldStyle> sytles) {
        this.context.setUpdateStyle(true);
        this.context.setTypeFieldStyles(sytles);
        return createField();
    }

    public Field<?> createFieldWithValue(ItemNodeModel node) {
        this.context.setWithValue(true);
        this.context.setNode(node);
        return createField();
    }

    public Field<?> createField() {

        if (context == null || context.getDataType() == null || source == null)
            throw new IllegalArgumentException();

        if (context.getDataType().getType() == null)
            throw new IllegalArgumentException("Data Type Object is null! "); //$NON-NLS-1$

        Field<?> field;
        TypeFieldFactory fieldFactory;
        DataType dataType = context.getDataType().getType();
        String baseType = context.getDataType().getType().getBaseTypeName();

        if (dataType.equals(DataTypeConstants.UUID) || dataType.equals(DataTypeConstants.AUTO_INCREMENT)
                || dataType.equals(DataTypeConstants.PICTURE) || dataType.equals(DataTypeConstants.URL)
                || dataType.equals(DataTypeConstants.MLS)) {

            fieldFactory = new CustomTypeFieldFactory(source, context);

        } else if (DataTypeConstants.STRING.getTypeName().equals(baseType)
               || DataTypeConstants.NORMALIZEDSTRING.getTypeName().equals(baseType)
               || DataTypeConstants.LANGUAGE.getTypeName().equals(baseType)
               || DataTypeConstants.TOKEN.getTypeName().equals(baseType)
               || DataTypeConstants.NMTOKEN.getTypeName().equals(baseType)
               || DataTypeConstants.NMTOKENS.getTypeName().equals(baseType)
               || DataTypeConstants.NAME.getTypeName().equals(baseType)
               || DataTypeConstants.NCNAME.getTypeName().equals(baseType)
               || DataTypeConstants.ID.getTypeName().equals(baseType)
               || DataTypeConstants.IDREF.getTypeName().equals(baseType)
               || DataTypeConstants.IDREFS.getTypeName().equals(baseType)
               || DataTypeConstants.ENTITY.getTypeName().equals(baseType)
               || DataTypeConstants.ENTITIES.getTypeName().equals(baseType)
               || DataTypeConstants.ANYURI.getTypeName().equals(baseType)
               || DataTypeConstants.QNAME.getTypeName().equals(baseType)
               || DataTypeConstants.NOTATION.getTypeName().equals(baseType)) {

            fieldFactory = new TextTypeFieldFactory(source, context);
        
        } else if (DataTypeConstants.INTEGER.getTypeName().equals(baseType)
                || DataTypeConstants.NONPOSITIVEINTEGER.getTypeName().equals(baseType)
                || DataTypeConstants.NEGATIVEINTEGER.getTypeName().equals(baseType)
                || DataTypeConstants.NONNEGATIVEINTEGER.getTypeName().equals(baseType)
                || DataTypeConstants.LONG.getTypeName().equals(baseType)
                || DataTypeConstants.INT.getTypeName().equals(baseType)
                || DataTypeConstants.SHORT.getTypeName().equals(baseType)
                || DataTypeConstants.UNSIGNEDLONG.getTypeName().equals(baseType)
                || DataTypeConstants.UNSIGNEDINT.getTypeName().equals(baseType)
                || DataTypeConstants.UNSIGNEDSHORT.getTypeName().equals(baseType)
                || DataTypeConstants.POSITIVEINTEGER.getTypeName().equals(baseType)
                || DataTypeConstants.BYTE.getTypeName().equals(baseType)
                || DataTypeConstants.UNSIGNEDBYTE.getTypeName().equals(baseType)
                || DataTypeConstants.DECIMAL.getTypeName().equals(baseType)
                || DataTypeConstants.FLOAT.getTypeName().equals(baseType)
                || DataTypeConstants.DOUBLE.getTypeName().equals(baseType)) {

            fieldFactory = new NumberTypeFieldFactory(source, context);

        } else if (DataTypeConstants.DURATION.getTypeName().equals(baseType)
                || DataTypeConstants.DATETIME.getTypeName().equals(baseType)
                || DataTypeConstants.TIME.getTypeName().equals(baseType)
                || DataTypeConstants.DATE.getTypeName().equals(baseType)
                || DataTypeConstants.GYEARMONTH.getTypeName().equals(baseType)
                || DataTypeConstants.GYEAR.getTypeName().equals(baseType)
                || DataTypeConstants.GDAY.getTypeName().equals(baseType)
                || DataTypeConstants.GMONTH.getTypeName().equals(baseType)) {

            fieldFactory = new DateTimeTypeFieldFactory(source, context);

        } else if (DataTypeConstants.BOOLEAN.getTypeName().equals(baseType)
                || DataTypeConstants.HEXBINARY.getTypeName().equals(baseType)
                || DataTypeConstants.BASE64BINARY.getTypeName().equals(baseType)) {

            fieldFactory = new MiscTypeFieldFactory(source, context);

        } else {

            fieldFactory = new DefaultTypeFieldFactory(source, context);

        }

        // TODO add SpecialTypeFieldFactory for FK, enumeration & MultipleField & polymorphism type

        if (source != null && source.getName().equals(TypeFieldSource.SEARCH_EDITOR))
            field = fieldFactory.createSearchField();
        else
            field = fieldFactory.createField();

        if (field != null) {

            updateFieldAttributes(field, context.getDataType(), context.getLanguage());

            if (context.isUpdateStyle())
                fieldFactory.updateStyle(field);
        }

        return field;
    }

    /**
     * DOC Administrator Comment method "updateFieldAttributes".
     * @param field
     * @param dataType
     */
    private void updateFieldAttributes(Field<?> field, TypeModel dataType, String language) {
        // facet set
        if (source != null && source.getName().equals(TypeFieldSource.CELL_EDITOR)) {
            // TODO merge the similar logic from tree detail
            if (dataType.getFacetErrorMsgs() != null && dataType.getFacetErrorMsgs().size() > 0)
                field.setData("facetErrorMsgs", dataType.getFacetErrorMsgs().get(language));//$NON-NLS-1$
            buildFacets(dataType, field);
        }
    }

    private void buildFacets(TypeModel typeModel, Widget w) {
        if (typeModel instanceof SimpleTypeModel && ((SimpleTypeModel) typeModel).getFacets() != null) {
            List<FacetModel> facets = ((SimpleTypeModel) typeModel).getFacets();
            for (FacetModel facet : facets) {
                FacetEnum.setFacetValue(facet.getName(), w, facet.getValue());
            }
        }
    }

}
