package com.amalto.webapp.core.util;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

import com.amalto.webapp.util.webservices.*;

import junit.framework.TestCase;


public class IXtentisRMIPortTest extends TestCase {
    
    public void testPutItemWithReportArray () {
        MockIXtentisRMIPort port = new MockIXtentisRMIPort();    
        WSPutItemWithReport[] reportArray = new WSPutItemWithReport[] {new WSPutItemWithReport(), new WSPutItemWithReport(), new WSPutItemWithReport()};
        WSPutItemWithReportArray reportArrayObj = new WSPutItemWithReportArray(reportArray);
        
        try {
            WSItemPKArray result = port.putItemWithReportArray(reportArrayObj);
            assertTrue(port.putItemWithReportCallCount == reportArray.length);
            assertTrue(result.getWsItemPK().length == reportArray.length);
            for (int i = 0; i < reportArray.length; ++i) {
                assertTrue(port.putItemWithReportHistory.get(i) == reportArray[i]);
            }
        }
        catch (RemoteException e) {
            fail();
        }
    }

       
    private class MockIXtentisRMIPort extends IXtentisRMIPort
    {
        public int putItemWithReportCallCount = 0;
        public List<WSPutItemWithReport> putItemWithReportHistory = new Vector<WSPutItemWithReport>();
        
        @Override
        public WSItemPK putItemWithReport(WSPutItemWithReport wsPutItemWithReport) throws RemoteException {
            
            ++putItemWithReportCallCount;
            putItemWithReportHistory.add(wsPutItemWithReport);
            
            return null;
        }

