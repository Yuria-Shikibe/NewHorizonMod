package newhorizon.expand.units.status;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;

public class BoostStatusEffect extends StatusEffect {
    public BoostStatusEffect(String name) {
        super(name);

        speedMultiplier = 20f;
        dragMultiplier = 0f;
    }

    @Override
    public void update(Unit unit, float time) {
        super.update(unit, time);
        unit.mounts();
    }

    public Effect dashEffect(Unit unit) {
        return new Effect(10, e -> {
            Draw.mixcol(unit.team.color, Color.white, e.fout());
            Draw.alpha(0.1f * e.fout(Interp.pow2Out) * Time.delta);
            Draw.color(unit.team.color);
            Draw.rect(unit.type.fullIcon, e.x, e.y, e.rotation - 90);
        });
    }
}
