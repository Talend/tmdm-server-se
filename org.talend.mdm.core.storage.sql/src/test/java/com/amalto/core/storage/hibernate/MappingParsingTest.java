/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.hibernate;

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.amalto.core.storage.StorageType;
import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.query.StorageTestCase;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.RDBMSDataSource;

public class MappingParsingTest extends TestCase {

    Logger LOGGER = LogManager.getLogger(HibernateStorage.class);
    private Level previousLevel;

    public MappingParsingTest() {

    }

    @Override
    public void setUp() throws Exception {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        
        LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();
        LoggerConfig loggerConfig = conf.getLoggerConfig(LOGGER.getName());
        previousLevel = loggerConfig.getLevel();
        loggerConfig.setLevel(Level.TRACE);
        ctx.updateLoggers(conf);
    }

    @Override
    public void tearDown() throws Exception {
        LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();
        LoggerConfig loggerConfig = conf.getLoggerConfig(LOGGER.getName());
        loggerConfig.setLevel(previousLevel);
        ctx.updateLoggers(conf);
    }

    public void testInheritanceIndexLength() throws Exception {
        // Loads data model
        MetadataRepository repository = new MetadataRepository();
        InputStream dataModel = MappingParsingTest.class.getResourceAsStream("MappingParsingTest_1.xsd");//$NON-NLS-1$
        assertNotNull(dataModel);
        repository.load(dataModel);
        // Creates storage (and overrides SQL name max length)
        Storage storage = new HibernateStorage("MappingParsingTest", StorageType.STAGING); //$NON-NLS-1$
        DataSourceDefinition definition = ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-DS1", "MDM"); //$NON-NLS-1$//$NON-NLS-2$
        RDBMSDataSource stagingDataSource = (RDBMSDataSource) definition.getStaging();
        stagingDataSource.setNameMaxLength(30);
        storage.init(definition);
        // Prepares storage (and adds fields to index).
        ComplexTypeMetadata partyType = repository.getComplexType("Party"); //$NON-NLS-1$
        List<Expression> indexedExpressions = new LinkedList<>();
        indexedExpressions.add(UserQueryBuilder.from(partyType).where(isNull(partyType.getField("PartyType"))).getExpression()); //$NON-NLS-1$
        indexedExpressions.add(UserQueryBuilder.from(partyType).where(isNull(partyType.getField("SixtID"))).getExpression()); //$NON-NLS-1$
        storage.prepare(repository, new HashSet<>(indexedExpressions), true, true);
        // Storage preparation created a DDL file in java.io.tmpdir, compares it to expected DDL.
        InputStream expectedDataInputStream = this.getClass().getResourceAsStream("MappingParsingResult.ddl"); //$NON-NLS-1$
        assertNotNull(expectedDataInputStream);
        String tmpDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
        String filename = tmpDir + File.separator + "MappingParsingTest_STAGING_H2.ddl"; //$NON-NLS-1$
        InputStream resultDataInputStream = new FileInputStream(new File(filename));
        assertNotNull(resultDataInputStream);
        String[] extectedSQLs = IOUtils.toString(expectedDataInputStream).split("\n"); //$NON-NLS-1$
        String[] resultSQLs = IOUtils.toString(resultDataInputStream).split("\n"); //$NON-NLS-1$
        assertEquals(extectedSQLs.length, resultSQLs.length);
        for (int i = 0; i < extectedSQLs.length; i++) {
            assertEquals(StringUtils.replace(extectedSQLs[i], "\r", ""), resultSQLs[i]); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

}
