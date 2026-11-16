package newhorizon.expand.block.distribution.delivery;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.graphics.Trail;

import static mindustry.Vars.headless;
import static mindustry.Vars.state;
import static mindustry.Vars.world;

public class CargoDeliveryBolt extends BulletType {

    public CargoDeliveryBolt() {
        super(1f, 0f);
        collides = true;
        collidesTiles = false;
        lifetime = 1f;
        drawSize = 32f;
        hitEffect = Fx.none;
        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        smokeEffect = Fx.none;
        trailLength = 18;
        trailWidth = 2.2f;
    }

    @Override
    public void init(Bullet b) {
        super.init(b);

        if (!(b.data() instanceof CargoDeliveryData data)) return;

        data.speed = Math.max(0.001f, b.vel.len());
        b.vel.setZero();
        data.buildSpline();

        if (data.controlPoints != null && data.controlPoints.length >= 3) {
            b.rotation(Angles.angle(
                    data.controlPoints[1].x, data.controlPoints[1].y,
                    data.controlPoints[2].x, data.controlPoints[2].y
            ));
            b.lifetime = Math.max(b.lifetime, data.length / data.speed * 3f + data.accelTime * 2f + 60f);
        }
    }

    @Override
    public void update(Bullet b) {
        if (!(b.data() instanceof CargoDeliveryData data)) {
            b.remove();
            return;
        }

        if (data.controlPoints == null || data.spline.controlPoints == null
                || data.spline.spanCount <= 0 || data.to == null) {
            b.remove();
            return;
        }

        data.travelTime += Time.delta;
        float accel = Mathf.clamp(data.travelTime / Math.max(data.accelTime, 0.001f));
        float speedFactor = 0.15f + 0.85f * Interp.pow2In.apply(accel);
        data.progress = Math.min(1f, data.progress + data.speed * speedFactor * Time.delta / data.length);

        while (data.pathIndex < data.path.size && data.progress >= (data.pathIndex + 1f) / data.spline.spanCount) {
            Building relay = world.build(data.path.get(data.pathIndex));
            if (relay == null || relay.dead() || relay.team != b.team
                    || !(relay.block instanceof CargoDeliveryNode || relay.block instanceof CargoDeliveryPlatform)) {
                b.remove();
                return;
            }

            if (relay instanceof CargoDeliveryNode.CargoDeliveryNodeBuild node) {
                node.onBoltPass();
            }
            data.pathIndex++;
        }

        if (data.progress >= 1f) {
            Building target = data.to;
            if (target == null || target.dead() || target.team != b.team
                    || !(target.block instanceof CargoDeliveryDeposit)) {
                b.remove();
                return;
            }

            ((CargoDeliveryDeposit.CargoDeliveryDepositBuild) target).handlePayload(b, data);
            return;
        }

        data.spline.valueAt(Tmp.v1, data.progress);
        data.spline.derivativeAt(Tmp.v2, data.progress);

        b.set(Tmp.v1.x, Tmp.v1.y);
        if (Tmp.v2.len2() > 0.0001f) {
            b.rotation(Tmp.v2.angle());
        }
        b.vel.setZero();

        updateTrail(b);
    }

    @Override
    public void draw(Bullet b) {
        if (!(b.data() instanceof CargoDeliveryData data)) return;

        Color color = data.item != null ? data.item.color : data.liquid != null ? data.liquid.color : Color.white;
        drawTrail(b, color);

        Draw.color(color);
        Fill.circle(b.x, b.y, 3.5f);
        Draw.alpha(0.65f);
        Lines.stroke(1.2f);
        Lines.circle(b.x, b.y, 6.5f);
        Draw.reset();
    }

    @Override
    public void updateTrail(Bullet b) {
        if (headless || trailLength <= 0) return;

        if (b.trail == null) {
            b.trail = new Trail(trailLength);
        }
        b.trail.length = trailLength;
        b.trail.update(b.x, b.y, trailInterp.apply(b.fin()));
    }

    protected void drawTrail(Bullet b, Color color) {
        if (trailLength <= 0 || b.trail == null || b.trail.size() <= 1) return;

        float z = Draw.z();
        Draw.z(z - 0.0001f);

        Tmp.c1.set(color).a(0.22f);
        b.trail.draw(Tmp.c1, trailWidth * 2.1f);
        Tmp.c1.set(color).a(0.72f);
        b.trail.draw(Tmp.c1, trailWidth * 0.7f);

        Draw.z(z);
    }

    @Override
    public void hit(Bullet b, float x, float y, boolean createFrags) {
        super.hit(b, x, y, createFrags);

        if (!(b.data() instanceof CargoDeliveryData data)) return;

        float explosiveness = 0f;
        float flammability = 0f;
        float power = 0f;

        if (data.item != null && data.itemAmount > 0) {
            explosiveness += data.item.explosiveness * data.itemAmount;
            flammability += data.item.flammability * data.itemAmount;
            power += data.item.charge * Mathf.pow(data.itemAmount, 1.1f) * 25f;
        }

        if (data.liquid != null && data.liquidAmount > 0.001f) {
            explosiveness += data.liquid.explosiveness * data.liquidAmount / 2f;
            flammability += data.liquid.flammability * data.liquidAmount / 2f;
            power += data.liquid.explosiveness * Mathf.pow(data.liquidAmount, 1.1f) * 25f;
        }

        data.release();
        data.drop(x, y, b.rotation());

        Damage.dynamicExplosion(x, y, flammability / 10f, explosiveness / 10f, power, 1f, state.rules.damageExplosions);
    }

    @Override
    public void despawned(Bullet b) {
        if (!(b.data() instanceof CargoDeliveryData data)) return;

        data.release();
        data.drop(b.x, b.y, b.rotation());
    }

    @Override
    public void removed(Bullet b) {
        if (b.data() instanceof CargoDeliveryData data) {
            if (!headless && trailLength > 0 && b.trail != null && b.trail.size() > 1) {
                Color color = data.item != null ? data.item.color : data.liquid != null ? data.liquid.color : Color.white;
                Fx.trailFade.at(b.x, b.y, trailWidth, color, b.trail.copy());
            }

            b.data(null);
            Pools.free(data);
        }
    }
}
