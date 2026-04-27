package newhorizon.expand.block.drawer;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Eachable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.blocks.production.Drill;
import mindustry.world.draw.DrawPistons;
import newhorizon.util.func.NHInterp;

public class DrawDrillPistonsInterp extends DrawPistons {

    @Override
    public void draw(Building build){
        if (build instanceof Drill.DrillBuild drill) {
            float progress = drill.timeDrilled * 2f;
            for(int i = 0; i < sides; i++){
                float scl = Mathf.absin(progress + sinOffset + sideOffset * sinScl * i, sinScl, 1);
                float len = Interp.smooth.apply(scl) * sinMag + lenOffset;
                float angle = angleOffset + i * 360f / sides;
                TextureRegion reg = regiont.found() && (Mathf.equal(angle, 315) || Mathf.equal(angle, 135)) ? regiont :
                        angle >= 135 && angle < 315 ? region2 : region1;
                if(Mathf.equal(angle, 315)) Draw.yscl = -1f;
                Tmp.v1.trns(angle, len, -horiOffset);
                Draw.rect(reg, build.x + Tmp.v1.x, build.y + Tmp.v1.y, angle);
                Draw.yscl = 1f;
            }
        }
    }
}
