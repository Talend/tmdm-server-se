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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;

import com.amalto.core.objects.DroppedItemPOJO;
import com.amalto.core.objects.DroppedItemPOJOPK;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.backgroundjob.BackgroundJobPOJO;
import com.amalto.core.objects.backgroundjob.BackgroundJobPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.menu.MenuEntryPOJO;
import com.amalto.core.objects.menu.MenuPOJO;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.objects.routing.AbstractRoutingOrderV2POJO;
import com.amalto.core.objects.routing.AbstractRoutingOrderV2POJOPK;
import com.amalto.core.objects.routing.CompletedRoutingOrderV2POJOPK;
import com.amalto.core.objects.routing.FailedRoutingOrderV2POJOPK;
import com.amalto.core.objects.routing.RoutingRuleExpressionPOJO;
import com.amalto.core.objects.routing.RoutingRulePOJO;
import com.amalto.core.objects.storedprocedure.StoredProcedurePOJO;
import com.amalto.core.objects.transformers.TransformerV2POJO;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.util.TransformerContext;
import com.amalto.core.objects.transformers.util.TransformerPluginVariableDescriptor;
import com.amalto.core.objects.transformers.util.TransformerProcessStep;
import com.amalto.core.objects.transformers.util.TransformerVariablesMapping;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.server.api.Transformer;
import com.amalto.core.webservice.BackgroundJobStatusType;
import com.amalto.core.webservice.WSBackgroundJob;
import com.amalto.core.webservice.WSBackgroundJobPK;
import com.amalto.core.webservice.WSBoolean;
import com.amalto.core.webservice.WSByteArray;
import com.amalto.core.webservice.WSDataCluster;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModel;
import com.amalto.core.webservice.WSDroppedItem;
import com.amalto.core.webservice.WSDroppedItemPK;
import com.amalto.core.webservice.WSExtractedContent;
import com.amalto.core.webservice.WSGetItemsPivotIndexPivotWithKeysTypedContentEntry;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSLinkedHashMap;
import com.amalto.core.webservice.WSMenu;
import com.amalto.core.webservice.WSMenuEntry;
import com.amalto.core.webservice.WSMenuMenuEntriesDescriptions;
import com.amalto.core.webservice.WSOutputDecisionTable;
import com.amalto.core.webservice.WSPipeline;
import com.amalto.core.webservice.WSPipelineTypedContentEntry;
import com.amalto.core.webservice.WSProcessBytesUsingTransformerWsOutputDecisionTableDecisions;
import com.amalto.core.webservice.WSRole;
import com.amalto.core.webservice.WSRoleSpecification;
import com.amalto.core.webservice.WSRoleSpecificationInstance;
import com.amalto.core.webservice.WSRoutingOrderV2;
import com.amalto.core.webservice.WSRoutingOrderV2PK;
import com.amalto.core.webservice.WSRoutingOrderV2Status;
import com.amalto.core.webservice.WSRoutingRule;
import com.amalto.core.webservice.WSRoutingRuleExpression;
import com.amalto.core.webservice.WSRoutingRuleOperator;
import com.amalto.core.webservice.WSStoredProcedure;
import com.amalto.core.webservice.WSStringPredicate;
import com.amalto.core.webservice.WSTransformerContext;
import com.amalto.core.webservice.WSTransformerContextPipeline;
import com.amalto.core.webservice.WSTransformerContextPipelinePipelineItem;
import com.amalto.core.webservice.WSTransformerContextProjectedItemPKs;
import com.amalto.core.webservice.WSTransformerPluginV2VariableDescriptor;
import com.amalto.core.webservice.WSTransformerProcessStep;
import com.amalto.core.webservice.WSTransformerV2;
import com.amalto.core.webservice.WSTransformerVariablesMapping;
import com.amalto.core.webservice.WSTypedContent;
import com.amalto.core.webservice.WSView;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.WSWhereItem;
import com.amalto.core.webservice.WSWhereOperator;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.amalto.xmlserver.interfaces.WhereOr;

public class XConverter {

    public static LinkedHashMap WS2VO(WSLinkedHashMap wsLinkedHashMap) {
        LinkedHashMap vo = new LinkedHashMap();
        WSGetItemsPivotIndexPivotWithKeysTypedContentEntry[] typedContentEntries = wsLinkedHashMap.getTypedContentEntry();
        for (WSGetItemsPivotIndexPivotWithKeysTypedContentEntry typedContentEntry : typedContentEntries) {
            String key = typedContentEntry.getKey();
            String[] value = typedContentEntry.getValue().getStrings();
            vo.put(key, value);
        }
        return vo;
    }

    public static WSDataModel VO2WS(DataModelPOJO vo) {
        WSDataModel s = new WSDataModel();
        s.setDescription(vo.getDescription());
        s.setName(vo.getName());
        s.setXsdSchema(vo.getSchema());
        return s;
    }

    public static DataModelPOJO WS2VO(WSDataModel ws) {
        DataModelPOJO dv = new DataModelPOJO();
        dv.setName(ws.getName());
        dv.setDescription(ws.getDescription());
        dv.setSchema(ws.getXsdSchema());
        return dv;
    }

    public static WSDataCluster VO2WS(DataClusterPOJO pojo) {
        WSDataCluster s = new WSDataCluster();
        s.setDescription(pojo.getDescription());
        s.setName(pojo.getName());
        s.setVocabulary(pojo.getVocabulary());
        return s;
    }

    public static DataClusterPOJO WS2VO(WSDataCluster ws) {
        DataClusterPOJO vo = new DataClusterPOJO();
        vo.setName(ws.getName());
        vo.setDescription(ws.getDescription());
        vo.setVocabulary(ws.getVocabulary());
        return vo;
    }

    public static WSView VO2WS(ViewPOJO pojo) {
        WSView s = new WSView();
        s.setDescription(pojo.getDescription());
        s.setName(pojo.getName());
        s.setTransformerPK(pojo.getTransformerPK());
        s.setIsTransformerActive(new WSBoolean(pojo.isTransformerActive()));
        String bes[] = null;
        Collection c = pojo.getSearchableBusinessElements().getList();
        if (c != null) {
            bes = new String[c.size()];
            int i = 0;
            for (Object aC : c) {
                String be = (String) aC;
                bes[i++] = be;
            }

        }
        s.setSearchableBusinessElements(bes);
        c = pojo.getViewableBusinessElements().getList();
        if (c != null) {
            bes = new String[c.size()];
            int i = 0;
            for (Object aC : c) {
                String be = (String) aC;
                bes[i++] = be;
            }

        }
        s.setViewableBusinessElements(bes);
        c = pojo.getWhereConditions().getList();
        if (c != null) {
            WSWhereCondition wcs[] = new WSWhereCondition[c.size()];
            int i = 0;
            for (Object aC : c) {
                WhereCondition wh = (WhereCondition) aC;
                wcs[i++] = VO2WS(wh);
            }

            s.setWhereConditions(wcs);
        }
        return s;
    }

