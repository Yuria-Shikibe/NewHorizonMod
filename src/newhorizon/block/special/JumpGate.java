package newhorizon.block.special;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
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
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.ItemModule;
import newhorizon.NewHorizon;
import newhorizon.content.NHFx;
import newhorizon.content.NHLoader;
import newhorizon.func.DrawFuncs;
import newhorizon.func.Functions;
import newhorizon.func.TableFuncs;
import newhorizon.func.Tables;
import newhorizon.interfaces.Linkablec;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.*;
import static newhorizon.func.Functions.regSize;
import static newhorizon.func.TableFuncs.LEN;
import static newhorizon.func.TableFuncs.OFFSET;

public class JumpGate extends Block {
    protected static final Seq<JumpGate> all = new Seq<>();
    
    public boolean adaptable = false;
    public boolean primary;
    public float spawnDelay = 5f;
    public float spawnReloadTime = 180f;
    public float spawnRange = 120f;
    public float range = 200f;
    public float inComeVelocity = 5f;
    public float atlasSizeScl = 1;
    public float basePowerDraw = 3f;
    public TextureRegion
            pointerRegion,
            arrowRegion;
    public Color baseColor;
    public final Seq<UnitSet> calls = new Seq<>();

    public float squareStroke = 2f;

    public JumpGate(String name){
        super(name);
        update = true;
        configurable = true;
        solid = true;
        hasPower = true;
        category = Category.units;
        
        all.add(this);
    
        consumes.powerCond(basePowerDraw, (JumpGateBuild b) -> !b.isCalling());
        consumes.powerCond(consumes.getPower().usage, JumpGateBuild::isCalling);
    }
    
    public boolean canReplace(Block other) {
        return super.canReplace(other) || other instanceof JumpGate && size > other.size;
    }
    
    public boolean canPlaceOn(Tile tile, Team team) {
        if(primary)return true;
        if (tile == null) {
            return false;
        } else {
            return tile.block() instanceof JumpGate && size > tile.block().size;
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
        if(adaptable)for(JumpGate gate : all){
            if(gate.size >= size)continue;
            calls.addAll(gate.calls);
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.powerUse, basePowerDraw * 60F, StatUnit.powerSecond);
        stats.add(Stat.output, (t) -> {
            t.row().add("[gray]Summon Types:").left().pad(OFFSET).row();
            for(UnitSet set : calls) {
                t.add(new Tables.UnitSetTable(set, table -> table.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(set, "[accent]Caution[gray]: Summon needs building.")).size(LEN))).fill().row();
            }
        });
    }

    public void showInfo(UnitSet set, String textExtra){
        BaseDialog dialogIn = new BaseDialog("More Info");
        dialogIn.addCloseListener();
        dialogIn.cont.margin(15f);
        dialogIn.cont.pane(t -> {
            t.image(set.type.icon(Cicon.full)).center().row();
            t.add("<<[accent] " + set.type.localizedName + " []>>").row();
            t.add("[lightgray]Call: [accent]" + set.type.localizedName + "[lightgray]; Level: [accent]" + set.level + "[]; Call num: [accent]" + set.callIns + "[].").left().padLeft(OFFSET).row();
            t.add("[lightgray]BuildNeededTime: [accent]" + TableFuncs.format(set.costTime() / 60) + "[lightgray] sec[]").left().padLeft(OFFSET).row();
            t.table(table -> {
                int index = 0;
                for(ItemStack stack : set.requirements()){
                    if(index % 5 == 0)table.row();
                    table.add(new ItemDisplay(stack.item, stack.amount, false)).padRight(5).left();
                    index ++;
                }
            }).fillY().left().padLeft(OFFSET).row();
            if(!textExtra.equals(""))t.add(textExtra).left().padLeft(OFFSET).row();
            t.image().fillX().pad(2).height(4f).color(Pal.accent);
            t.row();
            t.button("@back", Icon.left, Styles.cleart, dialogIn::hide).size(LEN * 3f, LEN).pad(OFFSET);
        }).fill().row();
        dialogIn.show();
    }

