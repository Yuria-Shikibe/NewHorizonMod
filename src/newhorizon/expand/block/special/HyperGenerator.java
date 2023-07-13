package newhorizon.expand.block.special;

import arc.Core;
import arc.audio.Sound;
import arc.func.Cons;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.ui.Bar;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;
import newhorizon.content.NHBullets;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.expand.bullets.EffectBulletType;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.tilesize;


public class HyperGenerator extends PowerGenerator{
	private static final float minWarmup = 0.075f;
	
	public TextureRegion bottomRegion, armorRegion, gateRegion;
	public TextureRegion[] plasmaRegions;
	public float warmupSpeed = 0.0015f;
	public float disabledSpeed = 0.015f;
	public float itemDuration = 360f;
	
	public float structureLim = 0.3f;
	public float beginDamageScl = 0.01f;
	public float destroyedExplodeLimit = 0.5f;
	
	public int updateLightning;
	public int updateLightningRand;
	public float lightningRange = 160f;
	public int lightningLen = 4;
	public int lightningLenRand = 8;
	public float lightningDamage = 120f;
	public int subNum = 1;
	public int subNumRand = 1;
	
	public Seq<StatusEffect> toApplyStatus = new Seq<>();
	public float statusRange = 480;
	public float statusDuration = 15 * 60;
	
	public Cons<HyperGeneratorBuild> explodeAction = entity -> {};
	public Cons<Position> explodeSub = entity -> {};
	public float explosionRadius;
	public float explosionDamage;
	
	public float maxVelScl = 1.25f, minVelScl = 0.75f;
	public float maxTimeScl = 1.25f, minTimeScl = 0.75f;
	
	public float plasmaScl = 0.975f;
	public float gateSize = 5f;
	public float effectCircleSize = -1;
	public float triWidth = 6f;
	public float triLength = 100f;
	public Effect updateEffect = NHFx.circle;
	public Effect workEffect = NHFx.line;
	public float updateEffectDiv = 20f;
	public float updateEffectSize = 20f;
	public float effectSinScl = 0.75f;
	public float effectSinMag = 0.11f;
	public Color effectColor = Color.white;
	
	public float workShake = 8f;
	public Sound workSound = Sounds.plasmaboom;
	
	protected BulletType destroyed;
	public float attract = 8f;
	
	public HyperGenerator(String name){
		super(name);
		this.hasPower = true;
		this.hasLiquids = true;
		this.liquidCapacity = 30.0F;
		this.hasItems = true;
		this.outputsPower = this.consumesPower = true;
		baseExplosiveness = 1000;
		this.explosionRadius = 220f;
		this.explosionDamage = 14000f;
		
		flags = EnumSet.of(BlockFlag.reactor, BlockFlag.generator);
	}
	
	public void setBars() {
		super.setBars();
		
		addBar("power", (HyperGeneratorBuild entity) -> new Bar(() ->
				Core.bundle.format("bar.poweroutput",
						Strings.fixed(Math.max(entity.getPowerProduction() - consPower.usage, 0) * 60 * entity.timeScale(), 1)),
				() -> Pal.powerBar,
				() -> entity.productionEfficiency));
	}
	
	public void setStats() {
		super.setStats();
		if (this.hasItems) {
			this.stats.add(Stat.productionTime, this.itemDuration / 60.0F, StatUnit.seconds);
		}
		
		stats.add(Stat.abilities, StatValues.statusEffects(toApplyStatus));
		stats.add(Stat.abilities, statusRange / tilesize, StatUnit.blocks);
		stats.add(Stat.abilities, statusDuration / 60f, StatUnit.seconds);
	}
	
	@Override
	public TextureRegion[] icons(){
		return this.teamRegion.found() && this.minfo.mod == null ? new TextureRegion[]{this.bottomRegion, this.region, this.teamRegions[Team.sharded.id], armorRegion} : new TextureRegion[]{this.bottomRegion, this.region, armorRegion};
	}
	
