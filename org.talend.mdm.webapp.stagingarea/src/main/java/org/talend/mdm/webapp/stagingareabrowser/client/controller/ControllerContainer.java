/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingareabrowser.client.controller;

import org.talend.mdm.webapp.stagingareabrowser.client.view.ResultsView;
import org.talend.mdm.webapp.stagingareabrowser.client.view.SearchView;

public class ControllerContainer {

    private static SearchController searchController;

    private static ResultsController resultsController;

    private static ControllerContainer instance;

    public static void initController(SearchView searchView, ResultsView resultsView) {
        setSearchView(searchView);
        setResultsView(resultsView);
    }

    public static void setSearchView(SearchView searchView) {
        searchController = new SearchController(searchView);
    }

    public static void setResultsView(ResultsView resultsView) {
        resultsController = new ResultsController(resultsView);
    }

    private ControllerContainer() {

    }

    public static ControllerContainer get() {
        if (instance == null) {
            instance = new ControllerContainer();
        }
        return instance;
    }

    public SearchController getSearchController() {
        return searchController;
    }

    public ResultsController getResultsController() {
        return resultsController;
    }
}