    public static ViewPOJO WS2VO(WSView ws) throws Exception {
        ViewPOJO pojo = new ViewPOJO();
        pojo.setName(ws.getName());
        pojo.setDescription(ws.getDescription());
        pojo.setTransformerPK(ws.getTransformerPK());
        pojo.setTransformerActive(ws.getIsTransformerActive().is_true());
        ArrayList l = new ArrayList();
        String s[] = ws.getSearchableBusinessElements();
        if (s != null) {
            for (int i = 0; i < s.length; i++) {
                l.add(ws.getSearchableBusinessElements()[i]);
            }

        }
        pojo.setSearchableBusinessElements(new ArrayListHolder(l));
        l = new ArrayList();
        s = ws.getViewableBusinessElements();
        if (s != null) {
            for (int i = 0; i < s.length; i++) {
                l.add(ws.getViewableBusinessElements()[i]);
            }

        }
        pojo.setViewableBusinessElements(new ArrayListHolder(l));
        l = new ArrayList();
        WSWhereCondition whs[] = ws.getWhereConditions();
        if (whs != null) {
            for (WSWhereCondition wh : whs) {
                l.add(WS2VO(wh));
            }

        }
        pojo.setWhereConditions(new ArrayListHolder(l));
        return pojo;
    }

    public static WSWhereCondition VO2WS(WhereCondition vo) {
        WSWhereCondition ws = new WSWhereCondition();
        WSWhereOperator op = WSWhereOperator.CONTAINS;
        String operator = vo.getOperator();
        if (operator.equals(WhereCondition.CONTAINS)) {
            op = WSWhereOperator.CONTAINS;
        } else if (operator.equals(WhereCondition.STARTSWITH)) {
            op = WSWhereOperator.STARTSWITH;
        } else if (operator.equals(WhereCondition.JOINS)) {
            op = WSWhereOperator.JOIN;
        } else if (operator.equals(WhereCondition.EQUALS)) {
            op = WSWhereOperator.EQUALS;
        } else if (operator.equals(WhereCondition.NOT_EQUALS)) {
            op = WSWhereOperator.NOT_EQUALS;
        } else if (operator.equals(WhereCondition.GREATER_THAN)) {
            op = WSWhereOperator.GREATER_THAN;
        } else if (operator.equals(WhereCondition.GREATER_THAN_OR_EQUAL)) {
            op = WSWhereOperator.GREATER_THAN_OR_EQUAL;
        } else if (operator.equals(WhereCondition.LOWER_THAN)) {
            op = WSWhereOperator.LOWER_THAN;
        } else if (operator.equals(WhereCondition.LOWER_THAN_OR_EQUAL)) {
            op = WSWhereOperator.LOWER_THAN_OR_EQUAL;
        } else if (operator.equals(WhereCondition.NO_OPERATOR)) {
            op = WSWhereOperator.NO_OPERATOR;
        } else if (operator.equals(WhereCondition.EMPTY_NULL)) {
            op = WSWhereOperator.EMPTY_NULL;
        }

        String predicate = vo.getStringPredicate();
        WSStringPredicate pr = WSStringPredicate.NONE;
        if ((predicate == null) || predicate.equals(WhereCondition.PRE_NONE)) {
            pr = WSStringPredicate.NONE;
        } else if (predicate.equals(WhereCondition.PRE_AND)) {
            pr = WSStringPredicate.AND;
        } else if (predicate.equals(WhereCondition.PRE_EXACTLY)) {
            pr = WSStringPredicate.EXACTLY;
        } else if (predicate.equals(WhereCondition.PRE_STRICTAND)) {
            pr = WSStringPredicate.STRICTAND;
        } else if (predicate.equals(WhereCondition.PRE_OR)) {
            pr = WSStringPredicate.OR;
        } else if (predicate.equals(WhereCondition.PRE_NOT)) {
            pr = WSStringPredicate.NOT;
        }

        ws.setLeftPath(vo.getLeftPath());
        ws.setOperator(op);
        ws.setRightValueOrPath(vo.getRightValueOrPath());
        ws.setStringPredicate(pr);
        return ws;
    }

    public static IWhereItem WS2VO(WSWhereItem ws) throws Exception {

        return WS2VO(ws, null);
    }

    public static IWhereItem WS2VO(WSWhereItem ws, WhereConditionFilter wcf) {

        if (ws == null) {
            return null;
        }

        if (ws.getWhereAnd() != null) {
            WhereAnd wand = new WhereAnd();
            WSWhereItem[] children = ws.getWhereAnd().getWhereItems();
            if (children != null) {
                for (WSWhereItem aChildren : children) {
                    wand.add(WS2VO(aChildren, wcf));
                }
            }
            return wand;
        } else if (ws.getWhereOr() != null) {
            WhereOr wor = new WhereOr();
            WSWhereItem[] children = ws.getWhereOr().getWhereItems();
            if (children != null) {
                for (WSWhereItem aChildren : children) {
                    wor.add(WS2VO(aChildren, wcf));
                }
            }
            return wor;
        } else if (ws.getWhereCondition() != null) {
            return WS2VO(ws.getWhereCondition(), wcf);
        } else {
            throw new IllegalArgumentException("The WSWhereItem mus have at least one child");
        }
    }

    public static WhereCondition WS2VO(WSWhereCondition ws) {
        return WS2VO(ws, null);
    }

