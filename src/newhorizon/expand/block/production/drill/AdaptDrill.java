package newhorizon.expand.block.production.drill;

import arc.Core;
import arc.func.Cons;
import arc.func.Floatf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Iconc;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.production.BurstDrill;
import mindustry.world.blocks.production.Drill;
import mindustry.world.consumers.ConsumePowerDynamic;
import mindustry.world.meta.*;
import newhorizon.content.NHStats;

import static mindustry.Vars.*;
import static newhorizon.util.func.NHFunc.globalEffectRand;

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
