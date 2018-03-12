/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.query.user;

public class At implements Expression {

    private final long dateTime;

    private Swing swing = Swing.CLOSEST;

    public At(long dateTime) {
        this.dateTime = dateTime;
    }

    public long getDateTime() {
        return dateTime;
    }

    public Swing getSwing() {
        return swing;
    }

    public void setSwing(Swing swing) {
        this.swing = swing;
    }

    @Override
    public Expression normalize() {
        return this;
    }

    @Override
    public boolean cache() {
        return false;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public enum Swing {
        CLOSEST,
        BEFORE,
        AFTER
    }
}
