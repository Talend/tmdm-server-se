/*
 * Generated by XDoclet - Do not edit!
 */
package com.amalto.core.objects.menu.ejb.local;

/**
 * Local home interface for MenuCtrl.
 * @xdoclet-generated
 * @copyright The XDoclet Team
 * @author XDoclet
 * @version ${version}
 */
public interface MenuCtrlLocalHome
   extends javax.ejb.EJBLocalHome
{
   public static final String COMP_NAME="java:comp/env/ejb/MenuCtrlLocal";
   public static final String JNDI_NAME="amalto/local/core/menuctrl";

   public com.amalto.core.objects.menu.ejb.local.MenuCtrlLocal create()
      throws javax.ejb.CreateException;

}
