package newhorizon.vars;

import arc.struct.Seq;
import mindustry.entities.Damage;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.world.Tile;
import newhorizon.bullets.EffectBulletType;

public class NHVars{
	public static Tile tmpTile;
	public static final Seq<Team> allTeamSeq = new Seq<Team>(Team.all.length).addAll(Team.all);
	public static final EffectBulletType groundHitter = new EffectBulletType(3f){
		{damage = 1f; collidesGround = absorbable = true; hitSize = 0;}
		
		@Override
		public void despawned(Bullet b){
			if(!b.absorbed)Damage.damage(b.team, b.x, b.y, b.fdata, b.damage, collidesAir, collidesGround);
		}
	};
}
