package newhorizon.units;

import arc.Core;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;
import newhorizon.content.NHLoader;

public class AutoOutlineWeapon extends Weapon{
	public AutoOutlineWeapon(){
		this("");
	}
	
	public AutoOutlineWeapon(String name){
		super(name);
		
		NHLoader.put(name + "@-outline");
		this.name = NewHorizon.MOD_NAME + name;
	}
	
	@Override
	public void load(){
		this.region = Core.atlas.find(this.name, Core.atlas.find("clear"));
		this.heatRegion = Core.atlas.find(this.name + "-heat");
		this.outlineRegion = Core.atlas.find(this.name + "-outline");
	}
	
	public AutoOutlineWeapon setPos(float x, float y){
		this.x = x;
		this.y = y;
		return this;
	}
}
