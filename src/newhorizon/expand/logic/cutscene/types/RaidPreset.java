package newhorizon.expand.logic.cutscene.types;

import arc.audio.Sound;
import arc.graphics.g2d.TextureRegion;
import mindustry.entities.bullet.BulletType;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.content.bullets.RaidBullets;

public enum RaidPreset {
    PRESET_RAID_0(RaidBullets.raidBullet_1, NHSounds.alarm, NHContent.raid, 15, 5, 1, 40),
    ;

    public static final RaidPreset[] all = values();

    public final BulletType bulletType;
    public final Sound raidAlarmSound;
    public final TextureRegion warningIcon;

    public final float alertTime;
    public final float raidTime;
    public final float raidScale;
    public final float inaccuracy;

    RaidPreset(
            BulletType bulletType, Sound raidAlarmSound, TextureRegion warningIcon,
            float alertTime, float raidTime, float raidScale, float inaccuracy
    ) {
        this.bulletType = bulletType;
        this.raidAlarmSound = raidAlarmSound;
        this.warningIcon = warningIcon;

        this.alertTime = alertTime;
        this.raidTime = raidTime;
        this.inaccuracy = inaccuracy;
        this.raidScale = raidScale;
    }

}
