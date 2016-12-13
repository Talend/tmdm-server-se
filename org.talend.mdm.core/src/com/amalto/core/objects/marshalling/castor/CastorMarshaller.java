/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.marshalling.castor;

import java.io.IOException;
import java.io.Writer;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;

import com.amalto.core.objects.marshalling.Marshaller;
import com.amalto.core.objects.marshalling.MarshallingException;

/**
 * {@link Marshaller} implementation based on Castor 
 */
public class CastorMarshaller implements Marshaller {
    
    private XMLContext castorContext;
    
    // package visibility to force using abstract factory
    CastorMarshaller(XMLContext context){
        this.castorContext = context;
    }

    @Override
    public void marshal(Object object, Writer output) throws MarshallingException {
        try {
            org.exolab.castor.xml.Marshaller castorMarshaller = this.castorContext.createMarshaller();
            castorMarshaller.setWriter(output);
            castorMarshaller.marshal(object);
        } catch (MarshalException | ValidationException | IOException e) {
            throw new MarshallingException(e);
        }
        
    }

    

}
