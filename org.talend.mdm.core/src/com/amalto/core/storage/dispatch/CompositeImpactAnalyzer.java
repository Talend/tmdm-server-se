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

package com.amalto.core.storage.dispatch;

import org.talend.mdm.commmon.metadata.compare.Change;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class CompositeImpactAnalyzer implements ImpactAnalyzer {
    private final List<ImpactAnalyzer> analyzers;

    public CompositeImpactAnalyzer(List<ImpactAnalyzer> analyzers) {
        this.analyzers = analyzers;
    }

    @Override
    public Map<Impact, List<Change>> analyzeImpacts(Compare.DiffResults diffResult) {
        Map<Impact, List<Change>> impacts = new HashMap<Impact, List<Change>>();
        for (Impact impact : Impact.values()) {
            impacts.put(impact, new LinkedList<Change>());
        }
        for (ImpactAnalyzer analyzer : analyzers) {
            Map<Impact, List<Change>> current = analyzer.analyzeImpacts(diffResult);
            for (Map.Entry<Impact, List<Change>> entry : current.entrySet()) {
                impacts.get(entry.getKey()).addAll(entry.getValue());
            }
        }
        return impacts;
    }
}
