/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DomEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.PreviewEvent;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;

/**
 * Provides a convenient wrapper for NumberField that adds a clickable dual trigger button (looks like a JSpinner in
 * java Swing).
 * 
 * <dl>
 * <dt><b>Events:</b></dt>
 * 
 * <dd><b>TriggerClick</b> : FieldEvent(field, event)<br>
 * <div>Fires after the trigger is clicked or keys up & down used.</div>
 * <ul>
 * <li>field : this</li>
 * <li>event : event</li>
 * </ul>
 * </dd>
 * </dl>
 * 
 * <dl>
 * <dt>Inherited Events:</dt>
 * <dd>Field Focus</dd>
 * <dd>Field Blur</dd>
 * <dd>Field Change</dd>
 * <dd>Field Invalid</dd>
 * <dd>Field Valid</dd>
 * <dd>Field KeyPress</dd>
 * <dd>Field SpecialKey</dd>
 * <dt>Custom Events:</dt>
 * <dd>Field SpinnerUpClick</dd>
 * <dd>Field SpinnerDownClick</dd>
 * </dl>
 * 
 * <code><pre>
 * SpinnerField spinnerInteger = new SpinnerField();
 * spinnerInteger.setFieldLabel("Spinner Integer");
 * spinnerInteger.setStepValue(Integer.valueOf(1));
 * spinnerInteger.setPropertyEditorType(Integer.class);
 * spinnerInteger.setFormat(NumberFormat.getFormat("##0"));
 * spinnerInteger.setAutoValidate(true);
 * spinnerInteger.setAllowDecimals(false);
 * spinnerInteger.setAllowBlank(false);
 * spinnerInteger.setMaxValue(100);
 * spinnerInteger.setMinValue(1);
 * spinnerInteger.setValue(45);
 * 
 * SpinnerField spinnerDouble = new SpinnerField();
 * spinnerDouble.setFieldLabel("Spinner Double");
 * spinnerDouble.setStepValue(Double.valueOf(0.1d));
 * spinnerDouble.setPropertyEditorType(Double.class);
 * spinnerDouble.setFormat(NumberFormat.getFormat("##0.00"));
 * spinnerDouble.setAutoValidate(true);
 * spinnerDouble.setAllowDecimals(true);
 * spinnerDouble.setAllowBlank(false);
 * spinnerDouble.setMaxValue(Double.valueOf(100.0d));
 * spinnerDouble.setMinValue(Double.valueOf(1.0d));
 * spinnerDouble.setValue(Double.valueOf(45.0d));
 * 
 * </pre></code>
 * 
 */
public class SpinnerField extends NumberField {

    protected BaseEventPreview focusEventPreview;

    protected El spinner_up;

    protected El spinner_down;

    protected String triggerStyle = "x-form-spinner-arrow";//$NON-NLS-1$

    protected boolean mimicing;

    private boolean editable = true;

    private boolean monitorTab = true;

    private boolean hideTrigger;

    public static final EventType SpinnerUpClick = new EventType();

    public static final EventType SpinnerDownClick = new EventType();

    private Number stepValue = 1.0d;

    private final TimerSpin timer = new TimerSpin();

    public SpinnerField() {
        super();
        ensureVisibilityOnSizing = true;
    }

    public void setHeight(int height) {
        super.setHeight(height);
        spinner_up.setHeight(height / 2);
        spinner_down.setHeight(height / 2);
    }
    public Number getStepValue() {
        return stepValue;
    }

    public void setStepValue(Number stepValue) {
        this.stepValue = stepValue;
    }

    /**
     * Returns the trigger style.
     * 
     * @return the trigger style
     */
    public String getTriggerStyle() {
        return triggerStyle;
    }

    /**
     * Returns true if the combo is editable.
     * 
     * @return true if editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Returns true if the trigger is hidden.
     * 
     * @return the hide trigger state
     */
    public boolean isHideTrigger() {
        return hideTrigger;
    }

    public boolean isMonitorTab() {
        return monitorTab;
    }

