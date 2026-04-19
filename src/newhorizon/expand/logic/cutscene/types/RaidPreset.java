package newhorizon.expand.logic.cutscene.types;

import arc.audio.Sound;
import mindustry.entities.bullet.BulletType;
import newhorizon.NewHorizon;
import newhorizon.content.NHSounds;
import newhorizon.content.bullets.RaidBullets;

public enum RaidPreset {
    PRESET_RAID_1(RaidBullets.defaultRaidBullet1, NHSounds.alert2, "event-default-raid-t1", 15, 5, 1, 40),
    PRESET_RAID_2(RaidBullets.defaultRaidBullet2, NHSounds.alert2, "event-default-raid-t2", 15, 5, 1, 40),
    PRESET_RAID_3(RaidBullets.defaultRaidBullet3, NHSounds.alert2, "event-default-raid-t1", 15, 5, 1, 40),

    EXPLOSIVE_RAID_1(RaidBullets.explosiveRaidBullet1, NHSounds.alert2, "event-explosive-raid-t1", 15, 5, 1, 40),

    RAIL_RAID_1(RaidBullets.railRaidBullet1, NHSounds.alert2, "event-rail-raid-t1", 15, 5, 1, 40),
    ;

    public static final RaidPreset[] all = values();

    public final BulletType bulletType;
    public final Sound raidAlarmSound;
    public final String warningIcon;

    public final float alertTime;
    public final float raidTime;
    public final float raidScale;
    public final float inaccuracy;

    RaidPreset(
            BulletType bulletType, Sound raidAlarmSound, String warningIcon,
            float alertTime, float raidTime, float raidScale, float inaccuracy
    ) {
        this.bulletType = bulletType;
        this.raidAlarmSound = raidAlarmSound;
        this.warningIcon = NewHorizon.name(warningIcon);

        this.alertTime = alertTime;
        this.raidTime = raidTime;
        this.inaccuracy = inaccuracy;
        this.raidScale = raidScale;
    }

}
