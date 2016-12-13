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

/**
 * Chooses and creates instances of {@link StandardPropertiesStrategy} depending on JVM.
 */
public class StandardPropertiesStrategyFactory {

    private StandardPropertiesStrategyFactory() {
    }

    /**
     * @return Returns a {@link StandardPropertiesStrategy} valid for current running JVM.
     */
    public static StandardPropertiesStrategy create() {
        // Only support Sun/Oracle (for the time being).
        return new SunOracleStandardPropertiesStrategy();
    }
}
