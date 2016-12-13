/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.validator;

import java.math.BigInteger;

import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.shared.FacetEnum;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;


public class NumberFieldValidator implements Validator {

    private static NumberFieldValidator instance;

    public static NumberFieldValidator getInstance(){
        if (instance == null){
            instance = new NumberFieldValidator();
        }
        return instance;
    }
    
    private NumberFieldValidator(){
    }
    
    public String validate(Field<?> field, String value) {
        String defaultMessage = "";//$NON-NLS-1$ 
        boolean succeed = true;

        String[] digits = value.replace(",", ".").replace("-", "").split("\\.");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$

        if (digits[0].equals("")) //$NON-NLS-1$
            digits[0] = "0"; //$NON-NLS-1$

        int totalLength;
        if (digits.length == 2) {
            totalLength = new Integer(new BigInteger(digits[0]).intValue() == 0 ? 0 : digits[0].length()) + digits[1].length();
        } else {
            totalLength = value.length();
        }

        String totalDigits = field.getData(FacetEnum.TOTAL_DIGITS.getFacetName());
        if (totalDigits != null && !totalDigits.equals("")) {//$NON-NLS-1$
            if (totalLength > Integer.parseInt(totalDigits)) {
                succeed = false;
                defaultMessage += MessagesFactory.getMessages().check_totalDigits() + totalDigits + "\n";//$NON-NLS-1$
            }
        }

        String fractionDigits = field.getData(FacetEnum.FRACTION_DIGITS.getFacetName());
        if (fractionDigits != null && !fractionDigits.equals("")) {//$NON-NLS-1$
            if (digits.length == 2 && digits[1].length() > Integer.parseInt(fractionDigits)) {
                succeed = false;
                defaultMessage += MessagesFactory.getMessages().check_fractionDigits() + fractionDigits + "\n";//$NON-NLS-1$
            }
        }
        String minInclusive = field.getData(FacetEnum.MIN_INCLUSIVE.getFacetName());
        if (minInclusive != null && !minInclusive.equals("")){//$NON-NLS-1$
            double min = Double.parseDouble(minInclusive);
            double numberValue = Double.parseDouble(value);
            if (numberValue < min){
                succeed = false;
                defaultMessage += MessagesFactory.getMessages().check_minInclusive() + min + "\n";//$NON-NLS-1$
            }
        }
        
        String maxInclusive = field.getData(FacetEnum.MAX_INCLUSIVE.getFacetName());
        if (maxInclusive != null && !maxInclusive.equals("")){//$NON-NLS-1$
            double max = Double.parseDouble(maxInclusive);
            double numberValue = Double.parseDouble(value);
            if (numberValue > max){
                succeed = false;
                defaultMessage += MessagesFactory.getMessages().check_maxInclusive() + max + "\n";//$NON-NLS-1$
            }
        }
        
        String minExclusive = field.getData(FacetEnum.MIN_EXCLUSIVE.getFacetName());
        if (minExclusive != null && !minExclusive.equals("")){//$NON-NLS-1$
            double min = Double.parseDouble(minExclusive);
            double numberValue = Double.parseDouble(value);
            if (numberValue <= min){
                succeed = false;
                defaultMessage += MessagesFactory.getMessages().check_minExclusive() + min + "\n";//$NON-NLS-1$
            }
        }
        
        String maxExclusive = field.getData(FacetEnum.MAX_EXCLUSIVE.getFacetName());
        if (maxExclusive != null && !maxExclusive.equals("")){//$NON-NLS-1$
            double max = Double.parseDouble(maxExclusive);
            double numberValue = Double.parseDouble(value);
            if (numberValue >= max){
                succeed = false;
                defaultMessage += MessagesFactory.getMessages().check_maxExclusive() + max + "\n";//$NON-NLS-1$
            }
        }
                
        if (!succeed){
            String error = field.getData("facetErrorMsgs");//$NON-NLS-1$
            if (error == null || error.equals("")){//$NON-NLS-1$
                return defaultMessage;
            } 
            return error;
        }
        return null;
    }

}
