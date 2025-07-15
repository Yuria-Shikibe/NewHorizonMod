package newhorizon.expand.block.special;

import arc.Core;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.UnitTypes;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.type.ItemStack;
import mindustry.type.PayloadSeq;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValues;
import mindustry.world.modules.ItemModule;
import newhorizon.NHVars;
import newhorizon.content.NHContent;
import newhorizon.expand.block.consumer.ConsumeRecipe;
import newhorizon.expand.entities.Spawner;
import newhorizon.expand.type.Recipe;
import newhorizon.util.ui.DelaySlideBar;

import static mindustry.Vars.*;

public class JumpGate extends Block {
    public Seq<UnitRecipe> recipeList = Seq.with();
    public float warmupPerSpawn = 0.2f;
    public float maxWarmupSpeed = 3f;

    public float maxRadius = 180f;
    public float minRadius = 40f;

    public int maxSpawnCount = 16;

    //todo duplicated code
    public Cons2<JumpGateBuild, Boolean> blockDrawer = (building, valid) -> {
        TextureRegion arrowRegion = NHContent.arrowRegion;
        TextureRegion pointerRegion = NHContent.pointerRegion;

        Draw.z(Layer.bullet);

        float scl = building.warmup() * 0.125f;
        float rot = building.totalProgress();

        Draw.color(building.team.color);
        Lines.stroke(8f * scl);
        Lines.square(building.x, building.y, building.block.size * tilesize / 2.5f, -rot);
        Lines.square(building.x, building.y, building.block.size * tilesize / 2f, rot);
        for (int i = 0; i < 4; i++) {
            float length = tilesize * building.block.size / 2f + 8f;
            float rotation = i * 90;
            float sin = Mathf.absin(building.totalProgress(), 16f, tilesize);
            float signSize = 0.75f + Mathf.absin(building.totalProgress() + 8f, 8f, 0.15f);

            Tmp.v1.trns(rotation + rot, -length);
            Draw.rect(arrowRegion, building.x + Tmp.v1.x, building.y + Tmp.v1.y, arrowRegion.width * scl, arrowRegion.height * scl, rotation + 90 + rot);
            length = tilesize * building.block.size / 2f + 3 + sin;
            Tmp.v1.trns(rotation, -length);
            Draw.rect(pointerRegion, building.x + Tmp.v1.x, building.y + Tmp.v1.y, pointerRegion.width * signSize * scl, pointerRegion.height * signSize * scl, rotation + 90);
        }
        Draw.color();
    };

    public JumpGate(String name) {
        super(name);
        solid = true;
        sync = true;
        breakable = true;
        update = true;
        commandable = true;
        configurable = true;
        saveConfig = true;
        canOverdrive = false;
        logicConfigurable = true;
        clearOnDoubleTap = true;
        allowConfigInventory = false;

        config(Integer.class, JumpGateBuild::changePlan);
        config(Float.class, JumpGateBuild::changeSpawnCount);
        configClear((JumpGateBuild e) -> e.recipeIndex = -1);

        consume(new ConsumeRecipe(JumpGateBuild::recipe));
        consumeBuilder.each(c -> c.multiplier = b -> {
            if (b instanceof JumpGateBuild gate) {
                return gate.costMultiplier();
            }
            return 1f;
        });
    }

    public void addUnitRecipe(UnitType unitType, float craftTime, Recipe recipe) {
        UnitRecipe unitRecipe = new UnitRecipe();
        unitRecipe.unitType = unitType;
        unitRecipe.craftTime = craftTime;
        unitRecipe.recipe = recipe;
        recipeList.add(unitRecipe);
    }

