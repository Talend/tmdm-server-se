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
