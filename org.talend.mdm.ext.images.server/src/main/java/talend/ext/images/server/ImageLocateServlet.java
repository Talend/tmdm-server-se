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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class ImageLocateServlet
 */
public class ImageLocateServlet extends HttpServlet {

    private static final long serialVersionUID = -3012919798771313147L;

    private static final Logger LOGGER = Logger.getLogger(ImageLocateServlet.class);

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String resourceFilePath = getResourceFilePath(request);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Resource file path: '" + resourceFilePath + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            File resourceFile = new File(resourceFilePath);
            if (resourceFile.exists()) {

                String strWidth = request.getParameter("width"); //$NON-NLS-1$
                String strHeight = request.getParameter("height"); //$NON-NLS-1$
                String strPreserveAspectRatio = request.getParameter("preserveAspectRatio"); //$NON-NLS-1$

                if (strWidth != null && strHeight != null) {
                    int width = Integer.valueOf(strWidth);
                    int height = Integer.valueOf(strHeight);
                    boolean preserveAspectRatio = Boolean.valueOf(strPreserveAspectRatio);
                    Thumbnails.of(resourceFile).size(width, height).keepAspectRatio(preserveAspectRatio)
                            .toOutputStream(response.getOutputStream());
                    response.getOutputStream().flush();
                } else {
                    FileInputStream fis = new FileInputStream(resourceFile);
                    try {
                        IOUtils.copy(fis, response.getOutputStream());
                    } finally {
                        IOUtils.closeQuietly(fis);
                    }
                    response.getOutputStream().flush();
                }

            } else {
                LOGGER.error("Resource file '" + resourceFilePath + "' not found!"); //$NON-NLS-1$ //$NON-NLS-2$
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found!"); //$NON-NLS-1$
                return;
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found!"); //$NON-NLS-1$
            return;
        }

    }

    private String getResourceFilePath(HttpServletRequest req) {
        // According to the servlet spec:
        // HttpServletRequest.getPathInfo() should be decoded by the web container;
        // HttpServletRequest.getRequestURI() should not be decoded by the web container.
        // In Tomcat connector URIEncoding defaults to ISO-8859-1 but might be changed.
        // So better to not rely onto getPathInfo() and ensure we got the 
        // raw UTF-8 value (thx to our CharacterEncodingFilter)
        String requestURI = req.getRequestURI();
        String path = StringUtils.substringAfterLast(requestURI, "/upload"); //$NON-NLS-1$
        try {
            path = URLDecoder.decode(path, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        path = ImageServerInfo.getInstance().getUploadPath() + File.separator + path;
        path = path.replaceAll("\\\\", "/"); //$NON-NLS-1$//$NON-NLS-2$
        return path;
    }
}
