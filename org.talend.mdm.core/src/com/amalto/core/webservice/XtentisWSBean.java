/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.webservice;

import java.rmi.RemoteException;

import javax.jws.WebService;

import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.interceptor.OutInterceptors;
import org.talend.mdm.commmon.util.core.ICoreConstants;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.integrity.FKIntegrityCheckResult;
import com.amalto.core.server.security.ws.WebServiceRoles;
import com.amalto.core.storage.transaction.InTransactionInterceptor;
import com.amalto.core.storage.transaction.OutTransactionInterceptor;

@WebService(name = "TMDMService", serviceName = "TMDMService", portName = "TMDMPort", targetNamespace = ICoreConstants.TALEND_NAMESPACE)
@InInterceptors(classes = InTransactionInterceptor.class)
@OutInterceptors(classes = OutTransactionInterceptor.class)
public class XtentisWSBean implements XtentisPort {

    protected XtentisPort delegator = BeanDelegatorContainer.getInstance().getXtentisWSDelegator();

    public XtentisWSBean() {
    }

    @Override
    public WSVersion getComponentVersion(WSGetComponentVersion wsGetComponentVersion) throws RemoteException {
        return delegator.getComponentVersion(wsGetComponentVersion);
    }

    @Override
    public WSString ping(WSPing wsPing) throws RemoteException {
        return delegator.ping(wsPing);
    }

