/*
 * Generated by XDoclet - Do not edit!
 */
package talend.core.transformer.plugin.v2.tiscall.ejb.local;

/**
 * Local home interface for TISCallTransformerPlugin.
 * @xdoclet-generated at 16-04-09
 * @copyright The XDoclet Team
 * @author XDoclet
 * @version ${version}
 */
public interface TISCallTransformerPluginLocalHome
   extends javax.ejb.EJBLocalHome
{
   public static final String COMP_NAME="java:comp/env/ejb/TISCallTransformerPluginLocal";
   public static final String JNDI_NAME="amalto/local/transformer/plugin/tisCall";

   public talend.core.transformer.plugin.v2.tiscall.ejb.local.TISCallTransformerPluginLocal create()
      throws javax.ejb.CreateException;

}
