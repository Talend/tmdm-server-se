/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query.user;

/**
 *
 */
public interface Predicate extends Visitable {

    Predicate CONTAINS = new Contains();

    class Contains implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    Predicate STARTS_WITH = new StartsWith();

    class StartsWith implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    Predicate GREATER_THAN = new GreaterThan();

    static class GreaterThan implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    Predicate GREATER_THAN_OR_EQUALS = new GreaterThanOrEquals();

    static class GreaterThanOrEquals implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    Predicate LOWER_THAN = new LowerThan();

    static class LowerThan implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    Predicate LOWER_THAN_OR_EQUALS = new LowerThanOrEquals();

    static class LowerThanOrEquals implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    Predicate AND = new And();

    static class And implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    Predicate EQUALS = new Equals();

    static class Equals implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    Predicate OR = new Or();

    static class Or implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    Predicate NOT = new Not();

    static class Not implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }

    Predicate IN = new In();

    static class In implements Predicate {
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

    }
}
