// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.hibernate;

import java.io.InputStream;

import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.SourceType;
import org.hibernate.boot.jaxb.internal.InputStreamXmlSource;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.XmlMappingBinderAccess;
import org.hibernate.service.ServiceRegistry;

public class MDMXmlMappingBinderAccess extends XmlMappingBinderAccess {

	private final ClassLoaderService classLoaderService;
	
	public MDMXmlMappingBinderAccess(ServiceRegistry serviceRegistry) {
		super(serviceRegistry);
		this.classLoaderService = serviceRegistry.getService( ClassLoaderService.class );
	}

	public Binding bind(String resource) {
		final InputStream xmlInputStream = classLoaderService.locateResourceStream(resource);
		final Origin origin = new Origin(SourceType.INPUT_STREAM, null);
		return new InputStreamXmlSource(origin, xmlInputStream, false).doBind(getMappingBinder());
	}
}
