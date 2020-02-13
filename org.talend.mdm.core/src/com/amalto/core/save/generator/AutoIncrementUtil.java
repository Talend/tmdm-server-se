/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save.generator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;

import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("nls")
public class AutoIncrementUtil {
    private static final Logger LOG = Logger.getLogger(AutoIncrementUtil.class);

    public static String getConceptForAutoIncrement(String storageName, String conceptName) {
        String concept = null;
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage storage = storageAdmin.get(storageName, storageAdmin.getType(storageName));
        if (storage != null) {
            MetadataRepository metadataRepository = storage.getMetadataRepository();
            if (metadataRepository != null) {
                if (conceptName.contains(".")) {
                    concept = conceptName.split("\\.")[0];
                } else {
                    concept = conceptName;
                }
                ComplexTypeMetadata complexType = metadataRepository.getComplexType(concept);
                if (complexType != null) {
                    concept = MetadataUtils.getSuperConcreteType(complexType).getName();
                }
            }
        }
        return concept;
    }

    /**
     * For inherited type, the origin fieldPath is like this: B.Id or C.id, B and C super type is A,
     * it used the super type as the concept. so it will return to A.id
     *
     * If the field path is in one complex type, like Course/Detail/Count, Course and Detail, both are the complex type,
     * the field path is don't modify.
     *
     * @param storageName the storage name
     * @param concept concept name, if it's the inherited, it is the super type name.
     * @param fieldPath the AUTO_INCREMENT type field's field path, with the concept name as the prefix.
     * @return the field path
     */
    public static String getAutoIncrementFieldPath(String storageName, String concept, String fieldPath) {
        if (storageName == null || concept == null || fieldPath == null) {
            return null;
        }
        // if the field is inherited, it should be remove the concept name from the fieldPath
        if (concept.equals(getConceptForAutoIncrement(storageName, fieldPath)) && fieldPath.contains(".")) {
            return fieldPath.substring(fieldPath.indexOf(".") + 1);
        }
        return fieldPath;
    }

    public static String[] generatedNormalField(String[] normalFields, String content) {
        List<String> generatedField = new ArrayList<>(normalFields.length);
        if (normalFields.length == 0) {
            return generatedField.toArray(new String[0]);
        }
        String beginName = content.substring(content.indexOf("<") + 1, content.indexOf(">"));
        if (StringUtils.countMatches(content, beginName) > 2) {
            String endName = "</" + beginName + ">";
            content = content.substring(0, content.indexOf(endName) + endName.length());
        }
        try {
            Document document = DocumentHelper.parseText(content);
            Element root = document.getRootElement();
            for (String fieldPath : normalFields) {
                Element element = root;
                if (!fieldPath.contains("/") && element.element(fieldPath) == null) {
                    generatedField.add(fieldPath);
                } else if (fieldPath.contains("/")) {
                    String[] allFieldPaths = fieldPath.split("/");
                    for (int i = 0; i < allFieldPaths.length; i++) {
                        element = element.element(allFieldPaths[i]);
                        if (element == null) {
                            if (i == 0) {
                                break;
                            }
                            generatedField.add(fieldPath);
                        }
                    }
                }

            }
        } catch (Exception e) {
            LOG.error("Faield to parse stream to Document");
        }

        return generatedField.toArray(new String[generatedField.size()]);
    }
}
