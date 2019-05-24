/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 *  %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.history.action;

import com.amalto.core.history.Action;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(JUnit4.class)
public class CompositeActionTest {

    private CompositeAction compositeAction;

    @Before
    public void setUp() {
        compositeAction = new CompositeAction(null, null, null, null);
    }

    @Test
    public void reverseXSITypeActionsTest() {
        List<Action> actions = new ArrayList<>();
        actions.add(new FieldUpdateAction(null, null, null, "User/first/Location", "J-Location-N", null, null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/first/@xsi:type", "JuniorSchool", "SeniorSchool", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/first/Name", "J-Name-N", "J-Name-N-to-O", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/first/Gaokao", null, "J-Location-N-O", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/second/Gaokao", "S-Gaokao-N", null, null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/second/@xsi:type", "SeniorSchool", "JuniorSchool", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/second/Name", "S-Name-N", "S-Name-N-O", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/second/Location", null, "S-Gaokao-N-O", null, null));

        List<Action> actualResult = compositeAction.reverseXSITypeActions(actions);
        assertEquals("User/first/Gaokao", ((FieldUpdateAction) actualResult.get(0)).getPath());
        assertEquals("User/first/Name", ((FieldUpdateAction) actualResult.get(1)).getPath());
        assertEquals("User/first/@xsi:type", ((FieldUpdateAction) actualResult.get(2)).getPath());
        assertEquals("User/first/Location", ((FieldUpdateAction) actualResult.get(3)).getPath());
        assertEquals("User/second/Location", ((FieldUpdateAction) actualResult.get(4)).getPath());
        assertEquals("User/second/Name", ((FieldUpdateAction) actualResult.get(5)).getPath());
        assertEquals("User/second/@xsi:type", ((FieldUpdateAction) actualResult.get(6)).getPath());
        assertEquals("User/second/Gaokao", ((FieldUpdateAction) actualResult.get(7)).getPath());
    }

    @Test
    public void reorderDeleteContainedTypeActionsTest() {
        List<Action> actions = new ArrayList<>();
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/code", "s", null, null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/features/actor", "sf", null, null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/features/vendor[1]", "sd", null, null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/features", null, null, null, null));

        List<Action> actualResult = compositeAction.reorderDeleteContainedTypeActions(actions);
        assertEquals("detail[2]/code", ((FieldUpdateAction) actualResult.get(0)).getPath());
        assertEquals("detail[2]/features", ((FieldUpdateAction) actualResult.get(1)).getPath());
        assertEquals("detail[2]/features/actor", ((FieldUpdateAction) actualResult.get(2)).getPath());
        assertEquals("detail[2]/features/vendor[1]", ((FieldUpdateAction) actualResult.get(3)).getPath());
    }
}