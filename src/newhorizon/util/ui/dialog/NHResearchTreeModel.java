package newhorizon.util.ui.dialog;

import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.content.TechTree.TechNode;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.consumers.ConsumeLiquids;
import newhorizon.content.NHTechTree;
import newhorizon.expand.block.production.factory.MultiBlockCrafter;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.expand.logic.ThreatLevel;

public final class NHResearchTreeModel {
    public static final String MATERIALS = "MATERIALS";
    public static final String FLUIDS = "FLUID SYSTEMS";
    public static final String MINING = "MINING";
    public static final String LOGISTICS = "LOGISTICS";
    public static final String POWER = "POWER";
    public static final String FABRICATION = "FABRICATION";
    public static final String COMBAT = "COMBAT";
    public static final String SYSTEMS = "ANCIENT SYSTEMS";

    private NHResearchTreeModel() {
    }

    public static Model build(TechNode root) {
        Model model = new Model();
        for (NHTechTree.ProductionNode node : NHTechTree.itemProductionTree) {
            visitProduction(model, node, MATERIALS, "materials", "MATERIALS", 0);
        }
        for (NHTechTree.ProductionNode node : NHTechTree.liquidProductionTree) {
            visitProduction(model, node, FLUIDS, "liquids", "LIQUID SYSTEMS", 0);
        }
        for (NHTechTree.ProductionNode node : NHTechTree.blockTechTree) visitBlock(model, node, null);
        if (root != null) {
            root.each(node -> {
                if (node != root && node.content != null && !model.assignments.containsKey(node.content)) {
                    model.assign(node.content, fallback(node.content));
                }
            });
        }
        return model;
    }

    private static void visitProduction(Model model, NHTechTree.ProductionNode node, String category,
                                        String groupKey, String groupTitle, int depth) {
        if (node == null || node.content == null) return;
        int threat = node.content instanceof Item ? itemThreat((Item) node.content)
                : node.content instanceof Liquid ? liquidThreat((Liquid) node.content) : 0;
        model.addGroup(new Group(groupKey, category, groupTitle, null));
        model.assign(node.content, new Assignment(category, groupKey, threat));
        for (NHTechTree.ProductionNode child : node.children) {
            visitProduction(model, child, category, groupKey, groupTitle, depth + 1);
        }
    }

    private static void visitBlock(Model model, NHTechTree.ProductionNode node, Block parent) {
        if (node == null || node.content == null) return;
        Block current = node.content instanceof Block ? (Block) node.content : parent;
        Assignment assignment = classify(node.content, current);
        model.addGroup(groupFor(node.content, current, assignment));
        model.assign(node.content, assignment);
        for (NHTechTree.ProductionNode child : node.children) visitBlock(model, child, current);
    }

    private static Assignment classify(UnlockableContent content, Block parent) {
        if (content instanceof UnitType) {
            return new Assignment(COMBAT, unitGroup(parent), unitThreat(parent, (UnitType) content));
        }
        if (!(content instanceof Block)) return fallback(content);
        Block block = (Block) content;
        String category;
        String group;
        switch (block.category) {
            case turret, defense, units -> {
                category = COMBAT;
                group = block.category == mindustry.type.Category.turret ? "combat-turrets"
                        : block.category == mindustry.type.Category.units ? "combat-unit-production" : "combat-defense";
            }
            case distribution -> {
                category = LOGISTICS;
                group = logisticsGroup(block);
            }
            case power -> { category = POWER; group = "power"; }
            case liquid -> {
                category = FLUIDS;
                group = liquidLogisticsGroup(block);
            }
            case crafting, production -> {
                if (isMiningBlock(block)) {
                    category = MINING;
                    group = "mining";
                } else {
                    UnlockableContent output = firstOutput(block);
                    if (output instanceof Liquid) {
                        category = FLUIDS;
                        group = "fluid-factory-" + output.name;
                    } else if (output != null) {
                        category = FABRICATION;
                        group = "factory-" + output.name;
                    } else {
                        category = FABRICATION;
                        group = "fabrication";
                    }
                }
            }
            default -> { category = SYSTEMS; group = "systems"; }
        }
        return new Assignment(category, group, blockThreat(block));
    }

