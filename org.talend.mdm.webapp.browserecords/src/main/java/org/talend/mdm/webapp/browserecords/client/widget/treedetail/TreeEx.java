/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.AbstractImagePrototype.ImagePrototypeElement;
import com.google.gwt.user.client.ui.Accessibility;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Tree.Resources;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.Widget;


@SuppressWarnings("deprecation")
public class TreeEx extends Widget implements HasSelectionHandlers<TreeItemEx>, HasWidgets, HasOpenHandlers<TreeItemEx>,
        HasCloseHandlers<TreeItemEx> {
    static class ImageAdapter {

        private static final Resources DEFAULT_RESOURCES = GWT.create(Resources.class);

        private final AbstractImagePrototype treeClosed;

        private final AbstractImagePrototype treeLeaf;

        private final AbstractImagePrototype treeOpen;

        public ImageAdapter() {
            this(DEFAULT_RESOURCES);
        }

        public ImageAdapter(Resources resources) {
            treeClosed = AbstractImagePrototype.create(resources.treeClosed());
            treeLeaf = AbstractImagePrototype.create(resources.treeLeaf());
            treeOpen = AbstractImagePrototype.create(resources.treeOpen());
        }

        public ImageAdapter(TreeImages images) {
            treeClosed = images.treeClosed();
            treeLeaf = images.treeLeaf();
            treeOpen = images.treeOpen();
        }

        public AbstractImagePrototype treeClosed() {
            return treeClosed;
        }

        public AbstractImagePrototype treeLeaf() {
            return treeLeaf;
        }

        public AbstractImagePrototype treeOpen() {
            return treeOpen;
        }
    }

    private static final int OTHER_KEY_DOWN = 63233;

    private static final int OTHER_KEY_LEFT = 63234;

    private static final int OTHER_KEY_RIGHT = 63235;

    private static final int OTHER_KEY_UP = 63232;

    private static boolean isArrowKey(int code) {
        switch (code) {
        case OTHER_KEY_DOWN:
        case OTHER_KEY_RIGHT:
        case OTHER_KEY_UP:
        case OTHER_KEY_LEFT:
        case KeyCodes.KEY_DOWN:
        case KeyCodes.KEY_RIGHT:
        case KeyCodes.KEY_UP:
        case KeyCodes.KEY_LEFT:
            return true;
        default:
            return false;
    }
    }

    /**
     * Normalized key codes. Also switches KEY_RIGHT and KEY_LEFT in RTL languages.
     */
    private static int standardizeKeycode(int code) {
        switch (code) {
        case OTHER_KEY_DOWN:
            code = KeyCodes.KEY_DOWN;
            break;
        case OTHER_KEY_RIGHT:
            code = KeyCodes.KEY_RIGHT;
            break;
        case OTHER_KEY_UP:
            code = KeyCodes.KEY_UP;
            break;
        case OTHER_KEY_LEFT:
            code = KeyCodes.KEY_LEFT;
            break;
        }
        if (LocaleInfo.getCurrentLocale().isRTL()) {
            if (code == KeyCodes.KEY_RIGHT) {
                code = KeyCodes.KEY_LEFT;
            } else if (code == KeyCodes.KEY_LEFT) {
                code = KeyCodes.KEY_RIGHT;
            }
        }
        return code;
    }

    private final Map<Widget, TreeItemEx> childWidgets = new HashMap<Widget, TreeItemEx>();

    private TreeItemEx curSelection;

    private ImageAdapter images;

    private String indentValue;

    private boolean isAnimationEnabled = false;

    private TreeItemEx root;

    private boolean useLeafImages;

    /**
     * Constructs an empty tree.
     */
    public TreeEx() {
        init(new ImageAdapter(), false);
    }

    public TreeEx(Resources resources) {
        init(new ImageAdapter(resources), false);
    }

    public TreeEx(Resources resources, boolean useLeafImages) {
        init(new ImageAdapter(resources), useLeafImages);
    }

    @Deprecated
    public TreeEx(TreeImages images) {
        init(new ImageAdapter(images), false);
    }

    @Deprecated
    public TreeEx(TreeImages images, boolean useLeafImages) {
        init(new ImageAdapter(images), useLeafImages);
    }

    public void add(Widget widget) {
        addItem(widget);
    }

    public TreeItemEx addItem(String itemHtml) {
        return (TreeItemEx) root.addItem(itemHtml);
    }

    public TreeItemEx addItem(SafeHtml itemHtml) {
        return (TreeItemEx) root.addItem(itemHtml);
    }

    public void addItem(TreeItemEx item) {
        root.addItem(item);
    }

    public TreeItemEx addItem(Widget widget) {
        return (TreeItemEx) root.addItem(widget);
    }

    public TreeItemEx addTextItem(String itemText) {
        return (TreeItemEx) root.addTextItem(itemText);
    }

    public void clear() {
        int size = root.getChildCount();
        for (int i = size - 1; i >= 0; i--) {
            root.getChild(i).remove();
        }
    }

    public void ensureSelectedItemVisible() {
        if (curSelection == null) {
            return;
        }

        TreeItemEx parent = (TreeItemEx) curSelection.getParentItem();
        while (parent != null) {
            parent.setState(true);
            parent = (TreeItemEx) parent.getParentItem();
        }
    }

    public TreeItemEx getItem(int index) {
        return (TreeItemEx) root.getChild(index);
    }

    public int getItemCount() {
        return root.getChildCount();
    }

    public TreeItemEx getSelectedItem() {
        return curSelection;
    }

    public TreeItemEx insertItem(int beforeIndex, String itemText) {
        return (TreeItemEx) root.insertItem(beforeIndex, itemText);
    }

    public TreeItemEx insertItem(int beforeIndex, SafeHtml itemHtml) {
        return (TreeItemEx) root.insertItem(beforeIndex, itemHtml);
    }

    public void insertItem(int beforeIndex, TreeItemEx item) {
        root.insertItem(beforeIndex, item);
    }

    public TreeItemEx insertItem(int beforeIndex, Widget widget) {
        return (TreeItemEx) root.insertItem(beforeIndex, widget);
    }

    public boolean isAnimationEnabled() {
        return isAnimationEnabled;
    }

    @Override
    @SuppressWarnings("fallthrough")
    public void onBrowserEvent(Event event) {
        int eventType = DOM.eventGetType(event);

        switch (eventType) {
        case Event.ONKEYDOWN: {
            // If nothing's selected, select the first item.
            if (curSelection == null) {
                if (root.getChildCount() > 0) {
                    onSelection((TreeItemEx) root.getChild(0), true);
                }
                super.onBrowserEvent(event);
                return;
            }
        }

        // Intentional fallthrough.
        case Event.ONKEYPRESS:
        case Event.ONKEYUP:
            // Issue 1890: Do not block history navigation via alt+left/right
            if (DOM.eventGetAltKey(event) || DOM.eventGetMetaKey(event)) {
                super.onBrowserEvent(event);
                return;
            }
            break;
        }

        switch (eventType) {

        case Event.ONMOUSEDOWN: {
            if ((DOM.eventGetCurrentTarget(event) == getElement()) && (event.getButton() == Event.BUTTON_LEFT)) {
                elementClicked(DOM.eventGetTarget(event));
            }
            break;
        }
        }
        // We must call super for all handlers.
        super.onBrowserEvent(event);
    }

    public boolean remove(Widget w) {
        TreeItemEx item = childWidgets.get(w);
        if (item == null) {
            return false;
        }
        item.setWidget(null);
        return true;
    }

    public boolean remove(IsWidget w) {
        return this.remove(w.asWidget());
    }

    public void removeItem(TreeItemEx item) {
        root.removeItem(item);
    }

    public void removeItems() {
        while (getItemCount() > 0) {
            removeItem(getItem(0));
        }
    }

    public void setAnimationEnabled(boolean enable) {
        isAnimationEnabled = enable;
    }

    public void setSelectedItem(TreeItemEx item) {
        setSelectedItem(item, true);
    }

    public void setSelectedItem(TreeItemEx item, boolean fireEvents) {
        if (item == null) {
            if (curSelection == null) {
                return;
            }
            curSelection.setSelected(false);
            curSelection = null;
            return;
        }
        onSelection(item, fireEvents);
    }

    public Iterator<TreeItemEx> treeItemIterator() {
        List<TreeItemEx> accum = new ArrayList<TreeItemEx>();
        root.addTreeItems(accum);
        return accum.iterator();
    }

    @Override
    protected void doAttachChildren() {
        try {
            final Widget[] widgets = new Widget[childWidgets.size()];
            childWidgets.keySet().toArray(widgets);
            for (Widget w : widgets) {
                _onAttach(w);
            }
        } finally {
        }
    }

    @Override
    protected void doDetachChildren() {
        try {
            final Widget[] widgets = new Widget[childWidgets.size()];
            childWidgets.keySet().toArray(widgets);
            for (Widget w : widgets) {
                _onDetach(w);
            }
        } finally {
        }
    }

    private native void _onAttach(Widget w)/*-{
		w.@com.google.gwt.user.client.ui.Widget::onAttach()();
    }-*/;

    private native void _onDetach(Widget w)/*-{
		w.@com.google.gwt.user.client.ui.Widget::onDetach()();
    }-*/;

    protected boolean isKeyboardNavigationEnabled(TreeItemEx currentItem) {
        return true;
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);
        root.ensureDebugId(baseID + "-root"); //$NON-NLS-1$
    }

    @Override
    protected void onLoad() {
        root.updateStateRecursive();
    }

    void adopt(Widget widget, TreeItemEx treeItem) {
        assert (!childWidgets.containsKey(widget));
        childWidgets.put(widget, treeItem);
        setParent(widget, this);
    }

    Map<Widget, TreeItemEx> getChildWidgets() {
        return childWidgets;
    }

    ImageAdapter getImages() {
        return images;
    }

    void maybeUpdateSelection(TreeItemEx itemThatChangedState, boolean isItemOpening) {
        if (!isItemOpening) {
            TreeItemEx tempItem = curSelection;
            while (tempItem != null) {
                if (tempItem == itemThatChangedState) {
                    setSelectedItem(itemThatChangedState);
                    return;
                }
                tempItem = (TreeItemEx) tempItem.getParentItem();
            }
        }
    }

    void orphan(Widget widget) {
        // Validation should already be done.
        assert (widget.getParent() == this);

        // Orphan.
        try {
            setParent(widget, null);
        } finally {
            // Logical detach.
            childWidgets.remove(widget);
        }
    }

    private native void setParent(Widget child, Widget parent)/*-{
		child.@com.google.gwt.user.client.ui.Widget::setParent(Lcom/google/gwt/user/client/ui/Widget;)(parent);
    }-*/;

    void showClosedImage(TreeItemEx treeItem) {
        showImage(treeItem, images.treeClosed());
    }

    void showLeafImage(TreeItemEx treeItem) {
        if (useLeafImages || treeItem.isFullNode()) {
            showImage(treeItem, images.treeLeaf());
        } else if (LocaleInfo.getCurrentLocale().isRTL()) {
            DOM.setStyleAttribute(treeItem.getElement(), "paddingRight", indentValue); //$NON-NLS-1$
        } else {
            DOM.setStyleAttribute(treeItem.getElement(), "paddingLeft", indentValue); //$NON-NLS-1$
        }
    }

    void showOpenImage(TreeItemEx treeItem) {
        showImage(treeItem, images.treeOpen());
    }

    private void collectElementChain(ArrayList<Element> chain, Element hRoot, Element hElem) {
        if ((hElem == null) || (hElem == hRoot)) {
            return;
        }
        collectElementChain(chain, hRoot, DOM.getParent(hElem));
        chain.add(hElem);
    }

    private boolean elementClicked(Element hElem) {
        ArrayList<Element> chain = new ArrayList<Element>();
        collectElementChain(chain, getElement(), hElem);

        TreeItemEx item = findItemByChain(chain, 0, root);
        if (item != null && item != root) {
            if (item.getChildCount() > 0 && DOM.isOrHasChild(item.getImageElement(), hElem)) {
                item.setState(!item.getState(), true);
                return true;
            } else if (DOM.isOrHasChild(item.getElement(), hElem)) {
                onSelection(item, true);
                return true;
            }
        }
        return false;
    }

    private TreeItemEx findDeepestOpenChild(TreeItemEx item) {
        if (!item.getState()) {
            return item;
    }
        return findDeepestOpenChild((TreeItemEx) item.getChild(item.getChildCount() - 1));
    }

    private TreeItemEx findItemByChain(ArrayList<Element> chain, int idx, TreeItemEx root) {
        if (idx == chain.size()) {
            return root;
        }

        Element hCurElem = chain.get(idx);
        for (int i = 0, n = root.getChildCount(); i < n; ++i) {
            TreeItemEx child = (TreeItemEx) root.getChild(i);
            if (child.getElement() == hCurElem) {
                TreeItemEx retItem = findItemByChain(chain, idx + 1, (TreeItemEx) root.getChild(i));
                if (retItem == null) {
                    return child;
                }
                return retItem;
            }
        }
        return findItemByChain(chain, idx + 1, root);
    }

    private TreeItemEx getTopClosedParent(TreeItemEx item) {
        TreeItemEx topClosedParent = null;
        TreeItemEx parent = (TreeItemEx) item.getParentItem();
        while (parent != null && parent != root) {
            if (!parent.getState()) {
                topClosedParent = parent;
            }
            parent = (TreeItemEx) parent.getParentItem();
        }
        return topClosedParent;
    }

    private void init(ImageAdapter images, boolean useLeafImages) {
        setImages(images, useLeafImages);
        setElement(DOM.createDiv());
        DOM.setStyleAttribute(getElement(), "position", "relative"); //$NON-NLS-1$ //$NON-NLS-2$
        DOM.setStyleAttribute(getElement(), "zoom", "1"); //$NON-NLS-1$//$NON-NLS-2$
        sinkEvents(Event.ONMOUSEDOWN | Event.ONCLICK | Event.KEYEVENTS);

        root = new TreeItemEx(true);
        root.setTree(this);
        setStyleName("gwt-Tree"); //$NON-NLS-1$
        Accessibility.setRole(getElement(), Accessibility.ROLE_TREE);
        getElement().getStyle().setMarginTop(3D, Unit.PX);
        getElement().getStyle().setOverflow(Overflow.AUTO);
    }

    private void maybeCollapseTreeItem() {
        TreeItemEx topClosedParent = getTopClosedParent(curSelection);
        if (topClosedParent != null) {
            setSelectedItem(topClosedParent);
        } else if (curSelection.getState()) {
            curSelection.setState(false);
        } else {
            TreeItemEx parent = (TreeItemEx) curSelection.getParentItem();
            if (parent != null) {
                setSelectedItem(parent);
            }
        }
    }

    private void onSelection(TreeItemEx item, boolean fireEvents) {
        if (item == root) {
            return;
        }

        if (curSelection != null) {
            curSelection.setSelected(false);
        }
        curSelection = item;

        if (curSelection != null) {
            curSelection.setSelected(true);
            if (fireEvents) {
                SelectionEvent.fire(this, curSelection);
            }
        }
    }

    private void setImages(ImageAdapter images, boolean useLeafImages) {
        this.images = images;
        this.useLeafImages = useLeafImages;

        if (!useLeafImages) {
            Image image = images.treeLeaf().createImage();
            DOM.setStyleAttribute(image.getElement(), "visibility", "hidden"); //$NON-NLS-1$ //$NON-NLS-2$
            RootPanel.get().add(image);
            int size = image.getWidth() + TreeItemEx.IMAGE_PAD;
            image.removeFromParent();
            indentValue = (size) + "px"; //$NON-NLS-1$
        }
    }

    private void showImage(TreeItemEx treeItem, AbstractImagePrototype proto) {
        Element holder = treeItem.getImageHolderElement();
        Element child = DOM.getFirstChild(holder);
        if (child == null) {
            DOM.appendChild(holder, proto.createElement().<Element> cast());
        } else {
            proto.applyTo(child.<ImagePrototypeElement> cast());
        }
    }

    public HandlerRegistration addSelectionHandler(SelectionHandler<TreeItemEx> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }

    public Iterator<Widget> iterator() {
        final Widget[] widgets = new Widget[childWidgets.size()];
        childWidgets.keySet().toArray(widgets);

        return createWidgetIterator(this, widgets);
    }

    private native Iterator<Widget> createWidgetIterator(HasWidgets container, Widget[] contained)/*-{
		@com.google.gwt.user.client.ui.WidgetIterators::createWidgetIterator(Lcom/google/gwt/user/client/ui/HasWidgets;[Lcom/google/gwt/user/client/ui/Widget;)(container, contained);
    }-*/;

    void fireStateChanged(TreeItemEx item, boolean open) {
        if (open) {
            OpenEvent.fire(this, item);
        } else {
            CloseEvent.fire(this, item);
        }
    }

    public HandlerRegistration addOpenHandler(OpenHandler<TreeItemEx> handler) {
        return addHandler(handler, OpenEvent.getType());
    }

    public HandlerRegistration addCloseHandler(CloseHandler<TreeItemEx> handler) {
        return addHandler(handler, CloseEvent.getType());
    }

}
