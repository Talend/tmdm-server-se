/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.delegator;

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.storage.record.DataRecordXmlWriter;
import com.amalto.core.util.User;
import com.amalto.core.util.XtentisException;

@SuppressWarnings("nls")
public abstract class ILocalUser implements IBeanDelegator {

    public ILocalUser getILocalUser() throws XtentisException {
        return null;
    }

    public HashSet<String> getRoles() {
        HashSet<String> set = new HashSet<String>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                set.add(authority.getAuthority());
            }
        }
        return set;
    }

    public String getUserXML() {
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage systemStorage = storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM);
        ComplexTypeMetadata userType = systemStorage.getMetadataRepository().getComplexType("User");
        UserQueryBuilder qb = from(userType).where(eq(userType.getField("username"), getUsername()));
        DataRecordWriter writer = new DataRecordXmlWriter(userType);
        StringWriter userXml = new StringWriter();
        try {
            systemStorage.begin();
            StorageResults results = systemStorage.fetch(qb.getSelect());
            for (DataRecord result : results) {
                writer.write(result, userXml);
            }
            systemStorage.commit();
        } catch (IOException e) {
            systemStorage.rollback();
            throw new RuntimeException("Could not access user record.", e);
        }
        return userXml.toString();
    }

    public String getIdentity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof LocalUserDetails) {
            return ((LocalUserDetails) principal).getId();
        }
        return (String) principal;
    }
    
    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof LocalUserDetails) {
            return ((LocalUserDetails) principal).getUsername();
        }
        return (String) principal;
    }

    public String getCredentials() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getCredentials();
    }

    public User getUser() {
        User user = new User();
        String xml = getUserXML();
        try {
            if (xml != null) {
                User.parse(xml, user);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not parse user xml.", e);
        }
        return user;
    }

    public boolean isAdmin(Class<?> objectTypeClass) throws XtentisException {
        return true;
    }

    public void logout() throws XtentisException {
        // TODO
    }

    public void resetILocalUsers() throws XtentisException {
    }

    public void setRoles(HashSet<String> roles) {
    }

    public void setUserXML(String userXML) {
    }

    public void setIdentity(String username) {
    }

    public void setUsername(String username) {
    }
    
    public boolean userCanRead(Class<?> objectTypeClass, String instanceId) throws XtentisException {
        return true;
    }

    public boolean userCanWrite() {
        return true;
    }

    public boolean userCanWrite(Class<?> objectTypeClass, String instanceId) throws XtentisException {
        return true;
    }

    public boolean userItemCanRead(ItemPOJO item) throws XtentisException {
        return true;
    }

    public boolean userItemCanWrite(ItemPOJO item, String datacluster, String concept) throws XtentisException {
        return true;
    }

    public User parseWithoutSystemRoles() throws Exception {
        User user = User.parse(getUserXML());
        return user;
    }

}
