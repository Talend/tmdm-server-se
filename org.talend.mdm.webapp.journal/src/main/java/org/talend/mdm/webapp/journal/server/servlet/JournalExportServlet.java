/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.talend.mdm.webapp.journal.server.service.JournalDBService;
import org.talend.mdm.webapp.journal.server.service.WebServiceImp;
import org.talend.mdm.webapp.journal.server.util.JournalExcel;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;

/**
 * The server side implementation of the RPC service.
 */
public class JournalExportServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(JournalExportServlet.class);

    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOG.info("Exporting excel file"); //$NON-NLS-1$
        JournalExcel journalExcel = new JournalExcel(request.getParameter("language")); //$NON-NLS-1$     
     
        response.reset();
        response.setContentType("application/vnd.ms-excel"); //$NON-NLS-1$

        response.setHeader("Content-Disposition", "attachment; filename=\"" + journalExcel.getFileName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Object[] resultArr = null;
        try {
            JournalDBService service = new JournalDBService(new WebServiceImp());
            resultArr = service.getResultListByCriteria(getCriteriaFromRequest(request), 0, -1, null, null);

            List<JournalGridModel> resultList = (List<JournalGridModel>) resultArr[1];
            int i = 1;
            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$            
            for (JournalGridModel model : resultList) {
                HSSFRow row = journalExcel.createRow((short)i++);
                row.createCell((short) 0).setCellValue(model.getDataContainer());
                row.createCell((short) 1).setCellValue(model.getDataModel());
                row.createCell((short) 2).setCellValue(model.getEntity());
                row.createCell((short) 3).setCellValue(model.getKey());
                row.createCell((short) 4).setCellValue(model.getOperationType());
                String operationTimeValue = (model.getOperationTime() == null ? "" : model.getOperationTime()); //$NON-NLS-1$
                if (!"".equals(operationTimeValue)) { //$NON-NLS-1$
                    operationTimeValue = simpleDateFormat.format(new Date(Long.parseLong(model.getOperationTime())));                    
                }
                row.createCell((short) 5).setCellValue(operationTimeValue);
                row.createCell((short) 6).setCellValue(model.getSource());
                row.createCell((short) 7).setCellValue(model.getUserName());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        response.setCharacterEncoding("utf-8"); //$NON-NLS-1$
        OutputStream out = response.getOutputStream();

        journalExcel.getWorkbook().write(out);
        out.close();
    }

    private JournalSearchCriteria getCriteriaFromRequest(HttpServletRequest request) {
        String dataModel = request.getParameter("dataModel"); //$NON-NLS-1$
        String entity = request.getParameter("entity"); //$NON-NLS-1$
        String key = request.getParameter("key"); //$NON-NLS-1$
        String source = request.getParameter("source"); //$NON-NLS-1$
        String operationType = request.getParameter("operationType"); //$NON-NLS-1$
        String startDate = request.getParameter("startDate"); //$NON-NLS-1$
        String endDate = request.getParameter("endDate"); //$NON-NLS-1$
        String isStrict = request.getParameter("isStrict"); //$NON-NLS-1$

        JournalSearchCriteria criteria = new JournalSearchCriteria();
        criteria.setDataModel(dataModel);
        criteria.setEntity(entity);
        criteria.setKey(key);
        criteria.setSource(source);
        criteria.setOperationType(operationType);
        criteria.setStrict(Boolean.parseBoolean(isStrict));

        if (startDate != null) {
            Date date = new Date();
            date.setTime(Long.valueOf(startDate));
            criteria.setStartDate(date);
        }

        if (endDate != null) {
            Date date = new Date();
            date.setTime(Long.valueOf(endDate));
            criteria.setEndDate(date);
        }

        return criteria;
    }
}