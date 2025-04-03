package newhorizon.content;

import mindustry.game.Schematic;
import mindustry.game.Schematics;

import java.io.IOException;

import static newhorizon.content.NHContent.scheDir;

public class NHSchematic {
    public static Schematic ruin_1x1, ruin_1x2, ruin_2x1, ruin_2x2, ruin_2x2_L;

    public static void load() throws IOException {
        loadSchematic();
    }

    public static void loadSchematic() throws IOException {
        ruin_1x1 = Schematics.read(scheDir.child("ruin_1x1" + ".msch"));
        ruin_1x2 = Schematics.read(scheDir.child("ruin_1x2" + ".msch"));
        ruin_2x1 = Schematics.read(scheDir.child("ruin_2x1" + ".msch"));
        ruin_2x2 = Schematics.read(scheDir.child("ruin_2x2" + ".msch"));
        ruin_2x2_L = Schematics.read(scheDir.child("ruin_2x2_L" + ".msch"));
    }

}