	@Override
	public void init(){
		super.init();
		if(effectCircleSize < 0)effectCircleSize = size * Vars.tilesize / 6f;
		clipSize = Math.max(clipSize, tilesize * size * 8f);
		destroyed = new EffectBulletType(600f){
			private final Effect updateEffect1, updateEffect2;
			
			{
				absorbable = hittable = false;
				speed = 0;
				lightningLen = lightningLenRand = 4;
				lightningDamage = HyperGenerator.this.lightningDamage / 2;
				lightning = 3;
				damage = splashDamage = lightningDamage / 1.5f;
				splashDamageRadius = 38f;
				
				hitColor = lightColor = lightningColor = effectColor;
				
				despawnEffect = NHFx.circleOut(hitColor, lightningRange * 1.5f);
				hitEffect = NHFx.collapserBulletExplode;
				updateEffect2 = NHFx.blast(hitColor, lightningRange / 2f);
				updateEffect1 = NHFx.circleOut(effectColor, lightningRange * 0.75f);
				
				hitShake = despawnShake = 80f;
				despawnSound = NHSounds.hugeBlast;
			}
			
			@Override
			public void init(Bullet b){
				super.init(b);
				Units.nearby(Tmp.r1.setCenter(b.x, b.y).setSize(lightningRange * 4), unit -> {
					unit.impulse(Tmp.v3.set(unit).sub(b.x, b.y).nor().scl(b.dst(unit) * unit.mass() / 160f));
				});
			}
			
			@Override
			public void draw(Bullet b){
				super.draw(b);
				float f = Mathf.curve(b.fout(), 0, 0.15f);
				float f2 = Mathf.curve(b.fin(), 0, 0.1f);
				Draw.color(effectColor);
				Fill.circle(b.x, b.y, size * tilesize / 3f * f);
				
				for(int i : Mathf.signs){
					Drawf.tri(b.x, b.y, triWidth * f2 * f, triLength * 1.3f * f2 * f, (i + 1) * 90 + Time.time * 2);
					Drawf.tri(b.x, b.y, triWidth * f2 * f, triLength * 1.3f * f2 * f, (i + 1) * 90 - Time.time * 2 + 90);
				}
				
				Draw.color(Color.black);
				Draw.z(Layer.effect + 0.01f);
				Fill.circle(b.x, b.y, size * tilesize / 5f * f);
				Draw.z(Layer.bullet);
				
				Drawf.light(b, lightningRange * 4f * b.fout(Interp.pow2Out), effectColor, 0.75f);
			}
			
			@Override
			public void update(Bullet b){
				super.update(b);
				Units.nearby(Tmp.r1.setCenter(b.x, b.y).setSize(lightningRange * 3f), unit -> {
					unit.impulse(Tmp.v3.set(unit).sub(b.x, b.y).nor().scl(-attract * 100.0f));
				});
				
				if(Mathf.chanceDelta((b.fin() * 3 + 1) / 4f * 0.65f)){
					NHFunc.randFadeLightningEffect(b.x, b.y, lightningRange * 1.5f, Mathf.random(12f, 20f), lightningColor, Mathf.chance(0.5));
				}
				
				if(Mathf.chanceDelta(0.2)){
					updateEffect2.at(b.x + Mathf.range(size * tilesize * 0.75f), b.y + Mathf.range(size * tilesize * 0.75f));
				}
				
				if(Mathf.chanceDelta(0.075)){
					updateEffect1.at(b.x + Mathf.range(size * tilesize), b.y + Mathf.range(size * tilesize));
				}
				
				Effect.shake(10f, 30f, b);
				
				if(b.timer(3, 8))PosLightning.createRange(b, b, Team.derelict, lightningRange * 2f, 255, effectColor, true, lightningDamage, subNum + Mathf.random(subNumRand), PosLightning.WIDTH,updateLightning + Mathf.random(updateLightningRand), point -> {
					NHFx.lightningHitSmall.at(point);
					Damage.damage(point.getX(), point.getY(), splashDamageRadius, splashDamage);
				});
				
				if(b.timer(4, 5)){
					float range = size * Vars.tilesize / 1.5f;
					NHFx.hyperExplode.at(b.x + Mathf.range(range), b.y + Mathf.range(range), effectColor);
					Sounds.explosionbig.at(b);
					NHBullets.hyperBlast.create(b, Team.derelict, b.x, b.y, Mathf.random(360), NHBullets.hyperBlast.damage * baseExplosiveness, Mathf.random(minVelScl, maxVelScl), Mathf.random(minTimeScl, maxTimeScl), new Object());
				}

				if(b.timer(5, 8)){
					float range = size * Vars.tilesize / 1.5f;
					NHFx.hitSparkLarge.at(b.x + Mathf.range(range), b.y + Mathf.range(range), effectColor);
					NHBullets.hyperBlastLinker.create(b, Team.derelict, b.x, b.y, Mathf.random(360), NHBullets.hyperBlast.damage * baseExplosiveness, Mathf.random(minVelScl, maxVelScl), Mathf.random(minTimeScl, maxTimeScl), new Object());
				}
			}
			
			@Override
			public void despawned(Bullet b){
				super.despawned(b);
				Units.nearby(Tmp.r1.setCenter(b.x, b.y).setSize(lightningRange * 4), unit -> {
					unit.vel.set(Tmp.v1.set(unit).sub(b).nor().scl(6));
					unit.kill();
				});
				
				for(int i = 0; i < 7; ++i) {
					Time.run((float)Mathf.random(80), () -> {
						NHFx.hyperExplode.at(b.x + Mathf.range(size * Vars.tilesize), b.y + Mathf.range(size * Vars.tilesize), effectColor);
						NHFx.hyperCloud.at(b.x + Mathf.range(size * Vars.tilesize), b.y + Mathf.range(size * Vars.tilesize), effectColor);
						NHFx.circle.at(b.x + Mathf.range(size * Vars.tilesize), b.y + Mathf.range(size * Vars.tilesize), explosionRadius, effectColor);
					});
				}
			}
		};
	}
	
	
	@Override
	public void load(){
		super.load();
		bottomRegion = Core.atlas.find(name + "-bottom");
		armorRegion = Core.atlas.find(name + "-armor");
		gateRegion = Core.atlas.find(name + "-gate");
		plasmaRegions = new TextureRegion[4];
		
		for(int index = 0; index < 4; ++index) {
			plasmaRegions[index] = Core.atlas.find(name + "-plasma-" + index);
		}
	}
	
