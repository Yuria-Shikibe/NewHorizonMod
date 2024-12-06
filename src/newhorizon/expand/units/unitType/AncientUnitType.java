package newhorizon.expand.units.unitType;

import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.ammo.ItemAmmoType;
import newhorizon.content.NHColor;
import newhorizon.content.NHItems;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.units.AncientEngine;

public class AncientUnitType extends NHUnitType {
	public boolean immuniseAll = true;
	
	protected static Seq<UnitType> toImmunise = new Seq<>();
	
	public AncientUnitType(String name){
		super(name);
		
		outlineColor = Pal.darkOutline;
		healColor = NHColor.ancientLightMid;
		lightColor = NHColor.ancientLightMid;
		
		ammoType = new ItemAmmoType(NHItems.zeta);
	}
	
	public void addEngine(float x, float y, float relativeRot, float rad, boolean flipAdd){
		if(flipAdd){
			for(int i : Mathf.signs){
				engines.add(new AncientEngine(x * i, y, rad, -90 + relativeRot * i, 0));
				engines.add(new AncientEngine(x * i, y, rad * 1.85f, -90 + relativeRot * i, Mathf.random(2f)).a(0.3f));
			}
		}else{
			engines.add(new AncientEngine(x, y, rad, -90 + relativeRot, 0));
			engines.add(new AncientEngine(x, y, rad * 1.85f, -90 + relativeRot, Mathf.random(2f)).a(0.3f));
		}
	}
	
	@Override
	public void init(){
		super.init();
		
		if(immuniseAll){
			NHUnitTypes.immunise(this);
		}
	}
}
