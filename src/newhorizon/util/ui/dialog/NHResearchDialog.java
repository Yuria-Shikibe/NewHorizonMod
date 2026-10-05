package newhorizon.util.ui.dialog;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.scene.ui.layout.Scl;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Scaling;
import mindustry.content.TechTree;
import mindustry.content.TechTree.TechNode;
import mindustry.game.EventType.ResearchEvent;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemSeq;
import mindustry.type.ItemStack;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.ctype.UnlockableContent;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ResearchDialog;
import mindustry.world.Block;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.content.NHPlanets;
import newhorizon.content.NHTechTree;

import static mindustry.Vars.net;
import static mindustry.Vars.state;
import static mindustry.Vars.ui;

public class NHResearchDialog extends ResearchDialog {
    private static final float CARD_WIDTH = 224f;
    private static final float CARD_HEIGHT = 104f;
    private static final float CARD_X_GAP = 74f;
    private static final float CARD_Y_GAP = 34f;
    private static final float CANVAS_PAD = 70f;

    private final ResearchDialog vanilla;
    private final Seq<Category> categories = new Seq<>();
    private final ObjectMap<UnlockableContent, Entry> entries = new ObjectMap<>();
    private final ObjectMap<UnlockableContent, TechNode> nativeNodes = new ObjectMap<>();
    private final ObjectMap<String, NodeGroup> nodeGroups = new ObjectMap<>();

    private Table detailTable;
    private TechCanvas canvas;
    private ScrollPane treePane;
    private Category selectedCategory;
    private Entry selectedEntry;
    private NodeGroup selectedNode;
    private ItemSeq researchItems;
    private ObjectMap<Sector, ItemSeq> researchCache = new ObjectMap<>();
    private float zoom = 1f;
    private boolean modelReady;
    private boolean forceNewHorizon;

    public static void install() {
        if (ui == null || ui.research instanceof NHResearchDialog) return;
        ui.research = new NHResearchDialog();
    }

    public NHResearchDialog() {
        super();
        vanilla = new ResearchDialog();
        installVanillaRootSelector();
        shouldPause = true;
        buildShell();
        Events.on(ResearchEvent.class, event -> {
            if (isShown()) Core.app.post(this::refreshSelected);
        });
    }

    @Override
    public NHResearchDialog show() {
        if (!forceNewHorizon && !isNewHorizonContext()) {
            vanilla.show();
            return this;
        }
        if (!modelReady) buildModel();
        researchItems = buildResearchItems();
        if (selectedCategory == null && categories.size > 0) selectedCategory = categories.first();
        rebuildShell();
        super.show();
        Core.app.post(() -> {
            if (selectedEntry != null) canvas.focusEntry(selectedEntry);
            else if (selectedNode != null) canvas.focusGroup(selectedNode);
            else canvas.focusFirst();
        });
        return this;
    }

    @Override
    public void hide() {
        if (vanilla.isShown()) vanilla.hide();
        super.hide();
        forceNewHorizon = false;
    }

    private boolean isNewHorizonContext() {
        Planet planet = null;
        if (ui != null && ui.planet != null && ui.planet.isShown()) {
            planet = ui.planet.state.planet;
        } else if (state != null && state.isCampaign() && state.rules.sector != null) {
            planet = state.rules.sector.planet;
        }
        return planet == NHPlanets.midantha || (planet != null && planet.techTree == NHTechTree.root);
    }

    private void installVanillaRootSelector() {
        Table title = vanilla.titleTable;
        if (title == null) return;
        title.clearChildren();
        title.top();
        title.button(button -> {
            button.image(Icon.tree).size(32f).padRight(8f);
            button.add(Core.bundle.get("nh.research.tech-tree")).growX();
        }, this::openRootSelector).width(260f).height(52f).pad(3f);
    }

    private void openRootSelector() {
        new BaseDialog(Core.bundle.get("nh.research.tech-tree-selector")) {{
            cont.pane(list -> {
                list.left();
                list.defaults().growX().height(62f).pad(3f);
                for (TechNode root : TechTree.roots) {
                    if (root.requiresUnlock && !root.content.unlockedHost() && root != NHTechTree.root) continue;
                    String label = root == NHTechTree.root ? Core.bundle.get("nh.research.new-horizon") : root.localizedName();
                    list.button(button -> {
                        button.left().margin(5f);
                        button.image(root.icon()).size(44f).padRight(10f);
                        button.add(label).growX().left();
                    }, Styles.flatTogglet, () -> {
                        hide();
                        if (root == NHTechTree.root) {
                            forceNewHorizon = true;
                            vanilla.hide();
                            NHResearchDialog.this.show();
                        } else {
                            NHResearchDialog.this.hide();
                            vanilla.show();
                            vanilla.rebuildTree(root);
                        }
                    }).height(62f).row();
                }
            }).grow();
            addCloseButton();
        }}.show();
    }
    private void buildShell() {
        clearChildren();
        cont.clearChildren();
        buttons.clearChildren();
        background(Tex.pane);
        add(cont).grow();
        cont.top().left().margin(10f);
        addCloseListener();
    }

