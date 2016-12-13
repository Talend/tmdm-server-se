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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DOC Administrator  class global comment. Detailed comment
 */
public class TypePath implements Serializable, IsSerializable {
    

    private static final String XPATH_SEPARATOR = "/"; //$NON-NLS-1$

    private List<TypePathParty> pathParties = null;
    
    private boolean hasVariantion = false;

    private List<String> allAliasXpaths = null;

    /**
     * DOC Administrator TypePath constructor comment.
     */
    public TypePath() {

    }

    /**
     * DOC Administrator TypePath constructor comment.
     */
    public TypePath(String xpath, Map<String, List<String>> aliasXpathMap) {

        if (xpath != null) {

            xpath = stripXpath(xpath);

            String[] parties = xpath.split(XPATH_SEPARATOR);
            this.pathParties = new ArrayList<TypePathParty>();
            for (String partyName : parties) {
                pathParties.add(new TypePathParty(partyName));
            }

        }

        if (aliasXpathMap != null && aliasXpathMap.size() > 0) {

            for (Iterator iterator = aliasXpathMap.keySet().iterator(); iterator.hasNext();) {
                String keyPath = (String) iterator.next();
                keyPath = stripXpath(keyPath);
                String[] keyPathParts = keyPath.split(XPATH_SEPARATOR);

                boolean validatePath = true;
                if(keyPathParts.length==0||keyPathParts.length>pathParties.size()){
                    validatePath = false;
                }else{
                    for (int i = 0; i < keyPathParts.length; i++) {
                        if (!keyPathParts[i].equals(pathParties.get(i).getPartyName())) {
                            validatePath = false;
                            break;
                        }
                    }
                }
                    
                if (validatePath) {

                    List<String> aliasPaths = aliasXpathMap.get(keyPath);
                    for (String aliasPath : aliasPaths) {
                        String aliasName = getAliasNameFromPath(aliasPath);
                        int updateIndex = keyPathParts.length - 1;
                        TypePathParty pathParty = pathParties.get(updateIndex);
                        if (aliasName != null && aliasName.trim().length() > 0) {
                            pathParty.addVariant(aliasName);
                            if (!hasVariantion)
                                hasVariantion = true;
                        }
                    }

                }

            }

        }

        // calculate all possible various once
        allAliasXpaths = findAllAliasXpaths();

    }

    public boolean hasVariantion() {
        return hasVariantion;
    }

    public List<TypePathParty> getPathParties() {
        return pathParties;
    }

    /**
     * DOC Administrator Comment method "getAliasNameFromPath".
     */
    private String getAliasNameFromPath(String path) {

        String aliasName = null;

        if (path == null || path.trim().length() == 0)
            return aliasName;
        
        int pos = path.lastIndexOf(XPATH_SEPARATOR);

        if (pos != -1) {
            aliasName = path.substring(pos + 1);
        }

        return aliasName;

    }


    /**
     * DOC Administrator Comment method "stripXpath".
     * @param xpath
     * @return
     */
    private String stripXpath(String xpath) {
        if (xpath.startsWith(XPATH_SEPARATOR))
            xpath = xpath.substring(1);
        return xpath;
    }

    public List<String> getAllAliasXpaths() {
        return allAliasXpaths;
    }

    /**
     * Re-calculate all possible various once
     */
    public List<String> findAllAliasXpaths() {
        List<String> xpaths = new ArrayList<String>();
        for (TypePathParty pathParty : pathParties) {
            xpaths=findAliasXpaths(pathParty, xpaths);
        }
        return xpaths;
    }

    private List<String> findAliasXpaths(TypePathParty pathParty, List<String> lastPaths) {
        
        List<String> possibilities = new ArrayList<String>();
        if (pathParty.getPartyName() != null)
            possibilities.add(pathParty.getPartyName());
        if (pathParty.getVariants() != null)
            possibilities.addAll(pathParty.getVariants());

        // first time
        if (lastPaths.size() == 0) {
            for (String path : possibilities) {
                lastPaths.add(path);
            }
            return lastPaths;
        }

        List<String> myPaths = new ArrayList<String>();
        for (String path : lastPaths) {
            for (String part : possibilities) {
                String newPath = path + XPATH_SEPARATOR + part;
                myPaths.add(newPath);
            }
        }

        return myPaths;

    }



}
