/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server.routing;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.AbstractJmsListeningContainer;
import org.springframework.stereotype.Component;

import bsh.EvalError;
import bsh.Interpreter;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.Service;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.routing.*;
import com.amalto.core.server.api.Item;
import com.amalto.core.server.api.RoutingEngine;
import com.amalto.core.server.api.RoutingRule;
import com.amalto.core.server.security.SecurityConfig;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

@Component
public class DefaultRoutingEngine implements RoutingEngine {

    public static final String EVENTS_DESTINATION = "org.talend.mdm.server.routing.events"; //$NON-NLS-1

    private static final Logger LOGGER = Logger.getLogger(DefaultRoutingEngine.class);

    private final Interpreter ntp = new Interpreter();

    @Autowired
    Item item;

    @Autowired
    RoutingRule routingRules;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    AbstractJmsListeningContainer jmsListeningContainer;

    @Value("${routing.engine.max.execution.time.millis}")
    private long timeToLive = 0;

    private static String strip(String condition) {
        String compiled = condition;
        compiled = compiled.replaceAll("(\\s+)([aA][nN][dD])(\\s+|\\(+)", "$1&&$3"); //$NON-NLS-1$ //$NON-NLS-2$
        compiled = compiled.replaceAll("(\\s+)([oO][rR])(\\s+|\\(+)", "$1||$3"); //$NON-NLS-1$//$NON-NLS-2$
        compiled = compiled.replaceAll("(\\s*)([nN][oO][tT])(\\s+|\\(+)", "$1!$3"); //$NON-NLS-1$ //$NON-NLS-2$
        return compiled;
    }

    private static Integer safeParse(String content) {
        try {
            return Integer.parseInt(content);
        } catch (Exception e) {
            return null;
        }
    }

