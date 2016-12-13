/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.routing.RoutingRulePOJO;
import com.amalto.core.objects.routing.RoutingRulePOJOPK;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.RoutingRule;

import java.util.ArrayList;
import java.util.Collection;


public class DefaultRoutingRule implements RoutingRule {

    private static final Logger LOGGER = Logger.getLogger(DefaultRoutingRule.class);

    /**
     * Creates or updates a menu
     */
    public RoutingRulePOJOPK putRoutingRule(RoutingRulePOJO routingRule) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("createRoutingRule() ");
        }
        try {
            if (routingRule.getConcept() == null || "".equals(routingRule.getConcept())) { //$NON-NLS-1$
                routingRule.setConcept("*"); //$NON-NLS-1$
            }
            ObjectPOJOPK pk = routingRule.store();
            if (pk == null) {
                throw new XtentisException("Unable to create the Routing Rule. Please check the XML Server logs");
            }
            return new RoutingRulePOJOPK(pk);
        } catch (Exception e) {
            String err = "Unable to create/update the menu " + routingRule.getName()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get menu
     */
    public RoutingRulePOJO getRoutingRule(RoutingRulePOJOPK pk) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getRoutingRule() ");
        }
        try {
            RoutingRulePOJO rule = ObjectPOJO.load(RoutingRulePOJO.class, pk);
            if (rule == null) {
                String err = "The Routing Rule " + pk.getUniqueId() + " does not exist.";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return rule;
        } catch (Exception e) {
            String err = "Unable to get the Routing Rule " + pk.toString()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get a RoutingRule - no exception is thrown: returns null if not found
     */
    public RoutingRulePOJO existsRoutingRule(RoutingRulePOJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(RoutingRulePOJO.class, pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            String info = "Could not check whether this Routing Rule \"" + pk.getUniqueId() + "\" exists:  "
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(info, e);
            }
            return null;
        }
    }

    /**
     * Remove a RoutingRule
     */
    public RoutingRulePOJOPK removeRoutingRule(RoutingRulePOJOPK pk) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removing " + pk.getUniqueId());
        }
        try {
            return new RoutingRulePOJOPK(ObjectPOJO.remove(RoutingRulePOJO.class, pk));
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the Routing Rule " + pk.toString()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Retrieve all RoutingRule PKs
     */
    public Collection<RoutingRulePOJOPK> getRoutingRulePKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> routingRules = ObjectPOJO.findAllPKs(RoutingRulePOJO.class, regex);
        ArrayList<RoutingRulePOJOPK> l = new ArrayList<RoutingRulePOJOPK>();
        for (ObjectPOJOPK currentRule : routingRules) {
            l.add(new RoutingRulePOJOPK(currentRule));
        }
        return l;
    }


}