// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.2_01, construire R40)
// Generated source version: 1.1.2

package com.amalto.webapp.util.webservices;

import com.sun.xml.rpc.encoding.*;
import com.sun.xml.rpc.encoding.xsd.XSDConstants;
import com.sun.xml.rpc.encoding.literal.*;
import com.sun.xml.rpc.encoding.literal.DetailFragmentDeserializer;
import com.sun.xml.rpc.encoding.simpletype.*;
import com.sun.xml.rpc.encoding.soap.SOAPConstants;
import com.sun.xml.rpc.encoding.soap.SOAP12Constants;
import com.sun.xml.rpc.streaming.*;
import com.sun.xml.rpc.wsdl.document.schema.SchemaConstants;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;

public class WSTransformerPluginV2VariableDescriptor_LiteralSerializer extends LiteralObjectSerializerBase implements Initializable  {
    private static final QName ns1_variableName_QNAME = new QName("", "variableName");
    private static final QName ns3_string_TYPE_QNAME = SchemaConstants.QNAME_TYPE_STRING;
    private CombinedSerializer ns3_myns3_string__java_lang_String_String_Serializer;
    private static final QName ns1_mandatory_QNAME = new QName("", "mandatory");
    private static final QName ns3_boolean_TYPE_QNAME = SchemaConstants.QNAME_TYPE_BOOLEAN;
    private CombinedSerializer ns3_myns3__boolean__boolean_Boolean_Serializer;
    private static final QName ns1_description_QNAME = new QName("", "description");
    private static final QName ns1_contentTypesRegex_QNAME = new QName("", "contentTypesRegex");
    private static final QName ns1_possibleValuesRegex_QNAME = new QName("", "possibleValuesRegex");
    
    public WSTransformerPluginV2VariableDescriptor_LiteralSerializer(QName type, String encodingStyle) {
        this(type, encodingStyle, false);
    }
    
    public WSTransformerPluginV2VariableDescriptor_LiteralSerializer(QName type, String encodingStyle, boolean encodeType) {
        super(type, true, encodingStyle, encodeType);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns3_myns3_string__java_lang_String_String_Serializer = (CombinedSerializer)registry.getSerializer("", java.lang.String.class, ns3_string_TYPE_QNAME);
        ns3_myns3__boolean__boolean_Boolean_Serializer = (CombinedSerializer)registry.getSerializer("", boolean.class, ns3_boolean_TYPE_QNAME);
    }
    
