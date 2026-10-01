package newhorizon.util.ui.dialog;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
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
import mindustry.world.blocks.production.GenericCrafter;
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
            button.add("TECH TREE").growX();
        }, this::openRootSelector).width(260f).height(52f).pad(3f);
    }

    private void openRootSelector() {
        new BaseDialog("TECH TREE SELECTOR") {{
            cont.pane(list -> {
                list.left();
                list.defaults().growX().height(62f).pad(3f);
                for (TechNode root : TechTree.roots) {
                    if (root.requiresUnlock && !root.content.unlockedHost() && root != NHTechTree.root) continue;
                    String label = root == NHTechTree.root ? "NEW HORIZON" : root.localizedName();
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
                            vanilla.switchTree(root);
                            vanilla.show();
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
                title.add("[accent]NEW HORIZON // RESEARCH NETWORK[]").left().row();
                title.add("[lightgray]Midantha technology architecture").left();
            }).growX();
            header.table(status -> {
                status.right();
                status.label(() -> "[accent]" + unlockedCount() + "[] / " + entries.size + " ONLINE").right().row();
                status.label(() -> researchItems == null ? "[gray]RESOURCE LINK OFFLINE" : "[lightgray]RESOURCE LINK ACTIVE").right();
            }).right();
            header.button("TECH TREE", Styles.cleart, this::openRootSelector).size(150f, 46f);
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
                labels.add("[gray]" + category.entries.size + " technologies").left();
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
            body.add(treePane).minSize(0f).grow();

            detailTable = new Table(Tex.button);
            detailTable.margin(10f);
            ScrollPane detailPane = new ScrollPane(detailTable, Styles.smallPane);
            detailPane.setScrollingDisabled(true, false);
            body.add(detailPane).width(320f).growY().padLeft(8f);
        }).grow().row();

        cont.table(footer -> {
            footer.left().defaults().pad(3f);
            footer.add("[gray]Drag: pan / Click node: expand / Select content: research").wrap().growX();
            footer.add().growX();
            footer.button("+", Styles.cleart, () -> canvas.setZoom(canvas.zoom + 0.1f)).size(42f, 38f);
            footer.button("100%", Styles.cleart, () -> canvas.setZoom(1f)).size(60f, 38f);
            footer.button("-", Styles.cleart, () -> canvas.setZoom(canvas.zoom - 0.1f)).size(42f, 38f);
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
            if (node == NHTechTree.root || node.content == null || node.content.minfo.mod != NewHorizon.MOD) return;
            if (!nativeNodes.containsKey(node.content)) nativeNodes.put(node.content, node);
        });

        ObjectMap<String, Category> byName = new ObjectMap<>();
        addCategory(byName, "MATERIALS", "process-multiple-steel-node", Color.valueOf("8dd6ff"));
        addCategory(byName, "FLUID SYSTEMS", "liquid-logistics-1", Color.valueOf("65c9d8"));
        addCategory(byName, "MINING", "mine-tungsten-node", Color.valueOf("b9d477"));
        addCategory(byName, "LOGISTICS", "logistics-1", Color.valueOf("f6c96d"));
        addCategory(byName, "POWER", "power-production-node", Color.valueOf("ffe46b"));
        addCategory(byName, "FABRICATION", "process-presstanium-node", Color.valueOf("e7a2ff"));
        addCategory(byName, "COMBAT", "quality-rare", Color.valueOf("ff8d8d"));
        addCategory(byName, "ANCIENT SYSTEMS", "quality-legendary", Color.valueOf("ffcf7a"));

        addNode(byName, "MATERIALS", "process-processor-junior-node", "JUNIOR PROCESSOR");
        addNode(byName, "MATERIALS", "process-processor-senior-node", "SENIOR PROCESSOR");
        addNode(byName, "MATERIALS", "process-processor-hyper-node", "HYPER PROCESSOR");
        addNode(byName, "MATERIALS", "process-presstanium-node", "PRESSTANIUM");
        addNode(byName, "MATERIALS", "process-multiple-steel-node", "MULTIPLE STEEL");
        addNode(byName, "MATERIALS", "process-seton-alloy-node", "SETON ALLOY");
        addNode(byName, "MATERIALS", "process-nodex-plate-node", "NODEX PLATE");
        addNode(byName, "MATERIALS", "process-hadronicomp-node", "HADRONICOMP");
        addNode(byName, "MATERIALS", "process-dark-energy-node", "DARK ENERGY");
        addNode(byName, "MATERIALS", "process-irayrond-panel-node", "IRAYROND PANEL");
        addNode(byName, "MATERIALS", "process-ancimembrane-node", "ANCIMEMBRANE");
        addNode(byName, "MATERIALS", "process-metal-oxhydrigen-node", "METAL OXYHYDRIGEN");
        addNode(byName, "MATERIALS", "process-phase-fabric-node", "PHASE FABRIC");
        addNode(byName, "MATERIALS", "process-surge-alloy-node", "SURGE ALLOY");
        addNode(byName, "MATERIALS", "process-thermo-core-positive-node", "POSITIVE THERMO CORE");
        addNode(byName, "MATERIALS", "process-thermo-core-negative-node", "NEGATIVE THERMO CORE");
        addNode(byName, "MATERIALS", "process-fusion-core-energy-node", "FUSION CORE ENERGY");
        addNode(byName, "MATERIALS", "process-zeta-node", "ZETA");
        addNode(byName, "MATERIALS", "basic-materials-node", "BASIC MATERIALS", "mine-silicon-node");

        addNode(byName, "FLUID SYSTEMS", "liquid-logistics-1", "LIQUID LOGISTICS I");
        addNode(byName, "FLUID SYSTEMS", "liquid-logistics-2", "LIQUID LOGISTICS II");
        addNode(byName, "FLUID SYSTEMS", "liquid-logistics-3", "LIQUID LOGISTICS III");
        addNode(byName, "FLUID SYSTEMS", "liquid-logistics-extend-1", "LIQUID EXTENSION I");
        addNode(byName, "FLUID SYSTEMS", "liquid-logistics-extend-2", "LIQUID EXTENSION II");
        addNode(byName, "FLUID SYSTEMS", "liquid-logistics-extend-3", "LIQUID EXTENSION III");
        addNode(byName, "FLUID SYSTEMS", "process-irdryon-fluid-node", "IRDRYON FLUID");

        addNode(byName, "MINING", "mine-silicon-node", "SILICON MINING");
        addNode(byName, "MINING", "mine-titanium-node", "TITANIUM MINING");
        addNode(byName, "MINING", "mine-tungsten-node", "TUNGSTEN MINING");
        addNode(byName, "MINING", "mine-zeta-node", "ZETA MINING");
        addNode(byName, "MINING", "mine-quantum-liquid-node", "QUANTUM LIQUID");
        addNode(byName, "MINING", "mine-xen-fluid-node", "XEN FLUID");

        addNode(byName, "LOGISTICS", "logistics-1", "LOGISTICS I");
        addNode(byName, "LOGISTICS", "logistics-2", "LOGISTICS II");
        addNode(byName, "LOGISTICS", "logistics-3", "LOGISTICS III");
        addNode(byName, "LOGISTICS", "logistics-extend-1", "LOGISTICS EXTENSION I");
        addNode(byName, "LOGISTICS", "logistics-extend-2", "LOGISTICS EXTENSION II");
        addNode(byName, "LOGISTICS", "logistics-extend-3", "LOGISTICS EXTENSION III");

        addNode(byName, "POWER", "power-production-node", "POWER PRODUCTION");
        addNode(byName, "FABRICATION", "fabrication-process-node", "FABRICATION", "process-presstanium-node");
        addNode(byName, "COMBAT", "quality-basic", "BASIC COMBAT");
        addNode(byName, "COMBAT", "quality-uncommon", "UNCOMMON COMBAT");
        addNode(byName, "COMBAT", "quality-rare", "RARE COMBAT");
        addNode(byName, "COMBAT", "quality-epic", "EPIC COMBAT");
        addNode(byName, "ANCIENT SYSTEMS", "quality-legendary", "LEGENDARY SYSTEMS");

        nativeNodes.each((content, node) -> {
            Category category = byName.get(categoryName(content));
            Entry entry = new Entry(content, node, category);
            entries.put(content, entry);
            category.entries.add(entry);
            String nodeAsset = nodeAssetFor(content, category, node.depth);
            NodeGroup group = nodeAsset == null ? null : nodeGroups.get(nodeAsset);
            if (group == null || group.category != category) group = category.nodes.first();
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
            if (entry.group == null || entry.node.parent == null || entry.node.parent.content == null) return;
            Entry parentEntry = entries.get(entry.node.parent.content);
            if (parentEntry == null || parentEntry.group == null || parentEntry.group == entry.group) return;
            NodeGroup parent = parentEntry.group;
            if (parent.category != entry.group.category) return;
            if (!entry.group.parentOptions.contains(parent)) entry.group.parentOptions.add(parent);
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

    private void addNode(ObjectMap<String, Category> byName, String categoryName, String asset, String title) {
        addNode(byName, categoryName, asset, title, asset);
    }

    private void addNode(ObjectMap<String, Category> byName, String categoryName, String asset, String title, String iconAsset) {
        Category category = byName.get(categoryName);
        if (category == null || nodeGroups.containsKey(asset)) return;
        NodeGroup group = new NodeGroup(asset, title, category, nodeIcon(iconAsset, null));
        nodeGroups.put(asset, group);
        category.nodes.add(group);
    }
    private void addCategory(ObjectMap<String, Category> byName, String title, String icon, Color color) {
        Category category = new Category(title, nodeIcon(icon, null), color);
        categories.add(category);
        byName.put(title, category);
    }

    private String categoryName(UnlockableContent content) {
        if (content instanceof Item) return "MATERIALS";
        if (content instanceof mindustry.type.Liquid) return "FLUID SYSTEMS";
        if (content instanceof mindustry.type.UnitType) return "COMBAT";
        if (content instanceof GenericCrafter) return "FABRICATION";
        if (content instanceof Block block) {
            return switch (block.category) {
                case turret, defense, units -> "COMBAT";
                case distribution -> "LOGISTICS";
                case liquid -> "FLUID SYSTEMS";
                case power -> "POWER";
                case production -> "MINING";
                case crafting -> "FABRICATION";
                default -> "ANCIENT SYSTEMS";
            };
        }
        return "ANCIENT SYSTEMS";
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
            detailTable.add("[accent]SELECT A TECHNOLOGY NODE[]").width(286f).wrap().left().row();
            detailTable.add("Each node uses a dedicated New Horizon node graphic. Select it to inspect its description and the contents attached behind it.")
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
            req.add("RELATED RESEARCH BRANCHES").color(Pal.accent).left().row();
            if (node.parentOptions.size == 0) {
                req.add("ROOT ACCESS").color(Pal.heal).left().row();
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

        detailTable.add("[accent]" + node.entries.size + "[] associated technologies").left().padTop(10f).row();
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
        detailTable.button("BACK TO NODE", Styles.cleart, () -> {
            selectedEntry = null;
            rebuildDetail();
        }).growX().height(38f).left().row();
        detailTable.image(entry.icon).size(74f).left().row();
        Label title = detailTable.add(entry.content.localizedName).width(286f).wrap().left().get();
        title.setFontScale(1.1f);
        detailTable.row();
        detailTable.add(entry.category.title).color(entry.category.color).left().row();
        detailTable.image(Tex.whiteui, entry.category.color).height(2f).growX().padTop(6f).padBottom(7f).row();
        detailTable.add(entry.content.displayDescription()).color(Color.lightGray).wrap().width(286f).left().row();

        detailTable.table(req -> {
            req.left().defaults().padTop(8f);
            req.add("RESEARCH ROUTE").color(Pal.accent).left().row();
            if (entry.prerequisites.size == 0) {
                req.add("NODE ACCESS").color(Pal.heal).left().row();
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
                    if (i + 1 < entry.prerequisites.size) req.add("OR").color(Pal.accent).left().padLeft(22f).row();
                }
            }
        }).growX().left().row();

        if (entry.node.requirements.length > 0) {
            detailTable.table(req -> {
                req.left().defaults().padTop(4f);
                req.add("RESOURCE INVESTMENT").color(Pal.accent).left().row();
                for (int i = 0; i < entry.node.requirements.length; i++) {
                    ItemStack stack = entry.node.requirements[i];
                    int current = entry.node.finishedRequirements[i].amount;
                    int available = researchItems == null ? 0 : researchItems.get(stack.item);
                    req.add(stack.item.localizedName + "  " + current + " / " + stack.amount + "  [gray](" + available + ")")
                            .color(current >= stack.amount ? Pal.heal : available > 0 ? Color.lightGray : Pal.remove).left().row();
                }
            }).growX().left().row();
        } else {
            detailTable.add("AUTO-SYNCHRONIZED CONTENT").color(Pal.heal).left().padTop(10f).row();
        }

        if (entry.content.unlockedHost()) {
            detailTable.add("ONLINE").color(Pal.heal).left().padTop(12f).row();
        } else {
            TextButton research = new TextButton("RESEARCH", Styles.togglet);
            research.clicked(() -> research(entry));
            research.update(() -> research.setDisabled(!canResearch(entry)));
            detailTable.add(research).growX().height(48f).padTop(12f).row();
            Label access = detailTable.label(() -> canResearch(entry) ? "ACCESS GRANTED" : "WAITING FOR PREREQUISITES / RESOURCES").left().get();
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

    private static String nodeAssetFor(UnlockableContent content, Category category, int depth) {
        String name = content.name.toLowerCase();
        if (category.title.equals("FABRICATION")) return "fabrication-process-node";
        if (category.title.equals("FLUID SYSTEMS")) {
            if (name.contains("irdryon")) return "process-irdryon-fluid-node";
            int tier = Math.max(1, Math.min(3, depth - 1));
            return "liquid-logistics-" + tier;
        }
        if (category.title.equals("MINING")) {
            if (name.contains("quantum")) return "mine-quantum-liquid-node";
            if (name.contains("xen")) return "mine-xen-fluid-node";
            if (name.contains("zeta")) return "mine-zeta-node";
            if (name.contains("silicon")) return "mine-silicon-node";
            if (name.contains("titanium")) return "mine-titanium-node";
            return "mine-tungsten-node";
        }
        if (category.title.equals("LOGISTICS")) {
            int tier = Math.max(1, Math.min(3, depth - 1));
            return "logistics-" + tier;
        }
        if (category.title.equals("POWER")) return "power-production-node";
        if (category.title.equals("ANCIENT SYSTEMS")) return "quality-legendary";
        if (category.title.equals("COMBAT")) {
            int tier = Math.max(1, Math.min(4, depth - 1));
            return switch (tier) {
                case 1 -> "quality-basic";
                case 2 -> "quality-uncommon";
                case 3 -> "quality-rare";
                default -> "quality-epic";
            };
        }
        if (name.contains("processor-junior")) return "process-processor-junior-node";
        if (name.contains("processor-senior")) return "process-processor-senior-node";
        if (name.contains("processor-hyper")) return "process-processor-hyper-node";
        if (name.contains("presstanium")) return "process-presstanium-node";
        if (name.contains("multiple-steel")) return "process-multiple-steel-node";
        if (name.contains("seton-alloy")) return "process-seton-alloy-node";
        if (name.contains("nodex-plate")) return "process-nodex-plate-node";
        if (name.contains("hadronicomp")) return "process-hadronicomp-node";
        if (name.contains("dark-energy")) return "process-dark-energy-node";
        if (name.contains("irayrond")) return "process-irayrond-panel-node";
        if (name.contains("ancimembrane")) return "process-ancimembrane-node";
        if (name.contains("metal-oxhydrigen")) return "process-metal-oxhydrigen-node";
        if (name.contains("phase-fabric")) return "process-phase-fabric-node";
        if (name.contains("surge-alloy")) return "process-surge-alloy-node";
        if (name.contains("thermo-core-positive")) return "process-thermo-core-positive-node";
        if (name.contains("thermo-core-negative")) return "process-thermo-core-negative-node";
        if (name.contains("fusion-energy") || name.contains("fusion-core")) return "process-fusion-core-energy-node";
        if (name.contains("zeta")) return "process-zeta-node";
        return "basic-materials-node";
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
        private float zoom = 1f;

        TechCanvas() {
            graph.setTransform(true);
            graph.cullable = false;
            addChild(graph);
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
            Table card = new Table(Tex.button);
            card.margin(8f);
            card.image(node.icon).size(48f).scaling(Scaling.fit).padRight(8f);
            Label name = card.add(node.title).width(152f).height(54f).wrap().left().get();
            name.setAlignment(Align.left);
            name.setFontScale(0.9f);
            card.row();
            card.add((node.expanded ? "- " : "+ ") + node.entries.size + " technologies")
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
            Table card = new Table(Tex.button);
            card.margin(8f);
            card.image(entry.icon).size(42f).scaling(Scaling.fit).padRight(8f);
            Label name = card.add(entry.content.localizedName).width(158f).height(54f).wrap().left().get();
            name.setAlignment(Align.left);
            name.setFontScale(0.9f);
            card.row();
            card.label(() -> entry.content.unlockedHost() ? "[green]ONLINE" : canResearch(entry) ? "[accent]RESEARCH" : "[gray]LOCKED")
                    .colspan(2).width(CARD_WIDTH - 16f).height(24f).left();
            card.clicked(() -> {
                selectedEntry = entry;
                selectedNode = entry.group;
                rebuildDetail();
            });
            card.update(() -> card.setColor(entry == selectedEntry ? entry.category.color : Color.white));
            return card;
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
                Lines.stroke(Scl.scl(2f));
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
        final String title;
        final Drawable icon;
        final Color color;
        final Seq<Entry> entries = new Seq<>();
        final Seq<Entry> roots = new Seq<>();
        final Seq<NodeGroup> nodes = new Seq<>();

        Category(String title, Drawable icon, Color color) {
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
            this.description = "Research branch: " + title.toLowerCase() + ". Expand to view its technologies. Connections follow the native research tree; select a technology to inspect its prerequisites and resource cost.";
            this.category = category;
            this.icon = icon;
        }
    }

    private static final class Entry {
        final UnlockableContent content;
        final TechNode node;
        final Category category;
        final Drawable icon;
        final Seq<Entry> children = new Seq<>();
        final Seq<UnlockableContent> prerequisites = new Seq<>();
        Entry parent;
        NodeGroup group;

        Entry(UnlockableContent content, TechNode node, Category category) {
            this.content = content;
            this.node = node;
            this.category = category;
            this.icon = contentIcon(content);
        }
    }
}

