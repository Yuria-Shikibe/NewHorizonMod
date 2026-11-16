package newhorizon.expand.units.probe;

import mindustry.ai.types.FlyingAI;
import mindustry.gen.Teamc;
import mindustry.world.Tile;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public class ProbeScannerAI extends FlyingAI {
    protected ProbeScannerEntity scanner;

    @Override
    public void unit(mindustry.gen.Unit unit) {
        super.unit(unit);
        scanner = unit instanceof ProbeScannerEntity entity ? entity : null;
    }

    @Override
    public Teamc findTarget(float x, float y, float range, boolean air, boolean ground) {
        return null;
    }

    @Override
    public void updateMovement() {
        if (scanner == null) return;
        if (scanner.leaving) return;

        Tile tile = scanner.targetTile();
        ProbeEntity owner = scanner.owner();
        if (tile != null) {
            moveTo(tile, 16f);
            unit.lookAt(tile);
        } else if (owner != null) {
            moveTo(owner, 48f);
        }
    }
}