    public static WhereCondition WS2VO(WSWhereCondition ws, WhereConditionFilter wcf) {

        String operator = WhereCondition.CONTAINS;
        if (ws.getOperator().equals(WSWhereOperator.CONTAINS)) {
            operator = WhereCondition.CONTAINS;
        } else if (ws.getOperator().equals(WSWhereOperator.CONTAINS_SENTENCE)) {
            operator = WhereCondition.CONTAINS_SENTENCE;
        } else if (ws.getOperator().equals(WSWhereOperator.STARTSWITH)) {
            operator = WhereCondition.STARTSWITH;
        } else if (ws.getOperator().equals(WSWhereOperator.JOIN)) {
            operator = WhereCondition.JOINS;
        } else if (ws.getOperator().equals(WSWhereOperator.EQUALS)) {
            operator = WhereCondition.EQUALS;
        } else if (ws.getOperator().equals(WSWhereOperator.NOT_EQUALS)) {
            operator = WhereCondition.NOT_EQUALS;
        } else if (ws.getOperator().equals(WSWhereOperator.GREATER_THAN)) {
            operator = WhereCondition.GREATER_THAN;
        } else if (ws.getOperator().equals(WSWhereOperator.GREATER_THAN_OR_EQUAL)) {
            operator = WhereCondition.GREATER_THAN_OR_EQUAL;
        } else if (ws.getOperator().equals(WSWhereOperator.LOWER_THAN)) {
            operator = WhereCondition.LOWER_THAN;
        } else if (ws.getOperator().equals(WSWhereOperator.LOWER_THAN_OR_EQUAL)) {
            operator = WhereCondition.LOWER_THAN_OR_EQUAL;
        } else if (ws.getOperator().equals(WSWhereOperator.NO_OPERATOR)) {
            operator = WhereCondition.NO_OPERATOR;
        } else if (ws.getOperator().equals(WSWhereOperator.FULLTEXTSEARCH)) {
            operator = WhereCondition.FULLTEXTSEARCH;
        } else if (ws.getOperator().equals(WSWhereOperator.EMPTY_NULL)) {
            operator = WhereCondition.EMPTY_NULL;
        }

        String predicate = WhereCondition.PRE_AND;
        if (ws.getStringPredicate().equals(WSStringPredicate.NONE)) {
            predicate = WhereCondition.PRE_NONE;
        } else if (ws.getStringPredicate().equals(WSStringPredicate.AND)) {
            predicate = WhereCondition.PRE_AND;
        } else if (ws.getStringPredicate().equals(WSStringPredicate.EXACTLY)) {
            predicate = WhereCondition.PRE_EXACTLY;
        } else if (ws.getStringPredicate().equals(WSStringPredicate.STRICTAND)) {
            predicate = WhereCondition.PRE_STRICTAND;
        } else if (ws.getStringPredicate().equals(WSStringPredicate.OR)) {
            predicate = WhereCondition.PRE_OR;
        } else if (ws.getStringPredicate().equals(WSStringPredicate.NOT)) {
            predicate = WhereCondition.PRE_NOT;
        }

        WhereCondition myWhereCondition = new WhereCondition(ws.getLeftPath(), operator, ws.getRightValueOrPath(), predicate,
                ws.isSpellCheck());

        if (wcf != null) {
            wcf.doFilter(myWhereCondition);
        }

        return myWhereCondition;
    }

    public static WSItemPK POJO2WS(ItemPOJOPK itemPK) {
        return new WSItemPK(new WSDataClusterPK(itemPK.getDataClusterPOJOPK().getUniqueId()), itemPK.getConceptName(),
                itemPK.getIds());
    }

    public static ItemPOJOPK WS2POJO(WSItemPK wsItemPK) {
        return new ItemPOJOPK(new DataClusterPOJOPK(wsItemPK.getWsDataClusterPK().getPk()), wsItemPK.getConceptName(),
                wsItemPK.getIds());
    }

    public static WSRoutingRule VO2WS(RoutingRulePOJO vo) {
        WSRoutingRule s = new WSRoutingRule();
        s.setDescription(vo.getDescription());
        s.setName(vo.getName());
        s.setConcept(vo.getConcept());
        s.setParameters(vo.getParameters());
        s.setServiceJNDI(vo.getServiceJNDI());
        s.setSynchronous(vo.isSynchronous());

        WSRoutingRuleExpression[] routingExpressions = null;
        Collection c = vo.getRoutingExpressions();
        if (c != null) {
            routingExpressions = new WSRoutingRuleExpression[c.size()];
            int i = 0;
            for (Object aC : c) {
                RoutingRuleExpressionPOJO rre = (RoutingRuleExpressionPOJO) aC;
                routingExpressions[i++] = VO2WS(rre);
            }
        }
        s.setWsRoutingRuleExpressions(routingExpressions);
        s.setCondition(vo.getCondition());
        s.setDeactive(vo.isDeActive());
        s.setExecuteOrder(vo.getExecuteOrder());
        return s;
    }

    public static RoutingRulePOJO WS2VO(WSRoutingRule ws) {
        RoutingRulePOJO vo = new RoutingRulePOJO();
        vo.setName(ws.getName());
        vo.setDescription(ws.getDescription());
        vo.setConcept(ws.getConcept());
        vo.setParameters(ws.getParameters());
        vo.setServiceJNDI(ws.getServiceJNDI());
        vo.setSynchronous(ws.isSynchronous());

        ArrayList<RoutingRuleExpressionPOJO> l = new ArrayList<RoutingRuleExpressionPOJO>();
        WSRoutingRuleExpression[] rre = ws.getWsRoutingRuleExpressions();
        if (rre != null) {
            for (WSRoutingRuleExpression aRre : rre) {
                l.add(WS2VO(aRre));
            }
        }
        vo.setRoutingExpressions(l);
        vo.setCondition(ws.getCondition());
        vo.setDeActive(ws.getDeactive());
        vo.setExecuteOrder(ws.getExecuteOrder());
        return vo;
    }

    public static WSRoutingRuleExpression VO2WS(RoutingRuleExpressionPOJO vo) {
        WSRoutingRuleExpression ws = new WSRoutingRuleExpression();

        ws.setName(vo.getName());
        ws.setXpath(vo.getXpath());
        ws.setValue(vo.getValue());
        switch (vo.getOperator()) {
        case RoutingRuleExpressionPOJO.CONTAINS:
            ws.setWsOperator(WSRoutingRuleOperator.CONTAINS);
            break;
        case RoutingRuleExpressionPOJO.EQUALS:
            ws.setWsOperator(WSRoutingRuleOperator.EQUALS);
            break;
        case RoutingRuleExpressionPOJO.GREATER_THAN:
            ws.setWsOperator(WSRoutingRuleOperator.GREATER_THAN);
            break;
        case RoutingRuleExpressionPOJO.GREATER_THAN_OR_EQUAL:
            ws.setWsOperator(WSRoutingRuleOperator.GREATER_THAN_OR_EQUAL);
            break;
        case RoutingRuleExpressionPOJO.IS_NOT_NULL:
            ws.setWsOperator(WSRoutingRuleOperator.IS_NOT_NULL);
            break;
        case RoutingRuleExpressionPOJO.IS_NULL:
            ws.setWsOperator(WSRoutingRuleOperator.IS_NULL);
            break;
        case RoutingRuleExpressionPOJO.LOWER_THAN:
            ws.setWsOperator(WSRoutingRuleOperator.LOWER_THAN);
            break;
        case RoutingRuleExpressionPOJO.LOWER_THAN_OR_EQUAL:
            ws.setWsOperator(WSRoutingRuleOperator.LOWER_THAN_OR_EQUAL);
            break;
        case RoutingRuleExpressionPOJO.MATCHES:
            ws.setWsOperator(WSRoutingRuleOperator.MATCHES);
            break;
        case RoutingRuleExpressionPOJO.NOT_EQUALS:
            ws.setWsOperator(WSRoutingRuleOperator.NOT_EQUALS);
            break;
        case RoutingRuleExpressionPOJO.STARTSWITH:
            ws.setWsOperator(WSRoutingRuleOperator.STARTSWITH);
            break;
        }
        return ws;
    }

