/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.server.actions;

import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.ServerContext;
import com.amalto.webapp.core.bean.Configuration;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.OutboundReferences;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.stagingarea.control.client.StagingAreaService;
import org.talend.mdm.webapp.stagingarea.control.shared.model.ConceptRelationshipModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaConfiguration;

import java.io.InputStream;
import java.util.*;

public class StagingAreaAction implements StagingAreaService {

    private static final Logger LOG                       = Logger.getLogger(StagingAreaAction.class);

    private static final String STAGING_AREA_PROPERTIES   = "/stagingarea.properties";                //$NON-NLS-1$

    private static final int    DEFAULT_REFRESH_INTERVALS = 1000;

    public StagingAreaConfiguration getStagingAreaConfig() {
        StagingAreaConfiguration cm = new StagingAreaConfiguration();
        try {
            InputStream is = StagingAreaAction.class.getResourceAsStream(STAGING_AREA_PROPERTIES);
            Properties prop = new Properties();
            prop.load(is);
            String refreshIntervals = prop.getProperty("refresh_intervals"); //$NON-NLS-1$
            if (refreshIntervals != null) {
                cm.setRefreshIntervals(Integer.parseInt(refreshIntervals));
            } else {
                cm.setRefreshIntervals(DEFAULT_REFRESH_INTERVALS);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return cm;
    }

    @Override
    public ConceptRelationshipModel getConceptRelation() throws ServiceException {
        try {
            // Get the data model.
            Configuration configuration = Configuration.getConfiguration();
            MetadataRepositoryAdmin admin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
            MetadataRepository repository = admin.get(configuration.getModel());
            // List outbound references for the data model.
            List<String> entityTypeNames = new LinkedList<String>();
            Map<String, String[]> relations = new HashMap<String, String[]>();
            for (ComplexTypeMetadata entityType : repository.getUserComplexTypes()) {
                entityTypeNames.add(entityType.getName());
                Set<ReferenceFieldMetadata> outboundReferences = entityType.accept(new OutboundReferences());
                String[] referencedTypes = new String[outboundReferences.size()];
                int i = 0;
                for (ReferenceFieldMetadata outboundReference : outboundReferences) {
                    referencedTypes[i++] = outboundReference.getReferencedType().getName();
                }
                relations.put(entityType.getName(), referencedTypes);
            }
            return new ConceptRelationshipModel(entityTypeNames.toArray(new String[entityTypeNames.size()]), relations);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

}
