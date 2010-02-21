package org.talend.mdm.test;

import java.rmi.RemoteException;

import urn_com_amalto_xtentis_webservice.WSBoolean;
import urn_com_amalto_xtentis_webservice.WSBusinessConceptPK;
import urn_com_amalto_xtentis_webservice.WSCount;
import urn_com_amalto_xtentis_webservice.WSDataClusterPK;
import urn_com_amalto_xtentis_webservice.WSDataModelPK;
import urn_com_amalto_xtentis_webservice.WSExistsItem;
import urn_com_amalto_xtentis_webservice.WSGetBusinessConceptValue;
import urn_com_amalto_xtentis_webservice.WSGetFullPathValues;
import urn_com_amalto_xtentis_webservice.WSGetItem;
import urn_com_amalto_xtentis_webservice.WSGetItemPKsByCriteria;
import urn_com_amalto_xtentis_webservice.WSGetItems;
import urn_com_amalto_xtentis_webservice.WSGetItemsPivotIndex;
import urn_com_amalto_xtentis_webservice.WSItem;
import urn_com_amalto_xtentis_webservice.WSItemPK;
import urn_com_amalto_xtentis_webservice.WSItemPKsByCriteriaResponseResults;
import urn_com_amalto_xtentis_webservice.WSPutItem;
import urn_com_amalto_xtentis_webservice.WSPutItemWithReport;
import urn_com_amalto_xtentis_webservice.WSQuickSearch;
import urn_com_amalto_xtentis_webservice.WSString;
import urn_com_amalto_xtentis_webservice.WSViewPK;
import urn_com_amalto_xtentis_webservice.WSViewSearch;
import urn_com_amalto_xtentis_webservice.WSXPathsSearch;

public class SearchWebserviceTestCase extends WebserviceTestCase {

	/***************************************************************************
	 * Search
	 * **************************************************************************/