    private static Group groupFor(UnlockableContent content, Block parent, Assignment assignment) {
        String key = assignment.groupKey;
        String title;
        String asset = null;
        if (key.equals("materials")) title = "MATERIALS";
        else if (key.equals("liquids")) title = "LIQUIDS";
        else if (key.equals("mining")) { title = "MINING"; asset = "mine-tungsten-node"; }
        else if (key.startsWith("logistics-")) {
            title = logisticsTitle(key);
            asset = key;
        }
        else if (key.equals("power")) { title = "POWER NETWORK"; asset = "power-production-node"; }
        else if (key.equals("fabrication")) title = "FABRICATION";
        else if (key.equals("combat-turrets")) { title = "TURRETS"; }
        else if (key.equals("combat-defense")) { title = "DEFENSE"; }
        else if (key.equals("combat-unit-production")) { title = "UNIT PRODUCTION"; }
        else if (key.equals("systems")) { title = "ANCIENT SYSTEMS"; }
        else if (key.startsWith("liquid-logistics-")) {
            title = liquidLogisticsTitle(key);
            asset = key;
        }
        else if (key.startsWith("unit-gate-")) {
            title = key.substring("unit-gate-".length()).replace('-', ' ').toUpperCase() + " GATE";
            asset = "unit-branch";
        } else if (key.startsWith("fluid-factory-")) {
            title = "LIQUID: " + key.substring("fluid-factory-".length()).replace('-', ' ').toUpperCase();
            asset = content instanceof Block ? "liquid-logistics-" + resourceTier((Block) content) : "liquid-logistics-2";
        } else if (key.startsWith("factory-")) {
            title = "FACTORY: " + key.substring("factory-".length()).replace('-', ' ').toUpperCase();
            asset = processAsset(key.substring("factory-".length()));
        } else {
            title = content == null ? key.toUpperCase() : content.name.replace('-', ' ').toUpperCase();
        }
        return new Group(key, assignment.category, title, asset);
    }

    private static String logisticsGroup(Block block) {
        String prefix = block.name.contains("extend") ? "logistics-extend-" : "logistics-";
        return prefix + resourceTier(block);
    }

    private static String liquidLogisticsGroup(Block block) {
        String prefix = block.name.contains("extend") ? "liquid-logistics-extend-" : "liquid-logistics-";
        return prefix + resourceTier(block);
    }

    private static String logisticsTitle(String key) {
        if (key.startsWith("logistics-extend-")) {
            return "LOGISTICS EXTENSION " + key.substring("logistics-extend-".length());
        }
        return "LOGISTICS " + key.substring("logistics-".length());
    }

    private static String liquidLogisticsTitle(String key) {
        if (key.startsWith("liquid-logistics-extend-")) {
            return "LIQUID LOGISTICS EXTENSION " + key.substring("liquid-logistics-extend-".length());
        }
        return "LIQUID LOGISTICS " + key.substring("liquid-logistics-".length());
    }
    private static String processAsset(String name) {
        return switch (name) {
            case "processor-junior" -> "process-processor-junior-node";
            case "processor-senior" -> "process-processor-senior-node";
            case "processor-hyper" -> "process-processor-hyper-node";
            case "presstanium" -> "process-presstanium-node";
            case "multiple-steel" -> "process-multiple-steel-node";
            case "seton-alloy" -> "process-seton-alloy-node";
            case "nodex-plate" -> "process-nodex-plate-node";
            case "hadronicomp" -> "process-hadronicomp-node";
            case "dark-energy" -> "process-dark-energy-node";
            case "irayrond-panel" -> "process-irayrond-panel-node";
            case "ancimembrane" -> "process-ancimembrane-node";
            case "metal-oxhydrigen" -> "process-metal-oxhydrigen-node";
            case "phase-fabric" -> "process-phase-fabric-node";
            case "surge-alloy" -> "process-surge-alloy-node";
            case "thermo-core-positive" -> "process-thermo-core-positive-node";
            case "thermo-core-negative" -> "process-thermo-core-negative-node";
            case "fusion-energy" -> "process-fusion-core-energy-node";
            case "zeta" -> "process-zeta-node";
            default -> null;
        };
    }

    private static Assignment fallback(UnlockableContent content) {
        if (content instanceof Item) return new Assignment(MATERIALS, "materials", itemThreat((Item) content));
        if (content instanceof Liquid) return new Assignment(FLUIDS, "liquids", liquidThreat((Liquid) content));
        if (content instanceof UnitType) return new Assignment(COMBAT, "combat-unit-production", 0);
        if (content instanceof Block) return new Assignment(SYSTEMS, "systems", blockThreat((Block) content));
        return new Assignment(SYSTEMS, "systems", 0);
    }

