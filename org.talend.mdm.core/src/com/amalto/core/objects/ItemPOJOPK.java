/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.marshalling.MarshallingFactory;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;


public class ItemPOJOPK implements Serializable,Comparable{
	
	private DataClusterPOJOPK dataClusterPK;
	private String conceptName;
	private String[] ids;
	
	/**
	 * For mashalling/unmarshalling purposes only
	 *
	 */
	public ItemPOJOPK() {
		super();
	}
	
	/**
	 * @param name
	 * @param clusterPK
	 * @param ids
	 */
	public ItemPOJOPK(DataClusterPOJOPK clusterPK, String name, String[] ids) {
		super();
		conceptName = name;
		dataClusterPK = clusterPK;
		this.ids = ids;
	}
	/**
	 * @return Returns the conceptName.
	 */
	public String getConceptName() {
		return conceptName;
	}
	/**
	 * @param conceptName The conceptName to set.
	 */
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}
	/**
	 * @return Returns the dataClusterPK.
	 */
	public DataClusterPOJOPK getDataClusterPOJOPK() {
		return dataClusterPK;
	}
	/**
	 * @param dataClusterPK The dataClusterPK to set.
	 */
	public void setDataClusterPOJOPK(DataClusterPOJOPK dataClusterPK) {
		this.dataClusterPK = dataClusterPK;
	}
	/**
	 * @return Returns the ids.
	 */
	public String[] getIds() {
		return ids;
	}
	/**
	 * @param ids The ids to set.
	 */
	public void setIds(String[] ids) {
		this.ids = ids;
	}
	
	/**
	 * Marshal the POJO PK to a Castor XML string
	 * @return the marshalled PK
	 * @throws XtentisException
	 */
	 public String  marshal() throws XtentisException {	
	        try {
	    		StringWriter sw = new StringWriter();
	    		MarshallingFactory.getInstance().getMarshaller(this.getClass()).marshal(this, sw);
	    		return sw.toString();
		    } catch (Exception e) {
	    	    String err = "Unable to marshal the PK "+getDataClusterPOJOPK()+"."+getConceptName()+"."+Util.joinStrings(getIds(),".")
	    	    		+": "+e.getClass().getName()+": "+e.getLocalizedMessage();
	    	    org.apache.log4j.Logger.getLogger(this.getClass()).error(err,e);
	    	    throw new XtentisException(err);
		    } 
	 }

	/**
	 * Unmarshal an Item POJO PK from a Castor XML string
	 * @return the ItemPOJOPK
	 * @throws XtentisException
	 */
	 public static ItemPOJOPK  unmarshal(String marshalledItemPOJOPK) throws XtentisException {	
	        try {
	            return MarshallingFactory.getInstance().getUnmarshaller(ItemPOJOPK.class).unmarshal(new StringReader(marshalledItemPOJOPK));
		    } catch (Exception e) {
	    	    String err = "Unable to unmarshal the PK "+marshalledItemPOJOPK
	    	    		+": "+e.getClass().getName()+": "+e.getLocalizedMessage();
	    	    org.apache.log4j.Logger.getLogger(ItemPOJOPK.class).error(err,e);
	    	    throw new XtentisException(err);
		    } 
	 }
	 
	 /**
	  * Returns a unique String representing this item
	  * @return the unique id String
	  */
	 public String getUniqueID() {
        StringBuilder uniqueIdBuilder = new StringBuilder();
        uniqueIdBuilder.append(dataClusterPK.getUniqueId());
        uniqueIdBuilder.append('.');
        uniqueIdBuilder.append(getConceptName());
        for (int i = 0; i < ids.length; i++) {
            uniqueIdBuilder.append('.');
            if (ids[i] != null)
                uniqueIdBuilder.append(ids[i].trim()); // trim added due to exist bu triming the ids
        }
        return uniqueIdBuilder.toString();
	  }

	@Override
	public String toString() {
		return getUniqueID();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
	    if (! (obj instanceof ItemPOJOPK)) return false;
	    ItemPOJOPK other = (ItemPOJOPK) obj;
	    return other.getUniqueID().equals(this.getUniqueID());
	}

	public int compareTo(Object o) {
		if(o instanceof ItemPOJOPK){
			ItemPOJOPK itemPOJOPK=((ItemPOJOPK) o);
			String itemPOJOPKID=itemPOJOPK.getUniqueID();
			return this.getUniqueID().compareTo(itemPOJOPKID);
		}
		return 0;
	}
	 
	 
}
