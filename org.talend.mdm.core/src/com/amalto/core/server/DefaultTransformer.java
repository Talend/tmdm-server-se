/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.backgroundjob.BackgroundJobPOJO;
import com.amalto.core.objects.backgroundjob.BackgroundJobPOJOPK;
import com.amalto.core.objects.transformers.TransformerV2POJO;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.util.TransformerCallBack;
import com.amalto.core.objects.transformers.util.TransformerContext;
import com.amalto.core.objects.transformers.util.TransformerGlobalContext;
import com.amalto.core.objects.transformers.util.TransformerPluginCallBack;
import com.amalto.core.objects.transformers.util.TransformerPluginContext;
import com.amalto.core.objects.transformers.util.TransformerPluginV2LocalInterface;
import com.amalto.core.objects.transformers.util.TransformerPluginVariableDescriptor;
import com.amalto.core.objects.transformers.util.TransformerProcessStep;
import com.amalto.core.objects.transformers.util.TransformerVariablesMapping;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.objects.transformers.util.TypedContent_Do_Not_Process;
import com.amalto.core.objects.transformers.util.TypedContent_Drop_Variable;
import com.amalto.core.objects.transformers.util.TypedContent_Use_Default;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.server.api.Transformer;
import com.amalto.core.util.JobActionInfo;
import com.amalto.core.util.PluginRegistry;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

public class DefaultTransformer implements TransformerPluginCallBack, com.amalto.core.server.api.Transformer {

    public static final long serialVersionUID = 1986745965402456L;

    public static final Logger LOGGER = Logger.getLogger(DefaultTransformer.class);

    protected final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss-SSS z");

