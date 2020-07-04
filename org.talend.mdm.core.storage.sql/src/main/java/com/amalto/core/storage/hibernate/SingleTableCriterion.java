/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.SQLCriterion;
import org.hibernate.type.Type;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

class SingleTableCriterion extends SQLCriterion {

    private static final long serialVersionUID = -1416041576110629822L;

    private static final Logger LOGGER = LogManager.getLogger(SingleTableCriterion.class);

    private final Criteria typeCriteria;

    private final ComplexTypeMetadata containedTypeMetadata;

    private final TableResolver resolver;

    SingleTableCriterion(Criteria typeSelectionCriteria, TableResolver resolver, ComplexTypeMetadata containedTypeMetadata) {
        super(StringUtils.EMPTY, new Object[0], new Type[0]);
        this.typeCriteria = typeSelectionCriteria;
        this.resolver = resolver;
        this.containedTypeMetadata = containedTypeMetadata;
    }

    /**
     * The destinction is to return a special WHERE condition to limit the return results with inheritance structure.
     * Hibernate has to join the major table with the result of a subselect which uses a union to get all matching
     * records from the <b> superEntity </b> and <b> subEntity </b> tables. The superentity is now also an entity and
     * you can, therefore, use it to define a relationship between the <b> superEntity </b> and the <b> subEntity </b>
     * entity, with the WHERE condition, Hibernate will map the <b> superEntity </b> to return.
     *
     * @return " this_.x_id in (select x_id from Entity)";
     */
    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        String containingTypeAlias = criteriaQuery.getSQLAlias(typeCriteria);// this_
        String containingType = resolver.get(containedTypeMetadata);// Party
        List<String> keyList = containedTypeMetadata.getKeyFields().stream().map((item) -> resolver.get(item))
                .collect(Collectors.<String> toList());

        StringBuilder builder = new StringBuilder();

        builder.append(getProjectionFragment(containingTypeAlias, keyList)).append(" IN (SELECT ") //$NON-NLS-1$
                .append(getProjectionFragment("", keyList)).append(" FROM ").append(containingType).append(")"); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The Where clause is : " + builder.toString());
        }
        return builder.toString();
    }

    private String getProjectionFragment(String alias, List<String> keyList) {
        StringBuilder builder = new StringBuilder();
        for (String keyItem : keyList) {
            builder.append(alias).append("".equals(alias) ? "" : ".").append(keyItem).append(","); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn("The fragment of this projection columns is " + builder.toString());
        }

        return StringUtils.removeEnd(builder.toString(), ","); //$NON-NLS-1$
    }
}
