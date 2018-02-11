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

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.talend.mdm.commmon.util.core.ICoreConstants;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.server.security.MDMPrincipal;
import com.amalto.core.util.LocalUser;

public abstract class AbstractLoginModule implements LoginModule {

    private static final Logger LOGGER = Logger.getLogger(AbstractLoginModule.class);

    public static final String AUTH_LOGIN_NAME = "javax.security.auth.login.name"; //$NON-NLS-1$

    public static final String AUTH_LOGIN_PASSWORD = "javax.security.auth.login.password"; //$NON-NLS-1$

    private static final String OPTION_USE_FIRST_PASS = "useFirstPass"; //$NON-NLS-1$

    private static final String OPTION_STORE_PASS = "storePass"; //$NON-NLS-1$

    private static final String OPTION_RESET_PASS = "resetPass"; //$NON-NLS-1$

    private static final String OPTION_ADMIN_MD5_PASSWORD = "adminMD5Password"; //$NON-NLS-1$

    protected String username;

    protected String password;

    protected Md5PasswordEncoder md5PasswordEncoder;

    // options
    private boolean useFirstPass;

    private boolean storePass;

    private boolean resetPass;

    private boolean adminMD5Password;

    // initial state
    private Subject subject;

    private CallbackHandler callbackHandler;

    @SuppressWarnings("rawtypes")
    private Map sharedState;

    // the authentication status
    private MDMPrincipal principal;

    private boolean succeeded;

    private boolean commitSucceeded;

    public AbstractLoginModule() {
    }

    public final void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;

        String option = (String) options.get(OPTION_USE_FIRST_PASS);
        useFirstPass = Boolean.valueOf(option);
        option = (String) options.get(OPTION_STORE_PASS);
        storePass = Boolean.valueOf(option);
        option = (String) options.get(OPTION_RESET_PASS);
        resetPass = Boolean.valueOf(option);
        option = (String) options.get(OPTION_ADMIN_MD5_PASSWORD);
        adminMD5Password = Boolean.valueOf(option);

        md5PasswordEncoder = new Md5PasswordEncoder();

        try {
            doInitialization(options);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            }
            throw new IllegalStateException();
        }
    }

    public final boolean login() throws LoginException {
        if (useFirstPass) {
            // use name and password saved by the first module in the stack
            retrievePass();
            succeeded = true;
        } else {
            handleCallbacks();
            try {

                if (LocalUser.isAdminUser(username)) {
                    String adminPassword = MDMConfiguration.getAdminPassword();
                    if (adminMD5Password) {
                        if (!md5PasswordEncoder.isPasswordValid(adminPassword, password, null)) {
                            throw new FailedLoginException("Invalid password"); //$NON-NLS-1$;
                        }
                    } else {
                        if (!password.equals(adminPassword)) {
                            throw new FailedLoginException("Invalid password"); //$NON-NLS-1$;
                        }
                    }
                } else {
                    doLogin();
                }
                succeeded = true;
                storePass();
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(e.getMessage(), e);
                }
                throw new FailedLoginException();
            }
        }
        return succeeded;
    }

    public final boolean commit() throws LoginException {
        if (!succeeded) {
            return false;
        }
        try {
            if (LocalUser.isAdminUser(username)) {
                principal = new MDMPrincipal(username);
                principal.addRole(ICoreConstants.ADMIN_PERMISSION);
            } else {
                principal = doCommit();
                // Any other user has access to the UI
                principal.addRole(ICoreConstants.UI_AUTHENTICATED_PERMISSION);
            }
            // Add authenticated role for all users
            principal.addRole(ICoreConstants.AUTHENTICATED_PERMISSION);
            Set<Principal> principals = subject.getPrincipals();
            if (!principals.contains(principal)) {
                subject.getPrincipals().add(principal);
            }
            // in any case, reset
            reset();
            commitSucceeded = true;
            return true;
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            }
            reset();
            return false;
        }
    }

    public final boolean abort() {
        if (!succeeded) {
            reset();
            return false;
        }
        if (succeeded && !commitSucceeded)
            reset();
        else
            logout();
        return true;
    }

    public final boolean logout() {
        subject.getPrincipals().remove(principal);
        reset();
        return true;
    }

    protected abstract void doInitialization(Map<String, ?> options) throws Exception;

    protected abstract void doLogin() throws Exception;

    protected abstract MDMPrincipal doCommit() throws Exception;

    protected abstract void doReset() throws Exception;

    private void retrievePass() {
        username = (String) sharedState.get(AUTH_LOGIN_NAME);
        Object object = sharedState.get(AUTH_LOGIN_PASSWORD);
        if (object instanceof char[])
            password = new String((char[]) object);
        else if (object != null) {
            password = object.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private void storePass() {
        if (storePass) {
            sharedState.put(AUTH_LOGIN_NAME, username);
            sharedState.put(AUTH_LOGIN_PASSWORD, password);
        }
    }

    private void resetPass() {
        if (resetPass) {
            sharedState.remove(AUTH_LOGIN_NAME);
            sharedState.remove(AUTH_LOGIN_PASSWORD);
        }
    }

    private void reset() {
        principal = null;
        username = null;
        password = null;
        succeeded = false;
        commitSucceeded = false;
        resetPass();
        try {
            doReset();
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            }
            // ignore it
        }
    }

    private void handleCallbacks() throws LoginException {
        if (callbackHandler == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No CallbackHandler available to collect authentication information"); //$NON-NLS-1$;
            }
            throw new LoginException();
        }
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("Username: "); //$NON-NLS-1$
        callbacks[1] = new PasswordCallback("Password: ", false); //$NON-NLS-1$

        try {
            callbackHandler.handle(callbacks);
        } catch (IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            }
            throw new LoginException();
        } catch (UnsupportedCallbackException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        e.getCallback().toString() + " not available to retrieve authentication information from the user", e); //$NON-NLS-1$;
            }
            throw new LoginException();
        }
        username = ((NameCallback) callbacks[0]).getName();
        char tmpPassword[] = ((PasswordCallback) callbacks[1]).getPassword();
        if (tmpPassword != null) {
            char[] credential = new char[tmpPassword.length];
            System.arraycopy(tmpPassword, 0, credential, 0, tmpPassword.length);
            ((PasswordCallback) callbacks[1]).clearPassword();
            password = new String(credential);
        }
    }
}