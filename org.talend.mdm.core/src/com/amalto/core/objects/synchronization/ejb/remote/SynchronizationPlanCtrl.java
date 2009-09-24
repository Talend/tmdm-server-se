/*
 * Generated by XDoclet - Do not edit!
 */
package com.amalto.core.objects.synchronization.ejb.remote;

/**
 * Remote interface for SynchronizationPlanCtrl.
 * @xdoclet-generated at 24-09-09
 * @copyright The XDoclet Team
 * @author XDoclet
 * @version ${version}
 */
public interface SynchronizationPlanCtrl
   extends javax.ejb.EJBObject
{
   /**
    * Creates or updates a SynchronizationPlan
    * @throws XtentisException
    */
   public com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJOPK putSynchronizationPlan( com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJO synchronizationPlan )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Creates or updates a SynchronizationPlan for a particular revision ID<br/> The user must have the role 'administration' to perform this task
    * @throws XtentisException
    */
   public com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJOPK putSynchronizationPlan( java.lang.String revisionID,com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJO synchronizationPlan )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Get SynchronizationPlan
    * @throws XtentisException
    */
   public com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJO getSynchronizationPlan( com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJOPK pk )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Get Synchronization Plan of a particular revision ID<br/> The user must have the 'administration' role to preform this task
    * @throws XtentisException
    */
   public com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJO getSynchronizationPlan( java.lang.String revisionID,com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJOPK pk )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Get a SynchronizationPlan - no exception is thrown: returns null if not found
    * @throws XtentisException
    */
   public com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJO existsSynchronizationPlan( com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJOPK pk )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Remove an item
    * @throws XtentisException
    */
   public com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJOPK removeSynchronizationPlan( com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJOPK pk )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Retrieve all SynchronizationPlan PKS
    * @throws XtentisException
    */
   public java.util.Collection getSynchronizationPlanPKs( java.lang.String regex )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Retrieve all the Unique ID Strings of Xtentis Objects of a particular name that match a particular instance pattern The user running this method must have an 'administration' role
    * @param revisionID
    * @param objectName
    * @param instancePattern
    * @param synchronizationPlanName The synchronization plan objects are NOT synchronized with <code>null</code> retrieves all objects
    * @return The uniqueID of the object
    */
   public java.util.ArrayList synchronizationGetAllUnsynchronizedObjectsIDs( java.lang.String revisionID,java.lang.String objectName,java.lang.String instancePattern,java.lang.String synchronizationPlanName )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Return the marshaled Xtentis Object (e.g. its XML) The user running this method must have an 'administration' role
    * @param revisionID
    * @param objectName
    * @param uniqueID The unique ID obtain by calling {@link #synchronizationGetAllUnsynchronizedObjectsIDs(String, String, String, String)}
    * @return The marshaled version of the object
    * @throws XtentisException
    */
   public java.lang.String synchronizationGetMarshaledObject( java.lang.String revisionID,java.lang.String objectName,java.lang.String uniqueID )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Replaces or creates the xtentis object XML. This methods is called by Synchronization Plans The user running this method must have an 'administration' role
    * @param revisionID
    * @param objectName
    * @param uniqueID The unique ID obtain by calling {@link #synchronizationGetAllUnsynchronizedObjectsIDs(String, String, String, String)}
    * @param xml
    * @throws XtentisException
    */
   public void synchronizationPutMarshaledObject( java.lang.String revisionID,java.lang.String objectName,java.lang.String uniqueID,java.lang.String xml )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Retrieve all the {@link ItemPOJOPK}s of Items in a revision that match a particular concept pattern and a particular instance pattern<br/> <br/> The user running this method must have an 'administration' role
    * @param revisionID
    * @param conceptPattern
    * @param instancePattern
    * @param synchronizationPlanPOJOPK The synchronization plan objects are NOT synchronized with <code>null</code> retrieves all objects
    * @param start
    * @param limit
    * @return The uniqueID of the object
    */
   public java.util.ArrayList synchronizationGetAllUnsynchronizedItemPOJOPKs( java.lang.String revisionID,com.amalto.core.objects.datacluster.ejb.DataClusterPOJOPK dataClusterPOJOPK,java.lang.String conceptPattern,java.lang.String instancePattern,com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJOPK synchronizationPlanPOJOPK,long start,int limit )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Return the marshaled Xtentis Item (e.g. its XML) The user running this method must have an 'administration' role
    * @param revisionID
    * @param itemPOJOPK
    * @return The marshaled version of the object
    * @throws XtentisException
    */
   public java.lang.String synchronizationGetMarshaledItem( java.lang.String revisionID,com.amalto.core.ejb.ItemPOJOPK itemPOJOPK )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Replaces or creates the xtentis object XML. This methods is called by Synchronization Plans The user running this method must have an 'administration' role
    * @param revisionID
    * @param xml the full Item XML
    * @throws XtentisException
    */
   public com.amalto.core.ejb.ItemPOJOPK synchronizationPutMarshaledItem( java.lang.String revisionID,java.lang.String xml )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

   /**
    * Execute the Synchronization Plan
    * @throws XtentisException
    * @return the status
    */
   public java.lang.String[] action( int action,com.amalto.core.objects.synchronization.ejb.SynchronizationPlanPOJOPK pk )
      throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

}
