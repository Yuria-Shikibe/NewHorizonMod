package newhorizon.util;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.PixmapIO;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.struct.ObjectSet;
import arc.util.Log;
import mindustry.type.Planet;
import mindustry.type.Sector;
import newhorizon.util.graphic.DrawUtil;

import static mindustry.Vars.renderer;
import static mindustry.Vars.ui;

public class DebugFunc {
    public static final String NH_ROOT_PATH = "E:/MindustryModDevLib/NewHorizonMod";
    public static final String NH_SPRITE_PATH = NH_ROOT_PATH + "/assets/sprites";
    public static final String NH_DEBUG_GRAPHIC_FOLDER = NH_SPRITE_PATH + "/debug/";

    public static Fi createPNGFile(String fileName){
        return new Fi(NH_DEBUG_GRAPHIC_FOLDER + fileName + ".png");
    }

    public static void outputAtlas(){
        ObjectSet<Texture> atlasAll = Core.atlas.getTextures();
        Log.info(atlasAll.size);
        int i = 0;
        for (Texture texture: atlasAll){
            i++;
            Fi fi = createPNGFile("atlas-" + i);
            PixmapIO.writePng(fi, texture.getTextureData().consumePixmap());
        }
    }
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
