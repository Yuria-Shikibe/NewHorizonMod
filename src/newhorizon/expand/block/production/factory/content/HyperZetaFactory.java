package newhorizon.expand.block.production.factory.content;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.production.factory.AdaptCrafter;

import static mindustry.type.ItemStack.with;

public class HyperZetaFactory extends AdaptCrafter {
    public TextureRegion[] rotRegion;

    public HyperZetaFactory() {
        super("hyper-zeta-factory");
        requirements(Category.crafting, BuildVisibility.shown,
                ItemStack.with(NHItems.multipleSteel, 60, NHItems.irayrondPanel, 50, NHItems.juniorProcessor, 100, NHItems.metalOxhydrigen, 80));

        size = 2;

        addLink(2, 0, 1,  /**/ 2, 1, 1,/**/
                -1, 0, 1, /**/-1, 1, 1 /**/);

        craftTime = 60f;
        consumePower(300 / 60f);
        consumeItems(with(Items.thorium, 6));
        consumeLiquid(NHLiquids.zetaFluid, 5 / 60f);
        outputItems = with(NHItems.zeta, 10);

        itemCapacity = 60;
        health = 1600;

        craftEffect = Fx.formsmoke;
        updateEffect = NHFx.trailToGray;
    }

    @Override
    public void load() {
        super.load();

        rotRegion = new TextureRegion[4];
        for (int i = 0; i < rotRegion.length; i++) {
            rotRegion[i] = Core.atlas.find(name + "-rotation-" + i);
        }
    }

    @Override
    public void loadIcon() {
        super.loadIcon();
        uiIcon = Core.atlas.find(name + "-icon");
    }

    public class HyperZetaFactoryBuild extends AdaptCrafterBuild {
        public void draw() {
            Draw.rect(rotRegion[rotation], x, y);
        }
    }
}
