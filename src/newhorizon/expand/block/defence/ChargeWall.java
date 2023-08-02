package newhorizon.expand.block.defence;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Posc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.type.Category;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.expand.bullets.AdaptedSapBulletType;
import newhorizon.util.feature.PosLightning;

import static mindustry.Vars.tilesize;

public class ChargeWall extends Block{
	public TextureRegion heatRegion, lightRegion;
	public float maxEnergy = size * size * 3000;
	public float maxHeat = size * size * 400;
	public float heatPerRise = 50f;
	public float healLightStMin = 0.35f;
	
	public float healReloadTime = 75f,
				 healPerEnr = 55f,
				 healPercent = size;
				
	public float shootReloadTime = 20f,
				 shootPerEnr = 80f,
				 shootDamage = 50f;
				
	public float coolingReloadTime = 30f;
	
	public float chargeCoefficient = 1.1f;
				
	public float range = 200f;
	public int lightningActHits = size * size * 4;
	
	public Color effectColor = NHColor.lightSkyBack;
	public Effect
		chargeActEffect = NHFx.circleSplash,
		hitEffect = NHFx.circleSplash,
		shootEffect = NHFx.circleSplash,
		onDestroyedEffect = Fx.none;
	
	public BulletType releaseType = new AdaptedSapBulletType() {
		{
			damage = shootDamage;
			status = StatusEffects.none;
			sapStrength = 0.45f;
			length = range;
			drawSize = range * 2;
			hitColor = color = effectColor;
			despawnEffect = shootEffect = Fx.none;
			width = 0.62f;
			lifetime = 35f;
		}
	};
	
	Cons<ChargeWallBuild> maxChargeAct = tile -> {
		chargeActEffect.at(tile.x, tile.y, effectColor);
		
		PosLightning.createRandom(tile.team, tile, tile.range(), effectColor, true, shootDamage, 8, PosLightning.WIDTH, 3, p -> {
			NHFx.lightningHitSmall.at(tile.x, tile.y, effectColor);
		});
	};
	Cons<ChargeWallBuild> destroyAct = tile -> {
		onDestroyedEffect.at(tile.x, tile.y, effectColor);
		
		PosLightning.createRandomRange(tile.team, tile, tile.range(), effectColor, true, shootDamage * 3, 10, PosLightning.WIDTH, 3, 8, p -> {
			NHFx.lightningHitLarge.at(tile.x, tile.y, effectColor);
		});
	};
	Cons<ChargeWallBuild> closestTargetAct = tile -> PosLightning.create(tile, tile.team, tile, tile.target, effectColor, true, shootDamage, 4, PosLightning.WIDTH, 2, target ->{
		hitEffect.at(target.getX(), target.getY(), tile.angleTo(target), effectColor);
		shootEffect.at(tile.x, tile.y, effectColor);
		releaseType.create(tile, tile.team, tile.x, tile.y, tile.angleTo(target));
	});
	
	public ChargeWall(String name){
		super(name);
		update = true;
		
		category = Category.defense;
		
		solid = true;
		destructible = true;
		group = BlockGroup.walls;
		buildCostMultiplier = 3f;
		canOverdrive = false;
		attacks = true;
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
	}
	
	@Override
	public void setBars(){
		super.setBars();
		addBar("Energy",
			(ChargeWallBuild entity) -> new Bar(
				() -> "Energy",
				() -> NHColor.lightSkyBack,
				() -> entity.energy / maxEnergy
			)
		);
		
		addBar("Heat",
			(ChargeWallBuild entity) -> new Bar(
				() -> "Heat",
				() -> Color.valueOf("#FF732A"),
				() -> entity.heat / maxHeat
			)
		);
	}
	
	@Override
	public void load(){
		super.load();
		heatRegion  = Core.atlas.find(name + "-heat");
		lightRegion = Core.atlas.find(name + "-light");
	}
	
