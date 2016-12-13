/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.datamodel.management.BusinessConcept;
import org.talend.mdm.webapp.base.client.exception.ParserException;
import org.talend.mdm.webapp.base.client.model.Criteria;
import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;
import org.talend.mdm.webapp.base.client.util.Parser;
import org.talend.mdm.webapp.base.server.exception.ExceptionConstants;
import org.talend.mdm.webapp.base.server.exception.WebBaseException;

import com.amalto.core.server.ServerContext;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSStringPredicate;
import com.amalto.core.webservice.WSViewPK;
import com.amalto.core.webservice.WSViewSearch;
import com.amalto.core.webservice.WSWhereAnd;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.WSWhereItem;
import com.amalto.core.webservice.WSWhereOr;
import com.amalto.core.webservice.XtentisPort;
import com.amalto.webapp.core.bean.Configuration;
import com.amalto.webapp.core.dmagent.SchemaWebAgent;
import com.amalto.webapp.core.util.Util;
import com.amalto.webapp.core.util.XtentisWebappException;

public class CommonUtil {

    public static final String AND = "AND"; //$NON-NLS-1$

    public static final String OR = "OR"; //$NON-NLS-1$ 

    public static final String EQUALS = "EQUALS"; //$NON-NLS-1$

    private static final Pattern extractIdPattern = Pattern.compile("\\[.*?\\]"); //$NON-NLS-1$

    private static final Messages MESSAGES = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.base.client.i18n.BaseMessages", CommonUtil.class.getClassLoader()); //$NON-NLS-1$    

    /**
     * DOC HSHU Comment method "getPort".
     * 
     * @return
     * @throws XtentisWebappException
     */
    public static XtentisPort getPort() throws XtentisWebappException {
        return com.amalto.webapp.core.util.Util.getPort();
    }

    /**
     * Join an arraylist of strings into a single string using a separator
     * 
     * @param strings
     * @param separator
     * @return a single string or null
     */
    public static String joinStrings(List<String> strings, String separator) {
        if (strings == null) {
            return null;
        }
        String res = ""; //$NON-NLS-1$ 
        for (int i = 0; i < strings.size(); i++) {
            res += (i > 0) ? separator : ""; //$NON-NLS-1$ 
            res += strings.get(i);
        }
        return res;
    }

    public static List<String> splitString(String valueString, char separator) {
        if (valueString == null || valueString.isEmpty()) {
            return null;
        }
        List<String> valueList = new ArrayList<String>();
        String[] valueArray = StringUtils.split(valueString, separator);
        for (String value : valueArray) {
            valueList.add(value);
        }
        return valueList;
    }

    public static WSWhereItem buildWhereItems(String criteria) throws ParserException, Exception {
        WSWhereItem wi;
        if (criteria.contains("../../t")) { //$NON-NLS-1$
            List<WSWhereItem> conditions = new ArrayList<WSWhereItem>();
            if (criteria.indexOf("../../t") - 5 > -1) { //$NON-NLS-1$
                conditions
                        .add(buildWhereItemsByCriteria(Parser.parse(criteria.substring(0, criteria.indexOf("../../t") - 5) + ")")));//$NON-NLS-1$  //$NON-NLS-2$
            }

            String modifyString = criteria.substring(criteria.indexOf("../../t")); //$NON-NLS-1$
            modifyString = modifyString.lastIndexOf(')') == -1 ? modifyString : modifyString.substring(0,
                    modifyString.length() - 1);
            String[] modifyArr = modifyString.split("AND"); //$NON-NLS-1$
            for (String str : modifyArr) {
                conditions.add(buildWhereItem(str.trim()));
            }

            WSWhereAnd and = new WSWhereAnd(conditions.toArray(new WSWhereItem[conditions.size()]));
            wi = new WSWhereItem(null, and, null);
        } else {
            wi = buildWhereItemsByCriteria(Parser.parse(criteria));
        }
        return wi;
    }

