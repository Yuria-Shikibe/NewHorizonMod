package newhorizon.expand.block.production.factory;

import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValue;
import newhorizon.expand.block.consumer.NHConsumeItemDynamic;
import newhorizon.expand.block.consumer.NHConsumeLiquidDynamic;
import newhorizon.expand.block.consumer.NHConsumeShowStat;
import newhorizon.util.ui.display.ItemDisplay;
import newhorizon.util.ui.display.LiquidDisplay;

public class RecipeGenericCrafter extends AdaptCrafter{
    public int alterRecipe = 0;
    public Seq<ItemStack[]> recipeItemInput = new Seq<>();
    public Seq<LiquidStack[]> recipeLiquidInput = new Seq<>();

    public RecipeGenericCrafter(String name) {
        super(name);

        consume(new NHConsumeItemDynamic(RecipeGenericCrafterBuild::getInputItems));
        consume(new NHConsumeLiquidDynamic(RecipeGenericCrafterBuild::getInputLiquids));
        consume(new NHConsumeShowStat(RecipeGenericCrafterBuild::getDisplayInputItems, RecipeGenericCrafterBuild::getDisplayInputLiquids));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.input, display());
    }

    public void addInput(ItemStack[] itemStacks, LiquidStack[] liquidStacks){
        recipeItemInput.add(itemStacks);
        recipeLiquidInput.add(liquidStacks);
        alterRecipe++;
    }

    public StatValue display(){
        return table -> {
            table.table(cont -> {
                for (int r = 0; r < alterRecipe; r++){
                    int idx = r;
                    cont.table(t -> {
                        t.label(() -> "Recipe " + (idx+1) + ": ");
                        if (recipeItemInput.get(idx) != null){
                            for (ItemStack stack : recipeItemInput.get(idx)){
                                t.add(new ItemDisplay(stack.item, stack.amount, craftTime, true)).padRight(5);
                            }
                        }
                        if (recipeItemInput.get(idx) != null){
                            for (LiquidStack stack : recipeLiquidInput.get(idx)){
                                t.add(new LiquidDisplay(stack.liquid, (stack.amount * 60f) * (60f / craftTime), true)).padRight(5);
                            }
                        }
                        t.left();
                    }).fillX();
                    cont.row();
                }
            });
        };
    }

    @Override
    public void init() {
        super.init();

        //i suggest set those true to avoid some problems
        hasItems = true;
        hasLiquids = true;
        hasPower = true;

        recipeItemInput.setSize(alterRecipe);
        recipeLiquidInput.setSize(alterRecipe);

        for (int i = 0; i < alterRecipe; i++) {
            for (ItemStack input: recipeItemInput.get(i)) {
                itemFilter[input.item.id] = true;
            }
            for (LiquidStack input: recipeLiquidInput.get(i)) {
                liquidFilter[input.liquid.id] = true;
            }
        }
    }

    public class RecipeGenericCrafterBuild extends AdaptCrafterBuild{
        public int recipeIdx = -1;

        public void updateRecipeIdx(){
            for (int i = 0; i < alterRecipe; i++) {
                boolean validItemInput = true;
                boolean validLiquidInput = true;
                for (ItemStack input: recipeItemInput.get(i)) {
                    if (items.get(input.item) < input.amount){
                        validItemInput = false;
                        break;
                    }
                }
                for (LiquidStack input: recipeLiquidInput.get(i)) {
                    if (liquids.get(input.liquid) < input.amount * Time.delta){
                        validLiquidInput = false;
                        break;
                    }
                }
                if (validItemInput && validLiquidInput) {
                    recipeIdx = i;
                    return;
                }
            }
            recipeIdx = -1;
        }

        public boolean validRecipeIdx(){
            if (recipeIdx < 0) return false;
            for (ItemStack input: recipeItemInput.get(recipeIdx)) {
                if (items.get(input.item) < input.amount){
                    return false;
                }
            }
            for (LiquidStack input: recipeLiquidInput.get(recipeIdx)) {
                if (liquids.get(input.liquid) < input.amount * Time.delta){
                    return false;
                }
            }
            return true;
        }

        @Override
        public void updateTile() {
            if (!validRecipeIdx()) updateRecipeIdx();
            super.updateTile();
        }

        @Override
        public void craft() {
            super.craft();
            updateRecipeIdx();
        }

        public ItemStack[] getDisplayInputItems(){
            if (recipeIdx < 0 && recipeItemInput.get(0) != null) return recipeItemInput.get(0);
            if (recipeIdx < 0) return null;
            return recipeItemInput.get(recipeIdx);
        }

        public LiquidStack[] getDisplayInputLiquids(){
            if (recipeIdx < 0 && recipeLiquidInput.get(0) != null) return recipeLiquidInput.get(0);
            if (recipeIdx < 0) return null;
            return recipeLiquidInput.get(recipeIdx);
        }

        public @Nullable ItemStack[] getInputItems(){
            if (recipeIdx < 0) return null;
            return recipeItemInput.get(recipeIdx);
        }

        public @Nullable LiquidStack[] getInputLiquids(){
            if (recipeIdx < 0) return null;
            return recipeLiquidInput.get(recipeIdx);
        }
    }
}
