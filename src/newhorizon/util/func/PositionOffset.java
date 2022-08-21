package newhorizon.util.func;

import arc.math.geom.Vec2;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import newhorizon.util.annotation.CacheBanned;
import newhorizon.util.annotation.ThreadOnly;

@CacheBanned
@ThreadOnly("main")
public class PositionOffset{
	public static final Vec2 tmp = new Vec2();
	
	public static Vec2 unitEngineOffset(Unit unit, UnitType.UnitEngine engine){
		return tmp.set(engine.x, engine.y).rotate(unit.rotation - 90);
	}
}
