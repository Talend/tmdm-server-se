// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.server.security;

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.util.Iterator;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;

/**
 * created by pwlin
 *
 */
@SuppressWarnings("nls")
public class SecurityUtils {

    public static ComplexTypeMetadata getUserType() {
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage systemStorage = storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM);
        ComplexTypeMetadata userType = systemStorage.getMetadataRepository().getComplexType("User");
        return userType;
    }

    public static DataRecord retrieveUserDataRecord(String username) throws AuthenticationServiceException {
        DataRecord userDataRecord = null;
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage systemStorage = storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM);
        ComplexTypeMetadata userType = systemStorage.getMetadataRepository().getComplexType("User");
        UserQueryBuilder qb = from(userType).where(eq(userType.getField("username"), username));
        systemStorage.begin();
        try {
            StorageResults users = systemStorage.fetch(qb.getSelect());
            Iterator<DataRecord> iterator = users.iterator();
            if (iterator.hasNext()) {
                userDataRecord = iterator.next();
            }
            systemStorage.commit();
        } catch (Exception e) {
            systemStorage.rollback();
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
        if (userDataRecord == null) {
            throw new AuthenticationServiceException("User '" + username + "' does not exist");
        }
        return userDataRecord;
    }

    public static boolean isAdminUser(String username) {
        return username.equals(MDMConfiguration.getAdminUser());
    }

    public static GrantedAuthority newAuthority(String role) {
        return new SimpleGrantedAuthority(role);
    }

}
