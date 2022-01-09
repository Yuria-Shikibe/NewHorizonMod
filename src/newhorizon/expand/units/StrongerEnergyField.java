package newhorizon.expand.units;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import mindustry.entities.abilities.EnergyFieldAbility;
import mindustry.gen.Unit;
import newhorizon.util.func.DrawFunc;

public class StrongerEnergyField extends EnergyFieldAbility{
	public StrongerEnergyField(float damage, float reload, float range){
		super(damage, reload, range);
	}
	
	@Override
	public void draw(Unit unit){
		super.draw(unit);
		
		Draw.color(color);
		DrawFunc.surround(unit.id, unit.x, unit.y, unit.hitSize() * 1.5f, 14, unit.hitSize() / 3, unit.hitSize(), Mathf.absin(15f, 1));
	}
}
