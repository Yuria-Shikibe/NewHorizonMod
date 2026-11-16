package newhorizon.expand.units.ai;

import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.ai.UnitCommand;
import mindustry.ai.types.CommandAI;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.units.probe.ProbeColonyManager;
import newhorizon.expand.units.probe.ProbeFactoryLayout;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public class ProbeAI extends CommandAI {
    protected ProbeEntity probe;
    private final Vec2 orbitPosition = new Vec2();
    private final Vec2 orbitVelocity = new Vec2();
    private final Vec2 orbitMovement = new Vec2();

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
            Tile destination = probe.miningTarget();
            if (destination != null) {
                moveTo(Tmp.v1.set(destination.worldx(), destination.worldy()), ProbeColonyManager.FACTORY_ARRIVAL_RANGE / 2f, 20f);
                unit.lookAt(destination.worldx(), destination.worldy());
                return;
            }
            if (!ProbeFactoryLayout.moving(probe) && (targetPos != null || attackTarget != null || commandQueue.any())) {
                super.defaultBehavior();
                return;
            }
            unit.vel.setZero();
            if (!ProbeFactoryLayout.moving(probe)) {
                unit.lookAt(probe.x + probe.factoryRadius, probe.y + probe.factoryRadius);
            }
            return;
        }

        if (probe.factoryCandidate) {
            Tile tile = probe.miningTarget();
            if (tile != null) {
                moveTo(Tmp.v1.set(tile.worldx(), tile.worldy()), ProbeColonyManager.FACTORY_ARRIVAL_RANGE / 2f, 20f);
                unit.lookAt(tile.worldx(), tile.worldy());
            } else {
                unit.vel.setZero();
            }
            return;
        }

        ProbeEntity factory = probe.factoryOwner();
        if (factory != null && ProbeColonyManager.isProductionAssistant(probe)) {
            ProbeFactoryLayout.assistantOrbitPosition(probe, factory, Time.time, orbitPosition, orbitVelocity);
            orbitMovement.set(orbitPosition).sub(unit).scl(0.12f).add(orbitVelocity).limit(unit.speed());
            unit.moveAt(orbitMovement);
            unit.lookAt(factory.x + factory.factoryRadius, factory.y + factory.factoryRadius);
            return;
        }
        if (factory == null && (targetPos != null || attackTarget != null || commandQueue.any())) {
            super.defaultBehavior();
            return;
        }

        if (ProbeColonyManager.shouldDeliver(probe, probe.miningTargetTile)) {
            if (factory != null && !unit.within(factory, ProbeColonyManager.deliveryRange(unit, factory))) {
                moveTo(factory, ProbeColonyManager.deliveryRange(unit, factory) / 2f, 20f);
            } else {
                unit.vel.setZero();
            }
            return;
        }

        Tile tile = probe.miningTarget();
        if (ProbeColonyManager.canHarvest(unit, tile)) {
            ProbeColonyManager.approachMine(this, unit, tile);
            return;
        }

        if (factory != null) {
            moveTo(factory, ProbeColonyManager.deliveryRange(unit, factory) * 2f, 20f, false, null);
            return;
        }

        unit.vel.setZero();
    }
}
