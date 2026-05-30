package newhorizon.expand.bullets;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.gen.Bullet;
import mindustry.type.StatusEffect;
import newhorizon.content.NHStatusEffects;
import newhorizon.util.game.PosLightning;

import static mindustry.Vars.state;

public class EmpDotBulletType extends ContinuousBulletType {

    public float DOTRadius = 12f;
    public float DOTDamage = 100f;
    public float radIncrease = 0.25f;
    public Effect sparkFx = Fx.none;
    public StatusEffect effect = NHStatusEffects.emp2;

    public EmpDotBulletType() {
        speed = 0f;
        lifetime = 120f;
        collides = false;
        hittable = false;
        absorbable = false;

        damage = DOTDamage;
        buildingDamageMultiplier = 0f;
        despawnEffect = hitEffect = Fx.none;
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
        rad += radIncrease * Time.delta;
        b.data = rad;
        if (b.timer(2, damageInterval)) {
            Damage.damage(b.team, b.x, b.y, rad * 1.2f, DOTDamage * b.damageMultiplier());
            Units.nearby(null, b.x, b.y, rad, unit -> {
                if (unit.team != b.team) {
                    unit.apply(effect, 30f);
                }
            });
        }

        for (int i = 0; i < 2; i++) {
            float chance = Mathf.lerp(0.5f, 0.2f, b.time / b.lifetime);
            if (Mathf.chanceDelta(chance)) {
                float a0 = Mathf.random(360) + b.rotation();
                float r0 = Mathf.random(-rad / 5, rad / 2) + rad;
                float a1 = Mathf.random(360) + b.rotation();
                float r1 = Mathf.random(-rad / 5, rad / 2) + rad;

                Tmp.v1.set(b.x + r0 * Mathf.sinDeg(a0), b.y + r0 * Mathf.cosDeg(a0));
                Tmp.v2.set(b.x + r1 * Mathf.sinDeg(a1), b.y + r1 * Mathf.cosDeg(a1));

                PosLightning.createEffect(Tmp.v1, Tmp.v2, lightningColor, 1, 2.5f);
                sparkFx.at(Tmp.v1);
                sparkFx.at(Tmp.v2);
            }
        }
    }
}
