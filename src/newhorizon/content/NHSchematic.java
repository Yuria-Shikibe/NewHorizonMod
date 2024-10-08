package newhorizon.content;

import mindustry.game.Schematic;
import mindustry.game.Schematics;
import newhorizon.expand.map.SchematicUtil;
import newhorizon.expand.map.TerrainSchematic;

import java.io.IOException;

import static newhorizon.content.NHContent.scheDir;

public class NHSchematic {
    public static TerrainSchematic TEST_CHUNK_WHITE, TEST_CHUNK_BLACK;
    public static TerrainSchematic QUANTUM_RIVER;

    public static Schematic[] QUANTUM_RIVER_BUILD;

    public static String nhss(String name){
        return scheDir.child(name + ".nhss").readString();
    }

    public static void load() throws IOException {
        TEST_CHUNK_WHITE = SchematicUtil.readBase64(nhss("test-chunk-white"));
        TEST_CHUNK_BLACK = SchematicUtil.readBase64(nhss("test-chunk-black"));
        QUANTUM_RIVER = SchematicUtil.readBase64(nhss("quantum-river"));

        loadSchematic();
    }

    public static void loadSchematic() throws IOException {
        QUANTUM_RIVER_BUILD = new Schematic[16];
        for (int i = 0; i < QUANTUM_RIVER_BUILD.length; i++){
            QUANTUM_RIVER_BUILD[i] = Schematics.read(scheDir.child("quantum-river-build-" + i + ".msch"));
        }
    }
}
