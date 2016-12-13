/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.groovy;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;

public class EmbedGroovy {

	private static final String VARIABLE_INPUT = "variableInput";
	private Binding binding = new Binding();
	
	public EmbedGroovy() {
		
	}
	
    public EmbedGroovy(String defaultParameterValue) {

		String[] paramNames = { VARIABLE_INPUT };
		Object[] paramValues = { defaultParameterValue };
		this.setParameters(paramNames, paramValues);
	}

	public Object getProperty(String name) {

		return binding.getProperty(name);

	}

	public void setParameters(String[] paramNames, Object[] paramValues) {

		int len = paramNames.length;

		if (len != paramValues.length) {

			System.out.println("parameters not match!");

		}

		for (int i = 0; i < len; i++) {

			binding.setProperty(paramNames[i], paramValues[i]);

		}

	}

	public Object runScriptFile(String scriptName) {

		GroovyShell shell = new GroovyShell(binding);

		try {

			return shell.evaluate(new File(scriptName));

		} catch (Exception e) {

			e.printStackTrace();

			return null;

		}

	}
	
	public Object runScript(String script) {

		GroovyShell shell = new GroovyShell(binding);

		try {

			return shell.evaluate(script);

		} catch (Exception e) {

			e.printStackTrace();

			return null;

		}

	}

	public static void main(String[] args) {

		EmbedGroovy embedGroovy = new EmbedGroovy();

		//Object result = embedGroovy.runScriptFile("src/Foo.groovy");
		Object result = embedGroovy.runScript(
		"def records = new XmlParser().parseText('<records><car><name>HSV Maloo</name></car></records>');" +
		"\ndef carName=records.car.name.text();" +
		"\nprintln carName; " +
		"\nreturn carName");

		System.out.println("Result:"+result);

		//System.out.println(embedGroovy.getProperty("foo"));

	}

}
