package newhorizon.expand.block.production.factory.content;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.liquid.LiquidBlock;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.production.factory.AdaptCrafter;

import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class HyperZetaFactory extends AdaptCrafter {
    public TextureRegion[] rotRegion;
    public TextureRegion baseRegion;
    public float flameRadius = 3f, flameRadiusIn = 1.9f, flameRadiusScl = 5f, flameRadiusMag = 2f, flameRadiusInMag = 1f;

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

        clipSize = 4 * tilesize;
    }

    @Override
    public void load() {
        super.load();

        rotRegion = new TextureRegion[4];
        for (int i = 0; i < rotRegion.length; i++) {
            rotRegion[i] = Core.atlas.find(name + "-rotation-" + i);
        }
        baseRegion = Core.atlas.find(name + "-base");
    }

    @Override
    public void loadIcon() {
        super.loadIcon();
        uiIcon = Core.atlas.find(name + "-icon");
    }

    public class HyperZetaFactoryBuild extends AdaptCrafterBuild {
        public void draw() {
            Draw.rect(baseRegion, x, y, rotdeg());
            LiquidBlock.drawTiledFrames(size, x, y, 2, 2, 2, 2, NHLiquids.zetaFluid, liquids.get(NHLiquids.zetaFluid) / block.liquidCapacity);
            Draw.rect(rotRegion[rotation], x, y);
            drawFlame(0, 0, 1f);
        }

        public void drawFlame(float sx, float sy, float scl) {
            if (warmup() > 0f) {
                float g = 0.3f;
                float r = 0.06f;
                float cr = Mathf.random(0.1f);

                Draw.z(Layer.block + 0.01f);

                Draw.alpha(((1f - g) + Mathf.absin(Time.time, 8f, g) + Mathf.random(r) - r) * warmup());

                Draw.tint(NHLiquids.zetaFluid.color);

                Tmp.v1.set(sx, sy).rotate(rotdeg()).add(this);
                Fill.circle(Tmp.v1.x, Tmp.v1.y, scl * (flameRadius + Mathf.absin(Time.time, flameRadiusScl, flameRadiusMag) + cr));
                Draw.color(1f, 1f, 1f, warmup());
                Fill.circle(Tmp.v1.x, Tmp.v1.y, scl * (flameRadiusIn + Mathf.absin(Time.time, flameRadiusScl, flameRadiusInMag) + cr));

                Draw.color();
            }
        }
    }
}
