package newhorizon.expand.game.MapMarker;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.game.MapObjectives;
import mindustry.game.Team;
import newhorizon.content.NHContent;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.util.graphic.DrawFunc;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.state;

public class RaidIndicator extends MapObjectives.PosMarker {
    public Vec2 source = new Vec2();
    public Vec2 target = new Vec2();
    public int teamID = Team.crux.id;
    public int icon = 0;
    public float radius = 50;
    public String timerName = "event-timer";
    public String iconName = "";
    public int eventSeed;
    public int markerId;
    /** Used by runtime event markers that are not backed by a map objective timer. */
    public float progressOverride = Float.NaN;

    public RaidIndicator(String name) {
        timerName = name;
    }

    public RaidIndicator() {
    }

    public TextureRegion icon() {
        if (iconName != null && !iconName.isEmpty()) return Core.atlas.find(iconName);
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

    public RaidIndicator setProgress(float progress) {
        progressOverride = progress;
        return this;
    }

    public RaidIndicator setIconName(String iconName) {
        this.iconName = iconName == null ? "" : iconName;
        return this;
    }

    public RaidIndicator setEventSeed(int eventSeed) {
        this.eventSeed = eventSeed;
        return this;
    }

    public RaidIndicator setMarkerId(int markerId) {
        this.markerId = markerId;
        return this;
    }

    @Override
    public void draw(float scaleFactor) {
        draw();
        drawArrow();
    }

    public void draw() {
        Team team = Team.get(teamID);
        TextureRegion markerIcon = icon();

        float fin = progress();

        // Minimap entities and labels have already been drawn when markers are rendered.
        // Additive blending here would brighten every label pixel underneath the warning.
        Draw.blend();
        Draw.color(team.color, Color.white, 0.075f);
        Draw.alpha(0.65f);

        float f = Interp.pow3Out.apply(Mathf.curve(1 - fin, 0, 0.01f));

        float iconScale = NHContent.fleet.width * f * Draw.scl
                / Math.max(Math.max(markerIcon.width, markerIcon.height), 1f);
        Draw.rect(markerIcon, target, markerIcon.width * iconScale, markerIcon.height * iconScale, 0);
        Lines.stroke(5f * f);
        Lines.circle(target.x, target.y, radius * (1 + Mathf.absin(4f, 0.055f)));

        DrawFunc.circlePercent(target.x, target.y, radius * (0.875f), fin, 0);

        Draw.reset();
        Draw.blend();
    }

    public void drawArrow() {
        float f = scale();
        float ang = source.angleTo(target);
        float outerR = radius * (1f + Mathf.absin(4f, 0.055f));

        Draw.blend();
        Draw.color(Team.get(teamID).color, Color.white, 0.075f);
        Draw.alpha(0.65f);

        for (int i = 0; i < 4; i++) {
            float s = (1 - ((Time.time + 25 * i) % 100) / 100) * f * Draw.scl * 1.75f;
            Tmp.v1.trns(ang + 180, outerR + 16f + 28f * i).add(target);
            Draw.rect(NHContent.arrowRegion, Tmp.v1, NHContent.arrowRegion.width * s, NHContent.arrowRegion.height * s, ang - 90);
        }

        Draw.reset();
        Draw.blend();
    }

    public float progress() {
        if (!Float.isNaN(progressOverride)) return Mathf.clamp(progressOverride);

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
