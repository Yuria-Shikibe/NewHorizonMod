package newhorizon.block.special;

import arc.Core;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.TextArea;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.core.World;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.*;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.ItemModule;
import newhorizon.NewHorizon;
import newhorizon.content.NHFx;
import newhorizon.content.NHLoader;
import newhorizon.func.DrawFuncs;
import newhorizon.func.Functions;
import newhorizon.func.TableFs;
import newhorizon.func.Tables;
import newhorizon.vars.NHCtrlVars;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static mindustry.Vars.*;
import static newhorizon.func.Functions.regSize;
import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;

public class JumpGate extends Block {
    protected static final ObjectMap<UnitSet, Integer> allSets = new ObjectMap<>();
    protected static final Seq<UnitSet> all = new Seq<>();
    
    public boolean adaptable = false;
    @Nullable public Block adaptBase = null;
    public float spawnDelay = 5f;
    public float spawnReloadTime = 180f;
    public float spawnRange = 120f;
    public float range = 200f;
    public float atlasSizeScl = 1;
    public float basePowerDraw = 2f;
    public TextureRegion
            pointerRegion,
            arrowRegion;
    public Color baseColor;
    public final Seq<UnitSet> calls = new Seq<>();
    public float squareStroke = 2f;
    
    protected final Vec2 linkP = new Vec2();
    
//    static{
//        EventTriggers.addActor(JumpGate.JumpGateBuild.class, (JumpGate.JumpGateBuild tile, Tile i) -> {
//            tile.configure(Tmp.p1.set(i.x, i.y));
//        });
//    }
    
    public JumpGate(String name){
        super(name);
        update = true;
        configurable = true;
        solid = true;
        hasPower = true;
        timers = 3;
        category = Category.units;
        consumes.powerCond(basePowerDraw, (JumpGateBuild b) -> !b.isCalling());
        consumes.powerCond(consumes.getPower().usage, JumpGateBuild::isCalling);
    
        config(IntSeq.class, JumpGateBuild::parsing);
        config(Point2.class, (Cons2<JumpGateBuild, Point2>)JumpGateBuild::linkPos);
        config(Integer.class, (JumpGateBuild tile, Integer i) -> {
            if(i < 0 || !tile.isCalling() || tile.getSet() == null)tile.startBuild(i);
            else tile.spawn(calls.get(i));
        });
    }
    
    public boolean canReplace(Block other) {
        return super.canReplace(other) || other instanceof JumpGate && size > other.size;
    }
    
    public boolean canPlaceOn(Tile tile, Team team) {
        if(adaptBase == null || state.rules.infiniteResources)return true;
        if (tile == null) {
            return false;
        } else {
            return tile.block() instanceof JumpGate && adaptBase == tile.block();
        }
    }
    
    public void placeBegan(Tile tile, Block previous) {
        if (previous instanceof JumpGate) {
            tile.setBlock(this, tile.team());
            Fx.placeBlock.at(tile, (float)tile.block().size);
            Fx.upgradeCore.at(tile, (float)tile.block().size);
        }
    }
    
