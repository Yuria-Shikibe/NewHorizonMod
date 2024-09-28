package newhorizon.expand.block.production.drill;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Items;
import mindustry.graphics.Layer;
import mindustry.type.Category;

import static mindustry.type.ItemStack.with;

public class SpeedModule extends DrillModule{
    public TextureRegion[] arrow = new TextureRegion[3];
    public SpeedModule() {
        super("speed-module");
        requirements(Category.production, with(Items.copper, 25, Items.lead, 20, Items.titanium, 35));
        size = 2;
        boostSpeed = 1f;
        powerMul = 1.0f;
        powerExtra = 180f;

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
