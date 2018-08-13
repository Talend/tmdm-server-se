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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource;

public abstract class StorageClassLoader extends ClassLoader {

    public static final String MAPPING_PUBLIC_ID = "-//Hibernate/Hibernate Mapping DTD 3.0//EN"; //$NON-NLS-1$

    public static final String CONFIGURATION_PUBLIC_ID = "-//Hibernate/Hibernate Configuration DTD 3.0//EN"; //$NON-NLS-1$

    public static final String HIBERNATE_CONFIG = "hibernate.cfg.xml"; //$NON-NLS-1$

    public static final String EHCACHE_XML_CONFIG = "ehcache.xml";

    private final Map<Thread, Stack<ClassLoader>> previousClassLoaders = new HashMap<Thread, Stack<ClassLoader>>();

    static final String HIBERNATE_CONFIG_TEMPLATE = "hibernate.cfg.template.xml"; //$NON-NLS-1$

    static final String HIBERNATE_MAPPING = "hibernate.hbm.xml"; //$NON-NLS-1$

    static final String HIBERNATE_MAPPING_TEMPLATE = "hibernate.hbm.template.xml"; //$NON-NLS-1$

    final Map<String, ComplexTypeMetadata> knownTypes = new HashMap<String, ComplexTypeMetadata>();

    final Map<String, Class<? extends Wrapper>> registeredClasses = new TreeMap<String, Class<? extends Wrapper>>();

    protected Collection<ComplexTypeMetadata> entityComplexTypes;

    final String storageName;

    final StorageType type;

    RDBMSDataSource dataSource;

    TableResolver resolver;

    private boolean isClosed;

    StorageClassLoader(ClassLoader parent,
                       String storageName,
                       StorageType type) {
        super(parent);
        this.storageName = storageName;
        this.type = type;
    }

    protected abstract InputStream generateHibernateMapping();

    protected abstract InputStream generateHibernateConfig();

    protected abstract InputStream generateEhCacheConfig();

    @Override
    public InputStream getResourceAsStream(String name) {
        assertNotClosed();
        try {
            if (HIBERNATE_CONFIG.equals(name)) {
                return generateHibernateConfig();
            } else if (HIBERNATE_MAPPING.equals(name)) {
                return generateHibernateMapping();
            } else if (EHCACHE_XML_CONFIG.equals(name)) {
                return generateEhCacheConfig();
            }
        } catch (Exception e) {
            // Hibernate tends to hide errors when getResourceAsStream fails.
            Logger.getLogger(StorageClassLoader.class).error("Error during dynamic creation of configurations", e);
        }
        return super.getResourceAsStream(name);
    }

    private void assertNotClosed() {
        if (isClosed) {
            throw new IllegalStateException("Class loader was closed.");
        }
    }

