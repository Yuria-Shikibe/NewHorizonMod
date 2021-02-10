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
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.Vars;
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
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.ui.ItemDisplay;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.Stat;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.func.DrawFuncs;
import newhorizon.func.Functions;
import newhorizon.func.TableFuncs;

import static mindustry.Vars.*;
import static newhorizon.func.Functions.regSize;
import static newhorizon.func.TableFuncs.LEN;
import static newhorizon.func.TableFuncs.OFFSET;

public class JumpGate extends Block {
    public float spawnDelay = 5f;
    public float spawnReloadTime = 180f;
    public float spawnRange = 120f;
    public float range = 200f;
    public float inComeVelocity = 5f;
    public TextureRegion
            pointerRegion,
            arrowRegion,
            bottomRegion,
            armorRegion;
    public Color baseColor;
    public final Seq<UnitSet> calls = new Seq<>();

    public float squareStroke = 2f;

    public JumpGate(String name){
        super(name);
        update = true;
        configurable = true;
        solid = true;
        hasPower = true;
        this.category = Category.units;
    }

    public void addSets(UnitSet... sets){
        calls.addAll(sets);
    }

    @Override
    public void init(){
        super.init();
        if(calls.isEmpty()) throw new IllegalArgumentException("Seq @calls is [red]EMPTY[].");
    }

