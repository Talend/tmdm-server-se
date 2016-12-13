/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.record;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;


public class DataRecordDefaultWriterTest extends TestCase {
    
    public void testWrite() throws IOException {
        String xml = "<referenceField>[111][222][444]</referenceField>"; //$NON-NLS-1$

        FieldMetadata fieldMetadata = new ReferenceFieldMetadata(null, true, false, true, "referenceField", null, null, Collections.<FieldMetadata>emptyList(), StringUtils.EMPTY, true, true, null, null, null, null, StringUtils.EMPTY, StringUtils.EMPTY); //$NON-NLS-1$
        DataRecord record = new DataRecord(null, null);
        Object[] values = new Object[3];
        values[0] = "111"; //$NON-NLS-1$
        values[1] = "222"; //$NON-NLS-1$
        values[2] = "444"; //$NON-NLS-1$
        record.set(fieldMetadata, values);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataRecordWriter dataRecordWriter = new DataRecordDefaultWriter();
        dataRecordWriter.write(record, output);
        String document = new String(output.toByteArray());
        assertEquals(true, document.contains(xml));
    }

}
