package newhorizon.expand.units.probe;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.part.RegionPart;
import mindustry.graphics.Layer;
import newhorizon.NewHorizon;
import newhorizon.expand.units.unitEntity.ProbeEntity;
import newhorizon.expand.units.unitType.ProbeUnitType;

public class ProbeFactoryPart extends RegionPart {
    private final ProbeUnitType owner;
    private final int orbitIndex;
    private final PartParams orbitParams = new PartParams();
    private TextureRegion orbitRegion;

    public ProbeFactoryPart(ProbeUnitType owner, String suffix, int orbitIndex) {
        super(suffix);
        this.owner = owner;
        this.orbitIndex = orbitIndex;
    }

    @Override
    public void load(String name) {
        super.load(name);
        orbitRegion = Core.atlas.find(NewHorizon.name("ancient-probe-factory-part"));
    }

    public int orbitIndex() {
        return orbitIndex;
    }

    @Override
    public void draw(PartParams params) {
        ProbeEntity factory = owner.drawingProbe();
        if (factory == null || ProbeFactoryParts.retracted(factory)) {
            super.draw(params);
            return;
        }

        float deployment = factory.factoryParts.extension(factory, orbitIndex);
        float movement = progress.getClamp(params, clampProgress);
        float side = (params.sideOverride == 1 ? -1f : 1f) * params.sideMultiplier;
        float localX = (x + moveX * movement) * side;
        float localY = y + moveY * movement;
        float partX = factory.x + factory.factoryParts.partX(orbitIndex);
        float partY = factory.y + factory.factoryParts.partY(orbitIndex);
        float facing = Angles.angle(partX, partY, factory.x + factory.factoryRadius, factory.y + factory.factoryRadius)
                - (rotation + moveRot * movement) * side;
        float partRotation = Mathf.slerp(params.rotation,
                facing + Mathf.sin(Time.time / 107f + orbitIndex * 1.31f) * 14f, deployment);
        orbitParams.set(params.warmup, params.reload, params.smoothReload, params.heat, params.recoil, params.charge,
                partX - Angles.trnsx(partRotation - 90f, localX, localY),
                partY - Angles.trnsy(partRotation - 90f, localX, localY), partRotation);
        orbitParams.sideMultiplier = params.sideMultiplier;
        orbitParams.sideOverride = params.sideOverride;
        orbitParams.life = params.life;

        boolean useOrbitRegion = orbitRegion != null && orbitRegion.found() && regions.length > 0;
        ProbeFactoryRenderer.drawPartLink(factory, partX, partY, deployment);
        float previousLayer = Draw.z();
        Draw.z(Layer.buildBeam + 1.3f - layerOffset);
        TextureRegion previousRegion = useOrbitRegion ? regions[0] : null;
        boolean previousOutline = outline;
        if (useOrbitRegion) {
            regions[0] = orbitRegion;
            outline = false;
        }
        try {
            super.draw(orbitParams);
        } finally {
            if (useOrbitRegion) {
                regions[0] = previousRegion;
                outline = previousOutline;
            }
            Draw.z(previousLayer);
        }
    }
}
