package newhorizon.expand.recipe;

import arc.Core;
import arc.graphics.Color;
import arc.util.Nullable;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;

public class Recipe {

    public Recipe(String name){
        this.name = name;

        recipeName = Core.bundle.get("recipe." + name + ".name");
        recipeDescription = Core.bundle.get("recipe." + name + ".desc");
        recipeDetail = Core.bundle.get("recipe." + name + ".deta");
    }

    public String name;
    public String[] tags;

    public @Nullable ItemStack[] inputItems;
    public @Nullable ItemStack[] outputItems;
    public @Nullable LiquidStack[] inputLiquids;
    public @Nullable LiquidStack[] outputLiquids;
    public float xenAmount = 0f;
    public float xenThreshold = 300f;
    public @Nullable PayloadStack[] inputPayloads;
    public @Nullable PayloadStack[] outputPayloads;
    public @Nullable float inputPower;
    public @Nullable float outputPower;
    public float craftTime = 60f;

    public int[] liquidOutputDirections = {-1};
    public boolean dumpExtraLiquid = true;
    public boolean ignoreLiquidFullness = false;

    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public @Nullable Color TintColor;

    public @Nullable String recipeName;
    public @Nullable String recipeDescription;
    public @Nullable String recipeDetail;

    public boolean hasXen(){
        return xenAmount != 0;
    }
}