    public void beforePlaceBegan(Tile tile, Block previous) {
        if(tile == null || tile.build == null || tile.build.core() == null)return;
        ItemModule items = tile.build.core().items();
        if (!Vars.state.rules.infiniteResources && items != null) {
            items.remove(ItemStack.mult(requirements, Vars.state.rules.buildCostMultiplier));
        }
    }
    
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        Color color = baseColor == null ? Pal.accent : baseColor;
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, color);
        if (Vars.world.tile(x, y) != null) {
            if (!canPlaceOn(Vars.world.tile(x, y), Vars.player.team())) {
                drawPlaceText(Core.bundle.get((Vars.player.team().core() == null || !Vars.player.team().core().items.has(requirements, Vars.state.rules.buildCostMultiplier)) && !Vars.state.rules.infiniteResources ? "bar.noresources" : "nh-need-base"), x, y, valid);
            }
        }
    }
    
    public void addSets(UnitSet... sets){
        calls.addAll(sets);
    }

    @Override
    public void init(){
        super.init();
        if(calls.isEmpty()) throw new IllegalArgumentException("Seq @calls is [red]EMPTY[].");
        for(UnitSet set : calls){
            allSets.put(set, size);
        }
        
        if(adaptable)for(UnitSet set : allSets.keys()){
            if(allSets.get(set) >= size)continue;
            calls.add(set);
        }
        
        calls.sort((set1, set2) -> set1.sortIndex[0] - set2.sortIndex[0] == 0 ? set1.sortIndex[1] - set2.sortIndex[1] : set1.sortIndex[0] - set2.sortIndex[0]);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.powerUse, basePowerDraw * 60F, StatUnit.powerSecond);
        if(adaptBase != null)stats.add(Stat.abilities, t -> {
            t.row().add(Core.bundle.get("toolmode.replace") + ":").left().pad(OFFSET).row();
            t.image(adaptBase.icon(Cicon.full)).size(LEN).padLeft(OFFSET).row();
        });
        stats.add(Stat.output, (t) -> {
            t.row().add(Core.bundle.get("editor.spawn") + ":").left().pad(OFFSET).row();
            for(UnitSet set : calls) {
                t.add(new Tables.UnitSetTable(set, table -> table.button(Icon.infoCircle, Styles.clearPartiali, () -> showInfo(set, new Label("[accent]Caution[gray]: Summon needs building."), null)).size(LEN))).fill().row();
            }
        });
    }

    public void showInfo(UnitSet set, Element extra, @Nullable ItemModule module){
        BaseDialog dialogIn = new BaseDialog("More Info");
        dialogIn.addCloseListener();
        dialogIn.cont.margin(15f);
        dialogIn.cont.pane(inner -> {
            inner.image(set.type.icon(Cicon.full)).center().row();
            inner.image().growX().height(OFFSET / 4).pad(OFFSET / 4f).color(Pal.accent).row();
            inner.add("[lightgray]" + Core.bundle.get("editor.spawn") + ": [accent]" + set.type.localizedName + "[lightgray] | Tier: [accent]" + set.sortIndex[1]).left().padLeft(OFFSET).row();
            inner.add("[lightgray]" + Core.bundle.get("waves.perspawn") + ": [accent]" + set.callIns).left().padLeft(OFFSET).row();
            inner.add("[lightgray]" + Core.bundle.get("stat.buildtime") + ": [accent]" + TableFs.format(set.costTimeVar() / 60) + "[lightgray] " + Core.bundle.get("unit.seconds")).left().padLeft(OFFSET).row();
            inner.image().growX().height(OFFSET / 4).pad(OFFSET / 4f).color(Pal.accent).row();
            inner.table(table -> {
                int index = 0;
                for(ItemStack stack : set.requirements()){
                    if(module != null || index % 7 == 0)table.row();
                    if(module != null){
                        TableFs.itemStack(table, stack, module);
                    }else table.add(new ItemDisplay(stack.item, stack.amount, false).left()).padLeft(OFFSET / 2).left();
                    index ++;
                }
            }).growX().fillY().left().padLeft(OFFSET).row();
            inner.image().growX().pad(OFFSET / 4f).height(OFFSET / 4).color(Pal.accent).row();
            inner.add(extra).left().padLeft(OFFSET).row();
            inner.button("@back", Icon.left, Styles.cleart, dialogIn::hide).size(LEN * 3f, LEN).pad(OFFSET);
        }).grow().row();
        dialogIn.show();
    }

    @Override
    public void setBars() {
        super.setBars();
        bars.add("progress",
            (JumpGateBuild entity) -> new Bar(
                () -> entity.isCalling() ?
                        Core.bundle.get("bar.progress") : "[lightgray]" + Iconc.cancel,
                () -> entity.isCalling() && entity.hasConsume(entity.getSet()) ? Pal.power : Pal.redderDust,
                () -> entity.isCalling() ? entity.buildReload / entity.getSet().costTime() : 0
            )
        );
    }

    @Override
    public void load(){
        super.load();
        pointerRegion = Core.atlas.find(NewHorizon.configName("jump-gate-pointer"));
        arrowRegion = Core.atlas.find(NewHorizon.configName("jump-gate-arrow"));
    }

    public class JumpGateBuild extends Building implements Ranged{
        public Color baseColor(){
            return baseColor == null ? team().color : baseColor;
        }
        public int spawnID = -1;
        public int link = -1;
        public float buildReload = 0f;
        public float progress;
        
        protected int performPlanIndex = -1;
        protected boolean success = true, error;
        protected float warmup;
        
        protected ObjectMap<Integer, Integer> spawnPlan = new ObjectMap<>();
    
        @Override
        public boolean onConfigureTileTapped(Building other){
            if (this == other || linkPos() == other.pos()) {
                configure(Tmp.p1.set(-1, -1));
            }
            return false;
        }
    
        @Override
        public void updateTile(){
            if(hasConsume(getSet()))progress += (efficiency() + warmup) * delta() * Mathf.clamp(Time.delta, 0.5f, 0.75f);
            if(isCalling() && hasConsume(getSet())){
                buildReload += efficiency() * Vars.state.rules.unitBuildSpeedMultiplier * delta();
                if(buildReload >= getSet().costTime() && hasConsume(getSet()) && !error){
                    configure(spawnID);
                }
            }
            
            if(efficiency() > 0){
                if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
                else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
            }else{
                if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
                else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
            }
    
            if(timer(2, 20))for(int boolf : spawnPlan.keys()){
                if(boolf <= state.wave){
                    int index = spawnPlan.get(boolf);
                    UnitSet set = calls.get(index);
                    if(canSpawn(set) && !isCalling() && !error){
                        IntSeq newSeq = config();
                        int i = spawnPlan.keys().toSeq().indexOf(boolf);
                        newSeq.removeRange(i * 2, i * 2 + 1);
                        spawnPlan.clear();
                        parsing(newSeq);
                        startBuild(index);
                        performPlanIndex = boolf;
                        break;
                    }
                }
            }
        }

        public Color getColor(UnitSet set){
            if(set == null)return baseColor();
            return (error || !hasConsume(getSet())) ? baseColor().cpy().lerp(Pal.ammo, 1 / Mathf.clamp((efficiency() + 1), 0, 2)) : baseColor();
        }
    
        public void parsing(IntSeq input){
            if(input.size == 0 || input.size % 2 == 1){
                spawnPlan.clear();
                return;
            }
            ObjectMap<Integer, Integer> cpy = spawnPlan.copy();
            for(int i = 0; i < input.size; i += 2){
                if(i + 1 > input.size){
                    ui.showErrorMessage("Not applicable to this build.");
                    spawnPlan = cpy;
                    break;
                }
                int j = input.get(i + 1);
                if(j < 0 || j >= calls.size){
                    ui.showErrorMessage("Not applicable to this build.");
                    spawnPlan = cpy;
                    break;
                }
                spawnPlan.put(input.get(i), j);
            }
//
//            Seq<Integer> sorted = spawnPlan.keys().toSeq().sort(Comparator.comparingInt(Integer::intValue));
//            Log.info(sorted);
//            cpy.clear();
//            for(int i : sorted){
//                cpy.put(i, spawnPlan.get(i));
//            }
//            spawnPlan = cpy;
        }
        
        @Override
        public void drawConfigure() {
            Color color = getColor(getSet());
            Drawf.dashCircle(x, y, range(), color);
            Draw.color(color);
            Lines.square(x, y, block().size * tilesize / 2f + 1.0f);
   
            Vec2 target = link();
            Draw.alpha(1f);
            Drawf.dashCircle(target.x, target.y, spawnRange, color);

            float angle = Tmp.v1.angle();
            
            Draw.color(Pal.gray);
            DrawFuncs.posSquareLink(color, 1.5f, 3.5f, true, this, target);
            Draw.color();
            
            if(core() != null)DrawFuncs.posSquareLinkArr(color, 1.5f, 3.5f, true, false, this, core());
            
            if(error)DrawFuncs.overlayText(Core.bundle.get("spawn-error"), x, y, size * tilesize / 2.0F, color, true);
            
            Draw.reset();
        }

        @Override
        public void buildConfiguration(Table table) {
            BaseDialog dialog = new BaseDialog("@spawn");
            dialog.addCloseListener();

            dialog.cont.pane(inner ->
                inner.table(callTable -> {
                    for(UnitSet set : calls) {
                        callTable.table(Tex.pane, info -> {
                            info.add(new Tables.UnitSetTable(set, table2 -> {
                                Label can = new Label("");
                                table2.update(() -> can.setText("[lightgray]Can Spawn?: " + TableFs.getJudge(canSpawn(set))));
                                table2.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(set, can, coreValid() ? core().items : null)).size(LEN);
                                table2.button(Icon.add, Styles.clearPartiali, () -> configure(calls.indexOf(set))).size(LEN).disabled(b -> !canSpawn(set) || error || isCalling());
                            })).fillY().growX().row();
                            Bar unitCurrent = new Bar(
                                    () -> Core.bundle.format("bar.unitcap",
                                            Fonts.getUnicodeStr(set.type.name),
                                            team.data().countType(set.type),
                                            Units.getCap(team)
                                        ),
                                    () -> Units.getCap(team) - team.data().countType(set.type) - set.callIns <= 0 ? Pal.redderDust : Pal.power,
                                    () -> (float)team.data().countType(set.type) / Units.getCap(team)
                            );
                            info.add(unitCurrent).growX().height(LEN - OFFSET);
                        }).fillY().growX().padTop(OFFSET).row();
                    }
                }).grow()
            ).grow().row();
            
            dialog.cont.add(new Bar(
                    () -> isCalling() ? hasConsume(getSet()) && !error ? "[lightgray]" + Core.bundle.get("editor.spawn") + ": [accent]" + getSet().type.localizedName + "[lightgray] | " + Core.bundle.get("ui.remain-time") + ": [accent]" + (int)(((getSet().costTime() - buildReload) / Time.toSeconds) / state.rules.unitBuildSpeedMultiplier) + "[lightgray] " + Core.bundle.get("unit.seconds") : "[red]Call Jammed"
                        : "[lightgray]" + Iconc.cancel,
                    () -> isCalling() && hasConsume(getSet()) && !error ? Pal.power : Pal.redderDust,
                    () -> isCalling() ? buildReload / getSet().costTime() : 0
            )).fillX().height(LEN).padTop(OFFSET / 2).row();
            dialog.cont.table(t -> {
                t.button("@cancel", Icon.cancel, Styles.cleart, () -> configure(-1)).padTop(OFFSET / 2).disabled(b -> !isCalling()).growX().height(LEN);
                t.button("@release", Icon.add, Styles.cleart, () -> configure(spawnID)).padTop(OFFSET / 2).disabled(b -> getSet() == null || success || !hasConsume(getSet()) || !canSpawn(getSet())).growX().height(LEN);
                t.button("@back", Icon.left, Styles.cleart, dialog::hide).padTop(OFFSET / 2).growX().height(LEN);
            }).growX().height(LEN);
            
            table.table(Tex.paneSolid, t -> {
                t.button("@spawn", Icon.add, Styles.cleart, dialog::show).size(LEN * 5, LEN).row();
                t.button(Core.bundle.get("spawn") + " " + Core.bundle.get("settings"), Icon.settings, Styles.cleart, () -> new BaseDialog("Planning"){{
                    addCloseListener();
                    Table plan = new Table();
                    updateInfoTable(plan);
                    
                    cont.add().growX().expandX().height(1f).row();
                    
                    cont.table(inner -> {
                        inner.pane(t -> {
                            t.top();
                            AtomicInteger wave = new AtomicInteger(state.wave);
                            Label l = new Label("");
                            
                            keyDown((key) -> {
                                if(key == KeyCode.left)wave.set(Math.max(wave.get() - 1, state.wave));
                                if(key == KeyCode.right)wave.set(wave.get() + 1);
                            });
                            
                            t.update(() -> l.setText("Target Wave: " + wave.get()));
                            t.table(lable -> {
                                lable.table(lable2 -> {
                                    lable2.button(Icon.leftOpen, Styles.clearPartiali, () -> wave.set(Math.max(wave.get() - 1, state.wave))).size(LEN).padLeft(OFFSET / 3);
                                    lable2.button(Icon.rightOpen, Styles.clearPartiali, () -> wave.set(wave.get() + 1)).size(LEN).padLeft(OFFSET / 3);
                                    TextArea ta = new TextArea("");
                                    lable2.add(ta).size(LEN * 2f, LEN).padLeft(OFFSET / 3);
                                    lable2.button(Icon.ok, Styles.clearPartiali, () -> {
                                        try{
                                            wave.set(Math.max(Integer.parseInt(ta.getText()), state.wave));
                                        }catch(NumberFormatException e){
                                            ta.clear();
                                            ui.showErrorMessage(e.getMessage());
                                        }
                                    }).size(LEN).padLeft(OFFSET / 3);
    
                                    lable2.button("@delete", Icon.cancel, Styles.transt, () -> {
                                        configure(IntSeq.with());
                                        updateInfoTable(plan);
                                    }).size(LEN * 3, LEN).padLeft(OFFSET / 3);
                                }).growX().row();
                                lable.add(l).left().row();
                            }).row();
                
                            t.slider(state.wave,  state.wave + 300, 1, 0, false, f -> wave.set((int)f)).update(s -> s.setValue(wave.get())).growX().height(LEN).row();
                
                            for(UnitSet set : calls) {
                                t.table(Tex.pane, info -> info.add(new Tables.UnitSetTable(set, table2 -> {
                                    Label can = new Label("");
                                    table2.button(Icon.add, Styles.clearPartiali, () -> {
                                        configure(IntSeq.with(wave.get(), calls.indexOf(set)));
                            
                                        updateInfoTable(plan);
                                    }).disabled(b -> spawnPlan.keys().toSeq().contains(wave.get())).size(LEN);
                                })).fillY().growX().row()).fillY().growX().padTop(OFFSET).row();
                            }
                        }).growX().left().growY();
                        inner.image().growY().width(OFFSET / 3).pad(OFFSET / 3).center().color(Color.gray);
                        inner.pane(plan.top()).growX().right().growY();
                    }).grow().row();
        
                    cont.table(t -> {
                        t.button("@back", Icon.left, Styles.cleart, this::hide).padTop(OFFSET / 2).growX().height(LEN);
                        t.button("@waves.copy", Icon.export, Styles.cleart, () -> {
                            StringBuilder builder = new StringBuilder();
                            for(int i : spawnPlan.keys()){
                                builder.append("-").append(i).append("-").append(calls.get(spawnPlan.get(i)).hashCode());
                            }
                            Core.app.setClipboardText(builder.toString().replaceFirst("-", ""));
                        }).padTop(OFFSET / 2).growX().height(LEN);
                        t.button("@waves.load", Icon.copy, Styles.cleart, () -> {
                            String[] text = Core.app.getClipboardText().split("-");
                            IntSeq seq = new IntSeq(text.length);
                            try{
                                int index = 0;
                                for(String s : text){
                                    if(s.equals(""))continue;
                                    int num = Integer.parseInt(s);
                                    if(index % 2 == 0){
                                        seq.add(num);
                                    }else{
                                        addLoop:{
                                            for(int i = 0; i < calls.size; i++){
                                                if(calls.get(i).hashCode() == num){
                                                    seq.add(i);
                                                    break addLoop;
                                                }
                                            }
                                            seq.add(-1);
                                        }
                                    }
                                    index ++;
                                }
                            }catch(NumberFormatException e){ui.showErrorMessage(e.getMessage());}
                            Log.info(seq);
                            configure(seq);
                            updateInfoTable(plan);
                        }).padTop(OFFSET / 2).growX().height(LEN);
                    }).growX().height(LEN);
        
                }}.show()).size(LEN * 5, LEN).row();
                t.button("@mod.ui.select-target", Icon.move, Styles.cleart, () -> TableFs.pointSelectTable(table, this::configure)).disabled(b -> NHCtrlVars.isSelecting).size(LEN * 5, LEN);
            }).fill();
        }

        protected void updateInfoTable(Table table){
            table.clear();
            for(int i : spawnPlan.keys().toSeq().sort(Comparator.comparingInt(Integer::intValue))){
                UnitSet unitSet = calls.get(spawnPlan.get(i));
                table.table(setTable -> {
                    setTable.background(Tex.paneSolid);
            
                    TableFs.tableImageShrink(unitSet.type.icon(Cicon.full), LEN, setTable.left());
                    setTable.add("Wave: " + i).growX();
                    setTable.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(unitSet, new Label("[lightgray]Can Spawn?: " + TableFs.getJudge(canSpawn(unitSet))), core().items)).size(LEN).padLeft(LEN);
                    setTable.button(Icon.cancel, Styles.clearPartiali, () -> {
                        spawnPlan.remove(i);
                        setTable.remove();
                    }).size(LEN);
                
                }).fillY().growX().padTop(OFFSET).row();
            }
        }
    
        @Override
        public IntSeq config(){
            IntSeq data = new IntSeq(spawnPlan.size * 2);
            for(int i : spawnPlan.keys()){
                data.add(i);
                data.add(spawnPlan.get(i));
            }
            return data;
        }
    
        @Override
        public void draw(){
            super.draw();
            Draw.z(Layer.bullet);
            float scl = warmup * atlasSizeScl;
            Lines.stroke(squareStroke * warmup, getColor(getSet()));
            float rot = progress;
            Lines.square(x, y, block.size * tilesize / 2.5f, -rot);
            Lines.square(x, y, block.size * tilesize / 2f, rot);
            for(int i = 0; i < 4; i++){
                float length = tilesize * block().size / 2f + 8f;
                Tmp.v1.trns(i * 90 + rot, -length);
                Draw.rect(arrowRegion,x + Tmp.v1.x,y + Tmp.v1.y, arrowRegion.width * Draw.scl * scl, arrowRegion.height * Draw.scl * scl, i * 90 + 90 + rot);
                float sin = Mathf.absin(progress, 16f, tilesize);
                length = tilesize * block().size / 2f + 3 + sin;
                float signSize = 0.75f + Mathf.absin(progress + 8f, 8f, 0.15f);
                Tmp.v1.trns(i * 90, -length);
                Draw.rect(pointerRegion, x + Tmp.v1.x,y + Tmp.v1.y, pointerRegion.width * Draw.scl * signSize * scl, pointerRegion.height * Draw.scl * signSize * scl, i * 90 + 90);
            }
            Draw.color();

            if(isCalling() && hasConsume(getSet())){
                Draw.z(Layer.bullet);
                Draw.color(getColor(getSet()));
                for (int l = 0; l < 4; l++) {
                    float angle = 45 + 90 * l;
                    float regSize = regSize(getType()) / 3f + Draw.scl;
                    for (int i = 0; i < 4; i++) {
                        Tmp.v1.trns(angle, (i - 4) * tilesize * 2);
                        float f = (100 - (progress - 25 * i) % 100) / 100;
                        Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, pointerRegion.width * regSize * f * scl, pointerRegion.height * regSize * f * scl, angle - 90);
                    }
                }

                if(error || !hasConsume(getSet())){
                    Draw.color(getColor(getSet()));
                    float signSize = 0.75f + Mathf.absin(progress + 8f, 8f, 0.15f);
                    for (int i = 0; i < 4; i++) {
                        Draw.rect(arrowRegion, x , y, arrowRegion.width * Draw.scl * signSize * scl, arrowRegion.height * Draw.scl * signSize * scl, 90 * i);
                    }
                }
            }
            Draw.reset();
        }

        public void consumeItems(){
            if(coreValid()){
                int i = 0;
                if(!cheating())team.core().items.remove(getSet().requirements());
            }
        }

        public boolean hasConsume(UnitSet set){
            if(set == null || cheating())return true;
            if(!coreValid())return false;
            return core().items.has(set.requirements());
        }

        public boolean canSpawn(UnitSet set) {
            return Units.canCreate(team, set.type) && (cheating() ||
                (coreValid() && ! isCalling() && hasConsume(set)
            ));
        }
        
        public void startBuild(int set){
            if(set < 0 || set >= calls.size){
                spawnID = -1;
                buildReload = 0;
            }else spawnID = set;
        }
        
        public void startBuild(UnitSet set){
            spawnID = calls.indexOf(set);
        }

        public void spawn(UnitSet set){
            if(!isValid() || !Units.canCreate(team, set.type))return;
            success = false;
            int spawnNum = set.callIns;
            Vec2 target = link();
            float Sx = target.x;
            float Sy = target.y;
            
            NHFx.spawn.at(x, y, regSize(set.type), baseColor(), this);
    
            success = Functions.spawnUnit(this, Sx, Sy, spawnRange, spawnReloadTime, spawnDelay, (long)Groups.unit.size() + Groups.build.size() << 8 + id << 2, set, baseColor());
            performPlanIndex = -1;
            
            if(success){
                consumeItems();
                buildReload = 0;
                spawnID = -1;
                error = false;
            }else error = true;
        }
    
        @Override public float range(){return range;}
        @Override public void write(Writes write) {
            write.i(spawnID);
            write.i(link);
            write.f(buildReload);
            write.f(warmup);
            write.i(performPlanIndex);
            write.i(spawnPlan.size);
            for(int boolf : spawnPlan.keys()){
                write.i(boolf);
                write.i(spawnPlan.get(boolf));
            }
        }
        @Override public void read(Reads read, byte revision) {
            spawnID = read.i();
            link = read.i();
            buildReload = read.f();
            warmup = read.f();
            performPlanIndex = read.i();
            int size = read.i();
            for(int i = 0; i < size; i++){
                spawnPlan.put(read.i(), read.i());
            }
        }
        public boolean isCalling(){ return spawnID >= 0; }
        public boolean coreValid() { return team.core() != null && team.core().items != null && !team.core().items.empty(); }
        public UnitType getType(){ return calls.get(spawnID).type; }
        public void setTarget(Integer pos){ link = pos; }
        public UnitSet getSet(){
            if(spawnID < 0 || spawnID >= calls.size)return null;
            return calls.get(spawnID);
        }
        
        public Vec2 link(){
            Tile t = world.tile(linkPos());
            if(t == null)return linkP.set(this);
            else return linkP.set(t);
        }
        public int linkPos(){return link; }
        public void linkPos(Point2 point2){
            Tile tile = world.tile(point2.x, point2.y);
            if(tile != null && tile.within(this, range()))link = point2.pack();
            else if(tile != null){
                Tmp.v1.set(tile).sub(this).nor().scl(range());
                link = Tmp.p1.set((int)World.conv(x + Tmp.v1.x), (int)World.conv(y + Tmp.v1.y)).pack();
            }else link = pos();
        }
    
        @Override
        public boolean cheating(){
            return super.cheating() || team == state.rules.waveTeam;
        }
    }

    public static class UnitSet{
        public final Seq<ItemStack> requirements = new Seq<>(ItemStack.class);
        public @NotNull UnitType type;
        public float costTime;
        public int callIns;
        
        //[0] -> Line or Type; [1] -> Tier.
        public final byte[] sortIndex;
        
        public UnitSet(){this(UnitTypes.alpha, new byte[]{-1, -1}, 0, 5); }
    
        public UnitSet(@NotNull UnitType type, byte[] sortIndex, float costTime, int callIns, ItemStack... requirements){
            this.type = type;
            this.sortIndex = sortIndex;
            this.costTime = costTime;
            this.callIns = callIns;
            this.requirements.addAll(requirements);
            if(!NHLoader.unitBuildCost.containsKey(type))NHLoader.unitBuildCost.put(type, ItemStack.mult(requirements, 1f / callIns * 20));
        }
    
        @Override
        public boolean equals(Object o){
            if(this == o) return true;
            if(!(o instanceof UnitSet)) return false;
            UnitSet set = (UnitSet)o;
            return callIns == set.callIns && type.equals(set.type) && Arrays.equals(sortIndex, set.sortIndex);
        }
    
        @Override
        public int hashCode(){
            int result = Objects.hash(type.name.hashCode(), callIns);
            result = 31 * result + Arrays.hashCode(sortIndex);
            return result;
        }
    
        public float costTime(){return costTime;}
        public float costTimeVar(){return costTime / state.rules.unitBuildSpeedMultiplier;}
        public ItemStack[] requirements(){ return requirements.toArray(); }
    }
}
