package newhorizon.expand.logic.cutscene.types;

import arc.audio.Sound;
import newhorizon.content.NHSounds;

public enum WarningSound {
    alarm(NHSounds.alarm)
    ;

    public final Sound sound;

    WarningSound(Sound sound) {
        this.sound = sound;
    }
}
