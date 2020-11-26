// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.server;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.web.context.support.XmlWebApplicationContext;


/**
 * Application Context could disable/enable spring schema validation by JVM Parameter "com.talend.mdm.springSchemaValidation"
 * <ul>
 * <li>Not set -- ENABLED (be default)</li>
 * <li>-Dcom.talend.mdm.springSchemaValidation=true   --ENABLED</li>
 * <li>-Dcom.talend.mdm.springSchemaValidation=false  --DISABLED</li>
 * </ul>
 * created by pwlin on Nov 25, 2020
 * 
 */
public class MDMXmlWebApplicationContext extends XmlWebApplicationContext {
    
    private static final String SPRING_SCHEMA_VALIDATION = "com.talend.mdm.springSchemaValidation";

    private static boolean getSpringSchemaValidation() {
        String validation = System.getProperty(SPRING_SCHEMA_VALIDATION);
        // only set "-Dcom.talend.mdm.springSchemaValidation=false" will disable it
        if (validation != null && validation.equalsIgnoreCase("false")) {
            return false;
        }
        return true;
    }

    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        // Create a new XmlBeanDefinitionReader for the given BeanFactory.
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

        // Configure the bean definition reader with this context's
        // resource loading environment.
        beanDefinitionReader.setValidating(getSpringSchemaValidation());
        beanDefinitionReader.setEnvironment(getEnvironment());
        beanDefinitionReader.setResourceLoader(this);
        beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

        // Allow a subclass to provide custom initialization of the reader,
        // then proceed with actually loading the bean definitions.
        initBeanDefinitionReader(beanDefinitionReader);
        loadBeanDefinitions(beanDefinitionReader);
    }
}
