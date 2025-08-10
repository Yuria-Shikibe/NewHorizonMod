package newhorizon.expand.block.special;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.Drawf;
import mindustry.world.blocks.defense.OverdriveProjector;

import static mindustry.Vars.*;

public class AdaptOverdriveProjector extends OverdriveProjector {
    public AdaptOverdriveProjector(String name) {
        super(name);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        drawPotentialLinks(x, y);
        drawOverlay(x * tilesize + offset, y * tilesize + offset, rotation);
        Drawf.dashRect(baseColor, x * tilesize + offset - range/2f, y * tilesize + offset - range/2f, range, range);
        indexer.eachBlock(player.team(), Tmp.r1.setCentered(x, y, range), other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
    }

    public class AdaptOverdriveProjectorBuild extends OverdriveBuild {
        @Override
        public void updateTile(){
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            heat = Mathf.lerpDelta(heat, efficiency > 0 ? 1f : 0f, 0.08f);
            charge += heat * Time.delta;

            if(hasBoost) phaseHeat = Mathf.lerpDelta(phaseHeat, optionalEfficiency, 0.1f);
            if(charge >= reload){
                float realRange = range + phaseHeat * phaseRangeBoost;
                charge = 0f;
                indexer.eachBlock(team, Tmp.r1.setCentered(x, y, realRange), other -> other.block.canOverdrive, other -> other.applyBoost(realBoost(), reload + 1f));
            }
            if(efficiency > 0) useProgress += delta();
            if(useProgress >= useTime){
                consume();
                useProgress %= useTime;
            }
        }

        @Override
        public void drawSelect(){
            float realRange = range + phaseHeat * phaseRangeBoost;
            indexer.eachBlock(team, Tmp.r1.setCentered(x, y, realRange), other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
            Drawf.dashRect(baseColor, x - realRange/2f, y - realRange/2f, realRange, realRange);
        }
    }
}