    private void rebuildShell() {
        cont.clearChildren();
        cont.top().left().margin(10f);

        cont.table(header -> {
            header.left().defaults().pad(4f);
            header.image(new TextureRegionDrawable(NHContent.icon2)).size(42f);
            header.table(title -> {
                title.left();
                title.add("[accent]" + Core.bundle.get("nh.research.network-title") + "[]").left().row();
                title.add("[lightgray]" + Core.bundle.get("nh.research.architecture") + "[]").left();
            }).growX();
            header.table(status -> {
                status.right();
                status.label(() -> Core.bundle.format("nh.research.online-count", unlockedCount(), entries.size)).right().row();
                status.label(() -> researchItems == null ? "[gray]" + Core.bundle.get("nh.research.resource-link-offline") : "[lightgray]" + Core.bundle.get("nh.research.resource-link-active")).right();
            }).right();
            header.button(Core.bundle.get("nh.research.tech-tree"), Styles.cleart, this::openRootSelector).size(150f, 46f);
            header.button("@back", Icon.left, Styles.cleart, this::hide).size(120f, 46f);
        }).growX().row();

        cont.image(Tex.whiteui, Pal.accent).height(2f).growX().padTop(3f).padBottom(7f).row();

        Table categoryTable = new Table();
        categoryTable.left();
        categoryTable.defaults().size(218f, 84f).pad(5f);
        for (Category category : categories) {
            TextButton button = new TextButton(category.title, category == selectedCategory ? Styles.togglet : Styles.clearTogglet);
            button.clearChildren();
            button.left().margin(7f);
            button.image(category.icon).size(34f).padRight(7f);
            button.table(labels -> {
                labels.left();
                labels.add(category.title).width(162f).wrap().left().row();
                labels.add("[gray]" + Core.bundle.format("nh.research.technology-count", category.entries.size)).left();
            }).growX();
            button.clicked(() -> selectCategory(category));
            categoryTable.add(button);
        }
        ScrollPane categoryPane = new ScrollPane(categoryTable, Styles.smallPane);
        categoryPane.setScrollingDisabled(false, true);
        categoryPane.setFadeScrollBars(false);
        cont.add(categoryPane).growX().height(102f).row();

        cont.table(body -> {
            body.top().left();
            canvas = new TechCanvas();
            treePane = new ScrollPane(canvas, Styles.smallPane);
            treePane.setScrollingDisabled(false, false);
            treePane.setFadeScrollBars(false);
            treePane.setOverscroll(false, false);
            treePane.update(() -> {
                if (treePane.hasMouse()) treePane.requestScroll();
            });
            treePane.addCaptureListener(new InputListener() {
                @Override
                public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                    float amount = Math.abs(amountY) > 0.0001f ? amountY : amountX;
                    if (Math.abs(amount) <= 0.0001f) return false;
                    canvas.setZoom(zoom - amount * 0.1f);
                    event.stop();
                    return true;
                }
            });
            body.add(treePane).minSize(0f).grow();

            detailTable = new Table(Tex.button);
            detailTable.margin(10f);
            ScrollPane detailPane = new ScrollPane(detailTable, Styles.smallPane);
            detailPane.setScrollingDisabled(true, false);
            body.add(detailPane).width(320f).growY().padLeft(8f);
        }).grow().row();

        cont.table(footer -> {
            footer.left().defaults().pad(3f);
            footer.add("[gray]" + Core.bundle.get("nh.research.controls") + "[]").wrap().growX();
            footer.add().growX();
            footer.button("+", Styles.cleart, () -> canvas.setZoom(zoom + 0.1f)).size(42f, 38f);
            footer.button("100%", Styles.cleart, () -> canvas.setZoom(1f)).size(60f, 38f);
            footer.button("-", Styles.cleart, () -> canvas.setZoom(zoom - 0.1f)).size(42f, 38f);
        }).growX().row();

