package newhorizon.content;

import mindustry.game.Schematic;
import mindustry.game.Schematics;
import newhorizon.expand.map.SchematicUtil;
import newhorizon.expand.map.TerrainSchematic;

import java.io.IOException;

import static newhorizon.content.NHContent.scheDir;

public class NHSchematic {
    public static String nhss(String name){
        return scheDir.child(name + ".nhss").readString();
    }

    public static void load() throws IOException {
        loadSchematic();
    }

    public static void loadSchematic() throws IOException {

    }
}
