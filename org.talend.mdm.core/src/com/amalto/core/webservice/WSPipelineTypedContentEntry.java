// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation ��1.1.2_01������� R40��
// Generated source version: 1.1.2

package com.amalto.core.webservice;


public class WSPipelineTypedContentEntry {
    protected java.lang.String output;
    protected com.amalto.core.webservice.WSExtractedContent wsExtractedContent;
    
    public WSPipelineTypedContentEntry() {
    }
    
    public WSPipelineTypedContentEntry(java.lang.String output, com.amalto.core.webservice.WSExtractedContent wsExtractedContent) {
        this.output = output;
        this.wsExtractedContent = wsExtractedContent;
    }
    
    public java.lang.String getOutput() {
        return output;
    }
    
    public void setOutput(java.lang.String output) {
        this.output = output;
    }
    
    public com.amalto.core.webservice.WSExtractedContent getWsExtractedContent() {
        return wsExtractedContent;
    }
    
    public void setWsExtractedContent(com.amalto.core.webservice.WSExtractedContent wsExtractedContent) {
        this.wsExtractedContent = wsExtractedContent;
    }
}
