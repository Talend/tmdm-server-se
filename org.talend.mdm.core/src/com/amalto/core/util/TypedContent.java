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

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author bgrieder
 * @deprecated - use TransformerV2 package
 *
 */
public class TypedContent {
	private byte[] bytes = null;

    private String contentType="";
	private InputStream stream = null;
	
	public TypedContent() {
		super();
	}
	
	public TypedContent(InputStream is, byte[] bytes, String contentType) {
		super();
		this.stream = is;
		this.bytes = bytes;
		this.contentType = contentType;
	}
	
	public InputStream getStream() {
		return stream;
	}
	public void setStream(InputStream is) {
		this.stream = is;
	}
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public static com.amalto.core.objects.transformers.util.TypedContent getNewTypedContent(TypedContent oldContent) {
    	com.amalto.core.objects.transformers.util.TypedContent newContent = null;
    	if (oldContent.getBytes() != null)
    		newContent = new com.amalto.core.objects.transformers.util.TypedContent(oldContent.getBytes(),oldContent.getContentType());
    	else if (oldContent.getStream() != null) {
    		newContent = new com.amalto.core.objects.transformers.util.TypedContent(oldContent.getStream(),oldContent.getContentType());
    	}
    	return newContent;
	}
	
	public static TypedContent getOldTypedContent(com.amalto.core.objects.transformers.util.TypedContent newContent) throws IOException{
    	TypedContent oldContent = new TypedContent();
   		oldContent.setStream(newContent.getContentStream());
   		//only fill the bytes if the source is actual bytes
   		if (newContent.getContentSourceType() == com.amalto.core.objects.transformers.util.TypedContent.SOURCE_BYTES)
   			oldContent.setBytes(newContent.getContentBytes());
   		oldContent.setContentType(newContent.getContentType());
    	return oldContent;
	}
	
}
