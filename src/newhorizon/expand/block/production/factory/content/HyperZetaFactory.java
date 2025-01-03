package newhorizon.expand.block.production.factory.content;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Build;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.production.factory.AdaptCrafter;

import static mindustry.type.ItemStack.with;

public class HyperZetaFactory extends AdaptCrafter {
    public TextureRegion[] rotRegion;

    public HyperZetaFactory() {
        super("hyper-zeta-factory");
        requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(NHItems.zeta, 5));

        size = 2;

        addLink(2, 0, 1,  /**/ 2, 1, 1, /**/
                0, 2, 1, /**/1, 2, 1 /**/);

        craftTime = 120f;
        consumePower(6);
        consumeItems(with(Items.thorium, 4));
        consumeLiquid(Liquids.cryofluid, 0.1f);
        outputItems = with(NHItems.zeta, 8);

        itemCapacity = 20;
    }

    @Override
    public void load() {
        super.load();

        rotRegion = new TextureRegion[4];
        for (int i = 0; i < rotRegion.length; i++) {
            rotRegion[i] = Core.atlas.find(name + "-rotation-" + i);
        }
    }

    public class HyperZetaFactoryBuild extends AdaptCrafterBuild {
        public void draw() {
            Draw.rect(rotRegion[rotation], x, y);
        }
    }
}
