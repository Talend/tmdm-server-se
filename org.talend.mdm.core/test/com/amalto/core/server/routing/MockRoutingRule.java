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

import com.amalto.core.objects.routing.RoutingRulePOJO;
import com.amalto.core.objects.routing.RoutingRulePOJOPK;
import com.amalto.core.server.api.RoutingRule;
import com.amalto.core.util.XtentisException;

import java.util.*;

public class MockRoutingRule implements RoutingRule {

    private final Map<String, RoutingRulePOJO> store = new HashMap<>();

    @Override
    public RoutingRulePOJOPK putRoutingRule(RoutingRulePOJO routingRule) throws XtentisException {
        RoutingRulePOJOPK key = new RoutingRulePOJOPK(routingRule.getPK().getUniqueId());
        store.put(key.getUniqueId(), routingRule);
        return key;
    }

    @Override
    public RoutingRulePOJO getRoutingRule(RoutingRulePOJOPK pk) throws XtentisException {
        return store.get(pk.getUniqueId());
    }

    @Override
    public RoutingRulePOJO existsRoutingRule(RoutingRulePOJOPK pk) throws XtentisException {
        return store.get(pk.getUniqueId());
    }

    @Override
    public RoutingRulePOJOPK removeRoutingRule(RoutingRulePOJOPK pk) throws XtentisException {
        store.remove(pk.getUniqueId());
        return pk;
    }

    @Override
    public Collection<RoutingRulePOJOPK> getRoutingRulePKs(String regex) throws XtentisException {
        Set<RoutingRulePOJOPK> match = new HashSet<>();
        Set<String> pkList = store.keySet();
        for (String routingRulePOJOPK : pkList) {
            if (routingRulePOJOPK.matches(regex)) {
                match.add(new RoutingRulePOJOPK(routingRulePOJOPK));
            }
        }
        return match;
    }
}
