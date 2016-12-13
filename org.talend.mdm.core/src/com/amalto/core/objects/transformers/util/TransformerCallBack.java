/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.transformers.util;

import com.amalto.core.util.XtentisException;


public interface TransformerCallBack {
	
	public void contentIsReady(TransformerContext context) throws XtentisException;
	
	public void done(TransformerContext context) throws XtentisException;
	
}