    public static WSWhereItem buildWhereItemsByCriteria(Criteria criteria) throws Exception {
        WSWhereItem wi = null;
        ArrayList<WSWhereItem> conditions = new ArrayList<WSWhereItem>();
        if (criteria instanceof MultipleCriteria) {
            MultipleCriteria multipleCriteria = (MultipleCriteria) criteria;
            if (multipleCriteria.getOperator().equals(AND)) {
                WSWhereAnd and = new WSWhereAnd();

                for (Criteria current : multipleCriteria.getChildren()) {
                    if (current instanceof SimpleCriterion) {
                        WSWhereItem item = buildWhereItem(current.toString());
                        conditions.add(item);
                    } else if (current instanceof MultipleCriteria) {
                        WSWhereItem item = buildWhereItemsByCriteria(current);
                        conditions.add(item);
                    }
                }
                and.setWhereItems(conditions.toArray(new WSWhereItem[conditions.size()]));
                wi = new WSWhereItem(null, and, null);
            } else if (multipleCriteria.getOperator().equals(OR)) {
                WSWhereOr or = new WSWhereOr();

                for (Criteria current : multipleCriteria.getChildren()) {
                    if (current instanceof SimpleCriterion) {
                        WSWhereItem item = buildWhereItem(current.toString());
                        conditions.add(item);
                    } else if (current instanceof MultipleCriteria) {
                        WSWhereItem item = buildWhereItemsByCriteria(current);
                        conditions.add(item);
                    }
                }
                or.setWhereItems(conditions.toArray(new WSWhereItem[conditions.size()]));
                wi = new WSWhereItem(null, null, or);
            }
        } else if (criteria instanceof SimpleCriterion) {
            wi = buildWhereItem(criteria.toString());
        }
        return wi;
    }

    public static WSWhereItem buildWhereItem(String criteria) throws Exception {
        WSWhereItem wi;
        String[] filters = criteria.split(" "); //$NON-NLS-1$ 
        String filterXpaths, filterOperators;
        String filterValues = ""; //$NON-NLS-1$ 

        filterXpaths = filters[0];
        filterOperators = filters[1];
        if (filters.length <= 2) {
            filterValues = " "; //$NON-NLS-1$
        } else {
            // Value might contains spaces (if search contains a sentence) and split call might separated words.
            for (int i = 2; i < filters.length; i++) {
                filterValues += filters[i];
                if (i != filters.length - 1) {
                    filterValues += " "; //$NON-NLS-1$ 
                }
            }
        }

        if (filterXpaths == null || filterXpaths.trim().equals("")) { //$NON-NLS-1$
            return null;
        }

        WSWhereCondition wc = new WSWhereCondition(filterXpaths, Util.getOperator(filterOperators), filterValues,
                WSStringPredicate.NONE, false);
        ArrayList<WSWhereItem> conditions = new ArrayList<WSWhereItem>();
        WSWhereItem item = new WSWhereItem(wc, null, null);
        conditions.add(item);

        if (filterValues != null && filterValues.equals("false")) {//$NON-NLS-1$
            String concept = filterXpaths.indexOf("/") != -1 ? filterXpaths.substring(0, filterXpaths.indexOf("/")) : filterXpaths; //$NON-NLS-1$ //$NON-NLS-2$
            BusinessConcept businessConcept = SchemaWebAgent.getInstance().getBusinessConcept(concept);
            businessConcept.load();
            String type = businessConcept.getXpathTypeMap().get(filterXpaths);
            if (type != null && type.equals("xsd:boolean")) { //$NON-NLS-1$
                wc.setStringPredicate(WSStringPredicate.OR);
                conditions.add(new WSWhereItem(new WSWhereCondition(filterXpaths, Util.getOperator("EMPTY_NULL"),//$NON-NLS-1$ 
                        filterValues, WSStringPredicate.NONE, false), null, null));
            }
        }

        if (conditions.size() == 0) {
            wi = null;
        } else {
            WSWhereOr or = new WSWhereOr(conditions.toArray(new WSWhereItem[conditions.size()]));
            wi = new WSWhereItem(null, null, or);
        }

        return wi;
    }

