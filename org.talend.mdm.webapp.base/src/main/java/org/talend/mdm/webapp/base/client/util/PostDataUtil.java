package org.talend.mdm.webapp.base.client.util;

import java.util.Iterator;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.InputElement;

public class PostDataUtil {

    static FormElement fe;
    public static void postData(String url, Map<String, String> param) {
        if (fe == null) {
            fe = Document.get().createFormElement();
            Document.get().getBody().appendChild(fe);
        }
        fe.setInnerHTML(""); //$NON-NLS-1$

        fe.setAction(url);
        fe.setMethod("post"); //$NON-NLS-1$

        if (param != null && param.size() > 0) {
            Iterator<String> iter = param.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = param.get(key);
                InputElement inputData = Document.get().createHiddenInputElement();
                inputData.setName(key);
                inputData.setValue(value);
                fe.appendChild(inputData);
            }
        }

        fe.submit();

    }
}
