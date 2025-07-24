package newhorizon.expand.block.production.factory;

import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.*;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.type.*;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;
import newhorizon.expand.block.consumer.*;
import newhorizon.expand.type.Recipe;

import static mindustry.world.meta.StatValues.stack;
import static mindustry.world.meta.StatValues.withTooltip;

public class RecipeGenericCrafter extends AdaptCrafter {
    public Seq<Recipe> recipes = new Seq<>();

    public RecipeGenericCrafter(String name) {
        super(name);

        consume(new ConsumeRecipe(RecipeGenericCrafterBuild::getRecipe, RecipeGenericCrafterBuild::getDisplayRecipe));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.input, display());
        stats.remove(Stat.output);
    }

    public void addInput(Object...objects) {
        Recipe recipe = new Recipe(objects);
        recipes.add(recipe);
    }

    public StatValue display() {
        return table -> {
            table.row();
            table.table(cont -> {
                for (int i = 0; i < recipes.size; i++){
                    Recipe recipe = recipes.get(i);
                    int finalI = i;
                    cont.table(t -> {
                        t.left().marginLeft(12f).add("[accent][" + (finalI + 1) + "]:[]").width(48f);
                        t.table(inner -> {
                            inner.table(row -> {
                                row.left();
                                recipe.inputItem.each(stack -> row.add(display(stack.item, stack.amount, craftTime / recipe.boostScl)));
                                recipe.inputLiquid.each(stack -> row.add(StatValues.displayLiquid(stack.liquid, stack.amount * Time.toSeconds, true)));
                                recipe.inputPayload.each(stack -> row.add(display(stack.item, stack.amount, craftTime / recipe.boostScl)));
                            }).growX();
                            inner.table(row -> {
                                row.left();
                                row.image(Icon.right).size(32f).padLeft(8f).padRight(12f);
                                if (outputItems != null) {
                                    for (var stack: outputItems){
                                        row.add(display(stack.item, Mathf.round(stack.amount * recipe.craftScl), craftTime / recipe.boostScl));
                                    }
                                }
                                if (outputLiquids != null) {
                                    for (var stack: outputLiquids){
                                        row.add(display(stack.liquid, stack.amount * craftTime, craftTime / recipe.boostScl));
                                    }
                                }
                                if (outputPayloads != null) {
                                    for (var stack: outputPayloads){
                                        row.add(display(stack.item, Mathf.round(stack.amount * recipe.craftScl), craftTime / recipe.boostScl));
                                    }
                                }
                            }).growX();
                        });
                    }).fillX();
                    cont.row();
                }
            });
        };
    }

    public static Table display(UnlockableContent content, float amount, float timePeriod){
        Table table = new Table();
        Stack stack = new Stack();

        stack.add(new Table(o -> {
            o.left();
            o.add(new Image(content.uiIcon)).size(32f).scaling(Scaling.fit);
        }));

        if(amount != 0){
            stack.add(new Table(t -> {
                t.left().bottom();
                t.add(amount >= 1000 ? UI.formatAmount((int)amount) : Strings.autoFixed(amount, 2)).style(Styles.outlineLabel);
                t.pack();
            }));
        }

        withTooltip(stack, content);

        table.add(stack);
        table.add((content.localizedName + "\n") + "[lightgray]" + Strings.autoFixed(amount / (timePeriod / 60f), 2) + StatUnit.perSecond.localized()).padLeft(2).padRight(5).style(Styles.outlineLabel);
        return table;
    }

    @Override
    public void init() {
        super.init();
        recipes.each(recipe -> {
            recipe.inputItem.each(stack -> itemFilter[stack.item.id] = true);
            recipe.inputLiquid.each(stack -> liquidFilter[stack.liquid.id] = true);
            recipe.inputPayload.each(stack -> payloadFilter.add(stack.item));
        });
    }

    public class RecipeGenericCrafterBuild extends AdaptCrafterBuild {
        public int recipeIndex = -1;

        public Recipe getRecipe() {
            if (recipeIndex < 0 || recipeIndex >= recipes.size) return null;
            return recipes.get(recipeIndex);
        }

        public Recipe getDisplayRecipe() {
            if (recipeIndex < 0 && recipes.size > 0) {
                return recipes.first();
            }
            return getRecipe();
        }

        @Override
        public float getPowerProduction() {
            return super.getPowerProduction();
        }

        public void updateRecipe() {
            for (int i = 0; i < recipes.size; i++) {
                boolean valid = true;

                for (ItemStack input : recipes.get(i).inputItem) {
                    if (items.get(input.item) < input.amount) {
                        valid = false;
                        break;
                    }
                }

                for (LiquidStack input : recipes.get(i).inputLiquid) {
                    if (liquids.get(input.liquid) < input.amount * Time.delta) {
                        valid = false;
                        break;
                    }
                }

                for (PayloadStack input : recipes.get(i).inputPayload) {
                    if (getPayloads().get(input.item) < input.amount) {
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    recipeIndex = i;
                    return;
                }
            }
            recipeIndex = -1;
        }

        public boolean validRecipe() {
            if (recipeIndex < 0) return false;
            for (ItemStack input : recipes.get(recipeIndex).inputItem) {
                if (items.get(input.item) < input.amount) {
                    return false;
                }
            }

            for (LiquidStack input : recipes.get(recipeIndex).inputLiquid) {
                if (liquids.get(input.liquid) < input.amount * Time.delta) {
                    return false;
                }
            }

            for (PayloadStack input : recipes.get(recipeIndex).inputPayload) {
                if (getPayloads().get(input.item) < input.amount) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void updateTile() {
            if (!validRecipe()) updateRecipe();
            super.updateTile();
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {
            if (getRecipe().ignoreLiquidOutput) return;
            super.handleLiquid(source, liquid, amount);
        }

        @Override
        public float getProgressIncrease(float baseTime) {
            float scl = 0f;
            if (!(recipeIndex < 0 || recipeIndex >= recipes.size)) scl = recipes.get(recipeIndex).boostScl;
            return super.getProgressIncrease(baseTime) * scl;
        }

        @Override
        public void craft() {
            consume();
            if (getRecipe() == null) return;

            if(outputItems != null){
                for(var output : outputItems){
                    for(int i = 0; i < Mathf.round(output.amount * getRecipe().craftScl); i++){
                        offload(output.item);
                    }
                }
            }

            if(outputPayloads != null){
                for(PayloadStack output : outputPayloads){
                    payloads.add(output.item, Mathf.round(output.amount * getRecipe().craftScl));
                }
            }

            if(wasVisible){
                craftEffect.at(x, y);
            }

            progress %= 1f;

            updateRecipe();
        }
    }
}
