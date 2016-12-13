/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.api;

import com.amalto.core.objects.customform.CustomFormPOJO;
import com.amalto.core.objects.customform.CustomFormPOJOPK;
import com.amalto.core.util.XtentisException;

import java.util.Collection;

/**
 *
 */
public interface CustomForm {
    CustomFormPOJOPK putCustomForm(CustomFormPOJO customForm) throws XtentisException;

    CustomFormPOJO getCustomForm(CustomFormPOJOPK pk) throws XtentisException;

    CustomFormPOJO getUserCustomForm(CustomFormPOJOPK cpk) throws XtentisException;

    CustomFormPOJO existsCustomForm(CustomFormPOJOPK pk) throws XtentisException;

    CustomFormPOJOPK removeCustomForm(CustomFormPOJOPK pk) throws XtentisException;

    Collection<CustomFormPOJOPK> getCustomFormPKs(String regex) throws XtentisException;
}
