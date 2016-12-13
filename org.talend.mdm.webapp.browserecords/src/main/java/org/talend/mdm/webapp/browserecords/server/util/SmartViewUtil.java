/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.talend.mdm.webapp.browserecords.server.actions.BrowseRecordsAction;
import org.talend.mdm.webapp.browserecords.server.provider.DefaultSmartViewProvider;
import org.talend.mdm.webapp.browserecords.server.provider.SmartViewProvider;
import org.talend.mdm.webapp.browserecords.shared.SmartViewDescriptions;
import org.talend.mdm.webapp.browserecords.shared.SmartViewDescriptions.SmartViewDescription;

import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.core.webservice.WSTransformerV2PK;
import com.amalto.webapp.core.util.XtentisWebappException;


/**
 * DOC Administrator  class global comment. Detailled comment
 */
public class SmartViewUtil {

    private static final Messages MESSAGES = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages", BrowseRecordsAction.class.getClassLoader()); //$NON-NLS-1$

    public static SmartViewDescriptions build(SmartViewProvider provider, String concept, String defaultIsoLang)
            throws XtentisWebappException,
            RemoteException {
        String smRegex = "Smart_view_" + concept + "(_([^#]+))?(#(.+))?";//$NON-NLS-1$//$NON-NLS-2$
        Pattern smp = Pattern.compile(smRegex);
        SmartViewDescriptions smDescs = new SmartViewDescriptions();
        // get process
        WSTransformerV2PK[] wstpks = provider.getWSTransformerV2PKs();
        for (int i = 0; i < wstpks.length; i++) {
            if (wstpks[i].getPk().matches(smRegex)) {
                SmartViewDescription smDesc = new SmartViewDescription(concept);
                smDesc.setName(wstpks[i].getPk());
                Matcher matcher = smp.matcher(wstpks[i].getPk());
                while (matcher.find()) {
                    smDesc.setIsoLang(matcher.group(2));
                    smDesc.setOptName(matcher.group(4));
                }

                String description = provider.getDescription(wstpks[i]);
                // Try to extract the Smart View display information from its description first
                if (description != null && defaultIsoLang != null && defaultIsoLang.length() != 0) {
                    Pattern p = Pattern.compile(".*\\[" + defaultIsoLang.toUpperCase() + ":(.*?)\\].*", Pattern.DOTALL);//$NON-NLS-1$//$NON-NLS-2$
                    smDesc.setDisplayName(p.matcher(description).replaceAll("$1"));//$NON-NLS-1$
                }
                if (smDesc.getDisplayName() == null || smDesc.getDisplayName().length() == 0) {
                    if (description != null && description.length() != 0)
                        smDesc.setDisplayName(description);
                    else if (smDesc.getOptName() == null)
                        smDesc.setDisplayName(MESSAGES.getMessage("smartview_defaultoption")); //$NON-NLS-1$
                    else
                        smDesc.setDisplayName(smDesc.getOptName());
                }
                smDescs.add(smDesc);
            }
        }
        return smDescs;
    }

    
    public static boolean checkSmartViewExistsByLangAndOptName(String concept, String language, String optname, boolean useNoLang) throws RemoteException, XtentisWebappException {
        SmartViewProvider provider = new DefaultSmartViewProvider();
        SmartViewDescriptions smDescs = build(provider, concept, language);

        Set<SmartViewDescriptions.SmartViewDescription> smDescSet = smDescs.get(language);
        if (useNoLang) {
            // Add the no language Smart Views too
            smDescSet.addAll(smDescs.get(null));
        }
        for (SmartViewDescriptions.SmartViewDescription smDesc : smDescSet) {
            if (optname != null) {
                if (optname.equals(smDesc.getOptName())) {
                    return true;
                }
            } else {
                if (smDesc.getOptName() == null) {
                    return true;
                }
            }
        }
        return false;
    }
}
