package newhorizon.content;

import arc.Core;
import arc.assets.AssetDescriptor;
import arc.assets.loaders.SoundLoader;
import arc.audio.Sound;
import mindustry.Vars;
import mindustry.audio.SoundLoop;

public class NHSounds{
	public static Sound
		alert2 = new Sound(),
		shock = new Sound(),
		alarm = new Sound(),
		hyperspace = new Sound(),
		launch = new Sound(),
		rapidLaser = new Sound(),
		railGunCharge = new Sound(),
		blaster = new Sound(),
		coil = new Sound(),
		flak = new Sound(),
		gauss = new Sound(),
		pulse = new Sound(),
		scatter = new Sound(),
		thermoShoot = new Sound(),
		jumpIn = new Sound(),
		metalWalk = new Sound(),
		hugeShoot = new Sound(),
		hugeBlast = new Sound(),
		signal = new Sound(),
		synchro = new Sound(),
		defenceBreak = new Sound(),
		railGunBlast = new Sound();
	
	public static SoundLoop alertLoop = new SoundLoop(alert2, 1);
	
	public static void alertLoop(){
		if(!Vars.headless && Core.audio.countPlaying(alert2) == 0)alert2.play();
	}
	
	public static void load(){
		alert2 = loadSound("alert-2");
		shock = loadSound("shock");
		alarm = loadSound("alarm");
		hyperspace = loadSound("hyperspace");
		rapidLaser = loadSound("rapidLaser");
		launch = loadSound("launch");
		railGunBlast = loadSound("railGunBlast");
		railGunCharge = loadSound("railGunCharge");
		blaster = loadSound("blaster");
		coil = loadSound("coil");
		flak = loadSound("flak");
		gauss = loadSound("gauss");
		pulse = loadSound("pulse");
		scatter = loadSound("scatter");
		jumpIn = loadSound("jumpIn");
		metalWalk = loadSound("metalWalk");
		thermoShoot = loadSound("thermoShoot");
		hugeShoot = loadSound("hugeShoot");
		hugeBlast = loadSound("hugeBlast");
		signal = loadSound("signal");
		synchro = loadSound("synchro");
		defenceBreak = loadSound("break");
		
		alertLoop = new SoundLoop(alert2, 1);
	}
	
	private static Sound loadSound(String soundName){
		if(!Vars.headless){
			String name = "sounds/" + soundName;
			String path = Vars.tree.get(name + ".ogg").exists() ? name + ".ogg" : name + ".mp3";
			
			Sound sound = new Sound();
			
			AssetDescriptor<?> desc = Core.assets.load(path, Sound.class, new SoundLoader.SoundParameter(sound));
			desc.errored = Throwable::printStackTrace;
			return sound;
		}else return new Sound();
	}
}
