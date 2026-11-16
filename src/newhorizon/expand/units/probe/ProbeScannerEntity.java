package newhorizon.expand.units.probe;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.expand.units.AdaptedTimedKillUnit;
import newhorizon.expand.units.unitEntity.ProbeEntity;
import newhorizon.util.func.NHMath;
import newhorizon.util.graphic.DrawFunc;

public class ProbeScannerEntity extends AdaptedTimedKillUnit {
    private static final float SCAN_WARMUP_SPEED = 0.0075f;
    private static final float SCAN_COOL_SPEED = 0.015f;
    private static final float SCAN_SHIFT_SPEED = 0.0875f;
    private static final float SCAN_DURATION = 30f * Time.toSeconds;
    private static final float SCAN_PROGRESS_SPEED = 1f / SCAN_DURATION;
    private static final float SCAN_ROTATE_SCALE = 180f;
    private static final float SCAN_SOURCE_X = 0.5f;
    private static final float SCAN_SOURCE_Y = 3f;
    private static final int SCAN_MATERIAL_WIDTH = 10;
    private static final float SCAN_MATERIAL_SPACING = 6f;

    private final Vec2 scanPos = new Vec2();
    private final Vec2 scanCenter = new Vec2();
    private final Vec2 tangentOne = new Vec2();
    private final Vec2 tangentTwo = new Vec2();
    private final Vec2 scanSource = new Vec2();
    private final Vec2 tempVec = new Vec2();
    private final Rand scanRandom = new Rand();
    private final Color scanColor = Pal.techBlue.cpy();
    private float scanLastSize;

    public int ownerId = -1;
    public int targetTile = -1;
    public float scanWarmup;
    public float scanProgress;
    public boolean triggered;
    public boolean leaving;
    public boolean raidLaunched;
    public float exitProgress;
    public float exitAngle;

    @Override
    public int classId() {
        return EntityRegister.getID(ProbeScannerEntity.class);
    }

    public ProbeEntity owner() {
        Unit unit = Groups.unit.getByID(ownerId);
        return unit instanceof ProbeEntity probe && probe.isValid() && probe.factory && probe.team == team ? probe : null;
    }

    public Tile targetTile() {
        return ProbeColonyManager.targetTile(targetTile);
    }

    public Building targetBuilding() {
        Tile tile = targetTile();
        return tile == null ? null : tile.build;
    }

    @Override
    public void add() {
        scanPos.set(x, y);
        super.add();
    }

    @Override
    public void update() {
        ProbeEntity owner = owner();
        if (!Vars.net.client() && owner == null) {
            kill();
            return;
        }

        super.update();
        if (Vars.net.client()) return;
        if (leaving) {
            updateDeparture();
            return;
        }
        if (triggered) return;

        Building target = targetBuilding();
        if (target == null || !target.isValid() || target.team == team) {
            scanProgress = Mathf.approachDelta(scanProgress, 0f, SCAN_PROGRESS_SPEED * 5f);
            scanWarmup = Mathf.lerpDelta(scanWarmup, 0f, SCAN_COOL_SPEED);
            scanPos.lerp(this, SCAN_SHIFT_SPEED * Time.delta);
            return;
        }

        if (within(target, 28f)) {
            scanWarmup = Mathf.lerpDelta(scanWarmup, 1f, SCAN_WARMUP_SPEED);
            scanLastSize = Mathf.lerpDelta(scanLastSize,
                    target.block.size * 4f * Mathf.sqrt2,
                    scanWarmup * SCAN_WARMUP_SPEED);
            scanPos.lerp(target, SCAN_SHIFT_SPEED * Time.delta);
            if (target.within(scanPos, scanLastSize / 1.5f)) {
                scanProgress = Mathf.approachDelta(scanProgress, 1.005f, SCAN_PROGRESS_SPEED);
            }
            if (scanProgress >= 1f) {
                triggered = true;
                raidLaunched = true;
                ProbeAirRaid.launch(this, target);
                leaving = true;
                exitAngle = angleTo(target) + 180f;
                Fx.teleportOut.at(x, y, rotation, team.color);
            }
        } else {
            scanProgress = Mathf.approachDelta(scanProgress, 0f, SCAN_PROGRESS_SPEED * 5f);
            scanWarmup = Mathf.lerpDelta(scanWarmup, 0f, SCAN_COOL_SPEED);
            scanPos.lerp(this, SCAN_SHIFT_SPEED * Time.delta);
        }
    }

    private void updateDeparture() {
        exitProgress = Mathf.clamp(exitProgress + Time.delta / 36f);
        float speed = type.speed * (1f + exitProgress * 0.8f);
        vel.trns(exitAngle, speed);
        elevation = Mathf.lerp(elevation, 1f, 0.08f);
        if (exitProgress < 1f) return;

        Fx.unitDespawn.at(x, y, rotation, team.color);
        kill();
    }

    @Override
    public void draw() {
        float z = Draw.z();
        if (leaving) Draw.alpha(1f - exitProgress);
        super.draw();

        if (!leaving) drawScan();

        Draw.alpha(1f);
        Draw.z(z);
    }

