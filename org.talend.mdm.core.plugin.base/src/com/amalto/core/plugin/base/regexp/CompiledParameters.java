/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.regexp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.regex.Pattern;

public class CompiledParameters implements Serializable {
	Pattern regexp = Pattern.compile(".*",Pattern.DOTALL);
	private String resultPattern = "{1}";
	private String resultingContentType = "text/xml";

	public Pattern getRegexp() {
		return regexp;
	}

	public void setRegexp(Pattern regexp) {
		this.regexp = regexp;
	}

	public String getResultingContentType() {
		return resultingContentType;
	}

	public void setResultingContentType(String resultingContentType) {
		this.resultingContentType = resultingContentType;
	}
	
	public String getResultPattern() {
		return resultPattern;
	}

	public void setResultPattern(String resultPattern) {
		this.resultPattern = resultPattern;
	}

	public String serialize() throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream ois = new ObjectOutputStream(bos);
		ois.writeObject(this);
		return new String(Base64.getEncoder().encode(bos.toByteArray()));
	}
	
	public static CompiledParameters deserialize(String base64String) throws IOException,ClassNotFoundException{
		byte[] bytes = Base64.getDecoder().decode(base64String); 
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		return (CompiledParameters)ois.readObject();
	}
	
}


