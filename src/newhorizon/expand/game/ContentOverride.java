package newhorizon.expand.game;

import mindustry.type.UnitType;
import newhorizon.expand.ability.passive.PassiveShield;

import static mindustry.Vars.content;

public class ContentOverride {
    public static void override(){
        overrideUnitTypeAbility();
    }

    public static void overrideUnitTypeAbility(){
        for (UnitType type: content.units()){
            if (type.abilities.contains(ability -> ability instanceof PassiveShield)) continue;
            type.abilities.add(new PassiveShield(type.health));
        }
    }
}
