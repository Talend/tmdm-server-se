/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.webapp.browserecords.server.util.SmartViewUtil;

import com.amalto.webapp.core.util.Util;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSExtractThroughTransformerV2;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSTransformerContextPipelinePipelineItem;
import com.amalto.core.webservice.WSTransformerV2PK;

public class SmartViewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public SmartViewServlet() {
        super();
    }

    @Override
    protected void doGet(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");//$NON-NLS-1$ 
        response.setHeader("Cache-Control", "no-cache, must-revalidate");//$NON-NLS-1$ //$NON-NLS-2$
        response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");//$NON-NLS-1$ //$NON-NLS-2$

        String idsString = request.getParameter("ids");//$NON-NLS-1$
        String concept = request.getParameter("concept");//$NON-NLS-1$
        if (concept == null || idsString == null) {
            return;
        }
        boolean isStaging = request.getParameter("isStaging") != null ? Boolean.parseBoolean(request.getParameter("isStaging")) : false; //$NON-NLS-1$ //$NON-NLS-2$
        String[] ids = idsString.split("@");//$NON-NLS-1$
        String language = (request.getParameter("language") != null ? request.getParameter("language").toUpperCase() : "EN");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String smartViewName = request.getParameter("name");//$NON-NLS-1$
        String optname;
        if (StringUtils.indexOf(smartViewName, '#') > 0) {
            optname = StringUtils.substringAfterLast(smartViewName, "#"); //$NON-NLS-1$
        } else {
            optname = null;
        }

        String transformer = null;
        try {
            boolean transfo_lang = SmartViewUtil.checkSmartViewExistsByLangAndOptName(concept, language, optname, false);

            if (transfo_lang) {
                transformer = "Smart_view_" + concept + "_" + language.toUpperCase();//$NON-NLS-1$ //$NON-NLS-2$
                if (optname != null && optname.length() > 0) {
                    transformer += "#" + optname;//$NON-NLS-1$
                }
            } else {
                // Fallback to the non-language one
                boolean transfo_no_lang = SmartViewUtil.checkSmartViewExistsByLangAndOptName(concept, null, optname, false);
                if (transfo_no_lang) {
                    transformer = "Smart_view_" + concept;//$NON-NLS-1$
                    if (optname != null && optname.length() > 0) {
                        transformer += "#" + optname;//$NON-NLS-1$
                    }
                } else {
                    transformer = null;
                }
            }
        } catch (Exception e) {
            org.apache.log4j.Logger.getLogger(this.getClass()).error(e);
            throw new ServletException(e);
        }

        String content = "";//$NON-NLS-1$
        String contentType = "text/html";//$NON-NLS-1$
        if (transformer != null) {
            String dataClusterPK;
            try {
                dataClusterPK = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataCluster(isStaging);
            } catch (Exception e) {
                String err = "Unable to get the cluster";
                org.apache.log4j.Logger.getLogger(this.getClass()).error(err, e);
                throw new ServletException(err, e);
            }
            try {
                // run the Transformer
                WSTransformerContextPipelinePipelineItem[] entries = Util
                        .getPort()
                        .extractThroughTransformerV2(
                                new WSExtractThroughTransformerV2(new WSItemPK(new WSDataClusterPK(dataClusterPK), concept, ids),
                                        new WSTransformerV2PK(transformer))).getPipeline().getPipelineItem();

                // Scan the entries - in priority, taka the content of the 'html' entry,
                // else take the content of the _DEFAULT_ entry
                for (WSTransformerContextPipelinePipelineItem entrie : entries) {
                    if ("_DEFAULT_".equals(entrie.getVariable())) {//$NON-NLS-1$
                        content = new String(entrie.getWsTypedContent().getWsBytes().getBytes(), "UTF-8");//$NON-NLS-1$
                        contentType = entrie.getWsTypedContent().getContentType();
                    }
                    if ("html".equals(entrie.getVariable())) {//$NON-NLS-1$
                        content = new String(entrie.getWsTypedContent().getWsBytes().getBytes(), "UTF-8");//$NON-NLS-1$
                        contentType = entrie.getWsTypedContent().getContentType();
                        break;
                    }
                }
            } catch (Exception e) {
                String err = "Unable to run the transformer '" + transformer + "'"; //$NON-NLS-1$//$NON-NLS-2$
                org.apache.log4j.Logger.getLogger(this.getClass()).error(err, e);
                throw new ServletException(err, e);
            }
        }

        if (contentType.startsWith("application/xhtml+xml")) { //$NON-NLS-1$
            response.setContentType("text/html");//$NON-NLS-1$
        } else {
            response.setContentType(contentType);
        }
        PrintWriter out = response.getWriter();
        out.write(content);
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
