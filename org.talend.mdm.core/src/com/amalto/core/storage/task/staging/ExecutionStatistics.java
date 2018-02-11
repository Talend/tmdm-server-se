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

package com.amalto.core.storage.task.staging;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@XmlRootElement(name = "execution")
@ApiModel(value="execution", description="Validation execution statistics")
public class ExecutionStatistics {

    private String id;

    private String startDate;

    private String endDate;

    private String runningTime;

    private int totalRecords;

    private int processedRecords;

    private int invalidRecords;

    public ExecutionStatistics() {
    }

    @ApiModelProperty(name="id", value="Execution id")
    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(name="processed_records", value="Number of records processed by this execution")
    @XmlElement(name = "processed_records")
    public int getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(int processedRecords) {
        this.processedRecords = processedRecords;
    }

    @ApiModelProperty(name="start_date", value="This execution start date")
    @XmlElement(name = "start_date")
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @ApiModelProperty(name="end_date", value="This execution end date")
    @XmlElement(name = "end_date")
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @ApiModelProperty(name="running_time", value="How long this execution did last")
    @XmlElement(name = "running_time")
    public String getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(String runningTime) {
        this.runningTime = runningTime;
    }

    @ApiModelProperty(name="total_record", value="Total number of records")
    @XmlElement(name = "total_record")
    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    @ApiModelProperty(name="invalid_records", value="Number of invalid records")
    @XmlElement(name = "invalid_records")
    public int getInvalidRecords() {
        return invalidRecords;
    }

    public void setInvalidRecords(int invalidRecords) {
        this.invalidRecords = invalidRecords;
    }
}