    @Override
    public void init() {
        super.init();
        clipSize = maxRadius;
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("progress", (JumpGateBuild e) -> new Bar("bar.progress", Pal.ammo, e::progress));
        addBar("efficiency", (JumpGateBuild e) -> new Bar(() -> Core.bundle.format("bar.efficiency", Strings.autoFixed(e.speedMultiplier * 100f, 0)), () -> Pal.techBlue, () -> e.speedMultiplier / maxWarmupSpeed));
        addBar("units", (JumpGateBuild e) -> new Bar(
                () -> e.unitType() == null ? "[lightgray]" + Iconc.cancel :
                        Core.bundle.format("bar.unitcap",
                                Fonts.getUnicodeStr(e.unitType().name),
                                e.team.data().countType(e.unitType()),
                                e.unitType() == null ? Units.getStringCap(e.team) : (e.unitType().useUnitCap ? Units.getStringCap(e.team) : "∞")
                        ),
                () -> Pal.power,
                () -> e.unitType() == null ? 0f : (e.unitType().useUnitCap ? (float) e.team.data().countType(e.unitType()) / Units.getCap(e.team) : 1f)
        ));
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.add(Stat.output, table -> {
            table.row();

            for (UnitRecipe unitPlan : recipeList) {
                Recipe recipe = unitPlan.recipe;
                UnitType plan = unitPlan.unitType;
                table.table(Styles.grayPanel, t -> {

                    if (plan.isBanned()) {
                        t.image(Icon.cancel).color(Pal.remove).size(40);
                        return;
                    }

                    if (plan.unlockedNow()) {
                        t.image(plan.uiIcon).size(40).pad(10f).left().scaling(Scaling.fit).with(i -> StatValues.withTooltip(i, plan));
                        t.table(info -> {
                            info.add(plan.localizedName).left();
                            info.row();
                            info.add(Strings.autoFixed(unitPlan.craftTime / 60f, 1) + " " + Core.bundle.get("unit.seconds")).color(Color.lightGray);
                        }).left();

                        t.table(req -> {
                            req.right();
                            int i = 0;
                            for (ItemStack stack: recipe.inputItem) {
                                if (++i % 6 == 0) req.row();
                                req.add(StatValues.stack(stack.item, stack.amount, true)).pad(5);
                            }
                            for (PayloadStack stack: recipe.inputPayload) {
                                if (++i % 6 == 0) req.row();
                                req.add(StatValues.stack(stack.item, stack.amount, true)).pad(5);
                            }
                        }).right().grow().pad(10f);
                    } else {
                        t.image(Icon.lock).color(Pal.darkerGray).size(40);
                    }
                }).growX().pad(5);
                table.row();
            }
        });
    }