    public void onComponentEvent(ComponentEvent ce) {
        super.onComponentEvent(ce);
        int type = ce.getEventTypeInt();
        if (ce.getTarget() == spinner_up.dom) {
            switch (type) {
            case Event.ONCLICK:
                onSpinnerUpClick(ce);
                break;
            case Event.ONMOUSEDOWN:
                timer.setComponentEvent(ce);
                timer.setEventType(SpinnerUpClick);
                timer.setSpinnerField(this);
                timer.scheduleRepeating(250);
                break;
            case Event.ONMOUSEUP:
                timer.cancel();
                break;
            }
        } else if (ce.getTarget() == spinner_down.dom) {
            switch (type) {
            case Event.ONCLICK:
                onSpinnerDownClick(ce);
                break;
            case Event.ONMOUSEDOWN:
                timer.setComponentEvent(ce);
                timer.setEventType(SpinnerDownClick);
                timer.setSpinnerField(this);
                timer.scheduleRepeating(250);
                break;
            case Event.ONMOUSEUP:
                timer.cancel();
                break;
            }
        } else if (ce.getTarget() == input.dom) {
            switch (type) {
            case Event.ONKEYDOWN:
                if (ce.getKeyCode() == KeyCodes.KEY_UP) {
                    timer.cancel();
                    onSpinnerUpClick(ce);
                } else if (ce.getKeyCode() == KeyCodes.KEY_DOWN) {
                    timer.cancel();
                    onSpinnerDownClick(ce);
                }
                break;
            case Event.ONKEYUP:
                if (ce.getKeyCode() == KeyCodes.KEY_UP) {
                    timer.cancel();
                } else if (ce.getKeyCode() == KeyCodes.KEY_DOWN) {
                    timer.cancel();
                }
                break;
            }
        }
    }

    /**
     * Allow or prevent the user from directly editing the field text. If false is passed, the user will only be able to
     * select from the items defined in the dropdown list.
     * 
     * @param editable true to allow the user to directly edit the field text
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        if (rendered) {
            El fromEl = getInputEl();
            if (!readOnly) {
                fromEl.dom.setPropertyBoolean("readOnly", !editable);//$NON-NLS-1$
            }
            fromEl.setStyleName("x-triggerfield-noedit", !editable);//$NON-NLS-1$
        }
    }

    /**
     * True to hide the trigger (defaults to false, pre-render).
     * 
     * @param hideTrigger true to hide the trigger
     */
    public void setHideTrigger(boolean hideTrigger) {
        this.hideTrigger = hideTrigger;
    }

    public void setMonitorTab(boolean monitorTab) {
        this.monitorTab = monitorTab;
    }

    /**
     * Sets the trigger style name.
     * 
     * @param triggerStyle
     */
    public void setTriggerStyle(String triggerStyle) {
        this.triggerStyle = triggerStyle;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (rendered) {
            el().setStyleName(readOnlyFieldStyle, readOnly);
            if (editable || (readOnly && !editable)) {
                getInputEl().dom.setPropertyBoolean("readOnly", readOnly);//$NON-NLS-1$
            }
        }
    }

    protected Size adjustInputSize() {
        return new Size(spinner_up.getWidth(), 0);
    }

    protected void afterRender() {
        super.afterRender();
        addStyleOnOver(spinner_up.dom, "x-form-trigger-over");//$NON-NLS-1$
        removeStyleName(fieldStyle);
    }

    protected void beforeBlur() {
    }

    protected void mimicBlur(PreviewEvent e, Element target) {
        if (!el().dom.isOrHasChild(target) && validateBlur(e, target)) {
            triggerBlur(null);
        }
    }

    protected void onKeyDown(FieldEvent fe) {
        super.onKeyDown(fe);
        if (monitorTab && fe.getKeyCode() == KeyCodes.KEY_TAB) {
            triggerBlur(fe);
        }
    }

    protected void onBlur(ComponentEvent ce) {
    }

    protected void onClick(ComponentEvent ce) {
        if (!readOnly && !editable && getInputEl().dom.isOrHasChild(ce.getTarget())) {
            onTriggerClick(ce);
            return;
        }
        super.onClick(ce);
    }

    protected void onDisable() {
        super.onDisable();
        addStyleName("x-item-disabled");//$NON-NLS-1$
    }

    protected void onEnable() {
        super.onEnable();
        removeStyleName("x-item-disabled");//$NON-NLS-1$
    }

    protected void onFocus(ComponentEvent ce) {
        super.onFocus(ce);
        if (!mimicing) {
            addStyleName("x-spinner-wrap-focus");//$NON-NLS-1$
            mimicing = true;
            focusEventPreview.add();
        }
    }

