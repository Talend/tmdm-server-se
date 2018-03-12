/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.amalto.core.storage.CloseableIterator;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import com.amalto.core.query.user.*;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;

public class InstanceScrollOptimization extends StandardQueryHandler {

    public InstanceScrollOptimization(Storage storage, MappingRepository mappings, TableResolver resolver,
            StorageClassLoader storageClassLoader, Session session, Select select, List<TypedExpression> selectedFields,
            Set<ResultsCallback> callbacks) {
        super(storage, mappings, resolver, storageClassLoader, session, select, selectedFields, callbacks);
    }

    @Override
    public StorageResults visit(Select select) {
        ComplexTypeMetadata mainType = select.getTypes().get(0);
        Paging paging = select.getPaging();
        int start = paging.getStart();
        if (start != 0) {
            throw new IllegalStateException(
                    "This optimization is only use for iterating over all instances (start is not supported).");
        }
        if (paging.getLimit() != Integer.MAX_VALUE) {
            // Optimization *could* work when page size is set... however it also means the analysis of the query is
            // redirecting to the wrong optimization, and this is an issue.
            throw new UnsupportedOperationException();
        }
        // Perform same query as input *but* adds an order by clause 'by id' that allows efficient filtering (iterator
        // should
        // only return unique results).
        Select copy = select.copy();
        Collection<FieldMetadata> keyFields = mainType.getKeyFields();
        for (FieldMetadata keyField : keyFields) {
            copy.addOrderBy(new OrderBy(new Field(keyField), OrderBy.Direction.ASC));
        }
        Criteria criteria = createCriteria(copy); // Perform the query with order by id.
        // Create the filtered result iterator
        CloseableIterator<DataRecord> nonFilteredIterator = new ScrollableIterator(mappings, storageClassLoader,
                criteria.scroll(), callbacks);
        Predicate isUniqueFilter = new UniquePredicate(); // Unique filter returns only different instances
        Iterator filteredIterator = IteratorUtils.filteredIterator(nonFilteredIterator, isUniqueFilter);
        FilteredIteratorWrapper uniqueIterator = new FilteredIteratorWrapper(nonFilteredIterator, filteredIterator, select);
        return new HibernateStorageResults(storage, select, uniqueIterator);
    }

    private static class UniquePredicate implements Predicate {

        public Object lastObject;

        @Override
        public boolean evaluate(Object object) {
            if (object.equals(lastObject)) {
                return false; // Provided object same as previous one: filter out.
            }
            lastObject = object;
            return true;
        }
    }

    // Internal class to provide a closeable iterator and filtered results.
    private static class FilteredIteratorWrapper implements CloseableIterator<DataRecord> {

        private final CloseableIterator<DataRecord> nonFilteredIterator;

        private final Iterator filteredIterator;

        private final Select select;

        private boolean isClosed;

        public FilteredIteratorWrapper(CloseableIterator<DataRecord> nonFilteredIterator, Iterator filteredIterator, Select select) {
            this.nonFilteredIterator = nonFilteredIterator;
            this.filteredIterator = filteredIterator;
            this.select = select;
        }

        @Override
        public void close() throws IOException {
            isClosed = true;
            nonFilteredIterator.close();
        }

        public boolean hasNext() {
            return !isClosed && filteredIterator.hasNext();
        }

        public DataRecord next() {
            if (isClosed) {
                throw new IllegalStateException("Can not move to next: iterator is closed.");
            }
            return (DataRecord) filteredIterator.next();
        }

        public void remove() {
            if (isClosed) {
                throw new IllegalStateException("Can not remove: iterator is closed.");
            }
            filteredIterator.remove();
        }

        @Override
        public String toString() {
            StringBuilder conditionToString = new StringBuilder();
            UserQueryDumpConsole.DumpPrinter dumpPrinter = new StringBuilderPrinter(conditionToString);
            Condition condition = select.getCondition();
            if (condition != null) {
                condition.accept(new UserQueryDumpConsole(dumpPrinter));
            }
            return "FilteredIteratorWrapper{" + "type=" + select.getTypes().get(0).getName() + ", condition="
                    + conditionToString.toString() + '}';
        }
    }
}
