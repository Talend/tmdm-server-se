/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amalto.core.objects.configurationinfo.assemble.AssembleConcreteBuilder;
import com.amalto.core.objects.configurationinfo.assemble.AssembleDirector;
import com.amalto.core.objects.configurationinfo.assemble.AssembleProc;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;

@SuppressWarnings("nls")
public class RunServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public RunServlet() {
        super();
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        PrintWriter writer = resp.getWriter();
        writer.write("<html><body>");
        try {
            if ("clean".equals(action)) {
                writer.write("<p><b>Cleanup the background jobs stored in MDM</b><br/>"
                        + "Check server log output to determine when clean is completed</b></p>");

                final AssembleConcreteBuilder concreteBuilder = new AssembleConcreteBuilder();
                final AssembleDirector director = new AssembleDirector(concreteBuilder);
                director.constructCleanPart();
                AssembleProc assembleProc = concreteBuilder.getAssembleProc();

            } else if ("init".equals(action)) {
                writer.write("<p><b>Initialize the clusters and build-in data</b><br/>"
                        + "Check server output to determine when init is completed</b></p>");

                final AssembleConcreteBuilder concreteBuilder = new AssembleConcreteBuilder();
                final AssembleDirector director = new AssembleDirector(concreteBuilder);
                director.constructInitPart();
                AssembleProc assembleProc = concreteBuilder.getAssembleProc();

            } else if ("reindex".equals(action)) {
                String container = req.getParameter("container");
                if (container == null) {
                    writer.write("<p><b>Container parameter is mandatory for action " + action + "</b><br/>");
                } else {
                    StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
                    Storage storage = storageAdmin.get(container, storageAdmin.getType(container));
                    try {
                        storage.begin();
                        storage.reindex();
                        storage.commit();
                    } catch (Exception e) {
                        storage.rollback();
                        throw new RuntimeException("Could not complete reindex action.", e);
                    }
                }
            } else {
                writer.write("<p><b>Unknown action: </b>" + action + "<br/>");
            }

        } catch (Exception e) {
            writer.write("<h1>An error occurred: " + e.getLocalizedMessage() + "</h1>");
        }
        writer.write("</body></html>");

    }

}