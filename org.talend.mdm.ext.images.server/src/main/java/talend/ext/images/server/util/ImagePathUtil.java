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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    public static String[] parseFileFullName(String fileName) {
        String[] result = new String[2];
        String simpleFileName = ""; //$NON-NLS-1$
        if (fileName.indexOf("/") == -1 && fileName.indexOf("\\") == -1 && fileName.contains(".")) { //$NON-NLS-1$ //$NON-NLS-2$
            simpleFileName = fileName;
        } else {
            String regExp = ".+[\\\\\\/](.+)$"; //$NON-NLS-1$
            Pattern p = Pattern.compile(regExp);
            Matcher m = p.matcher(fileName);
            m.find();
            simpleFileName = m.group(1);
        }

        int point = simpleFileName.lastIndexOf("."); //$NON-NLS-1$
        result[0] = simpleFileName.substring(0, point);
        result[1] = simpleFileName.substring(point + 1);
        return result;
    }
}