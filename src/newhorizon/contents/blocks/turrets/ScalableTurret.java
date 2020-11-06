package newhorizon.contents.blocks.turrets;

import mindustry.entities.*;
import arc.*;
import arc.math.*;
import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.ctype.*;
import mindustry.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.campaign.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.experimental.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import newhorizon.contents.items.*;
import newhorizon.contents.colors.*;
import newhorizon.contents.bullets.*;
import newhorizon.contents.interfaces.Scalablec;

import newhorizon.contents.blocks.special.UpgraderBlock.*;
import newhorizon.contents.data.*;
import newhorizon.NewHorizon;

import static newhorizon.contents.data.UpgradeData.*;
import static mindustry.type.ItemStack.*;
import static mindustry.Vars.*;

public class ScalableTurret extends ChargeTurret{
	public UpgradeBaseData defaultBaseData = new UpgradeBaseData();
	public UpgradeAmmoData defaultAmmoData = new UpgradeAmmoData("emergency-replace", "Default data", UpgradeData.none, 0f, 0, new ItemStack(NHItems.emergencyReplace, 0));
	
	public ItemStack ammoConsume = new ItemStack(NHItems.emergencyReplace, 0);
	
	public Color baseColor = NHColor.darkEnrColor;
	//Load Mod Factories
	public ScalableTurret(String name){
		super(name);
		hasPower = true;
		hasItems = true;
	}
	
	public void addConsume(ItemStack stack){
		ammoConsume = stack;
		consumes.item(stack.item, stack.amount);
	}
	
	@Override
	public void load(){
		super.load();
		baseRegion = Core.atlas.find("new-horizon-block-" + size);
		defaultAmmoData.load();
		defaultBaseData.load();
	}
	
	@Override
    public void setStats(){
        super.setStats();
		stats.add(Stat.damage, 0, StatUnit.none);
    }

    @Override
    public void init(){
        consumes.powerCond(powerUse, TurretBuild::isActive);
        super.init();
	}
	
	public class ScalableTurretBuild extends TurretBuild implements Scalablec{
		public UpgradeBaseData baseData = defaultBaseData;
		public UpgradeAmmoData ammoData = defaultAmmoData;
		
		public boolean shooting;
		public float powerUse = 1f;
		
		@Override public boolean shouldTurn(){return !shooting;}
    
    	@Override
        public void shoot(BulletType ammo){
        	if(ammo.compareTo(UpgradeData.none) == 0)return;
            useAmmo();

            tr.trns(rotation, size * tilesize / 2f);
			ammoData.chargeBeginEffect.at(x + tr.x, y + tr.y, rotation);
			ammoData.chargeEffect.at(x + tr.x, y + tr.y, rotation);
            
			if(ammoData.chargeTime > 0)shooting = true;

            Time.run(ammoData.chargeTime, () -> {
            	if(ammoData.burstSpacing > 0.0001f){
					for(int i = 0; i < ammoData.salvos; i++){
						Time.run(ammoData.burstSpacing * i, () -> {
							if(!isValid())return;
							tr.trns(rotation, 1.15f* (size * tilesize / 2f), Mathf.range(ammoData.randX) );
							recoil = recoilAmount;
							heat = 2f;
							bullet(ammo, rotation + Mathf.range(ammoData.inaccuracy));
							effects();
						});
					}
				}
				if(!isValid())return;
				shooting = false;
            });
        }
        
        @Override
        public BulletType peekAmmo(){
        	return getAmmoData() == null ? UpgradeData.none : getAmmoData().selectAmmo == null ? UpgradeData.none : getAmmoData().selectAmmo;
		}
		
		@Override
		public BulletType useAmmo(){
			this.items.remove(ammoConsume);
            return peekAmmo();
        }
		
		public boolean hasAmmo(){
			return consValid();
		}
		
