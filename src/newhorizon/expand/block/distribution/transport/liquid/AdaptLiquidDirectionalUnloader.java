package newhorizon.expand.block.distribution.transport.liquid;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.TargetPriority;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.content;

public class AdaptLiquidDirectionalUnloader extends Block {
    public TextureRegion[] topRegions = new TextureRegion[4];
    public TextureRegion baseRegion, liquidRegion;

    public float speed = 5f;

    public AdaptLiquidDirectionalUnloader(String name) {
        super(name);

        group = BlockGroup.liquids;
        update = true;
        solid = true;
        configurable = true;
        saveConfig = true;
        rotate = true;
        noUpdateDisabled = true;
        unloadable = false;
        envDisabled = Env.none;
        clearOnDoubleTap = true;
        priority = TargetPriority.transport;
        hasLiquids = true;
        liquidCapacity = 50;
        outputsLiquid = true;

        config(Liquid.class, (AdaptLiquidDirectionalUnloaderBuild tile, Liquid Liquid) -> tile.unloadLiquid = Liquid);
        configClear((AdaptLiquidDirectionalUnloaderBuild tile) -> tile.unloadLiquid = null);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.speed, 60f * speed, StatUnit.liquidSecond);
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
        drawPlanConfigCenter(plan, plan.config, "duct-unloader-center");
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("items");
    }

    @Override
    public void load() {
        super.load();
        for (int i = 0; i < topRegions.length; i++) {
            topRegions[i] = Core.atlas.find(name + "-top-" + i);
        }
        baseRegion = Core.atlas.find(name + "-base");
        liquidRegion = Core.atlas.find(name + "-liquid");
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(baseRegion, plan.drawx(), plan.drawy());
        Draw.rect(topRegions[plan.rotation], plan.drawx(), plan.drawy());
        drawPlanConfig(plan, list);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }


    public class AdaptLiquidDirectionalUnloaderBuild extends Building {
        public float unloadTimer = 0f;
        public Liquid unloadLiquid = null;
        public int offset = 0;

        @Override
        public void updateTile() {
            if (liquids.current() != unloadLiquid) liquids.clear();

            if ((unloadTimer += edelta()) >= Time.delta) {
                Building back = back();

                if (back != null && back.block != null && back.liquids != null && back.team == team && unloadLiquid != null) {
                    float fl = liquids.get(unloadLiquid), bl = back.liquids.get(unloadLiquid), fc = this.block.liquidCapacity, bc = back.block.liquidCapacity;
                    if (bl > 0 && bl / bc > fl / fc) {
                        float amount = Math.min(speed * Time.delta, back.liquids.get(unloadLiquid));
                        float a = Math.min(amount, this.block.liquidCapacity - this.liquids.get(unloadLiquid));
                        float balance = Math.min(a, (bl / bc - fl / fc) * bc);
                        this.handleLiquid(this, unloadLiquid, balance);
                        back.liquids.remove(unloadLiquid, balance);
                    }
                }

                unloadTimer %= speed;
            }

            if(liquids.currentAmount() > 0.01f){
                dumpLiquid(liquids.current(), 2f, 0);
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(AdaptLiquidDirectionalUnloader.this, table, content.liquids(), () -> unloadLiquid, this::configure);
        }

        @Override
        public Liquid config() {
            return unloadLiquid;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(unloadLiquid == null ? -1 : unloadLiquid.id);
            write.s(offset);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int id = read.s();
            unloadLiquid = id == -1 ? null : content.liquids().get(id);
            offset = read.s();
        }

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            if(unloadLiquid != null) {
                Draw.color(unloadLiquid.color);
                Draw.rect(liquidRegion, x, y);
                Draw.color();
            }

            Draw.rect(topRegions[rotation], x, y);
        }

        @Override
        public void drawSelect() {
            drawIO();

            Draw.reset();
        }

        private void drawIO() {
            Building front = front(), back = back();

            if (unloadLiquid != null && front != null && back != null && front.acceptLiquid(this, unloadLiquid)) {
                float alpha = Math.abs(100f - (Time.time * 2f) % 100f) / 100f;

                float ix = front.x;
                float iy = front.y;
                float ox = back.x;
                float oy = back.y;
                float px = Mathf.lerp(ix, ox, alpha);
                float py = Mathf.lerp(iy, oy, alpha);

                //background
                Draw.z(Layer.blockOver);
                Draw.color(Pal.gray);
                Lines.stroke(2.5f);
                Fill.square(ix, iy, 2.5f, 45);
                Fill.square(ox, oy, 2.5f, 45);
                Lines.stroke(4f);

                Lines.line(ix, iy, ox, oy);

                //Colored
                Draw.z(Layer.blockOver + 0.0001f);
                Draw.color(unloadLiquid == null ? Pal.gray : unloadLiquid.color);
                Fill.square(ix, iy, 1f, 45);
                Fill.square(ox, oy, 1f, 45);
                Lines.stroke(1f);

                Lines.line(ix, iy, ox, oy);

                //Point
                Draw.z(Layer.blockOver + 0.0002f);
                Draw.mixcol(Draw.getColor(), 1f);
                Draw.color();
                Fill.square(px, py, 1f, 45);
                Draw.mixcol();
            }
        }
    }
}
