/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.task;

import org.talend.mdm.commmon.metadata.MetadataRepository;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.context.SaverSource;
import com.amalto.core.storage.Storage;

public class StagingConfiguration {
    private final Storage origin;
    private final MetadataRepository stagingRepository;
    private final MetadataRepository userRepository;
    private final SaverSource source;
    private final SaverSession.Committer committer;
    private final Storage destination;
    private final Filter filter;

    public StagingConfiguration(Storage origin,
                                SaverSource source,
                                SaverSession.Committer committer,
                                Storage destination,
                                Filter filter) {
        this.origin = origin;
        this.stagingRepository = origin.getMetadataRepository();
        this.userRepository = destination.getMetadataRepository();
        this.source = source;
        this.committer = committer;
        this.destination = destination;
        this.filter = filter;
    }

    public Storage getOrigin() {
        return origin;
    }

    public MetadataRepository getStagingRepository() {
        return stagingRepository;
    }

    public MetadataRepository getUserRepository() {
        return userRepository;
    }

    public SaverSource getSource() {
        return source;
    }

    public SaverSession.Committer getCommitter() {
        return committer;
    }

    public Storage getDestination() {
        return destination;
    }
    
    public Filter getFilter() {
        return filter;
    }
}