    public static RoutingRuleExpressionPOJO WS2VO(WSRoutingRuleExpression ws) {

        if (ws == null) {
            return null;
        }

        int operator = 1;
        if (ws.getWsOperator().equals(WSRoutingRuleOperator.CONTAINS)) {
            operator = RoutingRuleExpressionPOJO.CONTAINS;
        } else if (ws.getWsOperator().equals(WSRoutingRuleOperator.EQUALS)) {
            operator = RoutingRuleExpressionPOJO.EQUALS;
        } else if (ws.getWsOperator().equals(WSRoutingRuleOperator.GREATER_THAN)) {
            operator = RoutingRuleExpressionPOJO.GREATER_THAN;
        } else if (ws.getWsOperator().equals(WSRoutingRuleOperator.GREATER_THAN_OR_EQUAL)) {
            operator = RoutingRuleExpressionPOJO.GREATER_THAN_OR_EQUAL;
        } else if (ws.getWsOperator().equals(WSRoutingRuleOperator.IS_NOT_NULL)) {
            operator = RoutingRuleExpressionPOJO.IS_NOT_NULL;
        } else if (ws.getWsOperator().equals(WSRoutingRuleOperator.IS_NULL)) {
            operator = RoutingRuleExpressionPOJO.IS_NULL;
        } else if (ws.getWsOperator().equals(WSRoutingRuleOperator.LOWER_THAN)) {
            operator = RoutingRuleExpressionPOJO.LOWER_THAN;
        } else if (ws.getWsOperator().equals(WSRoutingRuleOperator.LOWER_THAN_OR_EQUAL)) {
            operator = RoutingRuleExpressionPOJO.LOWER_THAN_OR_EQUAL;
        } else if (ws.getWsOperator().equals(WSRoutingRuleOperator.MATCHES)) {
            operator = RoutingRuleExpressionPOJO.MATCHES;
        } else if (ws.getWsOperator().equals(WSRoutingRuleOperator.NOT_EQUALS)) {
            operator = RoutingRuleExpressionPOJO.NOT_EQUALS;
        } else if (ws.getWsOperator().equals(WSRoutingRuleOperator.STARTSWITH)) {
            operator = RoutingRuleExpressionPOJO.STARTSWITH;
        }

        return new RoutingRuleExpressionPOJO(ws.getName(), ws.getXpath(), operator, ws.getValue());
    }

    public static WSStoredProcedure POJO2WS(StoredProcedurePOJO storedProcedurePOJO) {
        WSStoredProcedure ws = new WSStoredProcedure();
        ws.setName(storedProcedurePOJO.getName());
        ws.setDescription(storedProcedurePOJO.getDescription());
        ws.setProcedure(storedProcedurePOJO.getProcedure());
        ws.setRefreshCache(storedProcedurePOJO.isRefreshCache());
        return ws;
    }

    public static StoredProcedurePOJO WS2POJO(WSStoredProcedure wsStoredProcedure) {
        StoredProcedurePOJO pojo = new StoredProcedurePOJO();
        pojo.setName(wsStoredProcedure.getName());
        pojo.setDescription(wsStoredProcedure.getDescription());
        pojo.setProcedure(wsStoredProcedure.getProcedure());
        pojo.setRefreshCache(wsStoredProcedure.getRefreshCache() == null ? false : wsStoredProcedure.getRefreshCache());
        return pojo;
    }

    public static HashMap<String, String> WS2POJO(WSOutputDecisionTable table) {
        HashMap<String, String> decisions = new HashMap<String, String>();
        if ((table == null) || (table.getDecisions() == null) || (table.getDecisions().length == 0)) {
            return decisions;
        }
        WSProcessBytesUsingTransformerWsOutputDecisionTableDecisions[] wsDecisions = table.getDecisions();
        for (WSProcessBytesUsingTransformerWsOutputDecisionTableDecisions wsDecision : wsDecisions) {
            decisions.put(wsDecision.getOutputVariableName(), wsDecision.getDecision());
        }
        return decisions;
    }

    public static WSTransformerContext POJO2WS(TransformerContext context) throws Exception {
        WSTransformerContext wsContext = new WSTransformerContext();

        WSTransformerContextPipeline wsPipeline = new WSTransformerContextPipeline();
        ArrayList<WSTransformerContextPipelinePipelineItem> wsList = new ArrayList<WSTransformerContextPipelinePipelineItem>();
        LinkedHashMap<String, TypedContent> pipeline = context.getPipelineClone();
        Set<String> variables = pipeline.keySet();
        for (String variable : variables) {
            WSTransformerContextPipelinePipelineItem wsItem = new WSTransformerContextPipelinePipelineItem();
            wsItem.setVariable(variable);
            wsItem.setWsTypedContent(POJO2WS(pipeline.get(variable)));
            wsList.add(wsItem);
        }
        wsPipeline.setPipelineItem(wsList.toArray(new WSTransformerContextPipelinePipelineItem[wsList.size()]));
        wsContext.setPipeline(wsPipeline);

        WSTransformerContextProjectedItemPKs wsProjectedItemPKs = new WSTransformerContextProjectedItemPKs();
        ArrayList<WSItemPK> wsPKList = new ArrayList<WSItemPK>();
        SortedSet<ItemPOJOPK> projectedPKs = context.getProjectedPKs();
        for (ItemPOJOPK pk : projectedPKs) {
            wsPKList.add(XConverter.POJO2WS(pk));
        }
        wsProjectedItemPKs.setWsItemPOJOPK(wsPKList.toArray(new WSItemPK[wsPKList.size()]));
        wsContext.setProjectedItemPKs(wsProjectedItemPKs);

        return wsContext;
    }

