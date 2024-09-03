package newhorizon.content;

import arc.Core;
import arc.assets.AssetDescriptor;
import arc.assets.loaders.SoundLoader;
import arc.audio.Sound;
import arc.struct.Seq;
import mindustry.Vars;

import java.lang.reflect.Field;

public class NHSounds{
	public static Sound
		cannon, laser2, laser3, laser4, laser5, thermo,
		flak2 = new Sound(),
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
		scatter = new Sound(),
		thermoShoot = new Sound(),
		jumpIn = new Sound(),
		metalWalk = new Sound(),
		hugeShoot = new Sound(),
		hugeBlast = new Sound(),
		signal = new Sound(),
		synchro = new Sound(),
		defenceBreak = new Sound(),
		railGunBlast = new Sound(),
		largeBeam = new Sound();
	
	public static void alertLoop(){
		if(!Vars.headless){
			Vars.control.sound.loop(alert2, 2f);
		}
	}
	
	public static void load(){
		Class<?> c = NHSounds.class;
		Seq<Field> fields = new Seq<>(c.getFields());
		fields.retainAll(f -> Sound.class.equals(f.getType()));
		try{
			for(Field f : fields)f.set(null, loadSound(f.getName()));
		}catch(IllegalAccessException e){
			e.printStackTrace();
		}
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
