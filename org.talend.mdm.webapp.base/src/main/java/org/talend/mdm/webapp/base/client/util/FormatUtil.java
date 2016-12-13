/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.util;


public class FormatUtil {

    public static String languageValueEncode(String value) {
        if (value != null && value.trim().length() > 0){
            if (value.contains("\\")) {//$NON-NLS-1$
                value = value.replace("\\", "&amp;#92;"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (value.contains("[")) {//$NON-NLS-1$
                value = value.replace("[", "&amp;#91;"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (value.contains("]")) {//$NON-NLS-1$
                value = value.replace("]", "&amp;#93;"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return value;
    }

    public static String languageValueDecode(String value) {
        if (value != null && value.trim().length() > 0) {
            if (value.contains("&amp;")) {//$NON-NLS-1$
                value = value.replace("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (value.contains("&#92;")) {//$NON-NLS-1$
                value = value.replace("&#92;", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (value.contains("&#91;")) {//$NON-NLS-1$
                value = value.replace("&#91;", "["); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (value.contains("&#93;")) {//$NON-NLS-1$
                value = value.replace("&#93;", "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return value;
    }

    public static String multiLanguageEncode(String multiLanguageString) {
        if (multiLanguageString != null && multiLanguageString.trim().length() > 0) {
            if (multiLanguageString.contains("&#92;")) {//$NON-NLS-1$
                multiLanguageString = multiLanguageString.replace("&#92;", "&amp;#92;"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (multiLanguageString.contains("&#91;")) {//$NON-NLS-1$
                multiLanguageString = multiLanguageString.replace("&#91;", "&amp;#91;"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (multiLanguageString.contains("&#93;")) {//$NON-NLS-1$
                multiLanguageString = multiLanguageString.replace("&#93;", "&amp;#93;"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return multiLanguageString;
    }

}
