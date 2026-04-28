package newhorizon.expand.block.production.drill;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.production.Drill;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import newhorizon.content.NHStats;

public class AdaptDrill extends Drill {
    public int maxModules = 1;
    public DrawBlock drawer = new DrawDefault();

    public AdaptDrill(String name) {
        super(name);
        size = 4;
        itemCapacity = 40;
        hasLiquids = false;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.maxModules, maxModules);
    }

    @Override
    public void load(){
        super.load();
        drawer.load(this);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("boost", (AdaptDrillBuild build) -> new Bar(
                () -> Core.bundle.format("nh.bar.module-boost", build.modules.size, maxModules, Mathf.round(build.moduleBoost * 100)),
                () -> Pal.accent,
                () -> (float) build.modules.size / maxModules)
        );
    }

    @Override
    public void loadIcon() {
        super.loadIcon();
        uiIcon = Core.atlas.find(name + "-icon", name);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        drawer.getRegionsToOutline(this, out);
    }

    public class AdaptDrillBuild extends DrillBuild {
        public Seq<DrillModule.DrillModuleBuild> modules = new Seq<>();
        public float moduleBoost = 0f;

        public float maxModules() {
            return maxModules;
        }

        public void updateModule() {
            moduleBoost = 0f;
            modules.each(module -> module.updateDrill(this));
        }

        @Override
        public void updateTile(){
            updateModule();

            if(timer(timerDump, dumpTime / timeScale)){
                dump(dominantItem != null && items.has(dominantItem) ? dominantItem : null);
            }

            if(dominantItem == null){
                return;
            }

            timeDrilled += warmup * delta();

            float delay = getDrillTime(dominantItem);

            if(items.total() < itemCapacity && dominantItems > 0 && efficiency > 0){
                float speed = (1 + moduleBoost) * efficiency;

                lastDrillSpeed = (speed * dominantItems * warmup) / delay;
                warmup = Mathf.approachDelta(warmup, efficiency, warmupSpeed);
                progress += delta() * dominantItems * speed * warmup;

                if(Mathf.chanceDelta(updateEffectChance * warmup))
                    updateEffect.at(x + Mathf.range(size * 2f), y + Mathf.range(size * 2f));
            }else{
                lastDrillSpeed = 0f;
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }

            if(dominantItems > 0 && progress >= delay && items.total() < itemCapacity){
                int amount = (int)(progress / delay);
                for(int i = 0; i < amount; i++){
                    offload(dominantItem);
                }

                progress %= delay;

                if(wasVisible && Mathf.chanceDelta(drillEffectChance * warmup)) drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
            }
        }

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            modules.each(build -> build.drillBuild = null);
            modules.clear();
            proximity.each(building -> {
                if (building instanceof DrillModule.DrillModuleBuild module) {
                    if (module.canApply(this)) {
                        module.drillBuild = this;
                        modules.add(module);
                        module.apply(this);
                    }
                }
            });
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.selected(this, Pal.accent);
            modules.each(building -> Drawf.selected(building, Pal.accent));
        }

        @Override
        public void remove() {
            super.remove();
            for (DrillModule.DrillModuleBuild module : modules) {
                module.drillBuild = null;
            }
        }
    }
}
