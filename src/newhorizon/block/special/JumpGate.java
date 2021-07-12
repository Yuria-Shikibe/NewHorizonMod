package newhorizon.block.special;

import arc.Core;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.Ranged;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;
import mindustry.ui.ItemDisplay;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.ItemModule;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.content.NHLoader;
import newhorizon.content.NHSounds;
import newhorizon.emergency.TimedcE;
import newhorizon.feature.NHBaseEntity;
import newhorizon.func.*;
import newhorizon.vars.NHVars;
import org.jetbrains.annotations.NotNull;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Objects;

import static mindustry.Vars.*;
import static newhorizon.func.NHFunc.regSize;
import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;

public class JumpGate extends Block {
    protected static final ObjectMap<UnitSet, Integer> allSets = new ObjectMap<>();
    protected static final Seq<UnitSet> all = new Seq<>();
    
    static{
        ClassIDIniter.put(Spawner.class, new ClassIDIniter.Set(Spawner::new));
    }
    
    public int maxSpawnPerOne = 15;
    public boolean adaptable = false;
    @Nullable public Block adaptBase = null;
    public float spawnDelay = 5f;
    public float spawnReloadTime = 180f;
    public float spawnRange = tilesize * 12;
    public float range = 200f;
    public float atlasSizeScl = 1;
    public float basePowerDraw = 2f;
    public TextureRegion
            pointerRegion,
            arrowRegion;
    public Color baseColor;
    public final Seq<UnitSet> calls = new Seq<>();
    public float squareStroke = 2f;
    
    public float cooldownTime = 300f;
    
