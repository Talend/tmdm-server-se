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

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.widget.ImagePreviewWindow;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.PropertyEditor;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Accessibility;
import com.google.gwt.user.client.ui.Image;

public class PictureField extends TextField<String> {

    private static final int DEFAULT_IMAGE_SCALE_SIZE = 150;

    public static final String DefaultImage = "secure/gxt/resources/images/talend/no_image.png"; //$NON-NLS-1$

    protected El wrap = new El(DOM.createSpan());

    Image image = new Image(DefaultImage);

    protected El input = new El(image.getElement());

    protected Element delHandler = new Image(Icons.INSTANCE.clear_icon()).getElement();

    protected Element addHandler = new Image(Icons.INSTANCE.image_add()).getElement();

    private EditWindow editWindow = new EditWindow();

    private boolean readOnly;

    private boolean isMandatory;

    private String imagePath;

    private Dialog dialog = new Dialog() {

        @Override
        protected void onButtonPressed(Button button) {
            super.onButtonPressed(button);
            if (button == getButtonBar().getItemByItemId(YES)) {

                // Only delete it from client side
                setValue(DataTypeConstants.PICTURE.getDefaultValue() == null ? null : (String) DataTypeConstants.PICTURE
                        .getDefaultValue());// reset value when delete

                dialog.hide();

                /*
                 * RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET,
                 * "/imageserver/secure/ImageDeleteServlet?uri=" + value);//$NON-NLS-1$
                 * 
                 * reqBuilder.setCallback(new RequestCallback() {
                 * 
                 * public void onResponseReceived(Request request, Response response) { String json =
                 * response.getText(); JSONObject jsObject = JSONParser.parse(json).isObject(); JSONBoolean success =
                 * jsObject.get("success").isBoolean(); //$NON-NLS-1$ JSONString message =
                 * jsObject.get("message").isString(); //$NON-NLS-1$ boolean succeed = success.booleanValue();
                 * MessageBox.alert(succeed ? MessagesFactory.getMessages().message_success() : MessagesFactory
                 * .getMessages().message_fail(), message.stringValue(), null); if (succeed) { setValue(null); }
                 * dialog.hide(); }
                 * 
                 * public void onError(Request request, Throwable exception) {
                 * MessageBox.alert(MessagesFactory.getMessages().error_title(), exception.getMessage(), null); } });
                 * try { reqBuilder.send(); } catch (RequestException e) { MessageBox.alert("RequestException",
                 * e.getMessage(), null); //$NON-NLS-1$ }
                 */

            } else if (button == getButtonBar().getItemByItemId(NO)) {
                dialog.hide();
            }
        }
    };

