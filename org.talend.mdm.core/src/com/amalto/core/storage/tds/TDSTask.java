// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.tds;

import java.util.List;
import java.util.Map;

public class TDSTask {

    public String id;

    public Integer version;

    public String externalID;

    public Boolean consumed;

    public Map<String, Object> record;

    public List<SourceRecord> sourceRecords;

    public static class SourceRecord {

        public Map<String, Object> record;

    }

}
