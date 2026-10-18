package newhorizon.expand.units.probe;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.expand.units.AdaptedTimedKillUnit;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public class ProbeMiningEntity extends AdaptedTimedKillUnit {
    public int ownerId = -1;
    public int targetTile = -1;
    public float workProgress;
    public transient float targetRetry;

    @Override
    public int classId() {
        return EntityRegister.getID(ProbeMiningEntity.class);
    }

    public ProbeEntity owner() {
        Unit unit = Groups.unit.getByID(ownerId);
        return unit instanceof ProbeEntity probe && probe.isValid() && probe.factory && probe.team == team ? probe : null;
    }

    public Tile targetTile() {
        return ProbeColonyManager.targetTile(targetTile);
    }

    @Override
    public void update() {
        if (!Vars.net.client() && owner() == null) {
            kill();
            return;
        }
        super.update();
    }

    @Override
    public void draw() {
        super.draw();
        ProbeColonyManager.drawMining(this, targetTile, workProgress);
    }

    @Override
    public void remove() {
        boolean wasAdded = added;
        if (wasAdded) ProbeColonyManager.onMiningRemoved(this);
        super.remove();
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        ownerId = read.i();
        targetTile = read.i();
        workProgress = read.f();
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        write.i(ownerId);
        write.i(targetTile);
        write.f(workProgress);
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);
        ownerId = read.i();
        targetTile = read.i();
        workProgress = read.f();
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);
        write.i(ownerId);
        write.i(targetTile);
        write.f(workProgress);
    }
}
