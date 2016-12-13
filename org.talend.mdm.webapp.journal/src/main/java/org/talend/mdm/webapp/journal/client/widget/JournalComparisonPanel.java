/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.journal.client.Journal;
import org.talend.mdm.webapp.journal.client.JournalServiceAsync;
import org.talend.mdm.webapp.journal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.journal.client.resources.icon.Icons;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalParameters;
import org.talend.mdm.webapp.journal.shared.JournalTreeModel;

import com.amalto.core.objects.UpdateReportPOJO;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.Joint;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.TreeNode;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelView;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelView.TreeViewRenderMode;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Accessibility;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class JournalComparisonPanel extends ContentPanel {
    
    private JournalServiceAsync service = Registry.get(Journal.JOURNAL_SERVICE);

    private ToolBar toolbar;

    private TreePanel<JournalTreeModel> tree;

    private TreePanelView<JournalTreeModel> view;

    private JournalTreeModel root;

    private JournalComparisonPanel otherPanel;

    private Map<String, JournalTreeModel> modelMap = new HashMap<String, JournalTreeModel>();

    private List<String> changeNodeList;

    private Button previousChangeButton;

    private Button nextChangeButton;

    private Button restoreButton;

    public JournalComparisonPanel(String title, final JournalParameters parameter, final JournalGridModel journalGridModel,
            final boolean isBeforePanel) {
        this.setFrame(false);
        this.setHeading(title);
        this.setLayout(new FitLayout());
        this.setBodyBorder(false);

        toolbar = new ToolBar();
        
        if (isBeforePanel) {
            previousChangeButton = new Button(MessagesFactory.getMessages().previous_change_button());
            previousChangeButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.up()));
            previousChangeButton.setEnabled(false);            
            toolbar.add(previousChangeButton);
            
            nextChangeButton = new Button(MessagesFactory.getMessages().next_change_button());
            nextChangeButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.down()));
            nextChangeButton.setIconAlign(IconAlign.RIGHT);
            nextChangeButton.setEnabled(false);
            toolbar.add(nextChangeButton);
        }
        
        toolbar.add(new FillToolItem());
        restoreButton = new Button(MessagesFactory.getMessages().restore_button());
        restoreButton.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.restore()));
        restoreButton.setEnabled(false);
        toolbar.add(restoreButton);
        

        service.getComparisionTree(parameter, UrlUtil.getLanguage(), new SessionAwareAsyncCallback<JournalTreeModel>() {

            @Override
            public void onSuccess(JournalTreeModel root) {
                JournalComparisonPanel.this.root = root;
                TreeStore<JournalTreeModel> store = new TreeStore<JournalTreeModel>();
                store.add(root, true);

                view = new TreePanelView<JournalTreeModel>() {

                    @Override
                    public void onSelectChange(JournalTreeModel model, boolean select) {
                        if (select) {
                            tree.setExpanded(treeStore.getParent(model), true);
                        }
                        TreeNode node = findNode(model);
                        if (node != null) {
                            Element e = getElementContainer(node);
                            if (e != null) {
                                if (model.getCls() != null && !"".equals(model.getCls())) { //$NON-NLS-1$                               
                                    El.fly(e).setStyleName("x-ftree2-selected", select); //$NON-NLS-1$
                                    Document doc = e.getOwnerDocument();
                                    com.google.gwt.dom.client.Element textElement = doc.getElementById(model.getId()
                                            + "-" + isBeforePanel + "-journal-tree-node-text"); //$NON-NLS-1$ //$NON-NLS-2$
                                    if (textElement != null) {
                                        El.fly(textElement).setStyleName("x-tree3-node " + model.get("cls"), select); //$NON-NLS-1$ //$NON-NLS-2$
                                    }
                                } else {
                                    El.fly(e).setStyleName("x-ftree2-selected", select); //$NON-NLS-1$
                                }
                                if (select) {
                                    String tid = tree.getId();
                                    Accessibility.setState(tree.getElement(),
                                            "aria-activedescendant", tid + "__" + node.getElement().getId()); //$NON-NLS-1$ //$NON-NLS-2$ 
                                }
                            }
                        }
                    }

                    @Override
                    public String getTemplate(ModelData m, String id, String text, AbstractImagePrototype icon,
                            boolean checkable, boolean checked, Joint joint, int level, TreeViewRenderMode renderMode) {

                        if (renderMode == TreeViewRenderMode.CONTAINER) {
                            return "<div unselectable=on class=\"x-tree3-node-ct\" role=\"group\"></div>"; //$NON-NLS-1$
                        }
                        StringBuilder sb = new StringBuilder();
                        if (renderMode == TreeViewRenderMode.ALL || renderMode == TreeViewRenderMode.MAIN) {
                            sb.append("<div unselectable=on id=\""); //$NON-NLS-1$
                            sb.append(id);
                            sb.append("\""); //$NON-NLS-1$

                            sb.append(" class=\"x-tree3-node\"  role=\"presentation\">"); //$NON-NLS-1$

                            String cls = "x-tree3-el"; //$NON-NLS-1$
                            if (GXT.isHighContrastMode) {
                                switch (joint) {
                                case COLLAPSED:
                                    cls += " x-tree3-node-joint-collapse"; //$NON-NLS-1$
                                    break;
                                case EXPANDED:
                                    cls += " x-tree3-node-joint-expand"; //$NON-NLS-1$
                                    break;
                                case NONE:
                                    break;
                                }
                            }

                            sb.append("<div unselectable=on class=\"" + cls + "\" id=\"" + tree.getId() + "__" + id + "\" role=\"treeitem\" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            sb.append(" aria-level=\"" + (level + 1) + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        if (renderMode == TreeViewRenderMode.ALL || renderMode == TreeViewRenderMode.BODY) {
                            Element jointElement = null;
                            switch (joint) {
                            case COLLAPSED:
                                jointElement = (Element) tree.getStyle().getJointCollapsedIcon().createElement().cast();
                                break;
                            case EXPANDED:
                                jointElement = (Element) tree.getStyle().getJointExpandedIcon().createElement().cast();
                                break;
                            case NONE:
                                break;
                            }

                            if (jointElement != null) {
                                El.fly(jointElement).addStyleName("x-tree3-node-joint"); //$NON-NLS-1$
                            }

                            sb.append("<img src=\""); //$NON-NLS-1$
                            sb.append(GXT.BLANK_IMAGE_URL);
                            sb.append("\" style=\"height: 18px; width: "); //$NON-NLS-1$
                            sb.append(level * getIndenting(findNode((JournalTreeModel) m)));
                            sb.append("px;\" />"); //$NON-NLS-1$
                            sb.append(jointElement == null ? "<img src=\"" + GXT.BLANK_IMAGE_URL //$NON-NLS-1$
                                    + "\" style=\"width: 16px\" class=\"x-tree3-node-joint\" />" : DOM.toString(jointElement)); //$NON-NLS-1$
                            if (checkable) {
                                Element e = (Element) (checked ? GXT.IMAGES.checked().createElement().cast() : GXT.IMAGES
                                        .unchecked().createElement().cast());
                                El.fly(e).addStyleName("x-tree3-node-check"); //$NON-NLS-1$
                                sb.append(DOM.toString(e));
                            } else {
                                sb.append("<span class=\"x-tree3-node-check\"></span>"); //$NON-NLS-1$
                            }

                            // see TMDM-5438
                            if ("Document".equals(text) && level == 0 && ((JournalTreeModel) m).getChildCount() == 0) { //$NON-NLS-1$
                                icon = GXT.IMAGES.tree_folder();
                            }
                            if (icon != null) {
                                Element e = icon.createElement().cast();
                                El.fly(e).addStyleName("x-tree3-node-icon"); //$NON-NLS-1$
                                sb.append(DOM.toString(e));
                            } else {
                                sb.append("<span class=\"x-tree3-node-icon\"></span>"); //$NON-NLS-1$
                            }
                            sb.append("<span id='" + ((JournalTreeModel) m).getId() + "-" + isBeforePanel + "-journal-tree-node-text'  unselectable=on class=\"x-tree3-node-text\">"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(text);
                            sb.append("</span>"); //$NON-NLS-1$
                        }

                        if (renderMode == TreeViewRenderMode.ALL || renderMode == TreeViewRenderMode.MAIN) {
                            sb.append("</div>"); //$NON-NLS-1$
                            sb.append("</div>"); //$NON-NLS-1$
                        }
                        return sb.toString();

                    }
                };

                tree = new TreePanel<JournalTreeModel>(store) {

                    @Override
                    protected String renderChild(JournalTreeModel parent, JournalTreeModel child, int depth,
                            TreeViewRenderMode renderMode) {
                        String nodeStr = super.renderChild(parent, child, depth, renderMode);
                        if (child.get("cls") != null) { //$NON-NLS-1$
                            nodeStr = nodeStr.replaceFirst("x-tree3-node", "x-tree3-node " + child.get("cls")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                        return nodeStr;
                    }
                };
                tree.setView(view);

                tree.setDisplayProperty("name"); //$NON-NLS-1$
                tree.getStyle().setLeafIcon(AbstractImagePrototype.create(Icons.INSTANCE.leaf()));

                if (modelMap.size() == 0) {
                    List<JournalTreeModel> list = store.getAllItems();
                    for (JournalTreeModel model : list) {
                        modelMap.put(model.getPath(), model);
                    }
                }

                tree.addListener(Events.Expand, new Listener<TreePanelEvent<JournalTreeModel>>() {

                    @Override
                    public void handleEvent(TreePanelEvent<JournalTreeModel> be) {
                        if (otherPanel.getModelMap().size() > 0) {
                            JournalTreeModel model = otherPanel.getModelMap().get(be.getItem().getPath());
                            otherPanel.getTree().setExpanded(model, true);
                        }
                    }
                });

                tree.addListener(Events.Collapse, new Listener<TreePanelEvent<JournalTreeModel>>() {

                    @Override
                    public void handleEvent(TreePanelEvent<JournalTreeModel> be) {
                        if (otherPanel.getModelMap().size() > 0) {
                            JournalTreeModel model = otherPanel.getModelMap().get(be.getItem().getPath());
                            otherPanel.getTree().setExpanded(model, false);
                        }
                    }
                });

                JournalComparisonPanel.this.add(tree);
                JournalComparisonPanel.this.layout(true);
                JournalComparisonPanel.this.expandRoot();
                if (changeNodeList != null && changeNodeList.size() > 0) {
                    tree.expandAll();
                }
            }
        });

        if (UpdateReportPOJO.OPERATION_TYPE_UPDATE.equals(journalGridModel.getOperationType())) {

            this.changeNodeList = journalGridModel.getChangeNodeList();

            final int count[] = new int[1];
            count[0] = -1;

            if (isBeforePanel && previousChangeButton != null && nextChangeButton != null) {
                previousChangeButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        if (count[0] > 0) {
                            count[0] = count[0] - 1;
                        }
                        buttonStatus(count[0]);
                        selectTreeNodeByPath(changeNodeList.get(count[0]));
                        otherPanel.selectTreeNodeByPath(changeNodeList.get(count[0]));
                    }
                });

                nextChangeButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        if (count[0] < changeNodeList.size() - 1) {
                            count[0] = count[0] + 1;
                        }
                        buttonStatus(count[0]);
                        selectTreeNodeByPath(changeNodeList.get(count[0]));
                        otherPanel.selectTreeNodeByPath(changeNodeList.get(count[0]));
                    }
                });
                nextChangeButton.setEnabled(true);
            }
        }
        
        if (UpdateReportPOJO.OPERATION_TYPE_UPDATE.equals(journalGridModel.getOperationType()) || (UpdateReportPOJO.OPERATION_TYPE_LOGICAL_DELETE.equals(journalGridModel.getOperationType()) && isBeforePanel)) {
            service.isAdmin(new SessionAwareAsyncCallback<Boolean>() {

                @Override
                public void onSuccess(Boolean isAdmin) {
                    if (isAdmin) {
                        restoreButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                            @Override
                            public void componentSelected(ButtonEvent ce) {
                                JournalGridPanel.getInstance().restore(parameter,true);
                            };
                        });
                        restoreButton.setEnabled(parameter.isAuth());
                    }
                }
            });
        }
        this.setTopComponent(toolbar);
    }

    private void buttonStatus(int index) {
        if (index == 0) {
            previousChangeButton.setEnabled(false);
        } else {
            previousChangeButton.setEnabled(true);
        }
        if (index == changeNodeList.size() - 1) {
            nextChangeButton.setEnabled(false);
        } else {
            nextChangeButton.setEnabled(true);
        }
    }

    public void expandRoot() {
        if (root.getChildren().size() > 0) {
            JournalTreeModel model = (JournalTreeModel) root.getChildren().get(0);
            tree.setExpanded(root, true);
            tree.setExpanded(model, true);
        }
    }

    public void selectTreeNodeByPath(String path) {
        JournalTreeModel model = modelMap.get(path);
        if (path.endsWith("[1]") && model == null) { //$NON-NLS-1$
            model = modelMap.get(path.replace("[1]", "")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        tree.getSelectionModel().select(false, model);
        tree.scrollIntoView(model);
    }

    public TreePanel<JournalTreeModel> getTree() {
        return tree;
    }

    public void setOtherPanel(JournalComparisonPanel otherPanel) {
        this.otherPanel = otherPanel;
    }

    public Map<String, JournalTreeModel> getModelMap() {
        return modelMap;
    }
}