    public static TransformerContext WS2POJO(WSTransformerContext wsContext) {
        TransformerContext context = new TransformerContext(new TransformerV2POJOPK(wsContext.getWsTransformerPK().getPk()));
        if (wsContext.getPipeline() != null) {
            for (int i = 0; i < wsContext.getPipeline().getPipelineItem().length; i++) {
                WSTransformerContextPipelinePipelineItem wsItem = wsContext.getPipeline().getPipelineItem()[i];
                context.putInPipeline(wsItem.getVariable(), WS2POJO(wsItem.getWsTypedContent()));
            }
        }
        if (wsContext.getProjectedItemPKs() != null) {
            for (int i = 0; i < wsContext.getProjectedItemPKs().getWsItemPOJOPK().length; i++) {
                WSItemPK wsPK = wsContext.getProjectedItemPKs().getWsItemPOJOPK()[i];
                context.getProjectedPKs().add(XConverter.WS2POJO(wsPK));
            }
        }

        return context;
    }

    public static WSTypedContent POJO2WS(TypedContent content) throws Exception {
        if (content == null) {
            return null;
        }
        WSTypedContent wsTypedContent = new WSTypedContent();
        if (content.getUrl() == null) {
            wsTypedContent.setWsBytes(new WSByteArray(content.getContentBytes()));
        }
        wsTypedContent.setUrl(content.getUrl());
        wsTypedContent.setContentType(content.getContentType());
        return wsTypedContent;
    }

    public static TypedContent WS2POJO(WSTypedContent wsContent) {
        TypedContent content = null;
        if (wsContent == null) {
            return null;
        }
        if (wsContent.getUrl() == null) {
            content = new TypedContent(wsContent.getWsBytes().getBytes(), wsContent.getContentType());
        } else {
            content = new TypedContent(wsContent.getUrl(), wsContent.getContentType());
        }
        return content;
    }

    public static WSTransformerVariablesMapping POJO2WS(TransformerVariablesMapping mappings) throws Exception {
        WSTransformerVariablesMapping wsMapping = new WSTransformerVariablesMapping();
        wsMapping.setPluginVariable(mappings.getPluginVariable());
        wsMapping.setPipelineVariable(mappings.getPipelineVariable());
        wsMapping.setHardCoding(POJO2WS(mappings.getHardCoding()));
        return wsMapping;
    }

    public static TransformerVariablesMapping WS2POJO(WSTransformerVariablesMapping wsMapping) {
        TransformerVariablesMapping mapping = new TransformerVariablesMapping();
        mapping.setPluginVariable(wsMapping.getPluginVariable());
        mapping.setPipelineVariable(wsMapping.getPipelineVariable());
        mapping.setHardCoding(WS2POJO(wsMapping.getHardCoding()));
        return mapping;
    }

    public static WSTransformerProcessStep POJO2WS(TransformerProcessStep processStep) throws Exception {
        WSTransformerProcessStep wsProcessStep = new WSTransformerProcessStep();
        wsProcessStep.setDescription(processStep.getDescription());
        wsProcessStep.setDisabled(processStep.isDisabled());
        wsProcessStep.setParameters(processStep.getParameters());
        wsProcessStep.setPluginJNDI(processStep.getPluginJNDI());

        ArrayList<WSTransformerVariablesMapping> wsMappings = new ArrayList<WSTransformerVariablesMapping>();
        ArrayList<TransformerVariablesMapping> list = processStep.getInputMappings();
        for (TransformerVariablesMapping mapping : list) {
            wsMappings.add(POJO2WS(mapping));
        }
        wsProcessStep.setInputMappings(wsMappings.toArray(new WSTransformerVariablesMapping[wsMappings.size()]));

        wsMappings = new ArrayList<WSTransformerVariablesMapping>();
        list = processStep.getOutputMappings();
        for (TransformerVariablesMapping mapping : list) {
            wsMappings.add(POJO2WS(mapping));
        }
        wsProcessStep.setOutputMappings(wsMappings.toArray(new WSTransformerVariablesMapping[wsMappings.size()]));
        return wsProcessStep;
    }

    public static TransformerProcessStep WS2POJO(WSTransformerProcessStep wsProcessStep) throws Exception {
        TransformerProcessStep processStep = new TransformerProcessStep();
        processStep.setDescription(wsProcessStep.getDescription());
        processStep.setDisabled(wsProcessStep.getDisabled());
        processStep.setParameters(wsProcessStep.getParameters());
        processStep.setPluginJNDI(wsProcessStep.getPluginJNDI());
        ArrayList<TransformerVariablesMapping> inputMappings = new ArrayList<TransformerVariablesMapping>();
        if (wsProcessStep.getInputMappings() != null) {
            for (int i = 0; i < wsProcessStep.getInputMappings().length; i++) {
                inputMappings.add(WS2POJO(wsProcessStep.getInputMappings()[i]));
            }
        }
        processStep.setInputMappings(inputMappings);
        ArrayList<TransformerVariablesMapping> outputMappings = new ArrayList<TransformerVariablesMapping>();
        if (wsProcessStep.getOutputMappings() != null) {
            for (int i = 0; i < wsProcessStep.getOutputMappings().length; i++) {
                outputMappings.add(WS2POJO(wsProcessStep.getOutputMappings()[i]));
            }
        }
        processStep.setOutputMappings(outputMappings);
        return processStep;
    }

    public static WSTransformerV2 POJO2WS(TransformerV2POJO transformerPOJO) throws Exception {
        WSTransformerV2 ws = new WSTransformerV2();
        ws.setName(transformerPOJO.getName());
        ws.setDescription(transformerPOJO.getDescription());
        ArrayList<WSTransformerProcessStep> wsSteps = new ArrayList<WSTransformerProcessStep>();
        ArrayList<TransformerProcessStep> processSteps = transformerPOJO.getProcessSteps();
        if (processSteps != null) {
            for (TransformerProcessStep processStep : processSteps) {
                wsSteps.add(POJO2WS(processStep));
            }
        }
        ws.setProcessSteps(wsSteps.toArray(new WSTransformerProcessStep[wsSteps.size()]));
        return ws;
    }

