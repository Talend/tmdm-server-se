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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.XtentisException;

/**
 * created by pwlin
 *
 */
@SuppressWarnings("nls")
public class SecurityUtils {

    public static String ID_TOKEN = "id_token";

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

    @SuppressWarnings("unchecked")
    public static String getIdToken() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> map = (Map<String, Object>) authentication.getUserAuthentication().getDetails();
        return ID_TOKEN + " " + map.get(ID_TOKEN);
    }

    public static OAuth2AccessToken buildAccessToken(String password) {
        if (!password.startsWith(ID_TOKEN)) {
            return null;
        }
        String idToken = password.substring(ID_TOKEN.length() + 1);
        DefaultOAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(StringUtils.EMPTY);
        Map<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put(ID_TOKEN, idToken);
        accessToken.setAdditionalInformation(additionalInformation);
        return accessToken;
    }

    /**
     * Return id_token if using SSO, else return password
     * 
     * @return
     */
    public static String getCredentials() {
        if (MDMConfiguration.isIamEnabled()) {
            return getIdToken();
        } else {
            try {
                return LocalUser.getLocalUser().getPassword();
            } catch (XtentisException e) {
                return StringUtils.EMPTY;
            }
        }
    }
}