    protected static final Vec2 linkVec = new Vec2();
    protected static final Point2 point = new Point2();
    
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
        logicConfigurable = true;
        config(Boolean.class, (JumpGateBuild tile, Boolean i) -> {
            if(i)tile.spawn(tile.getSet());
            else tile.startBuild(-1, 0);
        });
        config(Point2.class, (Cons2<JumpGateBuild, Point2>)JumpGateBuild::linkPos);
        config(Integer.class, (JumpGateBuild tile, Integer data) -> {
            point.set(Point2.unpack(data));
            if(point.x < 0 || !tile.isCalling() || tile.getSet() == null)tile.startBuild(point.x, point.y);
        });
        configClear((JumpGateBuild tile) -> tile.startBuild(-1, 0));
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
            t.image(adaptBase.fullIcon).size(LEN).padLeft(OFFSET).row();
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
            inner.image(set.type.fullIcon).center().row();
            inner.image().growX().height(OFFSET / 4).pad(OFFSET / 4f).color(Pal.accent).row();
            inner.add("[lightgray]" + Core.bundle.get("editor.spawn") + ": [accent]" + set.type.localizedName + "[lightgray] | Tier: [accent]" + set.sortIndex[1]).left().padLeft(OFFSET).row();
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
                () -> entity.isCalling() ? entity.buildProgress / entity.costTime(entity.getSet(), true) : 0
            )
        );
        bars.add("cooldown",
                (JumpGateBuild entity) -> new Bar(
                        () -> Core.bundle.get("stat.cooldowntime"),
                        () -> Pal.lancerLaser,
                        () -> entity.cooling ? (cooldownTime - entity.cooldown) / cooldownTime : 0
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
        public int spawnID = -1;
        public int link = -1;
        public float buildProgress = 0f;
        public float progress;
        public float warmup;
        public boolean jammed;
        
        public float cooldown = 0;
        public boolean cooling = false;
        
        //Local var
        public transient int spawnNum = 1;
        public int buildingSpawnNum = -1;
        
        @Override
        public boolean onConfigureTileTapped(Building other){
            if(this == other)configure(Tmp.p1.set(-1, -1));
            return false;
        }
    
        @Override
        public void updateTile(){
            if(hasConsume(getSet()))progress += (efficiency() + warmup) * delta() * Mathf.curve(Time.delta, 0f, 1f);
            if(isCalling() && Units.canCreate(team, getType())){
                buildProgress += efficiency() * state.rules.unitBuildSpeedMultiplier * delta();
                if(state.rules.infiniteResources || (buildProgress >= costTime(getSet(), true) && !jammed)){
                    spawn(getSet());
                }
            }
            
            if(cooling){
                if(Mathf.chanceDelta(0.2f))Fx.reactorsmoke.at(x + Mathf.range(tilesize * size / 2), y + Mathf.range(tilesize * size / 2));
                if(timer.get(0, 4)) for(int i = 0; i < 4; i++){
                    Fx.shootSmallSmoke.at(x, y, i * 90);
                }
                
                cooldown += (efficiency() + warmup) * delta() * Mathf.curve(Time.delta, 0f, 1f);
                if(cooldown > cooldownTime){
                    cooling = false;
                    cooldown = 0;
                }
            }
            
            if(efficiency() > 0){
                if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
                else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
            }else{
                if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
                else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
            }
        }

        public Color getColor(UnitSet set){
            if(cooling)return Pal.lancerLaser;
            if(jammed || (set != null && !canSpawn(set, true)))return Tmp.c1.set(team.color).lerp(Pal.ammo, Mathf.absin(10f, 0.3f) + 0.1f);
            else return team.color;
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
            
            if(jammed)DrawFuncs.overlayText(Core.bundle.get("spawn-error"), x, y, size * tilesize / 2.0F, color, true);
            
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
                                table2.update(() -> can.setText("[lightgray]Can Spawn?: " + TableFs.getJudge(canSpawn(set, false))));
                                table2.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(set, can, core() != null ? core().items : null)).size(LEN);
                                table2.button(Icon.add, Styles.clearPartiali, () -> configure(Tmp.p1.set(calls.indexOf(set), spawnNum).pack())).size(LEN).disabled(b -> (team.data().countType(set.type) + spawnNum > Units.getCap(team)) || jammed || isCalling() || !hasConsume(set) || cooling);
                            })).fillY().growX().row();
                            Bar unitCurrent = new Bar(
                                () -> Core.bundle.format("bar.unitcap",
                                        Fonts.getUnicodeStr(set.type.name),
                                        team.data().countType(set.type),
                                        Units.getCap(team)),
                                () -> canSpawn(set, false) ? Pal.accent : Units.canCreate(team, set.type) ? Pal.ammo : Pal.redderDust,
                                () -> (float)team.data().countType(set.type) / Units.getCap(team)
                            );
                            info.add(unitCurrent).growX().height(LEN - OFFSET);
                        }).fillY().growX().padTop(OFFSET).row();
                    }
                }).grow()
            ).grow().row();
            dialog.cont.table(t -> {
                Label l = new Label("");
                Slider s = new Slider(1, Mathf.clamp(Units.getCap(team), 1, maxSpawnPerOne), 1, false);
                s.moved((i) -> {
                    spawnNum = (int)i;
                    if(!isCalling())buildingSpawnNum = spawnNum;
                });
                t.update(() -> {
                    l.setText("[gray]<" + Core.bundle.get("filter.option.amount") + ": [lightgray]" + spawnNum + "[]>");
                    s.setValue(spawnNum);
                });
                
                t.add(s).growX().height(LEN);
                t.add(l).fillX().height(LEN).padRight(OFFSET).padLeft(OFFSET);
                t.add(new Bar(
                        () ->
                            !isCalling() ? "[lightgray]" + Iconc.cancel :
                            Units.canCreate(team, getType()) && !jammed ? "[lightgray]" + Core.bundle.get("editor.spawn") + ": [accent]" + getSet().type.localizedName + "[lightgray] | " + Core.bundle.get("ui.remain-time") + ": [accent]" + (int)Math.max( (costTime(getSet(), true) - buildProgress) / Time.toSeconds / state.rules.unitBuildSpeedMultiplier, 0) + "[lightgray] " + Core.bundle.get("unit.seconds") :
                            "[red]Call Jammed",
                        () -> isCalling() && canSpawn(getSet(), true) && !jammed ? Pal.power : Pal.redderDust,
                        () -> isCalling() ? buildProgress / costTime(getSet(), true) : 0
                )).growX().height(LEN);
            }).growX().height(LEN).row();
            dialog.cont.table(t -> {
                t.button("@back", Icon.left, Styles.cleart, dialog::hide).padTop(OFFSET / 2).growX().height(LEN);
                t.button("@cancel", Icon.cancel, Styles.cleart, () -> configure(false)).padTop(OFFSET / 2).disabled(b -> !isCalling()).growX().height(LEN);
                t.button("@release", Icon.add, Styles.cleart, () -> configure(true)).padTop(OFFSET / 2).disabled(b -> getSet() == null || !jammed).growX().height(LEN);
            }).growX().height(LEN).bottom();
            dialog.keyDown(c -> {
                if(c == KeyCode.left)spawnNum = Mathf.clamp(--spawnNum, 1, Mathf.clamp(Units.getCap(team), 1, maxSpawnPerOne));
                if(c == KeyCode.right)spawnNum = Mathf.clamp(++spawnNum, 1, Mathf.clamp(Units.getCap(team), 1, maxSpawnPerOne));
            });
            
            table.table(Tex.paneSolid, t -> {
                t.button("@spawn", Icon.add, Styles.cleart, dialog::show).size(LEN * 5, LEN).row();
                t.button("@mod.ui.select-target", Icon.move, Styles.cleart, () -> TableFs.pointSelectTable(table, this::configure)).disabled(b -> NHVars.ctrl.isSelecting).size(LEN * 5, LEN);
            }).fill();
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

            if(isCalling()){
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
                if(jammed || !Units.canCreate(team, getType())){
                    Draw.color(getColor(getSet()));
                    float signSize = 0.75f + Mathf.absin(progress + 8f, 8f, 0.15f);
                    for (int i = 0; i < 4; i++) {
                        Draw.rect(arrowRegion, x , y, arrowRegion.width * Draw.scl * signSize * scl, arrowRegion.height * Draw.scl * signSize * scl, 90 * i);
                    }
                }
            }
            Draw.reset();
            
            Drawf.light(team, tile, size * tilesize * 4 * warmup, team.color, 0.95f);
        }

        public void consumeItems(){
            if(!cheating() && core() != null)core().items.remove(ItemStack.mult(getSet().requirements(), buildingSpawnNum));
        }

        public boolean hasConsume(UnitSet set){
            if(set == null || cheating())return true;
            if(core() == null)return false;
            return core().items.has(ItemStack.mult(set.requirements(), spawnNum));
        }

        public float costTime(UnitSet set, boolean buildingParma){
            return (buildingParma ? buildingSpawnNum : spawnNum) * set.costTime();
        }
        
        public boolean canSpawn(UnitSet set, boolean buildingParma) {
            return team.data().countType(set.type) + (buildingParma ? buildingSpawnNum : spawnNum) <= Units.getCap(team);
        }
        
        public void startBuild(int set, int spawnNum){
            jammed = false;
            buildProgress = 0;
            if(isCalling())cooling = true;
            
            if(set < 0 || set >= calls.size){
                spawnID = -1;
            }else{
                spawnID = set;
                buildingSpawnNum = spawnNum;
                consumeItems();
            }
        }
        
        public void spawn(UnitSet set){
            if(!isValid())return;
            boolean success;
            
            Vec2 target = link();
            
            NHFx.spawn.at(x, y, regSize(set.type), team.color, this);
    
            success = NHFunc.spawnUnit(this, target.x, target.y, spawnRange, spawnReloadTime, spawnDelay, getType(), buildingSpawnNum);
            
            if(success){
                buildProgress = 0;
                spawnID = -1;
                buildingSpawnNum = spawnNum;
                jammed = false;
                cooling = true;
            }else jammed = true;
        }
    
        @Override public float range(){return range;}
        @Override public void write(Writes write) {
            write.i(spawnID);
            write.i(link);
            write.f(buildProgress);
            write.f(warmup);
            write.i(buildingSpawnNum);
            
            write.bool(cooling);
            write.f(cooldown);
        }
        @Override public void read(Reads read, byte revision) {
            spawnID = read.i();
            link = read.i();
            buildProgress = read.f();
            warmup = read.f();
            buildingSpawnNum = read.i();
            
            cooling = read.bool();
            cooldown = read.f();
        }
        
        public boolean isCalling(){ return spawnID >= 0; }
        public UnitType getType(){ return calls.get(spawnID).type;}
        public UnitSet getSet(){
            if(spawnID < 0 || spawnID >= calls.size)return null;
            return calls.get(spawnID);
        }
        
        public Vec2 link(){
            Tile t = world.tile(linkPos());
            if(t == null)return linkVec.set(this);
            else return linkVec.set(t);
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
            return super.cheating() || team == state.rules.waveTeam || state.rules.infiniteResources;
        }
    }

    public static class UnitSet{
        public final Seq<ItemStack> requirements = new Seq<>(ItemStack.class);
        public @NotNull UnitType type;
        public float costTime;
        
        //[0] -> Line or Type; [1] -> Tier.
        public final byte[] sortIndex;
        
        public UnitSet(){this(UnitTypes.alpha, new byte[]{-1, -1}, 0); }
    
        public UnitSet(@NotNull UnitType type, byte[] sortIndex, float costTime, ItemStack... requirements){
            this.type = type;
            this.sortIndex = sortIndex;
            this.costTime = costTime;
            this.requirements.addAll(requirements);
            if(!NHLoader.unitBuildCost.containsKey(type))NHLoader.unitBuildCost.put(type, ItemStack.mult(requirements, 20));
        }
    
        @Override
        public boolean equals(Object o){
            if(this == o) return true;
            if(!(o instanceof UnitSet)) return false;
            UnitSet set = (UnitSet)o;
            return type.equals(set.type) && Arrays.equals(sortIndex, set.sortIndex);
        }
    
        @Override
        public int hashCode(){
            int result = Objects.hash(type.name.hashCode());
            result = 31 * result + Arrays.hashCode(sortIndex);
            return result;
        }
    
        public float costTime(){return costTime;}
        public float costTimeVar(){return costTime / state.rules.unitBuildSpeedMultiplier;}
        public ItemStack[] requirements(){ return requirements.toArray(); }
    }
    
    public static class Spawner extends NHBaseEntity implements Syncc, TimedcE, Rotc{
        public Team team = Team.derelict;
        public UnitType type = UnitTypes.alpha;
        public int spawnNum = 0;
        public float time = 0, lifetime;
        public float rotation;
        
        public int ownerPos = -1;
        public boolean checked = false;
        
        public transient long lastUpdated, updateSpacing;
    
        public transient Unit addUnit = Nulls.unit;
    
        @Override
        public float clipSize(){
            return 500;
        }
    
        public void init(UnitType type, int spawnNum, Team team, Position pos, float rotation, float lifetime, int ownerPos){
            this.type = type;
            this.spawnNum = spawnNum;
            this.lifetime = lifetime;
            this.rotation = rotation;
            this.team = team;
            this.ownerPos = ownerPos;
            set(pos);
            NHFx.spawnWave.at(x, y, size, team.color);
        }
    
        @Override
        public void add(){
            super.add();
            Groups.sync.add(this);
        }
    
        @Override
        public void remove(){
            super.remove();
            Groups.sync.remove(this);
    
            if(Vars.net.client()){
                Vars.netClient.addRemovedEntity(id());
            }
        }
    
        @Override
        public void update(){
            if(Units.canCreate(team, type))time += Time.delta;
            
            if(time > lifetime){
                dump();
                effect();
                remove();
            }
            
            if(!checked){
                Building b = world.build(ownerPos);
                if(b != null && b.team == team && b.isValid() && b instanceof JumpGateBuild)b.configure(false);
                checked = true;
            }
        }
        
        public void effect(){
            Effect.shake(type.hitSize / 3f, type.hitSize / 4f, addUnit);
            NHSounds.jumpIn.at(addUnit.x, addUnit.y);
            if(type.flying){
                
                NHFx.jumpTrail.at(addUnit.x, addUnit.y, rotation(), team.color, type);
                addUnit.apply(StatusEffects.slow, NHFx.jumpTrail.lifetime);
            }else{
                NHFx.spawn.at(x, y, type.hitSize, team.color);
                Fx.unitSpawn.at(addUnit.x, addUnit.y, rotation(), type);
                Time.run(Fx.unitSpawn.lifetime, () -> {
                    for(int j = 0; j < 3; j++){
                        Time.run(j * 8, () -> Fx.spawn.at(addUnit));
                    }
                    NHFx.spawnGround.at(addUnit.x, addUnit.y, type.hitSize / tilesize * 3, team.color);
                    NHFx.circle.at(addUnit.x, addUnit.y, type.hitSize * 4, team.color);
                });
            }
        }
        
        public void dump(){
            addUnit = type.create(team);
            addUnit.set(x, y);
            addUnit.rotation = rotation();
            if(!Vars.net.client())addUnit.add();
            addUnit.apply(StatusEffects.unmoving, Fx.unitSpawn.lifetime);
        }
    
        @Override
        public void draw(){
            TextureRegion pointerRegion = NHContent.pointerRegion, arrowRegion = NHContent.arrowRegion;
            
            Draw.z(Layer.effect - 1f);
            
            boolean can = Units.canCreate(team, type);
    
            float regSize = NHFunc.regSize(type);
            Draw.color(can ? team.color : Tmp.c1.set(team.color).lerp(Pal.ammo, Mathf.absin(Time.time * DrawFuncs.sinScl, 8f, 0.3f) + 0.1f));
            
            for(int i = 0; i < 4; i++){
                float sin = Mathf.absin(Time.time, 16f, tilesize);
                float length = (tilesize * 5 + sin) * fout() + tilesize;
                float signSize = regSize + 0.75f + Mathf.absin(Time.time + 8f, 8f, 0.15f);
                Tmp.v1.trns(i * 90, -length);
                Draw.rect(pointerRegion, x + Tmp.v1.x, y + Tmp.v1.y, pointerRegion.width * Draw.scl * signSize, pointerRegion.height * Draw.scl * signSize, i * 90 - 90);
            }
    
            if(can){
                for(int i = -4; i <= 4; i++){
                    if(i == 0)continue;
                    Tmp.v1.trns(rotation, i * tilesize * 2);
                    float f = (100 - (Time.time - 12.5f * i) % 100) / 100;
                    Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, arrowRegion.width * (regSize / 2f + Draw.scl) * f, arrowRegion.height * (regSize / 2f + Draw.scl) * f, rotation() - 90);
                }
    
                float railF = Mathf.curve(fin(Interp.circleIn), 0f, 0.1f) * Mathf.curve(fout(Interp.pow4Out), 0f, 0.1f) * fin();
                Tmp.v1.trns(rotation, 0f, (2 - railF) * tilesize * 1.4f);
    
                Lines.stroke(railF * 2f);
                for(int i : Mathf.signs){
                    Lines.lineAngleCenter(x + Tmp.v1.x * i, y + Tmp.v1.y * i, rotation(), tilesize * (3f + railF) * tilesize * Mathf.curve(fout(Interp.pow5Out), 0f, 0.1f));
                }
            }
            
            if(can)DrawFuncs.overlayText(Fonts.tech, String.valueOf(Mathf.ceil((lifetime - time) / 60f)), x, y, 0, 0,0.25f, team.color, false, true);
            else{
                Draw.z(Layer.effect);
                Draw.color(Pal.ammo);
    
                float s = Mathf.clamp(size / 4f, 12f, 20f);
                Draw.rect(Icon.warning.getRegion(), x, y, s, s);
            }
            Draw.reset();
        }
    
        @Override
        public void write(Writes write){
            super.write(write);
            write.f(lifetime);
            write.f(time);
            write.f(rotation);
            write.i(spawnNum);
            
            TypeIO.writeUnitType(write, type);
            TypeIO.writeTeam(write, team);
        }
    
        @Override
        public void read(Reads read){
            super.read(read);
            lifetime = read.f();
            time = read.f();
            rotation = read.f();
            spawnNum = read.i();
            
            
            type = TypeIO.readUnitType(read);
            team = TypeIO.readTeam(read);
            
            afterRead();
        }
    
        @Override public boolean serialize(){return true;}
        @Override public int classId(){return ClassIDIniter.getID(getClass());}
    
        @Override
        public void snapSync(){}
    
        @Override
        public void snapInterpolation(){}
    
        @Override
        public void readSync(Reads read){
            x = read.f();
            y = read.f();
            lifetime = read.f();
            time = read.f();
            rotation = read.f();
            spawnNum = read.i();
            ownerPos = read.i();
            checked = read.bool();
            
            type = TypeIO.readUnitType(read);
            team = TypeIO.readTeam(read);
    
            afterSync();
        }
    
        @Override
        public void writeSync(Writes write){
            write.f(x);
            write.f(y);
            write.f(lifetime);
            write.f(time);
            write.f(rotation);
            write.i(spawnNum);
            write.i(ownerPos);
            write.bool(checked);
            
            TypeIO.writeUnitType(write, type);
            TypeIO.writeTeam(write, team);
        }
    
        @Override
        public void readSyncManual(FloatBuffer floatBuffer){
        
        }
    
        @Override
        public void writeSyncManual(FloatBuffer floatBuffer){
        
        }
    
        @Override
        public void afterSync(){
        
        }
    
        @Override
        public void interpolate(){
        
        }
    
        @Override public long lastUpdated(){return lastUpdated;}
        @Override public void lastUpdated(long l){lastUpdated = l;}
        @Override public long updateSpacing(){return updateSpacing;}
        @Override public void updateSpacing(long l){updateSpacing = l;}
        @Override public float fin(){return time / lifetime;}
        @Override public float time(){return time;}
        @Override public void time(float v){time = v;}
        @Override public float lifetime(){return lifetime;}
        @Override public void lifetime(float v){lifetime = v;}
        @Override public float rotation(){return rotation;}
        @Override public void rotation(float v){rotation = v;}
    }
}
