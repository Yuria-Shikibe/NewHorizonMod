package newhorizon.expand.block.distribution.delivery;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.CatmullRomSpline;
import arc.math.geom.Vec2;
import arc.struct.IntSeq;
import arc.util.Nullable;
import arc.util.pooling.Pool.Poolable;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.Liquid;

import static mindustry.Vars.world;

public class CargoDeliveryData implements Poolable {
    public Building from, to;
    public final IntSeq path = new IntSeq();

    public @Nullable Item item;
    public int itemAmount;
    public @Nullable Liquid liquid;
    public float liquidAmount;

    public float speed;
    public float progress;
    public float length = 1f;
    public float travelTime;
    public float accelTime = 30f;
    public int pathIndex;
    public boolean reserved;

    public @Nullable Vec2[] controlPoints;
    public final CatmullRomSpline<Vec2> spline = new CatmullRomSpline<>();

    public void buildSpline() {
        if (from == null || to == null) {
            controlPoints = null;
            return;
        }

        int relayCount = path.size;
        Vec2[] points = new Vec2[relayCount + 4];
        points[0] = new Vec2(from.x, from.y);
        points[1] = new Vec2(from.x, from.y);

        for (int i = 0; i < relayCount; i++) {
            Building relay = world.build(path.get(i));
            if (relay == null) relay = from;
            points[i + 2] = new Vec2(relay.x, relay.y);
        }

        points[relayCount + 2] = new Vec2(to.x, to.y);
        points[relayCount + 3] = new Vec2(to.x, to.y);

        controlPoints = points;
        spline.set(points, false);
        length = Math.max(1f, spline.approxLength(Math.max(16, (relayCount + 1) * 8)));
        progress = 0f;
        travelTime = 0f;
        pathIndex = 0;
    }

    public void release() {
        if (reserved && to instanceof CargoDeliveryDeposit.CargoDeliveryDepositBuild deposit) {
            deposit.release(this);
        }
        reserved = false;
    }

    public void drop(float x, float y, float rotation) {
        if (item != null && itemAmount > 0) {
            Fx.dropItem.at(x, y, rotation + Mathf.range(100f), Color.white, item);
            itemAmount = 0;
        }

        if (liquid != null && liquidAmount > 0.001f) {
            Fx.hitLiquid.at(x, y, rotation, liquid.color);
            liquidAmount = 0f;
        }
    }

    @Override
    public void reset() {
        from = null;
        to = null;
        path.clear();
        item = null;
        itemAmount = 0;
        liquid = null;
        liquidAmount = 0f;
        speed = 0f;
        progress = 0f;
        length = 1f;
        travelTime = 0f;
        accelTime = 30f;
        pathIndex = 0;
        reserved = false;
        controlPoints = null;
        spline.controlPoints = null;
    }
}

