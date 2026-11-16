package newhorizon.expand.block.distribution.delivery;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.IntSeq;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class CargoDeliveryDraw {
    public static void drawNodeLinks(Building build) {
        if (headless || (!CargoDeliveryGraph.isRelay(build) && !CargoDeliveryGraph.isDeposit(build))) return;

        IntSeq links = CargoDeliveryGraph.linksOf(build);
        if (links.isEmpty()) return;

        float oldZ = Draw.z();
        Draw.z(Layer.power);
        boolean fullTerminal = build.block instanceof CargoDeliveryNode;
        if (fullTerminal) drawTerminal(build);

        for (int i = 0; i < links.size; i++) {
            Building other = world.build(links.get(i));
            if (other == null || other.team != build.team) continue;

            if (!fullTerminal) {
                drawTerminalArc(build, Angles.angle(build.x, build.y, other.x, other.y));
            }

            if (CargoDeliveryGraph.isRelay(other)) {
                if (!CargoDeliveryGraph.isRelay(build) || other.id < build.id) continue;
                drawLink(build, other, Pal.place, 0.82f);
            } else if (CargoDeliveryGraph.isDeposit(other)) {
                if (CargoDeliveryGraph.isDeposit(build)) continue;
                drawLink(build, other, Pal.heal, 0.76f);
            }
        }

        Draw.z(oldZ);
        Draw.reset();
    }

    private static void drawLink(Building a, Building b, Color color, float alpha) {
        float angle = Angles.angle(a.x, a.y, b.x, b.y);
        float radiusA = terminalRadius(a);
        float radiusB = terminalRadius(b);
        float x1 = a.x + Angles.trnsx(angle, radiusA);
        float y1 = a.y + Angles.trnsy(angle, radiusA);
        float x2 = b.x + Angles.trnsx(angle + 180f, radiusB);
        float y2 = b.y + Angles.trnsy(angle + 180f, radiusB);

        Draw.color(a.team.color, alpha * 0.95f);
        Lines.stroke(2.7f);
        Lines.line(x1, y1, x2, y2);

        Draw.color(color, alpha * 0.95f);
        Lines.stroke(1.8f);
        Lines.line(x1, y1, x2, y2);

        float pulse = 0.85f + Mathf.absin(Time.time, 5f, 0.15f);
        Draw.color(a.team.color, alpha * pulse);
        Fill.circle(x1, y1, 2.4f);
        Fill.circle(x2, y2, 2.4f);
        Draw.color(color, alpha * 1.15f);
        Fill.circle(x1, y1, 1.05f);
        Fill.circle(x2, y2, 1.05f);

        Draw.reset();
    }

    private static void drawTerminal(Building build) {
        float radius = terminalRadius(build);
        float pulse = 0.82f + Mathf.absin(Time.time, 5f, 0.18f);

        Draw.color(build.team.color, 0.18f * pulse);
        Lines.stroke(4.2f);
        Lines.circle(build.x, build.y, radius);

        Draw.color(build.team.color, 0.82f * pulse);
        Lines.stroke(1f);
        Lines.circle(build.x, build.y, radius);
    }

    private static void drawTerminalArc(Building build, float angle) {
        float radius = terminalRadius(build);
        float pulse = 0.82f + Mathf.absin(Time.time, 5f, 0.18f);
        float arcAngle = 54f;

        Draw.color(build.team.color, 0.22f * pulse);
        Lines.stroke(3.2f);
        Lines.arc(build.x, build.y, radius, arcAngle / 360f, angle - arcAngle / 2f);

        Draw.color(build.team.color, 0.88f * pulse);
        Lines.stroke(1f);
        Lines.arc(build.x, build.y, radius, arcAngle / 360f, angle - arcAngle / 2f);
    }

    private static float terminalRadius(Building build) {
        return build.block.size * tilesize / 2f + 3.5f;
    }
}
