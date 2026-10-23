package newhorizon.expand.units.unitType;

import mindustry.type.UnitType;
import newhorizon.expand.units.AdaptedMissileUnitType;

public class ProbePhantomUnitType extends AdaptedMissileUnitType {
    public final UnitType source;

    public ProbePhantomUnitType(String name, UnitType source) {
        super(name);
        this.source = source;
    }

    @Override
    public void load() {
        super.load();

        if (source == null) return;

        baseRegion = source.baseRegion;
        legRegion = source.legRegion;
        region = source.region;
        previewRegion = source.previewRegion;
        shadowRegion = source.shadowRegion;
        cellRegion = source.cellRegion;
        outlineRegion = source.outlineRegion;
        itemCircleRegion = source.itemCircleRegion;
        softShadowRegion = source.softShadowRegion;
        jointRegion = source.jointRegion;
        footRegion = source.footRegion;
        legBaseRegion = source.legBaseRegion;
        baseJointRegion = source.baseJointRegion;
        treadRegion = source.treadRegion;
        mineLaserRegion = source.mineLaserRegion;
        mineLaserEndRegion = source.mineLaserEndRegion;
    }
}
