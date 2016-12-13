/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.talend.mdm.commmon.util.core.Crypt;

public class ActiveMQConnectionFactoryExtension extends ActiveMQConnectionFactory {

    @Override
    public void setPassword(String password) {
        try {
            this.password = Crypt.decrypt(password);
        } catch (Exception e) {
            throw new RuntimeException("Can not read activemq password: " + e, e); //$NON-NLS-1$
        }
    }
}
