package newhorizon.expand.bullets;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Trail;

import static mindustry.Vars.headless;

public class TracerBulletType extends BasicBulletType {
    public float tracerRandRange = 8f;
    public float tracerUpdateInterval = 1f;

    public void updateTrail(Bullet b) {
        if (!headless && trailLength > 0) {
            if (b.trail == null) {
                b.trail = new Trail(trailLength);
            }
            b.trail.length = trailLength;
            if (b.timer(1, tracerUpdateInterval)) {
                b.trail.update(b.x + Mathf.random(-tracerRandRange, tracerRandRange), b.y + Mathf.random(-tracerRandRange, tracerRandRange), trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
            }
        }
    }
}
