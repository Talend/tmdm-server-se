/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.load;

import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;

import com.amalto.core.load.context.AutoGenStateContext;
import com.amalto.core.load.context.DefaultStateContext;
import com.amalto.core.load.context.StateContext;
import com.amalto.core.save.generator.AutoIdGenerator;

/**
 * <p>
 * A parser that reads XML from input stream and transform it into XML documents
 * that can be stored in MDM.
 * </p>
 * <p>
 * This parser reads "full" XML documents, and XML document must be valid. In
 * case the XML stream contains : <br/>
 * &lt;doc&gt;<br/>
 * ...<br/>
 * &lt;/doc&gt;<br/>
 * &lt;doc&gt;<br/>
 * ...<br/>
 * &lt;/doc&gt;<br/>
 * <br/>
 * Consider using {@link com.amalto.core.load.io.XMLRootInputStream} to wrap the
 * XML fragments.
 * </p>
 * <p>
 * <b>Note:</b> This class is thread-safe.
 * </p>
 */
public class LoadParser {

    private static final Logger log = Logger.getLogger(LoadParser.class);

    private LoadParser() {
    }

    /**
     * <p>
     * Parse XML from the input stream and call back once data is ready to
     * persisted in MDM.
     * </p>
     * <p>
     * This method ends once the input stream is exhausted.
     * </p>
     *
     * @param inputStream The input stream to the XML fragments.
     * @param config      Configuration of the LoadParser.
     * @param callback    The callback called when a document is ready to be persisted
     *                    in MDM.
     * @return The context created during the parsing.
     */
    public static StateContext parse(InputStream inputStream, Configuration config, LoadParserCallback callback) {
        return parse(inputStream, config, Constants.DEFAULT_PARSER_LIMIT, callback);
    }

    /**
     * <p>
     * Parse XML from the input stream and call back once data is ready to
     * persisted in MDM.
     * </p>
     * <p>
     * This method ends once the input stream is exhausted <b>OR</b> when the
     * number of callback has reached <code>limit</code>.
     * </p>
     *
     * @param inputStream The input stream to the XML fragments.
     * @param config      Configuration of the LoadParser.
     * @param limit       A limit for documents to persist. Once this limit is reached,
     *                    this method ends.
     * @param callback    The callback called when a document is ready to be persisted
     * @return The context created during the parsing.
     */
    public static StateContext parse(InputStream inputStream, Configuration config, int limit, LoadParserCallback callback) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("LoadParser callback cannot be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }

        XMLStreamReader reader = null;
        try {
            reader = MDMXMLUtils.createXMLStreamReader(inputStream);

            // Useful to know what implementation of StAX was actually chosen in app server context.
            if (log.isDebugEnabled()) {
                ClassLoader classLoader = reader.getClass().getClassLoader();
                if (classLoader != null) {
                    URL resource = classLoader.getResource(reader.getClass().getName().replace(".", "/") + ".class");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (resource != null) {
                        log.debug("XML Stream reader implementation location: " + resource); //$NON-NLS-1$
                    }
                }
            }

            StateContext context = new DefaultStateContext(config.getPayLoadElementName(), config.getIdPaths(), config.getDataClusterName(), config.getDataModelName(), limit, callback);
            if (config.isAutoGenPK()) {
                AutoIdGenerator autoIdGenerator = config.getIdGenerator();
                // Change the context to auto-generate metadata
                context = AutoGenStateContext.decorate(context, config.getIdPaths(), autoIdGenerator);
            }

            while (!context.hasFinished()) {
                context.parse(reader);
            }

            return context;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                log.error("Exception on close reader.", e);
            }
        }
    }

    public static class Configuration {
        private final String payLoadElementName;
        private final String[] idPaths;
        private final boolean autoGenPK;
        private final String dataClusterName;
        private final String dataModelName;
        private final AutoIdGenerator idGenerator;

        public Configuration(String payLoadElementName, String[] idPaths, boolean autoGenPK, String dataClusterName, String dataModelName, AutoIdGenerator idGenerator) {
            this.payLoadElementName = payLoadElementName;
            this.idPaths = idPaths;
            this.autoGenPK = autoGenPK;
            this.dataClusterName = dataClusterName;
            this.dataModelName = dataModelName;
            this.idGenerator = idGenerator;
        }

        /**
         * @return the local name of the XML element to look for while parsing
         *         (e.g. if top level XML element for TypeA is 'typeA', pass
         *         'typeA' as parameter.
         */
        public String getPayLoadElementName() {
            return payLoadElementName;
        }

        /**
         * @return The XPaths expressions to evaluate when build ID for the
         *         document. Array allows to pass several XPaths for ID composed
         *         of 1+ values.
         */
        public String[] getIdPaths() {
            return idPaths;
        }

        public boolean isAutoGenPK() {
            return autoGenPK;
        }

        public String getDataClusterName() {
            return dataClusterName;
        }

        public String getDataModelName() {
            return dataModelName;
        }

        public AutoIdGenerator getIdGenerator() {
            return idGenerator;
        }
    }
}
