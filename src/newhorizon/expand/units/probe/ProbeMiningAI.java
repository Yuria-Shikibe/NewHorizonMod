package newhorizon.expand.units.probe;

import mindustry.ai.types.FlyingAI;
import mindustry.gen.Teamc;
import mindustry.world.Tile;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public class ProbeMiningAI extends FlyingAI {
    protected ProbeMiningEntity miner;

    @Override
    public void unit(mindustry.gen.Unit unit) {
        super.unit(unit);
        miner = unit instanceof ProbeMiningEntity entity ? entity : null;
    }

    @Override
    public Teamc findTarget(float x, float y, float range, boolean air, boolean ground) {
        return null;
    }

    @Override
    public void updateMovement() {
        if (miner == null) return;

        Tile tile = miner.targetTile();
        ProbeEntity owner = miner.owner();
        if (ProbeColonyManager.shouldDeliver(miner, miner.targetTile)) {
            if (owner != null) moveTo(owner, 40f);
            return;
        }
        if (tile != null) {
            moveTo(tile, 12f);
            unit.lookAt(tile);
        } else if (owner != null) {
            moveTo(owner, 48f);
        }
    }
}
