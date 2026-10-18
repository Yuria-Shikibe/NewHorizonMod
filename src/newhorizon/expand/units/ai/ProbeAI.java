package newhorizon.expand.units.ai;

import mindustry.ai.UnitCommand;
import mindustry.ai.types.CommandAI;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.units.probe.ProbeColonyManager;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public class ProbeAI extends CommandAI {
    protected ProbeEntity probe;

    @Override
    public void unit(Unit unit) {
        super.unit(unit);
        probe = unit instanceof ProbeEntity entity ? entity : null;
    }

    @Override
    public Teamc findTarget(float x, float y, float range, boolean air, boolean ground) {
        return null;
    }

    @Override
    public boolean hasCommand() {
        return probe != null && (probe.factory || probe.factoryCandidate || probe.factoryId >= 0) || super.hasCommand();
    }

    @Override
    public void updateUnit() {
        if (probe != null && (probe.factory || probe.factoryCandidate)
                && currentCommand() != NHUnitTypes.probeProductionCommand) {
            command = UnitCommand.moveCommand;
        }
        super.updateUnit();
    }

    @Override
    public void defaultBehavior() {
        if (probe == null) {
            super.defaultBehavior();
            return;
        }

        if (probe.factory) {
            unit.vel.setZero();
            return;
        }

        if (probe.factoryCandidate) {
            Tile tile = probe.miningTarget();
            if (tile != null) {
                moveTo(tile, 20f);
                unit.lookAt(tile);
            } else {
                unit.vel.setZero();
            }
            return;
        }

        ProbeEntity factory = probe.factoryOwner();
        if (factory == null && (targetPos != null || attackTarget != null || commandQueue.any())) {
            super.defaultBehavior();
            return;
        }

        if (ProbeColonyManager.shouldDeliver(probe, probe.miningTargetTile)) {
            if (factory != null) moveTo(factory, 32f);
            else unit.vel.setZero();
            return;
        }

        Tile tile = probe.miningTarget();
        if (tile != null) {
            moveTo(tile, 20f);
            unit.lookAt(tile);
            return;
        }

        if (factory != null) {
            moveTo(factory, 64f);
            return;
        }

        unit.vel.setZero();
    }
}
