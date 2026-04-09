package newhorizon.expand.logic.cutscene.types;

import mindustry.entities.bullet.BulletType;
import newhorizon.content.bullets.RaidBullets;

public enum RaidPreset {
    PRESET_RAID_0(RaidBullets.raidBullet_1),
    ;

    public static final RaidPreset[] all = values();

    public final BulletType bulletType;

    RaidPreset(BulletType bulletType) {
        this.bulletType = bulletType;
    }

}
