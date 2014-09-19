// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation （1.1.2_01，编译版 R40）
// Generated source version: 1.1.2

package com.amalto.core.webservice;


import java.util.Map;
import java.util.HashMap;

public class WSRoutingEngineV2Status {
    private java.lang.String value;
    private static Map valueMap = new HashMap();
    public static final String _DEADString = "DEAD";
    public static final String _STOPPEDString = "STOPPED";
    public static final String _SUSPENDEDString = "SUSPENDED";
    public static final String _RUNNINGString = "RUNNING";
    
    public static final java.lang.String _DEAD = new java.lang.String(_DEADString);
    public static final java.lang.String _STOPPED = new java.lang.String(_STOPPEDString);
    public static final java.lang.String _SUSPENDED = new java.lang.String(_SUSPENDEDString);
    public static final java.lang.String _RUNNING = new java.lang.String(_RUNNINGString);
    
    public static final WSRoutingEngineV2Status DEAD = new WSRoutingEngineV2Status(_DEAD);
    public static final WSRoutingEngineV2Status STOPPED = new WSRoutingEngineV2Status(_STOPPED);
    public static final WSRoutingEngineV2Status SUSPENDED = new WSRoutingEngineV2Status(_SUSPENDED);
    public static final WSRoutingEngineV2Status RUNNING = new WSRoutingEngineV2Status(_RUNNING);

    public WSRoutingEngineV2Status() {
    }

    protected WSRoutingEngineV2Status(java.lang.String value) {
        this.value = value;
        valueMap.put(this.toString(), this);
    }
    
    public java.lang.String getValue() {
        return value;
    }
    
    public static WSRoutingEngineV2Status fromValue(java.lang.String value)
        throws java.lang.IllegalStateException {
        if (DEAD.value.equals(value)) {
            return DEAD;
        } else if (STOPPED.value.equals(value)) {
            return STOPPED;
        } else if (SUSPENDED.value.equals(value)) {
            return SUSPENDED;
        } else if (RUNNING.value.equals(value)) {
            return RUNNING;
        }
        throw new IllegalArgumentException();
    }
    
    public static WSRoutingEngineV2Status fromString(String value)
        throws java.lang.IllegalStateException {
        WSRoutingEngineV2Status ret = (WSRoutingEngineV2Status)valueMap.get(value);
        if (ret != null) {
            return ret;
        }
        if (value.equals(_DEADString)) {
            return DEAD;
        } else if (value.equals(_STOPPEDString)) {
            return STOPPED;
        } else if (value.equals(_SUSPENDEDString)) {
            return SUSPENDED;
        } else if (value.equals(_RUNNINGString)) {
            return RUNNING;
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
        if (!(obj instanceof WSRoutingEngineV2Status)) {
            return false;
        }
        return ((WSRoutingEngineV2Status)obj).value.equals(value);
    }
    
    public int hashCode() {
        return value.hashCode();
    }
}
