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
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.production.factory.AdaptCrafter;

import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class GlassQuantifier extends AdaptCrafter {
    public TextureRegion[] rotRegion;
    public TextureRegion liquidRegion, topRegion;
    public float flameRadius = 3f, flameRadiusIn = 1.9f, flameRadiusScl = 5f, flameRadiusMag = 2f, flameRadiusInMag = 1f;

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

        clipSize = 4 * tilesize;
    }

    @Override
    public void load() {
        super.load();

        rotRegion = new TextureRegion[4];
        for (int i = 0; i < rotRegion.length; i++) {
            rotRegion[i] = Core.atlas.find(name + "-rotation-" + i);
        }
        liquidRegion = Core.atlas.find(name + "-liquid");
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public void loadIcon() {
        super.loadIcon();
        uiIcon = Core.atlas.find(name + "-icon");
    }

    public class GlassQuantifierBuild extends AdaptCrafterBuild {
        public void draw() {
            Draw.rect(rotRegion[rotation], x, y);
            Draw.color(NHLiquids.quantumEntity.color, liquids.get(NHLiquids.quantumEntity) / block.liquidCapacity);
            Draw.rect(liquidRegion, x, y, rotdeg());
            Draw.color();

            drawFlame(1, 1, 0.8f);
            drawFlame(8, 0, 0.5f);
            drawFlame(0, 8, 0.5f);
            Draw.z(Layer.block + 0.1f);
            Draw.rect(topRegion, x, y, rotdeg());
        }

        public void drawFlame(float sx, float sy, float scl) {
            if(warmup() > 0f){
                float g = 0.3f;
                float r = 0.06f;
                float cr = Mathf.random(0.1f);

                Draw.z(Layer.block + 0.01f);

                Draw.alpha(((1f - g) + Mathf.absin(Time.time, 8f, g) + Mathf.random(r) - r) * warmup());

                Draw.tint(NHLiquids.quantumEntity.color);

                Tmp.v1.set(sx, sy).rotate(rotdeg()).add(this);
                Fill.circle(Tmp.v1.x, Tmp.v1.y, scl * (flameRadius + Mathf.absin(Time.time, flameRadiusScl, flameRadiusMag) + cr));
                Draw.color(1f, 1f, 1f, warmup());
                Fill.circle(Tmp.v1.x, Tmp.v1.y, scl * (flameRadiusIn + Mathf.absin(Time.time, flameRadiusScl, flameRadiusInMag) + cr));

                Draw.color();
            }
        }
    }
}
