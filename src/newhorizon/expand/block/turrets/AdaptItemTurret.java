package newhorizon.expand.block.turrets;

import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.meta.Stat;
import newhorizon.content.NHStatValues;

public class AdaptItemTurret extends ItemTurret {
    public AdaptItemTurret(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, NHStatValues.ammo(ammoTypes, 0, false));
    }
}
