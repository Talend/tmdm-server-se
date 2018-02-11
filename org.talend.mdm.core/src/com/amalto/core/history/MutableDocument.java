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

package com.amalto.core.history;

import com.amalto.core.history.accessor.Accessor;

/**
 * A {@link Document} that is able to change.
 */
public interface MutableDocument extends Document {

    /**
     * @param path The path for the accessor.
     * @return Create a {@link Accessor} for a field in this document.
     */
    Accessor createAccessor(String path);

    /**
     * @return Returns this document as a DOM tree. This method must be used with extra caution: it <b>might</b> give
     * direct access to the underlying representation of the {@link MutableDocument}.
     */
    org.w3c.dom.Document asDOM();

    /**
     * @return A DOM document similar to {@link #asDOM()} but without Talend MDM specific attributes.
     */
    org.w3c.dom.Document asValidationDOM();

    /**
     * @return Returns a document with created status.
     * @param content Content of the document at its creation time.
     */
    MutableDocument create(MutableDocument content);

    /**
     * Replaces content of this document with another. This is similar to {@link #create(MutableDocument)} but without the
     * create status update.
     * @param content Content of a document.
     * @return Returns a document with content from parameter. Document is not expected to be in created status 
     */
    MutableDocument setContent(MutableDocument content);
    
    /**
     * Deletes the document. MDM supports two different kinds of deletes: LOGICAL and PHYSICAL.
     *
     * @param deleteType The type of delete to perform.
     * @return A {@link MutableDocument} after delete has been performed.
     * @see DeleteType
     */
    MutableDocument delete(DeleteType deleteType);

    /**
     * Recovers the document. MDM supports two different kinds of deletes: LOGICAL and PHYSICAL.
     *
     * @param deleteType Type of delete to recover.
     * @return A recovered document.
     * @throws IllegalStateException If the document wasn't deleted by the <code>deleteType</code> type.
     * @see DeleteType
     */
    MutableDocument recover(DeleteType deleteType);

    /**
     * @return Returns a unmodifiable document that might be result of action optimizations.
     */
    Document applyChanges();

    /**
     * @return A copy of this document: modifications on copy are not reflected on this document. For DOM based
     * implementation, this require a (deep) copy of the DOM tree, so use this method carefully.
     */
    MutableDocument copy();

    /**
     * Removes all empty values (MDM never stores empty values). Clean is expected to be a recursive clean (removes all
     * empty fields and containing field element if all of them are empty).
     */
    void clean();

    /**
     * Set the task id (or "group id") for this document.
     * @param taskId A String task id (usually a UUID).
     */
    void setTaskId(String taskId);
    
    /**
     * @return whether a missing element in the document should be considered as an empty value
     * or a missing value. 
     * 
     * Empty value means information exists and is empty (empty string)
     * Missing value means document is not complete (value was not provided an should be unchanged)
     */
    boolean considerMissingElementsAsEmpty();
}
