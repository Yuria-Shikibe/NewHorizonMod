package newhorizon.block.turrets;


import arc.audio.Sound;
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
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.Stat;
import newhorizon.content.NHBullets;
import newhorizon.content.NHContent;
import newhorizon.content.NHUpgradeDatas;
import newhorizon.feature.UpgradeData;
import newhorizon.feature.UpgradeData.DataEntity;
import newhorizon.func.TableFunc;
import newhorizon.func.TextureFilterValue;
import newhorizon.interfaces.ScalableBlockc;
import newhorizon.interfaces.Scalablec;
import newhorizon.interfaces.Upgraderc;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.func.TableFunc.getPercent;

public class ScalableTurret extends Turret implements ScalableBlockc{
	public UpgradeData defaultData = NHUpgradeDatas.none;
	public Block upgraderBlock = null;
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
    public void setStats(){
		stats.add(Stat.abilities, new TextureFilterValue(
				upgraderBlock != null && upgraderBlock.icon(Cicon.xlarge).found() ? upgraderBlock.icon(Cicon.xlarge) : NHContent.iconLevel,
				"[accent]Caution[gray]: Need be linked by [lightgray]<" + (upgraderBlock == null ? "UPGRADE BLOCK" : upgraderBlock.localizedName) + ">[gray] to function."
		));
		super.setStats();
    }

	@Override
    public void init(){
        consumes.powerCond(powerUse, TurretBuild::isActive);
        super.init();
	}
	
	@Override
	public void setLink(Block block){
		upgraderBlock = block;
	}
	
	public class ScalableTurretBuild extends TurretBuild implements Scalablec{
		protected DataEntity data = defaultData.newSubEntity();
		protected int fromPos = -1;
		
		Bullet bullet;
        float bulletLife;
        
		@Override
        public void shoot(BulletType ammo){
        	if(getData().type().selectAmmo == NHBullets.none)return;
            useAmmo();

            tr.trns(rotation, size * tilesize / 2f);
		    getData().type().chargeBeginEffect.at(x + tr.x, y + tr.y, rotation);
		    getData().type().chargeEffect.at(x + tr.x, y + tr.y, rotation);
		    
		    charging = true;
		    
		    if(getData().type().chargeTime > 0){
		    	Sound sound = getData().type().chargeSound == null ? chargeSound : getData().type().chargeSound;
			    sound.at(x + tr.x, y + tr.y, 1);
		    }
		    
            Time.run(getData().type().chargeTime, () -> {
	            if(!isValid())return;
	            charging = false;
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
			return consValid() && cons.optionalValid();
		}
		
		@Override
		public BlockStatus status(){
			if(hasAmmo())return BlockStatus.active;
			else if(!validateTarget())return BlockStatus.noInput;
			else return BlockStatus.noOutput;
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
		public float range(){
			return data.type().range < 0 ? range : data.type().range;
		}
		
		@Override
		protected void findTarget(){
			if(targetAir && !targetGround){
				target = Units.bestEnemy(team, x, y, range(), e -> !e.dead() && !e.isGrounded(), unitSort);
			}else{
				target = Units.bestTarget(team, x, y, range(), e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> true, unitSort);
			}
		}
		
		
		@Override
		public void updateTile(){
			if(charging)return;
			super.updateTile();
			if(isContiunous() && bulletLife > 0 && bullet != null){
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
            }
		}
		
		protected float reloadTime(){
			float realReload = getData().type().reloadTime <= 0 ? reloadTime : getData().type().reloadTime;
			return realReload * 1 / (1 + data.speedUP());
		}
		
		@Override
		protected void updateShooting(){
			if(isContiunous() && bulletLife > 0 && bullet != null){
                return;
            }

            if(reload >= reloadTime()){
                shoot(peekAmmo());

                reload = 0f;
            }else{
                reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            }
        }
		
		@Override
		protected void bullet(BulletType type, float angle){
			if(isContiunous()){
				bullet = type.create(tile.build, team, x + tr.x, y + tr.y, angle);
				bulletLife = getData().type().continuousTime;
			}else{
				float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range() / type.range()) : 1f;
				type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(getData().type().velocityInaccuracy), lifeScl);
			}
        }
		
		@Override public void resetUpgrade(){
			fromPos = -1;
			setData(defaultData.newSubEntity());
		}
		@Override public Color getColor(){return baseColor;}
		@Override public boolean isContiunous(){return getData().type().continuousTime > 0;}
		@Override public float handleDamage(float amount) {return Mathf.clamp(amount * (1 - data.defenceUP()), amount * data.type().maxDamageReduce, amount);}
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
			t.table(Tex.paneSolid, table -> {
				table.table(cont -> cont.image(getData().type().icon).left()).left().growX();
				table.table(cont -> {
					cont.add("[lightgray]Level: [accent]" + getData().level + "[]", Styles.techLabel).left().pad(TableFunc.OFFSET).row();
					cont.image().fillX().pad(TableFunc.OFFSET / 2).height(TableFunc.OFFSET / 3).color(Color.lightGray).left().row();
					cont.add("[lightgray]ReloadSpeedUp: [accent]" + getPercent(data.speedUP())).left().row();
					cont.add("[lightgray]DefenceUP: [accent]" + getPercent(data.defenceUP())).left().row();
				}).growX().right().padRight(TableFunc.OFFSET / 3);
			}).grow().row();

			if(getData() != null && upgraderc() != null)upgraderc().buildSwitchAmmoTable(t, true);

		}
	}
}









