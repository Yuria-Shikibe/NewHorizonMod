package newhorizon.expand.units.ablility;

import arc.Core;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import newhorizon.expand.entities.GravityTrapField;

public class GravityTrapAbility extends Ability{
	public float range;
	
	protected GravityTrapField field;
	
	public GravityTrapAbility(float range){
		this.range = range;
	}
	
	@Override
	public void update(Unit unit){
		if(field == null){
			field = new GravityTrapField(unit, range).add();
		}
		
		field.setPosition(unit);
	}
	
	@Override
	public void death(Unit unit){
		if(field != null)field.remove();
	}
	
	@Override
	public String localized(){
		return Core.bundle.format("ability.gravity-trap", range / Vars.tilesize);
	}
}
