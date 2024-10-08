package newhorizon.expand.bullets;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.gen.Bullet;
import mindustry.type.StatusEffect;
import newhorizon.content.NHStatusEffects;
import newhorizon.util.feature.PosLightning;

import static mindustry.Vars.state;

public class DOTBulletType extends ContinuousBulletType {

    public float DOTRadius = 12f;
    public float DOTDamage = 100f;
    public float radIncrease = 0.25f;
    public StatusEffect effect = NHStatusEffects.emp2;
    public Effect fx = Fx.none;
    public DOTBulletType() {
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
    public void draw(Bullet b) {
        float rad = (float) b.data;
        for (int i = 0; i < 2; i++) {
            float chance = Mathf.lerp(0.5f, 0.2f, b.time/b.lifetime);
            if (Mathf.chance(chance) && state.isPlaying() && b.timer(1, 1)){
                float a0 = Mathf.random(360) + b.rotation();
                float r0 = Mathf.random(-rad/5, rad/2) + rad;
                float a1 = Mathf.random(360) + b.rotation();
                float r1 = Mathf.random(-rad/5, rad/2) + rad;

                Vec2 pos0 = new Vec2(b.x + r0 * Mathf.sinDeg(a0), b.y + r0 * Mathf.cosDeg(a0));
                Vec2 pos1 = new Vec2(b.x + r1 * Mathf.sinDeg(a1), b.y + r1 * Mathf.cosDeg(a1));

                PosLightning.createEffect(pos0, pos1, lightningColor, 1, 2.5f);
                fx.at(pos0);
                fx.at(pos1);
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
        if (b.timer(2, damageInterval)){
            Damage.damage(b.team, b.x, b.y, rad * 1.2f, DOTDamage * b.damageMultiplier());
            Units.nearby(null, b.x, b.y, rad, unit -> {
                if (unit.team != b.team){
                    unit.apply(effect, 30f);
                }
            });
        }
    }
}
