/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.routing;

import com.amalto.core.integrity.FKIntegrityCheckResult;
import com.amalto.core.objects.DroppedItemPOJOPK;
import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.util.TransformerCallBack;
import com.amalto.core.objects.transformers.util.TransformerContext;
import com.amalto.core.objects.view.ViewPOJOPK;
import com.amalto.core.server.api.Item;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.ItemPKCriteria;
import org.apache.commons.lang.NotImplementedException;

import java.util.*;

public class MockItem implements Item {

    private final Map<String, ItemPOJO> store = new HashMap<String, ItemPOJO>();

    @Override
    public ItemPOJOPK putItem(ItemPOJO item, DataModelPOJO dataModel) throws XtentisException {
        ItemPOJOPK key = new ItemPOJOPK(new DataClusterPOJOPK(dataModel.getPK()), item.getConceptName(), item.getItemIds());
        store.put(key.toString(), item);
        return key;
    }
    
    @Override
    public ItemPOJOPK updateItemMetadata(ItemPOJO item) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ItemPOJO getItem(ItemPOJOPK pk) throws XtentisException {
        return store.get(pk.toString());
    }

    @Override
    public boolean isItemModifiedByOther(ItemPOJOPK item, long time) throws XtentisException {
        return false;
    }

    @Override
    public ItemPOJO existsItem(ItemPOJOPK pk) throws XtentisException {
        return store.get(pk.toString());
    }

    @Override
    public ItemPOJOPK deleteItem(ItemPOJOPK pk, boolean override) throws XtentisException {
        store.remove(pk.toString());
        return pk;
    }

    @Override
    public int deleteItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem search, int spellThreshold, boolean override) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public DroppedItemPOJOPK dropItem(ItemPOJOPK itemPOJOPK, String partPath, boolean override) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList viewSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK, IWhereItem whereItem, int spellThreshold, int start, int limit) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList viewSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK, IWhereItem whereItem, int spellThreshold, String orderBy, String direction, int start, int limit) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList xPathsSearch(DataClusterPOJOPK dataClusterPOJOPK, String forceMainPivot, ArrayList<String> viewablePaths, IWhereItem whereItem, int spellThreshold, int start, int limit, boolean returnCount) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList xPathsSearch(DataClusterPOJOPK dataClusterPOJOPK, String forceMainPivot, ArrayList<String> viewablePaths, IWhereItem whereItem, int spellThreshold, String orderBy, String direction, int start, int limit, boolean returnCount) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public long count(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem, int spellThreshold) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList quickSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK, String searchValue, boolean matchAllWords, int spellThreshold, String orderBy, String direction, int start, int limit) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList getFullPathValues(DataClusterPOJOPK dataClusterPOJOPK, String businessElementPath, IWhereItem whereItem, int spellThreshold, String orderBy, String direction) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public void extractUsingTransformerThroughView(DataClusterPOJOPK dataClusterPOJOPK, TransformerContext context, TransformerCallBack globalCallBack, ViewPOJOPK viewPOJOPK, IWhereItem whereItem, int spellThreshold, String orderBy, String direction, int start, int limit) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public TransformerContext extractUsingTransformerThroughView(DataClusterPOJOPK dataClusterPOJOPK, TransformerV2POJOPK transformerPOJOPK, ViewPOJOPK viewPOJOPK, IWhereItem whereItem, int spellThreshold, String orderBy, String direction, int start, int limit) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList runQuery(DataClusterPOJOPK dataClusterPOJOPK, String query, String[] parameters) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public List<String> getConceptsInDataCluster(DataClusterPOJOPK dataClusterPOJOPK) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public long countItemsByCustomFKFilters(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, String injectedXpath) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList getItemsByCustomFKFilters(DataClusterPOJOPK dataClusterPOJOPK, ArrayList<String> viewablePaths, String customXPath, IWhereItem whereItem, int start, int limit, String orderBy, String direction, boolean returnCount) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList getItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem, int spellThreshold, int start, int limit, boolean totalCountOnFirstRow) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public ArrayList getItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem, int spellThreshold, String orderBy, String direction, int start, int limit, boolean totalCountOnFirstRow) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public FKIntegrityCheckResult checkFKIntegrity(String dataClusterPK, String concept, String[] ids) throws XtentisException {
        throw new NotImplementedException();
    }

    @Override
    public List<String> getItemPKsByCriteria(ItemPKCriteria criteria) throws XtentisException {
        throw new NotImplementedException();
    }
}
