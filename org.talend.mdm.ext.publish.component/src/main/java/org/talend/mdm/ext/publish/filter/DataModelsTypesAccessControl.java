/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.filter;

import java.util.List;

public class DataModelsTypesAccessControl extends AccessController {

    @Override
    public boolean validate(List<String> resourceInstances, AccessControlPropertiesReader propertiesReader) {

        String bannedPattern = propertiesReader.getProperty("datamodelstypes.name.ban.pattern"); //$NON-NLS-1$

        if (resourceInstances != null && resourceInstances.size() > 0 && bannedPattern != null) {
            String datamodelName = resourceInstances.get(0);
            if (datamodelName.matches(bannedPattern)) {
                getLocalLogger().debug("The types of datamodel " + datamodelName + " was banned! ");
                return false;
            }
        }

        return true;
    }

}
