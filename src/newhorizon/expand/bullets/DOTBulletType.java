package newhorizon.expand.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.entities.Damage;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import newhorizon.content.NHFx;
import newhorizon.util.feature.PosLightning;

public class DOTBulletType extends BasicBulletType {

    public float DOTRadius = 12f;
    public float DOTDamage = 100f;
    public float radIncrease = 0.25f;
    public DOTBulletType() {
        speed = 0f;
        lifetime = 120f;
        collides = false;
        hittable = false;
        absorbable = false;
    }

    @Override
    public void draw(Bullet b) {
        float rad = (float) b.data;
        for (int i = 0; i < 2; i++) {
            float chance = Mathf.lerp(0.05f, 0.02f, b.time/b.lifetime);
            if (Mathf.chance(chance) && b.timer(1, 2)){
                float a0 = Mathf.random(360) + b.rotation();
                float r0 = Mathf.random(-rad/5, rad/2) + rad;
                float a1 = Mathf.random(360) + b.rotation();
                float r1 = Mathf.random(-rad/5, rad/2) + rad;

                Vec2 pos0 = new Vec2(b.x + r0 * Mathf.sinDeg(a0), b.y + r0 * Mathf.cosDeg(a0));
                Vec2 pos1 = new Vec2(b.x + r1 * Mathf.sinDeg(a1), b.y + r1 * Mathf.cosDeg(a1));

                PosLightning.createEffect(pos0, pos1, Pal.techBlue, 1, 2.5f);
                NHFx.triSpark.at(pos0);
                NHFx.triSpark.at(pos1);
            }
        }
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        b.data = DOTRadius;
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        float rad = (float) b.data;
        rad += radIncrease;
        b.data = rad;
        if (b.timer(2, 5)){
            Damage.damage(b.team, b.x, b.y, rad * 1.2f, DOTDamage * b.damageMultiplier());
        }
    }
}
