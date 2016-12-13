/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.properties;

import java.util.Properties;

public interface StandardPropertiesStrategy {
    /**
     * @return Returns the {@link System#getProperties()} properties used by default in the current running JVM. This method must returns valid
     *         properties that can allow a simple Java's main method to be executed without any issue.
     */
    Properties getStandardProperties();

}
