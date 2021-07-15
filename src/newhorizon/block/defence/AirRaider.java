package newhorizon.block.defence;

import arc.audio.Sound;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.gen.Sounds;
import newhorizon.vars.NHVars;

import static mindustry.Vars.tilesize;

public class AirRaider extends CommandableAttackerBlock{
	public int salvos = 3;
	public float burstSpacing = 8f;
	public float shootSpread = 38f;
	
	public Effect shootEffect = Fx.none;
	public Effect smokeEffect = Fx.none;
	public Effect triggeredEffect = Fx.none;
	public Sound shootSound = Sounds.artillery;
	public float shootShake = 4f;
	
	protected Vec2 tr = new Vec2();
	
	public AirRaider(String name){
		super(name);
		
		reloadTime = 600f;
		range = 1000f;
		spread = 40f;
		prepareDelay = 90f;
	}
	
	@Override
	public void setStats(){
		super.setStats();
	}
	
	public class AirRaiderBuild extends CommandableAttackerBlockBuild{
		public void effects(float rotation){
			Effect fshootEffect = shootEffect == Fx.none ? bulletHitter.shootEffect : shootEffect;
			Effect fsmokeEffect = smokeEffect == Fx.none ? bulletHitter.smokeEffect : smokeEffect;
			
			fshootEffect.at(x + tr.x, y + tr.y, rotation);
			fsmokeEffect.at(x + tr.x, y + tr.y, rotation);
			shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));
			
			if(shootShake > 0){
				Effect.shake(shootShake, shootShake, this);
			}
		}
		
		@Override
		public void shoot(Integer pos){
			Tmp.p1.set(Point2.unpack(pos));
			
			for(int i = 0; i < salvos; i++){
				Time.run(burstSpacing * i, () -> {
					tr.setToRandomDirection().scl(shootSpread);
					Tmp.v5.setToRandomDirection().scl(spread);
					
					float toX = World.unconv(Tmp.p1.x) + Tmp.v5.x, toY = World.unconv(Tmp.p1.y) + Tmp.v5.y;
					float ang = Angles.angle(x + tr.x, y + tr.y, toX, toY);
					float lifeScl = bulletHitter.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, toX, toY) / bulletHitter.range(), 0f, range() / bulletHitter.range()) : 1f;
					bulletHitter.create(
						this, team, x + tr.x, y + tr.y,
						ang,
						1f, lifeScl
					);
					triggeredEffect.at(this);
					effects(ang);
				});
			}
			
			reload = 0f;
			consume();
		}
		
		@Override
		public float delayTime(){
			Tmp.p1.set(Point2.unpack(NHVars.world.commandPos));
			return (dst(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y)) / tilesize * (tilesize / bulletHitter.speed) ) / Time.toSeconds;
		}
	}
}
