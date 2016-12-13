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

import java.util.Collection;

import com.amalto.core.objects.backgroundjob.BackgroundJobPOJO;
import com.amalto.core.objects.backgroundjob.BackgroundJobPOJOPK;
import com.amalto.core.util.XtentisException;

/**
 *
 */
public interface BackgroundJob {

    BackgroundJobPOJOPK putBackgroundJob(BackgroundJobPOJO backgroundJob) throws XtentisException;

    BackgroundJobPOJO getBackgroundJob(BackgroundJobPOJOPK pk) throws XtentisException;

    BackgroundJobPOJO existsBackgroundJob(BackgroundJobPOJOPK pk) throws XtentisException;

    BackgroundJobPOJOPK removeBackgroundJob(BackgroundJobPOJOPK pk) throws XtentisException;

    Collection<BackgroundJobPOJOPK> getBackgroundJobPKs(String regex) throws XtentisException;
}
