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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.amalto.core.objects.datacluster.DataClusterPOJOPK;

public class UserManageImpl extends UserManage {

    private static final Logger log = Logger.getLogger(UserManageImpl.class);

    private static List<User> users;

    /**
     * Get the number of active users.
     * 
     * @return
     */
    @Override
    public int getActiveUsers() {
        List<User> activeUsers = new ArrayList<User>();

        for (User user : getUsers()) {
            if (user.enabled) {
                activeUsers.add(user);
            }
        }

        return activeUsers.size();
    }

    /**
     * Check if exist the specify user.
     * 
     * @param user
     * @return
     */
    @Override
    public boolean isExistUser(User user) {
        boolean result = false;

        for (User existUser : getUsers()) {
            if (user.getUserName().equals(existUser.getUserName())) {
                return true;
            }
        }

        return result;
    }

    /**
     * Check if update active attribute of specify user.
     * 
     * @param user
     * @return
     */
    @Override
    public boolean isActivatingUser(User user) {
        boolean result = false;

        if (!user.isEnabled()) {
            return false;
        }

        for (User existUser : getUsers()) {
            if (existUser.getUserName().equals(user.getUserName())) {
                return existUser.isEnabled() != user.isEnabled();
            }
        }
        return result;
    }

    /**
     * get the rolenames of specify user.
     * 
     * @param user
     * @return
     */
    @Override
    public Set<String> getOriginalRole(User user) {
        Set<String> result = new HashSet<String>();

        for (User existUser : getUsers()) {
            if (existUser.getUserName().equals(user.getUserName())) {
                result = existUser.getRoleNames();
                break;
            }
        }

        return result;
    }

    @Override
    protected int getUserCount(String matchRole) {
        int userCount = 0;
        for (User user : getUsers()) {
            if (user.enabled) {
                Set<String> userRoles = user.getRoleNames();
                if (userRoles.contains(matchRole)) {
                    userCount++;
                    break;
                }
            }
        }
        return userCount;
    }

    private List<User> getUsers() {
        if (users == null) {
            listUsers();
        }
        return users;
    }

    List<User> listUsers() {
        List<String> results = new ArrayList<String>();
        users = new ArrayList<User>();

        try {
            results = Util.getItemCtrl2Local().getItems(new DataClusterPOJOPK(PROVISIONING_CLUSTER), USER_CONCEPT, null, 0, 0,
                    Integer.MAX_VALUE, false);

            for (String userXML : results) {
                User user = User.parse(userXML);
                users.add(user);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return users;
    }
}