        rebuildCanvas();
        rebuildDetail();
    }

    private void buildModel() {
        modelReady = false;
        categories.clear();
        entries.clear();
        nativeNodes.clear();
        nodeGroups.clear();
        if (NHTechTree.root == null) return;

        NHTechTree.root.each(node -> {
            if (node != NHTechTree.root && node.content != null && !nativeNodes.containsKey(node.content)) {
                nativeNodes.put(node.content, node);
            }
        });

        ObjectMap<String, Category> byName = new ObjectMap<>();
        addCategory(byName, NHResearchTreeModel.MATERIALS, Icon.box, Color.valueOf("8dd6ff"));
        addCategory(byName, NHResearchTreeModel.FLUIDS, Icon.liquid, Color.valueOf("65c9d8"));
        addCategory(byName, NHResearchTreeModel.MINING, Icon.production, Color.valueOf("b9d477"));
        addCategory(byName, NHResearchTreeModel.LOGISTICS, Icon.distribution, Color.valueOf("f6c96d"));
        addCategory(byName, NHResearchTreeModel.POWER, Icon.power, Color.valueOf("ffe46b"));
        addCategory(byName, NHResearchTreeModel.FABRICATION, Icon.production, Color.valueOf("e7a2ff"));
        addCategory(byName, NHResearchTreeModel.COMBAT, Icon.turret, Color.valueOf("ff8d8d"));
        addCategory(byName, NHResearchTreeModel.SYSTEMS, Icon.logic, Color.valueOf("ffcf7a"));
        addCategory(byName, NHResearchTreeModel.MAPS, Icon.tree, Color.valueOf("98b7ff"));

        NHResearchTreeModel.Model researchModel = NHResearchTreeModel.build(NHTechTree.root);
        for (NHResearchTreeModel.Group definition : researchModel.groups) {
            Drawable icon = definition.asset == null ? categoryIcon(definition.category)
                    : nodeIcon(definition.asset, null);
            addVanillaNode(byName, definition.category, definition.key, definition.title, icon);
        }

        nativeNodes.each((content, node) -> {
            NHResearchTreeModel.Assignment assignment = researchModel.assignment(content);
            String categoryName = assignment == null ? NHResearchTreeModel.SYSTEMS : assignment.category;
            Category category = byName.get(categoryName);
            if (category == null) return;
            Entry entry = new Entry(content, node, category, assignment == null ? 0 : assignment.threat);
            entries.put(content, entry);
            category.entries.add(entry);
            NodeGroup group = assignment == null ? null : nodeGroups.get(assignment.groupKey);
            if (group == null || group.category != category) group = nearestBranchGroup(node, category);
            if (group == null) group = category.nodes.first();
            group.entries.add(entry);
            entry.group = group;
        });

        entries.each((content, entry) -> {
            TechNode parent = entry.node.parent;
            Entry parentEntry = parent == null || parent.content == null ? null : entries.get(parent.content);
            if (parentEntry != null && parentEntry != entry) {
                parentEntry.children.add(entry);
                entry.parent = parentEntry;
            } else {
                entry.category.roots.add(entry);
            }
            if (parent != null && parent.content != null && parent.content.minfo.mod == NewHorizon.MOD) {
                entry.prerequisites.add(parent.content);
            }
        });

        nodeGroups.each((asset, group) -> {
            group.minDepth = Integer.MAX_VALUE;
            for (Entry entry : group.entries) group.minDepth = Math.min(group.minDepth, entry.node.depth);
            if (group.minDepth == Integer.MAX_VALUE) group.minDepth = 0;
        });
        entries.each((content, entry) -> {
            if (entry.group == null) return;
            TechNode ancestor = entry.node.parent;
            while (ancestor != null && ancestor.content != null) {
                Entry parentEntry = entries.get(ancestor.content);
                if (parentEntry != null && parentEntry.group != null && parentEntry.group != entry.group) {
                    NodeGroup parent = parentEntry.group;
                    if (parent.category == entry.group.category && !entry.group.parentOptions.contains(parent)) {
                        entry.group.parentOptions.add(parent);
                    }
                    break;
                }
                ancestor = ancestor.parent;
            }
        });
        nodeGroups.each((asset, group) -> {
            group.parentOptions.sort((a, b) -> b.minDepth - a.minDepth);
            for (NodeGroup candidate : group.parentOptions) {
                if (candidate.minDepth < group.minDepth) {
                    group.parent = candidate;
                    break;
                }
            }
        });

        categories.each(category -> {
            category.entries.sort((a, b) -> a.node.depth - b.node.depth);
            category.roots.sort((a, b) -> a.node.depth - b.node.depth);
            category.nodes.sort((a, b) -> a.minDepth - b.minDepth);
        });
        if (categories.size > 0) selectedCategory = categories.first();
        modelReady = true;
    }

    private Drawable categoryIcon(String category) {
        return switch (category) {
            case NHResearchTreeModel.FLUIDS -> Icon.liquid;
            case NHResearchTreeModel.MINING -> Icon.production;
            case NHResearchTreeModel.LOGISTICS -> Icon.distribution;
            case NHResearchTreeModel.POWER -> Icon.power;
            case NHResearchTreeModel.COMBAT -> Icon.turret;
            case NHResearchTreeModel.SYSTEMS -> Icon.logic;
            case NHResearchTreeModel.MAPS -> Icon.tree;
            default -> Icon.box;
        };
    }
    private void addVanillaNode(ObjectMap<String, Category> byName, String categoryName, String asset, String title, Drawable icon) {
        Category category = byName.get(categoryName);
        if (category == null || nodeGroups.containsKey(asset)) return;
        NodeGroup group = new NodeGroup(asset, title, category, icon);
        nodeGroups.put(asset, group);
        category.nodes.add(group);
    }

    private void addCategory(ObjectMap<String, Category> byName, String key, Drawable icon, Color color) {
        Category category = new Category(key, Core.bundle.get("nh.research.category." + key), icon, color);
        categories.add(category);
        byName.put(key, category);
    }

    private NodeGroup nearestBranchGroup(TechNode node, Category category) {
        TechNode current = node == null ? null : node.parent;
        while (current != null) {
            Entry parentEntry = current.content == null ? null : entries.get(current.content);
            if (parentEntry != null && parentEntry.group != null && parentEntry.group.category == category) {
                return parentEntry.group;
            }
            current = current.parent;
        }
        return null;
    }
    private void selectCategory(Category category) {
        selectedCategory = category;
        selectedNode = null;
        selectedEntry = null;
        rebuildShell();
    }

    private void rebuildCanvas() {
        if (canvas == null || selectedCategory == null) return;
        canvas.setNodes(selectedCategory);
        rebuildDetail();
    }

    private void rebuildDetail() {
        if (detailTable == null) return;
        detailTable.clearChildren();
        detailTable.top().left().margin(12f);
        if (selectedEntry != null) {
            buildContentDetail(selectedEntry);
            return;
        }
        if (selectedNode == null) {
            detailTable.add("[accent]" + Core.bundle.get("nh.research.select-node") + "[]").width(286f).wrap().left().row();
            detailTable.add(Core.bundle.get("nh.research.select-node-description"))
                    .color(Color.lightGray).wrap().width(286f).left().padTop(8f);
            return;
        }

        NodeGroup node = selectedNode;
        detailTable.image(node.icon).size(96f).left().row();
        Label title = detailTable.add(node.title).width(286f).wrap().left().get();
        title.setFontScale(1.1f);
        detailTable.row();
        detailTable.add(node.category.title).color(node.category.color).left().row();
        detailTable.image(Tex.whiteui, node.category.color).height(2f).growX().padTop(6f).padBottom(7f).row();
        detailTable.add(node.description).color(Color.lightGray).wrap().width(286f).left().row();

        detailTable.table(req -> {
            req.left().defaults().padTop(8f);
            req.add(Core.bundle.get("nh.research.related-branches")).color(Pal.accent).left().row();
            if (node.parentOptions.size == 0) {
                req.add(Core.bundle.get("nh.research.root-access")).color(Pal.heal).left().row();
            } else {
                for (int i = 0; i < node.parentOptions.size; i++) {
                    NodeGroup prerequisite = node.parentOptions.get(i);
                    req.button(prerequisite.title, Styles.cleart, () -> {
                        selectedNode = prerequisite;
                        selectedEntry = null;
                        rebuildDetail();
                        canvas.focusGroup(prerequisite);
                    }).width(286f).height(44f).row();
                }
            }
        }).growX().left().row();

        detailTable.add("[accent]" + Core.bundle.format("nh.research.associated-technology-count", node.entries.size) + "[]").left().padTop(10f).row();
        Table contentList = new Table();
        contentList.left();
        contentList.defaults().growX().height(58f).pad(3f);
        for (Entry entry : node.entries) {
            TextButton contentButton = new TextButton(entry.content.localizedName, Styles.clearTogglet);
            contentButton.clearChildren();
            contentButton.left().margin(5f);
            contentButton.image(entry.icon).size(38f).padRight(7f);
            contentButton.add(entry.content.localizedName).width(224f).wrap().left();
            contentButton.clicked(() -> selectEntry(entry));
            contentList.add(contentButton).row();
        }
        ScrollPane contentPane = new ScrollPane(contentList, Styles.smallPane);
        contentPane.setScrollingDisabled(true, false);
        contentPane.setFadeScrollBars(false);
        detailTable.add(contentPane).growX().height(270f).padTop(6f).row();
    }

    private boolean nodeUnlocked(NodeGroup node) {
        return node.entries.size > 0 && node.entries.contains(entry -> entry.content.unlockedHost());
    }

    private void selectEntry(Entry entry) {
        boolean changeCategory = selectedCategory != entry.category;
        selectedCategory = entry.category;
        selectedNode = entry.group;
        selectedEntry = entry;
        entry.group.expanded = true;
        if (changeCategory) rebuildShell();
        else rebuildCanvas();
        Core.app.post(() -> canvas.focusEntry(entry));
    }

    private void buildContentDetail(Entry entry) {
        detailTable.button(Core.bundle.get("nh.research.back-to-node"), Styles.cleart, () -> {
            selectedEntry = null;
            rebuildDetail();
        }).growX().height(38f).left().row();
        detailTable.table(iconRow -> {
            iconRow.image(entry.icon).size(74f);
            iconRow.add().growX();
            iconRow.button(Icon.info, Styles.clearNonei, () -> ui.content.show(entry.content)).size(48f);
        }).width(286f).left().row();
        Label title = detailTable.add(entry.content.localizedName).width(286f).wrap().left().get();
        title.setFontScale(1.1f);
        detailTable.row();
        detailTable.add(Core.bundle.format("nh.research.category-threat", entry.category.title, entry.threat)).color(entry.category.color).left().row();
        detailTable.image(Tex.whiteui, entry.category.color).height(2f).growX().padTop(6f).padBottom(7f).row();
        detailTable.add(entry.content.displayDescription()).color(Color.lightGray).wrap().width(286f).left().row();

        detailTable.table(req -> {
            req.left().defaults().padTop(8f);
            req.add(Core.bundle.get("nh.research.research-route")).color(Pal.accent).left().row();
            if (entry.prerequisites.size == 0) {
                req.add(Core.bundle.get("nh.research.node-access")).color(Pal.heal).left().row();
            } else {
                for (int i = 0; i < entry.prerequisites.size; i++) {
                    UnlockableContent prerequisite = entry.prerequisites.get(i);
                    Entry prerequisiteEntry = entries.get(prerequisite);
                    if (prerequisiteEntry != null) {
                        req.button(prerequisite.localizedName, Styles.cleart, () -> selectEntry(prerequisiteEntry))
                                .width(286f).height(44f).row();
                    } else {
                        req.add(prerequisite.localizedName).width(286f).wrap().left().row();
                    }
                    if (i + 1 < entry.prerequisites.size) req.add(Core.bundle.get("nh.research.or")).color(Pal.accent).left().padLeft(22f).row();
                }
            }
        }).growX().left().row();

        if (entry.node.requirements.length > 0) {
            detailTable.table(req -> {
                req.left().defaults().padTop(4f);
                req.add(Core.bundle.get("nh.research.resource-investment")).color(Pal.accent).left().row();
                for (int i = 0; i < entry.node.requirements.length; i++) {
                    ItemStack stack = entry.node.requirements[i];
                    int current = entry.node.finishedRequirements[i].amount;
                    int available = researchItems == null ? 0 : researchItems.get(stack.item);
                    req.add(stack.item.localizedName + "  " + current + " / " + stack.amount + "  [gray](" + available + ")")
                            .color(current >= stack.amount ? Pal.heal : available > 0 ? Color.lightGray : Pal.remove).left().row();
                }
            }).growX().left().row();
        } else {
            detailTable.add(Core.bundle.get("nh.research.auto-synchronized-content")).color(Pal.heal).left().padTop(10f).row();
        }

        if (entry.content.unlockedHost()) {
            detailTable.add(Core.bundle.get("nh.research.online")).color(Pal.heal).left().padTop(12f).row();
        } else {
            TextButton research = new TextButton(Core.bundle.get("nh.research.research"), Styles.togglet);
            research.clicked(() -> research(entry));
            research.update(() -> research.setDisabled(!canResearch(entry)));
            detailTable.add(research).growX().height(48f).padTop(12f).row();
            Label access = detailTable.label(() -> Core.bundle.get(canResearch(entry) ? "nh.research.access-granted" : "nh.research.waiting-for-prerequisites"))
                    .left().get();
            access.update(() -> access.setColor(canResearch(entry) ? Pal.heal : Pal.remove));
            detailTable.row();
        }
    }
    private void refreshSelected() {
        if (!isShown()) return;
        researchItems = buildResearchItems();
        rebuildCanvas();
        rebuildDetail();
    }

    private boolean canResearch(Entry entry) {
        if (entry.content.unlockedHost() || net != null && net.client()) return false;
        if (entry.prerequisites.size > 0 && !entry.prerequisites.contains(UnlockableContent::unlockedHost)) return false;
        if (entry.node.objectives.contains(objective -> !objective.complete())) return false;
        if (entry.node.requirements.length == 0) return true;
        for (int i = 0; i < entry.node.requirements.length; i++) {
            if (entry.node.finishedRequirements[i].amount < entry.node.requirements[i].amount
                    && (researchItems == null || researchItems.get(entry.node.requirements[i].item) > 0)) return true;
        }
        return allRequirementsMet(entry);
    }

    private boolean allRequirementsMet(Entry entry) {
        for (int i = 0; i < entry.node.requirements.length; i++) {
            if (entry.node.finishedRequirements[i].amount < entry.node.requirements[i].amount) return false;
        }
        return true;
    }

    private void research(Entry entry) {
        if (!canResearch(entry)) return;
        boolean complete = true;
        for (int i = 0; i < entry.node.requirements.length; i++) {
            ItemStack requirement = entry.node.requirements[i];
            ItemStack finished = entry.node.finishedRequirements[i];
            int available = researchItems == null ? 0 : researchItems.get(requirement.item);
            int used = Math.max(Math.min(requirement.amount - finished.amount, available), 0);
            if (used > 0) {
                consumeResearchItem(requirement.item, used);
                researchItems.remove(requirement.item, used);
            }
            finished.amount += used;
            if (finished.amount < requirement.amount) complete = false;
        }
        if (complete) {
            entry.content.unlock();
            TechNode parent = entry.node.parent;
            while (parent != null) {
                parent.content.unlock();
                parent = parent.parent;
            }
            autoUnlock(entry);
            Sounds.uiUnlock.play();
            Events.fire(new ResearchEvent(entry.content));
        }
        entry.node.save();
        refreshSelected();
    }

    private void autoUnlock(Entry parent) {
        for (Entry child : parent.children) {
            if (!child.content.unlockedHost() && child.node.requirements.length == 0 && child.node.objectives.isEmpty()
                    && (child.prerequisites.size == 0 || child.prerequisites.contains(UnlockableContent::unlockedHost))
                    && !child.content.alwaysUnlocked) {
                child.content.quietUnlock();
                autoUnlock(child);
            }
        }
    }

    private int unlockedCount() {
        int count = 0;
        for (Entry entry : entries.values()) if (entry.content.unlockedHost()) count++;
        return count;
    }

    private ItemSeq buildResearchItems() {
        ItemSeq result = new ItemSeq();
        researchCache.clear();
        for (Planet planet : Seq.with(NHPlanets.midantha)) {
            for (Sector sector : planet.sectors) {
                if (sector.hasBase() && !sector.isFrozen()) {
                    ItemSeq cached = sector.items();
                    researchCache.put(sector, cached);
                    cached.each((item, amount) -> result.add(item, Math.max(amount, 0)));
                }
            }
        }
        return result;
    }

    private void consumeResearchItem(Item item, int amount) {
        int remaining = amount;
        Sector current = state != null && state.isCampaign() ? state.rules.sector : null;
        for (Sector sector : researchCache.keys()) {
            if (remaining == 0 || sector == current) continue;
            ItemSeq sequence = researchCache.get(sector);
            int removed = Math.min(sequence.get(item), remaining);
            if (removed > 0) {
                sector.removeItem(item, removed);
                sequence.remove(item, removed);
                remaining -= removed;
            }
        }
        if (remaining > 0 && current != null && researchCache.containsKey(current)) {
            ItemSeq sequence = researchCache.get(current);
            int removed = Math.min(sequence.get(item), remaining);
            if (removed > 0) {
                current.removeItem(item, removed);
                sequence.remove(item, removed);
            }
        }
    }
    private Drawable nodeIcon(String asset, UnlockableContent fallback) {
        TextureRegion region = Core.atlas.find(NewHorizon.name(asset));
        if (fallback == null) return new TextureRegionDrawable(region);
        return region == Core.atlas.find("error") ? new TextureRegionDrawable(fallback.uiIcon) : new TextureRegionDrawable(region);
    }

    private static Drawable contentIcon(UnlockableContent content) {
        return new TextureRegionDrawable(content.uiIcon);
    }
    private final class TechCanvas extends WidgetGroup {
        private final Seq<GraphNode> vertices = new Seq<>();
        private final Seq<GraphNode> roots = new Seq<>();
        private final ObjectMap<NodeGroup, GraphNode> groups = new ObjectMap<>();
        private final ObjectMap<Entry, GraphNode> contents = new ObjectMap<>();
        private final WidgetGroup graph = new WidgetGroup() {
            @Override
            public void draw() {
                validate();
                applyTransform(computeTransform());
                drawConnections();
                drawChildren();
                resetTransform();
            }
        };
        private float layoutWidth;
        private float layoutHeight;
        private final Vec2[] touchPositions = {new Vec2(), new Vec2()};
        private final Vec2 screenPosition = new Vec2();
        private float pinchDistance;
        private float pinchZoom;
        private boolean pinching;

        TechCanvas() {
            graph.setTransform(true);
            graph.cullable = false;
            addChild(graph);
            addCaptureListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    if (pointer > 1) return false;
                    touchPositions[pointer].set(x, y);
                    if (pointer == 1) {
                        pinchDistance = touchPositions[0].dst(touchPositions[1]);
                        pinchZoom = zoom;
                        pinching = pinchDistance > 0f;
                        return pinching;
                    }
                    return false;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    if (pointer > 1 || !pinching) return;
                    touchPositions[pointer].set(x, y);
                    float distance = touchPositions[0].dst(touchPositions[1]);
                    if (pinchDistance > 0f && distance > 0f) {
                        setZoom(pinchZoom * distance / pinchDistance);
                    }
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    if (pointer > 1) return;
                    touchPositions[pointer].set(x, y);
                    pinching = false;
                }

            });
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (!pinching || !Core.input.isTouched(0) || !Core.input.isTouched(1)) return;
            for (int pointer = 0; pointer < 2; pointer++) {
                screenPosition.set(Core.input.mouseX(pointer), Core.input.mouseY(pointer));
                screenToLocalCoordinates(screenPosition);
                touchPositions[pointer].set(screenPosition);
            }
            float distance = touchPositions[0].dst(touchPositions[1]);
            if (pinchDistance > 0f && distance > 0f) {
                setZoom(pinchZoom * distance / pinchDistance);
            }
        }

        void setNodes(Category category) {
            graph.clearChildren();
            vertices.clear();
            roots.clear();
            groups.clear();
            contents.clear();
            if (category == null) return;

            for (NodeGroup group : category.nodes) {
                if (group.entries.isEmpty()) continue;
                GraphNode vertex = new GraphNode(group, null, createNodeCard(group));
                vertices.add(vertex);
                groups.put(group, vertex);
            }
            for (Entry entry : category.entries) {
                if (!entry.group.expanded) continue;
                GraphNode vertex = new GraphNode(entry.group, entry, createContentCard(entry));
                vertices.add(vertex);
                contents.put(entry, vertex);
            }
            for (GraphNode vertex : vertices) {
                GraphNode parent;
                if (vertex.entry == null) {
                    parent = vertex.group.parent == null ? null : groups.get(vertex.group.parent);
                } else {
                    Entry entry = vertex.entry;
                    parent = entry.parent != null && entry.parent.group == entry.group ? contents.get(entry.parent) : null;
                    if (parent == null) parent = groups.get(entry.group);
                }
                vertex.parent = parent;
                if (parent == null) roots.add(vertex);
                else parent.children.add(vertex);
            }

            float top = Scl.scl(CANVAS_PAD);
            layoutWidth = Scl.scl(CARD_WIDTH + CANVAS_PAD * 2f);
            for (GraphNode root : roots) {
                measure(root, new ObjectSet<>());
                place(root, 0, top);
                top += root.span + Scl.scl(CARD_Y_GAP);
            }
            layoutHeight = top + Scl.scl(CANVAS_PAD);
            for (GraphNode vertex : vertices) {
                vertex.y = layoutHeight - vertex.y - Scl.scl(CARD_HEIGHT);
                vertex.card.setBounds(vertex.x, vertex.y, Scl.scl(CARD_WIDTH), Scl.scl(CARD_HEIGHT));
                graph.addChild(vertex.card);
            }
            graph.setSize(layoutWidth, layoutHeight);
            graph.setScale(zoom);
            setSize(getPrefWidth(), getPrefHeight());
            invalidateHierarchy();
        }

        private float measure(GraphNode vertex, ObjectSet<GraphNode> visiting) {
            if (!visiting.add(vertex)) throw new IllegalStateException("Cyclic research graph");
            float childrenHeight = 0f;
            for (GraphNode child : vertex.children) childrenHeight += measure(child, visiting);
            childrenHeight += Math.max(0, vertex.children.size - 1) * Scl.scl(CARD_Y_GAP);
            visiting.remove(vertex);
            vertex.span = Math.max(Scl.scl(CARD_HEIGHT), childrenHeight);
            return vertex.span;
        }

        private void place(GraphNode vertex, int depth, float top) {
            vertex.x = Scl.scl(CANVAS_PAD + depth * (CARD_WIDTH + CARD_X_GAP));
            vertex.y = top + (vertex.span - Scl.scl(CARD_HEIGHT)) / 2f;
            layoutWidth = Math.max(layoutWidth, vertex.x + Scl.scl(CARD_WIDTH + CANVAS_PAD));
            for (GraphNode child : vertex.children) {
                place(child, depth + 1, top);
                top += child.span + Scl.scl(CARD_Y_GAP);
            }
        }

        private Table createNodeCard(NodeGroup node) {
            Table card = new Table(Tex.button) {
                @Override
                public void draw() {
                    super.draw();
                    if (nodeUnlocked(node)) drawOnlineOutline(this);
                }
            };
            card.margin(8f);
            card.image(node.icon).size(48f).scaling(Scaling.fit).padRight(8f);
            Label name = card.add(node.title).width(152f).height(54f).wrap().left().get();
            name.setAlignment(Align.left);
            name.setFontScale(0.9f);
            card.row();
            card.add((node.expanded ? "- " : "+ ") + Core.bundle.format("nh.research.technology-count", node.entries.size))
                    .colspan(2).width(CARD_WIDTH - 16f).height(24f).color(node.category.color).left();
            card.clicked(() -> {
                selectedNode = node;
                selectedEntry = null;
                node.expanded = !node.expanded;
                Core.app.post(() -> {
                    rebuildCanvas();
                    focusGroup(node);
                });
            });
            card.update(() -> card.setColor(node == selectedNode ? node.category.color : Color.white));
            return card;
        }

        private Table createContentCard(Entry entry) {
            Table card = new Table(Tex.button) {
                @Override
                public void draw() {
                    super.draw();
                    if (entry.content.unlockedHost()) drawOnlineOutline(this);
                }
            };
            card.margin(8f);
            card.image(entry.icon).size(42f).scaling(Scaling.fit).padRight(8f);
            Label name = card.add(entry.content.localizedName).width(158f).height(54f).wrap().left().get();
            name.setAlignment(Align.left);
            name.setFontScale(0.9f);
            card.row();
            card.label(() -> Core.bundle.format("nh.research.card-status",
                    entry.content.unlockedHost() ? "[green]" + Core.bundle.get("nh.research.online")
                            : canResearch(entry) ? "[accent]" + Core.bundle.get("nh.research.research")
                            : "[gray]" + Core.bundle.get("nh.research.locked")))
                    .colspan(2).width(CARD_WIDTH - 16f).height(24f).left();
            card.clicked(() -> {
                selectedEntry = entry;
                selectedNode = entry.group;
                rebuildDetail();
            });
            card.update(() -> card.setColor(entry == selectedEntry ? entry.category.color : Color.white));
            return card;
        }

        private void drawOnlineOutline(Table card) {
            float x = card.x;
            float y = card.y;
            float width = card.getWidth();
            float height = card.getHeight();
            float corner = Scl.scl(9f);
            Draw.color(Pal.accent);
            Lines.stroke(Scl.scl(3f));
            Lines.line(x + corner, y, x + width - corner, y);
            Lines.line(x + width - corner, y, x + width, y + corner);
            Lines.line(x + width, y + corner, x + width, y + height - corner);
            Lines.line(x + width, y + height - corner, x + width - corner, y + height);
            Lines.line(x + width - corner, y + height, x + corner, y + height);
            Lines.line(x + corner, y + height, x, y + height - corner);
            Lines.line(x, y + height - corner, x, y + corner);
            Lines.line(x, y + corner, x + corner, y);
            Draw.reset();
        }

        void focusGroup(NodeGroup group) {
            GraphNode vertex = groups.get(group);
            if (vertex != null) focus(vertex);
        }

        void focusFirst() {
            if (!roots.isEmpty()) focus(roots.first());
        }

        void focusEntry(Entry entry) {
            GraphNode vertex = contents.get(entry);
            if (vertex != null) focus(vertex);
        }

        private void focus(GraphNode vertex) {
            if (treePane == null) return;
            treePane.validate();
            treePane.scrollTo(vertex.x * zoom, vertex.y * zoom,
                    Scl.scl(CARD_WIDTH) * zoom, Scl.scl(CARD_HEIGHT) * zoom, true, true);
            treePane.updateVisualScroll();
        }

        void setZoom(float value) {
            if (treePane == null) {
                zoom = Math.max(0.3f, Math.min(2f, value));
                graph.setScale(zoom);
                return;
            }
            float percentX = treePane.getScrollPercentX();
            float percentY = treePane.getScrollPercentY();
            zoom = Math.max(0.3f, Math.min(2f, value));
            graph.setScale(zoom);
            invalidateHierarchy();
            treePane.validate();
            treePane.setScrollPercentX(percentX);
            treePane.setScrollPercentY(percentY);
            treePane.updateVisualScroll();
        }

        @Override
        public float getPrefWidth() {
            return layoutWidth * zoom;
        }

        @Override
        public float getPrefHeight() {
            return layoutHeight * zoom;
        }

        private void drawConnections() {
            for (GraphNode child : vertices) {
                if (child.parent == null) continue;
                GraphNode parent = child.parent;
                boolean unlocked = child.entry == null ? nodeUnlocked(child.group) : child.entry.content.unlockedHost();
                boolean available = child.entry != null && canResearch(child.entry);
                Color color = unlocked ? Pal.accent : available ? child.group.category.color : Pal.gray;
                float startX = parent.x + Scl.scl(CARD_WIDTH);
                float startY = parent.y + Scl.scl(CARD_HEIGHT / 2f);
                float endX = child.x;
                float endY = child.y + Scl.scl(CARD_HEIGHT / 2f);
                float elbowX = startX + Scl.scl(CARD_X_GAP / 2f);
                Draw.color(Pal.darkestGray);
                Lines.stroke(Scl.scl(5f));
                branch(startX, startY, elbowX, endX, endY);
                Draw.color(color);
                Lines.stroke(Scl.scl(3f));
                branch(startX, startY, elbowX, endX, endY);
            }
            Draw.reset();
        }

        private void branch(float startX, float startY, float elbowX, float endX, float endY) {
            Lines.line(startX, startY, elbowX, startY);
            Lines.line(elbowX, startY, elbowX, endY);
            Lines.line(elbowX, endY, endX, endY);
        }
    }

    private static final class GraphNode {
        final NodeGroup group;
        final Entry entry;
        final Table card;
        final Seq<GraphNode> children = new Seq<>();
        GraphNode parent;
        float x;
        float y;
        float span;

        GraphNode(NodeGroup group, Entry entry, Table card) {
            this.group = group;
            this.entry = entry;
            this.card = card;
        }
    }

    private static final class Category {
        final String key;
        final String title;
        final Drawable icon;
        final Color color;
        final Seq<Entry> entries = new Seq<>();
        final Seq<Entry> roots = new Seq<>();
        final Seq<NodeGroup> nodes = new Seq<>();

        Category(String key, String title, Drawable icon, Color color) {
            this.key = key;
            this.title = title;
            this.icon = icon;
            this.color = color;
        }
    }

    private static final class NodeGroup {
        final String asset;
        final String title;
        final String description;
        final Category category;
        final Drawable icon;
        final Seq<Entry> entries = new Seq<>();
        final Seq<NodeGroup> parentOptions = new Seq<>();
        boolean expanded = true;
        NodeGroup parent;
        int minDepth;

        NodeGroup(String asset, String title, Category category, Drawable icon) {
            this.asset = asset;
            this.title = title;
            this.description = Core.bundle.format("nh.research.branch-description", title);
            this.category = category;
            this.icon = icon;
        }
    }

    private static final class Entry {
        final UnlockableContent content;
        final TechNode node;
        final Category category;
        final Drawable icon;
        final int threat;
        final Seq<Entry> children = new Seq<>();
        final Seq<UnlockableContent> prerequisites = new Seq<>();
        Entry parent;
        NodeGroup group;

        Entry(UnlockableContent content, TechNode node, Category category, int threat) {
            this.content = content;
            this.node = node;
            this.category = category;
            this.threat = threat;
            this.icon = contentIcon(content);
        }
    }
}
