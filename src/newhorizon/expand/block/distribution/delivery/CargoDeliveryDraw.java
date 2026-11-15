package newhorizon.expand.block.distribution.delivery;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class CargoDeliveryDraw {
    public static final float fullHoverTime = 90f;

    protected static final Seq<Building> tmpPath = new Seq<>();

    public static boolean hovered(Building build) {
        if (headless || control == null || control.input == null || control.input.isPlacing()) return false;

        return Core.scene != null && !Core.scene.hasMouse()
                && world.buildWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY()) == build;
    }

    public static void drawNetwork(Building focus, float hoverTime) {
        if (focus == null || !CargoDeliveryGraph.isParticipant(focus)) return;

        CargoDeliveryGraph.ensureUpdated();
        CargoDeliveryGraph graph = CargoDeliveryGraph.get(focus.team);
        Seq<Building> component = graph.component(focus);

        float detail = Mathf.curve(hoverTime, 60f, fullHoverTime);
        Color focusColor = typeColor(focus);

        float oldZ = Draw.z();
        Draw.z(Layer.overlayUI);

        Seq<Building> direct = graph.adjacency.get(focus);
        if (direct != null) {
            for (Building other : direct) {
                drawLink(focus, other, focusColor, 0.22f + 0.12f * detail, false, false);
            }
        }

        for (Building a : component) {
            if (a == focus) continue;

            Seq<Building> links = graph.adjacency.get(a);
            if (links == null) continue;

            for (Building b : links) {
                if (b == focus || !component.contains(b) || a.id >= b.id) continue;
                drawLink(a, b, Pal.lightishGray, 0.12f + 0.10f * detail, false, false);
            }
        }

        if (detail > 0.01f) {
            CargoDeliveryGraph.PathResult result = graph.distances(focus);

            if (CargoDeliveryGraph.isDeposit(focus)) {
                for (Building platform : graph.relays) {
                    if (!(platform.block instanceof CargoDeliveryPlatform) || result.dist.get(platform) == null) continue;

                    if (graph.pathTo(result, focus, platform, tmpPath)) {
                        drawPath(focus, tmpPath, focusColor, 0.55f * detail, true);
                    }
                }
            } else {
                for (CargoDeliveryDeposit.CargoDeliveryDepositBuild deposit : graph.deposits) {
                    if (result.dist.get(deposit) == null) continue;

                    if (graph.pathTo(result, focus, deposit, tmpPath)) {
                        drawPath(focus, tmpPath, focusColor, 0.55f * detail, false);
                    }
                }

                if (focus.block instanceof CargoDeliveryNode) {
                    for (Building platform : graph.relays) {
                        if (!(platform.block instanceof CargoDeliveryPlatform) || result.dist.get(platform) == null) continue;

                        if (graph.pathTo(result, focus, platform, tmpPath)) {
                            drawPath(focus, tmpPath, focusColor, 0.40f * detail, true);
                        }
                    }
                }
            }
        }

        if (detail > 0.01f) {
            for (Building build : component) {
                if (build != focus) drawMarker(build, 0.85f * detail, false);
            }
        }
        drawMarker(focus, 1f, true);

        Draw.z(oldZ);
        Draw.reset();
        tmpPath.clear();
    }

    private static void drawPath(Building focus, Seq<Building> path, Color color, float alpha, boolean reverse) {
        Building previous = focus;

        for (Building next : path) {
            drawLink(previous, next, color, alpha, true, reverse);
            previous = next;
        }
    }

    private static void drawLink(Building a, Building b, Color color, float alpha, boolean animated, boolean reverse) {
        float angle = Angles.angle(a.x, a.y, b.x, b.y);
        float x1 = a.x + Angles.trnsx(angle, a.block.size * tilesize / 2f);
        float y1 = a.y + Angles.trnsy(angle, a.block.size * tilesize / 2f);
        float x2 = b.x + Angles.trnsx(angle + 180f, b.block.size * tilesize / 2f);
        float y2 = b.y + Angles.trnsy(angle + 180f, b.block.size * tilesize / 2f);

        Draw.color(Pal.gray, alpha * 0.8f);
        Lines.stroke(3.5f);
        Lines.line(x1, y1, x2, y2);

        Draw.color(color, alpha);
        Lines.stroke(1.5f);
        Lines.line(x1, y1, x2, y2);

        if (animated) {
            float progress = Time.time / 60f % 1f;
            if (reverse) progress = 1f - progress;

            Draw.color(color, alpha);
            Fill.square(Mathf.lerp(x1, x2, progress), Mathf.lerp(y1, y2, progress), 2.5f, 45f);
        }

        Draw.reset();
    }

    private static void drawMarker(Building build, float alpha, boolean highlight) {
        Color color = typeColor(build);

        Draw.color(Color.white, alpha);

        Drawf.square(build.x, build.y, build.block.size * tilesize / 2f + 2f, 45f,
                Tmp.c1.set(color).a(alpha));

        if (highlight) {
            Drawf.circles(build.x, build.y,
                    build.block.size * tilesize / 2f + 5f + Mathf.absin(4f, 1f), color);
        }

        Draw.reset();
    }

    private static Color typeColor(Building build) {
        if (build.block instanceof CargoDeliveryPlatform) return Pal.accent;
        if (build.block instanceof CargoDeliveryNode) return Pal.place;
        if (build.block instanceof CargoDeliveryDeposit) return Pal.heal;
        return Pal.lightishGray;
    }
}
