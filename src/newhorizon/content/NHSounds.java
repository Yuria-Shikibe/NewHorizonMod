package newhorizon.content;

import arc.Core;
import arc.assets.AssetDescriptor;
import arc.assets.loaders.SoundLoader;
import arc.audio.Sound;
import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Sounds;
import mindustry.mod.Mods;
import newhorizon.NewHorizon;

import java.lang.reflect.Field;

public class NHSounds {
    public static ObjectMap<String, Sound> sounds = new ObjectMap<>();

    public static Sound
            laser2, laser3, laser4, laser5, thermo,
            flak2, alert2, shock, alarm, launch,
            rapidLaser, railGunCharge, blaster, flak, gauss, scatter,
            thermoShoot, jumpIn, metalWalk, hugeShoot, hugeBlast,
            signal, synchro, railGunBlast, largeBeam, coil1, coil2,

            blastHuge, blastShockwave, blastSmoke, blastZap, hyperspace,
            loopLaser1, loopLaser2, loopLaser3,
            shootBlaster1, shootBlaster2, shootBlaster3,
            shootCoil1, shootCoil2, shootCoil3,
            shootFlak1, shootFlak2, shootFlak3, shootFlak4,
            shootGauss1, shootGauss2, shootGauss3,
            shootHuge,
            shootMissile1, shootMissile2, shootMissile3, shootMissile4,
            shootPulse1, shootPulse2, shootPulse3, shootPulse4,
            shootRailgun1, shootRailgun2, shootRailgun3,
            shootScatter1, shootScatter2, shootScatter3,
            shootThermo1, shootThermo2, shootThermo3, shootThermo4,
            uiAlert1, uiAlert2, uiSignal, unitJumpIn
    ;

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
        String path = "sounds/" + soundName;
        String filePath = Vars.tree.get(path + ".ogg").exists() ? path + ".ogg" : path + ".mp3";

        Sound sound = new Sound();
        AssetDescriptor<?> desc = Core.assets.load(filePath, Sound.class, new SoundLoader.SoundParameter(sound));
        desc.errored = Throwable::printStackTrace;
        return sound;
    }
}

