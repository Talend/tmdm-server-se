/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query.optimization;

import com.amalto.core.query.user.metadata.Timestamp;
import com.amalto.core.util.Util;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import com.amalto.core.query.user.*;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import org.apache.log4j.Logger;

import java.util.List;

public class UpdateReportOptimizer implements Optimizer {

    private static final Logger LOGGER = Logger.getLogger(UpdateReportOptimizer.class);

    private static final UpdateReportDataModel REPORT_DATA_MODEL = new UpdateReportDataModel();

    public void optimize(Select select) {
        List<ComplexTypeMetadata> types = select.getTypes();
        if (types.size() == 1) {
            ComplexTypeMetadata mainType = types.get(0);
            if ("Update".equals(mainType.getName())) { //$NON-NLS-1$
                String dataModelName;
                synchronized (REPORT_DATA_MODEL) {
                    dataModelName = select.accept(REPORT_DATA_MODEL); // 1- Try to get data model name in query.
                }
                if (dataModelName == null) {
                    try {
                        dataModelName = Util.getUserDataModel(); // 2- Try to get data model name from current user.
                    } catch (Exception e) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Could not access current user data model.", e);
                        }
                    } catch (NoClassDefFoundError e) {
                        LOGGER.error("Could not get current user data model (missing class issue).");
                    }
                }
                if (dataModelName != null) {
                    Server server = ServerContext.INSTANCE.get();
                    MetadataRepository repository = server.getMetadataRepositoryAdmin().get(dataModelName);
                    // Query SuperType
                    if (repository != null) {
                        select.setCondition(select.accept(new UpdateReportTransformer(repository)));    
                    }
                    
                } else if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Can't optimize update report query: no data model information in query.");
                }
            }
        }
    }

    private static class UpdateReportDataModel extends VisitorAdapter<String> {

        private boolean isDataModel;

        public UpdateReportDataModel() {
        }

        @Override
        public String visit(Select select) {
            Condition condition = select.getCondition();
            if (condition != null) {
                return condition.accept(this);
            } else {
                return null;
            }
        }

        @Override
        public String visit(Field field) {
            if("DataModel".equals(field.getFieldMetadata().getName())) { //$NON-NLS-1$
                isDataModel = true;
            }
            return null;
        }

        @Override
        public String visit(StringConstant constant) {
            if (isDataModel) {
                isDataModel = false; // Reset for reuse.
                return constant.getValue();
            }
            return null;
        }

        @Override
        public String visit(LongConstant constant) {
            return null;
        }

        @Override
        public String visit(Isa isa) {
            return null;
        }

        @Override
        public String visit(UnaryLogicOperator condition) {
            return condition.getCondition().accept(this);
        }

        @Override
        public String visit(IsEmpty isEmpty) {
            return null;
        }

        @Override
        public String visit(IsNull isNull) {
            return null;
        }

        @Override
        public String visit(NotIsEmpty notIsEmpty) {
            return null;
        }

        @Override
        public String visit(NotIsNull notIsNull) {
            return null;
        }

        @Override
        public String visit(BinaryLogicOperator condition) {
            String result = condition.getLeft().accept(this);
            if (result != null) {
                return result;
            } else {
                return condition.getRight().accept(this);
            }
        }

        @Override
        public String visit(Timestamp timestamp) {
            return null;
        }

        @Override
        public String visit(Compare condition) {
            String result = condition.getLeft().accept(this);
            if (result != null) {
                return result;
            } else {
                return condition.getRight().accept(this);
            }
        }

        @Override
        public String visit(Range range) {
            return null;
        }

        @Override
        public String visit(Condition condition) {
            return null;
        }

        @Override
        public String visit(FullText fullText) {
            return null;
        }

        @Override
        public String visit(FieldFullText fullText) {
            return null;
        }
    }

    private static class UpdateReportTransformer extends VisitorAdapter<Condition> {

        private final MetadataRepository repository;

        private boolean isUpdateReportEntityField;

        private String updateReportEntityTypeName;

        public UpdateReportTransformer(MetadataRepository repository) {
            this.repository = repository;
        }

        @Override
        public Condition visit(Select select) {
            Condition condition = select.getCondition();
            if (condition != null) {
                return condition.accept(this);
            } else {
                return null;
            }
        }

        @Override
        public Condition visit(Field field) {
            if("Concept".equals(field.getFieldMetadata().getName())) { //$NON-NLS-1$
                isUpdateReportEntityField = true;
            }
            return null;
        }

        @Override
        public Condition visit(StringConstant constant) {
            if (isUpdateReportEntityField) {
                updateReportEntityTypeName = constant.getValue();
            }
            return null;
        }

        @Override
        public Condition visit(Isa isa) {
            return isa;
        }

        @Override
        public Condition visit(UnaryLogicOperator condition) {
            Condition originalCondition = condition.getCondition();
            Condition newCondition = originalCondition.accept(this);
            if (originalCondition != newCondition) {
                return new UnaryLogicOperator(newCondition, condition.getPredicate());
            } else {
                return condition;
            }
        }

        @Override
        public Condition visit(IsEmpty isEmpty) {
            return isEmpty;
        }

        @Override
        public Condition visit(IsNull isNull) {
            return isNull;
        }

        @Override
        public Condition visit(NotIsEmpty notIsEmpty) {
            return notIsEmpty;
        }

        @Override
        public Condition visit(NotIsNull notIsNull) {
            return notIsNull;
        }

        @Override
        public Condition visit(BinaryLogicOperator condition) {
            Condition originalLeft = condition.getLeft();
            Condition newLeft = originalLeft.accept(this);
            Condition originalRight = condition.getRight();
            Condition newRight = originalRight.accept(this);
            if(newLeft != originalLeft || newRight != originalRight) {
                return new BinaryLogicOperator(newLeft, condition.getPredicate(), newRight);
            } else {
                return condition;
            }
        }

        @Override
        public Condition visit(Timestamp timestamp) {
            return null;
        }

        @Override
        public Condition visit(Compare condition) {
            Predicate predicate = condition.getPredicate();
            condition.getLeft().accept(this);
            if (isUpdateReportEntityField) {
                condition.getRight().accept(this);
                ComplexTypeMetadata type = repository.getComplexType(updateReportEntityTypeName);
                Condition current = condition;
                if (type != null) {
                    for (TypeMetadata superType : type.getSuperTypes()) {
                        current =  new BinaryLogicOperator(current, Predicate.OR, new Compare(condition.getLeft(), predicate, new StringConstant(superType.getName())));
                    }
                    for (TypeMetadata subType : type.getSubTypes()) {
                        current = new BinaryLogicOperator(current, Predicate.OR, new Compare(condition.getLeft(), predicate, new StringConstant(subType.getName())));
                    }    
                }                
                isUpdateReportEntityField = false;
                updateReportEntityTypeName = null;
                return current;
            } else {
                return condition;
            }
        }

        @Override
        public Condition visit(Range range) {
            return range;
        }

        @Override
        public Condition visit(Condition condition) {
            return condition;
        }

        @Override
        public Condition visit(FullText fullText) {
            return fullText;
        }

        @Override
        public Condition visit(FieldFullText fullText) {
            return fullText;
        }
    }
}