		@Override
		public void drawSelect(){
			super.drawSelect();
			if(isConnected())drawConnected();
			float len = block.size * tilesize / 2 - tilesize;
			Draw.color();
			Draw.rect(ammoData.icon, x - len, y + len);
			Draw.color(baseColor);
			Draw.rect(NewHorizon.NHNAME + "upgrade-icon-outline", x - len, y + len);
			Draw.color();
		}
				
		@Override
		public void updateTile(){
			unit.ammo(power.status * unit.type().ammoCapacity);
			consumes.powerCond(powerUse, (TurretBuild entity) -> entity.target != null || (entity.logicControlled() && entity.logicShooting));
			if(!validateTarget()) target = null;

			recoil = Mathf.lerpDelta(recoil, 0f, restitution);
			heat = Mathf.lerpDelta(heat, 0f, cooldown);

			unit.health(health);
			unit.rotation(rotation);
			unit.team(team);

			if(logicControlTime > 0){
				logicControlTime -= Time.delta;
			}

			if(hasAmmo()){

				if(timer(timerTarget, targetInterval)){
					findTarget();
				}

				if(validateTarget()){
					boolean canShoot = true;

					if(isControlled()){
						targetPos.set(unit.aimX(), unit.aimY());
						canShoot = unit.isShooting();
					}else if(logicControlled()){ //logic behavior
						canShoot = logicShooting;
					}else{
						BulletType type = peekAmmo();
						float speed = type.speed;
                     
						if(speed < 0.1f) speed = 9999999f;

						targetPos.set(Predict.intercept(this, target, speed));
						if(targetPos.isZero()){
							targetPos.set(target);
						}

						if(Float.isNaN(rotation)){
							rotation = 0;
						}
					}

					float targetRot = angleTo(targetPos);

					if(shouldTurn()){
						turnToTarget(targetRot);
					}

					if(Angles.angleDist(rotation, targetRot) < shootCone && canShoot){
						updateShooting();
					}
				}
			}

			if(acceptCoolant){
				updateCooling();
			}
		}
		
		protected float reloadTime(){
			return ammoData.reloadTime <= 0 ? reloadTime : ammoData.reloadTime;
		}
		
		@Override
		protected void updateShooting(){
            if(reload >= reloadTime()){
                BulletType type = peekAmmo();

                shoot(type);

                reload = 0f;
            }else{
                reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            }
        }

		@Override
		protected void updateCooling(){
			float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;
			
			Liquid liquid = liquids.current();

			float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, ((reloadTime - reload) / coolantMultiplier) / liquid.heatCapacity)) * baseReloadSpeed();
			reload += used * liquid.heatCapacity * coolantMultiplier;
			liquids.remove(liquid, used);

			if(Mathf.chance(0.06 * used)){
				coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
			}
		}
		
		@Override
		protected void bullet(BulletType type, float angle){
            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

            type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(ammoData.velocityInaccuracy), lifeScl);
        }
		
		@Override
		public void resetUpgrade(){
			baseData = defaultBaseData;
			ammoData = defaultAmmoData;
		}
		
		@Override
		public void drawConnected(){
			float sin = Mathf.absin(Time.time(), 6f, 1f);
			for(int i = 0; i < 4; i++){
				float length = tilesize * block.size / 2 + 3 + sin;
				Tmp.v1.trns(i * 90, -length);
				Draw.color(Pal.gray);
				Draw.rect(NewHorizon.NHNAME + "linked-arrow-back", x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
				Draw.color(baseColor);
				Draw.rect(NewHorizon.NHNAME + "linked-arrow", 	 x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
			}
		}
		
		@Override public boolean isConnected(){return baseData == null ? false : upgrader() != null;}
		@Override public UpgraderBlockBuild upgrader(){return baseData.from;}
		
	    @Override public UpgradeBaseData getBaseData(){return baseData;}
		@Override public UpgradeAmmoData getAmmoData(){return ammoData;}
    	
		@Override public void setBaseData(UpgradeBaseData data){this.baseData = data;}
		@Override public void setAmmoData(UpgradeAmmoData data){this.ammoData = data;}
	}
}









