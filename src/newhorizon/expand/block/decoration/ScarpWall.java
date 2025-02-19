package newhorizon.expand.block.decoration;

import arc.Core;
import mindustry.world.blocks.defense.Wall;
import newhorizon.util.graphic.SpriteUtil;

public class ScarpWall extends Wall {
    public ScarpWall(String name) {
        super(name);
        variants = 8;
    }

    @Override
    public void load() {
        super.load();
        variantRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-atlas"), size * 32, size * 32);
    }
}
