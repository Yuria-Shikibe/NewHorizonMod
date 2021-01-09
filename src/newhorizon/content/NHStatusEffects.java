package newhorizon.content;

import arc.math.geom.Vec2;
import mindustry.ctype.ContentList;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;

public class NHStatusEffects{
    public static final StatusEffect
        staticVel = new StatusEffect("staticVel") {
        @Override
        public void update(Unit unit, float time){
            super.update(unit, time);
            unit.vel = unit.vel.scl(0.65f);
        }

        {
            this.color = Pal.gray;
            this.speedMultiplier = 0.00001F;
        }
    };
}
