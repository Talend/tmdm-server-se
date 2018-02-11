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

package com.amalto.core.load.path;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 */
public class PathMatcher {
    private int nextMatchIndex = 1;
    private int maxIndex;
    private final Set<Integer> pathElementsToMatch;
    private final String path;

    public PathMatcher(String path) {
        this.path = path;
        StringTokenizer tokenizer = new StringTokenizer(path, "/");

        pathElementsToMatch = new HashSet<Integer>(tokenizer.countTokens() + 1);
        int position = 1;
        while (tokenizer.hasMoreTokens()) {
            String pathElement = tokenizer.nextToken();
            pathElementsToMatch.add(computeHashCode(pathElement, position));

            maxIndex++;
            position++;
        }
    }

    public PathMatch match(String pathElement) {
        if (nextMatchIndex < maxIndex) {
            if (pathElementsToMatch.contains(computeHashCode(pathElement, nextMatchIndex))) {
                nextMatchIndex++;
                return PathMatch.PARTIAL;
            } else {
                nextMatchIndex = 1;
                return PathMatch.NONE;
            }
        } else if (nextMatchIndex == maxIndex) {
            if (pathElementsToMatch.contains(computeHashCode(pathElement, nextMatchIndex))) {
                return PathMatch.FULL;
            } else {
                nextMatchIndex = 1;
                return PathMatch.NONE;
            }
        }

        // nextMatchIndex > maxIndex (should not happen)
        throw new IllegalStateException("Exceeded the maxIndex for path");
    }

    private static int computeHashCode(String elementName, int position) {
        return elementName.hashCode() >> position;
    }

    @Override
    public String toString() {
        return path;
    }
}
