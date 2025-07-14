package newhorizon.util;

import arc.Core;
import arc.files.Fi;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.FrameBuffer;
import arc.math.Interp;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.ScreenUtils;
import arc.util.Tmp;
import arc.util.serialization.Json;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;
import arc.util.serialization.JsonWriter;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.mod.Mods;
import mindustry.type.Category;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.world.Block;
import newhorizon.util.graphic.DrawFunc;

import java.io.StringWriter;
import java.util.Objects;

import static mindustry.Vars.*;
import static newhorizon.NewHorizon.MOD;

public class DebugFunc {
    public static final String NH_ROOT_PATH = "E:/project/MindustryModDevLib/NewHorizonMod";
    public static final String NH_BUNDLE_PATH = NH_ROOT_PATH + "/assets/bundles/blank/";
    public static final String NH_SPRITE_PATH = NH_ROOT_PATH + "/assets/sprites";
    public static final String NH_DEBUG_GRAPHIC_FOLDER = NH_SPRITE_PATH + "/debug/";
    public static final String NH_DEBUG_JSON_DATA_FOLDER = NH_ROOT_PATH + "/data/";
    public static final String NH_SPRITE_ICON_PATH = NH_ROOT_PATH + "/icons/";
    public static final Color[] NH_SPRITE_PALETTE = {
            Color.valueOf("abb1bf"), //light
            Color.valueOf("8e909c"), //mid
            Color.valueOf("5a5d70"), //dark
            Color.valueOf("2e3039")  //black
    };

    public static final Color[] EXOPROSOPA_SPRITE_PALETTE = {
            Color.valueOf("6b6881"),
            Color.valueOf("4b495a"),
            Color.valueOf("32303c"),
            Color.valueOf("26262f"),
    };

    public static final Color[] ASTHOSUS_SPRITE_PALETTE = {
            Color.valueOf("8f8a77"),
            Color.valueOf("7a7564"),
            Color.valueOf("5b574e"),
            Color.valueOf("44413a"),
    };