    public static class UnitRecipe{
        public UnitType unitType = UnitTypes.alpha;
        public float craftTime = 10 * 60f;
        public Recipe recipe = Recipe.empty;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class JumpGateBuild extends Building {
        public float speedMultiplier = 1f;
        public float progress;
        public float warmup;
        public float spawnWarmup;
        public int recipeIndex;
        public int spawnCount = 1;
        public @Nullable Vec2 command = new Vec2(Float.NaN, Float.NaN);

        public ItemModule tmpItem = new ItemModule();

        @Override
        public Vec2 getCommandPosition() {
            return command;
        }

        @Override
        public void onCommand(Vec2 target) {
            command.set(target);
        }

        @Override
        public PayloadSeq getPayloads() {
            return NHVars.worldData.teamPayloadData.getPayload(team);
        }

        public UnitRecipe unitRecipe() {
            if (recipeIndex < 0 || recipeIndex > recipeList.size - 1) return null;
            return recipeList.get(recipeIndex);
        }

        public UnitType unitType() {
            if (recipeIndex < 0 || recipeIndex > recipeList.size - 1) return null;
            return recipeList.get(recipeIndex).unitType;
        }

        public Recipe recipe() {
            if (unitRecipe() == null) return Recipe.empty;
            return unitRecipe().recipe;
        }

        public float craftTime() {
            if (recipeIndex < 0 || recipeIndex > recipeList.size - 1) return 0f;
            return recipeList.get(recipeIndex).craftTime;
        }

        public float costMultiplier() {
            return state.rules.teams.get(team).unitCostMultiplier * spawnCount;
        }

        public boolean canSpawn() {
            return unitRecipe() != null;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.dashCircle(x, y, maxRadius, team.color);
            Drawf.dashCircle(x, y, minRadius, team.color);

            if (unitType() != null) drawItemSelection(unitType());

            if (Float.isNaN(command.x) || Float.isNaN(command.y)) return;
            Lines.stroke(3f, Pal.gray);
            Lines.square(command.x, command.y, 8f, 45f);
            Lines.stroke(1f, team.color);
            Lines.square(command.x, command.y, 8f, 45f);
            Draw.reset();
        }

        public void changePlan(int idx) {
            if (idx == -1) return;
            idx = Mathf.clamp(idx, 0, recipeList.size - 1);
            if (idx == recipeIndex) return;
            progress = 0f;
            recipeIndex = idx;
            speedMultiplier = 1f;
        }

        public void changeSpawnCount(float count) {
            spawnCount = Mathf.round(Mathf.clamp(count, 1, maxSpawnCount));
            progress = 0f;
            speedMultiplier = 1f;
        }

        public void spawnUnit() {
            if (unitRecipe() == null) return;
            if (unitType() == null) return;

            if (!net.client()) {
                float rot = core() == null ? Angles.angle(x, y, command.x, command.y) : Angles.angle(core().x, core().y, x, y);
                Spawner spawner = new Spawner();
                Tmp.v1.setToRandomDirection().setLength(Mathf.random(minRadius, maxRadius)).add(this);
                spawner.init(unitType(), team, Tmp.v1, rot, Mathf.clamp(unitRecipe().craftTime / maxWarmupSpeed, 5f * 60, 15f * 60));
                if (command != null) spawner.commandPos.set(command.cpy());
                spawner.add();
            }

            speedMultiplier = Mathf.clamp(speedMultiplier + warmupPerSpawn, 1, maxWarmupSpeed);
        }

        @Override
        public void updateTile() {
            super.updateTile();
            warmup = Mathf.lerp(warmup, efficiency, 0.01f);
            spawnWarmup = Mathf.lerp(spawnWarmup, efficiency, 0.01f);
            items = closestCore() == null? tmpItem: closestCore().items;
            if (unitRecipe() == null || unitType() == null) {
                progress = 0f;
                return;
            }
            if (canSpawn() && Units.canCreate(team, unitType())) {
                progress += getProgressIncrease(craftTime() * Mathf.sqrt(spawnCount));
            }
            if (progress >= 1) {
                for (int i = 0; i < spawnCount; i++){
                    spawnUnit();
                }
                consume();
                progress = 0f;
            }
        }

        @Override
        public float getProgressIncrease(float baseTime) {
            return super.getProgressIncrease(baseTime) * speedMultiplier;
        }

        public boolean canConsume() {
            return !(unitRecipe() == null || unitType() == null) && canSpawn() && Units.canCreate(team, unitType());
        }

        @Override
        public void buildConfiguration(Table table) {
            table.table(inner -> {
                inner.background(Tex.paneSolid);

                inner.label(() -> unitType() == null? "@empty": (unitType().localizedName + " x" + spawnCount)).growX().left().row();
                inner.slider(1, maxSpawnCount, 1, 1, this::configure).growX().row();
                inner.image().size(320, 4).color(Pal.accent).padTop(12f).padBottom(8f).growX().row();
                inner.pane(selectionTable -> {
                    for (int i = 0; i < recipeList.size; i++) {
                        int finalI = i;
                        UnitRecipe unitRecipe = recipeList.get(i);
                        UnitType type = unitRecipe.unitType;
                        selectionTable.button(button -> {
                            button.table(selection -> selection.stack(
                                    new DelaySlideBar(
                                            () -> Pal.techBlue,
                                            () -> "          " + Core.bundle.format("bar.unitcap",
                                                    type.localizedName,
                                                    team.data().countType(type),
                                                    type.useUnitCap ? Units.getStringCap(team) : "∞"),
                                            () -> type.useUnitCap ? (float) team.data().countType(type) / Units.getCap(team) : 1f),
                                    new Table(image -> image.image(type.uiIcon).scaling(Scaling.fit).size(48, 48).padTop(6f).padBottom(6f).padLeft(8f)).left(),
                                    new Table(req -> {
                                        req.right();
                                        req.update(() -> {
                                            req.clear();
                                            int j = 0;
                                            for (ItemStack stack: unitRecipe.recipe.inputItem) {
                                                if (++j % 6 == 0) req.row();
                                                req.add(StatValues.stack(stack.item, stack.amount * spawnCount, false)).pad(5);
                                            }
                                            for (PayloadStack stack: unitRecipe.recipe.inputPayload) {
                                                if (++j % 6 == 0) req.row();
                                                req.add(StatValues.stack(stack.item, stack.amount * spawnCount, false)).pad(5);
                                            }
                                        });
                                    }).marginLeft(60).marginTop(32f).left()
                            ).expandX().fillX()).growX();
                            button.update(() -> {
                                if (unitRecipe() == null) {
                                    button.setChecked(false);
                                }else {
                                    button.setChecked(unitRecipe == unitRecipe());
                                }
                            });
                        }, Styles.underlineb, () -> configure(finalI)).expandX().fillX().margin(0).pad(4);
                        selectionTable.row();
                    }
                }).width(342).maxHeight(400).padRight(2);
            }).width(360).maxHeight(364);
        }

        @Override
        public UnitType config() {
            return unitType();
        }

        @Override
        public float progress() {
            return progress;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public void draw() {
            super.draw();
            blockDrawer.get(this, unitType() != null && getCommandPosition() != null);
        }

        @Override
        public byte version() {
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(speedMultiplier);
            write.f(progress);
            write.i(recipeIndex);
            TypeIO.writeVec2(write, command);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision == 2){
                speedMultiplier = read.f();
                progress = read.f();
                recipeIndex = read.i();
                command = TypeIO.readVec2(read);
            }
        }
    }
}
