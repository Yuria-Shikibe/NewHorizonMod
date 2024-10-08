package newhorizon.expand.block.production.drill;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Items;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import newhorizon.content.NHItems;

import static mindustry.type.ItemStack.with;

public class SpeedModule extends DrillModule{
    public TextureRegion[] arrow = new TextureRegion[3];
    public SpeedModule() {
        super("speed-module");
        health = 760;
        requirements(Category.production, with(NHItems.juniorProcessor, 30, NHItems.presstanium, 25, Items.phaseFabric, 10, NHItems.multipleSteel, 20));
        size = 2;
        boostSpeed = 0.5f;
        powerMul = 0.4f;
        powerExtra = 80f;

        stackable = true;
    }

    @Override
    public void load() {
        super.load();
        for (int i = 0; i < 3; i++){
            arrow[i] = Core.atlas.find(name + "-arrow-" + i);
        }
    }

    public class SpeedModuleBuild extends DrillModuleBuild{
        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.effect);
            for (int i = 0; i < 3; i++){
                float scl = (Mathf.sinDeg(-Time.time * 3 + 120 * i) * 1.2f + (Mathf.sinDeg(-Time.time * 3 + 120 * i + 120)) * 0.6f) * smoothWarmup;
                Draw.alpha(scl);
                Draw.rect(arrow[i], x, y, rotdeg());
            }
            Draw.reset();
        }
    }
}
