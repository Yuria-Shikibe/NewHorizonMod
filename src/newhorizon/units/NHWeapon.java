package newhorizon.units;

import arc.Core;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;

public class NHWeapon extends Weapon{
	public NHWeapon(){
		this("");
	}
	
	public NHWeapon(String name){
		super(NewHorizon.configName(name));
	}
	
	@Override
	public NHWeapon copy(){
		return (NHWeapon)super.copy();
	}
	
	@Override
	public void load(){
		this.region = Core.atlas.find(this.name, Core.atlas.find("clear"));
		this.heatRegion = Core.atlas.find(this.name + "-heat");
		this.outlineRegion = Core.atlas.find(this.name + "-outline");
	}
	
	public NHWeapon setAlternate(boolean b){
		alternate = b;
		return this;
	}
	
	public NHWeapon setPos(float x, float y){
		this.x = x;
		this.y = y;
		return this;
	}
	
	public NHWeapon salvoDelay(int total, int id){
		this.firstShotDelay = reload/ total * id;
		return this;
	}
	
	public NHWeapon setDelay(float delay){
		this.firstShotDelay = delay;
		return this;
	}
}
