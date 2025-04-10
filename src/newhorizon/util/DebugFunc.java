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
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.OS;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.world.Block;
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

    public static void updateBlockList(){
        //only run debug for me
        if (OS.username.equals("LaoHuaJi")){
            StringMap map = new StringMap();
            String prev = readBlockList();
            String[] lists = prev.split("\n");
            for (String list: lists){
                map.put(list.split(" ")[0], list);
            }
            for (Block block: content.blocks()){
                if (block.isModded() && block.name.startsWith("new-horizon")){
                    String blockData = writeBlockNoLine(block);
                    map.put(blockData.split(" ")[0], blockData);
                }
            }
            Seq<String> ordered = new Seq<>(map.size);
            map.each((ignored, data) -> ordered.add(data));
            ordered.sort(String::compareTo);

            StringBuilder sb = new StringBuilder();
            ordered.each((data) -> sb.append(data).append("\n"));
            Fi.get(NH_ROOT_PATH).child("blocklist.txt").writeString(sb.toString());
        }
    }

    public static String readBlockList(){
        Fi fi = Fi.get(NH_ROOT_PATH).child("blocklist.txt");
        if (fi.exists()) return fi.readString();
        return "";
    }

    public static void outputSettings(){
        StringBuilder sb = new StringBuilder();
        for (String string: Core.settings.keys()){
            sb.append(string).append("\n");
        }
        Core.app.setClipboardText(sb.toString());
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

    public static void writeBulletTypeList(){
        StringBuilder sb = new StringBuilder();
        for (BulletType type: content.bullets()){
            String id = type.id + "";
            String name = type.getClass().getName();
            sb.append(id);
            sb.append(" ");
            sb.append(name);
            sb.append("\n");
        }
        Core.app.setClipboardText(sb.toString());
    }

    public static void writeBlockList(){
        StringBuilder sb = new StringBuilder();
        for (Block block: content.blocks()){
            if (block.isModded() && block.name.startsWith("new-horizon")){
                sb.append(writeBlock(block));
            }
        }
        Core.app.setClipboardText(sb.toString());
    }

    public static String writeBlockNoLine(Block block) {
        return block.name +
                " " +
                (block.synthetic() ? "1" : "0") +
                " " +
                (block.solid ? "1" : "0") +
                " " +
                block.size +
                " " +
                block.mapColor.rgb888();
    }

    public static String writeBlock(Block block) {
        return block.name +
                " " +
                (block.synthetic() ? "1" : "0") +
                " " +
                (block.solid ? "1" : "0") +
                " " +
                block.size +
                " " +
                block.mapColor.rgb888() +
                "\n";
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
