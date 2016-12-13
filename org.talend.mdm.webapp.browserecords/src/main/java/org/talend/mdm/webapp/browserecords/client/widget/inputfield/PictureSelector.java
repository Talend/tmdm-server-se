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

import java.util.LinkedList;
import java.util.List;

import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.util.ImageUtil;
import org.talend.mdm.webapp.base.client.widget.PagingToolBarEx;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.dnd.ListViewDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class PictureSelector extends ContentPanel {

    private ListLoader<ListLoadResult<BeanModel>> imageloader;

    final private PagingToolBarEx pagingBar = new PagingToolBarEx(10);

    final private TextField<String> searchFiled = new TextField<String>();

    private List<org.talend.mdm.webapp.base.client.model.Image> result;

    private List<org.talend.mdm.webapp.base.client.model.Image> all;

    public PictureSelector(final Window parentWindow, final PictureField targetField) {

        RpcProxy<BasePagingLoadResult<org.talend.mdm.webapp.base.client.model.Image>> proxy = new RpcProxy<BasePagingLoadResult<org.talend.mdm.webapp.base.client.model.Image>>() {

            @Override
            protected void load(Object loadConfig,
                    AsyncCallback<BasePagingLoadResult<org.talend.mdm.webapp.base.client.model.Image>> callback) {
                PagingLoadConfig pagingLoadConfig = (PagingLoadConfig) loadConfig;
                List<org.talend.mdm.webapp.base.client.model.Image> pagingList = new LinkedList<org.talend.mdm.webapp.base.client.model.Image>();
                int start = pagingLoadConfig.getOffset();
                int limit = result.size();
                if (pagingBar.getPageSize() > 0) {
                    limit = (start + pagingBar.getPageSize() < limit ? start + pagingBar.getPageSize() : limit);
                }
                for (int i = pagingLoadConfig.getOffset(); i < limit; i++) {
                    pagingList.add(result.get(i));
                }
                callback.onSuccess(new BasePagingLoadResult<org.talend.mdm.webapp.base.client.model.Image>(pagingList,
                        ((PagingLoadConfig) loadConfig).getOffset(), result.size()));
            }
        };

        final PagingLoader<PagingLoadResult<ItemBaseModel>> loader = new BasePagingLoader<PagingLoadResult<ItemBaseModel>>(proxy);
        final ListStore<ItemBaseModel> store = new ListStore<ItemBaseModel>(loader);

        LayoutContainer container = new LayoutContainer();
        BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 200, 100, 300);
        HorizontalPanel searchPanel = new HorizontalPanel();
        searchFiled.setWidth(240);
        searchFiled.addKeyListener(new KeyListener() {

            @Override
            public void componentKeyUp(ComponentEvent event) {
                if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                    // When user press Enter key, perform the search.
                    doSearch(loader);
                }
            }

        });

        Button searchButton = new Button(MessagesFactory.getMessages().search_btn());
        searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                doSearch(loader);
            }
        });
        searchPanel.add(searchFiled);
        searchPanel.add(searchButton);
        searchPanel.setSpacing(10);
        searchPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        container.add(searchPanel, northData);

        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER, 200, 100, 300);

        ListView<ItemBaseModel> view = new ListView<ItemBaseModel>() {

            @Override
            protected ItemBaseModel prepareData(ItemBaseModel model) {
                org.talend.mdm.webapp.base.client.model.Image image = (org.talend.mdm.webapp.base.client.model.Image) model;
                model.set("shortName", Format.ellipse(image.getName(), 15)); //$NON-NLS-1$
                model.set("url", image.getUri() + "?width=80&height=60&randomNum=" + Math.random()); //$NON-NLS-1$ //$NON-NLS-2$ 
                return model;
            }
        };

        view.setId("img-chooser-view"); //$NON-NLS-1$
        view.setTemplate(getTemplate());
        view.setBorders(false);
        view.setStore(store);
        view.setItemSelector("div.thumb-wrap"); //$NON-NLS-1$ 
        view.setStateful(true);

        view.addListener(Events.OnDoubleClick, new Listener<ListViewEvent<ModelData>>() {

            public void handleEvent(ListViewEvent<ModelData> be) {
                org.talend.mdm.webapp.base.client.model.Image image = (org.talend.mdm.webapp.base.client.model.Image) be
                        .getModel();
                targetField.setValue(image.getUri().replace(ImageUtil.IMAGE_PATH, "")); //$NON-NLS-1$
                parentWindow.hide();
            }
        });

        setHeaderVisible(false);
        new ListViewDragSource(view);
        container.add(view, centerData);
        add(container);
        pagingBar.bind(loader);
        this.setBottomComponent(pagingBar);
        setBorders(false);
        setLayout(new FitLayout());
        setScrollMode(Scroll.AUTOY);

        RpcProxy<List<org.talend.mdm.webapp.base.client.model.Image>> imageProxy = new RpcProxy<List<org.talend.mdm.webapp.base.client.model.Image>>() {

            @Override
            protected void load(Object loadConfig,
                    final AsyncCallback<List<org.talend.mdm.webapp.base.client.model.Image>> callback) {

                RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, Location.getPath() + "/../"
                        + ImageUtil.IMAGE_SERVER_PATH);
                requestBuilder.setCallback(new RequestCallback() {

                    public void onResponseReceived(Request request, Response response) {
                        try {
                            List<org.talend.mdm.webapp.base.client.model.Image> images = ImageUtil.getImages(response.getText());
                            result = all = images;
                            loader.load();
                        } catch (Exception e) {
                            MessageBox.alert(MessagesFactory.getMessages().error_title(), e.getMessage(), null);
                        }
                    }

                    public void onError(Request request, Throwable exception) {
                        MessageBox.alert(MessagesFactory.getMessages().error_title(), exception.getMessage(), null);
                    }
                });
                try {
                    requestBuilder.send();
                } catch (RequestException e) {
                    MessageBox.alert("RequestException", e.getMessage(), null); //$NON-NLS-1$
                }
            }
        };

        imageloader = new BaseListLoader<ListLoadResult<BeanModel>>(imageProxy, new BeanModelReader());
    }

    private void doSearch(PagingLoader<PagingLoadResult<ItemBaseModel>> loader) {
        if (searchFiled.getValue() == null || "".equals(searchFiled.getValue())) { //$NON-NLS-1$
            result = all;
        } else {
            result = new LinkedList<org.talend.mdm.webapp.base.client.model.Image>();
            for (org.talend.mdm.webapp.base.client.model.Image image : all) {
                // no case sensitive
                if (image.getName() != null && image.getName().toLowerCase().contains(searchFiled.getValue().toLowerCase())) {
                    result.add(image);
                }
            }
        }
        pagingBar.first();
    }

    public void refresh() {
        searchFiled.setValue(""); //$NON-NLS-1$
        imageloader.load();
    }

    private native String getTemplate() /*-{
		return [
				'<tpl for=".">',
				'<div class="thumb-wrap" id="{name}" style="border: 1px solid white">',
				'<div class="thumb"><img src="{url}" title="{name}"></div>',
				'<span>{shortName}</span></div>', '</tpl>' ].join("");
    }-*/;

}
