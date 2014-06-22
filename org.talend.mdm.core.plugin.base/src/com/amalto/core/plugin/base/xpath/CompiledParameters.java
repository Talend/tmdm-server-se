package com.amalto.core.plugin.base.xpath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CompiledParameters implements Serializable {
	private String xPath = null;
	private String resultingContentType = null;
	
	public String getResultingContentType() {
		return resultingContentType;
	}
	public void setResultingContentType(String resultingContentType) {
		this.resultingContentType = resultingContentType;
	}

	public String getXPath() {
		return xPath;
	}
	public void setXPath(String path) {
		xPath = path;
	}

	public String serialize() throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream ois = new ObjectOutputStream(bos);
		ois.writeObject(this);
		return new BASE64Encoder().encode(bos.toByteArray());
	}
	
	public static CompiledParameters deserialize(String base64String) throws IOException,ClassNotFoundException{
		byte[] bytes = new BASE64Decoder().decodeBuffer(base64String); 
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		return (CompiledParameters)ois.readObject();
	}
	
}


