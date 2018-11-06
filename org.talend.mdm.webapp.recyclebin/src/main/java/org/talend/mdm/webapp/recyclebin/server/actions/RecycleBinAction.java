/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.server.actions;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.amalto.core.server.ServerContext;
import com.amalto.webapp.core.bean.Configuration;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.server.util.CommonUtil;
import org.talend.mdm.webapp.recyclebin.client.RecycleBinService;
import org.talend.mdm.webapp.recyclebin.shared.DroppedItemBeforeDeletingException;
import org.talend.mdm.webapp.recyclebin.shared.ItemsTrashItem;
import org.talend.mdm.webapp.recyclebin.shared.NoPermissionException;

import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.util.BeforeDeletingErrorException;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.LocaleUtil;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.core.webservice.WSConceptKey;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSDroppedItem;
import com.amalto.core.webservice.WSDroppedItemPK;
import com.amalto.core.webservice.WSDroppedItemPKArray;
import com.amalto.core.webservice.WSExistsItem;
import com.amalto.core.webservice.WSFindAllDroppedItemsPKs;
import com.amalto.core.webservice.WSGetBusinessConceptKey;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSLoadDroppedItem;
import com.amalto.core.webservice.WSRecoverDroppedItem;
import com.amalto.core.webservice.WSRemoveDroppedItem;
import com.amalto.webapp.core.dmagent.SchemaWebAgent;
import com.amalto.webapp.core.util.DataModelAccessor;
import com.amalto.webapp.core.util.Util;
import com.amalto.webapp.core.util.Webapp;

public class RecycleBinAction implements RecycleBinService {

    private static final Logger LOG = Logger.getLogger(RecycleBinAction.class);

    private static final Messages MESSAGES = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.recyclebin.client.i18n.RecycleBinMessages", RecycleBinAction.class.getClassLoader()); //$NON-NLS-1$