    @Override
    public void setBars() {
        super.setBars();
        bars.add("progress",
            (JumpGateBuild entity) -> new Bar(
                    () -> Core.bundle.get("bar.progress"),
                    () -> Pal.power,
                    () -> entity.getSet() == null ? 0 : entity.buildReload / entity.getSet().costTime()
            )
        );
    }

    @Override
    public void load(){
        super.load();
        pointerRegion = Core.atlas.find(NewHorizon.MOD_NAME + "jump-gate-pointer");
        arrowRegion = Core.atlas.find(NewHorizon.MOD_NAME + "jump-gate-arrow");
    }

    public class JumpGateBuild extends Building implements Ranged, Linkablec{
        public Color baseColor(){
            return baseColor == null ? team().color : baseColor;
        }
        public int spawnID = -1;
        public int link = -1;
        public float buildReload = 0f;
        public float progress;
        protected boolean success, error;

        
        @Override
        public void updateTile(){
            super.updateTile();
            if(hasConsume(getSet()))progress += efficiency();
            if(isCalling()){
                buildReload += efficiency() * (state.rules.infiniteResources ? Float.MAX_VALUE : 1);
                if(buildReload >= getSet().costTime() && hasConsume(getSet()) && !error){
                    spawn(getSet());
                }
            }
        }

        public Color getColor(UnitSet set){
            if(set == null)return baseColor();
            return (error || !hasConsume(getSet())) ? baseColor().cpy().lerp(Pal.ammo, 1 / Mathf.clamp((efficiency() + 1), 0, 2)) : baseColor();
        }
    
        @Override
        public void configure(Object value){
            super.configure(value);
            if(value instanceof Integer){
                setTarget((Integer)value);
            }else if(value instanceof UnitSet){
                startBuild((UnitSet)value);
            }
        }
    
