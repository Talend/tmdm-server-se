// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation ��1.1.2_01������� R40��
// Generated source version: 1.1.2

package com.amalto.core.webservice;


public class WSExtractedContent {
    protected com.amalto.core.webservice.WSByteArray wsByteArray;
    protected java.lang.String contentType;
    
    public WSExtractedContent() {
    }
    
    public WSExtractedContent(com.amalto.core.webservice.WSByteArray wsByteArray, java.lang.String contentType) {
        this.wsByteArray = wsByteArray;
        this.contentType = contentType;
    }
    
    public com.amalto.core.webservice.WSByteArray getWsByteArray() {
        return wsByteArray;
    }
    
    public void setWsByteArray(com.amalto.core.webservice.WSByteArray wsByteArray) {
        this.wsByteArray = wsByteArray;
    }
    
    public java.lang.String getContentType() {
        return contentType;
    }
    
    public void setContentType(java.lang.String contentType) {
        this.contentType = contentType;
    }
}
