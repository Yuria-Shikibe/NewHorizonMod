package newhorizon.expand.logic;

import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import newhorizon.content.NHTechTree;
import newhorizon.expand.block.special.JumpGate;

public final class UnitProductionRecipe {
    private static final UnitProductionRecipe invalid = new UnitProductionRecipe(0f, 0f);
    private static final ObjectMap<UnitType, UnitProductionRecipe> registered = new ObjectMap<>();
    private static final ObjectMap<UnitType, UnitProductionRecipe> cache = new ObjectMap<>();
    private static boolean indexed;

    public final float contribution;
    public final float buildTime;

    private UnitProductionRecipe(float contribution, float buildTime) {
        this.contribution = contribution;
        this.buildTime = buildTime;
    }

    public static void clear() {
        registered.clear();
        cache.clear();
        indexed = false;
    }

    public static UnitProductionRecipe get(UnitType type) {
        if (type == null || Vars.content == null) return invalid;
        UnitProductionRecipe result = cache.get(type);
        if (result != null) return result;
        index();
        result = registered.get(type);
        if (result == null) {
            float[] time = new float[1];
            type.getRequirements(null, time);
            result = create(type.getTotalRequirements(), time[0]);
        }
        ItemStack[] explicit = NHTechTree.unitBuildCost.get(type);
        if (explicit != null && explicit.length > 0) result = create(explicit, result.buildTime);
        cache.put(type, result);
        return result;
    }

    private static void index() {
        if (indexed) return;
        indexed = true;
        for (Block block : Vars.content.blocks()) {
            if (!(block instanceof JumpGate gate)) continue;
            for (JumpGate.UnitRecipe recipe : gate.recipeList) {
                if (recipe.unitType == null || recipe.recipe == null) continue;
                UnitProductionRecipe candidate = create(recipe.baseRequirements(), recipe.craftTime);
                if (candidate == invalid) continue;
                UnitProductionRecipe previous = registered.get(recipe.unitType);
                if (previous == null || candidate.contribution < previous.contribution
                        || candidate.contribution == previous.contribution && candidate.buildTime < previous.buildTime) {
                    registered.put(recipe.unitType, candidate);
                }
            }
        }
    }

    private static UnitProductionRecipe create(ItemStack[] requirements, float buildTime) {
        if (requirements == null || requirements.length == 0 || !Float.isFinite(buildTime) || buildTime <= 0f) return invalid;
        float contribution = 0f;
        for (ItemStack stack : requirements) {
            if (stack == null || stack.item == null || stack.amount <= 0) return invalid;
            contribution += stack.amount * ThreatLevel.getItemWeight(stack.item);
        }
        return Float.isFinite(contribution) && contribution > 0f
                ? new UnitProductionRecipe(contribution, buildTime) : invalid;
    }
}
