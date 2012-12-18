package com.amalto.core.objects.datamodel.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.amalto.core.metadata.ComplexTypeMetadata;
import com.amalto.core.metadata.MetadataRepository;
import com.amalto.core.server.DataModel;
import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.ServerContext;
import org.apache.commons.lang.NotImplementedException;

import com.amalto.core.ejb.ObjectPOJO;
import com.amalto.core.ejb.ObjectPOJOPK;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;

/**
 * @author Bruno Grieder
 * 
 * @ejb.bean name="DataModelCtrl"
 *			display-name="Name for DataModelCtrl"
 *			description="Description for DataModelCtrl"
 *          jndi-name="amalto/remote/core/datamodelctrl"
 * 		  	local-jndi-name = "amalto/local/core/datamodelctrl"
 *          type="Stateless"
 *          view-type="both"
 * 
 * @ejb.remote-facade
 * 
 * @ejb.permission
 * 	view-type = "remote"
 * 	role-name = "administration"
 * @ejb.permission
 * 	view-type = "local"
 * 	unchecked = "true"
 *  
 */
public class DataModelCtrlBean implements SessionBean, DataModel {
  
	public static final long serialVersionUID = 1264958272;

    private static final Logger LOGGER = Logger.getLogger(DataModelCtrlBean.class);

    /**
     * DataModelCtrlBean.java
     * Constructor
     * 
     */
    public DataModelCtrlBean() {
        super();
    }

    public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
    }

    public void ejbRemove() throws EJBException, RemoteException {
    }

    public void ejbActivate() throws EJBException, RemoteException {
    }

    public void ejbPassivate() throws EJBException, RemoteException {
    }
    
    public void ejbCreate() throws javax.ejb.CreateException {
    }
    
    public void ejbPostCreate() throws javax.ejb.CreateException {
    }

    /**
     * Creates or updates a DataModel
     *
     * @throws XtentisException
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public DataModelPOJOPK putDataModel(DataModelPOJO dataModel) throws XtentisException {
        try {
            if ((dataModel.getSchema() == null) || "".equals(dataModel.getSchema())) {  //$NON-NLS-1$
                // put an empty schema
                dataModel.setSchema(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //$NON-NLS-1$
                                "<xsd:schema " + //$NON-NLS-1$
                                "	elementFormDefault=\"qualified\"" + //$NON-NLS-1$
                                "	xml:lang=\"EN\"" + //$NON-NLS-1$
                                "	xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + //$NON-NLS-1$ 
                                "</xsd:schema>" //$NON-NLS-1$
                );
            } 
            ObjectPOJOPK pk = dataModel.store();
            if (pk == null) {
                throw new XtentisException("Unable to create the Data Model. Please check the XML Server logs");
            }
            return new DataModelPOJOPK(pk);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to create/update the Data Model " + dataModel.getName()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get Data Model
     *
     * @throws XtentisException
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public DataModelPOJO getDataModel(DataModelPOJOPK pk) throws XtentisException {
        if (pk == null || pk.getUniqueId() == null) {
            throw new XtentisException("The Data Model can't be empty!");
        }
        try {
            DataModelPOJO sp = ObjectPOJO.load(DataModelPOJO.class, pk);
            if (sp == null && pk.getUniqueId() != null && !"null".equals(pk.getUniqueId())) {
                String err = "The Data Model " + pk.getUniqueId() + " does not exist.";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return sp;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the Data Model " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get a DataModel - no exception is thrown: returns null if not found
     *
     * @throws XtentisException
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public DataModelPOJO existsDataModel(DataModelPOJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(DataModelPOJO.class, pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            String info = "Could not check whether this Data Model exists: " + pk.getUniqueId()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(info, e);
            return null;
        }
    }

    /**
     * Remove a Data Model
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method 
     */
    public DataModelPOJOPK removeDataModel(DataModelPOJOPK pk) throws XtentisException{
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removing "+pk.getUniqueId());
        }

        try {
        	return new DataModelPOJOPK(ObjectPOJO.remove(DataModelPOJO.class,pk));
	    } catch (XtentisException e) {
	    	throw(e);
	    } catch (Exception e) {
    	    String err = "Unable to remove the DataModel "+pk.toString()
    	    		+": "+e.getClass().getName()+": "+e.getLocalizedMessage();
    	    LOGGER.error(err);
    	    throw new XtentisException(err, e);
	    }
    }    

    /**
	 * Retrieve all DataModel PKs
	 * 
	 * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method 
     */       
    public Collection<DataModelPOJOPK> getDataModelPKs(String regex) throws XtentisException {
    	Collection<ObjectPOJOPK> dataModelPKs = ObjectPOJO.findAllPKs(DataModelPOJO.class,regex);
    	ArrayList<DataModelPOJOPK> l = new ArrayList<DataModelPOJOPK>();
        for (ObjectPOJOPK dataModelPK : dataModelPKs) {
            l.add(new DataModelPOJOPK(dataModelPK));
        }
    	return l;
    }

    /**
     * Checks the datamodel - returns the "corrected schema"
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     * @deprecated 
     */
    public String checkSchema(String schema) throws XtentisException{
        return schema;
    }

    /**
     * Put a Business Concept Schema
     *
     * @return its name
     * @throws XtentisException
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     * @deprecated 
     */
    public String putBusinessConceptSchema(DataModelPOJOPK pk, String conceptSchemaString) throws XtentisException {
        throw new NotImplementedException();
    }

    /**
     * Delete a Business Concept
     *
     * @return its name
     * @throws XtentisException
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     * @deprecated 
     */
    public String deleteBusinessConcept(DataModelPOJOPK pk, String businessConceptName) throws XtentisException {
        throw new NotImplementedException();
    }

    /**
     * Find all Business Concepts names
     *
     * @throws XtentisException
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public String[] getAllBusinessConceptsNames(DataModelPOJOPK pk) throws XtentisException {
        MetadataRepositoryAdmin metadataRepositoryAdmin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
        MetadataRepository repository = metadataRepositoryAdmin.get(pk.getUniqueId());
        Collection<ComplexTypeMetadata> userComplexTypes = repository.getUserComplexTypes();
        String[] businessConceptNames = new String[userComplexTypes.size()];
        int i = 0;
        for (ComplexTypeMetadata currentType : userComplexTypes) {
            businessConceptNames[i++] = currentType.getName();
        }
        return businessConceptNames;
    }

}