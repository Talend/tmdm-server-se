/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

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
package com.amalto.core.jobox.watch;

import java.util.List;

/**
 * Interface for listening to disk file changes.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 * @see DirMonitor
 */
public interface DirListener {

    /**
     * Called when one of the monitored files are created, deleted or modified.
     *
     * @param newFiles    List of new file names (relative to Jobox container).
     * @param deleteFiles List of deleted file names (relative to Jobox container).
     * @param modifyFiles List of modified file names (relative to Jobox container).
     */
    void fileChanged(List<String> newFiles, List<String> deleteFiles, List<String> modifyFiles);

    /**
     * Called when the job context are changed
     *
     * @param jobFile Name of the job to reload
     * @param context Job context to be used.
     */
    void contextChanged(String jobFile, String context);
}
