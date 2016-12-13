/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.talend.mdm.webapp.base.shared.TypePath;
import org.talend.mdm.webapp.base.shared.TypePathParty;


/**
 * DOC Administrator  class global comment. Detailed comment
 */
@SuppressWarnings("nls")
public class TypePathTest extends TestCase {

    TypePath typePath;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */

    @Override
    protected void setUp() throws Exception {

        Map<String, List<String>> aliasXpathMap = new HashMap<String, List<String>>();
        List<String> detailAlias=new ArrayList<String>(){};
        detailAlias.add("Contract/detail:ContractDetailType");
        detailAlias.add("Contract/detail:ContractDetailSubType");
        List<String> codeAlias=new ArrayList<String>(){};
        codeAlias.add("Contract/detail/code:myCodeType");
        aliasXpathMap.put("Contract/detail", detailAlias);
        aliasXpathMap.put("Contract/detail/code",codeAlias );

        typePath = new TypePath("Contract/detail/code", aliasXpathMap);

        super.setUp();
    }

    /**
     * DOC Administrator Comment method "testGetPathParties".
     */
    public void testGetPathParties() {

        List<TypePathParty> parties = typePath.getPathParties();
        assertEquals(3, parties.size());

        for (TypePathParty part : parties) {

            if (part.getPartyName().equals("detail"))
                assertEquals(2, part.getVariants().size());
            else if (part.getPartyName().equals("code"))
                assertEquals(1, part.getVariants().size());

        }

    }

    /**
     * DOC Administrator Comment method "testFindAllAliasXpaths".
     */
    public void testFindAllAliasXpaths() {
        List<String> paths = typePath.getAllAliasXpaths();
        assertEquals(6, paths.size());
    }

}