    /**
     * Creates or updates a Transformer
     */
    @Override
    public TransformerV2POJOPK putTransformer(TransformerV2POJO transformer) throws XtentisException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("putTransformer() " + transformer.getName()); //$NON-NLS-1$
        }
        try {
            // Check and compile the parameters one by one
            ArrayList<TransformerProcessStep> specs = transformer.getProcessSteps();
            for (TransformerProcessStep step : specs) {
                // get the plugin
                TransformerPluginV2LocalInterface plugin = getPlugin(step.getPluginJNDI());
                step.setCompiledParameters(plugin.compileParameters(step.getParameters()));
            }
            return new TransformerV2POJOPK(transformer.store());
        } catch (Exception e) {
            String err = "Unable to create/update the Transfomer '" + transformer.getName() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(err, e);
            throw new XtentisException(err);
        }
    }

    /**
     * Get item
     */
    @Override
    public TransformerV2POJO getTransformer(TransformerV2POJOPK pk) throws XtentisException {
        try {
            TransformerV2POJO transformer = ObjectPOJO.load(TransformerV2POJO.class, pk);
            if (transformer == null) {
                String err = "The Transformer '" + pk.getUniqueId() + "' does not exist."; //$NON-NLS-1$ //$NON-NLS-2$
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return transformer;
        } catch (Exception e) {
            String err = "Unable to get the Transformer '" + pk.toString() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(err, e);
            throw new XtentisException(err);
        }
    }

    /**
     * Get a Transformer - no exception is thrown: returns null if not found
     */
    @Override
    public TransformerV2POJO existsTransformer(TransformerV2POJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(TransformerV2POJO.class, pk);
        } catch (Exception e) {
            String info = "Could not check whether this Transformer exists '" + pk.toString() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(info, e);
            return null;
        }
    }

    /**
     * Remove an item
     */
    @Override
    public TransformerV2POJOPK removeTransformer(TransformerV2POJOPK pk) throws XtentisException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("removeTransformer() " + pk.getUniqueId()); //$NON-NLS-1$
        }
        try {
            return new TransformerV2POJOPK(ObjectPOJO.remove(TransformerV2POJO.class, pk));
        } catch (Exception e) {
            String err = "Unable to remove the Transformer '" + pk.toString() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(err, e);
            throw new XtentisException(err);
        }
    }

    /**
     * Retrieve all Transformer PKS
     */
    @Override
    public Collection<TransformerV2POJOPK> getTransformerPKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(TransformerV2POJO.class, regex);
        ArrayList<TransformerV2POJOPK> l = new ArrayList<TransformerV2POJOPK>();
        for (ObjectPOJOPK objectPOJOPK : c) {
            l.add(new TransformerV2POJOPK(objectPOJOPK));
        }
        return l;
    }

    /**
     * Read an item and process it through a transformer. The content of the item is mapped to the
     * {@link #DEFAULT_VARIABLE} variable
     * 
     * @return The pipeline after the transformer is run
     * @throws com.amalto.core.util.XtentisException
     */
    @Override
    public TransformerContext extractThroughTransformer(TransformerV2POJOPK transformerV2POJOPK, ItemPOJOPK itemPOJOPK)
            throws XtentisException {
        TransformerContext ctx = new TransformerContext(transformerV2POJOPK);
        try {
            ItemPOJO item = Util.getItemCtrl2Local().getItem(itemPOJOPK);
            ctx.putInPipeline(Transformer.DEFAULT_VARIABLE, new TypedContent(item.getProjectionAsString().getBytes("UTF-8"), //$NON-NLS-1$
                    "text/xml; charset=utf-8")); //$NON-NLS-1$
        } catch (Exception e) {
            String err = "Unable to extract '" + itemPOJOPK.getUniqueID() + "' through the Transformer '" //$NON-NLS-1$ //$NON-NLS-2$
                    + transformerV2POJOPK.getUniqueId() + "'"; //$NON-NLS-1$
            LOGGER.error(err, e);
            throw new XtentisException(err);
        }

        return executeUntilDone(ctx);

    }

    /**
     * Executes theTransformer as job
     */
    @Override
    public BackgroundJobPOJOPK executeAsJob(TransformerContext context, TransformerCallBack callBack) throws XtentisException {
        try {
            // create a Background Job
            BackgroundJobPOJO bgPOJO = new BackgroundJobPOJO();
            bgPOJO.setDescription("Execute Transformer " + context.getTransformerV2POJOPK().getUniqueId()
                    + " as a Background Job");
            bgPOJO.setMessage("Scheduling the job");
            bgPOJO.setPercentage(-1);
            bgPOJO.setSerializedObject(null);
            bgPOJO.setStatus(BackgroundJobPOJO._SCHEDULED_);
            synchronized (DateTimeConstant.DATE_FORMAT) {
                bgPOJO.setTimestamp(DateTimeConstant.DATE_FORMAT.format(new Date(System.currentTimeMillis())));
            }
            bgPOJO.store();
            // launch job in background
            JobActionInfo actionInfo = new JobActionInfo(bgPOJO.getId(), context.getTransformerV2POJOPK().getUniqueId(), // action
                    context);
            executeAsBackGroundJob(actionInfo, callBack);
            return new BackgroundJobPOJOPK(bgPOJO.getPK());
        } catch (Exception e) {
            String err = "Unable to execute the Transformer '" + context.getTransformerV2POJOPK().getUniqueId() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(err, e);
            throw new XtentisException(err);
        }
    }

    /**
     * Executes theTransformer as background job
     */
    public void executeAsBackGroundJob(JobActionInfo actionInfo, TransformerCallBack callBack) throws XtentisException {
        String transformerName = actionInfo.getAction();
        TransformerGlobalContext transformerContext = null;

        try {
            // recover process parameters
            transformerContext = new TransformerGlobalContext((TransformerContext) actionInfo.getInfo());

            // update Back Ground Job
            BackgroundJobPOJO bgPOJO = Util.getBackgroundJobCtrlLocal().getBackgroundJob(
                    new BackgroundJobPOJOPK(actionInfo.getJobId()));
            bgPOJO.setMessage("Starting processing"); //$NON-NLS-1$
            bgPOJO.setStatus(BackgroundJobPOJO._RUNNING_);
            bgPOJO.setTimestamp(sdf.format(new Date(System.currentTimeMillis())));
            try {
                Util.getBackgroundJobCtrlLocal().putBackgroundJob(bgPOJO);
            } catch (Exception unlikely) {
                unlikely.printStackTrace();
            }
            transformerContext.setJob(bgPOJO);

            // Execute
            execute(transformerContext, new TransformerCallBack() {

                @Override
                public void contentIsReady(TransformerContext globalContext) throws XtentisException {
                    long counter = ((TransformerGlobalContext) globalContext).getIterationNumber();
                    org.apache.log4j.Logger.getLogger(this.getClass()).trace("contentIsReady() item " + counter); //$NON-NLS-1$
                    if (counter % 100 == 0) {
                        long time = System.currentTimeMillis() - ((TransformerGlobalContext) globalContext).getStartTime();
                        int processRate = (int) ((double) (counter * 1000) / (double) time);
                        BackgroundJobPOJO bgPOJO = ((TransformerGlobalContext) globalContext).getJob();
                        bgPOJO.setMessage("Processed item " + counter + " at " + processRate + " items per second"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                        bgPOJO.setStatus(BackgroundJobPOJO._RUNNING_);
                        bgPOJO.setTimestamp(sdf.format(new Date(System.currentTimeMillis())));
                        try {
                            Util.getBackgroundJobCtrlLocal().putBackgroundJob(bgPOJO);
                        } catch (Exception e) {
                            LOGGER.error("Unable to execute the transformer as back ground job."); //$NON-NLS-1$
                        }
                    }
                }

                @Override
                public void done(TransformerContext globalContext) throws XtentisException {
                    org.apache.log4j.Logger.getLogger(this.getClass()).trace("done() "); //$NON-NLS-1$
                    long counter = ((TransformerGlobalContext) globalContext).getIterationNumber();
                    long time = System.currentTimeMillis() - ((TransformerGlobalContext) globalContext).getStartTime();
                    int processRate = (int) ((double) (counter * 1000) / (double) time);
                    BackgroundJobPOJO bgPOJO = ((TransformerGlobalContext) globalContext).getJob();
                    ;
                    // Add the Item PKs to the pipeline as comma separated lines
                    String pksAsLine = ""; //$NON-NLS-1$
                    Collection<ItemPOJOPK> pks = ((TransformerGlobalContext) globalContext).getProjectedPKs();
                    synchronized (pks) {
                        for (ItemPOJOPK pk : pks) {
                            if (!"".equals(pksAsLine)) { //$NON-NLS-1$
                                pksAsLine += "\n"; //$NON-NLS-1$
                            }
                            pksAsLine += pk.getUniqueID();
                        }
                    }
                    org.apache.log4j.Logger.getLogger(this.getClass()).debug("done() Projected PKs\n" + pksAsLine); //$NON-NLS-1$

                    try {
                        globalContext.putInPipeline("records saved in the database", new TypedContent( //$NON-NLS-1$
                                pksAsLine.getBytes("UTF-8"), "text/plain; charset=\"utf-8\"")); //$NON-NLS-1$//$NON-NLS-2$
                    } catch (Exception e) {
                        LOGGER.error("Unable put item pks to transformer context."); //$NON-NLS-1$
                    }

                    try {
                        bgPOJO.setMessage("Processing successfully completed at item " + counter + " (running at " + processRate //$NON-NLS-1$//$NON-NLS-2$
                                + " items per second)"); //$NON-NLS-1$
                        bgPOJO.setStatus(BackgroundJobPOJO._COMPLETED_);
                        bgPOJO.setTimestamp(sdf.format(new Date(System.currentTimeMillis())));
                        bgPOJO.setSerializedObject(null);
                        bgPOJO.setPipeline(globalContext.getPipelineClone());
                        try {
                            Util.getBackgroundJobCtrlLocal().putBackgroundJob(bgPOJO);
                        } catch (Exception e) {
                            LOGGER.error("Unable add the Item PKs to the back ground job pipeline."); //$NON-NLS-1$
                        }
                    } catch (Exception e) {
                        String err = "Transformer Done but unable to store the result in the background object: " + ": " //$NON-NLS-1$ //$NON-NLS-2$
                                + e.getClass().getName() + ": " + e.getLocalizedMessage(); //$NON-NLS-1$
                        LOGGER.error(err, e);
                        throw new XtentisException(err);
                    }
                }
            });

        } catch (Exception e) {
            try {
                // Update Background job and try to put pipeline
                BackgroundJobPOJO bgPOJO = Util.getBackgroundJobCtrlLocal().getBackgroundJob(
                        new BackgroundJobPOJOPK(actionInfo.getJobId()));
                bgPOJO.setMessage("Error processing Transformer. '" + transformerName + "': " + e.getMessage()); //$NON-NLS-1$//$NON-NLS-2$
                bgPOJO.setStatus(BackgroundJobPOJO._STOPPED_);
                bgPOJO.setTimestamp(sdf.format(new Date(System.currentTimeMillis())));
                bgPOJO.setSerializedObject(null);
                bgPOJO.setPipeline(transformerContext == null ? null : transformerContext.getPipelineClone());
                try {
                    Util.getBackgroundJobCtrlLocal().putBackgroundJob(bgPOJO);
                } catch (Exception err) {
                    LOGGER.error(err);
                }
            } catch (Exception ex) {
                String err = "Unable to Process the Transformer '" + transformerName + "': " + e.getClass().getName() + ": " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        + e.getLocalizedMessage();
                LOGGER.error(err, e);
            }
        }
    }

    /**
     * Executes the Transformer synchronously
     */
    @Override
    public void execute(TransformerContext context, TransformerCallBack callBack) throws XtentisException {
        // TODO for long running process, asynchronous execution will not block client (Studio or WebUI)
        // current design is still based on synchronous execution of process
        // transform the context as a global context if not already done
        // The context will be a TransformerContext if called directly,
        // a TransformerGlobalCOntext if called from process
        TransformerGlobalContext globalContext = null;
        if (context instanceof TransformerGlobalContext) {
            globalContext = (TransformerGlobalContext) context;
        } else {
            globalContext = new TransformerGlobalContext(context);
        }
        // sets the callback in the transformers used by the plugins when the content is ready
        // {@link contentIsReady}
        globalContext.setExecuteCallBack(callBack);
        try {
            // and run the plugin
            TransformerV2POJO transformerPOJO = globalContext.getTransformerPOJO();
            if ((transformerPOJO.getProcessSteps() != null) && (transformerPOJO.getProcessSteps().size() > 0)) {
                executePlugin(globalContext, 0);
            }
            // signal done to the call back
            callBack.done(globalContext);
        } catch (Exception e) {
            String err = "Unable to execute the Transformer: '" + context.getTransformerV2POJOPK().getUniqueId() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(err, e);
            throw new XtentisException(err);
        } finally {
            // End the plugins
            try {
                LinkedHashMap<Integer, TransformerPluginV2LocalInterface> instantiatedPlugins = globalContext
                        .getInstantiatedPlugins();
                Set<Integer> pluginNumbers = instantiatedPlugins.keySet();
                for (Integer pluginNumber : pluginNumbers) {
                    TransformerPluginV2LocalInterface plugin = instantiatedPlugins.get(pluginNumber);
                    plugin.end(globalContext.getTransformerPluginContext(pluginNumber.intValue()));
                }
                // clean global context
                globalContext.removeAll();
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * Executes the Transformer Synchronously The Typed Content passed is stored in the DEFAULT pipeline variable
     */
    @Override
    public void execute(TransformerContext context, TypedContent content, TransformerCallBack callBack) throws XtentisException {
        if (content != null) {
            context.putInPipeline(DEFAULT_VARIABLE, content);
        }
        execute(context, callBack);
    }

    /**
     * Executes the Transformer and returns only when it is done
     */
    @Override
    public TransformerContext executeUntilDone(TransformerContext context) throws XtentisException {
        // Set ready variable
        context.put("com.amalto.core.objects.transformers.v2.transformerCtrlV2.ready", Boolean.FALSE); //$NON-NLS-1$
        // execute the Transformer
        execute(context, new TransformerCallBack() {

            @Override
            public void contentIsReady(TransformerContext context) throws XtentisException {
            }

            @Override
            public void done(TransformerContext context) throws XtentisException {
                context.put("com.amalto.core.objects.transformers.v2.transformerCtrlV2.ready", Boolean.TRUE); //$NON-NLS-1$
            }
        });
        if (context.get("com.amalto.core.objects.transformers.v2.transformerCtrlV2.ready") != null) { //$NON-NLS-1$
            while (context.get("com.amalto.core.objects.transformers.v2.transformerCtrlV2.ready").equals(Boolean.FALSE)) { //$NON-NLS-1$
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            context.remove("com.amalto.core.objects.transformers.v2.transformerCtrlV2.ready"); //$NON-NLS-1$
        }
        return context;
    }

    /**
     * Executes the Transformer and returns only when it is done The Typed Content passed is stored in the DEFAULT
     * pipeline variable
     */
    @Override
    public TransformerContext executeUntilDone(TransformerContext context, TypedContent content) throws XtentisException {
        // Store the content in the default pipeline variable
        context.putInPipeline(DEFAULT_VARIABLE, content);
        return executeUntilDone(context);
    }

    /**
     * Executes a plugin
     */
    protected void executePlugin(TransformerGlobalContext globalContext, int pluginNumber) throws XtentisException {
        // fetch the Transformer and the instantiated plugins
        TransformerV2POJO transformerPOJO = globalContext.getTransformerPOJO();
        // fetch the Process Step
        TransformerProcessStep processStep = transformerPOJO.getProcessSteps().get(pluginNumber);
        // fetch the Plugin context
        TransformerPluginContext pluginContext = globalContext.getTransformerPluginContext(pluginNumber);
        /*******************************************************************
         * Initialize the Plugin - if not already done
         *******************************************************************/
        HashMap<Integer, TransformerPluginV2LocalInterface> instantiatedPlugins = globalContext.getInstantiatedPlugins();
        // get the plugin parameters
        String parameters = processStep.getCompiledParameters();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("executePlugin() " + processStep.getDescription() + " -- Initializing plugin " //$NON-NLS-1$ //$NON-NLS-2$
                    + processStep.getPluginJNDI());
        }
        // Check if plugin is already instantiated and initialized
        TransformerPluginV2LocalInterface plugin = null;
        if ((plugin = instantiatedPlugins.get(new Integer(pluginNumber))) == null) {
            try {
                // fetch and initialize the plugin
                plugin = getPlugin(processStep.getPluginJNDI());
                // set global context to each plugin by aiming
                plugin.setGlobalContext(globalContext);
                // the plugin handle is the plugin number
                plugin.init(globalContext.getTransformerPluginContext(pluginNumber), parameters);
            } catch (XtentisException e) {
                throw (e);
            }
            // update the Map of running plugins
            instantiatedPlugins.put(pluginNumber, plugin);
        }
        /*******************************************************************
         * Check the input mappings Stop this Step if a mandatory Input mapping is missing
         *******************************************************************/
        try {
            // fetch descriptors
            ArrayList<TransformerPluginVariableDescriptor> descriptors = plugin.getInputVariableDescriptors("en"); //$NON-NLS-1$
            // loop over the input variables - map content and determine if all mandatory variables have content
            for (TransformerPluginVariableDescriptor descriptor : descriptors) {
                TypedContent content = getMappedInputVariable(descriptor, processStep, globalContext);
                if (content instanceof TypedContent_Do_Not_Process) {
                    // we stop the plugin processing
                    if (LOGGER.isDebugEnabled()) {
                        String msg = "Transformer step '" + processStep.getDescription() + "' of Transformer '" //$NON-NLS-1$ //$NON-NLS-2$
                                + globalContext.getTransformerV2POJOPK().getUniqueId() + "': " //$NON-NLS-1$
                                + "not executed --> input variable '" + descriptor.getVariableName() + "' is marked as stopped"; //$NON-NLS-1$ //$NON-NLS-2$
                        LOGGER.debug("executeNextPlugin() " + msg); //$NON-NLS-1$
                    }
                    return;
                }
                // insert the plugin input variable content in the plugin context
                pluginContext.put(descriptor.getVariableName(), content);
            }
        } catch (Exception e) {
            String err = "Transformer step '" + processStep.getDescription() + "' of Transformer '" //$NON-NLS-1$ //$NON-NLS-2$
                    + globalContext.getTransformerV2POJOPK().getUniqueId() + "':" + "the mapping of the input variables failed"; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
        /*******************************************************************
         * Execute the plugin
         *******************************************************************/
        try {
            // content will be sent through the contentIsReady method below.
            pluginContext.setPluginCallBack(this);
            // check if disabled should not exe this plugin by aiming
            if (!processStep.isDisabled()) {
                // execute the plugin
                plugin.execute(pluginContext);
            } else {
                // sigal content is ready by aiming
                pluginContext.getPluginCallBack().contentIsReady(pluginContext);
            }
        } catch (XtentisException e) {
            throw (e);
        }

        // Done
    }

    private TransformerPluginV2LocalInterface getPlugin(String pluginJNDI) {
        return PluginRegistry.getInstance().getPlugin(pluginJNDI);
    }

    /**
     * Returns the content to be mapped to the input of a plugin
     * 
     * @param descriptor
     * @param processStep
     * @param context
     * @return the TypedContent to be mapped
     * @throws com.amalto.core.util.XtentisException
     */
    private TypedContent getMappedInputVariable(TransformerPluginVariableDescriptor descriptor,
            TransformerProcessStep processStep, TransformerContext context) throws XtentisException {

        try {

            String pluginVariable = descriptor.getVariableName();

            // find input in mappings and detemine pipeline variable name
            TransformerVariablesMapping mapping = null;
            for (TransformerVariablesMapping mpg : processStep.getInputMappings()) {
                if (pluginVariable.equals(mpg.getPluginVariable())) {
                    mapping = mpg;
                    break;
                }
            }

            // Mapping not found
            if (mapping == null) {
                // The input variable is mandatory - it is a design time error
                if (descriptor.isMandatory()) {
                    String err = "Transformer step '" + processStep.getDescription() + "' of Transformer '" //$NON-NLS-1$ //$NON-NLS-2$
                            + context.getTransformerV2POJOPK().getUniqueId() + "':" + "The input variable '" //$NON-NLS-1$ //$NON-NLS-2$
                            + descriptor.getVariableName() + "' of plugin '" + processStep.getPluginJNDI() //$NON-NLS-1$
                            + "' is mandatory and was not mapped during the Transformer '" //$NON-NLS-1$
                            + context.getTransformerV2POJOPK().getUniqueId() + "' design."; //$NON-NLS-1$
                    LOGGER.error(err);
                    throw new XtentisException(err);
                }
                // not mandatory --> return the special USE_DEFAULT Typed Content
                return new TypedContent_Use_Default();
            }

            String pipelineVariable = mapping.getPipelineVariable();
            if (pipelineVariable == null) {
                pipelineVariable = Transformer.DEFAULT_VARIABLE;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getMappedInputVariable() Mapping pipeline " + pipelineVariable + " ---> " + pluginVariable); //$NON-NLS-1$ //$NON-NLS-2$
            }
            // Mapping is found --> retrieve the pipeline content;
            TypedContent content = context.getFromPipeline(pipelineVariable);

            // if content is null, the variable was never initialized, do not process it
            if (content == null) {
                return new TypedContent_Do_Not_Process();
            }

            // if content is marked as do not process, well.... do not process
            if (content instanceof TypedContent_Do_Not_Process) {
                return content;
            }

            // check if content-types match
            boolean match = false;
            for (Pattern pattern : descriptor.getContentTypesRegex()) {
                if (pattern.matcher(Util.extractTypeAndSubType(content.getContentType())).matches()) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                String err = "Transformer step '" + processStep.getDescription() + "' of Transformer '" //$NON-NLS-1$ //$NON-NLS-2$
                        + context.getTransformerV2POJOPK().getUniqueId() + "':" + "The input variable '" //$NON-NLS-1$ //$NON-NLS-2$
                        + descriptor.getVariableName() + "' of plugin '" + processStep.getPluginJNDI() //$NON-NLS-1$
                        + "' cannot accept the pipeline variable '" + mapping.getPipelineVariable() + "' content-type of '" //$NON-NLS-1$ //$NON-NLS-2$
                        + content.getContentType() + "'"; //$NON-NLS-1$
                LOGGER.error(err);
                throw new XtentisException(err);
            }

            // check possible values regex
            if ((descriptor.getPossibleValuesRegex() != null) && (descriptor.getPossibleValuesRegex().size() != 0)
                    && (!descriptor.getPossibleValuesRegex().contains(".*")) //$NON-NLS-1$
                    && (Util.extractTypeAndSubType(content.getContentType()).startsWith("text"))) { //$NON-NLS-1$
                String charset = Util.extractCharset(content.getContentType());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int b;
                while ((b = content.getContentStream().read()) != -1) {
                    bos.write(b);
                }
                String text = new String(bos.toByteArray(), charset);
                match = false;
                for (Pattern pattern : descriptor.getContentTypesRegex()) {
                    if (pattern.matcher(text).matches()) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    String err = "Transformer step '" + processStep.getDescription() + "' of Transformer '" //$NON-NLS-1$ //$NON-NLS-2$
                            + context.getTransformerV2POJOPK().getUniqueId() + "':" + "The input variable '" //$NON-NLS-1$ //$NON-NLS-2$
                            + descriptor.getVariableName() + "' of plugin '" + processStep.getPluginJNDI() //$NON-NLS-1$
                            + "' cannot accept the pipeline variable '" + mapping.getPipelineVariable() + "' value of '" + text //$NON-NLS-1$ //$NON-NLS-2$
                            + "'"; //$NON-NLS-1$
                    LOGGER.error(err);
                    throw new XtentisException(err);
                }
            }

            return content;
        } catch (Exception e) {
            String err = "Transformer step '" + processStep.getDescription() + "' of Transformer '" //$NON-NLS-1$ //$NON-NLS-2$
                    + context.getTransformerV2POJOPK().getUniqueId() + "':" + "the mapping of the input variables failed"; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(err, e);
            throw new XtentisException(err);
        }

    }

    /*****************************************************************
     * TransformerPluginCallBack Implementation
     *****************************************************************/

    /**
     * Implementation of
     * {@link com.amalto.core.objects.transformers.util.TransformerPluginCallBack#contentIsReady(com.amalto.core.objects.transformers.util.TransformerPluginContext)}
     */
    @Override
    public void contentIsReady(TransformerPluginContext pluginContext) throws XtentisException {

        // fetch the process step
        TransformerGlobalContext globalContext = pluginContext.getTransformerGlobalContext();
        TransformerProcessStep processStep = pluginContext.getTransformerPOJO().getProcessSteps()
                .get(pluginContext.getPluginNumber());
        // TransformerPluginV2LocalInterface plugin = globalContext.getInstantiatedPlugins().get(new
        // Integer(pluginContext.getPluginNumber()));

        /*******************************************************************
         * Map the output variables to the pipeline
         *******************************************************************/
        try {

            // fetch output Mappings
            ArrayList<TransformerVariablesMapping> outputMappings = processStep.getOutputMappings();

            // first create all the pipeline variables
            for (TransformerVariablesMapping mapping : outputMappings) {
                if (mapping.getPipelineVariable() != null) {
                    String pluginvariable = mapping.getPluginVariable();
                    if (pluginvariable != null) {
                        // map the output of the plugin to the pipeline variable
                        TypedContent content = (TypedContent) pluginContext.get(pluginvariable);
                        // check content null
                        if (content != null) {
                            if (content instanceof TypedContent_Drop_Variable) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("contentIsReady() Dropping pipeline variable: " + mapping.getPipelineVariable()); //$NON-NLS-1$
                                }
                                globalContext.removeFrompipeline(mapping.getPipelineVariable());
                            } else {
                                if (LOGGER.isDebugEnabled()) {
                                    String s = content.toString();
                                    LOGGER.debug("contentIsReady() Mapping plugin variable: " + pluginvariable //$NON-NLS-1$
                                            + " -----> pipeline: " + mapping.getPipelineVariable() + "   Content: " //$NON-NLS-1$ //$NON-NLS-2$
                                            + s.substring(0, Math.min(0, s.length())) + "..."); //$NON-NLS-1$
                                }
                                globalContext.putInPipeline(mapping.getPipelineVariable(), content);
                            }
                        }
                    } else if (mapping.getHardCoding() != null) {
                        // harcode the value into the pipeline
                        if (mapping.getHardCoding() instanceof TypedContent_Drop_Variable) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("contentIsReady() Hard Dropping pipeline variable: " + mapping.getPipelineVariable()); //$NON-NLS-1$
                            }
                            globalContext.removeFrompipeline(mapping.getPipelineVariable());
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("contentIsReady() Hard Coding the result in pipeline variable: " //$NON-NLS-1$
                                        + mapping.getPipelineVariable() + "   Content: " //$NON-NLS-1$
                                        + mapping.getHardCoding().getContentType().toString().substring(0, 100) + "..."); //$NON-NLS-1$
                            }
                            globalContext.putInPipeline(mapping.getPipelineVariable(), mapping.getHardCoding());
                        }
                    } else {
                        String err = "Output Mapping incorrect for pipeline variable '" + mapping.getPipelineVariable() //$NON-NLS-1$
                                + "' in process step " + processStep.getDescription() + ": " //$NON-NLS-1$ //$NON-NLS-2$
                                + "both the plugin variable name and the hard codings are empty"; //$NON-NLS-1$
                        LOGGER.error(err);
                        throw new XtentisException(err);
                    }
                }
            }

        } catch (XtentisException e) {
            throw (e);
        }

        /*******************************************************************
         * If more plugins --> Execute the next Plugin If not, signal to the main callback that content is ready
         *******************************************************************/
        if (pluginContext.getPluginNumber() + 1 < pluginContext.getTransformerPOJO().getProcessSteps().size()) {
            executePlugin(globalContext, pluginContext.getPluginNumber() + 1);
            return;
        }

        // end of transformer branch, signal that content is ready
        if (LOGGER.isDebugEnabled()) {
            String msg = "End of current branch of Transformer '" + pluginContext.getTransformerV2POJOPK().getUniqueId() //$NON-NLS-1$
                    + "' calling execute callback"; //$NON-NLS-1$
            LOGGER.debug("contentIsReady() " + msg); //$NON-NLS-1$
        }
        globalContext.getExecuteCallBack().contentIsReady(globalContext);
    }
}