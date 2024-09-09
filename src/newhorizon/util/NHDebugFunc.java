package newhorizon.util;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureAtlas;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.util.Log;
import mindustry.Vars;
import newhorizon.NewHorizon;

public class NHDebugFunc {
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
}