    @Override
    public WSString logout(WSLogout logout) throws RemoteException {
        return delegator.logout(logout);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSInt initMDM(WSInitData initData) throws RemoteException {
        return delegator.initMDM(initData);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSMDMConfig getMDMConfiguration() throws RemoteException {
        return delegator.getMDMConfiguration();
    }

    @Override
    public WSDataModel getDataModel(WSGetDataModel wsDataModelget) throws RemoteException {
        return delegator.getDataModel(wsDataModelget);
    }

    @Override
    public WSBoolean existsDataModel(WSExistsDataModel wsExistsDataModel) throws RemoteException {
        return delegator.existsDataModel(wsExistsDataModel);
    }

    @Override
    public WSDataModelPKArray getDataModelPKs(WSRegexDataModelPKs regexp) throws RemoteException {
        return delegator.getDataModelPKs(regexp);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSDataModelPK deleteDataModel(WSDeleteDataModel wsDeleteDataModel) throws RemoteException {
        return delegator.deleteDataModel(wsDeleteDataModel);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSDataModelPK putDataModel(WSPutDataModel wsDataModel) throws RemoteException {
        return delegator.putDataModel(wsDataModel);
    }

    @Override
    public WSString checkSchema(WSCheckSchema wsSchema) throws RemoteException {
        return delegator.checkSchema(wsSchema);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSString putBusinessConcept(WSPutBusinessConcept wsPutBusinessConcept) throws RemoteException {
        return delegator.putBusinessConcept(wsPutBusinessConcept);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSString putBusinessConceptSchema(WSPutBusinessConceptSchema wsPutBusinessConceptSchema) throws RemoteException {
        return delegator.putBusinessConceptSchema(wsPutBusinessConceptSchema);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSString deleteBusinessConcept(WSDeleteBusinessConcept wsDeleteBusinessConcept) throws RemoteException {
        return delegator.deleteBusinessConcept(wsDeleteBusinessConcept);
    }

    @Override
    public WSStringArray getBusinessConcepts(WSGetBusinessConcepts wsGetBusinessConcepts) throws RemoteException {
        return delegator.getBusinessConcepts(wsGetBusinessConcepts);
    }

    @Override
    public WSConceptKey getBusinessConceptKey(WSGetBusinessConceptKey wsGetBusinessConceptKey) throws RemoteException {
        return delegator.getBusinessConceptKey(wsGetBusinessConceptKey);
    }

    @Override
    public WSDataCluster getDataCluster(WSGetDataCluster wsDataClusterGet) throws RemoteException {
        return delegator.getDataCluster(wsDataClusterGet);
    }

    @Override
    public WSBoolean existsDataCluster(WSExistsDataCluster wsExistsDataCluster) throws RemoteException {
        return delegator.existsDataCluster(wsExistsDataCluster);
    }

    @Override
    public WSBoolean existsDBDataCluster(WSExistsDBDataCluster wsExistsDataCluster) throws RemoteException {
        return delegator.existsDBDataCluster(wsExistsDataCluster);
    }

    @Override
    public WSDataClusterPKArray getDataClusterPKs(WSRegexDataClusterPKs regexp) throws RemoteException {
        return delegator.getDataClusterPKs(regexp);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSDataClusterPK deleteDataCluster(WSDeleteDataCluster wsDeleteDataCluster) throws RemoteException {
        return delegator.deleteDataCluster(wsDeleteDataCluster);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSDataClusterPK putDataCluster(WSPutDataCluster wsDataCluster) throws RemoteException {
        return delegator.putDataCluster(wsDataCluster);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSBoolean putDBDataCluster(WSPutDBDataCluster wsDataCluster) throws RemoteException {
        return delegator.putDBDataCluster(wsDataCluster);
    }

    @Override
    public WSStringArray getConceptsInDataCluster(WSGetConceptsInDataCluster wsGetConceptsInDataCluster) throws RemoteException {
        return delegator.getConceptsInDataCluster(wsGetConceptsInDataCluster);
    }

    @Override
    public WSView getView(WSGetView wsViewGet) throws RemoteException {
        return delegator.getView(wsViewGet);
    }

    @Override
    public WSBoolean existsView(WSExistsView wsExistsView) throws RemoteException {
        return delegator.existsView(wsExistsView);
    }

    @Override
    public WSViewPKArray getViewPKs(WSGetViewPKs regexp) throws RemoteException {
        return delegator.getViewPKs(regexp);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSViewPK deleteView(WSDeleteView wsDeleteView) throws RemoteException {
        return delegator.deleteView(wsDeleteView);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSViewPK putView(WSPutView wsView) throws RemoteException {
        return delegator.putView(wsView);
    }

    @Override
    public WSStringArray viewSearch(WSViewSearch wsViewSearch) throws RemoteException {
        return delegator.viewSearch(wsViewSearch);
    }

    @Override
    public WSStringArray xPathsSearch(WSXPathsSearch wsXPathsSearch) throws RemoteException {
        return delegator.xPathsSearch(wsXPathsSearch);
    }

    @Override
    public WSString count(WSCount wsCount) throws RemoteException {
        return delegator.count(wsCount);
    }

    @Override
    public WSStringArray getItems(WSGetItems wsGetItems) throws RemoteException {
        return delegator.getItems(wsGetItems);
    }

    @Override
    public WSStringArray getItemsSort(WSGetItemsSort wsGetItemsSort) throws RemoteException {
        return delegator.getItemsSort(wsGetItemsSort);
    }

    @Override
    public WSItemPKsByCriteriaResponse getItemPKsByCriteria(WSGetItemPKsByCriteria wsGetItemPKsByCriteria) throws RemoteException {
        return delegator.getItemPKsByCriteria(wsGetItemPKsByCriteria);
    }

    @Override
    public WSItemPKsByCriteriaResponse getItemPKsByFullCriteria(WSGetItemPKsByFullCriteria wsGetItemPKsByFullCriteria)
            throws RemoteException {
        return delegator.getItemPKsByFullCriteria(wsGetItemPKsByFullCriteria);
    }

    @Override
    public WSItem getItem(WSGetItem wsGetItem) throws RemoteException {
        return delegator.getItem(wsGetItem);
    }

    @Override
    public WSBoolean existsItem(WSExistsItem wsExistsItem) throws RemoteException {
        return delegator.existsItem(wsExistsItem);
    }

    @Override
    public WSStringArray quickSearch(WSQuickSearch wsQuickSearch) throws RemoteException {
        return delegator.quickSearch(wsQuickSearch);
    }

    @Override
    public WSString getBusinessConceptValue(WSGetBusinessConceptValue wsGetBusinessConceptValue) throws RemoteException {
        return delegator.getBusinessConceptValue(wsGetBusinessConceptValue);
    }

    @Override
    public WSStringArray getFullPathValues(WSGetFullPathValues wsGetFullPathValues) throws RemoteException {
        return delegator.getFullPathValues(wsGetFullPathValues);
    }

    @Override
    public WSItemPK putItem(WSPutItem wsPutItem) throws RemoteException {
        return delegator.putItem(wsPutItem);
    }

    @Override
    public WSItemPK updateItemMetadata(WSUpdateMetadataItem wsUpdateMetadataItem) throws RemoteException {
        return delegator.updateItemMetadata(wsUpdateMetadataItem);
    }

    @Override
    public WSItemPKArray putItemArray(WSPutItemArray wsPutItemArray) throws RemoteException {
        return delegator.putItemArray(wsPutItemArray);
    }

    @Override
    public WSItemPKArray putItemWithReportArray(com.amalto.core.webservice.WSPutItemWithReportArray wsPutItemWithReportArray)
            throws RemoteException {
        return delegator.putItemWithReportArray(wsPutItemWithReportArray);
    }

    @Override
    public WSItemPK putItemWithReport(com.amalto.core.webservice.WSPutItemWithReport wsPutItemWithReport) throws RemoteException {
        return delegator.putItemWithReport(wsPutItemWithReport);
    }

    @Override
    public WSItemPK putItemWithCustomReport(com.amalto.core.webservice.WSPutItemWithCustomReport wsPutItemWithCustomReport)
            throws RemoteException {
        return delegator.putItemWithCustomReport(wsPutItemWithCustomReport);
    }

    @Override
    public WSPipeline extractUsingTransformer(WSExtractUsingTransformer wsExtractUsingTransformer) throws RemoteException {
        return delegator.extractUsingTransformer(wsExtractUsingTransformer);
    }

    @Override
    public WSPipeline extractUsingTransformerThruView(WSExtractUsingTransformerThruView wsExtractUsingTransformerThruView)
            throws RemoteException {
        return delegator.extractUsingTransformerThruView(wsExtractUsingTransformerThruView);
    }

    @Override
    public WSItemPK deleteItem(WSDeleteItem wsDeleteItem) throws RemoteException {
        return delegator.deleteItem(wsDeleteItem);
    }

    @Override
    public WSString deleteItemWithReport(WSDeleteItemWithReport wsDeleteItem) throws RemoteException {
        return delegator.deleteItemWithReport(wsDeleteItem);
    }

    @Override
    public WSInt deleteItems(WSDeleteItems wsDeleteItems) throws RemoteException {
        return delegator.deleteItems(wsDeleteItems);
    }

    @Override
    public WSDroppedItemPK dropItem(WSDropItem wsDropItem) throws RemoteException {
        return delegator.dropItem(wsDropItem);
    }

    @Override
    public WSStringArray runQuery(WSRunQuery wsRunQuery) throws RemoteException {
        return delegator.runQuery(wsRunQuery);
    }

    @Override
    public WSServiceGetDocument getServiceDocument(WSString serviceName) throws RemoteException {
        return delegator.getServiceDocument(serviceName);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSString getServiceConfiguration(WSServiceGetConfiguration wsGetConfiguration) throws RemoteException {
        return delegator.getServiceConfiguration(wsGetConfiguration);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSCheckServiceConfigResponse checkServiceConfiguration(WSCheckServiceConfigRequest serviceName) throws RemoteException {
        return delegator.checkServiceConfiguration(serviceName);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSString putServiceConfiguration(WSServicePutConfiguration wsPutConfiguration) throws RemoteException {
        return delegator.putServiceConfiguration(wsPutConfiguration);
    }

    @Override
    public WSString serviceAction(WSServiceAction wsServiceAction) throws RemoteException {
        return delegator.serviceAction(wsServiceAction);
    }

    @Override
    public WSServicesList getServicesList(WSGetServicesList wsGetServicesList) throws RemoteException {
        return delegator.getServicesList(wsGetServicesList);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSStoredProcedurePK deleteStoredProcedure(WSDeleteStoredProcedure wsStoredProcedureDelete) throws RemoteException {
        return delegator.deleteStoredProcedure(wsStoredProcedureDelete);
    }

    @Override
    public WSStringArray executeStoredProcedure(WSExecuteStoredProcedure wsExecuteStoredProcedure) throws RemoteException {
        return delegator.executeStoredProcedure(wsExecuteStoredProcedure);
    }

    @Override
    public WSStoredProcedure getStoredProcedure(WSGetStoredProcedure wsGetStoredProcedure) throws RemoteException {
        return delegator.getStoredProcedure(wsGetStoredProcedure);
    }

    @Override
    public WSBoolean existsStoredProcedure(WSExistsStoredProcedure wsExistsStoredProcedure) throws RemoteException {
        return delegator.existsStoredProcedure(wsExistsStoredProcedure);
    }

    @Override
    public WSStoredProcedurePKArray getStoredProcedurePKs(WSRegexStoredProcedure regex) throws RemoteException {
        return delegator.getStoredProcedurePKs(regex);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSStoredProcedurePK putStoredProcedure(WSPutStoredProcedure wsStoredProcedure) throws RemoteException {
        return delegator.putStoredProcedure(wsStoredProcedure);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSMenuPK deleteMenu(WSDeleteMenu wsMenuDelete) throws RemoteException {
        return delegator.deleteMenu(wsMenuDelete);
    }

    @Override
    public WSMenu getMenu(WSGetMenu wsGetMenu) throws RemoteException {
        return delegator.getMenu(wsGetMenu);
    }

    @Override
    public WSBoolean existsMenu(WSExistsMenu wsExistsMenu) throws RemoteException {
        return delegator.existsMenu(wsExistsMenu);
    }

    @Override
    public WSMenuPKArray getMenuPKs(WSGetMenuPKs regex) throws RemoteException {
        return delegator.getMenuPKs(regex);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSMenuPK putMenu(WSPutMenu wsMenu) throws RemoteException {
        return delegator.putMenu(wsMenu);
    }

    @Override
    public WSBackgroundJob getBackgroundJob(WSGetBackgroundJob wsBackgroundJobGet) throws RemoteException {
        return delegator.getBackgroundJob(wsBackgroundJobGet);
    }

    @Override
    public WSBackgroundJobPKArray findBackgroundJobPKs(WSFindBackgroundJobPKs wsFindBackgroundJobPKs) throws RemoteException {
        return delegator.findBackgroundJobPKs(wsFindBackgroundJobPKs);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSBackgroundJobPK putBackgroundJob(WSPutBackgroundJob wsputjob) throws RemoteException {
        return delegator.putBackgroundJob(wsputjob);
    }

    @Override
    public WSTransformer getTransformer(WSGetTransformer wsGetTransformer) throws RemoteException {
        return delegator.getTransformer(wsGetTransformer);
    }

    @Override
    public WSBoolean existsTransformer(WSExistsTransformer wsExistsTransformer) throws RemoteException {
        return delegator.existsTransformer(wsExistsTransformer);
    }

    @Override
    public WSTransformerPKArray getTransformerPKs(WSGetTransformerPKs regex) throws RemoteException {
        return delegator.getTransformerPKs(regex);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSTransformerPK putTransformer(WSPutTransformer wsTransformer) throws RemoteException {
        return delegator.putTransformer(wsTransformer);
    }

    @Override
    public WSPipeline processBytesUsingTransformer(WSProcessBytesUsingTransformer wsProcessBytesUsingTransformer)
            throws RemoteException {
        return delegator.processBytesUsingTransformer(wsProcessBytesUsingTransformer);
    }

    @Override
    public WSPipeline processFileUsingTransformer(WSProcessFileUsingTransformer wsProcessFileUsingTransformer)
            throws RemoteException {
        return delegator.processFileUsingTransformer(wsProcessFileUsingTransformer);
    }

    @Override
    public WSBackgroundJobPK processBytesUsingTransformerAsBackgroundJob(
            WSProcessBytesUsingTransformerAsBackgroundJob wsProcessBytesUsingTransformerAsBackgroundJob) throws RemoteException {
        return delegator.processBytesUsingTransformerAsBackgroundJob(wsProcessBytesUsingTransformerAsBackgroundJob);
    }

    @Override
    public WSBackgroundJobPK processFileUsingTransformerAsBackgroundJob(
            WSProcessFileUsingTransformerAsBackgroundJob wsProcessFileUsingTransformerAsBackgroundJob) throws RemoteException {
        return delegator.processFileUsingTransformerAsBackgroundJob(wsProcessFileUsingTransformerAsBackgroundJob);
    }

    @Override
    public WSDroppedItemPKArray findAllDroppedItemsPKs(WSFindAllDroppedItemsPKs regex) throws RemoteException {
        return delegator.findAllDroppedItemsPKs(regex);
    }

    @Override
    public WSDroppedItem loadDroppedItem(WSLoadDroppedItem wsLoadDroppedItem) throws RemoteException {
        return delegator.loadDroppedItem(wsLoadDroppedItem);
    }

    @Override
    public WSItemPK recoverDroppedItem(WSRecoverDroppedItem wsRecoverDroppedItem) throws RemoteException {
        return delegator.recoverDroppedItem(wsRecoverDroppedItem);
    }

    @Override
    public WSDroppedItemPK removeDroppedItem(WSRemoveDroppedItem wsRemoveDroppedItem) throws RemoteException {
        return delegator.removeDroppedItem(wsRemoveDroppedItem);
    }

    @Override
    public WSRoutingRule getRoutingRule(WSGetRoutingRule wsRoutingRuleGet) throws RemoteException {
        return delegator.getRoutingRule(wsRoutingRuleGet);
    }

    @Override
    public WSBoolean existsRoutingRule(WSExistsRoutingRule wsExistsRoutingRule) throws RemoteException {
        return delegator.existsRoutingRule(wsExistsRoutingRule);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSRoutingRulePK deleteRoutingRule(WSDeleteRoutingRule wsDeleteRoutingRule) throws RemoteException {
        return delegator.deleteRoutingRule(wsDeleteRoutingRule);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSRoutingRulePK putRoutingRule(WSPutRoutingRule wsRoutingRule) throws RemoteException {
        return delegator.putRoutingRule(wsRoutingRule);
    }

    @Override
    public WSRoutingRulePKArray getRoutingRulePKs(WSGetRoutingRulePKs regex) throws RemoteException {
        return delegator.getRoutingRulePKs(regex);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSTransformerV2PK deleteTransformerV2(WSDeleteTransformerV2 wsTransformerV2Delete) throws RemoteException {
        return delegator.deleteTransformerV2(wsTransformerV2Delete);
    }

    @Override
    public WSTransformerV2 getTransformerV2(WSGetTransformerV2 wsGetTransformerV2) throws RemoteException {
        return delegator.getTransformerV2(wsGetTransformerV2);
    }

    @Override
    public WSBoolean existsTransformerV2(WSExistsTransformerV2 wsExistsTransformerV2) throws RemoteException {
        return delegator.existsTransformerV2(wsExistsTransformerV2);
    }

    @Override
    public WSTransformerV2PKArray getTransformerV2PKs(WSGetTransformerV2PKs regex) throws RemoteException {
        return delegator.getTransformerV2PKs(regex);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSTransformerV2PK putTransformerV2(WSPutTransformerV2 wsTransformerV2) throws RemoteException {
        return delegator.putTransformerV2(wsTransformerV2);
    }

    @Override
    public WSTransformerContext executeTransformerV2(WSExecuteTransformerV2 wsExecuteTransformerV2) throws RemoteException {
        return delegator.executeTransformerV2(wsExecuteTransformerV2);
    }

    @Override
    public WSBackgroundJobPK executeTransformerV2AsJob(WSExecuteTransformerV2AsJob wsExecuteTransformerV2AsJob)
            throws RemoteException {
        return delegator.executeTransformerV2AsJob(wsExecuteTransformerV2AsJob);
    }

    @Override
    public WSTransformerContext extractThroughTransformerV2(WSExtractThroughTransformerV2 wsExtractThroughTransformerV2)
            throws RemoteException {
        return delegator.extractThroughTransformerV2(wsExtractThroughTransformerV2);
    }

    @Override
    public WSBoolean existsTransformerPluginV2(WSExistsTransformerPluginV2 wsExistsTransformerPlugin) throws RemoteException {
        return delegator.existsTransformerPluginV2(wsExistsTransformerPlugin);
    }

    @Override
    public WSString getTransformerPluginV2Configuration(WSTransformerPluginV2GetConfiguration wsGetConfiguration)
            throws RemoteException {
        return delegator.getTransformerPluginV2Configuration(wsGetConfiguration);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSString putTransformerPluginV2Configuration(WSTransformerPluginV2PutConfiguration wsPutConfiguration)
            throws RemoteException {
        return delegator.putTransformerPluginV2Configuration(wsPutConfiguration);
    }

    @Override
    public WSTransformerPluginV2Details getTransformerPluginV2Details(
            WSGetTransformerPluginV2Details wsGetTransformerPluginDetails) throws RemoteException {
        return delegator.getTransformerPluginV2Details(wsGetTransformerPluginDetails);
    }

    @Override
    public WSTransformerPluginV2SList getTransformerPluginV2SList(WSGetTransformerPluginV2SList wsGetTransformerPluginsList)
            throws RemoteException {
        return delegator.getTransformerPluginV2SList(wsGetTransformerPluginsList);
    }

    @Override
    public WSRoutingOrderV2PK deleteRoutingOrderV2(WSDeleteRoutingOrderV2 wsDeleteRoutingOrder) throws RemoteException {
        return BeanDelegatorContainer.getInstance().getXtentisWSDelegator().deleteRoutingOrderV2(wsDeleteRoutingOrder);
    }

    @Override
    public WSRoutingOrderV2PK executeRoutingOrderV2Asynchronously(
            WSExecuteRoutingOrderV2Asynchronously wsExecuteRoutingOrderAsynchronously) throws RemoteException {
        return BeanDelegatorContainer.getInstance().getXtentisWSDelegator()
                .executeRoutingOrderV2Asynchronously(wsExecuteRoutingOrderAsynchronously);
    }

    @Override
    public WSString executeRoutingOrderV2Synchronously(WSExecuteRoutingOrderV2Synchronously wsExecuteRoutingOrderSynchronously)
            throws RemoteException {
        return BeanDelegatorContainer.getInstance().getXtentisWSDelegator()
                .executeRoutingOrderV2Synchronously(wsExecuteRoutingOrderSynchronously);
    }

    @Override
    public WSRoutingOrderV2PKArray getRoutingOrderV2PKsByCriteria(
            WSGetRoutingOrderV2PKsByCriteria wsGetRoutingOrderV2PKsByCriteria) throws RemoteException {
        return delegator.getRoutingOrderV2PKsByCriteria(wsGetRoutingOrderV2PKsByCriteria);
    }

    @Override
    public WSRoutingOrderV2Array getRoutingOrderV2SByCriteria(WSGetRoutingOrderV2SByCriteria wsGetRoutingOrderV2SByCriteria)
            throws RemoteException {
        return delegator.getRoutingOrderV2SByCriteria(wsGetRoutingOrderV2SByCriteria);
    }

    @Override
    public WSRoutingEngineV2Status routingEngineV2Action(WSRoutingEngineV2Action wsRoutingEngineAction) throws RemoteException {
        return delegator.routingEngineV2Action(wsRoutingEngineAction);
    }

    @Override
    public WSRoutingRulePKArray routeItemV2(WSRouteItemV2 wsRouteItem) throws RemoteException {
        return BeanDelegatorContainer.getInstance().getXtentisWSDelegator().routeItemV2(wsRouteItem);
    }

    @Override
    public WSCategoryData getMDMCategory(WSCategoryData request) throws RemoteException {
        return delegator.getMDMCategory(request);
    }

    @Override
    public WSMDMJobArray getMDMJob(WSMDMNULL mdmJobRequest) throws RemoteException {
        return delegator.getMDMJob(mdmJobRequest);
    }

    @Override
    public WSAutoIncrement getAutoIncrement(WSAutoIncrement wsAutoIncrementRequest) throws RemoteException {
        return delegator.getAutoIncrement(wsAutoIncrementRequest);
    }

    @Override
    public WSBoolean isItemModifiedByOther(WSIsItemModifiedByOther wsItem) throws RemoteException {
        return delegator.isItemModifiedByOther(wsItem);
    }

    @Override
    public WSString countItemsByCustomFKFilters(WSCountItemsByCustomFKFilters wsCountItemsByCustomFKFilters)
            throws RemoteException {
        return delegator.countItemsByCustomFKFilters(wsCountItemsByCustomFKFilters);
    }

    @Override
    public WSStringArray getItemsByCustomFKFilters(WSGetItemsByCustomFKFilters wsGetItemsByCustomFKFilters)
            throws RemoteException {
        return delegator.getItemsByCustomFKFilters(wsGetItemsByCustomFKFilters);
    }

    @Override
    public WSItemPK partialPutItem(WSPartialPutItem wsPartialPutItem) throws RemoteException {
        return delegator.partialPutItem(wsPartialPutItem);
    }

    @Override
    public WSRoutingOrderV2Array getRoutingOrderV2ByCriteriaWithPaging(
            WSGetRoutingOrderV2ByCriteriaWithPaging wsGetRoutingOrderV2ByCriteriaWithPaging) throws RemoteException {
        return delegator.getRoutingOrderV2ByCriteriaWithPaging(wsGetRoutingOrderV2ByCriteriaWithPaging);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSDigest getDigest(WSDigestKey wsDigestKey) throws RemoteException {
        return delegator.getDigest(wsDigestKey);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSLong updateDigest(WSDigest wsDigest) throws RemoteException {
        return delegator.updateDigest(wsDigest);
    }

    @Override
    public WSBoolean isPagingAccurate(WSInt wsInt) throws RemoteException {
        return delegator.isPagingAccurate(wsInt);
    }

    @Override
    public WSBoolean supportStaging(WSDataClusterPK dataClusterPK) throws RemoteException {
        return delegator.supportStaging(dataClusterPK);
    }

    @Override
    public WSRolePKArray getRolePKs(WSGetRolePKs regex) throws RemoteException {
        return delegator.getRolePKs(regex);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSRolePK putRole(WSPutRole wsRole) throws RemoteException {
        return delegator.putRole(wsRole);
    }

    @Override
    @WebServiceRoles(ICoreConstants.ADMIN_PERMISSION)
    public WSRolePK deleteRole(WSDeleteRole wsRoleDelete) throws RemoteException {
        return delegator.deleteRole(wsRoleDelete);
    }

    @Override
    public FKIntegrityCheckResult checkFKIntegrity(WSDeleteItem deleteItem) throws RemoteException {
        return delegator.checkFKIntegrity(deleteItem);
    }

    @Override
    public WSRole getRole(WSGetRole wsGetRole) throws RemoteException {
        return delegator.getRole(wsGetRole);
    }

    @Override
    public WSBoolean existsRole(WSExistsRole wsExistsRole) throws RemoteException {
        return delegator.existsRole(wsExistsRole);
    }
}