    private void drawScan() {
        Building target = targetBuilding();
        if (target == null || !target.isValid() || scanWarmup < 0.425f || scanLastSize <= 0f) return;

        float savedZ = Draw.z();
        Draw.z(Layer.effect);
        float warmup = Mathf.curve(scanWarmup, 0.425f, 0.95f);
        float scanRadius = scanLastSize;
        float size = scanRadius * warmup;
        float offset = (warmup + Mathf.absin(8f, warmup * 0.65f)) * scanRadius / 2f;
        Vec2 center = scanCenter.set(scanPos).add(Mathf.cosDeg(Time.time * 1.35f) * offset,
                Mathf.sinDeg(Time.time * 1.35f) * offset);

        float distance = Mathf.dst(x, y, center.x, center.y);
        float angle = NHMath.acosRad(Mathf.clamp(size / Math.max(distance, 0.001f), -1f, 1f)) * Mathf.radiansToDegrees;
        float angleTo = angleTo(center.x, center.y);
        float residual = 90f - angle;
        float rotation = scanProgress * SCAN_ROTATE_SCALE + Time.time / 4f;
        Color color = scanColor.set(team.color).lerp(Color.white, Mathf.absin(5f, 0.325f));

        Lines.stroke(warmup * 1.25f);
        Draw.color(color);
        tangentOne.trns(angleTo + angle, size);
        tangentTwo.trns(angleTo - angle, size);
        Vec2 source = scanSource.trns(this.rotation, SCAN_SOURCE_Y, SCAN_SOURCE_X).add(x, y);
        Lines.circle(center.x, center.y, size);
        DrawFunc.circlePercent(center.x, center.y, size + 5f, scanProgress,
                rotation + DrawFunc.rotator_360() + Time.time);
        Drawf.light(center.x, center.y, size * 1.5f, color, warmup * 0.75f);

        Lines.line(source.x, source.y, tangentOne.x + center.x, tangentOne.y + center.y, true);
        DrawFunc.lineAngleLerp(tangentOne.x + center.x, tangentOne.y + center.y,
                -residual + angleTo, size * 6f, true, color, Color.clear);
        Lines.line(source.x, source.y, tangentTwo.x + center.x, tangentTwo.y + center.y, true);
        DrawFunc.lineAngleLerp(tangentTwo.x + center.x, tangentTwo.y + center.y,
                residual + angleTo, size * 6f, true, color, Color.clear);

        float maxDistance = Mathf.sqrt2 * SCAN_MATERIAL_SPACING * (SCAN_MATERIAL_WIDTH / 2f) * 0.55f;
        Lines.stroke(warmup * 0.75f);
        for (int cellX = -SCAN_MATERIAL_WIDTH / 2; cellX <= SCAN_MATERIAL_WIDTH / 2; cellX++) {
            for (int cellY = -SCAN_MATERIAL_WIDTH / 2; cellY <= SCAN_MATERIAL_WIDTH / 2; cellY++) {
                scanRandom.setSeed((long) cellX + 8L << cellY);
                float materialDistance = Mathf.dst(cellX, cellY);
                float scale = Mathf.maxZero(1f - materialDistance / maxDistance);
                Draw.mixcol(Color.white, Mathf.absin(scanRandom.random(2f, 8f), scanRandom.random(0.025f, 0.35f)));
                Draw.alpha(scale * scanRandom.random(scale * 0.25f, scale * 1.125f)
                        * (0.5f + Mathf.absin(scanRandom.random(2f, 5f), scanRandom.random(0.125f, 0.65f))));
                tempVec.set(cellX * SCAN_MATERIAL_SPACING, cellY * SCAN_MATERIAL_SPACING).rotate(rotation).add(center);
                Lines.lineAngleCenter(tempVec.x, tempVec.y, rotation, SCAN_MATERIAL_SPACING * 0.8f);
                Lines.lineAngleCenter(tempVec.x, tempVec.y, rotation - 90f, SCAN_MATERIAL_SPACING * 0.8f);
            }
        }

        Draw.mixcol();
        Draw.z(Layer.buildBeam);
        Fill.light(center.x, center.y, (int) (Lines.circleVertices(size) * 0.75f), size * 1.25f,
                color.a((0.85f + Mathf.absin(4f, 0.15f)) * warmup), Color.clear);
        Draw.reset();
        Draw.z(savedZ);
    }

    @Override
    public void remove() {
        boolean wasAdded = added;
        if (wasAdded) ProbeColonyManager.onScannerRemoved(ownerId);
        super.remove();
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        ownerId = read.i();
        targetTile = read.i();
        scanWarmup = read.f();
        scanProgress = read.f();
        triggered = read.bool();
        leaving = read.bool();
        raidLaunched = read.bool();
        exitProgress = read.f();
        exitAngle = read.f();
        Building target = targetBuilding();
        if (target != null) scanPos.set(target.x, target.y);
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        write.i(ownerId);
        write.i(targetTile);
        write.f(scanWarmup);
        write.f(scanProgress);
        write.bool(triggered);
        write.bool(leaving);
        write.bool(raidLaunched);
        write.f(exitProgress);
        write.f(exitAngle);
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);
        ownerId = read.i();
        targetTile = read.i();
        scanWarmup = read.f();
        scanProgress = read.f();
        triggered = read.bool();
        leaving = read.bool();
        raidLaunched = read.bool();
        exitProgress = read.f();
        exitAngle = read.f();
        Building target = targetBuilding();
        if (target != null) scanPos.set(target.x, target.y);
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);
        write.i(ownerId);
        write.i(targetTile);
        write.f(scanWarmup);
        write.f(scanProgress);
        write.bool(triggered);
        write.bool(leaving);
        write.bool(raidLaunched);
        write.f(exitProgress);
        write.f(exitAngle);
    }
}
