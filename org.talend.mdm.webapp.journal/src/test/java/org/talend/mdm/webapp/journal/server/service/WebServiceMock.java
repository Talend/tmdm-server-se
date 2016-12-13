/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Set;

import org.talend.mdm.webapp.base.server.util.Constants;

import com.amalto.webapp.core.util.XtentisWebappException;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSGetItemsSort;
import com.amalto.core.webservice.WSItem;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSStringArray;
import com.sun.xml.xsom.XSElementDecl;


/**
 * created by talend2 on 2013-2-7
 * Detailled comment
 *
 */
public class WebServiceMock implements WebService {
    
    private boolean enterpriseVersion = false;
    
    private String forbiddenDataModelName = ""; //$NON-NLS-1$
    
    private String forbiddenconceptName = ""; //$NON-NLS-1$

    public WSItem getItem(WSGetItem wSGetItem) throws XtentisWebappException, RemoteException {   
        String[] ids = {"genericUI","1360140140037"}; //$NON-NLS-1$ //$NON-NLS-2$
        WSItemPK pk = wSGetItem.getWsItemPK();
        if ("UpdateReport".equals(pk.getWsDataClusterPK().getPk()) && "Update".equals(pk.getConceptName()) && "genericUI".equals(pk.getIds()[0]) && "1360140140037".equals(pk.getIds()[1])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            WSItem item = new WSItem();
            item.setConceptName("Update"); //$NON-NLS-1$
            item.setContent("<Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1361153957282</TimeInMillis><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>1</Key><Item><path>Name</path><oldValue>1</oldValue><newValue>123</newValue></Item><OperationType>UPDATE</OperationType></Update>"); //$NON-NLS-1$
            item.setDataModelName("UpdateReport"); //$NON-NLS-1$
            item.setIds(ids);
            item.setWsDataClusterPK(new WSDataClusterPK("UpdateReport")); //$NON-NLS-1$
            return item;
        } else if ("Product".equals(pk.getWsDataClusterPK().getPk()) && "ProductFamily".equals(pk.getConceptName()) && "1".equals(pk.getIds()[0])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            WSItem item = new WSItem();
            item.setConceptName("ProductFamily"); //$NON-NLS-1$
            item.setContent("<ProductFamily><Id>1</Id><Name>Test_Product_FKInfo</Name></ProductFamily>"); //$NON-NLS-1$
            item.setDataModelName("UpdateReport"); //$NON-NLS-1$ 
            item.setWsDataClusterPK(new WSDataClusterPK("Product")); //$NON-NLS-1$
            return item;
        } else {
            return null;
        }        
    }
    
