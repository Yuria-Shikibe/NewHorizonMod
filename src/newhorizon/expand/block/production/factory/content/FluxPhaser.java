package newhorizon.expand.block.production.factory.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.util.Tmp;
import mindustry.content.Items;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.production.factory.AdaptCrafter;
import newhorizon.util.func.MathUtil;
import newhorizon.util.graphic.DrawFunc;

import java.util.concurrent.atomic.AtomicInteger;

import static mindustry.type.ItemStack.with;

public class FluxPhaser extends AdaptCrafter {
    public TextureRegion icon;
    public TextureRegion baseRegion;
    public TextureRegion glowRegion;
    public TextureRegion[] rotRegion;

    public FluxPhaser() {
        super("flux-phaser");

        requirements(Category.crafting, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));

        size = 3;

        addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

        craftTime = 5f;
        consumePower(4);
        consumeItems(with(Items.silicon, 1, NHItems.zeta, 1));
        outputItems = with(Items.phaseFabric, 1);

        itemCapacity = 20;
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
        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y, rotdeg());

            if(warmup() > 0.001f) {
                Draw.color(Pal.accent);
                Draw.alpha(MathUtil.timeValue(0.9f, 1.1f, 0.3f) * warmup());
                Tmp.v1.trns(rotdeg(), MathUtil.timeValue(-18, 18, 1.223f, 0.224f)).add(this);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() + 90, 8);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() - 90, 8);

                Draw.color(Pal.accent, Color.white, 0.1f);
                Draw.alpha(MathUtil.timeValue(0.9f, 1.1f, 0.5f) * warmup());
                Tmp.v1.trns(rotdeg(), MathUtil.timeValue(-18, 18, 1.451f, 0.123f)).add(this);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() + 90, 8);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() - 90, 8);

                Draw.color(Pal.accent, Color.white, 0.2f);
                Draw.alpha(MathUtil.timeValue(0.9f, 1.1f, 0.723f) * warmup());
                Tmp.v1.trns(rotdeg(), MathUtil.timeValue(-18, 18, 1.812f, 0.323f)).add(this);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() + 90, 8);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() - 90, 8);

                Draw.color(Pal.accent, Color.white, 0.04f);
                Draw.alpha(MathUtil.timeValue(0.9f, 1.1f, 0.923f) * warmup());
                Tmp.v1.trns(rotdeg(), MathUtil.timeValue(-18, 18, 2.321f, 0.643f)).add(this);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() + 90, 8);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() - 90, 8);

                Draw.color(Pal.accent, Color.white, 0.12f);
                Draw.alpha(MathUtil.timeValue(0.9f, 1.1f, 1.171f) * warmup());
                Tmp.v1.trns(rotdeg() + 90, MathUtil.timeValue(-10, 10, 0.72f)).add(this);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg(), 15);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() + 180, 15);

                Draw.color(Pal.accent, Color.white, 0.03f);
                Draw.alpha(MathUtil.timeValue(0.9f, 1.1f, 1.441f) * warmup());
                Tmp.v1.trns(rotdeg() + 90, MathUtil.timeValue(-10, 10, 1.123f, 0.12f)).add(this);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg(), 15);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, rotdeg() + 180, 15);
            }

            Draw.color();

            Draw.z(Layer.block + 0.1f);
            Draw.rect(rotRegion[rotation], x, y);

            if(warmup() > 0.001f) {
                Draw.z(Layer.effect);
                Draw.color(Pal.accent);
                Draw.alpha(MathUtil.timeValue(0.9f, 1.1f, 0.8f) * warmup());
                Draw.rect(glowRegion, x, y, rotdeg());
                Draw.reset();
                Draw.z(Layer.block);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            AtomicInteger i = new AtomicInteger(0);

            linkProximityMap.each((target, source) -> {
                Draw.color(Pal.remove);
                Draw.alpha(0.5f);
                Draw.z(Layer.block + 3f);

                Lines.line(target.x, target.y, source.x, source.y);
                Fill.square(target.x, target.y, target.hitSize()/2f - 2f);

                DrawFunc.drawText(i + "", target.x, target.y);
                i.getAndIncrement();
            });
        }
    }
}
