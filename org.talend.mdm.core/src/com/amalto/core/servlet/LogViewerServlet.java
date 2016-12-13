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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

import com.amalto.core.servlet.FileChunkLoader.FileChunkInfo;

public class LogViewerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private File file;

    private int defaultMaxLines;

    private String charset;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String location = config.getInitParameter("logFile"); //$NON-NLS-1$
        try {
            file = getLogFile(location);
        } catch (FileNotFoundException e) {
            throw new ServletException(e.getMessage(), e);
        }

        String defaultMaxLinesString = config.getInitParameter("maxLinesByChunk"); //$NON-NLS-1$
        defaultMaxLines = Integer.parseInt(defaultMaxLinesString);
        charset = config.getInitParameter("charset"); //$NON-NLS-1$
        if (charset == null) {
            charset = Charset.defaultCharset().name();
        }
    }

    /**
     * The expected calls are
     * <ul>
     * <li>GET /log HTTP/1.1<br/>
     * => Request to download the whole file</li>
     * <li>GET /log?position=x HTTP/1.1<br/>
     * => Request to get file chunk at position x with default max of lines</li>
     * <li>GET /log?position=x&maxLines=y HTTP/1.1<br/>
     * => Request to get file chunk at position x with specified max of lines</li>
     * </ul>
     * <p>
     * If position parameter is negative, it will start from end of file ('tail -maxLines' like)
     * </p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long position;
        String positionString = request.getParameter("position"); //$NON-NLS-1$
        if (positionString != null) {
            position = Long.parseLong(positionString);
            int maxLines;
            String maxLinesString = request.getParameter("maxLines"); //$NON-NLS-1$
            if (maxLinesString != null) {
                maxLines = Integer.parseInt(maxLinesString);
            } else {
                maxLines = defaultMaxLines;
            }
            writeLogFileChunk(position, maxLines, response);
        } else {
            downloadLogFile(response);
        }
    }

    private void writeLogFileChunk(long position, int maxLines, HttpServletResponse response) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        FileChunkLoader loader = new FileChunkLoader(file);
        FileChunkInfo chunkInfo = loader.loadChunkTo(bufferedOutputStream, position, maxLines);
        bufferedOutputStream.close();

        response.setContentType("text/plain;charset=" + charset); //$NON-NLS-1$
        response.setCharacterEncoding(charset);
        response.setHeader("X-Log-Position", String.valueOf(chunkInfo.nextPosition)); //$NON-NLS-1$
        response.setHeader("X-Log-Lines", String.valueOf(chunkInfo.lines)); //$NON-NLS-1$

        OutputStream responseOutputStream = response.getOutputStream();
        outputStream.writeTo(responseOutputStream);
        responseOutputStream.close();
    }

    private void downloadLogFile(HttpServletResponse response) throws IOException {
        String filename = file.getAbsolutePath();
        filename = FilenameUtils.getName(filename);
        response.setContentType("text/x-log; name=\"" + filename + '"'); //$NON-NLS-1$
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + '"'); //$NON-NLS-1$ //$NON-NLS-2$
        FileInputStream fis = new FileInputStream(file);
        try {
            OutputStream responseOutputStream = response.getOutputStream();
            IOUtils.copyLarge(fis, responseOutputStream);
            responseOutputStream.close();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
                // ignore it
            }
        }
    }

    private File getLogFile(String location) throws FileNotFoundException {
        String resolvedLocation = SystemPropertyUtils.resolvePlaceholders(location);
        File file = ResourceUtils.getFile(resolvedLocation);
        if (!file.exists()) {
            throw new FileNotFoundException("Log file [" + resolvedLocation + "] not found");
        }
        return file;
    }
}
