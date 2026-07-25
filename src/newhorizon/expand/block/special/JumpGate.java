package newhorizon.expand.block.special;

import arc.Core;
import arc.func.Cons;
import arc.func.Cons2;
import arc.func.Floatp;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.event.HandCursorListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.core.World;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.Ranged;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.PayloadSeq;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ContentInfoDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;
import mindustry.world.modules.ItemModule;
import newhorizon.NHVars;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.expand.entities.Spawner;
import newhorizon.expand.type.Recipe;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.ui.ItemImageDynamic;
import newhorizon.util.ui.TableFunc;
import newhorizon.util.ui.display.ItemDisplay;
import newhorizon.util.ui.display.ItemImage;

import static mindustry.Vars.*;
import static newhorizon.util.func.NHFunc.regSize;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;
import static newhorizon.util.ui.TableFunc.dialogHeight;
import static newhorizon.util.ui.TableFunc.dialogWidth;
import static newhorizon.util.ui.TableFunc.ui;

public class JumpGate extends Block {
    public Seq<UnitRecipe> recipeList = Seq.with();

    public float warmupPerSpawn = 0.2f;
    public float maxWarmupSpeed = 3f;
    public float maxRadius = 180f;
    public int maxSpawnCount = 16;

    public float spawnDelay = 5f;
    public float spawnReloadTime = 180f;
    public float spawnRange = tilesize * 12f;
    public float cooldownTime = 300f;
    public float buildSpeedMultiplierCoefficient = 1f;
    public float atlasSizeScl = 1f;
    public float squareStroke = 2f;
    public float progressCircleRadiusScl = 1.15f;
    public float pointerOutsidePad = 10f;

    protected static int selectID = -1, selectNum = 1;
    protected static final Vec2 linkVec = new Vec2();
    protected static final Point2 point = new Point2();
    protected static final ObjectSet<Item> tmpItems = new ObjectSet<>();

    public float progressCircleRadius() {
        return size * tilesize / progressCircleRadiusScl;
    }

    public Cons2<JumpGateBuild, Boolean> blockDrawer = (building, valid) -> {
        TextureRegion arrowRegion = NHContent.arrowRegion;
        TextureRegion pointerRegion = NHContent.pointerRegion;

        Draw.z(Layer.bullet);

        float sizeScl = building.block.size / 5f;
        float scl = building.warmup() * atlasSizeScl * sizeScl;
        float rot = building.totalProgress();
        float circleR = progressCircleRadius();

        Draw.color(building.getColor(building.getRecipe()));
        Lines.stroke(squareStroke * building.warmup() * sizeScl);
        Lines.square(building.x, building.y, building.block.size * tilesize / 2.5f, -rot);
        Lines.square(building.x, building.y, building.block.size * tilesize / 2f, rot);
        for (int i = 0; i < 4; i++) {
            float length = tilesize * building.block.size / 2f + 8f * sizeScl;
            float rotation = i * 90;
            float sin = Mathf.absin(building.totalProgress(), 16f, tilesize * sizeScl);
            float signSize = 0.75f + Mathf.absin(building.totalProgress() + 8f, 8f, 0.15f);

            Tmp.v1.trns(rotation + rot, -length);
            Draw.rect(arrowRegion, building.x + Tmp.v1.x, building.y + Tmp.v1.y, arrowRegion.width * Draw.scl * scl, arrowRegion.height * Draw.scl * scl, rotation + 90 + rot);
            length = circleR + pointerOutsidePad * sizeScl + sin;
            Tmp.v1.trns(rotation, -length);
            Draw.rect(pointerRegion, building.x + Tmp.v1.x, building.y + Tmp.v1.y, pointerRegion.width * Draw.scl * signSize * scl, pointerRegion.height * Draw.scl * signSize * scl, rotation + 90);
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
        copyConfig = true;
        canOverdrive = false;
        logicConfigurable = true;
        clearOnDoubleTap = true;
        allowConfigInventory = false;
        acceptsItems = true;
        unloadable = true;
        hasPower = true;
        hasItems = true;
        separateItemCapacity = true;
        timers = 3;

        config(Boolean.class, (JumpGateBuild tile, Boolean i) -> {
            if (i) tile.spawn(tile.getRecipe());
            else tile.startBuild(-1, 0);
        });

        config(Point2.class, (Cons2<JumpGateBuild, Point2>) JumpGateBuild::linkPos);

        config(IntSeq.class, (JumpGateBuild tile, IntSeq seq) -> {
            if (seq.size < 2) return;
            int mode = seq.get(0);
            if (mode == 0) {
                if (seq.size < 3) return;
                tile.enqueueBuild(seq.get(1), seq.get(2), seq.size >= 4 && seq.get(3) != 0);
            } else if (mode == 1) {
                tile.buildQueue.clear();
            } else if (mode == 2) {
                int idx = seq.get(1);
                if (idx >= 0 && idx < tile.buildQueue.size) {
                    tile.buildQueue.remove(idx);
                }
            } else if (mode == 3) {
                if (seq.size < 3) return;
                tile.moveQueue(seq.get(1), seq.get(2));
            }
        });

        configClear((JumpGateBuild tile) -> {
            tile.buildQueue.clear();
            tile.startBuild(-1, 0);
        });
    }

    public void addUnitRecipe(UnitType unitType, float craftTime, Recipe recipe) {
        UnitRecipe unitRecipe = new UnitRecipe();
        unitRecipe.unitType = unitType;
        unitRecipe.craftTime = craftTime;
        unitRecipe.recipe = recipe;
        recipeList.add(unitRecipe);
    }

    public static boolean hideRecipe(UnitType type) {
        return state.rules.bannedUnits.contains(type) || type.locked() && !state.rules.infiniteResources && state.isCampaign();
    }

    @Override
    public void init() {
        super.init();
        clipSize = Math.max(maxRadius, size * tilesize * 4f);
        if (spawnRange <= 0f) spawnRange = tilesize * 12f;
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("progress",
                (JumpGateBuild entity) -> new Bar(
                        () -> entity.isCalling() ? Core.bundle.get("bar.progress") : "[lightgray]" + Iconc.cancel,
                        () -> entity.isCalling() && Units.canCreate(entity.team, entity.getType()) ? Pal.power : Pal.redderDust,
                        () -> entity.isCalling() ? entity.buildProgress / entity.costTime(entity.getRecipe(), true) : 0
                )
        );
        addBar("cooldown",
                (JumpGateBuild entity) -> new Bar(
                        () -> Core.bundle.get("stat.cooldowntime"),
                        () -> Pal.lancerLaser,
                        () -> entity.cooling ? (cooldownTime - entity.cooldown) / cooldownTime : 0
                )
        );
        addBar("units", (JumpGateBuild e) -> new Bar(
                () -> e.getType() == null ? "[lightgray]" + Iconc.cancel :
                        Core.bundle.format("bar.unitcap",
                                Fonts.getUnicodeStr(e.getType().name),
                                e.team.data().countType(e.getType()),
                                e.getType().useUnitCap ? Units.getStringCap(e.team) : "\u221E"
                        ),
                () -> Pal.power,
                () -> e.getType() == null ? 0f : (e.getType().useUnitCap ? (float) e.team.data().countType(e.getType()) / Units.getCap(e.team) : 1f)
        ));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, maxRadius / tilesize, StatUnit.blocks);
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
                            for (ItemStack stack : recipe.inputItem) {
                                if (++i % 6 == 0) req.row();
                                req.add(StatValues.stack(stack.item, stack.amount, true)).pad(5);
                            }
                            for (PayloadStack stack : recipe.inputPayload) {
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

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, maxRadius, Pal.accent);
    }