    private static String unitGroup(Block parent) {
        if (parent == null) return "combat-unit-production";
        String name = parent.name;
        if (name.contains("basic")) return "unit-gate-basic";
        if (name.contains("primary")) return "unit-gate-primary";
        if (name.contains("standard")) return "unit-gate-standard";
        if (name.contains("hyper")) return "unit-gate-hyper";
        return "combat-unit-production";
    }

    private static boolean isMiningBlock(Block block) {
        String type = block.getClass().getName();
        String name = block.name;
        return type.contains("Drill") || type.contains("OreCollector") || type.contains("Radiator")
                || name.contains("drill") || name.contains("collector") || name.contains("extractor")
                || name.contains("mining") || name.contains("radiator");
    }

    private static UnlockableContent firstOutput(Block block) {
        if (block instanceof RecipeGenericCrafter) {
            RecipeGenericCrafter crafter = (RecipeGenericCrafter) block;
            for (newhorizon.expand.type.Recipe recipe : crafter.recipes) {
                if (!recipe.outputItem.isEmpty()) return recipe.outputItem.first().item;
                if (!recipe.outputLiquid.isEmpty()) return recipe.outputLiquid.first().liquid;
            }
        }
        if (block instanceof MultiBlockCrafter) {
            MultiBlockCrafter crafter = (MultiBlockCrafter) block;
            if (crafter.outputItems != null && crafter.outputItems.length > 0) return crafter.outputItems[0].item;
            if (crafter.outputItem != null) return crafter.outputItem.item;
            if (crafter.outputLiquids != null && crafter.outputLiquids.length > 0) return crafter.outputLiquids[0].liquid;
            if (crafter.outputLiquid != null) return crafter.outputLiquid.liquid;
        }
        if (block instanceof GenericCrafter) {
            GenericCrafter crafter = (GenericCrafter) block;
            if (crafter.outputItems != null && crafter.outputItems.length > 0) return crafter.outputItems[0].item;
            if (crafter.outputItem != null) return crafter.outputItem.item;
            if (crafter.outputLiquids != null && crafter.outputLiquids.length > 0) return crafter.outputLiquids[0].liquid;
            if (crafter.outputLiquid != null) return crafter.outputLiquid.liquid;
        }
        return null;
    }

    private static int resourceTier(Block block) {
        int maxDepth = 0;
        int maxThreat = 0;
        if (block.requirements != null) {
            for (ItemStack stack : block.requirements) {
                maxDepth = Math.max(maxDepth, itemDepth(stack.item, NHTechTree.itemProductionTree, 0));
                maxThreat = Math.max(maxThreat, itemThreat(stack.item));
            }
        }
        if (block.consumers != null) {
            for (Consume consume : block.consumers) {
                if (consume instanceof ConsumeItems) {
                    ItemStack[] stacks = ((ConsumeItems) consume).items;
                    if (stacks != null) for (ItemStack stack : stacks) {
                        maxDepth = Math.max(maxDepth, itemDepth(stack.item, NHTechTree.itemProductionTree, 0));
                        maxThreat = Math.max(maxThreat, itemThreat(stack.item));
                    }
                } else if (consume instanceof ConsumeLiquid) {
                    Liquid liquid = ((ConsumeLiquid) consume).liquid;
                    maxDepth = Math.max(maxDepth, liquidDepth(liquid, NHTechTree.liquidProductionTree, 0));
                    maxThreat = Math.max(maxThreat, liquidThreat(liquid));
                } else if (consume instanceof ConsumeLiquids) {
                    LiquidStack[] stacks = ((ConsumeLiquids) consume).liquids;
                    if (stacks != null) for (LiquidStack stack : stacks) {
                        maxDepth = Math.max(maxDepth, liquidDepth(stack.liquid, NHTechTree.liquidProductionTree, 0));
                        maxThreat = Math.max(maxThreat, liquidThreat(stack.liquid));
                    }
                }
            }
        }
        if (block instanceof RecipeGenericCrafter) {
            RecipeGenericCrafter crafter = (RecipeGenericCrafter) block;
            for (newhorizon.expand.type.Recipe recipe : crafter.recipes) {
                for (ItemStack stack : recipe.inputItem) {
                    maxDepth = Math.max(maxDepth, itemDepth(stack.item, NHTechTree.itemProductionTree, 0));
                    maxThreat = Math.max(maxThreat, itemThreat(stack.item));
                }
                for (LiquidStack stack : recipe.inputLiquid) {
                    maxDepth = Math.max(maxDepth, liquidDepth(stack.liquid, NHTechTree.liquidProductionTree, 0));
                    maxThreat = Math.max(maxThreat, liquidThreat(stack.liquid));
                }
            }
        }
        if (maxDepth >= 4 || maxThreat >= 9) return 3;
        if (maxDepth >= 2 || maxThreat >= 4) return 2;
        return 1;
    }
    private static int blockThreat(Block block) {
        int result = 0;
        if (block.requirements != null) {
            for (ItemStack stack : block.requirements) result = Math.max(result, itemThreat(stack.item));
        }
        if (block.consumers != null) {
            for (Consume consume : block.consumers) {
                if (consume instanceof ConsumeItems) {
                    ItemStack[] stacks = ((ConsumeItems) consume).items;
                    if (stacks != null) for (ItemStack stack : stacks) result = Math.max(result, itemThreat(stack.item));
                } else if (consume instanceof ConsumeLiquid) {
                    result = Math.max(result, liquidThreat(((ConsumeLiquid) consume).liquid));
                } else if (consume instanceof ConsumeLiquids) {
                    LiquidStack[] stacks = ((ConsumeLiquids) consume).liquids;
                    if (stacks != null) for (LiquidStack stack : stacks) result = Math.max(result, liquidThreat(stack.liquid));
                }
            }
        }
        if (block instanceof RecipeGenericCrafter) {
            RecipeGenericCrafter crafter = (RecipeGenericCrafter) block;
            for (newhorizon.expand.type.Recipe recipe : crafter.recipes) {
                for (ItemStack stack : recipe.inputItem) result = Math.max(result, itemThreat(stack.item));
                for (LiquidStack stack : recipe.inputLiquid) result = Math.max(result, liquidThreat(stack.liquid));
            }
        }
        return result;
    }

