/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DOC Administrator  class global comment. Detailed comment
 */
public class TypePathParty implements Serializable, IsSerializable {

    private String partyName;

    private List<String> variants;

    /**
     * DOC Administrator TypePath.PathParty constructor comment.
     */
    public TypePathParty() {

    }

    public TypePathParty(String partyName) {
        super();
        this.partyName = partyName;
    }

    public String getPartyName() {
        return partyName;
    }

    public List<String> getVariants() {
        if (variants == null)
            variants = new ArrayList<String>();
        return variants;
    }

    public void addVariant(String variantName) {
        getVariants().add(variantName);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("TypePathParty [partyName=") //$NON-NLS-1$
                .append(partyName).append("; variants="); //$NON-NLS-1$

        if (variants != null && variants.size() > 0) {
            for (int i = 0; i < variants.size(); i++) {
                sb.append(variants.get(i));
                if (i < variants.size() - 1)
                    sb.append(","); //$NON-NLS-1$
            }
        }

        sb.append("]"); //$NON-NLS-1$

        return sb.toString();
    }

}
