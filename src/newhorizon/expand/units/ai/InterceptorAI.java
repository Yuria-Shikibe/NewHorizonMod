package newhorizon.expand.units.ai;

import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.gen.Teamc;
import mindustry.gen.Unitc;

import static mindustry.Vars.state;

public class InterceptorAI extends FlyingAI {

	@Override
	public void updateMovement() {
		unloadPayloads();

		if (target != null && unit.hasWeapons()) {
			if (unit.type.circleTarget) {
				circleAttack(240f);
			} else {
				moveTo(target, unit.type.range * 0.8f);
				unit.lookAt(target);
			}
		}

		if (target == null && state.rules.waves && unit.team == state.rules.defaultTeam) {
			moveTo(getClosestSpawner(), state.rules.dropZoneRadius + 230f);
		}
	}

	@Override
	public Teamc findTarget(float x, float y, float range, boolean air, boolean ground) {
		Teamc result = findMainTarget(x, y, range, air, false);

		//if the main target is in range, use it, otherwise target whatever is closest
		return checkTarget(result, x, y, range) ? target(x, y, range, air, false) : result;
	}

	@Override
	public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground) {
		return Units.closestEnemy(unit.team, x, y, range * 10, Unitc::isFlying);
	}
}
