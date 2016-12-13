/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage;

import org.apache.log4j.Logger;
import org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatformProvider;

// Do not remove, dynamically called by Hibernate (see META-INF/services/).
public class JTAProvider implements JtaPlatformProvider {

    public static final Logger LOGGER = Logger.getLogger(JTAProvider.class);

    @Override
    public JtaPlatform getProvidedJtaPlatform() {
        /*
         * MDM does not provide/use any TM, however endorsed libraries (for Bonita) may wrongly indicate to Hibernate
         * that a TM is available. Code below ensures JTA is always disabled.
         */
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Disabling JTA support.");
        }
        return NoJtaPlatform.INSTANCE;
    }
}
