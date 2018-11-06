/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.ICoreConstants;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.DataModelHelper;
import org.talend.mdm.webapp.browserecords.server.util.CommonUtil;
import org.talend.mdm.webapp.journal.client.JournalService;
import org.talend.mdm.webapp.journal.server.service.JournalDBService;
import org.talend.mdm.webapp.journal.server.service.JournalHistoryService;
import org.talend.mdm.webapp.journal.server.service.WebServiceImp;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalParameters;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;
import org.talend.mdm.webapp.journal.shared.JournalTreeModel;

import com.amalto.core.history.exception.UnsupportedUndoPhysicalDeleteException;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.LocaleUtil;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.webapp.core.util.DataModelAccessor;
import com.amalto.webapp.core.util.Util;
import com.amalto.webapp.core.util.Webapp;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSExistsItem;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSRegexDataModelPKs;
import com.amalto.core.webservice.XtentisPort;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class JournalAction extends RemoteServiceServlet implements JournalService {

    private static final Logger LOG = Logger.getLogger(JournalAction.class);

    private JournalDBService service = new JournalDBService(new WebServiceImp());

    private static final Messages MESSAGES = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.journal.client.i18n.JournalMessages", JournalAction.class.getClassLoader()); //$NON-NLS-1$

    @Override
    public ItemBasePageLoadResult<JournalGridModel> getJournalList(JournalSearchCriteria criteria, BasePagingLoadConfigImpl load)
            throws ServiceException {
        int start = load.getOffset();
        int limit = load.getLimit();
        String sort = load.getSortDir();
        String field = load.getSortField();
        try {
            Object[] result = service.getResultListByCriteria(criteria, start, limit, sort, field);
            int totalSize = Integer.parseInt(result[0].toString());
            @SuppressWarnings("unchecked")
            List<JournalGridModel> resultList = (List<JournalGridModel>) result[1];
            return new ItemBasePageLoadResult<JournalGridModel>(resultList, load.getOffset(), totalSize);
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public JournalTreeModel getDetailTreeModel(JournalParameters parameter, String language) throws ServiceException {
        String[] idsArr = parameter.getIds().split("\\."); //$NON-NLS-1$
        try {
            EntityModel entityModel = new EntityModel();
            DataModelHelper.parseSchema(parameter.getDataModelName(), parameter.getConceptName(), entityModel, LocalUser
                    .getLocalUser().getRoles());
            return service.getDetailTreeModel(idsArr, entityModel, language);
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public JournalTreeModel getComparisionTree(JournalParameters parameter, String language) throws ServiceException {
        try {
            JournalTreeModel root;
            if (parameter.isAuth()) {
                String xmlStr = JournalHistoryService.getInstance().getComparisionTreeString(parameter);
                if (StringUtils.isNotBlank(xmlStr)) {
                    org.dom4j.Document doc = org.talend.mdm.webapp.base.server.util.XmlUtil.parseText(xmlStr);
                    EntityModel entityModel = new EntityModel();
                    DataModelHelper.parseSchema(parameter.getDataModelName(), parameter.getConceptName(), entityModel, LocalUser
                            .getLocalUser().getRoles());

                    Map<String, String[]> formatMap = CommonUtil.checkDisplayFormat(entityModel, language);
                    Map<String, Object> returnValue = CommonUtil.formatQuerylValue(formatMap, doc, entityModel,
                            parameter.getConceptName());
                    xmlStr = org.talend.mdm.webapp.base.server.util.XmlUtil.toXml(((org.dom4j.Document) returnValue
                            .get(CommonUtil.RESULT)));

                }
                root = service.getComparisionTreeModel(xmlStr);
            } else {
                root = new JournalTreeModel("root", "Document", "root"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            return root;
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (UnsupportedUndoPhysicalDeleteException unsupportedUndoException) {
            throw new ServiceException(MESSAGES.getMessage(LocaleUtil.getLocale(language), "unsupport_restore_message")); //$NON-NLS-1$
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public boolean isEnterpriseVersion() {
        return Webapp.INSTANCE.isEnterpriseVersion();
    }

    @Override
    public boolean checkDCAndDM(String dataContainer, String dataModel) {
        return Util.checkDCAndDM(dataContainer.replaceAll("#STAGING$", ""), dataModel); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public boolean checkConflict(String itemPk, String conceptName, String id) throws ServiceException {
        try {
            String ids[] = { id };
            WSDataClusterPK wddcpk = new WSDataClusterPK(itemPk);
            return Util.getPort().existsItem(new WSExistsItem(new WSItemPK(wddcpk, conceptName, ids))).is_true();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

    @Override
    public void restoreRecord(JournalParameters parameter, String language) throws ServiceException {
        Locale locale = LocaleUtil.getLocale(language);
        try {
            if (DataModelAccessor.getInstance().checkRestoreAccess(parameter.getDataModelName(),parameter.getConceptName())) {
                JournalHistoryService.getInstance().restoreRecord(parameter, language);
            } else {
                throw new ServiceException(MESSAGES.getMessage(locale, "restore_no_permissions")); //$NON-NLS-1$
            }
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (UnsupportedUndoPhysicalDeleteException unsupportedUndoException) {
            throw new ServiceException(MESSAGES.getMessage(locale, "unsupport_restore_message")); //$NON-NLS-1$
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(MESSAGES.getMessage(locale, "restore_fail")); //$NON-NLS-1$
        }
    }

    @Override
    public String getReportString(int start, int limit, String sort, String field, String language, String dataModel,
            String entity, String key, String source, String operationType, String startDate, String endDate, boolean isStrict)
            throws ServiceException {

        try {
            JournalSearchCriteria criteria = this.buildCriteria(dataModel, entity, key, source, operationType, startDate,
                    endDate, isStrict);
            Object[] result = service.getResultListByCriteria(criteria, start, limit, sort, field);
            @SuppressWarnings("unchecked")
            List<JournalGridModel> resultList = (List<JournalGridModel>) result[1];
            return this.generateEventString(resultList, language, criteria.getStartDate());
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        }

    }

    @Override
    public boolean isAdmin() {
        try {
            return LocalUser.getLocalUser().getRoles().contains(ICoreConstants.SYSTEM_ADMIN_ROLE);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.error(e.getMessage());
            }
            return false;
        }
    }

    @Override
    public boolean isJournalHistoryExist(JournalParameters parameter) throws ServiceException {
        try {
            if (isEnterpriseVersion()) {
                JournalHistoryService.getInstance().getComparisionTreeString(parameter);
                return true;
            } else {
                return true;
            }
        } catch (UnsupportedUndoPhysicalDeleteException exception) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("Undo for physical delete is not supported."); //$NON-NLS-1$
            }
            return false;
        } catch (Exception exception) {
            LOG.error(exception.getMessage(), exception);
            throw new ServiceException(exception.getMessage());
        }
    }   
    
    @Override
    public List<ItemBaseModel> getDataModels() throws Exception {
        List<ItemBaseModel> dataModels = new ArrayList<ItemBaseModel>();
        ItemBaseModel all = new ItemBaseModel();
        all.set("label", "ALL"); //$NON-NLS-1$ //$NON-NLS-2$
        all.set("key", "ALL"); //$NON-NLS-1$ //$NON-NLS-2$
        dataModels.add(all);
        XtentisPort port = com.amalto.webapp.core.util.Util.getPort();
        // port.getDataModelPKs() can only get the models user has access
        WSDataModelPK[] wsDataModelsPKs = port.getDataModelPKs(new WSRegexDataModelPKs("*")).getWsDataModelPKs(); //$NON-NLS-1$
        Map<String, XSystemObjects> xDataModelsMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_MODEL);
        for (WSDataModelPK wsDataModelsPK : wsDataModelsPKs) {
            if (!XSystemObjects.isXSystemObject(xDataModelsMap, wsDataModelsPK.getPk())) {
                ItemBaseModel bm = new ItemBaseModel();
                bm.set("label", wsDataModelsPK.getPk()); //$NON-NLS-1$
                bm.set("key", wsDataModelsPK.getPk()); //$NON-NLS-1$
                dataModels.add(bm);
            }
        }
        return dataModels;
    }

    private JournalSearchCriteria buildCriteria(String dataModel, String entity, String key, String source, String operationType,
            String startDate,
            String endDate, boolean isStrict) {
        JournalSearchCriteria criteria = new JournalSearchCriteria();
        if (!dataModel.equalsIgnoreCase("")) { //$NON-NLS-1$
            criteria.setDataModel(dataModel);
        }
        if (!entity.equalsIgnoreCase("")) { //$NON-NLS-1$
            criteria.setEntity(entity);
        }
        if (!key.equalsIgnoreCase("")) { //$NON-NLS-1$
            criteria.setKey(key);
        }
        if (!source.equalsIgnoreCase("")) { //$NON-NLS-1$
            criteria.setSource(source);
        }
        if (!operationType.equalsIgnoreCase("")) { //$NON-NLS-1$
            criteria.setOperationType(operationType);
        }
        if (!startDate.equalsIgnoreCase("")) { //$NON-NLS-1$
            criteria.setStartDate(new Date(Long.parseLong(startDate)));
        }
        if (!endDate.equalsIgnoreCase("")) { //$NON-NLS-1$
            criteria.setEndDate(new Date(Long.parseLong(endDate)));
        }
        criteria.setStrict(isStrict);
        return criteria;
    }

    private String generateEventString(List<JournalGridModel> resultList, String language, Date startDate) throws ParseException {
        StringBuilder sb = new StringBuilder("{'dateTimeFormat': 'iso8601',"); //$NON-NLS-1$
        sb.append("'events' : ["); //$NON-NLS-1$
        int i = 0;
        for (JournalGridModel model : resultList) {
            ++i;
            sb.append("{'start':'").append(this.computeTime(model.getOperationDate())).append("',"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("'title':'").append(model.getOperationDate()).append(" - ").append(model.getOperationType()).append("',"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            //            sb.append("'link':'").append("javascript:showDialog(\"").append(model.toString()).append("\")',"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Locale locale = LocaleUtil.getLocale(language);
            sb.append("'description':'").append(MESSAGES.getMessage(locale, "updatereport.timeline.label.userName")).append(":").append(model.getUserName()).append("<br>") //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$ //$NON-NLS-4$
                    .append(MESSAGES.getMessage(locale, "updatereport.timeline.label.source")).append(":").append(model.getSource()).append("<br>") //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
                    .append(MESSAGES.getMessage(locale, "updatereport.timeline.label.entity")).append(":").append(model.getEntity()).append("<br>") //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
                    .append(MESSAGES.getMessage(locale, "updatereport.timeline.label.dataContainer")).append(":").append(model.getDataContainer()).append("<br>") //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
                    .append(MESSAGES.getMessage(locale, "updatereport.timeline.label.dataModel")).append(":").append(model.getDataModel()).append("<br>") //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
                    .append(MESSAGES.getMessage(locale, "updatereport.timeline.label.key")).append(":").append(model.getKey()).append("<br>"); //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
            sb.append("'}"); //$NON-NLS-1$
            if (i != resultList.size()) {
                sb.append(","); //$NON-NLS-1$
            }
        }
        sb.append("]}"); //$NON-NLS-1$
        sb.append("@||@"); //$NON-NLS-1$
        if (resultList.size() >= 1) {
            JournalGridModel obj = resultList.get(0);
            sb.append(this.changeDataFormat(obj.getOperationDate()));
        } else {
            sb.append(this.getDateStringPlusGMT(startDate));
        }
        sb.append("@||@"); //$NON-NLS-1$
        if (resultList.size() > 0) {
            sb.append("true"); //$NON-NLS-1$
        } else {
            sb.append("false"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    private String computeTime(String src) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
        Date d = sdf.parse(src);
        Calendar c = Calendar.getInstance();
        int offset = c.getTimeZone().getRawOffset() / 3600000;
        c.setTime(d);
        c.add(Calendar.HOUR, offset);
        return sdf.format(c.getTime());
    }

    private String changeDataFormat(String src) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
        Date d = sdf.parse(src);
        return this.getDateStringPlusGMT(d);
    }

    private String getDateStringPlusGMT(Date date) {
        if (date == null) {
            date = new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.ENGLISH); //$NON-NLS-1$
        String timeStr = sdf.format(date);
        return timeStr + " GMT"; //$NON-NLS-1$
    }
}