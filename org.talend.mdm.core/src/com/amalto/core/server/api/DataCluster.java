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

import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.util.XtentisException;

import java.util.Collection;

/**
 *
 */
public interface DataCluster {
    DataClusterPOJOPK putDataCluster(DataClusterPOJO dataCluster) throws XtentisException;

    DataClusterPOJO getDataCluster(DataClusterPOJOPK pk) throws XtentisException;

    DataClusterPOJO existsDataCluster(DataClusterPOJOPK pk) throws XtentisException;

    DataClusterPOJOPK removeDataCluster(DataClusterPOJOPK pk) throws XtentisException;

    Collection<DataClusterPOJOPK> getDataClusterPKs(String regex) throws XtentisException;

    int addToVocabulary(DataClusterPOJOPK pk, String string) throws XtentisException;

    Collection<String> spellCheck(DataClusterPOJOPK dcpk, String sentence, int treshold, boolean ignoreNonExistantWords) throws XtentisException;
}
