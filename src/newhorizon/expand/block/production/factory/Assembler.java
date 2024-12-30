package newhorizon.expand.block.production.factory;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import mindustry.world.consumers.ConsumePowerDynamic;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import newhorizon.expand.block.AdaptBlock;
import newhorizon.expand.block.AdaptBuilding;
import newhorizon.expand.block.consumer.NHConsumeItemDynamic;
import newhorizon.expand.block.consumer.NHConsumeLiquidDynamic;
import newhorizon.expand.block.consumer.NHConsumeShowStat;
import newhorizon.expand.recipe.Recipe;
import newhorizon.util.ui.display.ArrowDisplay;
import newhorizon.util.ui.display.ItemDisplay;
import newhorizon.util.ui.display.LiquidDisplay;

import static mindustry.Vars.control;
import static mindustry.Vars.iconMed;
import static newhorizon.content.NHContent.timeIcon;

public class Assembler extends AdaptBlock {
    public float warmupSpeed = 0.02f;
    public int[] capacities = {};
    public Seq<Recipe> recipeSeq = new Seq<>();
    public DrawBlock drawer = new DrawDefault();

    public Assembler(String name) {
        super(name);
        hasXen = false;

        itemCapacity = 30;
        liquidCapacity = 30f;

        update = true;
        solid = true;
        ambientSound = Sounds.machine;
        sync = true;
        ambientSoundVolume = 0.03f;
        flags = EnumSet.of(BlockFlag.factory);
        drawArrow = false;

        //TODO maybe merge this?

        consume(new NHConsumeItemDynamic((AssemblerBlockBuild e) -> e.currentRecipeIndex != -1 ? recipeSeq.get(Math.min(e.currentRecipeIndex, recipeSeq.size - 1)).inputItems : null));
        consume(new NHConsumeLiquidDynamic((AssemblerBlockBuild e) -> e.currentRecipeIndex != -1 ? recipeSeq.get(Math.min(e.currentRecipeIndex, recipeSeq.size - 1)).inputLiquids : null));
        consume(new ConsumePowerDynamic(p -> {AssemblerBlockBuild e = (AssemblerBlockBuild) p; return e.getInputPower();}));

        consume(new NHConsumeShowStat(
            (AssemblerBlockBuild e) -> e.currentRecipeIndex != -1 ? recipeSeq.get(Math.min(e.currentRecipeIndex, recipeSeq.size - 1)).inputItems : null,
            (AssemblerBlockBuild e) -> e.currentRecipeIndex != -1 ? recipeSeq.get(Math.min(e.currentRecipeIndex, recipeSeq.size - 1)).inputLiquids : null
        ));

        configurable = true;
        saveConfig = true;

        config(Integer.class, (AssemblerBlockBuild tile, Integer i) -> {
            if (!configurable) return;

            if (tile.currentRecipeIndex == i) return;
            tile.currentRecipeIndex = i < 0 || i >= recipeSeq.size ? -1 : i;
            tile.progress = 0;
        });

        config(Recipe.class, (AssemblerBlockBuild tile, Recipe val) -> {
            if (!configurable) return;

            int next = recipeSeq.indexOf(val);
            if (tile.currentRecipeIndex == next) return;
            tile.currentRecipeIndex = next;
            tile.progress = 0;
        });

        configClear((AssemblerBlockBuild tile) -> tile.currentRecipeIndex = -1);

    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void init() {
        super.init();
        capacities = new int[Vars.content.items().size];
        for (Recipe r : recipeSeq) {
            if (r.hasXen()) hasXen = true;
            if (r.inputLiquids != null || r.outputLiquids != null) hasLiquids = true;
            if (r.inputPower > 0) consumesPower = true;
            if (r.outputPower > 0) outputsPower = true;
            if (r.inputItems != null) {
                for (ItemStack stack : r.inputItems) {
                    capacities[stack.item.id] = Math.max(capacities[stack.item.id], stack.amount * 10);
                    itemCapacity = Math.max(itemCapacity, stack.amount * 2);
                }
            }
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        //removeBar("items");
        //removeBar("liquid");
        //removeBar("power");
        //todo add bar here..
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.itemCapacity);

        stats.add(Stat.output, table -> {
            table.row();

            for(Recipe r : recipeSeq){
                table.table(Styles.grayPanel, info -> {
                    info.label(() -> "[accent]" + r.recipeName).pad(5,5,0,0).left().row();
                    info.label(() -> "[white]" + r.recipeDescription).pad(5,10,5,10).size(480, 0).wrap().left().row();
                    info.table(recipeInfo -> {
                        recipeInfo.table(time -> {
                            time.left();
                            time.add(new Stack() {{
                                //todo
                                add(new Image(timeIcon).setScaling(Scaling.fit));

                                Table t = new Table().left().bottom();
                                t.add(Strings.autoFixed(r.craftTime / 60f, 1) + "s").style(Styles.outlineLabel);
                                add(t);
                            }});
                        }).size(40,0);
                        recipeInfo.image().growY().pad(0,10,0,10).width(5).color(Pal.gray);
                        recipeInfo.table(input -> {
                            input.left();
                            if(r.inputItems != null){
                                for(ItemStack stack: r.inputItems){
                                    input.add(new ItemDisplay(stack.item, stack.amount, false));
                                }
                            }
                            if(r.inputLiquids != null){
                                for(LiquidStack stack: r.inputLiquids){
                                    input.add(new LiquidDisplay(stack.liquid, stack.amount * r.craftTime, false));
                                }
                            }
                        }).size(160,0);

                        recipeInfo.table(middle -> {
                            middle.add(new ArrowDisplay(0, true));
                        }).grow();

                        recipeInfo.table(output -> {
                            output.right();
                            if(r.outputItems != null){
                                for(ItemStack stack: r.outputItems){
                                    output.add(new ItemDisplay(stack.item, stack.amount, false));
                                }
                            }
                            if(r.outputLiquids != null){
                                for(LiquidStack stack: r.outputLiquids){
                                    output.add(new LiquidDisplay(stack.liquid, stack.amount * r.craftTime, false));
                                }
                            }
                        }).size(160,0);;
                    }).right().grow().pad(20f).row();
                    if (Core.bundle.has(r.recipeDetail)){
                        Table deta = new Table();
                        deta.label(() -> "[gray]" + r.recipeDetail).pad(0,15,8,15).size(460, 0).wrap().left().row();
                        Collapser coll = new Collapser(deta, true);
                        coll.setDuration(0.1f);

                        info.table(ft -> {
                            ft.left();
                            ft.label(() -> Core.bundle.get("recipe.expand-detail"));
                            ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        }).pad(0,10,5,0).left();
                        info.row();
                        info.add(coll);
                    }
                }).growX().pad(5);
                table.row();
            }
        });
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
    }

    public class AssemblerBlockBuild extends AdaptBuilding {
        public float progress;
        public float totalProgress;
        public float warmup;
        public int currentRecipeIndex = -1;


        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        public @Nullable Recipe current() {
            return currentRecipeIndex != -1? recipeSeq.get(currentRecipeIndex): null;
        }

        @Override
        public void created() {
            super.created();
        }

        public float getInputPower() {
            return currentRecipeIndex != -1? current().inputPower: 0f;
        }

        @Override
        public Object config() {
            return currentRecipeIndex;
        }

        @Override
        public void updateTile() {

            if (!configurable) currentRecipeIndex = 0;
            if (currentRecipeIndex < 0 || currentRecipeIndex >= recipeSeq.size) currentRecipeIndex = -1;

            if (efficiency > 0 && currentRecipeIndex != -1) {

                LiquidStack[] OutputLiquids = current().outputLiquids;

                progress += getProgressIncrease(current().craftTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);

                //continuously output based on efficiency
                if (OutputLiquids != null) {
                    float inc = getProgressIncrease(1f);
                    for (LiquidStack output : OutputLiquids) {
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                    }
                }

                if(wasVisible && Mathf.chanceDelta(current().updateEffectChance)){
                    current().updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                }

            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            //TODO may look bad, revert to edelta() if so
            totalProgress += warmup * Time.delta;

            if (progress >= 1f) {
                craft();
                xen.graph.handleXen(current().xenAmount, current().xenThreshold);
            }

            dumpOutputs();
        }

        @Override
        public float getPowerProduction(){
            if (current() == null) return 0;
            //just prevent awful situation
            return current().outputPower > 0 ? current().outputPower * warmup() + 0.00001f: 0;
        }

        @Override
        public void buildConfiguration(Table table) {

            Table selection = new Table();
            selection.left().defaults().size(60f);

            Table cont = (new Table()).top();
            cont.left().defaults().left().growX();

            Runnable rebuild = () -> {
                selection.clearChildren();
                if (recipeSeq.size > 0) {
                    for (Recipe r : recipeSeq) {

                        ImageButton button = new ImageButton();

                        button.table(info -> {
                            info.table(t -> {
                                //input draw left
                                info.add(new Stack() {{
                                    add(new Image(timeIcon).setScaling(Scaling.fit));

                                    Table t = new Table().left().bottom();
                                    t.add(Strings.autoFixed(r.craftTime / 60f , 1)+ "s").style(Styles.outlineLabel);
                                    add(t);
                                }}).size(iconMed).padRight(12);
                                t.left();
                                //input items
                                if (r.inputItems != null) {
                                    for (int i = 0; i < r.inputItems.length; i++) {
                                        ItemStack stack = r.inputItems[i];
                                        info.add(new ItemDisplay(stack.item, stack.amount, false)).padRight(3);
                                    }
                                }
                                //input liquids
                                if (r.inputLiquids != null) {
                                    for (int i = 0; i < r.inputLiquids.length; i++) {
                                        LiquidStack stack = r.inputLiquids[i];
                                        info.add(new LiquidDisplay(stack.liquid, stack.amount * r.craftTime, false)).padRight(3);
                                    }
                                }
                                //todo payload here
                                //input heat
                                //if (r.inputHeatAmount > 0 && !r.isCoolant) {
                                //    info.add(new HeatDisplay(r.inputHeatAmount, false));
                                //}
                                //if (r.outputHeatAmount > 0 && r.isCoolant) {
                                //    info.add(new HeatDisplay(r.outputHeatAmount, true));
                                //}
                                //input power
                                //if (r.inputPower > 0) {
                                //    info.add(new PowerDisplay(r.inputPower, true));
                                //}

                                //arrow
                                info.add(new ArrowDisplay(0, true));

                                //output items
                                if (r.outputItems != null) {
                                    for (int i = 0; i < r.outputItems.length; i++) {
                                        ItemStack stack = r.outputItems[i];
                                        info.add(new ItemDisplay(stack.item, stack.amount, false)).padRight(3);
                                    }
                                }
                                //output liquids
                                if (r.outputLiquids != null) {
                                    for (int i = 0; i < r.outputLiquids.length; i++) {
                                        LiquidStack stack = r.outputLiquids[i];
                                        info.add(new LiquidDisplay(stack.liquid, stack.amount * r.craftTime, false)).padRight(3);
                                    }
                                }
                                //todo payload here
                                //output heat
                                //if (r.inputHeatAmount > 0 && r.isCoolant) {
                                //    info.add(new HeatDisplay(r.inputHeatAmount, false));
                                //}
                                //if (r.outputHeatAmount > 0 && !r.isCoolant) {
                                //    info.add(new HeatDisplay(r.outputHeatAmount, true));
                                //}

                                //output power
                                //if (r.outputPower > 0) {
                                //    info.add(new PowerDisplay(r.outputPower, false));
                                //}
                            });
                        }).left().expand().pad(10f);

                        button.setStyle(Styles.clearNoneTogglei);

                        //ImageButton infoTrigger = new ImageButton();
                        //infoTrigger.table(t -> {
                        //    t.add(new Image(r.unlocked? Icon.infoCircle: Icon.lock).setScaling(Scaling.fit)).size(iconLarge).pad(6,8,6,8).expandY();
                        //}).tooltip(t -> {
                        //        t.setBackground(Styles.grayPanel);
                        //        t.add(r.unlocked ? "[accent]"+ r.recipeName+ "[]\n[white]"+ r.recipeDescription + "[]": "[accent]"+ r.recipeName+ "[]\n[red]Research Required[]").wrap().width(300).pad(8,8,8,-6);
                        //    }
                        //);
                        //infoTrigger.setStyle(Styles.clearNonei);

                        /*
                        Table info = new Table();
                        info.table(t -> {
                            t.label(() -> r.recipeName);
                        });

                         */

                        button.changed(() -> {
                            currentRecipeIndex = recipeSeq.indexOf(r);
                            this.items.clear();
                            this.liquids.clear();
                            this.update();
                            configure(currentRecipeIndex);
                            control.input.config.hideConfig();
                        });
                        button.update(() ->
                            button.setChecked(currentRecipeIndex == recipeSeq.indexOf(r))
                        );

                        //infoTrigger.changed(() -> {
                        //    //info.visible = true;
                        //});
                        cont.add(button);
                        //cont.add(infoTrigger);
                        cont.row();
                    }
                }

            };
            rebuild.run();

            Table main = (new Table()).background(Styles.black6);
            main.add(selection).left().row();
            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            if (this.block != null) {
                pane.setScrollYForce(this.block.selectScroll);
                pane.update(() -> this.block.selectScroll = pane.getScrollY());
            }
            pane.setOverscroll(false, false);
            main.add(pane).maxHeight(500f);
            table.top().add(main);
        }

        @Override
        public boolean shouldConsume() {
            if (currentRecipeIndex == -1) return false;

            if (current().outputItems != null) {
                for (ItemStack output : current().outputItems) {
                    if (items.get(output.item) + output.amount > itemCapacity) {
                        return false;
                    }
                }
            }
            if (current().outputLiquids != null && !current().ignoreLiquidFullness) {
                boolean allFull = true;
                for (LiquidStack output : current().outputLiquids) {
                    if (liquids.get(output.liquid) >= liquidCapacity - 0.001f) {
                        if (!current().dumpExtraLiquid) {
                            return false;
                        }
                    } else {
                        //if there's still space left, it's not full for all liquids
                        allFull = false;
                    }
                }

                //if there is no space left for any liquid, it can't reproduce
                if (allFull) {
                    return false;
                }
            }

            return enabled;
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return capacities[item.id];
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return (
                currentRecipeIndex != -1
                    && current().inputItems != null
                    && this.items.get(item) < this.getMaximumAccepted(item)
                    && Structs.contains(current().inputItems, stack -> stack.item == item)
                    //&& acceptItemDir(source)
            );
        }
        @Override
        public boolean canDump(Building to, Item item) {
            return true;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return (currentRecipeIndex != -1 && current().inputLiquids != null && this.liquids.get(liquid) < this.block.liquidCapacity) &&
                Structs.contains(current().inputLiquids, stack -> stack.liquid == liquid);
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
        }

        @Override
        public float getProgressIncrease(float baseTime) {
            LiquidStack[] OutputLiquids = current().outputLiquids;
            if (current().ignoreLiquidFullness) {
                return super.getProgressIncrease(baseTime);
            }

            //limit progress increase by maximum amount of liquid it can produce
            float scaling = 1f, max = 1f;
            if (OutputLiquids != null) {
                max = 0f;
                for (LiquidStack s : OutputLiquids) {
                    float value = (liquidCapacity - liquids.get(s.liquid)) / (s.amount * edelta());
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }

            //when dumping excess take the maximum value instead of the minimum.
            return super.getProgressIncrease(baseTime) * (current().dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }

        public float warmupTarget() {
            return 1f;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        public void craft() {
            if (currentRecipeIndex != -1) {
                consume();
                ItemStack[] OutputItems = current().outputItems;
                if (OutputItems != null) {
                    for (ItemStack output : OutputItems) {
                        for (int i = 0; i < output.amount; i++) {
                            offload(output.item);
                        }
                    }
                }

                if(wasVisible){
                    current().craftEffect.at(x, y);
                }

                progress %= 1f;
            }
        }

        public void dumpOutputs() {
            if (currentRecipeIndex != -1) {

                ItemStack[] OutputItems = current().outputItems;
                LiquidStack[] OutputLiquids = current().outputLiquids;
                int[] liquidOutputDirections = current().liquidOutputDirections;

                if (OutputItems != null && timer(timerDump, dumpTime / timeScale)) {
                    for (ItemStack output : OutputItems) {
                        dump(output.item);
                    }
                }

                if (OutputLiquids != null) {
                    for (int i = 0; i < OutputLiquids.length; i++) {
                        int dir = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;

                        dumpLiquid(OutputLiquids[i].liquid, 2f, dir);
                    }
                }
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(currentRecipeIndex);
            write.f(progress);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            currentRecipeIndex = read.i();
            progress = read.f();
            warmup = read.f();
        }
    }
}