    public static String[] getItemBeans(String dataClusterPK, String viewPk, String criteria, int skip, int max, String sortDir,
            String sortCol, String language) throws Exception {
        WSWhereItem wi = null;
        if (criteria != null) {
            wi = buildWhereItems(criteria);
        }
        String[] results = getPort().viewSearch(
                new WSViewSearch(new WSDataClusterPK(dataClusterPK), new WSViewPK(viewPk), wi, -1, skip, max, sortCol, sortDir))
                .getStrings();
        return results;
    }

    public static String[] extractFKRefValue(String ids, String language) {
        List<String> idList = new ArrayList<String>();
        Matcher matcher = extractIdPattern.matcher(ids);
        boolean hasMatchedOnce = false;
        while (matcher.find()) {
            String id = matcher.group();
            id = id.replaceAll("\\[", "").replaceAll("\\]", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            idList.add(id);
            hasMatchedOnce = true;
        }

        if (!hasMatchedOnce) {
            throw new IllegalArgumentException(MESSAGES.getMessage(new Locale(language), "exception_fk_malform", ids)); //$NON-NLS-1$
        }

        return idList.toArray(new String[idList.size()]);
    }

    public static String buildCriteriaByIds(String[] keys, String[] ids) {
        if (keys == null || ids == null) {
            return null;
        }
        if (keys.length > 0 && keys.length < ids.length) {
            // See TMDM-5943: Id may contain '.' characters
            // (in this case: concatenate all ids[i] where i > keys.length into a single value separated with '.').
            String[] newIds = new String[keys.length];
            System.arraycopy(ids, 0, newIds, 0, keys.length - 1);
            StringBuilder idConcatenation = new StringBuilder();
            for (int i = keys.length - 1; i < ids.length; i++) {
                idConcatenation.append(ids[i]);
                if (i < ids.length - 1) {
                    idConcatenation.append('.');
                }
            }
            newIds[newIds.length - 1] = idConcatenation.toString();
            ids = newIds; // Update ids with the newly computed id values.
        }

        StringBuilder criteria = new StringBuilder();
        if (keys.length == 1 && ids.length == 1) {
            criteria.append(keys[0]).append(" ").append(EQUALS).append(" ").append(ids[0]); //$NON-NLS-1$ //$NON-NLS-2$
            return criteria.toString();
        }

        criteria.append("((").append(keys[0]).append(" ").append(EQUALS).append(" ").append(ids[0]).append(")"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        for (int i = 1; i < keys.length; i++) {
            criteria.append(" AND (").append(keys[i]).append(" ").append(EQUALS).append(" ").append(ids[i]).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        criteria.append(")"); //$NON-NLS-1$
        return criteria.toString();
    }

    public static String[] extractIdWithDots(String[] keys, String ids) throws WebBaseException {
        List<String> idList = new ArrayList<String>();
        if (keys.length == 1) {
            idList.add(ids);
        } else {
            StringTokenizer tokenizer = new StringTokenizer(ids, "."); //$NON-NLS-1$
            if (!tokenizer.hasMoreTokens()) {
                throw new WebBaseException(ExceptionConstants.ID_FORMAT_EXCEPTION, ids);
            }
            while (tokenizer.hasMoreTokens()) {
                idList.add(tokenizer.nextToken());
            }
        }
        return idList.toArray(new String[idList.size()]);
    }

    public static MetadataRepository getCurrentRepository() throws Exception {
        return ServerContext.INSTANCE.get().getMetadataRepositoryAdmin().get(Configuration.getConfiguration().getModel());
    }
}