    public static TransformerV2POJO WS2POJO(WSTransformerV2 wsTransformerV2) throws Exception {
        TransformerV2POJO pojo = new TransformerV2POJO();
        pojo.setName(wsTransformerV2.getName());
        pojo.setDescription(wsTransformerV2.getDescription());
        ArrayList<TransformerProcessStep> steps = new ArrayList<TransformerProcessStep>();
        WSTransformerProcessStep[] wsSteps = wsTransformerV2.getProcessSteps();
        if (wsSteps != null) {
            for (WSTransformerProcessStep wsStep : wsSteps) {
                TransformerProcessStep step = WS2POJO(wsStep);
                steps.add(step);
            }
        }
        pojo.setProcessSteps(steps);
        return pojo;
    }

    public static HashMap<String, TypedContent> WS2POJO(WSPipeline wsPipeline) {
        if (wsPipeline == null) {
            return null;
        }

        HashMap<String, TypedContent> pipeline = new HashMap<String, TypedContent>();
        WSPipelineTypedContentEntry[] entries = wsPipeline.getTypedContentEntry();
        if (entries == null) {
            return pipeline;
        }

        for (WSPipelineTypedContentEntry entry : entries) {
            pipeline.put(entry.getOutput(), new TypedContent(entry.getWsExtractedContent().getWsByteArray().getBytes(), entry
                    .getWsExtractedContent().getContentType()));
        }
        return pipeline;
    }

    public static WSTransformerPluginV2VariableDescriptor POJO2WS(TransformerPluginVariableDescriptor descriptor) {
        WSTransformerPluginV2VariableDescriptor wsDescriptor = new WSTransformerPluginV2VariableDescriptor();
        wsDescriptor.setVariableName(descriptor.getVariableName());
        if (descriptor.getDescriptions().size() > 0) {
            wsDescriptor.setDescription(descriptor.getDescriptions().values().iterator().next());
        }
        wsDescriptor.setMandatory(descriptor.isMandatory());
        ArrayList<String> contentTypesRegex = new ArrayList<String>();
        if (descriptor.getContentTypesRegex() != null) {
            for (Pattern p : descriptor.getContentTypesRegex()) {
                contentTypesRegex.add(p.toString());
            }
        }
        wsDescriptor.setContentTypesRegex(contentTypesRegex.toArray(new String[contentTypesRegex.size()]));
        ArrayList<String> possibleValuesRegex = new ArrayList<String>();
        if (descriptor.getPossibleValuesRegex() != null) {
            for (Pattern p : descriptor.getPossibleValuesRegex()) {
                possibleValuesRegex.add(p.toString());
            }
        }
        wsDescriptor.setPossibleValuesRegex(possibleValuesRegex.toArray(new String[possibleValuesRegex.size()]));
        return wsDescriptor;
    }

    public static WSRole POJO2WS(RolePOJO rolePOJO) {
        WSRole ws = new WSRole();
        ws.setName(rolePOJO.getName());
        ws.setDescription(rolePOJO.getDescription());
        Set objectTypes = rolePOJO.getRoleSpecifications().keySet();
        ArrayList wsSpecifications = new ArrayList();
        WSRoleSpecification wsSpecification;
        for (Iterator iter = objectTypes.iterator(); iter.hasNext(); wsSpecifications.add(wsSpecification)) {
            String objectType = (String) iter.next();
            RoleSpecification specification = rolePOJO.getRoleSpecifications().get(objectType);
            ArrayList wsInstances = new ArrayList();
            Set instanceIds = specification.getInstances().keySet();
            WSRoleSpecificationInstance wsInstance;
            for (Iterator iterator = instanceIds.iterator(); iterator.hasNext(); wsInstances.add(wsInstance)) {
                String id = (String) iterator.next();
                RoleInstance instance = specification.getInstances().get(id);
                String wsParameters[] = instance.getParameters().toArray(new String[instance.getParameters().size()]);
                wsInstance = new WSRoleSpecificationInstance(id, instance.isWriteable(), wsParameters);
            }

            wsSpecification = new WSRoleSpecification(objectType, specification.isAdmin(),
                    (WSRoleSpecificationInstance[]) wsInstances.toArray(new WSRoleSpecificationInstance[wsInstances.size()]));
        }

        ws.setSpecification((WSRoleSpecification[]) wsSpecifications.toArray(new WSRoleSpecification[wsSpecifications.size()]));
        return ws;
    }

    public static RolePOJO WS2POJO(WSRole wsRole) {
        RolePOJO pojo = new RolePOJO();
        pojo.setName(wsRole.getName());
        pojo.setDescription(wsRole.getDescription());
        HashMap specifications = new HashMap();
        if (wsRole.getSpecification() != null) {
            for (int i = 0; i < wsRole.getSpecification().length; i++) {
                WSRoleSpecification wsSpecification = wsRole.getSpecification()[i];
                RoleSpecification specification = new RoleSpecification();
                specification.setAdmin(wsSpecification.isAdmin());
                if (wsSpecification.getInstance() != null) {
                    for (int j = 0; j < wsSpecification.getInstance().length; j++) {
                        WSRoleSpecificationInstance wsInstance = wsSpecification.getInstance()[j];
                        RoleInstance instance = new RoleInstance();
                        instance.setWriteable(wsInstance.isWritable());
                        instance.setParameters(new HashSet());
                        if (wsInstance.getParameter() != null) {
                            instance.getParameters().addAll(Arrays.asList(wsInstance.getParameter()));
                        }
                        specification.getInstances().put(wsInstance.getInstanceName(), instance);
                    }

                }
                specifications.put(wsSpecification.getObjectType(), specification);
            }

        }
        pojo.setRoleSpecifications(specifications);
        return pojo;
    }

    public static WSMenu POJO2WS(MenuPOJO pojo) {
        WSMenu ws = new WSMenu();
        ws.setName(pojo.getName());
        ws.setDescription(pojo.getDescription());
        if (pojo.getMenuEntries() != null) {
            WSMenuEntry[] wsSubMenus = new WSMenuEntry[pojo.getMenuEntries().size()];
            int i = 0;
            for (MenuEntryPOJO menuEntry : pojo.getMenuEntries()) {
                wsSubMenus[i++] = POJO2WS(menuEntry);
            }
            ws.setMenuEntries(wsSubMenus);
        }
        return ws;
    }

    public static MenuPOJO WS2POJO(WSMenu ws) {
        MenuPOJO pojo = new MenuPOJO();
        pojo.setName(ws.getName());
        pojo.setDescription(ws.getDescription());
        ArrayList<MenuEntryPOJO> menuEntries = new ArrayList<MenuEntryPOJO>();
        if (ws.getMenuEntries() != null) {
            for (int i = 0; i < ws.getMenuEntries().length; i++) {
                menuEntries.add(WS2POJO(ws.getMenuEntries()[i]));
            }
        }
        pojo.setMenuEntries(menuEntries);
        return pojo;
    }

