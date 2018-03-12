/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.security.jaas;

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.util.core.Crypt;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.server.security.MDMPrincipal;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;

/**
 * Default implementation to work with a JAAS config file of the form:
 * 
 * <pre>
 * MDM  {  
 *   com.amalto.core.server.security.jaas.DefaultLoginModule required 
 *   users="user1,user2,user3,user4" 
 *   passwords="pwd1,pwd2,pwd3,pwd4"
 * };
 * </pre>
 */
public class DefaultLoginModule extends AbstractLoginModule {

    private static final String OPTION_USERS = "logins"; //$NON-NLS-1$

    private static final String OPTION_PASSWORDS = "passwords"; //$NON-NLS-1$

    private Map<String, String> passwordByUserMap = new HashMap<String, String>();

    @Override
    protected void doInitialization(Map<String, ?> options) throws Exception {
        String[] userArray = getArrayFromOption(options, OPTION_USERS);
        String[] passwordArray = getArrayFromOption(options, OPTION_PASSWORDS);
        for (int i = 0; i < userArray.length; i++) {
            if (passwordArray[i].contains(";encrypt")) { //$NON-NLS-1$
                passwordArray[i] = passwordArray[i].replaceAll(";encrypt", Crypt.ENCRYPT); //$NON-NLS-1$
            }
            passwordByUserMap.put(userArray[i], Crypt.decrypt(passwordArray[i]));
        }
    }

    private String[] getArrayFromOption(Map<String, ?> options, String option) {
        String values = (String) options.get(option);
        if (values.contains(Crypt.ENCRYPT)) {
            values = values.replaceAll(Crypt.ENCRYPT, ";encrypt"); //$NON-NLS-1$
        }
        String[] valueArray = StringUtils.splitPreserveAllTokens(values, ',');
        return valueArray;
    }

    @Override
    protected void doLogin() throws Exception {
        if(!passwordByUserMap.containsKey(username)) {
            throw new FailedLoginException("Invalid username"); //$NON-NLS-1$
        }
        String savedPassword = passwordByUserMap.get(username);
        if (password == null || !password.equals(savedPassword)) {
            throw new FailedLoginException("Invalid password"); //$NON-NLS-1$
        }
    }

    @Override
    protected MDMPrincipal doCommit() throws Exception {
        MDMPrincipal principal = new MDMPrincipal(username);

        // Fill roles based on information from database
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage systemStorage = storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM);
        ComplexTypeMetadata userType = systemStorage.getMetadataRepository().getComplexType("User"); //$NON-NLS-1$
        UserQueryBuilder qb = from(userType).where(eq(userType.getField("username"), username)); //$NON-NLS-1$
        systemStorage.begin();
        try {
            StorageResults users = systemStorage.fetch(qb.getSelect());
            DataRecord user = (DataRecord) users.iterator().next();
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) ((DataRecord) user.get("roles")).get("role"); //$NON-NLS-1$ //$NON-NLS-2$
            principal.addRoles(roles);
            systemStorage.commit();
        } catch (Exception e) {
            systemStorage.rollback();
            throw e;
        }

        return principal;
    }

    @Override
    protected void doReset() {
    }
}
