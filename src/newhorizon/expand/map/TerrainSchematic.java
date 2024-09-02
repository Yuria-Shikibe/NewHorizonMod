package newhorizon.expand.map;

import mindustry.game.Schematic;
import mindustry.game.Team;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.world;

public class TerrainSchematic {
    public static void placeTerrain(Schematic schem, int x, int y){
        int ox = x - schem.width/2, oy = y - schem.height/2;
        schem.tiles.each(st -> {
            Tile tile = world.tile(st.x + ox, st.y + oy);
            if (tile != null && st.block instanceof Floor){
                tile.setFloor((Floor) st.block);
            }
        });
    }
}
