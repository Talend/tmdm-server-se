/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server;

import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.customform.CustomFormPOJO;
import com.amalto.core.objects.customform.CustomFormPOJOPK;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.objects.role.RolePOJOPK;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.RoleSpecification;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.CustomForm;
import com.amalto.core.server.security.SecurityConfig;

import java.util.*;

public class DefaultCustomForm implements CustomForm {

    private static final Logger LOGGER = Logger.getLogger(DefaultCustomForm.class);

    /**
     * Creates or updates a CustomForm
     */
    public CustomFormPOJOPK putCustomForm(CustomFormPOJO customForm) throws XtentisException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("putCustomForm() ");
        }
        try {
            ObjectPOJOPK pk = customForm.store();
            if (pk == null) {
                throw new XtentisException("Check the XML Server logs");
            }
            return (CustomFormPOJOPK) pk;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to create/update the CustomForm " + customForm.getPK().getUniqueId() + ": "
                    + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get CustomForm
     */
    public CustomFormPOJO getCustomForm(CustomFormPOJOPK pk) throws XtentisException {
        try {
            CustomFormPOJO sp = ObjectPOJO.load(CustomFormPOJO.class, pk);
            if (sp == null) {
                String err = "The CustomForm " + pk.getUniqueId() + " does not exist.";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return sp;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the CustomForm " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get CustomForm according to the current user's role
     */
    public CustomFormPOJO getUserCustomForm(CustomFormPOJOPK cpk) throws XtentisException {
        try {
            List<String> ids= new ArrayList<String>();
            String objectType = "Custom Layout"; //$NON-NLS-1$
            ILocalUser user = LocalUser.getLocalUser();
            HashSet<String> roleNames = user.getRoles();
            for (String roleName : roleNames) {
                if (SecurityConfig.isSecurityPermission(roleName)) {
                    continue;
                }
                //load Role
                RolePOJO role = ObjectPOJO.load(RolePOJO.class, new RolePOJOPK(roleName));
                //get Specifications for the View Object
                RoleSpecification specification = role.getRoleSpecifications().get(objectType);
                if (specification != null) {
                    Set<String> keys = specification.getInstances().keySet();
                    for (String id : keys) {
                        ids.add(id);
                    }
                }
            }
            List<CustomFormPOJO> list = new ArrayList<CustomFormPOJO>();
            for (String pk : ids) {
                CustomFormPOJO pojo = ObjectPOJO.load(CustomFormPOJO.class, new ObjectPOJOPK(pk.split("\\.\\.")));//$NON-NLS-1$
                if (pojo!=null && pojo.getDatamodel().equals(cpk.getDatamodel())
                        && pojo.getEntity().equals(cpk.getEntity())) {
                    list.add(pojo);
                }
            }
            if (list.size() > 0) {
                return list.get(0);
            }
            return null;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the CustomForm associated to the current user's Role: "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get a CustomForm - no exception is thrown: returns null if not found
     */
    public CustomFormPOJO existsCustomForm(CustomFormPOJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(CustomFormPOJO.class, pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            String info = "Could not check whether this CustomForm exists:  " + pk.getUniqueId() + ": " + e.getClass().getName()
                    + ": " + e.getLocalizedMessage();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(info, e);
            }
            return null;
        }
    }

    /**
     * Remove an item
     */
    public CustomFormPOJOPK removeCustomForm(CustomFormPOJOPK pk) throws XtentisException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Removing " + pk.getUniqueId());
        }
        try {
            return new CustomFormPOJOPK(ObjectPOJO.remove(CustomFormPOJO.class, pk));
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the CustomForm " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Retrieve all CustomForm PKS
     */
    public Collection<CustomFormPOJOPK> getCustomFormPKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(CustomFormPOJO.class, regex);
        ArrayList<CustomFormPOJOPK> l = new ArrayList<CustomFormPOJOPK>();
        for (ObjectPOJOPK customFormPK : c) {
            l.add(new CustomFormPOJOPK(customFormPK));
        }
        return l;
    }
}
