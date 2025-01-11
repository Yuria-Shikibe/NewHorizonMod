package newhorizon.expand.block.production.factory.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Rand;
import arc.util.Tmp;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.expand.block.production.factory.AdaptCrafter;
import newhorizon.util.func.MathUtil;

import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class FluxPhaser extends AdaptCrafter {
    public TextureRegion icon;
    public TextureRegion baseRegion;
    public TextureRegion glowRegion;
    public TextureRegion[] rotRegion;

    public FluxPhaser() {
        super("flux-phaser");

        requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                NHItems.irayrondPanel, 100, NHItems.seniorProcessor, 80, Items.plastanium, 60, NHItems.zeta, 100));

        size = 3;

        addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

        craftTime = 120f;
        consumePower(1080 / 60f);
        consumeItems(with(Items.silicon, 6, NHItems.zeta, 6));
        outputItems = with(Items.phaseFabric, 9);

        itemCapacity = 40;
        health = 3000;

        updateEffect = new Effect(40f, 80f, e -> {
            Draw.color(Items.phaseFabric.color, Pal.accent, e.fin() * 0.8f);
            Lines.stroke(2f * e.fout());
            Lines.spikes(e.x, e.y, 12 * e.finpow(), 1.5f * e.fout() + 4 * e.fslope(), 4, 45);
        });
        craftEffect = NHFx.square(Items.phaseFabric.color, 38, 5, 34, 5);

        clipSize = 5 * tilesize;
    }

    @Override
    public void load() {
        super.load();
        icon = Core.atlas.find(name + "-icon");
        baseRegion = Core.atlas.find(name + "-base");
        glowRegion = Core.atlas.find(name + "-glow");

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

    public class FluxPhaserBuilding extends AdaptCrafterBuild{
        public Rand rand = new Rand(id);
        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y, rotdeg());

            rand.setSeed(id);
            if(warmup() > 0.001f) {
                Draw.color(Pal.accent);
                Draw.alpha(MathUtil.timeValue(0.8f, 0.9f, 0.8f) * warmup());
                Draw.rect(glowRegion, x, y, rotdeg());
                Draw.reset();
                Draw.z(Layer.block);

                drawHorizontalLine();
                drawHorizontalLine();
                drawHorizontalLine();
                drawHorizontalLine();
                drawHorizontalLine();
                drawHorizontalLine();

                drawVerticalLine();
                drawVerticalLine();
                drawVerticalLine();
            }
            Draw.color();
            Draw.rect(rotRegion[rotation], x, y);
        }

        public void drawVerticalLine(){
            Draw.color(Pal.accent, Color.white, 0.5f);
            Draw.alpha(MathUtil.timeValue(0.9f, 1.1f, rand.random(1f)) * warmup());
            Tmp.v1.trns(rotdeg(), MathUtil.timeValue(-18, 18, rand.random(0.5f, 1.5f), rand.random(4f))).add(this);
            Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() + 90, 8);
            Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() - 90, 8);
        }

        public void drawHorizontalLine(){
            Draw.color(Pal.accent, Color.white, 0.5f);
            Draw.alpha(MathUtil.timeValue(0.9f, 1.1f, rand.random(1f)) * warmup());
            Tmp.v1.trns(rotdeg() + 90, MathUtil.timeValue(-10, 10, rand.random(0.5f, 1.2f), rand.random(4f))).add(this);
            Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg(), 15);
            Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() + 180, 15);
        }
    }
}