    @Override
    public ItemBasePageLoadResult<ItemsTrashItem> getTrashItems(String regex, BasePagingLoadConfigImpl load)
            throws ServiceException {
        try {
            if (regex == null || regex.length() == 0) {
                regex = ""; //$NON-NLS-1$
            }
            regex = regex.replaceAll("\\*", "");//$NON-NLS-1$//$NON-NLS-2$
            regex = ".*" + regex + ".*";//$NON-NLS-1$//$NON-NLS-2$
            List<ItemsTrashItem> li = new ArrayList<ItemsTrashItem>();
            WSDroppedItemPKArray pks = Util.getPort().findAllDroppedItemsPKs(new WSFindAllDroppedItemsPKs(regex));
            WSDroppedItemPK[] items = pks.getWsDroppedItemPK();
            for (WSDroppedItemPK pk : items) {
                WSDroppedItem wsItem = Util.getPort().loadDroppedItem(new WSLoadDroppedItem(pk));
                String conceptName = wsItem.getConceptName();
                String modelName = Configuration.getConfiguration().getCluster();
                if (modelName != null) {
                    // TODO Remove isEnterprise
                    // For enterprise version we check the user roles first, if one user don't have read permission on a
                    // DataModel Object, then ignore it
                    if (Webapp.INSTANCE.isEnterpriseVersion()
                            && !LocalUser.getLocalUser().userCanRead(DataModelPOJO.class, modelName)) {
                        continue;
                    }
                    if (!Webapp.INSTANCE.isEnterpriseVersion()
                            || (DataModelAccessor.getInstance().checkReadAccess(modelName, conceptName))) {
                        MetadataRepository repository = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin().get(modelName);
                        ItemsTrashItem item = WS2POJO(wsItem, repository, (String) load.get("language")); //$NON-NLS-1$
                        li.add(item);
                    }
                }
            }
            List<ItemsTrashItem> sublist = new ArrayList<ItemsTrashItem>();
            int start = load.getOffset(), limit = load.getLimit();
            if (li.size() > 0) {
                start = start < li.size() ? start : li.size() - 1;
                int end = li.size() < (start + limit) ? li.size() - 1 : (start + limit - 1);
                for (int i = start; i < end + 1; i++) {
                    sublist.add(li.get(i));
                }
            }
            return new ItemBasePageLoadResult<ItemsTrashItem>(sublist, load.getOffset(), li.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

    // TODO Move to XConverter
    private static ItemsTrashItem WS2POJO(WSDroppedItem item, MetadataRepository repository, String language) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); //$NON-NLS-1$
        String projection = item.getProjection();
        String[] values = org.talend.mdm.webapp.recyclebin.server.actions.Util.getItemNameByProjection(item.getConceptName(),
                projection, repository, language);
        return new ItemsTrashItem(item.getConceptName(), values[1],
                Util.joinStrings(item.getIds(), "."), values[0] != null ? values[0] : "", df.format(new Date(//$NON-NLS-1$ //$NON-NLS-2$
                        item.getInsertionTime())), item.getInsertionUserName(), item.getWsDataClusterPK().getPk(),
                item.getProjection(), item.getUniqueId());
    }

    @Override
    public boolean isEntityPhysicalDeletable(String conceptName) throws ServiceException {
        try {
            boolean isDeletable = !SchemaWebAgent.getInstance().isEntityDenyPhysicalDeletable(conceptName);
            if (!isDeletable) {
                throw new NoPermissionException();
            } else {
                return true;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

    @Override
    public synchronized String removeDroppedItem(String clusterName, String modelName, String conceptName, String ids, String language)
            throws ServiceException {
        try {
            WSGetBusinessConceptKey conceptKey = new WSGetBusinessConceptKey(new WSDataModelPK(modelName), conceptName);
            WSConceptKey key = CommonUtil.getPort().getBusinessConceptKey(conceptKey);
            String[] ids1 = CommonUtil.extractIdWithDots(key.getFields(), ids);
            WSDataClusterPK dataClusterPK = new WSDataClusterPK(clusterName);
            WSItemPK wsItemPK = new WSItemPK(dataClusterPK, conceptName, ids1);
            WSDroppedItemPK wsDroppedItemPK = new WSDroppedItemPK(wsItemPK, "/"); //$NON-NLS-1$
            WSRemoveDroppedItem wsRemoveDroppedItem = new WSRemoveDroppedItem(wsDroppedItemPK);
            Util.getPort().removeDroppedItem(wsRemoveDroppedItem);
            Locale locale = LocaleUtil.getLocale(language);
            return MESSAGES.getMessage(locale, "delete_process_validation_success"); //$NON-NLS-1$;
        } catch (RemoteException e) {
            if (e.getCause() != null && BeforeDeletingErrorException.class.isInstance(e.getCause())) {
                BeforeDeletingErrorException exception = (BeforeDeletingErrorException) e.getCause();
                throw new DroppedItemBeforeDeletingException(exception.getMessageType(), exception.getMessage());
            }
            throw new ServiceException(e.getLocalizedMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

    @Override
    public boolean checkConflict(String clusterName, String conceptName, String id) throws ServiceException {
        try {
            String ids[] = { id };
            WSDataClusterPK dataClusterPK = new WSDataClusterPK(clusterName);
            return Util.getPort().existsItem(new WSExistsItem(new WSItemPK(dataClusterPK, conceptName, ids))).is_true();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

    @Override
    public synchronized void recoverDroppedItem(String clusterName, String modelName, String conceptName, String ids) throws ServiceException {
        try {
            if (Webapp.INSTANCE.isEnterpriseVersion()
                    && !DataModelAccessor.getInstance().checkRestoreAccess(modelName, conceptName)) {
                throw new NoPermissionException();
            }
            WSGetBusinessConceptKey conceptKey = new WSGetBusinessConceptKey(new WSDataModelPK(modelName), conceptName);
            WSConceptKey key = CommonUtil.getPort().getBusinessConceptKey(conceptKey);
            String[] ids1 = CommonUtil.extractIdWithDots(key.getFields(), ids);
            WSDataClusterPK wsDataClusterPK = new WSDataClusterPK(clusterName);
            WSItemPK wsItemPK = new WSItemPK(wsDataClusterPK, conceptName, ids1);
            WSDroppedItemPK wsDroppedItemPK = new WSDroppedItemPK(wsItemPK, "/"); //$NON-NLS-1$
            WSRecoverDroppedItem recoverDroppedItem = new WSRecoverDroppedItem(wsDroppedItemPK);
            Util.getPort().recoverDroppedItem(recoverDroppedItem);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }
}