    public PictureField(boolean isMandatory) {
        setFireChangeEventOnSetValue(true);
        regJs(delHandler);
        regJs(addHandler);
        dialog.setHeading(MessagesFactory.getMessages().confirm_title());
        dialog.addText(MessagesFactory.getMessages().confirm_delete_img());
        dialog.setModal(true);
        dialog.setBlinkModal(true);
        dialog.setButtons(Dialog.YESNO);

        propertyEditor = new PropertyEditor<String>() {

            @Override
            public String getStringValue(String value) {
                return value;
            }

            @Override
            public String convertStringValue(String value) {
                return PictureField.this.value;
            }
        };

        image.addErrorHandler(new ErrorHandler() {

            @Override
            public void onError(ErrorEvent event) {
                image.setUrl(DefaultImage);
            }
        });

        image.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                com.google.gwt.dom.client.Element element = event.getRelativeElement();
                if (element == image.getElement() && !isInternalImageURL(image.getUrl())) {
                    int width = image.getWidth();
                    int height = image.getHeight();
                    int size = DEFAULT_IMAGE_SCALE_SIZE;
                    if (width > 0 && width > height) {
                        if (width > size) {
                            image.setPixelSize(size, (height * size / width));
                        }
                    } else if (height > size) {
                        image.setPixelSize((width * size / height), size);
                    }
                }
            }
        });

        image.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if (imagePath != null) {
                    new ImagePreviewWindow(imagePath);
                }
            }
        });

        this.setStyleAttribute("cursor", "pointer"); //$NON-NLS-1$ //$NON-NLS-2$
        this.isMandatory = isMandatory;
    }

    public PictureField() {
        this(false);
    }

    @Override
    protected void onRender(Element target, int index) {
        input.setId(XDOM.getUniqueId());
        input.makePositionable();

        input.dom.getStyle().setMarginRight(5, Unit.PX);
        wrap.dom.appendChild(input.dom);
        wrap.dom.appendChild(delHandler);
        wrap.dom.appendChild(addHandler);

        setElement(wrap.dom, target, index);
        ComponentHelper.doAttach(image);
        super.onRender(target, index);
    }

    private void handlerClick(Element el) {
        if (!readOnly) {
            if (el == addHandler) {
                editWindow.show();
            } else if (el == delHandler) {
                dialog.show();
            }
        }
    }

    private native void regJs(Element el)/*-{
		var instance = this;
		el.onclick = function() {
			instance.@org.talend.mdm.webapp.browserecords.client.widget.inputfield.PictureField::handlerClick(Lcom/google/gwt/user/client/Element;)(this);
		};
    }-*/;

    @Override
    public void setValue(String value) {
        if (isMandatory) {
            if (BrowseRecords.getSession().getAppHeader().isAutoValidate()) {
                if (value != null && value.length() != 0) {
                    this.setAllowBlank(true);
                    super.setValue(value);
                    this.validate();
                } else {
                    this.setAllowBlank(false);
                    super.setValue(""); //$NON-NLS-1$
                    this.validate();
                }
            }
        }

        // external source
        if (value != null && (value.toLowerCase().startsWith("http") || value.toLowerCase().startsWith("https"))) { //$NON-NLS-1$//$NON-NLS-2$
            image.setUrl(value);
            return;
        }

        String oldValue = this.value;

        if (value != null && value.length() != 0) {
            if (value.startsWith("/imageserver")) { //$NON-NLS-1$
                value = value.substring(1);
            }
            if (!value.startsWith("imageserver")) { //$NON-NLS-1$
                value = "imageserver/" + value; //$NON-NLS-1$
            }

            this.value = value;
            image.setUrl(scaleInternalUrl(this.value, DEFAULT_IMAGE_SCALE_SIZE));
            imagePath = this.value;

        } else {
            this.value = value;
            image.setUrl(DefaultImage);
        }

        if (isFireChangeEventOnSetValue()) {
            fireChangeEvent(oldValue, value);
        }
    }

    public String getImageURL() {
        return image.getUrl();
    }

    private boolean isInternalImageURL(String url) {
        if (url == null || url.trim().length() == 0) {
            return false;
        }

        return CommonUtil.getHost(url).equals(com.google.gwt.user.client.Window.Location.getHost());
    }

    private String scaleInternalUrl(String inputValue, int size) {

        if (inputValue == null || inputValue.trim().length() == 0) {
            return inputValue;
        }
        image.getElement().removeAttribute("width"); //$NON-NLS-1$
        image.getElement().removeAttribute("height"); //$NON-NLS-1$
        return inputValue += "?width=" + size + "&height=" + size + "&preserveAspectRatio=true"; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        this.readOnly = readOnly;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    class EditWindow extends Window {

        private FormPanel editForm = new FormPanel();

        private FileUploadField file = new FileUploadField();

        private TextField<String> name;

        PictureSelector pictureSelector = new PictureSelector(EditWindow.this, PictureField.this);

        private SelectionListener<ButtonEvent> listener = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button button = ce.getButton();
                if (button == uploadButton) {
                    if (editForm.isValid()) {
                        if (name.getValue().contains("#")) { //$NON-NLS-1$
                            name.setValue(name.getValue().replaceAll("#", "$")); //$NON-NLS-1$//$NON-NLS-2$
                        }
                        editForm.submit();
                    }
                } else if (button == resetButton) {
                    editForm.reset();
                }
            }
        };

        private Button uploadButton = new Button(MessagesFactory.getMessages().button_upload(), listener);

        private Button resetButton = new Button(MessagesFactory.getMessages().button_reset(), listener);

        public EditWindow() {
            super();
            this.setLayout(new FitLayout());
            this.setHeading(MessagesFactory.getMessages().picture_select_title());
            this.setSize(540, 450);
            this.setResizable(true);
            this.setModal(true);
            this.setBlinkModal(true);

            TabPanel uploadTabPanel = new TabPanel();
            TabItem localTabItem = new TabItem(MessagesFactory.getMessages().picture_upload_local_title());
            ContentPanel localContentPanel = new ContentPanel();

            FormData formData = new FormData("85%"); //$NON-NLS-1$
            editForm.setEncoding(FormPanel.Encoding.MULTIPART);
            editForm.setMethod(FormPanel.Method.POST);
            editForm.setAction("imageserver/ImageUploadServlet"); //$NON-NLS-1$
            editForm.setHeaderVisible(false);
            editForm.setBodyBorder(false);
            editForm.setLabelWidth(110);

            MultiField<String> imgIdRow = new MultiField<String>();
            imgIdRow.setFieldLabel(MessagesFactory.getMessages().picture_field_imgid());

            name = new TextField<String>();
            name.setFieldLabel(""); //$NON-NLS-1$
            name.setName("fileName"); //$NON-NLS-1$
            name.setAllowBlank(false);

            imgIdRow.add(name);
            final LabelField extFileNameLabel = new LabelField();
            imgIdRow.add(extFileNameLabel);

            MultiField<String> catalogRow = new MultiField<String>();
            catalogRow.setFieldLabel(MessagesFactory.getMessages().picture_field_imgcatalog());

            final TextField<String> catalog = new TextField<String>();
            catalog.setFieldLabel(""); //$NON-NLS-1$
            catalog.setName("catalogName"); //$NON-NLS-1$

            catalogRow.add(catalog);
            catalogRow.add(new LabelField());

            file.setAllowBlank(false);
            file.setName("imageFile");//$NON-NLS-1$
            file.setFieldLabel(MessagesFactory.getMessages().picture_field_label());
            file.setFireChangeEventOnSetValue(true);
            file.addListener(Events.Change, new Listener<FieldEvent>() {

                @Override
                public void handleEvent(FieldEvent be) {
                    // reset imgId
                    catalog.setValue(""); //$NON-NLS-1$
                    name.setValue(""); //$NON-NLS-1$
                    // auto fill img id
                    if ((name.getValue() == null || name.getValue().isEmpty())
                            && (file.getValue() != null && !file.getValue().isEmpty())) {
                        String[] parsedFileName = CommonUtil.parseFileName(file.getValue());
                        name.setValue(parsedFileName[0]);
                        extFileNameLabel.setText(parsedFileName[1].length() == 0 ? "" : "." + parsedFileName[1]); //$NON-NLS-1$ //$NON-NLS-2$
                        // name.focus();
                    }
                }

            });

            editForm.add(file, formData);
            editForm.add(catalogRow, formData);
            editForm.add(imgIdRow, formData);
            editForm.addListener(Events.Submit, new Listener<FormEvent>() {

                @Override
                public void handleEvent(FormEvent be) {
                    String json = be.getResultHtml();
                    JSONObject jsObject = JSONParser.parse(json).isObject();
                    JSONBoolean success = jsObject.get("success").isBoolean(); //$NON-NLS-1$
                    JSONString message = jsObject.get("message").isString(); //$NON-NLS-1$
                    if (success.booleanValue()) {
                        MessageBox.alert(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                                .upload_pic_ok(), null);
                    } else {
                        MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                                .upload_pic_fail(), null);
                    }
                    if (success.booleanValue()) {
                        setValue(message.stringValue());
                    } else {
                        setValue(null);
                    }
                    EditWindow.this.hide();
                }

            });

            localContentPanel.setStyleAttribute("margin", "20px"); //$NON-NLS-1$//$NON-NLS-2$
            localContentPanel.add(editForm);
            editForm.setLayout(new FormLayout());
            localContentPanel.setButtonAlign(HorizontalAlignment.CENTER);
            localContentPanel.addButton(uploadButton);
            localContentPanel.addButton(resetButton);
            localContentPanel.setHeaderVisible(false);
            localTabItem.add(localContentPanel);
            uploadTabPanel.add(localTabItem);

            TabItem remoteTabItem = new TabItem(MessagesFactory.getMessages().picture_upload_remote_title());
            remoteTabItem.setLayout(new FitLayout());
            remoteTabItem.add(pictureSelector);
            remoteTabItem.addListener(Events.Show, new Listener() {

                @Override
                public void handleEvent(BaseEvent be) {
                    pictureSelector.refresh();
                }

            });
            uploadTabPanel.add(remoteTabItem);
            add(uploadTabPanel);
        }

        @Override
        protected void onShow() {
            super.onShow();
            pictureSelector.refresh();
            editForm.reset();
        }
    }

    @Override
    protected void setAriaState(String stateName, String stateValue) {
        Accessibility.setState(wrap.dom, stateName, stateValue);
    }
}
