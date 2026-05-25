package newhorizon.content;

import arc.Core;
import arc.assets.AssetDescriptor;
import arc.assets.loaders.SoundLoader;
import arc.audio.Sound;
import arc.struct.ObjectMap;
import arc.util.Log;
import mindustry.Vars;

import java.lang.reflect.Field;

public class NHSounds {
    public static ObjectMap<String, Sound> sounds = new ObjectMap<>();

    public static Sound

            laser4, laser5, shock, launch, rapidLaser, railGunCharge, metalWalk, largeBeam,

            blastArc9000, blastShockwave, blastSmoke, blastZap, hyperspace,

            loopLaser1, loopLaser2, loopLaser3,

            shootArc9000,
            shootBlaster1, shootBlaster2, shootBlaster3,
            shootCoil1, shootCoil2, shootCoil3,
            shootFlak1, shootFlak2, shootFlak3, shootFlak4, shootFlak5, shootFlak6,
            shootGauss1, shootGauss2, shootGauss3,
            shootMissile1, shootMissile2, shootMissile3, shootMissile4,
            shootPulse1, shootPulse2, shootPulse3, shootPulse4, shootPulse5,
            shootRailgun1, shootRailgun2, shootRailgun3,
            shootScatter1, shootScatter2, shootScatter3,
            shootThermo1, shootThermo2, shootThermo3, shootThermo4,
            uiAlert1, uiAlarm1, uiSignal, unitJumpIn;

    public static void load() {
        try {
            for (Field field : NHSounds.class.getFields()) {
                if (field.getType().equals(Sound.class)) {
                    field.set(null, loadSound(field.getName()));
                }
            }
        } catch (IllegalAccessException e) {
            Log.err(e);
        }
    }

    private static Sound loadSound(String soundName) {
        Sound sound = new Sound();
        if (Vars.headless) return sound;

        String path = "sounds/" + soundName;
        String filePath = Vars.tree.get(path + ".ogg").exists() ? path + ".ogg" : path + ".mp3";

        AssetDescriptor<?> desc = Core.assets.load(filePath, Sound.class, new SoundLoader.SoundParameter(sound));
        desc.errored = Throwable::printStackTrace;
        return sound;
    }
}