        @Override
        public void drawConfigure() {
            Color color = getColor(getSet());
            Drawf.dashCircle(x, y, range(), color);
            Draw.color(color);
            Lines.square(x, y, block().size * tilesize / 2f + 1.0f);
            drawLink();
            if(link() != null) {
                Building target = link();
                Draw.alpha(0.3f);
                Fill.square(target.x, target.y, target.block.size / 2f * tilesize);
                Draw.alpha(1f);
                Drawf.dashCircle(target.x, target.y, spawnRange, color);
                Draw.color(color);
                Lines.square(target.x, target.y, target.block().size * tilesize / 2f + 1.0f);
            }else Drawf.dashCircle(x, y, spawnRange, color);
            
            if(core() != null)DrawFuncs.posSquareLinkArr(color, 1.5f, 3.5f, true, false, this, core());
            
            if(error)DrawFuncs.overlayText(Core.bundle.get("spawn-error"), x, y, size * tilesize / 2.0F, color);
            
            Draw.reset();
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            if (this == other || link == other.pos()) {
                configure(-1);
                return false;
            }
            if (other.within(this, range())) {
                configure(other.pos());
                return false;
            }
            return true;
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
                                table2.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(set, "[lightgray]CanCall?: " + TableFuncs.getJudge(canSpawn(set)) + "[]")).size(LEN);
                                table2.button(Icon.add, Styles.clearPartiali, () -> configure(set)).size(LEN).disabled(b -> !canSpawn(set) || error);
                            })).fillY().growX().row();
                            Bar unitCurrent = new Bar(
                                    () -> Core.bundle.format("bar.unitcap",
                                                    Fonts.getUnicodeStr(set.type.name),
                                                    team.data().countType(set.type),
                                                    Units.getCap(team)
                                            ),
                                    () -> Units.getCap(team) - team.data().countType(set.type) - set.callIns <= 0 ? Pal.redderDust : Pal.power,
                                    () ->  (float)team.data().countType(set.type) / Units.getCap(team)
                            );
                            info.add(unitCurrent).growX().height(LEN - OFFSET);
                        }).fillY().growX().padTop(OFFSET).row();
                    }
                }).grow()
            ).grow().row();
            dialog.cont.button("@release", Icon.add, Styles.cleart, () -> spawn(getSet())).padTop(OFFSET / 2).disabled(b -> getSet() == null || success || !hasConsume(getSet()) || !canSpawn(getSet())).fillX().height(LEN).row();
            dialog.cont.button("@back", Icon.left, Styles.cleart, dialog::hide).padTop(OFFSET / 2).fillX().height(LEN).row();
            
            table.button("@spawn", Icon.add, dialog::show).size(LEN * 5, LEN);
        }

        @Override
        public void draw(){
            super.draw();
            Draw.z(Layer.bullet);
            if(efficiency() > 0){
                Lines.stroke(squareStroke, getColor(getSet()));
                float rot = progress;
                Lines.square(x, y, block.size * tilesize / 2.5f, -rot);
                Lines.square(x, y, block.size * tilesize / 2f, rot);
                for(int i = 0; i < 4; i++){
                    float length = tilesize * block().size / 2f + 8f;
                    Tmp.v1.trns(i * 90 + rot, -length);
                    Draw.rect(arrowRegion,x + Tmp.v1.x,y + Tmp.v1.y, arrowRegion.width * Draw.scl * atlasSizeScl, arrowRegion.height * Draw.scl * atlasSizeScl, i * 90 + 90 + rot);
                    float sin = Mathf.absin(progress, 16f, tilesize);
                    length = tilesize * block().size / 2f + 3 + sin;
                    float signSize = 0.75f + Mathf.absin(progress + 8f, 8f, 0.15f);
                    Tmp.v1.trns(i * 90, -length);
                    Draw.rect(pointerRegion, x + Tmp.v1.x,y + Tmp.v1.y, pointerRegion.width * Draw.scl * signSize * atlasSizeScl, pointerRegion.height * Draw.scl * signSize * atlasSizeScl, i * 90 + 90);
                }
                Draw.color();
            }

            if(isCalling() && efficiency() > 0.1f && hasConsume(getSet())){
                Draw.z(Layer.bullet);
                Draw.color(getColor(getSet()));
                for (int l = 0; l < 4; l++) {
                    float angle = 45 + 90 * l;
                    float regSize = regSize(getType()) / 3f + Draw.scl;
                    for (int i = 0; i < 4; i++) {
                        Tmp.v1.trns(angle, (i - 4) * tilesize * 2);
                        float f = (100 - (progress - 25 * i) % 100) / 100;
                        Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, pointerRegion.width * regSize * f * atlasSizeScl, pointerRegion.height * regSize * f * atlasSizeScl, angle - 90);
                    }
                }

                if(error || !hasConsume(getSet())){
                    Draw.color(getColor(getSet()));
                    float signSize = 0.75f + Mathf.absin(progress + 8f, 8f, 0.15f);
                    for (int i = 0; i < 4; i++) {
                        Draw.rect(arrowRegion, x , y, arrowRegion.width * Draw.scl * signSize * atlasSizeScl, arrowRegion.height * Draw.scl * signSize * atlasSizeScl, 90 * i);
                    }
                }
            }
            Draw.reset();
        }

        public void consumeItems(){
            if(coreValid()){
                int i = 0;
//                for(ItemStack stack : getSet().requirements()){
//                    Delivery.DeliveryData data = Pools.obtain(Delivery.DeliveryData.class, Delivery.DeliveryData::new);
//                    data.items[Vars.content.items().indexOf(stack.item)] = stack.amount;
//                    i ++;
//                    Time.run(i * spawnDelay, () -> {
//                        Tmp.v1.rnd(block().size * tilesize / 2f).add(this);
//                        if(isValid())NHContent.deliveryBullet.create(this, team, Tmp.v1.x, Tmp.v1.y, angleTo(core()), 1f, 1f, 10000f, data);
//                    });
//                }
                
                if(!state.rules.infiniteResources)team.core().items.remove(getSet().requirements());
            }
        }

        public boolean hasConsume(UnitSet set){
            if(set == null || state.rules.infiniteResources)return true;
            if(!coreValid())return false;
            CoreBlock.CoreBuild core = team.core();
            return core.items.has(set.requirements());
        }

        public boolean canSpawn(UnitSet set) {
            return Units.canCreate(team, set.type) && (state.rules.infiniteResources ||
                (coreValid() && ! isCalling() && hasConsume(set)
            ));
        }

        public void startBuild(UnitSet set){
            spawnID = calls.indexOf(set);
            if(getSet().showText)ui.showInfoPopup("[accent]<<Caution>>[]:Team : " + team.name + "[] starts summon level[accent] " + set.level + " []fleet.", 8f, 0, 20, 20, 20, 20);
        }

        public void spawn(UnitSet set){
            if(!isValid() || !Units.canCreate(team, set.type))return;
            success = false;
            float Sx, Sy;
            int spawnNum = set.callIns;
            if(team.data().countType(set.type) + spawnNum > Units.getCap(team)){
                spawnNum = Units.getCap(team) - team.data().countType(set.type);
            }

            if(link() != null) {
                Building target = link();
                Sx = target.x;
                Sy = target.y;
            }else{
                Sx = x;
                Sy = y;
            }

            NHFx.spawn.at(x, y, regSize(set.type), baseColor(), this);
            success = Functions.spawnUnit(this, Sx, Sy, spawnNum, set.level, spawnRange, spawnReloadTime, spawnDelay, inComeVelocity, set.type, baseColor());
            if(success){
                if(getSet().showText)ui.showInfoPopup("[accent]<<Caution>>[]: Level [accent]" + getSet().level + "[] fleet in coming at [" + TableFuncs.format(x / tilesize) + ", " + TableFuncs.format(y / tilesize) + "].", spawnReloadTime / 60f, 0, 20, 20, 20, 20);
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
        }
        @Override public void read(Reads read, byte revision) {
            spawnID = read.i();
            link = read.i();
            buildReload = read.f();
        }
        public boolean isCalling(){ return spawnID >= 0; }
        public boolean coreValid() { return team.core() != null && team.core().items != null && !team.core().items.empty(); }
        public UnitType getType(){ return calls.get(spawnID).type; }
        public void setTarget(Integer pos){ link = pos; }
        public UnitSet getSet(){
            if(spawnID < 0 || spawnID >= calls.size)return null;
            return calls.get(spawnID);
        }
        
        @Override public int linkPos(){ return link; }
        @Override public void linkPos(int value){ link = value; }
        @Override public Color getLinkColor(){ return getColor(getSet()); }
    }

    public static class UnitSet{
        public float level;
        public final Seq<ItemStack> requirements = new Seq<>(ItemStack.class);
        public @NotNull UnitType type;
        public float costTime;
        public int callIns;
        public boolean showText ;
        
        public UnitSet(){this(0, UnitTypes.alpha, 0, 5); }

        public UnitSet(float level, @NotNull UnitType type, float costTime, int callIns, ItemStack... requirements){
            this(level, type, costTime, callIns, true, requirements);
        }
    
        public UnitSet(float level, @NotNull UnitType type, float costTime, int callIns, boolean showText, ItemStack... requirements){
            this.showText = showText;
            this.type = type;
            this.level = level;
            this.costTime = costTime;
            this.callIns = callIns;
            this.requirements.addAll(requirements);
            if(!NHLoader.unitBuildCost.containsKey(type))NHLoader.unitBuildCost.put(type, ItemStack.mult(requirements, 1f / callIns * 20));
        }

        public float costTime(){return costTime * (1 + Vars.state.rules.unitBuildSpeedMultiplier);}
        public ItemStack[] requirements(){ return requirements.toArray(); }
    }
}