    void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * Check that a rule actually matches a document
     *
     * @return true if it matches
     * @throws XtentisException
     */
    private static boolean ruleExpressionMatches(ItemPOJO itemPOJO, RoutingRuleExpressionPOJO exp) throws XtentisException {
        Integer contentInt, expInt;
        String expXpath = exp.getXpath();
        if (expXpath.startsWith(itemPOJO.getConceptName())) {
            expXpath = StringUtils.substringAfter(expXpath, itemPOJO.getConceptName() + '/');
        }
        try {
            String[] contents = Util.getTextNodes(itemPOJO.getProjection(), expXpath);
            if (contents.length == 0 && exp.getOperator() == RoutingRuleExpressionPOJO.IS_NULL) {
                return true;
            }
            boolean match = false;
            for (String content : contents) {
                if (match) {
                    break;
                }
                switch (exp.getOperator()) {
                case RoutingRuleExpressionPOJO.CONTAINS:
                    match = StringUtils.contains(content, exp.getValue());
                    break;
                case RoutingRuleExpressionPOJO.EQUALS:
                    match = StringUtils.equals(content, exp.getValue());
                    break;
                case RoutingRuleExpressionPOJO.GREATER_THAN:
                    expInt = safeParse(exp.getValue());
                    contentInt = safeParse(content);
                    if (expInt == null || contentInt == null) {
                        continue;
                    }
                    if (contentInt > expInt) {
                        match = true;
                    }
                    break;
                case RoutingRuleExpressionPOJO.GREATER_THAN_OR_EQUAL:
                    expInt = safeParse(exp.getValue());
                    contentInt = safeParse(content);
                    if (expInt == null || contentInt == null) {
                        continue;
                    }
                    if (contentInt >= expInt) {
                        match = true;
                    }
                    break;
                case RoutingRuleExpressionPOJO.IS_NOT_NULL:
                    match = content != null;
                    break;
                case RoutingRuleExpressionPOJO.LOWER_THAN:
                    expInt = safeParse(exp.getValue());
                    contentInt = safeParse(content);
                    if (expInt == null || contentInt == null) {
                        continue;
                    }
                    if (contentInt < expInt) {
                        match = true;
                    }
                    break;
                case RoutingRuleExpressionPOJO.LOWER_THAN_OR_EQUAL:
                    expInt = safeParse(exp.getValue());
                    contentInt = safeParse(content);
                    if (expInt == null || contentInt == null) {
                        continue;
                    }
                    if (contentInt <= expInt) {
                        match = true;
                    }
                    break;
                case RoutingRuleExpressionPOJO.MATCHES:
                    match = StringUtils.countMatches(content, exp.getValue()) > 0;
                    break;
                case RoutingRuleExpressionPOJO.NOT_EQUALS:
                    match = !StringUtils.equals(content, exp.getValue());
                    break;
                case RoutingRuleExpressionPOJO.STARTSWITH:
                    if (content != null) {
                        match = content.startsWith(exp.getValue());
                    }
                    break;
                }
            }
            return match;
        } catch (TransformerException e) {
            String err = "Subscription rule expression match: unable extract xpath '" + exp.getXpath() + "' from Item '"
                    + itemPOJO.getItemPOJOPK().getUniqueID() + "': " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    @Override
    public RoutingRulePOJOPK[] route(final ItemPOJOPK itemPOJOPK) throws XtentisException {
        // The cached ItemPOJO - will only be retrieved if needed: we have expressions on the routing rules
        String type = itemPOJOPK.getConceptName();
        // Get the item
        ItemPOJO itemPOJO = item.getItem(itemPOJOPK);
        if (itemPOJO == null) { // Item does not exist, no rule can apply.
            return new RoutingRulePOJOPK[0];
        }
        // Rules that matched
        ArrayList<RoutingRulePOJO> routingRulesThatMatched = new ArrayList<>();
        // loop over the known rules
        Collection<RoutingRulePOJOPK> routingRulePOJOPKs = routingRules.getRoutingRulePKs(".*"); //$NON-NLS-1$
        for (RoutingRulePOJOPK routingRulePOJOPK : routingRulePOJOPKs) {
            RoutingRulePOJO routingRule = routingRules.getRoutingRule(routingRulePOJOPK);
            if (routingRule.isDeActive()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(routingRule.getName() + " disabled, skip it!");
                }
                continue;
            }
            // check if type matches the routing rule
            if (!"*".equals(routingRule.getConcept())) { //$NON-NLS-1$
                if (!type.equals(routingRule.getConcept())) {
                    continue;
                }
            }
            // check if all routing rule expression matches - null: always matches
            // aiming modify see 4572 add condition to check
            if (routingRule.getCondition() == null || routingRule.getCondition().trim().length() == 0) {
                boolean matches = true;
                Collection<RoutingRuleExpressionPOJO> routingExpressions = routingRule.getRoutingExpressions();
                if (routingExpressions != null) {
                    for (RoutingRuleExpressionPOJO routingExpression : routingExpressions) {
                        if (!ruleExpressionMatches(itemPOJO, routingExpression)) {
                            matches = false; // Rule doesn't match: expect a full match to consider routing rule.
                            break;
                        }
                    }
                }
                if (!matches) {
                    continue;
                }
            } else {
                String condition = routingRule.getCondition();
                String compileCondition = strip(condition);
                Collection<RoutingRuleExpressionPOJO> routingExpressions = routingRule.getRoutingExpressions();
                try {
                    for (RoutingRuleExpressionPOJO pojo : routingExpressions) {
                        if (pojo.getName() != null && pojo.getName().trim().length() > 0) {
                            Pattern p1 = Pattern.compile(pojo.getName(), Pattern.CASE_INSENSITIVE);
                            Matcher m1 = p1.matcher(condition);
                            while (m1.find()) {
                                ntp.set(m1.group(), ruleExpressionMatches(itemPOJO, pojo));
                            }
                        }
                    }
                    // compile
                    ntp.eval("routingRuleResult = " + compileCondition + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                    boolean result = (Boolean) ntp.get("routingRuleResult"); //$NON-NLS-1$
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(condition + " : " + result);
                        if (result) {
                            LOGGER.debug("Trigger \"" + (routingRule.getName() == null ? "" : routingRule.getName())
                                    + "\" matched! ");
                        }
                    }
                    if (!result) {
                        continue;
                    }
                } catch (EvalError e) {
                    String err = "Condition compile error :" + e.getMessage();
                    LOGGER.error(err, e);
                    throw new XtentisException(err, e);
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("route() Routing Rule MATCH '" + routingRulePOJOPK.getUniqueId() + "' for item '"
                        + itemPOJOPK.getUniqueID() + "'");
            }
            // increment matching routing rules counter
            routingRulesThatMatched.add(routingRule);
        }
        // Sort execution order and send JMS message
        Collections.sort(routingRulesThatMatched);
        final String[] ruleNames = new String[routingRulesThatMatched.size()];
        final String[] ruleParameters = new String[routingRulesThatMatched.size()];
        int i = 0;
        for (RoutingRulePOJO rulePOJO : routingRulesThatMatched) {
            ruleNames[i] = rulePOJO.getPK().getUniqueId();
            ruleParameters[i] = rulePOJO.getParameters();
            i++;
        }
        jmsTemplate.send(EVENTS_DESTINATION, new MessageCreator() {

            @Override
            public Message createMessage(Session session) throws JMSException {
                Message message = session.createMessage();
                message.setObjectProperty("rules", Arrays.asList(ruleNames)); //$NON-NLS-1$
                message.setStringProperty("container", itemPOJOPK.getDataClusterPOJOPK().getUniqueId()); //$NON-NLS-1$
                message.setStringProperty("type", itemPOJOPK.getConceptName()); //$NON-NLS-1$
                message.setStringProperty("pk", Util.joinStrings(itemPOJOPK.getIds(), ".")); //$NON-NLS-1$ //$NON-NLS-2$
                message.setObjectProperty("parameters", Arrays.asList(ruleParameters)); //$NON-NLS-1$
                if (timeToLive > 0) {
                    message.setJMSExpiration(System.currentTimeMillis() + timeToLive);
                } else {
                    message.setJMSExpiration(0);
                }
                return message;
            }
        });
        // Log debug information if no rule found for document
        if (routingRulesThatMatched.size() == 0) {
            if (LOGGER.isDebugEnabled()) {
                String err = "Unable to find a routing rule for document " + itemPOJOPK.getUniqueID();
                LOGGER.debug(err);
            }
            return new RoutingRulePOJOPK[0];
        }
        // Contract imposes to send matching rule names
        RoutingRulePOJOPK[] pks = new RoutingRulePOJOPK[routingRulesThatMatched.size()];
        for (int j = 0; j < pks.length; j++) {
            pks[j] = new RoutingRulePOJOPK(ruleNames[j]);
        }
        return pks;
    }

    // Called by Spring, do not remove
    @Override
    public void consume(final Message message) {
        try {
            final List<String> rules = (List<String>) message.getObjectProperty("rules");
            final List<String> parameters = (List<String>) message.getObjectProperty("parameters");
            final String pk = message.getStringProperty("pk");
            String type = message.getStringProperty("type");
            String container = message.getStringProperty("container");
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
                final Service service = Util.retrieveComponent(routingRule.getServiceJNDI());
                final int parameterIndex = ruleIndex;
                SecurityConfig.invokeSynchronousPrivateInternal(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            service.receiveFromInbound(itemPOJOPK, message.getJMSMessageID(), parameters.get(parameterIndex));
                            message.acknowledge();
                            // Record routing order was successfully executed.
                            CompletedRoutingOrderV2POJO completedRoutingOrder = new CompletedRoutingOrderV2POJO();
                            completedRoutingOrder.setItemPOJOPK(itemPOJOPK);
                            completedRoutingOrder.setServiceJNDI(routingRule.getServiceJNDI());
                            completedRoutingOrder.setServiceParameters(routingRule.getParameters());
                            completedRoutingOrder.store();
                        } catch (Exception e) {
                            try {
                                String err = "Unable to execute the Routing Order '" + message.getJMSMessageID() + "'."
                                        + " The service: '" + routingRule.getServiceJNDI() + "' failed to execute. "
                                        + e.getMessage();
                                // Record routing order has failed.
                                FailedRoutingOrderV2POJO failedRoutingOrder = new FailedRoutingOrderV2POJO();
                                failedRoutingOrder.setMessage(err);
                                failedRoutingOrder.setItemPOJOPK(itemPOJOPK);
                                failedRoutingOrder.setServiceJNDI(routingRule.getServiceJNDI());
                                failedRoutingOrder.setServiceParameters(routingRule.getParameters());
                                failedRoutingOrder.store();
                            } catch (Exception e1) {
                                LOGGER.error("Trigger execution execution error.", e);
                                LOGGER.error("Unable to store trigger execution for '" + rules + "' on '" + pk + "'.", e1);
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to process message.", e);
        }
    }

    @Override
    public void start() throws XtentisException {
        jmsListeningContainer.start();
    }

    @Override
    public void stop() throws XtentisException {
        jmsListeningContainer.stop();
    }

    @Override
    public void suspend(boolean suspend) throws XtentisException {
        if (suspend) {
            jmsListeningContainer.stop();
        } else {
            jmsListeningContainer.start();
        }
    }

    // TODO It doesn't make quite sense to stop a consumer (stop is just an infinite suspend, right?)
    @Override
    public int getStatus() throws XtentisException {
        boolean active = jmsListeningContainer.isActive();
        if (active) {
            return RoutingEngine.RUNNING;
        } else {
            return RoutingEngine.SUSPENDED;
        }
    }
}
