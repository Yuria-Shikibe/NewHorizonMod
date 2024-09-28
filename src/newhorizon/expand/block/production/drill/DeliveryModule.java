package newhorizon.expand.block.production.drill;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.type.ItemStack.with;

public class DeliveryModule extends DrillModule{
    public DeliveryModule() {
        super("delivery-module");

        requirements(Category.production, with(Items.copper, 25, Items.lead, 20, Items.titanium, 35));
        size = 2;
        powerMul = 1.5f;
        powerExtra = 600f;
        coreSend = true;
    }

    public class DeliveryModuleBuild extends DrillModuleBuild{
        @Override
        public void draw() {
            super.draw();

            Draw.z(Layer.effect);
            Draw.color(team.color, Color.white, 0.2f);
            Lines.stroke(1.2f * smoothWarmup);


            float ang1 = DrawFunc.rotator_90(DrawFunc.cycle(Time.time / 4f, 0, 45), 0.15f);
            float ang2 = DrawFunc.rotator_90(DrawFunc.cycle(Time.time / 3f, 0, 120), 0.15f);

            Lines.spikes(x, y, 8 + 4 * Mathf.sinDeg(Time.time * 3f + 20), 3 + Mathf.sinDeg(Time.time * 2.5f), 4, ang1 + 45);
            Lines.spikes(x, y, 7 + 3 * Mathf.sinDeg(Time.time * 3.2f), 4 + 1.2f * Mathf.sinDeg(Time.time * 2.2f), 4, ang2);

            Lines.square(x, y, 8, Time.time / 8f);
            Lines.square(x, y, 8, -Time.time / 8f);

        }
    }
}
