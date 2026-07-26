package newhorizon.expand.block.drawer;

import arc.graphics.Color;
import arc.math.Angles;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.entities.effect.RadialEffect;

public class RadialEffectRotated extends RadialEffect {
    float createX, createY;

    public RadialEffectRotated(Effect effect, int amount, float spacing, float lengthOffset, float x, float y) {
        super(effect, amount, spacing, lengthOffset, 0f);
        this.createX = x;
        this.createY = y;
    }

    public RadialEffectRotated(Effect effect, int amount, float x, float y) {
        super(effect, amount, 0, 0, 0f);
        this.createX = x;
        this.createY = y;
    }

    @Override
    public void create(float x, float y, float rotation, Color color, Object data) {
        if (!shouldCreate()) return;

        rotation += rotationOffset;
        Tmp.v1.set(createX, createY).rotate(rotation);

        for (int i = 0; i < amount; i++) {
            effect.create(
                    x + Tmp.v1.x + Angles.trnsx(rotation, lengthOffset),
                    y + Tmp.v1.y + Angles.trnsy(rotation, lengthOffset),
                    rotation + effectRotationOffset,
                    color,
                    data
            );
            rotation += rotationSpacing;
        }
    }
}


