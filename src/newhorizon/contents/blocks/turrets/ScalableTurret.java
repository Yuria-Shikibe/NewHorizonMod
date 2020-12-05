package newhorizon.contents.blocks.turrets;


import mindustry.entities.*;
import arc.scene.ui.layout.*;
import arc.*;
import arc.math.*;
import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.ui.*;
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
import static newhorizon.contents.data.UpgradeBaseData.*;
import static mindustry.type.ItemStack.*;
import static mindustry.Vars.*;

public class ScalableTurret extends Turret{
	public UpgradeBaseData defaultBaseData = new UpgradeBaseData();
	public UpgradeAmmoData defaultAmmoData = new UpgradeAmmoData();
	
	public float powerUse;
	
	public Color baseColor = Pal.accent;
	//Load Mod Factories
	public ScalableTurret(String name){
		super(name);
		itemCapacity = 60;
		configurable = true;
		hasPower = true;
		hasItems = true;
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
		stats.add(Stat.damage, defaultAmmoData.selectAmmo.damage, StatUnit.none);
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
		
		Bullet bullet;
        float bulletLife;
		
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
							tr.trns(rotation, (size * tilesize / 2f) - recoil, Mathf.range(ammoData.randX) );
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
        protected void effects(){
            Effect fshootEffect = shootEffect == Fx.none ? peekAmmo().shootEffect : shootEffect;
            Effect fsmokeEffect = smokeEffect == Fx.none ? peekAmmo().smokeEffect : smokeEffect;

			fshootEffect.at(x + tr.x, y + tr.y, rotation);
			fsmokeEffect.at(x + tr.x, y + tr.y, rotation);
			getAmmoData().shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));

			if(shootShake > 0){
                Effect.shake(shootShake, shootShake, this);
			}

			recoil = recoilAmount;
		}

        
        @Override
        public BulletType peekAmmo(){
        	return getAmmoData() == null ? UpgradeData.none : getAmmoData().selectAmmo == null ? UpgradeData.none : getAmmoData().selectAmmo;
		}
		
		@Override
		public BulletType useAmmo(){
			this.items.remove(consumes.getItem().items);
            return peekAmmo();
        }
		
		public boolean hasAmmo(){
			return consValid();
		}
		
		@Override
		public void drawConfigure(){
			float len = block.size * tilesize / 2 - tilesize;
			Drawf.dashCircle(x, y, range(), baseColor);
			Lines.stroke(1f, baseColor);
			Lines.square(x, y, block.size * tilesize / 2.0f + 1.0f);
			Draw.reset();
			if(isConnected())drawConnected();
			
			Draw.rect(ammoData.icon, x - len, y + len);
			Draw.color(baseColor);
			Draw.rect(NewHorizon.NHNAME + "upgrade-icon-outline", x - len, y + len);
			Draw.reset();
		}
		
		@Override
		public void updateTile(){
			super.updateTile();
			
			if(!isContiunous())return;
			if(bulletLife > 0 && bullet != null){
				tr.trns(rotation, block.size * tilesize / 2f, 0f);
                bullet.rotation(rotation);
                bullet.set(x + tr.x, y + tr.y);
                bullet.time(0f);
                heat = 1f;
                recoil = recoilAmount;
                bulletLife -= Time.delta / Math.max(efficiency(), 0.00001f);
                if(bulletLife <= 0f){
                    bullet = null;
                }
            }else if(reload > 0){
                Liquid liquid = liquids.current();
                float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;

                float used = (cheating() ? maxUsed * Time.delta : Math.min(liquids.get(liquid), maxUsed * Time.delta)) * liquid.heatCapacity * coolantMultiplier;
                reload -= used;
                liquids.remove(liquid, used);

                if(Mathf.chance(0.06 * used)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }

		}
		
		protected float reloadTime(){
			float realReload = ammoData.reloadTime <= 0 ? reloadTime : ammoData.reloadTime;
			return realReload * (1 - Mathf.clamp(baseData.speedMPL * baseData.level, 0, maxReloadReduce) );
		}
		
		@Override
		protected void updateShooting(){
			if(isContiunous() && bulletLife > 0 && bullet != null){
                return;
            }

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
			if(isContiunous()){
				bullet = type.create(tile.build, team, x + tr.x, y + tr.y, angle);
				bulletLife = ammoData.continuousTime;
			}else{
				float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;
				type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(ammoData.velocityInaccuracy), lifeScl);
			}
        }
		
		@Override
		public void resetUpgrade(){
			baseData = defaultBaseData;
			ammoData = defaultAmmoData;
		}
		
		@Override
		public void drawConnected(){
			float sin = Mathf.absin(Time.time, 6f, 1f);
			for(int i = 0; i < 4; i++){ 
				float length = tilesize * block.size / 2 + 3 + sin;
				Tmp.v1.trns(i * 90, -length);
				Draw.color(Pal.gray);
				Draw.rect(NewHorizon.NHNAME + "linked-arrow-back", x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
				Draw.color(baseColor);
				Draw.rect(NewHorizon.NHNAME + "linked-arrow", 	 x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
			}
			Draw.reset();
		}
		
				  protected boolean isContiunous(){return ammoData.continuousTime > 0;}
		@Override public float handleDamage(float amount) {return amount * (1 - Mathf.clamp(baseData.defenceMPL * baseData.level, 0, maxDamageReduce));}
		
		@Override public boolean isConnected(){return baseData == null ? false : upgrader() != null;}
		@Override public UpgraderBlockBuild upgrader(){return baseData.from;}
		
	    @Override public UpgradeBaseData getBaseData(){return baseData;}
		@Override public UpgradeAmmoData getAmmoData(){return ammoData;}
    	
		@Override public void setBaseData(UpgradeBaseData data){this.baseData = data;}
		@Override public void setAmmoData(UpgradeAmmoData data){this.ammoData = data;}
	}
}