    public void showInfo(UnitRecipe set, Element extra, @Nullable ItemModule module) {
        BaseDialog dialogIn = new BaseDialog("More Info");
        dialogIn.addCloseListener();
        dialogIn.cont.margin(15f);
        if (!mobile) {
            dialogIn.cont.marginLeft(220f).marginRight(220f);
        }
        dialogIn.cont.pane(inner -> {
            inner.button(new TextureRegionDrawable(set.unitType.fullIcon), Styles.clearNonei, () -> new ContentInfoDialog().show(set.unitType)).growX().fillY().center().row();
            inner.image().growX().height(OFFSET / 4).pad(OFFSET / 4f).color(Pal.accent).row();
            inner.add("[lightgray]" + Core.bundle.get("editor.spawn") + ": [accent]" + set.unitType.localizedName).left().padLeft(OFFSET).row();
            inner.add("[lightgray]" + Core.bundle.get("stat.buildtime") + ": [accent]" + Strings.fixed(set.costTimeVar() / 60, 2) + "[lightgray] " + Core.bundle.get("unit.seconds")).left().padLeft(OFFSET).row();
            inner.image().growX().height(OFFSET / 4).pad(OFFSET / 4f).color(Pal.accent).row();
            inner.table(table -> {
                int index = 0;
                for (ItemStack stack : set.baseRequirements()) {
                    if (module != null || index % 7 == 0) table.row();
                    if (module != null) {
                        TableFunc.itemStack(table, stack, module);
                    } else {
                        table.add(new ItemDisplay(stack.item, stack.amount, false).left()).padLeft(OFFSET / 2).left();
                    }
                    index++;
                }
                for (PayloadStack stack : set.recipe.inputPayload) {
                    if (module != null || index % 7 == 0) table.row();
                    table.add(StatValues.stack(stack.item, stack.amount, true)).padLeft(OFFSET / 2).left();
                    index++;
                }
            }).growX().fillY().left().padLeft(OFFSET).row();
            inner.image().growX().pad(OFFSET / 4f).height(OFFSET / 4).color(Pal.accent).row();
            inner.add(extra).left().padLeft(OFFSET).row();
            inner.button("@back", Icon.left, Styles.cleart, dialogIn::hide).size(LEN * 3f, LEN).pad(OFFSET);
        }).grow().row();
        dialogIn.show();
    }

    public void buildRecipeTable(Table parent, UnitRecipe set, Cons<Table> cons) {
        if (state.rules.bannedUnits.contains(set.unitType)) {
            parent.table(Styles.grayPanel, t2 -> {
                t2.margin(6f);
                t2.defaults().left().padRight(OFFSET);
                t2.table(Tex.clear, table2 -> {
                    TableFunc.tableImageShrink(set.unitType.fullIcon, LEN, table2, i -> i.color.set(Pal.gray));
                    table2.image(Icon.cancel).size(LEN + OFFSET * 1.5f).color(Color.scarlet).padLeft(OFFSET / 2f);
                }).left().padLeft(OFFSET * 2f);
                t2.pane(table2 -> table2.add(Core.bundle.get("banned")));
            }).growX().fillY().padBottom(OFFSET / 2).row();
        } else if (set.unitType.locked() && !state.rules.infiniteResources && state.isCampaign()) {
            parent.table(Styles.grayPanel, t2 -> {
                t2.margin(6f);
                t2.defaults().left().padRight(OFFSET);
                t2.table(Tex.clear, table2 -> table2.image(Icon.lock).size(LEN + OFFSET * 1.5f)).left().padLeft(OFFSET / 2f);
                t2.pane(table2 -> table2.add("[gray]Need to be researched.").left().row()).grow();
            }).growX().fillY().padBottom(OFFSET / 2).row();
        } else {
            parent.table(Styles.grayPanel, t2 -> {
                t2.margin(6f);
                t2.defaults().left().padRight(OFFSET);
                t2.image(set.unitType.fullIcon).size(LEN + OFFSET).scaling(Scaling.fit).left().padLeft(OFFSET / 2f);
                t2.pane(table2 -> {
                    table2.left().marginLeft(12f);
                    table2.add("[lightgray]" + Core.bundle.get("editor.spawn") + ": [accent]" + set.unitType.localizedName).left().row();
                    table2.add("[lightgray]" + Core.bundle.get("stat.buildtime") + ": [accent]" + TableFunc.format(set.costTimeVar() / 60) + "[lightgray] " + Core.bundle.get("unit.seconds")).row();
                }).growX().height(LEN).center();
                t2.pane(items -> {
                    items.right();
                    for (ItemStack stack : set.baseRequirements()) {
                        items.add(new ItemImage(stack.item.fullIcon, stack.amount)).padRight(OFFSET / 2).left();
                    }
                    for (PayloadStack stack : set.recipe.inputPayload) {
                        items.add(StatValues.stack(stack.item, stack.amount, true)).padRight(OFFSET / 2).left();
                    }
                }).growX().height(LEN).center();
                t2.table(cons).fillX().height(LEN + OFFSET).right();
            }).growX().fillY().padBottom(OFFSET / 2).row();
        }
    }

    public static class UnitRecipe {
        public UnitType unitType = UnitTypes.alpha;
        public float craftTime = 10 * 60f;
        public Recipe recipe = Recipe.empty;

        public float costTime() {
            return craftTime;
        }

        public float costTimeVar() {
            return craftTime / state.rules.unitBuildSpeedMultiplier;
        }

        public ItemStack[] baseRequirements() {
            return recipe.inputItem.toArray(ItemStack.class);
        }

        public ItemStack[] dynamicRequirements() {
            return ItemStack.mult(baseRequirements(), state.rules.unitCost(teamFallback()));
        }

        public ItemStack[] dynamicRequirements(mindustry.game.Team team) {
            return ItemStack.mult(baseRequirements(), state.rules.unitCost(team));
        }

