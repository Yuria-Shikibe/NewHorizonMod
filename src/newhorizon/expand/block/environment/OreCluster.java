package newhorizon.expand.block.environment;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.Tile;

public class OreCluster extends Block {
    public float layer = Layer.blockProp;

    public OreCluster(String name) {
        super(name);
        solid = true;
        destructible = true;
        drawTeamOverlay = false;

        itemDrop = Items.copper;
    }

    @Override
    public TextureRegion[] icons(){
        return variants == 0 ? super.icons() : new TextureRegion[]{Core.atlas.find(name + "1")};
    }

    @Override
    public boolean canBreak(Tile tile) {
        return false;
    }


    public class OrePropBuilding extends Building {
        @Override
        public void draw() {
            Draw.z(Layer.blockProp);
            Draw.rect(region, x, y);
        }
    }
}
