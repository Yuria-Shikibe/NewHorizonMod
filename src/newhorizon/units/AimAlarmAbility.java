package newhorizon.units;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

public class AimAlarmAbility extends Ability{
	@Override
	public void draw(Unit unit){
		Draw.z(Layer.effect);
		float sin = Mathf.absin(10f, 0.55f);
		Lines.stroke(3f + sin, unit.team.color);
		for(WeaponMount weapon : unit.mounts){
			Lines.spikes(weapon.aimX, weapon.aimY, weapon.weapon.inaccuracy + 5f, 4f + 4 * sin, 4, Time.time);
		}
		
		
	}
}
