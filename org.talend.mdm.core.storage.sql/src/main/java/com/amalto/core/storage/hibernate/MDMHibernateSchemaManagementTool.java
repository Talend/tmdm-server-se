// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.hibernate;

import java.util.Map;

import org.hibernate.boot.registry.selector.spi.StrategySelector;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.schema.JdbcMetadaAccessStrategy;
import org.hibernate.tool.schema.internal.DefaultSchemaFilterProvider;
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool;
import org.hibernate.tool.schema.internal.IndividuallySchemaMigratorImpl;
import org.hibernate.tool.schema.spi.SchemaCreator;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;
import org.hibernate.tool.schema.spi.SchemaMigrator;

import com.amalto.core.storage.hibernate.mapping.MDMGroupedSchemaMigratorImpl;


/**
 * created by hwzhu on Aug 12, 2020
 * Detailled comment
 *
 */
public class MDMHibernateSchemaManagementTool extends HibernateSchemaManagementTool {

    private static final long serialVersionUID = 1L;

    @Override
    public SchemaCreator getSchemaCreator(Map options) {
        return new MDMSchemaCreatorImpl( this, getSchemaFilterProvider( options ).getCreateFilter() );
    }

    @Override
    public SchemaMigrator getSchemaMigrator(Map options) {
        if ( determineJdbcMetadaAccessStrategy( options ) == JdbcMetadaAccessStrategy.GROUPED ) {
            return new MDMGroupedSchemaMigratorImpl( this, getSchemaFilterProvider( options ).getMigrateFilter() );
        }
        else {
            return new IndividuallySchemaMigratorImpl( this, getSchemaFilterProvider( options ).getMigrateFilter() );
        }
    }

    private JdbcMetadaAccessStrategy determineJdbcMetadaAccessStrategy(Map options) {
        return JdbcMetadaAccessStrategy.interpretSetting( options );
    }

    private SchemaFilterProvider getSchemaFilterProvider(Map options) {
        final Object configuredOption = (options == null)
                ? null
                : options.get( AvailableSettings.HBM2DDL_FILTER_PROVIDER );
        return getServiceRegistry().getService( StrategySelector.class ).resolveDefaultableStrategy(
                SchemaFilterProvider.class,
                configuredOption,
                DefaultSchemaFilterProvider.INSTANCE
        );
    }
}
