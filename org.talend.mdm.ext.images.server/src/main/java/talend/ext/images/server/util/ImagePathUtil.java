/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package talend.ext.images.server.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;

public abstract class ImagePathUtil {

    /**
     * Encode an URL path according to RFC 2396
     * @param path Absolute uri path
     * @return
     */
    public static String encodeURL(String path) {
        try {
            // Don't use java.net.URLEncoder as it is meant for application/x-www-form-urlencoded content, such as the
            // query string. Use java.net.URI to encode URL image path
            URI uri = new URI("f", null, path, null); //$NON-NLS-1$
            return StringUtils.substringAfter(uri.toASCIIString(), "f:"); //$NON-NLS-1$
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}