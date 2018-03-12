/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.schema.validation;

import com.amalto.core.util.ValidateException;
import org.w3c.dom.Element;

/**
 *
 */
public interface Validator {

    class NoOpValidator implements Validator {
        private NoOpValidator() {
        }

        public void validate(Element element) {
            // No op
        }
    }

    Validator NO_OP_VALIDATOR = new NoOpValidator();

    void validate(Element element) throws ValidateException;

}
