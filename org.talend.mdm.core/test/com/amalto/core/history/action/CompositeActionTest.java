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

        actions.clear();
        actions.add(new FieldUpdateAction(null, null, null, "changeName", null, "update1", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "phoneNumber", null, "22222222222", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "mobileNumber", null, "22222222222", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "addressAttorneyAF/mrMrs", "2", "null", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "addressAttorneyAF/lastName", "john", "null", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "addressAttorneyAF/@xsi:type", "addressPersonType", "addressPaymentBeneficiaryType", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "addressAttorneyAF/addressLine1", "Beijing", "eee", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "addressAttorneyAF/addressLine2", "Dongcheng", "eee", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "addressAttorneyAF/street", "wangfujing", "eee", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "addressAttorneyAF/houseNumber", "2", "33", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "addressAttorneyAF/dwellingNumber", "2", "33", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "addressAttorneyAF/postOfficeBoxNumber", "2", "33", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "addressAttorneyAF/postOfficeBoxText", "2", "33", null, null));

        actualResult = compositeAction.reverseXSITypeActions(actions);
        assertEquals("changeName", ((FieldUpdateAction) actualResult.get(0)).getPath());
        assertEquals("phoneNumber", ((FieldUpdateAction) actualResult.get(1)).getPath());
        assertEquals("mobileNumber", ((FieldUpdateAction) actualResult.get(2)).getPath());
        assertEquals("addressAttorneyAF/postOfficeBoxText", ((FieldUpdateAction) actualResult.get(3)).getPath());
        assertEquals("addressAttorneyAF/postOfficeBoxNumber", ((FieldUpdateAction) actualResult.get(4)).getPath());
        assertEquals("addressAttorneyAF/dwellingNumber", ((FieldUpdateAction) actualResult.get(5)).getPath());
        assertEquals("addressAttorneyAF/houseNumber", ((FieldUpdateAction) actualResult.get(6)).getPath());
        assertEquals("addressAttorneyAF/street", ((FieldUpdateAction) actualResult.get(7)).getPath());
        assertEquals("addressAttorneyAF/addressLine2", ((FieldUpdateAction) actualResult.get(8)).getPath());
        assertEquals("addressAttorneyAF/addressLine1", ((FieldUpdateAction) actualResult.get(9)).getPath());
        assertEquals("addressAttorneyAF/@xsi:type", ((FieldUpdateAction) actualResult.get(10)).getPath());
        assertEquals("addressAttorneyAF/lastName", ((FieldUpdateAction) actualResult.get(11)).getPath());
        assertEquals("addressAttorneyAF/mrMrs", ((FieldUpdateAction) actualResult.get(12)).getPath());

        actions.clear();
        actions.add(new FieldUpdateAction(null, null, null, "paymentBenefitType/iban", "aa", null, null, null));
        actions.add(new FieldUpdateAction(null, null, null, "paymentBenefitType/addressBanking/addressLine1", "Beijing", null, null, null));
        actions.add(new FieldUpdateAction(null, null, null, "paymentBenefitType/@xsi:type", "paymentTransferType", "paymentCashType", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "paymentBenefitType/dummy", null, "dd", null, null));

        actualResult = compositeAction.reverseXSITypeActions(actions);
        assertEquals("paymentBenefitType/dummy", ((FieldUpdateAction) actualResult.get(0)).getPath());
        assertEquals("paymentBenefitType/@xsi:type", ((FieldUpdateAction) actualResult.get(1)).getPath());
        assertEquals("paymentBenefitType/addressBanking/addressLine1", ((FieldUpdateAction) actualResult.get(2)).getPath());
        assertEquals("paymentBenefitType/iban", ((FieldUpdateAction) actualResult.get(3)).getPath());

        actions.clear();
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/Name[2]", "ContractDetailType", "ContractDetailSubType", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/Name[1]", "ContractDetailType", "ContractDetailSubType", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/@xsi:type", "ContractDetailType", "ContractDetailSubType", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/code", "super-code", "sub-code", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/features/actor", null, "sub-actor", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/features/vendor[1]", null, "sub-vendor", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/features/boolValue", null, "true", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/Price[2]", null, "yes", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/Price[1]", null, "yes", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/ReadOnlyEle[3]", null, "false", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/ReadOnlyEle[2]", null, "false", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/ReadOnlyEle[1]", null, "yes", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/Description[2]", null, "yes", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[1]/Description[1]", null, "yes", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/@xsi:type", "ContractDetailType", "ContractDetailSubType", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/code", "super-code", "sub-code", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/features/actor", null, "sub-actor", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/features/vendor[1]", null, "sub-vendor", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/features/boolValue", null, "true", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/ReadOnlyEle[3]", null, "false", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/ReadOnlyEle[2]", null, "false", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/ReadOnlyEle[1]", null, "yes", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/Description[2]", null, "yes", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "detail[2]/Description[1]", null, "yes", null, null));


        actualResult = compositeAction.reverseXSITypeActions(actions);
        assertEquals("detail[1]/Description[2]", ((FieldUpdateAction) actualResult.get(0)).getPath());
        assertEquals("detail[1]/Description[1]", ((FieldUpdateAction) actualResult.get(1)).getPath());
        assertEquals("detail[1]/ReadOnlyEle[3]", ((FieldUpdateAction) actualResult.get(2)).getPath());
        assertEquals("detail[1]/ReadOnlyEle[2]", ((FieldUpdateAction) actualResult.get(3)).getPath());
        assertEquals("detail[1]/ReadOnlyEle[1]", ((FieldUpdateAction) actualResult.get(4)).getPath());
        assertEquals("detail[1]/Price[2]", ((FieldUpdateAction) actualResult.get(5)).getPath());
        assertEquals("detail[1]/Price[1]", ((FieldUpdateAction) actualResult.get(6)).getPath());
        assertEquals("detail[1]/features/boolValue", ((FieldUpdateAction) actualResult.get(7)).getPath());
        assertEquals("detail[1]/features/vendor[1]", ((FieldUpdateAction) actualResult.get(8)).getPath());
        assertEquals("detail[1]/features/actor", ((FieldUpdateAction) actualResult.get(9)).getPath());
        assertEquals("detail[1]/code", ((FieldUpdateAction) actualResult.get(10)).getPath());
        assertEquals("detail[1]/@xsi:type", ((FieldUpdateAction) actualResult.get(11)).getPath());
        assertEquals("detail[1]/Name[2]", ((FieldUpdateAction) actualResult.get(12)).getPath());
        assertEquals("detail[1]/Name[1]", ((FieldUpdateAction) actualResult.get(13)).getPath());
        assertEquals("detail[2]/Description[2]", ((FieldUpdateAction) actualResult.get(14)).getPath());
        assertEquals("detail[2]/Description[1]", ((FieldUpdateAction) actualResult.get(15)).getPath());
        assertEquals("detail[2]/ReadOnlyEle[3]", ((FieldUpdateAction) actualResult.get(16)).getPath());
        assertEquals("detail[2]/ReadOnlyEle[2]", ((FieldUpdateAction) actualResult.get(17)).getPath());
        assertEquals("detail[2]/ReadOnlyEle[1]", ((FieldUpdateAction) actualResult.get(18)).getPath());
        assertEquals("detail[2]/features/boolValue", ((FieldUpdateAction) actualResult.get(19)).getPath());
        assertEquals("detail[2]/features/vendor[1]", ((FieldUpdateAction) actualResult.get(20)).getPath());
        assertEquals("detail[2]/features/actor", ((FieldUpdateAction) actualResult.get(21)).getPath());
        assertEquals("detail[2]/code", ((FieldUpdateAction) actualResult.get(22)).getPath());
        assertEquals("detail[2]/@xsi:type", ((FieldUpdateAction) actualResult.get(23)).getPath());
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

    @Test
    public void removeXSITypeAndNullValueActionTest(){
        List<Action> actions = new ArrayList<>();
        actions.add(new FieldUpdateAction(null, null, null, "User/first/Location", "J-Location-N", null, null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/first/@xsi:type", "JuniorSchool", "SeniorSchool", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/first/Name", "J-Name-N", "J-Name-N-to-O", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/first/Gaokao", null, "J-Location-N-O", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/first", null, null, null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/second/Gaokao", "S-Gaokao-N", null, null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/second/@xsi:type", "SeniorSchool", "JuniorSchool", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/second/Name", "S-Name-N", "S-Name-N-O", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/second/Location", null, "S-Gaokao-N-O", null, null));
        actions.add(new FieldUpdateAction(null, null, null, "User/second", null, null, null, null));

        CompositeAction compositeAction = new CompositeAction(null, null, null, actions);
        compositeAction.removeXSITypeAndNullValueAction();
        List<Action> actualResult = compositeAction.getActions();
        assertEquals("User/first/Location", ((FieldUpdateAction)actualResult.get(0)).getPath());
        assertEquals("User/first/@xsi:type", ((FieldUpdateAction)actualResult.get(1)).getPath());
        assertEquals("User/first/Name", ((FieldUpdateAction)actualResult.get(2)).getPath());
        assertEquals("User/first/Gaokao", ((FieldUpdateAction)actualResult.get(3)).getPath());
        assertEquals("User/second/Gaokao", ((FieldUpdateAction)actualResult.get(4)).getPath());
        assertEquals("User/second/@xsi:type", ((FieldUpdateAction)actualResult.get(5)).getPath());
        assertEquals("User/second/Name", ((FieldUpdateAction)actualResult.get(6)).getPath());
        assertEquals("User/second/Location", ((FieldUpdateAction)actualResult.get(7)).getPath());
    }
}