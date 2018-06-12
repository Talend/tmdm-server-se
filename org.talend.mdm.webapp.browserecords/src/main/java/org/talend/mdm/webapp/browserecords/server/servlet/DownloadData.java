/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.talend.mdm.webapp.browserecords.server.bizhelpers.ViewHelper;
import org.talend.mdm.webapp.browserecords.server.util.CSVWriter;
import org.talend.mdm.webapp.browserecords.server.util.DownloadUtil;
import org.talend.mdm.webapp.browserecords.server.util.DownloadWriter;
import org.talend.mdm.webapp.browserecords.server.util.ExcelWriter;
import org.talend.mdm.webapp.browserecords.shared.Constants;

import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;

public class DownloadData extends HttpServlet {

    private Messages messages = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages", DownloadData.class.getClassLoader()); //$NON-NLS-1$

    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DownloadWriter writer = null;
        List<String> idsList = null;
        Map<String, String> colFkMap = null;
        Map<String, List<String>> fkMap = null;
        String fkColXPath = null;
        String fkInfo = null;
        
        String viewPk = request.getParameter("tableName"); //$NON-NLS-1$
        String concept = ViewHelper.getConceptFromDefaultViewName(viewPk);
        String header = new String(request.getParameter("header").getBytes("iso-8859-1"), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String xpath = request.getParameter("xpath"); //$NON-NLS-1$
        String criteria = request.getParameter("criteria"); //$NON-NLS-1$
        String name = new String(request.getParameter("fileName").getBytes("iso-8859-1"), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String fileType = request.getParameter("fileType"); //$NON-NLS-1$
        String multipleValueSeparator = request.getParameter("multipleValueSeparator"); //$NON-NLS-1$
        boolean fkResovled = Boolean.valueOf(request.getParameter("fkResovled")); //$NON-NLS-1$
        String fkDisplay = request.getParameter("fkDisplay"); //$NON-NLS-1$
        String language = request.getParameter("language"); //$NON-NLS-1$
        
        if (request.getParameter("itemIdsListString") != null && !request.getParameter("itemIdsListString").isEmpty()) { //$NON-NLS-1$ //$NON-NLS-2$
            idsList = org.talend.mdm.webapp.base.shared.util.CommonUtil
                    .convertStrigToList(request.getParameter("itemIdsListString"), Constants.FILE_EXPORT_IMPORT_SEPARATOR); //$NON-NLS-1$
        }

        try {
            String[] headerArray = DownloadUtil.convertXml2Array(header, "item"); //$NON-NLS-1$
            String[] xpathArray = DownloadUtil.convertXml2Array(xpath, "item"); //$NON-NLS-1$
            if (fkResovled) {
                colFkMap = new HashMap<String, String>();
                fkMap = new HashMap<String, List<String>>();
                fkColXPath = request.getParameter("fkColXPath"); //$NON-NLS-1$
                fkInfo = request.getParameter("fkInfo"); //$NON-NLS-1$
                DownloadUtil.assembleFkMap(colFkMap, fkMap, fkColXPath, fkInfo);
            }
            response.reset();
            if (Constants.FILE_TYPE_CSV.equals(fileType)) {
                response.setContentType("text/csv"); //$NON-NLS-1$
                writer = new CSVWriter(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator,
                        fkDisplay, fkResovled, colFkMap, fkMap, isStaging(), language);
            } else if (Constants.FILE_TYPE_EXCEL.equals(fileType)) {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); //$NON-NLS-1$
                writer = new ExcelWriter(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator,
                        fkDisplay, fkResovled, colFkMap, fkMap, isStaging(), language);
            } else {
                throw new ServletException(messages.getMessage("unsupported_file_type", fileType)); //$NON-NLS-1$
            }
            response.setHeader("Content-Disposition", //$NON-NLS-1$
                    "attachment; filename=\"" + URLEncoder.encode(writer.generateFileName(name), "UTF-8") + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
            OutputStream out = response.getOutputStream();
            writer.writeFile();
            writer.write(out);
            out.close();
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    protected boolean isStaging() {
        return false;
    }
}
