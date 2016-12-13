/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage;

import org.hibernate.boot.registry.selector.SimpleStrategyRegistrationImpl;
import org.hibernate.boot.registry.selector.StrategyRegistration;
import org.hibernate.boot.registry.selector.StrategyRegistrationProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import java.util.Collections;
import java.util.List;

public class DBCPRegistrationProviderImpl implements StrategyRegistrationProvider {
    private static final List<StrategyRegistration> REGISTRATIONS = Collections.singletonList(
            (StrategyRegistration) new SimpleStrategyRegistrationImpl<>(
                    ConnectionProvider.class,
                    DBCPConnectionProvider.class,
                    "dbcp",
                    DBCPConnectionProvider.class.getSimpleName()
            )
    );

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<StrategyRegistration> getStrategyRegistrations() {
        return REGISTRATIONS;
    }
}