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

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.spi.XmlMappingBinderAccess;
import org.hibernate.service.ServiceRegistry;

public class MDMMetadataSources extends MetadataSources {

    private static final long serialVersionUID = 3359949654618694093L;

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