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

package com.amalto.core.util;

import junit.framework.TestCase;

import java.util.*;

public class SynchronizedNowTest extends TestCase {

    public static final int LOOP_COUNT = 10000;

    public static final int THREAD_COUNT = 4;

    private static class TestSet<T> implements Set<T> {

        private final Set<T> delegate;

        private TestSet(Set<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return delegate.iterator();
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T1> T1[] toArray(T1[] a) {
            return delegate.toArray(a);
        }

        public boolean add(T t) {
            synchronized (delegate) {
                if (delegate.contains(t)) {
                    throw new IllegalArgumentException(t + " already exists.");
                }
                return delegate.add(t);
            }
        }

        @Override
        public boolean remove(Object o) {
            return delegate.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        public boolean addAll(Collection<? extends T> c) {
            return delegate.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return delegate.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return delegate.removeAll(c);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }

    public void testStrictSequence() throws Exception {
        // Test System.currentTimeMillis() (expected to fail).
        TestSet<Long> testSet = new TestSet<Long>(new HashSet<Long>());
        int i;
        for (i = 0; i < LOOP_COUNT; i++) {
            try {
                testSet.add(System.currentTimeMillis());
            } catch (Exception e) {
                break;
            }
        }
        assertNotSame(i, LOOP_COUNT);
        // Test with SynchronizedNow
        SynchronizedNow now = new SynchronizedNow();
        testSet = new TestSet<Long>(new HashSet<Long>());
        for (i = 0; i < LOOP_COUNT; i++) {
            try {
                testSet.add(now.getTime());
            } catch (IllegalArgumentException e) {
                fail("Time did not follow strict sequence.");
            }
        }
    }

    public void testStrictSequenceWithConcurrentAccess() throws Exception {
        // Test with SynchronizedNow
        final SynchronizedNow now = new SynchronizedNow();
        final TestSet<Long> testSet = new TestSet<Long>(new HashSet<Long>());
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < LOOP_COUNT; i++) {
                    try {
                        testSet.add(now.getTime());
                    } catch (IllegalArgumentException e) {
                        fail("Time did not follow strict sequence.");
                    }
                }
            }
        };
        List<Thread> threads = new LinkedList<Thread>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads.add(new Thread(r));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        assertEquals(THREAD_COUNT * LOOP_COUNT, testSet.size());
    }
}
