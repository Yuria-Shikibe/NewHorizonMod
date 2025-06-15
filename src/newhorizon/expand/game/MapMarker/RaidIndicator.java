package newhorizon.expand.game.MapMarker;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.game.MapObjectives;
import mindustry.game.Team;
import mindustry.graphics.Layer;
import newhorizon.content.NHContent;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.util.graphic.DrawFunc;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class RaidIndicator extends MapObjectives.PosMarker {
    public Vec2 source = new Vec2();
    public Vec2 target = new Vec2();
    public int teamID = Team.crux.id;
    public int icon = 0;
    public float radius = 50;
    public String timerName = "event-timer";

    public RaidIndicator(String name){
        timerName = name;
    }

    public RaidIndicator() {}

    public TextureRegion icon() {
        return switch (icon) {
            case 1 -> NHContent.raid;
            case 2 -> NHContent.fleet;
            case 3 -> NHContent.capture;
            default -> NHContent.objective;
        };
    }

    public RaidIndicator init(int teamID, int icon, float radius, String timerName) {
        this.teamID = teamID;
        this.icon = icon;
        this.radius = radius;
        this.timerName = timerName;
        return this;
    }

    public RaidIndicator setPosition(Vec2 source, Vec2 target) {
        this.source.set(source);
        this.target.set(target);
        return this;
    }

    @Override
    public void draw(float scaleFactor) {
        draw();
        drawArrow();
    }

    public void draw() {
        Team team = Team.get(teamID);

        float fin = progress();

        Draw.blend(Blending.additive);
        Draw.z(Layer.legUnit + 1);
        Draw.color(team.color, Color.white, 0.075f);
        Draw.alpha(0.65f);

        float f = Interp.pow3Out.apply(Mathf.curve(1 - fin, 0, 0.01f));

        Draw.rect(icon(), target, NHContent.fleet.width * f * Draw.scl, NHContent.fleet.height * f * Draw.scl, 0);
        Lines.stroke(5f * f);
        Lines.circle(target.x, target.y, radius * (1 + Mathf.absin(4f, 0.055f)));

        DrawFunc.circlePercent(target.x, target.y, radius * (0.875f), fin, 0);

        Draw.reset();
        Draw.blend();
    }

    public void drawArrow() {
        float ang = source.angleTo(target);

        Draw.color(Team.get(teamID).color, Color.white, 0.075f);
        Draw.blend(Blending.additive);

        float size = NHContent.arrowRegion.height;

        for (int i = 0; i < radius / size * tilesize; i++) {
            float s = (1 - ((Time.time + 25 * i) % 100) / 100) * scale() * Draw.scl * 1.75f;
            Tmp.v1.trns(ang + 180, 36 + 12 * i).add(target);
            Draw.rect(NHContent.arrowRegion, Tmp.v1, size * s, size * s, ang - 90);
        }

        Draw.blend();
    }

    public float progress() {
        AtomicReference<Float> progress = new AtomicReference<>(0f);
        state.rules.objectives.each(mapObjective -> {
            if (mapObjective instanceof TriggerObjective obj && Objects.equals(obj.timer, timerName)) {
                progress.set(Mathf.clamp(obj.getCountup() / obj.duration));
            }
        });
        return progress.get();
    }

    public float scale() {
        return Interp.pow3Out.apply(Mathf.curve(1 - progress(), 0, 0.05f));
    }
}
