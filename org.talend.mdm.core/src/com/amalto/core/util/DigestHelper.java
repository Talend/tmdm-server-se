/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import com.amalto.core.server.StorageAdminImpl;
import org.apache.commons.lang.StringUtils;
import com.amalto.core.objects.configurationinfo.ConfigurationInfoPOJO;
import com.amalto.core.objects.customform.CustomFormPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.menu.MenuPOJO;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.objects.routing.RoutingRulePOJO;
import com.amalto.core.objects.storedprocedure.StoredProcedurePOJO;
import com.amalto.core.objects.transformers.TransformerV2POJO;
import com.amalto.core.objects.view.ViewPOJO;
import org.apache.log4j.Logger;

public class DigestHelper {

    private static final String TYPE_DATA_CLUSTER = "MDM.DataCluster"; //$NON-NLS-1$
    
    private static final String TYPE_DATA_MODEL = "MDM.DataModel"; //$NON-NLS-1$
    
    private static final String TYPE_VIEW = "MDM.View"; //$NON-NLS-1$
    
    private static final String TYPE_ROLE = "MDM.Role"; //$NON-NLS-1$
    
    private static final String TYPE_MENU = "MDM.Menu"; //$NON-NLS-1$
    
    private static final String TYPE_STORED_PROCEDURE = "MDM.StoredProcedure"; //$NON-NLS-1$
    
    private static final String TYPE_UNIVERSE = "MDM.Universe"; //$NON-NLS-1$
    
    private static final String TYPE_SYNCHRONIZATION_PLAN = "MDM.SynchronizationPlan"; //$NON-NLS-1$
    
    private static final String TYPE_WORKFLOW = "MDM.Workflow"; //$NON-NLS-1$

    private static final String TYPE_TRANSFORMER = "MDM.TransformerV2"; //$NON-NLS-1$
    
    private static final String TYPE_ROUTING_RULE = "MDM.RoutingRule"; //$NON-NLS-1$

    private static final String TYPE_JOB_MODEL = "MDM.JobModel"; //$NON-NLS-1$

    private static final String TYPE_SERVICE_CONFIGURATION = "MDM.ServiceConfiguration"; //$NON-NLS-1$
    
    private static final String TYPE_RESOURCE = "MDM.Resource"; //$NON-NLS-1$

    private static final String TYPE_CUSTOM_FORM = "MDM.CustomForm"; //$NON-NLS-1$

    private static final String TYPE_MATCH_RULE = "MDM.MatchRule"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(DigestHelper.class);

    private static DigestHelper instance;
    
    public static synchronized DigestHelper getInstance() {
        if (instance == null) {
            instance = new DigestHelper();
        }
        return instance;
    }

    /**
     * @param type A type of resource (see constants in {@link DigestHelper}).
     * @return The MDM internal class name
     */
    public String getTypeName(String type) {
        String name = null;
        if (TYPE_DATA_CLUSTER.equals(type)) {
            name = DataClusterPOJO.class.getName();
        } else if (TYPE_DATA_MODEL.equals(type)) {
            name = DataModelPOJO.class.getName();
        } else if (TYPE_VIEW.equals(type)) {
            name = ViewPOJO.class.getName();
        } else if (TYPE_ROLE.equals(type)) {
            name = RolePOJO.class.getName();
        } else if (TYPE_MENU.equals(type)) {
            name = MenuPOJO.class.getName();
        } else if (TYPE_STORED_PROCEDURE.equals(type)) {
            name = StoredProcedurePOJO.class.getName();
        } else if (TYPE_TRANSFORMER.equals(type)) {
            name = TransformerV2POJO.class.getName();
        } else if (TYPE_ROUTING_RULE.equals(type)) {
            name = RoutingRulePOJO.class.getName();
        } else if (TYPE_SERVICE_CONFIGURATION.equals(type)) {
            name = ConfigurationInfoPOJO.class.getName();
        } else if (TYPE_CUSTOM_FORM.equals(type)) {
            name = CustomFormPOJO.class.getName();
        }  else if (TYPE_MATCH_RULE.equals(type)) {
            name = StorageAdminImpl.MATCH_RULE_POJO_CLASS; // Class is not in class path when running CE edition.
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Type '" + type + "' is not supported.");
            }
        }
        return name != null ? StringUtils.substringAfterLast(name, ".") : null; //$NON-NLS-1$
    }

}
