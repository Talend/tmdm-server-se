/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.webservice;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="WSWhereCondition")
public class WSWhereCondition {
    protected java.lang.String leftPath;
    protected com.amalto.core.webservice.WSWhereOperator operator;
    protected java.lang.String rightValueOrPath;
    protected com.amalto.core.webservice.WSStringPredicate stringPredicate;
    protected boolean spellCheck;
    
    public WSWhereCondition() {
    }
    
    public WSWhereCondition(java.lang.String leftPath, com.amalto.core.webservice.WSWhereOperator operator, java.lang.String rightValueOrPath, com.amalto.core.webservice.WSStringPredicate stringPredicate, boolean spellCheck) {
        this.leftPath = leftPath;
        this.operator = operator;
        this.rightValueOrPath = rightValueOrPath;
        this.stringPredicate = stringPredicate;
        this.spellCheck = spellCheck;
    }
    
    public java.lang.String getLeftPath() {
        return leftPath;
    }
    
    public void setLeftPath(java.lang.String leftPath) {
        this.leftPath = leftPath;
    }
    
    public com.amalto.core.webservice.WSWhereOperator getOperator() {
        return operator;
    }
    
    public void setOperator(com.amalto.core.webservice.WSWhereOperator operator) {
        this.operator = operator;
    }
    
    public java.lang.String getRightValueOrPath() {
        return rightValueOrPath;
    }
    
    public void setRightValueOrPath(java.lang.String rightValueOrPath) {
        this.rightValueOrPath = rightValueOrPath;
    }
    
    public com.amalto.core.webservice.WSStringPredicate getStringPredicate() {
        return stringPredicate;
    }
    
    public void setStringPredicate(com.amalto.core.webservice.WSStringPredicate stringPredicate) {
        this.stringPredicate = stringPredicate;
    }
    
    public boolean isSpellCheck() {
        return spellCheck;
    }
    
    public void setSpellCheck(boolean spellCheck) {
        this.spellCheck = spellCheck;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WSWhereCondition))
            return false;

        WSWhereCondition that = (WSWhereCondition) o;

        if (spellCheck != that.spellCheck)
            return false;
        if (leftPath != null ? !leftPath.equals(that.leftPath) : that.leftPath != null)
            return false;
        if (operator != null ? !operator.equals(that.operator) : that.operator != null)
            return false;
        if (rightValueOrPath != null ? !rightValueOrPath.equals(that.rightValueOrPath) : that.rightValueOrPath != null)
            return false;
        if (stringPredicate != null ? !stringPredicate.equals(that.stringPredicate) : that.stringPredicate != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = leftPath != null ? leftPath.hashCode() : 0;
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        result = 31 * result + (rightValueOrPath != null ? rightValueOrPath.hashCode() : 0);
        result = 31 * result + (stringPredicate != null ? stringPredicate.hashCode() : 0);
        result = 31 * result + (spellCheck ? 1 : 0);
        return result;
    }
}
