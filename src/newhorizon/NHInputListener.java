package newhorizon;

import arc.Core;
import arc.KeyBinds;
import arc.input.InputDevice;
import arc.input.KeyCode;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Unit;
import newhorizon.expand.units.EnergyUnit;
import newhorizon.util.annotation.HeadlessDisabled;

import java.lang.reflect.Field;

import static arc.Core.keybinds;

@HeadlessDisabled
public class NHInputListener{
	public Unit currentUnit;

	public NHInputListener(){}

	public static void registerModBinding(){
		try{
			Field definitionsField = KeyBinds.class.getDeclaredField("definitions");
			Field defaultCacheField = KeyBinds.class.getDeclaredField("defaultCache");
			
			definitionsField.setAccessible(true);
			defaultCacheField.setAccessible(true);
			
			KeyBinds.KeyBind[] definitionsNH = NHKeyBinding.values();
			KeyBinds.KeyBind[] definitions = (KeyBinds.KeyBind[])definitionsField.get(keybinds);
			Seq<KeyBinds.KeyBind> definitionSeq = new Seq<>(false, definitions.length + definitionsNH.length, KeyBinds.KeyBind.class);
			definitionSeq.addAll(definitionsNH).addAll(definitions);
			definitionsField.set(keybinds, definitionSeq.toArray());
			
			ObjectMap<KeyBinds.KeyBind, ObjectMap<InputDevice.DeviceType, KeyBinds.Axis>> defaultCache = (ObjectMap<KeyBinds.KeyBind, ObjectMap<InputDevice.DeviceType, KeyBinds.Axis>>)defaultCacheField.get(keybinds);
			for(KeyBinds.KeyBind def : definitionsNH){
				defaultCache.put(def, new ObjectMap<>());
				for(InputDevice.DeviceType type : InputDevice.DeviceType.values()){
					defaultCache.get(def).put(type,
							def.defaultValue(type) instanceof KeyBinds.Axis ?
									(KeyBinds.Axis)def.defaultValue(type) : new KeyBinds.Axis((KeyCode)def.defaultValue(type)));
				}
			}
			
		}catch(NoSuchFieldException | IllegalAccessException e){
			e.printStackTrace();
		}
	}
	public void update(){
		if(Vars.player != null)updatePlayerStatus();
		
		updateActiveAbility();
	}
	
	protected void updatePlayerStatus(){
		currentUnit = Vars.player.unit();
	}
	
	protected void updateActiveAbility(){
		if(currentUnit instanceof EnergyUnit){
			EnergyUnit u = (EnergyUnit)currentUnit;
			if(u.teleportValid() && Core.input.keyTap(NHKeyBinding.teleport)){
				Tmp.v1.set(Core.camera.unproject(Core.input.mouse())).sub(u).clampLength(0, u.teleportRange).add(u);
				
				u.teleport(Tmp.v1.x, Tmp.v1.y);
			}
		}
	}
}