    @Override
    public void setStats() {
        super.setStats();
        this.stats.add(Stat.output, (t) -> {
            t.row().add("[gray]Summon Types:").left().pad(TableFuncs.OFFSET).row();
            for(UnitSet set : calls) {
                if(set.type.locked() && !state.rules.infiniteResources){
                    t.table(Tex.button, t2 -> {
                        t2.table(Tex.button, table2 -> table2.image(Icon.lock).size(LEN).center()).left().size(LEN + TableFuncs.OFFSET * 1.5f).pad(TableFuncs.OFFSET);

                        t2.pane(table2 -> table2.add("[gray]Need to be researched.").left().row()).size(LEN * 6f, LEN).left().pad(TableFuncs.OFFSET);

                        t2.table(table2 -> table2.image(Icon.lock).size(LEN).center()).height(LEN + TableFuncs.OFFSET).disabled(b -> true).growX().left().pad(TableFuncs.OFFSET);
                    }).fillX().growY().padBottom(TableFuncs.OFFSET / 2).row();
                }else{
                    t.table(Tex.button, t2 -> {
                        t2.table(Tex.button, table2 -> table2.image(set.type.icon(Cicon.full)).size(LEN).center()).left().size(LEN + TableFuncs.OFFSET * 1.5f).pad(TableFuncs.OFFSET);

                        t2.pane(table2 -> {
                            table2.add("[lightgray]Summon: [accent]" + set.type.localizedName + "[lightgray]; Level: [accent]" + set.level + "[].").left().row();
                            table2.add("[lightgray]NeededTime: [accent]" + TableFuncs.format(set.costTime() / 60) + "[lightgray] sec[]").left().row();
                        }).size(LEN * 6f, LEN).left().pad(TableFuncs.OFFSET);

                        t2.table(table2 -> table2.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(set, "[accent]Caution[]: Summon needs building.")).size(LEN)).height(LEN + TableFuncs.OFFSET).growX().left().pad(TableFuncs.OFFSET);
                    }).fillX().growY().padBottom(TableFuncs.OFFSET / 2).row();
                }
            }
        });
    }

    public void showInfo(UnitSet set, String textExtra){
        BaseDialog dialogIn = new BaseDialog("More Info");
        dialogIn.addCloseListener();
        dialogIn.cont.margin(15f);
        dialogIn.cont.table(Tex.button, t -> t.image(set.type.icon(Cicon.full)).center()).grow().row();
        dialogIn.cont.add("<<[accent] " + set.type.localizedName + " []>>").row();
        dialogIn.cont.add("[lightgray]Call: [accent]" + set.type.localizedName + "[lightgray]; Level: [accent]" + set.level + "[]; Call num: [accent]" + set.callIns + "[].").left().padLeft(TableFuncs.OFFSET).row();
        dialogIn.cont.add("[lightgray]BuildNeededTime: [accent]" + TableFuncs.format(set.costTime() / 60) + "[lightgray] sec[]").left().padLeft(TableFuncs.OFFSET).row();
        dialogIn.cont.pane(table -> {
            int index = 0;
            for(ItemStack stack : set.requirements()){
                if(index % 5 == 0)table.row();
                table.add(new ItemDisplay(stack.item, stack.amount, false)).padRight(5).left();
                index ++;
            }
        }).left().padLeft(TableFuncs.OFFSET).row();
        if(!textExtra.equals(""))dialogIn.cont.add(textExtra).left().padLeft(TableFuncs.OFFSET).row();
        dialogIn.cont.image().fillX().pad(2).height(4f).color(Pal.accent);
        dialogIn.cont.row();
        dialogIn.cont.button("@back", Icon.left, dialogIn::hide).size(LEN * 2.5f, LEN).pad(TableFuncs.OFFSET / 3);
        dialogIn.show();
    }

    @Override
    public void setBars() {
        super.setBars();
        bars.add("progress",
                (JumpGateBuild entity) -> new Bar(
                        () -> "Progress",
                        () -> Pal.power,
                        () -> entity.getSet() == null ? 0 : entity.buildReload / entity.getSet().costTime()
                )
        );
    }

    @Override
    public void load(){
        super.load();
        pointerRegion = Core.atlas.find(this.name + "-pointer");
        arrowRegion = Core.atlas.find(this.name + "-arrow");
        bottomRegion = Core.atlas.find(this.name + "-bottom");
        armorRegion = Core.atlas.find(this.name + "-armor");
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        Color color = baseColor == null ? Pal.accent : baseColor;
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, color);
    }

    @Override
    protected TextureRegion[] icons() {
        return this.teamRegion.found() && this.minfo.mod == null ? new TextureRegion[]{this.bottomRegion, this.region, this.teamRegions[Team.sharded.id], armorRegion} : new TextureRegion[]{this.bottomRegion, this.region, armorRegion};
    }

    public class JumpGateBuild extends Building implements Ranged {
        public Color baseColor(){
            return baseColor == null ? this.team().color : baseColor;
        }
        public int spawnID = -1;
        public int spawnPOS = -1;
        public int spawns = 1;

        public float buildReload = 0f;

        public float progress;
        
        protected boolean success, error;

        @Override
        public void updateTile(){
            super.updateTile();
            if(hasConsume(getSet()))progress += efficiency();
            if(isCalling()){
                this.buildReload += efficiency() * (state.rules.infiniteResources ? Float.MAX_VALUE : 1);
                if(this.buildReload >= getSet().costTime() && hasConsume(getSet()) && !error){
                    spawn(getSet());
                }
            }
        }

        public UnitType getType(){ return calls.get(spawnID).type; }
        
        public void setTarget(int pos){ this.spawnPOS = pos; }
        
        public Building target(){ return Vars.world.build(spawnPOS); }

        public UnitSet getSet(){
            if(spawnID < 0 || spawnID >= calls.size)return null;
            return calls.get(spawnID);
        }

        public Color getColor(UnitSet set){
            if(set == null)return baseColor();
            return (isCalling() && (!hasConsume(set) || !success)) ? baseColor().cpy().lerp(Pal.ammo, 1 / Mathf.clamp((efficiency() + 1), 0, 2)) : baseColor();
        }

        @Override
        public void drawConfigure() {
            Drawf.dashCircle(x, y, range(), baseColor());
            Draw.color(baseColor());
            Lines.square(x, y, block().size * tilesize / 2f + 1.0f);
            if(target() != null) {
                Building target = target();
                Draw.alpha(0.3f);
                Fill.square(target.x, target.y, target.block.size / 2f * tilesize);
                Draw.alpha(1f);
                Drawf.dashCircle(target.x, target.y, spawnRange, baseColor());
                Draw.color(baseColor());
                Lines.square(target.x, target.y, target.block().size * tilesize / 2f + 1.0f);

                DrawFuncs.posSquareLinkArr(getColor(getSet()), 1.5f, 3.5f, true, false, this, target, core());
                Drawf.arrow(x, y, target.x, target.y, 15f, 6f, getColor(getSet()));

            }else Drawf.dashCircle(x, y, spawnRange, baseColor());
            
            if(error){
                DrawFuncs.overlayText(Core.bundle.get("spawn-error"), x, y, size * tilesize / 2.0F, getColor(getSet()));
            }
            
            Draw.reset();
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            if (this == other || this.spawnPOS == other.pos()) {
                setTarget(-1);
                return false;
            }
            if (other.within(this, range())) {
                setTarget(other.pos());
                return false;
            }
            return true;
        }
        
        @Override
        public void buildConfiguration(Table table) {
            BaseDialog dialog = new BaseDialog("Call");
            dialog.addCloseListener();

            dialog.cont.table(t -> {
                t.table(inner -> {
                    inner.button("@back", Icon.left, dialog::hide).padBottom(TableFuncs.OFFSET / 2).fillX().height(LEN).row();
                    inner.button("@release", Icon.add, () -> spawn(getSet())).padBottom(TableFuncs.OFFSET / 2).disabled(b -> getSet() == null ||success || !hasConsume(getSet()) || !canSpawn(getSet())).fillX().height(LEN).row();
                    for(UnitSet set : calls) {
                        if(set.type.locked() && !state.rules.infiniteResources){
                            inner.table(Tex.buttonSquareDown, t2 -> {
                                t2.table(Tex.clear, table2 -> table2.image(Icon.lock).size(LEN).center()).left().size(LEN + TableFuncs.OFFSET * 1.5f).pad(TableFuncs.OFFSET);

                                t2.pane(table2 -> table2.add("[gray]Need to be researched.").left().row()).size(LEN * 6f, LEN).left().pad(TableFuncs.OFFSET);

                                t2.table(table2 -> table2.image(Icon.lock).size(LEN).center()).height(LEN + TableFuncs.OFFSET).disabled(b -> true).growX().left().pad(TableFuncs.OFFSET);
                            }).fillX().height(LEN).padBottom(TableFuncs.OFFSET / 2).row();
                        }else {
                            inner.table(Tex.button, t2 -> {
                                t2.table(Tex.clear, table2 -> table2.image(set.type.icon(Cicon.full)).size(LEN).center()).left().grow().pad(TableFuncs.OFFSET);

                                t2.pane(table2 -> {
                                    table2.add("[lightgray]Call: [accent]" + set.type.localizedName + "[lightgray]; Level: [accent]" + set.level + "[].").left().row();
                                    table2.add("[lightgray]NeededTime: [accent]" + TableFuncs.format(set.costTime() / 60) + "[lightgray] sec[]").left().row();
                                }).size(LEN * 6f, LEN).left().pad(TableFuncs.OFFSET);

                                t2.table(Tex.clear, table2 -> {
                                    table2.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(set, "[lightgray]CanCall?: " + TableFuncs.getJudge(canSpawn(set)) + "[]")).size(LEN);
                                    table2.button(Icon.add, Styles.clearPartiali, () -> startBuild(set)).size(LEN).disabled(b -> !canSpawn(set));
                                }).height(LEN).growX().left().pad(TableFuncs.OFFSET);
                            }).fillX().height(LEN + OFFSET).padBottom(TableFuncs.OFFSET / 2).row();
                        }
                    }
                }).fillX().height(Core.graphics.getHeight() - LEN * 3).row();
            }).fill();

            table.button("Spawn", Icon.add, dialog::show).size(LEN * 5, LEN);
        }

        @Override
        public void draw(){
            Draw.rect(bottomRegion, x, y);
            super.draw();
            Draw.rect(armorRegion, x, y);
            Draw.reset();
            Draw.z(Layer.bullet);
            if(efficiency() > 0){
                Lines.stroke(squareStroke, getColor(getSet()));
                float rot = progress;
                Lines.square(x, y, block.size * tilesize / 2.5f, -rot);
                Lines.square(x, y, block.size * tilesize / 2f, rot);
                for(int i = 0; i < 4; i++){
                    float length = tilesize * block().size / 2f + 8f;
                    Tmp.v1.trns(i * 90 + rot, -length);
                    Draw.rect(arrowRegion,x + Tmp.v1.x,y + Tmp.v1.y,i * 90 + 90 + rot);
                    float sin = Mathf.absin(progress, 16f, tilesize);
                    length = tilesize * block().size / 2f + 3 + sin;
                    float signSize = 0.75f + Mathf.absin(progress + 8f, 8f, 0.15f);
                    Tmp.v1.trns(i * 90, -length);
                    Draw.rect(pointerRegion, x + Tmp.v1.x,y + Tmp.v1.y, pointerRegion.width * Draw.scl * signSize, pointerRegion.height * Draw.scl * signSize, i * 90 + 90);
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
                        Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, pointerRegion.width * regSize * f, pointerRegion.height * regSize * f, angle - 90);
                    }
                }

                if(!success || !hasConsume(getSet())){
                    Draw.color(getColor(getSet()));
                    float signSize = 0.75f + Mathf.absin(progress + 8f, 8f, 0.15f);
                    for (int i = 0; i < 4; i++) {
                        Draw.rect(arrowRegion, x , y, arrowRegion.width * Draw.scl * signSize, arrowRegion.height * Draw.scl * signSize, 90 * i);
                    }
                }
            }
            Draw.reset();
        }
        @Override public float range(){return range;}
        @Override public void write(Writes write) {
            write.i(this.spawnID);
            write.i(this.spawnPOS);
            write.i(this.spawns);
            write.f(this.buildReload);
        }
        @Override public void read(Reads read, byte revision) {
            this.spawnID = read.i();
            this.spawnPOS = read.i();
            this.spawns = read.i();
            this.buildReload = read.f();
        }

        public boolean isCalling(){ return spawnID >= 0; }

        public boolean coreValid() { return this.team.core() != null && this.team.core().items != null && !this.team.core().items.empty(); }

        public void consumeItems(){
            if(coreValid()){
                int i = 0;
                for(ItemStack stack : getSet().requirements()){
                    Delivery.DeliveryData data = Pools.obtain(Delivery.DeliveryData.class, Delivery.DeliveryData::new);
                    Log.info(stack);
                    data.items[Vars.content.items().indexOf(stack.item)] = stack.amount;
                    i ++;
                    Time.run(i * spawnDelay, () -> {
                        Tmp.v1.rnd(block().size * tilesize / 2f).add(this);
                        if(isValid())NHContent.deliveryBullet.create(this, team, Tmp.v1.x, Tmp.v1.y, angleTo(core()), 1f, 1f, 10000f, data);
                    });
                }
                
                if(!state.rules.infiniteResources)this.team.core().items.remove(getSet().requirements());
            }
        }

        public boolean hasConsume(UnitSet set){
            if(set == null || state.rules.infiniteResources)return true;
            if(!coreValid())return false;
            CoreBlock.CoreBuild core = this.team.core();
            return core.items.has(set.requirements());
        }

        public boolean canSpawn(UnitSet set) {
            return Units.canCreate(team, set.type) && (state.rules.infiniteResources ||
                (coreValid() && ! isCalling() && hasConsume(set)
            ));
        }

        public void startBuild(UnitSet set){
            this.spawnID = calls.indexOf(set);
            ui.showInfoPopup("[accent]<<Caution>>[]:Team : " + team.name + "[] starts summon level[accent] " + set.level + " []fleet.", 8f, 0, 20, 20, 20, 20);
        }

        public void spawn(UnitSet set){
            if(!isValid() || !Units.canCreate(team, set.type))return;
            
            float Sx, Sy;
            int spawnNum = set.callIns;
            if(team.data().countType(set.type) + spawnNum > Units.getCap(team)){
                spawnNum = Units.getCap(team) - team.data().countType(set.type);
            }

            if(target() != null) {
                Building target = target();
                Sx = target.x;
                Sy = target.y;
            }else{
                Sx = x;
                Sy = y;
            }

            NHFx.spawn.at(x, y, regSize(set.type), baseColor(), this);
            success = Functions.spawnUnit(this, Sx, Sy, spawnNum, set.level, spawnRange, spawnReloadTime, spawnDelay, inComeVelocity, set.type, baseColor());
            if(success){
                consumeItems();
                this.spawns = set.callIns;
                this.buildReload = 0;
                this.spawnID = -1;
                success = false;
                error = false;
            }else error = true;
        }
    }

    public static class UnitSet{
        public int level;
        public final Seq<ItemStack> requirements = new Seq<>(ItemStack.class);
        public UnitType type;
        public float costTime = 60f;
        public int callIns = 5;

        public UnitSet(){this(0, UnitTypes.alpha, 0, 5); }

        public UnitSet(int level, UnitType type){
            this.type = type;
            this.level = level;
            this.requirements.add(new ItemStack(NHItems.emergencyReplace, 0));
        }

        public UnitSet(int level, UnitType type, float costTime, int callIns, ItemStack... requirements){
            this.type = type;
            this.level = level;
            this.costTime = costTime;
            this.callIns = callIns;
            this.requirements.addAll(requirements);
        }

        public float costTime(){return /*Vars.state.rules.infiniteResources ? 0f :*/ costTime * (1 + Vars.state.rules.unitBuildSpeedMultiplier);}
        public ItemStack[] requirements(){ return requirements.toArray(); }
    }
}
