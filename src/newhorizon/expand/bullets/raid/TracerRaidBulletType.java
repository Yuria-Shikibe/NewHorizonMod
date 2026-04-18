package newhorizon.expand.bullets.raid;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.graphics.Trail;
import newhorizon.NHSetting;
import newhorizon.content.NHFx;
import newhorizon.util.struct.Vec2Seq;

import static mindustry.Vars.headless;

public class TracerRaidBulletType extends BasicRaidBulletType {
    protected static final Vec2 v1 = new Vec2(), v2 = new Vec2(), v3 = new Vec2();
    protected static final Rand rand = new Rand();

    public int tracerCount = 1;
    public int tracerLength = 12;
    public float tracerWidth = 3f;
    public float tracerRandRange = 10f;
    public float tracerUpdateInterval = 3f;


    @Override
    public void init(Bullet b) {
        Trail[] sideTrails = new Trail[tracerCount];
        for (int i = 0; i < tracerCount; i++) {
            sideTrails[i] = new Trail(tracerLength);
        }
        b.data = sideTrails;
    }

    @Override
    public void removed(Bullet b){
        super.removed(b);
        if (!(b.data() instanceof Trail[])) return;
        Trail[] sideTrails = (Trail[]) b.data;
        for (Trail trail : sideTrails) {
            Fx.trailFade.at(b.x, b.y, tracerWidth, b.team.color, trail.copy());
        }
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        updateSideTrail(b);
    }

    @Override
    public void drawTrail(Bullet b) {
        super.drawTrail(b);
        drawSideTrail(b);
    }

    public void drawSideTrail(Bullet b) {
        if (!(b.data() instanceof Trail[])) return;
        Trail[] sideTrails = (Trail[]) b.data;
        float z = Draw.z();
        Draw.z(z - 0.0001f);
        for (Trail trail : sideTrails) {
            if (tracerLength > 0) {
                if (trail != null) {
                    trail.draw(b.team.color, tracerWidth);
                }
            }
        }
        Draw.z(z);
    }

    public void updateSideTrail(Bullet b) {
        if (!(b.data() instanceof Trail[])) return;
        Trail[] sideTrails = (Trail[]) b.data;
        if (!headless && tracerLength > 0 && b.timer(1, tracerUpdateInterval)) {
            for (int i = 0; i < tracerCount; i++) {
                if (sideTrails[i] == null) sideTrails[i] = new Trail(tracerLength);
                sideTrails[i].update(
                        b.x + Mathf.random(-tracerRandRange, tracerRandRange),
                        b.y + Mathf.random(-tracerRandRange, tracerRandRange),
                        trailInterp.apply(b.fin())
                );
            }
        }
    }
}