	public class ChargeWallBuild extends Building implements Ranged{
		public float energy;
		public float healReload;
		public float dmgScl = 1;
		public float heat;
		public float shootHeat;
		public float shootReload;
		public float coolingReload;
		
		public Posc target;
		
		@Override
		public float range(){return range;}
		
		@Override
		public float handleDamage(float amount) {return amount * dmgScl;}
		
		@Override
		public boolean collision(Bullet other) {
			float dmg = other.damage() * other.type().buildingDamageMultiplier;
			this.damage(dmg);
			energy += chargeCoefficient * dmg * dmgScl;
			return true;
		}
		
		@Override
		public void updateTile() {
			coolingReload += Time.delta;
			if(coolingReload > coolingReloadTime)updateCooling();
			shootHeat = Mathf.lerpDelta(shootHeat, 0f, 0.015f);
			if (timer(0, 20)) {
				findTarget();
			}
			if (!validateTarget()) target = null;
			else if(energy > shootPerEnr)updateShooting();
			fixEnr();
			updateHealTile();
			if(energy > maxEnergy){
				maxChargeAct.get(this);
				heatRise();
			}
			
			if(heat > (maxHeat * healLightStMin)){
				if(Mathf.chance(0.15f * Time.delta)){
					Fx.reactorsmoke.at(tile.x + Mathf.range(size * tilesize / 2), tile.y + Mathf.range(size * tilesize / 2));
				}
			}
		}
		
		@Override
		public void onDestroyed() {
			super.onDestroyed();
			destroyAct.get(this);
			if(target != null)closestTargetAct.get(this);
		}
		
		protected void updateShooting(){
			coolingReload = 0f;
			if(shootReload < shootReloadTime){
				shootReload += Time.delta;
			}else{
				energy -= shootPerEnr;
				shootReload = 0f;
				shootHeat = 1f;
				closestTargetAct.get(this);
			}
		}
		
		protected void updateCooling(){
			energy = Mathf.lerpDelta(energy, 0f, 0.015f);
			heat = Mathf.lerpDelta(heat, 0f, 0.015f);
		}
		
		protected void findTarget(){
			target = Units.closestTarget(team, x, y, range());
		}
		
		protected boolean validateTarget() {
			return !Units.invalidateTarget(target, team, x, y);
		}
		
		protected void fixEnr(){if(energy < 0)energy = 0f;}
		
		protected void updateHealTile(){
			if(energy < healPerEnr)return;
			if(healReload < healReloadTime){
				healReload += Time.delta;
			}else{
				energy -= healPerEnr;
				healReload = 0f;
				Fx.healBlockFull.at(x, y, block.size, effectColor);
				if(healthf() > 0.975f)this.heat -= healPercent * maxHeat;
				this.heal(healPercent * this.maxHealth);
			}
		}
		
		protected void heatRise(){
			energy = 0f;
			if(heat < maxHeat){heat += heatPerRise;}
			else {
				onDestroyed();
				kill();
			}
		}
		
		@Override
		public void draw(){
			Draw.rect(region, x, y);
			Draw.z(Layer.bullet);
			Draw.color(effectColor);
			Draw.alpha(energy / maxEnergy * 4);
			Draw.rect(lightRegion, x, y);
			Draw.reset();
			if(heat > maxHeat * healLightStMin){
				Draw.blend(Blending.additive);
				float flash = 1f + ((heat - maxHeat * healLightStMin) / (1f - maxHeat * healLightStMin)) * 5.4f;
                flash += flash * Time.delta;
                Draw.color(Color.red, Color.yellow, Mathf.absin(flash, 9f, 1f));
                Draw.alpha(0.6f);
                Draw.rect(heatRegion, x, y);
            }
            Draw.blend();
            Draw.reset();
		}
		
		@Override
		public void drawSelect() {
			Drawf.dashCircle(x, y, range(), team.color);
		}
		
		
	}
}