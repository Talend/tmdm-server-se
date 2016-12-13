/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.query.user;

/**
 * Represents a condition that returns always the same value. This can be used as filler for binary logic operators or
 * to invalidate queries (i.e. "select type where false" should return no result).
 */
public interface ConstantCondition extends Condition {

    /**
     * @return The condition constant value (<code>true</code> is condition is true, <code>false</code> otherwise).
     */
    boolean value();

}
