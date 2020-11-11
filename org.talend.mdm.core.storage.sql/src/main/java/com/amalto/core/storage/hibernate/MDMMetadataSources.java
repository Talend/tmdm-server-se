package com.amalto.core.storage.hibernate;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.spi.XmlMappingBinderAccess;
import org.hibernate.service.ServiceRegistry;

public class MDMMetadataSources extends MetadataSources {

	
	public MDMMetadataSources() {
		super();
	}

	public MDMMetadataSources(ServiceRegistry serviceRegistry) {
		super(serviceRegistry);
	}

	@Override
	public XmlMappingBinderAccess getXmlMappingBinderAccess() {
		return new MDMXmlMappingBinderAccess(super.getServiceRegistry());
	}
}
