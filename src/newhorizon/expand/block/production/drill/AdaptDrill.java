package newhorizon.expand.block.production.drill;

import arc.struct.Seq;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.world.blocks.production.Drill;
import newhorizon.content.NHStats;

public class AdaptDrill extends Drill {
    public int maxModules = 1;

    public AdaptDrill(String name) {
        super(name);
        size = 4;
        itemCapacity = 40;
        canOverdrive = false;
        drawTeamOverlay = false;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.maxModules, maxModules);
    }

    public class AdaptDrillBuild extends DrillBuild {
        public Seq<DrillModule.DrillModuleBuild> modules = new Seq<>();

        public float maxModules() {
            return maxModules;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
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

        @Override
        public Object senseObject(LAccess sensor) {
            if (sensor == LAccess.firstItem) return dominantItem;
            return super.senseObject(sensor);
        }
    }
}
