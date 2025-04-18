package newhorizon.expand.block.special;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.Ranged;
import mindustry.type.ItemStack;
import mindustry.type.PayloadSeq;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NHGroups;
import newhorizon.content.NHFx;
import newhorizon.content.blocks.ModuleBlock;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static mindustry.Vars.*;
import static newhorizon.NHVars.worldData;

public class AssignedBeacon extends Block {
    //public static final Seq<Building> likedBuildings = Seq.with();
    public float range = 60f;
    public float powerCons = 600 / 60f;
    public int maxLink = 4;
    public int moduleSlot = 2;

    public Color overdriveColor = Color.valueOf("feb380");

    public AssignedBeacon(String name) {
        super(name);
        solid = true;
        update = true;
        configurable = true;
        saveConfig = true;
        copyConfig = true;

        schematicPriority = 30;

        size = 3;

        config(Block.class, (AssignedBeaconBuild entity, Block block) -> entity.addModulePlan(entity.getModuleId(block)));
        config(Integer.class, AssignedBeaconBuild::addLink);
        config(Integer[].class, (AssignedBeaconBuild entity, Integer[] data) -> {
            if (data.length != maxLink + moduleSlot) {
                Log.info("[AssignedBeacon] Invalid config: " + Arrays.toString(data));
                return;
            }
            for (int i = 0; i < moduleSlot; i++){
                entity.modulePlans[i] = data[i];
            }
            for(int i = 0; i < maxLink; i++){
                Point2 p = Point2.unpack(data[i + moduleSlot]);
                entity.addLink(Point2.pack(p.x + entity.tileX(), p.y + entity.tileY()));
            }
        });
        configClear((AssignedBeaconBuild entity) -> Arrays.fill(entity.targets, -1));

        consumePowerDynamic((AssignedBeaconBuild entity) -> entity.powerMul * powerCons);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("speedMul", (AssignedBeaconBuild e) -> new Bar(
                () -> Core.bundle.format("nh.bar.speed-boost", Strings.autoFixed((e.speedMul - 1f) * 100, 0)),
                () -> Pal.techBlue,
                () -> 1f
        ));
        addBar("productivityMul", (AssignedBeaconBuild e) -> new Bar(
                () -> Core.bundle.format("nh.bar.productivity-boost", Strings.autoFixed((e.craftMul) * 100, 0)),
                () -> Pal.ammo,
                () -> 1f
        ));
        addBar("powerMul", (AssignedBeaconBuild e) -> new Bar(
                () -> Core.bundle.format("nh.bar.power-multiplier", Strings.autoFixed(e.powerMul, 1)),
                () -> Pal.heal,
                () -> 1f
        ));
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.powerConnections, maxLink, StatUnit.none);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class AssignedBeaconBuild extends Building implements Ranged {
        public float progress;
        public float powerMul = 1f;
        public float speedMul = 1f;
        public float craftMul = 0f;
        public int[] targets = new int[maxLink];
        public float[] targetProgress = new float[maxLink];
        public int[] modulePlans = new int[moduleSlot];
        public int[] modules = new int[moduleSlot];

        @Override
        public void updateTile() {
            super.updateTile();
            checkLinks();
            NHGroups.beaconBoostLinks.put(this, linkBuilds());

            for (int i = 0; i < moduleSlot; i++) {
                PayloadSeq teamPayload = worldData.teamPayloadData.getPayload(team);
                //has plans, skip check if sandbox
                if (modulePlans[i] != -1 && modules[i] == -1 && (state.rules.infiniteResources || (getModule(modulePlans[i]) != null && teamPayload.get(getModule(modulePlans[i])) > 0))) {
                    if (!state.rules.infiniteResources) teamPayload.remove(getModule(modulePlans[i]), 1);
                    modules[i] = modulePlans[i];
                }
                //has plans but wrong module or no plans but has module
                if ((modulePlans[i] != -1 && modules[i] != modulePlans[i]) || (modulePlans[i] == -1 && modules[i] != -1)) {
                    if (!state.rules.infiniteResources) teamPayload.add(getModule(modules[i]), 1);
                    //tag as removed
                    modules[i] = -1;
                }
            }

            applyAllModules();

            progress += edelta();
            for(int i = 0; i < targets.length; i++){
                int pos = targets[i];
                Building b = Vars.world.build(pos);
                if (b instanceof GenericCrafter.GenericCrafterBuild gcb) {
                    if (gcb.block instanceof GenericCrafter gc && gc.canOverdrive){
                        if (craftMul > 0f){
                            targetProgress[i] += gcb.edelta() * craftMul;
                            if (targetProgress[i] > gc.craftTime) {
                                if (gcb.items != null && gc.outputItems != null) {
                                    for (ItemStack stack: gc.outputItems){
                                        gcb.items.add(stack.item, stack.amount);
                                    }
                                    targetProgress[i] %= gc.craftTime;
                                    NHFx.darkEnergySmoke.at(gcb);
                                }
                            }
                        }

                        if (progress >= 60){
                            if (speedMul > 1f){
                                gcb.applyBoost(speedMul, 90f);
                            }else {
                                gcb.applySlowdown(speedMul, 90f);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public float progress() {
            return progress;
        }

        @Override
        public void created() {
            super.created();
            Arrays.fill(targets, -1);
            Arrays.fill(targetProgress, -1);
            Arrays.fill(modulePlans, -1);
            Arrays.fill(modules, -1);
        }

        @Override
        public void buildConfiguration(Table table) {
            table.table(t -> {
                t.background(Styles.black6);
                //t.label(() -> Arrays.toString(modulePlans) + Arrays.toString(modules)).row();
                t.table(module -> {
                    module.table(c -> {
                        c.fill();
                        for(int i = 0; i < moduleSlot; i++){
                            int finalI = i;
                            Image icon = new Image();
                            Label label = new Label("");
                            icon.update(() -> icon.setDrawable(getModule(modulePlans[finalI]) == null? Icon.cancel.getRegion(): getModule(modulePlans[finalI]).region));
                            icon.setScaling(Scaling.bounded);
                            label.update(() -> label.setText(() -> modulePlans[finalI] == -1? "": modules[finalI] == -1? Iconc.cancel + "": Iconc.ok + ""));
                            c.stack(icon, label).size(48, 48);
                        }
                    }).expandX().fillX();
                    module.button("@clear", Styles.grayt, () -> configure(Blocks.air)).size(60, 0).expandY().fillY();
                }).row();
                t.table(select -> {
                    for (int i = 0; i < 9; i++) {
                        Block modulePlan = getModule(i);
                        ImageButton button = select.button(Tex.whiteui, Styles.clearNonei, 48f, () -> configure(modulePlan)).get();
                        button.getStyle().imageUp = new TextureRegionDrawable(getModule(i).uiIcon);
                    }
                });
            });
        }

        public void addModulePlan(int module){
            if (module == -1){
                clearModulePlans();
            }else {
                for(int i = 0; i < moduleSlot; i++){
                    if (modulePlans[i] == -1){
                        modulePlans[i] = module;
                        return;
                    }
                }
            }
        }

        public void clearModulePlans(){
            PayloadSeq teamPayload = worldData.teamPayloadData.getPayload(team);
            for(int i = 0; i < moduleSlot; i++){
                Block b = getModule(modulePlans[i]);
                if (b != null) teamPayload.add(b, 1);
                modulePlans[i] = -1;
            }
        }

        //ye this is hardcoded here so this is a todo
        public @Nullable Block getModule(int num){
            return switch(num){
                case 0 -> ModuleBlock.speedModule1;
                case 1 -> ModuleBlock.speedModule2;
                case 2 -> ModuleBlock.speedModule3;
                case 3 -> ModuleBlock.productivityModule1;
                case 4 -> ModuleBlock.productivityModule2;
                case 5 -> ModuleBlock.productivityModule3;
                case 6 -> ModuleBlock.efficiencyModule1;
                case 7 -> ModuleBlock.efficiencyModule2;
                case 8 -> ModuleBlock.efficiencyModule3;
                default -> null;
            };
        }

        //uhhhh
        public @Nullable int getModuleId(Block module){
            if (module == ModuleBlock.speedModule1) return 0;
            if (module == ModuleBlock.speedModule2) return 1;
            if (module == ModuleBlock.speedModule3) return 2;
            if (module == ModuleBlock.productivityModule1) return 3;
            if (module == ModuleBlock.productivityModule2) return 4;
            if (module == ModuleBlock.productivityModule3) return 5;
            if (module == ModuleBlock.efficiencyModule1) return 6;
            if (module == ModuleBlock.efficiencyModule2) return 7;
            if (module == ModuleBlock.efficiencyModule3) return 8;
            return -1;
        }

        //asdsadasdasdasdasdasdasdasd
        public void applyModule(int num){
            switch(num){
                case 0 -> apply(2f, 1f, 0f);
                case 1 -> apply(4f, 3f, 0f);
                case 2 -> apply(6f, 5f, 0f);
                case 3 -> apply(2f, -0.2f, 0.25f);
                case 4 -> apply(4f, -0.33f, 0.5f);
                case 5 -> apply(8f, -0.5f, 1f);
                case 6 -> apply(0.25f, 0f, 0f);
                case 7 -> apply(0.125f, 0f, 0f);
                case 8 -> apply(0.0625f, 0f, 0f);
                default -> {}
            }
        }

        public void apply(float powerMul, float speedMul, float craftMul){
            this.powerMul *= powerMul;
            this.speedMul += speedMul;
            this.craftMul += craftMul;
        }

        public void applyAllModules(){
            powerMul = 1f;
            speedMul = 1f;
            craftMul = 0f;
            for (int module: modules){
                applyModule(module);
            }
            powerMul = Mathf.clamp(powerMul, 1f, 100f);
            speedMul = Mathf.clamp(speedMul, 0.2f, 100f);
            craftMul = Mathf.clamp(craftMul, 0f, 512f);
        }

        @Override
        //compressed for config use
        public Integer[] config(){
            Integer[] out = new Integer[moduleSlot + maxLink];
            for (int i = 0; i < moduleSlot; i++){
                out[i] = modulePlans[i];
            }
            for(int i = 0; i < maxLink; i++){
                Point2 p = Point2.unpack(targets[i]).sub(tile.x, tile.y);
                out[i + moduleSlot] = Point2.pack(p.x, p.y);
            }
            return out;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(other != null && linkValid(other) && containBuild(other)){
                configure(other.pos());
                return false;
            }
            return true;
        }

        public boolean linkValid(Building b){
            return b != null && Math.abs(b.x - x) <= range() && Math.abs(b.y - y) <= range()
                    && b.team == team && b.block.canOverdrive && b instanceof GenericCrafter.GenericCrafterBuild;
        }

        public boolean containBuild(Building b){
            AtomicBoolean contains = new AtomicBoolean(false);
            NHGroups.beaconBoostLinks.each((source, targets) -> {
                if (source != this && targets.contains(target -> target == b)){
                    contains.set(true);
                }
            });
            return !contains.get();
        }

        /** return all buildings from target IntSeq */
        public Seq<Building> linkBuilds(){
            Seq<Building> buildings = new Seq<>();
            for(int pos : targets){
                Building b = Vars.world.build(pos);
                if(b != null) buildings.add(b);
            }
            return buildings;
        }

        public void addLink(int value){
            Building other = Vars.world.build(value);
            if(other != null && linkValid(other) && containBuild(other)) {
                //first check for duplicated position
                for (int i = 0; i < targets.length; i++) {
                    if (targets[i] == value){
                        targets[i] = -1;
                        targetProgress[i] = -1;
                        return;
                    }
                }
                //-1 means empty slot, replace it
                for (int i = 0; i < targets.length; i++) {
                    if (targets[i] == -1){
                        targets[i] = value;
                        targetProgress[i] = 0;
                        return;
                    }
                }
            }
        }

        @Override
        public void drawSelect() {
            Drawf.dashSquare(Pal.accent, x, y, range() * 2);

            Draw.z(Layer.blockOver);
            Draw.color(overdriveColor);
            Draw.alpha(0.3f);
            Fill.square(x, y, size * tilesize / 2f);

            Draw.z(Layer.blockOver + 0.01f);
            Draw.color(overdriveColor);
            Lines.stroke(1.5f);
            Lines.square(x, y, size * tilesize / 2f + 0.5f);

            for (Building b: linkBuilds()){
                if (b == null) continue;
                Tmp.v1.set(Geometry.raycastRect(b.x, b.y, x, y, Tmp.r1.setCentered(x, y, size * tilesize)));
                Tmp.v2.set(Geometry.raycastRect(x, y, b.x, b.y, Tmp.r1.setCentered(b.x, b.y, b.block.size * tilesize)));

                Draw.z(Layer.blockOver);
                Draw.color(overdriveColor);
                Draw.alpha(0.3f);
                Fill.square(b.x, b.y, b.block.size * tilesize / 2f);

                Draw.z(Layer.blockOver + 0.01f);
                Draw.color(overdriveColor);
                Lines.stroke(1f);
                Lines.square(b.x, b.y, b.block.size * tilesize / 2f + 0.5f);
                Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
            }
        }

        @Override
        public void drawConfigure() {
            NHGroups.beaconBoostLinks.keys().toSeq().each(b -> {
                if (b == null) return;
                b.drawSelect();
            });
        }

        //check all links and remove invalid one
        public void checkLinks(){
            for (int i = 0; i < targets.length; i++) {
                if (!linkValid(Vars.world.build(targets[i]))) {
                    targets[i] = -1;
                    targetProgress[i] = -1;
                }
            }
        }

        @Override
        public void remove() {
            super.remove();
            NHGroups.beaconBoostLinks.remove(this);
        }

        @Override
        public float range() {
            return range;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(powerMul);
            write.f(speedMul);
            write.f(craftMul);
            TypeIO.writeInts(write, targets);
            TypeIO.writeInts(write, modulePlans);
            TypeIO.writeInts(write, modules);
            write.i(targetProgress.length);
            for (float v : targetProgress) {
                write.f(v);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
            powerMul = read.f();
            speedMul = read.f();
            craftMul = read.f();
            targets = TypeIO.readInts(read);
            modulePlans = TypeIO.readInts(read);
            modules = TypeIO.readInts(read);
            int length = read.i();
            for (int i = 0; i < length; i++) {
                targetProgress[i] = read.f();
            }
        }
    }
}
