/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.marshalling;

import com.amalto.core.objects.marshalling.castor.CastorMarshallingFactory;

/**
 * Main entry to do Object / XML mapping in MDM 
 */
public abstract class MarshallingFactory {
    
    private static final MarshallingFactory CASTOR_FACTORY = new CastorMarshallingFactory();
    
    /**
     * @return the most appropriate OXM framework for MDM
     */
    public static MarshallingFactory getInstance() {
        return CASTOR_FACTORY;
    }
    
    /**
     * @param clazz
     * @return a {@link Marshaller} for the provided class (Object to XML)
     * @throws MarshallingException in case an issue occurred initializing the implementation
     */
    public abstract Marshaller getMarshaller(Class<?> clazz) throws MarshallingException;
    
    /**
     * 
     * @param clazz
     * @return an {@link Unmarshaller} for the provided class (XML to Object)
     * @throws MarshallingException in case an issue occurred initializing the implementation
     */
    public abstract <T> Unmarshaller<T> getUnmarshaller(Class<T> clazz) throws MarshallingException;

}
