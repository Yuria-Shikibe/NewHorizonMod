package newhorizon.expand.block.distribution.item.logistics;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.world.blocks.distribution.Sorter;
import newhorizon.NewHorizon;

public class AdaptSorter extends Sorter {
    public TextureRegion itemRegion;

    public AdaptSorter(String name) {
        super(name);

        placeableLiquid = true;
        drawTeamOverlay = false;
    }

    @Override
    public void load() {
        super.load();
        itemRegion = Core.atlas.find(NewHorizon.name("logistics-item"));
    }

    public class AdaptSorterBuild extends SorterBuild {
        @Override
        public void draw() {
            Draw.rect(region, x, y);

            if (sortItem != null) {
                Draw.color(sortItem.color);
                Draw.rect(itemRegion, x, y);
                Draw.color();
            }
        }
    }
}
