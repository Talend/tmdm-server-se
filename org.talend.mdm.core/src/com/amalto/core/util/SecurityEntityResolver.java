/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SecurityEntityResolver implements EntityResolver {

   public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException 
   { 
	   if (systemId != null) {
           Pattern httpUrl = Pattern.compile("(http|https|ftp):(\\//|\\\\)(.*):(.*)");
           Matcher match = httpUrl.matcher(systemId);
           if (match.matches()) {
               StringBuilder buffer = new StringBuilder();
               String credentials = new String(Base64.encodeBase64("admin:talend".getBytes()));
               URL url = new URL(systemId);
               URLConnection conn = url.openConnection();
               conn.setAllowUserInteraction(true);
               conn.setDoOutput(true);
               conn.setDoInput(true);
               conn.setRequestProperty("Authorization", "Basic " + credentials);
               conn.setRequestProperty("Expect", "100-continue");
               InputStreamReader doc = new InputStreamReader(conn.getInputStream());
               BufferedReader reader = new BufferedReader(doc);
               String line = reader.readLine();
               while (line != null) {
                   buffer.append(line);
                   line = reader.readLine();
               }
               return new InputSource(new StringReader(buffer.toString()));
           } else {
               int mark = systemId.indexOf("file:///");
               String path = systemId.substring((mark != -1 ? mark + "file:///".length() : 0));
               File file = new File(path);
               return new InputSource(file.toURL().openStream());
           }

       }
       return null; 
  } 

}