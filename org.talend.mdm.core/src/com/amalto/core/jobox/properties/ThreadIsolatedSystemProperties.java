/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.properties;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * <p>
 * Implementation of {@link Properties} that keeps a different instance for each registered ("isolated") thread and
 * default properties for non registered threads ("integrated").
 * </p>
 * <p>
 * Note: implementation is designed to be thread safe.
 * </p>
 *
 * @see #isolateThread(Thread, java.util.Properties)
 * @see #integrateThread(Thread)
 */
public class ThreadIsolatedSystemProperties extends Properties {

    private static final Map<ClassLoader, Properties> threadProperties = new HashMap<ClassLoader, Properties>();

    private static final Logger LOGGER = Logger.getLogger(ThreadIsolatedSystemProperties.class);

    private static ThreadIsolatedSystemProperties instance;

    private final Properties defaultSystemProperties;

    private ThreadIsolatedSystemProperties(Properties defaultSystemProperties) {
        this.defaultSystemProperties = defaultSystemProperties;
    }

    /**
     * @return Returns {@link ThreadIsolatedSystemProperties} singleton instance.
     */
    public static ThreadIsolatedSystemProperties getInstance() {
        if (instance == null) {
            instance = new ThreadIsolatedSystemProperties(System.getProperties());
        }
        return instance;
    }

    /**
     * "Isolates" a thread: this means calls to this class methods will return thread-specific properties (default properties
     * for the thread being the <code>threadDefault</code> parameter).
     *
     * @param thread        A {@link Thread} (could be {@link Thread#currentThread()} for instance).
     * @param threadDefault Default properties for the <code>thread</code>. Please note registered thread will work on
     *                      <b>a copy</b> of the <code>threadDefault</code> parameter.
     */
    public void isolateThread(Thread thread, Properties threadDefault) {
        synchronized (threadProperties) {
            if (threadDefault == this) {
                // Prevents infinite loops for system property lookup.
                throw new IllegalArgumentException("Cannot accept instance " + threadDefault.getClass().getName() + " as parameter."); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Isolating thread '" + thread.getName() + "'");  //$NON-NLS-1$  //$NON-NLS-2$
            }
            threadProperties.put(thread.getContextClassLoader(), threadDefault);
        }
    }

    /**
     * "Re-integrates" a thread: this means <code>thread</code> will now work on shared properties.
     *
     * @param thread The {@link Thread} to re-integrate.
     */
    public void integrateThread(Thread thread) {
        synchronized (threadProperties) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Integrating thread '" + thread.getName() + "'");  //$NON-NLS-1$  //$NON-NLS-2$
            }
            threadProperties.remove(thread.getContextClassLoader());
        }
    }

    /**
     * @param thread A {@link Thread} instance.
     * @return Returns current thread {@link Properties} instance.
     */
    public Properties getThreadProperties(Thread thread) {
        synchronized (threadProperties) {
            if (thread == null) {
                return defaultSystemProperties;
            }

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Requesting for thread system properties '" + thread.getName() + "'");  //$NON-NLS-1$  //$NON-NLS-2$
            }

            Properties currentThreadProperties = threadProperties.get(thread.getContextClassLoader());
            if (currentThreadProperties == null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Thread '" + thread.getName() + "' is not isolated. Return default properties");  //$NON-NLS-1$  //$NON-NLS-2$
                }
                return defaultSystemProperties;
            } else {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Thread '" + thread.getName() + "' is isolated. Return thread properties (dump below).");  //$NON-NLS-1$  //$NON-NLS-2$
                    LOGGER.trace(currentThreadProperties.toString());
                }
                return currentThreadProperties;
            }
        }
    }

    /**
     * @return Returns current running thread {@link Properties} instance.
     */
    Properties getThreadProperties() {
        return getThreadProperties(Thread.currentThread());
    }

    /*
     * DELEGATE METHODS for java.util.Hashtable
     */
    @Override
    public synchronized void putAll(Map<?, ?> map) {
        Set<? extends Map.Entry<?, ?>> entries = map.entrySet();
        for (Map.Entry<?, ?> entry : entries) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        return setProperty(((String) key), ((String) value));
    }

    @Override
    public synchronized int size() {
        return getThreadProperties().size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return getThreadProperties().isEmpty();
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return getThreadProperties().keys();
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return getThreadProperties().elements();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return getThreadProperties().contains(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return getThreadProperties().containsValue(o);
    }

    @Override
    public synchronized boolean containsKey(Object o) {
        return getThreadProperties().containsKey(o);
    }

    @Override
    public synchronized Object get(Object o) {
        return getThreadProperties().get(o);
    }

    @Override
    public synchronized Object remove(Object o) {
        return getThreadProperties().remove(o);
    }

    @Override
    public synchronized void clear() {
        getThreadProperties().clear();
    }

    @Override
    public synchronized Object clone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized String toString() {
        return getThreadProperties().toString();
    }

    @Override
    public Set<Object> keySet() {
        return getThreadProperties().keySet();
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return getThreadProperties().entrySet();
    }

    @Override
    public Collection<Object> values() {
        return getThreadProperties().values();
    }

    /*
    * DELEGATE METHODS for Properties
    */
    @Override
    public Object setProperty(String key, String value) {
        return getThreadProperties().setProperty(key, value);
    }

    @Override
    public void load(Reader reader) throws IOException {
        getThreadProperties().load(reader);
    }

    @Override
    public void load(InputStream inStream) throws IOException {
        getThreadProperties().load(inStream);
    }

    @Override
    @Deprecated
    public void save(OutputStream out, String comments) {
        getThreadProperties().save(out, comments);
    }

    @Override
    public void store(Writer writer, String comments) throws IOException {
        getThreadProperties().store(writer, comments);
    }

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        getThreadProperties().store(out, comments);
    }

    @Override
    public void loadFromXML(InputStream in) throws IOException {
        getThreadProperties().loadFromXML(in);
    }

    @Override
    public void storeToXML(OutputStream os, String comment) throws IOException {
        getThreadProperties().storeToXML(os, comment);
    }

    @Override
    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        getThreadProperties().storeToXML(os, comment, encoding);
    }

    @Override
    public String getProperty(String key) {
        return getThreadProperties().getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return getThreadProperties().getProperty(key, defaultValue);
    }

    @Override
    public Enumeration<?> propertyNames() {
        return getThreadProperties().propertyNames();
    }

    @Override
    public Set<String> stringPropertyNames() {
        return getThreadProperties().stringPropertyNames();
    }

    @Override
    public void list(PrintStream out) {
        getThreadProperties().list(out);
    }

    @Override
    public void list(PrintWriter out) {
        getThreadProperties().list(out);
    }
}
