/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.api;

import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.storedprocedure.StoredProcedurePOJO;
import com.amalto.core.objects.storedprocedure.StoredProcedurePOJOPK;
import com.amalto.core.util.XtentisException;

import java.util.Collection;

/**
 *
 */
public interface StoredProcedure {
    StoredProcedurePOJOPK putStoredProcedure(StoredProcedurePOJO storedProcedure) throws XtentisException;

    StoredProcedurePOJO getStoredProcedure(StoredProcedurePOJOPK pk) throws XtentisException;

    StoredProcedurePOJO existsStoredProcedure(StoredProcedurePOJOPK pk)    throws XtentisException;

    StoredProcedurePOJOPK removeStoredProcedure(StoredProcedurePOJOPK pk)
    throws XtentisException;

    Collection<String> execute(
            StoredProcedurePOJOPK sppk,
            DataClusterPOJOPK dcpk,
            String[] parameters
    )throws XtentisException;

    Collection<StoredProcedurePOJOPK> getStoredProcedurePKs(String regex) throws XtentisException;
}
