/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmartViewDescriptions {

    private static final long serialVersionUID = 1L;

    private Map<String, List<SmartViewDescription>> descs;

    public static class SmartViewDescription {

        private String entity;

        private String name;

        private String displayName;

        private String isoLang;

        private String optName;

        public SmartViewDescription(String entity) {
            if (entity == null)
                throw new NullPointerException();
            this.entity = entity;
        }

        public String getName() {
            return name;
        }

        public void setName(String value) {
            name = value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String value) {
            displayName = value;
        }

        public String getIsoLang() {
            return isoLang;
        }

        public void setIsoLang(String value) {
            isoLang = value;
        }

        public String getOptName() {
            return optName;
        }

        public void setOptName(String value) {
            optName = value;
        }

        @Override
        public boolean equals(Object obj) {
            // Smart Views on same entity are considered equivalent if their option names are equal.
            // Meaning we don't bother of their isoLang
            if (obj instanceof SmartViewDescription) {
                SmartViewDescription other = (SmartViewDescription) obj;
                if (!entity.equals(other.entity))
                    return false;
                if (optName == null)
                    return other.optName == null;
                return optName.equals(other.optName);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return optName == null ? "".hashCode() : optName.hashCode(); //$NON-NLS-1$
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public Set<SmartViewDescription> get(String isoLang) {
        Set<SmartViewDescription> smSet = new HashSet<SmartViewDescription>();
        if (descs != null) {
            if (isoLang == null)
                isoLang = ""; //$NON-NLS-1$
            List<SmartViewDescription> smList = descs.get(isoLang.toLowerCase());
            if (smList != null)
                smSet.addAll(smList);
        }
        return smSet;
    }

    public void add(SmartViewDescription smDesc) {
        if (descs == null)
            descs = new HashMap<String, List<SmartViewDescription>>();
        String key;
        if (smDesc.isoLang == null)
            key = ""; //$NON-NLS-1$
        else
            key = smDesc.isoLang.toLowerCase();
        List<SmartViewDescription> smList = descs.get(key);
        if (smList == null) {
            smList = new ArrayList<SmartViewDescription>();
            descs.put(key, smList);
        }
        smList.add(smDesc);
    }
}
