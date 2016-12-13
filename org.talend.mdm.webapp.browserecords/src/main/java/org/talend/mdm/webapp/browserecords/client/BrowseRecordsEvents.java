/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client;

import com.extjs.gxt.ui.client.event.EventType;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class BrowseRecordsEvents {

    // NOTE: We precede each code by 300 so that there is no chance we mistake
    // an event in BrowseRecordsController.handleEvent for one of these events.
    // That is, if we receive an event in BrowseRecordsController.handleEvent
    // and see that its code is 3001, we know for sure that it is one of these
    // events. This is just a precaution in case BrowseRecordsController.handleEvent
    // should receive events other than those defined here with codes that might
    // intersect these codes.

    public static final int InitFrameCode = 3001;

    public static final EventType InitFrame = new EventType(InitFrameCode);

    public static final int InitSearchContainerCode = 3002;

    public static final EventType InitSearchContainer = new EventType(InitSearchContainerCode);

    public static final int GetViewCode = 3003;

    public static final EventType GetView = new EventType(GetViewCode);

    public static final int SearchViewCode = 3004;

    public static final EventType SearchView = new EventType(SearchViewCode);

    public static final int CreateForeignKeyViewCode = 3005;

    public static final EventType CreateForeignKeyView = new EventType(CreateForeignKeyViewCode);

    public static final int ViewItemCode = 3007;

    public static final EventType ViewItem = new EventType(ViewItemCode);

    public static final int ViewForeignKeyCode = 3008;

    public static final EventType ViewForeignKey = new EventType(ViewForeignKeyCode);

    public static final int SaveItemCode = 3009;

    public static final EventType SaveItem = new EventType(SaveItemCode);

    public static final int UpdatePolymorphismCode = 30010;

    public static final EventType UpdatePolymorphism = new EventType(UpdatePolymorphismCode);

    public static final int ExecuteVisibleRuleCode = 30011;

    public static final EventType ExecuteVisibleRule = new EventType(ExecuteVisibleRuleCode);

    public static final int ViewLineageItemCode = 30012;

    public static final EventType ViewLineageItem = new EventType(ViewLineageItemCode);

    public static final int DefaultViewCode = 30013;

    public static final EventType DefaultView = new EventType(DefaultViewCode);

    public static final int BulkUpdateItemCode = 30015;

    public static final EventType BulkUpdateItem = new EventType(BulkUpdateItemCode);
}
