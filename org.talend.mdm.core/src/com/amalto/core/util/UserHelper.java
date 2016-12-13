/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.util.Set;

public final class UserHelper {

    private static UserHelper instance;

    private static UserHelper instanceFromRemote;

    private UserManage um;

    private UserHelper(boolean isFromRemote) {
        um = new UserManageOptimizedImpl(isFromRemote);
    }

    public static synchronized UserHelper getInstance() {
        return getInstance(false);
    }

    public static synchronized UserHelper getInstance(boolean isFromRemote) {
        if (!isFromRemote) {
            if (instance == null) {
                instance = new UserHelper(false);
            }
            return instance;
        } else {
            if (instanceFromRemote == null) {
                instanceFromRemote = new UserHelper(true);
            }
            return instanceFromRemote;
        }
    }

    public void overrideUserManage(UserManage _um) {
        this.um = _um;
    }

    /**
     * get viewer users.
     * 
     * @return
     */
    public int getViewerUsers() {
        return um.getViewerUsers();
    }

    /**
     * get the number of normal users.
     * 
     * @return
     */
    public int getNormalUsers() {
        return um.getNormalUsers();
    }

    /**
     * Get the number of admin users.
     * 
     * @return
     */
    public int getNBAdminUsers() {
        return um.getNBAdminUsers();
    }

    /**
     * Get the number of active users.
     * 
     * @return
     */
    public int getActiveUsers() {
        return um.getActiveUsers();
    }

    /**
     * Check if exist the specify user.
     * 
     * @param user
     * @return
     */
    public boolean isExistUser(User user) {
        return um.isExistUser(user);
    }

    /**
     * Check if update active attribute of specify user.
     * 
     * @param user
     * @return
     */
    public boolean isActivatingUser(User user) {
        return um.isActivatingUser(user);
    }

    /**
     * get the rolenames of specify user.
     * 
     * @param user
     * @return
     */
    public Set<String> getOriginalRole(User user) {
        return um.getOriginalRole(user);
    }

    /**
     * check the users.
     */
    public void checkUsers() {
        if (um instanceof UserManageImpl) {
            ((UserManageImpl) um).listUsers();
        }
    }
}
