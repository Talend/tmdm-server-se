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

import java.io.StringReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import com.amalto.core.objects.marshalling.MarshallingException;
import com.amalto.core.objects.marshalling.MarshallingFactory;

public class RoleMenuParameters {
    public String getParentID() {
        String parentID = "";
        return parentID;}

    public int getPosition() {
        int position = 0;
        return position;}

    // TODO: change this method signature to do not expose Castor Exception anymore
    public static RoleMenuParameters unmarshalMenuParameters(String parameters) throws ValidationException ,MarshalException{
        try {
            return MarshallingFactory.getInstance().getUnmarshaller(RoleMenuParameters.class).unmarshal(new StringReader(parameters));
        } catch (MarshallingException e) {
            throw new MarshalException(e);
        }
	}

}
