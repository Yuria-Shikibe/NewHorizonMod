package newhorizon.content;

import arc.Core;
import arc.assets.AssetDescriptor;
import arc.assets.loaders.SoundLoader;
import arc.audio.Sound;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;

import java.lang.reflect.Field;

public class NHSounds {

	public static Sound
			laser2, laser3, laser4, laser5, thermo,
			flak2, alert2, shock, alarm, hyperspace, launch,
			rapidLaser, railGunCharge, blaster, flak, gauss, scatter,
			thermoShoot, jumpIn, metalWalk, hugeShoot, hugeBlast,
			signal, synchro, railGunBlast, largeBeam, coil1, coil2;

	public static void alertLoop() {
		if (!Vars.headless) {
			Vars.control.sound.loop(alert2, 2f);
		}
	}

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
		if (Vars.headless) {
			return new Sound();
		}

		String path = "sounds/" + soundName;
		String filePath = Vars.tree.get(path + ".ogg").exists() ? path + ".ogg" : path + ".mp3";

		Sound sound = new Sound();
		AssetDescriptor<?> desc = Core.assets.load(filePath, Sound.class, new SoundLoader.SoundParameter(sound));
		desc.errored = Throwable::printStackTrace;
		return sound;
	}
}

