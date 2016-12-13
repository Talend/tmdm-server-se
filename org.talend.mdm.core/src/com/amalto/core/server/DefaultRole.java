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

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.objects.role.RolePOJOPK;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.Role;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultRole implements Role {

    private static final Logger LOGGER = Logger.getLogger(DefaultRole.class);

    public RolePOJOPK putRole(RolePOJO role) throws XtentisException {
        LOGGER.trace("putRole() ");
        try {
            ObjectPOJOPK pk = role.store();
            if (pk == null) {
                throw new XtentisException("Check the XML Server logs");
            }
            return new RolePOJOPK(pk);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to create/update the Role " + role.getName() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    public RolePOJO getRole(RolePOJOPK pk) throws XtentisException {

        try {
            RolePOJO sp = ObjectPOJO.load(RolePOJO.class, pk);
            if (sp == null) {
                String err = "The Role " + pk.getUniqueId() + " does not exist.";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return sp;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the Role " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    public RolePOJO existsRole(RolePOJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(RolePOJO.class, pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            String info = "Could not check whether this Role exists:  " + pk.getUniqueId() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.debug(info, e);
            return null;
        }
    }

    public RolePOJOPK removeRole(RolePOJOPK pk) throws XtentisException {
        LOGGER.trace("Removing " + pk.getUniqueId());
        try {
            return new RolePOJOPK(ObjectPOJO.remove(RolePOJO.class, pk));
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the Role " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    public Collection<RolePOJOPK> getRolePKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(RolePOJO.class, regex);
        ArrayList<RolePOJOPK> l = new ArrayList<RolePOJOPK>();
        for (ObjectPOJOPK currentObject : c) {
            l.add(new RolePOJOPK(currentObject));
        }
        return l;
    }
}
