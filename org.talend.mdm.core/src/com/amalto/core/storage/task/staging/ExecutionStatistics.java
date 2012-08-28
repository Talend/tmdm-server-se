/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.task.staging;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "execution")
public class ExecutionStatistics {

    private String id;

    private Date startDate;

    private Date endDate;

    private String runningTime;

    private double totalRecords;

    private double processedRecords;

    private int invalidRecords;

    public ExecutionStatistics() {
    }

    public ExecutionStatistics(String uuid, int processedRecords, Date startDate, Date endDate) {
        this.id = uuid;
        this.processedRecords = processedRecords;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "processed_records")
    public double getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(double processedRecords) {
        this.processedRecords = processedRecords;
    }

    @XmlElement(name = "start_date")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @XmlElement(name = "end_date")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @XmlElement(name = "running_time")
    public String getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(String runningTime) {
        this.runningTime = runningTime;
    }

    @XmlElement(name = "total_record")
    public double getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(double totalRecords) {
        this.totalRecords = totalRecords;
    }

    @XmlElement(name = "invalid_records")
    public int getInvalidRecords() {
        return invalidRecords;
    }

    public void setInvalidRecords(int invalidRecords) {
        this.invalidRecords = invalidRecords;
    }
}
