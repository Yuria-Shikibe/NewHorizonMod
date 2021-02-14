package newhorizon.block.turrets;


import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHBullets;
import newhorizon.content.NHContent;
import newhorizon.content.NHUpgradeDatas;
import newhorizon.feature.UpgradeData;
import newhorizon.feature.UpgradeData.DataEntity;
import newhorizon.func.TableFuncs;
import newhorizon.func.TextureFilterValue;
import newhorizon.interfaces.Scalablec;
import newhorizon.interfaces.Upgraderc;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.func.TableFuncs.getPercent;

public class ScalableTurret extends Turret{
	public UpgradeData defaultData = NHUpgradeDatas.none;
	
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
	}
	
	@Override
    public void setStats(){
        super.setStats();
		stats.add(Stat.damage, defaultData.selectAmmo.damage, StatUnit.none);
		stats.add(Stat.input, new TextureFilterValue(NHContent.iconLevel, "[accent]Caution[]: Need be linked."));
    }

    @Override
    public void init(){
        consumes.powerCond(powerUse, TurretBuild::isActive);
        super.init();
	}
	
	public class ScalableTurretBuild extends TurretBuild implements Scalablec{
		protected DataEntity data = NHUpgradeDatas.none.newSubEntity();
		protected int fromPos = -1;

		public boolean shooting;
		
		Bullet bullet;
        float bulletLife;
		
		@Override public boolean shouldTurn(){return !shooting;}
		
		@Override
        public void shoot(BulletType ammo){
        	if(ammo.compareTo(NHBullets.none) == 0)return;
            useAmmo();

            tr.trns(rotation, size * tilesize / 2f);
		    getData().type().chargeBeginEffect.at(x + tr.x, y + tr.y, rotation);
		    getData().type().chargeEffect.at(x + tr.x, y + tr.y, rotation);
   
			if(getData().type().chargeTime > 0)shooting = true;

            Time.run(getData().type().chargeTime, () -> {
            	if(getData().type().burstSpacing > 0.0001f){
					for(int i = 0; i < getData().type().salvos; i++){
						Time.run(getData().type().burstSpacing * i, () -> {
							if(!isValid() || ammo.compareTo(getData().type().selectAmmo) != 0)return;
							tr.trns(rotation, (size * tilesize / 2f) - recoil, Mathf.range(getData().type().randX) );
							recoil = recoilAmount;
							heat = 2f;
							bullet(ammo, rotation + Mathf.range(getData().type().inaccuracy));
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
			getData().type().shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));

			if(shootShake > 0){
                Effect.shake(shootShake, shootShake, this);
			}

			recoil = recoilAmount;
		}

        
        @Override
        public BulletType peekAmmo(){
        	return getData() == null ? NHBullets.none : getData().type().selectAmmo == null ? NHBullets.none : getData().type().selectAmmo;
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
			Drawf.dashCircle(x, y, range(), baseColor);

			Lines.stroke(1f, getColor());
			Lines.square(x, y, block().size * tilesize / 2.0f + 1.0f);
			Draw.reset();

			drawConnected();
			if(upgraderc() != null)upgraderc().drawLink();
			drawMode();
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
			float realReload = getData().type().reloadTime <= 0 ? reloadTime : getData().type().reloadTime;
			return realReload * (1 + data.speedUP());
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
				bulletLife = getData().type().continuousTime;
			}else{
				float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;
				type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(getData().type().velocityInaccuracy), lifeScl);
			}
        }
		
		@Override public void resetUpgrade(){
			fromPos = -1;
			setData(NHUpgradeDatas.none.newSubEntity());
		}
		@Override public Color getColor(){return baseColor;}
		@Override public boolean isContiunous(){return getData().type().continuousTime > 0;}
		@Override public float handleDamage(float amount) {return amount * Mathf.clamp(1 - data.defenceUP());}
		@Override public boolean isConnected(){return upgraderc() != null;}
		@Override public Upgraderc upgraderc(){
			if(world.build(fromPos) == null){
				fromPos = -1;
				return null;
			}
			if(world.build(fromPos) != null && world.build(fromPos) instanceof Upgraderc)return (Upgraderc)world.build(fromPos);
			return null;
		}
	    @Override public DataEntity getData(){return data;}
		@Override public void setData(DataEntity data){
			this.data = data;
		}
		@Override public void setLinkPos(int i){
			this.fromPos = i;
		}
		@Override public void write(Writes write) {
			write.i(this.fromPos);
		}
		@Override public void read(Reads read, byte revision) {
			this.fromPos = read.i();
		}

		@Override
		public void buildConfiguration(Table t) {
			t.table(Tex.button, table -> {
				table.table(cont -> cont.image(getData().type().icon).left()).left().growX();
				table.table(cont -> {
					cont.add("[lightgray]Level: [accent]" + getData().level + "[]", Styles.techLabel).left().pad(TableFuncs.OFFSET).row();
					cont.image().fillX().pad(TableFuncs.OFFSET / 2).height(TableFuncs.OFFSET / 3).color(Color.lightGray).left().row();
					cont.add("[lightgray]ReloadSpeedUp: [accent]" + getPercent(data.speedUP())).left().row();
					cont.add("[lightgray]DefenceUP: [accent]" + getPercent(data.defenceUP())).left().row();
				}).growX().right().padRight(TableFuncs.OFFSET / 3);
			}).grow().padLeft(TableFuncs.OFFSET).padRight(TableFuncs.OFFSET).row();

			if(getData() != null && upgraderc() != null)upgraderc().buildSwitchAmmoTable(t, true);

		}
	}
}









