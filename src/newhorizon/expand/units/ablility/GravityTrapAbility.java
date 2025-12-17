package newhorizon.expand.units.ablility;

import arc.Core;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import newhorizon.expand.entities.GravityTrapField;

public class GravityTrapAbility extends Ability {
    protected GravityTrapField field;
    public float range;

    public GravityTrapAbility(float range) {
        this.range = range;
    }

    @Override
    public void created(Unit unit) {
        super.created(unit);
        field = new GravityTrapField(unit.team, range);
    }

    @Override
    public void update(Unit unit) {
        field.update(unit);
    }

    @Override
    public void death(Unit unit) {
        field.remove();
    }

    @Override
    public String localized() {
        return Core.bundle.format("ability.gravity-trap", range / Vars.tilesize);
    }
}
