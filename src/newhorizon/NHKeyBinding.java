package newhorizon;

import arc.KeyBinds;
import arc.input.InputDevice;
import arc.input.KeyCode;

public enum NHKeyBinding implements KeyBinds.KeyBind{
	teleport(KeyCode.j);
	
	private final KeyBinds.KeybindValue defaultValue;
	private final String category;
	
	NHKeyBinding(KeyBinds.KeybindValue defaultValue, String category){
		this.defaultValue = defaultValue;
		this.category = category;
	}
	
	NHKeyBinding(KeyBinds.KeybindValue defaultValue){
		this(defaultValue, "new-horizon");
	}
	
	@Override
	public KeyBinds.KeybindValue defaultValue(InputDevice.DeviceType type){
		return defaultValue;
	}
	
	@Override
	public String category(){
		return category;
	}
}
