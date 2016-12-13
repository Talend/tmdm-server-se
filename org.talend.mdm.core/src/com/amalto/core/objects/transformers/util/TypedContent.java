/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.transformers.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;

import com.amalto.core.util.Util;


public class TypedContent implements Serializable{
		
	public static final int SOURCE_BYTES= 1;
	public static final int SOURCE_STREAM= 2;
	public static final int SOURCE_URL= 3;
	
	private String url = null;
	private byte[] bytes = null;;
	private String contentType="";
	private InputStream stream = null;
	
	private InputStream tempStream = null;
	private byte[] tempBytes = null;
	
	public TypedContent() {
		super();
	}
	
	public TypedContent(String url, String contentType) {
		super();
		this.url = url;;
		this.contentType = contentType;
	}

	
	public TypedContent(InputStream is, String contentType) {
		super();
		this.stream = is;
		this.contentType = contentType;
	}

	public TypedContent(byte[] bytes, String contentType) {
		super();
		this.bytes = bytes;
		this.contentType = contentType;
	}
	
	public String getContentType() {
		return contentType;
	}
	public String getUrl() {
		return url;
	}

	/**
	 * This method will wrap a Stream around the content whatever its source<bt>
	 * @see #getContentSourceType()
	 * @return A Stream wrapped around the content
	 * @throws IOException
	 */
	public InputStream getContentStream() throws IOException{
		if (tempStream == null) {
			if (stream!=null)
				tempStream = stream;
			else if (bytes!=null)
				tempStream = new ByteArrayInputStream(bytes);
			else if (url!=null)
				tempStream = new URL(url).openStream();
			else throw new IOException("No available content");
		}
		return tempStream; 
	}
	
	/**
	 * This method will return the bytes of the content whatever its source<br>
	 * Use with caution, you may trigger reading a very large stream<br>
	 * @see #getContentSourceType()
	 * @return an array of bytes
	 * @throws IOException
	 */
	public byte[] getContentBytes() throws IOException{
		if (tempBytes == null) {
			if (bytes!=null) {
				tempBytes = bytes;
			} 
			else if (stream!=null) {
				tempBytes = Util.getBytesFromStream(stream);
				stream.close();
			}
			else if (url!=null) {
				InputStream is = new URL(url).openStream();
				tempBytes = Util.getBytesFromStream(is);
				is.close();
			}
		}
		return tempBytes; 
	}

	/**
	 * Returns the type of the underlying source of the content<br>
	 * 
	 * @return One of {@link #SOURCE_BYTES}, {@link #SOURCE_STREAM}, {@link #SOURCE_URL} 
	 */
	public int getContentSourceType() {
		if (stream!=null)  return SOURCE_STREAM;
		if (url!=null) return SOURCE_URL;
		return SOURCE_BYTES;
	}
	
	
	public void closeContentStream() {
		try {tempStream.close();}catch(Exception e) {}
		tempStream = null;
		stream = null;
	}

	@Override
	public String toString() {
		String s= "TypedContent";
		if (contentType!=null) {
			s+=" ["+contentType+"]";
			if (bytes!=null) {
				String charset = Util.extractCharset(contentType);
				try {
					s+=" @Bytes: "+new String(bytes,charset);
				}catch (UnsupportedEncodingException e) {
					s+=" @Bytes: [Unsupported Encoding: "+charset+"]";
				}
			} 
			else if (stream!=null) {
				s+=" @Stream";
			}
			else if (url!=null) {
				s+=" @URL: "+url;
			}
		} else {
			s+="[NULL]";
		}
		return s;
	}
	
	/******************************************************************
	 * 
	 * Serializable Implementation
	 * 
	 ******************************************************************/
	
	private void writeObject(java.io.ObjectOutputStream out)  throws IOException {
		HashMap<String, Serializable> serializable = new HashMap<String, Serializable>();
		serializable.put("bytes", this.getContentBytes());
		serializable.put("content_type", this.getContentType());
		out.writeObject(serializable);
	}
	
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    	HashMap<String, Serializable> serializable = (HashMap<String, Serializable>) in.readObject();
    	this.tempBytes = (byte[])serializable.get("bytes");
    	this.bytes = tempBytes;
    	this.url = null;
    	this.stream = null;
    	this.contentType = (String) serializable.get("content_type");
    }
	
	
	
}
