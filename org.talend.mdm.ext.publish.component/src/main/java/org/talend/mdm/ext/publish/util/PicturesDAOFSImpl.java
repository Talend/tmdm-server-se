/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;

public class PicturesDAOFSImpl implements PicturesDAO {

    private static final Logger LOGGER = Logger.getLogger(PicturesDAOFSImpl.class);

    private String picturesLocation;

    private static final String FILTER_IMAGR_START_SYMBOL = "."; //$NON-NLS-1$

    public PicturesDAOFSImpl(String picturesLocation) {
        this.picturesLocation = picturesLocation; 
    }

    public String[] getAllPKs() {

        List<String> pks = new ArrayList<String>();
        
        // The imageserver upload home conversion:
        // Use catalog folders under root directory
        // Store imagefiles within each catalog folder
        // Do not support multi level catalogs
        File uploadHome = new File(picturesLocation);
        if (uploadHome.exists()) {
            File[] catalogs = uploadHome.listFiles();
            for (int i = 0; i < catalogs.length; i++) {
                String pk = ""; //$NON-NLS-1$
                if (catalogs[i].isDirectory()) {
                    String prefix = catalogs[i].getName() + "-"; //$NON-NLS-1$
                    File[] pictures = catalogs[i].listFiles();
                    for (int j = 0; j < pictures.length; j++) {
                        // FIXME Concurrent issue
                        if (allowedImageResource(pictures[j])) {
                            pk = prefix;
                            pk += pictures[j].getName();
                            pks.add(pk);
                        }
                    }
                } else {
                    // on root level
                    if (allowedImageResource(catalogs[i])) {
                        pk = catalogs[i].getName();

                        // for the case file name with "-" under root folder
                        if (pk.indexOf("-") != -1) //$NON-NLS-1$
                            pks.add("-" + pk); //$NON-NLS-1$
                        else
                            pks.add(pk);
                    }
                }

            }
        }

        return (String[]) pks.toArray(new String[pks.size()]);
    }

    private boolean allowedImageResource(File file) {
        if (file == null)
            return false;

        if (file.exists() && file.isFile() && !file.getName().startsWith(FILTER_IMAGR_START_SYMBOL) && isImage(file))
            return true;

        return false;
    }

    private boolean isImage(File file) {
        ImageInputStream is = null;
        try {
            is = ImageIO.createImageInputStream(file);
            if (is == null) {
                return false;
            }

            Iterator<ImageReader> iter = ImageIO.getImageReaders(is);
            if (!iter.hasNext()) {
                return false;
            } else {
                ImageReader reader = iter.next();
                if (reader.getFormatName() != null && reader.getFormatName().length() > 0)
                    return true;
            }

        } catch (IOException e) {
            LOGGER.error("Error on creating image inputstream! ", e); //$NON-NLS-1$
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                LOGGER.error("Error on closing image inputstream! ", e); //$NON-NLS-1$
            }
        }

        return false;
    }
}
