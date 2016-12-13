/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.servlet;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.amalto.core.jobox.JobContainer;
import com.amalto.core.util.Util;

public class UploadFile extends HttpServlet {

    private static final long serialVersionUID = 1254957429740848823L;

    private static final Logger LOG = Logger.getLogger(UploadFile.class);

    private static final String PARAMETER_CONTEXT = "contextStr"; //$NON-NLS-1$

    private static final String PARAMETER_DEPLOY_JOB = "deployjob"; //$NON-NLS-1$

    private static final String PARAMETER_DELETE_FILE = "deletefile"; //$NON-NLS-1$ 

    private String containerWebAppsPath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String path = getServletConfig().getServletContext().getRealPath("/"); //$NON-NLS-1$
        File webAppPath = new File(path);
        containerWebAppsPath = webAppPath.getParent();
        if (LOG.isDebugEnabled()) {
            LOG.debug("container webapps path-->" + path); //$NON-NLS-1$
        }
    }

    @Override
    // Should be HTTP PUT
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Writer writer = response.getWriter();
        // delete file ?
        String deleteFilename = request.getParameter(PARAMETER_DELETE_FILE);
        if (deleteFilename != null) {
            deleteFile(deleteFilename, writer);
        } else {
            uploadFile(request, writer);
        }
    }

    private void uploadFile(HttpServletRequest req, Writer writer) throws ServletException, IOException {
        // upload file
        if (!ServletFileUpload.isMultipartContent(req)) {
            throw new ServletException("Upload File Error: the request is not multipart!"); //$NON-NLS-1$
        }
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();

        // Set upload parameters
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(0);
        upload.setFileItemFactory(factory);
        upload.setSizeMax(-1);

        // Parse the request
        List<FileItem> items;
        try {
            items = upload.parseRequest(req);
        } catch (Exception e) {
            throw new ServletException(e.getMessage(), e);
        }

        // Process the uploaded items
        if (items != null && items.size() > 0) {
            // Only one file
            Iterator<FileItem> iter = items.iterator();
            FileItem item = iter.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(item.getFieldName());
            }

            File file = null;
            if (!item.isFormField()) {
                try {
                    String filename = item.getName();
                    if (req.getParameter(PARAMETER_DEPLOY_JOB) != null) {
                        String contextStr = req.getParameter(PARAMETER_CONTEXT);
                        file = writeJobFile(item, filename, contextStr);
                    } else if (filename.endsWith(".bar")) { //$NON-NLS-1$
                        file = writeWorkflowFile(item, filename);
                    } else {
                        throw new IllegalArgumentException("Unknown deployment for file '" + filename + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (Exception e) {
                    throw new ServletException(e.getMessage(), e);
                }
            } else {
                throw new ServletException("Couldn't process request"); //$NON-NLS-1$);
            }
            String urlRedirect = req.getParameter("urlRedirect"); //$NON-NLS-1$
            if (Boolean.valueOf(urlRedirect)) {
                String redirectUrl = req.getContextPath() + "?mimeFile=" + file.getName(); //$NON-NLS-1$
                writer.write(redirectUrl);
            } else {
                writer.write(file.getAbsolutePath());
            }
        }
        writer.close();
    }

    private File writeJobFile(FileItem item, String filename, String context) throws Exception {
        File file;
        if (item.getName().endsWith(".zip")) { //$NON-NLS-1$
            String dir = JobContainer.getUniqueInstance().getDeployDir();
            file = new File(dir + File.separator + filename);

            // better handle concurrent file system modifications
            // see com.amalto.core.jobox.watch.JoboxListener.fileChanged(List<String>, List<String>,
            // List<String>)
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Deleting file '" + file.getAbsolutePath() + "'"); //$NON-NLS-1$//$NON-NLS-2$
                }
                item.write(file);
                JobContainer jobContainer = JobContainer.getUniqueInstance();
                jobContainer.setContextStrToBeSaved(file.getAbsolutePath(), context);
            } catch (EOFException e) {
                LOG.warn("Attempted to to update job '" + item.getName() + "' but is being modified by concurrent process."); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (ZipException e) {
                LOG.warn("Attempted to to update job '" + item.getName() + "' but is being modified by concurrent process."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else if (item.getName().endsWith(".war")) { //$NON-NLS-1$
            file = new File(containerWebAppsPath + File.separator + filename);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Writing file '" + file.getAbsolutePath() + "'"); //$NON-NLS-1$//$NON-NLS-2$
            }
            item.write(file);
        } else {
            throw new IllegalArgumentException("Unknown job deployment for file '" + filename + "'"); //$NON-NLS-1$//$NON-NLS-2$
        }
        return file;
    }

    private File writeWorkflowFile(FileItem item, String filename) throws Exception {
        String barpath = Util.getBarHomeDir();
        if (!new File(barpath).exists()) {
            new File(barpath).mkdir();
        }
        File file = new File(barpath + File.separator + filename);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Writing file '" + file.getAbsolutePath() + "'"); //$NON-NLS-1$//$NON-NLS-2$
        }
        item.write(file);
        return file;
    }

    private void deleteFile(String filename, Writer writer) throws IOException {
        File file;
        if (filename.endsWith(".zip")) { //$NON-NLS-1$
            file = new File(JobContainer.getUniqueInstance().getDeployDir() + File.separator + filename);
        } else if (filename.endsWith(".war")) { //$NON-NLS-1$
            file = new File(containerWebAppsPath + File.separator + filename);
        } else if (filename.endsWith(".bar")) { //$NON-NLS-1$
            String barpath = Util.getBarHomeDir();
            file = new File(barpath + File.separator + filename);
        } else {
            throw new IllegalArgumentException("No support for file '" + filename + "'"); //$NON-NLS-1$//$NON-NLS-2$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting file '" + file.getAbsolutePath() + "'"); //$NON-NLS-1$//$NON-NLS-2$
        }
        file.delete();
        writer.write("Delete sucessfully"); //$NON-NLS-1$
        writer.close();
        return;
    }
}