	public class HyperGeneratorBuild extends GeneratorBuild{
		public float progress;
		public float warmup;
		
		
		@Override
		public void updateTile(){
			super.updateTile();
			if(efficiency > 0){
				if (this.timer(0, itemDuration / edelta())) {
					if(warmup > destroyedExplodeLimit){
						workSound.at(this, Mathf.random(0.9f, 1.1f));
						NHFx.hyperInstall.at(x, y, effectCircleSize / 1.5f * (warmup + 0.3f), effectColor);
						Effect.shake(workShake, workShake, this);
						if(Mathf.chanceDelta(warmup / 2))PosLightning.createRandomRange(Team.derelict, this, lightningRange, effectColor, true, lightningDamage * Math.max(Mathf.curve(1 - health / maxHealth(), structureLim, 1f) + beginDamageScl, 0.001f), lightningLen + Mathf.random(lightningLenRand), PosLightning.WIDTH, subNum + Mathf.random(subNumRand),updateLightning + Mathf.random(updateLightningRand), point -> {
							NHFx.lightningHitLarge.at(point.getX(), point.getY(), effectColor);
						});
						
						if(optionalEfficiency > 0){
							Units.nearby(team, x, y, statusRange, u -> {
								Fx.chainLightning.at(x, y, 0, effectColor, u);
								toApplyStatus.each(s -> {
									u.apply(s, statusDuration);
								});
								
								u.healFract(2.5f);
							});
						}
					}
					
					this.consume();
				}
				progress += efficiency() * Time.delta;
				if(Mathf.equal(warmup, 1.0F, 0.0015F)){
					warmup = 1.0F;
				}else warmup = Mathf.lerpDelta(warmup, 1, warmupSpeed + (Mathf.num(Vars.net.active()) * 0.025f));
			}else{
				if(Mathf.equal(warmup, 0F, 0.0015F)){
					warmup = 0F;
				}else warmup = Mathf.lerpDelta(warmup, 0, disabledSpeed);
			}
			productionEfficiency = Mathf.pow(warmup, 5.0F);
			
			if(warmup > minWarmup){
				for(int i : Mathf.signs){
					if(Mathf.chance(warmup / updateEffectDiv))updateEffect.at(x + i * Mathf.random(effectCircleSize), y + i * Mathf.random(effectCircleSize), updateEffectSize * warmup, effectColor);
				}
				if(Mathf.chanceDelta( Mathf.curve(1 - health / maxHealth(), structureLim, 1f) / 25f)){
					PosLightning.createRandomRange(Team.derelict, this, lightningRange, effectColor, true, lightningDamage * (Mathf.curve(1 - health / maxHealth(), structureLim, 1f) + beginDamageScl), lightningLen + Mathf.random(lightningLenRand), PosLightning.WIDTH, subNum + Mathf.random(subNumRand),updateLightning + Mathf.random(updateLightningRand), point -> {
						NHFx.lightningHitLarge.at(point.getX(), point.getY(), effectColor);
					});
				}
				if(Mathf.chanceDelta(warmup / updateEffectDiv * 1.5f)) workEffect.at(x, y, updateEffectSize * 3f * warmup, effectColor);
				if(Mathf.chanceDelta(warmup / updateEffectDiv * 3f)){
					Tmp.v1.rnd(size * tilesize * warmup * 0.9f).add(tile);
					NHFx.chainLightningFade.at(x, y, 12f, effectColor, Tmp.v1.cpy());
				}
				if(Mathf.chanceDelta(warmup / updateEffectDiv * 2f)){
					Tmp.v1.rnd(size * tilesize * warmup * 1.5f).add(tile);
					NHFx.chainLightningFadeReversed.at(x, y, 12f, effectColor, Tmp.v1.cpy());
				}
			}
		}
		
