package newhorizon.expand.units;

import arc.func.Cons;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;

public class NHWeapon extends Weapon{
	public boolean drawShadow = false;
	
	public NHWeapon(){
		this("");
	}
	
	public NHWeapon(String name){
		super(NewHorizon.name(name));
	}
	
	@Override
	public NHWeapon copy(){
		return (NHWeapon)super.copy();
	}
	
//	@Override
//	public void load(){
//		this.region = Core.atlas.find(name, Core.atlas.find("clear"));
//		this.heatRegion = Core.atlas.find(name + "-heat");
//		this.outlineRegion = Core.atlas.find(name + "-outline");
//	}
	@Override
	public void draw(Unit unit, WeaponMount mount){
		float
				rotation = unit.rotation - 90,
				weaponRotation  = rotation + (rotate ? mount.rotation : 0),
				recoil = -((mount.reload) / reload * this.recoil),
				wx = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, recoil),
				wy = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, recoil);
		
		if(shadow > 0){
			Drawf.shadow(wx, wy, shadow);
		}
		
		if(outlineRegion.found() && top){
			Draw.rect(outlineRegion,
					wx, wy,
					outlineRegion.width * Draw.scl * -Mathf.sign(flipSprite),
					region.height * Draw.scl,
					weaponRotation);
		}
		
		Draw.rect(region,
				wx, wy,
				region.width * Draw.scl * -Mathf.sign(flipSprite),
				region.height * Draw.scl,
				weaponRotation);
		
		if(heatRegion.found() && mount.heat > 0){
			Draw.color(heatColor, mount.heat);
			Draw.blend(Blending.additive);
			Draw.rect(heatRegion,
					wx, wy,
					heatRegion.width * Draw.scl * -Mathf.sign(flipSprite),
					heatRegion.height * Draw.scl,
					weaponRotation);
			Draw.blend();
			Draw.color();
		}
		
		if(!drawShadow)return;
		float z = Draw.z();
		Draw.z(Math.min(Layer.darkness, unit.elevation > 0.5f ? (unit.type.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : unit.type.groundLayer + Mathf.clamp(unit.type.hitSize / 4000f, 0, 0.01f) - 1f));
		Draw.color(Pal.shadow);
		float e = Math.max(unit.elevation, unit.type.visualElevation);
		Draw.rect(region,
				wx + UnitType.shadowTX * e, wy + UnitType.shadowTY * e,
				region.width * Draw.scl * -Mathf.sign(flipSprite),
				region.height * Draw.scl,
				weaponRotation);
		Draw.color();
		Draw.z(z);
	}
	
	public NHWeapon setInaccuracy(float inaccuracy){
		this.inaccuracy = inaccuracy;
		return this;
	}
	
	public NHWeapon setAutoTarget(boolean active){
		autoTarget = active;
		controllable = !active;
		return this;
	}
	
	public NHWeapon setAlternate(boolean b){
		alternate = b;
		return this;
	}
	
	public NHWeapon set(Cons<NHWeapon> cons){
		cons.get(this);
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
	
	public NHWeapon setType(BulletType type){
		bullet = type;
		return this;
	}
	
	public static Weapon setPos(Weapon weapon, float x, float y){
		weapon.x = x;
		weapon.y = y;
		return weapon;
	}
	
	public static Weapon set(Weapon weapon, Cons<Weapon> cons){
		cons.get(weapon);
		return weapon;
	}
}
