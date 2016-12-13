/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package talend.ext.images.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ImageDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ImageDeleteServlet.class);

    private String outputFormat;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        outputFormat = config.getInitParameter("output-format"); //$NON-NLS-1$
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String result = onDelete(request);
        response.setContentType("text/html"); //$NON-NLS-1$
        PrintWriter writer = response.getWriter();
        writer.write(result);
        writer.close();
    }

    private String onDelete(HttpServletRequest request) {
        try {
            String uri = request.getParameter("uri"); //$NON-NLS-1$
            if (logger.isDebugEnabled()) {
                logger.debug("Input URI: " + uri); //$NON-NLS-1$
            }
            if (uri == null || uri.length() == 0) {
                return buildDeleteResult(false, "Request parameter 'uri' can not be empty!"); //$NON-NLS-1$
            }
            String contextPath = request.getSession().getServletContext().getContextPath();
            // normalize uri
            uri = normalizeURI(uri, contextPath);
            String toDeleteFilePath = buildDeleteFilePath(uri);
            if (logger.isDebugEnabled()) {
                logger.debug("To Delete File Path: " + toDeleteFilePath); //$NON-NLS-1$
            }
            // delete on file system
            // TODO care about synchronized when delete file
            File toDeleteFile = new File(toDeleteFilePath);
            if (toDeleteFile.exists()) {
                if (toDeleteFile.delete()) {
                    return buildDeleteResult(true, "The target file has been deleted from MDM Image Server successfully! "); //$NON-NLS-1$
                } else {
                    return buildDeleteResult(false, "To delete the target file from file system failed!"); //$NON-NLS-1$
                }
            } else {
                return buildDeleteResult(false, "The target file does not exist on file system!"); //$NON-NLS-1$
            }
        } catch (Exception e) {
            String err = "Exception occurred during deleting!"; //$NON-NLS-1$
            logger.error(err, e);
            return buildDeleteResult(false, err);
        }
    }

    private String normalizeURI(String uri, String contextPath) {
        if (uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        if (uri.startsWith("/")) { //$NON-NLS-1$
            uri = uri.substring(1);
        }
        return uri;
    }

    private static String buildDeleteFilePath(String uri) throws Exception {
        try {
            StringBuffer fullFilename = new StringBuffer();
            String filename = URLDecoder.decode(uri, "UTF-8").substring(uri.indexOf("/") + 1); //$NON-NLS-1$ //$NON-NLS-2$
            filename = StringUtils.replace(filename, "/", File.separator); //$NON-NLS-1$
            fullFilename.append(ImageServerInfo.getInstance().getUploadPath()).append(File.separator).append(filename);
            return fullFilename.toString();
        } catch (UnsupportedEncodingException e) {
            logger.error("Exception occured during decoding URI:" + uri, e); //$NON-NLS-1$
            throw new Exception(e);
        }

    }

    private String buildDeleteResult(boolean success, String message) {
        StringBuilder sb = new StringBuilder();
        if ("xml".equals(outputFormat)) { //$NON-NLS-1$
            sb.append("<DeleteResult>"); //$NON-NLS-1$
            sb.append("<success>").append(success).append("</success>"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("<message>").append(message).append("</message>"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("</DeleteResult>"); //$NON-NLS-1$
        } else if ("json".equals(outputFormat)) { //$NON-NLS-1$
            sb.append("{"); //$NON-NLS-1$
            sb.append("\"success\":").append(success).append(","); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\"message\":\"").append(message).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("}"); //$NON-NLS-1$
        }
        return sb.toString();
    }
}
