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

import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;

import com.amalto.core.objects.marshalling.MarshallingException;
import com.amalto.core.objects.marshalling.Unmarshaller;

/**
 * Castor implementation of {@link Unmarshaller}
 * 
 * @param <T>
 */
public class CastorUnmarshaller<T> implements Unmarshaller<T> {
    
    private org.exolab.castor.xml.Unmarshaller castorUnmarshaller;
    
    // package visibility to force using abstract factory
    CastorUnmarshaller(org.exolab.castor.xml.Unmarshaller castorUnmarshaller){
        this.castorUnmarshaller = castorUnmarshaller;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T unmarshal(Reader input) throws MarshallingException {
        try {
            return (T) this.castorUnmarshaller.unmarshal(new InputSource(input));
        } catch (MarshalException | ValidationException e) {
            throw new MarshallingException(e);
        }
    }

}
