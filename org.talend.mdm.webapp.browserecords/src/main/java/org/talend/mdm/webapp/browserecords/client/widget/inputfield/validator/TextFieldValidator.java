package org.talend.mdm.webapp.browserecords.client.widget.inputfield.validator;

import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.shared.FacetEnum;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;


public class TextFieldValidator implements Validator {

    private static TextFieldValidator instance;
    
    private static String AUTO_INCREMENT = "(Auto)"; //$NON-NLS-1$
    
    private boolean validateFlag = true;
    
    public static TextFieldValidator getInstance(){
        if (instance == null){
            instance = new TextFieldValidator();
        }
        return instance;
    }
    
    private TextFieldValidator(){
        validateFlag = BrowseRecords.getSession().getAppHeader().isAutoValidate();
    }
    
    public String validate(Field<?> field, String value) {
        if(!validateFlag)
            return null;
        String defaultMessage = "";//$NON-NLS-1$
        boolean succeed = true;
        String length = field.getData(FacetEnum.LENGTH.getFacetName());
        if (length != null && !length.equals("")) {//$NON-NLS-1$
            if (value.length() != Integer.parseInt(length)){
                succeed = false;
                defaultMessage += MessagesFactory.getMessages().check_length() + length + "\n";//$NON-NLS-1$
            }
        }
        
        String minLength = field.getData(FacetEnum.MIN_LENGTH.getFacetName());
        if (minLength != null && !minLength.equals("")) {//$NON-NLS-1$
            if (value.length() < Integer.parseInt(minLength)){
                succeed = false;
                defaultMessage += MessagesFactory.getMessages().check_minLength() + minLength + "\n";//$NON-NLS-1$
            }
        }
        
        String maxLength = field.getData(FacetEnum.MAX_LENGTH.getFacetName());
        if (maxLength != null && !maxLength.equals("")) {//$NON-NLS-1$
            if (value.length() > Integer.parseInt(maxLength)){
                succeed = false;
                defaultMessage += MessagesFactory.getMessages().check_maxLength() + maxLength + "\n";//$NON-NLS-1$
            }
        }
        
        String pattern = field.getData(FacetEnum.PATTERN.getFacetName());

        if (pattern != null && !pattern.equals("") && value != null) { //$NON-NLS-1$            
            if (!value.matches(pattern)) {
                succeed = false;
                defaultMessage += MessagesFactory.getMessages().check_pattern(value, pattern) + "\n";//$NON-NLS-1$
            }
        }

        if(!BrowseRecords.getSession().getAppHeader().isAutoValidate())
            validateFlag = false;

        if (!succeed){
            String error = field.getData("facetErrorMsgs");//$NON-NLS-1$
            if (error == null || error.equals("")){//$NON-NLS-1$
                return defaultMessage;
            } 
            return error;
        }
        return null;
    }

    
    public boolean isValidateFlag() {
        return validateFlag;
    }
 
    public void setValidateFlag(boolean validateFlag) {
        this.validateFlag = validateFlag;
    }
}
