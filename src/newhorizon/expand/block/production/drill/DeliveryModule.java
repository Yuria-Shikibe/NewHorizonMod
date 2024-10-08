package newhorizon.expand.block.production.drill;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import newhorizon.content.NHItems;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.type.ItemStack.with;

public class DeliveryModule extends DrillModule{
    public DeliveryModule() {
        super("delivery-module");
        health = 800;

        requirements(Category.production, with(NHItems.juniorProcessor, 50, NHItems.irayrondPanel, 25, NHItems.seniorProcessor, 50, NHItems.multipleSteel, 50, NHItems.setonAlloy, 10));
        size = 2;
        powerMul = 1.2f;
        powerExtra = 300f;
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