	public void testViewSearch() {
		WSViewSearch wsViewSearch = new WSViewSearch();
		wsViewSearch.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsViewSearch.setWsViewPK(new WSViewPK("Browse_items_Student"));
		String[] wsStringArray;
		try {
			wsStringArray = defaultPort.viewSearch(wsViewSearch);
			for (int i = 0; i < wsStringArray.length; i++) {
				System.out.println(wsStringArray[i]);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void testXPathsSearch() {
		WSXPathsSearch wsXPathsSearch = new WSXPathsSearch();
		wsXPathsSearch.setMaxItems(1);
		wsXPathsSearch.setOrderBy("id");
		wsXPathsSearch.setPivotPath("Student/id");
		wsXPathsSearch.setWsDataClusterPK(new WSDataClusterPK("Order"));
		String[] wsStringArray;
		try {
			wsStringArray = defaultPort.xPathsSearch(wsXPathsSearch);
			for (int i = 0; i < wsStringArray.length; i++) {
				System.out.println(wsStringArray[i]);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void testGetItemsPivotIndex() {
		try {
			WSGetItemsPivotIndex wsGetItemsPivotIndex = new WSGetItemsPivotIndex();
			wsGetItemsPivotIndex.setClusterName("Order");
			wsGetItemsPivotIndex.setMainPivotName("Student");
			String[] wsStringArray = defaultPort
					.getItemsPivotIndex(wsGetItemsPivotIndex);
			for (int i = 0; i < wsStringArray.length; i++) {
				System.out.println(wsStringArray[i]);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void testCount() {
		WSCount wsCount = new WSCount();
		wsCount.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsCount.setCountPath("Student/id");
		try {
			WSString count = defaultPort.count(wsCount);
			System.out.println(count);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void testGetItems() {
		WSGetItems wsGetItems = new WSGetItems();
		wsGetItems.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsGetItems.setConceptName("Student");
		try {
			String[] items = defaultPort.getItems(wsGetItems);
			for (int i = 0; i < items.length; i++) {
				System.out.println(items[i]);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void testGetItemPKsByCriteria() {
		WSGetItemPKsByCriteria wsGetItemPKsByCriteria = new WSGetItemPKsByCriteria();
		wsGetItemPKsByCriteria.setConceptName("Student");
		wsGetItemPKsByCriteria.setWsDataClusterPK(new WSDataClusterPK("Order"));
		try {
			WSItemPKsByCriteriaResponseResults[] wsItemPKsByCriteriaResponseResults = defaultPort
					.getItemPKsByCriteria(wsGetItemPKsByCriteria);
			for (int i = 0; i < wsItemPKsByCriteriaResponseResults.length; i++) {
				System.out.println(wsItemPKsByCriteriaResponseResults[i]
						.getWsItemPK().getConceptName());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void testGetItem() {
		WSGetItem wsGetItem = new WSGetItem();
		WSItemPK wsItemPK = new WSItemPK();
		wsItemPK.setConceptName("Student");
		wsItemPK.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsGetItem.setWsItemPK(wsItemPK);
		try {
			WSItem wsItem = defaultPort.getItem(wsGetItem);
			System.out.println(wsItem.getDataModelName() + "/"
					+ wsItem.getConceptName());
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void testExistsItem() {
		WSExistsItem wsExistsItem = new WSExistsItem();
		WSItemPK wsItemPK = new WSItemPK();
		wsItemPK.setConceptName("Student");
		wsItemPK.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsExistsItem.setWsItemPK(wsItemPK);
		try {
			WSBoolean flag;
			flag = defaultPort.existsItem(wsExistsItem);
			System.out.println(flag);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void testQuickSearch() {
		WSQuickSearch wsQuickSearch = new WSQuickSearch();
		wsQuickSearch.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsQuickSearch.setWsViewPK(new WSViewPK("Browse_items_Student"));
		try {
			String[] stringArray = defaultPort.quickSearch(wsQuickSearch);
			for (int i = 0; i < stringArray.length; i++) {
				System.out.println(stringArray[i]);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void testGetBusinessConceptValue() {
		WSGetBusinessConceptValue wsGetBusinessConceptValue = new WSGetBusinessConceptValue();
		WSBusinessConceptPK wsBusinessConceptPK = new WSBusinessConceptPK();
		wsBusinessConceptPK.setConceptName("Student");
		wsGetBusinessConceptValue.setWsBusinessConceptPK(wsBusinessConceptPK);
		wsGetBusinessConceptValue.setWsDataClusterPK(new WSDataClusterPK(
				"Order"));
		try {
			WSString value = defaultPort
					.getBusinessConceptValue(wsGetBusinessConceptValue);
			System.out.println(value);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void testGetFullPathValues() {
		WSGetFullPathValues wsGetFullPathValues = new WSGetFullPathValues();
		wsGetFullPathValues.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsGetFullPathValues.setFullPath("Order/Student/id");
		try {
			String[] stringArray = defaultPort
					.getFullPathValues(wsGetFullPathValues);
			for (int i = 0; i < stringArray.length; i++) {
				System.out.println(stringArray[i]);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void testPutItem() {
		WSPutItem wsPutItem = new WSPutItem();
		wsPutItem.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsPutItem.setWsDataModelPK(new WSDataModelPK("Order"));
		wsPutItem.setIsUpdate(false);
		wsPutItem.setXmlString(BUSINESSCONCEPTSCHEMA);
		try {
			WSItemPK wsItemPK = defaultPort.putItem(wsPutItem);
			System.out.println(wsItemPK.getWsDataClusterPK().getPk() + "/"
					+ wsItemPK.getConceptName());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void testPutItemArray() {
		WSPutItem[] wsPutItemArray = new WSPutItem[1];
		WSPutItem wsPutItem = new WSPutItem();
		wsPutItem.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsPutItem.setWsDataModelPK(new WSDataModelPK("Order"));
		wsPutItem.setIsUpdate(false);
		wsPutItemArray[0] = wsPutItem;
		try {
			WSItemPK[] wsItemPKArray = defaultPort.putItemArray(wsPutItemArray);
			for (int i = 0; i < wsItemPKArray.length; i++) {
				System.out.println(wsItemPKArray[i].getConceptName());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void testPutItemWithReportArray() {
		WSPutItemWithReport[] wsPutItemWithReportArray = new WSPutItemWithReport[1];
		WSPutItemWithReport wsPutItemWithReport = new WSPutItemWithReport();
		wsPutItemWithReport.setInvokeBeforeSaving(true);
		WSPutItem wsPutItem = new WSPutItem();
		wsPutItem.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsPutItem.setWsDataModelPK(new WSDataModelPK("Order"));
		wsPutItem.setIsUpdate(false);
		wsPutItemWithReport.setWsPutItem(wsPutItem);
		wsPutItemWithReportArray[0] = wsPutItemWithReport;
		try {
			WSItemPK[] wsItemPKArray = defaultPort
					.putItemWithReportArray(wsPutItemWithReportArray);
			for (int i = 0; i < wsItemPKArray.length; i++) {
				System.out.println(wsItemPKArray[i].getConceptName());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void testPutItemWithReport() {
		WSPutItemWithReport wsPutItemWithReport = new WSPutItemWithReport();
		wsPutItemWithReport.setInvokeBeforeSaving(true);
		WSPutItem wsPutItem = new WSPutItem();
		wsPutItem.setWsDataClusterPK(new WSDataClusterPK("Order"));
		wsPutItem.setWsDataModelPK(new WSDataModelPK("Order"));
		wsPutItem.setIsUpdate(false);
		wsPutItemWithReport.setWsPutItem(wsPutItem);
		try {
			WSItemPK wsItemPK = defaultPort
					.putItemWithReport(wsPutItemWithReport);
			assertNotNull(wsItemPK);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

}
