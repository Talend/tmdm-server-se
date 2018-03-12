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

package com.amalto.core.storage.hibernate;

// TODO To use this class, we need Lucene 3.0+... but version of Hibernate Search is too old for this.
class MatchingClausesVisitor {
}

/*
class MatchingClausesVisitor extends Scorer.ScorerVisitor<org.apache.lucene.search.Query, org.apache.lucene.search.Query, Scorer> {

    private final List<Query> matchedQueries = new LinkedList<Query>();

    @Override
    public void visitRequired(Query parent, Query child, Scorer scorer) {
        if (child instanceof BooleanQuery) {
            for (Object booleanClauseObject : ((BooleanQuery) child)) {
                if (booleanClauseObject instanceof BooleanClause) {
                    if (!booleanClauseObject.toString().isEmpty()) {
                        matchedQueries.add(((BooleanClause) booleanClauseObject).getQuery());
                    }
                } else {
                    throw new NotImplementedException("No support for clauses " + booleanClauseObject.getClass().getName());
                }
            }
        } else {
            throw new NotImplementedException("No support for queries " + child.getClass().getName());
        }
    }

    public List<Query> getMatchedQueries() {
        return matchedQueries;
    }
}
*/
