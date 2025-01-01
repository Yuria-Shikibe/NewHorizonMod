package newhorizon.util;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Planet;
import mindustry.type.Sector;
import newhorizon.NewHorizon;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.*;

public class DebugFunc {
    public static final String NH_ROOT_PATH = "E:/project/MindustryModDevLib/NewHorizonMod";
    public static final String NH_SPRITE_ICON_PATH = NH_ROOT_PATH + "/icons/";
    public static final String NH_SPRITE_PATH = NH_ROOT_PATH + "/assets/sprites";
    public static final String NH_DEBUG_GRAPHIC_FOLDER = NH_SPRITE_PATH + "/debug/";

    public static Fi createPNGFile(String fileName){
        return new Fi(NH_DEBUG_GRAPHIC_FOLDER + fileName + ".png");
    }

    public static Fi createIconPNGFile(String fileName){
        return new Fi(NH_SPRITE_ICON_PATH + fileName + ".png");
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

    public static void outputContentSprites(){
        for (Seq<Content> contents: content.getContentMap()){
            for (Content content: contents){
                if (content.minfo.mod == NewHorizon.MOD){
                    String name = "content";
                    TextureRegion icon = null;
                    if (content instanceof UnlockableContent) {
                        name = ((UnlockableContent) content).name;
                        icon = ((UnlockableContent) content).fullIcon;
                    }

                    if (icon != null) {
                        Fi fi = createIconPNGFile(name);
                        Pixmap pixmap = Core.atlas.getPixmap(icon).crop();
                        PixmapIO.writePng(fi, pixmap);
                        pixmap.dispose();
                    }
                }
            }
        }
    }

    public static void renderSectorId(){
        Planet planet = ui.planet.state.planet;
        Draw.color(Color.white);
        for(Sector sec : planet.sectors){
            if(sec != null){
                String secText = "[" + sec.id + "]";
                renderer.planets.drawPlane(sec, () -> DrawFunc.drawText(secText, 0, 15, 1.8f));
            }
        }
        Draw.reset();
    }
}
