package newhorizon.expand.units;

import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.type.Weapon;

public class SalvoWeapon extends Weapon{
	
	/**
	 * The position of each turret relative to the unit. <p>
	 * uses ({@link Weapon#x}, {@link Weapon#y}) as the fire center of the salvo system.
	 *
	 * */
	public Seq<Vec2> poses = new Seq<>();
}