		@Override
		public void draw(){
			super.draw();
			Color drawColor = Tmp.c1.set(effectColor).lerp(Color.white, Mathf.absin(Time.time * 1.3f, 1.0F, 0.08F));
			
			Draw.rect(bottomRegion, x, y);
			
			for(int i = 0; i < plasmaRegions.length; ++i) {
				float r = (size * Vars.tilesize - 3.0F + Mathf.absin(Time.time, 2.0F + i, 5.0F - i * 0.5F)) * plasmaScl;
				Draw.color(effectColor, drawColor, (float)(i / plasmaRegions.length));
				Draw.alpha((0.25F + Mathf.absin(Time.time, 2.0F + i * 2.0F, 0.3F + i * 0.05F)) * warmup);
				Draw.blend(Blending.additive);
				Draw.rect(plasmaRegions[i], x, y, r, r, Time.time * (12.0F + i * 6.0F) * warmup);
				Draw.blend();
			}
			Draw.color();
			
			for(int i = 0; i < 4; i++){
				for(int j : Mathf.signs){
					Tmp.v1.trns(90 * i + 45, -(1 - j) * size * Vars.tilesize * Draw.scl * Mathf.sqrt2);
					Tmp.v2.trns(90 * i - 45 , -gateSize * j * warmup).add(Tmp.v1);
					TextureRegion gate = new TextureRegion(gateRegion);
					gate.flip(j < 0, j < 0);
					Draw.rect(gate, x + Tmp.v2.x, y + Tmp.v2.y, i * 90);
				}
			}
			
			Draw.rect(region, x, y);
			Draw.rect(armorRegion, x, y);
			
			if(warmup > minWarmup){
				float drawSin = (1 + Mathf.absin(progress, effectSinScl * 2f, effectSinMag * 1.1f)) * warmup;
				float drawSin2 = (1 + Mathf.absin(progress, effectSinScl, effectSinMag)) * warmup;
				float drawSin3 = (1 + Mathf.absin(progress + effectSinScl / 2, effectSinScl, effectSinMag)) * warmup;
				
				Draw.z(Layer.effect - 1f);
				Draw.color(drawColor);
				
				
				
				Fill.circle(x, y, effectCircleSize * drawSin);
				for(int i : Mathf.signs){
					Drawf.tri(x, y, triWidth * warmup, triLength * drawSin2, (i + 1) * 90 + progress);
					Drawf.tri(x, y, triWidth * warmup * 0.8f, triLength * drawSin3 * 0.8f, (i + 1) * 90 - progress * 1.1f + 90);
				}
				
				Lines.stroke(warmup * triWidth * 0.55f);
				DrawFunc.circlePercentFlip(x, y, size * tilesize * 0.85f * (1 + Mathf.absin(progress * 2f, 24f, 0.125f)) * warmup, progress * 0.85f, 30f);
				
				Lines.stroke(warmup * triWidth * 0.35f);
				DrawFunc.circlePercentFlip(x, y, size * tilesize * 1.1f * (1 + Mathf.absin(progress * 1.25f, 24f, 0.125f)) * warmup, progress * 0.95f + 5f, 45f);
			}
			Draw.reset();
			
			Drawf.light(tile, size * tilesize * 4 * warmup, effectColor, 0.95f);
		}
		
