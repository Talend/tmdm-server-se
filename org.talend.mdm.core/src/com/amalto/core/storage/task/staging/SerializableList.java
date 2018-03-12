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

package com.amalto.core.storage.task.staging;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SerializableList<T> implements List<T> {

    private final List<T> list;

    private final String rootElement;

    private final String itemElement;

    private SerializableList(List<T> list, String rootElement, String itemElement) {
        this.list = list;
        this.rootElement = rootElement;
        this.itemElement = itemElement;
    }

    public static <T> SerializableList<T> create(List<T> list, String rootElement, String itemElement) {
        return new SerializableList<T>(list, rootElement, itemElement);
    }

    public String getRootElement() {
        return rootElement;
    }

    public String getItemElement() {
        return itemElement;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return list.toArray(ts);
    }

    public boolean add(T t) {
        return list.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        return list.containsAll(objects);
    }

    public boolean addAll(Collection<? extends T> ts) {
        return list.addAll(ts);
    }

    public boolean addAll(int i, Collection<? extends T> ts) {
        return list.addAll(i, ts);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        return list.removeAll(objects);
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        return list.retainAll(objects);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean equals(Object o) {
        return list.equals(o);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public T get(int i) {
        return list.get(i);
    }

    public T set(int i, T t) {
        return list.set(i, t);
    }

    public void add(int i, T t) {
        list.add(i, t);
    }

    @Override
    public T remove(int i) {
        return list.remove(i);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        return list.listIterator(i);
    }

    @Override
    public List<T> subList(int i, int i1) {
        return list.subList(i, i1);
    }

}