    public static void generateBlankBundle() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                """
                        #TO TRANSLATORS:
                        #The following parts are mainly New Horizon's content translations (items, blocks, etc.)
                        #For some content's name, they are random created word if you couldnt find the word root.
                        #In this case, its ok for you to create your own translation in your own language
                        #For the content details, its ok to add content for your self, for example lore or even memes.
                        #The .detail is ok for that, just keep the .description accurate.
                        #If you want to make the .detail, remove the '#' mark in the line.
                        #Its ok to add credit with your name inside the translate bundle.
                        
                        #THANK YOU FOR HELP TRANSLATING THIS MOD!
                        """);
        sb.append("\n\n#[[REGION ITEM]]\n\n\n");
        contentIterator(ContentType.item, content -> sb.append(contentBlankBundle(content)));
        sb.append("\n\n#[[REGION LIQUID]]\n\n\n");
        contentIterator(ContentType.liquid, content -> sb.append(contentBlankBundle(content)));

        Boolf<UnlockableContent> modFilter = content -> content instanceof Block block && block.minfo.mod == MOD;
        sb.append("\n\n#[[REGION BLOCK]]\n\n\n");
        sb.append("#TURRET PART\n");
        contentIterator(ContentType.block, content -> modFilter.get(content) && ((Block) content).category == Category.turret, content -> sb.append(contentBlankBundle(content)));
        sb.append("#PRODUCTION PART\n");
        contentIterator(ContentType.block, content -> modFilter.get(content) && ((Block) content).category == Category.production, content -> sb.append(contentBlankBundle(content)));
        sb.append("#DISTRIBUTION PART\n");
        contentIterator(ContentType.block, content -> modFilter.get(content) && ((Block) content).category == Category.distribution, content -> sb.append(contentBlankBundle(content)));
        sb.append("#LIQUID PART\n");
        contentIterator(ContentType.block, content -> modFilter.get(content) && ((Block) content).category == Category.liquid, content -> sb.append(contentBlankBundle(content)));
        sb.append("#POWER PART\n");
        contentIterator(ContentType.block, content -> modFilter.get(content) && ((Block) content).category == Category.power, content -> sb.append(contentBlankBundle(content)));
        sb.append("#DEFENSE PART\n");
        contentIterator(ContentType.block, content -> modFilter.get(content) && ((Block) content).category == Category.defense, content -> sb.append(contentBlankBundle(content)));
        sb.append("#CRAFTING PART\n");
        contentIterator(ContentType.block, content -> modFilter.get(content) && ((Block) content).category == Category.crafting, content -> sb.append(contentBlankBundle(content)));
        sb.append("#UNIT PART\n");
        contentIterator(ContentType.block, content -> modFilter.get(content) && ((Block) content).category == Category.units, content -> sb.append(contentBlankBundle(content)));
        sb.append("#SPECIAL PART\n");
        contentIterator(ContentType.block, content -> modFilter.get(content) && ((Block) content).category == Category.effect, content -> sb.append(contentBlankBundle(content)));
        sb.append("#LOGIC PART\n");
        contentIterator(ContentType.block, content -> modFilter.get(content) && ((Block) content).category == Category.logic, content -> sb.append(contentBlankBundle(content)));

        sb.append("\n\n#[[REGION UNIT]]\n\n\n");
        contentIterator(ContentType.unit, content -> sb.append(contentBlankBundle(content)));

        sb.append("\n\n#[[REGION STATUS]]\n\n\n");
        contentIterator(ContentType.status, content -> sb.append(contentBlankBundle(content)));

        sb.append("\n\n#[[REGION PLANET]]\n\n\n");
        contentIterator(ContentType.planet, content -> sb.append(contentBlankBundle(content)));

        sb.append("\n\n#[[REGION SECTOR]]\n\n\n");
        contentIterator(ContentType.sector, content -> sb.append(contentBlankBundle(content)));

        sb.append("\n\n#[[REGION WEATHER]]\n\n\n");
        contentIterator(ContentType.weather, content -> sb.append(contentBlankBundle(content)));

        Fi blankBundle = new Fi(NH_BUNDLE_PATH + "bundle.blank-content.txt");
        blankBundle.writeString(sb.toString());
    }

    public static String contentBlankBundle(UnlockableContent content) {
        String prefix = content.getContentType().name() + ".";
        return prefix + content.name + ".name = \n" + prefix + content.name + ".description = \n#" + prefix + content.name + ".detail = \n";
    }

    public static void contentIterator(ContentType type, Boolf<UnlockableContent> filter, Cons<UnlockableContent> iterator) {
        for (Content content : content.getBy(type)) {
            if (content instanceof UnlockableContent unlockableContent) {
                if (filter.get(unlockableContent)) {
                    iterator.get(unlockableContent);
                }
            }
        }
    }

    public static void contentIterator(ContentType type, Cons<UnlockableContent> iterator) {
        contentIterator(type, c -> c.minfo.mod == MOD, iterator);
    }

    public static Fi createPNGFile(String fileName) {
        return new Fi(NH_DEBUG_GRAPHIC_FOLDER + fileName + ".png");
    }

    public static Fi createIconPNGFile(String fileName) {
        return new Fi(NH_SPRITE_ICON_PATH + fileName + ".png");
    }

    public static Fi createJsonFile(String fileName) {
        return new Fi(NH_DEBUG_JSON_DATA_FOLDER + fileName + ".json");
    }

    public static void outputIcon() {
        Icon.icons.each((name, drawable) -> {
            TextureRegion region = drawable.getRegion();
            outputTextureRegion(name, region);
        });
    }

    public static void outlineIcon() {
        Fi folder = new Fi(NH_DEBUG_GRAPHIC_FOLDER);
        folder.findAll().each(sprite -> outlineSprite(sprite, Pal.gray, 4, true, false));
    }

    public static void outlineSprite(Fi sprite, Color color, int stroke, boolean expand, boolean smooth) {
        int pad = expand ? stroke * 2 : 0;
        Pixmap process = PixmapIO.readPNG(sprite);
        Pixmap result = new Pixmap(process.width + pad, process.height + pad);
        result.fill(Color.clear);
        result.draw(process, stroke, stroke);
        Pixmap out = smooth ? outlineSmooth(result, color, stroke) : result.outline(color, stroke);
        Fi fi = new Fi(NH_DEBUG_GRAPHIC_FOLDER + "/outline/" + sprite.nameWithoutExtension() + "-outline.png");
        PixmapIO.writePng(fi, out);
        process.dispose();
        result.dispose();
        out.dispose();
    }

    public static Pixmap outlineSmooth(Pixmap pixmap, Color color, int radius) {
        final int alphaThreshold = 10;

        Pixmap result = pixmap.copy();

        float[][] distance = new float[pixmap.width][pixmap.height];

        for (int y = 0; y < pixmap.height; y++) {
            for (int x = 0; x < pixmap.width; x++) {
                if (pixmap.getA(x, y) >= alphaThreshold) {
                    distance[x][y] = 0f;
                } else {
                    distance[x][y] = Float.POSITIVE_INFINITY;
                }
            }
        }

        for (int y = 0; y < pixmap.height; y++) {
            for (int x = 0; x < pixmap.width; x++) {
                if (distance[x][y] == 0f) continue;

                for (int yy = Math.max(0, y - radius); yy <= Math.min(pixmap.height - 1, y + radius); yy++) {
                    for (int xx = Math.max(0, x - radius); xx <= Math.min(pixmap.width - 1, x + radius); xx++) {
                        if (distance[xx][yy] == 0f) {
                            float dx = x - xx;
                            float dy = y - yy;
                            float distSq = dx * dx + dy * dy;
                            if (distSq < distance[x][y] * distance[x][y]) {
                                distance[x][y] = (float) Math.sqrt(distSq);
                            }
                        }
                    }
                }
            }
        }

        for (int y = 0; y < pixmap.height; y++) {
            for (int x = 0; x < pixmap.width; x++) {
                float d = distance[x][y];
                if (d > 0 && d <= radius) {
                    float alpha = Interp.pow10Out.apply(1f - d / radius);
                    int rgba = Tmp.c1.set(color).a(alpha).rgba8888();
                    result.setRaw(x, y, rgba);
                }
            }
        }

        return result;
    }

    public static void outputTextureRegion(String name, TextureRegion region) {
        Draw.blend();
        Draw.reset();

        Tmp.m1.set(Draw.proj());
        Tmp.m2.set(Draw.trans());

        FrameBuffer buffer = new FrameBuffer(region.width, region.height);

        buffer.begin(Color.clear);

        Draw.proj().setOrtho(0, buffer.getHeight(), buffer.getWidth(), -buffer.getHeight());
        Draw.flush();

        Draw.rect(region, region.width / 2f, region.height / 2f, region.width, region.height);

        Draw.flush();
        Draw.trans().idt();

        buffer.end();

        Draw.proj(Tmp.m1);
        Draw.trans(Tmp.m2);

        Draw.flush();

        buffer.begin();
        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, buffer.getWidth(), buffer.getHeight());
        Fi fi = createPNGFile(name);
        fi.writePng(pixmap);
        buffer.end();

        buffer.dispose();
    }

    public static void outputAtlas() {
        ObjectSet<Texture> atlasAll = Core.atlas.getTextures();
        Log.info(atlasAll.size);
        int i = 0;
        for (Texture texture : atlasAll) {
            i++;
            Fi fi = createPNGFile("atlas-" + i);
            PixmapIO.writePng(fi, texture.getTextureData().consumePixmap());
        }
    }

    public static void unlockModContent() {
        for (Seq<Content> contents : content.getContentMap()) {
            for (Content content : contents) {
                if (content instanceof UnlockableContent unlockableContent) {
                    if (unlockableContent.minfo.mod == MOD) unlockableContent.quietUnlock();
                }
            }
        }
    }

    public static void replaceAllSpriteColor(String path, Color[] palette) {
        Fi fi = new Fi(path);
        fi.walk(file -> replaceSpriteColor(path, file, palette));
    }

    public static void replaceSpriteColor(String parentPath, Fi sprite, Color[] palette) {
        if (!Objects.equals(sprite.extension(), "png")) return;
        Fi parent = sprite.parent();
        Fi out = new Fi(NH_DEBUG_GRAPHIC_FOLDER + parent.name().replace(parentPath, "") + "/" + sprite.name());
        Pixmap pixmap = new Pixmap(sprite);
        pixmap.each((x, y) -> {
            if (pixmap.get(x, y) == palette[0].rgba()) pixmap.set(x, y, NH_SPRITE_PALETTE[0]);
            if (pixmap.get(x, y) == palette[1].rgba()) pixmap.set(x, y, NH_SPRITE_PALETTE[1]);
            if (pixmap.get(x, y) == palette[2].rgba()) pixmap.set(x, y, NH_SPRITE_PALETTE[2]);
            if (pixmap.get(x, y) == palette[3].rgba()) pixmap.set(x, y, NH_SPRITE_PALETTE[3]);
        });
        PixmapIO.writePng(out, pixmap);
        pixmap.dispose();
    }

    public static void replaceAtlas(Color[] palette){
        for (TextureAtlas.AtlasRegion region: Core.atlas.getRegions()){
            Pixmap pixmap = region.pixmapRegion.pixmap;
            pixmap.each((x, y) -> {
                if (pixmap.get(x, y) == palette[0].rgba()) pixmap.set(x, y, NH_SPRITE_PALETTE[0]);
                if (pixmap.get(x, y) == palette[1].rgba()) pixmap.set(x, y, NH_SPRITE_PALETTE[1]);
                if (pixmap.get(x, y) == palette[2].rgba()) pixmap.set(x, y, NH_SPRITE_PALETTE[2]);
                if (pixmap.get(x, y) == palette[3].rgba()) pixmap.set(x, y, NH_SPRITE_PALETTE[3]);
            });
        };
    }

    public static void outputSettings() {
        StringBuilder sb = new StringBuilder();
        for (String string : Core.settings.keys()) {
            sb.append(string).append("\n");
        }
        Core.app.setClipboardText(sb.toString());
    }

    public static void outputContentSprites() {
        for (Seq<Content> contents : content.getContentMap()) {
            for (Content content : contents) {
                if (content.minfo.mod == MOD) {
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

    public static void writeBulletTypeList() {
        StringBuilder sb = new StringBuilder();
        for (BulletType type : content.bullets()) {
            String id = type.id + "";
            String name = type.getClass().getName();
            sb.append(id);
            sb.append(" ");
            sb.append(name);
            sb.append("\n");
        }
        Core.app.setClipboardText(sb.toString());
    }

    public static void writeBlockList() {
        StringBuilder sb = new StringBuilder();
        for (Block block : content.blocks()) {
            if (block.isModded() && block.name.startsWith("new-horizon")) {
                sb.append(writeBlock(block));
            }
        }
        Core.app.setClipboardText(sb.toString());
    }

    public static void updateBlockList() {
        StringMap map = new StringMap();
        String prev = readBlockList();
        String[] lists = prev.split("\n");
        for (String list : lists) {
            map.put(list.split(" ")[0], list);
        }
        for (Block block : content.blocks()) {
            if (block.isModded() && block.name.startsWith("new-horizon")) {
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

    public static String readBlockList() {
        Fi fi = Fi.get(NH_ROOT_PATH).child("blocklist.txt");
        if (fi.exists()) return fi.readString();
        return "";
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

    public static void writeTeamList(){
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        for (Team team: Team.all){
            json.addChild(String.valueOf(team.id), new JsonValue(team.color.rgba8888()));
        }
        createJsonFile("teamlist").writeString(json.prettyPrint(JsonWriter.OutputType.json, 2));
    }

    public static void writeVanillaBlockList() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        contentIterator(ContentType.block, Content::isVanilla, content -> writeBlockJsonValue(content, json));
        createJsonFile("blocklist-vanilla").writeString(json.prettyPrint(JsonWriter.OutputType.json, 2));
        mods.list().each(mod -> {
            if (mod.meta.hidden || !mod.enabled()) return;
            JsonValue modJson = new JsonValue(JsonValue.ValueType.object);
            contentIterator(ContentType.block, content -> content.minfo != null && content.minfo.mod == mod, content -> writeBlockJsonValue(content, modJson));
            createJsonFile("blocklist-" + mod.name).writeString(modJson.prettyPrint(JsonWriter.OutputType.json, 2));
        });
    }

    public static void writeBlockJsonValue(UnlockableContent content, JsonValue json) {
        if (content instanceof Block block) {
            JsonValue data = new JsonValue(JsonValue.ValueType.object);

            data.addChild("synthetic", new JsonValue(block.synthetic()));
            data.addChild("solid", new JsonValue(block.solid));
            data.addChild("size", new JsonValue(block.size));
            data.addChild("color", new JsonValue(block.mapColor.rgba8888()));

            json.addChild(block.name, data);
        }
    }

    public static void renderSectorId() {
        Planet planet = ui.planet.state.planet;
        Draw.color(Color.white);
        for (Sector sec : planet.sectors) {
            if (sec != null) {
                String secText = "[" + sec.id + "]";
                renderer.planets.drawPlane(sec, () -> DrawFunc.drawText(secText, 0, 15, 1.8f));
            }
        }
        Draw.reset();
    }
}
