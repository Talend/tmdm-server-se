/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.load.xml;

import com.amalto.core.load.State;
import com.amalto.core.load.context.StateContext;
import com.amalto.core.save.generator.AutoIncrementUtil;
import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Set;

@SuppressWarnings("nls")
public class AutoFieldGeneration implements State {

    @Override public void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException {
        try {
            String currentContainerPath = AutoIncrementUtil.getCurrentPath(context.getCurrentLocation());
            Set<String> autoFields = AutoIncrementUtil.normalAutoIncrementField(currentContainerPath, context.getNormalFieldGenerators().keySet());
            for (String path : autoFields) {
                String normalFieldPath = StringUtils.substringAfter(path, currentContainerPath);
                normalFieldPath = normalFieldPath.startsWith("/") ? normalFieldPath.substring(1): normalFieldPath;
                context.getWriter().writeStartElement(normalFieldPath);
                context.getWriter().writeCharacters(context.getNormalFieldGenerators().get(path)
                        .generateId(context.getMetadata().getDataClusterName(), context.getMetadata().getName(),
                                path.replaceAll("/", ".")));
                context.getWriter().writeEndElement(normalFieldPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate automatic field", e);
        }

    }

    public boolean isFinal() {
        return false;
    }
}
