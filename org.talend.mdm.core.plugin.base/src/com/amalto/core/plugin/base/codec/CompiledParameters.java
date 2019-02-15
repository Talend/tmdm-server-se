/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public class CompiledParameters implements Serializable {
	
	private String method ;
	
	private String algorithm;
	
	private boolean wrap;
	
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}


	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	
	
	public boolean isWrap() {
		return wrap;
	}

	public void setWrap(boolean wrap) {
		this.wrap = wrap;
	}

    public String serialize() throws IOException {
        ObjectOutputStream ois = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ois = new ObjectOutputStream(bos);
            ois.writeObject(this);
            return new String(Base64.getEncoder().encode(bos.toByteArray()));
        } finally {
            ois.close();
        }
    }

    public static CompiledParameters deserialize(String base64String) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = null;
        try {
            if (base64String == null || base64String.length() == 0)
                return new CompiledParameters();
            byte[] bytes = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            return (CompiledParameters) ois.readObject();
        } finally {
            ois.close();
        }
    }
}