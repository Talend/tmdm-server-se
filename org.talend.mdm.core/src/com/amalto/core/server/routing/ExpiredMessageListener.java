/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server.routing;

import java.util.Arrays;
import java.util.List;

import javax.jms.Message;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.routing.FailedRoutingOrderV2POJO;
import com.amalto.core.objects.routing.RoutingRulePOJO;
import com.amalto.core.objects.routing.RoutingRulePOJOPK;
import com.amalto.core.server.api.RoutingRule;
import com.amalto.core.server.security.SecurityConfig;

@Component
public class ExpiredMessageListener {

    private static final Logger LOGGER = Logger.getLogger(ExpiredMessageListener.class);

    @Autowired
    RoutingRule routingRules;

    // Called by Spring, do not remove
    @SuppressWarnings("unchecked")
    public void consume(final Message msg) {
        try {
            ActiveMQMessage message = (ActiveMQMessage) ((ActiveMQMessage) msg).getDataStructure();
            Object rulesObj = message.getObjectProperty("rules"); //$NON-NLS-1$
            final List<String> rules = rulesObj instanceof List ? (List<String>) rulesObj : Arrays.asList(rulesObj.toString());
            final String pk = message.getStringProperty("pk"); //$NON-NLS-1$
            String type = message.getStringProperty("type"); //$NON-NLS-1$
            String container = message.getStringProperty("container"); //$NON-NLS-1$
            // Record routing order has failed.
            for (int ruleIndex = 0; ruleIndex < rules.size(); ruleIndex++) {
                String rule = rules.get(ruleIndex);
                final RoutingRulePOJO routingRule = routingRules.getRoutingRule(new RoutingRulePOJOPK(rule));
                // Apparently rule is not (yet) deployed onto this system's DB instance, but... that 's
                // rather unexpected since all nodes in cluster are supposed to share same system DB.
                if (routingRule == null) {
                    throw new RuntimeException("Cannot execute rule(s) " + rules + ": routing rule '" + rule
                            + "' can not be found.");
                }
                // Proceed with rule execution
                final ItemPOJOPK itemPOJOPK = new ItemPOJOPK(new DataClusterPOJOPK(container), type, pk.split("\\.")); //$NON-NLS-1$
                SecurityConfig.invokeSynchronousPrivateInternal(new Runnable() {

                    @Override
                    public void run() {
                        FailedRoutingOrderV2POJO failedRoutingOrder = createFail("Routing order '" + pk + "' expired.");
                        try {
                            failedRoutingOrder.store();
                        } catch (Throwable e) {
                            LOGGER.error("Unable to store failed routing order (enable DEBUG for details).");
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Unable to store failed routing order.", e);
                            }
                        }
                    }

                    private FailedRoutingOrderV2POJO createFail(String errorMessage) {
                        // Record routing order has failed.
                        FailedRoutingOrderV2POJO failedRoutingOrder = new FailedRoutingOrderV2POJO();
                        failedRoutingOrder.setMessage(errorMessage);
                        failedRoutingOrder.setItemPOJOPK(itemPOJOPK);
                        failedRoutingOrder.setServiceJNDI(routingRule.getServiceJNDI());
                        failedRoutingOrder.setServiceParameters(routingRule.getParameters());
                        return failedRoutingOrder;
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to process message.", e);
        }
    }
}
