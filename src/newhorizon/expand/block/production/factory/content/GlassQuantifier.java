package newhorizon.expand.block.production.factory.content;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.production.factory.AdaptCrafter;

import static mindustry.type.ItemStack.with;

public class GlassQuantifier extends AdaptCrafter {
    public TextureRegion[] rotRegion;

    public GlassQuantifier() {
        super("glass-quantifier");
        requirements(Category.crafting, BuildVisibility.shown,
                ItemStack.with(NHItems.multipleSteel, 40, NHItems.juniorProcessor, 60, NHItems.zeta, 100, Items.plastanium, 50));

        size = 2;

        addLink(2, 0, 1,  /**/ 2, 1, 1, /**/
                0, 2, 1, /**/1, 2, 1 /**/);

        craftTime = 60f;
        consumePower(480 / 60f);
        consumeItems(with(Items.sand, 4));
        consumeLiquid(NHLiquids.quantumEntity, 0.1f);
        outputItems = with(Items.metaglass, 6f);

        itemCapacity = 45;
        health = 1600;

        craftEffect = Fx.smeltsmoke;
        updateEffect = Fx.smeltsmoke;
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

    public class GlassQuantifierBuild extends AdaptCrafterBuild {
        public void draw() {
            Draw.rect(rotRegion[rotation], x, y);
        }
    }
}