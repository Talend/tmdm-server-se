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

import java.io.Serializable;


/**
 * @author bgrieder
 * 
 */
public class RoutingRuleExpressionPOJO implements Serializable{
   
	public final static int CONTAINS =0;
	public final static int MATCHES =1;
	public final static int STARTSWITH =2;
	//public final static int JOINS =3;
    public final static int EQUALS = 4;
    public final static int NOT_EQUALS = 5;
    public final static int GREATER_THAN = 6;
    public final static int GREATER_THAN_OR_EQUAL = 7;
    public final static int LOWER_THAN = 8;
    public final static int LOWER_THAN_OR_EQUAL = 9;
    public final static int IS_NULL = 10;
    public final static int IS_NOT_NULL = 11;
	
    private String name;
    private String xpath;
    private int operator;
    private String value;
        
    
    public RoutingRuleExpressionPOJO() {
    	//for the castor
    }

    /**
	 * @param xpath
	 * @param operator
	 * @param value
	 */
	public RoutingRuleExpressionPOJO(String name, String xpath, int operator, String value) {
		this.name = name;
		this.xpath = xpath;
		this.operator = operator;
		this.value = value;
	}
	
    /**
	 * @return Returns the name of the rule
	 */
	public String getName() {
		return name;
	}

    
    public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the operator.
	 */
	public int getOperator() {
		return operator;
	}



	/**
	 * @param operator The operator to set.
	 */
	public void setOperator(int operator) {
		this.operator = operator;
	}



	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}



	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}



	/**
	 * @return Returns the xpath.
	 */
	public String getXpath() {
		return xpath;
	}



	/**
	 * @param xpath The xpath to set.
	 */
	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

}
