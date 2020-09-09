package com.amalto.core.storage.hibernate;

import java.io.InputStream;

import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;

public class StorageClassLoaderServiceImpl extends ClassLoaderServiceImpl {

	private final ClassLoader classLoaders;
	
	public StorageClassLoaderServiceImpl(ClassLoader classLoaders) {
		super();
		this.classLoaders = classLoaders;
	}


	@Override
	public InputStream locateResourceStream(String name) {
		try {
			final InputStream stream = classLoaders.getResourceAsStream( name );
			if ( stream != null ) {
				return stream;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
