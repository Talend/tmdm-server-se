/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package talend.core.transformer.plugin.v2.tiscall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.mock.env.MockEnvironment;

import com.amalto.core.objects.Plugin;
import com.amalto.core.util.PluginRegistry;
import com.amalto.core.util.XtentisException;

@SuppressWarnings("nls")
public class TISCallTransformerPluginBeanTest {

    private static AbstractApplicationContext context;

    @BeforeClass
    public static void setup() {
        System.setProperty("mdm.root.ignoreIfNotFound", "true");
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setResourceLoader(new PathMatchingResourcePatternResolver());
        MockEnvironment env = new MockEnvironment();
        env.setProperty("mdm.routing.engine.broker.url", "vm://localhost?broker.persistent=false");
        env.setProperty("mdm.routing.engine.consumers","1");
        env.setProperty("routing.engine.max.execution.time.millis","300000");
        context.setEnvironment(env);
        context.load("classpath:mdm-test-context.xml");
        // FIXME Setting default-lazy-init on the top level beans element seems not applied to beans inside an imported
        // resource
        // Workaround: set all beans to be lazy-init programmatically
        // See also https://gist.github.com/eeichinger/1979033 as an alternative
        for (String beanName : context.getBeanDefinitionNames()) {
            context.getBeanDefinition(beanName).setLazyInit(true);
        }
        context.refresh();
        TISCallTransformerPluginBeanTest.context = context;
    }

    @AfterClass
    public static void tearDown() {
        context.destroy();
        System.setProperty("mdm.root.ignoreIfNotFound", "false");
    }

    @Test
    public void testGetPlugins() {
        PluginRegistry registry = (PluginRegistry) context.getBean("pluginRegistry");
        Plugin plugin = registry.getPlugin(TISCallTransformerPluginBean.PLUGIN_NAME);
        assertNotNull(plugin);
        assertTrue(plugin instanceof TISCallTransformerPluginBean);
        try {
            plugin = registry.getPlugin("amalto/local/transformer/plugin/xslt");
            assertNull(plugin);     // xslt component is not scanned
        } catch (NoSuchBeanDefinitionException e) {

        }
    }

    @Test
    public void testBuildParameters() throws XtentisException {
        String expetedResult = "<results>\n<Agency>\n<Region>Lausanne</Region>\n<Etablissement><Adresse>Paris</Adresse></Etablissement>\n</Agency>\n</results>\n";
        List<String[]> list = new ArrayList<String[]>();
        String[] values = { "Lausanne", "Paris" };
        list.add(values);
        ConceptMappingParam conceptMappingParam = new ConceptMappingParam("Agency", "{p0:Region,p1:\"Etablissement/Adresse\"}");
        TISCallTransformerPluginBean bean = new TISCallTransformerPluginBean();
        String parameter = bean.buildParameters(list, conceptMappingParam);
        assertEquals(expetedResult, parameter);
    }
}