    public WSStringArray getItemsBySort(WSGetItemsSort wsGetItemsSort) throws RemoteException, XtentisWebappException {
        if ("Update".equals(wsGetItemsSort.getConceptName()) && "UpdateReport".equals(wsGetItemsSort.getWsDataClusterPK().getPk()) && Constants.SEARCH_DIRECTION_ASC.equals(wsGetItemsSort.getDir()) && "Update/Key".equals(wsGetItemsSort.getSort())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String[] result = {"<totalCount>20</totalCount>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371707848921</TimeInMillis><OperationType>CREATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>1</Key></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371707857579</TimeInMillis><OperationType>UPDATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>1</Key><Item><path>Name</path><oldValue>1</oldValue><newValue>2</newValue></Item><Item><path>Description</path><oldValue>1</oldValue><newValue>2</newValue></Item><Item><path>Price</path><oldValue>1.00</oldValue><newValue>2</newValue></Item></Update></result>","<result><Update><UserName>user</UserName><Source>genericUI</Source><TimeInMillis>1371708282306</TimeInMillis><OperationType>CREATE</OperationType><DataCluster>T</DataCluster><DataModel>T</DataModel><Concept>T1</Concept><Key>1</Key></Update></result>","<result><Update><UserName>user</UserName><Source>genericUI</Source><TimeInMillis>1371708286721</TimeInMillis><OperationType>UPDATE</OperationType><DataCluster>T</DataCluster><DataModel>T</DataModel><Concept>T1</Concept><Key>1</Key><Item><path>Name</path><oldValue>1</oldValue><newValue>2</newValue></Item></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371708555686</TimeInMillis><OperationType>CREATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>ProductFamily</Concept><Key>1</Key></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371708564048</TimeInMillis><OperationType>CREATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>ProductFamily</Concept><Key>2</Key></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371708577402</TimeInMillis><OperationType>UPDATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>1</Key><Item><path>Family</path><newValue>[1]</newValue></Item></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371708584531</TimeInMillis><OperationType>UPDATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>1</Key><Item><path>Family</path><oldValue>[1]</oldValue><newValue>[2]</newValue></Item></Update></result>","<result><Update><UserName>user</UserName><Source>genericUI</Source><TimeInMillis>1371710319093</TimeInMillis><OperationType>CREATE</OperationType><DataCluster>T</DataCluster><DataModel>T</DataModel><Concept>T1</Concept><Key>2</Key></Update></result>","<result><Update><UserName>user</UserName><Source>genericUI</Source><TimeInMillis>1371710363803</TimeInMillis><OperationType>LOGIC_DELETE</OperationType><DataCluster>T</DataCluster><DataModel>T</DataModel><Concept>T1</Concept><Key>2</Key></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371710391431</TimeInMillis><OperationType>CREATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>4</Key></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371710394878</TimeInMillis><OperationType>LOGIC_DELETE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>1</Key></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371710401555</TimeInMillis><OperationType>LOGIC_DELETE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>4</Key></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371710409573</TimeInMillis><OperationType>RESTORED</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>1</Key></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371710709610</TimeInMillis><OperationType>CREATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>3</Key></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371710716006</TimeInMillis><OperationType>UPDATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>3</Key><Item><path>Family</path><newValue>[1]</newValue></Item></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371710721949</TimeInMillis><OperationType>UPDATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>3</Key><Item><path>Family</path><oldValue>[1]</oldValue><newValue>[2]</newValue></Item></Update></result>","<result><Update><UserName>administrator</UserName><Source>genericUI</Source><TimeInMillis>1371710853832</TimeInMillis><OperationType>UPDATE</OperationType><DataCluster>Product</DataCluster><DataModel>Product</DataModel><Concept>Product</Concept><Key>3</Key><Item><path>Family</path><oldValue>[2]</oldValue><newValue>[1]</newValue></Item></Update></result>","<result><Update><UserName>user</UserName><Source>genericUI</Source><TimeInMillis>1371711263168</TimeInMillis><OperationType>RESTORED</OperationType><DataCluster>T</DataCluster><DataModel>T</DataModel><Concept>T1</Concept><Key>2</Key></Update></result>","<result><Update><UserName>user</UserName><Source>genericUI</Source><TimeInMillis>1371711269798</TimeInMillis><OperationType>LOGIC_DELETE</OperationType><DataCluster>T</DataCluster><DataModel>T</DataModel><Concept>T1</Concept><Key>2</Key></Update></result>"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$ //$NON-NLS-19$ //$NON-NLS-20$ //$NON-NLS-21$
            return new WSStringArray(result);
        } else {
            return null;
        } 
    }

    public XSElementDecl getXSElementDecl(String dataModel, String concept) throws Exception {
        String xsd = ""; //$NON-NLS-1$
        if ("Product".equals(dataModel) && "Product".equals(concept)) { //$NON-NLS-1$ //$NON-NLS-2$
            xsd = readFile("Product.xsd"); //$NON-NLS-1$
        }
        return com.amalto.core.util.Util.getConceptMap(xsd).get(concept);
    }
    
    public boolean isAuth(Set<String> roleSet) throws Exception {
        return true;
    }
    
    private String readFile(String fileName) throws IOException{
        String txt = ""; //$NON-NLS-1$
        String tempString = ""; //$NON-NLS-1$
        InputStream in = WebServiceMock.class.getResourceAsStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while ((tempString = reader.readLine()) != null) {
            txt += tempString;
        }
        reader.close();      
        return txt;
    }

    @Override
    public boolean isEnterpriseVersion() {        
        return this.enterpriseVersion;
    }

    @Override
    public boolean userCanRead(Class<?> dataModel, String dataModelName) {
        if (dataModelName.equals(forbiddenDataModelName)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean checkReadAccess(String dataModelName, String conceptName) {
        if (conceptName.equals(forbiddenconceptName)) {
            return false; 
        } else {
            return true;
        }
    }

    public void setEnterpriseVersion(boolean enterpriseVersion) {
        this.enterpriseVersion = enterpriseVersion;
    }

    
    public void setForbiddenDataModelName(String forbiddenDataModelName) {
        this.forbiddenDataModelName = forbiddenDataModelName;
    }

    public void setForbiddenconceptName(String forbiddenconceptName) {
        this.forbiddenconceptName = forbiddenconceptName;
    }
}
