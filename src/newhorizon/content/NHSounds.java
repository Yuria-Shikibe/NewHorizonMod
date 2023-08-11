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
		fields.filter(f -> Sound.class.equals(f.getType()));
		try{
			for(Field f : fields)f.set(null, loadSound(f.getName()));
		}catch(IllegalAccessException e){
			e.printStackTrace();
		}
		
//		flak2 = loadSound("flak2");
//		alert2 = loadSound("alert2");
//		shock = loadSound("shock");
//		alarm = loadSound("alarm");
//		hyperspace = loadSound("hyperspace");
//		rapidLaser = loadSound("rapidLaser");
//		launch = loadSound("launch");
//		railGunBlast = loadSound("railGunBlast");
//		railGunCharge = loadSound("railGunCharge");
//		blaster = loadSound("blaster");
//		coil = loadSound("coil");
//		flak = loadSound("flak");
//		gauss = loadSound("gauss");
//		scatter = loadSound("scatter");
//		jumpIn = loadSound("jumpIn");
//		metalWalk = loadSound("metalWalk");
//		thermoShoot = loadSound("thermoShoot");
//		hugeShoot = loadSound("hugeShoot");
//		hugeBlast = loadSound("hugeBlast");
//		signal = loadSound("signal");
//		synchro = loadSound("synchro");
//		defenceBreak = loadSound("break");
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
