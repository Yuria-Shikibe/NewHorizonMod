package newhorizon.expand.block.turrets;

import mindustry.entities.pattern.ShootPattern;

public interface AdaptTurret {
    ShootPattern pattern();

    float reloadModifier();

    float kineticModifier();

    float energyModifier();

    float rangeModifier();
}
