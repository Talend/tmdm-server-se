/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.transaction;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;

public abstract class TransactionInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOGGER = Logger.getLogger(TransactionInterceptor.class);

    private static final String TRANSACTION_ID = "transaction-id"; //$NON-NLS-1$

    private static final String LOGOUT_OPERATION_NAME = "logout"; //$NON-NLS-1$

    private static final String PING_OPERATION_NAME = "ping"; //$NON-NLS-1$

    public TransactionInterceptor(String phase) {
        super(phase);
    }

    public TransactionState getState(Message message) throws Fault {
        if (message != null) {
            final SoapMessage soapMessage = (SoapMessage) message;
            final MessageInfo messageInfo = soapMessage.get(MessageInfo.class);
            if (messageInfo != null) {
                String messageOperationName = messageInfo.getName().getLocalPart();
                if (PING_OPERATION_NAME.equals(messageOperationName) || LOGOUT_OPERATION_NAME.equals(messageOperationName)) {
                    // No need for transactions for ping and logout.
                    return ImplicitTransactionState.INSTANCE;
                }
                final List<Header> headers = soapMessage.getHeaders();
                if (headers != null) {
                    for (Header header : headers) {
                        QName name = header.getName();
                        if (name != null && SkipAttributeDocumentBuilder.TALEND_NAMESPACE.equals(name.getNamespaceURI())
                                && TRANSACTION_ID.equals(name.getLocalPart())) {
                            String transactionID = ((Element) header.getObject()).getTextContent();
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Transaction ID from SOAP request: " + transactionID);
                            }
                            return new ExplicitTransaction(transactionID);
                        }
                    }
                }
            }
        }
        return ImplicitTransactionState.INSTANCE;
    }

}
