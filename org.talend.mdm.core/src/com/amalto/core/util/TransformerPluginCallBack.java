/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

/**
 * 
 * @author bgrieder
 * @deprecated - use TransformerV2 package
 *
 */
public interface TransformerPluginCallBack {
	
	public void contentIsReady(int pluginHandle, TypedContent content, TransformerPluginContext context) throws XtentisException;
	
	public void done(int pluginHandle, TransformerPluginContext context) throws XtentisException;
	
	public void stopped(int pluginHandle, TransformerPluginContext context) throws XtentisException;

}
