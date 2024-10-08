package newhorizon.util;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import mindustry.type.Planet;
import mindustry.type.Sector;
import newhorizon.util.graphic.DrawUtil;

import static mindustry.Vars.renderer;
import static mindustry.Vars.ui;

public class DebugFunc {
    public static void renderSectorId(){
        Planet planet = ui.planet.state.planet;
        Draw.color(Color.white);
        for(Sector sec : planet.sectors){
            if(sec != null){
                String secText = "[" + sec.id + "]";
                renderer.planets.drawPlane(sec, () -> DrawUtil.drawText(secText, 0, 15, 1.8f));
            }
        }
        Draw.reset();
    }
}
