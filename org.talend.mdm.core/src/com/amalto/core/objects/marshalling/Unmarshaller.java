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

import java.io.Reader;

/**
 * An object able to unmarshal objects from XML (XML -> object transformation) 
 *
 * @param <T> type of expected objects
 */
public interface Unmarshaller<T> {
    
    /**
     * Unmarshal data read from input and returns the created object of type T
     * 
     * @param input
     * @return an instance of type T built from data read from input
     * @throws MarshallingException in case of any error
     */
    public T unmarshal(Reader input) throws MarshallingException;

}
