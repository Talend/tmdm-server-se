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

import java.util.Date;

/**
 * <b>Important note:</b> Implementations of this interface are not meant to be thread safe. Use this with caution in
 * multi threaded environment.
 */
public interface DocumentHistoryNavigator {
    /**
     * @return Returns current state of the document.
     */
    Document current();

    /**
     * @return Returns next state of the document (i.e. the state of the document one step ahead in the future).
     * @throws IllegalStateException if no next state is available.
     * @see #hasNext()
     */
    Document next();

    /**
     * @return Returns <code>true</code> if history has a future state for the document.
     */
    boolean hasNext();

    /**
     * @return Returns previous state of the document (i.e. the state of the document one step ahead in the past).
     * @throws IllegalStateException if no previous state is available.
     * @see #hasPrevious()
     */
    Document previous();

    /**
     * @return Returns <code>true</code> if history has a past state for the document.
     */
    boolean hasPrevious();

    /**
     * <p>
     * Jumps to the state of the document at date <code>date</code>. History navigators MUST support bi-directional
     * navigation: if the date is <b>before</b> the current state of the document, they will move to previous states
     * until it reaches the date. If the date is <b>after</b> the current state of the document, they will move to next
     * states until it reaches the date.
     * </p>
     *
     * @param date A valid date (i.e. not a date after <i>now</i>).
     * @return The document state at date <code>date</code>
     * @throws IllegalArgumentException if <code>date</code> is after <code>System.currentTimeMillis()</code> or if
     *                                  <code>date</code> is null.
     */
    Document goTo(Date date);

    /**
     * @return The document at its oldest known state. This is <b>usually</b> (but not always) the state of the document
     * when it has just been created (if first recorded event on the document is a creation).
     */
    Document last();

    /**
     * @return Returns how many actions were applied to the document during its known recorded history.
     */
    int size();

    /**
     * @return Returns the current action done to the document.
     */
    Action currentAction();
}
