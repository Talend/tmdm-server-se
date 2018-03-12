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

package com.amalto.core.load;

import java.util.HashSet;
import java.util.Set;

import com.amalto.core.load.path.PathMatch;
import com.amalto.core.load.path.PathMatcher;
import junit.framework.TestCase;

/**
 *
 */
@SuppressWarnings("nls")
public class PathMatcherTest extends TestCase {
    private PathMatcher path;

    public void testSimpleMatch() {
        String path = "/Id";
        Set<PathMatcher> paths = new HashSet<PathMatcher>();
        paths.add(new PathMatcher(path));
        String[] elements = new String[]{"Id"};

        assertTrue(matchElements(paths, elements));
    }

    public void testSimpleMatchFail() {
        String path = "/Id2";
        Set<PathMatcher> paths = new HashSet<PathMatcher>();
        paths.add(new PathMatcher(path));
        String[] elements = new String[]{"Id"};

        assertFalse(matchElements(paths, elements));
    }

    public void testNested2() {
        String path = "/Id/Id2";
        Set<PathMatcher> paths = new HashSet<PathMatcher>();
        paths.add(new PathMatcher(path));
        String[] elements = new String[]{"Id", "Id2"};

        assertTrue(matchElements(paths, elements));
    }

    public void testNested5() {
        String path = "/Id1/Id2/Id3/Id4/Id5";
        Set<PathMatcher> paths = new HashSet<PathMatcher>();
        paths.add(new PathMatcher(path));
        String[] elements = new String[]{"Id1", "Id2", "Id3", "Id4", "Id5"};

        assertTrue(matchElements(paths, elements));
    }

    public void testSameNested2() {
        String path = "/Id/Id";
        Set<PathMatcher> paths = new HashSet<PathMatcher>();
        paths.add(new PathMatcher(path));
        String[] elements = new String[]{"Id", "Id"};

        assertTrue(matchElements(paths, elements));
    }

    public void testSameNested5() {
        String path = "/Id/Id/Id/Id/Id";
        Set<PathMatcher> paths = new HashSet<PathMatcher>();
        paths.add(new PathMatcher(path));
        String[] elements = new String[]{"Id", "Id", "Id", "Id", "Id"};

        assertTrue(matchElements(paths, elements));
    }

    public void testSimpleMatchMultiplePaths() {
        Set<PathMatcher> paths = new HashSet<PathMatcher>();
        paths.add(new PathMatcher("/Id0"));
        paths.add(new PathMatcher("/Id1"));
        paths.add(new PathMatcher("/Id"));
        String[] elements = new String[]{"Id"};

        assertTrue(matchElements(paths, elements));
    }

    public void testSimpleMatchMultiplePathsFail() {
        Set<PathMatcher> paths = new HashSet<PathMatcher>();
        paths.add(new PathMatcher("/Id0"));
        paths.add(new PathMatcher("/Id1"));
        paths.add(new PathMatcher("/Id2"));
        String[] elements = new String[]{"Id"};

        assertFalse(matchElements(paths, elements));
    }

    public void testMultiplePathsFail() {
        Set<PathMatcher> paths = new HashSet<PathMatcher>();
        paths.add(new PathMatcher("/Id"));

        assertFalse(matchElements(paths, new String[]{"Id2"}));
        assertFalse(matchElements(paths, new String[]{"Id20"}));
        assertTrue(matchElements(paths, new String[]{"Id"}));
    }

    private boolean match(String elementName, Set<PathMatcher> paths) {
        if (path == null) {
            for (PathMatcher currentPath : paths) {
                PathMatch match = currentPath.match(elementName);
                switch (match) {
                    case PARTIAL:
                        path = currentPath;
                    case NONE:
                        break;
                    case FULL:
                        return true;
                    default:
                        throw new IllegalArgumentException("Unsupported match type: " + match);
                }

                if (path != null) {
                    break;
                }
            }
            return false;
        } else {
            PathMatch match = path.match(elementName);
            switch (match) {
                case NONE:
                    path = null;
                case PARTIAL:
                    return false;
                case FULL:
                    return true;
                default:
                    throw new IllegalArgumentException("Unsupported match type: " + match);
            }
        }
    }

    private boolean matchElements(Set<PathMatcher> paths, String[] elements) {
        for (String currentElement : elements) {
            if (match(currentElement, paths)) {
                return true;
            }
        }
        return false;
    }

}
