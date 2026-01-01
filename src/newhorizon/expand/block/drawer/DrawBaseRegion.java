package newhorizon.expand.block.drawer;

import arc.Core;
import mindustry.world.Block;
import mindustry.world.draw.DrawRegion;
import newhorizon.NewHorizon;

public class DrawBaseRegion extends DrawRegion {
    public DrawBaseRegion(String suffix) {
        super(suffix);
    }

    @Override
    public void load(Block block){
        region = Core.atlas.find(NewHorizon.name("bottom" + suffix));
    }
}
