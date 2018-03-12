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

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import java.util.*;

/**
 * Provides a way to split a query (as {@link com.amalto.core.query.user.Select select}) into multiple sub-queries (one
 * sub query per type). Returned result for consumer is same as
 * {@link com.amalto.core.storage.Storage#fetch(Expression) fetch}).
 */
public class Split {

    private static final Logger LOGGER = Logger.getLogger(Split.class);

    /**
     * <p>
     * Splits the <code>select</code> into sub queries (one sub query per type in {@link Select#getTypes() select types}
     * ), but still returns a {@link com.amalto.core.storage.StorageResults} implementation that performs on-the-fly
     * merge of sub queries results.
     * </p>
     * <p>
     * If select has only one type, this method is equivalent to {@link Storage#fetch(Expression)}.
     * </p>
     * 
     * @param storage The storage to be used to perform sub queries.
     * @param select The select expression to be split into sub queries.
     * @return Merged results for all sub queries.
     */
    public static StorageResults fetchAndMerge(Storage storage, Select select) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage cannot be null.");
        }
        if (select == null || select.getTypes().isEmpty()) {
            throw new IllegalArgumentException("Select cannot be null.");
        }
        if (select.getTypes().size() == 1) { // Optimization: no need to split (one 1 type selected)
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No need to generate sub queries (only one type selected in query: '"
                        + select.getTypes().get(0).getName() + "').");
            }
            return storage.fetch(select);
        } else {
            // Build multiple selects based on types in original select
            List<Select> selects = new ArrayList<Select>(select.getTypes().size());
            Paging paging = select.getPaging();
            // No paging and start from beginning
            for (ComplexTypeMetadata currentType : select.getTypes()) {
                // Only remember the current type in query
                Select typeSelect = select.copy(); // Also copies conditions (if any)
                typeSelect.getTypes().clear();
                typeSelect.getTypes().add(currentType);
                // Start and limit to be handled by ResultsMerge (no need for paging in type's queries).
                typeSelect.getPaging().setStart(0);
                typeSelect.getPaging().setLimit(Integer.MAX_VALUE);
                selects.add(typeSelect);
            }
            // A bit of logging (to ease diagnostic of issues).
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Original query led to " + selects.size() + " sub queries (enable trace for dump).");
            }
            if (LOGGER.isTraceEnabled()) {
                for (Select currentSelect : selects) {
                    currentSelect.accept(new UserQueryDumpConsole(LOGGER, Level.TRACE));
                }
            }
            // Returns a StorageResults that evaluate and merge results from sub queries
            return new ResultsMerge(selects, storage, paging.getStart(), paging.getLimit());
        }
    }

    private static class ResultsMerge implements StorageResults {

        private static final Logger LOGGER = Logger.getLogger(ResultsMerge.class);

        private final Storage storage;

        private final List<Select> selects;

        private final Map<ComplexTypeMetadata, Integer> typeToCount = new LinkedHashMap<ComplexTypeMetadata, Integer>();

        private final int limit;

        public ResultsMerge(List<Select> subQueries, Storage storage, int start, int limit) {
            this.limit = limit;
            if (subQueries.isEmpty()) {
                throw new IllegalArgumentException("Expected several selects, got " + subQueries.size() + ".");
            }
            this.selects = new LinkedList<Select>(subQueries);
            this.storage = storage;
            // Filter selects based on start and limit
            if (start > 0 || limit != Integer.MAX_VALUE) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Query specified paging information (start: " + start + " / limit: " + limit + "). Pruning "
                            + selects.size() + " sub queries.");
                }
                initCounts(selects, storage); // Needs counts to compute paging
                int currentIndex = 0;
                Iterator<Select> iterator = selects.iterator();
                // Remove unneeded queries
                int removedQueryCount = 0;
                while (iterator.hasNext()) {
                    Select next = iterator.next();
                    Integer queryCount = typeToCount.get(next.getTypes().get(0));
                    if (currentIndex + queryCount <= start) {
                        removedQueryCount++;
                        iterator.remove();
                        currentIndex += queryCount;
                    } else {
                        break;
                    }
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Removed " + removedQueryCount + " first sub queries (start index too high).");
                }
                // Test if a new start is needed for query
                if (currentIndex < start) {
                    int queryStart = start - currentIndex;
                    Select select = selects.get(0);
                    Paging paging = select.getPaging();
                    paging.setStart(queryStart);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("First sub query starts at result #" + queryStart + " (to match start index of " + start
                                + ".");
                    }
                }
                // Takes care of limit
                int currentLimit = 0;
                removedQueryCount = 0;
                if (limit != Integer.MAX_VALUE) {
                    iterator = selects.iterator();
                    while (iterator.hasNext()) {
                        Select next = iterator.next();
                        if (currentLimit >= limit) {
                            removedQueryCount++;
                            iterator.remove(); // No need this query (already too many results according to limit).
                        } else {
                            Integer queryCount = typeToCount.get(next.getTypes().get(0)) - next.getPaging().getStart();
                            if (currentLimit + queryCount < limit) {
                                next.getPaging().setLimit(queryCount);
                            } else {
                                next.getPaging().setLimit(limit - currentLimit);
                            }
                            currentLimit += queryCount;
                        }
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Removed " + removedQueryCount
                                + " sub queries (no need because already enough sub queries to serve limit).");
                    }
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Pruned sub queries to match paging requirements (now " + selects.size() + " sub queries).");
                }
            }
        }

        // Compute count for each sub query
        private void initCounts(List<Select> selects, Storage storage) {
            if (typeToCount.isEmpty()) {
                for (Select select : selects) {
                    StorageResults results = storage.fetch(select);
                    try {
                        typeToCount.put(select.getTypes().get(0), results.getCount());
                    } finally {
                        results.close();
                    }
                }
            }
        }

        @Override
        public int getSize() {
            if (limit != Integer.MAX_VALUE) {
                return limit;
            }
            int size = 0;
            for (Select select : selects) {
                StorageResults results = storage.fetch(select);
                try {
                    size += results.getSize();
                } finally {
                    results.close();
                }
            }
            return size;
        }

        @Override
        public int getCount() {
            initCounts(selects, storage); // Needs counts to compute result
            int count = 0;
            for (Integer currentCount : typeToCount.values()) {
                count += currentCount;
            }
            return count;
        }

        @Override
        public void close() {
            // Nothing to do
        }

        @Override
        public Iterator<DataRecord> iterator() {
            if (selects.isEmpty()) {
                return Collections.<DataRecord>emptySet().iterator(); // TMDM-7290: Paging filtering left no sub query, returns empty result.
            }
            return new MergeIterator(selects);
        }

        private class MergeIterator implements Iterator<DataRecord> {

            private final Iterator<Select> selects;

            private Iterator<DataRecord> currentIterator;

            public MergeIterator(List<Select> selectList) {
                selects = selectList.iterator();
            }

            @Override
            public boolean hasNext() {
                while ((currentIterator == null || !currentIterator.hasNext()) && selects.hasNext()) {
                    Select next = selects.next();
                    currentIterator = storage.fetch(next).iterator();
                }
                return currentIterator != null && (currentIterator.hasNext() || selects.hasNext());
            }

            @Override
            public DataRecord next() {
                return currentIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
}
