/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.io.StringReader;
import java.io.StringWriter;

import com.amalto.core.objects.marshalling.MarshallingFactory;
import com.amalto.xmlserver.interfaces.WhereCondition;

public class RoleWhereCondition {
	String leftPath;
	String operator;
	String rightValueOrPath;
	String predicate;
	public String getLeftPath() {return leftPath;}
	public void setLeftPath(String leftPath) {this.leftPath = leftPath;}
	public String getOperator() {return operator;}
	public void setOperator(String operator) {	this.operator = operator;}
	public String getPredicate() {return predicate;}
	public void setPredicate(String predicate) {this.predicate = predicate;}
	public String getRightValueOrPath() {return rightValueOrPath;}
	public void setRightValueOrPath(String righValueOrPath) {this.rightValueOrPath = righValueOrPath;}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		try {
		    MarshallingFactory.getInstance().getMarshaller(this.getClass()).marshal(this, sw);
		} catch (Exception e) {
			String err = "toString() ERROR MARSHALLING WhereCondition: "+e.getClass().getName()+": "+e.getLocalizedMessage();
			org.apache.log4j.Category.getInstance(this.getClass()).error(err);
			throw new RuntimeException(err); 
		}
		return sw.toString();
	}
	
	public static RoleWhereCondition parse(String marshalledWC) throws XtentisException{
		RoleWhereCondition rwc = null;
		try {
            if (marshalledWC == null || marshalledWC.trim().length() == 0) {
                return null;
            }
            rwc = MarshallingFactory.getInstance().getUnmarshaller(RoleWhereCondition.class).unmarshal(new StringReader(marshalledWC));
		} catch (Exception e) {
			String err = "parse() ERROR UNMARSHALLING WhereCondition \""+marshalledWC+"\": "+e.getClass().getName()+": "+e.getLocalizedMessage();
			org.apache.log4j.Category.getInstance(RoleWhereCondition.class).error(err);
			throw new XtentisException(err); 
		}
		return rwc;
	}
	
	public WhereCondition toWhereCondition() {
		String operator = WhereCondition.CONTAINS;
		if (this.getOperator().equals("Contains")) {
            operator = WhereCondition.CONTAINS;
        } else if (this.getOperator().equals("=")) {
            operator = WhereCondition.EQUALS;
        } else if (this.getOperator().equals(">")) {
            operator = WhereCondition.GREATER_THAN;
        } else if (this.getOperator().equals(">=")) {
            operator = WhereCondition.GREATER_THAN_OR_EQUAL;
        } else if (this.getOperator().equals("<")) {
            operator = WhereCondition.LOWER_THAN;
        } else if (this.getOperator().equals("<=")) {
            operator = WhereCondition.LOWER_THAN_OR_EQUAL;
        } else if (this.getOperator().equals("!=")) {
            operator = WhereCondition.NOT_EQUALS;
        } else if (this.getOperator().equals("Starts With")) {
            operator = WhereCondition.STARTSWITH;
        } else if (this.getOperator().equals("No Operator")) {
            operator = WhereCondition.NO_OPERATOR;
        } else if (this.getOperator().equals("Is Empty Or Null")) {
            operator = WhereCondition.EMPTY_NULL;
        }
		
		String predicate = WhereCondition.PRE_NONE;
		if (this.getPredicate().equalsIgnoreCase("and")) {
            predicate = WhereCondition.PRE_AND;
        } else if (this.getPredicate().equals("exactly")) {
            predicate = WhereCondition.PRE_EXACTLY;
        } else if (this.getPredicate().equals("")) {
            predicate = WhereCondition.PRE_NONE;
        } else if (this.getPredicate().equalsIgnoreCase("not")) {
            predicate = WhereCondition.PRE_NOT;
        } else if (this.getPredicate().equalsIgnoreCase("or")) {
            predicate = WhereCondition.PRE_OR;
        } else if (this.getPredicate().equals("strict and")) {
            predicate = WhereCondition.PRE_STRICTAND;
        }

        return new WhereCondition(
				this.getLeftPath(),
				operator,
				this.getRightValueOrPath(),
				predicate,
				false
		);
		
	}
}
