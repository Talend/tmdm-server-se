/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.record;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 *
 */
public interface DataRecordWriter {

    /**
     * Writes a {@link DataRecord} record to the <code>output</code>.
     *
     * @param record A {@link DataRecord} instance.
     * @param output A {@link OutputStream} instance.
     * @throws IOException In case of errors while writing to <code>output</code>.
     * @see DataRecordReader#read(String, org.talend.mdm.commmon.metadata.MetadataRepository, org.talend.mdm.commmon.metadata.ComplexTypeMetadata, Object)
     */
    public void write(DataRecord record, OutputStream output) throws IOException;

    /**
     * Writes a {@link DataRecord} record to the <code>writer</code>.
     *
     * @param record A {@link DataRecord} instance.
     * @param writer A {@link Writer} instance.
     * @throws IOException In case of errors while writing to <code>output</code>.
     * @see DataRecordReader#read(String, org.talend.mdm.commmon.metadata.MetadataRepository, org.talend.mdm.commmon.metadata.ComplexTypeMetadata, Object)
     */
    public void write(DataRecord record, Writer writer) throws IOException;

}
