package newhorizon.expand.block.production.factory;

import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.type.*;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;
import newhorizon.expand.block.consumer.*;
import newhorizon.expand.type.Recipe;

import static mindustry.world.meta.StatValues.stack;
import static mindustry.world.meta.StatValues.withTooltip;

public class RecipeGenericCrafter extends AdaptCrafter {
    public Seq<Recipe> recipes = new Seq<>();

    public Seq<Item> itemOutput = new Seq<>();
    public Seq<Liquid> liquidOutput = new Seq<>();
    public Seq<UnlockableContent> payloadOutput = new Seq<>();

    public RecipeGenericCrafter(String name) {
        super(name);
        consume(new ConsumeRecipe(RecipeGenericCrafterBuild::getRecipe, RecipeGenericCrafterBuild::getDisplayRecipe));
    }

    @Override
    public void init() {
        super.init();
        recipes.each(recipe -> {
            recipe.inputItem.each(stack -> itemFilter[stack.item.id] = true);
            recipe.inputLiquid.each(stack -> liquidFilter[stack.liquid.id] = true);
            recipe.inputPayload.each(stack -> payloadFilter.add(stack.item));

            recipe.outputItem.each(stack -> itemOutput.add(stack.item));
            recipe.outputLiquid.each(stack -> liquidOutput.add(stack.liquid));
            recipe.outputPayload.each(stack -> payloadOutput.add(stack.item));
        });

        outputItem = null;
        outputLiquid = null;

        outputItems = null;
        outputLiquids = null;
        outputPayloads = null;

        craftTime = 60f;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.input, display());
        stats.remove(Stat.output);
        stats.remove(Stat.productionTime);
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
                                recipe.inputItem.each(stack -> row.add(display(stack.item, stack.amount, recipe.craftTime)));
                                recipe.inputLiquid.each(stack -> row.add(display(stack.liquid, stack.amount * Time.toSeconds, 60f)));
                                recipe.inputPayload.each(stack -> row.add(display(stack.item, stack.amount, recipe.craftTime)));
                            }).growX();
                            inner.table(row -> {
                                row.left();
                                row.image(Icon.right).size(32f).padLeft(8f).padRight(12f);
                                recipe.outputItem.each(stack -> row.add(display(stack.item, stack.amount, recipe.craftTime)));
                                recipe.outputLiquid.each(stack -> row.add(display(stack.liquid, stack.amount * Time.toSeconds, 60f)));
                                recipe.outputPayload.each(stack -> row.add(display(stack.item, stack.amount, recipe.craftTime)));
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
            if (getRecipe() == null) return;
            if(efficiency > 0){
                float inc = getProgressIncrease(craftTime / getRecipe().craftTime);
                getRecipe().outputLiquid.each(stack -> {
                    handleLiquid(this, stack.liquid, Math.min(stack.amount * inc, liquidCapacity - liquids.get(stack.liquid)));
                });
            }
            getRecipe().outputItem.each(stack -> {
                if (items.get(stack.item) >= itemCapacity) {
                    items.set(stack.item, itemCapacity);
                }
            });
            getRecipe().outputPayload.each(stack -> {
                if (getPayloads().get(stack.item) >= payloadCapacity) {
                    getPayloads().remove(stack.item, getPayloads().get(stack.item) - payloadCapacity);
                }
            });
        }

        @Override
        public void dumpOutputs() {
            boolean timer = timer(timerDump, dumpTime / timeScale);
            if (timer) {
                itemOutput.each(this::dump);
                payloadOutput.each(output -> {
                    BuildPayload payload = new BuildPayload((Block) output, team);
                    payload.set(x, y, rotdeg());
                    dumpPayload(payload);
                });
            }
            liquidOutput.each(output -> dumpLiquid(output, 2f, -1));
        }

        @Override
        public boolean shouldConsume() {
            if (getRecipe() == null) return false;
            for (var output : getRecipe().outputItem) {
                if (items.get(output.item) + output.amount > itemCapacity) {
                    return powerProduction > 0;
                }
            }
            for (var output : getRecipe().outputPayload) {
                if (getPayloads().get(output.item) + output.amount > payloadCapacity) {
                    return powerProduction > 0;
                }
            }
            if (!ignoreLiquidFullness) {
                if (getRecipe().outputLiquid.isEmpty()) return true;
                boolean allFull = true;
                for (var output : getRecipe().outputLiquid) {
                    if (liquids.get(output.liquid) >= liquidCapacity - 0.001f) {
                        if (!dumpExtraLiquid) {
                            return false;
                        }
                    } else {
                        allFull = false;
                    }
                }
                if (allFull) {
                    return false;
                }
            }
            return enabled;
        }

        @Override
        public float getProgressIncrease(float baseTime) {
            float scl = 1f;
            if (getRecipe() != null) scl = getRecipe().craftTime / craftTime;
            return super.getProgressIncrease(baseTime) / scl;
        }

        @Override
        public void craft() {
            if (getRecipe() == null) return;

            consume();

            getRecipe().outputItem.each(stack -> {
                for(int i = 0; i < stack.amount; i++){
                    offload(stack.item);
                }
            });
            getRecipe().outputPayload.each(stack -> payloads.add(stack.item, stack.amount));

            progress %= 1f;

            if(wasVisible) craftEffect.at(x, y);
            updateRecipe();
        }
    }
}