    @Override
    public URL getResource(String name) {
        assertNotClosed();
        if (EHCACHE_XML_CONFIG.equals(name)) {
            try {
                return new URL("http", "fakehost", 0, '/' + EHCACHE_XML_CONFIG, new URLStreamHandler() { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    @Override
                    protected URLConnection openConnection(URL u) throws IOException {
                        return new URLConnection(u) {
                            @Override
                            public void connect() throws IOException {
                            }

                            @Override
                            public InputStream getInputStream() throws IOException {
                                return generateEhCacheConfig();
                            }
                        };
                    }
                });
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return super.getResource(name);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        assertNotClosed();
        Class registeredClass = registeredClasses.get(name);
        if (registeredClass != null) {
            return registeredClass;
        }
        return super.findClass(name);
    }

    public ComplexTypeMetadata getTypeFromClass(Class<?> clazz) {
        assertNotClosed();
        // First pass: strict class name equality (don't use isAssignable).
        for (Map.Entry<String, Class<? extends Wrapper>> typeMetadata : registeredClasses.entrySet()) {
            if (typeMetadata.getValue().getName().equals(clazz.getName())) {
                return knownTypes.get(typeMetadata.getKey());
            }
        }
        // In case first pass didn't find anything, try isAssignable.
        for (Map.Entry<String, Class<? extends Wrapper>> typeMetadata : registeredClasses.entrySet()) {
            if (typeMetadata.getValue().isAssignableFrom(clazz)) {
                return knownTypes.get(typeMetadata.getKey());
            }
        }
        throw new IllegalArgumentException("Class '" + clazz.getName() + "' is not registered.");
    }

    public Class<? extends Wrapper> getClassFromType(ComplexTypeMetadata type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        assertNotClosed();
        Class<? extends Wrapper> registeredClass = registeredClasses.get(type.getName());
        if (registeredClass != null) {
            return registeredClass;
        }
        throw new IllegalArgumentException("Type '" + type.getName() + "' is not registered.");
    }
    
    @Override
    protected Package getPackage(String name) {
        if(ClassCreator.PACKAGE_PREFIX.subSequence(0, ClassCreator.PACKAGE_PREFIX.length() -1).equals(name)){
            return this.getClass().getPackage(); // funny ack
        }
        return super.getPackage(name);
    }

    public void register(ComplexTypeMetadata metadata, Class<? extends Wrapper> newClass) {
        assertNotClosed();
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata cannot be null");
        }
        knownTypes.put(metadata.getName(), metadata);
        register(metadata.getName(), newClass);
    }

    public void register(String typeName, Class<? extends Wrapper> newClass) {
        assertNotClosed();
        registeredClasses.put(typeName, newClass);
    }

    public void setDataSourceConfiguration(DataSource dataSource) {
        assertNotClosed();
        if (!(dataSource instanceof RDBMSDataSource)) {
            throw new IllegalArgumentException("Expected an instance of " + RDBMSDataSource.class.getName() + " but was " + dataSource);
        }
        this.dataSource = (RDBMSDataSource) dataSource;
    }

    public void setTableResolver(TableResolver resolver) {
        this.resolver = resolver;
    }

    public void close() {
        if (!isClosed) {
            for (Map.Entry<Thread, Stack<ClassLoader>> entry : previousClassLoaders.entrySet()) {
                reset(entry.getKey());
            }
            registeredClasses.clear();
            knownTypes.clear();
            isClosed = true;
        }
    }

    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Replace the thread's context class loader with this class loader. "Binds" remembers the
     * {@link Thread#getContextClassLoader()} value, so a call to {@link #unbind(Thread)} will restore previous context
     * class loader. Method support nesting bind calls, in this case successive calls ({@link #unbind(Thread)}) will
     * successively restored previous class loaders.
     *
     * @param thread A non-null Thread instance (usually {@link Thread#currentThread()}. Calling this method will
     *               <code>null</code> has no effect.
     * @see #unbind(Thread)
     */
    public void bind(Thread thread) {
        if (thread == null) {
            return;
        }
        synchronized(previousClassLoaders){
            Stack<ClassLoader> classLoaders = previousClassLoaders.get(thread);
            if (classLoaders == null) {
                classLoaders = new Stack<ClassLoader>();
                previousClassLoaders.put(thread, classLoaders);
            }
            classLoaders.push(thread.getContextClassLoader());
        }
        thread.setContextClassLoader(this);
    }

    /**
     * Restore the previous context class loader.
     *
     * @param thread A non-null Thread instance (usually {@link Thread#currentThread()}. Calling this method will
     *               <code>null</code> has no effect.
     * @see #bind(Thread)
     */
    public void unbind(Thread thread) {
        if (thread == null) {
            return;
        }
        synchronized(previousClassLoaders){
            Stack<ClassLoader> classLoaders = previousClassLoaders.get(thread);
            if (classLoaders != null && !classLoaders.isEmpty()) {
                ClassLoader previousClassLoader = classLoaders.pop();
                thread.setContextClassLoader(previousClassLoader);
            }
            if(classLoaders != null && classLoaders.isEmpty()){
                previousClassLoaders.remove(thread);
            }
        }
    }

    /**
     * Similar to {@link #unbind(Thread)} but in case successive calls to {@link #bind(Thread)} were made, this method
     * ensures the very first class loader is restored.
     *
     * @param thread A non-null Thread instance (usually {@link Thread#currentThread()}. Calling this method will
     *               <code>null</code> has no effect.
     */
    public void reset(Thread thread) {
        if (thread == null) {
            return;
        }
        synchronized(previousClassLoaders){
            Stack<ClassLoader> classLoaders = previousClassLoaders.get(thread);
            if (classLoaders != null && !classLoaders.isEmpty()) {
                thread.setContextClassLoader(classLoaders.firstElement());
                classLoaders.clear();
            }
            if(classLoaders != null && classLoaders.isEmpty()){
                previousClassLoaders.remove(thread);
            }
        }
    }

    public void setEntityComplexTypes(Collection<ComplexTypeMetadata> entityComplexTypes) {
        this.entityComplexTypes = entityComplexTypes;
    }

}
