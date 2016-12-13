/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.amalto.core.jobox.JobInfo;

public class JoboxUtil {

    private static final Logger LOGGER = Logger.getLogger(JoboxUtil.class);

    private JoboxUtil() {
    }

    public static void deleteFolder(String folderPath) {
        try {
            deleteAllFiles(folderPath);
            File myFilePath = new File(folderPath);
            if (!myFilePath.exists()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Delete folder failed for '" + folderPath + "' (path does not exist)."); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return;
            }
            if (!myFilePath.delete()) {
                LOGGER.error("Delete folder failed for '" + folderPath + "'."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (Exception e) {
            throw new JoboxException(e);
        }
    }

    public static void cleanFolder(String folderPath) {
        try {
            deleteAllFiles(folderPath);
        } catch (Exception e) {
            throw new JoboxException(e);
        }
    }

    private static void deleteAllFiles(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            LOGGER.warn("Expected a folder '" + path + "' but was a file."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String[] tempList = file.list();
        File temp;
        for (String currentTempFile : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + currentTempFile);
            } else {
                temp = new File(path + File.separator + currentTempFile);
            }
            if (temp.isFile()) {
                if (!temp.delete()) {
                    LOGGER.error("Delete folder failed for '" + currentTempFile + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            if (temp.isDirectory()) {
                deleteAllFiles(path + "/" + currentTempFile);//$NON-NLS-1$
                deleteFolder(path + "/" + currentTempFile);//$NON-NLS-1$
            }
        }
    }

    public static String trimExtension(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int i = filename.lastIndexOf('.');
            if ((i > -1) && (i < (filename.length()))) {
                return filename.substring(0, i);
            }
        }
        return filename;
    }

    public static void extract(String zipPathFile, String destinationPath) throws Exception {
        FileInputStream fins = new FileInputStream(zipPathFile);
        ZipInputStream zipInputStream = new ZipInputStream(fins);
        try {
            ZipEntry ze;
            byte ch[] = new byte[256];
            while ((ze = zipInputStream.getNextEntry()) != null) {
                File zipFile = new File(destinationPath + ze.getName());
                File zipFilePath = new File(zipFile.getParentFile().getPath());
                if (ze.isDirectory()) {
                    if (!zipFile.exists()) {
                        if (!zipFile.mkdirs()) {
                            LOGGER.error("Create folder failed for '" + zipFile.getAbsolutePath() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                    zipInputStream.closeEntry();
                } else {
                    if (!zipFilePath.exists()) {
                        if (!zipFilePath.mkdirs()) {
                            LOGGER.error("Create folder failed for '" + zipFilePath.getAbsolutePath() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
                    try {
                        int i;
                        while ((i = zipInputStream.read(ch)) != -1) {
                            fileOutputStream.write(ch, 0, i);
                        }
                        zipInputStream.closeEntry();
                    } finally {
                        fileOutputStream.close();
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(fins);
            IOUtils.closeQuietly(zipInputStream);
        }

    }

    public static void findFirstFile(JobInfo jobInfo, File root, String fileName, List<File> resultList) {
        if (resultList.size() > 0) {
            return;
        }
        if (root.isFile()) {
            if (root.getName().equals(fileName)) {
                if (jobInfo == null
                        || root.getParentFile().getParentFile().getName().toLowerCase()
                                .startsWith(jobInfo.getName().toLowerCase())) {
                    resultList.add(root);
                }
            }
        } else if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    findFirstFile(jobInfo, file, fileName, resultList);
                }
            }
        }
    }

    private static void zipContents(File dir, String zipPath, ZipOutputStream zos) {
        String[] children = dir.list();
        if (children == null) {
            return;
        }
        for (String currentChild : children) {
            File child = new File(dir, currentChild);
            String childZipPath = zipPath + File.separator + child.getName();
            if (child.isDirectory()) {
                zipContents(child, childZipPath, zos);
            } else {
                try {
                    zip(child, childZipPath, zos);
                } catch (Exception e) {
                    throw new JoboxException(e);
                }
            }
        }
    }

    public static void zip(File file, String zipFilePath) throws IOException {
        if (zipFilePath == null) {
            zipFilePath = file.getAbsolutePath() + ".zip"; //$NON-NLS-1$
        }

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath));
        try {
            if (file.isDirectory()) {
                JoboxUtil.zipContents(file, file.getName(), zos);
            } else {
                try {
                    JoboxUtil.zip(file, file.getName(), zos);
                } catch (Exception e) {
                    throw new JoboxException(e);
                }
            }
        } finally {
            zos.close();
        }
    }

    private static void zip(File file, String zipPath, ZipOutputStream zos) throws IOException {
        FileInputStream is = null;
        try {
            byte[] buf = new byte[1024];
            // Add ZIP entry to output stream.
            zos.putNextEntry(new ZipEntry(zipPath));
            // Transfer bytes from the file to the ZIP file
            int len;
            is = new FileInputStream(file);
            while ((len = is.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private static boolean ignoreLibrary(String name) {
        return false;
    }

    public static URL[] getClasspathURLs(String paths, JobInfo info) {
        List<URL> urls = new ArrayList<URL>();
        if (paths == null || paths.length() <= 0) {
            return new URL[0];
        }
        String separator = System.getProperty("path.separator"); //$NON-NLS-1$
        String[] pathToAdds = paths.split(separator);
        for (String pathToAdd : pathToAdds) {
            if (pathToAdd != null && pathToAdd.length() > 0) {
                try {
                    File fileToAdd = new File(pathToAdd).getCanonicalFile();
                    if (ignoreLibrary(fileToAdd.getName())) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Ignoring " + fileToAdd.toURI().toURL() + " from job '" + info.getName() + "'. "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                    } else {
                        urls.add(fileToAdd.toURI().toURL());
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Added " + fileToAdd.toURI().toURL() + " to job '" + info.getName() + "'. "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                    }
                } catch (IOException e) {
                    throw new JoboxException(e);
                }
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }

    public static String parseMainClassFromJCL(String content) throws IOException {
        String mainClass = null;
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                boolean hasJAL = false;
                Queue<String> myQueue = new LinkedList<String>();
                String[] tokens = line.split("\\s"); //$NON-NLS-1$
                for (String token : tokens) {
                    if (hasJAL && token.trim().length() > 0) {
                        myQueue.offer(token.trim());
                    }
                    if ("java".equals(token)) { //$NON-NLS-1$
                        hasJAL = true;
                    }
                }
                if (hasJAL) {
                    String str;
                    boolean needConsume = false;
                    while ((str = myQueue.poll()) != null) {
                        if (!str.startsWith("-")) { //$NON-NLS-1$
                            if (needConsume) {
                                needConsume = false;// consume
                            } else {
                                mainClass = str;
                                break;
                            }
                        }
                        if (str.startsWith("-")) { //$NON-NLS-1$
                            str = str.substring(1);
                            if (str.startsWith("-")) { //$NON-NLS-1$
                                str = str.substring(1);
                            }
                            // FIXME is there any more?
                            if ("cp".equals(str) || "classpath".equals(str) || "jar".equals(str)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                needConsume = true;
                            }
                        }
                    }
                }
            }
        }
        return mainClass;
    }

}
