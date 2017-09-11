/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
class MatchedWordsCollector {
}

/*
class MatchedWordsCollector extends Collector {

    private final MatchingClausesVisitor matchingClauses = new MatchingClausesVisitor();

    private final Set<String> matchedWords = new HashSet<String>();

    private final IndexReader reader;

    private boolean hasComputedResults = false;

    MatchedWordsCollector(IndexReader reader) {
        this.reader = reader;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        if (!hasComputedResults) {
            scorer.visitScorers(matchingClauses);
            List<Query> matchedClauses = matchingClauses.getMatchedQueries();
            for (Query matchedQuery : matchedClauses) {
                if (matchedQuery instanceof ConstantScoreQuery) {
                    ConstantScoreQuery query = (ConstantScoreQuery) matchedQuery;

                    DocIdSet matchedDocuments = query.getFilter().getDocIdSet(reader);
                    DocIdSetIterator matchedDocumentIterator = matchedDocuments.iterator();
                    String fieldName = StringUtils.substringBefore(query.getFilter().toString(), ":");
                    while (matchedDocumentIterator.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                        String value = FieldCache.DEFAULT.getStrings(reader, fieldName)[matchedDocumentIterator.docID()];
                        if (value != null) {
                            matchedWords.add(value);
                        } else {
                            throw new IllegalStateException("Unexpected");
                        }
                    }
                } else if (matchedQuery instanceof TermQuery) {
                    TermQuery query = (TermQuery) matchedQuery;
                    matchedWords.add(query.getTerm().text());
                } else {
                    throw new NotImplementedException("No support for query " + matchedQuery.getClass().getName());
                }
            }

            hasComputedResults = true;
        }
    }

    @Override
    public void collect(int i) throws IOException {
        // Nothing to do
    }

    @Override
    public void setNextReader(IndexReader indexReader, int i) throws IOException {
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    public Set<String> getMatchedWords() {
        return matchedWords;
    }
}
*/
