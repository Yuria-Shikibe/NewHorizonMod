package newhorizon.expand.block.turrets;

import arc.Core;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.Strings;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BulletType;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHColor;
import newhorizon.content.NHStatValues;
import newhorizon.content.NHStats;

public class SpeedupTurret extends PowerTurret{

	public float overheatTime = 360f;
	public float overheatCoolAmount = 1.25f;

	public float maxSpeedupScl = 0.5f;
	public float speedupPerShoot = 0.075f;
	
	public float slowDownReloadTime = 150f;
	public float inaccuracyUp = 0f;
	
	public float maxHeatEffectChance = 0.3f;
	public Effect heatEffect = Fx.reactorsmoke;
	
	public SpeedupTurret(String name){
		super(name);
	}
	
	@Override
	public void setBars(){
		super.setBars();
		addBar("liquid",
			(SpeedupTurretBuild entity) -> new Bar(
				() -> Core.bundle.format("nh.bar.speed-up", Strings.autoFixed((entity.speedupScl) * 100, 0)),
				() -> NHColor.lightSkyBack,
				() -> entity.speedupScl / maxSpeedupScl
			)
		);
		
		addBar("overheat",
			(SpeedupTurretBuild entity) -> new Bar(
				() -> Core.bundle.format("nh.bar.overheat", Strings.autoFixed(Mathf.clamp(entity.overheat / overheatTime, 0, overheatTime) * 100, 0)),
				() -> entity.requireCompleteCooling ? Pal.redderDust : Pal.powerLight,
				() -> entity.overheat / overheatTime
			)
		);
	}
	
	@Override
	public void setStats(){
		super.setStats();
		stats.add(NHStats.fireRateMax, Strings.autoFixed((1 + maxSpeedupScl) * 100, 0) + "%");
		stats.add(NHStats.overheatCooldown, overheatTime / overheatCoolAmount / Time.toSeconds, StatUnit.seconds);

		stats.remove(Stat.ammo);
		stats.add(Stat.ammo, NHStatValues.ammo(ObjectMap.of(this, shootType), 0, false));

		if(coolant != null){
			stats.remove(Stat.booster);
			stats.add(Stat.booster, NHStatValues.boosters(reload, coolant.amount, coolantMultiplier, true, l -> l.coolant && consumesLiquid(l)));
		}
	}
	
	public class SpeedupTurretBuild extends PowerTurretBuild{
		public float speedupScl = 0f;
		public float slowDownReload = 0f;
		public float overheat = 0;
		public boolean requireCompleteCooling = false;

		@Override
		public void updateTile(){
			updateCooldown();
			if(overheat < overheatTime && !requireCompleteCooling){
				super.updateTile();
			}else{
				forceCoolDown();
			}
		}

		public void updateCooldown(){
			if(slowDownReload > 0f){
				slowDownReload -= Time.delta;
			}else{
				speedupScl = Mathf.lerpDelta(speedupScl, 0f, 0.05f);
				if(!requireCompleteCooling)coolDown();
			}

			if(overheat > overheatTime * 0.3f){
				if(Mathf.chanceDelta(maxHeatEffectChance * (requireCompleteCooling ? 1 : overheat / overheatTime))){
					heatEffect.at(x + Mathf.range(Vars.tilesize * size / 2), y + Mathf.range(Vars.tilesize * size / 2), rotation, heatColor);
				}
			}
		}

		public void forceCoolDown(){
			slowDownReload = 0;
			coolDown();
			if(linearWarmup){
				shootWarmup = Mathf.approachDelta(shootWarmup, 0, shootWarmupSpeed);
			}else{
				shootWarmup = Mathf.lerpDelta(shootWarmup, 0, shootWarmupSpeed);
			}

			unit.tile(this);
			unit.rotation(rotation);
			unit.team(team);
			curRecoil = Mathf.approachDelta(curRecoil, 0, 1 / recoilTime);
			recoilOffset.trns(rotation, -Mathf.pow(curRecoil, recoilPow) * recoil);

			if(logicControlTime > 0){
				logicControlTime -= Time.delta;
			}

			if(overheat <= 0){
				overheat = 0;
				requireCompleteCooling = false;
			}
		}
		
		public void coolDown(){
			if (overheat > 0){
				overheat -= overheatCoolAmount * (1 + coolantEfficiency()) * Time.delta;
			}
		}

		public float coolantEfficiency(){
			return liquids.current() == null ? 0 : liquids.current().heatCapacity;
		}
		
		@Override
		protected void updateShooting(){
            if(reloadCounter >= reload){
                BulletType type = peekAmmo();

                shoot(type);
	
	            reloadCounter = 0f;
            }else{
	            reloadCounter += (1 + speedupScl) * delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
	            overheat = Mathf.approachDelta(overheat, overheatTime + 0.05f, efficiency * timeScale * ((speedupScl / maxSpeedupScl) * 1) / (1 + (liquids.current() == null ? 0 : liquids.current().heatCapacity)));
	            if(overheat > overheatTime)requireCompleteCooling = true;
            }
        }
				
		@Override
		protected void shoot(BulletType type){
			super.shoot(type);
			
			slowDownReload = slowDownReloadTime;
			if(speedupScl < maxSpeedupScl){
				speedupScl += speedupPerShoot;
			}else speedupScl = maxSpeedupScl;
		}
		
		@Override
		protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
			super.bullet(type, xOffset, yOffset, angleOffset + Mathf.range(speedupScl * inaccuracyUp), mover);
		}

		@Override
		public byte version() {
			return 2;
		}

		@Override
		public void write(Writes write) {
			super.write(write);

			write.f(overheat);
			write.f(slowDownReload);
			write.f(speedupScl);
			write.bool(requireCompleteCooling);
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			if (revision == 2){
				overheat = read.f();
				slowDownReload = read.f();
				speedupScl = read.f();
				requireCompleteCooling = read.bool();
			}
		}
	}
}













