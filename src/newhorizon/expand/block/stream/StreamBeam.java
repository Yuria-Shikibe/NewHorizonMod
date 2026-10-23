package newhorizon.expand.block.stream;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import newhorizon.content.NHContent;
import newhorizon.expand.type.ParticleStream;
import newhorizon.util.func.MathUtil;

import static mindustry.Vars.world;

public class StreamBeam {
    public float lastStrokeScale, lastOutput;
    public Color lastColor = Color.clear.cpy();

    public float amountCap = -1f;
    public int beamLength = 5;

    public ParticleStream currentStream;
    public Building source, target;
    public int distance;
    public boolean clog;

    public int rotationOffset;
    public ParticleStream filter;

    public StreamBeam(Building source) {
        this.source = source;
    }

    public void update() {
        if (source == null) return;

        getCurrentStream();
        clog = false;
        target = null;
        distance = beamLength;

        for (int i = 0; i <= beamLength; i++) {
            Building building = world.build(
                    source.tileX() + Geometry.d4x(getRotation()) * (i + 1),
                    source.tileY() + Geometry.d4y(getRotation()) * (i + 1)
            );
            if (building instanceof StreamBlock.StreamBuild streamBuild) {
                target = building;
                distance = i;
                clog = !streamBuild.acceptStream(this);
                break;
            }
        }

        lastStrokeScale = Mathf.lerpDelta(lastStrokeScale, beamStrokeScale(), 0.05f);
        transportStream();
    }

    public void getCurrentStream() {
        currentStream = null;

        if (!(source instanceof StreamBlock.StreamBuild streamBuild) || streamBuild.streamAmount <= 0.0001f
                || streamBuild.currentStream == null) {
            lastColor.lerp(Color.white, 0.05f * Time.delta);
            return;
        }

        currentStream = filter == null ? streamBuild.currentStream : filter;
        lastColor.lerp(currentStream.color, 0.1f * Time.delta);
    }

    public void transportStream() {
        lastOutput = 0f;
        if (currentStream == null || !(source instanceof StreamBlock.StreamBuild sourceBuild)) return;

        float cap = amountCap >= 0f ? amountCap * source.edelta() : 5f;
        float requested = Math.min(cap, sourceBuild.streamAmount);
        if (requested <= 0f) return;

        float transferred = 0f;
        if (target == null) {
            transferred = requested;
        } else if (target instanceof StreamBlock.StreamBuild targetBuild
                && targetBuild.acceptStream(this) && targetBuild.canAcceptStream(this)) {
            transferred = Math.min(requested, targetBuild.streamCapacity - targetBuild.streamAmount);
            if (transferred > 0f) targetBuild.handleStream(this, transferred);
        } else {
            clog = true;
        }

        sourceBuild.streamAmount -= transferred;
        sourceBuild.clearStreamIfEmpty();
        lastOutput = transferred;
    }

    public int getRotation() {
        return source == null ? 0 : (source.rotation + rotationOffset + 4) % 4;
    }

    public void draw() {
        if (source == null) return;

        Draw.z(Layer.blockOver);

        if (clog) {
            Draw.color(Pal.remove);
            Draw.rect(Icon.warning.getRegion(), source.x, source.y, 4f, 4f);
        }

        float length = Math.max(0f, distance) * 8f;
        float scale = MathUtil.timeValue(0.75f, 1f, 2f) * lastStrokeScale;

        Tmp.v1.setZero().add(4f, 0f).rotate(getRotation() * 90).add(source);
        Tmp.v2.setZero().add(4f + length, 0f).rotate(getRotation() * 90).add(source);

        Draw.alpha(1f);
        Draw.color(lastColor);
        Drawf.laser(NHContent.beamLaser, NHContent.beamLaserEnd, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, scale);
        Draw.color();
        Drawf.laser(NHContent.beamLaserInner, NHContent.beamLaserInnerEnd, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, scale);
    }

    public float beamStrokeScale() {
        if (source == null || currentStream == null || Time.delta <= 0f) return 0f;
        float value = Mathf.clamp(lastOutput / (0.5f * Time.delta));
        return value < 0.01f ? 0f : Mathf.sqrt(value);
    }
}
