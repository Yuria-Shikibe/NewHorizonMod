package newhorizon.expand.units.unitType;

import mindustry.gen.Unit;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public class ProbeUnitType extends AncientUnitType {
    private ProbeEntity drawingProbe;

    public ProbeUnitType(String name) {
        super(name);
    }

    public ProbeEntity drawingProbe() {
        return drawingProbe;
    }

    @Override
    public void draw(Unit unit) {
        ProbeEntity previous = drawingProbe;
        drawingProbe = unit instanceof ProbeEntity probe ? probe : null;
        try {
            super.draw(unit);
        } finally {
            drawingProbe = previous;
        }
    }
}
