package newhorizon.expand.units.probe;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
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
import mindustry.world.Tile;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.expand.units.AdaptedTimedKillUnit;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public class ProbeScannerEntity extends AdaptedTimedKillUnit {
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
            scanWarmup = Mathf.approachDelta(scanWarmup, 0f, 0.03f);
            scanProgress = 0f;
            return;
        }

        if (within(target, 28f)) {
            scanWarmup = Mathf.approachDelta(scanWarmup, 1f, 0.02f);
            scanProgress = Mathf.approachDelta(scanProgress, 1f, 0.012f);
            if (scanProgress >= 1f) {
                triggered = true;
                leaving = true;
                exitAngle = angleTo(target) + 180f;
                Fx.teleportOut.at(x, y, rotation, team.color);
            }
        } else {
            scanWarmup = Mathf.approachDelta(scanWarmup, 0f, 0.02f);
            scanProgress = 0f;
        }
    }

    private void updateDeparture() {
        exitProgress = Mathf.clamp(exitProgress + Time.delta / 36f);
        float speed = type.speed * (1f + exitProgress * 0.8f);
        vel.trns(exitAngle, speed);
        elevation = Mathf.lerp(elevation, 1f, 0.08f);
        if (exitProgress < 1f) return;

        if (!raidLaunched) {
            raidLaunched = true;
            ProbeAirRaid.launch(this, targetBuilding());
        }
        Fx.unitDespawn.at(x, y, rotation, team.color);
        kill();
    }

    @Override
    public void draw() {
        float z = Draw.z();
        if (leaving) Draw.alpha(1f - exitProgress);
        super.draw();

        if (leaving) {
            Draw.alpha(1f);
            Draw.z(Layer.effect);
            Draw.color(team.color);
            Draw.alpha(1f - exitProgress);
            Lines.stroke(1.5f * (1f - exitProgress));
            Lines.circle(x, y, hitSize * (1f + exitProgress));
            Draw.color();
            Draw.alpha(1f);
            Draw.z(z);
            return;
        }

        Building target = targetBuilding();
        if (target == null || !target.isValid()) return;

        Draw.z(Layer.effect);
        Draw.color(team.color);
        Draw.alpha(0.25f + scanWarmup * 0.5f);
        Lines.stroke(1.2f + scanWarmup * 1.5f);
        Lines.circle(target.x, target.y, 12f + target.block.size * 4f * scanWarmup);
        Lines.line(x, y, target.x, target.y);
        Drawf.light(target.x, target.y, 32f + target.block.size * 5f, team.color, scanWarmup * 0.6f);
        Draw.color();
        Draw.alpha(1f);
        Draw.z(z);
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