    protected void onSpinnerUpClick(ComponentEvent ce) {
        if (getValue() == null || isValid() == false) {
            return;
        }
        Number newValue = getValue().doubleValue() + getStepValue().doubleValue();
        if (newValue.doubleValue() <= getMaxValue().doubleValue()) {
            setValue(newValue);
            fireEvent(SpinnerUpClick, ce);
        }
    }

    protected void onSpinnerDownClick(ComponentEvent ce) {
        if (getValue() == null || isValid() == false) {
            return;
        }
        Number newValue = getValue().doubleValue() - getStepValue().doubleValue();
        if (newValue.doubleValue() >= getMinValue().doubleValue()) {
            setValue(newValue);
            fireEvent(SpinnerDownClick, ce);
        }
    }

    protected void onRender(Element target, int index) {

        focusEventPreview = new BaseEventPreview() {

            protected boolean onAutoHide(final PreviewEvent ce) {
                if (ce.getEventTypeInt() == Event.ONMOUSEDOWN) {
                    mimicBlur(ce, ce.getTarget());
                }
                return false;
            }
        };

        if (el() != null) {
            super.onRender(target, index);
            return;
        }

        setElement(DOM.createDiv(), target, index);

        input = new El(DOM.createInputText());

        addStyleName("x-form-field-wrap");//$NON-NLS-1$

        input.addStyleName(fieldStyle);

        spinner_up = new El(DOM.createImg());
        spinner_up.dom.setClassName("x-form-spinner-up " + triggerStyle);//$NON-NLS-1$
        spinner_up.dom.setPropertyString("src", GXT.BLANK_IMAGE_URL);//$NON-NLS-1$

        spinner_down = new El(DOM.createImg());
        spinner_down.dom.setClassName("x-form-spinner-down " + triggerStyle);//$NON-NLS-1$
        spinner_down.dom.setPropertyString("src", GXT.BLANK_IMAGE_URL);//$NON-NLS-1$

        el().appendChild(input.dom);
        el().appendChild(spinner_up.dom);
        el().appendChild(spinner_down.dom);

        if (hideTrigger) {
            spinner_up.setVisible(false);
            spinner_down.setVisible(false);
        }

        super.onRender(target, index);

        if (!editable) {
            setEditable(false);
        }
    }

    protected void onResize(int width, int height) {
        super.onResize(width, height);
        if (GXT.isIE && !hideTrigger) {
            int y;
            if ((y = input.getY()) != spinner_up.getY()) {
                spinner_up.setY(y);
            }
        }
    }

    protected void onTriggerClick(ComponentEvent ce) {
        fireEvent(Events.TriggerClick, ce);
    }

    protected void triggerBlur(ComponentEvent ce) {
        mimicing = false;
        focusEventPreview.remove();
        beforeBlur();
        removeStyleName("x-spinner-wrap-focus");//$NON-NLS-1$
        super.onBlur(ce);
    }

    protected boolean validateBlur(DomEvent ce, Element target) {
        return true;
    }

    private static class TimerSpin extends Timer {

        private ComponentEvent componentEvent;

        private EventType eventType;

        private SpinnerField spinnerField;

        private int count = 0;

        @SuppressWarnings("unused")//$NON-NLS-1$
        public ComponentEvent getComponentEvent() {
            return componentEvent;
        }

        public void setComponentEvent(ComponentEvent componentEvent) {
            this.componentEvent = componentEvent;
        }

        @SuppressWarnings("unused")//$NON-NLS-1$
        public EventType getEventType() {
            return eventType;
        }

        public void setEventType(EventType eventType) {
            this.eventType = eventType;
        }

        @SuppressWarnings("unused")//$NON-NLS-1$
        public SpinnerField getSpinnerField() {
            return spinnerField;
        }

        public void setSpinnerField(SpinnerField spinnerField) {
            this.spinnerField = spinnerField;
        }

        public void cancel() {
            super.cancel();
            count = 0;
        }

        public void run() {
            count++;
            if (eventType == SpinnerUpClick)
                spinnerField.onSpinnerUpClick(componentEvent);
            else if (eventType == SpinnerDownClick)
                spinnerField.onSpinnerDownClick(componentEvent);
            if (count == 2)
                scheduleRepeating(20);
        }

    }

}
