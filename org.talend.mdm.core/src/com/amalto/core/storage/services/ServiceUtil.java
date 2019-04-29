/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.save.MultiRecordsSaveException;
import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.storage.exception.ConstraintViolationException;
import com.amalto.core.util.Util;
import com.amalto.core.util.ValidateException;
import com.ctc.wstx.exc.WstxUnexpectedCharException;

public class ServiceUtil {

    private static final Logger LOGGER = Logger.getLogger(ServiceUtil.class);

    public static String getDataModelNameByEntityName(MetadataRepositoryAdmin metadataRepositoryAdmin,
            List<String> dataModelNames, String entityName) {
        try {
            for (String dataModelName : dataModelNames) {
                MetadataRepository repository = metadataRepositoryAdmin.get(dataModelName);
                if (null != repository.getComplexType(entityName)) {
                    return dataModelName;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get data model name by entity name.", e); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }

    public static List<String> getNoAccessRolesByEntity(MetadataRepositoryAdmin metadataRepositoryAdmin,
             List<String> dataModelNames, String entityName) {
        try {
            for (String dataModelName : dataModelNames) {
                MetadataRepository repository = metadataRepositoryAdmin.get(dataModelName);
                ComplexTypeMetadata complexTypeMetadata = repository.getComplexType(entityName);
                if (complexTypeMetadata != null) {
                    return complexTypeMetadata.getHideUsers();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get no access role by entity name.", e); //$NON-NLS-1$
        }
        return Collections.EMPTY_LIST;
    }

    public static List<String> getDataModelNames() {
        List<String> validDataModelNames = new ArrayList<>();
        try {
            Collection<DataModelPOJOPK> allDataModelPOJOPKs = Util.getDataModelCtrlLocal().getDataModelPKs(".*"); //$NON-NLS-1$
            Map<String, XSystemObjects> xDataClustersMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_MODEL);
            for (DataModelPOJOPK dataModelPOJOPK : allDataModelPOJOPKs) {
                String dataModelName = dataModelPOJOPK.getUniqueId();
                // XML Schema's schema is not aimed to be checked.
                if (!"XMLSCHEMA---".equals(dataModelName) && !xDataClustersMap.containsKey(dataModelName)) { //$NON-NLS-1$
                    validDataModelNames.add(dataModelName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data model names.", e); //$NON-NLS-1$
        }
        return validDataModelNames;
    }

    public static Response getErrorResponse(Throwable e, String message) {
        String responseMessage = message == null ? e.getMessage() : message;
        if (e instanceof ConstraintViolationException) {
            LOGGER.warn(responseMessage, e);
            return Response.status(Response.Status.FORBIDDEN).entity(responseMessage).build();
        } else if (e instanceof XMLStreamException || e instanceof IllegalArgumentException
                || e instanceof MultiRecordsSaveException
                || (e.getCause() != null && (e.getCause() instanceof IllegalArgumentException
                        || e.getCause() instanceof IllegalStateException || e.getCause() instanceof ValidateException
                        || e.getCause() instanceof WstxUnexpectedCharException))) {
            LOGGER.warn(responseMessage, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(responseMessage).build();
        } else if (e instanceof NotFoundException) {
            LOGGER.error(responseMessage, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            LOGGER.error(responseMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseMessage).build();
        }
    }
}
