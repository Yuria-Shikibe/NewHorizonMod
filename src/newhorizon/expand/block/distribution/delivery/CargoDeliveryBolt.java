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
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;

import static mindustry.Vars.world;

public class CargoDeliveryBolt extends BulletType {

    public CargoDeliveryBolt() {
        super(1f, 0f);
        collides = false;
        collidesTiles = false;
        lifetime = 1f;
        drawSize = 32f;
        hitEffect = Fx.none;
        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        smokeEffect = Fx.none;
        trailLength = 8;
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
    }

    @Override
    public void draw(Bullet b) {
        if (!(b.data() instanceof CargoDeliveryData data)) return;

        Color color = data.item != null ? data.item.color : data.liquid != null ? data.liquid.color : Color.white;
        Draw.color(color);
        Fill.circle(b.x, b.y, 3.5f);
        Draw.alpha(0.65f);
        Lines.stroke(1.2f);
        Lines.circle(b.x, b.y, 6.5f);
        Draw.reset();
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
            b.data(null);
            Pools.free(data);
        }
    }
}
