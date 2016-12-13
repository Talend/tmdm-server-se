/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.webapp.general.server.util.Utils;

import com.amalto.core.util.LocaleUtil;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;

public class ControllerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(ControllerServlet.class);
    
    private static final Messages MESSAGES = MessagesFactory.getMessages("org.talend.mdm.webapp.general.server.servlet.messages", //$NON-NLS-1$
            ControllerServlet.class.getClassLoader());

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        doPost(arg0, arg1);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String language = null;
        Locale locale = null;
        // Try first the user language
        try {
            String storedLanguage = com.amalto.webapp.core.util.Util.getDefaultLanguage();
            if (storedLanguage != null && !"".equals(storedLanguage)) { //$NON-NLS-1$
                language = storedLanguage;
                locale = new Locale(language.toLowerCase());
            }
        } catch (Exception e1) {
            LOG.error("Load User Language Error!", e1); //$NON-NLS-1$
        }
        // Try then the language set on request
        if (language == null) {
            locale = LocaleUtil.getLocale(req);
            language = locale.getLanguage().toLowerCase();
        }
        req.getSession().setAttribute("language", language); //$NON-NLS-1$
        res.setContentType("text/html; charset=UTF-8"); //$NON-NLS-1$
        res.setHeader("Content-Type", "text/html; charset=UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
        PrintWriter out = res.getWriter();
        try {
            String html = getHtml(language);
            out.write(html);
        } catch (Exception e) {
            req.getSession().invalidate();
            String message = e.getLocalizedMessage();
            if (message == null) {
                message = MESSAGES.getMessage(locale, "error.occured"); //$NON-NLS-1$
            }
            String html = getHtmlError(message, locale);
            out.write(html);
        }
    }

    @SuppressWarnings("nls")
    protected String getHtml(String language) throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<title>Talend MDM</title>\n");
        html.append("<meta id='gwt:property' name='gwt:property' content='locale=").append(language).append("'>\n");
        html.append("<meta http-equiv='X-UA-Compatible' content='IE=9'>\n");
        html.append("<link rel='shortcut icon' href='auth/favicon.ico'/>\n");
        html.append("<link rel='stylesheet' type='text/css' href='auth/googleapi/fonts.css'/>\n");
        html.append("<link rel='stylesheet' type='text/css' href='secure/gxt/resources/css/gxt-all.css'/>\n");
        html.append("<link rel='stylesheet' type='text/css' href='General.css'/>\n");
        html.append("<link rel='stylesheet' type='text/css' href='General-menus.css'/>\n");
        List<String> cssImports = Utils.getCssImport();
        for (String css : cssImports) {
            html.append(css);
        }
        html.append("<script type='text/javascript' language='javascript' src='general/general.nocache.js'></script>\n"); //$NON-NLS-1$
        html.append(Utils.getCommonImport());
        List<String> imports = Utils.getJavascriptImport();
        for (String js : imports) {
            html.append(js);
        }
        
        html.append("<link rel='stylesheet' type='text/css' href='secure/css/mdm.css'/>\n");
        // Custom HTML
        String customHtml = MDMConfiguration.getConfiguration().getProperty("mdm.custom.html");
        if (customHtml != null) {
            html.append(customHtml);
        }
        html.append("</head>");
        html.append("<body style=\"-moz-user-select: -moz-none\">");
        html.append("<div id='loading'>");
        html.append("<div class='loading-indicator'>");
        html.append("<img src='secure/gxt/resources/images/default/shared/large-loading.gif' width='32' height='32' style='margin-right:8px;float:left;vertical-align:top;'/>");
        html.append("</div>");
        html.append("</div>");
        html.append("<iframe src=\"javascript:''\" id='__gwt_historyFrame' tabIndex='-1' style='position:absolute;width:0;height:0;border:0'></iframe>");
        html.append("</body>");
        return html.toString();
    }

    @SuppressWarnings("nls")
    private String getHtmlError(String message, Locale locale) {
        String contextPath = this.getServletContext().getContextPath() ;
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head>");
        html.append("<meta http-equiv='Content-Type' content='text/html;charset=UTF-8'/>");
        html.append("<link rel='shortcut icon'  href='" + contextPath + "/auth/favicon.ico'/>\n");
        html.append("<link rel='stylesheet' type='text/css'  href='" + contextPath + "/auth/googleapi/fonts.css'/>\n");
        html.append("<link rel='stylesheet' type='text/css' href='" + contextPath + "/auth/loginPage.css'/>");
        html.append("</head>");
        html.append("<body style='text-align: center;'>");
        html.append("<table width='100%' class='header1' border='0'>");
        html.append("<tr><td height='128' width='50%' class='logo'><img src='" + contextPath + "/auth/logo.png'></td></tr>");
        html.append("<tr><td class='suiteName' id='suiteName' colspan='2' height='56'>Talend MDM</td></tr>");
        html.append("</table>");
        
        String errorTitle = MESSAGES.getMessage(locale, "login.error"); //$NON-NLS-1$
        String backLogin = MESSAGES.getMessage(locale, "back.login"); //$NON-NLS-1$     

        html.append("<h3>").append(errorTitle).append("</h3>"); //$NON-NLS-1$ //$NON-NLS-2$
        html.append("<p><font size='4' color='red'>").append(message).append("</font></p>"); //$NON-NLS-1$ //$NON-NLS-2$
        html.append("<a href='").append(contextPath).append("/auth/logout'>").append(backLogin).append("</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        html.append("</body>");
        html.append("</html>");
        return html.toString();
    }
}
