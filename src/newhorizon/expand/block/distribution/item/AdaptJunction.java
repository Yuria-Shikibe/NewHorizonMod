package newhorizon.expand.block.distribution.item;

import mindustry.world.blocks.distribution.Junction;

public class AdaptJunction extends Junction {
    public AdaptJunction(String name) {
        super(name);

        placeableLiquid = true;
        drawTeamOverlay = false;
    }
}
