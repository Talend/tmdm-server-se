/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.service.dumptoconsole;

import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.Service;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;

@org.springframework.stereotype.Service("amalto/local/service/dumptoconsole")
public class DumpToConsoleBean extends Service {

    private static final Logger LOGGER = Logger.getLogger(DumpToConsoleBean.class);

    public String getConfiguration(String optionalParameters) {
        return "";
    }

    @Override
    public String getDefaultConfiguration() {
        return null;
    }

    public String getDescription(String twoLetterLanguageCode) {
        return "This service dumps the content of the Item to the console and logs it as INFO";
    }

    public String getDocumentation(String twoLettersLanguageCode) {
        return "N/A";
    }

    public String getStatus() {
        return "OK";
    }

    public void start() {
    }

    public void stop() {
    }

    public Serializable receiveFromOutbound(HashMap<String, Serializable> map) throws XtentisException {
        try {
            String contentType = (String) map.get("contentType");
            String charset = (String) map.get("charset");
            byte[] bytes = (byte[]) map.get("bytes");
            if (contentType != null && contentType.toLowerCase().startsWith("text")) {
                dump(new String(bytes, charset));
            }else{
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                dump(ois.readObject().toString());
            }
        } catch (Exception e) {
            String err = "ERROR: unable to dump the message to console"
                    + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error("Dump to console: " + err);
            throw new XtentisException(err);
        }
        return null;
    }

    public String receiveFromInbound(ItemPOJOPK itemPK, String routingOrderID, String parameters) throws XtentisException {
        try {
            String xml = Util.getItemCtrl2Local().getItem(itemPK).getProjectionAsString();
            dump(xml);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to dump to console"
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err);
        }
        return "Document successfully dumped to console";
    }

    private void dump(String string) {
        System.out.println("DUMP TO CONSOLE<<<<<<<<<<<<");
        System.out.println(string);
        System.out.println(">>>>>>>>>>>>DUMP TO CONSOLE");
    }

    public Serializable fetchFromOutbound(String command, String parameters, String schedulePlanID) {
        // N/A
        return null;
    }

    @Override
    public void putConfiguration(String configuration) {

    }
}