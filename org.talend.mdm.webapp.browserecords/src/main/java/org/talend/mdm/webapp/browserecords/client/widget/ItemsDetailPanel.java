/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.talend.mdm.webapp.browserecords.client.util.LabelUtil;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.Widget;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ItemsDetailPanel extends ContentPanel {

    public final static String SINGLETON = "SINGLETON"; //$NON-NLS-1$

    public final static String MULTIPLE = "MULTIPLE"; //$NON-NLS-1$

    public final static String PKTAB = "PKTAB"; //$NON-NLS-1$

    // Panel within gray outer border in which all content is contained
    private LayoutContainer mainPanel = new LayoutContainer();

    // The north gray border in which to place the breadcrumb
    private LayoutContainer ncBorder = new LayoutContainer();

    // The tab panel widget.
    private ItemsDetailTabPanel itemsDetailTabPanel = new ItemsDetailTabPanel();

    private SimplePanel breadCrumb = new SimplePanel();

    protected LayoutContainer banner = new LayoutContainer();

    private Text textTitle = new Text();

    private ContentPanel treeDetail;

    private boolean isOutMost;

    private boolean isStaging;

    private boolean lineage;

    public static interface ItemsDetailPanelCreator {

        ItemsDetailPanel newInstance();
    }

    private static ItemsDetailPanelCreator creator;

    public static void initialize(ItemsDetailPanelCreator impl) {
        ItemsDetailPanel.creator = impl;
    }

    protected ItemsDetailPanel() {
        super();
        breadCrumb.getElement().getStyle().setOverflow(Overflow.HIDDEN);
        this.setBodyBorder(false);
        this.setHeaderVisible(false);
        this.initPanel();
    }

    public static ItemsDetailPanel newInstance() {
        if (creator == null) {
            return new ItemsDetailPanel();
        }
        return creator.newInstance();
    }

    public interface ForeignKeyHandler {

        void onSelect();
    }

    private Map<ContentPanel, ForeignKeyHandler> fkHandlerMap = new HashMap<ContentPanel, ItemsDetailPanel.ForeignKeyHandler>();

    public void addFkHandler(ContentPanel itemPanel, ForeignKeyHandler fkHandler) {
        this.fkHandlerMap.put(itemPanel, fkHandler);
    }

    private void initPanel() {
        this.setId("ItemsDetailPanel"); //$NON-NLS-1$

        // This has border layout in order to draw gray borders
        this.setLayout(new BorderLayout());

        // Configure the mainPanel
        this.mainPanel.setId("ItemsDetailPanel-mainPanel"); //$NON-NLS-1$
        this.mainPanel.setLayout(new RowLayout(Orientation.VERTICAL));

        // Initialize the outer gray border panels.
        this.initBorderPanels();

        // Add breadcrumb to north gray border
        ncBorder.add(breadCrumb);
        ncBorder.addListener(Events.Resize, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                BreadCrumb bc = (BreadCrumb) breadCrumb.getWidget();
                if (bc != null) {
                    bc.adjust();
                }
            }
        });

        // Configure the textTitle within the banner
        textTitle.setId("ItemsDetailPanel-textTitle"); //$NON-NLS-1$

        banner.setHeight(BANNER_HEIGHT);
        banner.setScrollMode(Scroll.AUTO);
        final LayoutContainer bannerWrapper = newBannerWrapper(banner);

        // Resize banner when parent resizes
        // For some reason this is not automatic
        mainPanel.addListener(Events.Resize, new Listener<BoxComponentEvent>() {

            @Override
            public void handleEvent(final BoxComponentEvent event) {
                bannerWrapper.setWidth(mainPanel.getWidth());
            }
        });
        this.mainPanel.add(bannerWrapper);

        // Initialize and add the tabPanel
        // Resize tab panel explicitly or it will vertically overflow the parent container of fixed height
        mainPanel.addListener(Events.Resize, new Listener<BoxComponentEvent>() {

            @Override
            public void handleEvent(final BoxComponentEvent event) {
                int newHeight = mainPanel.getHeight() - BANNER_HEIGHT;
                if (newHeight < 0) {
                    newHeight = 0;
                }
                itemsDetailTabPanel.setHeight(newHeight);
                itemsDetailTabPanel.setWidth(mainPanel.getWidth());
            }
        });
        mainPanel.add(itemsDetailTabPanel);
    }

    public void initBreadCrumb(BreadCrumb breadCrumb) {
        breadCrumb.getElement().setId("ItemsDetailPanel-breadCrumb"); //$NON-NLS-1$
        this.breadCrumb.setWidget(breadCrumb);
    }

    public void clearBreadCrumb() {
        this.breadCrumb.clear();
    }

    public void appendBreadCrumb(String concept, String label, String ids, String pkInfo) {
        if (this.breadCrumb.getWidget() instanceof BreadCrumb) {
            BreadCrumb curBC = (BreadCrumb) this.breadCrumb.getWidget();
            curBC.appendBreadCrumb(concept, label, ids, pkInfo);
        }
    }

    public void initBanner(List<String> xpathList, String desc) {
        clearBanner();
        String toolTipString = ""; //$NON-NLS-1$
        if (desc != null && !desc.equals("")) { //$NON-NLS-1$
            toolTipString = "<img style='margin-left:16px;' " + //$NON-NLS-1$
                    "src='secure/img/genericUI/information_icon.png' title='" //$NON-NLS-1$
                    + LabelUtil.convertSpecialHTMLCharacter(desc) + "'/>"; //$NON-NLS-1$ 
        }
        if (xpathList != null && xpathList.size() > 0) {
            int i = 1;
            for (String str : xpathList) {
                if (i == 1) {
                    textTitle.setText(Format.htmlEncode(str) + toolTipString);
                    banner.add(textTitle);
                    i++;
                    continue;
                }
                Text subTitle = new Text();
                subTitle.setStyleName("ItemsDetailPanel-subTitle"); //$NON-NLS-1$
                subTitle.setText(Format.htmlEncode(str));
                banner.add(subTitle);
            }
            banner.layout(true);
        }
    }

    public void clearBanner() {
        textTitle.setText(null);
        banner.removeAll();
        banner.layout(true);
        if (isStaging) {
            Text text = new Text(MessagesFactory.getMessages().staging_hint());
            text.setId("staging-flag"); //$NON-NLS-1$
            banner.add(text);
        }
    }

    public void clearAll() {
        clearBanner();
        clearBreadCrumb();
        clearContent();
    }

    public ItemDetailTabPanelContentHandle addTabItem(String title, ContentPanel panel, String pattern, String id) {
        if (itemsDetailTabPanel.getTabTitles().contains(title)) {
            itemsDetailTabPanel.closeTabPanelWithTitle(title);
        }
        return itemsDetailTabPanel.addTabItem(title, panel, pattern, id);
    }

    public ItemPanel getCurrentItemPanel() {
        return itemsDetailTabPanel.getCurrentlySelectedTabItemPanel();
    }

    public void clearContent() {
        itemsDetailTabPanel.clear();
    }

    public void clearChildrenContent() {
        itemsDetailTabPanel.clearChildrenContent();
    }

    public void closeCurrentTab() {
        itemsDetailTabPanel.closeCurrentTab();
    }

    public Widget getFirstTabWidget() {

        return itemsDetailTabPanel.getFirstTabWidget();
    }

    public Widget getPrimaryKeyTabWidget() {
        return itemsDetailTabPanel.getPrimaryKeyTabWidget();
    }

    public Widget getCurrentlySelectedTabWidget() {

        return itemsDetailTabPanel.getCurrentlySelectedTabWidget();
    }

    public void selectTabAtIndex(int index) {
        this.itemsDetailTabPanel.selectTabAtIndex(index);
    }

    public int getTabCount() {
        return itemsDetailTabPanel.getTabCount();
    }

    public Widget getTabWidgetAtIndex(int index) {
        return this.itemsDetailTabPanel.getTabWidgetAtIndex(index);
    }

    public void closeTabPanelWithId(String tabItemId) {

        this.itemsDetailTabPanel.closeTabPanelWithId(tabItemId);
    }

    /**
     * Handle returned by addTabItem used to delete the tab item added.
     */
    public class ItemDetailTabPanelContentHandle {

        private String id = ""; //$NON-NLS-1$

        public ItemDetailTabPanelContentHandle(String id) {
            this.id = id;
        }

        public void deleteContent() {
            ItemsDetailPanel.this.closeTabPanelWithId(id);
        }
    }

    /**
     * Custom tab panel within ItemsDetailPanel that supports the desired custom look and feel.
     */
    private class ItemsDetailTabPanel extends LayoutContainer {

        public static final int TAB_BAR_HEIGHT = 28;

        // There is unfortunately no way to easily get the scrollbar size
        // But it seems fairly standard that operating systems use 16px or 18px
        // This should be set to the maximum pixel size of scrollbars on any system
        public static final int SCROLL_BAR_HEIGHT = 18;

        // Internal state, saves id's and panels corresponding to each tab
        private Vector<String> tabIds = new Vector<String>();

        // Saves tab titles so that avoid duplicate tab title
        private Vector<String> tabTitles = new Vector<String>();

        private Vector<ContentPanel> tabPanels = new Vector<ContentPanel>();

        // Visual elements, the tab bar, a horizontal spacer, and the content panel
        private TabBar tabBar = new TabBar();

        private HandlerRegistration handlerRegistration = null;

        // Panel containing tabBar so that when there are too many tabs, a horizontal scrollbar appears
        private ScrollPanel tabBarScrollPanel = new ScrollPanel();

        private LayoutContainer tabContent = new LayoutContainer();

        // The desired width/height of tabContent and what is within tabContent
        // What is within tabContent must be smaller because of the borders of tabContent
        private int curTabContentWidth = 0;

        private int curTabContentHeight = 0;

        private int curTabContentInnerWidth = 0;

        private int curTabContentInnerHeight = 0;

        /**
         * ItemsDetailTabPanel constructor.
         */
        public ItemsDetailTabPanel() {
            this.setLayout(new RowLayout(Orientation.VERTICAL));

            // Resize listener for the tab panel, recalculate sizes needed and resize everything within
            this.addListener(Events.Resize, new Listener<BoxComponentEvent>() {

                @Override
                public void handleEvent(final BoxComponentEvent event) {
                    // Recalculate tabContent size
                    ItemsDetailTabPanel.this.calcTabContentSize();

                    // Resize tabContent
                    ItemsDetailTabPanel.this.tabContent.setHeight(ItemsDetailTabPanel.this.curTabContentHeight);
                    ItemsDetailTabPanel.this.tabContent.setWidth(ItemsDetailTabPanel.this.curTabContentWidth);

                    // Resize what is in tabContent, if anything
                    Widget contentWidget = ItemsDetailTabPanel.this.tabContent.getWidget(0);
                    if (contentWidget != null) {
                        contentWidget.setHeight(ItemsDetailTabPanel.this.curTabContentInnerHeight + "px"); //$NON-NLS-1$
                        contentWidget.setWidth(ItemsDetailTabPanel.this.curTabContentInnerWidth + "px"); //$NON-NLS-1$
                    }
                }
            });

            this.buildNewTabBar();

            this.tabBarScrollPanel.getElement().setId("ItemsDetailPanel-tabBarScrollPanel"); //$NON-NLS-1$
            this.tabBarScrollPanel.setHeight((SCROLL_BAR_HEIGHT + TAB_BAR_HEIGHT) + "px"); //$NON-NLS-1$
            this.add(tabBarScrollPanel);

            this.tabContent.setId("ItemsDetailPanel-tabContent"); //$NON-NLS-1$
            this.add(this.tabContent);
        }

        /**
         * Decommission the previous tabBar if there is one and replace it with a new one. The reason we do it this way
         * is so that clearing the ItemsDetailPanel when it is already rendered is fast. If we removed the tabs from the
         * existing tabBar one by one, each removal will have to be rendered. By making it fast to clear a rendered
         * ItemsDetailPanel, we can reuse it to display a record of the same entity.
         */
        private void buildNewTabBar() {
            if (this.handlerRegistration != null) {
                handlerRegistration.removeHandler();
            }
            if (fkHandlerMap != null) {
                fkHandlerMap.clear();
            }

            this.tabBar = new TabBar();
            // Tab selection listener for tab panel, change the content displayed and size it appropriately
            this.handlerRegistration = this.tabBar.addSelectionHandler(new SelectionHandler<Integer>() {

                @Override
                public void onSelection(SelectionEvent<Integer> arg0) {

                    ItemsDetailTabPanel.this.tabContent.removeAll();

                    int selectedItemIndex = arg0.getSelectedItem();
                    ContentPanel newPanel = ItemsDetailTabPanel.this.tabPanels.get(selectedItemIndex);
                    ItemsDetailTabPanel.this.tabContent.add(newPanel);

                    ItemsDetailTabPanel.this.layout(true);

                    newPanel.setHeight(ItemsDetailTabPanel.this.curTabContentInnerHeight);
                    newPanel.setWidth(ItemsDetailTabPanel.this.curTabContentInnerWidth);

                    ForeignKeyHandler fkHandler = fkHandlerMap.get(newPanel);
                    if (fkHandler != null) {
                        fkHandler.onSelect();
                        fkHandlerMap.remove(newPanel);
                    }
                }
            });
            this.tabBar.getElement().setId("ItemsDetailPanel-tabBar"); //$NON-NLS-1$
            this.tabBarScrollPanel.setWidget(this.tabBar);
        }

        /**
         * Get the number of tabs.
         * 
         * @return The number of tabs.
         */
        public int getTabCount() {
            return this.tabBar.getTabCount();
        }

        /**
         * Called by resize listener to recalculate what tabContent and the within it should be in size. What is within
         * must be smaller due to borders of tabContent.
         */
        private void calcTabContentSize() {
            this.curTabContentHeight = this.getHeight() - TAB_BAR_HEIGHT - SCROLL_BAR_HEIGHT;
            if (this.curTabContentHeight < 0) {
                this.curTabContentHeight = 0;
            }

            this.curTabContentWidth = this.getWidth();

            this.curTabContentInnerHeight = this.curTabContentHeight - 1;
            if (this.curTabContentInnerHeight < 0) {
                this.curTabContentInnerHeight = 0;
            }

            this.curTabContentInnerWidth = this.curTabContentWidth - 2;
            if (this.curTabContentInnerWidth < 0) {
                this.curTabContentInnerWidth = 0;
            }
        }

        /**
         * Add a new tab.
         * 
         * @param title
         * @param panel
         * @param pattern
         * @param id
         */
        public ItemDetailTabPanelContentHandle addTabItem(String title, ContentPanel panel, String pattern, String id) {
            if (pattern.equalsIgnoreCase(ItemsDetailPanel.MULTIPLE)) {
                this.closeTabPanelWithId(id);

                if (this.getTabCount() == 0) {
                    // Adding a first tab

                    // Create the tab, which must be special because it is the first
                    // It must have the Home icon in it
                    Label tabLabel = this.createTabLabel(title);
                    SimplePanel firstTabPanel = new SimplePanel();
                    firstTabPanel.add(tabLabel);
                    firstTabPanel.addStyleName("gwt-TabBarItem-first"); //$NON-NLS-1$
                    this.tabBar.addTab(firstTabPanel);

                    // Save the ID
                    this.tabIds.add(id);

                    // Save the ID
                    this.tabTitles.add(title);

                    // Save the panel
                    this.tabPanels.add(panel);

                    // Select the tab
                    this.tabBar.selectTab(0);
                } else {
                    // Create the tab
                    Label tabLabel = this.createTabLabel(title);
                    this.tabBar.addTab(tabLabel);

                    // Save the ID
                    this.tabIds.add(id);

                    // Save the ID
                    this.tabTitles.add(title);

                    // Save the panel
                    this.tabPanels.add(panel);
                }

            } else {
                panel.setId(PKTAB); // Setup id='PKTAB' constant when render Primary Key Tab
                int itemIndex = this.tabIds.indexOf(id);
                if (itemIndex == -1) {
                    if (this.getTabCount() == 0) {
                        // Create the first tab, which must be special because it is
                        // the first. It must have the Home icon in it
                        Label tabLabel = this.createTabLabel(title);
                        SimplePanel firstTabPanel = new SimplePanel();
                        firstTabPanel.add(tabLabel);
                        firstTabPanel.addStyleName("gwt-TabBarItem-first"); //$NON-NLS-1$
                        this.tabBar.addTab(firstTabPanel);
                    } else {
                        // Create the tab
                        Label tabLabel = this.createTabLabel(title);
                        this.tabBar.addTab(tabLabel);
                    }

                    // Save the ID
                    this.tabIds.add(id);

                    // Save the ID
                    this.tabTitles.add(title);

                    // Save the panel
                    this.tabPanels.add(panel);

                    // Select the tab
                    this.tabBar.selectTab(this.getTabCount() - 1);
                } else {
                    this.tabPanels.set(itemIndex, panel);
                    if (this.tabBar.getSelectedTab() == itemIndex) {
                        // Replacing the currently selected tab

                        this.tabContent.removeAll();
                        this.tabContent.add(panel);

                        this.layout(true);

                        panel.setHeight(this.curTabContentInnerHeight);
                        panel.setWidth(this.curTabContentInnerWidth);
                    }
                }
            }

            return new ItemDetailTabPanelContentHandle(id);
        }

        // These are pixel measurements for determining when to truncate labels
        // They are highly dependent on the specific font used and are very particular
        // to the current look and feel.
        public static final int APPROXIMATE_LABEL_LETTER_WIDTH = 10;

        public static final int TAB_LABEL_WIDTH = 130;

        public static final int APPROXIMATE_ELLIPSIS_WIDTH = 9;

        public static final int MAX_LETTERS_PER_LABEL = TAB_LABEL_WIDTH / APPROXIMATE_LABEL_LETTER_WIDTH;

        public static final int LETTERS_PER_LABEL_AFTER_TRUNCATE = (TAB_LABEL_WIDTH - APPROXIMATE_ELLIPSIS_WIDTH)
                / APPROXIMATE_LABEL_LETTER_WIDTH;

        /**
         * Takes care of truncating and adding ellipsis to the tab label if it is too long. This actually calculates
         * exactly how long the rendered text will be and truncates it as little as possible.
         * 
         * Takes care of adding tooltip to the tab label for when mouse hovers over the label.
         * 
         * @param title The text to display in the label.
         * @return label to add to tab.
         */
        private Label createTabLabel(String title) {
            String shortTitle = title;
            if (shortTitle.length() > MAX_LETTERS_PER_LABEL) {
                shortTitle = shortTitle.substring(0, LETTERS_PER_LABEL_AFTER_TRUNCATE) + "..."; //$NON-NLS-1$
            }

            Label tabLabel = new Label(shortTitle);
            tabLabel.addStyleName("ItemsDetailPanel-tabLabel"); //$NON-NLS-1$
            tabLabel.addStyleName("gwt-Label"); //$NON-NLS-1$
            tabLabel.getElement().setAttribute("title", title); //$NON-NLS-1$

            return tabLabel;
        }

        /**
         * This does nothing if index is not valid. If tab is selected, tries to select next one if there is one, and if
         * not, select the previous one.
         * 
         * @param index Index of the tab to remove. If out of bounds, method does nothing.
         */
        public void closeTabAtIndex(int index) {
            int tabCount = this.getTabCount();
            if (index >= 0 && index < tabCount) {
                // Attempting to close a valid tab

                if (tabCount == 1) {// Closing the last tab

                    this.tabContent.removeAll();
                } else if (index == this.tabBar.getSelectedTab()) {// Attempting to close selected tab and at least one
                                                                   // more remains

                    if (index < tabCount - 1) {
                        // Selected tab being removed is not the last tab
                        this.tabBar.selectTab(index + 1);
                    } else {
                        // Selected tab being removed is the last tab
                        this.tabBar.selectTab(tabCount - 2);
                    }
                }

                this.tabBar.removeTab(index);
                this.tabIds.remove(index);
                this.tabTitles.remove(index);
                this.tabPanels.remove(index);
            }
        }

        /**
         * Clear all but the first tab.
         */
        public void clearChildrenContent() {
            while (this.getTabCount() > 1) {
                this.closeTabAtIndex(1);
            }
        }

        /**
         * Close tab with specified id. This does nothing if no tab has id.
         * 
         * @param tabItemId
         */
        public void closeTabPanelWithId(String tabItemId) {
            int index = tabIds.indexOf(tabItemId);
            if (index >= 0) {
                this.closeTabAtIndex(index);
            }
        }

        /**
         * Close tab with title. This does nothing if no tab title.
         * 
         * @param tabTitle
         */
        public void closeTabPanelWithTitle(String tabTitle) {
            int index = this.tabTitles.indexOf(tabTitle);
            if (index >= 0) {
                this.closeTabAtIndex(index);
            }
        }

        /**
         * Close the current tab. This does nothing if no tab currently selected.
         */
        public void closeCurrentTab() {
            int index = this.tabBar.getSelectedTab();
            if (index >= 0) {
                this.closeTabAtIndex(index);
            }
        }

        /**
         * Select the tab at index. This does nothing if index is not valid.
         * 
         * @param index Index of tab to select, starts from 0.
         */
        public void selectTabAtIndex(int index) {
            if (0 <= index && index < this.getTabCount()) {
                this.tabBar.selectTab(index);
            }
        }

        /**
         * Clear both display and internal state.
         */
        public void clear() {
            this.buildNewTabBar();
            this.tabIds.removeAllElements();
            this.tabTitles.removeAllElements();
            this.tabPanels.removeAllElements();
            this.tabContent.removeAll();
        }

        /**
         * Return the Primary Key Tab by constant 'PKTAB'
         * 
         * @return
         */
        public Widget getPrimaryKeyTabWidget() {
            if (this.tabPanels.size() > 0) {
                for (ContentPanel panel : this.tabPanels) {
                    if (PKTAB.equals(panel.getId())) {
                        return panel;
                    }
                }
            }
            return null;
        }

        /**
         * Returns widget contained in first tab. Returns null if no tabs.
         * 
         * @return
         */
        public Widget getFirstTabWidget() {
            if (this.getTabCount() > 0) {
                return this.tabPanels.get(0);
            }
            return null;
        }

        /**
         * Returns widget contained in currently selected tab. Returns null if no currently selected tab.
         * 
         * @return Widget in currently selected tab.
         */
        public Widget getCurrentlySelectedTabWidget() {
            if (this.tabBar.getSelectedTab() >= 0) {
                Widget w = this.tabContent.getWidget(0);
                return w;
            }
            return null;
        }

        /**
         * Returns ItemPanel in currently selected tab. If no currently selected tab or widget not ItemPanel, null is
         * returned.
         * 
         * @return ItemPanel in currently selected tab.
         */
        public ItemPanel getCurrentlySelectedTabItemPanel() {
            if (this.tabBar.getSelectedTab() >= 0) {
                Widget w = this.tabContent.getWidget(0);
                if (w != null && w instanceof ItemPanel) {
                    return (ItemPanel) w;
                }
            }
            return null;
        }

        /**
         * Get widget in tab at specified index. Returns null if index is not valid.
         * 
         * @param index
         * @return
         */
        public Widget getTabWidgetAtIndex(int index) {
            if (0 <= index && index < this.getTabCount()) {
                return this.tabPanels.get(index);
            }

            return null;
        }

        public Vector<String> getTabTitles() {
            return this.tabTitles;
        }
    }

    private static final int BANNER_WRAPPER_LEFT_BORDER_WIDTH = 10;

    private static final int BANNER_WRAPPER_RIGHT_BORDER_WIDTH = 9;

    private static final int BANNER_HEIGHT = 70;

    /**
     * Wraps the banner in a panel that gives it side borders.
     * 
     * @param banner
     * @return
     */
    private LayoutContainer newBannerWrapper(LayoutContainer banner) {
        LayoutContainer bannerWrapper = new LayoutContainer(new BorderLayout());
        bannerWrapper.setId("ItemsDetailPanel-bannerWrapper"); //$NON-NLS-1$
        bannerWrapper.setHeight(BANNER_HEIGHT);

        LayoutContainer bannerWrapperLeft = new LayoutContainer();
        bannerWrapperLeft.setWidth(BANNER_WRAPPER_LEFT_BORDER_WIDTH);
        bannerWrapperLeft.setId("ItemsDetailPanel-bannerWrapperLeft"); //$NON-NLS-1$
        bannerWrapper.add(bannerWrapperLeft, newBorderData(LayoutRegion.WEST, BANNER_WRAPPER_LEFT_BORDER_WIDTH));

        LayoutContainer bannerWrapperRight = new LayoutContainer();
        bannerWrapperRight.setWidth(BANNER_WRAPPER_RIGHT_BORDER_WIDTH);
        bannerWrapperRight.setId("ItemsDetailPanel-bannerWrapperRight"); //$NON-NLS-1$
        bannerWrapper.add(bannerWrapperRight, newBorderData(LayoutRegion.EAST, BANNER_WRAPPER_RIGHT_BORDER_WIDTH));

        banner.setId("ItemsDetailPanel-bannerWrapperCenter"); //$NON-NLS-1$
        bannerWrapper.add(banner, newBorderCenterData());

        return bannerWrapper;
    }

    private static final int SIDE_BORDER_WIDTH = 14;

    private static final int NORTH_BORDER_HEIGHT = 25;

    private static final int SOUTH_BORDER_HEIGHT = 11;

    /**
     * Initialize the gray outer borders of the ItemsDetailPanel using a border layout to emulate the HTML technique of
     * using an image for each corner and side.
     */
    private void initBorderPanels() {
        // North strip of the background
        LayoutContainer nBorder = new LayoutContainer(new BorderLayout());
        nBorder.setId("ItemsDetailPanel-nBorder"); //$NON-NLS-1$

        LayoutContainer nwCorner = new LayoutContainer();
        nwCorner.setId("ItemsDetailPanel-nwCorner"); //$NON-NLS-1$
        nBorder.add(nwCorner, newBorderData(LayoutRegion.WEST, SIDE_BORDER_WIDTH));

        LayoutContainer neCorner = new LayoutContainer();
        neCorner.setId("ItemsDetailPanel-neCorner"); //$NON-NLS-1$
        nBorder.add(neCorner, newBorderData(LayoutRegion.EAST, SIDE_BORDER_WIDTH));

        // ncBorder is an instance variable because we put the breadcrumb in it
        ncBorder.setId("ItemsDetailPanel-ncBorder"); //$NON-NLS-1$
        nBorder.add(this.ncBorder, newBorderCenterData());

        this.add(nBorder, newBorderData(LayoutRegion.NORTH, NORTH_BORDER_HEIGHT));

        // South strip of the background
        LayoutContainer sBorder = new LayoutContainer(new BorderLayout());
        sBorder.setId("ItemsDetail-sBorder"); //$NON-NLS-1$

        LayoutContainer swCorner = new LayoutContainer();
        swCorner.setId("ItemsDetailPanel-swCorner"); //$NON-NLS-1$
        sBorder.add(swCorner, newBorderData(LayoutRegion.WEST, SIDE_BORDER_WIDTH));

        LayoutContainer seCorner = new LayoutContainer();
        seCorner.setId("ItemsDetailPanel-seCorner"); //$NON-NLS-1$
        sBorder.add(seCorner, newBorderData(LayoutRegion.EAST, SIDE_BORDER_WIDTH));

        LayoutContainer scBorder = new LayoutContainer();
        scBorder.setId("ItemsDetailPanel-scBorder"); //$NON-NLS-1$
        sBorder.add(scBorder, newBorderCenterData());

        this.add(sBorder, newBorderData(LayoutRegion.SOUTH, SOUTH_BORDER_HEIGHT));

        // West strip of the background
        LayoutContainer wBorder = new LayoutContainer();
        wBorder.setId("ItemsDetailPanel-wBorder"); //$NON-NLS-1$
        this.add(wBorder, newBorderData(LayoutRegion.WEST, SIDE_BORDER_WIDTH));

        // East strip of the background
        LayoutContainer eBorder = new LayoutContainer();
        eBorder.setId("ItemsDetailPanel-eBorder"); //$NON-NLS-1$
        this.add(eBorder, newBorderData(LayoutRegion.EAST, SIDE_BORDER_WIDTH));

        // Main panel containing all the content
        this.add(this.mainPanel, newBorderCenterData());
    }

    /**
     * Utility method for generating BorderLayoutData for the borders.
     * 
     * @param region
     * @param width
     * @return
     */
    private BorderLayoutData newBorderData(LayoutRegion region, int width) {
        BorderLayoutData result = new BorderLayoutData(region, width, width, width);
        result.setSplit(false);
        result.setCollapsible(false);
        return result;
    }

    /**
     * Utility method for generating BorderLayoutData for the borders.
     * 
     * @return
     */
    private BorderLayoutData newBorderCenterData() {
        BorderLayoutData ncData = new BorderLayoutData(LayoutRegion.CENTER);
        ncData.setSplit(false);
        ncData.setCollapsible(false);
        return ncData;
    }

    public ContentPanel getTreeDetail() {
        return treeDetail;
    }

    public void setTreeDetail(ContentPanel treeDetail) {
        this.treeDetail = treeDetail;
    }

    private SavedCallback savedCallback;

    public void setSavedCallback(SavedCallback savedCallback) {
        this.savedCallback = savedCallback;
    }

    public void savedCallback(String ids) {
        if (savedCallback != null) {
            savedCallback.savedCallback(ids);
        }
    }

    public boolean isOutMost() {
        return isOutMost;
    }

    public void setOutMost(boolean isOutMost) {
        this.isOutMost = isOutMost;
    }

    public boolean isStaging() {
        return this.isStaging;
    }

    public void setStaging(boolean isStaging) {
        this.isStaging = isStaging;
    }

    public boolean isLineage() {
        return this.lineage;
    }

    public void setLineage(boolean lineage) {
        this.lineage = lineage;
    }
}
