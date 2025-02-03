package newhorizon.content;

import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;

public class NHStats {
    public static final Stat

        damageReduction = new Stat("damage-reduction", StatCat.general),

        powerConsModifier = new Stat("power-cons-modifier", StatCat.function),
        minerBoosModifier = new Stat("miner-boost-modifier", StatCat.function),
        itemConvertList = new Stat("item-convert-list", StatCat.function),
        maxBoostPercent = new Stat("max-boost-percent", StatCat.function),

        increaseWhenShooting = new Stat("increase-when-shooting", StatCat.function),
        decreaseNotShooting = new Stat("decrease-not-shooting", StatCat.function);
}