        public WSConceptRevisionMap getConceptsInDataClusterWithRevisions(
                WSGetConceptsInDataClusterWithRevisions wsGetConceptsInDataClusterWithRevisions) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSItemPK updateItemMetadata(WSUpdateMetadataItem wsUpdateMetadataItem) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSItemPK partialPutItem(WSPartialPutItem wsPartialPutItem) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSItemPKArray updateItemArrayMetadata(WSUpdateItemArrayMetadata wsUpdateItemArrayMetadata) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSItemPK putItemWithCustomReport(WSPutItemWithCustomReport wsPutItemWithCustomReport) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSRole getRole(WSGetRole wsGetRole) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean existsRole(WSExistsRole wsExistsRole) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSRolePKArray getRolePKs(WSGetRolePKs regex) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSRolePK putRole(WSPutRole wsRole) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSRolePK deleteRole(WSDeleteRole wsRoleDelete) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSStringArray getObjectsForRoles(WSGetObjectsForRoles wsRoleDelete) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBackgroundJobPK versioningCommitItems(WSVersioningCommitItems wsVersioningCommitItems) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean versioningRestoreItemByRevision(WSVersioningRestoreItemByRevision wsVersioningRestoreItemByRevision)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSVersioningItemHistory versioningGetItemHistory(WSVersioningGetItemHistory wsVersioningGetItemHistory)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSVersioningItemsVersions versioningGetItemsVersions(WSVersioningGetItemsVersions wsVersioningGetItemsVersions)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSString versioningGetItemContent(WSVersioningGetItemContent wsVersioningGetItemContent) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSVersioningObjectsVersions versioningGetObjectsVersions(
                WSVersioningGetObjectsVersions wsVersioningGetObjectsVersions) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSVersioningUniverseVersions versioningGetUniverseVersions(
                WSVersioningGetUniverseVersions wsVersioningGetUniverseVersions) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSVersioningSystemConfiguration getVersioningSystemConfiguration(
                WSGetVersioningSystemConfiguration wsGetVersioningSystemConfiguration) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSString putVersioningSystemConfiguration(WSPutVersioningSystemConfiguration wsPutVersioningSystemConfiguration)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSVersioningInfo versioningGetInfo(WSVersioningGetInfo wsVersioningGetInfo) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBackgroundJobPK versioningTagObjects(WSVersioningTagObjects wsVersioningTagObjects) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBackgroundJobPK versioningTagUniverse(WSVersioningTagUniverse wsVersioningTagUniverse) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBackgroundJobPK versioningTagItems(WSVersioningTagItems wsVersioningTagItems) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBackgroundJobPK versioningRestoreObjects(WSVersioningRestoreObjects wsVersioningRestoreObjects)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBackgroundJobPK versioningRestoreUniverse(WSVersioningRestoreUniverse wsVersioningRestoreUniverse)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBackgroundJobPK versioningRestoreItems(WSVersioningRestoreItems wsVersioningRestoreItems) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSUniverse getUniverse(WSGetUniverse wsGetUniverse) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean existsUniverse(WSExistsUniverse wsExistsUniverse) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSUniversePKArray getUniversePKs(WSGetUniversePKs regex) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSUniversePKArray getUniverseByRevision(WSGetUniverseByRevision wsUniverseByRevision) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSUniversePK putUniverse(WSPutUniverse wsUniverse) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSUniversePK deleteUniverse(WSDeleteUniverse wsUniverseDelete) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSStringArray getObjectsForUniverses(WSGetObjectsForUniverses regex) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSUniverse getCurrentUniverse(WSGetCurrentUniverse wsGetCurrentUniverse) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSSynchronizationPlan getSynchronizationPlan(WSGetSynchronizationPlan wsGetSynchronizationPlan)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean existsSynchronizationPlan(WSExistsSynchronizationPlan wsExistsSynchronizationPlan)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSSynchronizationPlanPKArray getSynchronizationPlanPKs(WSGetSynchronizationPlanPKs regex) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSSynchronizationPlanPK putSynchronizationPlan(WSPutSynchronizationPlan wsSynchronizationPlan)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSSynchronizationPlanPK deleteSynchronizationPlan(WSDeleteSynchronizationPlan wsSynchronizationPlanDelete)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSStringArray getObjectsForSynchronizationPlans(WSGetObjectsForSynchronizationPlans regex) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSStringArray getSynchronizationPlanObjectsAlgorithms(WSGetSynchronizationPlanObjectsAlgorithms regex)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSStringArray getSynchronizationPlanItemsAlgorithms(WSGetSynchronizationPlanItemsAlgorithms regex)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSSynchronizationPlanStatus synchronizationPlanAction(WSSynchronizationPlanAction wsSynchronizationPlanAction)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSStringArray synchronizationGetUnsynchronizedObjectsIDs(
                WSSynchronizationGetUnsynchronizedObjectsIDs wsSynchronizationGetUnsynchronizedObjectsIDs) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSString synchronizationGetObjectXML(WSSynchronizationGetObjectXML wsSynchronizationGetObjectXML)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSString synchronizationPutObjectXML(WSSynchronizationPutObjectXML wsSynchronizationPutObjectXML)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSItemPKArray synchronizationGetUnsynchronizedItemPKs(
                WSSynchronizationGetUnsynchronizedItemPKs wsSynchronizationGetUnsynchronizedItemPKs) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSString synchronizationGetItemXML(WSSynchronizationGetItemXML wsSynchronizationGetItemXML) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSItemPK synchronizationPutItemXML(WSSynchronizationPutItemXML wsSynchronizationPutItemXML) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSSynchronizationItem getSynchronizationItem(WSGetSynchronizationItem wsGetSynchronizationItem)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean existsSynchronizationItem(WSExistsSynchronizationItem wsExistsSynchronizationItem)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSSynchronizationItemPKArray getSynchronizationItemPKs(WSGetSynchronizationItemPKs regex) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSSynchronizationItemPK putSynchronizationItem(WSPutSynchronizationItem wsSynchronizationItem)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSSynchronizationItemPK deleteSynchronizationItem(WSDeleteSynchronizationItem wsSynchronizationItemDelete)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSSynchronizationItem resolveSynchronizationItem(WSResolveSynchronizationItem wsResolveSynchronizationItem)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSWorkflowProcessDefinitionUUIDArray workflowGetProcessDefinitions(
                WSWorkflowGetProcessDefinitions wsworkflowProcessDefinitions) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSWorkflowProcessDefinitionUUID workflowDeploy(WSWorkflowDeploy wsWorkflowDeploy) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean workflowUnDeploy(WSWorkflowUnDeploy wsWorkflowUnDeploy) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSProcessTaskInstanceArray workflowGetTaskList(WSWorkflowGetTaskList uuid) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSProcessInstanceArray workflowGetProcessInstances(WSWorkflowGetProcessInstances uuid) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean workflowDeleteProcessInstances(WSWorkflowDeleteProcessInstancesRequest deleteWolkflowRequest)
                throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean workflowUnassignTask(WSUnassignTask task) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean workflowAssignTask(WSAssignTask task) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean workflowSetTaskPriority(WSSetTaskPriority task) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean workflowSuspendTask(WSSuspendTask task) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean workflowStartProcessInstance(WSStartProcessInstance task) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSMDMJobArray getMDMJob(WSMDMNULL mdmJobRequest) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean putMDMJob(WSPUTMDMJob putMDMJobRequest) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSBoolean deleteMDMJob(WSDELMDMJob deleteMDMJobRequest) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public WSAutoIncrement getAutoIncrement(WSAutoIncrement wsAutoIncrementRequest) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }

        public List<String> globalSearch(String dataCluster, String keyword, int start, int end) throws RemoteException {
            
            throw new RemoteException("Not supported exception! ");
        }
        
        
        
    }
}
