/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.util;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.amalto.core.util.LocalUser;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.ServerContext;
import com.amalto.core.webservice.WSDataModel;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSGetDataModel;

public class DataModelAccessor {

    private static final Logger LOG = Logger.getLogger(DataModelAccessor.class);

    private static DataModelAccessor accessor;

    public static synchronized DataModelAccessor getInstance() {
        if (accessor == null) {
            accessor = new DataModelAccessor();
        }
        return accessor;
    }

    public boolean checkReadAccess(String dataModelName, String conceptName) {
        try {
            if (dataModelName != null && conceptName != null) {
                Collection<String> roleList = LocalUser.getLocalUser().getRoles();
                MetadataRepositoryAdmin admin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
                MetadataRepository repository = admin.get(dataModelName);
                return _checkReadAccess(repository, conceptName, roleList);
            } else {
                return false;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    protected boolean checkReadAccess(String modelXSD, String conceptName, List<String> roles) {
        MetadataRepository repository = new MetadataRepository();
        repository.load(new ByteArrayInputStream(modelXSD.getBytes()));
        return _checkReadAccess(repository, conceptName, roles);
    }

    private boolean _checkReadAccess(MetadataRepository repository, String conceptName, Collection<String> roleList) {
        ComplexTypeMetadata type = repository.getComplexType(conceptName);
        if (type != null) {
            List<String> noAccessRoles = new ArrayList<String>(type.getHideUsers());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Roles without access " + noAccessRoles); //$NON-NLS-1$
            }
            noAccessRoles.retainAll(roleList);
            boolean userIsNoAccess = !noAccessRoles.isEmpty();
            return !userIsNoAccess;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Complex Type " + conceptName + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return false;
        }
    }

    public boolean checkRestoreAccess(String dataModelName, String conceptName) {
        try {
            if (dataModelName != null && conceptName != null) {
                Collection<String> roleList = LocalUser.getLocalUser().getRoles(); //$NON-NLS-1$
                MetadataRepositoryAdmin admin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
                MetadataRepository repository = admin.get(dataModelName);
                return _checkRestoreAccess(repository, conceptName, roleList);
            } else {
                return false;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    protected boolean checkRestoreAccess(String modelXSD, String conceptName, List<String> roles) {
        MetadataRepository repository = new MetadataRepository();
        repository.load(new ByteArrayInputStream(modelXSD.getBytes()));
        return _checkRestoreAccess(repository, conceptName, roles);
    }

    private boolean _checkRestoreAccess(MetadataRepository repository, String conceptName, Collection<String> roleList) {
        ComplexTypeMetadata type = repository.getComplexType(conceptName);
        if (type != null) {
            List<String> noAccessRoles = new ArrayList<String>(type.getHideUsers());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Roles without access " + noAccessRoles); //$NON-NLS-1$
            }
            List<String> writeAccessRoles = new ArrayList<String>(type.getWriteUsers());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Roles with write permission " + writeAccessRoles); //$NON-NLS-1$
            }
            noAccessRoles.retainAll(roleList);
            boolean userIsNoAccess = !noAccessRoles.isEmpty();
            writeAccessRoles.retainAll(roleList);
            boolean userHasWriteAccess = !writeAccessRoles.isEmpty();
            return !userIsNoAccess && userHasWriteAccess;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Complex Type " + conceptName + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return false;
        }
    }
}
