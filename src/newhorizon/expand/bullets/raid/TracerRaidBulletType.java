package newhorizon.expand.bullets.raid;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import newhorizon.NHSetting;
import newhorizon.content.NHFx;
import newhorizon.util.struct.Vec2Seq;

public class TracerRaidBulletType extends BasicRaidBulletType {
    protected static final Vec2 v1 = new Vec2(), v2 = new Vec2(), v3 = new Vec2();
    protected static final Rand rand = new Rand();
    public int tracers = 2;
    public int tracerFadeOffset = 10;
    public int tracerStrokeOffset = 15;
    public float tracerStroke = 3f;
    public float tracerSpacing = 8f;
    public float tracerRandX = 6f;
    public float tracerUpdateSpacing = 0.3f;

    private void addEndVec(Bullet b) {
        if (!Vars.headless && (b.data instanceof Vec2Seq[])) {
            Vec2Seq[] pointsArr = (Vec2Seq[]) b.data();
            for (Vec2Seq points : pointsArr) {
                points.add(b.x, b.y);
                points.add(tracerStroke, tracerFadeOffset);
                NHFx.lightningFade.at(b.x, b.y, tracerStrokeOffset, b.team.color, points);
            }

            b.data = null;
        }
    }

    @Override
    public void despawned(Bullet b) {
        super.despawned(b);
        addEndVec(b);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        super.hitEntity(b, entity, health);
        hit(b);
    }

    @Override
    public void hit(Bullet b) {
        super.hit(b);
        addEndVec(b);
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        if (Vars.headless || (!NHSetting.enableDetails() && trailLength > 0)) return;
        Vec2Seq[] points = new Vec2Seq[tracers];
        for (int i = 0; i < tracers; i++) {
            Vec2Seq p = new Vec2Seq();
            points[i] = p;
        }
        b.data = points;
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        if (!Vars.headless && b.timer(2, tracerUpdateSpacing)) {
            if (!(b.data instanceof Vec2Seq[])) return;
            Vec2Seq[] points = (Vec2Seq[]) b.data();
            for (Vec2Seq seq : points) {
                v2.trns(b.rotation(), 0, rand.range(tracerRandX));
                v1.setToRandomDirection(rand).scl(tracerSpacing);
                seq.add(v3.set(b.x, b.y).add(v1).add(v2));
            }
        }
    }

    @Override
    public void drawTrail(Bullet b) {
        super.drawTrail(b);

        if ((b.data instanceof Vec2Seq[])) {
            Vec2Seq[] pointsArr = (Vec2Seq[]) b.data();
            for (Vec2Seq points : pointsArr) {
                if (points.size() < 2) return;
                Draw.color(b.team.color);
                for (int i = 1; i < points.size(); i++) {
                    Lines.stroke(Mathf.clamp((i + tracerFadeOffset / 2f) / points.size() * (tracerStrokeOffset - (points.size() - i)) / tracerStrokeOffset) * tracerStroke);
                    points.setVec2(i - 1, Tmp.v1);
                    points.setVec2(i, Tmp.v2);
                    Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, false);
                    Fill.circle(Tmp.v1.x, Tmp.v1.y, Lines.getStroke() / 2);
                }
                Fill.circle(points.peekTmp().x, points.peekTmp().y, tracerStroke);
            }
        }
    }
}
