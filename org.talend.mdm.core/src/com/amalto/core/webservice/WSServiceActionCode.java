// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation （1.1.2_01，编译版 R40）
// Generated source version: 1.1.2

package com.amalto.core.webservice;


import java.util.Map;
import java.util.HashMap;

public class WSServiceActionCode {
    private java.lang.String value;
    private static Map valueMap = new HashMap();
    public static final String _STARTString = "START";
    public static final String _STOPString = "STOP";
    public static final String _STATUSString = "STATUS";
    public static final String _EXECUTEString = "EXECUTE";
    
    public static final java.lang.String _START = new java.lang.String(_STARTString);
    public static final java.lang.String _STOP = new java.lang.String(_STOPString);
    public static final java.lang.String _STATUS = new java.lang.String(_STATUSString);
    public static final java.lang.String _EXECUTE = new java.lang.String(_EXECUTEString);
    
    public static final WSServiceActionCode START = new WSServiceActionCode(_START);
    public static final WSServiceActionCode STOP = new WSServiceActionCode(_STOP);
    public static final WSServiceActionCode STATUS = new WSServiceActionCode(_STATUS);
    public static final WSServiceActionCode EXECUTE = new WSServiceActionCode(_EXECUTE);

    public WSServiceActionCode() {
    }

    protected WSServiceActionCode(java.lang.String value) {
        this.value = value;
        valueMap.put(this.toString(), this);
    }
    
    public java.lang.String getValue() {
        return value;
    }
    
    public static WSServiceActionCode fromValue(java.lang.String value)
        throws java.lang.IllegalStateException {
        if (START.value.equals(value)) {
            return START;
        } else if (STOP.value.equals(value)) {
            return STOP;
        } else if (STATUS.value.equals(value)) {
            return STATUS;
        } else if (EXECUTE.value.equals(value)) {
            return EXECUTE;
        }
        throw new IllegalArgumentException();
    }
    
    public static WSServiceActionCode fromString(String value)
        throws java.lang.IllegalStateException {
        WSServiceActionCode ret = (WSServiceActionCode)valueMap.get(value);
        if (ret != null) {
            return ret;
        }
        if (value.equals(_STARTString)) {
            return START;
        } else if (value.equals(_STOPString)) {
            return STOP;
        } else if (value.equals(_STATUSString)) {
            return STATUS;
        } else if (value.equals(_EXECUTEString)) {
            return EXECUTE;
        }
        throw new IllegalArgumentException();
    }
    
    public String toString() {
        return value.toString();
    }
    
    private Object readResolve()
        throws java.io.ObjectStreamException {
        return fromValue(getValue());
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof WSServiceActionCode)) {
            return false;
        }
        return ((WSServiceActionCode)obj).value.equals(value);
    }
    
    public int hashCode() {
        return value.hashCode();
    }
}
