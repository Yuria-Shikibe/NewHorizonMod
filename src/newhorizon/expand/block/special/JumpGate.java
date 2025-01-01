package newhorizon.expand.block.special;

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
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.core.World;
import mindustry.entities.Units;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.Ranged;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;
import mindustry.ui.ItemDisplay;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ContentInfoDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.ItemModule;
import newhorizon.NHVars;
import newhorizon.NewHorizon;
import newhorizon.content.NHFx;
import newhorizon.content.NHTechTree;
import newhorizon.expand.units.unitType.NHUnitType;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.ui.ItemImageDynamic;
import newhorizon.util.ui.NHUIFunc;
import newhorizon.util.ui.TableFunc;

import java.util.Arrays;
import java.util.Objects;

import static mindustry.Vars.*;
import static newhorizon.util.func.NHFunc.regSize;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class JumpGate extends Block {
    protected static ItemModule nextItems;
    protected static final ObjectIntMap<UnitSet> allSets = new ObjectIntMap<>();
    protected static final Seq<IntMap.Entry<UnitSet>> tmpSetSeq = new Seq<>();
    protected static int lastSelectedInt = 0;
    
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
    public final IntMap<UnitSet> calls = new IntMap<>();
    public float squareStroke = 2f;
    
    public float cooldownTime = 300f;
    
    public float buildSpeedMultiplierCoefficient = 1;
    
    protected static UnitSet tmpSet;
    
    protected static int selectID = 0, selectNum = 1;
    
    protected static final Vec2 linkVec = new Vec2();
    protected static final Point2 point = new Point2();
    
    public JumpGate(String name){
        super(name);
        copyConfig = true;
//        highUnloadPriority = false;
        update = true;
        sync = true;
        configurable = true;
        acceptsItems = true;
        unloadable = true;
        solid = true;
        commandable = true;
        hasPower = hasItems = true;
        timers = 3;
        envEnabled = Env.any;
        category = Category.units;
        logicConfigurable = true;
        separateItemCapacity = true;
        group = BlockGroup.units;
        
        consumePowerCond(basePowerDraw, (JumpGateBuild b) -> !b.isCalling());
        
        config(Boolean.class, (JumpGateBuild tile, Boolean i) -> {
            if(i)tile.spawn(tile.getSet());
            else tile.startBuild(0, 0);
        });
        
        config(Point2.class, (Cons2<JumpGateBuild, Point2>)JumpGateBuild::linkPos);
    
        /*[0]for command type;[1]for spawn Hash ID;[2]for spawn num */
        /*
            [0] -> 0 General Start Spawn
            [0] -> 1 Setting Building Plan
            [0] -> ...
         */
        config(IntSeq.class, (JumpGateBuild tile, IntSeq seq) -> {
            if(seq.size < 3)return;
            if(seq.get(0) == 0){
                tile.startBuild(seq.get(1), seq.get(2));
            }else{
                tile.planSpawnID = seq.get(1);
                tile.planSpawnNum = seq.get(2);
            }
        });
        configClear((JumpGateBuild tile) -> tile.startBuild(0, 0));
    }
    
    @Override
    public boolean canReplace(Block other){
        return super.canReplace(other) || (other instanceof JumpGate && size > other.size);
    }
    
    public void drawDefaultPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        TextureRegion reg = getPlanRegion(plan, list);
        Draw.rect(reg, plan.drawx(), plan.drawy(), !rotate || !rotateDraw ? 0 : plan.rotation * 90);
        
        if(plan.worldContext && player != null && teamRegion != null && teamRegion.found()){
            if(teamRegions[player.team().id] == teamRegion) Draw.color(player.team().color);
            Draw.rect(teamRegions[player.team().id], plan.drawx(), plan.drawy());
            Draw.color();
        }
        
        drawPlanConfig(plan, list);
    }
    
    /*
    public boolean canPlaceOn(Tile tile, Team team) {
        if(adaptBase == null || state.rules.infiniteResources)return true;
        CoreBlock.CoreBuild core = team.core();
        if(core == null || (!state.rules.infiniteResources && !core.items.has(requirements, state.rules.buildCostMultiplier))) return false;
    
        if(tile == null) {
            return false;
        }else{
            return adaptBase == tile.block();
        }
    }
    
    @Override
    public void placeBegan(Tile tile, Block previous){
        //finish placement immediately when a block is replaced.
        if(previous instanceof JumpGate){
            tile.setBlock(this, tile.team());
            Fx.placeBlock.at(tile, tile.block().size);
            Fx.upgradeCore.at(tile, tile.block().size);
            
            //set up the correct items
            if(nextItems != null){
                //force-set the total items
                if(tile.build != null){
                    tile.build.items.set(nextItems);
                }
                
                nextItems = null;
            }
        }
    }
    
    public void beforePlaceBegan(Tile tile, Block previous) {
        if(tile == null || tile.build == null || tile.build.core() == null)return;
        ItemModule items = tile.build.core().items();
        if (!Vars.state.rules.infiniteResources && items != null) {
            items.remove(ItemStack.mult(requirements, Vars.state.rules.buildCostMultiplier));
        }
    
        nextItems = tile.build.items;
    
//        if(!NHVars.state.jumpGateUseCoreItems && tile.build != null)nextItems = tile.build.items;
    }*/
    
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        Color color = baseColor == null ? Pal.accent : baseColor;
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, color);
//        if (Vars.world.tile(x, y) != null) {
//            if (!canPlaceOn(Vars.world.tile(x, y), Vars.player.team())) {
//                drawPlaceText(Core.bundle.get((Vars.player.team().core() == null || !Vars.player.team().core().items.has(requirements, Vars.state.rules.buildCostMultiplier)) && !Vars.state.rules.infiniteResources ? "bar.noresources" : "nh-need-base"), x, y, valid);
//            }
//        }
    }
    
    public void addSets(UnitSet... sets){
        for(UnitSet set : sets){
            calls.put(set.hashCode(), set);
        }
    }

    public static boolean hideSet(UnitType type){
        return state.rules.bannedUnits.contains(type) || type.locked() && !state.rules.infiniteResources && state.isCampaign();
    }
    
    @Override
    public void init(){
        super.init();
        if(calls.isEmpty()) throw new IllegalArgumentException("Seq @calls is [red]EMPTY[].");
        for(UnitSet set : calls.values()){
            allSets.put(set, size);
        }
        
        clipSize = size * tilesize * 4;
        if(adaptable)for(UnitSet set : allSets.keys()){
            if(allSets.get(set) >= size)continue;
            calls.put(set.hashCode(), set);
        }
        
        Seq<UnitSet> keys = calls.values().toArray();
        calls.clear();
        keys.sort();
        for(UnitSet set : keys)calls.put(set.hashCode(), set);
    }
    
    public Seq<Integer> getSortedKeys(){
        Seq<UnitSet> keys = calls.values().toArray().sort();
        Seq<Integer> hashs = new Seq<>();
        for(UnitSet set : keys){
            hashs.add(set.hashCode());
        }
        return hashs;
    }
    
    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
    }
    
    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.powerUse, basePowerDraw * 60F, StatUnit.powerSecond);
     
        stats.add(Stat.output, (t) -> {
            t.row().add(Core.bundle.get("editor.spawn") + ":").left().pad(OFFSET).row();
            for(Integer i : getSortedKeys()) {
                UnitSet set = calls.get(i);
                t.add(new NHUIFunc.UnitSetTable(set, table -> {
                    table.button(Icon.infoCircle, Styles.clearNonei, () -> showInfo(set, new Label("[]"), null, null)).size(LEN);
                })).fill().row();
            }
        });
    }

    public void showInfo(UnitSet set, Element extra, @Nullable ItemModule module, @Nullable Team team){
        BaseDialog dialogIn = new BaseDialog("More Info");
        dialogIn.addCloseListener();
        dialogIn.cont.margin(15f);
        if(!mobile){
            dialogIn.cont.marginLeft(220f).marginRight(220f);
        }
        dialogIn.cont.pane(inner -> {
            inner.button(new TextureRegionDrawable(set.type.fullIcon), Styles.clearNonei, () -> new ContentInfoDialog().show(set.type)).growX().fillY().center().row();
            inner.image().growX().height(OFFSET / 4).pad(OFFSET / 4f).color(Pal.accent).row();
            inner.add("[lightgray]" + Core.bundle.get("editor.spawn") + ": [accent]" + set.type.localizedName + "[lightgray] | Tier: [accent]" + set.sortIndex[1]).left().padLeft(OFFSET).row();
            inner.add("[lightgray]" + Core.bundle.get("stat.buildtime") + ": [accent]" + Strings.fixed(set.costTimeVar() / 60, 2) + "[lightgray] " + Core.bundle.get("unit.seconds")).left().padLeft(OFFSET).row();
            inner.image().growX().height(OFFSET / 4).pad(OFFSET / 4f).color(Pal.accent).row();
            inner.table(table -> {
                int index = 0;
                for(ItemStack stack : team == null ? set.baseRequirements() : set.dynamicRequirements(team)){
                    if(module != null || index % 7 == 0)table.row();
                    if(module != null){
                        TableFunc.itemStack(table, stack, module);
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
        addBar("progress",
            (JumpGateBuild entity) -> new Bar(
                () -> entity.isCalling() ?
                        Core.bundle.get("bar.progress") : "[lightgray]" + Iconc.cancel,
                () -> entity.isCalling() && Units.canCreate(entity.team, entity.getType()) ? Pal.power : Pal.redderDust,
                () -> entity.isCalling() ? entity.buildProgress / entity.costTime(entity.getSet(), true) : 0
            )
        );
        addBar("cooldown",
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
        pointerRegion = Core.atlas.find(NewHorizon.name("jump-gate-pointer"));
        arrowRegion = Core.atlas.find(NewHorizon.name("jump-gate-arrow"));
    }

    public class JumpGateBuild extends Building implements Ranged{
        public int spawnID = 0;
        public int link = -1;
        public float buildProgress = 0;
        public float totalProgress;
        public float warmup;
        public boolean jammed;
        
        public float cooldown = 0;
        public boolean cooling = false;
        
        public int spawnNum = 1;
        public int buildingSpawnNum = 0;
        
        public int planSpawnID = 0;
        public int planSpawnNum = 0;
        
        public Vec2 commandPos = null;
    
        @Override
        public void onCommand(Vec2 target){
            hitbox(Tmp.r1);
            if(Tmp.r1.contains(target))commandPos = null;
            else commandPos = target;
        }
    
        @Override
        public Vec2 getCommandPosition(){
            return commandPos;
        }
    
        @Override
        public boolean acceptItem(Building source, Item item){
            return !NHVars.worldData.jumpGateUsesCoreItems && realItems().get(item) < getMaximumAccepted(item);
        }
    
        @Override
        public void created(){
            super.created();
            
            UnitSet set;
            if(!cheating() && (set = calls.get(planSpawnID)) != null && hideSet(set.type))spawnID = 0;
        }
    
        @Override
        public IntSeq config(){
            return IntSeq.with(1, planSpawnID, planSpawnNum);
        }
        
        @Override
        public void updateTile(){
            totalProgress += (efficiency() + warmup) * delta() * Mathf.curve(Time.delta, 0f, 0.5f);
            if(!cooling && isCalling() && Units.canCreate(team, getType())){
                buildProgress += efficiency() * state.rules.unitBuildSpeedMultiplier * delta() * warmup * state.rules.unitBuildSpeed(team);
                if(buildProgress >= costTime(getSet(), true) && !jammed){
                    spawn(getSet());
                }
            }
            
            if(cooling){
                if(Mathf.chanceDelta(0.2f))Fx.reactorsmoke.at(x + Mathf.range(tilesize * size / 2), y + Mathf.range(tilesize * size / 2));
                if(timer.get(0, 4)) for(int i = 0; i < 4; i++){
                    Fx.shootSmallSmoke.at(x, y, i * 90);
                }
                
                cooldown += warmup * delta();
                if(cooldown > cooldownTime){
                    cooling = false;
                    cooldown = 0;
                }
            }
            
            if(efficiency() > 0 && power.status > 0.5f){
                if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
                else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
            }else{
                if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
                else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
            }
            
            if(timer(1, 20) && calls.containsKey(planSpawnID) && planSpawnNum > 0 && power.status > 0.5f && hasConsume(calls.get(planSpawnID), planSpawnNum)){
                if(!isCalling() && !cooling){
                    startBuild(planSpawnID, planSpawnNum);
                }
                
                if(jammed){
                    Tile t = null;
                    while(t == null){
                        Tmp.v1.set(1, 1).rnd(range()).add(this).clamp(0, 0, world.unitWidth(), world.unitHeight());
                        t = world.tile(World.toTile(Tmp.v1.x), World.toTile(Tmp.v1.y));
                    }
                    link = t.pos();
                    spawn(getSet());
                }
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
            DrawFunc.posSquareLink(color, 1.5f, 3.5f, true, this, target);
            Draw.color();
            
            if(core() != null && NHVars.worldData.jumpGateUsesCoreItems)DrawFunc.posSquareLinkArr(color, 1.5f, 3.5f, true, false, this, core());
            
            if(jammed) DrawFunc.overlayText(Core.bundle.get("spawn-error"), x, y, size * tilesize / 2.0F, color, true);
            
            Draw.reset();
        }
    
        public float speedMultiplier(int spawnNum){
            return Mathf.sqrt(spawnNum) * buildSpeedMultiplierCoefficient;
        }
        
        @Override
        public void updateTableAlign(Table table){
            if(NHVars.worldData.jumpGateUsesCoreItems)super.updateTableAlign(table);
            else{
                Vec2 pos = Core.input.mouseScreen(x - block.size * 4f - 1.0F, y);
                table.setPosition(pos.x, pos.y, Align.right);
            }
        }
    
        @Override
        public void buildConfiguration(Table table) {
            BaseDialog dialog = new BaseDialog("@spawn");
            dialog.addCloseListener();
            
            dialog.cont.pane(inner ->
                inner.table(callTable -> {
                    for(Integer hashcode : getSortedKeys()) {
                        UnitSet set = calls.get(hashcode);
                        callTable.table(Tex.pane, info -> {
                            info.add(new NHUIFunc.UnitSetTable(set, table2 -> {
                                table2.button(Icon.infoCircle, Styles.clearNonei, () -> showInfo(set, new Label(() -> ("[lightgray]Construction Available?: " + TableFunc.judge(canSpawn(set, false) && hasConsume(set, spawnNum)))), realItems(), team)).size(LEN);
                                table2.button(Icon.add, Styles.clearNonei, () -> configure(IntSeq.with(0, hashcode, spawnNum))).size(LEN).disabled(b -> (team.data().countType(set.type) + spawnNum > Units.getCap(team)) || jammed || isCalling() || !hasConsume(set, spawnNum) || cooling);
                            })).fillY().growX().row();
                            if(!hideSet(set.type)){
                                Bar unitCurrent = new Bar(() -> Core.bundle.format("bar.unitcap", Fonts.getUnicodeStr(set.type.name), team.data().countType(set.type), Units.getCap(team)), () -> canSpawn(set, false) ? Pal.accent : Units.canCreate(team, set.type) ? Pal.ammo : Pal.redderDust, () -> (float)team.data().countType(set.type) / Units.getCap(team));
                                info.add(unitCurrent).growX().height(LEN - OFFSET);
                            }
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
                    l.setText("[gray]<" + Core.bundle.get("filter.option.amount") + ": [lightgray]" + spawnNum + "[] | " + Core.bundle.get("stat.buildspeedmultiplier") + ": [lightgray]" + Strings.fixed(speedMultiplier(spawnNum), 2) + "[]>");
                    s.setValue(spawnNum);
                });
                
                Stack stack = new Stack(s, new Table(ta -> {
                    ta.center();
                    ta.add(l);
                }));
                
                t.add(stack).growX().height(LEN).padRight(OFFSET).padLeft(OFFSET).align(Align.center);
                if(Core.graphics.isPortrait()){
                    t.row().add().height(6f).growX();
                    t.row();
                }
                t.add(new Bar(
                    () ->
                        !isCalling() ? "[lightgray]" + Iconc.cancel :
                        Units.canCreate(team, getType()) && !jammed ? "[lightgray]" + Core.bundle.get("editor.spawn") + ": [accent]" + buildingSpawnNum + "[]* [accent]" + getSet().type.localizedName + "[lightgray]" + (mobile ? "\n" : " | ") + Core.bundle.get("ui.remain-time") + ": [accent]" + (int)Math.max( (costTime(getSet(), true) - buildProgress) / Time.toSeconds / state.rules.unitBuildSpeedMultiplier, 0) + "[lightgray] " + Core.bundle.get("unit.seconds") :
                        "[red]Call Jammed",
                    () -> isCalling() && canSpawn(getSet(), true) && !jammed ? Pal.power : Pal.redderDust,
                    () -> isCalling() ? buildProgress / costTime(getSet(), true) : 0
                )).growX().height(LEN);
            }).growX().height(LEN).row();
            dialog.cont.table(t -> {
                t.button("@back", Icon.left, Styles.cleart, dialog::hide).padTop(OFFSET / 2).marginLeft(OFFSET).growX().height(LEN);
                t.button("@cancel", Icon.cancel, Styles.cleart, () -> configure(false)).marginLeft(OFFSET).padTop(OFFSET / 2).disabled(b -> !isCalling()).growX().height(LEN);
                t.button("@release", Icon.add, Styles.cleart, () -> configure(true)).marginLeft(OFFSET).padTop(OFFSET / 2).disabled(b -> getSet() == null || !jammed).growX().height(LEN);
            }).growX().height(LEN).bottom();
            dialog.keyDown(c -> {
                if(c == KeyCode.left)spawnNum = Mathf.clamp(--spawnNum, 1, Mathf.clamp(Units.getCap(team), 1, maxSpawnPerOne));
                if(c == KeyCode.right)spawnNum = Mathf.clamp(++spawnNum, 1, Mathf.clamp(Units.getCap(team), 1, maxSpawnPerOne));
            });
            
            table.table(Tex.paneSolid, t -> {
                t.button("@spawn", Icon.add, Styles.cleart, dialog::show).size(LEN * 5, LEN).row();
                t.button("@mod.ui.select-target", Icon.move, Styles.cleart, () -> TableFunc.selectPos(table, this::configure)).size(LEN * 5, LEN).row();
                t.button("@settings", Icon.settings, Styles.cleart, () -> new BaseDialog("@settings"){{
                    Label l = new Label(""), currentPlan = new Label("");
                    Slider s = new Slider(1, Mathf.clamp(Units.getCap(team), 1, maxSpawnPerOne), 1, false);
                    s.moved((i) -> selectNum = (int)i);
                    cont.update(() -> {
                        if(calls.get(planSpawnID) == null || planSpawnNum < 1){
                            currentPlan.setText("None");
                        }else{
                            currentPlan.setText(Core.bundle.get("editor.spawn") + ": [accent]" + planSpawnNum + "[]* [accent]" + calls.get(planSpawnID).type.localizedName);
                        }
                        l.setText("[gray]<" + Core.bundle.get("filter.option.amount") + ": [lightgray]" + selectNum + "[] | " + Core.bundle.get("stat.buildspeedmultiplier") + ": [lightgray]" + Strings.fixed(speedMultiplier(selectNum), 2)+ "[]>");
                        s.setValue(selectNum);
                    });
                    cont.table(t -> {
                        tmpSetSeq.clear();
                        for(IntMap.Entry<UnitSet> entry : calls){
                            IntMap.Entry<UnitSet> entryN = new IntMap.Entry<>();
                            entryN.key = entry.key;
                            entryN.value = entry.value;
                            tmpSetSeq.add(entryN);
                        }
    
                        tmpSetSeq.sortComparing(s1 -> s1.value);
                        lastSelectedInt = 0;
                        
                        if(!Core.graphics.isPortrait())t.marginLeft(LEN * 2).marginRight(LEN * 2);
                        t.pane(table -> {
                            for(int i = 0; i < tmpSetSeq.size; i++){
                                IntMap.Entry<UnitSet> entry = tmpSetSeq.get(i);
                                UnitSet set = entry.value;
                                if(hideSet(set.type))continue;
                                int finalI = i;
                                table.table(Tex.whiteui, in -> {
                                    in.marginLeft(6).marginRight(6);
                                    
                                    in.update(() -> {
                                        if(planSpawnID == entry.key){
                                            if(planSpawnNum > 0){
                                                if(hasConsume(set, planSpawnNum))in.color.set(Pal.accent);
                                                else in.color.set(Pal.ammo);
                                            }else in.color.set(Pal.lightishGray);
                                        }else if(selectID == entry.key){
                                            in.color.set(Pal.accent).lerp(Pal.gray, 0.5f);
                                        }else in.color.set(Pal.gray);
                                    });
                                    
                                    in.button(new TextureRegionDrawable(set.type.fullIcon), Styles.emptyi, LEN, () -> {
                                        selectID = entry.key;
                                        lastSelectedInt = finalI;
                                    }).padRight(OFFSET * 2).padLeft(4f);
                                    in.add(set.type.localizedName).left().fill();
                                    in.table(ta -> {
                                        ta.right();
                                        for(ItemStack stack : set.dynamicRequirements(team)){
                                            ta.add(new ItemImageDynamic(stack.item, () -> stack.amount * selectNum, realItems())).padRight(OFFSET / 2).left();
                                        }
                                    }).growX().height(LEN).right();
                                }).growX().height(LEN + OFFSET).padBottom(OFFSET).row();
                            }
    
                            lastSelectedInt = 0;
                        }).grow().row();
                        t.add(new Stack(s, new Table(ta -> {
                            ta.center();
                            ta.add(l);
                        }))).growX().height(LEN).center().row();
                        
                        t.table(Styles.grayPanel, cur -> {
                            cur.image(Icon.rightOpen).padLeft(OFFSET).padRight(16f);
                            cur.add(currentPlan).growX().fillY().row();
                        }).fillX().height(LEN).row();
                        
                        
    
                        t.table(t1 -> {
                            t1.button("@back", Icon.left, Styles.cleart, this::hide).marginLeft(OFFSET).growX().height(LEN);
                            t1.button("@cancel", Icon.cancel, Styles.cleart, () -> configure(IntSeq.with(1, 0, 0))).marginLeft(OFFSET).growX().height(LEN);
                            t1.button("@confirm", Icon.cancel, Styles.cleart, () -> configure(IntSeq.with(1, selectID, selectNum))).marginLeft(OFFSET).growX().height(LEN);
                        }).growX().fillY();
                    }).grow().row();
                    
                    addCloseListener();
                    
                    
                    keyDown(c -> {
                        if(c == KeyCode.backspace)configure(IntSeq.with(1, 0, 0));
                        if(c == KeyCode.enter)configure(IntSeq.with(1, selectID, selectNum));
    
                        if(c == KeyCode.down){
                            if(lastSelectedInt < tmpSetSeq.size - 1){
                                selectID = tmpSetSeq.get(++lastSelectedInt).key;
                            }else{
                                selectID = tmpSetSeq.get(lastSelectedInt = 0).key;
                            }
                        }
    
                        if(c == KeyCode.up){
                            if(lastSelectedInt > 0){
                                selectID = tmpSetSeq.get(--lastSelectedInt).key;
                            }else{
                                selectID = tmpSetSeq.get(lastSelectedInt = tmpSetSeq.size - 1).key;
                            }
                        }
    
                        if(c == KeyCode.left)selectNum = Mathf.clamp(--selectNum, 1, Mathf.clamp(Units.getCap(team), 1, maxSpawnPerOne));
                        if(c == KeyCode.right)selectNum = Mathf.clamp(++selectNum, 1, Mathf.clamp(Units.getCap(team), 1, maxSpawnPerOne));
                    });
                }}.show()).size(LEN * 5, LEN);
            }).fill();
        }
    
        @Override
        public void draw(){
            super.draw();
            Draw.z(Layer.bullet);
            float scl = warmup * atlasSizeScl;
            Lines.stroke(squareStroke * warmup, getColor(getSet()));
            float rot = totalProgress;
            Lines.square(x, y, block.size * tilesize / 2.5f, -rot);
            Lines.square(x, y, block.size * tilesize / 2f, rot);
            for(int i = 0; i < 4; i++){
                float length = tilesize * block().size / 2f + 8f;
                Tmp.v1.trns(i * 90 + rot, -length);
                Draw.rect(arrowRegion,x + Tmp.v1.x,y + Tmp.v1.y, arrowRegion.width * Draw.scl * scl, arrowRegion.height * Draw.scl * scl, i * 90 + 90 + rot);
                float sin = Mathf.absin(totalProgress, 16f, tilesize);
                length = tilesize * block().size / 2f + 3 + sin;
                float signSize = 0.75f + Mathf.absin(totalProgress + 8f, 8f, 0.15f);
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
                        float f = (100 - (totalProgress - 25 * i) % 100) / 100;
                        Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, pointerRegion.width * regSize * f * scl, pointerRegion.height * regSize * f * scl, angle - 90);
                    }
                }
                if(jammed || !Units.canCreate(team, getType())){
                    Draw.color(getColor(getSet()));
                    float signSize = 0.75f + Mathf.absin(totalProgress + 8f, 8f, 0.15f);
                    for (int i = 0; i < 4; i++) {
                        Draw.rect(arrowRegion, x , y, arrowRegion.width * Draw.scl * signSize * scl, arrowRegion.height * Draw.scl * signSize * scl, 90 * i);
                    }
                }
                DrawFunc.circlePercent(x, y, size * tilesize / 1.5f, buildProgress / costTime(getSet(), true), 0);
            }
            Draw.reset();
            
            Drawf.light(tile, size * tilesize * 4 * warmup, team.color, 0.95f);
        }

        public void consumeItems(){
            if(!cheating())realItems().remove(ItemStack.mult(getSet().dynamicRequirements(team), buildingSpawnNum));
        }

        public boolean hasConsume(UnitSet set, int num){
            if(set == null || cheating() || (!state.rules.pvp && team == state.rules.waveTeam))return true;
            return realItems().has(ItemStack.mult(set.dynamicRequirements(team), num * state.rules.teams.get(team).unitCostMultiplier));
        }

        public float costTime(UnitSet set, boolean buildingParma){
            return (buildingParma ? buildingSpawnNum : spawnNum) * set.costTime() / speedMultiplier(buildingParma ? buildingSpawnNum : spawnNum);
        }
        
        public boolean canSpawn(UnitSet set, boolean buildingParma) {
            return team.data().countType(set.type) + (buildingParma ? buildingSpawnNum : spawnNum) <= Units.getCap(team);
        }
        
        public void startBuild(int set, int spawnNum){
            jammed = false;
            
            if(isCalling())cooling = true;
            
            if(!calls.keys().toArray().contains(set)){
                if(isCalling()){
                    if(getSet() != null){
                        Building target = NHVars.worldData.jumpGateUsesCoreItems && team.data().hasCore() ? team.core() : self();
                        
                        for(ItemStack stack : ItemStack.mult(getSet().dynamicRequirements(team), buildingSpawnNum * (costTime(getSet(), true) - buildProgress) / costTime(getSet(), true))){
                            realItems().add(stack.item, Math.min(stack.amount, target.getMaximumAccepted(stack.item) - realItems().get(stack.item)));
                        }
                    }
                }
                
                spawnID = 0;
                buildProgress = 0;
            }else{
                spawnID = set;
                buildProgress = 1;
                buildingSpawnNum = spawnNum;
                consumeItems();
            }
        }
        
        public void spawn(UnitSet set){
            if(!isValid())return;
            boolean success;
            
            Vec2 target = link();
            
            NHFx.spawn.at(x, y, regSize(set.type), team.color, this);
    
            success = NHFunc.spawnUnit(team, target.x, target.y, angleTo(target), spawnRange, spawnReloadTime, spawnDelay, getType(), buildingSpawnNum, s -> {
                if(commandPos != null)s.commandPos.set(commandPos);
            });
            
            if(success){
                buildProgress = 0;
                spawnID = 0;
                buildingSpawnNum = spawnNum;
                jammed = false;
                cooling = true;
            }else jammed = true;
        }
    
        @Override public float range(){return range;}
        @Override public void write(Writes write) {
            write.s(0);
            
            write.i(spawnID);
            write.i(link);
            write.f(buildProgress);
            write.f(warmup);
            write.i(buildingSpawnNum);
            
            write.bool(cooling);
            write.f(cooldown);
    
            write.i(planSpawnID);
            write.i(planSpawnNum);
            
            TypeIO.writeVecNullable(write, commandPos);
        }
        
        @Override public void read(Reads read, byte revision) {
            short ver = read.s();
            
            spawnID = read.i();
            link = read.i();
            buildProgress = read.f();
            warmup = read.f();
            buildingSpawnNum = read.i();
            
            cooling = read.bool();
            cooldown = read.f();
            
            planSpawnID = read.i();
            planSpawnNum = read.i();
    
            commandPos = TypeIO.readVecNullable(read);
        }
    
        @Override
        public float warmup(){
            return warmup;
        }
    
        @Override
        public float progress(){
            return buildProgress /  costTime(getSet(), true);
        }
    
        @Override
        public float totalProgress(){
            return totalProgress;
        }
    
        @Override
        public void displayBars(Table table){
            super.displayBars(table);
            table.row().table(t -> {
                t.left();
                t.label(() -> "[lightgray]Constructing: [accent]" + (getType() == null ? Core.bundle.get("none") : getType().localizedName)).pad(OFFSET / 2f);
                t.image(() -> getType() == null ? Icon.cancel.getRegion() : getType().uiIcon).size(LEN - OFFSET).scaling(Scaling.fit);
            }).growX().fillY().visible(this::isCalling);
        }
    
        public boolean isCalling(){return calls.containsKey(spawnID);}
        public UnitType getType(){
            UnitSet set = calls.get(spawnID);
            return set == null ? null : set.type;
        }
        public UnitSet getSet(){
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
                link = point.set((int)World.conv(x + Tmp.v1.x), (int)World.conv(y + Tmp.v1.y)).pack();
            }else link = pos();
        }
    
        @Override
        public boolean cheating(){
            return super.cheating();// || (team == state.rules.waveTeam && !state.rules.pvp || state.rules.infiniteResources);
        }
        
        public ItemModule realItems(){
            return NHVars.worldData.jumpGateUsesCoreItems && team.data().hasCore() ? team.core().items() : items;
        }
    }
    
    public static class UnitSet implements Comparable<UnitSet>{
        public final Seq<ItemStack> requirements = new Seq<>(ItemStack.class);
        public UnitType type;
        public float costTime;
        
        //[0] -> Line or Type; [1] -> Tier.
        public final byte[] sortIndex;
        
        public UnitSet(){this(UnitTypes.alpha, new byte[]{-1, -1}, 0); }
    
        public UnitSet(UnitType type, byte[] sortIndex, float costTime, ItemStack... requirements){
            Arrays.sort(requirements, Structs.comparingInt(j -> j.item.id));
            this.type = type;
            this.sortIndex = sortIndex;
            this.costTime = costTime;
            this.requirements.addAll(requirements);
            if(!NHTechTree.unitBuildCost.containsKey(type)){
                NHTechTree.unitBuildCost.put(type, ItemStack.mult(requirements, 15));
               if(type instanceof NHUnitType){
                   ((NHUnitType)type).setRequirements(requirements);
               }
            }
        }
    
        @Override
        public boolean equals(Object o){
            if(this == o) return true;
            if(!(o instanceof UnitSet)) return false;
            UnitSet set = (UnitSet)o;
            return type.equals(set.type) && Arrays.equals(sortIndex, set.sortIndex);
        }
    
        @Override
        public String toString(){
            return "UnitSet{" + "type=" + type + ", sortIndex=" + Arrays.toString(sortIndex) + '}';
        }
    
        @Override
        public int hashCode(){
            int result = Objects.hash(type.name.hashCode());
            result = 31 * result + Arrays.hashCode(sortIndex);
            return result;
        }
    
        public float costTime(){return costTime;}
        public float costTimeVar(){return costTime / state.rules.unitBuildSpeedMultiplier;}
        public ItemStack[] baseRequirements(){
            return requirements.toArray();
        }
        
        public ItemStack[] dynamicRequirements(Team team){
            return ItemStack.mult(requirements.toArray(), state.rules.unitCost(team));
        }
    
        @Override
        public int compareTo(UnitSet set2){
            return sortIndex[0] - set2.sortIndex[0] == 0 ? sortIndex[1] - set2.sortIndex[1] : sortIndex[0] - set2.sortIndex[0];
        }
    }
    
}
