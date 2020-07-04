/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save.generator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@SuppressWarnings("nls")
public class AutoIncrementGenerator {

    private static final Logger LOGGER = LogManager.getLogger(AutoIncrementGenerator.class);

    private static final AutoIdGenerator AUTO_ID_GENERATOR;

    static {
        AUTO_ID_GENERATOR = InMemoryAutoIncrementGenerator.getInstance();
        LOGGER.info("Clustered access support for autoincrement id generator is disabled.");
    }

    public static AutoIdGenerator get() {
        return AUTO_ID_GENERATOR;
    }

}
