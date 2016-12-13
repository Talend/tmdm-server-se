/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.server.actions;

import java.util.List;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Util {
    
    public static String[] getItemNameByProjection(String conceptName, String projection, MetadataRepository repository, String language) throws Exception {
        String[] values = new String[2];
        Document doc = com.amalto.core.util.Util.parse(projection);
        FieldMetadata firstPrimaryKeyInfo = null;
        if (repository != null ) {
            ComplexTypeMetadata type = repository.getComplexType(conceptName);
            if (type != null) {
                List<FieldMetadata> pkInfos = type.getPrimaryKeyInfo();
                if (pkInfos != null && pkInfos.size() > 0) {
                    firstPrimaryKeyInfo = pkInfos.get(0);
                }
            }
        }
        if (firstPrimaryKeyInfo != null) {
            // get the xpath by firstPrimaryKeyInfo, it is a SoftFieldRef
            Element pkInfo = firstPrimaryKeyInfo.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT);
            if (pkInfo != null && pkInfo.getTextContent() != null) {
                values[0] = com.amalto.core.util.Util.getFirstTextNode(doc, "ii/p/" + pkInfo.getTextContent()); //$NON-NLS-1$
                if (firstPrimaryKeyInfo.getType().getName().equals(DataTypeConstants.MLS.getTypeName())) {
                    values[0] = MultilanguageMessageParser.getValueByLanguage(values[0], language);
                }
            }
        }
        values[1] = com.amalto.core.util.Util.getFirstTextNode(doc, "ii/dmn"); //$NON-NLS-1$
        return values;
    }
}
