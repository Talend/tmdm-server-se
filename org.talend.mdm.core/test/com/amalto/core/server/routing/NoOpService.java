/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server.routing;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.Service;
import com.amalto.core.util.XtentisException;

@org.springframework.stereotype.Service("amalto/local/service/test/no_op_service")
public class NoOpService extends Service {

    public static int pauseTime = 400;

    public static void setPauseTime(int time) {
        pauseTime = time;
    }

    @Override
    public String getDescription(String twoLettersLanguageCode) {
        return StringUtils.EMPTY;
    }

    @Override
    public String getDocumentation(String twoLettersLanguageCode) {
        return StringUtils.EMPTY;
    }

    @Override
    public void start() throws XtentisException {
    }

    @Override
    public void stop() throws XtentisException {
    }

    @Override
    public String getStatus() {
        return "OK";
    }

    @Override
    public Serializable receiveFromOutbound(HashMap<String, Serializable> map) throws XtentisException {
        return null;
    }

    @Override
    public String receiveFromInbound(ItemPOJOPK itemPK, String routingOrderID, String parameters) throws XtentisException {
        try {
            Thread.sleep(pauseTime);
        } catch (InterruptedException e) {
            throw new XtentisException(e);
        }
        return StringUtils.EMPTY;
    }

    @Override
    public Serializable fetchFromOutbound(String command, String parameters, String schedulePlanID) throws XtentisException {
        return StringUtils.EMPTY;
    }

    @Override
    public void putConfiguration(String configuration) {
    }

    @Override
    public String getConfiguration(String optionalParameter) throws XtentisException {
        return StringUtils.EMPTY;
    }

    @Override
    public String getDefaultConfiguration() {
        return StringUtils.EMPTY;
    }
}
