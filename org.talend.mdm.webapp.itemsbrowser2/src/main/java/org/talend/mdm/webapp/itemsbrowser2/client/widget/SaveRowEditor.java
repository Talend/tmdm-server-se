package org.talend.mdm.webapp.itemsbrowser2.client.widget;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ItemBean;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.itemsbrowser2.client.ItemsServiceAsync;
import org.talend.mdm.webapp.itemsbrowser2.client.ItemsView;
import org.talend.mdm.webapp.itemsbrowser2.client.Itemsbrowser2;
import org.talend.mdm.webapp.itemsbrowser2.client.boundary.GetService;
import org.talend.mdm.webapp.itemsbrowser2.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.itemsbrowser2.client.model.ItemResult;
import org.talend.mdm.webapp.itemsbrowser2.client.util.DateUtil;
import org.talend.mdm.webapp.itemsbrowser2.client.util.Locale;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

public class SaveRowEditor extends RowEditor<ItemBean> {

    ItemsServiceAsync service = (ItemsServiceAsync) Registry.get(Itemsbrowser2.ITEMS_SERVICE);

    @Override
    protected void onRowClick(GridEvent<ItemBean> e) {
        // cancel click Editor
    }

    @Override
    public void startEditing(int rowIndex, boolean doFocus) {
        super.startEditing(rowIndex, doFocus);
        grid.getSelectionModel().setLocked(true);

    }

    @Override
    public void stopEditing(boolean saveChanges) {
        super.stopEditing(saveChanges);
        grid.getSelectionModel().setLocked(false);
        if (saveChanges) {
            Document doc = XMLParser.createDocument();
            Map<String, Element> elementSet = new HashMap<String, Element>();

            final ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
            Map<String, Object> originalMap = itemBean.getOriginalMap();
            Map<String, TypeModel> metaType = Itemsbrowser2.getSession().getCurrentEntityModel().getMetaDataTypes();
            
            for (String index : metaType.keySet()) {
                TypeModel typeModel = metaType.get(index);
                Object value = itemBean.get(typeModel.getXpath());
                String key = typeModel.getXpath();

                if (value instanceof List) {
                    String parentPath = key.substring(0, key.lastIndexOf('/'));
                    String elName = key.substring(key.lastIndexOf('/') + 1);
                    createElements(parentPath, elName, (List) value, elementSet, doc);
                } else {
                    if (typeModel.getForeignkey() != null) {
                        String str = value.toString();
                        value = str.substring(str.lastIndexOf("-") + 1, str.length()); //$NON-NLS-1$
                    }else{
                        if(originalMap.containsKey(key)){
                            Object data = originalMap.get(key);
                            if (data instanceof Date) {
                                value = DateUtil.getDate((Date) data);
                            } else {
                                value = String.valueOf(data);
                            }
                        }
                    }
                    createElements(typeModel.getXpath(), value == null ? "" : value.toString(), elementSet, doc);//$NON-NLS-1$
                }
            }

            Element el = elementSet.get(itemBean.getConcept());
            doc.appendChild(el);
            itemBean.setItemXml(doc.toString());
            // Window.alert(itemBean.getItemXml());
            service.saveItemBean(itemBean, Locale.getLanguage(Itemsbrowser2.getSession().getAppHeader()),
                    new SessionAwareAsyncCallback<ItemResult>() {

                @Override
                protected void doOnFailure(Throwable arg0) {

                }

                public void onSuccess(ItemResult arg0) {
                    Record record = null;
                    Store<ItemBean> store = grid.getStore();
                    if (store != null) {
                        record = store.getRecord(itemBean);
                    }
                    if (arg0.getStatus() == ItemResult.SUCCESS) {
                        if (record != null) {
                            record.commit(false);
                        }
                        refreshForm(itemBean);
                        MessageBox.alert(MessagesFactory.getMessages().info_title(),
                                Locale.getExceptionMessageByLanguage(GetService.getLanguage(), arg0.getDescription()), null);
                    } else if (arg0.getStatus() == ItemResult.FAILURE) {
                        if (record != null) {
                            record.reject(false);
                        }
                        String str = arg0.getDescription().replaceAll("\\[", "{").replaceAll("\\]", "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        MessageBox.alert(MessagesFactory.getMessages().error_title(),
                                Locale.getExceptionMessageByLanguage(GetService.getLanguage(), str), null);
                    }
                }
            });
        }
    }

    private void refreshForm(ItemBean itemBean) {
        if (!Itemsbrowser2.getSession().getAppHeader().isUsingDefaultForm()) {
            ItemsSearchContainer itemsSearchContainer = Registry.get(ItemsView.ITEMS_SEARCH_CONTAINER);
            itemsSearchContainer.getItemsFormPanel().getElement().getStyle().setOverflow(Overflow.AUTO);
            itemsSearchContainer.getItemsFormPanel().reSize();
            GetService.renderFormWindow(itemBean.getIds(), itemBean.getConcept(), false, itemsSearchContainer.getItemsFormPanel()
                    .getElement(), true, false, false);
        }
    }

    private void createElements(String xpath, String value, Map<String, Element> elementSet, Document doc) {
        Element parent = null;
        String[] xps = xpath.split("/"); //$NON-NLS-1$
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        for (String xp : xps) {
            if (isFirst) {
                sb.append(xp);
                isFirst = false;
            } else {
                sb.append("/" + xp); //$NON-NLS-1$
            }
            Element tempEl = elementSet.get(sb.toString());
            if (tempEl == null) {
                tempEl = (Element) doc.createElement(xp);
                elementSet.put(sb.toString(), tempEl);
            }
            if (parent != null) {
                parent.appendChild(tempEl);
            }
            parent = tempEl;
        }
        parent.appendChild(doc.createTextNode(value));
    }

    public void createElements(String xpath, String elName, List value, Map<String, Element> elementSet, Document doc) {
        Element parent = null;
        String[] xps = xpath.split("/");//$NON-NLS-1$
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        for (String xp : xps) {
            if (isFirst) {
                sb.append(xp);
                isFirst = false;
            } else {
                sb.append("/" + xp);//$NON-NLS-1$
            }
            Element tempEl = elementSet.get(sb.toString());
            if (tempEl == null) {
                tempEl = (Element) doc.createElement(xp);
                elementSet.put(sb.toString(), tempEl);
            }
            if (parent != null) {
                parent.appendChild(tempEl);
            }
            parent = tempEl;
        }
        for (Object o : value) {
            Element el = doc.createElement(elName);
            el.appendChild(doc.createTextNode(o.toString()));
            parent.appendChild(el);
        }
    }
}
