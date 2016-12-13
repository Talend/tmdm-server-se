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

import org.apache.log4j.Logger;

public class ImageServerInfo {

    private static final Logger logger = Logger.getLogger(ImageServerInfo.class);

    private static ImageServerInfo instance;

    private String uploadPath;

    private String tempPath;

    private String locateBaseUrl;

    private boolean initialized = false;

    public static synchronized ImageServerInfo createInstance() {
        if (instance == null) {
            instance = new ImageServerInfo();
        }
        return instance;
    }

    public static synchronized ImageServerInfo getInstance() {
        if (instance == null || !instance.initialized) {
            throw new IllegalStateException();
        }
        return instance;
    }
    
    private ImageServerInfo() {
    }

    public synchronized void init() {
        if (initialized)
            throw new IllegalStateException();

        logger.info("Images upload Base Url: " + locateBaseUrl); //$NON-NLS-1$

        File uploadFolder = new File(uploadPath);
        File tempUploadFolder = new File(tempPath);

        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        if (!tempUploadFolder.exists()) {
            tempUploadFolder.mkdirs();
        }

        if (!uploadFolder.exists() || !uploadFolder.canWrite() || !tempUploadFolder.exists() || !tempUploadFolder.canWrite()) {
            throw new IllegalStateException("Image Upload directory or Upload temp directory is not available for writing!"); //$NON-NLS-1$
        } else {
            logger.info("Images Upload Base Path: " + uploadPath); //$NON-NLS-1$
            logger.info("Images Temporary Base Path: " + tempPath); //$NON-NLS-1$
        }
        initialized = true;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public void setLocateBaseUrl(String locateBaseUrl) {
        this.locateBaseUrl = locateBaseUrl;
    }

    public String getUploadPath() {
        if (!initialized) {
            throw new IllegalStateException();
        }
        return uploadPath;
    }

    public String getTempPath() {
        if (!initialized) {
            throw new IllegalStateException();
        }
        return tempPath;
    }

    public String getLocateBaseUrl() {
        if (!initialized) {
            throw new IllegalStateException();
        }
        return locateBaseUrl;
    }
}
