/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.talend.mdm.webapp.stagingarea.control.shared.event.ModelEvent;

import java.io.Serializable;

public class StagingContainerModel extends AbstractHasModelEvents implements IsSerializable, Serializable {

    private static final long serialVersionUID = -9135215288938203541L;

    private String            dataContainer;

    private String            dataModel;

    private int               invalidRecords;

    private int               totalRecords;

    private int               validRecords;

    private int               waitingValidationRecords;

    public StagingContainerModel() {
        super();
    }

    public String getDataContainer() {
        return dataContainer;
    }

    public void setDataContainer(String dataContainer) {
        this.dataContainer = dataContainer;
        notifyHandlers(new ModelEvent(ModelEvent.Types.CONTAINER_MODEL_CHANGED, this));
    }

    public String getDataModel() {
        return dataModel;
    }

    public void setDataModel(String dataModel) {
        this.dataModel = dataModel;
        notifyHandlers(new ModelEvent(ModelEvent.Types.CONTAINER_MODEL_CHANGED, this));
    }

    public int getInvalidRecords() {
        return invalidRecords;
    }

    public void setInvalidRecords(int invalidRecords) {
        this.invalidRecords = invalidRecords;
        notifyHandlers(new ModelEvent(ModelEvent.Types.CONTAINER_MODEL_CHANGED, this));
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
        notifyHandlers(new ModelEvent(ModelEvent.Types.CONTAINER_MODEL_CHANGED, this));
    }

    public int getValidRecords() {
        return validRecords;
    }

    public void setValidRecords(int validRecords) {
        this.validRecords = validRecords;
        notifyHandlers(new ModelEvent(ModelEvent.Types.CONTAINER_MODEL_CHANGED, this));
    }

    public int getWaitingValidationRecords() {
        return waitingValidationRecords;
    }

    public void setWaitingValidationRecords(int waitingValidationRecords) {
        this.waitingValidationRecords = waitingValidationRecords;
        notifyHandlers(new ModelEvent(ModelEvent.Types.CONTAINER_MODEL_CHANGED, this));
    }
}