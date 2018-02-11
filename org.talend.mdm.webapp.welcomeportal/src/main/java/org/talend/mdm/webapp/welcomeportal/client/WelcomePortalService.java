/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client;

import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.shared.AppHeader;
import org.talend.mdm.webapp.welcomeportal.client.mvc.PortalProperties;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("WelcomePortalService")
public interface WelcomePortalService extends RemoteService {

    public boolean isHiddenWorkFlowTask() throws ServiceException;

    public boolean isHiddenTDSTask() throws ServiceException;

    public int getWorkflowTaskMsg() throws ServiceException;

    public Map<String, String> getStandaloneProcess(String language) throws ServiceException;

    public String runProcess(String transformerPK) throws ServiceException;

    boolean isEnterpriseVersion() throws ServiceException;

    public String getMenuLabel(String language, String id) throws Exception;

    public String getCurrentDataContainer() throws ServiceException;

    public Map<Boolean, Integer> getWelcomePortletConfig() throws Exception;

    public PortalProperties getPortalConfig() throws ServiceException;

    public void savePortalConfig(PortalProperties config) throws ServiceException;

    public void savePortalConfig(String key, String value) throws ServiceException;

    public void savePortalConfig(String key, String portletName, String value) throws ServiceException;

    public void savePortalConfigAutoAndSetting(String portletName, List<String> coinfig) throws ServiceException;

    public AppHeader getAppHeader() throws ServiceException;

    public String getCurrentDataModel() throws ServiceException;
}