    private static int unitThreat(Block parent, UnitType unit) {
        int result = parent == null ? 0 : blockThreat(parent);
        if (parent instanceof JumpGate) {
            JumpGate gate = (JumpGate) parent;
            for (JumpGate.UnitRecipe recipe : gate.recipeList) {
                if (recipe.unitType != unit || recipe.recipe == null) continue;
                for (ItemStack stack : recipe.recipe.inputItem) result = Math.max(result, itemThreat(stack.item));
                for (LiquidStack stack : recipe.recipe.inputLiquid) result = Math.max(result, liquidThreat(stack.liquid));
            }
        }
        return result;
    }

    private static int itemThreat(Item item) {
        int result = 0;
        if (ThreatLevel.threatMap != null) {
            for (IntMap.Entry<Seq<Item>> entry : ThreatLevel.threatMap) {
                if (entry.value.contains(item)) result = Math.max(result, entry.key);
            }
        }
        int depth = itemDepth(item, NHTechTree.itemProductionTree, 0);
        return Math.max(result, depth < 0 ? 0 : depth);
    }

    private static int itemDepth(Item item, Seq<NHTechTree.ProductionNode> roots, int depth) {
        for (NHTechTree.ProductionNode node : roots) {
            if (node.content == item) return depth;
            int child = itemDepth(item, node.children, depth + 1);
            if (child >= 0) return child;
        }
        return -1;
    }

    private static int liquidThreat(Liquid liquid) {
        int depth = liquidDepth(liquid, NHTechTree.liquidProductionTree, 0);
        return depth < 0 ? 0 : depth + 1;
    }

    private static int liquidDepth(Liquid liquid, Seq<NHTechTree.ProductionNode> roots, int depth) {
        for (NHTechTree.ProductionNode node : roots) {
            if (node.content == liquid) return depth;
            int child = liquidDepth(liquid, node.children, depth + 1);
            if (child >= 0) return child;
        }
        return -1;
    }

    public static final class Model {
        public final ObjectMap<UnlockableContent, Assignment> assignments = new ObjectMap<>();
        public final Seq<Group> groups = new Seq<>();
        private final ObjectSet<String> groupKeys = new ObjectSet<>();

        private void assign(UnlockableContent content, Assignment assignment) {
            assignments.put(content, assignment);
        }

        private void addGroup(Group group) {
            if (group != null && groupKeys.add(group.key)) groups.add(group);
        }

        public Assignment assignment(UnlockableContent content) {
            return assignments.get(content);
        }
    }

    public static final class Assignment {
        public final String category;
        public final String groupKey;
        public final int threat;

        private Assignment(String category, String groupKey, int threat) {
            this.category = category;
            this.groupKey = groupKey;
            this.threat = threat;
        }
    }

    public static final class Group {
        public final String key;
        public final String category;
        public final String title;
        public final String asset;

        private Group(String key, String category, String title, String asset) {
            this.key = key;
            this.category = category;
            this.title = title;
            this.asset = asset;
        }
    }
}
