package newhorizon.expand.block.distribution.delivery;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.struct.IntSeq;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class CargoDeliveryDraw {
    public static void drawNodeLinks(Building build) {
        if (headless || !CargoDeliveryGraph.isRelay(build)) return;

        IntSeq links = CargoDeliveryGraph.linksOf(build);
        if (links.isEmpty()) return;

        float oldZ = Draw.z();
        Draw.z(Layer.power);

        for (int i = 0; i < links.size; i++) {
            Building other = world.build(links.get(i));
            if (other == null || other.team != build.team) continue;

            if (CargoDeliveryGraph.isRelay(other)) {
                if (other.id < build.id) continue;
                drawLink(build, other, Pal.place, 0.55f);
            } else if (CargoDeliveryGraph.isDeposit(other)) {
                drawLink(build, other, Pal.heal, 0.45f);
            }
        }

        Draw.z(oldZ);
        Draw.reset();
    }

    private static void drawLink(Building a, Building b, Color color, float alpha) {
        float angle = Angles.angle(a.x, a.y, b.x, b.y);
        float x1 = a.x + Angles.trnsx(angle, a.block.size * tilesize / 2f);
        float y1 = a.y + Angles.trnsy(angle, a.block.size * tilesize / 2f);
        float x2 = b.x + Angles.trnsx(angle + 180f, b.block.size * tilesize / 2f);
        float y2 = b.y + Angles.trnsy(angle + 180f, b.block.size * tilesize / 2f);

        Draw.color(Pal.gray, alpha * 0.75f);
        Lines.stroke(2.8f);
        Lines.line(x1, y1, x2, y2);

        Draw.color(color, alpha);
        Lines.stroke(1.2f);
        Lines.line(x1, y1, x2, y2);

        Draw.reset();
    }
}
