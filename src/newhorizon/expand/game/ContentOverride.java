package newhorizon.expand.game;

import arc.util.Log;
import mindustry.ctype.UnlockableContent;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;
import newhorizon.content.NHStatValues;
import newhorizon.expand.ability.passive.PassiveShield;

import static mindustry.Vars.content;

public class ContentOverride {
    public static void override(){
        overrideUnitTypeAbility();
        //for (Block block: content.blocks()){
        //    var map = block.stats.toMap();
        //    Log.info(block.name + ": " + map.size);
        //    if (map.get(StatCat.function) != null && map.get(StatCat.function).get(Stat.ammo) != null){
        //        if (block instanceof ItemTurret itemTurret){
        //            Log.info(block.name + " override");
        //            block.stats.remove(Stat.ammo);
        //            block.stats.add(Stat.ammo, NHStatValues.ammo(itemTurret.ammoTypes, 0, false));
        //        }
        //    }
        //}
    }

    public static void overrideUnitTypeAbility(){
        for (UnitType type: content.units()){
            if (type.abilities.contains(ability -> ability instanceof PassiveShield)) continue;
            type.abilities.add(new PassiveShield(type.health));
        }
    }
}
