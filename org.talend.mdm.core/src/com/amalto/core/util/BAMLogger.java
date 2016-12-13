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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;

public class BAMLogger {

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss.S z"); //$NON-NLS-1$
    public static boolean log = false;

    static {
        String val = (String) MDMConfiguration.getConfiguration().get("bam.logging"); //$NON-NLS-1$
        if ("true".equals(val) || "yes".equals(val))log = true; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @SuppressWarnings("nls")
    public static void log(String key, String username, String permission, Class<? extends ObjectPOJO> objectClass,
            ObjectPOJOPK pk, boolean authorized) {
        if (!log)
            return;
        StringBuilder line = new StringBuilder();
        line.append("[BAM ").append(key).append("] ");
        line.append("[DATE ").append(sdf.format(new Date(System.currentTimeMillis()))).append("] ");
        line.append("[USER ").append(username).append("] ");
        line.append("[PERMISSION ").append(permission).append("] ");
        line.append("[OBJECT ").append(ObjectPOJO.getObjectName(objectClass)).append("] ");
        line.append("[INSTANCE ").append(pk.getUniqueId()).append("] ");
        line.append("[AUTHORIZED").append(authorized ? " YES" : " NO").append("]");

        if ("read".equals(permission))
            org.apache.log4j.Logger.getLogger(BAMLogger.class).debug(line);
        else
            org.apache.log4j.Logger.getLogger(BAMLogger.class).info(line);
    }

}
