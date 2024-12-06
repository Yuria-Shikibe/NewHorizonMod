package newhorizon.expand.units.unitType;

import mindustry.graphics.Layer;

public class FloodUnitType extends NHUnitType{
    public FloodUnitType(String name) {
        super(name);

        lockLegBase = true;
        legContinuousMove = true;
        legStraightness = 0.6f;
        baseLegStraightness = 0.5f;

        legCount = 8;
        legLength = 30f;
        legForwardScl = 2.1f;
        legMoveSpace = 1.05f;
        rippleScale = 1.2f;
        stepShake = 0.5f;
        legGroupSize = 2;
        legExtension = -6f;
        legBaseOffset = 19f;
        legStraightLength = 0.9f;
        legMaxLength = 1.2f;

        hovering = true;
        shadowElevation = 0.4f;
        groundLayer = Layer.legUnit;

        alwaysShootWhenMoving = true;
    }
}
