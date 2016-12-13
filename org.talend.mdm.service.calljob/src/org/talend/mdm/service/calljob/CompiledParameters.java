/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.service.calljob;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

public class CompiledParameters implements Serializable {

	String url;
	List<ContextParam> tisContext;
	
	public List<ContextParam> getTisContext() {
		return tisContext;
	}

	public void setTisContext(List<ContextParam> tisContext) {
		this.tisContext = tisContext;
	}


	String username;
	String password;
	String contentType = "text/xml; charset=utf-8";
	ConceptMappingParam conceptMappingParam;

	public CompiledParameters() {
    }

	public String getUrl() {
    	return url;
    }

	public void setUrl(String url) {
    	this.url = url;
    }
	

	public String getUsername() {
    	return username;
    }

	public void setUsername(String username) {
    	this.username = username;
    }

	public String getPassword() {
    	return password;
    }

	public void setPassword(String password) {
    	this.password = password;
    }
	
	public String getContentType() {
    	return contentType;
    }

	public void setContentType(String contentType) {
    	this.contentType = contentType;
    }
	
	
	public ConceptMappingParam getConceptMappingParam() {
		return conceptMappingParam;
	}

	public void setConceptMappingParam(ConceptMappingParam conceptMappingParam) {
		this.conceptMappingParam = conceptMappingParam;
	}

	public String serialize() throws IOException{
		String xml="<configuration>";
		xml+="<url>"+StringEscapeUtils.escapeXml(url)+"</url>";
		xml+="<username>"+username+"</username>";
		xml+="<password>"+password+"</password>";
	
		for(ContextParam kv: tisContext){
			xml+="<contextParam>";
			xml+="<name>"+kv.getName()+"</name>";
			xml+="<value>"+kv.getValue()+"</value>";			
			xml+="</contextParam>";
		};
		if(this.conceptMappingParam!=null){
			xml+="<conceptMapping>";
			xml+="<concept>"+conceptMappingParam.getConcept()+"</concept>";
			xml+="<fields>"+conceptMappingParam.getFields()+"</fields>";
			xml+="</conceptMapping>";
		}
		xml+="</configuration>";
		return xml;
	}
	
	public static CompiledParameters deserialize(String xml) 
		throws IOException,						
						XtentisException,
						ParserConfigurationException,
						SAXException, TransformerException
	{
		CompiledParameters compiled = new CompiledParameters();
		Element params = Util.parse(xml).getDocumentElement();
		
		//TISCall - mandatory
		String url = Util.getFirstTextNode(params, "url");
		if (url==null) {
			String err = "The url parameter of the TIS Call Transformer Plugin cannot be empty";			
			throw new XtentisException(err);
		}
		compiled.setUrl(url);

		
		Document parametersDoc = params.getOwnerDocument();
		List<ContextParam> paramsList=new ArrayList<ContextParam>();
		NodeList paramList = Util.getNodeList(parametersDoc.getDocumentElement(), "//contextParam");
		for (int i=0; i<paramList.getLength(); i++) {
			String paramName = Util.getFirstTextNode(paramList.item(i), "name");
			String paramValue = Util.getFirstTextNode(paramList.item(i), "value");
			if (paramValue == null)
				paramValue = "";
			
			if (paramName!=null) {
				boolean ispiple=false;

				Pattern pipeParamPattern = Pattern.compile("\\{(.*?)\\}");
				Matcher match = pipeParamPattern.matcher(paramValue);
				if (match.matches()) {
					//paramValue = match.group(1);
					ispiple = true;
				}
				paramsList.add(new ContextParam(paramName,paramValue,ispiple));
			}
		}
		compiled.setTisContext(paramsList);
		//content Type - defaults to "text/plain; charset=utf-8"
		String contentType = Util.getFirstTextNode(params, "contentType");
		if (contentType == null) contentType = "text/xml; charset=utf-8";
		compiled.setContentType(contentType);

		//username - defaults to null
		String username = Util.getFirstTextNode(params, "username");
		compiled.setUsername(username);

		//password - defaults to null
		String password = Util.getFirstTextNode(params, "password");
		compiled.setPassword(password);
		
		//conceptMapping
		NodeList conceptMappingList = Util.getNodeList(parametersDoc.getDocumentElement(), "//conceptMapping");
		if(conceptMappingList!=null&&conceptMappingList.getLength()>0){
			Node conceptMapping=conceptMappingList.item(0);
			String concept=Util.getFirstTextNode(conceptMapping, "concept");
			String fields=Util.getFirstTextNode(conceptMapping, "fields");
			compiled.setConceptMappingParam(new ConceptMappingParam(concept,fields));
		}else{
			compiled.setConceptMappingParam(null);
		}
	
		return compiled;
	}
	
}