    public static WSMenuEntry POJO2WS(MenuEntryPOJO pojo) {
        WSMenuEntry ws = new WSMenuEntry();
        ws.setId(pojo.getId());
        Set<String> languages = pojo.getDescriptions().keySet();
        WSMenuMenuEntriesDescriptions[] wsDescriptions = new WSMenuMenuEntriesDescriptions[languages.size()];
        int i = 0;
        for (String language : languages) {
            wsDescriptions[i] = new WSMenuMenuEntriesDescriptions();
            wsDescriptions[i].setLanguage(language);
            wsDescriptions[i].setLabel(pojo.getDescriptions().get(language));
            i++;
        }
        ws.setDescriptions(wsDescriptions);
        ws.setContext(pojo.getContext());
        ws.setApplication(pojo.getApplication());
        ws.setIcon(pojo.getIcon());
        if (pojo.getSubMenus() != null) {
            WSMenuEntry[] wsSubMenus = new WSMenuEntry[pojo.getSubMenus().size()];
            i = 0;
            for (MenuEntryPOJO menuEntry : pojo.getSubMenus()) {
                wsSubMenus[i++] = POJO2WS(menuEntry);
            }
            ws.setSubMenus(wsSubMenus);
        }
        return ws;
    }

    public static MenuEntryPOJO WS2POJO(WSMenuEntry ws) {
        MenuEntryPOJO pojo = new MenuEntryPOJO();
        pojo.setId(ws.getId());
        HashMap<String, String> descriptions = new HashMap<String, String>();
        if (ws.getDescriptions() != null) {
            for (int i = 0; i < ws.getDescriptions().length; i++) {
                descriptions.put(ws.getDescriptions()[i].getLanguage(), ws.getDescriptions()[i].getLabel());
            }
        }
        pojo.setDescriptions(descriptions);
        pojo.setContext(ws.getContext());
        pojo.setApplication(ws.getApplication());
        pojo.setIcon(ws.getIcon());
        ArrayList<MenuEntryPOJO> subMenus = new ArrayList<MenuEntryPOJO>();
        if (ws.getSubMenus() != null) {
            for (int i = 0; i < ws.getSubMenus().length; i++) {
                subMenus.add(WS2POJO(ws.getSubMenus()[i]));
            }
        }
        pojo.setSubMenus(subMenus);
        return pojo;
    }

