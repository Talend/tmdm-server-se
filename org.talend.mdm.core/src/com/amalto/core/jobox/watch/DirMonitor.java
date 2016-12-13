/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.watch;

/*
 * This code is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

import com.amalto.core.jobox.JobContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for monitoring changes in disk files. Usage:
 * 
 * 1. Implement the FileListener interface. 2. Create a FileMonitor instance. 3. Add the file(s)/directory(ies) to
 * listen for.
 * 
 * fileChanged() will be called when a monitored file is created, deleted or its modified time changes.
 * 
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class DirMonitor {

    private final Timer timer;

    private final Map<File, DirLog> files; // File -> Long

    private final Collection<DirListener> listeners; // of (FileListener)

    /**
     * Create a file monitor instance with specified polling interval.
     * 
     * @param pollingInterval Polling interval in milli seconds.
     */
    public DirMonitor(long pollingInterval) {
        files = new HashMap<File, DirLog>();
        listeners = new ArrayList<DirListener>();

        timer = new Timer();
        timer.schedule(new FileMonitorNotifier(), 0, pollingInterval);
    }

    /**
     * Stop the file monitor polling.
     */
    public void stop() {
        timer.cancel();
    }

    /**
     * Add file to listen for. File may be any java.io.File (including a directory) and may well be a non-existing file
     * in the case where the creating of the file is to be trepped.
     * <p>
     * More than one file can be listened for. When the specified file is created, modified or deleted, listeners are
     * notified.
     * 
     * @param file File to listen for.
     */
    public void addFile(File file) {
        if (!files.containsKey(file)) {
            files.put(file, new DirLog(file));
        }
    }

    /**
     * Add listener to this file monitor.
     * 
     * @param fileListener Listener to add.
     */
    public void addListener(DirListener fileListener) {
        listeners.add(fileListener);
    }

    /**
     * call listener to change the job context
     */
    public void changeContextStr(String entityPath, String context) {
        for (DirListener listener : listeners) {
            listener.contextChanged(entityPath, context);
        }
    }

    /**
     * This is the timer thread which is executed every n milliseconds according to the setting of the file monitor. It
     * investigates the file in question and notify listeners if changed.
     */
    private class FileMonitorNotifier extends TimerTask {

        public void run() {
            // Loop over the registered files and see which have changed.
            // Use a copy of the list in case listener wants to alter the
            // list within its fileChanged method.
            Collection<File> files = new ArrayList<File>(DirMonitor.this.files.keySet());

            for (File file : files) {
                DirLog lastDirLog = DirMonitor.this.files.get(file);
                DirLog newDirLog = new DirLog(file);

                Map<String, Long> lastFilesMap = lastDirLog.getFilesModifiedTime();
                Map<String, Long> newFilesMap = newDirLog.getFilesModifiedTime();

                List<String> newFiles = new ArrayList<String>();
                List<String> deleteFiles = new ArrayList<String>();
                List<String> modifyFiles = new ArrayList<String>();

                for (String fileName : newFilesMap.keySet()) {
                    if (lastFilesMap.containsKey(fileName)) {
                        long lastModifiedTime = lastFilesMap.get(fileName);
                        long newModifiedTime = newFilesMap.get(fileName);
                        if (newModifiedTime != lastModifiedTime) {
                            modifyFiles.add(fileName);
                        }
                    } else {
                        newFiles.add(fileName);
                    }
                }

                for (String fileName : lastFilesMap.keySet()) {
                    if (!newFilesMap.containsKey(fileName)) {
                        deleteFiles.add(fileName);
                    }
                }

                if (newFiles.size() > 0 || deleteFiles.size() > 0 || modifyFiles.size() > 0) {

                    // Register new modified time
                    DirMonitor.this.files.put(file, newDirLog);

                    // Notify listeners
                    JobContainer container = JobContainer.getUniqueInstance();
                    try {
                        container.lock(true);
                        for (DirListener listener : listeners) {
                            listener.fileChanged(newFiles, deleteFiles, modifyFiles);
                        }
                    } finally {
                        container.unlock(true);
                    }
                }
            }
        }
    }
}
