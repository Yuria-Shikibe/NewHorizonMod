package newhorizon.block.defence;

import arc.Core;
import arc.audio.Sound;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import mindustry.world.meta.values.AmmoListValue;
import newhorizon.vars.NHWorldVars;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

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
		range = 600f;
		spread = 40f;
		prepareDelay = 90f;
	}
	
	@Override
	public void setStats(){
		super.setStats();
		ObjectMap<Block, BulletType> map = new ObjectMap<>();
		map.put(this, bulletHitter);
		stats.add(Stat.ammo, new AmmoListValue<>(map));
	}
	
	@Override
	public void setBars() {
		super.setBars();
		bars.add("progress",
			(AirRaiderBuild entity) -> new Bar(
				() -> Core.bundle.get("bar.progress"),
				() -> Pal.power,
				() -> entity.reload / reloadTime
			)
		);
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
		public void updateTile(){
			if(isCharging()){
				reload = Mathf.clamp(reload + efficiency() * delta(), 0, reloadTime);
			}
			
			if(isPreparing()){
				countBack -= efficiency() * delta();
			}else if(preparing){
				countBack = prepareDelay;
				preparing = false;
				shoot(lastTarget);
			}
		}
		
		@Override
		public boolean canCommand(){
			Tile tile = world.tile(NHWorldVars.commandPos);
			return tile != null && consValid() && reload >= reloadTime && NHWorldVars.commandPos > 0 && within(tile, range);
		}
		
		@Override
		public void shoot(Integer pos){
			Tmp.p1.set(Point2.unpack(NHWorldVars.commandPos));
			
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
		public boolean isCharging(){
			return consValid() && reload < reloadTime;
		}
		
		@Override
		public float delayTime(){
			Tmp.p1.set(Point2.unpack(NHWorldVars.commandPos));
			return (dst(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y)) / tilesize * (tilesize / bulletHitter.speed) ) / Time.toSeconds;
		}
	}
}
