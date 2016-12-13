/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.routing;

import java.util.ArrayList;
import java.util.List;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.metadata.LongString;

/**
 * @author bgrieder
 * 
 */
public class RoutingRulePOJO extends ObjectPOJO implements Comparable<RoutingRulePOJO> {

    private String name;

    private String description;

    private List<RoutingRuleExpressionPOJO> routingExpressions = new ArrayList<RoutingRuleExpressionPOJO>();

    private boolean synchronous = false;

    private String concept;

    private String serviceJNDI;

    private String parameters;

    private String condition;

    private int executeOrder;

    private boolean deActive;

    public RoutingRulePOJO() {
    }

    public RoutingRulePOJO(String name) {
        super();
        this.name = name;
    }

    public RoutingRulePOJO(String name, String description, ArrayList<RoutingRuleExpressionPOJO> routingExpressions,
            boolean synchronous, String concept, String serviceJNDI, String parameters, String condition, boolean deActive,
            int executeOrder) {
        super();
        this.name = name;
        this.description = description;
        this.routingExpressions = routingExpressions;
        this.synchronous = synchronous;
        this.concept = concept;
        this.serviceJNDI = serviceJNDI;
        this.parameters = parameters;
        this.condition = condition;
        this.deActive = deActive;
        this.executeOrder = executeOrder;
    }

    public boolean isDeActive() {
        return deActive;
    }

    public void setDeActive(boolean deActive) {
        this.deActive = deActive;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @LongString
    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public List<RoutingRuleExpressionPOJO> getRoutingExpressions() {
        return routingExpressions;
    }

    public void setRoutingExpressions(List<RoutingRuleExpressionPOJO> routingExpressions) {
        this.routingExpressions = routingExpressions;
    }

    public String getServiceJNDI() {
        return serviceJNDI;
    }

    public void setServiceJNDI(String serviceJNDI) {
        this.serviceJNDI = serviceJNDI;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    @Override
    public ObjectPOJOPK getPK() {
        if (getName() == null) {
            return null;
        }
        return new ObjectPOJOPK(new String[] { name });
    }

    /**
     * Getter for executeOrder.
     * 
     * @return the executeOrder
     */
    public int getExecuteOrder() {
        return this.executeOrder;
    }

    /**
     * Sets the executeOrder.
     * 
     * @param executeOrder the executeOrder to set
     */
    public void setExecuteOrder(int executeOrder) {
        this.executeOrder = executeOrder;
    }

    @Override
    public int compareTo(RoutingRulePOJO routingRule) {
        if (routingRule == null) {
            return 1;
        }
        // ascending order
        return this.executeOrder - routingRule.executeOrder;
    }

}
