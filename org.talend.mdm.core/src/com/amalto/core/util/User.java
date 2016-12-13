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

import java.util.*;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class User implements Cloneable {
    
    private static final Logger LOG = Logger.getLogger(User.class);

    Integer ID;

    String userName;

    String givenName;

    String familyName;

    String phoneNumber;

    String realEmail;

    String fakeEmail;

    String company;

    long registrationDateAsLong;

    long lastVisitDateAsLong;
    
    long lastSyncTimeAsLong;

    String password;

    boolean enabled;

    boolean viewRealEmail;

    String signature;

    String homePage;

    String universe;

    String language;

    Set<Role> roles;

    Map<String, String> properties;

    Map<String, String> applications;

    Set<String> roleNames;

    public User() {

    }

    /**
     * User format
     * 
     * <user> <username/> <password/> <givename/> <familyname/> <portalid/> <signature/> <realemail/> <fakeemail/>
     * <viewrealemail/> <compagny/> <registrationdate/> <lastvisitdate/> <enabled/> <homePage> <roles> <role/> </roles>
     * <properties> <property> <name/> <value/> </property> </properties> <applications> <application> <name/> <value/>
     * </application> </applications> </user>
     */

    public String serialize() {
        String user = "<User>" + "    <username>" + userName + "</username>" + "    <password>" + password + "</password>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                + "    <givenname>" + (givenName == null ? "" : givenName) + "</givenname>" + "    <familyname>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + (familyName == null ? "" : familyName) + "</familyname>" + "    <phonenumber>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + (phoneNumber == null ? "" : phoneNumber) + "</phonenumber>" + "    <company>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + (company == null ? "" : company) + "</company>" + "    <id>" + ID + "</id>" + "    <signature>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                + (signature == null ? "" : signature) + "</signature>" + "    <realemail>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + (realEmail == null ? "" : realEmail) + "</realemail>" + "    <fakeemail>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + (fakeEmail == null ? "" : fakeEmail) + "</fakeemail>" + "    <viewrealemail>" + (viewRealEmail ? "yes" : "no") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                + "</viewrealemail>" + "    <registrationdate>" //$NON-NLS-1$ //$NON-NLS-2$
                + (registrationDateAsLong == 0 ? System.currentTimeMillis() : registrationDateAsLong) + "</registrationdate>" //$NON-NLS-1$
                + "    <lastvisitdate>" + lastVisitDateAsLong + "</lastvisitdate>"  + "    <lastsynctime>" + lastSyncTimeAsLong + "</lastsynctime>"  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + "    <enabled>" + (enabled ? "yes" : "no") + "</enabled>" + "    <homepage>" + homePage + "</homepage>" + "    <universe>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                + (universe == null ? "" : universe) + "</universe>" + "    <language>" + (language == null ? "" : language) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + "</language>"; //$NON-NLS-1$
        user += "    <roles>"; //$NON-NLS-1$
        Iterator<String> iter = roleNames.iterator();
        while (iter.hasNext()) {
            user += "<role>" + iter.next() + "</role>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        user += "    </roles>"; //$NON-NLS-1$
        user += "    <properties>"; //$NON-NLS-1$
        if (properties != null) {
            for (iter = properties.keySet().iterator(); iter.hasNext();) {
                String key = iter.next();
                String value = properties.get(key);
                user += "        <property>" + "            <name>" + key + "</name>" + "            <value>" + value //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        + "</value>" + "        </property>"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        user += "    </properties>"; //$NON-NLS-1$

        user += "    <applications>"; //$NON-NLS-1$
        if (applications != null) {
            for (iter = applications.keySet().iterator(); iter.hasNext();) {
                String key = iter.next();
                String value = applications.get(key);
                user += "        <application>" + "            <name>" + key + "</name>" + "            <value>" + value //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        + "</value>" + "        </application>"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        user += "    </applications>"; //$NON-NLS-1$
        user += "</User>"; //$NON-NLS-1$

        return user;

    }

    public static User parse(String xml) throws Exception {
        User user = new User();
        parse(xml, user);
        return user;
    }

    public static void parse(String xml, User user) throws Exception {

        try {
            Element result = Util.parse(xml).getDocumentElement();
            user.setUserName(Util.getFirstTextNode(result, "//username")); //$NON-NLS-1$
            user.setPassword(Util.getFirstTextNode(result, "//password")); //$NON-NLS-1$
            user.setGivenName(Util.getFirstTextNode(result, "//givenname")); //$NON-NLS-1$
            user.setFamilyName(Util.getFirstTextNode(result, "//familyname")); //$NON-NLS-1$
            user.setPhoneNumber(Util.getFirstTextNode(result, "//phonenumber")); //$NON-NLS-1$
            user.setCompany(Util.getFirstTextNode(result, "//company")); //$NON-NLS-1$
            user.setSignature(Util.getFirstTextNode(result, "//signature")); //$NON-NLS-1$
            user.setRealEmail(Util.getFirstTextNode(result, "//realemail")); //$NON-NLS-1$
            user.setFakeEmail(Util.getFirstTextNode(result, "//fakeemail")); //$NON-NLS-1$
            user.setViewRealEmail("yes".equals(Util.getFirstTextNode(result, "//viewrealemail"))); //$NON-NLS-1$ //$NON-NLS-2$

            try {
                user.setRegistrationDate(new Date(Long.parseLong(Util.getFirstTextNode(result, "//registrationdate")))); //$NON-NLS-1$
            } catch (Exception nfe) {
                user.setRegistrationDate(null);
            }

            try {
                user.setLastVisitDate(new Date(Long.parseLong(Util.getFirstTextNode(result, "//lastvisitdate")))); //$NON-NLS-1$
            } catch (Exception nfe) {
                user.setLastVisitDate(null);
            }
            
            try {
                user.setLastSyncTime(new Date(Long.parseLong(Util.getFirstTextNode(result, "//lastsynctime")))); //$NON-NLS-1$
            } catch (Exception nfe) {
                user.setLastSyncTime(null);
            }
            user.setEnabled("yes".equals(Util.getFirstTextNode(result, "//enabled"))); //$NON-NLS-1$ //$NON-NLS-2$
            user.setHomePage(Util.getFirstTextNode(result, "//homepage")); //$NON-NLS-1$
            user.setUniverse(Util.getFirstTextNode(result, "//universe")); //$NON-NLS-1$
            user.setLanguage(Util.getFirstTextNode(result, "//language")); //$NON-NLS-1$

            String[] roles = Util.getTextNodes(result, "//roles/role"); //$NON-NLS-1$
            HashSet<String> rs = new HashSet<String>();
            if (roles != null) {
                Collections.addAll(rs, roles);
            }
            user.setRoleNames(rs);

            // XtentisPropertyMap propertyMap = new XtentisPropertyMap(user);
            HashMap<String, String> props = new HashMap<String, String>();
            NodeList properties = Util.getNodeList(result, "//properties/property"); //$NON-NLS-1$
            if (properties != null) {
                for (int i = 0; i < properties.getLength(); i++) {
                    Element property = (Element) properties.item(i);
                    String name = Util.getFirstTextNode(property, "name"); //$NON-NLS-1$
                    String value = Util.getFirstTextNode(property, "value"); //$NON-NLS-1$
                    props.put(name, value);
                }
            }
            user.setProperties(props);

            HashMap<String, String> apps = new HashMap<String, String>();
            NodeList appNodes = Util.getNodeList(result, "//applications/application"); //$NON-NLS-1$
            if (appNodes != null) {
                for (int i = 0; i < appNodes.getLength(); i++) {
                    Element application = (Element) appNodes.item(i);
                    String name = Util.getFirstTextNode(application, "name"); //$NON-NLS-1$
                    String value = Util.getFirstTextNode(application, "value"); //$NON-NLS-1$
                    apps.put(name, value);
                }
            }
            user.setApplications(apps);
            if(LOG.isDebugEnabled()){
                LOG.debug("parsing xml complete."); //$NON-NLS-1$
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new Exception("Failed to parse user: " + ": " + e.getLocalizedMessage());  //$NON-NLS-1$//$NON-NLS-2$
        }

    }

    public Map<String, String> getApplications() {
        return applications;
    }

    public void setApplications(Map<String, String> applications) {
        this.applications = applications;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // for groovy
    public String getFakeemail() {
        return getFakeEmail();
    }
    
    public String getFakeEmail() {
        return fakeEmail;
    }
    
    public void setFakeEmail(String fakeEmail) {
        this.fakeEmail = fakeEmail;
    }

    // for groovy
    public String getFamilyname() {
        return getFamilyName();
    }
    
    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    // for groovy
    public String getGivenname() {
        return getGivenName();
    }

    public String getGivenName() {
        return givenName;
    }
    
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    // for groovy
    public String getHomepage() {
        return getHomePage();
    }
    
    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer id) {
        ID = id;
    }

    // for groovy
    public long getLastvisitdate() {
        return getLastVisitDateAsLong();
    }
    
    public long getLastVisitDateAsLong() {
        return lastVisitDateAsLong;
    }

    public void setLastVisitDate(Date lastVisitDate) {
        this.lastVisitDateAsLong = (lastVisitDate == null ? 0 : lastVisitDate.getTime());
    }
    
    public long getLastSyncTime() {
        return lastSyncTimeAsLong;
    }

    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTimeAsLong = (lastSyncTime == null ? 0 : lastSyncTime.getTime());
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // for groovy
    public String getPhonenumber() {
        return getPhoneNumber();
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    // for groovy
    public String getRealemail() {
        return getRealEmail();
    }
    
    public String getRealEmail() {
        return realEmail;
    }

    public void setRealEmail(String realEmail) {
        this.realEmail = realEmail;
    }

    // for groovy
    public long getRegistrationdate() {
        return getRegistrationDate();
    }
    
    public long getRegistrationDate() {
        return registrationDateAsLong;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDateAsLong = (registrationDate == null ? 0 : registrationDate.getTime());
    }

    // for groovy
    public Set<String> getRolenames() {
        return getRoleNames();
    }
    
    public Set<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(Set<String> roleNames) {
        this.roleNames = roleNames;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    // for groovy
    public String getUsername() {
        return getUserName();
    }
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    // for groovy
    public boolean isViewrealemail() {
        return isViewRealEmail();
    }

    public boolean isViewRealEmail() {
        return viewRealEmail;
    }

    public void setViewRealEmail(boolean viewRealEmail) {
        this.viewRealEmail = viewRealEmail;
    }

    public String getUniverse() {
        return universe;
    }

    public void setUniverse(String universe) {
        this.universe = universe;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

}
