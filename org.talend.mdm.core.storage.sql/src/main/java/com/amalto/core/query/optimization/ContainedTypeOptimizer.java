/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */
package com.amalto.core.query.optimization;

import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.StringConstant;
import com.amalto.core.query.user.VisitorAdapter;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;

public class ContainedTypeOptimizer implements Optimizer {

    private static final ContainedTypeOptimization CONTAINED_OPTIMIZATION = new ContainedTypeOptimization();

    public void optimize(Select select) {
        synchronized (CONTAINED_OPTIMIZATION) {
            select.getSelectedFields().forEach(field -> {
                if (field instanceof ContainedTypeFieldMetadata) {
                    field.accept(CONTAINED_OPTIMIZATION);
                }
            });
        }
    }

    private static class ContainedTypeOptimization extends VisitorAdapter<Select> {

        @Override
        public Select visit(StringConstant constant) {
            if (constant.getValue().isEmpty()) {
                constant = new StringConstant(StringUtils.EMPTY);
            }
            return null;
        }
    }
}