        private mindustry.game.Team teamFallback() {
            return player == null ? mindustry.game.Team.sharded : player.team();
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class JumpGateBuild extends Building implements Ranged {
        public int spawnID = -1;
        public int link = -1;
        public float buildProgress;
        public float totalProgress;
        public float warmup;
        public boolean jammed;

        public float cooldown;
        public boolean cooling;
        public boolean releasing;

        public int spawnNum = 1;
        public int buildingSpawnNum;

        public final Seq<int[]> buildQueue = new Seq<>();
        public boolean buildingLoop;
        public int activeGen;

        public @Nullable UnitType queueSnapType;
        public int queueSnapCount;
        public boolean queueSnapLoop;
        public float queueSnapProgress;
        public boolean queueSnapJammed;
        public boolean snapFromReleasing;
        public boolean releaseExitLock;
        public @Nullable int[] promotedEntry;

        public @Nullable Vec2 commandPos;

        @Override
        public void onCommand(Vec2 target) {
            hitbox(Tmp.r1);
            if (Tmp.r1.contains(target)) commandPos = null;
            else commandPos = target;
        }

        @Override
        public Vec2 getCommandPosition() {
            return commandPos;
        }

        @Override
        public PayloadSeq getPayloads() {
            return NHVars.worldData.teamPayloadData.getPayload(team);
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            getPayloads().add(payload.content(), 1);
            Fx.payloadDeposit.at(payload.x(), payload.y(), payload.angleTo(this), new UnitAssembler.YeetData(new Vec2(x, y), payload.content()));
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return !usesCoreItems() && realItems().get(item) < getMaximumAccepted(item);
        }

        @Override
        public void created() {
            super.created();
            buildQueue.removeAll(e -> {
                UnitRecipe set = getRecipe(e[0]);
                return set == null || hideRecipe(set.unitType);
            });
        }

        @Override
        public IntSeq config() {
            return IntSeq.with(0, selectID, spawnNum);
        }

        public boolean usesCoreItems() {
            return NHVars.worldData.worldData.jumpGateUsesCoreItems;
        }

        public ItemModule realItems() {
            return usesCoreItems() && team.data().hasCore() ? team.core().items : items;
        }

        public UnitRecipe getRecipe() {
            return getRecipe(spawnID);
        }

        public UnitRecipe getRecipe(int id) {
            if (id < 0 || id >= recipeList.size) return null;
            return recipeList.get(id);
        }

        public UnitType getType() {
            UnitRecipe set = getRecipe();
            return set == null ? null : set.unitType;
        }

        public boolean isCalling() {
            return getRecipe() != null;
        }

        public boolean canConsume() {
            return isCalling() && Units.canCreate(team, getType());
        }

        public float speedMultiplier(int num) {
            return Mathf.sqrt(num) * buildSpeedMultiplierCoefficient;
        }

        public float costTime(UnitRecipe set, boolean buildingParma) {
            if (set == null) return 1f;
            int num = buildingParma ? buildingSpawnNum : spawnNum;
            return Math.max(num, 1) * set.costTime() / speedMultiplier(Math.max(num, 1));
        }

        public boolean canSpawn(UnitRecipe set, boolean buildingParma) {
            if (set == null) return false;
            int num = buildingParma ? buildingSpawnNum : spawnNum;
            return team.data().countType(set.unitType) + num <= Units.getCap(team);
        }

        public boolean canSpawn(UnitRecipe set, int num) {
            if (set == null) return false;
            return team.data().countType(set.unitType) + num <= Units.getCap(team);
        }

        public boolean hasConsume(UnitRecipe set, int num) {
            if (set == null || cheating() || (!state.rules.pvp && team == state.rules.waveTeam)) return true;
            float mult = num * state.rules.teams.get(team).unitCostMultiplier;
            if (!realItems().has(ItemStack.mult(set.baseRequirements(), mult))) return false;
            for (PayloadStack stack : set.recipe.inputPayload) {
                if (getPayloads().get(stack.item) < Math.round(stack.amount * mult)) return false;
            }
            return true;
        }

        public void consumeItems() {
            if (cheating() || getRecipe() == null) return;
            float mult = buildingSpawnNum * state.rules.teams.get(team).unitCostMultiplier;
            realItems().remove(ItemStack.mult(getRecipe().baseRequirements(), mult));
            for (PayloadStack stack : getRecipe().recipe.inputPayload) {
                getPayloads().remove(stack.item, Math.round(stack.amount * mult));
            }
        }

        public void enqueueBuild(int id, int num) {
            enqueueBuild(id, num, false);
        }

        public void enqueueBuild(int id, int num, boolean loop) {
            UnitRecipe set = getRecipe(id);
            if (set == null || num < 1 || hideRecipe(set.unitType)) return;
            buildQueue.add(new int[]{id, num, loop ? 1 : 0});
            tryStartQueue();
        }

        public void moveQueue(int from, int to) {
            if (from < 0 || to < 0 || from >= buildQueue.size || to >= buildQueue.size || from == to) return;
            buildQueue.insert(to, buildQueue.remove(from));
        }

        public static boolean isLoopEntry(int[] entry) {
            return entry != null && entry.length > 2 && entry[2] != 0;
        }

        private void handleDrag(Table rowTable, int[] indexRef, boolean[] dragging, Runnable onDone) {
            rowTable.addCaptureListener(new InputListener() {
                float startStageY;
                int from;
                int to;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    if (x > rowTable.getWidth() - 40f) return false;
                    dragging[0] = true;
                    from = indexRef[0];
                    to = indexRef[0];
                    startStageY = event.stageY;
                    rowTable.toFront();
                    event.stop();
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    if (!dragging[0]) return;
                    float dy = event.stageY - startStageY;
                    rowTable.translation.y = dy;
                    to = Mathf.clamp(from - Math.round(dy / (LEN + 4f)), 0, Math.max(buildQueue.size - 1, 0));
                    event.stop();
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    if (!dragging[0]) return;
                    rowTable.translation.y = 0f;
                    dragging[0] = false;
                    if (from != to) {
                        configure(IntSeq.with(3, from, to));
                    }
                    onDone.run();
                    event.stop();
                }
            });
        }

        private void animateQueueIn(Element row) {
            row.clearActions();
            row.color.a = 0f;
            row.translation.x = 56f;
            row.actions(Actions.parallel(
                    Actions.fadeIn(0.28f, Interp.pow2Out),
                    Actions.translateBy(-56f, 0f, 0.28f, Interp.pow2Out)
            ));
        }

        private void animateQueueOut(Element row, Runnable done) {
            row.clearActions();
            row.touchable = Touchable.disabled;
            row.actions(
                    Actions.parallel(
                            Actions.fadeOut(0.28f, Interp.pow2In),
                            Actions.translateBy(56f, 0f, 0.28f, Interp.pow2In)
                    ),
                    Actions.run(done)
            );
        }

        private Element releasingBar(Floatp progress) {
            return new Element() {
                {
                    touchable = Touchable.disabled;
                }

                @Override
                public void draw() {
                    float x = this.x, y = this.y, w = width, h = height;
                    float p = Mathf.clamp(progress.get());
                    float a = parentAlpha * color.a;

                    Draw.color(Pal.darkerGray, a);
                    Fill.crect(x, y, w, h);
                    Draw.color(Pal.accent, a);
                    Fill.crect(x, y, w * p, h);

                    String text = Core.bundle.get("mod.ui.releasing");
                    Font font = Fonts.outline;
                    GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
                    font.getData().setScale(1f);
                    lay.setText(font, text);
                    float tx = x + (w - lay.width) / 2f;
                    float ty = y + (h + lay.height) / 2f;

                    if (p < 0.999f) {
                        if (clipBegin(x + w * p, y, w * (1f - p), h)) {
                            font.setColor(Pal.accent);
                            font.getColor().a = a;
                            font.draw(text, tx, ty);
                            clipEnd();
                        }
                    }
                    if (p > 0.001f) {
                        if (clipBegin(x, y, w * p, h)) {
                            font.setColor(Color.white);
                            font.getColor().a = a;
                            font.draw(text, tx, ty);
                            clipEnd();
                        }
                    }

                    font.getData().setScale(1f);
                    Pools.free(lay);
                    Draw.reset();
                }
            };
        }

        public void captureQueueSnap() {
            if (releasing && queueSnapType != null) {
                queueSnapProgress = 1f;
                queueSnapJammed = false;
                snapFromReleasing = true;
                return;
            }
            UnitRecipe recipe = getRecipe();
            if (recipe == null) return;
            snapFromReleasing = false;
            queueSnapType = recipe.unitType;
            queueSnapCount = buildingSpawnNum;
            queueSnapLoop = buildingLoop;
            queueSnapProgress = Mathf.clamp(buildProgress / Math.max(costTime(recipe, true), 1f));
            queueSnapJammed = jammed;
        }

        public void tryStartQueue() {
            if (releaseExitLock || isCalling() || cooling || buildQueue.isEmpty()) return;
            if (power != null && power.status < 0.5f) return;

            int[] entry = buildQueue.first();
            UnitRecipe set = getRecipe(entry[0]);
            if (set == null) {
                buildQueue.remove(0);
                tryStartQueue();
                return;
            }
            if (!hasConsume(set, entry[1]) || !canSpawn(set, entry[1])) return;

            buildingLoop = isLoopEntry(entry);
            promotedEntry = entry;
            buildQueue.remove(0);
            startBuild(entry[0], entry[1]);
        }

        public Color getColor(UnitRecipe set) {
            if (cooling) return Pal.lancerLaser;
            if (jammed || (set != null && !canSpawn(set, true))) {
                return Tmp.c1.set(team.color).lerp(Pal.ammo, Mathf.absin(10f, 0.3f) + 0.1f);
            }
            return team.color;
        }

        @Override
        public void updateTile() {
            totalProgress += (efficiency + warmup) * delta() * Mathf.curve(Time.delta, 0f, 0.5f);
            if (!cooling && isCalling() && Units.canCreate(team, getType())) {
                buildProgress += efficiency * state.rules.unitBuildSpeed(team) * delta() * warmup;
                if (buildProgress >= costTime(getRecipe(), true) && !jammed) {
                    spawn(getRecipe());
                }
            }

            if (cooling) {
                if (Mathf.chanceDelta(0.2f)) {
                    Fx.reactorsmoke.at(x + Mathf.range(tilesize * size / 2f), y + Mathf.range(tilesize * size / 2f));
                }
                if (timer.get(0, 4)) {
                    for (int i = 0; i < 4; i++) {
                        Fx.shootSmallSmoke.at(x, y, i * 90);
                    }
                }
                cooldown += warmup * delta();
                if (cooldown > cooldownTime) {
                    boolean wasReleasing = releasing;
                    if (wasReleasing) captureQueueSnap();
                    cooling = false;
                    releasing = false;
                    cooldown = 0;
                    if (wasReleasing) {
                        releaseExitLock = true;
                        Time.run(0.32f * Time.toSeconds, () -> {
                            if (!isValid()) return;
                            if (releaseExitLock) {
                                releaseExitLock = false;
                                snapFromReleasing = false;
                                tryStartQueue();
                            }
                        });
                    } else {
                        tryStartQueue();
                    }
                }
            }

            if (efficiency > 0 && power.status > 0.5f) {
                if (Mathf.equal(warmup, 1, 0.0015f)) warmup = 1f;
                else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
            } else {
                if (Mathf.equal(warmup, 0, 0.0015f)) warmup = 0f;
                else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
            }

            if (timer(1, 20)) {
                tryStartQueue();
                if (jammed && isCalling()) {
                    Tile t = null;
                    while (t == null) {
                        Tmp.v1.set(1, 1).rnd(range()).add(this).clamp(0, 0, world.unitWidth(), world.unitHeight());
                        t = world.tile(World.toTile(Tmp.v1.x), World.toTile(Tmp.v1.y));
                    }
                    link = t.pos();
                    spawn(getRecipe());
                }
            }
        }

        public void startBuild(int set, int num) {
            jammed = false;
            if (isCalling()) cooling = true;

            if (getRecipe(set) == null) {
                if (isCalling() && getRecipe() != null) {
                    captureQueueSnap();
                    float remain = (costTime(getRecipe(), true) - buildProgress) / costTime(getRecipe(), true);
                    float mult = buildingSpawnNum * remain * state.rules.teams.get(team).unitCostMultiplier;
                    Building target = usesCoreItems() && team.data().hasCore() ? team.core() : self();
                    for (ItemStack stack : ItemStack.mult(getRecipe().baseRequirements(), mult)) {
                        realItems().add(stack.item, Math.min(stack.amount, target.getMaximumAccepted(stack.item) - realItems().get(stack.item)));
                    }
                }
                spawnID = -1;
                buildProgress = 0;
                buildingLoop = false;
                releasing = false;
            } else {
                activeGen++;
                spawnID = set;
                buildProgress = 1;
                buildingSpawnNum = num;
                spawnNum = num;
                releasing = false;
                consumeItems();
            }
        }

        public void spawn(UnitRecipe set) {
            if (!isValid() || set == null) return;
            Vec2 target = link();
            NHFx.spawn.at(x, y, regSize(set.unitType), team.color, this);

            boolean wasLoop = buildingLoop;
            int loopId = spawnID;
            int loopNum = buildingSpawnNum;
            int count = Math.max(buildingSpawnNum, 1);

            Seq<Vec2> points = new Seq<>();
            Seq<Tile> tiles = NHFunc.ableToSpawn(set.unitType, target.x, target.y, spawnRange);
            if (tiles.size > 0) {
                for (int i = 0; i < count; i++) {
                    Tile t = tiles.random();
                    points.add(new Vec2(t.worldx(), t.worldy()));
                }
            } else if (set.unitType.flying) {
                for (int i = 0; i < count; i++) {
                    Tmp.v2.trns(Mathf.random(360f), Mathf.random(spawnRange * 0.9f)).add(target.x, target.y);
                    points.add(new Vec2(Tmp.v2.x, Tmp.v2.y));
                }
            }

            boolean success = points.size == count;
            if (success && !net.client()) {
                float angle = angleTo(target.x, target.y);
                for (int i = 0; i < points.size; i++) {
                    Spawner spawner = Pools.obtain(Spawner.class, Spawner::new);
                    spawner.init(set.unitType, team, points.get(i), angle, spawnReloadTime + i * spawnDelay);
                    if (commandPos != null) spawner.commandPos.set(commandPos);
                    spawner.add();
                }
            }

            if (success) {
                queueSnapType = set.unitType;
                queueSnapCount = count;
                queueSnapLoop = wasLoop;
                queueSnapProgress = 1f;
                queueSnapJammed = false;
                buildProgress = 0;
                spawnID = -1;
                jammed = false;
                cooling = true;
                releasing = true;
                buildingLoop = false;
                if (wasLoop) {
                    buildQueue.insert(0, new int[]{loopId, loopNum, 1});
                }
            } else {
                jammed = true;
            }
        }

        @Override
        public void drawConfigure() {
            Color color = getColor(getRecipe());
            Drawf.dashCircle(x, y, range(), color);
            Draw.color(color);
            Lines.square(x, y, block.size * tilesize / 2f + 1f);

            Vec2 target = link();
            Draw.alpha(1f);
            Drawf.dashCircle(target.x, target.y, spawnRange, color);
            DrawFunc.posSquareLink(color, 1.5f, 3.5f, true, this, target);
            Draw.color();

            if (core() != null && usesCoreItems()) {
                DrawFunc.posSquareLinkArr(color, 1.5f, 3.5f, true, false, this, core());
            }
            if (jammed) {
                DrawFunc.overlayText(Core.bundle.get("spawn-error"), x, y, size * tilesize / 2f, color, true);
            }
            Draw.reset();
        }

        @Override
        public void updateTableAlign(Table table) {
            if (usesCoreItems()) {
                super.updateTableAlign(table);
            } else {
                Vec2 pos = Core.input.mouseScreen(x - block.size * 4f - 1f, y);
                table.setPosition(pos.x, pos.y, Align.right);
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            BaseDialog dialog = new BaseDialog("@spawn");
            dialog.addCloseListener();

            float dialogW = dialogWidth(960f, 0.82f);
            float dialogH = dialogHeight(620f, 0.76f);
            float leftW = dialogW / 3f;
            float rightW = dialogW * 2f / 3f;
            float len = Mathf.clamp(ui(LEN), 42f, dialogH * 0.09f);

            if (selectID < 0 || selectID >= recipeList.size || hideRecipe(recipeList.get(selectID).unitType)) {
                for (int i = 0; i < recipeList.size; i++) {
                    if (!hideRecipe(recipeList.get(i).unitType)) {
                        selectID = i;
                        break;
                    }
                }
            }
            selectNum = Mathf.clamp(selectNum, 1, maxSpawnCount);

            dialog.cont.table(main -> {
                main.table(Styles.black3, left -> {
                    left.top();
                    left.table(Tex.pane, res -> {
                        res.top().left();
                        res.add("@mod.ui.core-resources").color(Pal.accent).left().pad(ui(6f)).row();
                        res.pane(list -> {
                            list.left().top();
                            tmpItems.clear();
                            for (UnitRecipe recipe : recipeList) {
                                for (ItemStack stack : recipe.recipe.inputItem) {
                                    tmpItems.add(stack.item);
                                }
                            }
                            int col = 0;
                            for (Item item : tmpItems) {
                                list.add(new ItemImageDynamic(item, () -> realItems().get(item), realItems())).pad(ui(4f));
                                if (++col % 4 == 0) list.row();
                            }
                        }).grow().pad(ui(4f));
                    }).growX().height(dialogH / 3f).pad(ui(4f)).row();

                    left.table(Tex.pane, queuePanel -> {
                        queuePanel.top();
                        queuePanel.add("@mod.ui.build-queue").color(Pal.accent).left().pad(ui(6f)).row();
                        Table queueList = new Table();
                        queueList.top();
                        int[] cache = {-999, -999, -999, -999};
                        boolean[] dragging = {false};
                        ObjectMap<Object, Table> rowMap = new ObjectMap<>();
                        ObjectSet<Object> animatingOut = new ObjectSet<>();
                        ObjectMap<Object, Integer> exitIndex = new ObjectMap<>();
                        Seq<Object> visualOrder = new Seq<>();
                        ObjectMap<Integer, int[]> activeModes = new ObjectMap<>();
                        float border = ui(2f);

                        Cons<Table> fillBuilding = host -> {
                            UnitRecipe cur = getRecipe();
                            if (cur == null) return;
                            int count = buildingSpawnNum;
                            boolean loop = buildingLoop;
                            host.stack(
                                    new Bar(
                                            () -> "",
                                            () -> jammed ? Pal.redderDust : Pal.accent,
                                            () -> buildProgress / Math.max(costTime(cur, true), 1f)
                                    ),
                                    new Table(row -> {
                                        row.left().marginLeft(8f).marginRight(8f);
                                        row.image(cur.unitType.uiIcon).size(ui(36f)).scaling(Scaling.fit).padRight(ui(8f));
                                        row.add(cur.unitType.localizedName + " x" + count).growX().left();
                                        if (loop) row.image(Icon.refresh).size(ui(24f)).padRight(ui(6f));
                                        row.add(new Label(() -> jammed ? "[red]" + Core.bundle.get("spawn-error") : "[accent]" + (int) Math.max((costTime(cur, true) - buildProgress) / Time.toSeconds, 0) + "s")).right();
                                    })
                            ).grow();
                        };

                        Cons<Table> fillReleasing = host -> {
                            host.clearChildren();
                            host.add(releasingBar(() -> Mathf.clamp(cooldown / Math.max(cooldownTime, 1f)))).grow();
                        };

                        Cons<Table> fillReleasingFull = host -> {
                            host.clearChildren();
                            host.add(releasingBar(() -> 1f)).grow();
                        };

                        Cons<Table> fillFrozen = host -> {
                            UnitType type = queueSnapType;
                            if (type == null) return;
                            float prog = queueSnapProgress;
                            boolean jam = queueSnapJammed;
                            int count = queueSnapCount;
                            boolean loop = queueSnapLoop;
                            host.clearChildren();
                            host.stack(
                                    new Bar(() -> "", () -> jam ? Pal.redderDust : Pal.accent, () -> prog),
                                    new Table(row -> {
                                        row.left().marginLeft(8f).marginRight(8f);
                                        row.image(type.uiIcon).size(ui(36f)).scaling(Scaling.fit).padRight(ui(8f));
                                        row.add(type.localizedName + " x" + count).growX().left();
                                        if (loop) row.image(Icon.refresh).size(ui(24f)).padRight(ui(6f));
                                        if (jam) row.add("[red]" + Core.bundle.get("spawn-error")).right();
                                        else row.add("[accent]0s").right();
                                    })
                            ).grow();
                        };

                        Cons2<Table, int[]> fillWaiting = (host, entry) -> {
                            UnitRecipe set = getRecipe(entry[0]);
                            if (set == null) return;
                            int count = entry[1];
                            boolean loop = isLoopEntry(entry);
                            int[] indexRef = new int[]{0};
                            host.userObject = indexRef;
                            host.stack(
                                    new Bar(() -> "", () -> Pal.darkerGray, () -> 0f),
                                    new Table(row -> {
                                        row.left().marginLeft(8f).marginRight(8f);
                                        Image handle = new Image(Icon.upOpen);
                                        handle.setColor(Pal.lightishGray);
                                        row.add(handle).size(ui(22f)).padRight(ui(6f));
                                        row.image(set.unitType.uiIcon).size(ui(36f)).scaling(Scaling.fit).padRight(ui(8f));
                                        row.add(set.unitType.localizedName + " x" + count).growX().left();
                                        if (loop) row.image(Icon.refresh).size(ui(24f)).padRight(ui(6f));
                                        row.button(Icon.cancel, Styles.clearNonei, () -> configure(IntSeq.with(2, indexRef[0]))).size(ui(32f));
                                    })
                            ).grow();
                            host.addListener(new HandCursorListener());
                            handleDrag(host, indexRef, dragging, () -> cache[0] = -999);
                        };

                        Runnable[] syncHold = {null};
                        Runnable syncQueue = () -> {
                            Seq<Object> desired = new Seq<>();
                            if (releasing || (isCalling() && getRecipe() != null)) {
                                desired.add(activeGen);
                            }
                            for (int[] e : buildQueue) desired.add(e);

                            Cons<Object> startExit = key -> {
                                if (animatingOut.contains(key)) return;
                                Table row = rowMap.get(key);
                                if (row == null) return;
                                int idx = visualOrder.size;
                                for (int i = 0; i < visualOrder.size; i++) {
                                    if (visualOrder.get(i) == key) {
                                        idx = i;
                                        break;
                                    }
                                }
                                exitIndex.put(key, idx);
                                animatingOut.add(key);
                                boolean finishRelease = key instanceof Integer && snapFromReleasing;
                                if (key instanceof Integer) {
                                    if (finishRelease) fillReleasingFull.get(row);
                                    else fillFrozen.get(row);
                                    activeModes.put((Integer) key, new int[]{3});
                                }
                                animateQueueOut(row, () -> {
                                    rowMap.remove(key);
                                    animatingOut.remove(key);
                                    exitIndex.remove(key);
                                    if (key instanceof Integer) activeModes.remove((Integer) key);
                                    row.remove();
                                    if (finishRelease) {
                                        snapFromReleasing = false;
                                        releaseExitLock = false;
                                    }
                                    cache[0] = -999;
                                    Core.app.post(() -> {
                                        if (finishRelease) tryStartQueue();
                                        if (syncHold[0] != null) syncHold[0].run();
                                    });
                                });
                            };

                            if (promotedEntry != null) {
                                Table promoted = rowMap.remove(promotedEntry);
                                promotedEntry = null;
                                if (promoted != null && (releasing || (isCalling() && getRecipe() != null))) {
                                    promoted.clearActions();
                                    promoted.clearChildren();
                                    promoted.clearListeners();
                                    promoted.color.a = 1f;
                                    promoted.translation.setZero();
                                    promoted.touchable = Touchable.enabled;
                                    Integer newKey = activeGen;
                                    rowMap.put(newKey, promoted);
                                    activeModes.put(newKey, new int[]{0});
                                } else if (promoted != null) {
                                    promoted.remove();
                                }
                            }

                            for (Object key : rowMap.keys().toSeq()) {
                                boolean keep = false;
                                for (Object d : desired) {
                                    if (d == key) {
                                        keep = true;
                                        break;
                                    }
                                }
                                if (!keep) startExit.get(key);
                            }

                            ObjectSet<Object> created = new ObjectSet<>();
                            for (Object key : desired) {
                                if (!rowMap.containsKey(key)) {
                                    Table row = new Table();
                                    rowMap.put(key, row);
                                    created.add(key);
                                    if (key instanceof Integer) {
                                        activeModes.put((Integer) key, new int[]{0});
                                    } else {
                                        fillWaiting.get(row, (int[]) key);
                                    }
                                }
                            }

                            if (releasing || (isCalling() && getRecipe() != null)) {
                                Integer curKey = activeGen;
                                Table active = rowMap.get(curKey);
                                int[] mode = activeModes.get(curKey);
                                if (active != null && mode != null) {
                                    int want = releasing ? 2 : 1;
                                    if (mode[0] != want) {
                                        mode[0] = want;
                                        active.clearChildren();
                                        if (want == 2) fillReleasing.get(active);
                                        else fillBuilding.get(active);
                                    }
                                }
                            }

                            for (Object key : desired) {
                                if (!(key instanceof int[])) continue;
                                Table row = rowMap.get(key);
                                if (row == null || !(row.userObject instanceof int[])) continue;
                                int qi = -1;
                                for (int i = 0; i < buildQueue.size; i++) {
                                    if (buildQueue.get(i) == key) {
                                        qi = i;
                                        break;
                                    }
                                }
                                if (qi >= 0) ((int[]) row.userObject)[0] = qi;
                            }

                            Seq<Object> visual = new Seq<>(desired);
                            Seq<Object> exits = animatingOut.toSeq();
                            exits.sort((a, b) -> Integer.compare(exitIndex.get(a, 999), exitIndex.get(b, 999)));
                            for (Object e : exits) {
                                int idx = exitIndex.get(e, visual.size);
                                visual.insert(Math.min(idx, visual.size), e);
                            }

                            queueList.clearChildren();
                            visualOrder.clear();
                            for (Object key : visual) {
                                Table row = rowMap.get(key);
                                if (row == null) continue;
                                queueList.add(row).growX().height(len).padBottom(ui(4f)).row();
                                visualOrder.add(key);
                                if (created.contains(key)) animateQueueIn(row);
                            }
                            if (desired.isEmpty() && animatingOut.isEmpty()) {
                                queueList.add("[lightgray]" + Core.bundle.get("none")).pad(ui(8f));
                            }
                        };

                        syncHold[0] = syncQueue;
                        syncHold[0].run();
                        queuePanel.pane(queueList).grow().pad(ui(4f)).update(p -> {
                            if (dragging[0]) return;
                            int a = spawnID;
                            int b = buildQueue.size;
                            int c = isCalling() ? buildingSpawnNum : -1;
                            int d = releasing ? 1 : 0;
                            for (int[] e : buildQueue) {
                                d = d * 31 + e[0] * 17 + e[1] + (isLoopEntry(e) ? 13 : 0);
                            }
                            d = d * 31 + (buildingLoop ? 1 : 0) + (jammed ? 7 : 0);
                            if (a != cache[0] || b != cache[1] || c != cache[2] || d != cache[3]) {
                                cache[0] = a;
                                cache[1] = b;
                                cache[2] = c;
                                cache[3] = d;
                                syncHold[0].run();
                            }
                        });
                        queuePanel.row();
                        queuePanel.button("@mod.ui.clear-wait-queue", Icon.trash, Styles.cleart, () -> configure(IntSeq.with(1, 0))).growX().height(len - ui(8f)).pad(ui(4f))
                                .disabled(b -> buildQueue.isEmpty());
                    }).grow().pad(ui(4f));
                }).size(leftW, dialogH).padRight(ui(6f));

                main.table(Styles.black3, right -> {
                    float cardW = rightW - ui(40f);
                    float cardH = Mathf.clamp(ui(86f), 64f, dialogH * 0.16f);
                    float border = ui(2f);
                    arc.scene.ui.layout.Cell<ScrollPane> paneCell = right.pane(grid -> {
                        grid.top().left();
                        for (int i = 0; i < recipeList.size; i++) {
                            UnitRecipe set = recipeList.get(i);
                            if (hideRecipe(set.unitType)) continue;
                            int idx = i;
                            grid.stack(
                                    new Table(Tex.pane, card -> {
                                        card.margin(ui(8f));
                                        card.touchable = Touchable.enabled;
                                        card.addListener(new HandCursorListener());
                                        card.clicked(() -> selectID = idx);
                                        card.table(body -> {
                                            body.table(info -> {
                                                info.left().top();
                                                info.table(header -> {
                                                    header.left();
                                                    header.image(set.unitType.uiIcon).size(ui(40f)).scaling(Scaling.fit).padRight(ui(8f));
                                                    header.add(set.unitType.localizedName).growX().left().wrap().labelAlign(Align.left);
                                                    header.add(new Label(() -> {
                                                        float sec = set.costTime() * selectNum / speedMultiplier(selectNum) / 60f / state.rules.unitBuildSpeedMultiplier;
                                                        return "[lightgray]" + Core.bundle.get("stat.buildtime") + ": [accent]" + Strings.fixed(sec, 1) + "[]" + Core.bundle.get("unit.seconds");
                                                    })).right().padLeft(ui(8f));
                                                }).growX().left().row();
                                                info.table(req -> {
                                                    req.left();
                                                    for (ItemStack stack : set.dynamicRequirements(team)) {
                                                        req.add(new ItemImageDynamic(stack.item, () -> Math.round(stack.amount * selectNum), realItems())).padRight(ui(4f));
                                                    }
                                                    for (PayloadStack stack : set.recipe.inputPayload) {
                                                        req.add(StatValues.stack(stack.item, Math.round(stack.amount * selectNum * state.rules.unitCost(team)), true)).padRight(ui(4f));
                                                    }
                                                }).growX().left().padTop(ui(6f));
                                            }).grow().left();

                                            body.image().width(border).color(Pal.gray).growY().padLeft(ui(6f)).padRight(ui(6f));
                                            body.button(Icon.info, Styles.clearNonei, () -> ui.content.show(set.unitType)).size(len).growY();
                                        }).grow();
                                    }),
                                    new Table(outline -> {
                                        outline.touchable = Touchable.disabled;
                                        outline.visible(() -> selectID == idx);
                                        outline.top().defaults().pad(0f);
                                        outline.image().color(Pal.accent).height(border).growX().row();
                                        outline.table(mid -> {
                                            mid.image().color(Pal.accent).width(border).growY();
                                            mid.add().grow();
                                            mid.image().color(Pal.accent).width(border).growY();
                                        }).grow().row();
                                        outline.image().color(Pal.accent).height(border).growX();
                                    })
                            ).size(cardW, cardH).pad(ui(6f)).padRight(ui(28f)).left().row();
                        }
                    }).grow().pad(ui(4f));
                    ScrollPane unitPane = paneCell.get();
                    unitPane.setScrollingDisabled(true, false);
                    unitPane.setFadeScrollBars(false);
                }).size(rightW, dialogH);
            }).size(dialogW, dialogH).row();

            dialog.cont.table(bottom -> {
                Label amountLabel = new Label("");
                Slider slider = new Slider(1, Mathf.clamp(Units.getCap(team), 1, maxSpawnCount), 1, false);
                slider.setValue(selectNum);
                slider.addListener(new HandCursorListener());
                slider.moved(v -> selectNum = (int) v);
                bottom.update(() -> {
                    UnitRecipe selected = getRecipe(selectID);
                    String name = selected == null ? Core.bundle.get("none") : selected.unitType.localizedName;
                    amountLabel.setText("[gray]" + Core.bundle.get("filter.option.amount") + ": [accent]" + selectNum +
                            "[] | " + name + " | " + Core.bundle.get("stat.buildspeedmultiplier") + ": [accent]" + Strings.fixed(speedMultiplier(selectNum), 2));
                    if (Math.abs(slider.getValue() - selectNum) > 0.01f) slider.setValue(selectNum);
                });
                bottom.add(new Stack(slider, new Table(t -> {
                    t.center();
                    t.add(amountLabel);
                }))).growX().height(len).pad(ui(6f)).row();

                bottom.table(actions -> {
                    actions.button("@back", Icon.left, Styles.cleart, dialog::hide).growX().height(len);
                    actions.button("@mod.ui.force-abort", Icon.cancel, Styles.cleart, () -> configure(false)).growX().height(len)
                            .disabled(b -> !isCalling());
                    actions.button("@mod.ui.infinite-prod", Icon.refresh, Styles.cleart, () -> {
                        if (selectID < 0) return;
                        configure(IntSeq.with(0, selectID, selectNum, 1));
                    }).growX().height(len).disabled(b -> {
                        UnitRecipe set = getRecipe(selectID);
                        return set == null || !canSpawn(set, selectNum) || !hasConsume(set, selectNum);
                    });
                    actions.button("@confirm", Icon.ok, Styles.cleart, () -> {
                        if (selectID < 0) return;
                        configure(IntSeq.with(0, selectID, selectNum));
                    }).growX().height(len).disabled(b -> {
                        UnitRecipe set = getRecipe(selectID);
                        return set == null || !canSpawn(set, selectNum) || !hasConsume(set, selectNum);
                    });
                }).growX().height(len);
            }).width(dialogW).padTop(ui(6f));

            dialog.keyDown(c -> {
                if (c == KeyCode.left) selectNum = Mathf.clamp(--selectNum, 1, Mathf.clamp(Units.getCap(team), 1, maxSpawnCount));
                if (c == KeyCode.right) selectNum = Mathf.clamp(++selectNum, 1, Mathf.clamp(Units.getCap(team), 1, maxSpawnCount));
                if (c == KeyCode.enter && selectID >= 0) configure(IntSeq.with(0, selectID, selectNum));
            });

            table.table(Tex.paneSolid, t -> {
                t.button("@spawn", Icon.add, Styles.cleart, dialog::show).size(len * 5, len).row();
                t.button("@mod.ui.select-target", Icon.move, Styles.cleart, () -> TableFunc.selectPos(table, this::configure)).size(len * 5, len).row();
                t.button("@mod.ui.force-abort", Icon.cancel, Styles.cleart, () -> configure(false)).size(len * 5, len).disabled(b -> !isCalling());
            }).fill();
        }

        @Override
        public void draw() {
            super.draw();
            blockDrawer.get(this, getType() != null && getCommandPosition() != null);

            TextureRegion arrowRegion = NHContent.arrowRegion;
            float sizeScl = block.size / 5f;
            float scl = warmup * atlasSizeScl * sizeScl;
            if (isCalling()) {
                Draw.z(Layer.bullet);
                Draw.color(getColor(getRecipe()));
                for (int l = 0; l < 4; l++) {
                    float angle = 45 + 90 * l;
                    float reg = regSize(getType()) / 3f + Draw.scl;
                    for (int i = 0; i < 4; i++) {
                        Tmp.v1.trns(angle, (i - 4) * tilesize * 2 * sizeScl);
                        float f = (100 - (totalProgress - 25 * i) % 100) / 100;
                        Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, arrowRegion.width * reg * f * scl, arrowRegion.height * reg * f * scl, angle - 90);
                    }
                }
                if (jammed || !Units.canCreate(team, getType())) {
                    float signSize = 0.75f + Mathf.absin(totalProgress + 8f, 8f, 0.15f);
                    for (int i = 0; i < 4; i++) {
                        Draw.rect(arrowRegion, x, y, arrowRegion.width * Draw.scl * signSize * scl, arrowRegion.height * Draw.scl * signSize * scl, 90 * i);
                    }
                }
                DrawFunc.circlePercent(x, y, progressCircleRadius(), buildProgress / costTime(getRecipe(), true), 0);
            }
            Draw.reset();
            Drawf.light(tile, size * tilesize * 4 * warmup, team.color, 0.95f);
        }

        @Override
        public void displayBars(Table table) {
            super.displayBars(table);
            table.row().table(t -> {
                t.left();
                t.label(() -> "[lightgray]Constructing: [accent]" + (getType() == null ? Core.bundle.get("none") : getType().localizedName)).pad(OFFSET / 2f);
                t.image(() -> getType() == null ? Icon.cancel.getRegion() : getType().uiIcon).size(LEN - OFFSET).scaling(Scaling.fit);
            }).growX().fillY().visible(this::isCalling);
        }

        @Override
        public float range() {
            return maxRadius;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float progress() {
            return isCalling() ? buildProgress / costTime(getRecipe(), true) : 0;
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        public Vec2 link() {
            Tile t = world.tile(linkPos());
            if (t == null) return linkVec.set(this);
            return linkVec.set(t);
        }

        public int linkPos() {
            return link;
        }

        public void linkPos(Point2 point2) {
            Tile tile = world.tile(point2.x, point2.y);
            if (tile != null && tile.within(this, range())) {
                link = point2.pack();
            } else if (tile != null) {
                Tmp.v1.set(tile).sub(this).nor().scl(range());
                link = point.set((int) World.conv(x + Tmp.v1.x), (int) World.conv(y + Tmp.v1.y)).pack();
            } else {
                link = pos();
            }
        }

        @Override
        public byte version() {
            return 6;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(spawnID);
            write.i(link);
            write.f(buildProgress);
            write.f(warmup);
            write.i(buildingSpawnNum);
            write.bool(cooling);
            write.f(cooldown);
            write.i(spawnNum);
            write.bool(buildingLoop);
            write.bool(releasing);
            write.i(buildQueue.size);
            for (int[] entry : buildQueue) {
                write.i(entry[0]);
                write.i(entry[1]);
                write.i(isLoopEntry(entry) ? 1 : 0);
            }
            TypeIO.writeVec2(write, commandPos == null ? new Vec2(Float.NaN, Float.NaN) : commandPos);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 6) {
                spawnID = read.i();
                link = read.i();
                buildProgress = read.f();
                warmup = read.f();
                buildingSpawnNum = read.i();
                cooling = read.bool();
                cooldown = read.f();
                spawnNum = read.i();
                buildingLoop = read.bool();
                releasing = read.bool();
                buildQueue.clear();
                int qsize = read.i();
                for (int i = 0; i < qsize; i++) {
                    buildQueue.add(new int[]{read.i(), read.i(), read.i()});
                }
                Vec2 v = TypeIO.readVec2(read);
                commandPos = (Float.isNaN(v.x) || Float.isNaN(v.y)) ? null : v;
            } else if (revision >= 5) {
                spawnID = read.i();
                link = read.i();
                buildProgress = read.f();
                warmup = read.f();
                buildingSpawnNum = read.i();
                cooling = read.bool();
                cooldown = read.f();
                spawnNum = read.i();
                buildingLoop = read.bool();
                releasing = false;
                buildQueue.clear();
                int qsize = read.i();
                for (int i = 0; i < qsize; i++) {
                    buildQueue.add(new int[]{read.i(), read.i(), read.i()});
                }
                Vec2 v = TypeIO.readVec2(read);
                commandPos = (Float.isNaN(v.x) || Float.isNaN(v.y)) ? null : v;
            } else if (revision >= 4) {
                spawnID = read.i();
                link = read.i();
                buildProgress = read.f();
                warmup = read.f();
                buildingSpawnNum = read.i();
                cooling = read.bool();
                cooldown = read.f();
                spawnNum = read.i();
                buildingLoop = false;
                buildQueue.clear();
                int qsize = read.i();
                for (int i = 0; i < qsize; i++) {
                    buildQueue.add(new int[]{read.i(), read.i(), 0});
                }
                Vec2 v = TypeIO.readVec2(read);
                commandPos = (Float.isNaN(v.x) || Float.isNaN(v.y)) ? null : v;
            } else if (revision >= 3) {
                spawnID = read.i();
                link = read.i();
                buildProgress = read.f();
                warmup = read.f();
                buildingSpawnNum = read.i();
                cooling = read.bool();
                cooldown = read.f();
                read.i();
                read.i();
                spawnNum = read.i();
                Vec2 v = TypeIO.readVec2(read);
                commandPos = (Float.isNaN(v.x) || Float.isNaN(v.y)) ? null : v;
            } else if (revision == 2) {
                read.f();
                buildProgress = read.f();
                spawnID = read.i();
                Vec2 v = TypeIO.readVec2(read);
                commandPos = (Float.isNaN(v.x) || Float.isNaN(v.y)) ? null : v;
            }
        }
    }
}
