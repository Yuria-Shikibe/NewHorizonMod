package newhorizon.expand.block.editor;

import arc.util.Log;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.block.environment.PlateFloor;

import static mindustry.Vars.world;

public class PlateFloorPlacer extends Block {
    public PlateFloor floor;
    public int type;
    public PlateFloorPlacer(String name, PlateFloor floor, int size, int variant) {
        super(name);

        solid = true;
        update = true;
        inEditor = false;
        destructible = true;
        hideDatabase = true;

        buildVisibility = BuildVisibility.editorOnly;
        category = Category.logic;

        destroyEffect = Fx.none;
        placeSound = Sounds.none;
        destroySound = Sounds.none;


        this.floor = floor;
        this.size = size;
        this.type = variant;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class PlateFloorBuild extends Building {

        @Override
        public void placed() {
            super.placed();

            if(block.isMultiblock()){
                int size = block.size, o = block.sizeOffset;
                for(int dx = 0; dx < size; dx++){
                    for(int dy = 0; dy < size; dy++){
                        Tile other = world.tile(tileX() + dx + o, tileY() + dy + o);
                        int index = dx + (size - 1 - dy) * size;
                        Log.info(index);
                        if(other != null) {
                            other.data = (byte) (((type & 0x0F) << 4) | ((size - 1) & 0x0F));
                            other.floorData = (byte) index;
                            other.setFloor(floor);
                            other.recache();
                        }
                    }
                }
            }else{
                tile.data = (byte) ((type & 0x0F) << 4);
                tile.floorData = 0;
                tile.setFloor(floor);
                tile.recache();
            }
            tile.remove();
        }
    }
}