    public Object doDeserialize(XMLReader reader,
        SOAPDeserializationContext context) throws Exception {
        com.amalto.webapp.util.webservices.WSTransformerPluginV2VariableDescriptor instance = new com.amalto.webapp.util.webservices.WSTransformerPluginV2VariableDescriptor();
        Object member=null;
        QName elementName;
        List values;
        Object value;
        
        reader.nextElementContent();
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_variableName_QNAME)) {
                member = ns3_myns3_string__java_lang_String_String_Serializer.deserialize(ns1_variableName_QNAME, reader, context);
                if (member == null) {
                    throw new DeserializationException("literal.unexpectedNull");
                }
                instance.setVariableName((java.lang.String)member);
                reader.nextElementContent();
            } else {
                throw new DeserializationException("literal.unexpectedElementName", new Object[] { ns1_variableName_QNAME, reader.getName() });
            }
        }
        else {
            throw new DeserializationException("literal.expectedElementName", reader.getName().toString());
        }
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_mandatory_QNAME)) {
                member = ns3_myns3__boolean__boolean_Boolean_Serializer.deserialize(ns1_mandatory_QNAME, reader, context);
                if (member == null) {
                    throw new DeserializationException("literal.unexpectedNull");
                }
                instance.setMandatory(((Boolean)member).booleanValue());
                reader.nextElementContent();
            } else {
                throw new DeserializationException("literal.unexpectedElementName", new Object[] { ns1_mandatory_QNAME, reader.getName() });
            }
        }
        else {
            throw new DeserializationException("literal.expectedElementName", reader.getName().toString());
        }
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_description_QNAME)) {
                member = ns3_myns3_string__java_lang_String_String_Serializer.deserialize(ns1_description_QNAME, reader, context);
                instance.setDescription((java.lang.String)member);
                reader.nextElementContent();
            } else {
                throw new DeserializationException("literal.unexpectedElementName", new Object[] { ns1_description_QNAME, reader.getName() });
            }
        }
        else {
            throw new DeserializationException("literal.expectedElementName", reader.getName().toString());
        }
        elementName = reader.getName();
        if ((reader.getState() == XMLReader.START) && (elementName.equals(ns1_contentTypesRegex_QNAME))) {
            values = new ArrayList();
            for(;;) {
                elementName = reader.getName();
                if ((reader.getState() == XMLReader.START) && (elementName.equals(ns1_contentTypesRegex_QNAME))) {
                    value = ns3_myns3_string__java_lang_String_String_Serializer.deserialize(ns1_contentTypesRegex_QNAME, reader, context);
                    values.add(value);
                    reader.nextElementContent();
                } else {
                    break;
                }
            }
            member = new java.lang.String[values.size()];
            member = values.toArray((Object[]) member);
            instance.setContentTypesRegex((java.lang.String[])member);
        }
        else {
            instance.setContentTypesRegex(new java.lang.String[0]);
        }
        elementName = reader.getName();
        if ((reader.getState() == XMLReader.START) && (elementName.equals(ns1_possibleValuesRegex_QNAME))) {
            values = new ArrayList();
            for(;;) {
                elementName = reader.getName();
                if ((reader.getState() == XMLReader.START) && (elementName.equals(ns1_possibleValuesRegex_QNAME))) {
                    value = ns3_myns3_string__java_lang_String_String_Serializer.deserialize(ns1_possibleValuesRegex_QNAME, reader, context);
                    values.add(value);
                    reader.nextElementContent();
                } else {
                    break;
                }
            }
            member = new java.lang.String[values.size()];
            member = values.toArray((Object[]) member);
            instance.setPossibleValuesRegex((java.lang.String[])member);
        }
        else {
            instance.setPossibleValuesRegex(new java.lang.String[0]);
        }
        
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (Object)instance;
    }
    
    public void doSerializeAttributes(Object obj, XMLWriter writer, SOAPSerializationContext context) throws Exception {
        com.amalto.webapp.util.webservices.WSTransformerPluginV2VariableDescriptor instance = (com.amalto.webapp.util.webservices.WSTransformerPluginV2VariableDescriptor)obj;
        
    }
    public void doSerialize(Object obj, XMLWriter writer, SOAPSerializationContext context) throws Exception {
        com.amalto.webapp.util.webservices.WSTransformerPluginV2VariableDescriptor instance = (com.amalto.webapp.util.webservices.WSTransformerPluginV2VariableDescriptor)obj;
        
        if (instance.getVariableName() == null) {
            throw new SerializationException("literal.unexpectedNull");
        }
        ns3_myns3_string__java_lang_String_String_Serializer.serialize(instance.getVariableName(), ns1_variableName_QNAME, null, writer, context);
        if (new Boolean(instance.isMandatory()) == null) {
            throw new SerializationException("literal.unexpectedNull");
        }
        ns3_myns3__boolean__boolean_Boolean_Serializer.serialize(new Boolean(instance.isMandatory()), ns1_mandatory_QNAME, null, writer, context);
        ns3_myns3_string__java_lang_String_String_Serializer.serialize(instance.getDescription(), ns1_description_QNAME, null, writer, context);
        if (instance.getContentTypesRegex() != null) {
            for (int i = 0; i < instance.getContentTypesRegex().length; ++i) {
                ns3_myns3_string__java_lang_String_String_Serializer.serialize(instance.getContentTypesRegex()[i], ns1_contentTypesRegex_QNAME, null, writer, context);
            }
        }
        if (instance.getPossibleValuesRegex() != null) {
            for (int i = 0; i < instance.getPossibleValuesRegex().length; ++i) {
                ns3_myns3_string__java_lang_String_String_Serializer.serialize(instance.getPossibleValuesRegex()[i], ns1_possibleValuesRegex_QNAME, null, writer, context);
            }
        }
    }
}
