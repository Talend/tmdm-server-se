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

import static junit.framework.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.mock.env.MockEnvironment;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.routing.RoutingRuleExpressionPOJO;
import com.amalto.core.objects.routing.RoutingRulePOJO;
import com.amalto.core.objects.routing.RoutingRulePOJOPK;
import com.amalto.core.server.api.Item;
import com.amalto.core.server.api.RoutingEngine;
import com.amalto.core.server.api.RoutingRule;
import com.amalto.core.util.PluginRegistry;
import com.amalto.core.util.XtentisException;

@SuppressWarnings("nls")
public class RoutingEngineTest {

    private static AbstractApplicationContext context;

    private static RoutingRule routingRule;

    private static Item item;

    private final DataClusterPOJOPK container = new DataClusterPOJOPK("Test");

    private final DataModelPOJO dataModel = new DataModelPOJO("Test");

    @BeforeClass
    public static void setup() {
        System.setProperty("mdm.root.ignoreIfNotFound", "true");
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setResourceLoader(new PathMatchingResourcePatternResolver());
        MockEnvironment env = new MockEnvironment();
        env.setProperty("mdm.routing.engine.broker.url", "vm://localhost?broker.persistent=false");
        env.setProperty("mdm.routing.engine.consumers", "1");
        env.setProperty("routing.engine.max.execution.time.millis", "300");
        context.setEnvironment(env);
        context.load("classpath:**/" + RoutingEngineTest.class.getName() + ".xml");
        // FIXME Setting default-lazy-init on the top level beans element seems not applied to beans inside an imported
        // resource
        // Workaround: set all beans to be lazy-init programatically
        // See also https://gist.github.com/eeichinger/1979033 as an alternative
        for (String beanName : context.getBeanDefinitionNames()) {
            context.getBeanDefinition(beanName).setLazyInit(true);
        }
        context.refresh();
        RoutingEngineTest.context = context;
        RoutingEngineTest.routingRule = context.getBean(RoutingRule.class);
        RoutingEngineTest.item = context.getBean(Item.class);
        context.getBean(PluginRegistry.class);
        // Plugin Registry initialization (used in routing rule execution)
        BeanDelegatorContainer.createInstance().setDelegatorInstancePool(
                Collections.<String, Object> singletonMap("LocalUser", new ILocalUser() {

                    @Override
                    public ILocalUser getILocalUser() throws XtentisException {
                        return this;
                    }
                }));
    }

    @AfterClass
    public static void tearDown() {
        context.destroy();
        System.setProperty("mdm.root.ignoreIfNotFound", "false");
    }

    private static void clearRules() throws com.amalto.core.util.XtentisException {
        Collection<RoutingRulePOJOPK> routingRulePKs = routingRule.getRoutingRulePKs(".*");
        for (RoutingRulePOJOPK pk : routingRulePKs) {
            routingRule.removeRoutingRule(pk);
        }
    }

    @Test
    public void testEmptyRules() throws Exception {
        clearRules();
        RoutingEngine routingEngine = context.getBean(RoutingEngine.class);
        RoutingRulePOJOPK[] routes = routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        assertEquals(0, routes.length);
    }

    @Test
    public void testMatchRuleType() throws Exception {
        RoutingEngine routingEngine = context.getBean(RoutingEngine.class);
        routingEngine.start();
        item.putItem(new ItemPOJO(container, "Person", new String[] { "1", "2" }, 0, "<Person><id>1</id><id2>2</id2></Person>"),
                dataModel);
        // Match all rule
        clearRules();
        RoutingRulePOJO rule = new RoutingRulePOJO("testTypeMatchRule");
        rule.setConcept("*");
        routingRule.putRoutingRule(rule);
        RoutingRulePOJOPK[] routes = routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        assertEquals(1, routes.length);
        // Match per type name
        clearRules();
        rule = new RoutingRulePOJO("testTypeMatchRule");
        rule.setConcept("Person");
        routingRule.putRoutingRule(rule);
        routes = routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        assertEquals(1, routes.length);
        // Non match
        clearRules();
        rule = new RoutingRulePOJO("testTypeMatchRule");
        rule.setConcept("Address");
        routingRule.putRoutingRule(rule);
        routes = routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        assertEquals(0, routes.length);
    }

