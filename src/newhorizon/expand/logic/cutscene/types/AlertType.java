package newhorizon.expand.logic.cutscene.types;

import arc.audio.Sound;
import newhorizon.content.NHSounds;

public enum AlertType {
    alarm(NHSounds.alarm)
    ;

    public final Sound sound;

    AlertType(Sound sound) {
        this.sound = sound;
    }
}
