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

import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.ResolverException;
import org.exolab.castor.xml.XMLContext;

import com.amalto.core.objects.marshalling.Marshaller;
import com.amalto.core.objects.marshalling.MarshallingException;
import com.amalto.core.objects.marshalling.MarshallingFactory;
import com.amalto.core.objects.marshalling.Unmarshaller;

/**
 * Castor implementation of MDM XOM framework.
 * 
 * This implementation keeps a cache of {@link XMLContext} to improve performances
 */
public class CastorMarshallingFactory extends MarshallingFactory {

    /**
     * Castor {@link XMLContext} cache per class
     */
    private Map<Class<?>, XMLContext> castorContextCache = new HashMap<Class<?>, XMLContext>();
    
    @Override
    public Marshaller getMarshaller(Class<?> clazz) throws MarshallingException {
        XMLContext context = getCastorXMLContext(clazz);
        return new CastorMarshaller(context);
    }

    @Override
    public <T> Unmarshaller<T> getUnmarshaller(Class<T> clazz) throws MarshallingException {
        XMLContext context = getCastorXMLContext(clazz);
        org.exolab.castor.xml.Unmarshaller castorUnmarshaller = context.createUnmarshaller(); 
        castorUnmarshaller.setValidation(false);
        // see 0023397 can't unmarshaller WSPipeline if unmarshaller.setReuseObjects(true)
        castorUnmarshaller.setReuseObjects(false);
        // Do not remove this line unless you know what you're doing
        castorUnmarshaller.setWhitespacePreserve(true);
        castorUnmarshaller.setClass(clazz);
        return new CastorUnmarshaller<T>(castorUnmarshaller);
    }
    
    protected XMLContext getCastorXMLContext(Class<?> clazz) throws MarshallingException {
        synchronized(castorContextCache){
            XMLContext context = castorContextCache.get(clazz);
            if(context == null){
                try {
                    context = new XMLContext();
                    context.addClass(clazz);
                    castorContextCache.put(clazz, context);
                } catch (ResolverException e) {
                    throw new MarshallingException("Failed to create new Castor XML context for class " + clazz, e); //$NON-NLS-1$
                }
            }
            return context;
        }
    }

    
    
}
