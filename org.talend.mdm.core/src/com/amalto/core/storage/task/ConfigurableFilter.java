/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */
package com.amalto.core.storage.task;

import java.util.ArrayList;
import java.util.List;

import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.Select;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.amalto.core.query.user.UserQueryBuilder.*;
import static com.amalto.core.query.user.UserStagingQueryBuilder.status;

public class ConfigurableFilter implements Filter {

    private final List<String> concepts;

    private final List<String> statusCodes;

    private final String startDate;

    private final String endDate;

    public ConfigurableFilter(Document filterConfiguration) {
        /*
        <config>
             <status-codes>
                 <code>500</code>
                 <code>400</code>
             </status-codes>
             <concepts>
                 <concept>Product</concept>
                 <concept>Store</concept>
                 <concept>ProductFamily</concept>
             </concepts>
        </config>
        */
        String startDate = null;
        String endDate = null;
        List<String> statusCodes = new ArrayList<String>();
        List<String> concepts = new ArrayList<String>();
        Element filterEl = filterConfiguration.getDocumentElement();
        NodeList fields = filterEl.getChildNodes();
        for (int i = 0; i < fields.getLength(); i++) {
            Node node = fields.item(i);
            if (node instanceof Element) {
                Element el = (Element) node;
                if ("start-date".equals(el.getNodeName())) { //$NON-NLS-1$
                    startDate = el.getTextContent();
                }
                if ("end-date".equals(el.getNodeName())) { //$NON-NLS-1$
                    endDate = el.getTextContent();
                }

                if ("status-codes".equals(el.getNodeName())) { //$NON-NLS-1$
                    NodeList codeList = el.getChildNodes();
                    for (int j = 0; j < codeList.getLength(); j++) {
                        Node code = codeList.item(j);
                        if (code instanceof Element && "code".equals(code.getNodeName())) { //$NON-NLS-1$
                            statusCodes.add(code.getTextContent());
                        }
                    }
                }

                if ("concepts".equals(el.getNodeName())) { //$NON-NLS-1$
                    NodeList conceptList = el.getChildNodes();
                    for (int k = 0; k < conceptList.getLength(); k++) {
                        Node concept = conceptList.item(k);
                        if (concept instanceof Element && "concept".equals(concept.getNodeName())) { //$NON-NLS-1$
                            concepts.add(concept.getTextContent());
                        }
                    }
                }
            }
        }
        this.startDate = startDate;
        this.endDate = endDate;
        this.statusCodes = statusCodes;
        this.concepts = concepts;
    }

    @Override
    public Select doFilter(Filterable task, ComplexTypeMetadata type) {
        Condition filterCondition;
        if (!statusCodes.isEmpty()) {
            Condition condition = null;
            for (String status : statusCodes) {
                if (condition == null) {
                    condition = eq(status(), status);
                } else {
                    condition = or(condition, eq(status(), status));
                }
                if (StagingConstants.NEW.equals(status)) {
                    condition = or(condition, or(isNull(status()), eq(status(), status)));
                }
            }
            filterCondition = condition;
        } else {
            filterCondition = task.getDefaultFilter();
        }
        // Date condition
        Condition start = null;
        Condition end = null;
        if (startDate != null && startDate.trim().length() > 0) {
            start = gte(timestamp(), startDate);
        }
        if (endDate != null && endDate.trim().length() > 0) {
            end = lt(timestamp(), endDate);
        }
        Condition dateCondition = null;
        if (start != null && end != null) {
            dateCondition = and(gte(timestamp(), startDate), lt(timestamp(), endDate));
        } else if (start != null) {
            dateCondition = start;
        } else if (end != null) {
            dateCondition = end;
        }
        filterCondition = (dateCondition == null) ? filterCondition : and(filterCondition, dateCondition);
        // Return built select clause
        return from(type).where(filterCondition).getSelect();
    }

    @Override
    public boolean exclude(ComplexTypeMetadata type) {
        return !concepts.isEmpty() && !concepts.contains(type.getName());
    }
}
