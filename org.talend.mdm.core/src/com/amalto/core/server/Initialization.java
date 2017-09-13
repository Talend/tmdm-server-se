/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.jobox.properties.ThreadIsolatedSystemProperties;
import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.objects.configurationinfo.assemble.AssembleConcreteBuilder;
import com.amalto.core.objects.configurationinfo.assemble.AssembleDirector;
import com.amalto.core.objects.configurationinfo.assemble.AssembleProc;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.save.generator.AutoIncrementGenerator;
import com.amalto.core.server.security.SecurityConfig;
import com.amalto.core.storage.DispatchWrapper;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.Version;

public class Initialization implements ApplicationListener<ContextRefreshedEvent>, InitializingBean, DisposableBean, ApplicationEventPublisherAware {

    @Autowired(required = true)
    private ServerLifecycle serverLifecycle;

    private static final Logger LOGGER = Logger.getLogger(Initialization.class);
    
    private Properties previousSystemProperties;
    
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.info("Shutdown in progress..."); //$NON-NLS-1$
        try {
            System.setProperties(previousSystemProperties);
            ServerContext.INSTANCE.get().close();
            LOGGER.info("Shutdown done."); //$NON-NLS-1$
        } catch (Exception e) {
            LOGGER.info("Shutdown done (with error).", e); //$NON-NLS-1$
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String version = Version.getSimpleVersionAsString(Initialization.class);
        LOGGER.info("======================================================="); //$NON-NLS-1$
        LOGGER.info("Talend MDM " + version); //$NON-NLS-1$
        LOGGER.info("======================================================="); //$NON-NLS-1$
        
        //TMDM-2933: ThreadIsolatedSystemProperties allows threads to get different system properties when needed.
        previousSystemProperties = System.getProperties();
        System.setProperties(ThreadIsolatedSystemProperties.getInstance());
        LOGGER.info("Enabled system properties isolation for threads."); //$NON-NLS-1$

        // Initializes server now
        if (serverLifecycle == null) {
            throw new IllegalStateException("Server lifecycle is not set (is server running on a supported platform?)"); //$NON-NLS-1$
        }
        final Server server = ServerContext.INSTANCE.get(serverLifecycle);
        server.init();
        // Initialize system storage
        LOGGER.info("Starting system storage..."); //$NON-NLS-1$
        final StorageAdmin storageAdmin = server.getStorageAdmin();
        String systemDataSourceName = storageAdmin.getDatasource(StorageAdmin.SYSTEM_STORAGE);
        storageAdmin.create(StorageAdmin.SYSTEM_STORAGE, StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM, systemDataSourceName);
        Storage systemStorage = storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM);
        if (systemStorage == null) {
            LOGGER.error("System storage could not start."); //$NON-NLS-1$
            throw new IllegalStateException("Could not start server (unable to initialize system storage)."); //$NON-NLS-1$
        } else {
            LOGGER.info("System storage started."); //$NON-NLS-1$
        }
        // Migration
        LOGGER.info("Initialization and migration of system database..."); //$NON-NLS-1$
        AssembleConcreteBuilder concreteBuilder = new AssembleConcreteBuilder();
        AssembleDirector director = new AssembleDirector(concreteBuilder);
        director.constructAll();
        final AssembleProc assembleProc = concreteBuilder.getAssembleProc();
        SecurityConfig.invokeSynchronousPrivateInternal(new Runnable() {

            @Override
            public void run() {
                assembleProc.run();
            }
        });
        LOGGER.info("Initialization and migration done."); //$NON-NLS-1$
        // Initialize autoincrement id generator
        LOGGER.info("Initializing autoincrement id generator..."); //$NON-NLS-1$
        SecurityConfig.invokeSynchronousPrivateInternal(new Runnable() {

            @Override
            public void run() {
                AutoIncrementGenerator.get();
            }
        });
        LOGGER.info("Autoincrement id generator initialized."); //$NON-NLS-1$
        // Find configured containers
        MetadataRepository repository = systemStorage.getMetadataRepository();
        String className = StringUtils.substringAfterLast(DataClusterPOJO.class.getName(), "."); //$NON-NLS-1$
        ComplexTypeMetadata containerType = repository.getComplexType(ClassRepository.format(className));
        UserQueryBuilder qb = from(containerType);
        final Set<String> containerNames = new HashSet<String>();
        systemStorage.begin();
        try {
            StorageResults containers = systemStorage.fetch(qb.getSelect());
            for (DataRecord container : containers) {
                String name = String.valueOf(container.get("name")); //$NON-NLS-1$
                if (!DispatchWrapper.isMDMInternal(name)) {
                    containerNames.add(name);
                }
            }
            systemStorage.commit();
        } catch (Exception e) {
            systemStorage.rollback();
            throw new RuntimeException("Could not list configured containers", e); //$NON-NLS-1$
        }
        // Log configured containers
        StringBuilder containerNamesLog = new StringBuilder();
        containerNamesLog.append('[').append(' ');
        for (String containerName : containerNames) {
            containerNamesLog.append(containerName).append(' ');
        }
        containerNamesLog.append(']');
        LOGGER.info("Container to initialize (" + containerNames.size() + " found) : " + containerNamesLog); //$NON-NLS-1$ //$NON-NLS-2$

        // Initialize configured containers
        SecurityConfig.invokeSynchronousPrivateInternal(new Runnable() {

            @Override
            public void run() {
                initContainers(server, storageAdmin, containerNames);
            }
        });

        LOGGER.info("Talend MDM " + version + " started."); //$NON-NLS-1$ //$NON-NLS-2$
        if(this.applicationEventPublisher != null){
            this.applicationEventPublisher.publishEvent(new MDMInitializationCompletedEvent(this));
        }
        
    }

    private void initContainers(Server server, StorageAdmin storageAdmin, Set<String> containerNames) {
        int i = 1;
        for (String containerName : containerNames) {
            LOGGER.info("Starting storage " + containerName + "(" + i + " of " + containerNames.size() + ") ..."); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$ //$NON-NLS-4$
            try {
                String datasource = storageAdmin.getDatasource(containerName);
                DataSourceDefinition dataSourceDefinition = server.getDefinition(datasource, containerName);
                storageAdmin.create(containerName, containerName, StorageType.MASTER, datasource);
                if (dataSourceDefinition.hasStaging()) {
                    storageAdmin.create(containerName, containerName, StorageType.STAGING, datasource);
                }
                LOGGER.info("Storage " + containerName + " started."); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (Exception e) {
                LOGGER.warn("Skipping container '" + containerName + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Skipping container '" + containerName + "' due to exception.", e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            i++;
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
