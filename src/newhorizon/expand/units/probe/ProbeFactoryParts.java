package newhorizon.expand.units.probe;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.entities.part.DrawPart;
import mindustry.entities.units.WeaponMount;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public final class ProbeFactoryParts {
    public static final int COUNT = 4;
    public static final float MAX_SPEED = 6f;
    private static final float MOVEMENT_HOLD = 12f;

    private final ProbeFactoryPart[] parts = new ProbeFactoryPart[COUNT];
    private final Vec2[] positions = new Vec2[COUNT];
    private final Vec2 home = new Vec2();
    private final Vec2 target = new Vec2();
    private final DrawPart.PartParams params = new DrawPart.PartParams();
    private float movementHold;
    private boolean retracted = true;
    private boolean deploying;

    public ProbeFactoryParts(ProbeEntity factory) {
        for (DrawPart part : factory.type.parts) {
            if (part instanceof ProbeFactoryPart factoryPart) parts[factoryPart.orbitIndex()] = factoryPart;
        }
        for (int index = 0; index < COUNT; index++) {
            positions[index] = new Vec2();
            home(factory, index, positions[index]);
        }
    }

    public static void update(ProbeEntity factory) {
        if (factory.factoryParts == null) {
            if (!factory.factory) return;
            factory.factoryParts = new ProbeFactoryParts(factory);
        }
        factory.factoryParts.updatePositions(factory);
    }

    public static boolean retracted(ProbeEntity factory) {
        return factory.factoryParts == null || factory.factoryParts.retracted;
    }

    public static boolean stationary(ProbeEntity factory) {
        return factory.factoryParts == null || factory.factoryParts.deploying;
    }

    public static boolean allowMovement(ProbeEntity factory, Vec2 movement) {
        if (movement.isZero(0.001f)) return true;
        if (factory.factoryParts == null) {
            if (!factory.factory) return true;
            factory.factoryParts = new ProbeFactoryParts(factory);
        }
        factory.factoryParts.movementHold = MOVEMENT_HOLD;
        factory.factoryParts.deploying = false;
        return factory.factoryParts.retracted;
    }

    public boolean retracted() {
        return retracted;
    }

    public float partX(int index) {
        return positions[index].x;
    }

    public float partY(int index) {
        return positions[index].y;
    }

    public float extension(ProbeEntity factory, int index) {
        home(factory, index, home);
        return Mathf.clamp(positions[index].dst(home) / 18f);
    }

    private void updatePositions(ProbeEntity factory) {
        movementHold = Math.max(0f, movementHold - Time.delta);
        boolean recalling = !deploying && !retracted;
        deploying = !recalling && factory.factory && !ProbeFactoryLayout.moving(factory) && movementHold <= 0f;
        boolean wasRetracted = retracted;
        retracted = !deploying;
        float step = MAX_SPEED * Time.delta;
        for (int index = 0; index < COUNT; index++) {
            if (parts[index] == null) continue;
            home(factory, index, home);
            if (deploying) {
                ProbeFactoryRenderer.orbitPosition(factory, index, Time.time, target).sub(factory.x, factory.y);
            } else {
                target.set(home);
                if (wasRetracted) positions[index].set(home);
            }
            positions[index].approach(target, step);
            if (!deploying && !positions[index].within(home, 0.001f)) retracted = false;
        }
    }

    private void home(ProbeEntity factory, int index, Vec2 result) {
        ProbeFactoryPart part = parts[index];
        if (part == null) {
            result.setZero();
            return;
        }
        WeaponMount mount = factory.mounts.length > part.weaponIndex ? factory.mounts[part.weaponIndex] : null;
        if (mount == null) {
            params.set(0f, 0f, 0f, 0f, 0f, 0f, factory.x, factory.y, factory.rotation);
        } else {
            params.set(mount.warmup, mount.reload / mount.weapon.reload, mount.smoothReload, mount.heat,
                    mount.recoil, mount.charge, factory.x, factory.y, factory.rotation);
        }
        float progress = part.progress.getClamp(params, part.clampProgress);
        result.set(part.x + part.moveX * progress, part.y + part.moveY * progress);
        float rotation = factory.rotation - 90f;
        result.set(Angles.trnsx(rotation, result.x, result.y), Angles.trnsy(rotation, result.x, result.y));
    }
}