    public static WSBackgroundJob POJO2WS(BackgroundJobPOJO pojo) throws Exception {
        try {
            WSBackgroundJob s = new WSBackgroundJob();
            s.setId(pojo.getId());
            s.setDescription(pojo.getDescription());
            switch (pojo.getStatus()) {
            case 0:
                s.setStatus(BackgroundJobStatusType.COMPLETED);
                break;
            case 1:
                s.setStatus(BackgroundJobStatusType.RUNNING);
                break;
            case 2:
                s.setStatus(BackgroundJobStatusType.SUSPENDED);
                break;
            case 3:
                s.setStatus(BackgroundJobStatusType.STOPPED);
                break;
            case 4:
                s.setStatus(BackgroundJobStatusType.CANCEL_REQUESTED);
                break;
            case 5:
                s.setStatus(BackgroundJobStatusType.SCHEDULED);
                break;
            default:
                throw new Exception("Unknow BackgroundJob Status: " + pojo.getStatus());
            }
            s.setMessage(pojo.getMessage());
            s.setPercentage(pojo.getPercentage());
            s.setTimestamp(pojo.getTimestamp());

            // concert core WSPipeline to webapp.core WSPipeline
            WSPipeline wsPipeline = new WSPipeline();
            WSPipelineTypedContentEntry[] wsEntries = new WSPipelineTypedContentEntry[pojo.getWsPipeline().getTypedContentEntry().length];
            for (int i = 0; i < pojo.getWsPipeline().getTypedContentEntry().length; i++) {
                String output = pojo.getWsPipeline().getTypedContentEntry()[i].getOutput();
                String contentType = pojo.getWsPipeline().getTypedContentEntry()[i].getWsExtractedContent().getContentType();
                byte[] bytes = pojo.getWsPipeline().getTypedContentEntry()[i].getWsExtractedContent().getWsByteArray().getBytes();
                wsEntries[i] = new WSPipelineTypedContentEntry(output,
                        new WSExtractedContent(new WSByteArray(bytes), contentType));
            }
            wsPipeline.setTypedContentEntry(wsEntries);
            s.setPipeline(wsPipeline);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }

    public static BackgroundJobPOJO WS2POJO(WSBackgroundJob ws) throws Exception {
        BackgroundJobPOJO pojo = new BackgroundJobPOJO();
        pojo.setId(ws.getId());
        pojo.setMessage(ws.getMessage());
        pojo.setDescription(ws.getDescription());
        pojo.setPercentage(ws.getPercentage());
        pojo.setTimestamp(ws.getTimestamp());
        if (ws.getStatus().equals(BackgroundJobStatusType.CANCEL_REQUESTED)) {
            pojo.setStatus(BackgroundJobPOJO._CANCEL_REQUESTED_);
        } else if (ws.getStatus().equals(BackgroundJobStatusType.COMPLETED)) {
            pojo.setStatus(BackgroundJobPOJO._COMPLETED_);
        } else if (ws.getStatus().equals(BackgroundJobStatusType.RUNNING)) {
            pojo.setStatus(BackgroundJobPOJO._RUNNING_);
        } else if (ws.getStatus().equals(BackgroundJobStatusType.SCHEDULED)) {
            pojo.setStatus(BackgroundJobPOJO._SCHEDULED_);
        } else if (ws.getStatus().equals(BackgroundJobStatusType.STOPPED)) {
            pojo.setStatus(BackgroundJobPOJO._STOPPED_);
        } else if (ws.getStatus().equals(BackgroundJobStatusType.SUSPENDED)) {
            pojo.setStatus(BackgroundJobPOJO._SUSPENDED_);
        }
        pojo.setPipeline(WS2POJO(ws.getPipeline()));
        // we do not rewrite the pipeline
        return pojo;
    }

    public static WSRoutingOrderV2PK POJO2WS(AbstractRoutingOrderV2POJOPK pojo) throws Exception {
        if (pojo == null) {
            return null;
        }
        try {
            WSRoutingOrderV2PK ws = new WSRoutingOrderV2PK();
            ws.setName(pojo.getName());
            switch (pojo.getStatus()) {
            case AbstractRoutingOrderV2POJO.ACTIVE:
                ws.setStatus(WSRoutingOrderV2Status.ACTIVE);
                break;
            case AbstractRoutingOrderV2POJO.COMPLETED:
                ws.setStatus(WSRoutingOrderV2Status.COMPLETED);
                break;
            case AbstractRoutingOrderV2POJO.FAILED:
                ws.setStatus(WSRoutingOrderV2Status.FAILED);
                break;
            }
            return ws;
        } catch (Exception e) {
            String err = "ERROR SYSTRACE: " + e.getMessage();
            org.apache.log4j.Logger.getLogger(XConverter.class).debug(err, e);
            throw (e);
        }
    }

    public static AbstractRoutingOrderV2POJOPK WS2POJO(WSRoutingOrderV2PK s) throws Exception {
        if (s == null) {
            return null;
        }
        try {
            AbstractRoutingOrderV2POJOPK pojo = null;
            if (s.getStatus().equals(WSRoutingOrderV2Status.COMPLETED)) {
                pojo = new CompletedRoutingOrderV2POJOPK(s.getName());
            } else if (s.getStatus().equals(WSRoutingOrderV2Status.FAILED)) {
                pojo = new FailedRoutingOrderV2POJOPK(s.getName());
            }
            return pojo;
        } catch (Exception e) {
            String err = "ERROR SYSTRACE: " + e.getMessage();
            org.apache.log4j.Logger.getLogger(XConverter.class).debug(err, e);
            throw (e);
        }
    }

    public static WSRoutingOrderV2 POJO2WS(AbstractRoutingOrderV2POJO pojo) throws Exception {
        if (pojo == null) {
            return null;
        }
        try {
            WSRoutingOrderV2 ws = new WSRoutingOrderV2();
            ws.setMessage(pojo.getMessage());
            ws.setName(pojo.getName());
            ws.setServiceJNDI(pojo.getServiceJNDI());
            ws.setServiceParameters(pojo.getServiceParameters());
            switch (pojo.getStatus()) {
            case AbstractRoutingOrderV2POJO.ACTIVE:
                ws.setStatus(WSRoutingOrderV2Status.ACTIVE);
                break;
            case AbstractRoutingOrderV2POJO.COMPLETED:
                ws.setStatus(WSRoutingOrderV2Status.COMPLETED);
                break;
            case AbstractRoutingOrderV2POJO.FAILED:
                ws.setStatus(WSRoutingOrderV2Status.FAILED);
                break;
            }
            ws.setTimeCreated(pojo.getTimeCreated());
            ws.setTimeLastRunCompleted(pojo.getTimeLastRunCompleted());
            ws.setTimeLastRunStarted(pojo.getTimeLastRunStarted());
            ws.setTimeScheduled(pojo.getTimeScheduled());
            ws.setWsItemPK(XConverter.POJO2WS(pojo.getItemPOJOPK()));
            return ws;
        } catch (Exception e) {
            String err = "ERROR SYSTRACE: " + e.getMessage();
            org.apache.log4j.Logger.getLogger(XConverter.class).debug(err, e);
            throw (e);
        }
    }

    public static WSDroppedItemPK POJO2WS(DroppedItemPOJOPK droppedItemPOJOPK) {
        ItemPOJOPK refItemPOJOPK = droppedItemPOJOPK.getRefItemPOJOPK();
        return new WSDroppedItemPK(POJO2WS(refItemPOJOPK), droppedItemPOJOPK.getPartPath());
    }

    public static DroppedItemPOJOPK WS2POJO(WSDroppedItemPK wsDroppedItemPK) {
        ItemPOJOPK refItemPOJOPK = WS2POJO(wsDroppedItemPK.getWsItemPK());
        return new DroppedItemPOJOPK(refItemPOJOPK, wsDroppedItemPK.getPartPath());
    }

    public static WSDroppedItem POJO2WS(DroppedItemPOJO droppedItemPOJO) {

        return new WSDroppedItem(new WSDataClusterPK(droppedItemPOJO.getDataClusterPOJOPK().getUniqueId()),
                droppedItemPOJO.getUniqueId(), droppedItemPOJO.getConceptName(), droppedItemPOJO.getIds(),
                droppedItemPOJO.getPartPath(), droppedItemPOJO.getInsertionUserName(), droppedItemPOJO.getInsertionTime(),
                droppedItemPOJO.getProjection());

    }

    public static DroppedItemPOJO WS2POJO(WSDroppedItem wsDroppedItem) {

        return new DroppedItemPOJO(new DataClusterPOJOPK(wsDroppedItem.getWsDataClusterPK().getPk()),
                wsDroppedItem.getUniqueId(), wsDroppedItem.getConceptName(), wsDroppedItem.getIds(), wsDroppedItem.getPartPath(),
                wsDroppedItem.getProjection(), wsDroppedItem.getInsertionUserName(), wsDroppedItem.getInsertionTime());

    }

    public static ItemPOJOPK[] WS2POJO(WSItemPK[] wsItemPKs) {
        if (wsItemPKs == null) {
            return null;
        } else {
            ItemPOJOPK[] itemPOJOPKs = new ItemPOJOPK[wsItemPKs.length];
            for (int i = 0; i < itemPOJOPKs.length; i++) {
                itemPOJOPKs[i] = WS2POJO(wsItemPKs[i]);
            }
            return itemPOJOPKs;
        }
    }

    public static WSPipeline POJO2WS(HashMap<String, TypedContent> pipeline) {
        try {
            ArrayList<WSPipelineTypedContentEntry> entries = new ArrayList<WSPipelineTypedContentEntry>();
            Set keys = pipeline.keySet();
            for (Object key : keys) {
                String output = (String) key;
                TypedContent content = pipeline.get(output);
                byte[] bytes = content.getContentBytes();
                if (bytes == null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int c;
                    while ((c = content.getContentStream().read()) != -1) {
                        bos.write(c);
                    }
                    bytes = bos.toByteArray();
                }
                WSExtractedContent wsContent = new WSExtractedContent(new WSByteArray(bytes), content.getContentType());
                WSPipelineTypedContentEntry wsEntry = new WSPipelineTypedContentEntry(
                        Transformer.DEFAULT_VARIABLE.equals(output) ? "" : output, wsContent);
                entries.add(wsEntry);
            }
            return new WSPipeline(entries.toArray(new WSPipelineTypedContentEntry[entries.size()]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static WSBackgroundJobPK POJO2WS(BackgroundJobPOJOPK pk) {
        return new WSBackgroundJobPK(pk.getUniqueId());
    }
}
