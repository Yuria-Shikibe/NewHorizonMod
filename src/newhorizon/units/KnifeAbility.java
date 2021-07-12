package newhorizon.units;

import arc.math.Mathf;
import arc.math.geom.Vec3;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;

public class KnifeAbility extends Ability{
	public float damage;
	
	public Vec3[] offset;
	
	
	protected float knifeStroke = 0f;
	protected boolean anyNearby = false;
	
	@Override
	public void draw(Unit unit){
		super.draw(unit);
	}
	
	@Override
	public void update(Unit unit){
		super.update(unit);
		knifeStroke = Mathf.lerpDelta(knifeStroke, anyNearby ? 1 : 0, 0.09f);
		
		anyNearby = false;
		Units.nearbyEnemies(unit.team, unit.x, unit.y, 120f, u -> anyNearby = true);
	}
	
	
}
