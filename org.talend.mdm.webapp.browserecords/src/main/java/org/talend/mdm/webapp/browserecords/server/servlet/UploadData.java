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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.log4j.Logger;
import org.talend.mdm.webapp.base.server.util.CommonUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.FileUtil;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.DataModelHelper;
import org.talend.mdm.webapp.browserecords.server.exception.UploadException;
import org.talend.mdm.webapp.browserecords.server.service.UploadService;
import org.talend.mdm.webapp.browserecords.server.util.UploadUtil;
import org.talend.mdm.webapp.browserecords.shared.Constants;

import com.amalto.core.save.MultiRecordsSaveException;
import com.amalto.core.util.CoreException;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.core.webservice.WSPutItemWithReport;
import com.amalto.core.webservice.WSPutItemWithReportArray;

/**
 * 
 * @author asaintguilhem
 * 
 * read excel and csv file
 */

@SuppressWarnings("serial")
public class UploadData extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(UploadData.class);

    private static final Messages MESSAGES = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages", UploadData.class.getClassLoader()); //$NON-NLS-1$

    private boolean cusExceptionFlag = false;
    

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        doPost(arg0, arg1);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        if (!FileUploadBase.isMultipartContent(request)) {
            throw new ServletException(MESSAGES.getMessage("error_upload")); //$NON-NLS-1$
        }
        // Create a new file upload handler
        DiskFileUpload upload = new DiskFileUpload();
        // Set upload parameters
        upload.setSizeThreshold(0);
        upload.setSizeMax(-1);

        PrintWriter writer = response.getWriter();
        try {
            String concept = null;
            String seperator = null;
            String textDelimiter = "\""; //$NON-NLS-1$
            String language = "en"; //$NON-NLS-1$
            Locale locale = null;
            String encoding = "utf-8";//$NON-NLS-1$
            Map<String, Boolean> headerVisibleMap = new LinkedHashMap<String, Boolean>();
            String headerString = null;
            String mandatoryField = null;
            List<String> inheritanceNodePathList = null;
            boolean headersOnFirstLine = false;
            boolean isPartialUpdate = false;
            String multipleValueSeparator = null;
            String fileType = null;
            File file = null;
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                // FIXME: should handle more than files in parts e.g. text passed as parameter
                FileItem item = iter.next();
                if (item.isFormField()) {
                    // we are not expecting any field just (one) file(s)
                    String name = item.getFieldName();
                    LOG.debug("doPost() Field: '" + name + "' - value:'" + item.getString() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
                    if (name.equals("concept")) { //$NON-NLS-1$
                        concept = item.getString();
                    } else if (name.equals("sep")) { //$NON-NLS-1$
                        seperator = item.getString();
                    } else if (name.equals("delimiter")) { //$NON-NLS-1$
                        textDelimiter = item.getString();
                    } else if (name.equals("language")) { //$NON-NLS-1$
                        locale = new Locale(item.getString());
                    } else if (name.equals("encodings")) { //$NON-NLS-1$
                        encoding = item.getString();
                    } else if (name.equals("header")) { //$NON-NLS-1$
                        headerString = item.getString();
                        List<String> headerItemList = org.talend.mdm.webapp.base.shared.util.CommonUtil.convertStrigToList(
                                headerString, Constants.FILE_EXPORT_IMPORT_SEPARATOR);
                        if (headerItemList != null) {
                            for (String headerItem : headerItemList) {
                                String[] headerItemArray = headerItem.split(Constants.HEADER_VISIBILITY_SEPARATOR);
                                headerVisibleMap.put(headerItemArray[0], Boolean.valueOf(headerItemArray[1]));
                            }
                        }
                    } else if (name.equals("mandatoryField")) { //$NON-NLS-1$
                        mandatoryField = item.getString();
                    } else if (name.equals("inheritanceNodePath")) { //$NON-NLS-1$
                        inheritanceNodePathList = org.talend.mdm.webapp.base.shared.util.CommonUtil.convertStrigToList(
                                item.getString(), Constants.FILE_EXPORT_IMPORT_SEPARATOR);
                    } else if (name.equals("headersOnFirstLine")) { //$NON-NLS-1$
                        headersOnFirstLine = "on".equals(item.getString()); //$NON-NLS-1$
                    } else if (name.equals("multipleValueSeparator")) { //$NON-NLS-1$
                        multipleValueSeparator = item.getString();
                    } else if (name.equals("isPartialUpdate")) { //$NON-NLS-1$
                        isPartialUpdate = "on".equals(item.getString()); //$NON-NLS-1$
                    }
                } else {
                    fileType = FileUtil.getFileType(item.getName());
                    file = File.createTempFile("upload", "tmp");//$NON-NLS-1$ //$NON-NLS-2$
                    LOG.debug("doPost() data uploaded in " + file.getAbsolutePath()); //$NON-NLS-1$
                    file.deleteOnExit();
                    item.write(file);
                }// if field
            }// while item
            if (!UploadUtil.isViewableXpathValid(headerVisibleMap.keySet(), concept)) {
                throw new UploadException(MESSAGES.getMessage(locale, "error_invaild_field", concept)); //$NON-NLS-1$
            }
            Set<String> mandatorySet = UploadUtil.chechMandatoryField(
                    org.talend.mdm.webapp.base.shared.util.CommonUtil.unescape(mandatoryField), headerVisibleMap.keySet());
            if (mandatorySet.size() > 0) {
                throw new UploadException(MESSAGES.getMessage(locale, "error_missing_mandatory_field")); //$NON-NLS-1$
            }
            UploadService service = generateUploadService(concept, fileType, isPartialUpdate, headersOnFirstLine,
                    headerVisibleMap, inheritanceNodePathList, multipleValueSeparator, seperator, encoding,
                    textDelimiter.charAt(0), language);

            List<WSPutItemWithReport> wsPutItemWithReportList = service.readUploadFile(file);

            putDocument(
                    new WSPutItemWithReportArray(wsPutItemWithReportList.toArray(new WSPutItemWithReport[wsPutItemWithReportList
                            .size()])), concept);
            writer.print(Constants.IMPORT_SUCCESS);
        } catch (Exception exception) {
            LOG.error(exception.getMessage(), exception);
            writer.print(extractErrorMessage(exception.getMessage()));
        } finally {
            writer.close();
        }
    }

    private void putDocument(WSPutItemWithReportArray wSPutItemWithReportArray, String concept) throws UploadException,
            ServletException {
        try {
            CommonUtil.getPort().putItemWithReportArray(wSPutItemWithReportArray);
        } catch (RemoteException exception) {
            Throwable cause = UploadUtil.getRootCause(exception);
            if (CoreException.class.isInstance(cause) && cause.getCause() != null) {
                if (MultiRecordsSaveException.class.isInstance(cause.getCause())) {
                    throw new UploadException(MESSAGES.getMessage("save_error") + " " + MESSAGES.getMessage("save_row_count", ((MultiRecordsSaveException)cause.getCause()).getRowCount() + 1) + cause.getCause().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                throw new UploadException(cause.getCause().getMessage());
            }
            throw new UploadException(cause.getMessage());
        } catch (Exception exception) {
            throw new ServletException(exception.getLocalizedMessage());
        }
    }

    protected EntityModel getEntityModel(String concept) throws Exception {
        EntityModel entityModel = new EntityModel();
        DataModelHelper.parseSchema(org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataModel(), concept,
                entityModel, LocalUser.getLocalUser().getRoles());
        return entityModel;
    }

    protected UploadService generateUploadService(String concept, String fileType, boolean isPartialUpdate, boolean headersOnFirstLine,
            Map<String, Boolean> headerVisibleMap, List<String> inheritanceNodePathList, String multipleValueSeparator,
            String seperator, String encoding, char textDelimiter, String language) throws Exception {
        return new UploadService(getEntityModel(concept), fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
    }

    public static String extractErrorMessage(String errorMsg) {
        String saveExceptionString = "com.amalto.core.save.SaveException: Exception occurred during save: "; //$NON-NLS-1$
        int saveExceptionIndex = errorMsg.indexOf(saveExceptionString);
        if (saveExceptionIndex > -1) {
            errorMsg = MESSAGES.getMessage("save_error") + errorMsg.substring(saveExceptionIndex + saveExceptionString.length()); //$NON-NLS-1$
        }

        return errorMsg;
    }
}
