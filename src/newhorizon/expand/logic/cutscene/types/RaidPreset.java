package newhorizon.expand.logic.cutscene.types;

import arc.audio.Sound;
import arc.graphics.g2d.TextureRegion;
import mindustry.entities.bullet.BulletType;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.content.bullets.RaidBullets;

public enum RaidPreset {
    PRESET_RAID_0(RaidBullets.raidBullet_1, NHSounds.alarm, NHContent.raid),
    ;

    public static final RaidPreset[] all = values();

    public final BulletType bulletType;
    public final Sound raidAlarmSound;
    public final TextureRegion warningIcon;

    RaidPreset(BulletType bulletType, Sound raidAlarmSound, TextureRegion warningIcon) {
        this.bulletType = bulletType;
        this.raidAlarmSound = raidAlarmSound;
        this.warningIcon = warningIcon;
    }

}