		@Override
		public void onDestroyed(){
			super.onDestroyed();
			if(warmup < destroyedExplodeLimit)return;
			explodeAction.get(this);
			int i;
			
			destroyed.create(this, Team.derelict, x, y, 0);
			
			for(i = 0; i < 30; i++){
				Time.run(Mathf.random(80f), () -> {
					explodeSub.get(this);
					Sounds.bang.at(this);
					Sounds.explosionbig.at(this);
//					NHBullets.hyperBlast.create(this, Team.derelict, x, y, Mathf.random(360), NHBullets.hyperBlast.damage * baseExplosiveness, Mathf.random(minVelScl, maxVelScl), Mathf.random(minTimeScl, maxTimeScl), new Object());
				});
			}
			
			for(i = 0; i < 10; i++){
				Time.run(i * (3 + Mathf.random(2f)), () -> {
					explodeSub.get(this);
					Sounds.explosionbig.at(this);
					PosLightning.createRandomRange(Team.derelict, this, lightningRange * 3f, effectColor, true, lightningDamage, lightningLen + Mathf.random(lightningLenRand), PosLightning.WIDTH, subNum + Mathf.random(subNumRand),updateLightning + Mathf.random(updateLightningRand), point -> {
						NHFx.lightningHitLarge.at(point.getX(), point.getY(), effectColor);
					});
				});
			}
			
			Sounds.explosionbig.at(this);
			Effect.shake(6.0F, 16.0F, x, y);
			
			for(i = 0; i < 7; ++i) {
				Time.run((float)Mathf.random(80), () -> {
					NHFx.hyperExplode.at(x + Mathf.range(size * Vars.tilesize), y + Mathf.range(size * Vars.tilesize), effectColor);
					NHFx.hyperCloud.at(x + Mathf.range(size * Vars.tilesize), y + Mathf.range(size * Vars.tilesize), effectColor);
					NHFx.circle.at(x + Mathf.range(size * Vars.tilesize), y + Mathf.range(size * Vars.tilesize), explosionRadius, effectColor);
				});
			}
			
			Damage.damage(x, y, explosionRadius, explosionDamage);
		}
		
		public float ambientVolume() {
			return this.warmup;
		}
		
		@Override
		public void drawLight(){
			Drawf.light(this.x, this.y, (110.0F + Mathf.absin(5.0F, 5.0F)) * this.warmup, effectColor, 0.8F * this.warmup);
		}
		
		@Override
		public void drawSelect(){
			super.drawSelect();
			Drawf.dashCircle(x, y, statusRange, team.color);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			this.warmup = read.f();
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.f(warmup);
		}
	}
}
