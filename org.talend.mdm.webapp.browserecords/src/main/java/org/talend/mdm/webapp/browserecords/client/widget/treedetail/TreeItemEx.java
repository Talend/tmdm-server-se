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
import java.util.List;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Accessibility;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class TreeItemEx extends UIObject {

    private static final double CHILD_MARGIN = 16.0;

    public static class TreeItemImpl {

        public TreeItemImpl() {
            initializeClonableElements();
        }

        void convertToFullNode(TreeItemEx item) {
            if (item.imageHolder == null) {
                // Extract the Elements from the object
                Element itemTable = DOM.clone(BASE_INTERNAL_ELEM, true);
                DOM.appendChild(item.getElement(), itemTable);
                Element tr = DOM.getFirstChild(DOM.getFirstChild(itemTable));
                Element tdImg = DOM.getFirstChild(tr);
                Element tdContent = DOM.getNextSibling(tdImg);

                // Undoes padding from table element.
                DOM.setStyleAttribute(item.getElement(), "padding", "0px");
                DOM.appendChild(tdContent, item.contentElem);
                item.imageHolder = tdImg;
            }
        }

        void initializeClonableElements() {
            if (GWT.isClient()) {
                // Create the base table element that will be cloned.
                BASE_INTERNAL_ELEM = DOM.createTable();
                Element contentElem = DOM.createDiv();
                Element tbody = DOM.createTBody(), tr = DOM.createTR();
                Element tdImg = DOM.createTD(), tdContent = DOM.createTD();
                DOM.appendChild(BASE_INTERNAL_ELEM, tbody);
                DOM.appendChild(tbody, tr);
                DOM.appendChild(tr, tdImg);
                DOM.appendChild(tr, tdContent);
                DOM.setStyleAttribute(tdImg, "verticalAlign", "middle");
                DOM.setStyleAttribute(tdContent, "verticalAlign", "middle");
                DOM.appendChild(tdContent, contentElem);
                DOM.setStyleAttribute(contentElem, "display", "inline");
                setStyleName(contentElem, "gwt-TreeItem");
                DOM.setStyleAttribute(BASE_INTERNAL_ELEM, "whiteSpace", "nowrap");

                // Create the base element that will be cloned
                BASE_BARE_ELEM = DOM.createDiv();

                // Simulates padding from table element.
                DOM.setStyleAttribute(BASE_BARE_ELEM, "padding", "3px");
                DOM.appendChild(BASE_BARE_ELEM, contentElem);
                Accessibility.setRole(contentElem, Accessibility.ROLE_TREEITEM);
            }
        }
    }

    public static class TreeItemImplIE6 extends TreeItemImpl {

        @Override
        void convertToFullNode(TreeItemEx item) {
            super.convertToFullNode(item);
            DOM.setStyleAttribute(item.getElement(), "marginBottom", "0px");
        }
    }

    private static class TreeItemAnimation extends Animation {

        private TreeItemEx curItem = null;

        private boolean opening = true;

        private int scrollHeight = 0;

        public void setItemState(TreeItemEx item, boolean animate) {
            // Immediately complete previous open
            cancel();

            // Open the new item
            if (animate) {
                curItem = item;
                opening = item.open;
                run(Math.min(ANIMATION_DURATION, ANIMATION_DURATION_PER_ITEM * curItem.getChildCount()));
            } else {
                UIObject.setVisible(item.childSpanElem, item.open);
            }
        }

        @Override
        protected void onComplete() {
            if (curItem != null) {
                if (opening) {
                    UIObject.setVisible(curItem.childSpanElem, true);
                    onUpdate(1.0);
                    DOM.setStyleAttribute(curItem.childSpanElem, "height", "auto");
                } else {
                    UIObject.setVisible(curItem.childSpanElem, false);
                }
                DOM.setStyleAttribute(curItem.childSpanElem, "overflow", "visible");
                DOM.setStyleAttribute(curItem.childSpanElem, "width", "auto");
                curItem = null;
            }
        }

        @Override
        protected void onStart() {
            scrollHeight = 0;

            if (!opening) {
                scrollHeight = curItem.childSpanElem.getScrollHeight();
            }
            DOM.setStyleAttribute(curItem.childSpanElem, "overflow", "hidden");

            super.onStart();

            if (opening) {
                UIObject.setVisible(curItem.childSpanElem, true);
                scrollHeight = curItem.childSpanElem.getScrollHeight();
            }
        }

        @Override
        protected void onUpdate(double progress) {
            int height = (int) (progress * scrollHeight);
            if (!opening) {
                height = scrollHeight - height;
            }

            height = Math.max(height, 1);

            DOM.setStyleAttribute(curItem.childSpanElem, "height", height + "px");

            int scrollWidth = DOM.getElementPropertyInt(curItem.childSpanElem, "scrollWidth");
            DOM.setStyleAttribute(curItem.childSpanElem, "width", scrollWidth + "px");
        }
    }

    static final int IMAGE_PAD = 7;

    private static final int ANIMATION_DURATION = 200;

    private static final int ANIMATION_DURATION_PER_ITEM = 75;

    private static TreeItemAnimation itemAnimation = new TreeItemAnimation();

    private static Element BASE_INTERNAL_ELEM;

    private static Element BASE_BARE_ELEM;

    private static TreeItemImpl impl = GWT.create(TreeItemImpl.class);

    private ArrayList<TreeItemEx> children;

    private Element contentElem, childSpanElem, imageHolder;

    private boolean isRoot;

    private boolean open;

    private TreeItemEx parent;

    private boolean selected;

    private Object userObject;

    private TreeEx tree;

    private Widget widget;

    public TreeItemEx() {
        this(false);
    }

    public TreeItemEx(String html) {
        this();
        setHTML(html);
    }

    public TreeItemEx(SafeHtml html) {
        this(html.asString());
    }

    public TreeItemEx(Widget widget) {
        this();
        setWidget(widget);
    }

    TreeItemEx(boolean isRoot) {
        this.isRoot = isRoot;
        Element elem = DOM.clone(BASE_BARE_ELEM, true);
        setElement(elem);
        contentElem = DOM.getFirstChild(elem);
        DOM.setElementAttribute(contentElem, "id", DOM.createUniqueId());

        // The root item always has children.
        if (isRoot) {
            initChildren();
        }
    }

    public TreeItemEx addItem(String itemHtml) {
        TreeItemEx ret = new TreeItemEx(itemHtml);
        addItem(ret);
        return ret;
    }

    public TreeItemEx addItem(SafeHtml itemHtml) {
        TreeItemEx ret = new TreeItemEx(itemHtml);
        addItem(ret);
        return ret;
    }

    public void addItem(TreeItemEx item) {
        // If this is the item's parent, removing the item will affect the child
        // count.
        maybeRemoveItemFromParent(item);
        insertItem(getChildCount(), item);
    }

    public TreeItemEx addItem(Widget widget) {
        TreeItemEx ret = new TreeItemEx(widget);
        addItem(ret);
        return ret;
    }

    public TreeItemEx addTextItem(String itemText) {
        TreeItemEx ret = new TreeItemEx();
        ret.setText(itemText);
        addItem(ret);
        return ret;
    }

    public TreeItemEx asTreeItem() {
        return this;
    }

    public TreeItemEx getChild(int index) {
        if ((index < 0) || (index >= getChildCount())) {
            return null;
        }

        return children.get(index);
    }

    public int getChildCount() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }

    public int getChildIndex(TreeItemEx child) {
        if (children == null) {
            return -1;
        }
        return children.indexOf(child);
    }

    public String getHTML() {
        return DOM.getInnerHTML(contentElem);
    }

    public TreeItemEx getParentItem() {
        return parent;
    }

    public boolean getState() {
        return open;
    }

    public String getText() {
        return DOM.getInnerText(contentElem);
    }

    public final TreeEx getTree() {
        return tree;
    }

    public Object getUserObject() {
        return userObject;
    }

    public Widget getWidget() {
        return widget;
    }

    public TreeItemEx insertItem(int beforeIndex, String itemText) throws IndexOutOfBoundsException {
        TreeItemEx ret = new TreeItemEx(itemText);
        insertItem(beforeIndex, ret);
        return ret;
    }

    public TreeItemEx insertItem(int beforeIndex, SafeHtml itemHtml) throws IndexOutOfBoundsException {
        TreeItemEx ret = new TreeItemEx(itemHtml);
        insertItem(beforeIndex, ret);
        return ret;
    }

    public void insertItem(int beforeIndex, TreeItemEx item) throws IndexOutOfBoundsException {
        // Detach item from existing parent.
        maybeRemoveItemFromParent(item);

        // Check the index after detaching in case this item was already the parent.
        int childCount = getChildCount();
        if (beforeIndex < 0 || beforeIndex > childCount) {
            throw new IndexOutOfBoundsException();
        }

        if (children == null) {
            initChildren();
        }

        // Set the margin.
        // Use no margin on top-most items.
        double margin = isRoot ? 0.0 : CHILD_MARGIN;
        if (LocaleInfo.getCurrentLocale().isRTL()) {
            item.getElement().getStyle().setMarginRight(margin, Unit.PX);
        } else {
            item.getElement().getStyle().setMarginLeft(margin, Unit.PX);
        }

        // Physical attach.
        Element childContainer = isRoot ? tree.getElement() : childSpanElem;
        if (beforeIndex == childCount) {
            childContainer.appendChild(item.getElement());
        } else {
            Element beforeElem = getChild(beforeIndex).getElement();
            childContainer.insertBefore(item.getElement(), beforeElem);
        }

        // Logical attach.
        // Explicitly set top-level items' parents to null if this is root.
        item.setParentItem(isRoot ? null : this);
        children.add(beforeIndex, item);

        // Adopt.
        item.setTree(tree);

        if (!isRoot && children.size() == 1) {
            updateState(false, false);
        }
    }

    public TreeItemEx insertItem(int beforeIndex, Widget widget) throws IndexOutOfBoundsException {
        TreeItemEx ret = new TreeItemEx(widget);
        insertItem(beforeIndex, ret);
        return ret;
    }

    public boolean isSelected() {
        return selected;
    }

    public void remove() {
        if (parent != null) {
            // If this item has a parent, remove self from it.
            parent.removeItem(this);
        } else if (tree != null) {
            // If the item has no parent, but is in the Tree, it must be a top-level
            // element.
            tree.removeItem(this);
        }
    }

    public void removeItem(TreeItemEx item) {
        // Validate.
        if (children == null || !children.contains(item)) {
            return;
        }

        // Orphan.
        TreeEx oldTree = tree;
        item.setTree(null);

        // Physical detach.
        if (isRoot) {
            oldTree.getElement().removeChild(item.getElement());
        } else {
            childSpanElem.removeChild(item.getElement());
        }

        // Logical detach.
        item.setParentItem(null);
        children.remove(item);

        if (!isRoot && children.size() == 0) {
            updateState(false, false);
        }
    }

    public void removeItems() {
        while (getChildCount() > 0) {
            removeItem(getChild(0));
        }
    }

    public void setHTML(String html) {
        setWidget(null);
        DOM.setInnerHTML(contentElem, html);
    }

    public void setHTML(SafeHtml html) {
        setHTML(html.asString());
    }

    public void setSelected(boolean selected) {
        if (this.selected == selected) {
            return;
        }
        this.selected = selected;
        setStyleName(getContentElem(), "gwt-TreeItem-selected", selected);
    }

    public void setState(boolean open) {
        setState(open, true);
    }

    public void setState(boolean open, boolean fireEvents) {
        if (open && getChildCount() == 0) {
            return;
        }

        // Only do the physical update if it changes
        if (this.open != open) {
            this.open = open;
            updateState(true, true);

            if (fireEvents && tree != null) {
                tree.fireStateChanged(this, open);
            }
        }
    }

    public void setText(String text) {
        setWidget(null);
        DOM.setInnerText(contentElem, text);
    }

    public void setUserObject(Object userObj) {
        userObject = userObj;
    }

    public void setWidget(Widget newWidget) {
        // Detach new child from old parent.
        if (newWidget != null) {
            newWidget.removeFromParent();
        }

        // Detach old child from tree.
        if (widget != null) {
            try {
                if (tree != null) {
                    tree.orphan(widget);
                }
            } finally {
                // Physical detach old child.
                contentElem.removeChild(widget.getElement());
                widget = null;
            }
        }

        // Clear out any existing content before adding a widget.
        DOM.setInnerHTML(contentElem, "");

        // Logical detach old/attach new.
        widget = newWidget;

        if (newWidget != null) {
            // Physical attach new.
            DOM.appendChild(contentElem, newWidget.getElement());

            // Attach child to tree.
            if (tree != null) {
                tree.adopt(widget, this);
            }

        }
    }

    protected Focusable getFocusable() {
        Focusable focus = getFocusableWidget();
        if (focus == null) {
            Widget w = getWidget();
            if (w instanceof Focusable) {
                focus = (Focusable) w;
            }
        }
        return focus;
    }

    @Deprecated
    protected HasFocus getFocusableWidget() {
        Widget w = getWidget();
        if (w instanceof HasFocus) {
            return (HasFocus) w;
        } else {
            return null;
        }
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);
        ensureDebugId(contentElem, baseID, "content");
        if (imageHolder != null) {
            // The image itself may or may not exist.
            ensureDebugId(imageHolder, baseID, "image");
        }

        if (children != null) {
            int childCount = 0;
            for (TreeItemEx child : children) {
                child.ensureDebugId(baseID + "-child" + childCount);
                childCount++;
            }
        }
    }

    void addTreeItems(List<TreeItemEx> accum) {
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            TreeItemEx item = children.get(i);
            accum.add(item);
            item.addTreeItems(accum);
        }
    }

    ArrayList<TreeItemEx> getChildren() {
        return children;
    }

    Element getContentElem() {
        return contentElem;
    }

    Element getImageElement() {
        return DOM.getFirstChild(getImageHolderElement());
    }

    Element getImageHolderElement() {
        if (!isFullNode()) {
            convertToFullNode();
        }
        return imageHolder;
    }

    void initChildren() {
        convertToFullNode();
        childSpanElem = DOM.createDiv();
        DOM.appendChild(getElement(), childSpanElem);
        DOM.setStyleAttribute(childSpanElem, "whiteSpace", "nowrap");
        children = new ArrayList<TreeItemEx>();
    }

    boolean isFullNode() {
        return imageHolder != null;
    }

    void maybeRemoveItemFromParent(TreeItemEx item) {
        if ((item.getParentItem() != null) || (item.getTree() != null)) {
            item.remove();
        }
    }

    void setParentItem(TreeItemEx parent) {
        this.parent = parent;
    }

    void setTree(TreeEx newTree) {
        // Early out.
        if (tree == newTree) {
            return;
        }

        // Remove this item from existing tree.
        if (tree != null) {
            if (tree.getSelectedItem() == this) {
                tree.setSelectedItem(null);
            }

            if (widget != null) {
                tree.orphan(widget);
            }
        }

        tree = newTree;
        for (int i = 0, n = getChildCount(); i < n; ++i) {
            children.get(i).setTree(newTree);
        }
        updateState(false, true);

        if (newTree != null) {
            if (widget != null) {
                // Add my widget to the new tree.
                newTree.adopt(widget, this);
            }
        }
    }

    void updateState(boolean animate, boolean updateTreeSelection) {
        // If the tree hasn't been set, there is no visual state to update.
        // If the tree is not attached, then update will be called on attach.
        if (tree == null || tree.isAttached() == false) {
            return;
        }

        if (getChildCount() == 0) {
            if (childSpanElem != null) {
                UIObject.setVisible(childSpanElem, false);
            }
            tree.showLeafImage(this);
            return;
        }

        // We must use 'display' rather than 'visibility' here,
        // or the children will always take up space.
        if (animate && (tree != null) && (tree.isAttached())) {
            itemAnimation.setItemState(this, tree.isAnimationEnabled());
        } else {
            itemAnimation.setItemState(this, false);
        }

        // Change the status image
        if (open) {
            tree.showOpenImage(this);
        } else {
            tree.showClosedImage(this);
        }

        // We may need to update the tree's selection in response to a tree state
        // change. For example, if the tree's currently selected item is a
        // descendant of an item whose branch was just collapsed, then the item
        // itself should become the newly-selected item.
        if (updateTreeSelection) {
            tree.maybeUpdateSelection(this, this.open);
        }
    }

    void updateStateRecursive() {
        updateStateRecursiveHelper();
        tree.maybeUpdateSelection(this, this.open);
    }

    private void convertToFullNode() {
        impl.convertToFullNode(this);
    }

    private void updateStateRecursiveHelper() {
        updateState(false, false);
        for (int i = 0, n = getChildCount(); i < n; ++i) {
            children.get(i).updateStateRecursiveHelper();
        }
    }
}
