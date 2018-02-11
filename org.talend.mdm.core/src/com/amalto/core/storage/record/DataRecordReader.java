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

package com.amalto.core.storage.record;


import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

/**
 *
 */
public interface DataRecordReader<T> {

    String METADATA_NAMESPACE = "http://www.talend.com/mdm/metadata"; //$NON-NLS-1$

    String TASK_ID = "task_id"; //$NON-NLS-1$

    /**
     * Read an input (typed as <code>T</code>) and returns a {@link DataRecord} instance that has user type
     * {@link ComplexTypeMetadata}.
     * @param revisionId      Revision id of the <code>input</code> to read. This value is used to set the value of revision in
     *                        {@link DataRecord#getRevisionId()}.
     * @param repository      Repository to be used to resolve actual type in case in inheritance.
     * @param input           An input (type may vary in implementations of this interface).   @return A {@link com.amalto.core.storage.record.DataRecord} instance with fields filled with values of <code>input</code>. Fields names are the
     *                        ones defined in <code>type</code> argument. Implementations should not return null but throw exception in
     *                        case of errors.  @throws RuntimeException Might be thrown by implementations of this interface.
     * @return A {@link DataRecord} that contains all information read from <code>input</code>.
     */
    DataRecord read(MetadataRepository repository, ComplexTypeMetadata type, T input);

}
