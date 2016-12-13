/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.codehaus.jettison.json.JSONObject;
import org.talend.mdm.commmon.util.datamodel.management.BusinessConcept;
import org.talend.mdm.commmon.util.datamodel.management.ReusableType;

import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSGetItemPKsByCriteria;
import com.amalto.core.webservice.WSGetItemPKsByFullCriteria;
import com.amalto.core.webservice.WSItemPKsByCriteriaResponse;
import com.amalto.core.webservice.WSItemPKsByCriteriaResponseResults;
import com.amalto.webapp.core.bean.Configuration;
import com.amalto.webapp.core.dmagent.SchemaWebAgent;
import com.amalto.webapp.core.util.Util;

public class ExportingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        org.apache.log4j.Logger.getLogger(this.getClass()).info("SERVLET exporting for excel "); //$NON-NLS-1$

        DateFormat df = new SimpleDateFormat("dd-MM-yyyy"); //$NON-NLS-1$
        response.reset();
        response.setContentType("application/vnd.ms-excel"); //$NON-NLS-1$
        String theReportFile = "Reporting_" + df.format(new Date()) + ".xls"; //$NON-NLS-1$ //$NON-NLS-2$
        response.setHeader("Content-Disposition", "attachment; filename=\"" + theReportFile + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet"); //$NON-NLS-1$
        sheet.setDefaultColumnWidth((short) 20);

        String parametersValues = request.getParameter("params"); //$NON-NLS-1$
        if (parametersValues == null) {
            parametersValues = ""; //$NON-NLS-1$
        }

        org.apache.log4j.Logger.getLogger(this.getClass()).debug("params =" + parametersValues); //$NON-NLS-1$

        boolean splitEnd = false;
        String tmpSplit = parametersValues;
        Vector<String> paramVector = new Vector<String>();
        while (!splitEnd) {
            int indexMatch = tmpSplit.indexOf("###"); //$NON-NLS-1$
            if (indexMatch == -1) {
                paramVector.add(tmpSplit);
                splitEnd = true;
            } else {
                if (indexMatch > 0) {
                    String tmpParam = tmpSplit.substring(0, indexMatch);
                    paramVector.add(tmpParam);
                } else {
                    paramVector.add(""); //$NON-NLS-1$
                }

                if (indexMatch + 3 >= tmpSplit.length()) {
                    tmpSplit = ""; //$NON-NLS-1$
                } else {
                    tmpSplit = tmpSplit.substring(indexMatch + 3);
                }
            }
        }

        // String []parameters = parametersValues.split("###");
        String[] parameters = new String[paramVector.size()];
        for (int i = 0; i < paramVector.size(); i++) {
            parameters[i] = paramVector.get(i);
        }

        org.apache.log4j.Logger.getLogger(this.getClass()).debug("nb params =" + parameters.length); //$NON-NLS-1$

        try {
            WSDataClusterPK wsDataClusterPK = new WSDataClusterPK();
            String entity = null;
            String contentWords = null;
            String keys = null;
            Long fromDate = new Long(-1);
            Long toDate = new Long(-1);
            String fkvalue = null;
            String dataObject = null;

            if (parametersValues != null && parametersValues.length() > 0) {
                JSONObject criteria = new JSONObject(parametersValues);

                Configuration configuration = Configuration.getConfiguration();
                wsDataClusterPK.setPk(configuration.getCluster());
                entity = !criteria.isNull("entity") ? (String) criteria.get("entity") : ""; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                keys = !criteria.isNull("key") && !"*".equals(criteria.get("key")) ? (String) criteria.get("key") : ""; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                fkvalue = !criteria.isNull("fkvalue") && !"*".equals(criteria.get("fkvalue")) ? (String) criteria.get("fkvalue") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        : ""; //$NON-NLS-1$
                dataObject = !criteria.isNull("dataObject") && !"*".equals(criteria.get("dataObject")) ? (String) criteria //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .get("dataObject") : ""; //$NON-NLS-1$//$NON-NLS-2$
                contentWords = !criteria.isNull("keyWords") ? (String) criteria.get("keyWords") : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                if (!criteria.isNull("fromDate")) { //$NON-NLS-1$
                    String startDate = (String) criteria.get("fromDate"); //$NON-NLS-1$
                    SimpleDateFormat dataFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
                    java.util.Date date = dataFmt.parse(startDate);
                    fromDate = date.getTime();
                }

                if (!criteria.isNull("toDate")) { //$NON-NLS-1$
                    String endDate = (String) criteria.get("toDate"); //$NON-NLS-1$
                    SimpleDateFormat dataFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
                    java.util.Date date = dataFmt.parse(endDate);
                    toDate = date.getTime();
                }
            }

            BusinessConcept businessConcept = SchemaWebAgent.getInstance().getBusinessConcept(entity);
            Map<String, String> foreignKeyMap = businessConcept.getForeignKeyMap();
            Set<String> foreignKeyXpath = foreignKeyMap.keySet();
            Set<String> xpathes = new HashSet<String>();

            for (String path : foreignKeyXpath) {
                String dataObjectPath = foreignKeyMap.get(path);
                if (dataObjectPath.indexOf(dataObject) != -1) {
                    xpathes.add(path.substring(1));
                }
            }

            List<String> types = SchemaWebAgent.getInstance().getBindingType(businessConcept.getE());
            for (String type : types) {
                List<ReusableType> subTypes = SchemaWebAgent.getInstance().getMySubtypes(type);
                for (ReusableType reusableType : subTypes) {
                    Map<String, String> fks = SchemaWebAgent.getInstance().getReferenceEntities(reusableType, dataObject);
                    Collection<String> fkPaths = fks != null ? fks.keySet() : null;
                    for (String fkpath : fkPaths) {
                        if (fks.get(fkpath).indexOf(dataObject) != -1) {
                            xpathes.add(fkpath);
                        }
                    }
                }
            }

            Map<String, String> inheritanceForeignKeyMap = businessConcept.getInheritanceForeignKeyMap();
            for (Map.Entry<String, String> entry : inheritanceForeignKeyMap.entrySet()) {
                if (entry.getValue().indexOf(dataObject) != -1) {
                    xpathes.add(entry.getKey().substring(1));
                }
            }

            StringBuilder keysb = new StringBuilder();
            keysb.append("$"); //$NON-NLS-1$
            keysb.append(joinSet(xpathes, ",")); //$NON-NLS-1$
            keysb.append("$"); //$NON-NLS-1$
            keysb.append(fkvalue);

            WSItemPKsByCriteriaResponse results = Util.getPort().getItemPKsByFullCriteria(
                    new WSGetItemPKsByFullCriteria(new WSGetItemPKsByCriteria(wsDataClusterPK, entity, contentWords, keysb
                            .toString(), keys, fromDate, toDate, 0, Integer.MAX_VALUE), false));

            // create a cell style
            HSSFCellStyle cs = wb.createCellStyle();
            HSSFFont f = wb.createFont();
            f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            cs.setFont(f);
            HSSFRow row = sheet.createRow((short) 0);

            if (results.getResults().length > 0) {
                row.createCell((short) 0).setCellValue("date"); //$NON-NLS-1$
                row.createCell((short) 1).setCellValue("entity"); //$NON-NLS-1$
                row.createCell((short) 2).setCellValue("key"); //$NON-NLS-1$
            }

            // set a style for these cells
            for (int i = 0; i < 3; i++) {
                row.getCell((short) i).setCellStyle(cs);
            }

            for (int i = 0; i < results.getResults().length; i++) {
                WSItemPKsByCriteriaResponseResults result = results.getResults()[i];
                if (i == 0) {
                    continue;
                }

                row = sheet.createRow((short) i);
                SimpleDateFormat dataFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
                String date = dataFmt.format(result.getDate());
                row.createCell((short) 0).setCellValue(date);
                row.createCell((short) 1).setCellValue(result.getWsItemPK().getConceptName());
                String[] ids = result.getWsItemPK().getIds();
                StringBuilder sb = new StringBuilder();

                if (ids != null) {
                    for (String id : ids) {
                        sb.append(id);
                    }
                }

                row.createCell((short) 2).setCellValue(sb.toString());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Write the output
        OutputStream out = response.getOutputStream();
        wb.write(out);
        out.close();
    }

    private String joinSet(Set<String> set, String decollator) {
        if (set == null) {
            return ""; //$NON-NLS-1$
        }
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        for (String str : set) {
            if (isFirst) {
                sb.append(str);
                isFirst = false;
                continue;
            }
            sb.append(decollator + str);
        }
        return sb.toString();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