    @Test
    public void testMatchRuleExpression() throws Exception {
        RoutingEngine routingEngine = context.getBean(RoutingEngine.class);
        routingEngine.start();
        clearRules();
        RoutingRulePOJO rule = new RoutingRulePOJO("testTypeMatchRule");
        rule.setConcept("*");
        List<RoutingRuleExpressionPOJO> expressions = Arrays.asList(new RoutingRuleExpressionPOJO("Person", "id",
                RoutingRuleExpressionPOJO.EQUALS, "1"), new RoutingRuleExpressionPOJO("Person", "id2",
                RoutingRuleExpressionPOJO.EQUALS, "2"));
        rule.setRoutingExpressions(expressions);
        routingRule.putRoutingRule(rule);
        item.putItem(new ItemPOJO(container, "Person", new String[] { "1", "2" }, 0, "<Person><id>1</id><id2>2</id2></Person>"),
                dataModel);
        RoutingRulePOJOPK[] routes = routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        assertEquals(1, routes.length);
        routes = routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "2", "2" }));
        assertEquals(0, routes.length);
    }

    @Test
    public void testMatchRulesOrder() throws Exception {
        RoutingEngine routingEngine = context.getBean(RoutingEngine.class);
        routingEngine.start();
        clearRules();
        RoutingRulePOJO rule1 = new RoutingRulePOJO("testTypeMatchRule1");
        rule1.setConcept("*");
        List<RoutingRuleExpressionPOJO> expressions = Arrays.asList(new RoutingRuleExpressionPOJO("Person", "id",
                RoutingRuleExpressionPOJO.EQUALS, "1"), new RoutingRuleExpressionPOJO("Person", "id2",
                RoutingRuleExpressionPOJO.EQUALS, "2"));
        rule1.setRoutingExpressions(expressions);
        rule1.setExecuteOrder(2);
        routingRule.putRoutingRule(rule1);
        RoutingRulePOJO rule2 = new RoutingRulePOJO("testTypeMatchRule2");
        rule2.setConcept("*");
        expressions = Arrays.asList(new RoutingRuleExpressionPOJO("Person", "id", RoutingRuleExpressionPOJO.EQUALS, "1"),
                new RoutingRuleExpressionPOJO("Person", "id2", RoutingRuleExpressionPOJO.EQUALS, "2"));
        rule2.setRoutingExpressions(expressions);
        rule2.setExecuteOrder(1);
        routingRule.putRoutingRule(rule2);
        item.putItem(new ItemPOJO(container, "Person", new String[] { "1", "2" }, 0, "<Person><id>1</id><id2>2</id2></Person>"),
                dataModel);
        RoutingRulePOJOPK[] routes = routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        assertEquals(2, routes.length);
        assertEquals("testTypeMatchRule2", routes[0].getUniqueId());
        assertEquals("testTypeMatchRule1", routes[1].getUniqueId());
    }

    @Test
    public void testExpiration() throws Exception {
        // Adds a record that matches the rule
        item.putItem(new ItemPOJO(container, "Person", new String[] { "1", "2" }, 0, "<Person><id>1</id><id2>2</id2></Person>"),
                dataModel);
        // Adds a routing rule
        TestRoutingEngine routingEngine = (TestRoutingEngine) context.getBean(RoutingEngine.class);
        routingEngine.start();
        clearRules();
        RoutingRulePOJO rule = new RoutingRulePOJO("testTypeMatchRule");
        rule.setConcept("*");
        rule.setServiceJNDI("amalto/local/service/test/no_op_service");
        routingRule.putRoutingRule(rule);
        // Expired message: put 2 messages, there's only one JMS consumer, service pauses for 400 ms, and expiration is
        // 300 ms
        // -> only 1 message should be consumed.
        NoOpService.setPauseTime(500);
        int previous = routingEngine.getConsumeCallCount();
        RoutingRulePOJOPK[] routes = routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        assertEquals(1, routes.length);
        routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        Thread.sleep(1000); // Give some time to process message
        assertEquals(previous + 1, routingEngine.getConsumeCallCount());
        // Expired message: put 2 messages, there's only one JMS consumer, service pauses for *200* ms, and expiration
        // is 300 ms
        // -> only 2 message should be consumed.
        NoOpService.setPauseTime(200);
        previous = routingEngine.getConsumeCallCount();
        routes = routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        Thread.sleep(1000); // Give some time to process messages
        assertEquals(1, routes.length);
        routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        Thread.sleep(1000); // Give some time to process messages
        assertEquals(previous + 2, routingEngine.getConsumeCallCount());
    }

    @Test
    public void testSynchronousRule() throws Exception {
        RoutingEngine routingEngine = context.getBean(RoutingEngine.class);
        routingEngine.start();
        clearRules();
        RoutingRulePOJO rule1 = new RoutingRulePOJO("testTypeMatchRule1");
        rule1.setConcept("*");
        rule1.setSynchronous(true);
        List<RoutingRuleExpressionPOJO> expressions = Arrays.asList(new RoutingRuleExpressionPOJO("Person", "id1",
                RoutingRuleExpressionPOJO.EQUALS, "1"), new RoutingRuleExpressionPOJO("Person", "id2",
                RoutingRuleExpressionPOJO.EQUALS, "2"));
        rule1.setRoutingExpressions(expressions);
        rule1.setExecuteOrder(2);
        routingRule.putRoutingRule(rule1);
        RoutingRulePOJO rule2 = new RoutingRulePOJO("testTypeMatchRule2");
        rule2.setConcept("*");
        rule2.setSynchronous(true);
        expressions = Arrays.asList(new RoutingRuleExpressionPOJO("Person", "id1", RoutingRuleExpressionPOJO.EQUALS, "1"),
                new RoutingRuleExpressionPOJO("Person", "id2", RoutingRuleExpressionPOJO.EQUALS, "2"));
        rule2.setRoutingExpressions(expressions);
        rule2.setExecuteOrder(1);
        routingRule.putRoutingRule(rule2);
        item.putItem(
                new ItemPOJO(container, "Person", new String[] { "1", "2" }, 0, "<Person><id1>1</id1><id2>2</id2></Person>"),
                dataModel);
        RoutingRulePOJOPK[] routes = routingEngine.route(new ItemPOJOPK(container, "Person", new String[] { "1", "2" }));
        assertEquals(2, routes.length);
        assertEquals("testTypeMatchRule2", routes[0].getUniqueId());
        assertEquals("testTypeMatchRule1", routes[1].getUniqueId());
    }
}
